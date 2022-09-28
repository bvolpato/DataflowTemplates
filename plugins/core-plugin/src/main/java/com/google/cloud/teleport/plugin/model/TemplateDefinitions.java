/*
 * Copyright (C) 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.plugin.model;

import com.google.cloud.teleport.metadata.Template;
import com.google.cloud.teleport.metadata.TemplateParameter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.beam.repackaged.core.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * POJO class that wraps the pair of a {@link Class} and the {@link Template} annotation that
 * represent a template.
 */
public class TemplateDefinitions {

  private static final Logger LOG = LoggerFactory.getLogger(TemplateDefinitions.class);

  private static final Class<? extends Annotation>[] PARAMETER_ANNOTATIONS =
      new Class[] {
        TemplateParameter.BigQueryTable.class,
        TemplateParameter.Boolean.class,
        TemplateParameter.DateTime.class,
        TemplateParameter.Duration.class,
        TemplateParameter.Enum.class,
        TemplateParameter.GcsReadFile.class,
        TemplateParameter.GcsReadFolder.class,
        TemplateParameter.GcsWriteFile.class,
        TemplateParameter.GcsWriteFolder.class,
        TemplateParameter.Integer.class,
        TemplateParameter.KmsEncryptionKey.class,
        TemplateParameter.Long.class,
        TemplateParameter.Password.class,
        TemplateParameter.PubsubSubscription.class,
        TemplateParameter.PubsubTopic.class,
        TemplateParameter.Text.class
      };

  /** Options that don't need annotations (i.e., from generic parameters). */
  private static final Set<String> IGNORED_FIELDS = Set.of("as");

  /**
   * List of the classes that declare product-specific options. Methods in those classes will not
   * require the usage of @TemplateParameter.
   */
  private static final Set<String> IGNORED_DECLARING_CLASSES = Set.of("Object");

  private Class<?> templateClass;
  private Template templateAnnotation;

  public TemplateDefinitions(Class<?> templateClass, Template templateAnnotation) {
    this.templateClass = templateClass;
    this.templateAnnotation = templateAnnotation;
  }

  public Class<?> getTemplateClass() {
    return templateClass;
  }

  public Template getTemplateAnnotation() {
    return templateAnnotation;
  }

  public boolean isClassic() {
    return templateAnnotation.flexContainerName() == null
        || templateAnnotation.flexContainerName().isEmpty();
  }

  public boolean isFlex() {
    return !isClassic();
  }

  public ImageSpec buildSpecModel(boolean validateFlag) {

    ImageSpec imageSpec = new ImageSpec();
    imageSpec.setDefaultEnvironment(Map.of());
    imageSpec.setImage("gcr.io/{project-id}/" + templateAnnotation.flexContainerName());

    SdkInfo sdkInfo = new SdkInfo();
    sdkInfo.setLanguage("JAVA");
    imageSpec.setSdkInfo(sdkInfo);

    ImageSpecMetadata metadata = new ImageSpecMetadata();
    metadata.setName(templateAnnotation.name());
    metadata.setDescription(templateAnnotation.description());

    if (isClassic()) {

      if (templateAnnotation.placeholderClass() != null
          && templateAnnotation.placeholderClass() != void.class) {
        metadata.setMainClass(templateAnnotation.placeholderClass().getName());
      } else {
        metadata.setMainClass(templateClass.getName());
      }
    }

    LOG.info(
        "Processing template for class {}. Template name: {}",
        templateClass,
        templateAnnotation.name());

    List<MethodDefinitions> methodDefinitions = new ArrayList<>();

    int order = 0;
    Map<Class<?>, Integer> classOrder = new HashMap<>();

    Class<?> optionsClass = templateAnnotation.optionsClass();

    if (templateAnnotation.optionsOrder() != null) {
      for (Class<?> options : templateAnnotation.optionsOrder()) {
        classOrder.putIfAbsent(options, order++);
      }
    }

    classOrder.putIfAbsent(optionsClass, order++);

    Method[] methods = optionsClass.getMethods();
    for (Method method : methods) {
      method.setAccessible(true);

      classOrder.putIfAbsent(method.getDeclaringClass(), order++);

      Annotation parameterAnnotation = getParameterAnnotation(method);
      if (parameterAnnotation == null) {

        // Ignore non-annotated params in this criteria
        if (method.getName().startsWith("set")
            || IGNORED_FIELDS.contains(method.getName())
            || method.getDeclaringClass().getName().startsWith("org.apache.beam.sdk")
            || method.getDeclaringClass().getName().startsWith("org.apache.beam.runners")
            || IGNORED_DECLARING_CLASSES.contains(method.getDeclaringClass().getSimpleName())) {
          continue;
        }

        LOG.warn(
            "Method {} (declared at {}) does not have an annotation",
            method.getName(),
            method.getDeclaringClass().getName());

        if (validateFlag) {
          throw new IllegalArgumentException(
              "Method " + method.getName() + " does not have a @TemplateParameter annotation.");
        }
        continue;
      }

      methodDefinitions.add(new MethodDefinitions(method, parameterAnnotation, classOrder));
    }

    Collections.sort(methodDefinitions);

    for (MethodDefinitions method : methodDefinitions) {

      Annotation parameterAnnotation = method.getTemplateParameter();

      ImageSpecParameter parameter = new ImageSpecParameter();
      parameter.setName(
          StringUtils.uncapitalize(method.getDefiningMethod().getName().replaceFirst("^get", "")));
      parameter.processParamType(parameterAnnotation);

      if (!method.getDefiningMethod().getName().equalsIgnoreCase("get" + parameter.getName())) {
        LOG.warn(
            "Name for the method and annotation do not match! {} vs {}",
            method.getDefiningMethod().getName(),
            parameter.getName());
      }

      if (Set.of(templateAnnotation.skipOptions()).contains(parameter.getName())) {
        continue;
      }

      metadata.getParameters().add(parameter);
    }

    imageSpec.setMetadata(metadata);

    return imageSpec;
  }

  public Annotation getParameterAnnotation(Method method) {

    for (Class<? extends Annotation> annotation : PARAMETER_ANNOTATIONS) {
      if (method.getAnnotation(annotation) != null) {
        return method.getAnnotation(annotation);
      }
    }
    // Annotation[] annotations = method.getAnnotations();
    //
    // for (Annotation annotation : annotations) {
    //   if (annotation.annotationType().getName().contains("TemplateParameter")
    //       || annotation.getClass().getName().contains("TemplateParameter")) {
    //     return annotation;
    //   }
    // }

    return null;
  }
}

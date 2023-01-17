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
import com.google.cloud.teleport.metadata.TemplateCreationParameter;
import com.google.cloud.teleport.metadata.TemplateCreationParameters;
import com.google.cloud.teleport.metadata.TemplateParameter;
import com.google.cloud.teleport.metadata.options.DefaultTemplateOptions;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.beam.repackaged.core.org.apache.commons.lang3.StringUtils;
import org.apache.beam.sdk.options.Default;
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
        TemplateParameter.ProjectId.class,
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

    Set<String> parameterNames = new HashSet<>();

    Method[] methods = optionsClass.getMethods();
    for (Method method : methods) {
      method.setAccessible(true);

      classOrder.putIfAbsent(method.getDeclaringClass(), order++);

      Annotation parameterAnnotation = getParameterAnnotation(method);
      if (parameterAnnotation == null) {

        boolean runtime = false;

        TemplateCreationParameters creationParameters =
            method.getAnnotation(TemplateCreationParameters.class);
        String methodName = method.getName();
        if (creationParameters != null) {
          for (TemplateCreationParameter creationParameterCandidate : creationParameters.value()) {

            if (creationParameterCandidate.template().equals(templateAnnotation.name())
                || StringUtils.isEmpty(creationParameterCandidate.template())) {
              runtime = true;

              if (StringUtils.isNotEmpty(creationParameterCandidate.value())) {
                metadata
                    .getRuntimeParameters()
                    .put(
                        getParameterNameFromMethod(methodName), creationParameterCandidate.value());
              }
            }
          }
        }

        TemplateCreationParameter creationParameter =
            method.getAnnotation(TemplateCreationParameter.class);
        if (creationParameter != null) {
          runtime = true;

          if (StringUtils.isNotEmpty(creationParameter.value())) {
            metadata
                .getRuntimeParameters()
                .put(getParameterNameFromMethod(methodName), creationParameter.value());
          }
        }

        // Ignore non-annotated params in this criteria
        if (runtime
            || methodName.startsWith("set")
            || IGNORED_FIELDS.contains(methodName)
            || method.getDeclaringClass().getName().startsWith("org.apache.beam.sdk")
            || method.getDeclaringClass().getName().startsWith("org.apache.beam.runners")
            || IGNORED_DECLARING_CLASSES.contains(method.getDeclaringClass().getSimpleName())) {
          continue;
        }

        LOG.warn(
            "Method {} (declared at {}) does not have an annotation",
            methodName,
            method.getDeclaringClass().getName());

        if (validateFlag && method.getAnnotation(Deprecated.class) == null) {
          throw new IllegalArgumentException(
              "Method "
                  + method.getDeclaringClass().getName()
                  + "."
                  + methodName
                  + "() does not have a @TemplateParameter annotation (and not deprecated).");
        }
        continue;
      }

      methodDefinitions.add(new MethodDefinitions(method, parameterAnnotation, classOrder));
    }

    Set<String> skipOptionsSet = Set.of(templateAnnotation.skipOptions());
    Collections.sort(methodDefinitions);

    for (MethodDefinitions method : methodDefinitions) {
      Annotation parameterAnnotation = method.getTemplateParameter();
      ImageSpecParameter parameter =
          getImageSpecParameter(
              method.getDefiningMethod().getName(),
              method.getDefiningMethod(),
              parameterAnnotation);

      if (skipOptionsSet.contains(parameter.getName())) {
        continue;
      }

      if (parameterNames.add(parameter.getName())) {
        metadata.getParameters().add(parameter);
      } else {
        LOG.warn(
            "Parameter {} was already added for the Template {}, skipping repetition.",
            parameter.getName(),
            templateAnnotation.name());
      }
    }

    for (Field defaultOption : DefaultTemplateOptions.class.getDeclaredFields()) {
      Annotation parameterAnnotation = getParameterAnnotation(defaultOption);
      if (parameterAnnotation == null) {
        continue;
      }
      ImageSpecParameter parameter =
          getImageSpecParameter(defaultOption.getName(), defaultOption, parameterAnnotation);

      if (parameterNames.add(defaultOption.getName())) {
        metadata.getParameters().add(parameter);
      }
    }

    imageSpec.setMetadata(metadata);

    return imageSpec;
  }

  private ImageSpecParameter getImageSpecParameter(
      String originalName, AccessibleObject target, Annotation parameterAnnotation) {
    ImageSpecParameter parameter = new ImageSpecParameter();
    parameter.setName(getParameterNameFromMethod(originalName));
    parameter.processParamType(parameterAnnotation);

    Object defaultValue = getDefault(target);
    String helpText = parameter.getHelpText();
    if (defaultValue != null && !helpText.toLowerCase().contains("default")) {
      if (!helpText.endsWith(".")) {
        helpText += ".";
      }
      helpText += " Defaults to: " + defaultValue;
      parameter.setHelpText(helpText);
    }

    if (!originalName.equalsIgnoreCase("get" + parameter.getName())) {
      LOG.warn(
          "Name for the method and annotation do not match! {} vs {}",
          originalName,
          parameter.getName());
    }
    return parameter;
  }

  /** This method is inspired by {@code org.apache.beam.sdk.options.PipelineOptionsReflector}. */
  private String getParameterNameFromMethod(String originalName) {
    String methodName;
    if (originalName.startsWith("is")) {
      methodName = originalName.substring(2);
    } else if (originalName.startsWith("get")) {
      methodName = originalName.substring(3);
    } else {
      methodName = originalName;
    }
    return Introspector.decapitalize(methodName);
  }

  private Object getDefault(AccessibleObject definingMethod) {

    if (definingMethod.getAnnotation(Default.String.class) != null) {
      return definingMethod.getAnnotation(Default.String.class).value();
    }
    if (definingMethod.getAnnotation(Default.Boolean.class) != null) {
      return definingMethod.getAnnotation(Default.Boolean.class).value();
    }
    if (definingMethod.getAnnotation(Default.Character.class) != null) {
      return definingMethod.getAnnotation(Default.Character.class).value();
    }
    if (definingMethod.getAnnotation(Default.Byte.class) != null) {
      return definingMethod.getAnnotation(Default.Byte.class).value();
    }
    if (definingMethod.getAnnotation(Default.Short.class) != null) {
      return definingMethod.getAnnotation(Default.Short.class).value();
    }
    if (definingMethod.getAnnotation(Default.Integer.class) != null) {
      return definingMethod.getAnnotation(Default.Integer.class).value();
    }
    if (definingMethod.getAnnotation(Default.Long.class) != null) {
      return definingMethod.getAnnotation(Default.Long.class).value();
    }
    if (definingMethod.getAnnotation(Default.Float.class) != null) {
      return definingMethod.getAnnotation(Default.Float.class).value();
    }
    if (definingMethod.getAnnotation(Default.Double.class) != null) {
      return definingMethod.getAnnotation(Default.Double.class).value();
    }
    if (definingMethod.getAnnotation(Default.Enum.class) != null) {
      return definingMethod.getAnnotation(Default.Enum.class).value();
    }

    return null;
  }

  public Annotation getParameterAnnotation(AccessibleObject accessibleObject) {

    for (Class<? extends Annotation> annotation : PARAMETER_ANNOTATIONS) {
      if (accessibleObject.getAnnotation(annotation) != null) {
        return accessibleObject.getAnnotation(annotation);
      }
    }

    return null;
  }
}

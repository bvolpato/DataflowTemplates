/*
 * Copyright (C) 2019 Google LLC
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
package com.google.cloud.teleport.v2.utils;

import com.google.cloud.teleport.metadata.TemplateParameter;
import com.google.cloud.teleport.metadata.util.MetadataUtils;
import com.google.re2j.Pattern;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.apache.beam.sdk.options.PipelineOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class provides common utility methods for validating Templates Metadata during runtime. */
public class MetadataValidator {
  private static final Logger LOG = LoggerFactory.getLogger(MetadataValidator.class);

  private MetadataValidator() {}

  /**
   * Validates a given instance of {@link PipelineOptions}, and reports divergences for invalid
   * parameter usage.
   *
   * <pre>Status: for now, errors/warnings will be provided but no exceptions thrown. This behavior
   * might change in the future.
   *
   * @param options Options instance to validate.
   */
  public static void validate(PipelineOptions options) {

    for (Method method : options.getClass().getMethods()) {

      Annotation parameterAnnotation = MetadataUtils.getParameterAnnotation(method);
      if (parameterAnnotation == null) {
        continue;
      }

      List<String> regexes = getRegexes(parameterAnnotation);
      if (regexes == null) {
        LOG.info(
            "Validation regex for method {} ({}) not specified.",
            method.getName(),
            parameterAnnotation.getClass().getSimpleName());
        continue;
      }

      for (String regex : regexes) {

        try {
          Object objectValue = method.invoke(options);
          if (objectValue == null || objectValue.toString().isEmpty()) {
            continue;
          }

          Pattern pattern = Pattern.compile(regex);
          if (!pattern.matches(objectValue.toString())) {
            LOG.warn(
                "Parameter {} ({}) not matching the expected format: {}",
                MetadataUtils.getParameterNameFromMethod(method.getName()),
                parameterAnnotation.getClass().getSimpleName(),
                regex);
          }

        } catch (Exception e) {
          LOG.warn(
              "Error validating method {} ({})",
              method.getName(),
              parameterAnnotation.getClass().getSimpleName());
        }
      }
    }
  }

  public static List<String> getRegexes(Annotation parameterAnnotation) {
    switch (parameterAnnotation.annotationType().getSimpleName()) {
      case "Text":
        TemplateParameter.Text simpleTextParam = (TemplateParameter.Text) parameterAnnotation;
        return Arrays.asList(simpleTextParam.regexes());
      case "GcsReadFile":
        TemplateParameter.GcsReadFile gcsReadFileParam =
            (TemplateParameter.GcsReadFile) parameterAnnotation;
        return List.of("^gs:\\/\\/[^\\n\\r]+$");
      case "GcsReadFolder":
        TemplateParameter.GcsReadFolder gcsReadFolderParam =
            (TemplateParameter.GcsReadFolder) parameterAnnotation;
        return List.of("^gs:\\/\\/[^\\n\\r]+$");
      case "GcsWriteFile":
        TemplateParameter.GcsWriteFile gcsWriteFileParam =
            (TemplateParameter.GcsWriteFile) parameterAnnotation;
        return List.of("^gs:\\/\\/[^\\n\\r]+$");
      case "GcsWriteFolder":
        TemplateParameter.GcsWriteFolder gcsWriteFolderParam =
            (TemplateParameter.GcsWriteFolder) parameterAnnotation;
        return List.of("^gs:\\/\\/[^\\n\\r]+$");
      case "PubsubSubscription":
        TemplateParameter.PubsubSubscription pubsubSubscriptionParam =
            (TemplateParameter.PubsubSubscription) parameterAnnotation;
        return List.of("^projects\\/[^\\n\\r\\/]+\\/subscriptions\\/[^\\n\\r\\/]+$|^$");
      case "PubsubTopic":
        TemplateParameter.PubsubTopic pubsubTopicParam =
            (TemplateParameter.PubsubTopic) parameterAnnotation;
        return List.of("^projects\\/[^\\n\\r\\/]+\\/topics\\/[^\\n\\r\\/]+$|^$");
      case "Password":
        TemplateParameter.Password passwordParam = (TemplateParameter.Password) parameterAnnotation;
        return null;
      case "ProjectId":
        TemplateParameter.ProjectId projectIdParam =
            (TemplateParameter.ProjectId) parameterAnnotation;
        return List.of("[a-z0-9\\-\\.\\:]+");
      case "Boolean":
        TemplateParameter.Boolean booleanParam = (TemplateParameter.Boolean) parameterAnnotation;
        return List.of("^(true|false)$");
      case "Integer":
        TemplateParameter.Integer integerParam = (TemplateParameter.Integer) parameterAnnotation;
        return List.of("^[0-9]+$");
      case "Long":
        TemplateParameter.Long longParam = (TemplateParameter.Long) parameterAnnotation;
        return List.of("^[0-9]+$");
      case "Enum":
        TemplateParameter.Enum enumParam = (TemplateParameter.Enum) parameterAnnotation;
        return List.of("^(" + String.join("|", enumParam.enumOptions()) + ")$");
      case "DateTime":
        TemplateParameter.DateTime dateTimeParam = (TemplateParameter.DateTime) parameterAnnotation;
        return List.of(
            "^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):(([0-9]{2})(\\\\.[0-9]+)?)Z$");
      case "BigQueryTable":
        TemplateParameter.BigQueryTable bigQueryTableParam =
            (TemplateParameter.BigQueryTable) parameterAnnotation;
        return List.of(".+:.+\\..+");
      case "KmsEncryptionKey":
        TemplateParameter.KmsEncryptionKey kmsEncryptionKeyParam =
            (TemplateParameter.KmsEncryptionKey) parameterAnnotation;
        return List.of(
            "^projects\\/[^\\n"
                + "\\r"
                + "\\/]+\\/locations\\/[^\\n"
                + "\\r"
                + "\\/]+\\/keyRings\\/[^\\n"
                + "\\r"
                + "\\/]+\\/cryptoKeys\\/[^\\n"
                + "\\r"
                + "\\/]+$");
      case "Duration":
        TemplateParameter.Duration durationParam = (TemplateParameter.Duration) parameterAnnotation;
        return List.of("^[1-9][0-9]*[s|m|h]$");
      default:
        return null;
    }
  }
}

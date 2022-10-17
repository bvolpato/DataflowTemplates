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
package com.google.cloud.teleport.plugin;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.teleport.metadata.Template;
import com.google.cloud.teleport.plugin.model.ImageSpec;
import com.google.cloud.teleport.plugin.model.TemplateDefinitions;
import com.google.cloud.teleport.plugin.sample.AtoBOk;
import com.google.common.io.Files;
import java.io.File;
import org.junit.Test;

public class TemplateSpecsGeneratorTest {

  TemplateDefinitions definitions =
      new TemplateDefinitions(AtoBOk.class, AtoBOk.class.getAnnotation(Template.class));
  ImageSpec imageSpec = definitions.buildSpecModel(false);
  TemplateSpecsGenerator generator = new TemplateSpecsGenerator();

  @Test
  public void saveImageSpec() {
    File outputFolder = Files.createTempDir().getAbsoluteFile();
    File saveImageSpec = generator.saveImageSpec(definitions, imageSpec, outputFolder);
    assertNotNull(saveImageSpec);
    assertTrue(saveImageSpec.exists());
  }

  @Test
  public void saveMetadata() {}

  @Test
  public void saveCommandSpec() {}

  @Test
  public void getTemplateNameDash() {}
}

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
package com.google.cloud.teleport.plugin.maven;

import static org.junit.Assert.*;

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;
import org.junit.Rule;
import org.junit.Test;

public class TemplateSpecsMojoTest {
  @Rule
  public MojoRule rule =
      new MojoRule() {
        @Override
        protected void before() throws Throwable {}

        @Override
        protected void after() {}
      };

  /** @throws Exception if any */
  @Test
  public void testSomething() throws Exception {
    // File pom = new File("target/test-classes/project-to-test/");
    // assertNotNull(pom);
    // assertTrue(pom.exists());
    //
    // MyMojo myMojo = (MyMojo) rule.lookupConfiguredMojo(pom, "docs");
    // assertNotNull(myMojo);
    // myMojo.execute();
    //
    // File outputDirectory = (File) rule.getVariableValueFromObject(myMojo, "outputDirectory");
    // assertNotNull(outputDirectory);
    // assertTrue(outputDirectory.exists());
    //
    // File touch = new File(outputDirectory, "touch.txt");
    // assertTrue(touch.exists());
  }

  /** Do not need the MojoRule. */
  @WithoutMojo
  @Test
  public void
      testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn() {
    assertTrue(true);
  }
}

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
package com.google.cloud.teleport.it.matchers;

import com.google.cloud.teleport.it.launcher.PipelineOperator.Result;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

/**
 * Subject that has assertion operations for {@link Result}, which is the end result of a pipeline
 * run.
 */
public final class ResultSubject extends Subject {

  private final Result actual;

  private ResultSubject(FailureMetadata metadata, Result actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public static Factory<ResultSubject, Result> result() {
    return ResultSubject::new;
  }

  /** Check if the subject reflects succeeded states */
  public void meetsConditions() {
    check("check if meets all conditions").that(actual).isEqualTo(Result.CONDITION_MET);
  }
}

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

import com.google.cloud.teleport.it.launcher.PipelineLauncher.JobState;
import com.google.cloud.teleport.it.launcher.PipelineLauncher.LaunchInfo;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;

/**
 * Subject that has assertion operations for {@link LaunchInfo}, usually coming from the result of a
 * pipeline launch.
 */
public final class LaunchInfoSubject extends Subject {

  private final LaunchInfo actual;

  private LaunchInfoSubject(FailureMetadata metadata, LaunchInfo actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public static Factory<LaunchInfoSubject, LaunchInfo> launchInfo() {
    return LaunchInfoSubject::new;
  }

  /** Check if the subject reflects succeeded states. */
  public void succeeded() {
    check("check if succeeded").that(actual.state()).isIn(JobState.ACTIVE_STATES);
  }

  /** Check if the subject reflects failure states. */
  public void failed() {
    check("check if succeeded").that(actual.state()).isIn(JobState.FAILED_STATES);
  }
}

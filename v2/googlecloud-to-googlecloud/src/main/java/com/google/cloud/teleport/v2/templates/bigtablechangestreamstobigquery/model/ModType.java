/*
 * Copyright (C) 2023 Google LLC
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
package com.google.cloud.teleport.v2.templates.bigtablechangestreamstobigquery.model;

/** Type of Cloud Bigtable mutations. */
public enum ModType {
  SET_CELL("SET_CELL"),
  DELETE_CELLS("DELETE_CELLS"),
  DELETE_FAMILY("DELETE_FAMILY"),
  UNKNOWN("UNKNOWN");

  private final String code;

  ModType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}

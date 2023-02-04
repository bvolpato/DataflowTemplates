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
package com.google.cloud.teleport.it.conditions;

import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.teleport.it.bigquery.BigQueryResourceManager;

/** ConditionCheck to validate if BigQuery has received a certain amount of rows. */
public class BigQueryRowsCheck extends ConditionCheck {

  private BigQueryResourceManager resourceManager;
  private TableId tableId;
  private Integer minRows;
  private Integer maxRows;

  public BigQueryRowsCheck(
      BigQueryResourceManager resourceManager, TableId tableId, Integer minRows) {
    this.resourceManager = resourceManager;
    this.tableId = tableId;
    this.minRows = minRows;
  }

  public BigQueryRowsCheck(
      BigQueryResourceManager resourceManager, TableId tableId, Integer minRows, Integer maxRows) {
    this.resourceManager = resourceManager;
    this.tableId = tableId;
    this.minRows = minRows;
    this.maxRows = maxRows;
  }

  @Override
  String getDescription() {
    if (this.maxRows != null) {
      return String.format(
          "BigQuery check if table %s has between %d and %d rows",
          this.tableId.getTable(), this.minRows, this.maxRows);
    }
    return String.format(
        "BigQuery check if table %s has %d rows", this.tableId.getTable(), this.minRows);
  }

  @Override
  CheckResult check() {
    TableResult tableResult = resourceManager.readTable(tableId);
    long totalRows = tableResult.getTotalRows();
    if (totalRows < this.minRows) {
      return new CheckResult(
          false, String.format("Expected %d but has only %d", this.minRows, totalRows));
    }
    if (this.maxRows != null && totalRows > this.maxRows) {
      return new CheckResult(
          false, String.format("Expected up to %d but found %d rows", this.maxRows, totalRows));
    }

    if (this.maxRows != null) {
      return new CheckResult(
          true,
          String.format(
              "Expected between %d and %d rows and found %d",
              this.minRows, this.maxRows, totalRows));
    }

    return new CheckResult(
        true, String.format("Expected at least %d rows and found %d", this.minRows, totalRows));
  }
}

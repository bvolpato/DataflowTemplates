package com.google.cloud.teleport.it.conditions;

import com.google.cloud.bigquery.TableId;
import com.google.cloud.teleport.it.bigquery.BigQueryResourceManager;

/** ConditionCheck to validate if BigQuery has received a certain amount of rows. */
public class BigQueryRowsCheck extends ConditionCheck {

  private BigQueryResourceManager resourceManager;
  private TableId tableId;
  private int rows;

  public BigQueryRowsCheck(BigQueryResourceManager resourceManager, TableId tableId, int rows) {
    this.resourceManager = resourceManager;
    this.tableId = tableId;
    this.rows = rows;
  }

  @Override
  String getDescription() {
    return String.format(
        "BigQuery check if table %s has %d rows", this.tableId.getTable(), this.rows);
  }

  @Override
  CheckResult check() {
    long totalRows = resourceManager.readTable(tableId).getTotalRows();
    if (totalRows >= this.rows) {
      return new CheckResult(
          true, String.format("Expected %d and found %d rows", this.rows, totalRows));
    }

    return new CheckResult(
        false, String.format("Expected %d but has only %d", this.rows, totalRows));
  }
}

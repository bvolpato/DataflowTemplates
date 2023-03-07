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

import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Subject that has assertion operations for record lists, usually coming from the result of a
 * template.
 */
public final class RecordsSubject extends Subject {

  @Nullable private final List<Map<String, Object>> actual;

  private RecordsSubject(FailureMetadata metadata, @Nullable List<Map<String, Object>> actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  public static Factory<RecordsSubject, List<Map<String, Object>>> records() {
    return RecordsSubject::new;
  }

  /** Check if records list has rows (i.e., is not empty). */
  public void hasRows() {
    check("there are rows").that(actual.size()).isGreaterThan(0);
  }

  /**
   * Check if records list has a specific number of rows.
   *
   * @param expectedRows Expected Rows
   */
  public void hasRows(int expectedRows) {
    check("there are %s rows", expectedRows).that(actual.size()).isEqualTo(expectedRows);
  }

  /**
   * Check if the records list has a specific row.
   *
   * @param record Expected row to search
   */
  public void hasRecord(Map<String, Object> record) {
    check("expected that contains record %s", record.toString()).that(actual).contains(record);
  }

  /**
   * Check if the records list matches a specific row (using partial / subset comparison).
   *
   * @param subset Expected subset to search in a record.
   */
  public void hasRecordSubset(Map<String, Object> subset) {

    Map<String, Object> expected = new TreeMap<>(subset);
    for (Map<String, Object> candidate : actual) {
      boolean match = true;
      for (Map.Entry<String, Object> entry : subset.entrySet()) {
        if (!candidate.containsKey(entry.getKey())
            || !candidate.get(entry.getKey()).equals(entry.getValue())) {
          match = false;
          break;
        }
      }

      if (match) {
        return;
      }
    }

    failWithoutActual(
        Fact.simpleFact(
            "expected that contains partial record " + expected + ", but only had " + actual));
  }

  /**
   * Check if the records list has specific rows, without guarantees of ordering. The way that
   * ordering is taken out of the equation is by converting all records to TreeMap, which guarantee
   * natural key ordering.
   *
   * @param records Expected rows to search
   */
  public void hasRecordsUnordered(List<Map<String, Object>> records) {

    for (Map<String, Object> record : records) {
      String expected = new TreeMap<>(record).toString();
      if (actual.stream()
          .noneMatch(candidate -> new TreeMap<>(candidate).toString().equals(expected))) {
        failWithoutActual(
            Fact.simpleFact(
                "expected that contains unordered record "
                    + expected
                    + ", but only had "
                    + actual));
      }
    }
  }

  /**
   * Check if the records list has specific rows, without guarantees of ordering. The way that
   * ordering is taken out of the equation is by converting all records to TreeMap, which guarantee
   * natural key ordering.
   *
   * <p>In this particular method, force the columns to be case-insensitive to maximize
   * compatibility.
   *
   * @param records Expected rows to search
   */
  public void hasRecordsUnorderedCaseInsensitiveColumns(List<Map<String, Object>> records) {

    for (Map<String, Object> record : records) {
      String expected = convertKeysToUpperCase(new TreeMap<>(record)).toString();
      if (actual.stream()
          .noneMatch(
              candidate ->
                  convertKeysToUpperCase(new TreeMap<>(candidate)).toString().equals(expected))) {
        failWithoutActual(
            Fact.simpleFact(
                "expected that contains unordered record (and case insensitive) "
                    + expected
                    + ", but only had "
                    + actual));
      }
    }
  }

  private TreeMap<String, Object> convertKeysToUpperCase(Map<String, Object> map) {
    return new TreeMap<>(
        map.entrySet().stream()
            .collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Entry::getValue)));
  }

  /**
   * Check if the records list has a specific row, without guarantees of ordering. The way that
   * ordering is taken out of the equation is by converting all records to TreeMap, which guarantee
   * natural key ordering.
   *
   * @param record Expected row to search
   */
  public void hasRecordUnordered(Map<String, Object> record) {
    this.hasRecordsUnordered(List.of(record));
  }

  /**
   * Check if the records list match exactly another list.
   *
   * @param records Expected records
   */
  public void hasRecords(List<Map<String, Object>> records) {
    check("records %s are there", records.toString())
        .that(actual)
        .containsExactlyElementsIn(records);
  }

  /**
   * Check if all the records match given record.
   *
   * @param record Expected record
   */
  public void allMatch(Map<String, Object> record) {
    List<Map<String, Object>> records = Collections.nCopies(actual.size(), record);
    hasRecords(records);
  }
}

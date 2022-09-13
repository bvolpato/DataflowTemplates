/*
 * Copyright (C) 2018 Google LLC
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
package com.google.cloud.teleport.templates.common;

import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.ValueProvider.StaticValueProvider;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Create.Values;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;

/** Unit tests for {@link ValueProviderBooleanFilter}. */
public class ValueProviderBooleanFilterTest {

  @Rule public final transient TestPipeline pipeline = TestPipeline.create();

  @Test
  public void testFilterTrue() {

    ValueProvider<Boolean> provider = StaticValueProvider.of(true);
    Values<Integer> source = Create.of(1, 2, 3);

    PCollection<Integer> filtered =
        pipeline.apply(source).apply(new ValueProviderBooleanFilter<>(provider));

    PAssert.that(filtered).containsInAnyOrder(1, 2, 3);

    pipeline.run();
  }

  @Test
  public void testFilterFalse() {

    ValueProvider<Boolean> provider = StaticValueProvider.of(false);
    Values<Integer> source = Create.of(1, 2, 3);

    PCollection<Integer> filtered =
        pipeline.apply(source).apply(new ValueProviderBooleanFilter<>(provider));

    PAssert.that(filtered.apply(Count.globally())).containsInAnyOrder(0L);

    pipeline.run();
  }
}

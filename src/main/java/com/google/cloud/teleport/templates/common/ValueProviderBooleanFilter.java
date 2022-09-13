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
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;

/**
 * Class {@link ValueProviderBooleanFilter}. It provides a {@link PTransform} implementation that
 * filters PCollection elements based on a ValueProvider that holds a Boolean.
 */
public class ValueProviderBooleanFilter<T> extends PTransform<PCollection<T>, PCollection<T>> {

  private final ValueProvider<Boolean> valueProvider;

  public ValueProviderBooleanFilter(ValueProvider<Boolean> valueProvider) {
    this.valueProvider = valueProvider;
  }

  public PCollection<T> expand(PCollection<T> input) {
    return input.apply(Filter.by(record -> valueProvider.get()));
  }
}

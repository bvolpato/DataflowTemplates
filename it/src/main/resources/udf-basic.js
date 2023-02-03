/**
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

/**
 * Identity function.
 * @param {T?} value input value
 * @return {T?} provided value
 */
function identity(value) {
  return value;
}

/**
 * Uppercase function.
 * @param {T?} value input value
 * @return {T?} provided value all in upper-case
 */
function uppercase(value) {
  return value.toUpperCase();
}

/**
 * Uppercase name function.
 * @param {T?} value input value
 * @return {T?} provided value with property "name" in upper-case
 */
function uppercaseName(value) {
  const data = JSON.parse(value);
  data.name = data.name.toUpperCase();
  return JSON.stringify(data);
}


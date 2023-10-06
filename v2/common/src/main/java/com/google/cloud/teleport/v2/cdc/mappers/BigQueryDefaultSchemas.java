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
package com.google.cloud.teleport.v2.cdc.mappers;

import com.google.cloud.bigquery.StandardSQLTypeName;
import java.util.HashMap;
import java.util.Map;

/** Class {@link BigQueryDefaultSchemas}. */
public final class BigQueryDefaultSchemas {

  public static final Map<String, StandardSQLTypeName> WEB_SDK_SCHEMA = new HashMap<>();

  static {
    WEB_SDK_SCHEMA.put("$os", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$browser", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$referrer", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$referring_domain", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$current_url", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$browser_version", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$screen_height", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$screen_width", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("mp_lib", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$lib_version", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("hostname", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$initial_referrer", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("$initial_referring_domain", StandardSQLTypeName.STRING);
    WEB_SDK_SCHEMA.put("branch", StandardSQLTypeName.STRING);
  }

  public static final Map<String, StandardSQLTypeName> DATASTREAM_METADATA_SCHEMA = new HashMap<>();

  static {
    DATASTREAM_METADATA_SCHEMA.put("_metadata_change_type", StandardSQLTypeName.STRING);
    DATASTREAM_METADATA_SCHEMA.put("_metadata_deleted", StandardSQLTypeName.BOOL);
    DATASTREAM_METADATA_SCHEMA.put("_metadata_timestamp", StandardSQLTypeName.TIMESTAMP);
    DATASTREAM_METADATA_SCHEMA.put("_metadata_read_timestamp", StandardSQLTypeName.TIMESTAMP);

    // Oracle specific metadata
    DATASTREAM_METADATA_SCHEMA.put("_metadata_row_id", StandardSQLTypeName.STRING);
    DATASTREAM_METADATA_SCHEMA.put("_metadata_rs_id", StandardSQLTypeName.STRING);
    DATASTREAM_METADATA_SCHEMA.put("_metadata_ssn", StandardSQLTypeName.INT64);

    // MySQL Specific Metadata
    DATASTREAM_METADATA_SCHEMA.put("_metadata_log_file", StandardSQLTypeName.STRING);
    DATASTREAM_METADATA_SCHEMA.put("_metadata_log_position", StandardSQLTypeName.INT64);
  }
}

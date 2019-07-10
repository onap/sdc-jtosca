/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.toscaparser.api.utils;


public enum JToscaErrorCodes {
    MISSING_META_FILE("JE1001"),
    INVALID_META_YAML_CONTENT("JE1002"),
    ENTRY_DEFINITION_NOT_DEFINED("JE1003"),
    MISSING_ENTRY_DEFINITION_FILE("JE1004"),
    GENERAL_ERROR("JE1005"),
    PATH_NOT_VALID("JE1006"),
    CSAR_TOSCA_VALIDATION_ERROR("JE1007"),
    INVALID_CSAR_FORMAT("JE1008");

    private String value;

    JToscaErrorCodes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static JToscaErrorCodes getByCode(String code) {
        for (JToscaErrorCodes v : values()) {
            if (v.getValue().equals(code)) {
                return v;
            }
        }
        return null;
    }
}

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

package org.onap.sdc.toscaparser.api.common;

import java.util.IllegalFormatException;

public class TOSCAException extends Exception {
    private String message = "An unkown exception has occurred";
    private static boolean FATAL_EXCEPTION_FORMAT_ERRORS = false;
    private String msgFmt = null;

    public TOSCAException(String... strings) {
        try {
            message = String.format(msgFmt, (Object[]) strings);
        } catch (IllegalFormatException e) {
            // TODO log

            if (FATAL_EXCEPTION_FORMAT_ERRORS) {
                throw e;
            }

        }

    }

    public String __str__() {
        return message;
    }

    public static void generate_inv_schema_property_error(String name, String attr, String value, String valid_values) {
        //TODO

    }

    public static void setFatalFormatException(boolean flag) {
        FATAL_EXCEPTION_FORMAT_ERRORS = flag;
    }

}


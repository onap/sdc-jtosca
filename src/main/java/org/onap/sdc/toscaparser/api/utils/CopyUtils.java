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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CopyUtils {

    private CopyUtils() {
    }

    @SuppressWarnings("unchecked")
    public static Object copyLhmOrAl(Object src) {
        if (src instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> dst = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, Object> me : ((LinkedHashMap<String, Object>) src).entrySet()) {
                dst.put(me.getKey(), me.getValue());
            }
            return dst;
        } else if (src instanceof ArrayList) {
            ArrayList<Object> dst = new ArrayList<Object>();
            for (Object o : (ArrayList<Object>) src) {
                dst.add(o);
            }
            return dst;
        } else {
            return null;
        }
    }
}

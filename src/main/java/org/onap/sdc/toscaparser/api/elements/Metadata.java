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

package org.onap.sdc.toscaparser.api.elements;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Metadata {

    private final Map<String, Object> metadataMap;

    public Metadata(Map<String, Object> metadataMap) {
        this.metadataMap = metadataMap != null ? metadataMap : new HashMap<>();
    }

    public String getValue(String key) {

        Object obj = this.metadataMap.get(key);
        if (obj != null) {
            return String.valueOf(obj);
        }
        return null;
    }

    /**
     * Get all properties of a Metadata object.<br>
     * This object represents the "metadata" section of some entity.
     *
     * @return all properties of this Metadata, as a key-value.
     */
    public Map<String, String> getAllProperties() {
        return metadataMap.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<String, String>(e.getKey(), String.valueOf(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public String toString() {
        return "Metadata{"
                + "metadataMap=" + metadataMap
                + '}';
    }

}

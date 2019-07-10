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

package org.onap.sdc.toscaparser.api.parameters;

import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.enums.ToscaElementNames;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Annotation {

    private static final String HEAT = "HEAT";
    private String name;
    private String type;
    private ArrayList<Property> properties;


    public Annotation() {
    }

    @SuppressWarnings("unchecked")
    public Annotation(Map.Entry<String, Object> annotationEntry) {
        if (annotationEntry != null) {
            name = annotationEntry.getKey();
            Map<String, Object> annValue = (Map<String, Object>) annotationEntry.getValue();
            type = (String) annValue.get(ToscaElementNames.TYPE.getName());
            properties = fetchProperties((Map<String, Object>) annValue.get(ToscaElementNames.PROPERTIES.getName()));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Property> getProperties() {
        return properties;
    }

    public void setProperties(ArrayList<Property> properties) {
        this.properties = properties;
    }

    private ArrayList<Property> fetchProperties(Map<String, Object> properties) {
        if (properties != null) {
            return (ArrayList<Property>) properties.entrySet().stream()
                    .map(Property::new)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public boolean isHeatSourceType() {
        if (properties == null) {
            return false;
        }
        Optional<Property> sourceType = properties.stream()
                .filter(p -> p.getName().equals(ToscaElementNames.SOURCE_TYPE.getName()))
                .findFirst();
        if (!sourceType.isPresent()) {
            return false;
        }
        return sourceType.get().getValue() != null && ((String) sourceType.get().getValue()).equals(HEAT);
    }

}

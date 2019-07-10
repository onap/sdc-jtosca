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

import org.onap.sdc.toscaparser.api.DataEntity;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.elements.EntityType;
import org.onap.sdc.toscaparser.api.elements.constraints.Constraint;
import org.onap.sdc.toscaparser.api.elements.constraints.Schema;
import org.onap.sdc.toscaparser.api.elements.enums.ToscaElementNames;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Input {

    private static final String TYPE = "type";
    private static final String DESCRIPTION = "description";
    private static final String DEFAULT = "default";
    private static final String CONSTRAINTS = "constraints";
    private static final String REQUIRED = "required";
    private static final String STATUS = "status";
    private static final String ENTRY_SCHEMA = "entry_schema";

    public static final String INTEGER = "integer";
    public static final String STRING = "string";
    public static final String BOOLEAN = "boolean";
    public static final String FLOAT = "float";
    public static final String LIST = "list";
    public static final String MAP = "map";
    public static final String JSON = "json";

    private static String[] inputField = {
            TYPE, DESCRIPTION, DEFAULT, CONSTRAINTS, REQUIRED, STATUS, ENTRY_SCHEMA
    };

    private static String[] primitiveTypes = {
            INTEGER, STRING, BOOLEAN, FLOAT, LIST, MAP, JSON
    };

    private String name;
    private Schema schema;
    private LinkedHashMap<String, Object> customDefs;
    private Map<String, Annotation> annotations;

    public Input() {
    }

    public Input(String name, LinkedHashMap<String, Object> schema, LinkedHashMap<String, Object> customDefinitions) {
        this.name = name;
        this.schema = new Schema(name, schema);
        customDefs = customDefinitions;
    }

    @SuppressWarnings("unchecked")
    public void parseAnnotations() {
        if (schema.getSchema() != null) {
            LinkedHashMap<String, Object> annotations = (LinkedHashMap<String, Object>) schema.getSchema().get(ToscaElementNames.ANNOTATIONS.getName());
            if (annotations != null) {
                setAnnotations(annotations.entrySet().stream()
                        .map(Annotation::new)
                        .filter(Annotation::isHeatSourceType)
                        .collect(Collectors.toMap(Annotation::getName, a -> a)));
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return schema.getType();
    }

    public String getDescription() {
        return schema.getDescription();
    }

    public boolean isRequired() {
        return schema.isRequired();
    }

    public Object getDefault() {
        return schema.getDefault();
    }

    public ArrayList<Constraint> getConstraints() {
        return schema.getConstraints();
    }

    public void validate(Object value) {
        validateField();
        validateType(getType());
        if (value != null) {
            validateValue(value);
        }
    }

    private void validateField() {
        for (String key : schema.getSchema().keySet()) {
            boolean bFound = false;
            for (String ifld : inputField) {
                if (key.equals(ifld)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE214", String.format(
                        "UnknownFieldError: Input \"%s\" contains unknown field \"%s\"",
                        name, key)));
            }
        }
    }

    private void validateType(String inputType) {
        boolean bFound = false;
        for (String pt : Schema.PROPERTY_TYPES) {
            if (pt.equals(inputType)) {
                bFound = true;
                break;
            }
        }

        if (!bFound) {
            if (customDefs.get(inputType) != null) {
                bFound = true;
            }
        }

        if (!bFound) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE215", String.format(
                    "ValueError: Invalid type \"%s\"", inputType)));
        }
    }

    @SuppressWarnings("unchecked")
    private void validateValue(Object value) {
        Object datatype;
        if (EntityType.TOSCA_DEF.get(getType()) != null) {
            datatype = EntityType.TOSCA_DEF.get(getType());
        } else if (EntityType.TOSCA_DEF.get(EntityType.DATATYPE_NETWORK_PREFIX + getType()) != null) {
            datatype = EntityType.TOSCA_DEF.get(EntityType.DATATYPE_NETWORK_PREFIX + getType());
        }

        String type = getType();
        // if it's one of the basic types DON'T look in customDefs
        if (Arrays.asList(primitiveTypes).contains(type)) {
            DataEntity.validateDatatype(getType(), value, null, customDefs, null);
            return;
        } else if (customDefs.get(getType()) != null) {
            datatype = customDefs.get(getType());
            DataEntity.validateDatatype(getType(), value, (LinkedHashMap<String, Object>) datatype, customDefs, null);
            return;
        }

        DataEntity.validateDatatype(getType(), value, null, customDefs, null);
    }

    public Map<String, Annotation> getAnnotations() {
        return annotations;
    }

    private void setAnnotations(Map<String, Annotation> annotations) {
        this.annotations = annotations;
    }

    public void resetAnnotaions() {
        annotations = null;
    }

    public LinkedHashMap<String, Object> getEntrySchema() {
        return schema.getEntrySchema();
    }

}

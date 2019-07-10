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

package org.onap.sdc.toscaparser.api;

import com.google.common.collect.Lists;
import org.onap.sdc.toscaparser.api.elements.constraints.Constraint;
import org.onap.sdc.toscaparser.api.elements.constraints.Schema;
import org.onap.sdc.toscaparser.api.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Property {
    // TOSCA built-in Property type
    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class.getName());

    private static final String TYPE = "type";
    private static final String REQUIRED = "required";
    private static final String DESCRIPTION = "description";
    private static final String DEFAULT = "default";
    private static final String CONSTRAINTS = "constraints";
    private static String entrySchema = "entry_schema";
    private static String dataType = "datatypes";

    private static final String[] PROPERTY_KEYS = {
            TYPE, REQUIRED, DESCRIPTION, DEFAULT, CONSTRAINTS};

    private static final String ENTRYTYPE = "type";
    private static final String ENTRYPROPERTIES = "properties";
    private static final String PATH_DELIMITER = "#";
    private static final String[] ENTRY_SCHEMA_KEYS = {
            ENTRYTYPE, ENTRYPROPERTIES};

    private String name;
    private Object value;
    private Schema schema;
    private LinkedHashMap<String, Object> customDef;

    public Property(Map.Entry<String, Object> propertyEntry) {
        name = propertyEntry.getKey();
        value = propertyEntry.getValue();
    }

    public Property(String propname,
                    Object propvalue,
                    LinkedHashMap<String, Object> propschemaDict,
                    LinkedHashMap<String, Object> propcustomDef) {

        name = propname;
        value = propvalue;
        customDef = propcustomDef;
        schema = new Schema(propname, propschemaDict);
    }

    public String getType() {
        return schema.getType();
    }

    public boolean isRequired() {
        return schema.isRequired();
    }

    public String getDescription() {
        return schema.getDescription();
    }

    public Object getDefault() {
        return schema.getDefault();
    }

    public ArrayList<Constraint> getConstraints() {
        return schema.getConstraints();
    }

    public LinkedHashMap<String, Object> getEntrySchema() {
        return schema.getEntrySchema();
    }


    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    // setter
    public Object setValue(Object vob) {
        value = vob;
        return value;
    }

    public void validate() {
        // Validate if not a reference property
        if (!Function.isFunction(value)) {
            if (getType().equals(Schema.STRING)) {
                value = value.toString();
            }
            value = DataEntity.validateDatatype(getType(), value,
                    getEntrySchema(),
                    customDef,
                    name);
            validateConstraints();
        }
    }

    private void validateConstraints() {
        if (getConstraints() != null) {
            for (Constraint constraint : getConstraints()) {
                constraint.validate(value);
            }
        }
    }

    @Override
    public String toString() {
        return "Property{"
                + "name='" + name + '\''
                + ", value=" + value
                + ", schema=" + schema
                + ", customDef=" + customDef
                + '}';
    }

    /**
     * Retrieves property value as list of strings if<br>
     * - the value is simple<br>
     * - the value is list of simple values<br>
     * - the provided path refers to a simple property inside a data type<br>
     *
     * @param propertyPath valid name of property for search.<br>
     *                     If a name refers to a simple field inside a datatype, the property name should be defined with # delimiter.<br>
     * @return List of property values. If not found, empty list will be returned.<br>
     * If property value is a list either of simple fields or of simple fields inside a datatype, all values from the list should be returned
     */
    public List<String> getLeafPropertyValue(String propertyPath) {
        List<String> propertyValueList = Collections.emptyList();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getLeafPropertyValue=> A new request: propertyPath: {}, value: {}", propertyPath, getValue());
        }
        if (propertyPath == null || getValue() == null
                //if entry_schema disappears, it is datatype,
                // otherwise it is map of simple types - should be ignored
                || isValueMapOfSimpleTypes()) {
            LOGGER.error("It is a wrong request - ignoring! propertyPath: {}, value: {}", propertyPath, getValue());
            return propertyValueList;
        }
        String[] path = propertyPath.split(PATH_DELIMITER);

        if (Schema.isRequestedTypeSimple(getPropertyTypeByPath(path))) {
            //the internal property type in the path is either simple or list of simple types
            if (isValueInsideDataType()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The requested is an internal simple property inside of a data type");
                }
                //requested value is an internal simple property inside of a data type
                propertyValueList = getSimplePropertyValueForComplexType(path);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("The requested property has simple type or list of simple types");
                }
                //the requested property is simple type or list of simple types
                propertyValueList = getSimplePropertyValueForSimpleType();
            }
        }
        return propertyValueList;
    }

    private boolean isValueMapOfSimpleTypes() {
        if (getValue() instanceof Map && getEntrySchema() != null) {
            LOGGER.warn("This property value is a map of simple types");
            return true;
        }
        return false;
    }

    private boolean isValueInsideDataType() {
        //value is either a list of values for data type
        //or data type
        return (Schema.LIST.equals(getType()) && isDataTypeInEntrySchema())
                || (getEntrySchema() == null && getType().contains(dataType));
    }

    private Object getSimpleValueFromComplexObject(Object current, String[] path) {
        if (current == null) {
            return null;
        }
        int index = 0;

        if (path.length > index) {
            for (int i = index; i < path.length; i++) {
                if (current instanceof Map) {
                    current = ((Map<String, Object>) current).get(path[i]);
                } else if (current instanceof List) {
                    current = ((List) current).get(0);
                    i--;
                } else {
                    return null;
                }
            }
        }
        if (current != null) {
            return current;
        }
        return null;
    }

    private List<String> getSimplePropertyValueForSimpleType() {
        if (getValue() instanceof List || getValue() instanceof Map) {
            return getSimplePropertyValueForComplexType(null);
        }
        return Lists.newArrayList(String.valueOf(value));
    }

    private List<String> getSimplePropertyValueForComplexType(String[] path) {
        if (getValue() instanceof List) {
            return ((List<Object>) getValue()).stream()
                    .map(v -> {
                        if (path != null) {
                            return getSimpleValueFromComplexObject(v, path);
                        } else {
                            return v;
                        }
                    })
                    //it might be null when get_input can't be resolved
                    // e.g.:
                    // - get_input has two parameters: 1. list and 2. index in this list
                    //and list has no value
                    // - neither value no default is defined for get_input
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }
        //it is data type
        List<String> valueList = Lists.newArrayList();
        String valueString = String.valueOf(getSimpleValueFromComplexObject(getValue(), path));
        if (Objects.nonNull(valueString)) {
            valueList.add(valueString);
        }
        return valueList;
    }

    private String getPropertyTypeByPath(String[] path) {
        String propertyType = calculatePropertyType();

        if (path.length > 0 && !path[0].isEmpty()) {
            return getInternalPropertyType(propertyType, path, 0);
        }
        return propertyType;
    }

    private String calculatePropertyType() {
        String propertyType = getType();
        if (Schema.LIST.equals(propertyType)) {
            //if it is list, return entry schema type
            return (String) getEntrySchema().get(ENTRYTYPE);
        }
        return propertyType;
    }

    private String calculatePropertyType(LinkedHashMap<String, Object> property) {
        String type = (String) property.get(TYPE);
        if (Schema.LIST.equals(type)) {
            //it might be a data type
            return getEntrySchemaType(property);
        }
        return type;
    }

    private String getInternalPropertyType(String dataTypeName, String[] path, int index) {
        if (path.length > index) {
            LinkedHashMap<String, Object> complexProperty = (LinkedHashMap<String, Object>) customDef.get(dataTypeName);
            if (complexProperty != null) {
                LinkedHashMap<String, Object> dataTypeProperties = (LinkedHashMap<String, Object>) complexProperty.get(ENTRYPROPERTIES);
                return getPropertyTypeFromCustomDefDeeply(path, index, dataTypeProperties);
            }
        }
        //stop searching - seems as wrong flow: the path is finished but the value is not found yet
        return null;
    }

    private String getEntrySchemaType(LinkedHashMap<String, Object> property) {
        LinkedHashMap<String, Object> entrySchema = (LinkedHashMap<String, Object>) property.get(Property.entrySchema);
        if (entrySchema != null) {
            return (String) entrySchema.get(TYPE);
        }
        return null;
    }

    private String getPropertyTypeFromCustomDefDeeply(String[] path, int index, LinkedHashMap<String, Object> properties) {
        if (properties != null) {
            LinkedHashMap<String, Object> foundProperty = (LinkedHashMap<String, Object>) (properties).get(path[index]);
            if (foundProperty != null) {
                String propertyType = calculatePropertyType(foundProperty);
                if (propertyType == null || index == path.length - 1) {
                    return propertyType;
                }
                return getInternalPropertyType(propertyType, path, index + 1);
            }
        }
        return null;
    }

    private boolean isDataTypeInEntrySchema() {
        String entrySchemaType = (String) getEntrySchema().get(ENTRYTYPE);
        return entrySchemaType != null && entrySchemaType.contains(dataType);
    }


}

/*python

class Property(object):
    '''TOSCA built-in Property type.'''

    PROPERTY_KEYS = (
        TYPE, REQUIRED, DESCRIPTION, DEFAULT, CONSTRAINTS
    ) = (
        'type', 'required', 'description', 'default', 'constraints'
    )

    ENTRY_SCHEMA_KEYS = (
        ENTRYTYPE, ENTRYPROPERTIES
    ) = (
        'type', 'properties'
    )

    def __init__(self, property_name, value, schema_dict, custom_def=None):
        self.name = property_name
        self.value = value
        self.custom_def = custom_def
        self.schema = Schema(property_name, schema_dict)

    @property
    def type(self):
        return self.schema.type

    @property
    def required(self):
        return self.schema.required

    @property
    def description(self):
        return self.schema.description

    @property
    def default(self):
        return self.schema.default

    @property
    def constraints(self):
        return self.schema.constraints

    @property
    def entry_schema(self):
        return self.schema.entry_schema

    def validate(self):
        '''Validate if not a reference property.'''
        if not is_function(self.value):
            if self.type == Schema.STRING:
                self.value = str(self.value)
            self.value = DataEntity.validate_datatype(self.type, self.value,
                                                      self.entry_schema,
                                                      self.custom_def,
                                                      self.name)
            self._validate_constraints()

    def _validate_constraints(self):
        if self.constraints:
            for constraint in self.constraints:
                constraint.validate(self.value)
*/

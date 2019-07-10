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

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.elements.DataType;
import org.onap.sdc.toscaparser.api.elements.PortSpec;
import org.onap.sdc.toscaparser.api.elements.PropertyDef;
import org.onap.sdc.toscaparser.api.elements.ScalarUnitFrequency;
import org.onap.sdc.toscaparser.api.elements.ScalarUnitSize;
import org.onap.sdc.toscaparser.api.elements.ScalarUnitTime;
import org.onap.sdc.toscaparser.api.elements.constraints.Constraint;
import org.onap.sdc.toscaparser.api.elements.constraints.Schema;
import org.onap.sdc.toscaparser.api.functions.Function;
import org.onap.sdc.toscaparser.api.utils.TOSCAVersionProperty;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.utils.ValidateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DataEntity {
    // A complex data value entity

    private LinkedHashMap<String, Object> customDef;
    private DataType dataType;
    private LinkedHashMap<String, PropertyDef> schema;
    private Object value;
    private String propertyName;

    public DataEntity(String _dataTypeName, Object _valueDict,
                      LinkedHashMap<String, Object> _customDef, String _propName) {

        customDef = _customDef;
        dataType = new DataType(_dataTypeName, _customDef);
        schema = dataType.getAllProperties();
        value = _valueDict;
        propertyName = _propName;
    }

    @SuppressWarnings("unchecked")
    public Object validate() {
        // Validate the value by the definition of the datatype

        // A datatype can not have both 'type' and 'properties' definitions.
        // If the datatype has 'type' definition
        if (dataType.getValueType() != null) {
            value = DataEntity.validateDatatype(dataType.getValueType(), value, null, customDef, null);
            Schema schemaCls = new Schema(propertyName, dataType.getDefs());
            for (Constraint constraint : schemaCls.getConstraints()) {
                constraint.validate(value);
            }
        }
        // If the datatype has 'properties' definition
        else {
            if (!(value instanceof LinkedHashMap)) {
                //ERROR under investigation
                String checkedVal = value != null ? value.toString() : null;

                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE001", String.format(
                        "TypeMismatchError: \"%s\" is not a map. The type is \"%s\"",
                        checkedVal, dataType.getType())));

                if (value instanceof List && ((List) value).size() > 0) {
                    value = ((List) value).get(0);
                }

                if (!(value instanceof LinkedHashMap)) {
                    return value;
                }
            }


            LinkedHashMap<String, Object> valueDict = (LinkedHashMap<String, Object>) value;
            ArrayList<String> allowedProps = new ArrayList<>();
            ArrayList<String> requiredProps = new ArrayList<>();
            LinkedHashMap<String, Object> defaultProps = new LinkedHashMap<>();
            if (schema != null) {
                allowedProps.addAll(schema.keySet());
                for (String name : schema.keySet()) {
                    PropertyDef propDef = schema.get(name);
                    if (propDef.isRequired()) {
                        requiredProps.add(name);
                    }
                    if (propDef.getDefault() != null) {
                        defaultProps.put(name, propDef.getDefault());
                    }
                }
            }

            // check allowed field
            for (String valueKey : valueDict.keySet()) {
                //1710 devlop JSON validation
                if (!("json").equals(dataType.getType()) && !allowedProps.contains(valueKey)) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE100", String.format(
                            "UnknownFieldError: Data value of type \"%s\" contains unknown field \"%s\"",
                            dataType.getType(), valueKey)));
                }
            }

            // check default field
            for (String defKey : defaultProps.keySet()) {
                Object defValue = defaultProps.get(defKey);
                if (valueDict.get(defKey) == null) {
                    valueDict.put(defKey, defValue);
                }

            }

            // check missing field
            ArrayList<String> missingProp = new ArrayList<>();
            for (String reqKey : requiredProps) {
                if (!valueDict.keySet().contains(reqKey)) {
                    missingProp.add(reqKey);
                }
            }
            if (missingProp.size() > 0) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE003", String.format(
                        "MissingRequiredFieldError: Data value of type \"%s\" is missing required field(s) \"%s\"",
                        dataType.getType(), missingProp.toString())));
            }

            // check every field
            for (String vname : valueDict.keySet()) {
                Object vvalue = valueDict.get(vname);
                LinkedHashMap<String, Object> schemaName = _findSchema(vname);
                if (schemaName == null) {
                    continue;
                }
                Schema propSchema = new Schema(vname, schemaName);
                // check if field value meets type defined
                DataEntity.validateDatatype(propSchema.getType(),
                        vvalue,
                        propSchema.getEntrySchema(),
                        customDef,
                        null);

                // check if field value meets constraints defined
                if (propSchema.getConstraints() != null) {
                    for (Constraint constraint : propSchema.getConstraints()) {
                        if (vvalue instanceof ArrayList) {
                            for (Object val : (ArrayList<Object>) vvalue) {
                                constraint.validate(val);
                            }
                        } else {
                            constraint.validate(vvalue);
                        }
                    }
                }
            }
        }
        return value;
    }

    private LinkedHashMap<String, Object> _findSchema(String name) {
        if (schema != null && schema.get(name) != null) {
            return schema.get(name).getSchema();
        }
        return null;
    }

    public static Object validateDatatype(String type,
                                          Object value,
                                          LinkedHashMap<String, Object> entrySchema,
                                          LinkedHashMap<String, Object> customDef,
                                          String propName) {
        // Validate value with given type

        // If type is list or map, validate its entry by entry_schema(if defined)
        // If type is a user-defined complex datatype, custom_def is required.

        if (Function.isFunction(value)) {
            return value;
        } else if (type == null) {
            //NOT ANALYZED
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE002", String.format(
                    "MissingType: Type is missing for value \"%s\"",
                    value.toString())));
            return value;
        } else if (type.equals(Schema.STRING)) {
            return ValidateUtils.validateString(value);
        } else if (type.equals(Schema.INTEGER)) {
            return ValidateUtils.validateInteger(value);
        } else if (type.equals(Schema.FLOAT)) {
            return ValidateUtils.validateFloat(value);
        } else if (type.equals(Schema.NUMBER)) {
            return ValidateUtils.validateNumeric(value);
        } else if (type.equals(Schema.BOOLEAN)) {
            return ValidateUtils.validateBoolean(value);
        } else if (type.equals(Schema.RANGE)) {
            return ValidateUtils.validateRange(value);
        } else if (type.equals(Schema.TIMESTAMP)) {
            ValidateUtils.validateTimestamp(value);
            return value;
        } else if (type.equals(Schema.LIST)) {
            ValidateUtils.validateList(value);
            if (entrySchema != null) {
                DataEntity.validateEntry(value, entrySchema, customDef);
            }
            return value;
        } else if (type.equals(Schema.SCALAR_UNIT_SIZE)) {
            return (new ScalarUnitSize(value)).validateScalarUnit();
        } else if (type.equals(Schema.SCALAR_UNIT_FREQUENCY)) {
            return (new ScalarUnitFrequency(value)).validateScalarUnit();
        } else if (type.equals(Schema.SCALAR_UNIT_TIME)) {
            return (new ScalarUnitTime(value)).validateScalarUnit();
        } else if (type.equals(Schema.VERSION)) {
            return (new TOSCAVersionProperty(value.toString())).getVersion();
        } else if (type.equals(Schema.MAP)) {
            ValidateUtils.validateMap(value);
            if (entrySchema != null) {
                DataEntity.validateEntry(value, entrySchema, customDef);
            }
            return value;
        } else if (type.equals(Schema.PORTSPEC)) {
            // tODO(TBD) bug 1567063, validate source & target as PortDef type
            // as complex types not just as integers
            PortSpec.validateAdditionalReq(value, propName, customDef);
        } else {
            DataEntity data = new DataEntity(type, value, customDef, null);
            return data.validate();
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public static Object validateEntry(Object value,
                                       LinkedHashMap<String, Object> entrySchema,
                                       LinkedHashMap<String, Object> customDef) {

        // Validate entries for map and list
        Schema schema = new Schema(null, entrySchema);
        Object valueob = value;
        ArrayList<Object> valueList = null;
        if (valueob instanceof LinkedHashMap) {
            valueList = new ArrayList<Object>(((LinkedHashMap<String, Object>) valueob).values());
        } else if (valueob instanceof ArrayList) {
            valueList = (ArrayList<Object>) valueob;
        }
        if (valueList != null) {
            for (Object v : valueList) {
                DataEntity.validateDatatype(schema.getType(), v, schema.getEntrySchema(), customDef, null);
                if (schema.getConstraints() != null) {
                    for (Constraint constraint : schema.getConstraints()) {
                        constraint.validate(v);
                    }
                }
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return "DataEntity{" +
                "customDef=" + customDef +
                ", dataType=" + dataType +
                ", schema=" + schema +
                ", value=" + value +
                ", propertyName='" + propertyName + '\'' +
                '}';
    }
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import MissingRequiredFieldError
from toscaparser.common.exception import TypeMismatchError
from toscaparser.common.exception import UnknownFieldError
from toscaparser.elements.constraints import Schema
from toscaparser.elements.datatype import DataType
from toscaparser.elements.portspectype import PortSpec
from toscaparser.elements.scalarunit import ScalarUnit_Frequency
from toscaparser.elements.scalarunit import ScalarUnit_Size
from toscaparser.elements.scalarunit import ScalarUnit_Time
from toscaparser.utils.gettextutils import _
from toscaparser.utils import validateutils


class DataEntity(object):
    '''A complex data value entity.'''

    def __init__(self, datatypename, value_dict, custom_def=None,
                 prop_name=None):
        self.custom_def = custom_def
        self.datatype = DataType(datatypename, custom_def)
        self.schema = self.datatype.get_all_properties()
        self.value = value_dict
        self.property_name = prop_name

    def validate(self):
        '''Validate the value by the definition of the datatype.'''

        # A datatype can not have both 'type' and 'properties' definitions.
        # If the datatype has 'type' definition
        if self.datatype.value_type:
            self.value = DataEntity.validate_datatype(self.datatype.value_type,
                                                      self.value,
                                                      None,
                                                      self.custom_def)
            schema = Schema(self.property_name, self.datatype.defs)
            for constraint in schema.constraints:
                constraint.validate(self.value)
        # If the datatype has 'properties' definition
        else:
            if not isinstance(self.value, dict):
                ValidationIssueCollector.appendException(
                    TypeMismatchError(what=self.value,
                                      type=self.datatype.type))
            allowed_props = []
            required_props = []
            default_props = {}
            if self.schema:
                allowed_props = self.schema.keys()
                for name, prop_def in self.schema.items():
                    if prop_def.required:
                        required_props.append(name)
                    if prop_def.default:
                        default_props[name] = prop_def.default

            # check allowed field
            for value_key in list(self.value.keys()):
                if value_key not in allowed_props:
                    ValidationIssueCollector.appendException(
                        UnknownFieldError(what=(_('Data value of type "%s"')
                                                % self.datatype.type),
                                          field=value_key))

            # check default field
            for def_key, def_value in list(default_props.items()):
                if def_key not in list(self.value.keys()):
                    self.value[def_key] = def_value

            # check missing field
            missingprop = []
            for req_key in required_props:
                if req_key not in list(self.value.keys()):
                    missingprop.append(req_key)
            if missingprop:
                ValidationIssueCollector.appendException(
                    MissingRequiredFieldError(
                        what=(_('Data value of type "%s"')
                              % self.datatype.type), required=missingprop))

            # check every field
            for name, value in list(self.value.items()):
                schema_name = self._find_schema(name)
                if not schema_name:
                    continue
                prop_schema = Schema(name, schema_name)
                # check if field value meets type defined
                DataEntity.validate_datatype(prop_schema.type, value,
                                             prop_schema.entry_schema,
                                             self.custom_def)
                # check if field value meets constraints defined
                if prop_schema.constraints:
                    for constraint in prop_schema.constraints:
                        if isinstance(value, list):
                            for val in value:
                                constraint.validate(val)
                        else:
                            constraint.validate(value)

        return self.value

    def _find_schema(self, name):
        if self.schema and name in self.schema.keys():
            return self.schema[name].schema

    @staticmethod
    def validate_datatype(type, value, entry_schema=None, custom_def=None,
                          prop_name=None):
        '''Validate value with given type.

        If type is list or map, validate its entry by entry_schema(if defined)
        If type is a user-defined complex datatype, custom_def is required.
        '''
        from toscaparser.functions import is_function
        if is_function(value):
            return value
        if type == Schema.STRING:
            return validateutils.validate_string(value)
        elif type == Schema.INTEGER:
            return validateutils.validate_integer(value)
        elif type == Schema.FLOAT:
            return validateutils.validate_float(value)
        elif type == Schema.NUMBER:
            return validateutils.validate_numeric(value)
        elif type == Schema.BOOLEAN:
            return validateutils.validate_boolean(value)
        elif type == Schema.RANGE:
            return validateutils.validate_range(value)
        elif type == Schema.TIMESTAMP:
            validateutils.validate_timestamp(value)
            return value
        elif type == Schema.LIST:
            validateutils.validate_list(value)
            if entry_schema:
                DataEntity.validate_entry(value, entry_schema, custom_def)
            return value
        elif type == Schema.SCALAR_UNIT_SIZE:
            return ScalarUnit_Size(value).validate_scalar_unit()
        elif type == Schema.SCALAR_UNIT_FREQUENCY:
            return ScalarUnit_Frequency(value).validate_scalar_unit()
        elif type == Schema.SCALAR_UNIT_TIME:
            return ScalarUnit_Time(value).validate_scalar_unit()
        elif type == Schema.VERSION:
            return validateutils.TOSCAVersionProperty(value).get_version()
        elif type == Schema.MAP:
            validateutils.validate_map(value)
            if entry_schema:
                DataEntity.validate_entry(value, entry_schema, custom_def)
            return value
        elif type == Schema.PORTSPEC:
            # tODO(TBD) bug 1567063, validate source & target as PortDef type
            # as complex types not just as integers
            PortSpec.validate_additional_req(value, prop_name, custom_def)
        else:
            data = DataEntity(type, value, custom_def)
            return data.validate()

    @staticmethod
    def validate_entry(value, entry_schema, custom_def=None):
        '''Validate entries for map and list.'''
        schema = Schema(None, entry_schema)
        valuelist = value
        if isinstance(value, dict):
            valuelist = list(value.values())
        for v in valuelist:
            DataEntity.validate_datatype(schema.type, v, schema.entry_schema,
                                         custom_def)
            if schema.constraints:
                for constraint in schema.constraints:
                    constraint.validate(v)
        return value
*/

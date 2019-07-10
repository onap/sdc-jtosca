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

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DataType extends StatefulEntityType {

    LinkedHashMap<String, Object> customDef;

    public DataType(String _dataTypeName, LinkedHashMap<String, Object> _customDef) {
        super(_dataTypeName, DATATYPE_NETWORK_PREFIX, _customDef);

        customDef = _customDef;
    }

    public DataType getParentType() {
        // Return a datatype this datatype is derived from
        if (defs != null) {
            String ptype = derivedFrom(defs);
            if (ptype != null) {
                return new DataType(ptype, customDef);
            }
        }
        return null;
    }

    public String getValueType() {
        // Return 'type' section in the datatype schema
        if (defs != null) {
            return (String) entityValue(defs, "type");
        }
        return null;
    }

    public ArrayList<PropertyDef> getAllPropertiesObjects() {
        //Return all properties objects defined in type and parent type
        ArrayList<PropertyDef> propsDef = getPropertiesDefObjects();
        DataType ptype = getParentType();
        while (ptype != null) {
            propsDef.addAll(ptype.getPropertiesDefObjects());
            ptype = ptype.getParentType();
        }
        return propsDef;
    }

    public LinkedHashMap<String, PropertyDef> getAllProperties() {
        // Return a dictionary of all property definition name-object pairs
        LinkedHashMap<String, PropertyDef> pno = new LinkedHashMap<>();
        for (PropertyDef pd : getAllPropertiesObjects()) {
            pno.put(pd.getName(), pd);
        }
        return pno;
    }

    public Object getAllPropertyValue(String name) {
        // Return the value of a given property name
        LinkedHashMap<String, PropertyDef> propsDef = getAllProperties();
        if (propsDef != null && propsDef.get(name) != null) {
            return propsDef.get(name).getPDValue();
        }
        return null;
    }

    public LinkedHashMap<String, Object> getDefs() {
        return defs;
    }

}

/*python

from toscaparser.elements.statefulentitytype import StatefulEntityType


class DataType(StatefulEntityType):
    '''TOSCA built-in and user defined complex data type.'''

    def __init__(self, datatypename, custom_def=None):
        super(DataType, self).__init__(datatypename,
                                       self.DATATYPE_NETWORK_PREFIX,
                                       custom_def)
        self.custom_def = custom_def

    @property
    def parent_type(self):
        '''Return a datatype this datatype is derived from.'''
        ptype = self.derived_from(self.defs)
        if ptype:
            return DataType(ptype, self.custom_def)
        return None

    @property
    def value_type(self):
        '''Return 'type' section in the datatype schema.'''
        return self.entity_value(self.defs, 'type')

    def get_all_properties_objects(self):
        '''Return all properties objects defined in type and parent type.'''
        props_def = self.get_properties_def_objects()
        ptype = self.parent_type
        while ptype:
            props_def.extend(ptype.get_properties_def_objects())
            ptype = ptype.parent_type
        return props_def

    def get_all_properties(self):
        '''Return a dictionary of all property definition name-object pairs.'''
        return {prop.name: prop
                for prop in self.get_all_properties_objects()}

    def get_all_property_value(self, name):
        '''Return the value of a given property name.'''
        props_def = self.get_all_properties()
        if props_def and name in props_def.key():
            return props_def[name].value
*/

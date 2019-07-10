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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.onap.sdc.toscaparser.api.elements.CapabilityTypeDef;
import org.onap.sdc.toscaparser.api.elements.PropertyDef;

public class CapabilityAssignment {

    private String name;
    private LinkedHashMap<String, Object> _properties;
    private CapabilityTypeDef _definition;
    private LinkedHashMap<String, Object> _customDef;

    public CapabilityAssignment(String cname,
                                LinkedHashMap<String, Object> cproperties,
                                CapabilityTypeDef cdefinition, LinkedHashMap<String, Object> customDef) {
        name = cname;
        _properties = cproperties;
        _definition = cdefinition;
        _customDef = customDef;
    }

    /**
     * Get the properties list for capability
     *
     * @return list of property objects for capability
     */
    public ArrayList<Property> getPropertiesObjects() {
        // Return a list of property objects
        ArrayList<Property> properties = new ArrayList<Property>();
        LinkedHashMap<String, Object> props = _properties;
        if (props != null) {
            for (Map.Entry<String, Object> me : props.entrySet()) {
                String pname = me.getKey();
                Object pvalue = me.getValue();

                LinkedHashMap<String, PropertyDef> propsDef = _definition.getPropertiesDef();
                if (propsDef != null) {
                    PropertyDef pd = (PropertyDef) propsDef.get(pname);
                    if (pd != null) {
                        properties.add(new Property(pname, pvalue, pd.getSchema(), _customDef));
                    }
                }
            }
        }
        return properties;
    }

    /**
     * Get the map of properties
     *
     * @return map of all properties contains dictionary of property name and property object
     */
    public LinkedHashMap<String, Property> getProperties() {
        // Return a dictionary of property name-object pairs
        LinkedHashMap<String, Property> npps = new LinkedHashMap<>();
        for (Property p : getPropertiesObjects()) {
            npps.put(p.getName(), p);
        }
        return npps;
    }

    /**
     * Get the property value by name
     *
     * @param pname - the property name for capability
     * @return the property value for this name
     */
    public Object getPropertyValue(String pname) {
        // Return the value of a given property name
        LinkedHashMap<String, Property> props = getProperties();
        if (props != null && props.get(pname) != null) {
            return props.get(name).getValue();
        }
        return null;
    }

    /**
     * Get the name for capability
     *
     * @return the name for capability
     */
    public String getName() {
        return name;
    }

    /**
     * Get the definition for capability
     *
     * @return CapabilityTypeDef - contain definition for capability
     */
    public CapabilityTypeDef getDefinition() {
        return _definition;
    }

    /**
     * Set the property for capability
     *
     * @param pname  - the property name for capability to set
     * @param pvalue - the property valiue for capability to set
     */
    public void setProperty(String pname, Object pvalue) {
        _properties.put(pname, pvalue);
    }

    @Override
    public String toString() {
        return "CapabilityAssignment{" +
                "name='" + name + '\'' +
                ", _properties=" + _properties +
                ", _definition=" + _definition +
                '}';
    }
}

/*python

from toscaparser.properties import Property


class CapabilityAssignment(object):
    '''TOSCA built-in capabilities type.'''

    def __init__(self, name, properties, definition):
        self.name = name
        self._properties = properties
        self.definition = definition

    def get_properties_objects(self):
        '''Return a list of property objects.'''
        properties = []
        props = self._properties
        if props:
            for name, value in props.items():
                props_def = self.definition.get_properties_def()
                if props_def and name in props_def:
                    properties.append(Property(name, value,
                                               props_def[name].schema))
        return properties

    def get_properties(self):
        '''Return a dictionary of property name-object pairs.'''
        return {prop.name: prop
                for prop in self.get_properties_objects()}

    def get_property_value(self, name):
        '''Return the value of a given property name.'''
        props = self.get_properties()
        if props and name in props:
            return props[name].value
*/ 

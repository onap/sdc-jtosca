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
import java.util.Map;

public class CapabilityTypeDef extends StatefulEntityType {
    // TOSCA built-in capabilities type

    private static final String TOSCA_TYPEURI_CAPABILITY_ROOT = "tosca.capabilities.Root";

    private String name;
    private String nodetype;
    private LinkedHashMap<String, Object> customDef;
    private LinkedHashMap<String, Object> properties;
    private LinkedHashMap<String, Object> parentCapabilities;

    @SuppressWarnings("unchecked")
    public CapabilityTypeDef(String cname, String ctype, String ntype, LinkedHashMap<String, Object> ccustomDef) {
        super(ctype, CAPABILITY_PREFIX, ccustomDef);

        name = cname;
        nodetype = ntype;
        properties = null;
        customDef = ccustomDef;
        if (defs != null) {
            properties = (LinkedHashMap<String, Object>) defs.get(PROPERTIES);
        }
        parentCapabilities = getParentCapabilities(customDef);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<PropertyDef> getPropertiesDefObjects() {
        // Return a list of property definition objects
        ArrayList<PropertyDef> propsdefs = new ArrayList<>();
        LinkedHashMap<String, Object> parentProperties = new LinkedHashMap<>();
        if (parentCapabilities != null) {
            for (Map.Entry<String, Object> me : parentCapabilities.entrySet()) {
                parentProperties.put(me.getKey(), ((LinkedHashMap<String, Object>) me.getValue()).get("properties"));
            }
        }
        if (properties != null) {
            for (Map.Entry<String, Object> me : properties.entrySet()) {
                propsdefs.add(new PropertyDef(me.getKey(), null, (LinkedHashMap<String, Object>) me.getValue()));
            }
        }
        if (parentProperties != null) {
            for (Map.Entry<String, Object> me : parentProperties.entrySet()) {
                LinkedHashMap<String, Object> props = (LinkedHashMap<String, Object>) me.getValue();
                if (props != null) {
                    for (Map.Entry<String, Object> pe : props.entrySet()) {
                        String prop = pe.getKey();
                        LinkedHashMap<String, Object> schema = (LinkedHashMap<String, Object>) pe.getValue();
                        // add parent property if not overridden by children type
                        if (properties == null || properties.get(prop) == null) {
                            propsdefs.add(new PropertyDef(prop, null, schema));
                        }
                    }
                }
            }
        }
        return propsdefs;
    }

    public LinkedHashMap<String, PropertyDef> getPropertiesDef() {
        LinkedHashMap<String, PropertyDef> pds = new LinkedHashMap<>();
        for (PropertyDef pd : getPropertiesDefObjects()) {
            pds.put(pd.getName(), pd);
        }
        return pds;
    }

    public PropertyDef getPropertyDefValue(String pdname) {
        // Return the definition of a given property name
        LinkedHashMap<String, PropertyDef> propsDef = getPropertiesDef();
        if (propsDef != null && propsDef.get(pdname) != null) {
            return (PropertyDef) propsDef.get(pdname).getPDValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> getParentCapabilities(LinkedHashMap<String, Object> customDef) {
        LinkedHashMap<String, Object> capabilities = new LinkedHashMap<>();
        CapabilityTypeDef parentCap = getParentType();
        if (parentCap != null) {
            String sParentCap = parentCap.getType();
            while (!sParentCap.equals(TOSCA_TYPEURI_CAPABILITY_ROOT)) {
                if (TOSCA_DEF.get(sParentCap) != null) {
                    capabilities.put(sParentCap, TOSCA_DEF.get(sParentCap));
                } else if (customDef != null && customDef.get(sParentCap) != null) {
                    capabilities.put(sParentCap, customDef.get(sParentCap));
                }
                sParentCap = (String) ((LinkedHashMap<String, Object>) capabilities.get(sParentCap)).get("derived_from");
            }
        }
        return capabilities;
    }

    public CapabilityTypeDef getParentType() {
        // Return a capability this capability is derived from
        if (defs == null) {
            return null;
        }
        String pnode = derivedFrom(defs);
        if (pnode != null && !pnode.isEmpty()) {
            return new CapabilityTypeDef(name, pnode, nodetype, customDef);
        }
        return null;
    }

    public boolean inheritsFrom(ArrayList<String> typeNames) {
        // Check this capability is in type_names

        // Check if this capability or some of its parent types
        // are in the list of types: type_names
        if (typeNames.contains(getType())) {
            return true;
        } else if (getParentType() != null) {
            return getParentType().inheritsFrom(typeNames);
        }
        return false;
    }

    // getters/setters

    public LinkedHashMap<String, Object> getProperties() {
        return properties;
    }

    public String getName() {
        return name;
    }
}

/*python
from toscaparser.elements.property_definition import PropertyDef
from toscaparser.elements.statefulentitytype import StatefulEntityType


class CapabilityTypeDef(StatefulEntityType):
    '''TOSCA built-in capabilities type.'''
    TOSCA_TYPEURI_CAPABILITY_ROOT = 'tosca.capabilities.Root'

    def __init__(self, name, ctype, ntype, custom_def=None):
        self.name = name
        super(CapabilityTypeDef, self).__init__(ctype, self.CAPABILITY_PREFIX,
                                                custom_def)
        self.nodetype = ntype
        self.properties = None
        self.custom_def = custom_def
        if self.PROPERTIES in self.defs:
            self.properties = self.defs[self.PROPERTIES]
        self.parent_capabilities = self._get_parent_capabilities(custom_def)

    def get_properties_def_objects(self):
        '''Return a list of property definition objects.'''
        properties = []
        parent_properties = {}
        if self.parent_capabilities:
            for type, value in self.parent_capabilities.items():
                parent_properties[type] = value.get('properties')
        if self.properties:
            for prop, schema in self.properties.items():
                properties.append(PropertyDef(prop, None, schema))
        if parent_properties:
            for parent, props in parent_properties.items():
                for prop, schema in props.items():
                    # add parent property if not overridden by children type
                    if not self.properties or \
                            prop not in self.properties.keys():
                        properties.append(PropertyDef(prop, None, schema))
        return properties

    def get_properties_def(self):
        '''Return a dictionary of property definition name-object pairs.'''
        return {prop.name: prop
                for prop in self.get_properties_def_objects()}

    def get_property_def_value(self, name):
        '''Return the definition of a given property name.'''
        props_def = self.get_properties_def()
        if props_def and name in props_def:
            return props_def[name].value

    def _get_parent_capabilities(self, custom_def=None):
        capabilities = {}
        parent_cap = self.parent_type
        if parent_cap:
            parent_cap = parent_cap.type
            while parent_cap != self.TOSCA_TYPEURI_CAPABILITY_ROOT:
                if parent_cap in self.TOSCA_DEF.keys():
                    capabilities[parent_cap] = self.TOSCA_DEF[parent_cap]
                elif custom_def and parent_cap in custom_def.keys():
                    capabilities[parent_cap] = custom_def[parent_cap]
                parent_cap = capabilities[parent_cap]['derived_from']
        return capabilities

    @property
    def parent_type(self):
        '''Return a capability this capability is derived from.'''
        if not hasattr(self, 'defs'):
            return None
        pnode = self.derived_from(self.defs)
        if pnode:
            return CapabilityTypeDef(self.name, pnode,
                                     self.nodetype, self.custom_def)

    def inherits_from(self, type_names):
        '''Check this capability is in type_names

           Check if this capability or some of its parent types
           are in the list of types: type_names
        '''
        if self.type in type_names:
            return True
        elif self.parent_type:
            return self.parent_type.inherits_from(type_names)
        else:
            return False*/

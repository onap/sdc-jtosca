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

import org.onap.sdc.toscaparser.api.UnsupportedType;
import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


public class StatefulEntityType extends EntityType {
    // Class representing TOSCA states

    public static final String[] INTERFACE_NODE_LIFECYCLE_OPERATIONS = {
            "create", "configure", "start", "stop", "delete"};

    public static final String[] INTERFACE_RELATIONSHIP_CONFIGURE_OPERATIONS = {
            "post_configure_source", "post_configure_target", "add_target", "remove_target"};

    public StatefulEntityType() {
        // void constructor for subclasses that don't want super
    }

    @SuppressWarnings("unchecked")
    public StatefulEntityType(String entityType, String prefix, LinkedHashMap<String, Object> customDef) {

        String entireEntityType = entityType;
        if (UnsupportedType.validateType(entireEntityType)) {
            defs = null;
        } else {
            if (entityType.startsWith(TOSCA + ":")) {
                entityType = entityType.substring(TOSCA.length() + 1);
                entireEntityType = prefix + entityType;
            }
            if (!entityType.startsWith(TOSCA)) {
                entireEntityType = prefix + entityType;
            }
            if (TOSCA_DEF.get(entireEntityType) != null) {
                defs = (LinkedHashMap<String, Object>) TOSCA_DEF.get(entireEntityType);
                entityType = entireEntityType;
            } else if (customDef != null && customDef.get(entityType) != null) {
                defs = (LinkedHashMap<String, Object>) customDef.get(entityType);
            } else {
                defs = null;
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE136", String.format(
                        "InvalidTypeError: \"%s\" is not a valid type", entityType)));
            }
        }
        type = entityType;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<PropertyDef> getPropertiesDefObjects() {
        // Return a list of property definition objects
        ArrayList<PropertyDef> properties = new ArrayList<PropertyDef>();
        LinkedHashMap<String, Object> props = (LinkedHashMap<String, Object>) getDefinition(PROPERTIES);
        if (props != null) {
            for (Map.Entry<String, Object> me : props.entrySet()) {
                String pdname = me.getKey();
                Object to = me.getValue();
                if (to == null || !(to instanceof LinkedHashMap)) {
                    String s = to == null ? "null" : to.getClass().getSimpleName();
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE137", String.format(
                            "Unexpected type error: property \"%s\" has type \"%s\" (expected dict)", pdname, s)));
                    continue;
                }
                LinkedHashMap<String, Object> pdschema = (LinkedHashMap<String, Object>) to;
                properties.add(new PropertyDef(pdname, null, pdschema));
            }
        }
        return properties;
    }

    public LinkedHashMap<String, PropertyDef> getPropertiesDef() {
        LinkedHashMap<String, PropertyDef> pds = new LinkedHashMap<String, PropertyDef>();
        for (PropertyDef pd : getPropertiesDefObjects()) {
            pds.put(pd.getName(), pd);
        }
        return pds;
    }

    public PropertyDef getPropertyDefValue(String name) {
        // Return the property definition associated with a given name
        PropertyDef pd = null;
        LinkedHashMap<String, PropertyDef> propsDef = getPropertiesDef();
        if (propsDef != null) {
            pd = propsDef.get(name);
        }
        return pd;
    }

    public ArrayList<AttributeDef> getAttributesDefObjects() {
        // Return a list of attribute definition objects
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> attrs = (LinkedHashMap<String, Object>) getValue(ATTRIBUTES, null, true);
        ArrayList<AttributeDef> ads = new ArrayList<>();
        if (attrs != null) {
            for (Map.Entry<String, Object> me : attrs.entrySet()) {
                String attr = me.getKey();
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> adschema = (LinkedHashMap<String, Object>) me.getValue();
                ads.add(new AttributeDef(attr, null, adschema));
            }
        }
        return ads;
    }

    public LinkedHashMap<String, AttributeDef> getAttributesDef() {
        // Return a dictionary of attribute definition name-object pairs

        LinkedHashMap<String, AttributeDef> ads = new LinkedHashMap<>();
        for (AttributeDef ado : getAttributesDefObjects()) {
            ads.put(((AttributeDef) ado).getName(), ado);
        }
        return ads;
    }

    public AttributeDef getAttributeDefValue(String name) {
        // Return the attribute definition associated with a given name
        AttributeDef ad = null;
        LinkedHashMap<String, AttributeDef> attrsDef = getAttributesDef();
        if (attrsDef != null) {
            ad = attrsDef.get(name);
        }
        return ad;
    }

    public String getType() {
        return type;
    }
}

/*python

from toscaparser.common.exception import InvalidTypeError
from toscaparser.elements.attribute_definition import AttributeDef
from toscaparser.elements.entity_type import EntityType
from toscaparser.elements.property_definition import PropertyDef
from toscaparser.unsupportedtype import UnsupportedType


class StatefulEntityType(EntityType):
    '''Class representing TOSCA states.'''

    interfaces_node_lifecycle_operations = ['create',
                                            'configure', 'start',
                                            'stop', 'delete']

    interfaces_relationship_configure_operations = ['post_configure_source',
                                                    'post_configure_target',
                                                    'add_target',
                                                    'remove_target']

    def __init__(self, entitytype, prefix, custom_def=None):
        entire_entitytype = entitytype
        if UnsupportedType.validate_type(entire_entitytype):
            self.defs = None
        else:
            if entitytype.startswith(self.TOSCA + ":"):
                entitytype = entitytype[(len(self.TOSCA) + 1):]
                entire_entitytype = prefix + entitytype
            if not entitytype.startswith(self.TOSCA):
                entire_entitytype = prefix + entitytype
            if entire_entitytype in list(self.TOSCA_DEF.keys()):
                self.defs = self.TOSCA_DEF[entire_entitytype]
                entitytype = entire_entitytype
            elif custom_def and entitytype in list(custom_def.keys()):
                self.defs = custom_def[entitytype]
            else:
                self.defs = None
                ValidationIssueCollector.appendException(
                    InvalidTypeError(what=entitytype))
        self.type = entitytype

    def get_properties_def_objects(self):
        '''Return a list of property definition objects.'''
        properties = []
        props = self.get_definition(self.PROPERTIES)
        if props:
            for prop, schema in props.items():
                properties.append(PropertyDef(prop, None, schema))
        return properties

    def get_properties_def(self):
        '''Return a dictionary of property definition name-object pairs.'''
        return {prop.name: prop
                for prop in self.get_properties_def_objects()}

    def get_property_def_value(self, name):
        '''Return the property definition associated with a given name.'''
        props_def = self.get_properties_def()
        if props_def and name in props_def.keys():
            return props_def[name].value

    def get_attributes_def_objects(self):
        '''Return a list of attribute definition objects.'''
        attrs = self.get_value(self.ATTRIBUTES, parent=True)
        if attrs:
            return [AttributeDef(attr, None, schema)
                    for attr, schema in attrs.items()]
        return []

    def get_attributes_def(self):
        '''Return a dictionary of attribute definition name-object pairs.'''
        return {attr.name: attr
                for attr in self.get_attributes_def_objects()}

    def get_attribute_def_value(self, name):
        '''Return the attribute definition associated with a given name.'''
        attrs_def = self.get_attributes_def()
        if attrs_def and name in attrs_def.keys():
            return attrs_def[name].value
*/

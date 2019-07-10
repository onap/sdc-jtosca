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

import org.onap.sdc.toscaparser.api.elements.PropertyDef;
import org.onap.sdc.toscaparser.api.elements.StatefulEntityType;
import org.onap.sdc.toscaparser.api.elements.EntityType;

public class RelationshipTemplate extends EntityTemplate {

    private static final String DERIVED_FROM = "derived_from";
    private static final String PROPERTIES = "properties";
    private static final String REQUIREMENTS = "requirements";
    private static final String INTERFACES = "interfaces";
    private static final String CAPABILITIES = "capabilities";
    private static final String TYPE = "type";
    @SuppressWarnings("unused")
    private static final String SECTIONS[] = {
            DERIVED_FROM, PROPERTIES, REQUIREMENTS, INTERFACES, CAPABILITIES, TYPE};

    private String name;
    private NodeTemplate target;
    private NodeTemplate source;
    private ArrayList<Property> _properties;

    public RelationshipTemplate(LinkedHashMap<String, Object> rtrelationshipTemplate,
                                String rtname,
                                LinkedHashMap<String, Object> rtcustomDef,
                                NodeTemplate rttarget,
                                NodeTemplate rtsource) {
        this(rtrelationshipTemplate, rtname, rtcustomDef, rttarget, rtsource, null);
    }

    public RelationshipTemplate(LinkedHashMap<String, Object> rtrelationshipTemplate,
                                String rtname,
                                LinkedHashMap<String, Object> rtcustomDef,
                                NodeTemplate rttarget,
                                NodeTemplate rtsource, NodeTemplate parentNodeTemplate) {
        super(rtname, rtrelationshipTemplate, "relationship_type", rtcustomDef, parentNodeTemplate);

        name = rtname;
        target = rttarget;
        source = rtsource;
        _properties = null;
    }

    public ArrayList<Property> getPropertiesObjects() {
        // Return properties objects for this template
        if (_properties == null) {
            _properties = _createRelationshipProperties();
        }
        return _properties;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public ArrayList<Property> _createRelationshipProperties() {
        ArrayList<Property> props = new ArrayList<Property>();
        LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> relationship = (LinkedHashMap<String, Object>) entityTpl.get("relationship");

        if (relationship == null) {
            for (Object val : entityTpl.values()) {
                if (val instanceof LinkedHashMap) {
                    relationship = (LinkedHashMap<String, Object>) ((LinkedHashMap<String, Object>) val).get("relationship");
                    break;
                }
            }
        }

        if (relationship != null) {
            properties = (LinkedHashMap<String, Object>) ((EntityType) typeDefinition).getValue(PROPERTIES, relationship, false);
        }
        if (properties == null) {
            properties = new LinkedHashMap<String, Object>();
        }
        if (properties == null) {
            properties = (LinkedHashMap<String, Object>) entityTpl.get(PROPERTIES);
        }
        if (properties == null) {
            properties = new LinkedHashMap<String, Object>();
        }

        if (properties != null) {
            for (Map.Entry<String, Object> me : properties.entrySet()) {
                String pname = me.getKey();
                Object pvalue = me.getValue();
                LinkedHashMap<String, PropertyDef> propsDef = ((StatefulEntityType) typeDefinition).getPropertiesDef();
                if (propsDef != null && propsDef.get(pname) != null) {
                    if (properties.get(pname) != null) {
                        pvalue = properties.get(name);
                    }
                    PropertyDef pd = (PropertyDef) propsDef.get(pname);
                    Property prop = new Property(pname, pvalue, pd.getSchema(), customDef);
                    props.add(prop);
                }
            }
        }
        ArrayList<PropertyDef> pds = ((StatefulEntityType) typeDefinition).getPropertiesDefObjects();
        for (PropertyDef p : pds) {
            if (p.getDefault() != null && properties.get(p.getName()) == null) {
                Property prop = new Property(p.getName(), (LinkedHashMap<String, Object>) p.getDefault(), p.getSchema(), customDef);
                props.add(prop);
            }
        }
        return props;
    }

    public void validate() {
        _validateProperties(entityTpl, (StatefulEntityType) typeDefinition);
    }

    // getters/setters
    public NodeTemplate getTarget() {
        return target;
    }

    public NodeTemplate getSource() {
        return source;
    }

    public void setSource(NodeTemplate nt) {
        source = nt;
    }

    public void setTarget(NodeTemplate nt) {
        target = nt;
    }

    @Override
    public String toString() {
        return "RelationshipTemplate{" +
                "name='" + name + '\'' +
                ", target=" + target.getName() +
                ", source=" + source.getName() +
                ", _properties=" + _properties +
                '}';
    }

}

/*python

from toscaparser.entity_template import EntityTemplate
from toscaparser.properties import Property

SECTIONS = (DERIVED_FROM, PROPERTIES, REQUIREMENTS,
            INTERFACES, CAPABILITIES, TYPE) = \
           ('derived_from', 'properties', 'requirements', 'interfaces',
            'capabilities', 'type')

log = logging.getLogger('tosca')


class RelationshipTemplate(EntityTemplate):
    '''Relationship template.'''
    def __init__(self, relationship_template, name, custom_def=None,
                 target=None, source=None):
        super(RelationshipTemplate, self).__init__(name,
                                                   relationship_template,
                                                   'relationship_type',
                                                   custom_def)
        self.name = name.lower()
        self.target = target
        self.source = source

    def get_properties_objects(self):
        '''Return properties objects for this template.'''
        if self._properties is None:
            self._properties = self._create_relationship_properties()
        return self._properties

    def _create_relationship_properties(self):
        props = []
        properties = {}
        relationship = self.entity_tpl.get('relationship')

        if not relationship:
            for value in self.entity_tpl.values():
                if isinstance(value, dict):
                    relationship = value.get('relationship')
                    break

        if relationship:
            properties = self.type_definition.get_value(self.PROPERTIES,
                                                        relationship) or {}
        if not properties:
            properties = self.entity_tpl.get(self.PROPERTIES) or {}

        if properties:
            for name, value in properties.items():
                props_def = self.type_definition.get_properties_def()
                if props_def and name in props_def:
                    if name in properties.keys():
                        value = properties.get(name)
                    prop = Property(name, value,
                                    props_def[name].schema, self.custom_def)
                    props.append(prop)
        for p in self.type_definition.get_properties_def_objects():
            if p.default is not None and p.name not in properties.keys():
                prop = Property(p.name, p.default, p.schema, self.custom_def)
                props.append(prop)
        return props

    def validate(self):
        self._validate_properties(self.entity_tpl, self.type_definition)*/

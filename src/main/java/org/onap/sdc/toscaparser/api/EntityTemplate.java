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
import org.onap.sdc.toscaparser.api.elements.*;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class EntityTemplate {
    // Base class for TOSCA templates

    protected static final String DERIVED_FROM = "derived_from";
    protected static final String PROPERTIES = "properties";
    protected static final String REQUIREMENTS = "requirements";
    protected static final String INTERFACES = "interfaces";
    protected static final String CAPABILITIES = "capabilities";
    protected static final String TYPE = "type";
    protected static final String DESCRIPTION = "description";
    protected static final String DIRECTIVES = "directives";
    protected static final String ATTRIBUTES = "attributes";
    protected static final String ARTIFACTS = "artifacts";
    protected static final String NODE_FILTER = "node_filter";
    protected static final String COPY = "copy";

    protected static final String SECTIONS[] = {
            DERIVED_FROM, PROPERTIES, REQUIREMENTS, INTERFACES,
            CAPABILITIES, TYPE, DESCRIPTION, DIRECTIVES,
            ATTRIBUTES, ARTIFACTS, NODE_FILTER, COPY};

    private static final String NODE = "node";
    private static final String CAPABILITY = "capability";
    private static final String RELATIONSHIP = "relationship";
    private static final String OCCURRENCES = "occurrences";

    protected static final String REQUIREMENTS_SECTION[] = {
            NODE, CAPABILITY, RELATIONSHIP, OCCURRENCES, NODE_FILTER};

    //# Special key names
    private static final String METADATA = "metadata";
    protected static final String SPECIAL_SECTIONS[] = {METADATA};

    protected String name;
    protected LinkedHashMap<String, Object> entityTpl;
    protected LinkedHashMap<String, Object> customDef;
    protected StatefulEntityType typeDefinition;
    private ArrayList<Property> _properties;
    private ArrayList<InterfacesDef> _interfaces;
    private ArrayList<RequirementAssignment> _requirements;
    private ArrayList<CapabilityAssignment> _capabilities;

    @Nullable
    private NodeTemplate _parentNodeTemplate;

    // dummy constructor for subclasses that don't want super
    public EntityTemplate() {
        return;
    }

    public EntityTemplate(String _name,
                          LinkedHashMap<String, Object> _template,
                          String _entityName,
                          LinkedHashMap<String, Object> _customDef) {
        this(_name, _template, _entityName, _customDef, null);
    }

    @SuppressWarnings("unchecked")
    public EntityTemplate(String _name,
                          LinkedHashMap<String, Object> _template,
                          String _entityName,
                          LinkedHashMap<String, Object> _customDef,
                          NodeTemplate parentNodeTemplate) {
        name = _name;
        entityTpl = _template;
        customDef = _customDef;
        _validateField(entityTpl);
        String type = (String) entityTpl.get("type");
        UnsupportedType.validateType(type);
        if (_entityName.equals("node_type")) {
            if (type != null) {
                typeDefinition = new NodeType(type, customDef);
            } else {
                typeDefinition = null;
            }
        }
        if (_entityName.equals("relationship_type")) {
            Object relationship = _template.get("relationship");
            type = null;
            if (relationship != null && relationship instanceof LinkedHashMap) {
                type = (String) ((LinkedHashMap<String, Object>) relationship).get("type");
            } else if (relationship instanceof String) {
                type = (String) entityTpl.get("relationship");
            } else {
                type = (String) entityTpl.get("type");
            }
            UnsupportedType.validateType(type);
            typeDefinition = new RelationshipType(type, null, customDef);
        }
        if (_entityName.equals("policy_type")) {
            if (type == null) {
                //msg = (_('Policy definition of "%(pname)s" must have'
                //       ' a "type" ''attribute.') % dict(pname=name))
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE140", String.format(
                        "ValidationError: Policy definition of \"%s\" must have a \"type\" attribute", name)));
            }
            typeDefinition = new PolicyType(type, customDef);
        }
        if (_entityName.equals("group_type")) {
            if (type != null) {
                typeDefinition = new GroupType(type, customDef);
            } else {
                typeDefinition = null;
            }
        }
        _properties = null;
        _interfaces = null;
        _requirements = null;
        _capabilities = null;
        _parentNodeTemplate = parentNodeTemplate;
    }

    public NodeTemplate getParentNodeTemplate() {
        return _parentNodeTemplate;
    }

    public String getType() {
        if (typeDefinition != null) {
            String clType = typeDefinition.getClass().getSimpleName();
            if (clType.equals("NodeType")) {
                return (String) ((NodeType) typeDefinition).getType();
            } else if (clType.equals("PolicyType")) {
                return (String) ((PolicyType) typeDefinition).getType();
            } else if (clType.equals("GroupType")) {
                return (String) ((GroupType) typeDefinition).getType();
            } else if (clType.equals("RelationshipType")) {
                return (String) ((RelationshipType) typeDefinition).getType();
            }
        }
        return null;
    }

    public Object getParentType() {
        if (typeDefinition != null) {
            String clType = typeDefinition.getClass().getSimpleName();
            if (clType.equals("NodeType")) {
                return ((NodeType) typeDefinition).getParentType();
            } else if (clType.equals("PolicyType")) {
                return ((PolicyType) typeDefinition).getParentType();
            } else if (clType.equals("GroupType")) {
                return ((GroupType) typeDefinition).getParentType();
            } else if (clType.equals("RelationshipType")) {
                return ((RelationshipType) typeDefinition).getParentType();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public RequirementAssignments getRequirements() {
        if (_requirements == null) {
            _requirements = _createRequirements();
        }
        return new RequirementAssignments(_requirements);
    }

    private ArrayList<RequirementAssignment> _createRequirements() {
        ArrayList<RequirementAssignment> reqs = new ArrayList<>();
        ArrayList<Map<String, Object>> requirements = (ArrayList<Map<String, Object>>)
                typeDefinition.getValue(REQUIREMENTS, entityTpl, false);
        if (requirements == null) {
            requirements = new ArrayList<>();
        }
        for (Map<String, Object> req : requirements) {
            for (String reqName : req.keySet()) {
                Object reqItem = req.get(reqName);
                if (reqItem instanceof LinkedHashMap) {
                    Object rel = ((LinkedHashMap<String, Object>) reqItem).get("relationship");
//					LinkedHashMap relationship = rel instanceof LinkedHashMap ? (LinkedHashMap) rel : null;
                    String nodeName = ((LinkedHashMap<String, Object>) reqItem).get("node").toString();
                    Object capability = ((LinkedHashMap<String, Object>) reqItem).get("capability");
                    String capabilityString = capability != null ? capability.toString() : null;

                    reqs.add(new RequirementAssignment(reqName, nodeName, capabilityString, rel));
                } else if (reqItem instanceof String) { //short notation
                    String nodeName = String.valueOf(reqItem);
                    reqs.add(new RequirementAssignment(reqName, nodeName));
                }
            }
        }
        return reqs;
    }

    public ArrayList<Property> getPropertiesObjects() {
        // Return properties objects for this template
        if (_properties == null) {
            _properties = _createProperties();
        }
        return _properties;
    }

    public LinkedHashMap<String, Property> getProperties() {
        LinkedHashMap<String, Property> props = new LinkedHashMap<>();
        for (Property po : getPropertiesObjects()) {
            props.put(po.getName(), po);
        }
        return props;
    }

    public Object getPropertyValue(String name) {
        LinkedHashMap<String, Property> props = getProperties();
        Property p = props.get(name);
        return p != null ? p.getValue() : null;
    }

    public String getPropertyType(String name) {
        Property property = getProperties().get(name);
        if (property != null) {
            return property.getType();
        }
        return null;
    }

    public ArrayList<InterfacesDef> getInterfaces() {
        if (_interfaces == null) {
            _interfaces = _createInterfaces();
        }
        return _interfaces;
    }

    public ArrayList<CapabilityAssignment> getCapabilitiesObjects() {
        // Return capabilities objects for this template
        if (_capabilities == null) {
            _capabilities = _createCapabilities();
        }
        return _capabilities;

    }

    public CapabilityAssignments getCapabilities() {
        LinkedHashMap<String, CapabilityAssignment> caps = new LinkedHashMap<String, CapabilityAssignment>();
        for (CapabilityAssignment cap : getCapabilitiesObjects()) {
            caps.put(cap.getName(), cap);
        }
        return new CapabilityAssignments(caps);
    }

    public boolean isDerivedFrom(String typeStr) {
        // Returns true if this object is derived from 'type_str'.
        // False otherwise

        if (getType() == null) {
            return false;
        } else if (getType().equals(typeStr)) {
            return true;
        } else if (getParentType() != null) {
            return ((EntityType) getParentType()).isDerivedFrom(typeStr);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<CapabilityAssignment> _createCapabilities() {
        ArrayList<CapabilityAssignment> capability = new ArrayList<CapabilityAssignment>();
        LinkedHashMap<String, Object> caps = (LinkedHashMap<String, Object>)
                ((EntityType) typeDefinition).getValue(CAPABILITIES, entityTpl, true);
        if (caps != null) {
            //?!? getCapabilities defined only for NodeType...
            LinkedHashMap<String, CapabilityTypeDef> capabilities = null;
            if (typeDefinition instanceof NodeType) {
                capabilities = ((NodeType) typeDefinition).getCapabilities();
            } else if (typeDefinition instanceof GroupType) {
                capabilities = ((GroupType) typeDefinition).getCapabilities();
            }
            for (Map.Entry<String, Object> me : caps.entrySet()) {
                String name = me.getKey();
                LinkedHashMap<String, Object> props = (LinkedHashMap<String, Object>) me.getValue();
                if (capabilities.get(name) != null) {
                    CapabilityTypeDef c = capabilities.get(name);  // a CapabilityTypeDef
                    LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();
                    // first use the definition default value
                    LinkedHashMap<String, Object> cprops = c.getProperties();
                    if (cprops != null) {
                        for (Map.Entry<String, Object> cpe : cprops.entrySet()) {
                            String propertyName = cpe.getKey();
                            LinkedHashMap<String, Object> propertyDef = (LinkedHashMap<String, Object>) cpe.getValue();
                            Object dob = propertyDef.get("default");
                            if (dob != null) {
                                properties.put(propertyName, dob);

                            }
                        }
                    }
                    // then update (if available) with the node properties
                    LinkedHashMap<String, Object> pp = (LinkedHashMap<String, Object>) props.get("properties");
                    if (pp != null) {
                        properties.putAll(pp);
                    }
                    CapabilityAssignment cap = new CapabilityAssignment(name, properties, c, customDef);
                    capability.add(cap);
                }
            }
        }
        return capability;
    }

    protected void _validateProperties(LinkedHashMap<String, Object> template, StatefulEntityType entityType) {
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>) entityType.getValue(PROPERTIES, template, false);
        _commonValidateProperties(entityType, properties);
    }

    protected void _validateCapabilities() {
        //BUG??? getCapabilities only defined in NodeType...
        LinkedHashMap<String, CapabilityTypeDef> typeCapabilities = ((NodeType) typeDefinition).getCapabilities();
        ArrayList<String> allowedCaps = new ArrayList<String>();
        if (typeCapabilities != null) {
            allowedCaps.addAll(typeCapabilities.keySet());
        }
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> capabilities = (LinkedHashMap<String, Object>)
                ((EntityType) typeDefinition).getValue(CAPABILITIES, entityTpl, false);
        if (capabilities != null) {
            _commonValidateField(capabilities, allowedCaps, "capabilities");
            _validateCapabilitiesProperties(capabilities);
        }
    }

    @SuppressWarnings("unchecked")
    private void _validateCapabilitiesProperties(LinkedHashMap<String, Object> capabilities) {
        for (Map.Entry<String, Object> me : capabilities.entrySet()) {
            String cap = me.getKey();
            LinkedHashMap<String, Object> props = (LinkedHashMap<String, Object>) me.getValue();
            CapabilityAssignment capability = getCapability(cap);
            if (capability == null) {
                continue;
            }
            CapabilityTypeDef capabilitydef = capability.getDefinition();
            _commonValidateProperties(capabilitydef, (LinkedHashMap<String, Object>) props.get(PROPERTIES));

            // validating capability properties values
            for (Property prop : getCapability(cap).getPropertiesObjects()) {
                prop.validate();

                if (cap.equals("scalable") && prop.getName().equals("default_instances")) {
                    LinkedHashMap<String, Object> propDict = (LinkedHashMap<String, Object>) props.get(PROPERTIES);
                    int minInstances = (int) propDict.get("min_instances");
                    int maxInstances = (int) propDict.get("max_instances");
                    int defaultInstances = (int) propDict.get("default_instances");
                    if (defaultInstances < minInstances || defaultInstances > maxInstances) {
                        //err_msg = ('"properties" of template "%s": '
                        //           '"default_instances" value is not between '
                        //           '"min_instances" and "max_instances".' %
                        //           self.name)
                        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE141", String.format(
                                "ValidationError: \"properties\" of template \"%s\": \"default_instances\" value is not between \"min_instances\" and \"max_instances\"",
                                name)));
                    }
                }
            }
        }
    }

    private void _commonValidateProperties(StatefulEntityType entityType, LinkedHashMap<String, Object> properties) {
        ArrayList<String> allowedProps = new ArrayList<String>();
        ArrayList<String> requiredProps = new ArrayList<String>();
        for (PropertyDef p : entityType.getPropertiesDefObjects()) {
            allowedProps.add(p.getName());
            // If property is 'required' and has no 'default' value then record
            if (p.isRequired() && p.getDefault() == null) {
                requiredProps.add(p.getName());
            }
        }
        // validate all required properties have values
        if (properties != null) {
            ArrayList<String> reqPropsNoValueOrDefault = new ArrayList<String>();
            _commonValidateField(properties, allowedProps, "properties");
            // make sure it's not missing any property required by a tosca type
            for (String r : requiredProps) {
                if (properties.get(r) == null) {
                    reqPropsNoValueOrDefault.add(r);
                }
            }
            // Required properties found without value or a default value
            if (!reqPropsNoValueOrDefault.isEmpty()) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE003", String.format(
                        "MissingRequiredFieldError: properties of template \"%s\" are missing field(s): %s",
                        name, reqPropsNoValueOrDefault.toString())));
            }
        } else {
            // Required properties in schema, but not in template
            if (!requiredProps.isEmpty()) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE004", String.format(
                        "MissingRequiredFieldError2: properties of template \"%s\" are missing field(s): %s",
                        name, requiredProps.toString())));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void _validateField(LinkedHashMap<String, Object> template) {
        if (!(template instanceof LinkedHashMap)) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE142", String.format(
                    "MissingRequiredFieldError: Template \"%s\" is missing required field \"%s\"", name, TYPE)));
            return;//???
        }
        boolean bBad = false;
        Object relationship = ((LinkedHashMap<String, Object>) template).get("relationship");
        if (relationship != null) {
            if (!(relationship instanceof String)) {
                bBad = (((LinkedHashMap<String, Object>) relationship).get(TYPE) == null);
            } else if (relationship instanceof String) {
                bBad = (template.get("relationship") == null);
            }
        } else {
            bBad = (template.get(TYPE) == null);
        }
        if (bBad) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE143", String.format(
                    "MissingRequiredFieldError: Template \"%s\" is missing required field \"%s\"", name, TYPE)));
        }
    }

    protected void _commonValidateField(LinkedHashMap<String, Object> schema, ArrayList<String> allowedList, String section) {
        for (String sname : schema.keySet()) {
            boolean bFound = false;
            for (String allowed : allowedList) {
                if (sname.equals(allowed)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE144", String.format(
                        "UnknownFieldError: Section \"%s\" of template \"%s\" contains unknown field \"%s\"", section, name, sname)));
            }
        }

    }

    @SuppressWarnings("unchecked")
    private ArrayList<Property> _createProperties() {
        ArrayList<Property> props = new ArrayList<Property>();
        LinkedHashMap<String, Object> properties = (LinkedHashMap<String, Object>)
                ((EntityType) typeDefinition).getValue(PROPERTIES, entityTpl, false);
        if (properties == null) {
            properties = new LinkedHashMap<String, Object>();
        }
        for (Map.Entry<String, Object> me : properties.entrySet()) {
            String pname = me.getKey();
            Object pvalue = me.getValue();
            LinkedHashMap<String, PropertyDef> propsDef = ((StatefulEntityType) typeDefinition).getPropertiesDef();
            if (propsDef != null && propsDef.get(pname) != null) {
                PropertyDef pd = (PropertyDef) propsDef.get(pname);
                Property prop = new Property(pname, pvalue, pd.getSchema(), customDef);
                props.add(prop);
            }
        }
        ArrayList<PropertyDef> pds = ((StatefulEntityType) typeDefinition).getPropertiesDefObjects();
        for (Object pdo : pds) {
            PropertyDef pd = (PropertyDef) pdo;
            if (pd.getDefault() != null && properties.get(pd.getName()) == null) {
                Property prop = new Property(pd.getName(), pd.getDefault(), pd.getSchema(), customDef);
                props.add(prop);
            }
        }
        return props;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<InterfacesDef> _createInterfaces() {
        ArrayList<InterfacesDef> interfaces = new ArrayList<>();
        LinkedHashMap<String, Object> typeInterfaces = new LinkedHashMap<String, Object>();
        if (typeDefinition instanceof RelationshipType) {
            if (entityTpl instanceof LinkedHashMap) {
                typeInterfaces = (LinkedHashMap<String, Object>) entityTpl.get(INTERFACES);
                if (typeInterfaces == null) {
                    for (String relName : entityTpl.keySet()) {
                        Object relValue = entityTpl.get(relName);
                        if (!relName.equals("type")) {
                            Object relDef = relValue;
                            LinkedHashMap<String, Object> rel = null;
                            if (relDef instanceof LinkedHashMap) {
                                Object relob = ((LinkedHashMap<String, Object>) relDef).get("relationship");
                                if (relob instanceof LinkedHashMap) {
                                    rel = (LinkedHashMap<String, Object>) relob;
                                }
                            }
                            if (rel != null) {
                                if (rel.get(INTERFACES) != null) {
                                    typeInterfaces = (LinkedHashMap<String, Object>) rel.get(INTERFACES);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            typeInterfaces = (LinkedHashMap<String, Object>)
                    ((EntityType) typeDefinition).getValue(INTERFACES, entityTpl, false);
        }
        if (typeInterfaces != null) {
            for (Map.Entry<String, Object> me : typeInterfaces.entrySet()) {
                String interfaceType = me.getKey();
                LinkedHashMap<String, Object> value = (LinkedHashMap<String, Object>) me.getValue();
                for (Map.Entry<String, Object> ve : value.entrySet()) {
                    String op = ve.getKey();
                    Object opDef = ve.getValue();
                    InterfacesDef iface = new InterfacesDef((EntityType) typeDefinition,
                            interfaceType,
                            this,
                            op,
                            opDef);
                    interfaces.add(iface);
                }

            }
        }
        return interfaces;
    }

    public CapabilityAssignment getCapability(String name) {
        // Provide named capability
        // :param name: name of capability
        // :return: capability object if found, None otherwise
        return getCapabilities().getCapabilityByName(name);
    }

    // getter
    public String getName() {
        return name;
    }

    public StatefulEntityType getTypeDefinition() {
        return typeDefinition;
    }

    public LinkedHashMap<String, Object> getCustomDef() {
        return customDef;
    }

    @Override
    public String toString() {
        return "EntityTemplate{" +
                "name='" + name + '\'' +
                ", entityTpl=" + entityTpl +
                ", customDef=" + customDef +
                ", typeDefinition=" + typeDefinition +
                ", _properties=" + _properties +
                ", _interfaces=" + _interfaces +
                ", _requirements=" + _requirements +
                ", _capabilities=" + _capabilities +
                '}';
    }
}

/*python

class EntityTemplate(object):
    '''Base class for TOSCA templates.'''

    SECTIONS = (DERIVED_FROM, PROPERTIES, REQUIREMENTS,
                INTERFACES, CAPABILITIES, TYPE, DESCRIPTION, DIRECTIVES,
                ATTRIBUTES, ARTIFACTS, NODE_FILTER, COPY) = \
               ('derived_from', 'properties', 'requirements', 'interfaces',
                'capabilities', 'type', 'description', 'directives',
                'attributes', 'artifacts', 'node_filter', 'copy')
    REQUIREMENTS_SECTION = (NODE, CAPABILITY, RELATIONSHIP, OCCURRENCES, NODE_FILTER) = \
                           ('node', 'capability', 'relationship',
                            'occurrences', 'node_filter')
    # Special key names
    SPECIAL_SECTIONS = (METADATA) = ('metadata')

    def __init__(self, name, template, entity_name, custom_def=None):
        self.name = name
        self.entity_tpl = template
        self.custom_def = custom_def
        self._validate_field(self.entity_tpl)
        type = self.entity_tpl.get('type')
        UnsupportedType.validate_type(type)
        if entity_name == 'node_type':
            self.type_definition = NodeType(type, custom_def) \
                if type is not None else None
        if entity_name == 'relationship_type':
            relationship = template.get('relationship')
            type = None
            if relationship and isinstance(relationship, dict):
                type = relationship.get('type')
            elif isinstance(relationship, str):
                type = self.entity_tpl['relationship']
            else:
                type = self.entity_tpl['type']
            UnsupportedType.validate_type(type)
            self.type_definition = RelationshipType(type,
                                                    None, custom_def)
        if entity_name == 'policy_type':
            if not type:
                msg = (_('Policy definition of "%(pname)s" must have'
                       ' a "type" ''attribute.') % dict(pname=name))
                ValidationIssueCollector.appendException(
                    ValidationError(msg))

            self.type_definition = PolicyType(type, custom_def)
        if entity_name == 'group_type':
            self.type_definition = GroupType(type, custom_def) \
                if type is not None else None
        self._properties = None
        self._interfaces = None
        self._requirements = None
        self._capabilities = None

    @property
    def type(self):
        if self.type_definition:
            return self.type_definition.type

    @property
    def parent_type(self):
        if self.type_definition:
            return self.type_definition.parent_type

    @property
    def requirements(self):
        if self._requirements is None:
            self._requirements = self.type_definition.get_value(
                self.REQUIREMENTS,
                self.entity_tpl) or []
        return self._requirements

    def get_properties_objects(self):
        '''Return properties objects for this template.'''
        if self._properties is None:
            self._properties = self._create_properties()
        return self._properties

    def get_properties(self):
        '''Return a dictionary of property name-object pairs.'''
        return {prop.name: prop
                for prop in self.get_properties_objects()}

    def get_property_value(self, name):
        '''Return the value of a given property name.'''
        props = self.get_properties()
        if props and name in props.keys():
            return props[name].value

    @property
    def interfaces(self):
        if self._interfaces is None:
            self._interfaces = self._create_interfaces()
        return self._interfaces

    def get_capabilities_objects(self):
        '''Return capabilities objects for this template.'''
        if not self._capabilities:
            self._capabilities = self._create_capabilities()
        return self._capabilities

    def get_capabilities(self):
        '''Return a dictionary of capability name-object pairs.'''
        return {cap.name: cap
                for cap in self.get_capabilities_objects()}

    def is_derived_from(self, type_str):
        '''Check if object inherits from the given type.

        Returns true if this object is derived from 'type_str'.
        False otherwise.
        '''
        if not self.type:
            return False
        elif self.type == type_str:
            return True
        elif self.parent_type:
            return self.parent_type.is_derived_from(type_str)
        else:
            return False

    def _create_capabilities(self):
        capability = []
        caps = self.type_definition.get_value(self.CAPABILITIES,
                                              self.entity_tpl, True)
        if caps:
            for name, props in caps.items():
                capabilities = self.type_definition.get_capabilities()
                if name in capabilities.keys():
                    c = capabilities[name]
                    properties = {}
                    # first use the definition default value
                    if c.properties:
                        for property_name in c.properties.keys():
                            prop_def = c.properties[property_name]
                            if 'default' in prop_def:
                                properties[property_name] = prop_def['default']
                    # then update (if available) with the node properties
                    if 'properties' in props and props['properties']:
                        properties.update(props['properties'])

                    cap = CapabilityAssignment(name, properties, c)
                    capability.append(cap)
        return capability

    def _validate_properties(self, template, entitytype):
        properties = entitytype.get_value(self.PROPERTIES, template)
        self._common_validate_properties(entitytype, properties)

    def _validate_capabilities(self):
        type_capabilities = self.type_definition.get_capabilities()
        allowed_caps = \
            type_capabilities.keys() if type_capabilities else []
        capabilities = self.type_definition.get_value(self.CAPABILITIES,
                                                      self.entity_tpl)
        if capabilities:
            self._common_validate_field(capabilities, allowed_caps,
                                        'capabilities')
            self._validate_capabilities_properties(capabilities)

    def _validate_capabilities_properties(self, capabilities):
        for cap, props in capabilities.items():
            capability = self.get_capability(cap)
            if not capability:
                continue
            capabilitydef = capability.definition
            self._common_validate_properties(capabilitydef,
                                             props[self.PROPERTIES])

            # validating capability properties values
            for prop in self.get_capability(cap).get_properties_objects():
                prop.validate()

                # tODO(srinivas_tadepalli): temporary work around to validate
                # default_instances until standardized in specification
                if cap == "scalable" and prop.name == "default_instances":
                    prop_dict = props[self.PROPERTIES]
                    min_instances = prop_dict.get("min_instances")
                    max_instances = prop_dict.get("max_instances")
                    default_instances = prop_dict.get("default_instances")
                    if not (min_instances <= default_instances
                            <= max_instances):
                        err_msg = ('"properties" of template "%s": '
                                   '"default_instances" value is not between '
                                   '"min_instances" and "max_instances".' %
                                   self.name)
                        ValidationIssueCollector.appendException(
                            ValidationError(message=err_msg))

    def _common_validate_properties(self, entitytype, properties):
        allowed_props = []
        required_props = []
        for p in entitytype.get_properties_def_objects():
            allowed_props.append(p.name)
            # If property is 'required' and has no 'default' value then record
            if p.required and p.default is None:
                required_props.append(p.name)
        # validate all required properties have values
        if properties:
            req_props_no_value_or_default = []
            self._common_validate_field(properties, allowed_props,
                                        'properties')
            # make sure it's not missing any property required by a tosca type
            for r in required_props:
                if r not in properties.keys():
                    req_props_no_value_or_default.append(r)
            # Required properties found without value or a default value
            if req_props_no_value_or_default:
                ValidationIssueCollector.appendException(
                    MissingRequiredFieldError(
                        what='"properties" of template "%s"' % self.name,
                        required=req_props_no_value_or_default))
        else:
            # Required properties in schema, but not in template
            if required_props:
                ValidationIssueCollector.appendException(
                    MissingRequiredFieldError(
                        what='"properties" of template "%s"' % self.name,
                        required=required_props))

    def _validate_field(self, template):
        if not isinstance(template, dict):
            ValidationIssueCollector.appendException(
                MissingRequiredFieldError(
                    what='Template "%s"' % self.name, required=self.TYPE))
        try:
            relationship = template.get('relationship')
            if relationship and not isinstance(relationship, str):
                relationship[self.TYPE]
            elif isinstance(relationship, str):
                template['relationship']
            else:
                template[self.TYPE]
        except KeyError:
            ValidationIssueCollector.appendException(
                MissingRequiredFieldError(
                    what='Template "%s"' % self.name, required=self.TYPE))

    def _common_validate_field(self, schema, allowedlist, section):
        for name in schema:
            if name not in allowedlist:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(
                        what=('"%(section)s" of template "%(nodename)s"'
                              % {'section': section, 'nodename': self.name}),
                        field=name))

    def _create_properties(self):
        props = []
        properties = self.type_definition.get_value(self.PROPERTIES,
                                                    self.entity_tpl) or {}
        for name, value in properties.items():
            props_def = self.type_definition.get_properties_def()
            if props_def and name in props_def:
                prop = Property(name, value,
                                props_def[name].schema, self.custom_def)
                props.append(prop)
        for p in self.type_definition.get_properties_def_objects():
            if p.default is not None and p.name not in properties.keys():
                prop = Property(p.name, p.default, p.schema, self.custom_def)
                props.append(prop)
        return props

    def _create_interfaces(self):
        interfaces = []
        type_interfaces = None
        if isinstance(self.type_definition, RelationshipType):
            if isinstance(self.entity_tpl, dict):
                if self.INTERFACES in self.entity_tpl:
                    type_interfaces = self.entity_tpl[self.INTERFACES]
                else:
                    for rel_def, value in self.entity_tpl.items():
                        if rel_def != 'type':
                            rel_def = self.entity_tpl.get(rel_def)
                            rel = None
                            if isinstance(rel_def, dict):
                                rel = rel_def.get('relationship')
                            if rel:
                                if self.INTERFACES in rel:
                                    type_interfaces = rel[self.INTERFACES]
                                    break
        else:
            type_interfaces = self.type_definition.get_value(self.INTERFACES,
                                                             self.entity_tpl)
        if type_interfaces:
            for interface_type, value in type_interfaces.items():
                for op, op_def in value.items():
                    iface = InterfacesDef(self.type_definition,
                                          interfacetype=interface_type,
                                          node_template=self,
                                          name=op,
                                          value=op_def)
                    interfaces.append(iface)
        return interfaces

    def get_capability(self, name):
        """Provide named capability

        :param name: name of capability
        :return: capability object if found, None otherwise
        """
        caps = self.get_capabilities()
        if caps and name in caps.keys():
            return caps[name]
*/

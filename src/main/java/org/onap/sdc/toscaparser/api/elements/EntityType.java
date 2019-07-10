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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.CopyUtils;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;
import org.onap.sdc.toscaparser.api.extensions.ExtTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class EntityType {

    private static Logger log = LoggerFactory.getLogger(EntityType.class.getName());

    private static final String TOSCA_DEFINITION_1_0_YAML = "TOSCA_definition_1_0.yaml";
    protected static final String DERIVED_FROM = "derived_from";
    protected static final String PROPERTIES = "properties";
    protected static final String ATTRIBUTES = "attributes";
    protected static final String REQUIREMENTS = "requirements";
    protected static final String INTERFACES = "interfaces";
    protected static final String CAPABILITIES = "capabilities";
    protected static final String TYPE = "type";
    protected static final String ARTIFACTS = "artifacts";

    @SuppressWarnings("unused")
    private static final String SECTIONS[] = {
            DERIVED_FROM, PROPERTIES, ATTRIBUTES, REQUIREMENTS,
            INTERFACES, CAPABILITIES, TYPE, ARTIFACTS
    };

    public static final String TOSCA_DEF_SECTIONS[] = {
            "node_types", "data_types", "artifact_types",
            "group_types", "relationship_types",
            "capability_types", "interface_types",
            "policy_types"};


    // TOSCA definition file
    //private final static String path = EntityType.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    //private final static String path =  EntityType.class.getClassLoader().getResource("TOSCA_definition_1_0.yaml").getFile();
    //private final static String TOSCA_DEF_FILE = EntityType.class.getClassLoader().getResourceAsStream("TOSCA_definition_1_0.yaml");

    private static LinkedHashMap<String, Object> TOSCA_DEF_LOAD_AS_IS = loadTdf();

    //EntityType.class.getClassLoader().getResourceAsStream("TOSCA_definition_1_0.yaml");

    @SuppressWarnings("unchecked")
    private static LinkedHashMap<String, Object> loadTdf() {
        String toscaDefLocation = EntityType.class.getClassLoader().getResource(TOSCA_DEFINITION_1_0_YAML).getFile();
        InputStream input = EntityType.class.getClassLoader().getResourceAsStream(TOSCA_DEFINITION_1_0_YAML);
        if (input == null) {
            log.error("EntityType - loadTdf - Couldn't load TOSCA_DEF_FILE {}", toscaDefLocation);
        }
        Yaml yaml = new Yaml();
        Object loaded = yaml.load(input);
        //@SuppressWarnings("unchecked")
        return (LinkedHashMap<String, Object>) loaded;
    }

    // Map of definition with pre-loaded values of TOSCA_DEF_FILE_SECTIONS
    public static LinkedHashMap<String, Object> TOSCA_DEF;

    static {
        TOSCA_DEF = new LinkedHashMap<String, Object>();
        for (String section : TOSCA_DEF_SECTIONS) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> value = (LinkedHashMap<String, Object>) TOSCA_DEF_LOAD_AS_IS.get(section);
            if (value != null) {
                for (String key : value.keySet()) {
                    TOSCA_DEF.put(key, value.get(key));
                }
            }
        }
    }

    public static final String DEPENDSON = "tosca.relationships.DependsOn";
    public static final String HOSTEDON = "tosca.relationships.HostedOn";
    public static final String CONNECTSTO = "tosca.relationships.ConnectsTo";
    public static final String ATTACHESTO = "tosca.relationships.AttachesTo";
    public static final String LINKSTO = "tosca.relationships.network.LinksTo";
    public static final String BINDSTO = "tosca.relationships.network.BindsTo";

    public static final String RELATIONSHIP_TYPE[] = {
            "tosca.relationships.DependsOn",
            "tosca.relationships.HostedOn",
            "tosca.relationships.ConnectsTo",
            "tosca.relationships.AttachesTo",
            "tosca.relationships.network.LinksTo",
            "tosca.relationships.network.BindsTo"};

    public static final String NODE_PREFIX = "tosca.nodes.";
    public static final String RELATIONSHIP_PREFIX = "tosca.relationships.";
    public static final String CAPABILITY_PREFIX = "tosca.capabilities.";
    public static final String INTERFACE_PREFIX = "tosca.interfaces.";
    public static final String ARTIFACT_PREFIX = "tosca.artifacts.";
    public static final String POLICY_PREFIX = "tosca.policies.";
    public static final String GROUP_PREFIX = "tosca.groups.";
    //currently the data types are defined only for network
    // but may have changes in the future.
    public static final String DATATYPE_PREFIX = "tosca.datatypes.";
    public static final String DATATYPE_NETWORK_PREFIX = DATATYPE_PREFIX + "network.";
    public static final String TOSCA = "tosca";

    protected String type;
    protected LinkedHashMap<String, Object> defs = null;

    public Object getParentType() {
        return null;
    }

    public String derivedFrom(LinkedHashMap<String, Object> defs) {
        // Return a type this type is derived from
        return (String) entityValue(defs, "derived_from");
    }

    public boolean isDerivedFrom(String type_str) {
        // Check if object inherits from the given type
        // Returns true if this object is derived from 'type_str'
        // False otherwise.
        if (type == null || this.type.isEmpty()) {
            return false;
        } else if (type == type_str) {
            return true;
        } else if (getParentType() != null) {
            return ((EntityType) getParentType()).isDerivedFrom(type_str);
        } else {
            return false;
        }
    }

    public Object entityValue(LinkedHashMap<String, Object> defs, String key) {
        if (defs != null) {
            return defs.get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object getValue(String ndtype, LinkedHashMap<String, Object> _defs, boolean parent) {
        Object value = null;
        if (_defs == null) {
            if (defs == null) {
                return null;
            }
            _defs = this.defs;
        }
        Object defndt = _defs.get(ndtype);
        if (defndt != null) {
            // copy the value to avoid that next operations add items in the
            // item definitions
            //value = copy.copy(defs[ndtype])
            value = CopyUtils.copyLhmOrAl(defndt);
        }

        if (parent) {
            EntityType p = this;
            if (p != null) {
                while (p != null) {
                    if (p.defs != null && p.defs.get(ndtype) != null) {
                        // get the parent value
                        Object parentValue = p.defs.get(ndtype);
                        if (value != null) {
                            if (value instanceof LinkedHashMap) {
                                for (Map.Entry<String, Object> me : ((LinkedHashMap<String, Object>) parentValue).entrySet()) {
                                    String k = me.getKey();
                                    if (((LinkedHashMap<String, Object>) value).get(k) == null) {
                                        ((LinkedHashMap<String, Object>) value).put(k, me.getValue());
                                    }
                                }
                            }
                            if (value instanceof ArrayList) {
                                for (Object pValue : (ArrayList<Object>) parentValue) {
                                    if (!((ArrayList<Object>) value).contains(pValue)) {
                                        ((ArrayList<Object>) value).add(pValue);
                                    }
                                }
                            }
                        } else {
                            // value = copy.copy(parent_value)
                            value = CopyUtils.copyLhmOrAl(parentValue);
                        }
                    }
                    p = (EntityType) p.getParentType();
                }
            }
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public Object getDefinition(String ndtype) {
        Object value = null;
        LinkedHashMap<String, Object> _defs;
        // no point in hasattr, because we have it, and it 
        // doesn't do anything except emit an exception anyway
        //if not hasattr(self, 'defs'):
        //    defs = None
        //    ValidationIssueCollector.appendException(
        //        ValidationError(message="defs is " + str(defs)))
        //else:
        //    defs = self.defs       	
        _defs = this.defs;


        if (_defs != null && _defs.get(ndtype) != null) {
            value = _defs.get(ndtype);
        }

        Object p = getParentType();
        if (p != null) {
            Object inherited = ((EntityType) p).getDefinition(ndtype);
            if (inherited != null) {
                // inherited = dict(inherited) WTF?!?
                if (value == null) {
                    value = inherited;
                } else {
                    //?????
                    //inherited.update(value)
                    //value.update(inherited)
                    for (Map.Entry<String, Object> me : ((LinkedHashMap<String, Object>) inherited).entrySet()) {
                        ((LinkedHashMap<String, Object>) value).put(me.getKey(), me.getValue());
                    }
                }
            }
        }
        return value;
    }

    public static void updateDefinitions(String version) {
        ExtTools exttools = new ExtTools();
        String extensionDefsFile = exttools.getDefsFile(version);

        try (InputStream input = EntityType.class.getClassLoader().getResourceAsStream(extensionDefsFile);) {
            Yaml yaml = new Yaml();
            LinkedHashMap<String, Object> nfvDefFile = (LinkedHashMap<String, Object>) yaml.load(input);
            LinkedHashMap<String, Object> nfvDef = new LinkedHashMap<>();
            for (String section : TOSCA_DEF_SECTIONS) {
                if (nfvDefFile.get(section) != null) {
                    LinkedHashMap<String, Object> value =
                            (LinkedHashMap<String, Object>) nfvDefFile.get(section);
                    for (String key : value.keySet()) {
                        nfvDef.put(key, value.get(key));
                    }
                }
            }
            TOSCA_DEF.putAll(nfvDef);
        } catch (IOException e) {
            log.error("EntityType - updateDefinitions - Failed to update definitions from defs file {}", extensionDefsFile);
            log.error("Exception:", e);
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE280",
                    String.format("Failed to update definitions from defs file \"%s\" ", extensionDefsFile)));
            return;
        }
    }
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import ValidationError
from toscaparser.extensions.exttools import ExtTools
import org.onap.sdc.toscaparser.api.utils.yamlparser

log = logging.getLogger('tosca')


class EntityType(object):
    '''Base class for TOSCA elements.'''

    SECTIONS = (DERIVED_FROM, PROPERTIES, ATTRIBUTES, REQUIREMENTS,
                INTERFACES, CAPABILITIES, TYPE, ARTIFACTS) = \
               ('derived_from', 'properties', 'attributes', 'requirements',
                'interfaces', 'capabilities', 'type', 'artifacts')

    TOSCA_DEF_SECTIONS = ['node_types', 'data_types', 'artifact_types',
                          'group_types', 'relationship_types',
                          'capability_types', 'interface_types',
                          'policy_types']

    '''TOSCA definition file.'''
    TOSCA_DEF_FILE = os.path.join(
        os.path.dirname(os.path.abspath(__file__)),
        "TOSCA_definition_1_0.yaml")

    loader = toscaparser.utils.yamlparser.load_yaml

    TOSCA_DEF_LOAD_AS_IS = loader(TOSCA_DEF_FILE)

    # Map of definition with pre-loaded values of TOSCA_DEF_FILE_SECTIONS
    TOSCA_DEF = {}
    for section in TOSCA_DEF_SECTIONS:
        if section in TOSCA_DEF_LOAD_AS_IS.keys():
            value = TOSCA_DEF_LOAD_AS_IS[section]
            for key in value.keys():
                TOSCA_DEF[key] = value[key]

    RELATIONSHIP_TYPE = (DEPENDSON, HOSTEDON, CONNECTSTO, ATTACHESTO,
                         LINKSTO, BINDSTO) = \
                        ('tosca.relationships.DependsOn',
                         'tosca.relationships.HostedOn',
                         'tosca.relationships.ConnectsTo',
                         'tosca.relationships.AttachesTo',
                         'tosca.relationships.network.LinksTo',
                         'tosca.relationships.network.BindsTo')

    NODE_PREFIX = 'tosca.nodes.'
    RELATIONSHIP_PREFIX = 'tosca.relationships.'
    CAPABILITY_PREFIX = 'tosca.capabilities.'
    INTERFACE_PREFIX = 'tosca.interfaces.'
    ARTIFACT_PREFIX = 'tosca.artifacts.'
    POLICY_PREFIX = 'tosca.policies.'
    GROUP_PREFIX = 'tosca.groups.'
    # currently the data types are defined only for network
    # but may have changes in the future.
    DATATYPE_PREFIX = 'tosca.datatypes.'
    DATATYPE_NETWORK_PREFIX = DATATYPE_PREFIX + 'network.'
    TOSCA = 'tosca'

    def derived_from(self, defs):
        '''Return a type this type is derived from.'''
        return self.entity_value(defs, 'derived_from')

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

    def entity_value(self, defs, key):
        if key in defs:
            return defs[key]

    def get_value(self, ndtype, defs=None, parent=None):
        value = None
        if defs is None:
            if not hasattr(self, 'defs'):
                return None
            defs = self.defs
        if ndtype in defs:
            # copy the value to avoid that next operations add items in the
            # item definitions
            value = copy.copy(defs[ndtype])
        if parent:
            p = self
            if p:
                while p:
                    if ndtype in p.defs:
                        # get the parent value
                        parent_value = p.defs[ndtype]
                        if value:
                            if isinstance(value, dict):
                                for k, v in parent_value.items():
                                    if k not in value.keys():
                                        value[k] = v
                            if isinstance(value, list):
                                for p_value in parent_value:
                                    if p_value not in value:
                                        value.append(p_value)
                        else:
                            value = copy.copy(parent_value)
                    p = p.parent_type
        return value

    def get_definition(self, ndtype):
        value = None
        if not hasattr(self, 'defs'):
            defs = None
            ValidationIssueCollector.appendException(
                ValidationError(message="defs is " + str(defs)))
        else:
            defs = self.defs
        if defs is not None and ndtype in defs:
            value = defs[ndtype]
        p = self.parent_type
        if p:
            inherited = p.get_definition(ndtype)
            if inherited:
                inherited = dict(inherited)
                if not value:
                    value = inherited
                else:
                    inherited.update(value)
                    value.update(inherited)
        return value


def update_definitions(version):
    exttools = ExtTools()
    extension_defs_file = exttools.get_defs_file(version)
    loader = toscaparser.utils.yamlparser.load_yaml
    nfv_def_file = loader(extension_defs_file)
    nfv_def = {}
    for section in EntityType.TOSCA_DEF_SECTIONS:
        if section in nfv_def_file.keys():
            value = nfv_def_file[section]
            for key in value.keys():
                nfv_def[key] = value[key]
    EntityType.TOSCA_DEF.update(nfv_def)
*/

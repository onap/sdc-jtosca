package org.openecomp.sdc.toscaparser.api.elements;

import org.openecomp.sdc.toscaparser.api.EntityTemplate;
import org.openecomp.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.openecomp.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class InterfacesDef extends StatefulEntityType {

	public static final String LIFECYCLE = "tosca.interfaces.node.lifecycle.Standard";
	public static final String CONFIGURE = "tosca.interfaces.relationship.Configure";
	public static final String LIFECYCLE_SHORTNAME = "Standard";
	public static final String CONFIGURE_SHORTNAME = "Configure";

	public static final String SECTIONS[] = {
			LIFECYCLE, CONFIGURE, LIFECYCLE_SHORTNAME,CONFIGURE_SHORTNAME
	};

	public static final String IMPLEMENTATION = "implementation";
	public static final String INPUTS = "inputs";
	
	public static final String INTERFACEVALUE[] = {IMPLEMENTATION, INPUTS};

	public static final String INTERFACE_DEF_RESERVED_WORDS[] = {
			"type", "inputs", "derived_from", "version", "description"};
	
	private EntityType ntype;
	private EntityTemplate nodeTemplate;
	private String name;
	private Object value;
	private String implementation;
	private LinkedHashMap<String,Object> inputs;

	
	@SuppressWarnings("unchecked")
	public InterfacesDef(EntityType inodeType,
					     String interfaceType,
					     EntityTemplate inodeTemplate,
					     String iname,
					     Object ivalue) {
		// void
		super();
		
        ntype = inodeType;
        nodeTemplate = inodeTemplate;
        type = interfaceType;
        name = iname;
        value = ivalue;
        implementation = null;
        inputs = null;
        defs = new LinkedHashMap<String,Object>();

        if(interfaceType.equals(LIFECYCLE_SHORTNAME)) {
            interfaceType = LIFECYCLE;
        }
        if(interfaceType.equals(CONFIGURE_SHORTNAME)) {
            interfaceType = CONFIGURE;
        }
      
        // only NodeType has getInterfaces "hasattr(ntype,interfaces)"
        // while RelationshipType does not
        if(ntype instanceof NodeType) {
	        if(((NodeType)ntype).getInterfaces() != null &&
	        		((NodeType)ntype).getInterfaces().values().contains(interfaceType)) {
	        	LinkedHashMap<String,Object> nii = (LinkedHashMap<String,Object>)
	        			((NodeType)ntype).getInterfaces().get(interfaceType);
	        	interfaceType = (String)nii.get("type");
	        }
        }
        if(inodeType != null) {
        	if(nodeTemplate != null && nodeTemplate.getCustomDef() != null &&
        			nodeTemplate.getCustomDef().values().contains(interfaceType)) {
        		defs = (LinkedHashMap<String,Object>)
        					nodeTemplate.getCustomDef().get(interfaceType);
        	}
        	else {
        		defs = (LinkedHashMap<String,Object>)TOSCA_DEF.get(interfaceType);
        	}
        }
        
        if(ivalue != null) {
        	if(ivalue instanceof LinkedHashMap) {
        		for(Map.Entry<String,Object> me: ((LinkedHashMap<String,Object>)ivalue).entrySet()) {
        			if(me.getKey().equals("implementation")) {
        				implementation = (String)me.getValue();
        			}
        			else if(me.getKey().equals("inputs")) {
        				inputs = (LinkedHashMap<String,Object>)me.getValue();
        			}
        			else {
                        ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE123", String.format(
                            "UnknownFieldError: \"interfaces\" of template \"%s\" contain unknown field \"%s\"",
                            nodeTemplate.getName(),me.getKey()))); 
        			}
        		}
        	}
    		else {
    			implementation = (String)ivalue;
    		}
        }
 	}

	public ArrayList<String> getLifecycleOps() {
		if(defs != null) {
			if(type.equals(LIFECYCLE)) {
				return _ops();
			}
		}
		return null;
	}
	
	public ArrayList<String> getConfigureOps() {
		if(defs != null) {
			if(type.equals(CONFIGURE)) {
				return _ops();
			}
		}
		return null;
	}
	
	private ArrayList<String> _ops() {
		return new ArrayList<String>(defs.keySet());
	}
	
	// getters/setters
	
	public LinkedHashMap<String,Object> getInputs() {
		return inputs;
	}
	
	public void setInput(String name,Object value) {
		inputs.put(name, value);
	}
}

/*python

#    Licensed under the Apache License, Version 2.0 (the "License"); you may
#    not use this file except in compliance with the License. You may obtain
#    a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#    License for the specific language governing permissions and limitations
#    under the License.

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import UnknownFieldError
from toscaparser.elements.statefulentitytype import StatefulEntityType

SECTIONS = (LIFECYCLE, CONFIGURE, LIFECYCLE_SHORTNAME,
            CONFIGURE_SHORTNAME) = \
           ('tosca.interfaces.node.lifecycle.Standard',
            'tosca.interfaces.relationship.Configure',
            'Standard', 'Configure')

INTERFACEVALUE = (IMPLEMENTATION, INPUTS) = ('implementation', 'inputs')

INTERFACE_DEF_RESERVED_WORDS = ['type', 'inputs', 'derived_from', 'version',
                                'description']


class InterfacesDef(StatefulEntityType):
    '''TOSCA built-in interfaces type.'''

    def __init__(self, node_type, interfacetype,
                 node_template=None, name=None, value=None):
        self.ntype = node_type
        self.node_template = node_template
        self.type = interfacetype
        self.name = name
        self.value = value
        self.implementation = None
        self.inputs = None
        self.defs = {}
        if interfacetype == LIFECYCLE_SHORTNAME:
            interfacetype = LIFECYCLE
        if interfacetype == CONFIGURE_SHORTNAME:
            interfacetype = CONFIGURE
        if hasattr(self.ntype, 'interfaces') \
           and self.ntype.interfaces \
           and interfacetype in self.ntype.interfaces:
            interfacetype = self.ntype.interfaces[interfacetype]['type']
        if node_type:
            if self.node_template and self.node_template.custom_def \
               and interfacetype in self.node_template.custom_def:
                self.defs = self.node_template.custom_def[interfacetype]
            else:
                self.defs = self.TOSCA_DEF[interfacetype]
        if value:
            if isinstance(self.value, dict):
                for i, j in self.value.items():
                    if i == IMPLEMENTATION:
                        self.implementation = j
                    elif i == INPUTS:
                        self.inputs = j
                    else:
                        what = ('"interfaces" of template "%s"' %
                                self.node_template.name)
                        ValidationIssueCollector.appendException(
                            UnknownFieldError(what=what, field=i))
            else:
                self.implementation = value

    @property
    def lifecycle_ops(self):
        if self.defs:
            if self.type == LIFECYCLE:
                return self._ops()

    @property
    def configure_ops(self):
        if self.defs:
            if self.type == CONFIGURE:
                return self._ops()

    def _ops(self):
        ops = []
        for name in list(self.defs.keys()):
            ops.append(name)
        return ops
*/
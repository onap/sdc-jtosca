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

import org.onap.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.onap.sdc.toscaparser.api.utils.ThreadLocalsHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class GroupType extends StatefulEntityType {

    private static final String DERIVED_FROM = "derived_from";
    private static final String VERSION = "version";
    private static final String METADATA = "metadata";
    private static final String DESCRIPTION = "description";
    private static final String PROPERTIES = "properties";
    private static final String MEMBERS = "members";
    private static final String INTERFACES = "interfaces";

    private static final String[] SECTIONS = {
            DERIVED_FROM, VERSION, METADATA, DESCRIPTION, PROPERTIES, MEMBERS, INTERFACES};

    private String groupType;
    private LinkedHashMap<String, Object> customDef;
    private String groupDescription;
    private String groupVersion;
    //private LinkedHashMap<String,Object> groupProperties;
    //private ArrayList<String> groupMembers;
    private LinkedHashMap<String, Object> metaData;

    @SuppressWarnings("unchecked")
    public GroupType(String groupType, LinkedHashMap<String, Object> customDef) {
        super(groupType, GROUP_PREFIX, customDef);

        this.groupType = groupType;
        this.customDef = customDef;
        validateFields();
        if (defs != null) {
            groupDescription = (String) defs.get(DESCRIPTION);
            groupVersion = (String) defs.get(VERSION);
            //groupProperties = (LinkedHashMap<String,Object>)defs.get(PROPERTIES);
            //groupMembers = (ArrayList<String>)defs.get(MEMBERS);
            Object mdo = defs.get(METADATA);
            if (mdo instanceof LinkedHashMap) {
                metaData = (LinkedHashMap<String, Object>) mdo;
            } else {
                metaData = null;
            }

            if (metaData != null) {
                validateMetadata(metaData);
            }
        }
    }

    public GroupType getParentType() {
        // Return a group statefulentity of this entity is derived from.
        if (defs == null) {
            return null;
        }
        String pgroupEntity = derivedFrom(defs);
        if (pgroupEntity != null) {
            return new GroupType(pgroupEntity, customDef);
        }
        return null;
    }

    public String getDescription() {
        return groupDescription;
    }

    public String getVersion() {
        return groupVersion;
    }

    @SuppressWarnings("unchecked")
    public LinkedHashMap<String, Object> getInterfaces() {
        Object ifo = getValue(INTERFACES, null, false);
        if (ifo instanceof LinkedHashMap) {
            return (LinkedHashMap<String, Object>) ifo;
        }
        return new LinkedHashMap<String, Object>();
    }

    private void validateFields() {
        if (defs != null) {
            for (String name : defs.keySet()) {
                boolean bFound = false;
                for (String sect : SECTIONS) {
                    if (name.equals(sect)) {
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE120", String.format(
                            "UnknownFieldError: Group Type \"%s\" contains unknown field \"%s\"",
                            groupType, name)));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateMetadata(LinkedHashMap<String, Object> metadata) {
        String mtt = (String) metadata.get("type");
        if (mtt != null && !mtt.equals("map") && !mtt.equals("tosca:map")) {
            ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE121", String.format(
                    "InvalidTypeError: \"%s\" defined in group for metadata is invalid",
                    mtt)));
        }
        for (String entrySchema : metadata.keySet()) {
            Object estob = metadata.get(entrySchema);
            if (estob instanceof LinkedHashMap) {
                String est = (String) ((LinkedHashMap<String, Object>) estob).get("type");
                if (!est.equals("string")) {
                    ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE122", String.format(
                            "InvalidTypeError: \"%s\" defined in group for metadata \"%s\" is invalid",
                            est, entrySchema)));
                }
            }
        }
    }

    public String getType() {
        return groupType;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<CapabilityTypeDef> getCapabilitiesObjects() {
        // Return a list of capability objects
        ArrayList<CapabilityTypeDef> typecapabilities = new ArrayList<>();
        LinkedHashMap<String, Object> caps = (LinkedHashMap<String, Object>) getValue(CAPABILITIES, null, true);
        if (caps != null) {
            // 'cname' is symbolic name of the capability
            // 'cvalue' is a dict { 'type': <capability type name> }
            for (Map.Entry<String, Object> me : caps.entrySet()) {
                String cname = me.getKey();
                LinkedHashMap<String, String> cvalue = (LinkedHashMap<String, String>) me.getValue();
                String ctype = cvalue.get("type");
                CapabilityTypeDef cap = new CapabilityTypeDef(cname, ctype, type, customDef);
                typecapabilities.add(cap);
            }
        }
        return typecapabilities;
    }

    public LinkedHashMap<String, CapabilityTypeDef> getCapabilities() {
        // Return a dictionary of capability name-objects pairs
        LinkedHashMap<String, CapabilityTypeDef> caps = new LinkedHashMap<>();
        for (CapabilityTypeDef ctd : getCapabilitiesObjects()) {
            caps.put(ctd.getName(), ctd);
        }
        return caps;
    }

}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import InvalidTypeError
from toscaparser.common.exception import UnknownFieldError
from toscaparser.elements.statefulentitytype import StatefulEntityType


class GroupType(StatefulEntityType):
    '''TOSCA built-in group type.'''

    SECTIONS = (DERIVED_FROM, VERSION, METADATA, DESCRIPTION, PROPERTIES,
                MEMBERS, INTERFACES) = \
               ("derived_from", "version", "metadata", "description",
                "properties", "members", "interfaces")

    def __init__(self, grouptype, custom_def=None):
        super(GroupType, self).__init__(grouptype, self.GROUP_PREFIX,
                                        custom_def)
        self.custom_def = custom_def
        self.grouptype = grouptype
        self._validate_fields()
        self.group_description = None
        if self.DESCRIPTION in self.defs:
            self.group_description = self.defs[self.DESCRIPTION]

        self.group_version = None
        if self.VERSION in self.defs:
            self.group_version = self.defs[self.VERSION]

        self.group_properties = None
        if self.PROPERTIES in self.defs:
            self.group_properties = self.defs[self.PROPERTIES]

        self.group_members = None
        if self.MEMBERS in self.defs:
            self.group_members = self.defs[self.MEMBERS]

        if self.METADATA in self.defs:
            self.meta_data = self.defs[self.METADATA]
            self._validate_metadata(self.meta_data)

    @property
    def parent_type(self):
        '''Return a group statefulentity of this entity is derived from.'''
        if not hasattr(self, 'defs'):
            return None
        pgroup_entity = self.derived_from(self.defs)
        if pgroup_entity:
            return GroupType(pgroup_entity, self.custom_def)

    @property
    def description(self):
        return self.group_description

    @property
    def version(self):
        return self.group_version

    @property
    def interfaces(self):
        return self.get_value(self.INTERFACES)

    def _validate_fields(self):
        if self.defs:
            for name in self.defs.keys():
                if name not in self.SECTIONS:
                    ValidationIssueCollector.appendException(
                        UnknownFieldError(what='Group Type %s'
                                          % self.grouptype, field=name))

    def _validate_metadata(self, meta_data):
        if not meta_data.get('type') in ['map', 'tosca:map']:
            ValidationIssueCollector.appendException(
                InvalidTypeError(what='"%s" defined in group for '
                                 'metadata' % (meta_data.get('type'))))
        for entry_schema, entry_schema_type in meta_data.items():
            if isinstance(entry_schema_type, dict) and not \
                    entry_schema_type.get('type') == 'string':
                ValidationIssueCollector.appendException(
                    InvalidTypeError(what='"%s" defined in group for '
                                     'metadata "%s"'
                                     % (entry_schema_type.get('type'),
                                        entry_schema)))
*/

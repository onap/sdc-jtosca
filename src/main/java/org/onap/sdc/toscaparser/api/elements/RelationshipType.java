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

import java.util.LinkedHashMap;

public class RelationshipType extends StatefulEntityType {

    private static final String DERIVED_FROM = "derived_from";
    private static final String VALID_TARGET_TYPES = "valid_target_types";
    private static final String INTERFACES = "interfaces";
    private static final String ATTRIBUTES = "attributes";
    private static final String PROPERTIES = "properties";
    private static final String DESCRIPTION = "description";
    private static final String VERSION = "version";
    private static final String CREDENTIAL = "credential";

    private static final String[] SECTIONS = {
            DERIVED_FROM, VALID_TARGET_TYPES, INTERFACES,
            ATTRIBUTES, PROPERTIES, DESCRIPTION, VERSION, CREDENTIAL};

    private String capabilityName;
    private LinkedHashMap<String, Object> customDef;

    public RelationshipType(String type, String capabilityName, LinkedHashMap<String, Object> customDef) {
        super(type, RELATIONSHIP_PREFIX, customDef);
        this.capabilityName = capabilityName;
        this.customDef = customDef;
    }

    public RelationshipType getParentType() {
        // Return a relationship this reletionship is derived from.'''
        String prel = derivedFrom(defs);
        if (prel != null) {
            return new RelationshipType(prel, null, customDef);
        }
        return null;
    }

    public Object getValidTargetTypes() {
        return entityValue(defs, "valid_target_types");
    }

    private void validateKeys() {
        for (String key : defs.keySet()) {
            boolean bFound = false;
            for (String section : SECTIONS) {
                if (key.equals(section)) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                ThreadLocalsHolder.getCollector().appendValidationIssue(new JToscaValidationIssue("JE133", String.format(
                        "UnknownFieldError: Relationshiptype \"%s\" has unknown field \"%s\"", type, key)));
            }
        }
    }
}

/*python

from toscaparser.common.exception import ValidationIssueCollector
from toscaparser.common.exception import UnknownFieldError
from toscaparser.elements.statefulentitytype import StatefulEntityType


class RelationshipType(StatefulEntityType):
    '''TOSCA built-in relationship type.'''
    SECTIONS = (DERIVED_FROM, VALID_TARGET_TYPES, INTERFACES,
                ATTRIBUTES, PROPERTIES, DESCRIPTION, VERSION,
                CREDENTIAL) = ('derived_from', 'valid_target_types',
                               'interfaces', 'attributes', 'properties',
                               'description', 'version', 'credential')

    def __init__(self, type, capability_name=None, custom_def=None):
        super(RelationshipType, self).__init__(type, self.RELATIONSHIP_PREFIX,
                                               custom_def)
        self.capability_name = capability_name
        self.custom_def = custom_def
        self._validate_keys()

    @property
    def parent_type(self):
        '''Return a relationship this reletionship is derived from.'''
        prel = self.derived_from(self.defs)
        if prel:
            return RelationshipType(prel, self.custom_def)

    @property
    def valid_target_types(self):
        return self.entity_value(self.defs, 'valid_target_types')

    def _validate_keys(self):
        for key in self.defs.keys():
            if key not in self.SECTIONS:
                ValidationIssueCollector.appendException(
                    UnknownFieldError(what='Relationshiptype "%s"' % self.type,
                                      field=key))
*/

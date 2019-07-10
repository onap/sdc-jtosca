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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CapabilityAssignments {

    private Map<String, CapabilityAssignment> capabilityAssignments;

    public CapabilityAssignments(Map<String, CapabilityAssignment> capabilityAssignments) {
        this.capabilityAssignments = capabilityAssignments != null ? new HashMap<>(capabilityAssignments) : new HashMap<>();
    }

    /**
     * Get all capability assignments for node template.<br>
     * This object can be either the original one, holding all capability assignments for this node template,or a filtered one, holding a filtered subset.<br>
     *
     * @return list of capability assignments for the node template. <br>
     * If there are no capability assignments, empty list is returned.
     */
    public List<CapabilityAssignment> getAll() {
        return new ArrayList<>(capabilityAssignments.values());
    }

    /**
     * Filter capability assignments by capability tosca type.
     *
     * @param type - The tosca type of capability assignments.
     * @return CapabilityAssignments object, containing capability assignments of this type.<br>
     * If no such found, filtering will result in an empty collection.
     */
    public CapabilityAssignments getCapabilitiesByType(String type) {
        Map<String, CapabilityAssignment> capabilityAssignmentsMap = capabilityAssignments.entrySet().stream()
                .filter(cap -> cap.getValue().getDefinition().getType().equals(type)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new CapabilityAssignments(capabilityAssignmentsMap);
    }

    /**
     * Get capability assignment by capability name.
     *
     * @param name - The name of capability assignment
     * @return capability assignment with this name, or null if no such capability assignment was found.
     */
    public CapabilityAssignment getCapabilityByName(String name) {
        return capabilityAssignments.get(name);
    }

}

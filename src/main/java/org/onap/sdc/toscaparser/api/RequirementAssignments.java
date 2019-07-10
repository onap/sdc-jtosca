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
import java.util.List;
import java.util.stream.Collectors;

public class RequirementAssignments {

    private List<RequirementAssignment> requirementAssignmentList;

    public RequirementAssignments(List<RequirementAssignment> requirementAssignments) {
        this.requirementAssignmentList = requirementAssignments != null ? new ArrayList<>(requirementAssignments) : new ArrayList<>();
    }

    /**
     * Get all requirement assignments for Node Template.<br>
     * This object can be either the original one, holding all requirement assignments for this node template,or a filtered one, holding a filtered subset.<br>
     *
     * @return list of requirement assignments for the node template. <br>
     * If there are no requirement assignments, empty list is returned.
     */
    public List<RequirementAssignment> getAll() {
        return new ArrayList<>(requirementAssignmentList);
    }

    /**
     * Filter requirement assignments by requirement name.
     *
     * @param reqName - The name of requirement
     * @return RequirementAssignments object, containing requirement assignments of this type.<br>
     * If no such found, filtering will result in an empty collection.
     */
    public RequirementAssignments getRequirementsByName(String reqName) {
        List<RequirementAssignment> requirementAssignments = requirementAssignmentList.stream()
                .filter(req -> req.getName().equals(reqName)).collect(Collectors.toList());

        return new RequirementAssignments(requirementAssignments);
    }
}

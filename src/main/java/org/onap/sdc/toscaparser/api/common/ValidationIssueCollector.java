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

package org.onap.sdc.toscaparser.api.common;

import java.util.*;

// Perfectly good enough... 

public class ValidationIssueCollector {

    private Map<String, JToscaValidationIssue> validationIssues = new HashMap<String, JToscaValidationIssue>();

    public void appendValidationIssue(JToscaValidationIssue issue) {

        validationIssues.put(issue.getMessage(), issue);

    }

    public List<String> getValidationIssueReport() {
        List<String> report = new ArrayList<>();
        if (!validationIssues.isEmpty()) {
            for (JToscaValidationIssue exception : validationIssues.values()) {
                report.add("[" + exception.getCode() + "]: " + exception.getMessage());
            }
        }

        return report;
    }

    public Map<String, JToscaValidationIssue> getValidationIssues() {
        return validationIssues;
    }


    public int validationIssuesCaught() {
        return validationIssues.size();
    }

}

package org.onap.sdc.toscaparser.api.common;

import java.util.*;

// Perfectly good enough... 

public class ValidationIssueCollector {

    private Map<String,JToscaValidationIssue> validationIssues = new HashMap<String,JToscaValidationIssue>();
    public void appendValidationIssue(JToscaValidationIssue issue) {

        validationIssues.put(issue.getMessage(),issue);

    }

    public List<String> getValidationIssueReport() {
        List<String> report = new ArrayList<>();
        if (!validationIssues.isEmpty()) {
            for (JToscaValidationIssue exception : validationIssues.values()) {
                report.add("["+exception.getCode()+"]: "+ exception.getMessage());
            }
        }

        return report;
    }
    public Map<String,JToscaValidationIssue> getValidationIssues() {
        return validationIssues;
    }


    public int validationIssuesCaught() {
        return validationIssues.size();
    }
    
}

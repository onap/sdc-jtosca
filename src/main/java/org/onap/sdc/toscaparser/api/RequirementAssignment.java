package org.onap.sdc.toscaparser.api;

import java.util.Map;

public class RequirementAssignment {

    private String name;
    private String nodeName;
    private String capabilityName;
    private Object relationship;

    public RequirementAssignment(String reqName, String nodeName) {
        this.name = reqName;
        this.nodeName = nodeName;
    }

    public RequirementAssignment(String reqName, String nodeName, String capabilityName) {
        this.name = reqName;
        this.nodeName = nodeName;
        this.capabilityName = capabilityName;
    }

    public RequirementAssignment(String reqName, String nodeName, String capabilityName, Object relationship) {
        this.name = reqName;
        this.nodeName = nodeName;
        this.capabilityName = capabilityName;
        this.relationship = relationship;
    }

    /**
     * Get the name for requirement assignment.
     * @return the name for requirement assignment.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name for requirement
     * @param name - the name for requirement to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the node name for requirement assignment.
     * @return the node name for requirement
     */
    public String getNodeTemplateName() {
        return nodeName;
    }

    /**
     * Set the node name for requirement
     * @param nodeName - the node name for requirement to set
     */
    public void setNodeTemplateName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Get the capability name for requirement assignment.
     * @return the capability name for requirement
     */
    public String getCapabilityName() {
        return capabilityName;
    }

    /**
     * Set the capability name for requirement assignment.
     * @param capabilityName - the capability name for requirement to set
     */
    public void setCapabilityName(String capabilityName) {
        this.capabilityName = capabilityName;
    }

    /**
     * Get the relationship object for requirement
     * @return the relationship object for requirement
     */
    public Object getRelationship() {
        return relationship;
    }
}

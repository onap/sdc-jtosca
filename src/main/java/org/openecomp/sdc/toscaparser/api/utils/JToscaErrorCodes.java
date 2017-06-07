package org.openecomp.sdc.toscaparser.api.utils;


public enum JToscaErrorCodes {
    MISSING_META_FILE("JT1001"),
    INVALID_META_YAML_CONTENT("JT1002"),
    ENTRY_DEFINITION_NOT_DEFINED("JT1003"),
    MISSING_ENTRY_DEFINITION_FILE ("JT1004"),
    GENERAL_ERROR("JT1005"),
    PATH_NOT_VALID("JT1006"),
    CSAR_TOSCA_VALIDATION_ERROR("JT1007"),
    INVALID_CSAR_FORMAT("JT1008");

    private String value;

    private JToscaErrorCodes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static JToscaErrorCodes getByCode(String code) {
        for(JToscaErrorCodes v : values()){
            if( v.getValue().equals(code)){
                return v;
            }
        }
        return null;
    }
}
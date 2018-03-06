package org.onap.sdc.toscaparser.api.utils;


public enum JToscaErrorCodes {
    MISSING_META_FILE("JE1001"),
    INVALID_META_YAML_CONTENT("JE1002"),
    ENTRY_DEFINITION_NOT_DEFINED("JE1003"),
    MISSING_ENTRY_DEFINITION_FILE ("JE1004"),
    GENERAL_ERROR("JE1005"),
    PATH_NOT_VALID("JE1006"),
    CSAR_TOSCA_VALIDATION_ERROR("JE1007"),
    INVALID_CSAR_FORMAT("JE1008");

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
package org.onap.sdc.toscaparser.api.common;

public class JToscaException extends Exception {

	private static final long serialVersionUID = 1L;
	private String code;

	public JToscaException(String message, String code) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	//JE1001 - Meta file missing
	//JE1002 - Invalid yaml content
	//JE1003 - Entry-Definition not defined in meta file
	//JE1004 - Entry-Definition file missing
	//JE1005 - General Error
	//JE1006 - General Error/Path not valid
}

package org.openecomp.sdc.toscaparser.api.common;

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

	//JT1001 - Meta file missing
	//JT1002 - Invalid yaml content
	//JT1003 - Entry-Definition not defined in meta file
	//JT1004 - Entry-Definition file missing
	//JT1005 - General Error
	//JT1006 - General Error/Path not valid
}

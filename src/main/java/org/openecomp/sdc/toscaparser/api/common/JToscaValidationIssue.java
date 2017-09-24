package org.openecomp.sdc.toscaparser.api.common;

public class JToscaValidationIssue {

	private String code;
	private String message;


	public JToscaValidationIssue(String code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Override
	public String toString() {
		return "JToscaError [code=" + code + ", message=" + message + "]";
	}
}

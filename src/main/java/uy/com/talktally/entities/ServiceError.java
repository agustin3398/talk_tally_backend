package uy.com.talktally.entities;

import java.io.Serializable;

public class ServiceError implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6533760477313216027L;
	private Boolean error;
	private Integer errorCode;
	private String errorDescription;

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	public Boolean getError() {
		return error;
	}

	public void setError(Boolean error) {
		this.error = error;
	}

}

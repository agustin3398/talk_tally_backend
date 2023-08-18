package uy.com.talktally.results;

import java.io.Serializable;

import uy.com.talktally.entities.ServiceError;

public class ResultSignUp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5195674030062771895L;
	private ServiceError serviceError;
	private String oKMessage;

	public ServiceError getServiceError() {
		return serviceError;
	}

	public void setServiceError(ServiceError serviceError) {
		this.serviceError = serviceError;
	}

	public String getoKMessage() {
		return oKMessage;
	}

	public void setoKMessage(String oKMessage) {
		this.oKMessage = oKMessage;
	}

}

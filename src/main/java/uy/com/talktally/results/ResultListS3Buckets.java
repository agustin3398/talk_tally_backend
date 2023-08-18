package uy.com.talktally.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uy.com.talktally.entities.ServiceError;

public class ResultListS3Buckets implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6627843660014108746L;
	private List<String> bucketsName = new ArrayList<>();
	private ServiceError serviceError;
	private String oKMessage;

	public List<String> getBucketsName() {
		return bucketsName;
	}

	public void setBucketsName(List<String> bucketsName) {
		this.bucketsName = bucketsName;
	}

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

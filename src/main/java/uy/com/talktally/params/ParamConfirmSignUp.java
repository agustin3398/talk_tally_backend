package uy.com.talktally.params;

import java.io.Serializable;

public class ParamConfirmSignUp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2454595471389236657L;
	private String mail;
	private String confirmationCode;

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getConfirmationCode() {
		return confirmationCode;
	}

	public void setConfirmationCode(String confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

}

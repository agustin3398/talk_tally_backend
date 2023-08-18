package uy.com.talktally.params;

import java.io.Serializable;

public class ParamLogin implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 7972633623873952856L;
	private String mail;
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

}

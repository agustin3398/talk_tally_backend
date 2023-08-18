package uy.com.talktally.params;

import java.io.Serializable;

public class ParamSignUp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3121463690665127742L;
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

package uy.com.talktally.utils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import uy.com.talktally.aws.AWSCredentialsManager;

public class UsersUtils implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2791074281223785076L;
	private static AWSCredentialsManager credentialsManager = new AWSCredentialsManager();

	private UsersUtils() {
		// Private constructor to prevent instantiation
	}

	public static String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName)
			throws NoSuchAlgorithmException, InvalidKeyException {
		final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

		SecretKeySpec signingKey = new SecretKeySpec(userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
				HMAC_SHA256_ALGORITHM);

		Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
		mac.init(signingKey);
		mac.update(userName.getBytes(StandardCharsets.UTF_8));
		byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
		return java.util.Base64.getEncoder().encodeToString(rawHmac);
	}

	public static boolean isUserEmailVerified(CognitoIdentityProviderClient cognitoClient, String mail) {
		try {
			AdminGetUserRequest adminGetUserRequest = AdminGetUserRequest.builder()
					.userPoolId(credentialsManager.loadUserPoolId()).username(mail).build();

			AdminGetUserResponse adminGetUserResponse = cognitoClient.adminGetUser(adminGetUserRequest);
			List<AttributeType> userAttributes = adminGetUserResponse.userAttributes();

			for (AttributeType attribute : userAttributes) {
				if (attribute.name().equals("email_verified") && attribute.value().equals("true")) {
					return true;
				}
			}
		} catch (CognitoIdentityProviderException e) {
			// Handle exceptions if needed
		}

		return false;
	}
	
	public static String getUserCognitoId(String username) {
		CognitoIdentityProviderClient cognitoClient = credentialsManager.createCognitoClient();
		
		AdminGetUserRequest adminGetUserRequest = AdminGetUserRequest.builder()
				.userPoolId(credentialsManager.loadUserPoolId()).username(username).build();
	    
		AdminGetUserResponse adminGetUserResponse = cognitoClient.adminGetUser(adminGetUserRequest);
	    return adminGetUserResponse.userAttributes().stream()
	            .filter(attr -> "sub".equals(attr.name())) // "sub" is the attribute name for Cognito ID
	            .findFirst()
	            .map(attr -> attr.value())
	            .orElse(null);
	}

}

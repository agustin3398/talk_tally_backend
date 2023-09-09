package uy.com.talktally.webservices;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResendConfirmationCodeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ResendConfirmationCodeResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import uy.com.talktally.aws.AWSCredentialsManager;
import uy.com.talktally.entities.ServiceError;
import uy.com.talktally.params.ParamConfirmSignUp;
import uy.com.talktally.params.ParamLogin;
import uy.com.talktally.params.ParamSignUp;
import uy.com.talktally.results.ResultConfirmSignUp;
import uy.com.talktally.results.ResultLogin;
import uy.com.talktally.results.ResultSignUp;
import uy.com.talktally.utils.UsersUtils;

@RestController
@RequestMapping("/api/WsUserServices")
public class WsUserServicesController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6633720756660999418L;
	private AWSCredentialsManager credentialsManager = new AWSCredentialsManager();
	private String mail;
	private String password;
	private String userPoolId;
	private String clientId;
	private String secretKey;
	private String secretVal;
	private String confirmationCode;

	@PostMapping("/signup")
	public ResultSignUp signUp(@RequestBody ParamSignUp param) {

		ResultSignUp result = new ResultSignUp();
		ServiceError serviceError = new ServiceError();
		CognitoIdentityProviderClient cognitoClient = credentialsManager.createCognitoClient();

		mail = param.getMail();
		password = param.getPassword();
		userPoolId = credentialsManager.loadUserPoolId();
		clientId = credentialsManager.loadUserPoolClientId();
		secretKey = credentialsManager.loadCognitoSecretKey();

		AttributeType attributeType = AttributeType.builder().name("email").value(mail).build();

		List<AttributeType> attrs = new ArrayList<>();
		attrs.add(attributeType);
		try {
			secretVal = UsersUtils.calculateSecretHash(clientId, secretKey, mail);
			SignUpRequest signUpRequest = SignUpRequest.builder().userAttributes(attrs).clientId(clientId)
					.username(mail).password(password).secretHash(secretVal).build();

			SignUpResponse response = cognitoClient.signUp(signUpRequest);

			// If the user creation is successful, you can set a success message
			result.setoKMessage("User created successfully. Check your email for verification instructions.");
			serviceError.setError(Boolean.FALSE);
			result.setServiceError(serviceError);
			System.out.println("User has been signed up");

		} catch (CognitoIdentityProviderException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorCode(103);
			serviceError.setErrorDescription("An error occurred during sign-up. Please try again later.");
			result.setServiceError(serviceError);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorCode(103);
			serviceError.setErrorDescription("An error occurred during sign-up. Please try again later.");
			result.setServiceError(serviceError);
		} catch (Exception e) {
			e.printStackTrace();
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorCode(103);
			serviceError.setErrorDescription("An error occurred during sign-up. Please try again later.");
			result.setServiceError(serviceError);
		}

		// You can handle additional response data if needed

		return result;
	}

	@PostMapping("/login")
	public ResultLogin loginUser(@RequestBody ParamLogin param) {
		// Implementación del método loginUser
		ResultLogin result = new ResultLogin();
		ServiceError serviceError = new ServiceError();
		CognitoIdentityProviderClient cognitoClient = credentialsManager.createCognitoClient();

		mail = param.getMail();
		password = param.getPassword();
		clientId = credentialsManager.loadUserPoolClientId();
		secretKey = credentialsManager.loadCognitoSecretKey();

		try {
			secretVal = UsersUtils.calculateSecretHash(clientId, secretKey, mail);

			Map<String, String> authParams = new HashMap<>();
			authParams.put("USERNAME", mail);
			authParams.put("PASSWORD", password);
			authParams.put("SECRET_HASH", secretVal);
			authParams.put("CLIENT_ID", clientId);

			AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
					.userPoolId(credentialsManager.loadUserPoolId()).clientId(clientId)
					.authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).authParameters(authParams).build();

			AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);

			// Check if the user's email is verified before allowing login
			if (!UsersUtils.isUserEmailVerified(cognitoClient, mail)) {
				serviceError.setError(Boolean.TRUE);
				serviceError.setErrorDescription("Email not verified. Please verify your email address.");
				serviceError.setErrorCode(107); // You can define your error codes
				result.setServiceError(serviceError);
				return result;
			}

			result.setoKMessage("Login successful!");
			serviceError.setError(Boolean.FALSE);
			result.setServiceError(serviceError);

			return result;
		} catch (NotAuthorizedException e) {
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorDescription("Invalid credentials");
			serviceError.setErrorCode(105);
			result.setServiceError(serviceError);
			return result;
		} catch (Exception e) {
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorDescription("Something went wrong");
			serviceError.setErrorCode(106);
			result.setServiceError(serviceError);
			return result;
		}
	}

	@PostMapping("/confirmSignUp")
	public ResultConfirmSignUp confirmSignUp(@RequestBody ParamConfirmSignUp param) {
		ResultConfirmSignUp result = new ResultConfirmSignUp();
		ServiceError serviceError = new ServiceError();
		CognitoIdentityProviderClient cognitoClient = credentialsManager.createCognitoClient();

		mail = param.getMail();
		confirmationCode = param.getConfirmationCode(); // User-provided confirmation code
		clientId = credentialsManager.loadUserPoolClientId();
		secretKey = credentialsManager.loadCognitoSecretKey();

		try {
			secretVal = UsersUtils.calculateSecretHash(clientId, secretKey, mail);

			AdminGetUserRequest adminGetUserRequest = AdminGetUserRequest.builder()
					.userPoolId(credentialsManager.loadUserPoolId()).username(mail).build();

			AdminGetUserResponse adminGetUserResponse = cognitoClient.adminGetUser(adminGetUserRequest);
			List<AttributeType> userAttributes = adminGetUserResponse.userAttributes();

			// Find the attribute for email verification status
			boolean isEmailVerified = false;
			for (AttributeType attribute : userAttributes) {
				if (attribute.name().equals("email_verified") && attribute.value().equals("true")) {
					isEmailVerified = true;
					break;
				}
			}

			if (isEmailVerified) {
				// Email is already verified
				result.setoKMessage("Email is already verified.");
				serviceError.setError(Boolean.TRUE);
				result.setServiceError(serviceError);
			} else {
				ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder().username(mail)
						.confirmationCode(confirmationCode).clientId(credentialsManager.loadUserPoolClientId())
						.secretHash(secretVal).build();

				ConfirmSignUpResponse response = cognitoClient.confirmSignUp(confirmSignUpRequest);

				// If confirmation is successful, you can set a success message
				result.setoKMessage("Email verification successful!");
				serviceError.setError(Boolean.FALSE);
				result.setServiceError(serviceError);
				System.out.println("Email has been verified");
			}

		} catch (CognitoIdentityProviderException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			// Handle errors, possibly send proper response to the client
		} catch (Exception e) {
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorCode(104); // You can define your error codes
			serviceError.setErrorDescription("An error occurred during email verification. Please try again.");
			// Handle errors
		}

		// You can handle additional response data if needed

		return result;
	}

	@PostMapping("/resend-confirmation-code")
	public ResultSignUp resendConfirmation(@RequestBody ParamSignUp param) {
		ResultSignUp result = new ResultSignUp();
		ServiceError serviceError = new ServiceError();
		CognitoIdentityProviderClient cognitoClient = credentialsManager.createCognitoClient();

		mail = param.getMail();

		try {
			secretVal = UsersUtils.calculateSecretHash(clientId, secretKey, mail);
			ResendConfirmationCodeRequest resendRequest = ResendConfirmationCodeRequest.builder()
					.clientId(credentialsManager.loadUserPoolClientId()).username(mail).secretHash(secretVal).build();

			ResendConfirmationCodeResponse response = cognitoClient.resendConfirmationCode(resendRequest);

			// If code resend is successful, you can set a success message
			result.setoKMessage("Confirmation code has been resent.");
			serviceError.setError(Boolean.FALSE);
			result.setServiceError(serviceError);
			System.out.println("Confirmation code has been resent");

		} catch (CognitoIdentityProviderException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			// Handle errors, possibly send proper response to the client
		} catch (Exception e) {
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorCode(105); // You can define your error codes
			serviceError.setErrorDescription("An error occurred while resending the confirmation code.");
			// Handle errors
		}

		// You can handle additional response data if needed

		return result;
	}

	public AWSCredentialsManager getCredentialsManager() {
		return credentialsManager;
	}

	public void setCredentialsManager(AWSCredentialsManager credentialsManager) {
		this.credentialsManager = credentialsManager;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserPoolId() {
		return userPoolId;
	}

	public void setUserPoolId(String userPoolId) {
		this.userPoolId = userPoolId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getSecretVal() {
		return secretVal;
	}

	public void setSecretVal(String secretVal) {
		this.secretVal = secretVal;
	}

	public String getConfirmationCode() {
		return confirmationCode;
	}

	public void setConfirmationCode(String confirmationCode) {
		this.confirmationCode = confirmationCode;
	}
}

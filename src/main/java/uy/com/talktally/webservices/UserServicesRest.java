package uy.com.talktally.webservices;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.VerifyEmailAddressRequest;
import uy.com.talktally.aws.AWSCredentialsManager;
import uy.com.talktally.entities.ServiceError;
import uy.com.talktally.params.ParamLogin;
import uy.com.talktally.params.ParamSignUp;
import uy.com.talktally.results.ResultLogin;
import uy.com.talktally.results.ResultSignUp;

@RestController
@RequestMapping("/UserServicesRest")
public class UserServicesRest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6633720756660999418L;
	private AWSCredentialsManager credentialsManager = new AWSCredentialsManager();

	@PostMapping("/signup")
	public ResultSignUp signUp(@RequestBody ParamSignUp param) {

		ResultSignUp result = new ResultSignUp();
		ServiceError serviceError = new ServiceError();
		CognitoIdentityProviderClient cognitoClient = credentialsManager.createCognitoClient();

		String mail = param.getMail();
		String password = param.getPassword();
		String userPoolId = credentialsManager.loadUserPoolId();
		String clientId = credentialsManager.loadUserPoolClientId();
		String secretKey = credentialsManager.loadCognitoSecretKey();

		AttributeType attributeType = AttributeType.builder().name("email").value(mail).build();

		List<AttributeType> attrs = new ArrayList<>();
		attrs.add(attributeType);
		try {
			String secretVal = calculateSecretHash(clientId, secretKey, mail);
			SignUpRequest signUpRequest = SignUpRequest.builder().userAttributes(attrs).clientId(clientId)
					.username(mail).password(password).secretHash(secretVal).build();

			SignUpResponse response = cognitoClient.signUp(signUpRequest);
			// If the signup is successful, you can set a success message
			result.setoKMessage("Sign-up successful!");
			serviceError.setError(Boolean.FALSE);
			result.setServiceError(serviceError);
			System.out.println("User has been signed up");

		} catch (CognitoIdentityProviderException e) {
			System.err.println(e.awsErrorDetails().errorMessage());
			System.exit(1);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			// You can handle additional response data for this case if needed
		} catch (Exception e) {
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorCode(103);
			serviceError.setErrorDescription("An error occurred during sign-up. Please try again later.");
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

		String mail = param.getMail();
		String password = param.getPassword();
		String clientId = credentialsManager.loadUserPoolClientId();
		String secretKey = credentialsManager.loadCognitoSecretKey();

		try {
			String secretVal = calculateSecretHash(clientId, secretKey, mail);

			Map<String, String> authParams = new HashMap<>();
			authParams.put("USERNAME", mail);
			authParams.put("PASSWORD", password);
			authParams.put("SECRET_HASH", secretVal);
			authParams.put("CLIENT_ID", clientId);

			AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
					.userPoolId(credentialsManager.loadUserPoolId()).clientId(clientId)
					.authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH).authParameters(authParams).build();


			AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);


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

	private static boolean verifyEmail(SesClient sesClient, String emailAddress) {

		boolean verified = false;

		VerifyEmailAddressRequest request = VerifyEmailAddressRequest.builder().emailAddress(emailAddress).build();

		try {
			sesClient.verifyEmailAddress(request);
			System.out.println("Verification email sent to: " + emailAddress);
			verified = true;
		} catch (Exception e) {
			System.err.println("Error verifying email: " + e.getMessage());
		}

		return verified;
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

	public AWSCredentialsManager getCredentialsManager() {
		return credentialsManager;
	}

	public void setCredentialsManager(AWSCredentialsManager credentialsManager) {
		this.credentialsManager = credentialsManager;
	}
}

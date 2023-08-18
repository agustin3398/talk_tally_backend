package uy.com.talktally.aws;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;

public class AWSCredentialsManager {
	private static String accessKey;
	private static String secretKey;
	private static String cognitoUserPoolID;
	private static String cognitoUserPoolClientId;
	private static String cognitoSecretKey;

	public AWSCredentialsManager() {
		loadCredentials();
	}

	private void loadCredentials() {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream("C:/Proyectos/awsconfig.properties")) {
			properties.load(fis);
			accessKey = properties.getProperty("aws_access_key_id");
			secretKey = properties.getProperty("aws_secret_access_key");
			cognitoUserPoolID = properties.getProperty("aws_cognito_userpool_id");
			cognitoUserPoolClientId = properties.getProperty("aws_cognito_userpool_clientId");
			cognitoSecretKey = properties.getProperty("aws_cognito_userpool_secretKey");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public S3Client createS3Client() {
		AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
		
		return S3Client.builder().region(Region.US_EAST_1)
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
	}

	public CognitoIdentityProviderClient createCognitoClient() {
		AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

		return CognitoIdentityProviderClient.builder().region(Region.US_EAST_1)
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
	}
	
	public SesClient createSesClient() {
		AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
		
		return SesClient.builder().region(Region.US_EAST_1)
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).build();
	}

	public String loadUserPoolId() {
		// Load user pool ID from properties file or any other configuration mechanism
		return cognitoUserPoolID;
	}

	public String loadUserPoolClientId() {
		// Load user pool ID from properties file or any other configuration mechanism
		return cognitoUserPoolClientId;
	}
	
	public String loadCognitoSecretKey() {
		// Load user pool ID from properties file or any other configuration mechanism
		return cognitoSecretKey;
	}
}

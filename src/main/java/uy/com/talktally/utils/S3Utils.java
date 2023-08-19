package uy.com.talktally.utils;

import java.io.Serializable;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import uy.com.talktally.aws.AWSCredentialsManager;

public class S3Utils implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6900239251008599516L;
	private static AWSCredentialsManager credentialsManager = new AWSCredentialsManager();
	private S3Utils() {
		// Private constructor to prevent instantiation
	}

	public static boolean bucketExists(String bucketName) {
		S3Client s3Client = credentialsManager.createS3Client();
		HeadBucketRequest request = HeadBucketRequest.builder().bucket(bucketName).build();
		try {
			s3Client.headBucket(request);
			return true;
		} catch (S3Exception e) {
			if (e.statusCode() == 404) {
				return false;
			}
			throw e;
		}
	}
}

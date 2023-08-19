package uy.com.talktally.webservices;

import java.io.Serializable;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import uy.com.talktally.aws.AWSCredentialsManager;
import uy.com.talktally.entities.ServiceError;
import uy.com.talktally.results.ResultListS3Buckets;
import uy.com.talktally.results.ResultSaveMeetingToS3;
import uy.com.talktally.utils.S3Utils;

@RestController
@RequestMapping("/S3ServiceRest")
public class S3ServicesRest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2222841452450050606L;

	private AWSCredentialsManager credentialsManager = new AWSCredentialsManager();
	private String BUCKET_NAME = "talktallyrecordings";

	@GetMapping("/listAllS3Buckets")
	public ResultListS3Buckets listAllS3Buckets() {
		ResultListS3Buckets result = new ResultListS3Buckets();
		ServiceError serviceError = new ServiceError();

		S3Client s3Client = credentialsManager.createS3Client();
		ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
		listBucketsResponse.buckets().forEach(bucket -> result.getBucketsName().add(bucket.name()));

		if (result.getBucketsName().isEmpty()) {
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorCode(100);
			serviceError.setErrorDescription("You don't have any buckets");
			result.setServiceError(serviceError);
		} else {
			serviceError.setError(Boolean.FALSE);
			result.setoKMessage("Buckets listed successfully");
		}
		return result;
	}

	@PostMapping("/saveMeetingToS3")
	public ResultSaveMeetingToS3 saveMeetingToS3(@RequestParam("file") MultipartFile file) {
		ResultSaveMeetingToS3 result = new ResultSaveMeetingToS3();
		ServiceError serviceError = new ServiceError();
		S3Client s3Client = credentialsManager.createS3Client();
		try {
			String fileName = file.getOriginalFilename();
			// Check if the bucket exists, create it if it doesn't
			if (!S3Utils.bucketExists(BUCKET_NAME)) {
				s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
			}

			PutObjectRequest request = PutObjectRequest.builder().bucket(BUCKET_NAME).key(fileName).build();
			s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
			result.setoKMessage("File uploaded successfully.");
			serviceError.setError(Boolean.FALSE);
		} catch (Exception e) {
			serviceError.setError(Boolean.TRUE);
			serviceError.setErrorCode(101);
			serviceError.setErrorDescription("Error trying to upload file");
			result.setServiceError(serviceError);

			return result;
		}

		return result;
	}

	public AWSCredentialsManager getCredentialsManager() {
		return credentialsManager;
	}

	public void setCredentialsManager(AWSCredentialsManager credentialsManager) {
		this.credentialsManager = credentialsManager;
	}

}

package uy.com.talktally.webservices;

import org.springframework.web.multipart.MultipartFile;

import uy.com.talktally.results.ResultListS3Buckets;
import uy.com.talktally.results.ResultSaveMeetingToS3;

public interface IWsS3Services {

	public ResultListS3Buckets listAllS3Buckets();
	
	public ResultSaveMeetingToS3 saveMeetingToS3(MultipartFile file);
}

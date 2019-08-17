package com.codetreatise.awss3service;

import java.io.File;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.codetreatise.bean.PhotoFile;

import javafx.collections.ObservableList;

@Service
public class S3Application {
	
	private static final AWSCredentials credentials;
    private static String bucketName;  

    static {
        credentials = new BasicAWSCredentials(
          "AKIAREP2W3OGFSW4F4YT", 
          "Uor8NdLf7MaeMPToZlDtN3VQuYgC6tCQXiHoXwPh"
        );
    }
    
    public AmazonS3 createConnection() {
		AmazonS3 s3client = AmazonS3ClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.AP_SOUTH_1).build();

		return s3client;
    }
    
    public boolean startUploadActivity(PhotoFile photofile, AmazonS3 amazonS3) throws InterruptedException {
		String existingBucketName = "taufiq2215-bucket"+"/"+"13082019";
		int maxUploadThreads = 5;
		TransferManager tm = TransferManagerBuilder.standard().withS3Client(amazonS3)
				.withMultipartUploadThreshold((long) (5 * 1024 * 1024))
				.withExecutorFactory(() -> Executors.newFixedThreadPool(maxUploadThreads)).build();

		ProgressListener progressListener = progressEvent -> {
			//System.out.println("Transferred bytes: " + progressEvent.getBytesTransferred());
			if (progressEvent.getEventType() == ProgressEventType.TRANSFER_COMPLETED_EVENT) {
				System.out.println("Event Completed");
			}
		};
		
		Upload upload = null;
		PutObjectRequest request = new PutObjectRequest(existingBucketName, photofile.getFileName(),
				new File(photofile.getFullPath()));
		request.setGeneralProgressListener(progressListener);
		upload = tm.upload(request);
		try {
			upload.waitForCompletion();
			System.out.println("Upload complete for "+photofile.getFileName());
		} catch (AmazonClientException e) {
			System.out.println("Error occurred while uploading file");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}

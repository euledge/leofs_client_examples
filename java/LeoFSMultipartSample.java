import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListPartsRequest;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PartListing;
import com.amazonaws.services.s3.model.UploadPartRequest;

public class LeoFSMultipartSample {

    public static void main(String[] args) throws IOException {

        ClientConfiguration config = new ClientConfiguration();
        config.setProxyHost("localhost"); // LeoFS Gateway Host
        config.setProxyPort(8080);        // LeoFS Gateway Port
        config.withProtocol(Protocol.HTTP);

        String accessKeyId = "YOUR_ACCESS_KEY_ID";
        String secretAccessKey = "YOUR_ACCESS_SECRET";

        AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);

        AmazonS3 s3 = new AmazonS3Client(credentials, config);

        String bucketName = "test_bucket";
        String key = "key";

        File file = createSampleFile();
        long contentLength = file.length();
        long partSize = 5 * 1024 * 1024; // 5MB

        try {
            // initialize
            List<PartETag> partETags = new ArrayList<PartETag>();

            InitiateMultipartUploadRequest initRequest =
                new InitiateMultipartUploadRequest(bucketName, key);

            System.out.println(initRequest);
            InitiateMultipartUploadResult initResponse =
                s3.initiateMultipartUpload(initRequest);
            
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));
 
                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(bucketName).withKey(key)
                    .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                    .withFileOffset(filePosition)
                    .withFile(file)
                    .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(s3.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: complete.
            CompleteMultipartUploadRequest compRequest = new 
                CompleteMultipartUploadRequest(bucketName, 
                                               key, 
                                               initResponse.getUploadId(), 
                                               partETags);

            s3.completeMultipartUpload(compRequest);

            // GET S3 object
            S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));

            // DELETE S3 object
            s3.deleteObject(bucketName, key);

            // DELETE bucket
            s3.deleteBucket(bucketName);

        } catch (AmazonServiceException ase) {
            System.out.println(ase.getMessage());
            System.out.println(ase.getStatusCode());
        } catch (AmazonClientException ace) {
            System.out.println(ace.getMessage());
        }
    }

    private static File createSampleFile() throws IOException {
        File file = File.createTempFile("leofs_test", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        for (int i=0; i < 1000; i++) {
            writer.write("aaaaaaaaaa");
        }
        writer.close();

        return file;
    }
}

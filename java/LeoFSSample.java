import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;

public class LeoFSSample {

    public static void main(String[] args) throws IOException {

        ClientConfiguration config = new ClientConfiguration();
        config.setProxyHost("localhost"); // LeoFS Gateway Host
        config.setProxyPort(8080);        // LeoFS Gateway Port
        config.withProtocol(Protocol.HTTP);

        String accessKeyId = "YOUR_ACCESS_KEY_ID";
        String secretAccessKey = "YOUR_ACCESS_SECRET";

        AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);

        AmazonS3 s3 = new AmazonS3Client(credentials, config);

        String bucketName = "test-bucket-" + UUID.randomUUID();
        String key = "test-key";

        try {
            // create bucket
            s3.createBucket(bucketName);

            // list buckets
            for (Bucket bucket : s3.listBuckets()) {
                System.out.println(bucket.getName());
            }
            System.out.println();

            // PUT S3 object
            s3.putObject(new PutObjectRequest(bucketName, key, createSampleFile()));

            // GET S3 object
            S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
            displayTextInputStream(object.getObjectContent());

            // list objects
            ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName));
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                System.out.println(objectSummary.getKey() + "  (size = " + objectSummary.getSize() + ")");
            }
            System.out.println();

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
        writer.write("Hello, world!\n");
        writer.close();

        return file;
    }

    private static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;
            System.out.println(line);
        }
        System.out.println();
    }
}

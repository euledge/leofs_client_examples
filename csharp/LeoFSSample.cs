using System;
using System.IO;

using Amazon.S3;
using Amazon.S3.Model;
using System.Configuration;
using Amazon.Runtime;

namespace LeoFSConsoleApp
{
    class LeoFSSample
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("==============================================");
            Console.WriteLine("Welcome to the LeoFS .NET Sample With AWS SDK!");
            Console.WriteLine("==============================================");

            // Configure LeoFS Access Point
            AmazonS3Config config = new AmazonS3Config();
            config.ProxyHost = "leofs";
            config.ProxyPort = 8080;
            config.CommunicationProtocol = Protocol.HTTP;

            // Create S3Client with connected
            // Ex.1 Omissible credentials parameter, Because credential loaded from the application's default configuration.
            //      Apprication's default configuration 
            //        - ConfigurationManager.AppSettings["AWSAccessKey"]
            //        - ConfigurationManager.AppSettings["AWSSecretKey"]
            //
            //      AmazonS3Client s3 = new AmazonS3Client(config);
            // Ex.2 Use explicit credentials parameter.
            //
            //      string accessKeyId = "YOUR_ACCESS_KEY_ID";
            //      string secretAccessKey = "YOUR_SECRET_ACCESS_KEY";
            //      AmazonS3Client s3 = new AmazonS3Client(accessKeyId, secretAccessKey, config);
            AmazonS3Client s3 = new AmazonS3Client(config);
            string bucketName = "test-bucket-" + Guid.NewGuid().ToString();
            string key = "test-key";

            try
            {
                // Create a bucket
                Console.WriteLine("Create Bucket : {0}", bucketName);
                s3.PutBucket(new PutBucketRequest().WithBucketName(bucketName));

                // PUT an object into the LeoFS
                string objectName = createFile();
                PutObjectRequest putObjectRequest = new PutObjectRequest
                {
                    BucketName = bucketName,
                    Key = key,
                    FilePath = objectName
                };

                Console.WriteLine("PUT an object : {0} {1}", key, objectName);
                s3.PutObject(putObjectRequest);

                // GET an object from the LeoFS
                Console.WriteLine("GET an object from the LeoFS : {0} {1}", bucketName, key);
                GetObjectResponse objectResponse = s3.GetObject(new GetObjectRequest()
                                                                    .WithBucketName(bucketName)
                                                                    .WithKey(key));
                Console.WriteLine("Object Contents : {0}", dumpInputStream(objectResponse.ResponseStream));

                // Retrieve list of objects from the LeoFS
                var listObjects = s3.ListObjects(new ListObjectsRequest().WithBucketName(bucketName));

                Console.WriteLine("Retrieve list of objects from the LeoFS : {0}", bucketName);
                foreach (var item in listObjects.S3Objects)
                {
                    Console.WriteLine("Key: {0}", item.Key);
                    Console.WriteLine("Size: {0}", item.Size);
                    Console.WriteLine("LastModified {0}", item.LastModified);
                    Console.WriteLine();
                }
                // DELETE an object from the LeoFS
                s3.DeleteObject(new DeleteObjectRequest().WithBucketName(bucketName).WithKey(key));

                // DELETE a bucket from the LeoFS
                s3.DeleteBucket(new DeleteBucketRequest().WithBucketName(bucketName));
            }
            catch (AmazonServiceException ase)
            {
                Console.WriteLine("ErrorMassage : {0}", ase.Message);
                Console.WriteLine("StatusCode : {0}", ase.StatusCode);
            }
            catch (AmazonClientException ace)
            {
                Console.WriteLine("ErrorMassage : {0}", ace.Message);
            }
            Console.WriteLine("Press any key to continue...");
            Console.ReadLine();
        }

        private static string createFile()
        {
            string path = Path.GetTempPath() + "leofs_test.txt";
            using (StreamWriter sw = new StreamWriter(path))
            {
                sw.WriteLine("Hello world!");
            }
            return path;
        }
        private static string dumpInputStream(Stream input)
        {
            string contents;
            using (StreamReader reader = new StreamReader(input))
            {
                contents = reader.ReadToEnd();
            }
            return contents;
        }
    }
}
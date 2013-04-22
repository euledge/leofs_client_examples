<?php
require "vendor/autoload.php";

use Aws\Common\Enum\Region;
use Aws\S3\S3Client;

$client = S3Client::factory(array(
  "key" => "YOUR ACCESS KEY ID",
  "secret" => "YOUR SECRET ACCESS KEY",
  "region" => Region::US_EAST_1,
  "scheme" => "http",
));

// list buckets
$buckets = $client->listBuckets()->toArray();

foreach($buckets as $bucket){
  print_r($bucket);
}
print("\n\n");

// create bucket
$result = $client->createBucket(array(
  "Bucket" => "test"
));

// PUT object
$client->putObject(array(
  "Bucket" => "test",
  "Key" => "key-test",
  "Body" => "Hello, world!"
));

// GET object
$object = $client->getObject(array(
  "Bucket" => "test",
  "Key" => "key-test"
));
print($object->get("Body"));
print("\n\n");

// HEAD object
$headers = $client->headObject(array(
  "Bucket" => "test",
  "Key" => "key-test"
));
print_r($headers->toArray());

// DELETE object
$client->deleteObject(array(
  "Bucket" => "test",
  "Key" => "key-test"
));
?>

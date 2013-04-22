<?php
require_once 'AWSSDKforPHP/sdk.class.php';

$s3 = new AmazonS3(array(
  "key" => "YOUR ACCESS KEY ID",
  "secret" => "YOUR SECRET ACCESS KEY",
));
$s3->use_ssl = false;
$s3->enable_path_style();

$bucket_name = "bucket";
$object_name = "key";

# create bucket (region is a dummy)
$bucket = $s3->create_bucket($bucket_name, AmazonS3::REGION_US_E1);

# create object
$object = $s3->create_object($bucket_name, $object_name, array("body" => "This is a new object."));

# get object
$object = $s3->get_object($bucket_name, $object_name);
print_r($object);

# get list of buckets
$buckets = $s3->get_bucket_list();
print_r($buckets);

# head
$head = $s3->get_object_headers($bucket_name, $object_name);
print_r($head);

# delete
$result = $s3->delete_object($bucket_name, $object_name);
print_r($result);
?>

require "aws-sdk"

Endpoint = "localhost"
Port = 8080

class LeoFSHandler < AWS::Core::Http::NetHttpHandler
  # magic to reconfigure port
  def handle(request, response)
    request.port = ::Port
    super
  end
end

AWS.config(
  access_key_id: "YOUR_ACCESS_KEY_ID", # set your s3 key
  secret_access_key: "YOUR_SECRET_ACCESS_KEY",
  s3_endpoint: Endpoint,
  http_handler: LeoFSHandler.new,
  s3_force_path_style: true,
  use_ssl: false
)

s3 = AWS::S3.new

# create bucket
s3.buckets.create("photo")

# get bucket
bucket = s3.buckets["photo"]

# create a new object
object = bucket.objects.create("image", "value")

# show objects in the bucket
bucket.objects.with_prefix("").each do |obj|
  p obj
end

# retrieve an object
object = bucket.objects["image"]

# insert an object
object.write(
  file: "/path/to/image.png",
  content_type: "png/image"
)

# HEAD an object
metadata = object.head
p metadata.to_hash

# GET an object
image = object.read

# DELETE an object
object.delete

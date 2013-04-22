require "aws-sdk"

Endpoint = "leofs.org"
Port = 8080

class LeoFSHandler < AWS::Core::Http::NetHttpHandler
  def handle(request, response)
    request.port = ::Port
    super
  end
end

AWS.config(
  :access_key_id => "YOUR_ACCESS_KEY_ID",
  :secret_access_key => "YOUR_SECRET_ACCESS_KEY",
  s3_endpoint: Endpoint,
  http_handler: LeoFSHandler.new,
  s3_force_path_style: true,
  use_ssl: false
)

file_path_for_multipart_upload = "/path/to/file"
bucket = AWS::S3.new.buckets["bucket-name"]
uploading_object = bucket.objects[File.basename(file.path)]

File.open(file_path_for_multipart_upload) do |file|
  uploading_object.multipart_upload do |upload|
    while part = file.read(5242880) # 5MB
      upload.add_part(part)
      warn "Aborted" if upload.aborted?
    end
  end
end

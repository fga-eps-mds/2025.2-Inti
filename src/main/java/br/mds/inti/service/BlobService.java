package br.mds.inti.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
@Slf4j
public class BlobService {

    private BlobServiceClient blobServiceClient;

    @Value("${azure.blob-storage.connection-string}")
    private String connectionString;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    @PostConstruct
    public void init() {
        try {
            blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        } catch (Exception e) {
            log.error("Failed to initialize BlobServiceClient", e);
        }
    }

    public String uploadImageWithDescription(MultipartFile file) throws IOException {

        if (!isImage(file)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not an image");

        String blobFilename = file.getOriginalFilename();
        if (blobFilename == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image filename is null");

        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobFilename);

        blobClient.upload(file.getInputStream(), file.getSize(), true);

        String blobName = blobClient.getBlobName();
        return blobName;
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp"));
    }

    public void deleteImage(String blobName) {
        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName);

        blobClient.delete();
    }
}
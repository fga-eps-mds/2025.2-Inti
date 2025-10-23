package br.mds.inti.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class BlobService {

    private BlobServiceClient blobServiceClient;

    @Value("${azure.blob-storage.connection-string}")
    private String connectionString;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;

    @Value("${azure.blob-storage.sas-token}")
    private String sasToken;

    @PostConstruct
    public void init() {
        log.info("CONNECTION STRING: " + connectionString);
        log.info("CONTAINER NAME: " + containerName);

        if (connectionString == null || connectionString.isBlank() ||
            containerName == null || containerName.isBlank()) {
            log.error("Azure Blob Storage configuration missing. Skipping BlobService initialization.");
            return;
        }

        try {
            blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        } catch (Exception e) {
            log.error("Failed to initialize BlobServiceClient", e);
        }
    }

    public String uploadImageWithDescription(MultipartFile image) throws IOException {

        String blobFilename = image.getOriginalFilename();

        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobFilename);

        blobClient.upload(image.getInputStream(), image.getSize(), true);

        String imageUrl = blobClient.getBlobUrl();
        return imageUrl;
    }
}
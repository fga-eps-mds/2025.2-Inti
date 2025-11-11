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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

    public String uploadImage(UUID userId, MultipartFile file) throws IOException {

        if (!isImage(file))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not an image");

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image filename is null");

        String fileExtension = getFileExtension(originalFilename);
        String blobFilename = generateUniqueName(userId, fileExtension);

        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobFilename);

        blobClient.upload(file.getInputStream(), file.getSize(), true);

        return blobClient.getBlobName();
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

    private String generateUniqueName(UUID userId, String fileExtension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s%s", userId, timestamp, randomSuffix, fileExtension);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return ".jpg";
    }

    public byte[] downloadImage(String blobName) {
        try {
            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient(containerName)
                    .getBlobClient(blobName);

            if (!blobClient.exists()) {
                return null;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error downloading image: {}", blobName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error downloading image");
        }
    }
}
package br.mds.inti.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.mds.inti.service.BlobService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/images")

public class ImageController {
    private final BlobService blobService;

    public ImageController(BlobService blobService) {
        this.blobService = blobService;
    }

    @GetMapping("/{blobName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String blobName) {
        try {
            byte[] imageData = blobService.downloadImage(blobName);

            if (imageData == null) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = detectMediaType(blobName);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(imageData);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving image");
        }
    }

    private MediaType detectMediaType(String blobName) {

        String lowerBlobName = blobName.toLowerCase();
        if (lowerBlobName.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lowerBlobName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lowerBlobName.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }

        return MediaType.IMAGE_JPEG;
    }

}

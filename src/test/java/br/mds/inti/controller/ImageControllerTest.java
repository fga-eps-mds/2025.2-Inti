package br.mds.inti.controller;

import br.mds.inti.service.BlobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    @Mock
    private BlobService blobService;

    @InjectMocks
    private ImageController imageController;

    private byte[] mockImageData;

    @BeforeEach
    void setUp() {
        mockImageData = "fake-image-data".getBytes();
    }

    @Test
    void getImage_WhenImageExists_ShouldReturnImageWithJpegMediaType() {
        // Arrange
        String blobName = "test-image.jpg";
        when(blobService.downloadImage(blobName)).thenReturn(mockImageData);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockImageData, response.getBody());
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
        verify(blobService).downloadImage(blobName);
    }

    @Test
    void getImage_WhenImageIsPng_ShouldReturnImageWithPngMediaType() {
        // Arrange
        String blobName = "test-image.png";
        when(blobService.downloadImage(blobName)).thenReturn(mockImageData);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        verify(blobService).downloadImage(blobName);
    }

    @Test
    void getImage_WhenImageIsGif_ShouldReturnImageWithGifMediaType() {
        // Arrange
        String blobName = "test-image.gif";
        when(blobService.downloadImage(blobName)).thenReturn(mockImageData);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_GIF, response.getHeaders().getContentType());
        verify(blobService).downloadImage(blobName);
    }

    @Test
    void getImage_WhenImageIsWebp_ShouldReturnImageWithWebpMediaType() {
        // Arrange
        String blobName = "test-image.webp";
        when(blobService.downloadImage(blobName)).thenReturn(mockImageData);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.parseMediaType("image/webp"), response.getHeaders().getContentType());
        verify(blobService).downloadImage(blobName);
    }

    @Test
    void getImage_WhenImageNotFound_ShouldReturnNotFound() {
        // Arrange
        String blobName = "nonexistent-image.jpg";
        when(blobService.downloadImage(blobName)).thenReturn(null);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(blobService).downloadImage(blobName);
    }

    @Test
    void getImage_WhenResponseStatusExceptionThrown_ShouldRethrowException() {
        // Arrange
        String blobName = "error-image.jpg";
        ResponseStatusException expectedException = new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid request");
        when(blobService.downloadImage(blobName)).thenThrow(expectedException);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> imageController.getImage(blobName));
        assertEquals(expectedException, exception);
        verify(blobService).downloadImage(blobName);
    }

    @Test
    void getImage_WhenGenericExceptionThrown_ShouldThrowInternalServerError() {
        // Arrange
        String blobName = "error-image.jpg";
        when(blobService.downloadImage(blobName)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> imageController.getImage(blobName));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Error retrieving image", exception.getReason());
        verify(blobService).downloadImage(blobName);
    }

    @Test
    void getImage_WithUppercaseExtension_ShouldDetectMediaTypeCorrectly() {
        // Arrange
        String blobName = "test-image.PNG";
        when(blobService.downloadImage(blobName)).thenReturn(mockImageData);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithMixedCaseExtension_ShouldDetectMediaTypeCorrectly() {
        // Arrange
        String blobName = "test-image.WeBp";
        when(blobService.downloadImage(blobName)).thenReturn(mockImageData);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(MediaType.parseMediaType("image/webp"), response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithNoExtension_ShouldDefaultToJpeg() {
        // Arrange
        String blobName = "test-image";
        when(blobService.downloadImage(blobName)).thenReturn(mockImageData);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }

    @Test
    void getImage_WithUnknownExtension_ShouldDefaultToJpeg() {
        // Arrange
        String blobName = "test-image.unknown";
        when(blobService.downloadImage(blobName)).thenReturn(mockImageData);

        // Act
        ResponseEntity<byte[]> response = imageController.getImage(blobName);

        // Assert
        assertNotNull(response);
        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }
}

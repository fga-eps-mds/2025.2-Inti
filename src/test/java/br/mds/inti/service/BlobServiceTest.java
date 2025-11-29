package br.mds.inti.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlobServiceTest {

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private BlobClient blobClient;

    @InjectMocks
    private BlobService blobService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        ReflectionTestUtils.setField(blobService, "containerName", "test-container");
    }

    @Test
    void uploadImage_WithValidJpegImage_ShouldUploadSuccessfully() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake-image-data".getBytes());

        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobName()).thenReturn("uploaded-blob-name.jpg");

        // Act
        String blobName = blobService.uploadImage(userId, file);

        // Assert
        assertNotNull(blobName);
        assertEquals("uploaded-blob-name.jpg", blobName);
        verify(blobClient).upload(any(), anyLong(), anyBoolean());
    }

    @Test
    void uploadImage_WithValidPngImage_ShouldUploadSuccessfully() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "fake-image-data".getBytes());

        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobName()).thenReturn("uploaded-blob-name.png");

        // Act
        String blobName = blobService.uploadImage(userId, file);

        // Assert
        assertNotNull(blobName);
        assertTrue(blobName.endsWith(".png"));
        verify(blobClient).upload(any(), anyLong(), anyBoolean());
    }

    @Test
    void uploadImage_WithValidWebpImage_ShouldUploadSuccessfully() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                "fake-image-data".getBytes());

        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobName()).thenReturn("uploaded-blob-name.webp");

        // Act
        String blobName = blobService.uploadImage(userId, file);

        // Assert
        assertNotNull(blobName);
        assertTrue(blobName.endsWith(".webp"));
        verify(blobClient).upload(any(), anyLong(), anyBoolean());
    }

    @Test
    void uploadImage_WithNonImageFile_ShouldThrowBadRequestException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "fake-pdf-data".getBytes());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> blobService.uploadImage(userId, file));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("File is not an image", exception.getReason());
        verify(blobClient, never()).upload(any(), anyLong(), anyBoolean());
    }

    @Test
    void uploadImage_WithNullContentType_ShouldThrowBadRequestException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                null,
                "fake-image-data".getBytes());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> blobService.uploadImage(userId, file));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("File is not an image", exception.getReason());
    }

    @Test
    void uploadImage_WithFilenameWithoutExtension_ShouldUseDefaultExtension() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testimage",
                "image/jpeg",
                "fake-image-data".getBytes());

        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobName()).thenReturn("uploaded-blob.jpg");

        // Act
        String blobName = blobService.uploadImage(userId, file);

        // Assert
        assertNotNull(blobName);
        verify(blobClient).upload(any(), anyLong(), anyBoolean());
    }

    @Test
    void deleteImageAlreadyDeleted() {
        // Arrange
        String blobName = "test-blob.jpg";
        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);

        // Act
        assertThrows(ResponseStatusException.class, () -> {
            blobService.deleteImage(blobName);
        });
    }

    @Test
    void downloadImage_WhenImageExists_ShouldReturnImageData() {
        // Arrange
        String blobName = "test-blob.jpg";
        byte[] imageData = "fake-image-data".getBytes();

        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);

        doAnswer(invocation -> {
            ByteArrayOutputStream outputStream = invocation.getArgument(0);
            outputStream.write(imageData);
            return null;
        }).when(blobClient).downloadStream(any(ByteArrayOutputStream.class));

        // Act
        byte[] result = blobService.downloadImage(blobName);

        // Assert
        assertNotNull(result);
        assertArrayEquals(imageData, result);
        verify(blobClient).exists();
        verify(blobClient).downloadStream(any(ByteArrayOutputStream.class));
    }

    @Test
    void downloadImage_WhenImageDoesNotExist_ShouldReturnNull() {
        // Arrange
        String blobName = "nonexistent-blob.jpg";

        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(false);

        // Act
        byte[] result = blobService.downloadImage(blobName);

        // Assert
        assertNull(result);
        verify(blobClient).exists();
        verify(blobClient, never()).downloadStream(any());
    }

    @Test
    void downloadImage_WhenExceptionOccurs_ShouldThrowInternalServerError() {
        // Arrange
        String blobName = "error-blob.jpg";

        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(blobName)).thenReturn(blobClient);
        when(blobClient.exists()).thenThrow(new RuntimeException("Azure connection error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> blobService.downloadImage(blobName));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Error downloading image", exception.getReason());
    }

    @Test
    void uploadImage_ShouldGenerateUniqueBlobName() throws IOException {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "fake-image-data".getBytes());

        when(blobServiceClient.getBlobContainerClient("test-container")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.getBlobName())
                .thenReturn("blob1.jpg")
                .thenReturn("blob2.jpg");

        // Act
        String blobName1 = blobService.uploadImage(userId, file1);
        String blobName2 = blobService.uploadImage(userId, file1);

        // Assert
        assertNotEquals(blobName1, blobName2);
    }
}

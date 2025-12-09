package br.mds.inti.controller;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.service.FollowService;
import br.mds.inti.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private FollowService followService;

    @InjectMocks
    private ProfileController profileController;

    private ProfileResponse mockProfileResponse;
    private FollowResponse mockFollowResponse;

    @BeforeEach
    void setUp() {
        mockProfileResponse = new ProfileResponse(
                "Test User",
                "testuser",
                "emailtest@gmail.com",
                "9980223030",
                "http://example.com/avatar.jpg",
                "Test bio",
                10,
                5,
                List.of());

        mockFollowResponse = new FollowResponse("Perfil seguido com sucesso.");
    }

    @Test
    void getMe_ShouldReturnProfileResponse() {
        // Arrange
        when(profileService.getProfile(0, 10)).thenReturn(mockProfileResponse);

        // Act
        ResponseEntity<ProfileResponse> response = profileController.getMe(10, 0);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProfileResponse, response.getBody());
        verify(profileService).getProfile(0, 10);
    }

    @Test
    void getPublicProfile_ShouldReturnProfileResponse() {
        // Arrange
        String username = "testuser";
        when(profileService.getProfileByUsername(username, 0, 10)).thenReturn(mockProfileResponse);

        // Act
        ResponseEntity<ProfileResponse> response = profileController.getPublicProfile(username, 10, 0);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockProfileResponse, response.getBody());
        verify(profileService).getProfileByUsername(username, 0, 10);
    }

    @Test
    void followProfile_ShouldReturnFollowResponse() {
        // Arrange
        String username = "usertofollow";
        when(followService.followProfile(username)).thenReturn(mockFollowResponse);

        // Act
        ResponseEntity<FollowResponse> response = profileController.followProfile(username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockFollowResponse, response.getBody());
        verify(followService).followProfile(username);
    }

    @Test
    void unfollowProfile_ShouldReturnFollowResponse() {
        // Arrange
        String username = "usertounfollow";
        when(followService.unfollowProfile(username)).thenReturn(mockFollowResponse);

        // Act
        ResponseEntity<FollowResponse> response = profileController.unfollowProfile(username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockFollowResponse, response.getBody());
        verify(followService).unfollowProfile(username);
    }

    @Test
    void getMe_WithDifferentPagination_ShouldCallServiceWithCorrectParams() {
        // Arrange
        when(profileService.getProfile(2, 20)).thenReturn(mockProfileResponse);

        // Act
        ResponseEntity<ProfileResponse> response = profileController.getMe(20, 2);

        // Assert
        assertNotNull(response);
        verify(profileService).getProfile(2, 20);
    }

    @Test
    void getPublicProfile_WithDifferentPagination_ShouldCallServiceWithCorrectParams() {
        // Arrange
        String username = "testuser";
        when(profileService.getProfileByUsername(username, 1, 5)).thenReturn(mockProfileResponse);

        // Act
        ResponseEntity<ProfileResponse> response = profileController.getPublicProfile(username, 5, 1);

        // Assert
        assertNotNull(response);
        verify(profileService).getProfileByUsername(username, 1, 5);
    }

    @Test
    void setMyProfilePhoto_ShouldReturnCreated() throws IOException {
        // Arrange
        MultipartFile mockImage = new MockMultipartFile(
                "myImage",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());
        doNothing().when(profileService).setPhoto(mockImage);

        // Act
        ResponseEntity<Void> response = profileController.setMyProfilePhoto(mockImage);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(profileService).setPhoto(mockImage);
    }

    @Test
    void setMyProfilePhoto_WhenIOExceptionThrown_ShouldThrowResponseStatusException() throws IOException {
        // Arrange
        MultipartFile mockImage = new MockMultipartFile(
                "myImage",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes());
        doThrow(new IOException("Upload failed")).when(profileService).setPhoto(mockImage);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileController.setMyProfilePhoto(mockImage));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Error trying to set profile image", exception.getReason());
        verify(profileService).setPhoto(mockImage);
    }

    @Test
    void updateUser_ShouldReturnCreated() throws IOException {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Name",
                "updateduser",
                "9988776655",
                "updated@example.com",
                "Updated bio",
                null);
        doNothing().when(profileService).updateUser(updateRequest);

        // Act
        ResponseEntity<Void> response = profileController.user(updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(profileService).updateUser(updateRequest);
    }

    @Test
    void updateUser_WhenIOExceptionThrown_ShouldThrowResponseStatusException() throws IOException {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Name",
                "updateduser",
                "9988776655",
                "updated@example.com",
                "Updated bio",
                null);
        doThrow(new IOException("Update failed")).when(profileService).updateUser(updateRequest);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileController.user(updateRequest));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Error trying to update profile", exception.getReason());
        verify(profileService).updateUser(updateRequest);
    }

    @Test
    void updateUser_WithProfilePicture_ShouldReturnCreated() throws IOException {
        // Arrange
        MultipartFile mockImage = new MockMultipartFile(
                "profilePicture",
                "newavatar.jpg",
                "image/jpeg",
                "new avatar content".getBytes());
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Name",
                "updateduser",
                "9988776655",
                "updated@example.com",
                "Updated bio",
                mockImage);
        doNothing().when(profileService).updateUser(updateRequest);

        // Act
        ResponseEntity<Void> response = profileController.user(updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(profileService).updateUser(updateRequest);
    }
}
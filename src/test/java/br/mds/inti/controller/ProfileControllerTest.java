package br.mds.inti.controller;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.dto.ProfileResponse;
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
    void getString_ShouldReturnTestString() {
        // Act
        ResponseEntity<String> response = profileController.getString();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("teste", response.getBody());
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
    void getStringOrg_ShouldReturnTestString() {
        // Act
        ResponseEntity<String> response = profileController.getStringOrg();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("teste", response.getBody());
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
}
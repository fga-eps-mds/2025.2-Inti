// java
package br.mds.inti.controller;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.service.FollowService;
import br.mds.inti.service.OrganizationService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private FollowService followService;

    @InjectMocks
    private OrganizationController organizationController;

    private ProfileResponse mockOrgResponse;
    private FollowResponse mockFollowResponse;

    @BeforeEach
    void setUp() {
        mockOrgResponse = new ProfileResponse(
                "Test Org",
                "testorg",
                "org@example.com",
                "9980223030",
                "http://example.com/logo.jpg",
                "Org description",
                100,
                50,
                List.of());

        mockFollowResponse = new FollowResponse("Organization followed successfully.");
    }

    @Test
    void getMyOrganization_ShouldReturnOrganizationResponse() {
        when(organizationService.getOrganization(anyInt(), anyInt(), any())).thenReturn(mockOrgResponse);

        ResponseEntity<ProfileResponse> response = organizationController.getMe(10, 0);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockOrgResponse, response.getBody());
        verify(organizationService).getOrganization(eq(0), eq(10), any());
    }

    @Test
    void getPublicOrganization_ShouldReturnOrganizationResponse() {
        String username = "testorg";
        when(organizationService.getOrganizationByUsername(username, 0, 10)).thenReturn(mockOrgResponse);

        ResponseEntity<ProfileResponse> response = organizationController.getPublicProfile(username, 10, 0);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockOrgResponse, response.getBody());
        verify(organizationService).getOrganizationByUsername(username, 0, 10);
    }

    @Test
    void followOrganization_ShouldReturnFollowResponse() {
        String username = "orgToFollow";
        when(followService.followProfile(username)).thenReturn(mockFollowResponse);

        ResponseEntity<FollowResponse> response = organizationController.followProfile(username);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockFollowResponse, response.getBody());
        verify(followService).followProfile(username);
    }

    @Test
    void unfollowOrganization_ShouldReturnFollowResponse() {
        String username = "orgToUnfollow";
        when(followService.unfollowProfile(username)).thenReturn(mockFollowResponse);

        ResponseEntity<FollowResponse> response = organizationController.unfollowOrganization(username);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockFollowResponse, response.getBody());
        verify(followService).unfollowProfile(username);
    }

    @Test
    void setOrganizationLogo_ShouldReturnCreated() throws IOException {
        MultipartFile mockImage = new MockMultipartFile(
                "logo",
                "logo.jpg",
                "image/jpeg",
                "logo content".getBytes());
        doNothing().when(organizationService).setPhoto(any(MultipartFile.class), any());

        ResponseEntity<Void> response = organizationController.setMyOrgnizationPhoto(mockImage);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(organizationService).setPhoto(eq(mockImage), any());
    }

    @Test
    void setOrganizationLogo_WhenIOExceptionThrown_ShouldThrowResponseStatusException() throws IOException {
        MultipartFile mockImage = new MockMultipartFile(
                "logo",
                "logo.jpg",
                "image/jpeg",
                "logo content".getBytes());
        doThrow(new IOException("Upload failed")).when(organizationService).setPhoto(any(MultipartFile.class), any());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> organizationController.setMyOrgnizationPhoto(mockImage));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Error trying to set organization image", exception.getReason());
        verify(organizationService).setPhoto(eq(mockImage), any());
    }

    @Test
    void updateOrganization_ShouldReturnCreated() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Org Name",
                "updatedorg",
                "updated@example.com",
                "Updated description",
                "bio",
                null);
        doNothing().when(organizationService).updateOrganization(any(UpdateUserRequest.class), any());

        ResponseEntity<Void> response = organizationController.updateOrganization(updateRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(organizationService).updateOrganization(eq(updateRequest), any());
    }

    @Test
    void updateOrganization_WhenIOExceptionThrown_ShouldThrowResponseStatusException() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Org Name",
                "updatedorg",
                "updated@example.com",
                "Updated description",
                "bio",
                null);
        doThrow(new IOException("Update failed"))
                .when(organizationService).updateOrganization(any(UpdateUserRequest.class), any());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> organizationController.updateOrganization(updateRequest));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Error trying to update organization", exception.getReason());
        verify(organizationService).updateOrganization(eq(updateRequest), any());
    }
}
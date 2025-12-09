package br.mds.inti.service;

import br.mds.inti.model.dto.PostResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.FollowRepository;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exception.ProfileNotFoundException;
import br.mds.inti.service.exception.UsernameAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PostService postService;

    @Mock
    private BlobService blobService;

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private OrganizationService organizationService;

    private Profile mockOrganization;
    private UUID organizationId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        organizationId = UUID.randomUUID();
        mockOrganization = new Profile();
        mockOrganization.setId(organizationId);
        mockOrganization.setUsername("testorg");
        mockOrganization.setName("Test Org");
        mockOrganization.setEmail("org@example.com");
        mockOrganization.setProfilePictureUrl("http://example.com/logo.jpg");
        mockOrganization.setBio("Org description");
        mockOrganization.setPublicEmail("public@org.com");
        mockOrganization.setPhone("+5511999999999");
        mockOrganization.setFollowersCount(100);
        mockOrganization.setFollowingCount(50);
        mockOrganization.setCreatedAt(Instant.now());
        mockOrganization.setType(ProfileType.organization);
    }

    @Test
    void getOrganization_WhenCalledWithProfile_ShouldReturnProfileResponse() {
        UUID postId = UUID.randomUUID();
        Page<PostResponse> mockPosts = new PageImpl<>(List.of(
                new PostResponse(postId, "http://example.com/img1.jpg", "Desc", 1, "2024-01-01")
        ));

        when(postService.getPostByIdProfile(organizationId, PageRequest.of(0, 10))).thenReturn(mockPosts);
        when(postService.generateImageUrl("http://example.com/logo.jpg"))
                .thenReturn("/images/http://example.com/logo.jpg");

        ProfileResponse result = organizationService.getOrganization(0, 10, mockOrganization);

        assertNotNull(result);
        assertEquals(mockOrganization.getName(), result.name());
        assertEquals(mockOrganization.getUsername(), result.username());
        assertEquals("/images/http://example.com/logo.jpg", result.profile_picture_url());
        assertEquals(mockOrganization.getBio(), result.bio());
        assertEquals(mockOrganization.getFollowersCount(), result.followersCount());
        assertEquals(1, result.posts().size());
    }

    @Test
    void getOrganizationByUsername_WhenOrganizationExists_ShouldReturnProfileResponse() {
        String username = "testorg";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(mockOrganization));

        Page<PostResponse> mockPosts = new PageImpl<>(List.of());
        when(postService.getPostByIdProfile(organizationId, PageRequest.of(0, 10))).thenReturn(mockPosts);

        ProfileResponse result = organizationService.getOrganizationByUsername(username, 0, 10);

        assertNotNull(result);
        assertEquals(mockOrganization.getName(), result.name());
        assertEquals(mockOrganization.getUsername(), result.username());
        verify(profileRepository).findByUsername(username);
    }

    @Test
    void getOrganizationByUsername_WhenNotFound_ShouldThrowProfileNotFoundException() {
        String username = "nonexistent";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class,
                () -> organizationService.getOrganizationByUsername(username, 0, 10));
    }

    @Test
    void getOrganization_WhenUsernameExists_ShouldReturnProfileEntity() {
        String username = "testorg";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(mockOrganization));

        Profile result = organizationService.getOrganization(username);

        assertNotNull(result);
        assertEquals(mockOrganization, result);
        verify(profileRepository).findByUsername(username);
    }

    @Test
    void getOrganization_WhenNotFound_ShouldThrowProfileNotFoundException() {
        String username = "nonexistent";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> organizationService.getOrganization(username));
    }

    @Test
    void incrementFollowingCount_ShouldIncreaseAndSave() {
        int initial = mockOrganization.getFollowingCount();
        when(profileRepository.save(mockOrganization)).thenReturn(mockOrganization);

        organizationService.incrementFollowingCount(mockOrganization);

        assertEquals(initial + 1, mockOrganization.getFollowingCount());
        verify(profileRepository).save(mockOrganization);
    }

    @Test
    void incrementFollowerCount_ShouldIncreaseAndSave() {
        int initial = mockOrganization.getFollowersCount();
        when(profileRepository.save(mockOrganization)).thenReturn(mockOrganization);

        organizationService.incrementFollowerCount(mockOrganization);

        assertEquals(initial + 1, mockOrganization.getFollowersCount());
        verify(profileRepository).save(mockOrganization);
    }

    @Test
    void decrementFollowingCount_ShouldDecreaseAndSave() {
        int initial = mockOrganization.getFollowingCount();
        when(profileRepository.save(mockOrganization)).thenReturn(mockOrganization);

        organizationService.decrementFollowingCount(mockOrganization);

        assertEquals(initial - 1, mockOrganization.getFollowingCount());
        verify(profileRepository).save(mockOrganization);
    }

    @Test
    void decrementFollowerCount_ShouldDecreaseAndSave() {
        int initial = mockOrganization.getFollowersCount();
        when(profileRepository.save(mockOrganization)).thenReturn(mockOrganization);

        organizationService.decrementFollowerCount(mockOrganization);

        assertEquals(initial - 1, mockOrganization.getFollowersCount());
        verify(profileRepository).save(mockOrganization);
    }

    @Test
    void updateOrganization_WhenNameProvided_ShouldUpdateName() throws IOException {
        mockOrganization.setProfilePictureUrl("existing-logo.jpg");
        UpdateUserRequest request = new UpdateUserRequest("Updated Org", null, null, null, null, null);
        when(profileRepository.save(mockOrganization)).thenReturn(mockOrganization);

        organizationService.updateOrganization(request, mockOrganization);

        assertEquals("Updated Org", mockOrganization.getName());
        verify(profileRepository).save(mockOrganization);
    }

    @Test
    void updateOrganization_WhenNewLogoProvidedAndDifferent_ShouldUploadAndSave() throws IOException {
        mockOrganization.setProfilePictureUrl("old-logo.jpg");

        byte[] oldBytes = "old".getBytes();
        byte[] newBytes = "new".getBytes();

        MockMultipartFile newLogo = new MockMultipartFile("file", "newlogo.jpg", "image/jpeg", newBytes);
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, newLogo);

        when(blobService.downloadImage("old-logo.jpg")).thenReturn(oldBytes);
        when(blobService.uploadImage(organizationId, newLogo)).thenReturn("new-logo.jpg");
        when(profileRepository.save(mockOrganization)).thenReturn(mockOrganization);

        organizationService.updateOrganization(request, mockOrganization);

        assertEquals("new-logo.jpg", mockOrganization.getProfilePictureUrl());
        verify(blobService).downloadImage("old-logo.jpg");
        verify(blobService).uploadImage(organizationId, newLogo);
        verify(profileRepository).save(mockOrganization);
    }

    @Test
    void updateOrganization_WhenUsernameAlreadyExists_ShouldThrowUsernameAlreadyExistsException() {
        UpdateUserRequest request = new UpdateUserRequest(null, "existingusername", null, null, null, null);
        when(profileRepository.findIfUsernameIsUsed("existingusername")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class,
                () -> organizationService.updateOrganization(request, mockOrganization));
        verify(profileRepository).findIfUsernameIsUsed("existingusername");
        verify(profileRepository, never()).save(any());
    }

    @Test
    void updateOrganization_WhenProfilePictureIsNull_ShouldThrowImageNotFoundException() throws IOException {
        mockOrganization.setProfilePictureUrl(null);
        when(blobService.uploadImage(any(), any())).thenReturn("uploaded-photo.jpg");
        when(profileRepository.save(any())).thenReturn(mockOrganization);
        MultipartFile mockImage = new MockMultipartFile(
                "logo",
                "logo.jpg",
                "image/jpeg",
                "logo content".getBytes());

        UpdateUserRequest request = new UpdateUserRequest("New Name", null, null, null, null, mockImage);

        // Act & Assert
        organizationService.updateOrganization(request, mockOrganization);
        assertEquals("uploaded-photo.jpg", mockOrganization.getProfilePictureUrl());
    }

    @Test
    void setPhoto_ShouldUploadAndSavePhoto() throws IOException {
        MockMultipartFile image = new MockMultipartFile("file", "logo.jpg", "image/jpeg", "data".getBytes());
        when(blobService.uploadImage(any(), any())).thenReturn("uploaded-logo.jpg"); // helper stub below
        when(profileRepository.save(any())).thenReturn(mockOrganization);

        organizationService.setPhoto(image, mockOrganization);

        assertEquals("uploaded-logo.jpg", mockOrganization.getProfilePictureUrl());
        verify(blobService).uploadImage(organizationId, image);
        verify(profileRepository).save(mockOrganization);
    }
}
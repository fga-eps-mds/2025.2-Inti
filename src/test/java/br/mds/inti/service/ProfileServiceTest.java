package br.mds.inti.service;

import br.mds.inti.model.dto.PostResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.model.entity.Follow;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.repositories.FollowRepository;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exceptions.ProfileNotFoundException;
import br.mds.inti.service.exceptions.UsernameAlreadyExistsException;
import br.mds.inti.service.exceptions.ImageNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PostService postService;

    @Mock
    private BlobService blobService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProfileService profileService;

    private Profile mockProfile;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        profileId = UUID.randomUUID();
        mockProfile = new Profile();
        mockProfile.setId(profileId);
        mockProfile.setUsername("testuser");
        mockProfile.setName("Test User");
        mockProfile.setEmail("test@example.com");
        mockProfile.setPassword("password");
        mockProfile.setProfilePictureUrl("http://example.com/avatar.jpg");
        mockProfile.setBio("Test bio");
        mockProfile.setPublicEmail("public@example.com");
        mockProfile.setPhone("+5511999999999");
        mockProfile.setType(ProfileType.user);
        mockProfile.setFollowersCount(10);
        mockProfile.setFollowingCount(5);
        mockProfile.setCreatedAt(Instant.now());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfile_WhenAuthenticated_ShouldReturnProfileResponse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        // Cria PostResponse com o construtor CORRETO baseado no seu record
        UUID postId1 = UUID.randomUUID();
        UUID postId2 = UUID.randomUUID();

        Page<PostResponse> mockPosts = new PageImpl<>(List.of(
                new PostResponse(postId1, "http://example.com/img1.jpg", "Description 1", 10, "2024-01-01"),
                new PostResponse(postId2, "http://example.com/img2.jpg", "Description 2", 5, "2024-01-02")));

        when(postService.getPostByIdProfile(profileId, PageRequest.of(0, 10))).thenReturn(mockPosts);

        // CORREÇÃO: Mock do generateImageUrl para retornar o valor esperado
        when(postService.generateImageUrl("http://example.com/avatar.jpg"))
                .thenReturn("/images/http://example.com/avatar.jpg");

        // Act
        ProfileResponse result = profileService.getProfile(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(profileId, result.id());
        assertEquals(mockProfile.getName(), result.name());
        assertEquals(mockProfile.getUsername(), result.username());
        assertEquals(Boolean.FALSE, result.isFollowing());
        // CORREÇÃO: Agora espera a URL transformada
        assertEquals("/images/http://example.com/avatar.jpg", result.profile_picture_url());
        assertEquals(mockProfile.getBio(), result.bio());
        assertEquals(mockProfile.getFollowersCount(), result.followersCount());
        assertEquals(mockProfile.getFollowingCount(), result.followingCount());
        assertEquals(2, result.posts().size());
    }

    @Test
    void getProfile_WhenNotAuthenticated_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> profileService.getProfile(0, 10));
    }

    @Test
    void getProfileByUsername_WhenUserExists_ShouldReturnProfileResponse() {
        // Arrange
        String username = "testuser";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(mockProfile));

        Page<PostResponse> mockPosts = new PageImpl<>(List.of());
        when(postService.getPostByIdProfile(profileId, PageRequest.of(0, 10))).thenReturn(mockPosts);

        // Act
        ProfileResponse result = profileService.getProfileByUsername(username, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(profileId, result.id());
        assertEquals(mockProfile.getName(), result.name());
        assertEquals(mockProfile.getUsername(), result.username());
        assertEquals(Boolean.FALSE, result.isFollowing());
        verify(profileRepository).findByUsername(username);
    }

    @Test
    void getProfileByUsername_WhenAuthenticatedAndFollowing_ShouldSetIsFollowingTrue() {
        // Arrange
        String username = "testuser";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(mockProfile));

        Profile requester = new Profile();
        requester.setId(UUID.randomUUID());
        requester.setUsername("viewer");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(requester);
        SecurityContextHolder.setContext(securityContext);

        when(followRepository.findFollowRelationship(requester, mockProfile))
                .thenReturn(Optional.of(new Follow()));

        Page<PostResponse> mockPosts = new PageImpl<>(List.of());
        when(postService.getPostByIdProfile(profileId, PageRequest.of(0, 10))).thenReturn(mockPosts);

        // Act
        ProfileResponse result = profileService.getProfileByUsername(username, 0, 10);

        // Assert
        assertTrue(result.isFollowing());
        verify(followRepository).findFollowRelationship(requester, mockProfile);
    }

    @Test
    void getProfileByUsername_WhenUserNotFound_ShouldThrowProfileNotFoundException() {
        // Arrange
        String username = "nonexistent";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileNotFoundException.class, () -> profileService.getProfileByUsername(username, 0, 10));
    }

    @Test
    void getProfile_WhenUserExists_ShouldReturnProfile() {
        // Arrange
        String username = "testuser";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.of(mockProfile));

        // Act
        Profile result = profileService.getProfile(username);

        // Assert
        assertNotNull(result);
        assertEquals(mockProfile, result);
        verify(profileRepository).findByUsername(username);
    }

    @Test
    void getProfile_WhenUserNotFound_ShouldThrowProfileNotFoundException() {
        // Arrange
        String username = "nonexistent";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileNotFoundException.class, () -> profileService.getProfile(username));
    }

    @Test
    void incrementFollowingCount_ShouldIncreaseCountAndSave() {
        // Arrange
        int initialCount = mockProfile.getFollowingCount();
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.incrementFollowingCount(mockProfile);

        // Assert
        assertEquals(initialCount + 1, mockProfile.getFollowingCount());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void incrementFollowerCount_ShouldIncreaseCountAndSave() {
        // Arrange
        int initialCount = mockProfile.getFollowersCount();
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.incrementFollowerCount(mockProfile);

        // Assert
        assertEquals(initialCount + 1, mockProfile.getFollowersCount());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void decrementFollowingCount_ShouldDecreaseCountAndSave() {
        // Arrange
        int initialCount = mockProfile.getFollowingCount();
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.decrementFollowingCount(mockProfile);

        // Assert
        assertEquals(initialCount - 1, mockProfile.getFollowingCount());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void decrementFollowerCount_ShouldDecreaseCountAndSave() {
        // Arrange
        int initialCount = mockProfile.getFollowersCount();
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.decrementFollowerCount(mockProfile);

        // Assert
        assertEquals(initialCount - 1, mockProfile.getFollowersCount());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void updateUser_WhenNotAuthenticated_ShouldThrowUnauthorizedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest("New Name", "newusername", "+5511999999999",
                "public@test.com", "New bio", null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> profileService.updateUser(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void updateUser_WhenAuthenticationPrincipalIsNotProfile_ShouldThrowUnauthorizedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest("New Name", "newusername", "+5511999999999",
                "public@test.com", "New bio", null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> profileService.updateUser(request));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void updateUser_WhenNameProvided_ShouldUpdateName() throws IOException {
        // Arrange
        mockProfile.setProfilePictureUrl("existing-photo.jpg");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest("Updated Name", null, null, null, null, null);
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.updateUser(request);

        // Assert
        assertEquals("Updated Name", mockProfile.getName());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void updateUser_WhenBioProvided_ShouldUpdateBio() throws IOException {
        // Arrange
        mockProfile.setProfilePictureUrl("existing-photo.jpg");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, "Updated bio", null);
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.updateUser(request);

        // Assert
        assertEquals("Updated bio", mockProfile.getBio());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void updateUser_WhenPublicEmailProvided_ShouldUpdatePublicEmail() throws IOException {
        // Arrange
        mockProfile.setProfilePictureUrl("existing-photo.jpg");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest(null, null, null, "newemail@public.com", null, null);
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.updateUser(request);

        // Assert
        assertEquals("newemail@public.com", mockProfile.getPublicEmail());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void updateUser_WhenPhoneProvided_ShouldUpdatePhone() throws IOException {
        // Arrange
        mockProfile.setProfilePictureUrl("existing-photo.jpg");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest(null, null, "+5511888888888", null, null, null);
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.updateUser(request);

        // Assert
        assertEquals("+5511888888888", mockProfile.getPhone());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void updateUser_WhenUsernameProvided_AndNotUsed_ShouldUpdateUsername() throws IOException {
        // Arrange
        mockProfile.setProfilePictureUrl("existing-photo.jpg");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest(null, "newusername", null, null, null, null);
        when(profileRepository.findIfUsernameIsUsed("newusername")).thenReturn(false);
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.updateUser(request);

        // Assert
        assertEquals("newusername", mockProfile.getUsername());
        verify(profileRepository).findIfUsernameIsUsed("newusername");
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void updateUser_WhenUsernameAlreadyExists_ShouldThrowUsernameAlreadyExistsException() {
        // Arrange
        mockProfile.setProfilePictureUrl("existing-photo.jpg");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest(null, "existingusername", null, null, null, null);
        when(profileRepository.findIfUsernameIsUsed("existingusername")).thenReturn(true);

        // Act & Assert
        assertThrows(UsernameAlreadyExistsException.class, () -> profileService.updateUser(request));
        verify(profileRepository).findIfUsernameIsUsed("existingusername");
        verify(profileRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenProfilePictureIsNull_ShouldThrowImageNotFoundException() {
        // Arrange
        mockProfile.setProfilePictureUrl(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        UpdateUserRequest request = new UpdateUserRequest("New Name", null, null, null, null, null);

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> profileService.updateUser(request));
        verify(profileRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenNewProfilePictureProvided_AndDifferentFromExisting_ShouldUpdateProfilePicture()
            throws IOException {
        // Arrange
        mockProfile.setProfilePictureUrl("old-photo.jpg");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        byte[] existingImageBytes = "existing-image-data".getBytes();
        byte[] newImageBytes = "new-image-data".getBytes();

        MockMultipartFile newImage = new MockMultipartFile("file", "newphoto.jpg", "image/jpeg", newImageBytes);
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, newImage);

        when(blobService.downloadImage("old-photo.jpg")).thenReturn(existingImageBytes);
        when(blobService.uploadImage(profileId, newImage)).thenReturn("new-photo.jpg");
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.updateUser(request);

        // Assert
        assertEquals("new-photo.jpg", mockProfile.getProfilePictureUrl());
        verify(blobService).downloadImage("old-photo.jpg");
        verify(blobService).uploadImage(profileId, newImage);
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void updateUser_WhenNewProfilePictureSameAsExisting_ShouldNotUploadNewImage() throws IOException {
        // Arrange
        mockProfile.setProfilePictureUrl("existing-photo.jpg");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        byte[] imageBytes = "same-image-data".getBytes();

        MockMultipartFile sameImage = new MockMultipartFile("file", "photo.jpg", "image/jpeg", imageBytes);
        UpdateUserRequest request = new UpdateUserRequest(null, null, null, null, null, sameImage);

        when(blobService.downloadImage("existing-photo.jpg")).thenReturn(imageBytes);
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.updateUser(request);

        // Assert
        assertEquals("existing-photo.jpg", mockProfile.getProfilePictureUrl());
        verify(blobService).downloadImage("existing-photo.jpg");
        verify(blobService, never()).uploadImage(any(), any());
        verify(profileRepository).save(mockProfile);
    }

    @Test
    void setPhoto_WhenNotAuthenticated_ShouldThrowUnauthorizedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        MockMultipartFile image = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image-data".getBytes());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> profileService.setPhoto(image));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void setPhoto_WhenAuthenticationPrincipalIsNotProfile_ShouldThrowUnauthorizedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.setContext(securityContext);

        MockMultipartFile image = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image-data".getBytes());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> profileService.setPhoto(image));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void setPhoto_WhenAuthenticated_ShouldUploadAndSaveProfilePicture() throws IOException {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(mockProfile);
        SecurityContextHolder.setContext(securityContext);

        MockMultipartFile image = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "image-data".getBytes());

        when(blobService.uploadImage(profileId, image)).thenReturn("uploaded-photo.jpg");
        when(profileRepository.save(mockProfile)).thenReturn(mockProfile);

        // Act
        profileService.setPhoto(image);

        // Assert
        assertEquals("uploaded-photo.jpg", mockProfile.getProfilePictureUrl());
        verify(blobService).uploadImage(profileId, image);
        verify(profileRepository).save(mockProfile);
    }
}
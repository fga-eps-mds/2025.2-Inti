package br.mds.inti.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import br.mds.inti.model.dto.PostResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exceptions.ProfileNotFoundException;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PostService postService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProfileService profileService;

    private Profile testProfile;
    private List<PostResponse> postList;
    private Page<PostResponse> postPage;

    @BeforeEach
    void setUp() {
        testProfile = new Profile();
        testProfile.setId(UUID.randomUUID());
        testProfile.setName("Test User");
        testProfile.setUsername("testuser");
        testProfile.setProfilePictureUrl("http://example.com/pic.jpg");
        testProfile.setBio("Test bio");
        testProfile.setFollowersCount(10);
        testProfile.setFollowingCount(20);

        postList = new ArrayList<>();
        postPage = new PageImpl<>(postList);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getProfile_WhenAuthenticated_ReturnsProfileResponse() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(postService.getPostByIdProfile(any(UUID.class), any(PageRequest.class)))
                .thenReturn(postPage);

        ProfileResponse response = profileService.getProfile(0, 10);

        assertNotNull(response);
        assertEquals(testProfile.getName(), response.name());
        assertEquals(testProfile.getUsername(), response.username());
        assertEquals(testProfile.getProfilePictureUrl(), response.profile_picture_url());
        assertEquals(testProfile.getBio(), response.bio());
        assertEquals(testProfile.getFollowersCount(), response.followersCount());
        assertEquals(testProfile.getFollowingCount(), response.followingCount());
        assertEquals(postList, response.posts());

        verify(postService).getPostByIdProfile(testProfile.getId(), PageRequest.of(0, 10));
    }

    @Test
    void getProfile_WhenNotAuthenticated_ThrowsException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> profileService.getProfile(0, 10));
        assertEquals("profile nao autenticado", exception.getMessage());
    }

    @Test
    void getProfileByUsername_WhenProfileExists_ReturnsProfileResponse() {
        when(profileRepository.findByUsername(anyString())).thenReturn(Optional.of(testProfile));
        when(postService.getPostByIdProfile(any(UUID.class), any(PageRequest.class)))
                .thenReturn(postPage);

        ProfileResponse response = profileService.getProfileByUsername("testuser", 0, 10);

        assertNotNull(response);
        assertEquals(testProfile.getName(), response.name());
        assertEquals(testProfile.getUsername(), response.username());
        assertEquals(testProfile.getProfilePictureUrl(), response.profile_picture_url());
        assertEquals(testProfile.getBio(), response.bio());
        assertEquals(testProfile.getFollowersCount(), response.followersCount());
        assertEquals(testProfile.getFollowingCount(), response.followingCount());
        assertEquals(postList, response.posts());

        verify(profileRepository).findByUsername("testuser");
        verify(postService).getPostByIdProfile(testProfile.getId(), PageRequest.of(0, 10));
    }

    @Test
    void getProfileByUsername_WhenProfileNotFound_ThrowsProfileNotFoundException() {
        String username = "nonexistent";
        when(profileRepository.findByUsername(username)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ProfileNotFoundException.class,
                () -> profileService.getProfileByUsername(username, 0, 10));
        assertEquals(username, exception.getMessage());

        verify(profileRepository).findByUsername(username);
        verify(postService, never()).getPostByIdProfile(any(), any());
    }

    @Test
    void updateUser_SetOnlyName_UpdatesOnlyName() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            "Updated Name",  
            null,            
            null,            
            null             
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        String result = profileService.updateUser(updateRequest);

        assertEquals("profile updated", result);
        assertEquals("Updated Name", testProfile.getName());
        assertEquals("testuser", testProfile.getUsername()); 
        assertEquals("Test bio", testProfile.getBio()); 
        assertEquals("profile-pic-blob-name", testProfile.getProfilePictureUrl()); 
        
        verify(profileRepository).save(testProfile);
        verify(profileRepository, never()).findIfUsernameIsUsed(anyString());
        verify(blobService, never()).uploadImage(any(), any());
    }

    @Test
    void updateUser_SetOnlyUsername_UpdatesOnlyUsername() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            null,             
            null,             
            "newusername",    
            null              
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(profileRepository.findIfUsernameIsUsed("newusername")).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        String result = profileService.updateUser(updateRequest);

        assertEquals("profile updated", result);
        assertEquals("newusername", testProfile.getUsername());
        assertEquals("Test User", testProfile.getName()); 
        assertEquals("Test bio", testProfile.getBio()); 
        
        verify(profileRepository).findIfUsernameIsUsed("newusername");
        verify(profileRepository).save(testProfile);
    }

    @Test
    void updateUser_SetOnlyBio_UpdatesOnlyBio() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            null,                    
            "New amazing bio",       
            null,                    
            null
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        String result = profileService.updateUser(updateRequest);

        assertEquals("profile updated", result);
        assertEquals("New amazing bio", testProfile.getBio());
        assertEquals("Test User", testProfile.getName()); 
        assertEquals("testuser", testProfile.getUsername()); 
        
        verify(profileRepository).save(testProfile);
    }

    @Test
    void updateUser_SetOnlyProfilePicture_UpdatesOnlyProfilePicture() throws IOException {
        MockMultipartFile newPicture = new MockMultipartFile(
            "profilePicture",
            "new-profile.jpg",
            "image/jpeg",
            "new_image_content".getBytes()
        );
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            null,        
            null,        
            null,        
            newPicture   
        );
        
        byte[] existingImageBytes = "old_image_content".getBytes();
        byte[] newImageBytes = "new_image_content".getBytes();
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(blobService.downloadImage("profile-pic-blob-name")).thenReturn(existingImageBytes);
        when(blobService.uploadImage(testProfileId, newPicture)).thenReturn("new-blob-name");
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        String result = profileService.updateUser(updateRequest);

        assertEquals("profile updated", result);
        assertEquals("new-blob-name", testProfile.getProfilePictureUrl());
        assertEquals("Test User", testProfile.getName()); 
        assertEquals("testuser", testProfile.getUsername()); 
        assertEquals("Test bio", testProfile.getBio()); 
        
        verify(blobService).downloadImage("profile-pic-blob-name");
        verify(blobService).uploadImage(testProfileId, newPicture);
        verify(profileRepository).save(testProfile);
    }

    @Test
    void updateUser_SameProfilePicture_DoesNotUpdatePicture() throws IOException {
        byte[] sameImageBytes = "same_image_content".getBytes();
        
        MockMultipartFile samePicture = new MockMultipartFile(
            "profilePicture",
            "profile.jpg",
            "image/jpeg",
            sameImageBytes
        );
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            null,         
            null,         
            null,         
            samePicture   
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(blobService.downloadImage("profile-pic-blob-name")).thenReturn(sameImageBytes);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        String result = profileService.updateUser(updateRequest);

        assertEquals("profile updated", result);
        assertEquals("profile-pic-blob-name", testProfile.getProfilePictureUrl()); 
        
        verify(blobService).downloadImage("profile-pic-blob-name");
        verify(blobService, never()).uploadImage(any(), any()); 
        verify(profileRepository).save(testProfile);
    }

    @Test
    void updateUser_SetAllFields_UpdatesAllFields() throws IOException {
        MockMultipartFile newPicture = new MockMultipartFile(
            "profilePicture",
            "complete.jpg",
            "image/jpeg",
            "complete_image".getBytes()
        );
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            "Complete Name",     
            "Complete bio",      
            "completeuser",      
            newPicture           
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(profileRepository.findIfUsernameIsUsed("completeuser")).thenReturn(false);
        when(blobService.downloadImage("profile-pic-blob-name")).thenReturn("old".getBytes());
        when(blobService.uploadImage(testProfileId, newPicture)).thenReturn("complete-blob");
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        String result = profileService.updateUser(updateRequest);

        assertEquals("profile updated", result);
        assertEquals("Complete Name", testProfile.getName());
        assertEquals("completeuser", testProfile.getUsername());
        assertEquals("Complete bio", testProfile.getBio());
        assertEquals("complete-blob", testProfile.getProfilePictureUrl());
        
        verify(profileRepository).findIfUsernameIsUsed("completeuser");
        verify(blobService).uploadImage(testProfileId, newPicture);
        verify(profileRepository).save(testProfile);
    }

    @Test
    void updateUser_SetNoFields_DoesNotUpdate() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            null,  
            null,  
            null,  
            null   
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        String result = profileService.updateUser(updateRequest);

        assertEquals("profile updated", result);
        assertEquals("Test User", testProfile.getName());
        assertEquals("testuser", testProfile.getUsername());
        assertEquals("Test bio", testProfile.getBio());
        assertEquals("profile-pic-blob-name", testProfile.getProfilePictureUrl());
        
        verify(profileRepository).save(testProfile);
        verify(profileRepository, never()).findIfUsernameIsUsed(anyString());
        verify(blobService, never()).uploadImage(any(), any());
    }

    @Test
    void updateUser_UsernameAlreadyExists_ThrowsException() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            null,              
            null,              
            "existinguser",    
            null               
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(profileRepository.findIfUsernameIsUsed("existinguser")).thenReturn(true);

        UsernameAlreadyExistsException exception = assertThrows(
            UsernameAlreadyExistsException.class, 
            () -> profileService.updateUser(updateRequest)
        );
        
        assertEquals("Esse username já está sendo usado", exception.getMessage());
        verify(profileRepository).findIfUsernameIsUsed("existinguser");
        verify(profileRepository, never()).save(any(Profile.class)); 
    }

    @Test
    void updateUser_BlankStrings_DoesNotUpdate() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            "   ",   
            "",      
            "  ",    
            null     
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        String result = profileService.updateUser(updateRequest);

        assertEquals("profile updated", result);
        assertEquals("Test User", testProfile.getName());
        assertEquals("testuser", testProfile.getUsername());
        assertEquals("Test bio", testProfile.getBio());
        
        verify(profileRepository).save(testProfile);
        verify(profileRepository, never()).findIfUsernameIsUsed(anyString());
    }

    @Test
    void updateUser_NoAuthentication_ThrowsResponseStatusException() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            "Some Name",
            null,
            null,
            null
        );
        
        when(securityContext.getAuthentication()).thenReturn(null);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> profileService.updateUser(updateRequest)
        );
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("No authentication provided", exception.getReason());
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void updateUser_InvalidPrincipal_ThrowsResponseStatusException() throws IOException {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            "Some Name",
            null,
            null,
            null
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not_a_profile_object"); 

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> profileService.updateUser(updateRequest)
        );
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("No authentication provided", exception.getReason());
        verify(profileRepository, never()).save(any(Profile.class));
    }

    @Test
    void updateUser_ImageUploadFails_ThrowsIOException() throws IOException {
        MockMultipartFile newPicture = new MockMultipartFile(
            "profilePicture",
            "new.jpg",
            "image/jpeg",
            "new_content".getBytes()
        );
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            null,
            null,
            null,
            newPicture
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(blobService.downloadImage("profile-pic-blob-name")).thenReturn("old".getBytes());
        when(blobService.uploadImage(testProfileId, newPicture))
            .thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> profileService.updateUser(updateRequest));
        
        verify(blobService).uploadImage(testProfileId, newPicture);
        verify(profileRepository, never()).save(any(Profile.class)); 
    }

    @Test
    void updateUser_ImageDownloadFails_ThrowsIOException() throws IOException {
        MockMultipartFile newPicture = new MockMultipartFile(
            "profilePicture",
            "new.jpg",
            "image/jpeg",
            "new_content".getBytes()
        );
        
        UpdateUserRequest updateRequest = new UpdateUserRequest(
            null,
            null,
            null,
            newPicture
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testProfile);
        when(blobService.downloadImage("profile-pic-blob-name"))
            .thenThrow(new IOException("Download failed"));

        assertThrows(IOException.class, () -> profileService.updateUser(updateRequest));
        
        verify(blobService).downloadImage("profile-pic-blob-name");
        verify(blobService, never()).uploadImage(any(), any());
        verify(profileRepository, never()).save(any(Profile.class));
    }
}
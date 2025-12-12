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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@Service
public class OrganizationService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    BlobService blobService;

    public ProfileResponse getOrganization(int page, int size, Profile profile) {

        Page<PostResponse> post = postService.getPostByIdProfile(profile.getId(), PageRequest.of(page, size));
        long totalPosts = post.getTotalElements();

        return new ProfileResponse(profile.getId(), profile.getName(), profile.getUsername(), profile.getPublicEmail(), profile.getPhone(),
            postService.generateImageUrl(profile.getProfilePictureUrl()),
            profile.getBio(), profile.getFollowersCount(), profile.getFollowingCount(), totalPosts, Boolean.FALSE, post.getContent());
    }

    public ProfileResponse getOrganizationByUsername(String username, int page, int size) {
        Profile publicProfile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException(username));

        if(!publicProfile.getType().equals(ProfileType.organization)) throw new ProfileNotFoundException("O perfil de username " + username + " não é uma organização");

        Page<PostResponse> post = postService.getPostByIdProfile(publicProfile.getId(), PageRequest.of(page, size));
        long totalPosts = post.getTotalElements();

        Boolean isFollowing = Boolean.FALSE;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Profile) {
            Profile requester = (Profile) auth.getPrincipal();
            if (!requester.getId().equals(publicProfile.getId())) {
                isFollowing = followRepository.findFollowRelationship(requester, publicProfile).isPresent();
            }
        }

        return new ProfileResponse(publicProfile.getId(), publicProfile.getName(), publicProfile.getUsername(), publicProfile.getPublicEmail(),
            publicProfile.getPhone(),
            postService.generateImageUrl(publicProfile.getProfilePictureUrl()), publicProfile.getBio(),
            publicProfile.getFollowersCount(),
            publicProfile.getFollowingCount(), totalPosts, isFollowing, post.getContent());
    }

    public Profile getOrganization(String username) {

        return profileRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException(username));

    }

    public void incrementFollowingCount(Profile profile) {
        profile.setFollowingCount(profile.getFollowingCount() + 1);
        profileRepository.save(profile);
    }

    public void incrementFollowerCount(Profile profile) {
        profile.setFollowersCount(profile.getFollowersCount() + 1);
        profileRepository.save(profile);
    }

    public void decrementFollowingCount(Profile profile) {
        profile.setFollowingCount(profile.getFollowingCount() - 1);
        profileRepository.save(profile);
    }

    public void decrementFollowerCount(Profile profile) {
        profile.setFollowersCount(profile.getFollowersCount() - 1);
        profileRepository.save(profile);
    }

    public void updateOrganization(UpdateUserRequest updateUserRequest, Profile profile) throws IOException {

        if (updateUserRequest.name() != null && !updateUserRequest.name().isBlank()) {
            profile.setName(updateUserRequest.name());
        }
        if (updateUserRequest.userBio() != null && !updateUserRequest.userBio().isBlank()) {
            profile.setBio(updateUserRequest.userBio());
        }

        if (updateUserRequest.publicemail() != null && !updateUserRequest.publicemail().isBlank()) {
            profile.setPublicEmail(updateUserRequest.publicemail());
        }

        if (updateUserRequest.phone() != null && !updateUserRequest.phone().isBlank()) {
            profile.setPhone(updateUserRequest.phone());
        }

        if (updateUserRequest.username() != null && !updateUserRequest.username().isBlank()) {
            if (profileRepository.findIfUsernameIsUsed(updateUserRequest.username())) {
                throw new UsernameAlreadyExistsException("Esse username já está sendo usado");
            } else {
                profile.setUsername(updateUserRequest.username());
            }
        }

        String blobName;
        if (profile.getProfilePictureUrl() == null || profile.getProfilePictureUrl().isEmpty()) {
            blobName = blobService.uploadImage(profile.getId(), updateUserRequest.profilePicture());
            profile.setProfilePictureUrl(blobName);
        } else {
            if (updateUserRequest.profilePicture() != null && !updateUserRequest.profilePicture().isEmpty()) {
                byte[] existingProfilePicture = blobService.downloadImage(profile.getProfilePictureUrl());
                byte[] newProfilePicture = updateUserRequest.profilePicture().getBytes();

                if (!Arrays.equals(existingProfilePicture, newProfilePicture)) {
                    blobName = blobService.uploadImage(profile.getId(), updateUserRequest.profilePicture());
                    profile.setProfilePictureUrl(blobName);
                }
            }
        }

        profile.setUpdatedAt(Instant.now());

        profileRepository.save(profile);
    }

    public void setPhoto(MultipartFile img, Profile profile) throws IOException {

        String blobName = blobService.uploadImage(profile.getId(), img);
        profile.setProfilePictureUrl(blobName);
        profileRepository.save(profile);
    }
}

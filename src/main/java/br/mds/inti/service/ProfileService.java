package br.mds.inti.service;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.mds.inti.model.dto.PostResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.dto.UpdateUserRequest;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exceptions.UsernameAlreadyExistsException;
import jakarta.validation.constraints.Null;
import br.mds.inti.service.exceptions.ProfileNotFoundException;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PostService postService;

    @Autowired
    BlobService blobService;

    public ProfileResponse getProfile(int page, int size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Profile) {
            Profile profile = (Profile) auth.getPrincipal();

            Page<PostResponse> post = postService.getPostByIdProfile(profile.getId(), PageRequest.of(page, size));

            return new ProfileResponse(profile.getName(), profile.getUsername(), profile.getProfilePictureUrl(),
                    profile.getBio(), profile.getFollowersCount(), profile.getFollowingCount(), post.getContent());
        }
        throw new RuntimeException("profile nao autenticado");
    }

    public ProfileResponse getProfileByUsername(String username, int page, int size) {
        Profile publicProfile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException(username));

        Page<PostResponse> post = postService.getPostByIdProfile(publicProfile.getId(), PageRequest.of(page, size));

        return new ProfileResponse(publicProfile.getName(), publicProfile.getUsername(),
                publicProfile.getProfilePictureUrl(), publicProfile.getBio(), publicProfile.getFollowersCount(),
                publicProfile.getFollowingCount(), post.getContent());
    }

    public String updateUser(UpdateUserRequest updateUserRequest) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !(auth.getPrincipal() instanceof Profile)) 
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authentication provided");

        Profile profile = (Profile) auth.getPrincipal();

        if (updateUserRequest.name() != null && !updateUserRequest.name().isBlank()) {
            profile.setName(updateUserRequest.name());
        }
        if (updateUserRequest.userBio() != null && !updateUserRequest.userBio().isBlank()) {
            profile.setBio(updateUserRequest.userBio());
        }
        
        if (updateUserRequest.username() != null && !updateUserRequest.username().isBlank()) {
            if (profileRepository.findIfUsernameIsUsed(updateUserRequest.username())) {
                throw new UsernameAlreadyExistsException("Esse username já está sendo usado");
            } else {
                profile.setUsername(updateUserRequest.username());
            }
        }

        if (updateUserRequest.profilePicture() != null && !updateUserRequest.profilePicture().isEmpty()) {
            byte[] existingProfilePicture = blobService.downloadImage(profile.getProfilePictureUrl());
            byte[] newProfilePicture = updateUserRequest.profilePicture().getBytes();

            if (!Arrays.equals(existingProfilePicture, newProfilePicture)) {
                String blobName = blobService.uploadImage(profile.getId(), updateUserRequest.profilePicture());
                profile.setProfilePictureUrl(blobName);
            }
        }

        profileRepository.save(profile);
        return "profile updated";
    }

}

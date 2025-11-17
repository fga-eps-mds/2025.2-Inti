package br.mds.inti.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.mds.inti.model.dto.PostResponse;
import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exceptions.ProfileNotFoundException;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PostService postService;

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

}

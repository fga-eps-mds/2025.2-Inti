package br.mds.inti.service;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.mds.inti.model.dto.ProfileResponse;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exceptions.ProfileNotFoundException;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    public ProfileResponse getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Profile) {
            Profile profile = (Profile) auth.getPrincipal();

            return new ProfileResponse(profile.getName(), profile.getUsername(), profile.getProfilePictureUrl(),
                    profile.getBio(), profile.getFollowersCount(), profile.getFollowingCount());
        }
        throw new RuntimeCryptoException("user nao autenticado");
    }

    public ProfileResponse getProfileByUsername(String username) {
        Profile publicProfile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException(username));

        return new ProfileResponse(publicProfile.getName(), publicProfile.getUsername(),
                publicProfile.getProfilePictureUrl(), publicProfile.getBio(), publicProfile.getFollowersCount(),
                publicProfile.getFollowingCount());
    }

}

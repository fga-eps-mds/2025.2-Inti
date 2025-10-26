package br.mds.inti.service;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.mds.inti.model.Profile;
import br.mds.inti.model.dto.auth.ProfileResponse;

@Service
public class ProfileService {

    public ProfileResponse getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Profile) {
            Profile profile = (Profile) auth.getPrincipal();

            return new ProfileResponse(profile.getUsername(), profile.getUsername(), profile.getProfilePictureUrl(),
                    profile.getBio(), profile.getFollowersCount(), profile.getFollowingCount());
        }
        throw new RuntimeCryptoException("user nao autenticado");
    }

}

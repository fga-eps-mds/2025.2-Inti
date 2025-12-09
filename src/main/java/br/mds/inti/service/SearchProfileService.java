package br.mds.inti.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.mds.inti.model.dto.SearchProfile;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.ProfileRepository;
import br.mds.inti.service.exception.ProfileNotFoundException;

@Service
public class SearchProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    public SearchProfile getProfileByUsername(String username){
        Profile publicProfile = profileRepository.findByUsername(username)
            .orElseThrow(() -> new ProfileNotFoundException(username));
            
        return new SearchProfile(publicProfile.getId(), publicProfile.getName(), publicProfile.getUsername(), publicProfile.getProfilePictureUrl());
    }
}

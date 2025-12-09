package br.mds.inti.service;

import java.time.Instant;

import br.mds.inti.service.exception.ProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.mds.inti.model.dto.FollowResponse;
import br.mds.inti.model.entity.Follow;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.entity.pk.FollowsPK;
import br.mds.inti.repositories.FollowRepository;
import br.mds.inti.service.exception.FollowRelationshipAlredyExistException;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private ProfileService profileService;

    public FollowResponse followProfile(String username) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null ||  !(auth.getPrincipal() instanceof Profile me)) throw new RuntimeException("profile nao autenticado");

        Profile profileToFollow = profileService.getProfile(username);
        if(profileToFollow == null){
            throw new ProfileNotFoundException(username);
        }

        if(profileToFollow.getId().equals(me.getId())){
            throw new ResponseStatusException(BAD_REQUEST, "You cannot follow yourself");
        }

        var followExistOpt = followRepository.findFollowRelationship(me, profileToFollow);
        if (followExistOpt.isPresent()) {
            throw new FollowRelationshipAlredyExistException("Already exist");
        }

        // Create new follow relationship
        FollowsPK primaryKey = new FollowsPK();
        primaryKey.setFollowerProfileId(me.getId());
        primaryKey.setFollowingProfileId(profileToFollow.getId());

        Follow action = new Follow();
        action.setId(primaryKey);
        action.setCreatedAt(Instant.now());
        action.setFollowerProfile(me);
        action.setFollowingProfile(profileToFollow);

        followRepository.save(action);
        profileService.incrementFollowerCount(profileToFollow);
        profileService.incrementFollowingCount(me);

        String msg = "Perfil seguido com sucesso.";
        return new FollowResponse(msg);

    }

    public FollowResponse unfollowProfile(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null ||  !(auth.getPrincipal() instanceof Profile me)) throw new RuntimeException("profile nao autenticado");

        Profile profileToUnfollow = profileService.getProfile(username);

        if(profileToUnfollow.getId().equals(me.getId())){
            throw new ResponseStatusException(BAD_REQUEST, "You cannot unfollow yourself");
        }

        Follow followToRemove = followRepository
                .findFollowRelationship(me, profileToUnfollow)
                .orElseThrow(() -> new RuntimeException("Follow não encontrado"));

        profileService.decrementFollowingCount(me);
        profileService.decrementFollowerCount(profileToUnfollow);
        followRepository.delete(followToRemove);

        String msg = "Você deixou de seguir este perfil.";
        return new FollowResponse(msg);
    }
}

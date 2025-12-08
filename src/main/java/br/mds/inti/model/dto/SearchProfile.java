package br.mds.inti.model.dto;

import java.util.UUID;

public record SearchProfile(UUID profileId, String name, String username, 
        String profile_picture_url){
}

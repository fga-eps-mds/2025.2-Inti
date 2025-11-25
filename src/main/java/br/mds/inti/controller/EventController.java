package br.mds.inti.controller;

import br.mds.inti.model.dto.EventDetailResponse;
import br.mds.inti.model.dto.EventRequestDTO;
import br.mds.inti.model.dto.EventResponseDTO;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.model.enums.ProfileType;
import br.mds.inti.service.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    EventService eventService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<EventResponseDTO> createEvent(@ModelAttribute @Valid EventRequestDTO eventRequestDTO) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        if(profile.getType() != ProfileType.organization) 
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User type is not an organization");

        EventResponseDTO eventResponseDTO = eventService.createEvent(profile, eventRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventResponseDTO);
    }

    @GetMapping("/{eventid}")
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable UUID id) {
        EventDetailResponse eventDetails = eventService.getEventById(id);
        return ResponseEntity.ok(eventDetails);
    }
}
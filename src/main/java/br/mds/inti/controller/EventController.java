package br.mds.inti.controller;

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

@RestController // define como um controller
@RequestMapping("/event") // request ter como endpoint base "/events"
public class EventController {

    @Autowired // injeta automaticamente a dependência, inserir esse Service no contexto do Controller
    EventService eventService;

    //GET, POST, PATCH, DELETE
    @PostMapping(consumes = "multipart/form-data") // define como POST (criação de algo) que recebe multipart (pra receber imagem)
    public ResponseEntity<EventResponseDTO> createEvent(@ModelAttribute @Valid EventRequestDTO eventRequestDTO) throws IOException { // retorna um ResponseEntity (possui status code), que tem uma entidade que vai ser o CreateEvent

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Profile profile = (Profile) authentication.getPrincipal();

        if(profile.getType() != ProfileType.organization) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User type is not an organization");

        EventResponseDTO eventResponseDTO = eventService.createEvent(profile, eventRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventResponseDTO);
    }

}

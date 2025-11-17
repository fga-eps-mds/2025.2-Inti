package br.mds.inti.service;

import br.mds.inti.model.dto.EventRequestDTO;
import br.mds.inti.model.dto.EventResponseDTO;
import br.mds.inti.model.entity.Event;
import br.mds.inti.model.entity.Profile;
import br.mds.inti.repositories.EventRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;

@Service
public class EventService {

    @Autowired
    BlobService blobService;

    @Autowired
    EventRepository eventRepository;

    final String EVENTO_CRIADO = "Evento criado com sucesso";

    public CreateEventResponseDTO createEvent(@NotNull Profile profile, @NotNull EventRequestDTO eventRequestDTO) throws IOException {
        Event event = new Event();
        event.setTitle(eventRequestDTO.title());
        event.setEventTime(eventRequestDTO.eventTime());
        event.setDescription(eventRequestDTO.description());

        String blobName = null;
        if(eventRequestDTO.image() != null) {
            blobName = blobService.uploadImageWithDescription(profile.getId(), eventRequestDTO.image());
        }

        event.setBlobName(blobName);
        event.setStreetAddress(eventRequestDTO.streetAddress());
        event.setAdministrativeRegion(eventRequestDTO.administrativeRegion());
        event.setCity(eventRequestDTO.city());
        event.setState(eventRequestDTO.state());
        event.setReferencePoint(eventRequestDTO.referencePoint());
        event.setLatitude(eventRequestDTO.latitude());
        event.setLongitude(eventRequestDTO.longitude());
        event.setCreatedAt(Instant.now());

        eventRepository.save(event);

        EventResponseDTO eventResponseDTO = new EventResponseDTO(event.getId(), EVENTO_CRIADO);
        return eventResponseDTO;
    }

}

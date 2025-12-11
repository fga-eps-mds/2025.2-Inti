package br.mds.inti.model.dto;

import java.util.List;

public record EventFollowingAttendeesDTO(
        List<FollowingAttendeeDTO> attendees
) {
}

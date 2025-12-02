package br.mds.inti.service.exceptions;

public class EventParticipantAlreadyExistsException extends RuntimeException{
    public EventParticipantAlreadyExistsException(String msg) {
        super(msg);
    }
}

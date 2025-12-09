package br.mds.inti.service.exception;

public class EventParticipantAlreadyExistsException extends RuntimeException{
    public EventParticipantAlreadyExistsException(String msg) {
        super(msg);
    }
}

package br.mds.inti.service.exceptions;

public class ProfileAlreadyExistsException extends RuntimeException {
    public ProfileAlreadyExistsException(String msg) {
        super(msg);
    }
}

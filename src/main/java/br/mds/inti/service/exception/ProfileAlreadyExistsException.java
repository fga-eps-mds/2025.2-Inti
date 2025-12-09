package br.mds.inti.service.exception;

public class ProfileAlreadyExistsException extends RuntimeException {
    public ProfileAlreadyExistsException(String msg) {
        super(msg);
    }
}

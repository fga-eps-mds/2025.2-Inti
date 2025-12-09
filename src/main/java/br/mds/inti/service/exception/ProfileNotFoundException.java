package br.mds.inti.service.exception;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String msg) {
        super(msg);
    }
}

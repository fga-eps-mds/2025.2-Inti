package br.mds.inti.service.exceptions;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String msg) {
        super(msg);
    }
}

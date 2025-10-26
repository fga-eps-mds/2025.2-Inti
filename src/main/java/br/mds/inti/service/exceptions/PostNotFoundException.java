package br.mds.inti.service.exceptions;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Object id) {
        super("id: " + id);
    }
}

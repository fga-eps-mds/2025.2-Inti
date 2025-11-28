package br.mds.inti.service.exceptions;

public class FollowRelationshipAlredyExistException extends RuntimeException {
    public FollowRelationshipAlredyExistException(Object obj) {
        super("" + obj);
    }

}

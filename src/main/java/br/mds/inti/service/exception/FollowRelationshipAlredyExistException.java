package br.mds.inti.service.exception;

public class FollowRelationshipAlredyExistException extends RuntimeException {
    public FollowRelationshipAlredyExistException(Object obj) {
        super("" + obj);
    }

}

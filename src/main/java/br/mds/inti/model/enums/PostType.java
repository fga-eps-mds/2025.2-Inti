package br.mds.inti.model.enums;

public enum PostType {
    FOLLOWED, // posts de quem voce segue diretamente
    ORGANIZATION, // posts de organizações
    SECOND_DEGREE, // posts de quem seus seguidores seguem
    POPULAR, // posts populares
    RANDOM // posts aleatorios
}

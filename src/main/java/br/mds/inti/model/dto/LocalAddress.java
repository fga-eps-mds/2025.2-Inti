package br.mds.inti.model.dto;

public record LocalAddress(
    String streetAddress,
    String administrativeRegion,
    String city,
    String state,
    String referencePoint
) {
}

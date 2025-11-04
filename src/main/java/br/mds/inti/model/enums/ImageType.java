package br.mds.inti.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ImageType {
    FILE("file"),
    URL("url"),
    BASE64("base64"),
    RAW("raw");

    String description;
}

package br.mds.inti.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum ImageType {
    FILE("file"),
    URL("url"),
    BASE64("base64"),
    RAW("raw");

    String description;
}

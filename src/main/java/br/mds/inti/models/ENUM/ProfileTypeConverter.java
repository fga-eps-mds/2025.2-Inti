package br.mds.inti.models.ENUM;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true) // aplica automaticamente a todos os campos ProfileType
public class ProfileTypeConverter implements AttributeConverter<ProfileType, String> {

    @Override
    public String convertToDatabaseColumn(ProfileType attribute) {
        if (attribute == null)
            return null;

        return attribute.name().toLowerCase();
    }

    @Override
    public ProfileType convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        return ProfileType.valueOf(dbData.toUpperCase());
    }
}
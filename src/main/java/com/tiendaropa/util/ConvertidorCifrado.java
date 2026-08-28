package com.tiendaropa.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Cifra y descifra automaticamente los campos marcados con
 * @Convert(converter = ConvertidorCifrado.class). En la base de datos
 * el valor queda guardado como "enc:BASE64".
 */
@Converter
public class ConvertidorCifrado implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String valor) {
        return CifradoAes.cifrar(valor);
    }

    @Override
    public String convertToEntityAttribute(String valor) {
        return CifradoAes.descifrar(valor);
    }
}

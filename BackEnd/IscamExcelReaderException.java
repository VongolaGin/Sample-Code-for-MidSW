package com.logismic.iscam.web.rest.errors;

import java.io.IOException;
import java.text.MessageFormat;

public class IscamExcelReaderException extends Exception {

    public static final String INVALID_COLUMNS_NUMBER ="El archivo debe contener exactamente las {0} columnas requeridas.";
    public static final String INVALID_HEADER = "El nombre de la columna {0} debe ser \"{1}\".";
    public static final String INVALID_FORMAT = "El formato del archivo es inválido";
    public static final String REQUIRED_VALUE = "En la fila {0} el campo {1} es requerido, pero se encuentra vacío";
    public static final String UNEXPECTED_VALUE = "Ha ocurrido un error al intentar leer el valor en la fila {0} campo {1}";;
    public static final String UNMATCH_VALUE = "En la fila {0} el campo {1} no concuerda con el valor esperado {2}";
    public static final String EMPTY_FILE = "El archivo {0} está vacío";
    public static final String FILES_NOT_PROVIDED = "No se ha detectado ningún archivo trabajado.";
    public static final String FILE_NOT_FOUND = "Archivo no encontrado.";
    public static final String CORRUPT_ROW = "La fila {0} del archivo {1} está corrupta.";
    public static final String LESS_COLUMNS = "La fila {0} del archivo {1} está corrupta.";
    public static final String MORE_COLUMNS = "La fila {0} del archivo {1} está corrupta.";


    public IscamExcelReaderException(String message, Object... params) {
        super(MessageFormat.format(message, params));
    }
}

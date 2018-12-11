package com.logismic.iscam.service;

import com.logismic.iscam.service.util.ArchivoTrabajadoColumnsEnum;
import com.logismic.iscam.service.util.Xlsx2CsvDataFile;
import com.logismic.iscam.web.rest.errors.IscamExcelReaderException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ArchivoTrabajadoReaderService extends Xlsx2CsvDataFile {

    private ArchivoTrabajadoColumnsEnum[] columnsEnum;

    public ArchivoTrabajadoReaderService() {
        super();
        super.headers = Arrays.stream(ArchivoTrabajadoColumnsEnum.values()).map(
            ArchivoTrabajadoColumnsEnum::getName
        ).toArray(String[]::new);
        columnsEnum = ArchivoTrabajadoColumnsEnum.values();
    }

    @Override
    protected String validateData(int col, String formattedValue) {
        ArchivoTrabajadoColumnsEnum columnEnum = columnsEnum[col];

        if (null == formattedValue)
            formattedValue = "";

        //Validamos vacío cuando es requerido.
        if ("" == formattedValue && columnEnum.getAllowBlank()) {
            return formattedValue;
        } else if ("" == formattedValue) {
            catchException(new IscamExcelReaderException(IscamExcelReaderException.REQUIRED_VALUE, super.currentRow, columnEnum.getName()));
            return null;
        }

        //Validamos el tipo del dato
        formattedValue = validateDataType(formattedValue, columnEnum.getTclass());
        if (null == formattedValue) {
            catchException(new IscamExcelReaderException(IscamExcelReaderException.UNEXPECTED_VALUE, currentRow, columnEnum.getName()));
            return null;
        }

        //Validamos que el informante sea el mismo que es proporcionado
        if (columnEnum == ArchivoTrabajadoColumnsEnum.FKInformante
            && !formattedValue.equals(claveIscam)) {
            catchException(new IscamExcelReaderException(IscamExcelReaderException.UNMATCH_VALUE, currentRow, columnEnum.getName(), claveIscam));
            return null;
        }

        if (columnEnum == ArchivoTrabajadoColumnsEnum.Periodo
            && !formattedValue.equals(periodoAcronimo)) {
            catchException(new IscamExcelReaderException(IscamExcelReaderException.UNMATCH_VALUE, currentRow, columnEnum.getName(), periodoAcronimo));
            return null;
        }

        return formattedValue;
    }


}

package com.logismic.iscam.service.util;

public enum ArchivoTrabajadoColumnsEnum implements ExcelColumnsEnum {
    FKInformante(0, "FKInformante", String.class, false),
    Periodo(1, "Periodo", String.class, false),
    ProductoInformante_Codigo(2,"ProductoInformante_Codigo", String.class),
    ProductoInformante_Descripcion(3,"ProductoInformante_Descripcion", String.class),
    Producto_Display(4,"Producto_Display", Float.class),
    Producto_PiezasDisplay(5,"Producto_PiezasDisplay", Float.class),
    Producto_Medida(6,"Producto_Medida", Float.class),
    Producto_Prefijo(7,"Producto_Prefijo", String.class),
    Cajas(8,"Cajas", Float.class),
    Piezas(9, "Piezas", Float.class),
    CajasInv(10,"CajasInv", Float.class),
    PiezasInv(11,"PiezasInv", Float.class),
    VentaCajas(12,"VentaCajas", Float.class),
    VentaPiezas(13,"VentaPiezas", Float.class),
    ValorInvCajas(14,"ValorInvCajas", Float.class),
    ValorInvPiezas(15, "ValorInvPiezas", Float.class),
    Producto_PrecioUnitario(16,"Producto_PrecioUnitario", Float.class),
    Sucursal_Clave(17,"Sucursal_Clave", String.class),
    Sucursal_Descripcion(18,"Sucursal_Descripcion", String.class),
    Sucursal_Canal(19,"Sucursal_Canal", String.class),
    Atributo01(20,"Atributo01", String.class),
    Atributo02(21,"Atributo02", String.class),
    Atributo03(22,"Atributo03", String.class),
    Atributo04(23,"Atributo04", String.class),
    Atributo05(24,"Atributo05", String.class),
    Atributo06(25,"Atributo06", String.class),
    Atributo07(26,"Atributo07", String.class),
    Atributo08(27,"Atributo08", String.class),
    Atributo09(28,"Atributo09", String.class),
    Atributo10(29,"Atributo10", String.class);

    private int position;
    private String name;
    private Class tclass;
    private Boolean allowBlank;
    ArchivoTrabajadoColumnsEnum(int columnPosition, String name, Class tclass) {
        this.tclass = tclass;
        this.position = columnPosition;
        this.name = name;
        this.allowBlank = true;
    }

    ArchivoTrabajadoColumnsEnum(int columnPosition, String name, Class tclass, Boolean allowBlank) {
        this.tclass = tclass;
        this.position = columnPosition;
        this.name = name;
        this.allowBlank = allowBlank;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public Class getTclass() {
        return tclass;
    }

    public Boolean getAllowBlank() {
        return allowBlank;
    }

    @Override
    public ExcelColumnsEnum[] getValues() {
        return ArchivoTrabajadoColumnsEnum.values();
    }
}

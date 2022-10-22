package pt.up.fe.comp;


import pt.up.fe.specs.util.SpecsEnums;

public enum EntityTypeAuxiliar {
    CLASS("class"),
    GLOBAL("global"),
    IMPORT("import"),
    METHOD("method"),
    EXTENDS("extends"),
    ARG("arg"),
    VARIABLE("var");


    private final String code;


    EntityTypeAuxiliar(String code){
        this.code = code;
    }

    @Override
    public String toString(){
        return this.code;
    }

    public EntityTypeAuxiliar fromName(String name){
        return SpecsEnums.fromName(EntityTypeAuxiliar.class, name);
    }
}

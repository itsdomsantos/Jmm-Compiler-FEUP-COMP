package pt.up.fe.comp;


import pt.up.fe.specs.util.SpecsEnums;

public enum TypeAuxiliar  {
    BOOLEAN("boolean", false),
    STRING("String", false),
    INTARRAY("int", true),
    INT("int", false),
    VOID("void", false),
    NONE("none", false),
    INVALID("invalid", false),
    CUSTOM("custom", false);


    private final String code;
    private final boolean isArray;


    TypeAuxiliar(String code, boolean isArray){
        this.code = code;
        this.isArray = isArray;
    }

    @Override
    public String toString(){
        return this.code;
    }

    public boolean getIsArrayType(){
        return this.isArray;
    }

    public static TypeAuxiliar getType(String TypeAST){
        if(TypeAST == "_Int")
            return TypeAuxiliar.INT;
        if(TypeAST == "_IntArray")
            return TypeAuxiliar.INTARRAY;
        if(TypeAST == "_Bool")
            return TypeAuxiliar.BOOLEAN;
        return TypeAuxiliar.CUSTOM;
    }

    public TypeAuxiliar fromName(String name){
        return SpecsEnums.fromName(TypeAuxiliar.class, name);
    }
}

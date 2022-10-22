package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;


public class OllirUtils {

    public static String getCode(Symbol symbol){
        return symbol.getName() + "." + getCode(symbol.getType());
    }

    public static String getName(Symbol symbol){
        return symbol.getName();
    }

    public static String getType(Symbol symbol) { return getCode(symbol.getType()); }
    public static String getCode(Type type){
        StringBuilder code = new StringBuilder();

        if(type.isArray()) code.append("array.");

        code.append(getType(type.getName()));

        return code.toString();
    }

    public static String getType(String type){
         switch(type) {
             case "void":
                 return "V";
             case "string":
                 return "String";
             case "int":
                 return "i32";
             case " Bool":
                 return "bool";
             default:
                 return type;
         }
    }

    //TODO
    public static String getExprType(JmmNode exprStmt) {
        return null;
    }
}

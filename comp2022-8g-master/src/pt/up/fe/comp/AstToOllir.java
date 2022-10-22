package pt.up.fe.comp;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.stream.Collectors;

public class AstToOllir extends AJmmVisitor<Boolean,Boolean> {
    private StringBuilder code;

    public AstToOllir(){
        code = new StringBuilder();
        addVisit("Start", this::visitStart);
        addVisit("ImportDeclaration", this::visitImportDeclaration);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
    }

    public Boolean visitStart(JmmNode start, Boolean dummy){
        System.out.println(start.getKind());
        for( var child: start.getJmmChild(0).getChildren()){
            visit(child);
        }
        return true;
    }

    public Boolean visitImportDeclaration(JmmNode importDecl, Boolean dummy){
        System.out.println(importDecl.getKind());
        var importCode = importDecl.getChildren().stream().map(id -> id.get("importName").strip()).collect(Collectors.joining("."));
        code.append("import ").append(importCode).append(";\n");
        System.out.println(importCode);
        return true;
    }


    public Boolean visitClassDeclaration(JmmNode classDecl, Boolean dummy){
        System.out.println(classDecl.getKind());
        var classCode = classDecl.get("classDecl").strip();
        code.append(classCode).append(" {\n");
        code.append(".construct ").append(classCode).append("().V {\n");
        code.append("invokespecial(this,\"<init>\").V;\n");
        code.append("}\n");
        code.append("}");

        System.out.println(classCode);
        return true;
    }


    public String getCode() {
        return code.toString();
    }
}

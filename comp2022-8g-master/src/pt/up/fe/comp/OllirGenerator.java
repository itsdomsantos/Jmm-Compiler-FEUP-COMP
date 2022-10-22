package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Code,Code> {
    private StringBuilder code;
    private SymbolTable symbolTable;
    private int tmp;

    public OllirGenerator(SymbolTable symbolTable) {
        this.code = new StringBuilder();
        this.symbolTable = symbolTable;
        this.tmp = 0;

        addVisit("Start", this::visitStart);
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("MethodDeclaration", this::visitMethodDeclaration);
        addVisit("Expr", this::visitExprStmtDeclaration);
        addVisit("_AssignmentStatement", this::visitAssignStmtDeclaration);
        addVisit("IntegerLiteral", this::visitIntegerLiteral);
        addVisit("_Identifier", this::visitIdentifier);
        addVisit("function_call", this::visitFunctionCall);
        addVisit("Return", this::visitReturn);
        addVisit("Value", this::visitValue);
        addVisit("VarDecl", this::visitVarDeclaration);
        addVisit("_NewObject", this::visitNewObject);
        addVisit("_NewArray", this::visitNewArray);
        addVisit("ArrayIndex", this::visitArrayIndex);
        addVisit("BinOpBoolLessThan", this::visitBinOpBool);
        addVisit("BinOpBoolAnd", this::visitBinOpBool);
        addVisit("BinOpInt", this::visitBinOpInt);
        addVisit("_True", this::visitTrue);
        addVisit("_False", this::visitFalse);
        addVisit("OpenCloseBrac", this::visitOpenCloseBrac);
        addVisit("Dot_length", this::visitDotLength);
    }

    private Code visitDotLength(JmmNode jmmNode, Code tmpCode) {
        var child = jmmNode.getJmmParent().getJmmChild(0);
        JmmNode tmpNode = jmmNode.getJmmParent();
        String name = new String();
        String type = new String();
        while (!tmpNode.getKind().equals("MethodDeclaration")) {
            tmpNode = tmpNode.getJmmParent();
        }
        if (symbolTable.getLocalVariables(tmpNode.get("methodName")).size() > 0) {
            for (int i = 0; i < symbolTable.getLocalVariables(tmpNode.get("methodName")).size(); i++) {
                if (symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i).getName().equals(child.get("name"))) {
                    name = OllirUtils.getName(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i));
                    type = OllirUtils.getType(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i));
                }
            }
        }

        if (symbolTable.getParameters(tmpNode.get("methodName")).size() > 0) {
            for (int i = 0; i < symbolTable.getParameters(tmpNode.get("methodName")).size(); i++) {
                if (symbolTable.getParameters(tmpNode.get("methodName")).get(i).getName().equals(child.get("name"))) {
                    name = OllirUtils.getName(symbolTable.getParameters(tmpNode.get("methodName")).get(i));
                    type = OllirUtils.getType(symbolTable.getParameters(tmpNode.get("methodName")).get(i));
                }
            }
        }

        code.append("arraylength(").append(name).append(".").append(type).append(")").append(".").append(type);
        return tmpCode;
    }

    private Code visitOpenCloseBrac(JmmNode openCloseBrac, Code tmpCode) {
        code.append("[");
        visit(openCloseBrac.getJmmChild(0));
        return tmpCode;
    }

    private Code visitFalse(JmmNode jmmNode, Code tmpCode) {
        code.append("false.bool");
        return tmpCode;
    }

    private Code visitTrue(JmmNode jmmNode, Code tmpCode) {
        code.append("true.bool");
        return tmpCode;
    }

    private Code visitBinOpInt(JmmNode binOpInt, Code tmpCode) {

        /*
        var leftOperatorBinOpInt = binOpInt.getJmmChild(0).getKind().equals("BinOpInt");
        var rightOperatorBinOpInt = binOpInt.getJmmChild(1).getKind().equals("BinOpInt");

        if(leftOperatorBinOpInt || rightOperatorBinOpInt ) {
            var tmpVar = "temp" + tmp;
            tmpCode.prefix.append(tmpVar).append(".i32 := ");
            tmp++;
            if(leftOperatorBinOpInt) {
                visit(binOpInt.getJmmChild(0).getJmmChild(0));
                tmpCode.prefix.append(" ").append(binOpInt.getJmmChild(0).get("op")).append(" ");
                visit(binOpInt.getJmmChild(0).getJmmChild(1));
            }
            if(rightOperatorBinOpInt) {
                visit(binOpInt.getJmmChild(1).getJmmChild(0));
                code.append(" ").append(binOpInt.getJmmChild(1).get("op")).append(" ");
                visit(binOpInt.getJmmChild(1).getJmmChild(1));
            }
        }
        */



        if(binOpInt.getJmmChild(0).getJmmChild(0).getKind().equals("IntegerLiteral")) {
            code.append(binOpInt.getJmmChild(0).getJmmChild(0).get("int")).append(".i32");
        }
        else{
            visit(binOpInt.getJmmChild(0));
        }
        code.append(" ");
        code.append(binOpInt.get("op"));
        code.append(".i32");
        code.append(" ");
        if(binOpInt.getJmmChild(1).getJmmChild(0).getKind().equals("IntegerLiteral")) {
            code.append(binOpInt.getJmmChild(1).getJmmChild(0).get("int")).append(".i32");
        }
        else {
            visit(binOpInt.getJmmChild(1));
        }
        return tmpCode;
    }

    private Code visitBinOpBool(JmmNode binOpBool, Code tmpCode) {
        JmmNode tmpNode = binOpBool.getJmmParent();
        String name = new String();
        String type = new String();
        while(!tmpNode.getKind().equals("MethodDeclaration")){
            tmpNode = tmpNode.getJmmParent();
        }
        if(symbolTable.getLocalVariables(tmpNode.get("methodName")).size() > 0) {
            for(int i=0; i < symbolTable.getLocalVariables(tmpNode.get("methodName")).size(); i++){
                if(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i).getName().equals(binOpBool.getJmmParent().getJmmChild(0).getJmmChild(0).get("name"))){
                    name = OllirUtils.getCode(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i));
                    type = OllirUtils.getType(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i));
                }
            }
        }
        if(symbolTable.getParameters(tmpNode.get("methodName")).size() > 0) {
            for(int i=0; i < symbolTable.getParameters(tmpNode.get("methodName")).size(); i++){
                if(symbolTable.getParameters(tmpNode.get("methodName")).get(i).getName().equals(binOpBool.getJmmParent().getJmmChild(0).getJmmChild(0).get("name"))){
                    name = OllirUtils.getName(symbolTable.getParameters(tmpNode.get("methodName")).get(i));
                    type = OllirUtils.getType(symbolTable.getParameters(tmpNode.get("methodName")).get(i));
                }
            }
        }


        if(binOpBool.getJmmChild(0).getJmmChild(0).getKind().equals("IntegerLiteral")){
            code.append(binOpBool.getJmmChild(0).getJmmChild(0).get("int")).append(".i32");
        }
        else {
            visit(binOpBool.getJmmChild(0));
        }
        code.append(" ");
        code.append(binOpBool.get("op"));
        if(binOpBool.getJmmChild(0).getJmmChild(0).getKind().equals("IntegerLiteral")){
            code.append(".i32");
        }
        else{
            code.append(".").append(type);
        }
        code.append(" ");
        if(binOpBool.getJmmChild(1).getJmmChild(0).getKind().equals("IntegerLiteral")){
            code.append(binOpBool.getJmmChild(1).getJmmChild(0).get("int")).append(".i32");
        }
        else {
            visit(binOpBool.getJmmChild(1));
        }
        return tmpCode;
    }

    public Code visitStart(JmmNode start, Code tmpCode) {
        for (var importString : symbolTable.getImports()) {
            code.append("import ").append(importString).append(";\n");
        }

        for (var child : start.getJmmChild(0).getChildren()) {
            visit(child);
        }
        return tmpCode;
    }

    public Code visitClassDeclaration(JmmNode classDecl, Code tmpCode) {
        code.append(symbolTable.getClassName());
        var superClass = symbolTable.getSuper();
        if (superClass != null) {
            code.append(" extends ").append(superClass);
        }
        code.append(" {\n");

        //fields
        for(var field : symbolTable.getFields()){
            code.append(".field private ").append(field.getName()).append(".").append(OllirUtils.getCode(field.getType())).append(";\n");
        }

        //construct
        code.append(".construct ").append(symbolTable.getClassName()).append("().V {\n");
        code.append("invokespecial(this,\"<init>\").V;\n");
        code.append("}\n");


        for (var child: classDecl.getChildren()) {
            visit(child);
        }

        code.append("}\n");

        return tmpCode;
    }

    public Code visitMethodDeclaration(JmmNode methodDecl, Code tmpCode) {
        var methodName = methodDecl.get("methodName");
        var isStatic = false;
        if(methodDecl.get("static").equals("true")) {
            isStatic = true;
        }

        var isArray = false;
        if(methodDecl.getJmmChild(0).get("isArray").equals("true")) {
            isArray = true;
        }

        code.append(".method public ");
        if(isStatic) code.append("static ");
        code.append(methodName + "(");

        var params = symbolTable.getParameters(methodName);
        var paramCode = params.stream()
                .map(symbol -> OllirUtils.getCode(symbol))
                .collect(Collectors.joining(", "));

        code.append(paramCode);

        code.append(").");
        if(isArray){
            code.append("array.");
        }
        var returnType = symbolTable.getReturnType(methodName).getName();
        code.append(OllirUtils.getType(returnType));

        code.append(" {\n");

        var lastParamIndex = -1;

        for(int i=1; i < methodDecl.getNumChildren(); i++){
            if(methodDecl.getJmmChild(i).getKind().equals("Param")) {
                lastParamIndex = i;
            }
        }

        var statements = methodDecl.getChildren().subList(lastParamIndex + 1, methodDecl.getNumChildren());
        for (var statement : statements) {
            if(statement.getKind().equals("Statement")) {
                visit(statement.getJmmChild(0));
            }
        }

        if(methodDecl.getJmmChild(methodDecl.getNumChildren()-1).getKind().equals("Return"))
        {
            visit(methodDecl.getJmmChild(methodDecl.getNumChildren()-1));
        }
        else {
            code.append("ret.V;\n");
        }

        code.append("}\n");

        return tmpCode;
    }

    public Code visitExprStmtDeclaration(JmmNode exprStmt, Code tmpCode) {
        String tmpVar = "tmp" + tmp;
        if(exprStmt.getJmmParent().getKind().equals("_AssignmentStatement")){
            visit(exprStmt.getJmmChild(0));
            code.append(";\n");
        }
        else {
            var flag = 0;
            var functionName = exprStmt.getJmmChild(0).getJmmChild(1).get("name");
            JmmNode tmpNode = exprStmt.getJmmParent();
            while (!tmpNode.getKind().equals("ClassDeclaration")) {
                tmpNode = tmpNode.getJmmParent();
            }
            for (var child : tmpNode.getChildren()) {
                if(child.getKind().equals("MethodDeclaration")) {
                    if (child.get("methodName").equals(functionName) && child.get("static").equals(false)) {
                        flag = 1;
                    }
                }
            }

            for (var importString : symbolTable.getImports()) {
                if (exprStmt.getJmmChild(0).getJmmChild(0).get("name").equals(importString)) {
                    flag = 1;
                }
            }

            if (flag == 1) {
                code.append("invokestatic(");
                code.append(exprStmt.getJmmChild(0).getJmmChild(0).get("name"));
            } else {
                code.append("invokevirtual(");
                visit(exprStmt.getJmmChild(0).getJmmChild(0));
            }
            code.append(", ");
            code.append("\"");
            visit(exprStmt.getJmmChild(0).getJmmChild(1));
            code.append("\"");

            for (var child : exprStmt.getJmmChild(0).getJmmChild(0).getChildren()) {
                visit(child);
            }

            for (var child : exprStmt.getJmmChild(0).getJmmChild(1).getChildren()) {
                code.append(", ");
                if (child.getJmmChild(0).getKind().equals("IntegerLiteral")) {
                    code.append(child.getJmmChild(0).get("int")).append(".i32");
                } else {
                    visit(child);
                }
            }
        }
        code.append(").V;\n");
        return tmpCode;
    }

    private Code visitAssignStmtDeclaration(JmmNode assignStmt, Code tmpCode) {
        JmmNode tmpNode = assignStmt.getJmmParent();
        String name = new String();
        String type = new String();
        int flag = 0;


        while(!tmpNode.getKind().equals("ClassDeclaration")){
            tmpNode = tmpNode.getJmmParent();
        }
        if(symbolTable.getFields().size() > 0) {
            for(int i=0; i < symbolTable.getFields().size(); i++){
                if(symbolTable.getFields().get(i).getName().equals(assignStmt.getJmmChild(0).getJmmChild(0).get("name"))){
                    flag = 1;
                    name = symbolTable.getFields().get(i).getName();
                    type = OllirUtils.getCode(symbolTable.getFields().get(i).getType());

                }
            }
        }

        tmpNode= assignStmt.getJmmParent();
        while(!tmpNode.getKind().equals("MethodDeclaration")){
            tmpNode = tmpNode.getJmmParent();
        }
        if(symbolTable.getLocalVariables(tmpNode.get("methodName")).size() > 0) {
            for(int i=0; i < symbolTable.getLocalVariables(tmpNode.get("methodName")).size(); i++){
                if(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i).getName().equals(assignStmt.getJmmChild(0).getJmmChild(0).get("name"))){
                    flag = 0;
                    name = OllirUtils.getCode(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i));
                    type = OllirUtils.getType(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i));
                }
            }
        }
        if(symbolTable.getParameters(tmpNode.get("methodName")).size() > 0) {
            for(int i=0; i < symbolTable.getParameters(tmpNode.get("methodName")).size(); i++){
                if(symbolTable.getParameters(tmpNode.get("methodName")).get(i).getName().equals(assignStmt.getJmmChild(0).getJmmChild(0).get("name"))){
                    name = OllirUtils.getName(symbolTable.getParameters(tmpNode.get("methodName")).get(i));
                    type = OllirUtils.getType(symbolTable.getParameters(tmpNode.get("methodName")).get(i));
                }
            }
        }

        if(flag == 1){
            code.append(name).append(".").append(type);
        }
        else{
            code.append(name);
        }
        code.append(" :=.").append(type).append(" ");
        if(assignStmt.getJmmChild(1).getChildren().size() > 1){
            if(assignStmt.getJmmChild(1).getJmmChild(1).getKind().equals("function_call")){
                code.append("invokevirtual(");
                visit(assignStmt.getJmmChild(1).getJmmChild(0));
                code.append(", ");
                code.append("\"");
                visit(assignStmt.getJmmChild(1).getJmmChild(1));
                code.append("\"");

                for (var child : assignStmt.getJmmChild(1).getJmmChild(1).getChildren()) {
                    code.append(", ");
                    if(child.getJmmChild(0).getKind().equals("IntegerLiteral")) {
                        code.append(child.getJmmChild(0).get("int")).append(".i32");
                    }
                    else {
                        visit(child);
                    }
                }

                code.append(").");
                code.append(type);
            }
            else {
                visit(assignStmt.getJmmChild(1));
            }
        }
        else {
            visit(assignStmt.getJmmChild(1));
        }
        if(!assignStmt.getJmmChild(1).getJmmChild(0).getKind().equals("IntegerLiteral") && !assignStmt.getJmmChild(1).getJmmChild(0).getKind().equals("_NewObject") && !assignStmt.getJmmChild(1).getJmmChild(0).getKind().equals("_NewArray")){
            code.append(";\n");
        }

        var notInt = !type.equals("i32") && !type.equals("array.i32");
        var notString = !type.equals("String") && !type.equals("array.String");
        var notBoolean = !type.equals("bool") && !type.equals("array.bool");
        var isNewObject = assignStmt.getJmmChild(1).getJmmChild(0).getKind().equals("_NewObject");
        if(notInt && notBoolean && notString && isNewObject) {
            code.append("invokespecial(")
                    .append(name)
                    .append(",\"<init>\").V;\n");
        }
        return tmpCode;
    }

    private Code visitNewObject(JmmNode newObject, Code tmpCode) {
        var name = newObject.get("name");
        code.append("new(").append(name).append(").").append(name).append(";\n");
        return tmpCode;
    }

    private Code visitIntegerLiteral(JmmNode integerLiteral, Code tmpCode) {
        code.append(integerLiteral.get("int")).append(".").append("i32;\n");
        return tmpCode;
    }

    private Code visitIdentifier(JmmNode identifier, Code tmpCode) {
        JmmNode tmpNode = identifier.getJmmParent();
        String name = new String();
        String type = new String();

        while(!tmpNode.getKind().equals("ClassDeclaration")){
            tmpNode = tmpNode.getJmmParent();
        }
        if(symbolTable.getFields().size() > 0) {
            for(int i=0; i < symbolTable.getFields().size(); i++){
                if(symbolTable.getFields().get(i).getName().equals(identifier.get("name"))){
                    name = symbolTable.getFields().get(i).getName();
                    type = OllirUtils.getCode(symbolTable.getFields().get(i).getType());

                }
            }
        }

        tmpNode= identifier.getJmmParent();
        while(!tmpNode.getKind().equals("MethodDeclaration")){
            tmpNode = tmpNode.getJmmParent();
        }

        if(symbolTable.getFields().size() > 0) {
            for(int i=0; i < symbolTable.getFields().size(); i++){
                if(symbolTable.getFields().get(i).getName().equals(identifier.get("name")))
                    name = symbolTable.getFields().get(i).getName();
                type = OllirUtils.getCode(symbolTable.getFields().get(i).getType());

            }
        }

        if(symbolTable.getParameters(tmpNode.get("methodName")).size() > 0) {
            for(int i=0; i < symbolTable.getParameters(tmpNode.get("methodName")).size(); i++){
                if(symbolTable.getParameters(tmpNode.get("methodName")).get(i).getName().equals(identifier.get("name"))){
                    name = OllirUtils.getName(symbolTable.getParameters(tmpNode.get("methodName")).get(i));
                    type = OllirUtils.getType(symbolTable.getParameters(tmpNode.get("methodName")).get(i));
                }
            }
        }

        if(symbolTable.getLocalVariables(tmpNode.get("methodName")).size() > 0) {
            for(int i=0; i < symbolTable.getLocalVariables(tmpNode.get("methodName")).size(); i++){
                if(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i).getName().equals(identifier.get("name"))){
                    name = OllirUtils.getName(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i));
                    type = OllirUtils.getType(symbolTable.getLocalVariables(tmpNode.get("methodName")).get(i));
                }
            }
        }

        code.append(name).append(".").append(type);
        return tmpCode;
    }

    private Code visitFunctionCall(JmmNode functionCall, Code tmpCode) {
        code.append(functionCall.get("name"));
        return tmpCode;
    }

    private Code visitReturn(JmmNode returnNode, Code tmpCode) {
        JmmNode tmpNode = returnNode.getJmmParent();
        while(!tmpNode.getKind().equals("MethodDeclaration")){
            tmpNode = tmpNode.getJmmParent();
        }
        var returnType = OllirUtils.getCode(symbolTable.getReturnType(tmpNode.get("methodName")));

        if(returnNode.getJmmChild(0).getKind().equals("Value")) {
            code.append("ret.").append(returnType).append(" ");
            visit(returnNode.getJmmChild(0));
            if(returnNode.getJmmChild(0).getJmmChild(0).getKind().equals("_Identifier")) {
                        code.append(";\n");
            }
        }
        if(returnNode.getJmmChild(0).getKind().equals("BinOpInt")) {
            JmmNode binOpInt = returnNode.getJmmChild(0);
            String tmpVar = "temp" + tmp;
            code.append(tmpVar).append(".").append(returnType);
            code.append(" :=.").append(returnType).append(" ");
            tmp++;
            visit(binOpInt.getJmmChild(0));
            code.append(".").append(returnType);
            code.append(" ").append(binOpInt.get("op")).append(".").append(OllirUtils.getType(binOpInt.get("type"))).append(" ");
            visit(binOpInt.getJmmChild(1));
            code.append(".").append(returnType);
            code.append(";\n");


            code.append("ret.").append(returnType).append(" ");
            code.append(tmpVar).append(".").append(returnType);
            code.append(";\n");
        }
            return tmpCode;
    }

    private Code visitValue(JmmNode valueNode, Code tmpCode) {
        if(valueNode.getChildren().size() > 1){
            for(var children : valueNode.getChildren()) {
                visit(children);
            }
        }
        else {
            visit(valueNode.getJmmChild(0));
        }
        return tmpCode;
    }

    private Code visitVarDeclaration(JmmNode varDeclNode, Code tmpCode) {
        return tmpCode;
    }

    private Code visitNewArray(JmmNode newArray, Code tmpCode) {
        code.append("new(array, ");
        visit(newArray.getJmmChild(0));

        return tmpCode;
    }

    private Code visitArrayIndex(JmmNode arrayIndex, Code tmpCode) {
        code.append(arrayIndex.getJmmChild(0).getJmmChild(0).get("int"));
            code.append(".i32");
        if(arrayIndex.getJmmParent().getKind().equals("OpenCloseBrac")) {
            code.append("]");
            code.append(".i32");
        }
        else {
            code.append(").i32;\n");
        }
        return tmpCode;
    }

    public String getCode() {
        return code.toString();
    }
}

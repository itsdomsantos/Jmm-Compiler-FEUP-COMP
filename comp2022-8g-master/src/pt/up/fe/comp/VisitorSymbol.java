package pt.up.fe.comp;

import org.w3c.dom.Entity;

import java_cup.runtime.symbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.io.ObjectInputStream.GetField;
import java.sql.Types;
import java.util.Map;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

import com.javacc.parser.tree.ImportDeclaration;

import static java.lang.String.valueOf;

public class VisitorSymbol extends PreorderJmmVisitor<MySymbolTable, Integer> {

    private final List<Report> reports;

    public VisitorSymbol() {
        this.reports=new ArrayList<>();

        addVisit("ImportDeclaration", this::visitImportDeclaration);

        addVisit("MethodDeclaration", this::visitMethodDeclaration);

        addVisit("ClassField", this::visitClassField);

        addVisit("ClassDeclaration", this::visitClassDeclaration);

    }

    private Integer visitClassField(JmmNode node, MySymbolTable symbolTable)
    {
        var child=node.getJmmChild(0);
        var type=child.get("type");
        var isArray=Boolean.valueOf(child.get("isArray"));
        var name=node.get("name");

        symbolTable.addFields(new Symbol(new Type(type,isArray), name));
        return 0;
    }

    private Integer visitImportDeclaration(JmmNode node, MySymbolTable symbolTable) {
        var importString = node.getChildren().stream().map(id -> id.get("importName"))
                .collect(Collectors.joining("."));

        symbolTable.addImport(importString);

        return 0;
    }

    private Integer visitMethodDeclaration(JmmNode node, MySymbolTable symbolTable) {

        var methodName = node.get("methodName");

        var returnTypeName = node.getJmmChild(0).get("type");
        var returnTypeArray = Boolean.valueOf(node.getJmmChild(0).get("isArray"));
        var returnType =new Type(returnTypeName, returnTypeArray);


        var nparams=node.getChildren().subList(0, node.getNumChildren()).stream().filter(n -> n.getKind().equals("Param")).collect(Collectors.toList());

        if(symbolTable.hasMethod(methodName))
        {
            //TODO mudar linha e coluna eventualmente
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Duplicated method with name "+methodName, null));

            return -1;
        }

        var paramSymbols= nparams.stream()
                .map(param -> new Symbol(new Type(param.getJmmChild(0).get("type"),Boolean.valueOf(param.getJmmChild(0).get("isArray"))),param.get("name"))).collect(Collectors.toList());


        symbolTable.addMethod(methodName, returnType, paramSymbols);

        //local variables
        var variables=node.getChildren().subList(0, node.getNumChildren()).stream().filter(n -> n.getKind().equals("VarDecl")).collect(Collectors.toList());

        var localVariables= variables.stream()
                .map(param -> new Symbol(new Type(param.getJmmChild(0).get("type"),Boolean.valueOf(param.getJmmChild(0).get("isArray"))),param.get("name"))).collect(Collectors.toList());

        symbolTable.addLocalVariables(methodName, localVariables);

        return 0;
    }

    private Integer visitClassDeclaration(JmmNode node, MySymbolTable symbolTable) {
        symbolTable.setClassName(node.get("className"));

        if(node.getAttributes().contains("extendedClassName"))
        {
            symbolTable.setSuper(node.get("extendedClassName"));
        }
        else symbolTable.setSuper(null);
        return 0;
    }

    private Integer defaultVisit(JmmNode node, MySymbolTable symbolTable) {
        if (node.getNumChildren() != 2) { // Start has 2 children because <LF> is a Token Node
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }
        return visit(node.getJmmChild(0));
    }
    public List<Report> getReports()
    {
        return this.reports;
    }
}

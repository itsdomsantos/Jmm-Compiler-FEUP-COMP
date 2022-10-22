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

public class VisitorEval extends PreorderJmmVisitor<MySymbolTable, Integer> {

    private final List<Report> reports;

    public VisitorEval() {
        this.reports=new ArrayList<>();
/*         this.stack = new Stack<>();
        this.symbolTable = symbolTable;
        // MYSYMBOL complementa o symbol
        MySymbol globalScope = new MySymbol(new Type(TypeAuxiliar.NONE.toString(), false), "global",
                EntityTypeAuxiliar.GLOBAL);
        this.stack.push(globalScope);
        this.symbolTable.startScope(globalScope); */



        addVisit("ArrayIndex", this::visitArrayIndex);

        addVisit("Return", this::visitReturn);

        addVisit("BinOpInt", this::visitBinOpInt);

        addVisit("function_call", this::visitFunctionCall);

        addVisit("_WhileStatement", this::visitWhile);

        addVisit("_ConditionalStatement", this::visitIf);

        addVisit("_AssignmentStatement", this::visitAssignment);

        addVisit("_Identifier", this::visitIdentifier);

        addVisit("_This", this::visitThis);

        addVisit("_ArrayAssignment", this::visitArrayAssignment);

    }

    private Integer visitIdentifier(JmmNode node, MySymbolTable symbolTable)
    {
        var type=getTypeIdentifier(node,symbolTable);
        if(type==null)
            return -1;
        if(!type.getName().equals("int") && !type.getName().equals("boolean") && !type.getName().equals("String"))
        {
            if(symbolTable.getClassName().equals(type.getName()))
                return 0;
            if(symbolTable.getImports().contains(type.getName()))
                return 0;
            if(symbolTable.getSuper()!=null)
            {
                if(symbolTable.getSuper().equals(type.getName()))
                    return 0;
            }

            else
            {
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Class not imported "+type.getName(), null));

                return -1;
            }
        }

        return 0;
    }
    private Type getTypeIdentifier(JmmNode node, MySymbolTable symbolTable){
        var method=node.getAncestor("MethodDeclaration");

        if(method.isPresent())
        {
            var node_name=node.get("name");
            var ret=symbolTable.getLocalVariable(node_name,method.get().get("methodName"));
            if(ret!=null)
                return ret.getType();

            ret=symbolTable.getParameter(node_name,method.get().get("methodName"));
            if(ret!=null)
                return ret.getType();

            ret=symbolTable.getField(node_name);
            if(ret!=null)
            {
                if(method.get().get("methodName").equals("main"))
                {
                    reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Field access in static method in method "+method.get().get("methodName"), null));
                    return null;
                }
                return ret.getType();
            }


            if(symbolTable.getImports().contains(node_name))
                return new Type(node_name,false);

            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Undeclared Variable "+node_name+" in method "+method.get().get("methodName"), null));
            return null;
        }
        return null;
    }

    public List<Report> getReports()
    {
        return this.reports;
    }

    private Integer visitThis(JmmNode node, MySymbolTable symbolTable) {

        System.out.println(node.getAncestor("MethodDeclaration").get().get("methodName"));
        if(node.getAncestor("MethodDeclaration").get().get("methodName").equals("main"))
        {
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "This in main", null));
            return -1;

        }
        return 0;
    }
    private Integer visitBinOpInt(JmmNode node, MySymbolTable symbolTable) {
        var child_1=node.getJmmChild(0);
        var child_2=node.getJmmChild(1);
        Type child_1_type;
        Type child_2_type;
        if(child_1.getKind().equals("Value"))
        {
            child_1_type=getTypeValue(child_1,symbolTable);
            if(child_1_type==null)
                return -1;
        }
        //not implemented yet
        else return 0;

        if(child_2.getKind().equals("Value"))
        {
            child_2_type=getTypeValue(child_2,symbolTable);
            if(child_2_type==null)
                return -1;
        }
        //not implemented yet
        else return 0;

        if(!child_1_type.equals(child_2_type)) {
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Addition on different types("+child_1_type.getName()+", "+child_2_type.getName(), null));
            return -1;
        }

        /*if(!child_1_type.isArray()){ // CODE CREATE CONFLITS WITH OTHER TESTS. CAN'T FIND WHY...
            child_2.put("int", child_1.getJmmChild(0).get("int")+child_2.getJmmChild(0).get("int"));

            child_1.replace(child_2);
            node.removeJmmChild(1);
        }*/

        node.put("type",child_1_type.getName());
        node.put("isArray",valueOf(child_1_type.isArray()));
        return 0;
    }

    private boolean checkFunctionParam(JmmNode node, MySymbolTable symbolTable)
    {
        var previous_token=node.getAncestor("Value").get().getJmmChild(0);


        if(previous_token.getKind().equals("_This"))
        {
            var parameters=symbolTable.getParameters(node.get("name"));
            if(parameters==null)
            {
                if (node.getNumChildren()==0)
                    return true;
                else
                {
                    if(symbolTable.getSuper()!=null)
                        return true;
                    else
                        return false;
                }
            }

            if(parameters.size()!=node.getNumChildren())
                return false;

            for (int i=0;i<parameters.size();i++)
            {
                var actual=node.getJmmChild(i);
                var actual_type=getTypeValue(actual,symbolTable);
                if(actual_type==null)
                    return false;
                if( !(actual_type.getName().equals(parameters.get(i).getType().getName()) && actual_type.isArray()==parameters.get(i).getType().isArray()) )
                {
                    return false;
                }
            }
        }
        else if(previous_token.getKind().equals("function_call"))
        {
            if(!symbolTable.getMethods().contains(node.get("name"))) {
                if (symbolTable.getSuper()!=null) {
                    return true;
                } else return false;
            }

            var parameters=symbolTable.getParameters(node.get("name"));

            if(parameters==null)
            {
                if (node.getNumChildren()==0)
                    return true;
                else return false;
            }

            if(parameters.size()!=node.getNumChildren())
                return false;


            for (int i=0;i<parameters.size();i++)
            {
                var actual=node.getJmmChild(i);
                var actual_type=getTypeValue(actual,symbolTable);
                if(actual_type==null)
                    return false;
                if( !(actual_type.getName().equals(parameters.get(i).getType().getName()) && actual_type.isArray()==parameters.get(i).getType().isArray()) )
                {
                    return false;
                }
            }
        }
        else if(previous_token.getKind().equals("_Identifier"))
        {
            var type=getTypeIdentifier(previous_token,symbolTable);
            if(type==null)
            {
                System.out.println("hello 1");
                return false;
            }
            if(symbolTable.getClassName().equals(previous_token.get("name")) ||
                    getTypeIdentifier(previous_token,symbolTable).getName().equals(symbolTable.getClassName()))
            {
                if(!symbolTable.getMethods().contains(node.get("name"))) {
                    if (symbolTable.getSuper()!=null) {
                        return true;
                    } else
                    {
                        return false;
                    }
                }

                var parameters=symbolTable.getParameters(node.get("name"));

                if(parameters==null)
                {
                    if (node.getNumChildren()==0)
                        return true;
                    else {
                        System.out.println("hello 3");
                        return false;
                    }
                }

                if(parameters.size()!=node.getNumChildren()){
                    System.out.println("hello 4");
                    return false;
                }



                for (int i=0;i<parameters.size();i++)
                {
                    var actual=node.getJmmChild(i);
                    var actual_type=getTypeValue(actual,symbolTable);
                    if(actual_type==null)
                    {
                        System.out.println("hello 5");
                        return false;
                    }
                    if( !(actual_type.getName().equals(parameters.get(i).getType().getName()) && actual_type.isArray()==parameters.get(i).getType().isArray()) )
                    {
                        System.out.println("hello 6");
                        return false;
                    }
                }
            }
        }



        return true;
    }

    private Type getTypeValue(JmmNode node, MySymbolTable symbolTable)
    {
        var child=node.getJmmChild(0);

        if(child.getKind().equals("IntegerLiteral"))
        {
            node.put("type","int");
            node.put("isArray","false");
        }
        else if(node.getKind().equals("BinOpInt")){
            return new Type("int",false);
        }
        else if(node.getKind().equals("BinOpBoolLessThan")){

            return new Type("boolean",false);
        }
        else if(node.getKind().equals("BinOpBoolAnd")){
            var child_type =getTypeValue(node.getJmmChild(0),symbolTable);
            var child_type_2 =getTypeValue(node.getJmmChild(1),symbolTable);
            if(child_type_2.getName().equals("int"))
            {
                return new Type("int",false);
            }
            return new Type("boolean",false);
        }
        else if(child.getKind().equals("_True"))
        {
            node.put("type","boolean");
            node.put("isArray","false");
        }
        else if(child.getKind().equals("_False"))
        {
            node.put("type","boolean");
            node.put("isArray","false");
        }
        else if(child.getKind().equals("_Identifier"))
        {
            var type=getTypeIdentifier(child,symbolTable);
            if(type==null)
            {
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Eu não sei bem o que se passou, mas alguma coisa está errada", null));
                return null;
            }

            if( node.getChildren().size()>1) {

                if (node.getJmmChild(1).getKind().equals("Dot_length") && type.isArray()==true) {
                    node.put("type", "int");
                    node.put("isArray", "false");
                }
                else if (node.getJmmChild(1).getKind().equals("function_call")) {
                    if(symbolTable.getClassName().equals(child.get("name"))
                            || getTypeIdentifier(child,symbolTable).getName().equals(symbolTable.getClassName())
                    )
                    {
                        var temp=symbolTable.getReturnType(node.getJmmChild(1).get("name"));
                        if(temp==null)
                        {
                            node.put("type", "null");
                            node.put("isArray", "false");
                        }
                        else{
                            node.put("type", temp.getName());
                            node.put("isArray", valueOf(temp.isArray()));
                        }

                    }
                    else
                    {
                        return new Type("null",false);
                    }

                }
                else{

                    if(node.getJmmChild(1).getKind().equals("OpenCloseBrac"))
                    {

                        var temp_type1=getTypeIdentifier(node.getJmmChild(0),symbolTable);

                        if(!temp_type1.isArray())
                        {
                            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Array access on not array", null));
                            return null;
                        }

                        node.put("type",type.getName());
                        node.put("isArray","false");

                    }
                    else {
                        node.put("type",type.getName());
                        node.put("isArray",valueOf(type.isArray()));
                    }


                }
            }
            else{
                node.put("type",type.getName());
                node.put("isArray",valueOf(type.isArray()));
            }

        }

        else if(child.getKind().equals("function_call"))
        {
            var temp=symbolTable.getReturnType(child.get("name"));
            node.put("type", temp.getName());
            node.put("isArray", valueOf(temp.isArray()));
        }
        else if(child.getKind().equals("_This"))
        {
            if(node.getChildren().size()>1)
            {
                if(node.getJmmChild(1).getKind().equals("function_call"))
                {
                    var temp=symbolTable.getReturnType(node.getJmmChild(1).get("name"));
                    if (temp==null)
                    {
                        if(symbolTable.getSuper()!=null)
                        {
                            node.put("type", "null");
                            node.put("isArray", "null");
                        }
                        else{
                            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Method "+node.getJmmChild(1).get("name")+" does not exists or is declared after calling on method "+node.getAncestor("MethodDeclaration").get().get("methodName"), null));
                            return null;
                        }


                    }
                    else {
                        node.put("type", temp.getName());
                        node.put("isArray", valueOf(temp.isArray()));
                    }

                }

            }
            else
            {
                node.put("type", symbolTable.getClassName());
                node.put("isArray", "false");
            }
        }
        else if(child.getKind().equals("_OpenCLoseParen"))
        {

            var temp=getExprType(node.getJmmChild(0),symbolTable);

            if(temp==null)
                return null;

            node.put("type", temp.getName());
            node.put("isArray", valueOf(temp.isArray()));
        }
        else if(child.getKind().equals("_Exclamation"))
        {
            node.put("type", "boolean");
            node.put("isArray", "false");
        }

        else if(child.getKind().equals("_NewArray"))
        {
            node.put("type", "int");
            node.put("isArray", "true");
        }

        else if(child.getKind().equals("_NewObject"))
        {
            if(node.getNumChildren()>1)
            {
                if(node.getJmmChild(1).getKind().equals("function_call"))
                {
                    node.put("type", "null");
                    node.put("isArray", "null");

                }
                else{
                    node.put("type", child.get("name"));
                    node.put("isArray", "false");
                }
            }
            else{
                node.put("type", child.get("name"));
                node.put("isArray", "false");
            }

        }

        else{
            node.put("type",child.getKind());
            node.put("isArray",node.getAncestor("MethodDeclaration").get().get("methodName"));
        }



        return new Type(node.get("type"), Boolean.parseBoolean(node.get("isArray")));
    }


    private Type getExprType(JmmNode node, MySymbolTable symbolTable)
    {
        if(node.getKind().equals("Expr"))
            node=node.getJmmChild(0);
        var method_type= symbolTable.getReturnType(node.getAncestor("MethodDeclaration").get().get("methodName"));

        if(node.getKind().equals("Value"))
        {
            return getTypeValue(node,symbolTable);
        }

        else if(node.getKind().equals("BinOpInt")){
            return new Type("int",false);
        }
        else if(node.getKind().equals("BinOpBoolLessThan")){

            return new Type("boolean",false);
        }
        else if(node.getKind().equals("BinOpBoolAnd")){
            var child_type =getTypeValue(node.getJmmChild(0),symbolTable);
            var child_type_2 =getTypeValue(node.getJmmChild(1),symbolTable);
            if(child_type_2.getName().equals("int"))
            {
                return new Type("int",false);
            }
            return new Type("boolean",false);
        }
        else if(node.getKind().equals("_OpenCLoseParen"))
        {
            return getExprType(node.getJmmChild(0),symbolTable);
        }
        else
        {
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "What is this? ("+node.getKind()+") on method "+node.getAncestor("MethodDeclaration").get().get("methodName"), null));
            return new Type("wtf",false);
        }

    }

    private Integer visitAssignment(JmmNode node, MySymbolTable symbolTable){
        var type1=getExprType(node.getJmmChild(0), symbolTable);
        var type2=getExprType(node.getJmmChild(1), symbolTable);

        if(type1==null || type2==null)
            return -1;

        if(!type1.getName().equals(type2.getName()) || !type1.isArray()==type2.isArray())
        {
            if(symbolTable.getImports().contains(type1.getName()) && symbolTable.getImports().contains(type2.getName()))
                return 0;
            if(type2.getName().equals(symbolTable.getClassName()) && type1.getName().equals(symbolTable.getSuper()))
                return 0;
            if(type2.getName().equals("null"))
                return 0;


            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Wrong assignment "+type1.getName()+" = "+type2.getName()+" on "+node.getAncestor("MethodDeclaration").get().get("methodName"), null));
            return -1;
        }
        return 0;
    }
    private Integer visitArrayAssignment(JmmNode node, MySymbolTable symbolTable) {

        var child = node.getJmmChild(1);
        var child_type=getExprType(child, symbolTable);

        if(!getTypeIdentifier(node,symbolTable).getName().equals(child_type.getName()))
        {
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Invalid Array Assignement on"+node.getAncestor("MethodDeclaration").get().get("methodName"), null));
            return -1;
        }

        return 0;
    }

    private Integer visitIf(JmmNode node, MySymbolTable symbolTable){
        var type=getExprType(node.getJmmChild(0), symbolTable);
        if(type==null)
            return -1;
        if(!type.getName().equals("boolean") && !type.getName().equals("null") )
        {
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Not bool in if on "+node.getAncestor("MethodDeclaration").get().get("methodName"), null));
            return -1;
        }
        return 0;
    }
    private Integer visitWhile(JmmNode node, MySymbolTable symbolTable){
        var type=getExprType(node.getJmmChild(0), symbolTable);
        if(type==null)
            return -1;
        if(type.isArray())
        {
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Array in While Condition on "+node.getAncestor("MethodDeclaration").get().get("methodName"), null));
            return -1;
        }
        return 0;
    }

    private Integer visitFunctionCall(JmmNode node, MySymbolTable symbolTable){


        if(checkFunctionParam(node,symbolTable))
            return 0;
        else {
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Incompatible arguments on call to method "+node.get("name")+" on method "+node.getAncestor("MethodDeclaration").get().get("methodName"), null));
            return -1;
        }

    }
    private Integer visitReturn(JmmNode node, MySymbolTable symbolTable)
    {
        var method_type= symbolTable.getReturnType(node.getAncestor("MethodDeclaration").get().get("methodName"));
        var type=getExprType(node.getJmmChild(0),symbolTable);
        if(type==null)
            return -1;
        if(type.getName().equals("null"))
        {
            return 0;
        }
        if(!type.equals(method_type))
        {
            if(getTypeValue(node.getJmmChild(0),symbolTable)==null)
            {
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Return type does not match method return type on method "
                        +node.getAncestor("MethodDeclaration").get().get("methodName"), null));

            }

            else {
                reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Return type does not match method return type on method "
                        +node.getAncestor("MethodDeclaration").get().get("methodName")+
                        "method claims "+method_type.getName()+" but returns "+getTypeValue(node.getJmmChild(0),symbolTable).getName(), null));

            }
            return -1;
        }
        return 0;



    }





    private Integer visitArrayIndex(JmmNode node, MySymbolTable symbolTable) {
        var value=node.getJmmChild(0);
        var type=getTypeValue(value,symbolTable);
        if(type==null)
            return -1;
        if(!type.getName().equals("int") || type.isArray()!=false )
        {
            System.out.println("cenas");
            System.out.println(type.getName());
            System.out.println(type.isArray());
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "Array index not int on method "+node.getAncestor("MethodDeclaration").get().get("methodName"), null));

            return -1;
        }
        System.out.println("cenas");
        System.out.println(type.getName());
        System.out.println(type.isArray());
        return 0;
    }



    private Integer defaultVisit(JmmNode node, MySymbolTable symbolTable) {
        if (node.getNumChildren() != 2) { // Start has 2 children because <LF> is a Token Node
            throw new RuntimeException("Illegal number of children in node " + node.getKind() + ".");
        }
        return visit(node.getJmmChild(0));
    }

}

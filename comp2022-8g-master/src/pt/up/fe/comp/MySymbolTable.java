package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MySymbolTable implements SymbolTable {
    public List<String> imports;
    private String className;
    private String superClass;
    
    private List<Symbol> classFields;
    public List<String> methods = new ArrayList<>();

    private Map<String, List<Symbol>> localVariables;
    private Map<String, Type> methodReturnTypes;
    private Map<String, List<Symbol>> methodParams;


    public MySymbolTable() {
        this.className=null;
        this.superClass=null;

        this.classFields= new ArrayList<>();
        this.imports=new ArrayList<>();
        this.methods=new ArrayList<>();
        
        this.localVariables= new HashMap<>();
        this.methodReturnTypes=new HashMap<>();
        this.methodParams=new HashMap<>();
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    public void addImport(String imported) {
        this.imports.add(imported);
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getSuper() {
        return this.superClass;
    }

    
    public void setSuper(String superClass) {
        this.superClass=superClass;
    }

    @Override
    public List<Symbol> getFields() {
        return this.classFields;
    }

    public Symbol getField(String name)
    {
        for (int i=0;i<this.classFields.size();i++)
        {
            if(this.classFields.get(i).getName().equals(name))
            {
                return this.classFields.get(i);
            }
        }
        return null;
    }

    public void addFields(Symbol field) {
        this.classFields.add(field);
    }

    @Override
    public List<String> getMethods() {
        return this.methods;
    }

    public boolean hasMethod(String methodSignature)
    {
        return methods.contains(methodSignature);
    }

    public void addMethod(String methodSignature, Type returnType, List<Symbol> params) {

        this.methods.add(methodSignature);

        this.methodReturnTypes.put(methodSignature,returnType);

        this.methodParams.put(methodSignature, params);
    }

    @Override
    public Type getReturnType(String methodSignature) {
        return methodReturnTypes.get(methodSignature);
    }

    @Override
    public List<Symbol> getParameters(String methodSignature) {
        return methodParams.get(methodSignature);
    }

    public Symbol getParameter(String name,String methodSignature)
    {
        for (int i=0;i<this.methodParams.get(methodSignature).size();i++)
        {
            if(this.methodParams.get(methodSignature).get(i).getName().equals(name))
            {
                return this.methodParams.get(methodSignature).get(i);
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodSignature) {
        return this.localVariables.get(methodSignature);
    }


    public Symbol getLocalVariable(String name, String methodSignature)
    {
        for (int i=0;i<this.localVariables.get(methodSignature).size();i++)
        {
            if(this.localVariables.get(methodSignature).get(i).getName().equals(name))
            {
                return this.localVariables.get(methodSignature).get(i);
            }
        }
        return null;
    }


    public void addLocalVariables(String methodSignature, List<Symbol> localVariables) {
        this.localVariables.put(methodSignature,localVariables);
    }

    @Override
    public String getName() {
        return this.className;
    }


}

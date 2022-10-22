package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.Collections;

public class JmmOptimizer implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        OllirGenerator ollirGenerator = new OllirGenerator(semanticsResult.getSymbolTable());
        ollirGenerator.visit(semanticsResult.getRootNode());
        var ollirCode = ollirGenerator.getCode();
        System.out.println(ollirCode);


        String OLLIR = "myClass {\n" +
                ".field private a.i32;\n" +
                ".construct myClass().V{\n" +
                "invokespecial(this, \"<init>\").V;\n" +
                "}\n" +
                "}";



        return new OllirResult(semanticsResult, ollirCode, Collections.emptyList());
    }
}

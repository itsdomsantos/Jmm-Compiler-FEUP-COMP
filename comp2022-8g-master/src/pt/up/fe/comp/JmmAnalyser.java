package pt.up.fe.comp;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


public class JmmAnalyser implements JmmAnalysis{


    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {

        List<Report> reports = new ArrayList<>();
        MySymbolTable symbolTable = new MySymbolTable();

        VisitorSymbol eval2 = new VisitorSymbol();
        eval2.visit(parserResult.getRootNode(),symbolTable);
        reports.addAll(eval2.getReports());

        VisitorEval eval = new VisitorEval();
        eval.visit(parserResult.getRootNode(),symbolTable);
        reports.addAll(eval.getReports());




        List<semanticAnalyser> analysers = Arrays.asList(new MainMethodCheck(symbolTable), new DeclaredVariableCheck(symbolTable), new ArrayPlusIntCheck(symbolTable)
        , new MethodParameters(symbolTable));

        for(var analyser : analysers){
            reports.addAll(analyser.getReports());
        }

        return new JmmSemanticsResult(parserResult, symbolTable, reports /*LIST OF REPORTS*/);
    }
}

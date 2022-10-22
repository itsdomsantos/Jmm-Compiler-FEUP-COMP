package pt.up.fe.comp;

import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeclaredVariableCheck implements semanticAnalyser  {

    private final MySymbolTable mySymbolTable;

    public DeclaredVariableCheck(MySymbolTable mySymbolTable) {
        this.mySymbolTable = mySymbolTable;
    }

    @Override
    public List<Report> getReports() {

        for(var method : mySymbolTable.getMethods()) {
            if(mySymbolTable.getParameters(method).contains("x") || mySymbolTable.getLocalVariables(method).contains("x")){
                return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "'Variable '" + mySymbolTable.getLocalVariables(method) + "' was not declared"));
            }
        }
        return Collections.emptyList();
    }
}

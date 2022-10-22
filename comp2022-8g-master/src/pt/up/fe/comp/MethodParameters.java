package pt.up.fe.comp;

import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodParameters implements semanticAnalyser  {

    private final MySymbolTable mySymbolTable;

    public MethodParameters(MySymbolTable mySymbolTable) {
        this.mySymbolTable = mySymbolTable;
    }

    @Override
    public List<Report> getReports() {



        if(!mySymbolTable.getMethods().contains("main")){
            //return Arrays.asList(new Report(ReportType.ERROR, Stage.SEMANTIC, -1, -1, "'Class '" + mySymbolTable.getClassName() + "' does not contain main method"));
        }
        return Collections.emptyList();
    }
}
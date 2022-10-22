package pt.up.fe.comp;

import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ArrayPlusIntCheck implements semanticAnalyser  {

    private final MySymbolTable mySymbolTable;

    public ArrayPlusIntCheck(MySymbolTable mySymbolTable) {
        this.mySymbolTable = mySymbolTable;
    }

    @Override
    public List<Report> getReports() {

        return Collections.emptyList();
    }
}

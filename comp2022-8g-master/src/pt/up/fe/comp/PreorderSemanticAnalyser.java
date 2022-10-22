package pt.up.fe.comp;

import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

public class PreorderSemanticAnalyser extends PreorderJmmVisitor<Integer, Integer> implements semanticAnalyser {

    private final List<Report> reports;

    public PreorderSemanticAnalyser(){
        reports = new ArrayList<>();
    }

    @Override
    public List<Report> getReports() {
        return null;
    }

    protected void addReport(Report report){
        this.reports.add(report);
    }
}

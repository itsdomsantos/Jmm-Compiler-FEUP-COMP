package pt.up.fe.comp;
import org.junit.Test;
import pt.up.fe.specs.util.SpecsIo;

public class AnalysisTest {

    //tem method overloading. ignorar
         @Test
    public void quickSort() {
        var results= TestUtils.analyse(SpecsIo.getResource("fixtures/public/QuickSort.jmm"));
        System.out.println("Symbol Table: "+ results.getSymbolTable().print());
        TestUtils.mustFail(results);
    }

    @Test
    public void findMaximum()
    {
        var results= TestUtils.analyse(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
        System.out.println("Symbol Table: "+ results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

     @Test
    public void HelloWorld()
    {
        var results= TestUtils.analyse(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        System.out.println("Symbol Table: "+ results.getSymbolTable().print());
        TestUtils.noErrors(results);
    } 

    @Test
    public void ticTacToe() {
        var results= TestUtils.analyse(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
        System.out.println("Symbol Table: "+ results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }

    @Test
    public void whileAndIf() {
        var results= TestUtils.analyse(SpecsIo.getResource("fixtures/public/WhileAndIf.jmm"));
        System.out.println("Symbol Table: "+ results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }


    @Test
    public void MonteCarloPi() {
        var results= TestUtils.analyse(SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm"));
        System.out.println("Symbol Table: "+ results.getSymbolTable().print());
        TestUtils.noErrors(results);
    }
}   

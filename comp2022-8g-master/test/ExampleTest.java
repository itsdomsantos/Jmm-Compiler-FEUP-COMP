import org.junit.Test;
import pt.up.fe.comp.TestUtils;

public class ExampleTest {

    @Test
    public void testExpression() {
        var parserResult=TestUtils.parse("2+3"); // isto retorna sempre, deve-se guardar o resultado numa variavel
        //ver linha em baixo
        //TestUtils.noErrors(r.getReports());
        TestUtils.mustFail(parserResult.getReports());
         //var parserResult = TestUtils.parse("2+3\n10+20\n");
         //parserResult.getReports().get(0).getException().get().printStackTrace();
        // System.out.println();
         //var analysisResult = TestUtils.analyse(parserResult);
    }

}

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class AnalyseTest {

    @Test
    public void testExpression() {
        var result = TestUtils.analyse(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        System.out.println(result.getSymbolTable().print());
    }
}


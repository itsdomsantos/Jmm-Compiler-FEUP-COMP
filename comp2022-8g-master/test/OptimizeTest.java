
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class OptimizeTest {

    @Test
    public void testHelloWorld() {
        //var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/cp2/CompileArithmetic.jmm"));
        //var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/cpf/1_parser_and_tree/AddMultConstants.jmm"));
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/cpf/2_semantic_analysis/lookup/VarLookup_Field.jmm"));

        System.out.println(result.getSymbolTable().print());
        TestUtils.noErrors(result.getReports());
    }
}
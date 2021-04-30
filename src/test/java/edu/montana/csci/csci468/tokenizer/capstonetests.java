package edu.montana.csci.csci468.tokenizer;

import edu.montana.csci.csci468.CatscriptTestBase;
import edu.montana.csci.csci468.parser.statements.FunctionCallStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

public class capstonetests extends CatscriptTestBase {

    @Test
    public void simpleTokenizerTest(){
        assertTokensAre("6 + 7", INTEGER, PLUS, INTEGER, EOF);
        assertTokensAre("5 - 8", "5", "-", "8", "<EOF>");
        assertTokensAre("9   -   9", INTEGER, MINUS, INTEGER, EOF);
        assertTokensAre("6   +   3", "6", "+", "3", "<EOF>");
        assertTokensAre("2  \n +  \n 2", INTEGER, PLUS, INTEGER, EOF);
        assertTokensAre("2  \n +  \n 2", "2", "+", "2", "<EOF>");
    }

    @Test
    public void simpleNumbers(){
        assertTokensAre("7", INTEGER, EOF);
        assertTokensAre( "90", "90", "<EOF>");
        assertTokensAre("4 12 1234567", INTEGER, INTEGER, INTEGER, EOF);
        assertTokensAre("8 21 4567891", "8", "21", "4567891", "<EOF>");
    }

    @Test
    void checkifunaryExpressionEvaluatesProperly() {
        assertEquals(-8, evaluateExpression("-8"));
        assertEquals(true, evaluateExpression("true"));
    }



    @Test
    void checkiffactorExpressionEvaluatesProperly() {
        assertEquals(20, evaluateExpression("5 * 4"));
        assertEquals(3, evaluateExpression("9 / 3"));
        assertEquals(6, evaluateExpression("2 * 6 / 2"));
    }


    @Test
    public void functionCallStatementCheck() {
        FunctionCallStatement expr = parseStatement("z(10, 4, 6, 4) y = 1", false);
        assertNotNull(expr);
        assertEquals("z", expr.getName());
        assertEquals(4, expr.getArguments().size());
    }


}

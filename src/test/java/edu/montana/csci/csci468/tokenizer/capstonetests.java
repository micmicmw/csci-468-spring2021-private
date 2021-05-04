package edu.montana.csci.csci468.tokenizer;

import edu.montana.csci.csci468.CatscriptTestBase;
import edu.montana.csci.csci468.parser.expressions.ListLiteralExpression;
import edu.montana.csci.csci468.parser.statements.ForStatement;
import edu.montana.csci.csci468.parser.statements.FunctionCallStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

public class capstonetests extends CatscriptTestBase {
/*
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





*/




    @Test
    void additiveParenthesisCheck()
    {
        assertEquals(7, evaluateExpression("(1 + 1)+(2+3)"));
        assertEquals(-4, evaluateExpression("(2 + 1)-(2+5)"));
        assertEquals(127, evaluateExpression("(56 + 41)+(25+5)"));
    }

    @Test
    void factorMultipleParenthesisCheck()
    {
        assertEquals(120, evaluateExpression("(2*2)*(5*3)*(1*2)"));
        assertEquals(0, evaluateExpression("(254*212)*(533*233)*(1*0)"));
        assertEquals((10*15*10), evaluateExpression("(5*2)*(5*3)*(5*2)"));
    }
    //Test for end of parenthesis
    @Test
    void multipleParenthesisCheck()
    {
        assertEquals(5, evaluateExpression("(((5)))"));
        assertEquals(13, evaluateExpression("((((10+3))))"));
        assertEquals(64, evaluateExpression("((5)+3)*((8))"));
    }

    @Test
    public void TokenizeVarStatement(){
        assertTokensAre("var z = 25", VAR, IDENTIFIER, EQUAL, INTEGER, EOF);
        assertTokensAre("var y <= 2", VAR,IDENTIFIER,LESS_EQUAL,INTEGER,EOF);
        assertTokensAre("var x != y", VAR, IDENTIFIER, BANG_EQUAL, IDENTIFIER, EOF);
    }


    @Test
    void forLoopParseCheck()
    {
        ForStatement forExpression = parseStatement("for(a in [123, 321, 555]){ print(a) }");
        assertNotNull(forExpression);
        assertEquals("a", forExpression.getVariableName());
        assertTrue(forExpression.getExpression() instanceof ListLiteralExpression);
        assertEquals(1, forExpression.getBody().size());
    }

}

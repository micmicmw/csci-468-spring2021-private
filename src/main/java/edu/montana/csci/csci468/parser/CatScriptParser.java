package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import java.util.ArrayList;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        if (tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }

    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }




    private Expression parseUnary_expression(){

        while(tokens.match(NOT,MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression righthand = parseUnary_expression();
            UnaryExpression unaryExpression = new UnaryExpression(operator, righthand);
            unaryExpression.setStart(operator);
            unaryExpression.setEnd(righthand.getEnd());
            return unaryExpression;
        }
            return parsePrimaryExpression();

    }

    private  Expression parseFactorExpression(){
        Expression expression = parseUnary_expression();
        while(tokens.match(STAR,SLASH)){
            Token operator = tokens.consumeToken();
            final Expression righthand = parseUnary_expression();
            FactorExpression factorExpression = new FactorExpression(operator,expression,righthand);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(righthand.getEnd());
            expression = factorExpression;
        }
        return expression;

    }






    private Expression parseComparisonExpression(){
        Expression expression = parseAdditiveExpression();
        while(tokens.match(GREATER_EQUAL,GREATER,LESS_EQUAL,LESS)){
            Token operator = tokens.consumeToken();
            final Expression rightHand = parseComparisonExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHand);
            comparisonExpression.setStart((expression.getStart()));
            comparisonExpression.setEnd(rightHand.getEnd());
            return comparisonExpression;
        }
        return expression;

    }


    private Expression parseEqualityExpression(){
        Expression expression= parseComparisonExpression();
        while(tokens.match(BANG_EQUAL,EQUAL_EQUAL)){
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseEqualityExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression,rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            return equalityExpression;
        }
            //parseComparisonExpression();
        return expression;


    }



    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();

        while(tokens.match(PLUS, MINUS)) {

            Token operator = tokens.consumeToken();

            final Expression rightHandSide = parseFactorExpression();;
            System.out.println(rightHandSide.toString());



            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;

    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        } else if(tokens.match(STRING)) {
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringExpression.setToken(stringToken);
            return stringExpression;
        } else if(tokens.match(IDENTIFIER)) {
                Token IdentifierToken = tokens.consumeToken();
                if(tokens.match(EOF)){
                    IdentifierExpression IdentifierExpression = new IdentifierExpression(IdentifierToken.getStringValue());
                    IdentifierExpression.setToken(IdentifierToken);
                    return IdentifierExpression;
                }else{
                    ArrayList<Expression> list = new ArrayList<>();
                    tokens.consumeToken();
                    while(!tokens.match(EOF,RIGHT_PAREN)){
                        if(tokens.match(COMMA)){
                            tokens.consumeToken();
                        }
                        list.add(parseExpression());
                    }
                    FunctionCallExpression functionExpression = new FunctionCallExpression(IdentifierToken.getStringValue(),list);
                    require(RIGHT_PAREN,functionExpression, ErrorType.UNTERMINATED_ARG_LIST) ;
                    return functionExpression;

                }

        } else if(tokens.match(TRUE, FALSE)) {
            Token BooleanToken = tokens.consumeToken();
            BooleanLiteralExpression BooleanExpression = new BooleanLiteralExpression(Boolean.parseBoolean(BooleanToken.getStringValue()));
            BooleanExpression.setToken(BooleanToken);
            return BooleanExpression;
        } else if(tokens.match(NULL)) {
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression NullExpression = new NullLiteralExpression();
            NullExpression.setToken(nullToken);
            return NullExpression;
        }else if(tokens.match(LEFT_BRACKET)){
            tokens.consumeToken();
            ArrayList<Expression> list = new ArrayList<>();
            while(!tokens.match(EOF,RIGHT_BRACKET)) {

                if(tokens.match(COMMA)){
                    tokens.consumeToken();
                }else{
                    list.add(parseExpression());
                }

            }
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(list);

            require(RIGHT_BRACKET,listLiteralExpression, ErrorType.UNTERMINATED_LIST) ;

            return listLiteralExpression;

        }else if(tokens.match(LEFT_PAREN)){

        Token parenToken =tokens.consumeToken();
        Expression expression= null;

        if(!tokens.match(RIGHT_PAREN)){
            expression = parseExpression();
        }

        ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(expression);
        require(RIGHT_PAREN,parenthesizedExpression) ;

        return parenthesizedExpression;
        }else{
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression();
            syntaxErrorExpression.setToken(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}

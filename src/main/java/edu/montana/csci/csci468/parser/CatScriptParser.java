package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.LinkedList;

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
        }else{
            Statement forStmt = parseForStatement();
            if(forStmt != null){
                return forStmt;
            }else{
                Statement ifStmt =parseIfStatement();
                if(ifStmt != null){
                    return  ifStmt;
                }else{
                    Statement varSmt = parseVarStatement();
                    if(varSmt != null){
                        return  varSmt;
                    }
                }
            }
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }
    private Statement parseVarStatement(){
        if(tokens.match(VAR)){
            VariableStatement variableStatement = new VariableStatement();
            variableStatement.setStart(tokens.consumeToken());
            variableStatement.setVariableName(tokens.consumeToken().getStringValue());
            if(tokens.match(EQUAL)){
                require(EQUAL,variableStatement);
                variableStatement.setExpression(parseExpression());

                return variableStatement;
            }else{
                require(COLON, variableStatement);
                variableStatement.setExplicitType(tokens.consumeToken().toString());
            }
        }
        return null;
    }
    private Statement parseIfStatement(){
        if(tokens.match(IF)){
            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, ifStatement);
            while(!tokens.match(RIGHT_PAREN) && !tokens.match(EOF)){
                ifStatement.setExpression(parseExpression());
            }
            require(RIGHT_PAREN, ifStatement);
            require(LEFT_BRACE, ifStatement);
            LinkedList<Statement> truestatements
                    = new LinkedList<Statement>();
            while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)){
                truestatements.add(parseProgramStatement());
            }
            ifStatement.setTrueStatements(truestatements);
            //require(RIGHT_PAREN,functionExpression, ErrorType.UNTERMINATED_ARG_LIST) ;
            if(tokens.match(EOF)){
                ifStatement.setEnd(require(RIGHT_BRACE, ifStatement));
                return ifStatement;
            }else{
                require(RIGHT_BRACE, ifStatement);
                require(ELSE, ifStatement);
                require(LEFT_BRACE, ifStatement);
                LinkedList<Statement> elsestatements
                        = new LinkedList<Statement>();
                while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)){
                    elsestatements.add(parseProgramStatement());
                }
                ifStatement.setElseStatements(elsestatements);
                ifStatement.setEnd((require(RIGHT_BRACE,ifStatement)));
                return ifStatement;
            }
        }else{
            return null;
        }
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
    private  Statement parseForStatement(){
        if(tokens.match(FOR)){
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);
            forStatement.setVariableName(tokens.consumeToken().getStringValue());
            require(IN, forStatement);
            forStatement.setExpression((parseExpression()));
            require(RIGHT_PAREN, forStatement);
            require(LEFT_BRACE, forStatement);

            LinkedList<Statement> statements
                    = new LinkedList<Statement>();

            while(!tokens.match(RIGHT_BRACE) && !tokens.match(EOF)){
                statements.add(parseProgramStatement());
            }

            forStatement.setBody(statements);
            forStatement.setEnd(require(RIGHT_BRACE, forStatement));

            return forStatement;
        }else{
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
            if(tokens.match(LEFT_PAREN)){
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
            }else{

                IdentifierExpression IdentifierExpression = new IdentifierExpression(IdentifierToken.getStringValue());
                IdentifierExpression.setToken(IdentifierToken);
                return IdentifierExpression;
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
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            //syntaxErrorExpression.setToken(tokens.consumeToken());
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
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
import java.util.List;

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
        } else {
            if (currentFunctionDefinition != null) {
                return parseReturnStatement();
            }else {
                Statement forStmt = parseForStatement();
                if (forStmt != null) {
                    return forStmt;
                } else {
                    Statement ifStmt = parseIfStatement();
                    if (ifStmt != null) {
                        return ifStmt;
                    } else {
                        Statement varSmt = parseVarStatement();
                        if (varSmt != null) {
                            return varSmt;
                        } else {
                            Statement funSmt = parseFunctionDeclaration();
                            System.out.println(funSmt);
                            if (funSmt != null) {
                                return funSmt;
                            } else {
                                Statement assiSmt = ParseAssignmentStatement();
                                if (assiSmt != null) {
                                    return assiSmt;
                                }
                            }
                        }
                    }
                }
            }
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }


    private Statement parseReturnStatement() {
        if (tokens.matchAndConsume(RETURN)) {
            ReturnStatement rs= new ReturnStatement();
            rs.setFunctionDefinition(currentFunctionDefinition);
            if (!tokens.match(RIGHT_BRACE)) {
                rs.setExpression(parseExpression());
            }
            return rs;
        } else {
            return null;
        }
    }

    private  Statement ParseAssignmentStatement(){
        if(tokens.match(IDENTIFIER)){


            Token ex = tokens.consumeToken();



            if(tokens.match(EQUAL)){
                AssignmentStatement assignmentStatement = new AssignmentStatement();
                assignmentStatement.setStart(ex);
                assignmentStatement.setVariableName(ex.getStringValue());
                require(EQUAL, assignmentStatement);
                Expression expr = parseExpression();
                assignmentStatement.setExpression(expr);
                assignmentStatement.setEnd(expr.getEnd());
                return assignmentStatement;
            }else{
                //Statement callExpression = parseFunctionCallExpression(ex);
                return new FunctionCallStatement(parseFunctionCallExpression(ex));
                //return callExpression;

            }

        }
        return null;
    }
    private FunctionCallExpression parseFunctionCallExpression(Token ex){

        List<Expression> args = new LinkedList<>();

        tokens.consumeToken();// not what Ryan did

        while(!tokens.match(RIGHT_PAREN) && !tokens.match(EOF)){
            Expression exp = parseExpression();
            args.add(exp);
            if(tokens.match(COMMA)){
                tokens.consumeToken();
            }else if(!tokens.match(RIGHT_PAREN)){
                FunctionCallExpression expression = new FunctionCallExpression(ex.getStringValue(), args);
                expression.setStart(ex);
                expression.setEnd(tokens.consumeToken());
                expression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                return expression;
            }

        }

        FunctionCallExpression expression = new FunctionCallExpression(ex.getStringValue(), args);
        expression.setStart(ex);
        expression.setEnd(tokens.consumeToken());

        return expression;



    }

    private Statement parseVarStatement() {
        if (tokens.match(VAR)) {

            VariableStatement variableStatement = new VariableStatement();
            variableStatement.setStart(tokens.consumeToken());

            variableStatement.setVariableName(require(IDENTIFIER, variableStatement).getStringValue());

            if (tokens.matchAndConsume(COLON)) {
                variableStatement.setExplicitType(typeChecker().getType());
                System.out.print(variableStatement.getExplicitType());

                require(EQUAL, variableStatement);
                Expression expr = parseExpression();
                variableStatement.setExpression(expr);
                variableStatement.setEnd(expr.getEnd());
                return variableStatement;
            } else {
                require(EQUAL, variableStatement);
                Expression expr = parseExpression();
                variableStatement.setExpression(expr);
                variableStatement.setExplicitType(expr.getType());
                variableStatement.setEnd(expr.getEnd());
                return  variableStatement;

            }
        }
        return null;
    }

    private TypeLiteral typeChecker(){
        Token token = tokens.getCurrentToken();
        if(token.getType() == IDENTIFIER){
            String type = token.getStringValue();
            if(type.equals("int")){
                tokens.consumeToken();
                TypeLiteral t1 = new TypeLiteral();
                t1.setType(CatscriptType.INT);
                return t1;
            }else{
                if(type.equals("string")){
                    tokens.consumeToken();
                    TypeLiteral t1 = new TypeLiteral();
                    t1.setType(CatscriptType.STRING);
                    return t1;
                }else{
                    if(type.equals("bool")) {
                        tokens.consumeToken();
                        TypeLiteral t1 = new TypeLiteral();
                        t1.setType(CatscriptType.BOOLEAN);
                        return t1;
                    }else{
                        if(type.equals("object")) {
                            tokens.consumeToken();
                            TypeLiteral t1 = new TypeLiteral();
                            t1.setType(CatscriptType.OBJECT);
                            return t1;
                        }else{
                            if(type.equals("list")) {
                                tokens.consumeToken();
                                TypeLiteral t1 = new TypeLiteral();
                                if(tokens.matchAndConsume(LESS)){
                                    t1.setType(CatscriptType.getListType(typeChecker().getType()));

                                    require(GREATER, t1);
                                }
                                return t1;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    //type_expression = 'int' | 'string' | 'bool' | 'object' | 'list' [, '<' , type_expression, '>']


    private Statement parseFunctionDeclaration() {
        if(tokens.matchAndConsume(FUNCTION)) {
            FunctionDefinitionStatement functionDefinitionStatement = new FunctionDefinitionStatement();
            Token ex = tokens.consumeToken();
            functionDefinitionStatement.setName(ex.getStringValue());
            functionDefinitionStatement.setStart(ex);
            require(LEFT_PAREN, functionDefinitionStatement);


            while (!tokens.match(RIGHT_PAREN) && !tokens.match(EOF)) {

                String Placer = tokens.consumeToken().getStringValue();
                if (tokens.match(COLON)) {
                    tokens.consumeToken();
                    functionDefinitionStatement.addParameter(Placer, typeChecker());
                } else {
                    functionDefinitionStatement.addParameter(Placer, typeChecker());
                }
                if (tokens.match(COMMA)) {
                    tokens.consumeToken();
                }



            }













            require(RIGHT_PAREN, functionDefinitionStatement);

            if (tokens.matchAndConsume(COLON)) {
                functionDefinitionStatement.setType(typeChecker());
            } else {
                functionDefinitionStatement.setType(null);
            }




            require(LEFT_BRACE, functionDefinitionStatement);


            try {
                List<Statement> body = new LinkedList<>();

                while (tokens.hasMoreTokens() && !tokens.match(RIGHT_BRACE)) {
                    Statement stmt = parseProgramStatement();
                    body.add(stmt);
                }
                functionDefinitionStatement.setBody(body);
            } finally {
                currentFunctionDefinition = null;
            }

            functionDefinitionStatement.setEnd(require(RIGHT_BRACE, functionDefinitionStatement));
            return functionDefinitionStatement;



        }
        /*
        if (tokens.matchAndConsume(FUNCTION)) {
            FunctionDefinitionStatement functionDefinitionStatement = new FunctionDefinitionStatement();
            Token name = require(IDENTIFIER, functionDefinitionStatement);
            functionDefinitionStatement.setName(name.getStringValue());
            require(LEFT_PAREN, functionDefinitionStatement);

            if (!tokens.matchAndConsume(RIGHT_PAREN)) {
                do {
                    // match an identifier
                    // if ':' call parseTypeLiteral

                    functionDefinitionStatement.addParameter(identifier.getStringValue(), CatscriptType.OBJECT);
                } while (tokens.matchAndConsume(COMMA));

            }
            require(RIGHT_PAREN, functionDefinitionStatement);

            if (tokens.matchAndConsume(COLON)) {
                // call parseTypeliteral
                functionDefinitionStatement.setType(resultOfParseTypeLiteral);
            } else {
                functionDefinitionStatement.setType(null);
            }
            require(LEFT_BRACE, functionDefinitionStatement);

            currentFunctionDefinition = functionDefinitionStatement;


            try {
                List<Statement> body = new LinkedList<>();
                while (!tokens.match(RIGHT_BRACE)) {
                    Statement stmt = parseProgramStatement();
                    body.add(stmt);
                }

            } finally {
                currentFunctionDefinition = null;
            }


            functionDefinitionStatement.setBody(body);
            require(RIGHT_BRACE, functionDefinitionStatement);
            return functionDefinitionStatement;
        }*/

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
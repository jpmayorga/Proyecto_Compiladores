/*
 * @(#)Parser.java                        2.1 2003/10/07
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package Triangle.SyntacticAnalyzer;


import Triangle.ErrorReporter;
import Triangle.AbstractSyntaxTrees.ActualParameter;
import Triangle.AbstractSyntaxTrees.ActualParameterSequence;
import Triangle.AbstractSyntaxTrees.ArrayAggregate;
import Triangle.AbstractSyntaxTrees.ArrayExpression;
import Triangle.AbstractSyntaxTrees.ArrayTypeDenoter;
import Triangle.AbstractSyntaxTrees.AssignCommand;
import Triangle.AbstractSyntaxTrees.BinaryExpression;
import Triangle.AbstractSyntaxTrees.CallCommand;
import Triangle.AbstractSyntaxTrees.CallExpression;
import Triangle.AbstractSyntaxTrees.CharacterExpression;
import Triangle.AbstractSyntaxTrees.CharacterLiteral;
import Triangle.AbstractSyntaxTrees.Command;
import Triangle.AbstractSyntaxTrees.ConstActualParameter;
import Triangle.AbstractSyntaxTrees.ConstDeclaration;
import Triangle.AbstractSyntaxTrees.ConstFormalParameter;
import Triangle.AbstractSyntaxTrees.Declaration;
import Triangle.AbstractSyntaxTrees.ProcFunc;

import Triangle.AbstractSyntaxTrees.DotVname;
import Triangle.AbstractSyntaxTrees.EmptyActualParameterSequence;
import Triangle.AbstractSyntaxTrees.EmptyCommand;
import Triangle.AbstractSyntaxTrees.EmptyFormalParameterSequence;
import Triangle.AbstractSyntaxTrees.Expression;
import Triangle.AbstractSyntaxTrees.FProcFunc;
import Triangle.AbstractSyntaxTrees.FieldTypeDenoter;
import Triangle.AbstractSyntaxTrees.FormalParameter;
import Triangle.AbstractSyntaxTrees.FormalParameterSequence;
import Triangle.AbstractSyntaxTrees.FuncActualParameter;
import Triangle.AbstractSyntaxTrees.FuncDeclaration;
import Triangle.AbstractSyntaxTrees.FuncFormalParameter;
import Triangle.AbstractSyntaxTrees.Identifier;
import Triangle.AbstractSyntaxTrees.IfCommand;
import Triangle.AbstractSyntaxTrees.IfExpression;
import Triangle.AbstractSyntaxTrees.IntegerExpression;
import Triangle.AbstractSyntaxTrees.IntegerLiteral;
import Triangle.AbstractSyntaxTrees.LetCommand;
import Triangle.AbstractSyntaxTrees.LetExpression;
import Triangle.AbstractSyntaxTrees.MultipleActualParameterSequence;
import Triangle.AbstractSyntaxTrees.MultipleArrayAggregate;
import Triangle.AbstractSyntaxTrees.MultipleFieldTypeDenoter;
import Triangle.AbstractSyntaxTrees.MultipleFormalParameterSequence;
import Triangle.AbstractSyntaxTrees.MultipleRecordAggregate;
import Triangle.AbstractSyntaxTrees.Operator;
import Triangle.AbstractSyntaxTrees.PProcFunc;
import Triangle.AbstractSyntaxTrees.ProcActualParameter;
import Triangle.AbstractSyntaxTrees.ProcDeclaration;
import Triangle.AbstractSyntaxTrees.ProcFormalParameter;
import Triangle.AbstractSyntaxTrees.Program;
import Triangle.AbstractSyntaxTrees.RecordAggregate;
import Triangle.AbstractSyntaxTrees.RecordExpression;
import Triangle.AbstractSyntaxTrees.RecordTypeDenoter;
import Triangle.AbstractSyntaxTrees.SequentialCommand;
import Triangle.AbstractSyntaxTrees.SequentialDeclaration;
import Triangle.AbstractSyntaxTrees.SequentialProcFunc;
import Triangle.AbstractSyntaxTrees.SimpleTypeDenoter;
import Triangle.AbstractSyntaxTrees.SimpleVname;
import Triangle.AbstractSyntaxTrees.SingleActualParameterSequence;
import Triangle.AbstractSyntaxTrees.SingleArrayAggregate;
import Triangle.AbstractSyntaxTrees.SingleDeclaration;
import Triangle.AbstractSyntaxTrees.SingleFieldTypeDenoter;
import Triangle.AbstractSyntaxTrees.SingleFormalParameterSequence;
import Triangle.AbstractSyntaxTrees.SingleRecordAggregate;
import Triangle.AbstractSyntaxTrees.SubscriptVname;
import Triangle.AbstractSyntaxTrees.TypeDeclaration;
import Triangle.AbstractSyntaxTrees.TypeDenoter;
import Triangle.AbstractSyntaxTrees.UnaryExpression;
import Triangle.AbstractSyntaxTrees.VarActualParameter;
import Triangle.AbstractSyntaxTrees.VarDeclaration;
import Triangle.AbstractSyntaxTrees.VarFormalParameter;
import Triangle.AbstractSyntaxTrees.Vname;
import Triangle.AbstractSyntaxTrees.VnameExpression;
import Triangle.AbstractSyntaxTrees.WhileCommand;
import Triangle.AbstractSyntaxTrees.UntilCommand;
import Triangle.AbstractSyntaxTrees.DoCommand;
import Triangle.AbstractSyntaxTrees.DollarLongIdentifier;
import Triangle.AbstractSyntaxTrees.DollarVname;
import Triangle.AbstractSyntaxTrees.ElseifCommand;
import Triangle.AbstractSyntaxTrees.ForDoCommand;
import Triangle.AbstractSyntaxTrees.ForUntilCommand;
import Triangle.AbstractSyntaxTrees.ForWhileCommand;
import Triangle.AbstractSyntaxTrees.IfElseCommand;
import Triangle.AbstractSyntaxTrees.LongCommand;
import Triangle.AbstractSyntaxTrees.LongExpression;
import Triangle.AbstractSyntaxTrees.LongIdentifier;
import Triangle.AbstractSyntaxTrees.LongTypeDenoter;
import Triangle.AbstractSyntaxTrees.PackageDeclaration;
import Triangle.AbstractSyntaxTrees.PackageIdentifier;
import Triangle.AbstractSyntaxTrees.RecursiveDeclaration;
import Triangle.AbstractSyntaxTrees.SimpleLongIdentifier;
import Triangle.AbstractSyntaxTrees.SimplePackageIdentifier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parser {

  private Scanner lexicalAnalyser;
  private ErrorReporter errorReporter;
  private Token currentToken;
  private SourcePosition previousTokenPosition;

  public Parser(Scanner lexer, ErrorReporter reporter) {
    lexicalAnalyser = lexer;
    errorReporter = reporter;
    previousTokenPosition = new SourcePosition();
  }

// accept checks whether the current token matches tokenExpected.
// If so, fetches the next token.
// If not, reports a syntactic error.

  void accept (int tokenExpected) throws SyntaxError {
    if (currentToken.kind == tokenExpected) {
      previousTokenPosition = currentToken.position;
      currentToken = lexicalAnalyser.scan();
    } else {
      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
    }
  }

  void acceptIt() {
    previousTokenPosition = currentToken.position;
    currentToken = lexicalAnalyser.scan();
  }

// start records the position of the start of a phrase.
// This is defined to be the position of the first
// character of the first token of the phrase.

  void start(SourcePosition position) {
    position.start = currentToken.position.start;
  }

// finish records the position of the end of a phrase.
// This is defined to be the position of the last
// character of the last token of the phrase.

  void finish(SourcePosition position) {
    position.finish = previousTokenPosition.finish;
  }

  void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
    SourcePosition pos = currentToken.position;
    errorReporter.reportError(messageTemplate, tokenQuoted, pos);
    throw(new SyntaxError());
  }

///////////////////////////////////////////////////////////////////////////////
//
// PROGRAMS
//
///////////////////////////////////////////////////////////////////////////////

  public Program parseProgram() {
    Program programAST = null;

    previousTokenPosition.start = 0;
    previousTokenPosition.finish = 0;
    currentToken = lexicalAnalyser.scan();

    try {
        PackageDeclaration pacDecAST = null;
        while(currentToken.kind == Token.PACKAGE){
            pacDecAST = parsePackageDeclaration();
            if(currentToken.kind != Token.SEMICOLON){
                syntacticError("; expected after end of program",
                        currentToken.spelling);
            }else{
                acceptIt();
            }
        }
      Command cAST = parseCommand();
      programAST = new Program(pacDecAST, cAST, previousTokenPosition);
      if (currentToken.kind != Token.EOT) {
        syntacticError("\"%\" not expected after end of program",
          currentToken.spelling);
      }
    }
    catch (SyntaxError s) { return null; }
    return programAST;
  }

///////////////////////////////////////////////////////////////////////////////
//
// LITERALS
//
///////////////////////////////////////////////////////////////////////////////

// parseIntegerLiteral parses an integer-literal, and constructs
// a leaf AST to represent it.

  IntegerLiteral parseIntegerLiteral() throws SyntaxError {
    IntegerLiteral IL = null;

    if (currentToken.kind == Token.INTLITERAL) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      IL = new IntegerLiteral(spelling, previousTokenPosition);
      currentToken = lexicalAnalyser.scan();
    } else {
      IL = null;
      syntacticError("integer literal expected here", "");
    }
    return IL;
  }

// parseCharacterLiteral parses a character-literal, and constructs a leaf
// AST to represent it.

  CharacterLiteral parseCharacterLiteral() throws SyntaxError {
    CharacterLiteral CL = null;

    if (currentToken.kind == Token.CHARLITERAL) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      CL = new CharacterLiteral(spelling, previousTokenPosition);
      currentToken = lexicalAnalyser.scan();
    } else {
      CL = null;
      syntacticError("character literal expected here", "");
    }
    return CL;
  }

// parseIdentifier parses an identifier, and constructs a leaf AST to
// represent it.

  Identifier parseIdentifier() throws SyntaxError {
    Identifier I = null;

    if (currentToken.kind == Token.IDENTIFIER) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      I = new Identifier(spelling, previousTokenPosition);
      currentToken = lexicalAnalyser.scan();
    } else {
      I = null;
      syntacticError("identifier expected here", "");
    }
    return I;
  }

// parseOperator parses an operator, and constructs a leaf AST to
// represent it.

  Operator parseOperator() throws SyntaxError {
    Operator O = null;

    if (currentToken.kind == Token.OPERATOR) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      O = new Operator(spelling, previousTokenPosition);
      currentToken = lexicalAnalyser.scan();
    } else {
      O = null;
      syntacticError("operator expected here", "");
    }
    return O;
  }

///////////////////////////////////////////////////////////////////////////////
//
// COMMANDS
//
///////////////////////////////////////////////////////////////////////////////

// parseCommand parses the command, and constructs an AST
// to represent its phrase structure.

  Command parseCommand() throws SyntaxError {
    Command commandAST = null; // in case there's a syntactic error

    SourcePosition commandPos = new SourcePosition();

    start(commandPos);
    commandAST = parseSingleCommand();
    while (currentToken.kind == Token.SEMICOLON) {
      acceptIt();
      Command c2AST = parseSingleCommand();
      finish(commandPos);
      commandAST = new SequentialCommand(commandAST, c2AST, commandPos);
    }
    return commandAST;
  }
  Command parseRestOfIf(Command commandAST,SourcePosition commandPos) throws SyntaxError{

    switch (currentToken.kind) {
        case Token.ELSIF:{
		acceptIt();
		Expression eAST = parseExpression();
                accept(Token.THEN);
                 Command c1AST = parseCommand();
                commandAST = new ElseifCommand(eAST, c1AST, commandPos);
		parseRestOfIf(commandAST,commandPos);
		}
        }

      return  commandAST;
        
  }
  
  Command parseSingleCommand() throws SyntaxError {
    Command commandAST = null; // in case there's a syntactic error

    SourcePosition commandPos = new SourcePosition();
    start(commandPos);

    switch (currentToken.kind) {

    case Token.IDENTIFIER:
      {
        Identifier iAST = parseIdentifier();
        if (currentToken.kind == Token.LPAREN) {
          acceptIt();
          ActualParameterSequence apsAST = parseActualParameterSequence();
          accept(Token.RPAREN);
          LongIdentifier lAST = new SimpleLongIdentifier(iAST, iAST.position);
          finish(commandPos);
          commandAST = new LongCommand(lAST, apsAST, commandPos);
          //commandAST = new CallCommand(iAST, apsAST, commandPos);

        } else if (currentToken.kind == Token.DOLLAR) {
            acceptIt();
            LongIdentifier lAST= parseRestOfLongIdentifier(iAST);
            accept(Token.LPAREN);
            ActualParameterSequence apsAST = parseActualParameterSequence();
            accept(Token.RPAREN);
            finish(commandPos);
            commandAST = new LongCommand(lAST, apsAST, commandPos);
        }else {

          Vname vAST = parseRestOfVname(iAST);
          accept(Token.BECOMES);
          Expression eAST = parseExpression();
          finish(commandPos);
          commandAST = new AssignCommand(vAST, eAST, commandPos);
        }
      }
      break;
/*
    case Token.BEGIN:
      acceptIt();
      commandAST = parseCommand();
      accept(Token.END);
      break;
*/
      
    case Token.NOTHING:
    {
      acceptIt();
      finish(commandPos);
      commandAST = new EmptyCommand(commandPos);
      break;
    } 
  
    case Token.LET:
      {
        acceptIt();
        Declaration dAST = parseDeclaration();
        accept(Token.IN);
        Command cAST = parseCommand();
        accept(Token.END);
	finish(commandPos);
	commandAST = new LetCommand(dAST,cAST,commandPos);
        break;

      }

    case Token.IF:
      {
        acceptIt();
        Expression eAST = parseExpression();
        accept(Token.THEN);
        Command c1AST = parseCommand();

        Command c2AST = parseRestOfIf(commandAST,commandPos);
        accept(Token.ELSE);
        Command c3AST = parseCommand();
        accept(Token.END);
	finish(commandPos);

            commandAST = new IfCommand(eAST,c1AST,c3AST,commandPos);


        break;

      }

      
	  case Token.LOOP:
      {
          System.out.println("Has loop");
        acceptIt();
		switch (currentToken.kind) 
		{
			case Token.WHILE:
			{
                            acceptIt();

			    Expression eAST = parseExpression();
				accept(Token.DO);
                                Command cAST = parseCommand();
				accept(Token.END);
				finish(commandPos);
				commandAST = new WhileCommand(eAST, cAST, commandPos);
				break;
			}
			case Token.UNTIL:
			{
                            acceptIt();

			    Expression eAST = parseExpression();
				accept(Token.DO);
                                Command cAST = parseCommand();
				accept(Token.END);
				finish(commandPos);
				commandAST = new UntilCommand(eAST, cAST, commandPos);
				break;
			}
			case Token.DO:
			{
                            
				accept(Token.DO);
                                Command cAST = parseCommand();
                                switch (currentToken.kind) {
                                    case Token.WHILE:{

                                        accept(Token.WHILE);

                                        Expression eAST = parseExpression();

                                        
                                        accept(Token.END);
                                        finish(commandPos);
                                        commandAST = new DoCommand(eAST, cAST, commandPos);
                                        break;
                                    }
                                    case Token.UNTIL:{

                                        accept(Token.UNTIL);

                                        Expression eAST = parseExpression();
                                        
                                        accept(Token.END);
                                        finish(commandPos);
                                        commandAST = new DoCommand(eAST, cAST, commandPos);
                                        break;
                                    }
                                    
                                }

                                        break;

			}
			case Token.FOR:
			{
                            
				accept(Token.FOR);
                                Identifier iAST = parseIdentifier();
                                accept(Token.FROM);
                                Expression eAST = parseExpression();
                                accept(Token.TO);
                                Expression eAST1 = parseExpression();

                                switch (currentToken.kind) {
                                    case Token.WHILE:{
                                        accept(Token.WHILE);

                                        Expression eAST2 = parseExpression();

                                        
                                        accept(Token.DO);
                                        Command cAST = parseCommand();                                 
                                        accept(Token.END);
                                        finish(commandPos);
                                        commandAST = new ForWhileCommand(iAST, eAST,eAST1, eAST2,cAST,commandPos);
                                        break;
                                    }
                                    case Token.UNTIL:{
                                        accept(Token.UNTIL);

                                        Expression eAST2 = parseExpression();
                                        
                                        accept(Token.DO);
                                        Command cAST = parseCommand();                                 
                                        accept(Token.END);
                                        finish(commandPos);
                                        commandAST = new ForUntilCommand(iAST, eAST,eAST1, eAST2,cAST,commandPos);
                                        break;
                                    }
                                    case Token.DO:{
                                        accept(Token.DO);
                                        Command cAST = parseCommand();                                 
                                        accept(Token.END);
                                        finish(commandPos);
                                        commandAST = new ForDoCommand(iAST, eAST,eAST1,cAST,commandPos);
                                        break;
                                    }
                                }

                                        break;

			}
		}


      }
   


    case Token.SEMICOLON:

      finish(commandPos);
      commandAST = new EmptyCommand(commandPos);
      break;

    default:
      syntacticError("\"%\" cannot start a command",
        currentToken.spelling);
      break;

    }

    return commandAST;
  }


///////////////////////////////////////////////////////////////////////////////
//
// EXPRESSIONS
//
///////////////////////////////////////////////////////////////////////////////

  Expression parseExpression() throws SyntaxError {
    Expression expressionAST = null; // in case there's a syntactic error

    SourcePosition expressionPos = new SourcePosition();

    start (expressionPos);

    switch (currentToken.kind) {

    case Token.LET:
      {
        acceptIt();
        Declaration dAST = parseDeclaration();
        accept(Token.IN);
        Expression eAST = parseExpression();
        finish(expressionPos);
        expressionAST = new LetExpression(dAST, eAST, expressionPos);
      }
      break;

    case Token.IF:
      {
        acceptIt();
        Expression e1AST = parseExpression();
        accept(Token.THEN);
        Expression e2AST = parseExpression();
        accept(Token.ELSE);
        Expression e3AST = parseExpression();
        finish(expressionPos);
        expressionAST = new IfExpression(e1AST, e2AST, e3AST, expressionPos);
      }
      break;

    default:
      expressionAST = parseSecondaryExpression();
      break;
    }
    return expressionAST;
  }

  Expression parseSecondaryExpression() throws SyntaxError {
    Expression expressionAST = null; // in case there's a syntactic error

    SourcePosition expressionPos = new SourcePosition();
    start(expressionPos);

    expressionAST = parsePrimaryExpression();
    while (currentToken.kind == Token.OPERATOR) {
      Operator opAST = parseOperator();
      Expression e2AST = parsePrimaryExpression();
      expressionAST = new BinaryExpression (expressionAST, opAST, e2AST,
        expressionPos);
    }
    return expressionAST;
  }

  Expression parsePrimaryExpression() throws SyntaxError {
    Expression expressionAST = null; // in case there's a syntactic error

    SourcePosition expressionPos = new SourcePosition();
    start(expressionPos);

    switch (currentToken.kind) {

    case Token.INTLITERAL:
      {
        IntegerLiteral ilAST = parseIntegerLiteral();
        finish(expressionPos);
        expressionAST = new IntegerExpression(ilAST, expressionPos);
      }
      break;

    case Token.CHARLITERAL:
      {
        CharacterLiteral clAST= parseCharacterLiteral();
        finish(expressionPos);
        expressionAST = new CharacterExpression(clAST, expressionPos);
      }
      break;

    case Token.LBRACKET:
      {
        acceptIt();
        ArrayAggregate aaAST = parseArrayAggregate();
        accept(Token.RBRACKET);
        finish(expressionPos);
        expressionAST = new ArrayExpression(aaAST, expressionPos);
      }
      break;

    case Token.LCURLY:
      {
        acceptIt();
        RecordAggregate raAST = parseRecordAggregate();
        accept(Token.RCURLY);
        finish(expressionPos);
        expressionAST = new RecordExpression(raAST, expressionPos);
      }
      break;

    case Token.IDENTIFIER:
      {
        Identifier iAST= parseIdentifier();
        if (currentToken.kind == Token.LPAREN) {
          acceptIt();
          ActualParameterSequence apsAST = parseActualParameterSequence();
          accept(Token.RPAREN);
          LongIdentifier lAST = new SimpleLongIdentifier(iAST, expressionPos);
          finish(expressionPos);
          expressionAST = new LongExpression(lAST, apsAST, expressionPos);
          //expressionAST = new CallExpression(iAST, apsAST, expressionPos);

        } else if (currentToken.kind == Token.DOLLAR){
            acceptIt();
            LongIdentifier lAST = parseRestOfLongIdentifier(iAST);
            accept(Token.LPAREN);
            ActualParameterSequence apsAST = parseActualParameterSequence();
            accept(Token.RPAREN);
            finish(expressionPos);
            expressionAST = new LongExpression(lAST, apsAST, expressionPos);
            //expressionAST = new RecordExpression(raAST, expressionPos);
        }else {
          Vname vAST = parseRestOfVname(iAST);
          finish(expressionPos);
          expressionAST = new VnameExpression(vAST, expressionPos);
        }
      }
      break;

    case Token.OPERATOR:
      {
        Operator opAST = parseOperator();
        Expression eAST = parsePrimaryExpression();
        finish(expressionPos);
        expressionAST = new UnaryExpression(opAST, eAST, expressionPos);
      }
      break;

    case Token.LPAREN:
      acceptIt();
      expressionAST = parseExpression();
      accept(Token.RPAREN);
      break;

    default:
      syntacticError("\"%\" cannot start an expression",
        currentToken.spelling);
      break;

    }
    return expressionAST;
  }

  RecordAggregate parseRecordAggregate() throws SyntaxError {
    RecordAggregate aggregateAST = null; // in case there's a syntactic error

    SourcePosition aggregatePos = new SourcePosition();
    start(aggregatePos);

    Identifier iAST = parseIdentifier();
    accept(Token.IS);
    Expression eAST = parseExpression();

    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      RecordAggregate aAST = parseRecordAggregate();
      finish(aggregatePos);
      aggregateAST = new MultipleRecordAggregate(iAST, eAST, aAST, aggregatePos);
    } else {
      finish(aggregatePos);
      aggregateAST = new SingleRecordAggregate(iAST, eAST, aggregatePos);
    }
    return aggregateAST;
  }

  ArrayAggregate parseArrayAggregate() throws SyntaxError {
    ArrayAggregate aggregateAST = null; // in case there's a syntactic error

    SourcePosition aggregatePos = new SourcePosition();
    start(aggregatePos);

    Expression eAST = parseExpression();
    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      ArrayAggregate aAST = parseArrayAggregate();
      finish(aggregatePos);
      aggregateAST = new MultipleArrayAggregate(eAST, aAST, aggregatePos);
    } else {
      finish(aggregatePos);
      aggregateAST = new SingleArrayAggregate(eAST, aggregatePos);
    }
    return aggregateAST;
  }

///////////////////////////////////////////////////////////////////////////////
//
// VALUE-OR-VARIABLE NAMES
//
///////////////////////////////////////////////////////////////////////////////

  Vname parseVname () throws SyntaxError {
    SourcePosition vnamePos = new SourcePosition();
    Vname vnameAST = null; // in case there's a syntactic error
    Identifier iAST = parseIdentifier();
    if(currentToken.kind == Token.DOLLAR){
        acceptIt();
        Identifier i2AST = parseIdentifier();
        Vname vAST = parseRestOfVname(i2AST);
        vnameAST = new DollarVname(iAST, vAST, iAST.position);
    }else{
        vnameAST = parseRestOfVname(iAST);
    }
    return vnameAST;
  }

  Vname parseRestOfVname(Identifier identifierAST) throws SyntaxError {
    SourcePosition vnamePos = new SourcePosition();
    vnamePos = identifierAST.position;
    Vname vAST = new SimpleVname(identifierAST, vnamePos);

    while (currentToken.kind == Token.DOT ||
           currentToken.kind == Token.LBRACKET) {

      if (currentToken.kind == Token.DOT) {
        acceptIt();
        Identifier iAST = parseIdentifier();
        vAST = new DotVname(vAST, iAST, vnamePos);
      } else {
        acceptIt();
        Expression eAST = parseExpression();
        accept(Token.RBRACKET);
        finish(vnamePos);
        vAST = new SubscriptVname(vAST, eAST, vnamePos);
      }
    }
    return vAST;
  }
  
///////////////////////////////////////////////////////////////////////////////
//
// DECLARATIONS
//
///////////////////////////////////////////////////////////////////////////////

  Declaration parseDeclaration() throws SyntaxError {
    Declaration declarationAST = null; // in case there's a syntactic error

    SourcePosition declarationPos = new SourcePosition();
    start(declarationPos);
    declarationAST = parseCompoundDeclaration();
    while (currentToken.kind == Token.SEMICOLON) {
      acceptIt();
      Declaration d2AST = parseCompoundDeclaration();
      finish(declarationPos);
      declarationAST = new SequentialDeclaration(declarationAST, d2AST,
        declarationPos);
    }
    return declarationAST;
  }

  Declaration parseCompoundDeclaration() throws SyntaxError { 
    Declaration declarationAST = null;
    
    SourcePosition declarationPos = new SourcePosition();
    start(declarationPos);

    switch (currentToken.kind) {

      case Token.RECURSIVE:
      {
        acceptIt();
        ProcFunc proFunc = parseProcFuncs();
        accept(Token.END);
        finish(declarationPos);
        declarationAST = new RecursiveDeclaration(proFunc, declarationPos);

      }    
        break;
      
      case Token.PRIVATE:
      {
        acceptIt();
        Declaration dAST = parseDeclaration();
        accept(Token.IN);
        Declaration d2AST = parseDeclaration();
        accept(Token.END);
        finish(declarationPos);
        declarationAST = new SequentialDeclaration(dAST, d2AST, declarationPos);
      }
      break;

      default:
      {
        Declaration sAST = parseSingleDeclaration();
        declarationAST = new SingleDeclaration(sAST, declarationPos);
      }
        break;
    } 
    return declarationAST;
  }

  Declaration parseSingleDeclaration() throws SyntaxError {
    Declaration declarationAST = null; // in case there's a syntactic error

    SourcePosition declarationPos = new SourcePosition();
    start(declarationPos);

    switch (currentToken.kind) {

    case Token.CONST:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.IS);
        Expression eAST = parseExpression();
        finish(declarationPos);
        declarationAST = new ConstDeclaration(iAST, eAST, declarationPos);
      }
      break;

    case Token.VAR:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        if (currentToken.kind == Token.COLON){
          accept(Token.COLON);
          TypeDenoter tAST = parseTypeDenoter();
          finish(declarationPos);
          declarationAST = new VarDeclaration(iAST, tAST, declarationPos);
        }
        if (currentToken.kind == Token.BECOMES){
          accept(Token.BECOMES);
          Expression eAST = parseExpression();
          finish(declarationPos);
          declarationAST = new VarDeclaration(iAST, eAST, declarationPos);
        } 
      }
      break;

    case Token.PROC:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.LPAREN);
        FormalParameterSequence fpsAST = parseFormalParameterSequence();
        accept(Token.RPAREN);
        accept(Token.IS);
        Command cAST = parseCommand();
        accept(Token.END);
        finish(declarationPos);
        declarationAST = new ProcDeclaration(iAST, fpsAST, cAST, declarationPos);
      }
      break;

    case Token.FUNC:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.LPAREN);
        FormalParameterSequence fpsAST = parseFormalParameterSequence();
        accept(Token.RPAREN);
        accept(Token.COLON);
        TypeDenoter tAST = parseTypeDenoter();
        accept(Token.IS);
        Expression eAST = parseExpression();
        finish(declarationPos);
        declarationAST = new FuncDeclaration(iAST, fpsAST, tAST, eAST,
          declarationPos);
      }
      break;

    case Token.TYPE:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.IS);
        TypeDenoter tAST = parseTypeDenoter();
        finish(declarationPos);
        declarationAST = new TypeDeclaration(iAST, tAST, declarationPos);
      }
      break;

    default:
      syntacticError("\"%\" cannot start a declaration",
        currentToken.spelling);
      break;

    }
    return declarationAST;
  }

///////////////////////////////////////////////////////////////////////////////
//
// Proc func
//
///////////////////////////////////////////////////////////////////////////////

ProcFunc parseProcFuncs() throws SyntaxError { //Falta probar
  ProcFunc procfuncsAST;

  SourcePosition procfuncsPos = new SourcePosition();

  start(procfuncsPos);
  procfuncsAST = parseProcFunc();
  do {
    accept(Token.BAR);
    ProcFunc pf2AST = parseProcFunc();
    finish(procfuncsPos);
    procfuncsAST = new SequentialProcFunc(procfuncsAST, pf2AST, procfuncsPos);
  } while (currentToken.kind == Token.BAR);

  return procfuncsAST;
}


ProcFunc parseProcFunc() throws SyntaxError {
  ProcFunc procfuncAST = null;

  SourcePosition procfuncPos = new SourcePosition();

  start(procfuncPos);
  switch(currentToken.kind) {
    case Token.PROC:
    {
      acceptIt();
      Identifier iAST = parseIdentifier();
      accept(Token.LPAREN);
      FormalParameterSequence fpsAST = parseFormalParameterSequence();
      accept(Token.RPAREN);
      accept(Token.IS);
      Command cAST = parseCommand();
      accept(Token.END);
      finish(procfuncPos);
      procfuncAST = new PProcFunc(iAST, fpsAST, cAST, procfuncPos);
    }
    break;

    case Token.FUNC:
    {
      acceptIt();
      Identifier iAST = parseIdentifier();
      accept(Token.LPAREN);
      FormalParameterSequence fpsAST = parseFormalParameterSequence();
      accept(Token.RPAREN);
      accept(Token.COLON);
      TypeDenoter tAST = parseTypeDenoter();
      accept(Token.IS);
      Expression eAST = parseExpression();
      finish(procfuncPos); 
      procfuncAST = new FProcFunc(iAST, fpsAST, tAST, eAST, procfuncPos);      
    }
    break;

    default:
    syntacticError("\"%\" cannot start a proc func",
      currentToken.spelling);
    break;
  }
  return procfuncAST;
  
}




///////////////////////////////////////////////////////////////////////////////
//
// PARAMETERS
//
///////////////////////////////////////////////////////////////////////////////

  FormalParameterSequence parseFormalParameterSequence() throws SyntaxError {
    FormalParameterSequence formalsAST;

    SourcePosition formalsPos = new SourcePosition();

    start(formalsPos);
    if (currentToken.kind == Token.RPAREN) {
      finish(formalsPos);
      formalsAST = new EmptyFormalParameterSequence(formalsPos);

    } else {
      formalsAST = parseProperFormalParameterSequence();
    }
    return formalsAST;
  }

  FormalParameterSequence parseProperFormalParameterSequence() throws SyntaxError {
    FormalParameterSequence formalsAST = null; // in case there's a syntactic error;

    SourcePosition formalsPos = new SourcePosition();
    start(formalsPos);
    FormalParameter fpAST = parseFormalParameter();
    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      FormalParameterSequence fpsAST = parseProperFormalParameterSequence();
      finish(formalsPos);
      formalsAST = new MultipleFormalParameterSequence(fpAST, fpsAST,
        formalsPos);

    } else {
      finish(formalsPos);
      formalsAST = new SingleFormalParameterSequence(fpAST, formalsPos);
    }
    return formalsAST;
  }

  FormalParameter parseFormalParameter() throws SyntaxError {
    FormalParameter formalAST = null; // in case there's a syntactic error;

    SourcePosition formalPos = new SourcePosition();
    start(formalPos);

    switch (currentToken.kind) {

    case Token.IDENTIFIER:
      {
        Identifier iAST = parseIdentifier();
        accept(Token.COLON);
        TypeDenoter tAST = parseTypeDenoter();
        finish(formalPos);
        formalAST = new ConstFormalParameter(iAST, tAST, formalPos);
      }
      break;

    case Token.VAR:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.COLON);
        TypeDenoter tAST = parseTypeDenoter();
        finish(formalPos);
        formalAST = new VarFormalParameter(iAST, tAST, formalPos);
      }
      break;

    case Token.PROC:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.LPAREN);
        FormalParameterSequence fpsAST = parseFormalParameterSequence();
        accept(Token.RPAREN);
        finish(formalPos);
        formalAST = new ProcFormalParameter(iAST, fpsAST, formalPos);
      }
      break;

    case Token.FUNC:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.LPAREN);
        FormalParameterSequence fpsAST = parseFormalParameterSequence();
        accept(Token.RPAREN);
        accept(Token.COLON);
        TypeDenoter tAST = parseTypeDenoter();
        finish(formalPos);
        formalAST = new FuncFormalParameter(iAST, fpsAST, tAST, formalPos);
      }
      break;

    default:
      syntacticError("\"%\" cannot start a formal parameter",
        currentToken.spelling);
      break;

    }
    return formalAST;
  }


  ActualParameterSequence parseActualParameterSequence() throws SyntaxError {
    ActualParameterSequence actualsAST;

    SourcePosition actualsPos = new SourcePosition();

    start(actualsPos);
    if (currentToken.kind == Token.RPAREN) {
      finish(actualsPos);
      actualsAST = new EmptyActualParameterSequence(actualsPos);

    } else {
      actualsAST = parseProperActualParameterSequence();
    }
    return actualsAST;
  }

  ActualParameterSequence parseProperActualParameterSequence() throws SyntaxError {
    ActualParameterSequence actualsAST = null; // in case there's a syntactic error

    SourcePosition actualsPos = new SourcePosition();

    start(actualsPos);
    ActualParameter apAST = parseActualParameter();
    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      ActualParameterSequence apsAST = parseProperActualParameterSequence();
      finish(actualsPos);
      actualsAST = new MultipleActualParameterSequence(apAST, apsAST,
        actualsPos);
    } else {
      finish(actualsPos);
      actualsAST = new SingleActualParameterSequence(apAST, actualsPos);
    }
    return actualsAST;
  }

  ActualParameter parseActualParameter() throws SyntaxError {
    ActualParameter actualAST = null; // in case there's a syntactic error

    SourcePosition actualPos = new SourcePosition();

    start(actualPos);

    switch (currentToken.kind) {

    case Token.IDENTIFIER:
    case Token.INTLITERAL:
    case Token.CHARLITERAL:
    case Token.OPERATOR:
    case Token.LET:
    case Token.IF:
    case Token.LPAREN:
    case Token.LBRACKET:
    case Token.LCURLY:
      {
        Expression eAST = parseExpression();
        finish(actualPos);
        actualAST = new ConstActualParameter(eAST, actualPos);
      }
      break;

    case Token.VAR:
      {
        acceptIt();
        Vname vAST = parseVname();
        finish(actualPos);
        actualAST = new VarActualParameter(vAST, actualPos);
      }
      break;

    case Token.PROC:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        finish(actualPos);
        actualAST = new ProcActualParameter(iAST, actualPos);
      }
      break;

    case Token.FUNC:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        finish(actualPos);
        actualAST = new FuncActualParameter(iAST, actualPos);
      }
      break;

    default:
      syntacticError("\"%\" cannot start an actual parameter",
        currentToken.spelling);
      break;

    }
    return actualAST;
  }

///////////////////////////////////////////////////////////////////////////////
//
// TYPE-DENOTERS
//
///////////////////////////////////////////////////////////////////////////////

  TypeDenoter parseTypeDenoter() throws SyntaxError {
    TypeDenoter typeAST = null; // in case there's a syntactic error
    SourcePosition typePos = new SourcePosition();

    start(typePos);

    switch (currentToken.kind) {

    case Token.IDENTIFIER:
      {
        LongIdentifier lAST = parseLongIdentifier();
        finish(typePos);
        typeAST = new LongTypeDenoter(lAST, typePos);
        
        /*Identifier iAST = parseIdentifier();
        finish(typePos);
        typeAST = new SimpleTypeDenoter(iAST, typePos);*/
      }
      break;

    case Token.ARRAY:
      {
        acceptIt();
        IntegerLiteral ilAST = parseIntegerLiteral();
        accept(Token.OF);
        TypeDenoter tAST = parseTypeDenoter();
        finish(typePos);
        typeAST = new ArrayTypeDenoter(ilAST, tAST, typePos);
      }
      break;

    case Token.RECORD:
      {
        acceptIt();
        FieldTypeDenoter fAST = parseFieldTypeDenoter();
        accept(Token.END);
        finish(typePos);
        typeAST = new RecordTypeDenoter(fAST, typePos);
      }
      break;

    default:
        syntacticError("\"%\" cannot start a type denoter",
        currentToken.spelling);
      break;

    }
    return typeAST;
  }

  FieldTypeDenoter parseFieldTypeDenoter() throws SyntaxError {
    FieldTypeDenoter fieldAST = null; // in case there's a syntactic error

    SourcePosition fieldPos = new SourcePosition();

    start(fieldPos);
    Identifier iAST = parseIdentifier();
    accept(Token.COLON);
    TypeDenoter tAST = parseTypeDenoter();
    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      FieldTypeDenoter fAST = parseFieldTypeDenoter();
      finish(fieldPos);
      fieldAST = new MultipleFieldTypeDenoter(iAST, tAST, fAST, fieldPos);
    } else {
      finish(fieldPos);
      fieldAST = new SingleFieldTypeDenoter(iAST, tAST, fieldPos);
    }
    return fieldAST;
  }
  
///////////////////////////////////////////////////////////////////////////////
//
// Package Declaration
//
///////////////////////////////////////////////////////////////////////////////

  PackageDeclaration parsePackageDeclaration () throws  SyntaxError {
      PackageDeclaration packageDeclarationAST = null;
      
      SourcePosition pDecPos = new SourcePosition();

      start(pDecPos);
    
      accept(Token.PACKAGE);
      Identifier pIdentAST = parseIdentifier();
      accept(Token.IS);
      Declaration dAST = parseDeclaration();
      accept(Token.END);
      finish(pDecPos);
      packageDeclarationAST = new PackageDeclaration(pIdentAST, dAST, pDecPos);
      return packageDeclarationAST;
  }


///////////////////////////////////////////////////////////////////////////////
//
// Package Identifier
//
///////////////////////////////////////////////////////////////////////////////

    PackageIdentifier parsePackageIdentifier () throws SyntaxError {
        PackageIdentifier packageIdentifierAST = null;
        Identifier iAST = parseIdentifier();
        packageIdentifierAST = parseRestOfPackageIdentifier(iAST);
        return packageIdentifierAST;
    }
    
    PackageIdentifier parseRestOfPackageIdentifier (Identifier identifierAST) throws  SyntaxError {
        SourcePosition packageIdentifierPos = new SourcePosition();
        packageIdentifierPos = identifierAST.position;
        PackageIdentifier pAST = new SimplePackageIdentifier(identifierAST, packageIdentifierPos);
        
        return pAST;
    }

///////////////////////////////////////////////////////////////////////////////
//
// Long Identifier
//
///////////////////////////////////////////////////////////////////////////////
    
    LongIdentifier parseLongIdentifier () throws SyntaxError {
        LongIdentifier longIdentifierAST = null;
        SourcePosition pDecPos = new SourcePosition();
        start(pDecPos);
        
        Identifier iAST = parseIdentifier();
        if(currentToken.kind == Token.DOLLAR){
            acceptIt();
            longIdentifierAST = parseRestOfLongIdentifier(iAST);
        }else{
            finish(pDecPos);
            longIdentifierAST = new SimpleLongIdentifier(iAST, pDecPos);
        }
        return longIdentifierAST;
    }
    
    LongIdentifier parseRestOfLongIdentifier(Identifier identifierAST) throws SyntaxError {
        LongIdentifier lAST = null;
        SourcePosition longPos = new SourcePosition();
        longPos = identifierAST.position;
        Identifier iAST = parseIdentifier();
        lAST = new DollarLongIdentifier(identifierAST, iAST, longPos);
        finish(longPos);
        return lAST;
  }
}

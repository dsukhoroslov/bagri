// This file was generated on Fri Nov 22, 2013 09:05 (UTC+04) by REx v5.28 which is Copyright (c) 1979-2013 by Gunther Rademacher <grd@gmx.net>
// REx command line: xquery-30.ebnf -main -java -ll 2 -backtrack -tree -name XQuery30RExParserLL
package com.bagri.xquery.rex;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class XQuery30RExParserLL
{
  public static void main(String args[]) throws Exception
  {
    if (args.length == 0)
    {
      System.out.println("Usage: java XQuery30RExParserLL INPUT...");
      System.out.println();
      System.out.println("  parse INPUT, which is either a filename or literal text enclosed in curly braces\n");
    }
    else
    {
      for (String arg : args)
      {
        Writer w = new OutputStreamWriter(System.out, "UTF-8");
        XmlSerializer s = new XmlSerializer(w);
        XQuery30RExParserLL parser = new XQuery30RExParserLL(read(arg), s);
        try
        {
          s.writeOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?" + ">");
          parser.parse_XQuery();
        }
        catch (ParseException pe)
        {
          throw new RuntimeException("ParseException while processing " + arg + ":\n" + parser.getErrorMessage(pe));
        }
        finally
        {
          w.close();
        }
      }
    }
  }

  public static class ParseException extends RuntimeException
  {
    private static final long serialVersionUID = 1L;
    private int begin, end, offending, expected, state;

    public ParseException(int b, int e, int s, int o, int x)
    {
      begin = b;
      end = e;
      state = s;
      offending = o;
      expected = x;
    }

    public String getMessage()
    {
      return offending < 0 ? "lexical analysis failed" : "syntax error";
    }

    public int getBegin() {return begin;}
    public int getEnd() {return end;}
    public int getState() {return state;}
    public int getOffending() {return offending;}
    public int getExpected() {return expected;}
  }

  public interface EventHandler
  {
    public void reset(CharSequence string);
    public void startNonterminal(String name, int begin);
    public void endNonterminal(String name, int end);
    public void terminal(String name, int begin, int end);
    public void whitespace(int begin, int end);
  }

  public static class XmlSerializer implements EventHandler
  {
    private CharSequence input;
    private String delayedTag;
    private Writer out;

    public XmlSerializer(Writer w)
    {
      input = null;
      delayedTag = null;
      out = w;
    }

    public void reset(CharSequence string)
    {
      input = string;
    }

    public void startNonterminal(String name, int begin)
    {
      if (delayedTag != null)
      {
        writeOutput("<");
        writeOutput(delayedTag);
        writeOutput(">");
      }
      delayedTag = name;
    }

    public void endNonterminal(String name, int end)
    {
      if (delayedTag != null)
      {
        delayedTag = null;
        writeOutput("<");
        writeOutput(name);
        writeOutput("/>");
      }
      else
      {
        writeOutput("</");
        writeOutput(name);
        writeOutput(">");
      }
    }

    public void terminal(String name, int begin, int end)
    {
      if (name.charAt(0) == '\'')
      {
        name = "TOKEN";
      }
      startNonterminal(name, begin);
      characters(begin, end);
      endNonterminal(name, end);
    }

    public void whitespace(int begin, int end)
    {
      characters(begin, end);
    }

    private void characters(int begin, int end)
    {
      if (begin < end)
      {
        if (delayedTag != null)
        {
          writeOutput("<");
          writeOutput(delayedTag);
          writeOutput(">");
          delayedTag = null;
        }
        writeOutput(input.subSequence(begin, end)
                         .toString()
                         .replace("&", "&amp;")
                         .replace("<", "&lt;")
                         .replace(">", "&gt;"));
      }
    }

    public void writeOutput(String content)
    {
      try
      {
        out.write(content);
      }
      catch (IOException e)
      {
        throw new RuntimeException(e);
      }
    }
  }

  private static String read(String input) throws Exception
  {
    if (input.startsWith("{") && input.endsWith("}"))
    {
      return input.substring(1, input.length() - 1);
    }
    else
    {
      byte buffer[] = new byte[(int) new java.io.File(input).length()];
      java.io.FileInputStream stream = new java.io.FileInputStream(input);
      stream.read(buffer);
      stream.close();
      String content = new String(buffer, System.getProperty("file.encoding"));
      return content.length() > 0 && content.charAt(0) == '\uFEFF'
           ? content.substring(1)
           : content;
    }
  }

  public XQuery30RExParserLL(CharSequence string, EventHandler t)
  {
    initialize(string, t);
  }

  public void initialize(CharSequence string, EventHandler eh)
  {
    eventHandler = eh;
    input = string;
    size = input.length();
    reset(0, 0, 0);
  }

  public CharSequence getInput()
  {
    return input;
  }

  public int getTokenOffset()
  {
    return b0;
  }

  public int getTokenEnd()
  {
    return e0;
  }

  public final void reset(int l, int b, int e)
  {
            b0 = b; e0 = b;
    l1 = l; b1 = b; e1 = e;
    l2 = 0;
    end = e;
    ex = -1;
    memo.clear();
    eventHandler.reset(input);
  }

  public void reset()
  {
    reset(0, 0, 0);
  }

  public static String getOffendingToken(ParseException e)
  {
    return e.getOffending() < 0 ? null : TOKEN[e.getOffending()];
  }

  public static String[] getExpectedTokenSet(ParseException e)
  {
    String[] expected;
    if (e.getExpected() < 0)
    {
      expected = getTokenSet(- e.getState());
    }
    else
    {
      expected = new String[]{TOKEN[e.getExpected()]};
    }
    return expected;
  }

  public String getErrorMessage(ParseException e)
  {
    String[] tokenSet = getExpectedTokenSet(e);
    String found = getOffendingToken(e);
    String prefix = input.subSequence(0, e.getBegin()).toString();
    int line = prefix.replaceAll("[^\n]", "").length() + 1;
    int column = prefix.length() - prefix.lastIndexOf("\n");
    int size = e.getEnd() - e.getBegin();
    return e.getMessage()
         + (found == null ? "" : ", found " + found)
         + "\nwhile expecting "
         + (tokenSet.length == 1 ? tokenSet[0] : java.util.Arrays.toString(tokenSet))
         + "\n"
         + (size == 0 || found != null ? "" : "after successfully scanning " + size + " characters beginning ")
         + "at line " + line + ", column " + column + ":\n..."
         + input.subSequence(e.getBegin(), Math.min(input.length(), e.getBegin() + 64))
         + "...";
  }

  public void parse_XQuery()
  {
    eventHandler.startNonterminal("XQuery", e0);
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Module();
    shift(24);                      // EOF
    eventHandler.endNonterminal("XQuery", e0);
  }

  private void parse_Module()
  {
    eventHandler.startNonterminal("Module", e0);
    switch (l1)
    {
    case 191:                       // 'xquery'
      lookahead2W(128);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | 'and' | 'cast' | 'castable' |
                                    // 'div' | 'encoding' | 'eq' | 'except' | 'ge' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'lt' | 'mod' | 'ne' | 'or' | 'to' | 'treat' |
                                    // 'union' | 'version' | '|' | '||'
      break;
    default:
      lk = l1;
    }
    if (lk == 27327                 // 'xquery' 'encoding'
     || lk == 48063)                // 'xquery' 'version'
    {
      parse_VersionDecl();
    }
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    switch (l1)
    {
    case 138:                       // 'module'
      lookahead2W(126);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | 'and' | 'cast' | 'castable' |
                                    // 'div' | 'eq' | 'except' | 'ge' | 'gt' | 'idiv' | 'instance' | 'intersect' |
                                    // 'is' | 'le' | 'lt' | 'mod' | 'namespace' | 'ne' | 'or' | 'to' | 'treat' |
                                    // 'union' | '|' | '||'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 35722:                     // 'module' 'namespace'
      whitespace();
      parse_LibraryModule();
      break;
    default:
      whitespace();
      parse_MainModule();
    }
    eventHandler.endNonterminal("Module", e0);
  }

  private void parse_VersionDecl()
  {
    eventHandler.startNonterminal("VersionDecl", e0);
    shift(191);                     // 'xquery'
    lookahead1W(80);                // S^WS | '(:' | 'encoding' | 'version'
    switch (l1)
    {
    case 106:                       // 'encoding'
      shift(106);                   // 'encoding'
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      shift(4);                     // StringLiteral
      break;
    default:
      shift(187);                   // 'version'
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      shift(4);                     // StringLiteral
      lookahead1W(74);              // S^WS | '(:' | ';' | 'encoding'
      if (l1 == 106)                // 'encoding'
      {
        shift(106);                 // 'encoding'
        lookahead1W(17);            // StringLiteral | S^WS | '(:'
        shift(4);                   // StringLiteral
      }
    }
    lookahead1W(28);                // S^WS | '(:' | ';'
    whitespace();
    parse_Separator();
    eventHandler.endNonterminal("VersionDecl", e0);
  }

  private void parse_MainModule()
  {
    eventHandler.startNonterminal("MainModule", e0);
    parse_Prolog();
    whitespace();
    parse_QueryBody();
    eventHandler.endNonterminal("MainModule", e0);
  }

  private void parse_LibraryModule()
  {
    eventHandler.startNonterminal("LibraryModule", e0);
    parse_ModuleDecl();
    lookahead1W(93);                // S^WS | EOF | '(:' | 'declare' | 'import'
    whitespace();
    parse_Prolog();
    eventHandler.endNonterminal("LibraryModule", e0);
  }

  private void parse_ModuleDecl()
  {
    eventHandler.startNonterminal("ModuleDecl", e0);
    shift(138);                     // 'module'
    lookahead1W(48);                // S^WS | '(:' | 'namespace'
    shift(139);                     // 'namespace'
    lookahead1W(122);               // NCName^Token | S^WS | '(:' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where'
    whitespace();
    parse_NCName();
    lookahead1W(29);                // S^WS | '(:' | '='
    shift(58);                      // '='
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    whitespace();
    parse_URILiteral();
    lookahead1W(28);                // S^WS | '(:' | ';'
    whitespace();
    parse_Separator();
    eventHandler.endNonterminal("ModuleDecl", e0);
  }

  private void parse_Prolog()
  {
    eventHandler.startNonterminal("Prolog", e0);
    for (;;)
    {
      lookahead1W(167);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | EOF | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      switch (l1)
      {
      case 93:                      // 'declare'
        lookahead2W(131);           // S^WS | EOF | '!' | '!=' | '#' | '%' | '(' | '(:' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | 'and' | 'base-uri' |
                                    // 'boundary-space' | 'cast' | 'castable' | 'construction' | 'context' |
                                    // 'copy-namespaces' | 'decimal-format' | 'default' | 'div' | 'eq' | 'except' |
                                    // 'function' | 'ge' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' |
                                    // 'lt' | 'mod' | 'namespace' | 'ne' | 'option' | 'or' | 'ordering' | 'to' |
                                    // 'treat' | 'union' | 'variable' | '|' | '||'
        break;
      case 123:                     // 'import'
        lookahead2W(129);           // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | 'and' | 'cast' | 'castable' |
                                    // 'div' | 'eq' | 'except' | 'ge' | 'gt' | 'idiv' | 'instance' | 'intersect' |
                                    // 'is' | 'le' | 'lt' | 'mod' | 'module' | 'ne' | 'or' | 'schema' | 'to' | 'treat' |
                                    // 'union' | '|' | '||'
        break;
      default:
        lk = l1;
      }
      if (lk != 19805               // 'declare' 'base-uri'
       && lk != 20061               // 'declare' 'boundary-space'
       && lk != 22365               // 'declare' 'construction'
       && lk != 22877               // 'declare' 'copy-namespaces'
       && lk != 23389               // 'declare' 'decimal-format'
       && lk != 24157               // 'declare' 'default'
       && lk != 35451               // 'import' 'module'
       && lk != 35677               // 'declare' 'namespace'
       && lk != 39005               // 'declare' 'ordering'
       && lk != 42107)              // 'import' 'schema'
      {
        break;
      }
      switch (l1)
      {
      case 93:                      // 'declare'
        lookahead2W(113);           // S^WS | '(:' | 'base-uri' | 'boundary-space' | 'construction' |
                                    // 'copy-namespaces' | 'decimal-format' | 'default' | 'namespace' | 'ordering'
        break;
      default:
        lk = l1;
      }
      if (lk == 24157)              // 'declare' 'default'
      {
        lk = memoized(0, e0);
        if (lk == 0)
        {
          int b0A = b0; int e0A = e0; int l1A = l1;
          int b1A = b1; int e1A = e1; int l2A = l2;
          int b2A = b2; int e2A = e2;
          try
          {
            try_DefaultNamespaceDecl();
            lk = -1;
          }
          catch (ParseException p1A)
          {
            lk = -2;
          }
          b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
          b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
          b2 = b2A; e2 = e2A; end = e2A; }}
          memoize(0, e0, lk);
        }
      }
      switch (lk)
      {
      case -1:
        whitespace();
        parse_DefaultNamespaceDecl();
        break;
      case 35677:                   // 'declare' 'namespace'
        whitespace();
        parse_NamespaceDecl();
        break;
      case 123:                     // 'import'
        whitespace();
        parse_Import();
        break;
      default:
        whitespace();
        parse_Setter();
      }
      lookahead1W(28);              // S^WS | '(:' | ';'
      whitespace();
      parse_Separator();
    }
    for (;;)
    {
      lookahead1W(167);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | EOF | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      switch (l1)
      {
      case 93:                      // 'declare'
        lookahead2W(130);           // S^WS | EOF | '!' | '!=' | '#' | '%' | '(' | '(:' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | 'and' | 'cast' |
                                    // 'castable' | 'context' | 'div' | 'eq' | 'except' | 'function' | 'ge' | 'gt' |
                                    // 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'lt' | 'mod' | 'ne' |
                                    // 'option' | 'or' | 'to' | 'treat' | 'union' | 'variable' | '|' | '||'
        break;
      default:
        lk = l1;
      }
      if (lk != 8029                // 'declare' '%'
       && lk != 22621               // 'declare' 'context'
       && lk != 29533               // 'declare' 'function'
       && lk != 37981               // 'declare' 'option'
       && lk != 47709)              // 'declare' 'variable'
      {
        break;
      }
      switch (l1)
      {
      case 93:                      // 'declare'
        lookahead2W(109);           // S^WS | '%' | '(:' | 'context' | 'function' | 'option' | 'variable'
        break;
      default:
        lk = l1;
      }
      switch (lk)
      {
      case 22621:                   // 'declare' 'context'
        whitespace();
        parse_ContextItemDecl();
        break;
      case 37981:                   // 'declare' 'option'
        whitespace();
        parse_OptionDecl();
        break;
      default:
        whitespace();
        parse_AnnotatedDecl();
      }
      lookahead1W(28);              // S^WS | '(:' | ';'
      whitespace();
      parse_Separator();
    }
    eventHandler.endNonterminal("Prolog", e0);
  }

  private void parse_Separator()
  {
    eventHandler.startNonterminal("Separator", e0);
    shift(50);                      // ';'
    eventHandler.endNonterminal("Separator", e0);
  }

  private void parse_Setter()
  {
    eventHandler.startNonterminal("Setter", e0);
    switch (l1)
    {
    case 93:                        // 'declare'
      lookahead2W(112);             // S^WS | '(:' | 'base-uri' | 'boundary-space' | 'construction' |
                                    // 'copy-namespaces' | 'decimal-format' | 'default' | 'ordering'
      break;
    default:
      lk = l1;
    }
    if (lk == 24157)                // 'declare' 'default'
    {
      lk = memoized(1, e0);
      if (lk == 0)
      {
        int b0A = b0; int e0A = e0; int l1A = l1;
        int b1A = b1; int e1A = e1; int l2A = l2;
        int b2A = b2; int e2A = e2;
        try
        {
          try_DefaultCollationDecl();
          lk = -2;
        }
        catch (ParseException p2A)
        {
          try
          {
            b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
            b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
            b2 = b2A; e2 = e2A; end = e2A; }}
            try_EmptyOrderDecl();
            lk = -6;
          }
          catch (ParseException p6A)
          {
            lk = -8;
          }
        }
        b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
        b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
        b2 = b2A; e2 = e2A; end = e2A; }}
        memoize(1, e0, lk);
      }
    }
    switch (lk)
    {
    case 20061:                     // 'declare' 'boundary-space'
      parse_BoundarySpaceDecl();
      break;
    case -2:
      parse_DefaultCollationDecl();
      break;
    case 19805:                     // 'declare' 'base-uri'
      parse_BaseURIDecl();
      break;
    case 22365:                     // 'declare' 'construction'
      parse_ConstructionDecl();
      break;
    case 39005:                     // 'declare' 'ordering'
      parse_OrderingModeDecl();
      break;
    case -6:
      parse_EmptyOrderDecl();
      break;
    case 22877:                     // 'declare' 'copy-namespaces'
      parse_CopyNamespacesDecl();
      break;
    default:
      parse_DecimalFormatDecl();
    }
    eventHandler.endNonterminal("Setter", e0);
  }

  private void parse_BoundarySpaceDecl()
  {
    eventHandler.startNonterminal("BoundarySpaceDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(32);                // S^WS | '(:' | 'boundary-space'
    shift(78);                      // 'boundary-space'
    lookahead1W(88);                // S^WS | '(:' | 'preserve' | 'strip'
    switch (l1)
    {
    case 159:                       // 'preserve'
      shift(159);                   // 'preserve'
      break;
    default:
      shift(173);                   // 'strip'
    }
    eventHandler.endNonterminal("BoundarySpaceDecl", e0);
  }

  private void parse_DefaultCollationDecl()
  {
    eventHandler.startNonterminal("DefaultCollationDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(41);                // S^WS | '(:' | 'default'
    shift(94);                      // 'default'
    lookahead1W(36);                // S^WS | '(:' | 'collation'
    shift(85);                      // 'collation'
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    whitespace();
    parse_URILiteral();
    eventHandler.endNonterminal("DefaultCollationDecl", e0);
  }

  private void try_DefaultCollationDecl()
  {
    shiftT(93);                     // 'declare'
    lookahead1W(41);                // S^WS | '(:' | 'default'
    shiftT(94);                     // 'default'
    lookahead1W(36);                // S^WS | '(:' | 'collation'
    shiftT(85);                     // 'collation'
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    try_URILiteral();
  }

  private void parse_BaseURIDecl()
  {
    eventHandler.startNonterminal("BaseURIDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(31);                // S^WS | '(:' | 'base-uri'
    shift(77);                      // 'base-uri'
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    whitespace();
    parse_URILiteral();
    eventHandler.endNonterminal("BaseURIDecl", e0);
  }

  private void parse_ConstructionDecl()
  {
    eventHandler.startNonterminal("ConstructionDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(37);                // S^WS | '(:' | 'construction'
    shift(87);                      // 'construction'
    lookahead1W(88);                // S^WS | '(:' | 'preserve' | 'strip'
    switch (l1)
    {
    case 173:                       // 'strip'
      shift(173);                   // 'strip'
      break;
    default:
      shift(159);                   // 'preserve'
    }
    eventHandler.endNonterminal("ConstructionDecl", e0);
  }

  private void parse_OrderingModeDecl()
  {
    eventHandler.startNonterminal("OrderingModeDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(52);                // S^WS | '(:' | 'ordering'
    shift(152);                     // 'ordering'
    lookahead1W(87);                // S^WS | '(:' | 'ordered' | 'unordered'
    switch (l1)
    {
    case 151:                       // 'ordered'
      shift(151);                   // 'ordered'
      break;
    default:
      shift(184);                   // 'unordered'
    }
    eventHandler.endNonterminal("OrderingModeDecl", e0);
  }

  private void parse_EmptyOrderDecl()
  {
    eventHandler.startNonterminal("EmptyOrderDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(41);                // S^WS | '(:' | 'default'
    shift(94);                      // 'default'
    lookahead1W(51);                // S^WS | '(:' | 'order'
    shift(150);                     // 'order'
    lookahead1W(43);                // S^WS | '(:' | 'empty'
    shift(104);                     // 'empty'
    lookahead1W(82);                // S^WS | '(:' | 'greatest' | 'least'
    switch (l1)
    {
    case 117:                       // 'greatest'
      shift(117);                   // 'greatest'
      break;
    default:
      shift(133);                   // 'least'
    }
    eventHandler.endNonterminal("EmptyOrderDecl", e0);
  }

  private void try_EmptyOrderDecl()
  {
    shiftT(93);                     // 'declare'
    lookahead1W(41);                // S^WS | '(:' | 'default'
    shiftT(94);                     // 'default'
    lookahead1W(51);                // S^WS | '(:' | 'order'
    shiftT(150);                    // 'order'
    lookahead1W(43);                // S^WS | '(:' | 'empty'
    shiftT(104);                    // 'empty'
    lookahead1W(82);                // S^WS | '(:' | 'greatest' | 'least'
    switch (l1)
    {
    case 117:                       // 'greatest'
      shiftT(117);                  // 'greatest'
      break;
    default:
      shiftT(133);                  // 'least'
    }
  }

  private void parse_CopyNamespacesDecl()
  {
    eventHandler.startNonterminal("CopyNamespacesDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(39);                // S^WS | '(:' | 'copy-namespaces'
    shift(89);                      // 'copy-namespaces'
    lookahead1W(86);                // S^WS | '(:' | 'no-preserve' | 'preserve'
    whitespace();
    parse_PreserveMode();
    lookahead1W(25);                // S^WS | '(:' | ','
    shift(39);                      // ','
    lookahead1W(83);                // S^WS | '(:' | 'inherit' | 'no-inherit'
    whitespace();
    parse_InheritMode();
    eventHandler.endNonterminal("CopyNamespacesDecl", e0);
  }

  private void parse_PreserveMode()
  {
    eventHandler.startNonterminal("PreserveMode", e0);
    switch (l1)
    {
    case 159:                       // 'preserve'
      shift(159);                   // 'preserve'
      break;
    default:
      shift(144);                   // 'no-preserve'
    }
    eventHandler.endNonterminal("PreserveMode", e0);
  }

  private void parse_InheritMode()
  {
    eventHandler.startNonterminal("InheritMode", e0);
    switch (l1)
    {
    case 126:                       // 'inherit'
      shift(126);                   // 'inherit'
      break;
    default:
      shift(143);                   // 'no-inherit'
    }
    eventHandler.endNonterminal("InheritMode", e0);
  }

  private void parse_DecimalFormatDecl()
  {
    eventHandler.startNonterminal("DecimalFormatDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(78);                // S^WS | '(:' | 'decimal-format' | 'default'
    switch (l1)
    {
    case 91:                        // 'decimal-format'
      shift(91);                    // 'decimal-format'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_EQName();
      break;
    default:
      shift(94);                    // 'default'
      lookahead1W(40);              // S^WS | '(:' | 'decimal-format'
      shift(91);                    // 'decimal-format'
    }
    for (;;)
    {
      lookahead1W(119);             // S^WS | '(:' | ';' | 'NaN' | 'decimal-separator' | 'digit' |
                                    // 'grouping-separator' | 'infinity' | 'minus-sign' | 'pattern-separator' |
                                    // 'per-mille' | 'percent' | 'zero-digit'
      if (l1 == 50)                 // ';'
      {
        break;
      }
      whitespace();
      parse_DFPropertyName();
      lookahead1W(29);              // S^WS | '(:' | '='
      shift(58);                    // '='
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      shift(4);                     // StringLiteral
    }
    eventHandler.endNonterminal("DecimalFormatDecl", e0);
  }

  private void parse_DFPropertyName()
  {
    eventHandler.startNonterminal("DFPropertyName", e0);
    switch (l1)
    {
    case 92:                        // 'decimal-separator'
      shift(92);                    // 'decimal-separator'
      break;
    case 119:                       // 'grouping-separator'
      shift(119);                   // 'grouping-separator'
      break;
    case 125:                       // 'infinity'
      shift(125);                   // 'infinity'
      break;
    case 136:                       // 'minus-sign'
      shift(136);                   // 'minus-sign'
      break;
    case 65:                        // 'NaN'
      shift(65);                    // 'NaN'
      break;
    case 156:                       // 'percent'
      shift(156);                   // 'percent'
      break;
    case 155:                       // 'per-mille'
      shift(155);                   // 'per-mille'
      break;
    case 192:                       // 'zero-digit'
      shift(192);                   // 'zero-digit'
      break;
    case 98:                        // 'digit'
      shift(98);                    // 'digit'
      break;
    default:
      shift(154);                   // 'pattern-separator'
    }
    eventHandler.endNonterminal("DFPropertyName", e0);
  }

  private void parse_Import()
  {
    eventHandler.startNonterminal("Import", e0);
    switch (l1)
    {
    case 123:                       // 'import'
      lookahead2W(84);              // S^WS | '(:' | 'module' | 'schema'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 42107:                     // 'import' 'schema'
      parse_SchemaImport();
      break;
    default:
      parse_ModuleImport();
    }
    eventHandler.endNonterminal("Import", e0);
  }

  private void parse_SchemaImport()
  {
    eventHandler.startNonterminal("SchemaImport", e0);
    shift(123);                     // 'import'
    lookahead1W(54);                // S^WS | '(:' | 'schema'
    shift(164);                     // 'schema'
    lookahead1W(92);                // StringLiteral | S^WS | '(:' | 'default' | 'namespace'
    if (l1 != 4)                    // StringLiteral
    {
      whitespace();
      parse_SchemaPrefix();
    }
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    whitespace();
    parse_URILiteral();
    lookahead1W(73);                // S^WS | '(:' | ';' | 'at'
    if (l1 == 75)                   // 'at'
    {
      shift(75);                    // 'at'
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      whitespace();
      parse_URILiteral();
      for (;;)
      {
        lookahead1W(69);            // S^WS | '(:' | ',' | ';'
        if (l1 != 39)               // ','
        {
          break;
        }
        shift(39);                  // ','
        lookahead1W(17);            // StringLiteral | S^WS | '(:'
        whitespace();
        parse_URILiteral();
      }
    }
    eventHandler.endNonterminal("SchemaImport", e0);
  }

  private void parse_SchemaPrefix()
  {
    eventHandler.startNonterminal("SchemaPrefix", e0);
    switch (l1)
    {
    case 139:                       // 'namespace'
      shift(139);                   // 'namespace'
      lookahead1W(122);             // NCName^Token | S^WS | '(:' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where'
      whitespace();
      parse_NCName();
      lookahead1W(29);              // S^WS | '(:' | '='
      shift(58);                    // '='
      break;
    default:
      shift(94);                    // 'default'
      lookahead1W(42);              // S^WS | '(:' | 'element'
      shift(102);                   // 'element'
      lookahead1W(48);              // S^WS | '(:' | 'namespace'
      shift(139);                   // 'namespace'
    }
    eventHandler.endNonterminal("SchemaPrefix", e0);
  }

  private void parse_ModuleImport()
  {
    eventHandler.startNonterminal("ModuleImport", e0);
    shift(123);                     // 'import'
    lookahead1W(47);                // S^WS | '(:' | 'module'
    shift(138);                     // 'module'
    lookahead1W(60);                // StringLiteral | S^WS | '(:' | 'namespace'
    if (l1 == 139)                  // 'namespace'
    {
      shift(139);                   // 'namespace'
      lookahead1W(122);             // NCName^Token | S^WS | '(:' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where'
      whitespace();
      parse_NCName();
      lookahead1W(29);              // S^WS | '(:' | '='
      shift(58);                    // '='
    }
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    whitespace();
    parse_URILiteral();
    lookahead1W(73);                // S^WS | '(:' | ';' | 'at'
    if (l1 == 75)                   // 'at'
    {
      shift(75);                    // 'at'
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      whitespace();
      parse_URILiteral();
      for (;;)
      {
        lookahead1W(69);            // S^WS | '(:' | ',' | ';'
        if (l1 != 39)               // ','
        {
          break;
        }
        shift(39);                  // ','
        lookahead1W(17);            // StringLiteral | S^WS | '(:'
        whitespace();
        parse_URILiteral();
      }
    }
    eventHandler.endNonterminal("ModuleImport", e0);
  }

  private void parse_NamespaceDecl()
  {
    eventHandler.startNonterminal("NamespaceDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(48);                // S^WS | '(:' | 'namespace'
    shift(139);                     // 'namespace'
    lookahead1W(122);               // NCName^Token | S^WS | '(:' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where'
    whitespace();
    parse_NCName();
    lookahead1W(29);                // S^WS | '(:' | '='
    shift(58);                      // '='
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    whitespace();
    parse_URILiteral();
    eventHandler.endNonterminal("NamespaceDecl", e0);
  }

  private void parse_DefaultNamespaceDecl()
  {
    eventHandler.startNonterminal("DefaultNamespaceDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(41);                // S^WS | '(:' | 'default'
    shift(94);                      // 'default'
    lookahead1W(79);                // S^WS | '(:' | 'element' | 'function'
    switch (l1)
    {
    case 102:                       // 'element'
      shift(102);                   // 'element'
      break;
    default:
      shift(115);                   // 'function'
    }
    lookahead1W(48);                // S^WS | '(:' | 'namespace'
    shift(139);                     // 'namespace'
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    whitespace();
    parse_URILiteral();
    eventHandler.endNonterminal("DefaultNamespaceDecl", e0);
  }

  private void try_DefaultNamespaceDecl()
  {
    shiftT(93);                     // 'declare'
    lookahead1W(41);                // S^WS | '(:' | 'default'
    shiftT(94);                     // 'default'
    lookahead1W(79);                // S^WS | '(:' | 'element' | 'function'
    switch (l1)
    {
    case 102:                       // 'element'
      shiftT(102);                  // 'element'
      break;
    default:
      shiftT(115);                  // 'function'
    }
    lookahead1W(48);                // S^WS | '(:' | 'namespace'
    shiftT(139);                    // 'namespace'
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    try_URILiteral();
  }

  private void parse_AnnotatedDecl()
  {
    eventHandler.startNonterminal("AnnotatedDecl", e0);
    shift(93);                      // 'declare'
    for (;;)
    {
      lookahead1W(96);              // S^WS | '%' | '(:' | 'function' | 'variable'
      if (l1 != 31)                 // '%'
      {
        break;
      }
      whitespace();
      parse_Annotation();
    }
    switch (l1)
    {
    case 186:                       // 'variable'
      whitespace();
      parse_VarDecl();
      break;
    default:
      whitespace();
      parse_FunctionDecl();
    }
    eventHandler.endNonterminal("AnnotatedDecl", e0);
  }

  private void parse_Annotation()
  {
    eventHandler.startNonterminal("Annotation", e0);
    shift(31);                      // '%'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_EQName();
    lookahead1W(104);               // S^WS | '%' | '(' | '(:' | 'function' | 'variable'
    if (l1 == 33)                   // '('
    {
      shift(33);                    // '('
      lookahead1W(103);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral | S^WS | '(:'
      whitespace();
      parse_Literal();
      for (;;)
      {
        lookahead1W(67);            // S^WS | '(:' | ')' | ','
        if (l1 != 39)               // ','
        {
          break;
        }
        shift(39);                  // ','
        lookahead1W(103);           // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral | S^WS | '(:'
        whitespace();
        parse_Literal();
      }
      shift(36);                    // ')'
    }
    eventHandler.endNonterminal("Annotation", e0);
  }

  private void try_Annotation()
  {
    shiftT(31);                     // '%'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_EQName();
    lookahead1W(104);               // S^WS | '%' | '(' | '(:' | 'function' | 'variable'
    if (l1 == 33)                   // '('
    {
      shiftT(33);                   // '('
      lookahead1W(103);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral | S^WS | '(:'
      try_Literal();
      for (;;)
      {
        lookahead1W(67);            // S^WS | '(:' | ')' | ','
        if (l1 != 39)               // ','
        {
          break;
        }
        shiftT(39);                 // ','
        lookahead1W(103);           // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral | S^WS | '(:'
        try_Literal();
      }
      shiftT(36);                   // ')'
    }
  }

  private void parse_VarDecl()
  {
    eventHandler.startNonterminal("VarDecl", e0);
    shift(186);                     // 'variable'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    lookahead1W(99);                // S^WS | '(:' | ':=' | 'as' | 'external'
    if (l1 == 73)                   // 'as'
    {
      whitespace();
      parse_TypeDeclaration();
    }
    lookahead1W(72);                // S^WS | '(:' | ':=' | 'external'
    switch (l1)
    {
    case 49:                        // ':='
      shift(49);                    // ':='
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_VarValue();
      break;
    default:
      shift(111);                   // 'external'
      lookahead1W(70);              // S^WS | '(:' | ':=' | ';'
      if (l1 == 49)                 // ':='
      {
        shift(49);                  // ':='
        lookahead1W(166);           // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
        whitespace();
        parse_VarDefaultValue();
      }
    }
    eventHandler.endNonterminal("VarDecl", e0);
  }

  private void parse_VarValue()
  {
    eventHandler.startNonterminal("VarValue", e0);
    parse_ExprSingle();
    eventHandler.endNonterminal("VarValue", e0);
  }

  private void parse_VarDefaultValue()
  {
    eventHandler.startNonterminal("VarDefaultValue", e0);
    parse_ExprSingle();
    eventHandler.endNonterminal("VarDefaultValue", e0);
  }

  private void parse_ContextItemDecl()
  {
    eventHandler.startNonterminal("ContextItemDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(38);                // S^WS | '(:' | 'context'
    shift(88);                      // 'context'
    lookahead1W(46);                // S^WS | '(:' | 'item'
    shift(130);                     // 'item'
    lookahead1W(99);                // S^WS | '(:' | ':=' | 'as' | 'external'
    if (l1 == 73)                   // 'as'
    {
      shift(73);                    // 'as'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_ItemType();
    }
    lookahead1W(72);                // S^WS | '(:' | ':=' | 'external'
    switch (l1)
    {
    case 49:                        // ':='
      shift(49);                    // ':='
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_VarValue();
      break;
    default:
      shift(111);                   // 'external'
      lookahead1W(70);              // S^WS | '(:' | ':=' | ';'
      if (l1 == 49)                 // ':='
      {
        shift(49);                  // ':='
        lookahead1W(166);           // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
        whitespace();
        parse_VarDefaultValue();
      }
    }
    eventHandler.endNonterminal("ContextItemDecl", e0);
  }

  private void parse_FunctionDecl()
  {
    eventHandler.startNonterminal("FunctionDecl", e0);
    shift(115);                     // 'function'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_EQName();
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(63);                // S^WS | '$' | '(:' | ')'
    if (l1 == 30)                   // '$'
    {
      whitespace();
      parse_ParamList();
    }
    shift(36);                      // ')'
    lookahead1W(101);               // S^WS | '(:' | 'as' | 'external' | '{'
    if (l1 == 73)                   // 'as'
    {
      shift(73);                    // 'as'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_SequenceType();
    }
    lookahead1W(81);                // S^WS | '(:' | 'external' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      whitespace();
      parse_FunctionBody();
      break;
    default:
      shift(111);                   // 'external'
    }
    eventHandler.endNonterminal("FunctionDecl", e0);
  }

  private void parse_ParamList()
  {
    eventHandler.startNonterminal("ParamList", e0);
    parse_Param();
    for (;;)
    {
      lookahead1W(67);              // S^WS | '(:' | ')' | ','
      if (l1 != 39)                 // ','
      {
        break;
      }
      shift(39);                    // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      whitespace();
      parse_Param();
    }
    eventHandler.endNonterminal("ParamList", e0);
  }

  private void try_ParamList()
  {
    try_Param();
    for (;;)
    {
      lookahead1W(67);              // S^WS | '(:' | ')' | ','
      if (l1 != 39)                 // ','
      {
        break;
      }
      shiftT(39);                   // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      try_Param();
    }
  }

  private void parse_Param()
  {
    eventHandler.startNonterminal("Param", e0);
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_EQName();
    lookahead1W(97);                // S^WS | '(:' | ')' | ',' | 'as'
    if (l1 == 73)                   // 'as'
    {
      whitespace();
      parse_TypeDeclaration();
    }
    eventHandler.endNonterminal("Param", e0);
  }

  private void try_Param()
  {
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_EQName();
    lookahead1W(97);                // S^WS | '(:' | ')' | ',' | 'as'
    if (l1 == 73)                   // 'as'
    {
      try_TypeDeclaration();
    }
  }

  private void parse_FunctionBody()
  {
    eventHandler.startNonterminal("FunctionBody", e0);
    parse_EnclosedExpr();
    eventHandler.endNonterminal("FunctionBody", e0);
  }

  private void try_FunctionBody()
  {
    try_EnclosedExpr();
  }

  private void parse_EnclosedExpr()
  {
    eventHandler.startNonterminal("EnclosedExpr", e0);
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("EnclosedExpr", e0);
  }

  private void try_EnclosedExpr()
  {
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(197);                    // '}'
  }

  private void parse_OptionDecl()
  {
    eventHandler.startNonterminal("OptionDecl", e0);
    shift(93);                      // 'declare'
    lookahead1W(50);                // S^WS | '(:' | 'option'
    shift(148);                     // 'option'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_EQName();
    lookahead1W(17);                // StringLiteral | S^WS | '(:'
    shift(4);                       // StringLiteral
    eventHandler.endNonterminal("OptionDecl", e0);
  }

  private void parse_QueryBody()
  {
    eventHandler.startNonterminal("QueryBody", e0);
    parse_Expr();
    eventHandler.endNonterminal("QueryBody", e0);
  }

  private void parse_Expr()
  {
    eventHandler.startNonterminal("Expr", e0);
    parse_ExprSingle();
    for (;;)
    {
      if (l1 != 39)                 // ','
      {
        break;
      }
      shift(39);                    // ','
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_ExprSingle();
    }
    eventHandler.endNonterminal("Expr", e0);
  }

  private void try_Expr()
  {
    try_ExprSingle();
    for (;;)
    {
      if (l1 != 39)                 // ','
      {
        break;
      }
      shiftT(39);                   // ','
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_ExprSingle();
    }
  }

  private void parse_ExprSingle()
  {
    eventHandler.startNonterminal("ExprSingle", e0);
    switch (l1)
    {
    case 114:                       // 'for'
      lookahead2W(150);             // S^WS | EOF | '!' | '!=' | '#' | '$' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' |
                                    // '/' | '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' |
                                    // 'and' | 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' |
                                    // 'default' | 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' |
                                    // 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' |
                                    // 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'sliding' | 'stable' | 'start' | 'to' | 'treat' | 'tumbling' | 'union' |
                                    // 'where' | '|' | '||' | '}'
      break;
    case 179:                       // 'try'
      lookahead2W(148);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '{' | '|' | '||' | '}'
      break;
    case 109:                       // 'every'
    case 134:                       // 'let'
    case 169:                       // 'some'
      lookahead2W(146);             // S^WS | EOF | '!' | '!=' | '#' | '$' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' |
                                    // '/' | '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' |
                                    // 'and' | 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' |
                                    // 'default' | 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' |
                                    // 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' |
                                    // 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    case 122:                       // 'if'
    case 174:                       // 'switch'
    case 182:                       // 'typeswitch'
      lookahead2W(144);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 7794:                      // 'for' '$'
    case 7814:                      // 'let' '$'
    case 43122:                     // 'for' 'sliding'
    case 46194:                     // 'for' 'tumbling'
      parse_FLWORExpr();
      break;
    case 7789:                      // 'every' '$'
    case 7849:                      // 'some' '$'
      parse_QuantifiedExpr();
      break;
    case 8622:                      // 'switch' '('
      parse_SwitchExpr();
      break;
    case 8630:                      // 'typeswitch' '('
      parse_TypeswitchExpr();
      break;
    case 8570:                      // 'if' '('
      parse_IfExpr();
      break;
    case 49587:                     // 'try' '{'
      parse_TryCatchExpr();
      break;
    default:
      parse_OrExpr();
    }
    eventHandler.endNonterminal("ExprSingle", e0);
  }

  private void try_ExprSingle()
  {
    switch (l1)
    {
    case 114:                       // 'for'
      lookahead2W(150);             // S^WS | EOF | '!' | '!=' | '#' | '$' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' |
                                    // '/' | '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' |
                                    // 'and' | 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' |
                                    // 'default' | 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' |
                                    // 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' |
                                    // 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'sliding' | 'stable' | 'start' | 'to' | 'treat' | 'tumbling' | 'union' |
                                    // 'where' | '|' | '||' | '}'
      break;
    case 179:                       // 'try'
      lookahead2W(148);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '{' | '|' | '||' | '}'
      break;
    case 109:                       // 'every'
    case 134:                       // 'let'
    case 169:                       // 'some'
      lookahead2W(146);             // S^WS | EOF | '!' | '!=' | '#' | '$' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' |
                                    // '/' | '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' |
                                    // 'and' | 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' |
                                    // 'default' | 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' |
                                    // 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' |
                                    // 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    case 122:                       // 'if'
    case 174:                       // 'switch'
    case 182:                       // 'typeswitch'
      lookahead2W(144);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 7794:                      // 'for' '$'
    case 7814:                      // 'let' '$'
    case 43122:                     // 'for' 'sliding'
    case 46194:                     // 'for' 'tumbling'
      try_FLWORExpr();
      break;
    case 7789:                      // 'every' '$'
    case 7849:                      // 'some' '$'
      try_QuantifiedExpr();
      break;
    case 8622:                      // 'switch' '('
      try_SwitchExpr();
      break;
    case 8630:                      // 'typeswitch' '('
      try_TypeswitchExpr();
      break;
    case 8570:                      // 'if' '('
      try_IfExpr();
      break;
    case 49587:                     // 'try' '{'
      try_TryCatchExpr();
      break;
    default:
      try_OrExpr();
    }
  }

  private void parse_FLWORExpr()
  {
    eventHandler.startNonterminal("FLWORExpr", e0);
    parse_InitialClause();
    for (;;)
    {
      lookahead1W(114);             // S^WS | '(:' | 'count' | 'for' | 'group' | 'let' | 'order' | 'return' | 'stable' |
                                    // 'where'
      if (l1 == 162)                // 'return'
      {
        break;
      }
      whitespace();
      parse_IntermediateClause();
    }
    whitespace();
    parse_ReturnClause();
    eventHandler.endNonterminal("FLWORExpr", e0);
  }

  private void try_FLWORExpr()
  {
    try_InitialClause();
    for (;;)
    {
      lookahead1W(114);             // S^WS | '(:' | 'count' | 'for' | 'group' | 'let' | 'order' | 'return' | 'stable' |
                                    // 'where'
      if (l1 == 162)                // 'return'
      {
        break;
      }
      try_IntermediateClause();
    }
    try_ReturnClause();
  }

  private void parse_InitialClause()
  {
    eventHandler.startNonterminal("InitialClause", e0);
    switch (l1)
    {
    case 114:                       // 'for'
      lookahead2W(95);              // S^WS | '$' | '(:' | 'sliding' | 'tumbling'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 7794:                      // 'for' '$'
      parse_ForClause();
      break;
    case 134:                       // 'let'
      parse_LetClause();
      break;
    default:
      parse_WindowClause();
    }
    eventHandler.endNonterminal("InitialClause", e0);
  }

  private void try_InitialClause()
  {
    switch (l1)
    {
    case 114:                       // 'for'
      lookahead2W(95);              // S^WS | '$' | '(:' | 'sliding' | 'tumbling'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 7794:                      // 'for' '$'
      try_ForClause();
      break;
    case 134:                       // 'let'
      try_LetClause();
      break;
    default:
      try_WindowClause();
    }
  }

  private void parse_IntermediateClause()
  {
    eventHandler.startNonterminal("IntermediateClause", e0);
    switch (l1)
    {
    case 114:                       // 'for'
    case 134:                       // 'let'
      parse_InitialClause();
      break;
    case 189:                       // 'where'
      parse_WhereClause();
      break;
    case 118:                       // 'group'
      parse_GroupByClause();
      break;
    case 90:                        // 'count'
      parse_CountClause();
      break;
    default:
      parse_OrderByClause();
    }
    eventHandler.endNonterminal("IntermediateClause", e0);
  }

  private void try_IntermediateClause()
  {
    switch (l1)
    {
    case 114:                       // 'for'
    case 134:                       // 'let'
      try_InitialClause();
      break;
    case 189:                       // 'where'
      try_WhereClause();
      break;
    case 118:                       // 'group'
      try_GroupByClause();
      break;
    case 90:                        // 'count'
      try_CountClause();
      break;
    default:
      try_OrderByClause();
    }
  }

  private void parse_ForClause()
  {
    eventHandler.startNonterminal("ForClause", e0);
    shift(114);                     // 'for'
    lookahead1W(21);                // S^WS | '$' | '(:'
    whitespace();
    parse_ForBinding();
    for (;;)
    {
      if (l1 != 39)                 // ','
      {
        break;
      }
      shift(39);                    // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      whitespace();
      parse_ForBinding();
    }
    eventHandler.endNonterminal("ForClause", e0);
  }

  private void try_ForClause()
  {
    shiftT(114);                    // 'for'
    lookahead1W(21);                // S^WS | '$' | '(:'
    try_ForBinding();
    for (;;)
    {
      if (l1 != 39)                 // ','
      {
        break;
      }
      shiftT(39);                   // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      try_ForBinding();
    }
  }

  private void parse_ForBinding()
  {
    eventHandler.startNonterminal("ForBinding", e0);
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    lookahead1W(105);               // S^WS | '(:' | 'allowing' | 'as' | 'at' | 'in'
    if (l1 == 73)                   // 'as'
    {
      whitespace();
      parse_TypeDeclaration();
    }
    lookahead1W(100);               // S^WS | '(:' | 'allowing' | 'at' | 'in'
    if (l1 == 69)                   // 'allowing'
    {
      whitespace();
      parse_AllowingEmpty();
    }
    lookahead1W(77);                // S^WS | '(:' | 'at' | 'in'
    if (l1 == 75)                   // 'at'
    {
      whitespace();
      parse_PositionalVar();
    }
    lookahead1W(45);                // S^WS | '(:' | 'in'
    shift(124);                     // 'in'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("ForBinding", e0);
  }

  private void try_ForBinding()
  {
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
    lookahead1W(105);               // S^WS | '(:' | 'allowing' | 'as' | 'at' | 'in'
    if (l1 == 73)                   // 'as'
    {
      try_TypeDeclaration();
    }
    lookahead1W(100);               // S^WS | '(:' | 'allowing' | 'at' | 'in'
    if (l1 == 69)                   // 'allowing'
    {
      try_AllowingEmpty();
    }
    lookahead1W(77);                // S^WS | '(:' | 'at' | 'in'
    if (l1 == 75)                   // 'at'
    {
      try_PositionalVar();
    }
    lookahead1W(45);                // S^WS | '(:' | 'in'
    shiftT(124);                    // 'in'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_AllowingEmpty()
  {
    eventHandler.startNonterminal("AllowingEmpty", e0);
    shift(69);                      // 'allowing'
    lookahead1W(43);                // S^WS | '(:' | 'empty'
    shift(104);                     // 'empty'
    eventHandler.endNonterminal("AllowingEmpty", e0);
  }

  private void try_AllowingEmpty()
  {
    shiftT(69);                     // 'allowing'
    lookahead1W(43);                // S^WS | '(:' | 'empty'
    shiftT(104);                    // 'empty'
  }

  private void parse_PositionalVar()
  {
    eventHandler.startNonterminal("PositionalVar", e0);
    shift(75);                      // 'at'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    eventHandler.endNonterminal("PositionalVar", e0);
  }

  private void try_PositionalVar()
  {
    shiftT(75);                     // 'at'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
  }

  private void parse_LetClause()
  {
    eventHandler.startNonterminal("LetClause", e0);
    shift(134);                     // 'let'
    lookahead1W(21);                // S^WS | '$' | '(:'
    whitespace();
    parse_LetBinding();
    for (;;)
    {
      if (l1 != 39)                 // ','
      {
        break;
      }
      shift(39);                    // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      whitespace();
      parse_LetBinding();
    }
    eventHandler.endNonterminal("LetClause", e0);
  }

  private void try_LetClause()
  {
    shiftT(134);                    // 'let'
    lookahead1W(21);                // S^WS | '$' | '(:'
    try_LetBinding();
    for (;;)
    {
      if (l1 != 39)                 // ','
      {
        break;
      }
      shiftT(39);                   // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      try_LetBinding();
    }
  }

  private void parse_LetBinding()
  {
    eventHandler.startNonterminal("LetBinding", e0);
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    lookahead1W(71);                // S^WS | '(:' | ':=' | 'as'
    if (l1 == 73)                   // 'as'
    {
      whitespace();
      parse_TypeDeclaration();
    }
    lookahead1W(27);                // S^WS | '(:' | ':='
    shift(49);                      // ':='
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("LetBinding", e0);
  }

  private void try_LetBinding()
  {
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
    lookahead1W(71);                // S^WS | '(:' | ':=' | 'as'
    if (l1 == 73)                   // 'as'
    {
      try_TypeDeclaration();
    }
    lookahead1W(27);                // S^WS | '(:' | ':='
    shiftT(49);                     // ':='
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_WindowClause()
  {
    eventHandler.startNonterminal("WindowClause", e0);
    shift(114);                     // 'for'
    lookahead1W(90);                // S^WS | '(:' | 'sliding' | 'tumbling'
    switch (l1)
    {
    case 180:                       // 'tumbling'
      whitespace();
      parse_TumblingWindowClause();
      break;
    default:
      whitespace();
      parse_SlidingWindowClause();
    }
    eventHandler.endNonterminal("WindowClause", e0);
  }

  private void try_WindowClause()
  {
    shiftT(114);                    // 'for'
    lookahead1W(90);                // S^WS | '(:' | 'sliding' | 'tumbling'
    switch (l1)
    {
    case 180:                       // 'tumbling'
      try_TumblingWindowClause();
      break;
    default:
      try_SlidingWindowClause();
    }
  }

  private void parse_TumblingWindowClause()
  {
    eventHandler.startNonterminal("TumblingWindowClause", e0);
    shift(180);                     // 'tumbling'
    lookahead1W(57);                // S^WS | '(:' | 'window'
    shift(190);                     // 'window'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    lookahead1W(75);                // S^WS | '(:' | 'as' | 'in'
    if (l1 == 73)                   // 'as'
    {
      whitespace();
      parse_TypeDeclaration();
    }
    lookahead1W(45);                // S^WS | '(:' | 'in'
    shift(124);                     // 'in'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    whitespace();
    parse_WindowStartCondition();
    if (l1 == 107                   // 'end'
     || l1 == 147)                  // 'only'
    {
      whitespace();
      parse_WindowEndCondition();
    }
    eventHandler.endNonterminal("TumblingWindowClause", e0);
  }

  private void try_TumblingWindowClause()
  {
    shiftT(180);                    // 'tumbling'
    lookahead1W(57);                // S^WS | '(:' | 'window'
    shiftT(190);                    // 'window'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
    lookahead1W(75);                // S^WS | '(:' | 'as' | 'in'
    if (l1 == 73)                   // 'as'
    {
      try_TypeDeclaration();
    }
    lookahead1W(45);                // S^WS | '(:' | 'in'
    shiftT(124);                    // 'in'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
    try_WindowStartCondition();
    if (l1 == 107                   // 'end'
     || l1 == 147)                  // 'only'
    {
      try_WindowEndCondition();
    }
  }

  private void parse_SlidingWindowClause()
  {
    eventHandler.startNonterminal("SlidingWindowClause", e0);
    shift(168);                     // 'sliding'
    lookahead1W(57);                // S^WS | '(:' | 'window'
    shift(190);                     // 'window'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    lookahead1W(75);                // S^WS | '(:' | 'as' | 'in'
    if (l1 == 73)                   // 'as'
    {
      whitespace();
      parse_TypeDeclaration();
    }
    lookahead1W(45);                // S^WS | '(:' | 'in'
    shift(124);                     // 'in'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    whitespace();
    parse_WindowStartCondition();
    whitespace();
    parse_WindowEndCondition();
    eventHandler.endNonterminal("SlidingWindowClause", e0);
  }

  private void try_SlidingWindowClause()
  {
    shiftT(168);                    // 'sliding'
    lookahead1W(57);                // S^WS | '(:' | 'window'
    shiftT(190);                    // 'window'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
    lookahead1W(75);                // S^WS | '(:' | 'as' | 'in'
    if (l1 == 73)                   // 'as'
    {
      try_TypeDeclaration();
    }
    lookahead1W(45);                // S^WS | '(:' | 'in'
    shiftT(124);                    // 'in'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
    try_WindowStartCondition();
    try_WindowEndCondition();
  }

  private void parse_WindowStartCondition()
  {
    eventHandler.startNonterminal("WindowStartCondition", e0);
    shift(171);                     // 'start'
    lookahead1W(108);               // S^WS | '$' | '(:' | 'at' | 'next' | 'previous' | 'when'
    whitespace();
    parse_WindowVars();
    lookahead1W(56);                // S^WS | '(:' | 'when'
    shift(188);                     // 'when'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("WindowStartCondition", e0);
  }

  private void try_WindowStartCondition()
  {
    shiftT(171);                    // 'start'
    lookahead1W(108);               // S^WS | '$' | '(:' | 'at' | 'next' | 'previous' | 'when'
    try_WindowVars();
    lookahead1W(56);                // S^WS | '(:' | 'when'
    shiftT(188);                    // 'when'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_WindowEndCondition()
  {
    eventHandler.startNonterminal("WindowEndCondition", e0);
    if (l1 == 147)                  // 'only'
    {
      shift(147);                   // 'only'
    }
    lookahead1W(44);                // S^WS | '(:' | 'end'
    shift(107);                     // 'end'
    lookahead1W(108);               // S^WS | '$' | '(:' | 'at' | 'next' | 'previous' | 'when'
    whitespace();
    parse_WindowVars();
    lookahead1W(56);                // S^WS | '(:' | 'when'
    shift(188);                     // 'when'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("WindowEndCondition", e0);
  }

  private void try_WindowEndCondition()
  {
    if (l1 == 147)                  // 'only'
    {
      shiftT(147);                  // 'only'
    }
    lookahead1W(44);                // S^WS | '(:' | 'end'
    shiftT(107);                    // 'end'
    lookahead1W(108);               // S^WS | '$' | '(:' | 'at' | 'next' | 'previous' | 'when'
    try_WindowVars();
    lookahead1W(56);                // S^WS | '(:' | 'when'
    shiftT(188);                    // 'when'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_WindowVars()
  {
    eventHandler.startNonterminal("WindowVars", e0);
    if (l1 == 30)                   // '$'
    {
      shift(30);                    // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_CurrentItem();
    }
    lookahead1W(106);               // S^WS | '(:' | 'at' | 'next' | 'previous' | 'when'
    if (l1 == 75)                   // 'at'
    {
      whitespace();
      parse_PositionalVar();
    }
    lookahead1W(102);               // S^WS | '(:' | 'next' | 'previous' | 'when'
    if (l1 == 160)                  // 'previous'
    {
      shift(160);                   // 'previous'
      lookahead1W(21);              // S^WS | '$' | '(:'
      shift(30);                    // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_PreviousItem();
    }
    lookahead1W(85);                // S^WS | '(:' | 'next' | 'when'
    if (l1 == 142)                  // 'next'
    {
      shift(142);                   // 'next'
      lookahead1W(21);              // S^WS | '$' | '(:'
      shift(30);                    // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_NextItem();
    }
    eventHandler.endNonterminal("WindowVars", e0);
  }

  private void try_WindowVars()
  {
    if (l1 == 30)                   // '$'
    {
      shiftT(30);                   // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_CurrentItem();
    }
    lookahead1W(106);               // S^WS | '(:' | 'at' | 'next' | 'previous' | 'when'
    if (l1 == 75)                   // 'at'
    {
      try_PositionalVar();
    }
    lookahead1W(102);               // S^WS | '(:' | 'next' | 'previous' | 'when'
    if (l1 == 160)                  // 'previous'
    {
      shiftT(160);                  // 'previous'
      lookahead1W(21);              // S^WS | '$' | '(:'
      shiftT(30);                   // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_PreviousItem();
    }
    lookahead1W(85);                // S^WS | '(:' | 'next' | 'when'
    if (l1 == 142)                  // 'next'
    {
      shiftT(142);                  // 'next'
      lookahead1W(21);              // S^WS | '$' | '(:'
      shiftT(30);                   // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_NextItem();
    }
  }

  private void parse_CurrentItem()
  {
    eventHandler.startNonterminal("CurrentItem", e0);
    parse_EQName();
    eventHandler.endNonterminal("CurrentItem", e0);
  }

  private void try_CurrentItem()
  {
    try_EQName();
  }

  private void parse_PreviousItem()
  {
    eventHandler.startNonterminal("PreviousItem", e0);
    parse_EQName();
    eventHandler.endNonterminal("PreviousItem", e0);
  }

  private void try_PreviousItem()
  {
    try_EQName();
  }

  private void parse_NextItem()
  {
    eventHandler.startNonterminal("NextItem", e0);
    parse_EQName();
    eventHandler.endNonterminal("NextItem", e0);
  }

  private void try_NextItem()
  {
    try_EQName();
  }

  private void parse_CountClause()
  {
    eventHandler.startNonterminal("CountClause", e0);
    shift(90);                      // 'count'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    eventHandler.endNonterminal("CountClause", e0);
  }

  private void try_CountClause()
  {
    shiftT(90);                     // 'count'
    lookahead1W(21);                // S^WS | '$' | '(:'
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
  }

  private void parse_WhereClause()
  {
    eventHandler.startNonterminal("WhereClause", e0);
    shift(189);                     // 'where'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("WhereClause", e0);
  }

  private void try_WhereClause()
  {
    shiftT(189);                    // 'where'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_GroupByClause()
  {
    eventHandler.startNonterminal("GroupByClause", e0);
    shift(118);                     // 'group'
    lookahead1W(33);                // S^WS | '(:' | 'by'
    shift(79);                      // 'by'
    lookahead1W(21);                // S^WS | '$' | '(:'
    whitespace();
    parse_GroupingSpecList();
    eventHandler.endNonterminal("GroupByClause", e0);
  }

  private void try_GroupByClause()
  {
    shiftT(118);                    // 'group'
    lookahead1W(33);                // S^WS | '(:' | 'by'
    shiftT(79);                     // 'by'
    lookahead1W(21);                // S^WS | '$' | '(:'
    try_GroupingSpecList();
  }

  private void parse_GroupingSpecList()
  {
    eventHandler.startNonterminal("GroupingSpecList", e0);
    parse_GroupingSpec();
    for (;;)
    {
      lookahead1W(116);             // S^WS | '(:' | ',' | 'count' | 'for' | 'group' | 'let' | 'order' | 'return' |
                                    // 'stable' | 'where'
      if (l1 != 39)                 // ','
      {
        break;
      }
      shift(39);                    // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      whitespace();
      parse_GroupingSpec();
    }
    eventHandler.endNonterminal("GroupingSpecList", e0);
  }

  private void try_GroupingSpecList()
  {
    try_GroupingSpec();
    for (;;)
    {
      lookahead1W(116);             // S^WS | '(:' | ',' | 'count' | 'for' | 'group' | 'let' | 'order' | 'return' |
                                    // 'stable' | 'where'
      if (l1 != 39)                 // ','
      {
        break;
      }
      shiftT(39);                   // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      try_GroupingSpec();
    }
  }

  private void parse_GroupingSpec()
  {
    eventHandler.startNonterminal("GroupingSpec", e0);
    parse_GroupingVariable();
    lookahead1W(120);               // S^WS | '(:' | ',' | ':=' | 'as' | 'collation' | 'count' | 'for' | 'group' |
                                    // 'let' | 'order' | 'return' | 'stable' | 'where'
    if (l1 == 49                    // ':='
     || l1 == 73)                   // 'as'
    {
      if (l1 == 73)                 // 'as'
      {
        whitespace();
        parse_TypeDeclaration();
      }
      lookahead1W(27);              // S^WS | '(:' | ':='
      shift(49);                    // ':='
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_ExprSingle();
    }
    if (l1 == 85)                   // 'collation'
    {
      shift(85);                    // 'collation'
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      whitespace();
      parse_URILiteral();
    }
    eventHandler.endNonterminal("GroupingSpec", e0);
  }

  private void try_GroupingSpec()
  {
    try_GroupingVariable();
    lookahead1W(120);               // S^WS | '(:' | ',' | ':=' | 'as' | 'collation' | 'count' | 'for' | 'group' |
                                    // 'let' | 'order' | 'return' | 'stable' | 'where'
    if (l1 == 49                    // ':='
     || l1 == 73)                   // 'as'
    {
      if (l1 == 73)                 // 'as'
      {
        try_TypeDeclaration();
      }
      lookahead1W(27);              // S^WS | '(:' | ':='
      shiftT(49);                   // ':='
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_ExprSingle();
    }
    if (l1 == 85)                   // 'collation'
    {
      shiftT(85);                   // 'collation'
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      try_URILiteral();
    }
  }

  private void parse_GroupingVariable()
  {
    eventHandler.startNonterminal("GroupingVariable", e0);
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    eventHandler.endNonterminal("GroupingVariable", e0);
  }

  private void try_GroupingVariable()
  {
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
  }

  private void parse_OrderByClause()
  {
    eventHandler.startNonterminal("OrderByClause", e0);
    switch (l1)
    {
    case 150:                       // 'order'
      shift(150);                   // 'order'
      lookahead1W(33);              // S^WS | '(:' | 'by'
      shift(79);                    // 'by'
      break;
    default:
      shift(170);                   // 'stable'
      lookahead1W(51);              // S^WS | '(:' | 'order'
      shift(150);                   // 'order'
      lookahead1W(33);              // S^WS | '(:' | 'by'
      shift(79);                    // 'by'
    }
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_OrderSpecList();
    eventHandler.endNonterminal("OrderByClause", e0);
  }

  private void try_OrderByClause()
  {
    switch (l1)
    {
    case 150:                       // 'order'
      shiftT(150);                  // 'order'
      lookahead1W(33);              // S^WS | '(:' | 'by'
      shiftT(79);                   // 'by'
      break;
    default:
      shiftT(170);                  // 'stable'
      lookahead1W(51);              // S^WS | '(:' | 'order'
      shiftT(150);                  // 'order'
      lookahead1W(33);              // S^WS | '(:' | 'by'
      shiftT(79);                   // 'by'
    }
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_OrderSpecList();
  }

  private void parse_OrderSpecList()
  {
    eventHandler.startNonterminal("OrderSpecList", e0);
    parse_OrderSpec();
    for (;;)
    {
      lookahead1W(116);             // S^WS | '(:' | ',' | 'count' | 'for' | 'group' | 'let' | 'order' | 'return' |
                                    // 'stable' | 'where'
      if (l1 != 39)                 // ','
      {
        break;
      }
      shift(39);                    // ','
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_OrderSpec();
    }
    eventHandler.endNonterminal("OrderSpecList", e0);
  }

  private void try_OrderSpecList()
  {
    try_OrderSpec();
    for (;;)
    {
      lookahead1W(116);             // S^WS | '(:' | ',' | 'count' | 'for' | 'group' | 'let' | 'order' | 'return' |
                                    // 'stable' | 'where'
      if (l1 != 39)                 // ','
      {
        break;
      }
      shiftT(39);                   // ','
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_OrderSpec();
    }
  }

  private void parse_OrderSpec()
  {
    eventHandler.startNonterminal("OrderSpec", e0);
    parse_ExprSingle();
    whitespace();
    parse_OrderModifier();
    eventHandler.endNonterminal("OrderSpec", e0);
  }

  private void try_OrderSpec()
  {
    try_ExprSingle();
    try_OrderModifier();
  }

  private void parse_OrderModifier()
  {
    eventHandler.startNonterminal("OrderModifier", e0);
    if (l1 == 74                    // 'ascending'
     || l1 == 97)                   // 'descending'
    {
      switch (l1)
      {
      case 74:                      // 'ascending'
        shift(74);                  // 'ascending'
        break;
      default:
        shift(97);                  // 'descending'
      }
    }
    lookahead1W(118);               // S^WS | '(:' | ',' | 'collation' | 'count' | 'empty' | 'for' | 'group' | 'let' |
                                    // 'order' | 'return' | 'stable' | 'where'
    if (l1 == 104)                  // 'empty'
    {
      shift(104);                   // 'empty'
      lookahead1W(82);              // S^WS | '(:' | 'greatest' | 'least'
      switch (l1)
      {
      case 117:                     // 'greatest'
        shift(117);                 // 'greatest'
        break;
      default:
        shift(133);                 // 'least'
      }
    }
    lookahead1W(117);               // S^WS | '(:' | ',' | 'collation' | 'count' | 'for' | 'group' | 'let' | 'order' |
                                    // 'return' | 'stable' | 'where'
    if (l1 == 85)                   // 'collation'
    {
      shift(85);                    // 'collation'
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      whitespace();
      parse_URILiteral();
    }
    eventHandler.endNonterminal("OrderModifier", e0);
  }

  private void try_OrderModifier()
  {
    if (l1 == 74                    // 'ascending'
     || l1 == 97)                   // 'descending'
    {
      switch (l1)
      {
      case 74:                      // 'ascending'
        shiftT(74);                 // 'ascending'
        break;
      default:
        shiftT(97);                 // 'descending'
      }
    }
    lookahead1W(118);               // S^WS | '(:' | ',' | 'collation' | 'count' | 'empty' | 'for' | 'group' | 'let' |
                                    // 'order' | 'return' | 'stable' | 'where'
    if (l1 == 104)                  // 'empty'
    {
      shiftT(104);                  // 'empty'
      lookahead1W(82);              // S^WS | '(:' | 'greatest' | 'least'
      switch (l1)
      {
      case 117:                     // 'greatest'
        shiftT(117);                // 'greatest'
        break;
      default:
        shiftT(133);                // 'least'
      }
    }
    lookahead1W(117);               // S^WS | '(:' | ',' | 'collation' | 'count' | 'for' | 'group' | 'let' | 'order' |
                                    // 'return' | 'stable' | 'where'
    if (l1 == 85)                   // 'collation'
    {
      shiftT(85);                   // 'collation'
      lookahead1W(17);              // StringLiteral | S^WS | '(:'
      try_URILiteral();
    }
  }

  private void parse_ReturnClause()
  {
    eventHandler.startNonterminal("ReturnClause", e0);
    shift(162);                     // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("ReturnClause", e0);
  }

  private void try_ReturnClause()
  {
    shiftT(162);                    // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_QuantifiedExpr()
  {
    eventHandler.startNonterminal("QuantifiedExpr", e0);
    switch (l1)
    {
    case 169:                       // 'some'
      shift(169);                   // 'some'
      break;
    default:
      shift(109);                   // 'every'
    }
    lookahead1W(21);                // S^WS | '$' | '(:'
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    lookahead1W(75);                // S^WS | '(:' | 'as' | 'in'
    if (l1 == 73)                   // 'as'
    {
      whitespace();
      parse_TypeDeclaration();
    }
    lookahead1W(45);                // S^WS | '(:' | 'in'
    shift(124);                     // 'in'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    for (;;)
    {
      if (l1 != 39)                 // ','
      {
        break;
      }
      shift(39);                    // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      shift(30);                    // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_VarName();
      lookahead1W(75);              // S^WS | '(:' | 'as' | 'in'
      if (l1 == 73)                 // 'as'
      {
        whitespace();
        parse_TypeDeclaration();
      }
      lookahead1W(45);              // S^WS | '(:' | 'in'
      shift(124);                   // 'in'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_ExprSingle();
    }
    shift(163);                     // 'satisfies'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("QuantifiedExpr", e0);
  }

  private void try_QuantifiedExpr()
  {
    switch (l1)
    {
    case 169:                       // 'some'
      shiftT(169);                  // 'some'
      break;
    default:
      shiftT(109);                  // 'every'
    }
    lookahead1W(21);                // S^WS | '$' | '(:'
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
    lookahead1W(75);                // S^WS | '(:' | 'as' | 'in'
    if (l1 == 73)                   // 'as'
    {
      try_TypeDeclaration();
    }
    lookahead1W(45);                // S^WS | '(:' | 'in'
    shiftT(124);                    // 'in'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
    for (;;)
    {
      if (l1 != 39)                 // ','
      {
        break;
      }
      shiftT(39);                   // ','
      lookahead1W(21);              // S^WS | '$' | '(:'
      shiftT(30);                   // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_VarName();
      lookahead1W(75);              // S^WS | '(:' | 'as' | 'in'
      if (l1 == 73)                 // 'as'
      {
        try_TypeDeclaration();
      }
      lookahead1W(45);              // S^WS | '(:' | 'in'
      shiftT(124);                  // 'in'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_ExprSingle();
    }
    shiftT(163);                    // 'satisfies'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_SwitchExpr()
  {
    eventHandler.startNonterminal("SwitchExpr", e0);
    shift(174);                     // 'switch'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(36);                      // ')'
    for (;;)
    {
      lookahead1W(34);              // S^WS | '(:' | 'case'
      whitespace();
      parse_SwitchCaseClause();
      if (l1 != 80)                 // 'case'
      {
        break;
      }
    }
    shift(94);                      // 'default'
    lookahead1W(53);                // S^WS | '(:' | 'return'
    shift(162);                     // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("SwitchExpr", e0);
  }

  private void try_SwitchExpr()
  {
    shiftT(174);                    // 'switch'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(36);                     // ')'
    for (;;)
    {
      lookahead1W(34);              // S^WS | '(:' | 'case'
      try_SwitchCaseClause();
      if (l1 != 80)                 // 'case'
      {
        break;
      }
    }
    shiftT(94);                     // 'default'
    lookahead1W(53);                // S^WS | '(:' | 'return'
    shiftT(162);                    // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_SwitchCaseClause()
  {
    eventHandler.startNonterminal("SwitchCaseClause", e0);
    for (;;)
    {
      shift(80);                    // 'case'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_SwitchCaseOperand();
      if (l1 != 80)                 // 'case'
      {
        break;
      }
    }
    shift(162);                     // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("SwitchCaseClause", e0);
  }

  private void try_SwitchCaseClause()
  {
    for (;;)
    {
      shiftT(80);                   // 'case'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_SwitchCaseOperand();
      if (l1 != 80)                 // 'case'
      {
        break;
      }
    }
    shiftT(162);                    // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_SwitchCaseOperand()
  {
    eventHandler.startNonterminal("SwitchCaseOperand", e0);
    parse_ExprSingle();
    eventHandler.endNonterminal("SwitchCaseOperand", e0);
  }

  private void try_SwitchCaseOperand()
  {
    try_ExprSingle();
  }

  private void parse_TypeswitchExpr()
  {
    eventHandler.startNonterminal("TypeswitchExpr", e0);
    shift(182);                     // 'typeswitch'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(36);                      // ')'
    for (;;)
    {
      lookahead1W(34);              // S^WS | '(:' | 'case'
      whitespace();
      parse_CaseClause();
      if (l1 != 80)                 // 'case'
      {
        break;
      }
    }
    shift(94);                      // 'default'
    lookahead1W(64);                // S^WS | '$' | '(:' | 'return'
    if (l1 == 30)                   // '$'
    {
      shift(30);                    // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_VarName();
    }
    lookahead1W(53);                // S^WS | '(:' | 'return'
    shift(162);                     // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("TypeswitchExpr", e0);
  }

  private void try_TypeswitchExpr()
  {
    shiftT(182);                    // 'typeswitch'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(36);                     // ')'
    for (;;)
    {
      lookahead1W(34);              // S^WS | '(:' | 'case'
      try_CaseClause();
      if (l1 != 80)                 // 'case'
      {
        break;
      }
    }
    shiftT(94);                     // 'default'
    lookahead1W(64);                // S^WS | '$' | '(:' | 'return'
    if (l1 == 30)                   // '$'
    {
      shiftT(30);                   // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_VarName();
    }
    lookahead1W(53);                // S^WS | '(:' | 'return'
    shiftT(162);                    // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_CaseClause()
  {
    eventHandler.startNonterminal("CaseClause", e0);
    shift(80);                      // 'case'
    lookahead1W(162);               // URIQualifiedName | QName^Token | S^WS | '$' | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 == 30)                   // '$'
    {
      shift(30);                    // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_VarName();
      lookahead1W(30);              // S^WS | '(:' | 'as'
      shift(73);                    // 'as'
    }
    lookahead1W(160);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_SequenceTypeUnion();
    shift(162);                     // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("CaseClause", e0);
  }

  private void try_CaseClause()
  {
    shiftT(80);                     // 'case'
    lookahead1W(162);               // URIQualifiedName | QName^Token | S^WS | '$' | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 == 30)                   // '$'
    {
      shiftT(30);                   // '$'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_VarName();
      lookahead1W(30);              // S^WS | '(:' | 'as'
      shiftT(73);                   // 'as'
    }
    lookahead1W(160);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_SequenceTypeUnion();
    shiftT(162);                    // 'return'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_SequenceTypeUnion()
  {
    eventHandler.startNonterminal("SequenceTypeUnion", e0);
    parse_SequenceType();
    for (;;)
    {
      lookahead1W(89);              // S^WS | '(:' | 'return' | '|'
      if (l1 != 195)                // '|'
      {
        break;
      }
      shift(195);                   // '|'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_SequenceType();
    }
    eventHandler.endNonterminal("SequenceTypeUnion", e0);
  }

  private void try_SequenceTypeUnion()
  {
    try_SequenceType();
    for (;;)
    {
      lookahead1W(89);              // S^WS | '(:' | 'return' | '|'
      if (l1 != 195)                // '|'
      {
        break;
      }
      shiftT(195);                  // '|'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_SequenceType();
    }
  }

  private void parse_IfExpr()
  {
    eventHandler.startNonterminal("IfExpr", e0);
    shift(122);                     // 'if'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(36);                      // ')'
    lookahead1W(55);                // S^WS | '(:' | 'then'
    shift(176);                     // 'then'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    shift(103);                     // 'else'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ExprSingle();
    eventHandler.endNonterminal("IfExpr", e0);
  }

  private void try_IfExpr()
  {
    shiftT(122);                    // 'if'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(36);                     // ')'
    lookahead1W(55);                // S^WS | '(:' | 'then'
    shiftT(176);                    // 'then'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
    shiftT(103);                    // 'else'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ExprSingle();
  }

  private void parse_TryCatchExpr()
  {
    eventHandler.startNonterminal("TryCatchExpr", e0);
    parse_TryClause();
    for (;;)
    {
      lookahead1W(35);              // S^WS | '(:' | 'catch'
      whitespace();
      parse_CatchClause();
      lookahead1W(121);             // S^WS | EOF | '(:' | ')' | ',' | ';' | ']' | 'ascending' | 'case' | 'catch' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'else' | 'empty' | 'end' |
                                    // 'for' | 'group' | 'let' | 'only' | 'order' | 'return' | 'satisfies' | 'stable' |
                                    // 'start' | 'where' | '}'
      if (l1 != 83)                 // 'catch'
      {
        break;
      }
    }
    eventHandler.endNonterminal("TryCatchExpr", e0);
  }

  private void try_TryCatchExpr()
  {
    try_TryClause();
    for (;;)
    {
      lookahead1W(35);              // S^WS | '(:' | 'catch'
      try_CatchClause();
      lookahead1W(121);             // S^WS | EOF | '(:' | ')' | ',' | ';' | ']' | 'ascending' | 'case' | 'catch' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'else' | 'empty' | 'end' |
                                    // 'for' | 'group' | 'let' | 'only' | 'order' | 'return' | 'satisfies' | 'stable' |
                                    // 'start' | 'where' | '}'
      if (l1 != 83)                 // 'catch'
      {
        break;
      }
    }
  }

  private void parse_TryClause()
  {
    eventHandler.startNonterminal("TryClause", e0);
    shift(179);                     // 'try'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_TryTargetExpr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("TryClause", e0);
  }

  private void try_TryClause()
  {
    shiftT(179);                    // 'try'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_TryTargetExpr();
    shiftT(197);                    // '}'
  }

  private void parse_TryTargetExpr()
  {
    eventHandler.startNonterminal("TryTargetExpr", e0);
    parse_Expr();
    eventHandler.endNonterminal("TryTargetExpr", e0);
  }

  private void try_TryTargetExpr()
  {
    try_Expr();
  }

  private void parse_CatchClause()
  {
    eventHandler.startNonterminal("CatchClause", e0);
    shift(83);                      // 'catch'
    lookahead1W(157);               // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_CatchErrorList();
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("CatchClause", e0);
  }

  private void try_CatchClause()
  {
    shiftT(83);                     // 'catch'
    lookahead1W(157);               // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_CatchErrorList();
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(197);                    // '}'
  }

  private void parse_CatchErrorList()
  {
    eventHandler.startNonterminal("CatchErrorList", e0);
    parse_NameTest();
    for (;;)
    {
      lookahead1W(91);              // S^WS | '(:' | '{' | '|'
      if (l1 != 195)                // '|'
      {
        break;
      }
      shift(195);                   // '|'
      lookahead1W(157);             // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_NameTest();
    }
    eventHandler.endNonterminal("CatchErrorList", e0);
  }

  private void try_CatchErrorList()
  {
    try_NameTest();
    for (;;)
    {
      lookahead1W(91);              // S^WS | '(:' | '{' | '|'
      if (l1 != 195)                // '|'
      {
        break;
      }
      shiftT(195);                  // '|'
      lookahead1W(157);             // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_NameTest();
    }
  }

  private void parse_OrExpr()
  {
    eventHandler.startNonterminal("OrExpr", e0);
    parse_AndExpr();
    for (;;)
    {
      if (l1 != 149)                // 'or'
      {
        break;
      }
      shift(149);                   // 'or'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_AndExpr();
    }
    eventHandler.endNonterminal("OrExpr", e0);
  }

  private void try_OrExpr()
  {
    try_AndExpr();
    for (;;)
    {
      if (l1 != 149)                // 'or'
      {
        break;
      }
      shiftT(149);                  // 'or'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_AndExpr();
    }
  }

  private void parse_AndExpr()
  {
    eventHandler.startNonterminal("AndExpr", e0);
    parse_ComparisonExpr();
    for (;;)
    {
      if (l1 != 72)                 // 'and'
      {
        break;
      }
      shift(72);                    // 'and'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_ComparisonExpr();
    }
    eventHandler.endNonterminal("AndExpr", e0);
  }

  private void try_AndExpr()
  {
    try_ComparisonExpr();
    for (;;)
    {
      if (l1 != 72)                 // 'and'
      {
        break;
      }
      shiftT(72);                   // 'and'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_ComparisonExpr();
    }
  }

  private void parse_ComparisonExpr()
  {
    eventHandler.startNonterminal("ComparisonExpr", e0);
    parse_StringConcatExpr();
    if (l1 == 26                    // '!='
     || l1 == 51                    // '<'
     || l1 == 55                    // '<<'
     || l1 == 56                    // '<='
     || l1 == 58                    // '='
     || l1 == 59                    // '>'
     || l1 == 60                    // '>='
     || l1 == 61                    // '>>'
     || l1 == 108                   // 'eq'
     || l1 == 116                   // 'ge'
     || l1 == 120                   // 'gt'
     || l1 == 129                   // 'is'
     || l1 == 132                   // 'le'
     || l1 == 135                   // 'lt'
     || l1 == 141)                  // 'ne'
    {
      switch (l1)
      {
      case 108:                     // 'eq'
      case 116:                     // 'ge'
      case 120:                     // 'gt'
      case 132:                     // 'le'
      case 135:                     // 'lt'
      case 141:                     // 'ne'
        whitespace();
        parse_ValueComp();
        break;
      case 55:                      // '<<'
      case 61:                      // '>>'
      case 129:                     // 'is'
        whitespace();
        parse_NodeComp();
        break;
      default:
        whitespace();
        parse_GeneralComp();
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_StringConcatExpr();
    }
    eventHandler.endNonterminal("ComparisonExpr", e0);
  }

  private void try_ComparisonExpr()
  {
    try_StringConcatExpr();
    if (l1 == 26                    // '!='
     || l1 == 51                    // '<'
     || l1 == 55                    // '<<'
     || l1 == 56                    // '<='
     || l1 == 58                    // '='
     || l1 == 59                    // '>'
     || l1 == 60                    // '>='
     || l1 == 61                    // '>>'
     || l1 == 108                   // 'eq'
     || l1 == 116                   // 'ge'
     || l1 == 120                   // 'gt'
     || l1 == 129                   // 'is'
     || l1 == 132                   // 'le'
     || l1 == 135                   // 'lt'
     || l1 == 141)                  // 'ne'
    {
      switch (l1)
      {
      case 108:                     // 'eq'
      case 116:                     // 'ge'
      case 120:                     // 'gt'
      case 132:                     // 'le'
      case 135:                     // 'lt'
      case 141:                     // 'ne'
        try_ValueComp();
        break;
      case 55:                      // '<<'
      case 61:                      // '>>'
      case 129:                     // 'is'
        try_NodeComp();
        break;
      default:
        try_GeneralComp();
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_StringConcatExpr();
    }
  }

  private void parse_StringConcatExpr()
  {
    eventHandler.startNonterminal("StringConcatExpr", e0);
    parse_RangeExpr();
    for (;;)
    {
      if (l1 != 196)                // '||'
      {
        break;
      }
      shift(196);                   // '||'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_RangeExpr();
    }
    eventHandler.endNonterminal("StringConcatExpr", e0);
  }

  private void try_StringConcatExpr()
  {
    try_RangeExpr();
    for (;;)
    {
      if (l1 != 196)                // '||'
      {
        break;
      }
      shiftT(196);                  // '||'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_RangeExpr();
    }
  }

  private void parse_RangeExpr()
  {
    eventHandler.startNonterminal("RangeExpr", e0);
    parse_AdditiveExpr();
    if (l1 == 177)                  // 'to'
    {
      shift(177);                   // 'to'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_AdditiveExpr();
    }
    eventHandler.endNonterminal("RangeExpr", e0);
  }

  private void try_RangeExpr()
  {
    try_AdditiveExpr();
    if (l1 == 177)                  // 'to'
    {
      shiftT(177);                  // 'to'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_AdditiveExpr();
    }
  }

  private void parse_AdditiveExpr()
  {
    eventHandler.startNonterminal("AdditiveExpr", e0);
    parse_MultiplicativeExpr();
    for (;;)
    {
      if (l1 != 38                  // '+'
       && l1 != 40)                 // '-'
      {
        break;
      }
      switch (l1)
      {
      case 38:                      // '+'
        shift(38);                  // '+'
        break;
      default:
        shift(40);                  // '-'
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_MultiplicativeExpr();
    }
    eventHandler.endNonterminal("AdditiveExpr", e0);
  }

  private void try_AdditiveExpr()
  {
    try_MultiplicativeExpr();
    for (;;)
    {
      if (l1 != 38                  // '+'
       && l1 != 40)                 // '-'
      {
        break;
      }
      switch (l1)
      {
      case 38:                      // '+'
        shiftT(38);                 // '+'
        break;
      default:
        shiftT(40);                 // '-'
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_MultiplicativeExpr();
    }
  }

  private void parse_MultiplicativeExpr()
  {
    eventHandler.startNonterminal("MultiplicativeExpr", e0);
    parse_UnionExpr();
    for (;;)
    {
      if (l1 != 37                  // '*'
       && l1 != 99                  // 'div'
       && l1 != 121                 // 'idiv'
       && l1 != 137)                // 'mod'
      {
        break;
      }
      switch (l1)
      {
      case 37:                      // '*'
        shift(37);                  // '*'
        break;
      case 99:                      // 'div'
        shift(99);                  // 'div'
        break;
      case 121:                     // 'idiv'
        shift(121);                 // 'idiv'
        break;
      default:
        shift(137);                 // 'mod'
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_UnionExpr();
    }
    eventHandler.endNonterminal("MultiplicativeExpr", e0);
  }

  private void try_MultiplicativeExpr()
  {
    try_UnionExpr();
    for (;;)
    {
      if (l1 != 37                  // '*'
       && l1 != 99                  // 'div'
       && l1 != 121                 // 'idiv'
       && l1 != 137)                // 'mod'
      {
        break;
      }
      switch (l1)
      {
      case 37:                      // '*'
        shiftT(37);                 // '*'
        break;
      case 99:                      // 'div'
        shiftT(99);                 // 'div'
        break;
      case 121:                     // 'idiv'
        shiftT(121);                // 'idiv'
        break;
      default:
        shiftT(137);                // 'mod'
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_UnionExpr();
    }
  }

  private void parse_UnionExpr()
  {
    eventHandler.startNonterminal("UnionExpr", e0);
    parse_IntersectExceptExpr();
    for (;;)
    {
      if (l1 != 183                 // 'union'
       && l1 != 195)                // '|'
      {
        break;
      }
      switch (l1)
      {
      case 183:                     // 'union'
        shift(183);                 // 'union'
        break;
      default:
        shift(195);                 // '|'
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_IntersectExceptExpr();
    }
    eventHandler.endNonterminal("UnionExpr", e0);
  }

  private void try_UnionExpr()
  {
    try_IntersectExceptExpr();
    for (;;)
    {
      if (l1 != 183                 // 'union'
       && l1 != 195)                // '|'
      {
        break;
      }
      switch (l1)
      {
      case 183:                     // 'union'
        shiftT(183);                // 'union'
        break;
      default:
        shiftT(195);                // '|'
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_IntersectExceptExpr();
    }
  }

  private void parse_IntersectExceptExpr()
  {
    eventHandler.startNonterminal("IntersectExceptExpr", e0);
    parse_InstanceofExpr();
    for (;;)
    {
      lookahead1W(132);             // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'intersect' |
                                    // 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' |
                                    // 'satisfies' | 'stable' | 'start' | 'to' | 'union' | 'where' | '|' | '||' | '}'
      if (l1 != 110                 // 'except'
       && l1 != 128)                // 'intersect'
      {
        break;
      }
      switch (l1)
      {
      case 128:                     // 'intersect'
        shift(128);                 // 'intersect'
        break;
      default:
        shift(110);                 // 'except'
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_InstanceofExpr();
    }
    eventHandler.endNonterminal("IntersectExceptExpr", e0);
  }

  private void try_IntersectExceptExpr()
  {
    try_InstanceofExpr();
    for (;;)
    {
      lookahead1W(132);             // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'intersect' |
                                    // 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' |
                                    // 'satisfies' | 'stable' | 'start' | 'to' | 'union' | 'where' | '|' | '||' | '}'
      if (l1 != 110                 // 'except'
       && l1 != 128)                // 'intersect'
      {
        break;
      }
      switch (l1)
      {
      case 128:                     // 'intersect'
        shiftT(128);                // 'intersect'
        break;
      default:
        shiftT(110);                // 'except'
      }
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_InstanceofExpr();
    }
  }

  private void parse_InstanceofExpr()
  {
    eventHandler.startNonterminal("InstanceofExpr", e0);
    parse_TreatExpr();
    lookahead1W(133);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'union' |
                                    // 'where' | '|' | '||' | '}'
    if (l1 == 127)                  // 'instance'
    {
      shift(127);                   // 'instance'
      lookahead1W(49);              // S^WS | '(:' | 'of'
      shift(146);                   // 'of'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_SequenceType();
    }
    eventHandler.endNonterminal("InstanceofExpr", e0);
  }

  private void try_InstanceofExpr()
  {
    try_TreatExpr();
    lookahead1W(133);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'union' |
                                    // 'where' | '|' | '||' | '}'
    if (l1 == 127)                  // 'instance'
    {
      shiftT(127);                  // 'instance'
      lookahead1W(49);              // S^WS | '(:' | 'of'
      shiftT(146);                  // 'of'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_SequenceType();
    }
  }

  private void parse_TreatExpr()
  {
    eventHandler.startNonterminal("TreatExpr", e0);
    parse_CastableExpr();
    lookahead1W(134);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '|' | '||' | '}'
    if (l1 == 178)                  // 'treat'
    {
      shift(178);                   // 'treat'
      lookahead1W(30);              // S^WS | '(:' | 'as'
      shift(73);                    // 'as'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_SequenceType();
    }
    eventHandler.endNonterminal("TreatExpr", e0);
  }

  private void try_TreatExpr()
  {
    try_CastableExpr();
    lookahead1W(134);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '|' | '||' | '}'
    if (l1 == 178)                  // 'treat'
    {
      shiftT(178);                  // 'treat'
      lookahead1W(30);              // S^WS | '(:' | 'as'
      shiftT(73);                   // 'as'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_SequenceType();
    }
  }

  private void parse_CastableExpr()
  {
    eventHandler.startNonterminal("CastableExpr", e0);
    parse_CastExpr();
    lookahead1W(135);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '|' | '||' | '}'
    if (l1 == 82)                   // 'castable'
    {
      shift(82);                    // 'castable'
      lookahead1W(30);              // S^WS | '(:' | 'as'
      shift(73);                    // 'as'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_SingleType();
    }
    eventHandler.endNonterminal("CastableExpr", e0);
  }

  private void try_CastableExpr()
  {
    try_CastExpr();
    lookahead1W(135);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '|' | '||' | '}'
    if (l1 == 82)                   // 'castable'
    {
      shiftT(82);                   // 'castable'
      lookahead1W(30);              // S^WS | '(:' | 'as'
      shiftT(73);                   // 'as'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_SingleType();
    }
  }

  private void parse_CastExpr()
  {
    eventHandler.startNonterminal("CastExpr", e0);
    parse_UnaryExpr();
    lookahead1W(137);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' | 'cast' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '|' | '||' | '}'
    if (l1 == 81)                   // 'cast'
    {
      shift(81);                    // 'cast'
      lookahead1W(30);              // S^WS | '(:' | 'as'
      shift(73);                    // 'as'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_SingleType();
    }
    eventHandler.endNonterminal("CastExpr", e0);
  }

  private void try_CastExpr()
  {
    try_UnaryExpr();
    lookahead1W(137);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | ']' | 'and' | 'ascending' | 'case' | 'cast' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '|' | '||' | '}'
    if (l1 == 81)                   // 'cast'
    {
      shiftT(81);                   // 'cast'
      lookahead1W(30);              // S^WS | '(:' | 'as'
      shiftT(73);                   // 'as'
      lookahead1W(155);             // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_SingleType();
    }
  }

  private void parse_UnaryExpr()
  {
    eventHandler.startNonterminal("UnaryExpr", e0);
    for (;;)
    {
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      if (l1 != 38                  // '+'
       && l1 != 40)                 // '-'
      {
        break;
      }
      switch (l1)
      {
      case 40:                      // '-'
        shift(40);                  // '-'
        break;
      default:
        shift(38);                  // '+'
      }
    }
    whitespace();
    parse_ValueExpr();
    eventHandler.endNonterminal("UnaryExpr", e0);
  }

  private void try_UnaryExpr()
  {
    for (;;)
    {
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      if (l1 != 38                  // '+'
       && l1 != 40)                 // '-'
      {
        break;
      }
      switch (l1)
      {
      case 40:                      // '-'
        shiftT(40);                 // '-'
        break;
      default:
        shiftT(38);                 // '+'
      }
    }
    try_ValueExpr();
  }

  private void parse_ValueExpr()
  {
    eventHandler.startNonterminal("ValueExpr", e0);
    switch (l1)
    {
    case 185:                       // 'validate'
      lookahead2W(151);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'lax' | 'le' |
                                    // 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'strict' | 'to' | 'treat' | 'type' | 'union' | 'where' |
                                    // '{' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 33721:                     // 'validate' 'lax'
    case 44217:                     // 'validate' 'strict'
    case 46521:                     // 'validate' 'type'
    case 49593:                     // 'validate' '{'
      parse_ValidateExpr();
      break;
    case 34:                        // '(#'
      parse_ExtensionExpr();
      break;
    default:
      parse_SimpleMapExpr();
    }
    eventHandler.endNonterminal("ValueExpr", e0);
  }

  private void try_ValueExpr()
  {
    switch (l1)
    {
    case 185:                       // 'validate'
      lookahead2W(151);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'lax' | 'le' |
                                    // 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'strict' | 'to' | 'treat' | 'type' | 'union' | 'where' |
                                    // '{' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 33721:                     // 'validate' 'lax'
    case 44217:                     // 'validate' 'strict'
    case 46521:                     // 'validate' 'type'
    case 49593:                     // 'validate' '{'
      try_ValidateExpr();
      break;
    case 34:                        // '(#'
      try_ExtensionExpr();
      break;
    default:
      try_SimpleMapExpr();
    }
  }

  private void parse_GeneralComp()
  {
    eventHandler.startNonterminal("GeneralComp", e0);
    switch (l1)
    {
    case 58:                        // '='
      shift(58);                    // '='
      break;
    case 26:                        // '!='
      shift(26);                    // '!='
      break;
    case 51:                        // '<'
      shift(51);                    // '<'
      break;
    case 56:                        // '<='
      shift(56);                    // '<='
      break;
    case 59:                        // '>'
      shift(59);                    // '>'
      break;
    default:
      shift(60);                    // '>='
    }
    eventHandler.endNonterminal("GeneralComp", e0);
  }

  private void try_GeneralComp()
  {
    switch (l1)
    {
    case 58:                        // '='
      shiftT(58);                   // '='
      break;
    case 26:                        // '!='
      shiftT(26);                   // '!='
      break;
    case 51:                        // '<'
      shiftT(51);                   // '<'
      break;
    case 56:                        // '<='
      shiftT(56);                   // '<='
      break;
    case 59:                        // '>'
      shiftT(59);                   // '>'
      break;
    default:
      shiftT(60);                   // '>='
    }
  }

  private void parse_ValueComp()
  {
    eventHandler.startNonterminal("ValueComp", e0);
    switch (l1)
    {
    case 108:                       // 'eq'
      shift(108);                   // 'eq'
      break;
    case 141:                       // 'ne'
      shift(141);                   // 'ne'
      break;
    case 135:                       // 'lt'
      shift(135);                   // 'lt'
      break;
    case 132:                       // 'le'
      shift(132);                   // 'le'
      break;
    case 120:                       // 'gt'
      shift(120);                   // 'gt'
      break;
    default:
      shift(116);                   // 'ge'
    }
    eventHandler.endNonterminal("ValueComp", e0);
  }

  private void try_ValueComp()
  {
    switch (l1)
    {
    case 108:                       // 'eq'
      shiftT(108);                  // 'eq'
      break;
    case 141:                       // 'ne'
      shiftT(141);                  // 'ne'
      break;
    case 135:                       // 'lt'
      shiftT(135);                  // 'lt'
      break;
    case 132:                       // 'le'
      shiftT(132);                  // 'le'
      break;
    case 120:                       // 'gt'
      shiftT(120);                  // 'gt'
      break;
    default:
      shiftT(116);                  // 'ge'
    }
  }

  private void parse_NodeComp()
  {
    eventHandler.startNonterminal("NodeComp", e0);
    switch (l1)
    {
    case 129:                       // 'is'
      shift(129);                   // 'is'
      break;
    case 55:                        // '<<'
      shift(55);                    // '<<'
      break;
    default:
      shift(61);                    // '>>'
    }
    eventHandler.endNonterminal("NodeComp", e0);
  }

  private void try_NodeComp()
  {
    switch (l1)
    {
    case 129:                       // 'is'
      shiftT(129);                  // 'is'
      break;
    case 55:                        // '<<'
      shiftT(55);                   // '<<'
      break;
    default:
      shiftT(61);                   // '>>'
    }
  }

  private void parse_ValidateExpr()
  {
    eventHandler.startNonterminal("ValidateExpr", e0);
    shift(185);                     // 'validate'
    lookahead1W(107);               // S^WS | '(:' | 'lax' | 'strict' | 'type' | '{'
    if (l1 != 193)                  // '{'
    {
      switch (l1)
      {
      case 181:                     // 'type'
        shift(181);                 // 'type'
        lookahead1W(155);           // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
        whitespace();
        parse_TypeName();
        break;
      default:
        whitespace();
        parse_ValidationMode();
      }
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("ValidateExpr", e0);
  }

  private void try_ValidateExpr()
  {
    shiftT(185);                    // 'validate'
    lookahead1W(107);               // S^WS | '(:' | 'lax' | 'strict' | 'type' | '{'
    if (l1 != 193)                  // '{'
    {
      switch (l1)
      {
      case 181:                     // 'type'
        shiftT(181);                // 'type'
        lookahead1W(155);           // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
        try_TypeName();
        break;
      default:
        try_ValidationMode();
      }
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(197);                    // '}'
  }

  private void parse_ValidationMode()
  {
    eventHandler.startNonterminal("ValidationMode", e0);
    switch (l1)
    {
    case 131:                       // 'lax'
      shift(131);                   // 'lax'
      break;
    default:
      shift(172);                   // 'strict'
    }
    eventHandler.endNonterminal("ValidationMode", e0);
  }

  private void try_ValidationMode()
  {
    switch (l1)
    {
    case 131:                       // 'lax'
      shiftT(131);                  // 'lax'
      break;
    default:
      shiftT(172);                  // 'strict'
    }
  }

  private void parse_ExtensionExpr()
  {
    eventHandler.startNonterminal("ExtensionExpr", e0);
    for (;;)
    {
      whitespace();
      parse_Pragma();
      lookahead1W(66);              // S^WS | '(#' | '(:' | '{'
      if (l1 != 34)                 // '(#'
      {
        break;
      }
    }
    shift(193);                     // '{'
    lookahead1W(170);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '}'
    if (l1 != 197)                  // '}'
    {
      whitespace();
      parse_Expr();
    }
    shift(197);                     // '}'
    eventHandler.endNonterminal("ExtensionExpr", e0);
  }

  private void try_ExtensionExpr()
  {
    for (;;)
    {
      try_Pragma();
      lookahead1W(66);              // S^WS | '(#' | '(:' | '{'
      if (l1 != 34)                 // '(#'
      {
        break;
      }
    }
    shiftT(193);                    // '{'
    lookahead1W(170);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '}'
    if (l1 != 197)                  // '}'
    {
      try_Expr();
    }
    shiftT(197);                    // '}'
  }

  private void parse_Pragma()
  {
    eventHandler.startNonterminal("Pragma", e0);
    shift(34);                      // '(#'
    lookahead1(154);                // URIQualifiedName | QName^Token | S | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    if (l1 == 16)                   // S
    {
      shift(16);                    // S
    }
    parse_EQName();
    lookahead1(11);                 // S | '#)'
    if (l1 == 16)                   // S
    {
      shift(16);                    // S
      lookahead1(1);                // PragmaContents
      shift(19);                    // PragmaContents
    }
    lookahead1(5);                  // '#)'
    shift(29);                      // '#)'
    eventHandler.endNonterminal("Pragma", e0);
  }

  private void try_Pragma()
  {
    shiftT(34);                     // '(#'
    lookahead1(154);                // URIQualifiedName | QName^Token | S | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    if (l1 == 16)                   // S
    {
      shiftT(16);                   // S
    }
    try_EQName();
    lookahead1(11);                 // S | '#)'
    if (l1 == 16)                   // S
    {
      shiftT(16);                   // S
      lookahead1(1);                // PragmaContents
      shiftT(19);                   // PragmaContents
    }
    lookahead1(5);                  // '#)'
    shiftT(29);                     // '#)'
  }

  private void parse_SimpleMapExpr()
  {
    eventHandler.startNonterminal("SimpleMapExpr", e0);
    parse_PathExpr();
    for (;;)
    {
      if (l1 != 25)                 // '!'
      {
        break;
      }
      shift(25);                    // '!'
      lookahead1W(165);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(:' | '.' |
                                    // '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_PathExpr();
    }
    eventHandler.endNonterminal("SimpleMapExpr", e0);
  }

  private void try_SimpleMapExpr()
  {
    try_PathExpr();
    for (;;)
    {
      if (l1 != 25)                 // '!'
      {
        break;
      }
      shiftT(25);                   // '!'
      lookahead1W(165);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(:' | '.' |
                                    // '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_PathExpr();
    }
  }

  private void parse_PathExpr()
  {
    eventHandler.startNonterminal("PathExpr", e0);
    switch (l1)
    {
    case 44:                        // '/'
      shift(44);                    // '/'
      lookahead1W(174);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | EOF | '!' | '!=' | '$' | '%' |
                                    // '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '.' | '..' | ';' | '<' | '<!--' |
                                    // '<<' | '<=' | '<?' | '=' | '>' | '>=' | '>>' | '@' | ']' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '|' | '||' | '}'
      switch (l1)
      {
      case 24:                      // EOF
      case 25:                      // '!'
      case 26:                      // '!='
      case 36:                      // ')'
      case 37:                      // '*'
      case 38:                      // '+'
      case 39:                      // ','
      case 40:                      // '-'
      case 50:                      // ';'
      case 55:                      // '<<'
      case 56:                      // '<='
      case 58:                      // '='
      case 59:                      // '>'
      case 60:                      // '>='
      case 61:                      // '>>'
      case 67:                      // ']'
      case 195:                     // '|'
      case 196:                     // '||'
      case 197:                     // '}'
        break;
      default:
        whitespace();
        parse_RelativePathExpr();
      }
      break;
    case 45:                        // '//'
      shift(45);                    // '//'
      lookahead1W(164);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(:' | '.' |
                                    // '..' | '<' | '<!--' | '<?' | '@' | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_RelativePathExpr();
      break;
    default:
      parse_RelativePathExpr();
    }
    eventHandler.endNonterminal("PathExpr", e0);
  }

  private void try_PathExpr()
  {
    switch (l1)
    {
    case 44:                        // '/'
      shiftT(44);                   // '/'
      lookahead1W(174);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | EOF | '!' | '!=' | '$' | '%' |
                                    // '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '.' | '..' | ';' | '<' | '<!--' |
                                    // '<<' | '<=' | '<?' | '=' | '>' | '>=' | '>>' | '@' | ']' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '|' | '||' | '}'
      switch (l1)
      {
      case 24:                      // EOF
      case 25:                      // '!'
      case 26:                      // '!='
      case 36:                      // ')'
      case 37:                      // '*'
      case 38:                      // '+'
      case 39:                      // ','
      case 40:                      // '-'
      case 50:                      // ';'
      case 55:                      // '<<'
      case 56:                      // '<='
      case 58:                      // '='
      case 59:                      // '>'
      case 60:                      // '>='
      case 61:                      // '>>'
      case 67:                      // ']'
      case 195:                     // '|'
      case 196:                     // '||'
      case 197:                     // '}'
        break;
      default:
        try_RelativePathExpr();
      }
      break;
    case 45:                        // '//'
      shiftT(45);                   // '//'
      lookahead1W(164);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(:' | '.' |
                                    // '..' | '<' | '<!--' | '<?' | '@' | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_RelativePathExpr();
      break;
    default:
      try_RelativePathExpr();
    }
  }

  private void parse_RelativePathExpr()
  {
    eventHandler.startNonterminal("RelativePathExpr", e0);
    parse_StepExpr();
    for (;;)
    {
      if (l1 != 44                  // '/'
       && l1 != 45)                 // '//'
      {
        break;
      }
      switch (l1)
      {
      case 44:                      // '/'
        shift(44);                  // '/'
        break;
      default:
        shift(45);                  // '//'
      }
      lookahead1W(164);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(:' | '.' |
                                    // '..' | '<' | '<!--' | '<?' | '@' | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      whitespace();
      parse_StepExpr();
    }
    eventHandler.endNonterminal("RelativePathExpr", e0);
  }

  private void try_RelativePathExpr()
  {
    try_StepExpr();
    for (;;)
    {
      if (l1 != 44                  // '/'
       && l1 != 45)                 // '//'
      {
        break;
      }
      switch (l1)
      {
      case 44:                      // '/'
        shiftT(44);                 // '/'
        break;
      default:
        shiftT(45);                 // '//'
      }
      lookahead1W(164);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(:' | '.' |
                                    // '..' | '<' | '<!--' | '<?' | '@' | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      try_StepExpr();
    }
  }

  private void parse_StepExpr()
  {
    eventHandler.startNonterminal("StepExpr", e0);
    switch (l1)
    {
    case 76:                        // 'attribute'
      lookahead2W(173);             // URIQualifiedName | QName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' |
                                    // ')' | '*' | '+' | ',' | '-' | '/' | '//' | '::' | ';' | '<' | '<<' | '<=' | '=' |
                                    // '>' | '>=' | '>>' | '[' | ']' | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery' | '{' |
                                    // '|' | '||' | '}'
      break;
    case 102:                       // 'element'
      lookahead2W(172);             // URIQualifiedName | QName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' |
                                    // ')' | '*' | '+' | ',' | '-' | '/' | '//' | ';' | '<' | '<<' | '<=' | '=' | '>' |
                                    // '>=' | '>>' | '[' | ']' | 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' |
                                    // 'attribute' | 'case' | 'cast' | 'castable' | 'child' | 'collation' | 'comment' |
                                    // 'count' | 'declare' | 'default' | 'descendant' | 'descendant-or-self' |
                                    // 'descending' | 'div' | 'document' | 'document-node' | 'element' | 'else' |
                                    // 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' | 'except' | 'following' |
                                    // 'following-sibling' | 'for' | 'function' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'if' | 'import' | 'instance' | 'intersect' | 'is' | 'item' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'module' | 'namespace' | 'namespace-node' | 'ne' | 'node' |
                                    // 'only' | 'or' | 'order' | 'ordered' | 'parent' | 'preceding' |
                                    // 'preceding-sibling' | 'processing-instruction' | 'return' | 'satisfies' |
                                    // 'schema-attribute' | 'schema-element' | 'self' | 'some' | 'stable' | 'start' |
                                    // 'switch' | 'text' | 'to' | 'treat' | 'try' | 'typeswitch' | 'union' |
                                    // 'unordered' | 'validate' | 'where' | 'xquery' | '{' | '|' | '||' | '}'
      break;
    case 139:                       // 'namespace'
    case 161:                       // 'processing-instruction'
      lookahead2W(149);             // NCName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' |
                                    // ',' | '-' | '/' | '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' |
                                    // '[' | ']' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' | 'collation' |
                                    // 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' |
                                    // 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' |
                                    // 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' |
                                    // 'satisfies' | 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '{' |
                                    // '|' | '||' | '}'
      break;
    case 86:                        // 'comment'
    case 100:                       // 'document'
    case 151:                       // 'ordered'
    case 175:                       // 'text'
    case 184:                       // 'unordered'
      lookahead2W(148);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '{' | '|' | '||' | '}'
      break;
    case 5:                         // URIQualifiedName
    case 105:                       // 'empty-sequence'
    case 122:                       // 'if'
    case 130:                       // 'item'
    case 174:                       // 'switch'
    case 182:                       // 'typeswitch'
      lookahead2W(140);             // S^WS | EOF | '!' | '!=' | '#' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    case 70:                        // 'ancestor'
    case 71:                        // 'ancestor-or-self'
    case 84:                        // 'child'
    case 95:                        // 'descendant'
    case 96:                        // 'descendant-or-self'
    case 112:                       // 'following'
    case 113:                       // 'following-sibling'
    case 153:                       // 'parent'
    case 157:                       // 'preceding'
    case 158:                       // 'preceding-sibling'
    case 167:                       // 'self'
      lookahead2W(147);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | '::' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' |
                                    // 'and' | 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' |
                                    // 'default' | 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' |
                                    // 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' |
                                    // 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    case 15:                        // QName^Token
    case 72:                        // 'and'
    case 74:                        // 'ascending'
    case 80:                        // 'case'
    case 81:                        // 'cast'
    case 82:                        // 'castable'
    case 85:                        // 'collation'
    case 90:                        // 'count'
    case 93:                        // 'declare'
    case 94:                        // 'default'
    case 97:                        // 'descending'
    case 99:                        // 'div'
    case 101:                       // 'document-node'
    case 103:                       // 'else'
    case 104:                       // 'empty'
    case 107:                       // 'end'
    case 108:                       // 'eq'
    case 109:                       // 'every'
    case 110:                       // 'except'
    case 114:                       // 'for'
    case 115:                       // 'function'
    case 116:                       // 'ge'
    case 118:                       // 'group'
    case 120:                       // 'gt'
    case 121:                       // 'idiv'
    case 123:                       // 'import'
    case 127:                       // 'instance'
    case 128:                       // 'intersect'
    case 129:                       // 'is'
    case 132:                       // 'le'
    case 134:                       // 'let'
    case 135:                       // 'lt'
    case 137:                       // 'mod'
    case 138:                       // 'module'
    case 140:                       // 'namespace-node'
    case 141:                       // 'ne'
    case 145:                       // 'node'
    case 147:                       // 'only'
    case 149:                       // 'or'
    case 150:                       // 'order'
    case 162:                       // 'return'
    case 163:                       // 'satisfies'
    case 165:                       // 'schema-attribute'
    case 166:                       // 'schema-element'
    case 169:                       // 'some'
    case 170:                       // 'stable'
    case 171:                       // 'start'
    case 177:                       // 'to'
    case 178:                       // 'treat'
    case 179:                       // 'try'
    case 183:                       // 'union'
    case 185:                       // 'validate'
    case 189:                       // 'where'
    case 191:                       // 'xquery'
      lookahead2W(144);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    if (lk == 18508                 // 'attribute' 'and'
     || lk == 18534                 // 'element' 'and'
     || lk == 18571                 // 'namespace' 'and'
     || lk == 18593                 // 'processing-instruction' 'and'
     || lk == 19020                 // 'attribute' 'ascending'
     || lk == 19046                 // 'element' 'ascending'
     || lk == 19083                 // 'namespace' 'ascending'
     || lk == 19105                 // 'processing-instruction' 'ascending'
     || lk == 20556                 // 'attribute' 'case'
     || lk == 20582                 // 'element' 'case'
     || lk == 20619                 // 'namespace' 'case'
     || lk == 20641                 // 'processing-instruction' 'case'
     || lk == 20812                 // 'attribute' 'cast'
     || lk == 20838                 // 'element' 'cast'
     || lk == 20875                 // 'namespace' 'cast'
     || lk == 20897                 // 'processing-instruction' 'cast'
     || lk == 21068                 // 'attribute' 'castable'
     || lk == 21094                 // 'element' 'castable'
     || lk == 21131                 // 'namespace' 'castable'
     || lk == 21153                 // 'processing-instruction' 'castable'
     || lk == 21836                 // 'attribute' 'collation'
     || lk == 21862                 // 'element' 'collation'
     || lk == 21899                 // 'namespace' 'collation'
     || lk == 21921                 // 'processing-instruction' 'collation'
     || lk == 23116                 // 'attribute' 'count'
     || lk == 23142                 // 'element' 'count'
     || lk == 23179                 // 'namespace' 'count'
     || lk == 23201                 // 'processing-instruction' 'count'
     || lk == 24140                 // 'attribute' 'default'
     || lk == 24166                 // 'element' 'default'
     || lk == 24203                 // 'namespace' 'default'
     || lk == 24225                 // 'processing-instruction' 'default'
     || lk == 24908                 // 'attribute' 'descending'
     || lk == 24934                 // 'element' 'descending'
     || lk == 24971                 // 'namespace' 'descending'
     || lk == 24993                 // 'processing-instruction' 'descending'
     || lk == 25420                 // 'attribute' 'div'
     || lk == 25446                 // 'element' 'div'
     || lk == 25483                 // 'namespace' 'div'
     || lk == 25505                 // 'processing-instruction' 'div'
     || lk == 26444                 // 'attribute' 'else'
     || lk == 26470                 // 'element' 'else'
     || lk == 26507                 // 'namespace' 'else'
     || lk == 26529                 // 'processing-instruction' 'else'
     || lk == 26700                 // 'attribute' 'empty'
     || lk == 26726                 // 'element' 'empty'
     || lk == 26763                 // 'namespace' 'empty'
     || lk == 26785                 // 'processing-instruction' 'empty'
     || lk == 27468                 // 'attribute' 'end'
     || lk == 27494                 // 'element' 'end'
     || lk == 27531                 // 'namespace' 'end'
     || lk == 27553                 // 'processing-instruction' 'end'
     || lk == 27724                 // 'attribute' 'eq'
     || lk == 27750                 // 'element' 'eq'
     || lk == 27787                 // 'namespace' 'eq'
     || lk == 27809                 // 'processing-instruction' 'eq'
     || lk == 28236                 // 'attribute' 'except'
     || lk == 28262                 // 'element' 'except'
     || lk == 28299                 // 'namespace' 'except'
     || lk == 28321                 // 'processing-instruction' 'except'
     || lk == 29260                 // 'attribute' 'for'
     || lk == 29286                 // 'element' 'for'
     || lk == 29323                 // 'namespace' 'for'
     || lk == 29345                 // 'processing-instruction' 'for'
     || lk == 29772                 // 'attribute' 'ge'
     || lk == 29798                 // 'element' 'ge'
     || lk == 29835                 // 'namespace' 'ge'
     || lk == 29857                 // 'processing-instruction' 'ge'
     || lk == 30284                 // 'attribute' 'group'
     || lk == 30310                 // 'element' 'group'
     || lk == 30347                 // 'namespace' 'group'
     || lk == 30369                 // 'processing-instruction' 'group'
     || lk == 30796                 // 'attribute' 'gt'
     || lk == 30822                 // 'element' 'gt'
     || lk == 30859                 // 'namespace' 'gt'
     || lk == 30881                 // 'processing-instruction' 'gt'
     || lk == 31052                 // 'attribute' 'idiv'
     || lk == 31078                 // 'element' 'idiv'
     || lk == 31115                 // 'namespace' 'idiv'
     || lk == 31137                 // 'processing-instruction' 'idiv'
     || lk == 32588                 // 'attribute' 'instance'
     || lk == 32614                 // 'element' 'instance'
     || lk == 32651                 // 'namespace' 'instance'
     || lk == 32673                 // 'processing-instruction' 'instance'
     || lk == 32844                 // 'attribute' 'intersect'
     || lk == 32870                 // 'element' 'intersect'
     || lk == 32907                 // 'namespace' 'intersect'
     || lk == 32929                 // 'processing-instruction' 'intersect'
     || lk == 33100                 // 'attribute' 'is'
     || lk == 33126                 // 'element' 'is'
     || lk == 33163                 // 'namespace' 'is'
     || lk == 33185                 // 'processing-instruction' 'is'
     || lk == 33868                 // 'attribute' 'le'
     || lk == 33894                 // 'element' 'le'
     || lk == 33931                 // 'namespace' 'le'
     || lk == 33953                 // 'processing-instruction' 'le'
     || lk == 34380                 // 'attribute' 'let'
     || lk == 34406                 // 'element' 'let'
     || lk == 34443                 // 'namespace' 'let'
     || lk == 34465                 // 'processing-instruction' 'let'
     || lk == 34636                 // 'attribute' 'lt'
     || lk == 34662                 // 'element' 'lt'
     || lk == 34699                 // 'namespace' 'lt'
     || lk == 34721                 // 'processing-instruction' 'lt'
     || lk == 35148                 // 'attribute' 'mod'
     || lk == 35174                 // 'element' 'mod'
     || lk == 35211                 // 'namespace' 'mod'
     || lk == 35233                 // 'processing-instruction' 'mod'
     || lk == 36172                 // 'attribute' 'ne'
     || lk == 36198                 // 'element' 'ne'
     || lk == 36235                 // 'namespace' 'ne'
     || lk == 36257                 // 'processing-instruction' 'ne'
     || lk == 37708                 // 'attribute' 'only'
     || lk == 37734                 // 'element' 'only'
     || lk == 37771                 // 'namespace' 'only'
     || lk == 37793                 // 'processing-instruction' 'only'
     || lk == 38220                 // 'attribute' 'or'
     || lk == 38246                 // 'element' 'or'
     || lk == 38283                 // 'namespace' 'or'
     || lk == 38305                 // 'processing-instruction' 'or'
     || lk == 38476                 // 'attribute' 'order'
     || lk == 38502                 // 'element' 'order'
     || lk == 38539                 // 'namespace' 'order'
     || lk == 38561                 // 'processing-instruction' 'order'
     || lk == 41548                 // 'attribute' 'return'
     || lk == 41574                 // 'element' 'return'
     || lk == 41611                 // 'namespace' 'return'
     || lk == 41633                 // 'processing-instruction' 'return'
     || lk == 41804                 // 'attribute' 'satisfies'
     || lk == 41830                 // 'element' 'satisfies'
     || lk == 41867                 // 'namespace' 'satisfies'
     || lk == 41889                 // 'processing-instruction' 'satisfies'
     || lk == 43596                 // 'attribute' 'stable'
     || lk == 43622                 // 'element' 'stable'
     || lk == 43659                 // 'namespace' 'stable'
     || lk == 43681                 // 'processing-instruction' 'stable'
     || lk == 43852                 // 'attribute' 'start'
     || lk == 43878                 // 'element' 'start'
     || lk == 43915                 // 'namespace' 'start'
     || lk == 43937                 // 'processing-instruction' 'start'
     || lk == 45388                 // 'attribute' 'to'
     || lk == 45414                 // 'element' 'to'
     || lk == 45451                 // 'namespace' 'to'
     || lk == 45473                 // 'processing-instruction' 'to'
     || lk == 45644                 // 'attribute' 'treat'
     || lk == 45670                 // 'element' 'treat'
     || lk == 45707                 // 'namespace' 'treat'
     || lk == 45729                 // 'processing-instruction' 'treat'
     || lk == 46924                 // 'attribute' 'union'
     || lk == 46950                 // 'element' 'union'
     || lk == 46987                 // 'namespace' 'union'
     || lk == 47009                 // 'processing-instruction' 'union'
     || lk == 48460                 // 'attribute' 'where'
     || lk == 48486                 // 'element' 'where'
     || lk == 48523                 // 'namespace' 'where'
     || lk == 48545)                // 'processing-instruction' 'where'
    {
      lk = memoized(2, e0);
      if (lk == 0)
      {
        int b0A = b0; int e0A = e0; int l1A = l1;
        int b1A = b1; int e1A = e1; int l2A = l2;
        int b2A = b2; int e2A = e2;
        try
        {
          try_PostfixExpr();
          lk = -1;
        }
        catch (ParseException p1A)
        {
          lk = -2;
        }
        b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
        b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
        b2 = b2A; e2 = e2A; end = e2A; }}
        memoize(2, e0, lk);
      }
    }
    switch (lk)
    {
    case -1:
    case 1:                         // IntegerLiteral
    case 2:                         // DecimalLiteral
    case 3:                         // DoubleLiteral
    case 4:                         // StringLiteral
    case 30:                        // '$'
    case 31:                        // '%'
    case 33:                        // '('
    case 42:                        // '.'
    case 51:                        // '<'
    case 52:                        // '<!--'
    case 57:                        // '<?'
    case 1356:                      // 'attribute' URIQualifiedName
    case 1382:                      // 'element' URIQualifiedName
    case 3723:                      // 'namespace' NCName^Token
    case 3745:                      // 'processing-instruction' NCName^Token
    case 3916:                      // 'attribute' QName^Token
    case 3942:                      // 'element' QName^Token
    case 7173:                      // URIQualifiedName '#'
    case 7183:                      // QName^Token '#'
    case 7238:                      // 'ancestor' '#'
    case 7239:                      // 'ancestor-or-self' '#'
    case 7240:                      // 'and' '#'
    case 7242:                      // 'ascending' '#'
    case 7244:                      // 'attribute' '#'
    case 7248:                      // 'case' '#'
    case 7249:                      // 'cast' '#'
    case 7250:                      // 'castable' '#'
    case 7252:                      // 'child' '#'
    case 7253:                      // 'collation' '#'
    case 7254:                      // 'comment' '#'
    case 7258:                      // 'count' '#'
    case 7261:                      // 'declare' '#'
    case 7262:                      // 'default' '#'
    case 7263:                      // 'descendant' '#'
    case 7264:                      // 'descendant-or-self' '#'
    case 7265:                      // 'descending' '#'
    case 7267:                      // 'div' '#'
    case 7268:                      // 'document' '#'
    case 7269:                      // 'document-node' '#'
    case 7270:                      // 'element' '#'
    case 7271:                      // 'else' '#'
    case 7272:                      // 'empty' '#'
    case 7273:                      // 'empty-sequence' '#'
    case 7275:                      // 'end' '#'
    case 7276:                      // 'eq' '#'
    case 7277:                      // 'every' '#'
    case 7278:                      // 'except' '#'
    case 7280:                      // 'following' '#'
    case 7281:                      // 'following-sibling' '#'
    case 7282:                      // 'for' '#'
    case 7283:                      // 'function' '#'
    case 7284:                      // 'ge' '#'
    case 7286:                      // 'group' '#'
    case 7288:                      // 'gt' '#'
    case 7289:                      // 'idiv' '#'
    case 7290:                      // 'if' '#'
    case 7291:                      // 'import' '#'
    case 7295:                      // 'instance' '#'
    case 7296:                      // 'intersect' '#'
    case 7297:                      // 'is' '#'
    case 7298:                      // 'item' '#'
    case 7300:                      // 'le' '#'
    case 7302:                      // 'let' '#'
    case 7303:                      // 'lt' '#'
    case 7305:                      // 'mod' '#'
    case 7306:                      // 'module' '#'
    case 7307:                      // 'namespace' '#'
    case 7308:                      // 'namespace-node' '#'
    case 7309:                      // 'ne' '#'
    case 7313:                      // 'node' '#'
    case 7315:                      // 'only' '#'
    case 7317:                      // 'or' '#'
    case 7318:                      // 'order' '#'
    case 7319:                      // 'ordered' '#'
    case 7321:                      // 'parent' '#'
    case 7325:                      // 'preceding' '#'
    case 7326:                      // 'preceding-sibling' '#'
    case 7329:                      // 'processing-instruction' '#'
    case 7330:                      // 'return' '#'
    case 7331:                      // 'satisfies' '#'
    case 7333:                      // 'schema-attribute' '#'
    case 7334:                      // 'schema-element' '#'
    case 7335:                      // 'self' '#'
    case 7337:                      // 'some' '#'
    case 7338:                      // 'stable' '#'
    case 7339:                      // 'start' '#'
    case 7342:                      // 'switch' '#'
    case 7343:                      // 'text' '#'
    case 7345:                      // 'to' '#'
    case 7346:                      // 'treat' '#'
    case 7347:                      // 'try' '#'
    case 7350:                      // 'typeswitch' '#'
    case 7351:                      // 'union' '#'
    case 7352:                      // 'unordered' '#'
    case 7353:                      // 'validate' '#'
    case 7357:                      // 'where' '#'
    case 7359:                      // 'xquery' '#'
    case 8463:                      // QName^Token '('
    case 8518:                      // 'ancestor' '('
    case 8519:                      // 'ancestor-or-self' '('
    case 8520:                      // 'and' '('
    case 8522:                      // 'ascending' '('
    case 8528:                      // 'case' '('
    case 8529:                      // 'cast' '('
    case 8530:                      // 'castable' '('
    case 8532:                      // 'child' '('
    case 8533:                      // 'collation' '('
    case 8538:                      // 'count' '('
    case 8541:                      // 'declare' '('
    case 8542:                      // 'default' '('
    case 8543:                      // 'descendant' '('
    case 8544:                      // 'descendant-or-self' '('
    case 8545:                      // 'descending' '('
    case 8547:                      // 'div' '('
    case 8548:                      // 'document' '('
    case 8551:                      // 'else' '('
    case 8552:                      // 'empty' '('
    case 8555:                      // 'end' '('
    case 8556:                      // 'eq' '('
    case 8557:                      // 'every' '('
    case 8558:                      // 'except' '('
    case 8560:                      // 'following' '('
    case 8561:                      // 'following-sibling' '('
    case 8562:                      // 'for' '('
    case 8563:                      // 'function' '('
    case 8564:                      // 'ge' '('
    case 8566:                      // 'group' '('
    case 8568:                      // 'gt' '('
    case 8569:                      // 'idiv' '('
    case 8571:                      // 'import' '('
    case 8575:                      // 'instance' '('
    case 8576:                      // 'intersect' '('
    case 8577:                      // 'is' '('
    case 8580:                      // 'le' '('
    case 8582:                      // 'let' '('
    case 8583:                      // 'lt' '('
    case 8585:                      // 'mod' '('
    case 8586:                      // 'module' '('
    case 8587:                      // 'namespace' '('
    case 8589:                      // 'ne' '('
    case 8595:                      // 'only' '('
    case 8597:                      // 'or' '('
    case 8598:                      // 'order' '('
    case 8599:                      // 'ordered' '('
    case 8601:                      // 'parent' '('
    case 8605:                      // 'preceding' '('
    case 8606:                      // 'preceding-sibling' '('
    case 8610:                      // 'return' '('
    case 8611:                      // 'satisfies' '('
    case 8615:                      // 'self' '('
    case 8617:                      // 'some' '('
    case 8618:                      // 'stable' '('
    case 8619:                      // 'start' '('
    case 8625:                      // 'to' '('
    case 8626:                      // 'treat' '('
    case 8627:                      // 'try' '('
    case 8631:                      // 'union' '('
    case 8632:                      // 'unordered' '('
    case 8633:                      // 'validate' '('
    case 8637:                      // 'where' '('
    case 8639:                      // 'xquery' '('
    case 17996:                     // 'attribute' 'ancestor'
    case 18022:                     // 'element' 'ancestor'
    case 18252:                     // 'attribute' 'ancestor-or-self'
    case 18278:                     // 'element' 'ancestor-or-self'
    case 19532:                     // 'attribute' 'attribute'
    case 19558:                     // 'element' 'attribute'
    case 21580:                     // 'attribute' 'child'
    case 21606:                     // 'element' 'child'
    case 22092:                     // 'attribute' 'comment'
    case 22118:                     // 'element' 'comment'
    case 23884:                     // 'attribute' 'declare'
    case 23910:                     // 'element' 'declare'
    case 24396:                     // 'attribute' 'descendant'
    case 24422:                     // 'element' 'descendant'
    case 24652:                     // 'attribute' 'descendant-or-self'
    case 24678:                     // 'element' 'descendant-or-self'
    case 25676:                     // 'attribute' 'document'
    case 25702:                     // 'element' 'document'
    case 25932:                     // 'attribute' 'document-node'
    case 25958:                     // 'element' 'document-node'
    case 26188:                     // 'attribute' 'element'
    case 26214:                     // 'element' 'element'
    case 26956:                     // 'attribute' 'empty-sequence'
    case 26982:                     // 'element' 'empty-sequence'
    case 27980:                     // 'attribute' 'every'
    case 28006:                     // 'element' 'every'
    case 28748:                     // 'attribute' 'following'
    case 28774:                     // 'element' 'following'
    case 29004:                     // 'attribute' 'following-sibling'
    case 29030:                     // 'element' 'following-sibling'
    case 29516:                     // 'attribute' 'function'
    case 29542:                     // 'element' 'function'
    case 31308:                     // 'attribute' 'if'
    case 31334:                     // 'element' 'if'
    case 31564:                     // 'attribute' 'import'
    case 31590:                     // 'element' 'import'
    case 33356:                     // 'attribute' 'item'
    case 33382:                     // 'element' 'item'
    case 35404:                     // 'attribute' 'module'
    case 35430:                     // 'element' 'module'
    case 35660:                     // 'attribute' 'namespace'
    case 35686:                     // 'element' 'namespace'
    case 35916:                     // 'attribute' 'namespace-node'
    case 35942:                     // 'element' 'namespace-node'
    case 37196:                     // 'attribute' 'node'
    case 37222:                     // 'element' 'node'
    case 38732:                     // 'attribute' 'ordered'
    case 38758:                     // 'element' 'ordered'
    case 39244:                     // 'attribute' 'parent'
    case 39270:                     // 'element' 'parent'
    case 40268:                     // 'attribute' 'preceding'
    case 40294:                     // 'element' 'preceding'
    case 40524:                     // 'attribute' 'preceding-sibling'
    case 40550:                     // 'element' 'preceding-sibling'
    case 41292:                     // 'attribute' 'processing-instruction'
    case 41318:                     // 'element' 'processing-instruction'
    case 42316:                     // 'attribute' 'schema-attribute'
    case 42342:                     // 'element' 'schema-attribute'
    case 42572:                     // 'attribute' 'schema-element'
    case 42598:                     // 'element' 'schema-element'
    case 42828:                     // 'attribute' 'self'
    case 42854:                     // 'element' 'self'
    case 43340:                     // 'attribute' 'some'
    case 43366:                     // 'element' 'some'
    case 44620:                     // 'attribute' 'switch'
    case 44646:                     // 'element' 'switch'
    case 44876:                     // 'attribute' 'text'
    case 44902:                     // 'element' 'text'
    case 45900:                     // 'attribute' 'try'
    case 45926:                     // 'element' 'try'
    case 46668:                     // 'attribute' 'typeswitch'
    case 46694:                     // 'element' 'typeswitch'
    case 47180:                     // 'attribute' 'unordered'
    case 47206:                     // 'element' 'unordered'
    case 47436:                     // 'attribute' 'validate'
    case 47462:                     // 'element' 'validate'
    case 48972:                     // 'attribute' 'xquery'
    case 48998:                     // 'element' 'xquery'
    case 49484:                     // 'attribute' '{'
    case 49494:                     // 'comment' '{'
    case 49508:                     // 'document' '{'
    case 49510:                     // 'element' '{'
    case 49547:                     // 'namespace' '{'
    case 49559:                     // 'ordered' '{'
    case 49569:                     // 'processing-instruction' '{'
    case 49583:                     // 'text' '{'
    case 49592:                     // 'unordered' '{'
      parse_PostfixExpr();
      break;
    default:
      parse_AxisStep();
    }
    eventHandler.endNonterminal("StepExpr", e0);
  }

  private void try_StepExpr()
  {
    switch (l1)
    {
    case 76:                        // 'attribute'
      lookahead2W(173);             // URIQualifiedName | QName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' |
                                    // ')' | '*' | '+' | ',' | '-' | '/' | '//' | '::' | ';' | '<' | '<<' | '<=' | '=' |
                                    // '>' | '>=' | '>>' | '[' | ']' | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery' | '{' |
                                    // '|' | '||' | '}'
      break;
    case 102:                       // 'element'
      lookahead2W(172);             // URIQualifiedName | QName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' |
                                    // ')' | '*' | '+' | ',' | '-' | '/' | '//' | ';' | '<' | '<<' | '<=' | '=' | '>' |
                                    // '>=' | '>>' | '[' | ']' | 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' |
                                    // 'attribute' | 'case' | 'cast' | 'castable' | 'child' | 'collation' | 'comment' |
                                    // 'count' | 'declare' | 'default' | 'descendant' | 'descendant-or-self' |
                                    // 'descending' | 'div' | 'document' | 'document-node' | 'element' | 'else' |
                                    // 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' | 'except' | 'following' |
                                    // 'following-sibling' | 'for' | 'function' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'if' | 'import' | 'instance' | 'intersect' | 'is' | 'item' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'module' | 'namespace' | 'namespace-node' | 'ne' | 'node' |
                                    // 'only' | 'or' | 'order' | 'ordered' | 'parent' | 'preceding' |
                                    // 'preceding-sibling' | 'processing-instruction' | 'return' | 'satisfies' |
                                    // 'schema-attribute' | 'schema-element' | 'self' | 'some' | 'stable' | 'start' |
                                    // 'switch' | 'text' | 'to' | 'treat' | 'try' | 'typeswitch' | 'union' |
                                    // 'unordered' | 'validate' | 'where' | 'xquery' | '{' | '|' | '||' | '}'
      break;
    case 139:                       // 'namespace'
    case 161:                       // 'processing-instruction'
      lookahead2W(149);             // NCName^Token | S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' |
                                    // ',' | '-' | '/' | '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' |
                                    // '[' | ']' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' | 'collation' |
                                    // 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' |
                                    // 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' |
                                    // 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' |
                                    // 'satisfies' | 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '{' |
                                    // '|' | '||' | '}'
      break;
    case 86:                        // 'comment'
    case 100:                       // 'document'
    case 151:                       // 'ordered'
    case 175:                       // 'text'
    case 184:                       // 'unordered'
      lookahead2W(148);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '{' | '|' | '||' | '}'
      break;
    case 5:                         // URIQualifiedName
    case 105:                       // 'empty-sequence'
    case 122:                       // 'if'
    case 130:                       // 'item'
    case 174:                       // 'switch'
    case 182:                       // 'typeswitch'
      lookahead2W(140);             // S^WS | EOF | '!' | '!=' | '#' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    case 70:                        // 'ancestor'
    case 71:                        // 'ancestor-or-self'
    case 84:                        // 'child'
    case 95:                        // 'descendant'
    case 96:                        // 'descendant-or-self'
    case 112:                       // 'following'
    case 113:                       // 'following-sibling'
    case 153:                       // 'parent'
    case 157:                       // 'preceding'
    case 158:                       // 'preceding-sibling'
    case 167:                       // 'self'
      lookahead2W(147);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | '::' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' |
                                    // 'and' | 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' |
                                    // 'default' | 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' |
                                    // 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' |
                                    // 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    case 15:                        // QName^Token
    case 72:                        // 'and'
    case 74:                        // 'ascending'
    case 80:                        // 'case'
    case 81:                        // 'cast'
    case 82:                        // 'castable'
    case 85:                        // 'collation'
    case 90:                        // 'count'
    case 93:                        // 'declare'
    case 94:                        // 'default'
    case 97:                        // 'descending'
    case 99:                        // 'div'
    case 101:                       // 'document-node'
    case 103:                       // 'else'
    case 104:                       // 'empty'
    case 107:                       // 'end'
    case 108:                       // 'eq'
    case 109:                       // 'every'
    case 110:                       // 'except'
    case 114:                       // 'for'
    case 115:                       // 'function'
    case 116:                       // 'ge'
    case 118:                       // 'group'
    case 120:                       // 'gt'
    case 121:                       // 'idiv'
    case 123:                       // 'import'
    case 127:                       // 'instance'
    case 128:                       // 'intersect'
    case 129:                       // 'is'
    case 132:                       // 'le'
    case 134:                       // 'let'
    case 135:                       // 'lt'
    case 137:                       // 'mod'
    case 138:                       // 'module'
    case 140:                       // 'namespace-node'
    case 141:                       // 'ne'
    case 145:                       // 'node'
    case 147:                       // 'only'
    case 149:                       // 'or'
    case 150:                       // 'order'
    case 162:                       // 'return'
    case 163:                       // 'satisfies'
    case 165:                       // 'schema-attribute'
    case 166:                       // 'schema-element'
    case 169:                       // 'some'
    case 170:                       // 'stable'
    case 171:                       // 'start'
    case 177:                       // 'to'
    case 178:                       // 'treat'
    case 179:                       // 'try'
    case 183:                       // 'union'
    case 185:                       // 'validate'
    case 189:                       // 'where'
    case 191:                       // 'xquery'
      lookahead2W(144);             // S^WS | EOF | '!' | '!=' | '#' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' |
                                    // '//' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    if (lk == 18508                 // 'attribute' 'and'
     || lk == 18534                 // 'element' 'and'
     || lk == 18571                 // 'namespace' 'and'
     || lk == 18593                 // 'processing-instruction' 'and'
     || lk == 19020                 // 'attribute' 'ascending'
     || lk == 19046                 // 'element' 'ascending'
     || lk == 19083                 // 'namespace' 'ascending'
     || lk == 19105                 // 'processing-instruction' 'ascending'
     || lk == 20556                 // 'attribute' 'case'
     || lk == 20582                 // 'element' 'case'
     || lk == 20619                 // 'namespace' 'case'
     || lk == 20641                 // 'processing-instruction' 'case'
     || lk == 20812                 // 'attribute' 'cast'
     || lk == 20838                 // 'element' 'cast'
     || lk == 20875                 // 'namespace' 'cast'
     || lk == 20897                 // 'processing-instruction' 'cast'
     || lk == 21068                 // 'attribute' 'castable'
     || lk == 21094                 // 'element' 'castable'
     || lk == 21131                 // 'namespace' 'castable'
     || lk == 21153                 // 'processing-instruction' 'castable'
     || lk == 21836                 // 'attribute' 'collation'
     || lk == 21862                 // 'element' 'collation'
     || lk == 21899                 // 'namespace' 'collation'
     || lk == 21921                 // 'processing-instruction' 'collation'
     || lk == 23116                 // 'attribute' 'count'
     || lk == 23142                 // 'element' 'count'
     || lk == 23179                 // 'namespace' 'count'
     || lk == 23201                 // 'processing-instruction' 'count'
     || lk == 24140                 // 'attribute' 'default'
     || lk == 24166                 // 'element' 'default'
     || lk == 24203                 // 'namespace' 'default'
     || lk == 24225                 // 'processing-instruction' 'default'
     || lk == 24908                 // 'attribute' 'descending'
     || lk == 24934                 // 'element' 'descending'
     || lk == 24971                 // 'namespace' 'descending'
     || lk == 24993                 // 'processing-instruction' 'descending'
     || lk == 25420                 // 'attribute' 'div'
     || lk == 25446                 // 'element' 'div'
     || lk == 25483                 // 'namespace' 'div'
     || lk == 25505                 // 'processing-instruction' 'div'
     || lk == 26444                 // 'attribute' 'else'
     || lk == 26470                 // 'element' 'else'
     || lk == 26507                 // 'namespace' 'else'
     || lk == 26529                 // 'processing-instruction' 'else'
     || lk == 26700                 // 'attribute' 'empty'
     || lk == 26726                 // 'element' 'empty'
     || lk == 26763                 // 'namespace' 'empty'
     || lk == 26785                 // 'processing-instruction' 'empty'
     || lk == 27468                 // 'attribute' 'end'
     || lk == 27494                 // 'element' 'end'
     || lk == 27531                 // 'namespace' 'end'
     || lk == 27553                 // 'processing-instruction' 'end'
     || lk == 27724                 // 'attribute' 'eq'
     || lk == 27750                 // 'element' 'eq'
     || lk == 27787                 // 'namespace' 'eq'
     || lk == 27809                 // 'processing-instruction' 'eq'
     || lk == 28236                 // 'attribute' 'except'
     || lk == 28262                 // 'element' 'except'
     || lk == 28299                 // 'namespace' 'except'
     || lk == 28321                 // 'processing-instruction' 'except'
     || lk == 29260                 // 'attribute' 'for'
     || lk == 29286                 // 'element' 'for'
     || lk == 29323                 // 'namespace' 'for'
     || lk == 29345                 // 'processing-instruction' 'for'
     || lk == 29772                 // 'attribute' 'ge'
     || lk == 29798                 // 'element' 'ge'
     || lk == 29835                 // 'namespace' 'ge'
     || lk == 29857                 // 'processing-instruction' 'ge'
     || lk == 30284                 // 'attribute' 'group'
     || lk == 30310                 // 'element' 'group'
     || lk == 30347                 // 'namespace' 'group'
     || lk == 30369                 // 'processing-instruction' 'group'
     || lk == 30796                 // 'attribute' 'gt'
     || lk == 30822                 // 'element' 'gt'
     || lk == 30859                 // 'namespace' 'gt'
     || lk == 30881                 // 'processing-instruction' 'gt'
     || lk == 31052                 // 'attribute' 'idiv'
     || lk == 31078                 // 'element' 'idiv'
     || lk == 31115                 // 'namespace' 'idiv'
     || lk == 31137                 // 'processing-instruction' 'idiv'
     || lk == 32588                 // 'attribute' 'instance'
     || lk == 32614                 // 'element' 'instance'
     || lk == 32651                 // 'namespace' 'instance'
     || lk == 32673                 // 'processing-instruction' 'instance'
     || lk == 32844                 // 'attribute' 'intersect'
     || lk == 32870                 // 'element' 'intersect'
     || lk == 32907                 // 'namespace' 'intersect'
     || lk == 32929                 // 'processing-instruction' 'intersect'
     || lk == 33100                 // 'attribute' 'is'
     || lk == 33126                 // 'element' 'is'
     || lk == 33163                 // 'namespace' 'is'
     || lk == 33185                 // 'processing-instruction' 'is'
     || lk == 33868                 // 'attribute' 'le'
     || lk == 33894                 // 'element' 'le'
     || lk == 33931                 // 'namespace' 'le'
     || lk == 33953                 // 'processing-instruction' 'le'
     || lk == 34380                 // 'attribute' 'let'
     || lk == 34406                 // 'element' 'let'
     || lk == 34443                 // 'namespace' 'let'
     || lk == 34465                 // 'processing-instruction' 'let'
     || lk == 34636                 // 'attribute' 'lt'
     || lk == 34662                 // 'element' 'lt'
     || lk == 34699                 // 'namespace' 'lt'
     || lk == 34721                 // 'processing-instruction' 'lt'
     || lk == 35148                 // 'attribute' 'mod'
     || lk == 35174                 // 'element' 'mod'
     || lk == 35211                 // 'namespace' 'mod'
     || lk == 35233                 // 'processing-instruction' 'mod'
     || lk == 36172                 // 'attribute' 'ne'
     || lk == 36198                 // 'element' 'ne'
     || lk == 36235                 // 'namespace' 'ne'
     || lk == 36257                 // 'processing-instruction' 'ne'
     || lk == 37708                 // 'attribute' 'only'
     || lk == 37734                 // 'element' 'only'
     || lk == 37771                 // 'namespace' 'only'
     || lk == 37793                 // 'processing-instruction' 'only'
     || lk == 38220                 // 'attribute' 'or'
     || lk == 38246                 // 'element' 'or'
     || lk == 38283                 // 'namespace' 'or'
     || lk == 38305                 // 'processing-instruction' 'or'
     || lk == 38476                 // 'attribute' 'order'
     || lk == 38502                 // 'element' 'order'
     || lk == 38539                 // 'namespace' 'order'
     || lk == 38561                 // 'processing-instruction' 'order'
     || lk == 41548                 // 'attribute' 'return'
     || lk == 41574                 // 'element' 'return'
     || lk == 41611                 // 'namespace' 'return'
     || lk == 41633                 // 'processing-instruction' 'return'
     || lk == 41804                 // 'attribute' 'satisfies'
     || lk == 41830                 // 'element' 'satisfies'
     || lk == 41867                 // 'namespace' 'satisfies'
     || lk == 41889                 // 'processing-instruction' 'satisfies'
     || lk == 43596                 // 'attribute' 'stable'
     || lk == 43622                 // 'element' 'stable'
     || lk == 43659                 // 'namespace' 'stable'
     || lk == 43681                 // 'processing-instruction' 'stable'
     || lk == 43852                 // 'attribute' 'start'
     || lk == 43878                 // 'element' 'start'
     || lk == 43915                 // 'namespace' 'start'
     || lk == 43937                 // 'processing-instruction' 'start'
     || lk == 45388                 // 'attribute' 'to'
     || lk == 45414                 // 'element' 'to'
     || lk == 45451                 // 'namespace' 'to'
     || lk == 45473                 // 'processing-instruction' 'to'
     || lk == 45644                 // 'attribute' 'treat'
     || lk == 45670                 // 'element' 'treat'
     || lk == 45707                 // 'namespace' 'treat'
     || lk == 45729                 // 'processing-instruction' 'treat'
     || lk == 46924                 // 'attribute' 'union'
     || lk == 46950                 // 'element' 'union'
     || lk == 46987                 // 'namespace' 'union'
     || lk == 47009                 // 'processing-instruction' 'union'
     || lk == 48460                 // 'attribute' 'where'
     || lk == 48486                 // 'element' 'where'
     || lk == 48523                 // 'namespace' 'where'
     || lk == 48545)                // 'processing-instruction' 'where'
    {
      lk = memoized(2, e0);
      if (lk == 0)
      {
        int b0A = b0; int e0A = e0; int l1A = l1;
        int b1A = b1; int e1A = e1; int l2A = l2;
        int b2A = b2; int e2A = e2;
        try
        {
          try_PostfixExpr();
          memoize(2, e0A, -1);
          lk = -3;
        }
        catch (ParseException p1A)
        {
          lk = -2;
          b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
          b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
          b2 = b2A; e2 = e2A; end = e2A; }}
          memoize(2, e0A, -2);
        }
      }
    }
    switch (lk)
    {
    case -1:
    case 1:                         // IntegerLiteral
    case 2:                         // DecimalLiteral
    case 3:                         // DoubleLiteral
    case 4:                         // StringLiteral
    case 30:                        // '$'
    case 31:                        // '%'
    case 33:                        // '('
    case 42:                        // '.'
    case 51:                        // '<'
    case 52:                        // '<!--'
    case 57:                        // '<?'
    case 1356:                      // 'attribute' URIQualifiedName
    case 1382:                      // 'element' URIQualifiedName
    case 3723:                      // 'namespace' NCName^Token
    case 3745:                      // 'processing-instruction' NCName^Token
    case 3916:                      // 'attribute' QName^Token
    case 3942:                      // 'element' QName^Token
    case 7173:                      // URIQualifiedName '#'
    case 7183:                      // QName^Token '#'
    case 7238:                      // 'ancestor' '#'
    case 7239:                      // 'ancestor-or-self' '#'
    case 7240:                      // 'and' '#'
    case 7242:                      // 'ascending' '#'
    case 7244:                      // 'attribute' '#'
    case 7248:                      // 'case' '#'
    case 7249:                      // 'cast' '#'
    case 7250:                      // 'castable' '#'
    case 7252:                      // 'child' '#'
    case 7253:                      // 'collation' '#'
    case 7254:                      // 'comment' '#'
    case 7258:                      // 'count' '#'
    case 7261:                      // 'declare' '#'
    case 7262:                      // 'default' '#'
    case 7263:                      // 'descendant' '#'
    case 7264:                      // 'descendant-or-self' '#'
    case 7265:                      // 'descending' '#'
    case 7267:                      // 'div' '#'
    case 7268:                      // 'document' '#'
    case 7269:                      // 'document-node' '#'
    case 7270:                      // 'element' '#'
    case 7271:                      // 'else' '#'
    case 7272:                      // 'empty' '#'
    case 7273:                      // 'empty-sequence' '#'
    case 7275:                      // 'end' '#'
    case 7276:                      // 'eq' '#'
    case 7277:                      // 'every' '#'
    case 7278:                      // 'except' '#'
    case 7280:                      // 'following' '#'
    case 7281:                      // 'following-sibling' '#'
    case 7282:                      // 'for' '#'
    case 7283:                      // 'function' '#'
    case 7284:                      // 'ge' '#'
    case 7286:                      // 'group' '#'
    case 7288:                      // 'gt' '#'
    case 7289:                      // 'idiv' '#'
    case 7290:                      // 'if' '#'
    case 7291:                      // 'import' '#'
    case 7295:                      // 'instance' '#'
    case 7296:                      // 'intersect' '#'
    case 7297:                      // 'is' '#'
    case 7298:                      // 'item' '#'
    case 7300:                      // 'le' '#'
    case 7302:                      // 'let' '#'
    case 7303:                      // 'lt' '#'
    case 7305:                      // 'mod' '#'
    case 7306:                      // 'module' '#'
    case 7307:                      // 'namespace' '#'
    case 7308:                      // 'namespace-node' '#'
    case 7309:                      // 'ne' '#'
    case 7313:                      // 'node' '#'
    case 7315:                      // 'only' '#'
    case 7317:                      // 'or' '#'
    case 7318:                      // 'order' '#'
    case 7319:                      // 'ordered' '#'
    case 7321:                      // 'parent' '#'
    case 7325:                      // 'preceding' '#'
    case 7326:                      // 'preceding-sibling' '#'
    case 7329:                      // 'processing-instruction' '#'
    case 7330:                      // 'return' '#'
    case 7331:                      // 'satisfies' '#'
    case 7333:                      // 'schema-attribute' '#'
    case 7334:                      // 'schema-element' '#'
    case 7335:                      // 'self' '#'
    case 7337:                      // 'some' '#'
    case 7338:                      // 'stable' '#'
    case 7339:                      // 'start' '#'
    case 7342:                      // 'switch' '#'
    case 7343:                      // 'text' '#'
    case 7345:                      // 'to' '#'
    case 7346:                      // 'treat' '#'
    case 7347:                      // 'try' '#'
    case 7350:                      // 'typeswitch' '#'
    case 7351:                      // 'union' '#'
    case 7352:                      // 'unordered' '#'
    case 7353:                      // 'validate' '#'
    case 7357:                      // 'where' '#'
    case 7359:                      // 'xquery' '#'
    case 8463:                      // QName^Token '('
    case 8518:                      // 'ancestor' '('
    case 8519:                      // 'ancestor-or-self' '('
    case 8520:                      // 'and' '('
    case 8522:                      // 'ascending' '('
    case 8528:                      // 'case' '('
    case 8529:                      // 'cast' '('
    case 8530:                      // 'castable' '('
    case 8532:                      // 'child' '('
    case 8533:                      // 'collation' '('
    case 8538:                      // 'count' '('
    case 8541:                      // 'declare' '('
    case 8542:                      // 'default' '('
    case 8543:                      // 'descendant' '('
    case 8544:                      // 'descendant-or-self' '('
    case 8545:                      // 'descending' '('
    case 8547:                      // 'div' '('
    case 8548:                      // 'document' '('
    case 8551:                      // 'else' '('
    case 8552:                      // 'empty' '('
    case 8555:                      // 'end' '('
    case 8556:                      // 'eq' '('
    case 8557:                      // 'every' '('
    case 8558:                      // 'except' '('
    case 8560:                      // 'following' '('
    case 8561:                      // 'following-sibling' '('
    case 8562:                      // 'for' '('
    case 8563:                      // 'function' '('
    case 8564:                      // 'ge' '('
    case 8566:                      // 'group' '('
    case 8568:                      // 'gt' '('
    case 8569:                      // 'idiv' '('
    case 8571:                      // 'import' '('
    case 8575:                      // 'instance' '('
    case 8576:                      // 'intersect' '('
    case 8577:                      // 'is' '('
    case 8580:                      // 'le' '('
    case 8582:                      // 'let' '('
    case 8583:                      // 'lt' '('
    case 8585:                      // 'mod' '('
    case 8586:                      // 'module' '('
    case 8587:                      // 'namespace' '('
    case 8589:                      // 'ne' '('
    case 8595:                      // 'only' '('
    case 8597:                      // 'or' '('
    case 8598:                      // 'order' '('
    case 8599:                      // 'ordered' '('
    case 8601:                      // 'parent' '('
    case 8605:                      // 'preceding' '('
    case 8606:                      // 'preceding-sibling' '('
    case 8610:                      // 'return' '('
    case 8611:                      // 'satisfies' '('
    case 8615:                      // 'self' '('
    case 8617:                      // 'some' '('
    case 8618:                      // 'stable' '('
    case 8619:                      // 'start' '('
    case 8625:                      // 'to' '('
    case 8626:                      // 'treat' '('
    case 8627:                      // 'try' '('
    case 8631:                      // 'union' '('
    case 8632:                      // 'unordered' '('
    case 8633:                      // 'validate' '('
    case 8637:                      // 'where' '('
    case 8639:                      // 'xquery' '('
    case 17996:                     // 'attribute' 'ancestor'
    case 18022:                     // 'element' 'ancestor'
    case 18252:                     // 'attribute' 'ancestor-or-self'
    case 18278:                     // 'element' 'ancestor-or-self'
    case 19532:                     // 'attribute' 'attribute'
    case 19558:                     // 'element' 'attribute'
    case 21580:                     // 'attribute' 'child'
    case 21606:                     // 'element' 'child'
    case 22092:                     // 'attribute' 'comment'
    case 22118:                     // 'element' 'comment'
    case 23884:                     // 'attribute' 'declare'
    case 23910:                     // 'element' 'declare'
    case 24396:                     // 'attribute' 'descendant'
    case 24422:                     // 'element' 'descendant'
    case 24652:                     // 'attribute' 'descendant-or-self'
    case 24678:                     // 'element' 'descendant-or-self'
    case 25676:                     // 'attribute' 'document'
    case 25702:                     // 'element' 'document'
    case 25932:                     // 'attribute' 'document-node'
    case 25958:                     // 'element' 'document-node'
    case 26188:                     // 'attribute' 'element'
    case 26214:                     // 'element' 'element'
    case 26956:                     // 'attribute' 'empty-sequence'
    case 26982:                     // 'element' 'empty-sequence'
    case 27980:                     // 'attribute' 'every'
    case 28006:                     // 'element' 'every'
    case 28748:                     // 'attribute' 'following'
    case 28774:                     // 'element' 'following'
    case 29004:                     // 'attribute' 'following-sibling'
    case 29030:                     // 'element' 'following-sibling'
    case 29516:                     // 'attribute' 'function'
    case 29542:                     // 'element' 'function'
    case 31308:                     // 'attribute' 'if'
    case 31334:                     // 'element' 'if'
    case 31564:                     // 'attribute' 'import'
    case 31590:                     // 'element' 'import'
    case 33356:                     // 'attribute' 'item'
    case 33382:                     // 'element' 'item'
    case 35404:                     // 'attribute' 'module'
    case 35430:                     // 'element' 'module'
    case 35660:                     // 'attribute' 'namespace'
    case 35686:                     // 'element' 'namespace'
    case 35916:                     // 'attribute' 'namespace-node'
    case 35942:                     // 'element' 'namespace-node'
    case 37196:                     // 'attribute' 'node'
    case 37222:                     // 'element' 'node'
    case 38732:                     // 'attribute' 'ordered'
    case 38758:                     // 'element' 'ordered'
    case 39244:                     // 'attribute' 'parent'
    case 39270:                     // 'element' 'parent'
    case 40268:                     // 'attribute' 'preceding'
    case 40294:                     // 'element' 'preceding'
    case 40524:                     // 'attribute' 'preceding-sibling'
    case 40550:                     // 'element' 'preceding-sibling'
    case 41292:                     // 'attribute' 'processing-instruction'
    case 41318:                     // 'element' 'processing-instruction'
    case 42316:                     // 'attribute' 'schema-attribute'
    case 42342:                     // 'element' 'schema-attribute'
    case 42572:                     // 'attribute' 'schema-element'
    case 42598:                     // 'element' 'schema-element'
    case 42828:                     // 'attribute' 'self'
    case 42854:                     // 'element' 'self'
    case 43340:                     // 'attribute' 'some'
    case 43366:                     // 'element' 'some'
    case 44620:                     // 'attribute' 'switch'
    case 44646:                     // 'element' 'switch'
    case 44876:                     // 'attribute' 'text'
    case 44902:                     // 'element' 'text'
    case 45900:                     // 'attribute' 'try'
    case 45926:                     // 'element' 'try'
    case 46668:                     // 'attribute' 'typeswitch'
    case 46694:                     // 'element' 'typeswitch'
    case 47180:                     // 'attribute' 'unordered'
    case 47206:                     // 'element' 'unordered'
    case 47436:                     // 'attribute' 'validate'
    case 47462:                     // 'element' 'validate'
    case 48972:                     // 'attribute' 'xquery'
    case 48998:                     // 'element' 'xquery'
    case 49484:                     // 'attribute' '{'
    case 49494:                     // 'comment' '{'
    case 49508:                     // 'document' '{'
    case 49510:                     // 'element' '{'
    case 49547:                     // 'namespace' '{'
    case 49559:                     // 'ordered' '{'
    case 49569:                     // 'processing-instruction' '{'
    case 49583:                     // 'text' '{'
    case 49592:                     // 'unordered' '{'
      try_PostfixExpr();
      break;
    case -3:
      break;
    default:
      try_AxisStep();
    }
  }

  private void parse_AxisStep()
  {
    eventHandler.startNonterminal("AxisStep", e0);
    switch (l1)
    {
    case 70:                        // 'ancestor'
    case 71:                        // 'ancestor-or-self'
    case 153:                       // 'parent'
    case 157:                       // 'preceding'
    case 158:                       // 'preceding-sibling'
      lookahead2W(142);             // S^WS | EOF | '!' | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '::' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 43:                        // '..'
    case 12358:                     // 'ancestor' '::'
    case 12359:                     // 'ancestor-or-self' '::'
    case 12441:                     // 'parent' '::'
    case 12445:                     // 'preceding' '::'
    case 12446:                     // 'preceding-sibling' '::'
      parse_ReverseStep();
      break;
    default:
      parse_ForwardStep();
    }
    lookahead1W(138);               // S^WS | EOF | '!' | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' | ';' |
                                    // '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' | 'ascending' |
                                    // 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' |
                                    // 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' |
                                    // 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' |
                                    // 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
    whitespace();
    parse_PredicateList();
    eventHandler.endNonterminal("AxisStep", e0);
  }

  private void try_AxisStep()
  {
    switch (l1)
    {
    case 70:                        // 'ancestor'
    case 71:                        // 'ancestor-or-self'
    case 153:                       // 'parent'
    case 157:                       // 'preceding'
    case 158:                       // 'preceding-sibling'
      lookahead2W(142);             // S^WS | EOF | '!' | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '::' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 43:                        // '..'
    case 12358:                     // 'ancestor' '::'
    case 12359:                     // 'ancestor-or-self' '::'
    case 12441:                     // 'parent' '::'
    case 12445:                     // 'preceding' '::'
    case 12446:                     // 'preceding-sibling' '::'
      try_ReverseStep();
      break;
    default:
      try_ForwardStep();
    }
    lookahead1W(138);               // S^WS | EOF | '!' | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' | ';' |
                                    // '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' | 'ascending' |
                                    // 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' |
                                    // 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' |
                                    // 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' |
                                    // 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
    try_PredicateList();
  }

  private void parse_ForwardStep()
  {
    eventHandler.startNonterminal("ForwardStep", e0);
    switch (l1)
    {
    case 76:                        // 'attribute'
      lookahead2W(145);             // S^WS | EOF | '!' | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '::' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    case 84:                        // 'child'
    case 95:                        // 'descendant'
    case 96:                        // 'descendant-or-self'
    case 112:                       // 'following'
    case 113:                       // 'following-sibling'
    case 167:                       // 'self'
      lookahead2W(142);             // S^WS | EOF | '!' | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '::' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 12364:                     // 'attribute' '::'
    case 12372:                     // 'child' '::'
    case 12383:                     // 'descendant' '::'
    case 12384:                     // 'descendant-or-self' '::'
    case 12400:                     // 'following' '::'
    case 12401:                     // 'following-sibling' '::'
    case 12455:                     // 'self' '::'
      parse_ForwardAxis();
      lookahead1W(157);             // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_NodeTest();
      break;
    default:
      parse_AbbrevForwardStep();
    }
    eventHandler.endNonterminal("ForwardStep", e0);
  }

  private void try_ForwardStep()
  {
    switch (l1)
    {
    case 76:                        // 'attribute'
      lookahead2W(145);             // S^WS | EOF | '!' | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '::' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    case 84:                        // 'child'
    case 95:                        // 'descendant'
    case 96:                        // 'descendant-or-self'
    case 112:                       // 'following'
    case 113:                       // 'following-sibling'
    case 167:                       // 'self'
      lookahead2W(142);             // S^WS | EOF | '!' | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // '::' | ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 12364:                     // 'attribute' '::'
    case 12372:                     // 'child' '::'
    case 12383:                     // 'descendant' '::'
    case 12384:                     // 'descendant-or-self' '::'
    case 12400:                     // 'following' '::'
    case 12401:                     // 'following-sibling' '::'
    case 12455:                     // 'self' '::'
      try_ForwardAxis();
      lookahead1W(157);             // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_NodeTest();
      break;
    default:
      try_AbbrevForwardStep();
    }
  }

  private void parse_ForwardAxis()
  {
    eventHandler.startNonterminal("ForwardAxis", e0);
    switch (l1)
    {
    case 84:                        // 'child'
      shift(84);                    // 'child'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    case 95:                        // 'descendant'
      shift(95);                    // 'descendant'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    case 76:                        // 'attribute'
      shift(76);                    // 'attribute'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    case 167:                       // 'self'
      shift(167);                   // 'self'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    case 96:                        // 'descendant-or-self'
      shift(96);                    // 'descendant-or-self'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    case 113:                       // 'following-sibling'
      shift(113);                   // 'following-sibling'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    default:
      shift(112);                   // 'following'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
    }
    eventHandler.endNonterminal("ForwardAxis", e0);
  }

  private void try_ForwardAxis()
  {
    switch (l1)
    {
    case 84:                        // 'child'
      shiftT(84);                   // 'child'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    case 95:                        // 'descendant'
      shiftT(95);                   // 'descendant'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    case 76:                        // 'attribute'
      shiftT(76);                   // 'attribute'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    case 167:                       // 'self'
      shiftT(167);                  // 'self'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    case 96:                        // 'descendant-or-self'
      shiftT(96);                   // 'descendant-or-self'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    case 113:                       // 'following-sibling'
      shiftT(113);                  // 'following-sibling'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    default:
      shiftT(112);                  // 'following'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
    }
  }

  private void parse_AbbrevForwardStep()
  {
    eventHandler.startNonterminal("AbbrevForwardStep", e0);
    if (l1 == 64)                   // '@'
    {
      shift(64);                    // '@'
    }
    lookahead1W(157);               // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_NodeTest();
    eventHandler.endNonterminal("AbbrevForwardStep", e0);
  }

  private void try_AbbrevForwardStep()
  {
    if (l1 == 64)                   // '@'
    {
      shiftT(64);                   // '@'
    }
    lookahead1W(157);               // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_NodeTest();
  }

  private void parse_ReverseStep()
  {
    eventHandler.startNonterminal("ReverseStep", e0);
    switch (l1)
    {
    case 43:                        // '..'
      parse_AbbrevReverseStep();
      break;
    default:
      parse_ReverseAxis();
      lookahead1W(157);             // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_NodeTest();
    }
    eventHandler.endNonterminal("ReverseStep", e0);
  }

  private void try_ReverseStep()
  {
    switch (l1)
    {
    case 43:                        // '..'
      try_AbbrevReverseStep();
      break;
    default:
      try_ReverseAxis();
      lookahead1W(157);             // URIQualifiedName | QName^Token | S^WS | Wildcard | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_NodeTest();
    }
  }

  private void parse_ReverseAxis()
  {
    eventHandler.startNonterminal("ReverseAxis", e0);
    switch (l1)
    {
    case 153:                       // 'parent'
      shift(153);                   // 'parent'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    case 70:                        // 'ancestor'
      shift(70);                    // 'ancestor'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    case 158:                       // 'preceding-sibling'
      shift(158);                   // 'preceding-sibling'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    case 157:                       // 'preceding'
      shift(157);                   // 'preceding'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
      break;
    default:
      shift(71);                    // 'ancestor-or-self'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shift(48);                    // '::'
    }
    eventHandler.endNonterminal("ReverseAxis", e0);
  }

  private void try_ReverseAxis()
  {
    switch (l1)
    {
    case 153:                       // 'parent'
      shiftT(153);                  // 'parent'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    case 70:                        // 'ancestor'
      shiftT(70);                   // 'ancestor'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    case 158:                       // 'preceding-sibling'
      shiftT(158);                  // 'preceding-sibling'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    case 157:                       // 'preceding'
      shiftT(157);                  // 'preceding'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
      break;
    default:
      shiftT(71);                   // 'ancestor-or-self'
      lookahead1W(26);              // S^WS | '(:' | '::'
      shiftT(48);                   // '::'
    }
  }

  private void parse_AbbrevReverseStep()
  {
    eventHandler.startNonterminal("AbbrevReverseStep", e0);
    shift(43);                      // '..'
    eventHandler.endNonterminal("AbbrevReverseStep", e0);
  }

  private void try_AbbrevReverseStep()
  {
    shiftT(43);                     // '..'
  }

  private void parse_NodeTest()
  {
    eventHandler.startNonterminal("NodeTest", e0);
    switch (l1)
    {
    case 76:                        // 'attribute'
    case 86:                        // 'comment'
    case 101:                       // 'document-node'
    case 102:                       // 'element'
    case 140:                       // 'namespace-node'
    case 145:                       // 'node'
    case 161:                       // 'processing-instruction'
    case 165:                       // 'schema-attribute'
    case 166:                       // 'schema-element'
    case 175:                       // 'text'
      lookahead2W(141);             // S^WS | EOF | '!' | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 8524:                      // 'attribute' '('
    case 8534:                      // 'comment' '('
    case 8549:                      // 'document-node' '('
    case 8550:                      // 'element' '('
    case 8588:                      // 'namespace-node' '('
    case 8593:                      // 'node' '('
    case 8609:                      // 'processing-instruction' '('
    case 8613:                      // 'schema-attribute' '('
    case 8614:                      // 'schema-element' '('
    case 8623:                      // 'text' '('
      parse_KindTest();
      break;
    default:
      parse_NameTest();
    }
    eventHandler.endNonterminal("NodeTest", e0);
  }

  private void try_NodeTest()
  {
    switch (l1)
    {
    case 76:                        // 'attribute'
    case 86:                        // 'comment'
    case 101:                       // 'document-node'
    case 102:                       // 'element'
    case 140:                       // 'namespace-node'
    case 145:                       // 'node'
    case 161:                       // 'processing-instruction'
    case 165:                       // 'schema-attribute'
    case 166:                       // 'schema-element'
    case 175:                       // 'text'
      lookahead2W(141);             // S^WS | EOF | '!' | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 8524:                      // 'attribute' '('
    case 8534:                      // 'comment' '('
    case 8549:                      // 'document-node' '('
    case 8550:                      // 'element' '('
    case 8588:                      // 'namespace-node' '('
    case 8593:                      // 'node' '('
    case 8609:                      // 'processing-instruction' '('
    case 8613:                      // 'schema-attribute' '('
    case 8614:                      // 'schema-element' '('
    case 8623:                      // 'text' '('
      try_KindTest();
      break;
    default:
      try_NameTest();
    }
  }

  private void parse_NameTest()
  {
    eventHandler.startNonterminal("NameTest", e0);
    switch (l1)
    {
    case 20:                        // Wildcard
      shift(20);                    // Wildcard
      break;
    default:
      parse_EQName();
    }
    eventHandler.endNonterminal("NameTest", e0);
  }

  private void try_NameTest()
  {
    switch (l1)
    {
    case 20:                        // Wildcard
      shiftT(20);                   // Wildcard
      break;
    default:
      try_EQName();
    }
  }

  private void parse_PostfixExpr()
  {
    eventHandler.startNonterminal("PostfixExpr", e0);
    parse_PrimaryExpr();
    for (;;)
    {
      lookahead1W(141);             // S^WS | EOF | '!' | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      if (l1 != 33                  // '('
       && l1 != 66)                 // '['
      {
        break;
      }
      switch (l1)
      {
      case 66:                      // '['
        whitespace();
        parse_Predicate();
        break;
      default:
        whitespace();
        parse_ArgumentList();
      }
    }
    eventHandler.endNonterminal("PostfixExpr", e0);
  }

  private void try_PostfixExpr()
  {
    try_PrimaryExpr();
    for (;;)
    {
      lookahead1W(141);             // S^WS | EOF | '!' | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' |
                                    // ';' | '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' |
                                    // 'ascending' | 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' |
                                    // 'descending' | 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' |
                                    // 'ge' | 'group' | 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      if (l1 != 33                  // '('
       && l1 != 66)                 // '['
      {
        break;
      }
      switch (l1)
      {
      case 66:                      // '['
        try_Predicate();
        break;
      default:
        try_ArgumentList();
      }
    }
  }

  private void parse_ArgumentList()
  {
    eventHandler.startNonterminal("ArgumentList", e0);
    shift(33);                      // '('
    lookahead1W(171);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | ')' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '?' |
                                    // '@' | 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' |
                                    // 'case' | 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' |
                                    // 'declare' | 'default' | 'descendant' | 'descendant-or-self' | 'descending' |
                                    // 'div' | 'document' | 'document-node' | 'element' | 'else' | 'empty' |
                                    // 'empty-sequence' | 'end' | 'eq' | 'every' | 'except' | 'following' |
                                    // 'following-sibling' | 'for' | 'function' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'if' | 'import' | 'instance' | 'intersect' | 'is' | 'item' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'module' | 'namespace' | 'namespace-node' | 'ne' | 'node' |
                                    // 'only' | 'or' | 'order' | 'ordered' | 'parent' | 'preceding' |
                                    // 'preceding-sibling' | 'processing-instruction' | 'return' | 'satisfies' |
                                    // 'schema-attribute' | 'schema-element' | 'self' | 'some' | 'stable' | 'start' |
                                    // 'switch' | 'text' | 'to' | 'treat' | 'try' | 'typeswitch' | 'union' |
                                    // 'unordered' | 'validate' | 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      whitespace();
      parse_Argument();
      for (;;)
      {
        lookahead1W(67);            // S^WS | '(:' | ')' | ','
        if (l1 != 39)               // ','
        {
          break;
        }
        shift(39);                  // ','
        lookahead1W(169);           // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
        whitespace();
        parse_Argument();
      }
    }
    shift(36);                      // ')'
    eventHandler.endNonterminal("ArgumentList", e0);
  }

  private void try_ArgumentList()
  {
    shiftT(33);                     // '('
    lookahead1W(171);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | ')' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '?' |
                                    // '@' | 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' |
                                    // 'case' | 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' |
                                    // 'declare' | 'default' | 'descendant' | 'descendant-or-self' | 'descending' |
                                    // 'div' | 'document' | 'document-node' | 'element' | 'else' | 'empty' |
                                    // 'empty-sequence' | 'end' | 'eq' | 'every' | 'except' | 'following' |
                                    // 'following-sibling' | 'for' | 'function' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'if' | 'import' | 'instance' | 'intersect' | 'is' | 'item' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'module' | 'namespace' | 'namespace-node' | 'ne' | 'node' |
                                    // 'only' | 'or' | 'order' | 'ordered' | 'parent' | 'preceding' |
                                    // 'preceding-sibling' | 'processing-instruction' | 'return' | 'satisfies' |
                                    // 'schema-attribute' | 'schema-element' | 'self' | 'some' | 'stable' | 'start' |
                                    // 'switch' | 'text' | 'to' | 'treat' | 'try' | 'typeswitch' | 'union' |
                                    // 'unordered' | 'validate' | 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      try_Argument();
      for (;;)
      {
        lookahead1W(67);            // S^WS | '(:' | ')' | ','
        if (l1 != 39)               // ','
        {
          break;
        }
        shiftT(39);                 // ','
        lookahead1W(169);           // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
        try_Argument();
      }
    }
    shiftT(36);                     // ')'
  }

  private void parse_PredicateList()
  {
    eventHandler.startNonterminal("PredicateList", e0);
    for (;;)
    {
      lookahead1W(138);             // S^WS | EOF | '!' | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' | ';' |
                                    // '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' | 'ascending' |
                                    // 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' |
                                    // 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' |
                                    // 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' |
                                    // 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      if (l1 != 66)                 // '['
      {
        break;
      }
      whitespace();
      parse_Predicate();
    }
    eventHandler.endNonterminal("PredicateList", e0);
  }

  private void try_PredicateList()
  {
    for (;;)
    {
      lookahead1W(138);             // S^WS | EOF | '!' | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | '/' | '//' | ';' |
                                    // '<' | '<<' | '<=' | '=' | '>' | '>=' | '>>' | '[' | ']' | 'and' | 'ascending' |
                                    // 'case' | 'cast' | 'castable' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' |
                                    // 'gt' | 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' |
                                    // 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' |
                                    // 'to' | 'treat' | 'union' | 'where' | '|' | '||' | '}'
      if (l1 != 66)                 // '['
      {
        break;
      }
      try_Predicate();
    }
  }

  private void parse_Predicate()
  {
    eventHandler.startNonterminal("Predicate", e0);
    shift(66);                      // '['
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(67);                      // ']'
    eventHandler.endNonterminal("Predicate", e0);
  }

  private void try_Predicate()
  {
    shiftT(66);                     // '['
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(67);                     // ']'
  }

  private void parse_PrimaryExpr()
  {
    eventHandler.startNonterminal("PrimaryExpr", e0);
    switch (l1)
    {
    case 139:                       // 'namespace'
      lookahead2W(127);             // NCName^Token | S^WS | '#' | '(' | '(:' | 'and' | 'ascending' | 'case' | 'cast' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '{'
      break;
    case 161:                       // 'processing-instruction'
      lookahead2W(125);             // NCName^Token | S^WS | '#' | '(:' | 'and' | 'ascending' | 'case' | 'cast' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '{'
      break;
    case 76:                        // 'attribute'
    case 102:                       // 'element'
      lookahead2W(159);             // URIQualifiedName | QName^Token | S^WS | '#' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '{'
      break;
    case 86:                        // 'comment'
    case 175:                       // 'text'
      lookahead2W(62);              // S^WS | '#' | '(:' | '{'
      break;
    case 100:                       // 'document'
    case 151:                       // 'ordered'
    case 184:                       // 'unordered'
      lookahead2W(94);              // S^WS | '#' | '(' | '(:' | '{'
      break;
    case 15:                        // QName^Token
    case 70:                        // 'ancestor'
    case 71:                        // 'ancestor-or-self'
    case 72:                        // 'and'
    case 74:                        // 'ascending'
    case 80:                        // 'case'
    case 81:                        // 'cast'
    case 82:                        // 'castable'
    case 84:                        // 'child'
    case 85:                        // 'collation'
    case 90:                        // 'count'
    case 93:                        // 'declare'
    case 94:                        // 'default'
    case 95:                        // 'descendant'
    case 96:                        // 'descendant-or-self'
    case 97:                        // 'descending'
    case 99:                        // 'div'
    case 103:                       // 'else'
    case 104:                       // 'empty'
    case 107:                       // 'end'
    case 108:                       // 'eq'
    case 109:                       // 'every'
    case 110:                       // 'except'
    case 112:                       // 'following'
    case 113:                       // 'following-sibling'
    case 114:                       // 'for'
    case 116:                       // 'ge'
    case 118:                       // 'group'
    case 120:                       // 'gt'
    case 121:                       // 'idiv'
    case 123:                       // 'import'
    case 127:                       // 'instance'
    case 128:                       // 'intersect'
    case 129:                       // 'is'
    case 132:                       // 'le'
    case 134:                       // 'let'
    case 135:                       // 'lt'
    case 137:                       // 'mod'
    case 138:                       // 'module'
    case 141:                       // 'ne'
    case 147:                       // 'only'
    case 149:                       // 'or'
    case 150:                       // 'order'
    case 153:                       // 'parent'
    case 157:                       // 'preceding'
    case 158:                       // 'preceding-sibling'
    case 162:                       // 'return'
    case 163:                       // 'satisfies'
    case 167:                       // 'self'
    case 169:                       // 'some'
    case 170:                       // 'stable'
    case 171:                       // 'start'
    case 177:                       // 'to'
    case 178:                       // 'treat'
    case 179:                       // 'try'
    case 183:                       // 'union'
    case 185:                       // 'validate'
    case 189:                       // 'where'
    case 191:                       // 'xquery'
      lookahead2W(61);              // S^WS | '#' | '(' | '(:'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 1:                         // IntegerLiteral
    case 2:                         // DecimalLiteral
    case 3:                         // DoubleLiteral
    case 4:                         // StringLiteral
      parse_Literal();
      break;
    case 30:                        // '$'
      parse_VarRef();
      break;
    case 33:                        // '('
      parse_ParenthesizedExpr();
      break;
    case 42:                        // '.'
      parse_ContextItemExpr();
      break;
    case 8463:                      // QName^Token '('
    case 8518:                      // 'ancestor' '('
    case 8519:                      // 'ancestor-or-self' '('
    case 8520:                      // 'and' '('
    case 8522:                      // 'ascending' '('
    case 8528:                      // 'case' '('
    case 8529:                      // 'cast' '('
    case 8530:                      // 'castable' '('
    case 8532:                      // 'child' '('
    case 8533:                      // 'collation' '('
    case 8538:                      // 'count' '('
    case 8541:                      // 'declare' '('
    case 8542:                      // 'default' '('
    case 8543:                      // 'descendant' '('
    case 8544:                      // 'descendant-or-self' '('
    case 8545:                      // 'descending' '('
    case 8547:                      // 'div' '('
    case 8548:                      // 'document' '('
    case 8551:                      // 'else' '('
    case 8552:                      // 'empty' '('
    case 8555:                      // 'end' '('
    case 8556:                      // 'eq' '('
    case 8557:                      // 'every' '('
    case 8558:                      // 'except' '('
    case 8560:                      // 'following' '('
    case 8561:                      // 'following-sibling' '('
    case 8562:                      // 'for' '('
    case 8564:                      // 'ge' '('
    case 8566:                      // 'group' '('
    case 8568:                      // 'gt' '('
    case 8569:                      // 'idiv' '('
    case 8571:                      // 'import' '('
    case 8575:                      // 'instance' '('
    case 8576:                      // 'intersect' '('
    case 8577:                      // 'is' '('
    case 8580:                      // 'le' '('
    case 8582:                      // 'let' '('
    case 8583:                      // 'lt' '('
    case 8585:                      // 'mod' '('
    case 8586:                      // 'module' '('
    case 8587:                      // 'namespace' '('
    case 8589:                      // 'ne' '('
    case 8595:                      // 'only' '('
    case 8597:                      // 'or' '('
    case 8598:                      // 'order' '('
    case 8599:                      // 'ordered' '('
    case 8601:                      // 'parent' '('
    case 8605:                      // 'preceding' '('
    case 8606:                      // 'preceding-sibling' '('
    case 8610:                      // 'return' '('
    case 8611:                      // 'satisfies' '('
    case 8615:                      // 'self' '('
    case 8617:                      // 'some' '('
    case 8618:                      // 'stable' '('
    case 8619:                      // 'start' '('
    case 8625:                      // 'to' '('
    case 8626:                      // 'treat' '('
    case 8627:                      // 'try' '('
    case 8631:                      // 'union' '('
    case 8632:                      // 'unordered' '('
    case 8633:                      // 'validate' '('
    case 8637:                      // 'where' '('
    case 8639:                      // 'xquery' '('
      parse_FunctionCall();
      break;
    case 49559:                     // 'ordered' '{'
      parse_OrderedExpr();
      break;
    case 49592:                     // 'unordered' '{'
      parse_UnorderedExpr();
      break;
    case 5:                         // URIQualifiedName
    case 31:                        // '%'
    case 101:                       // 'document-node'
    case 105:                       // 'empty-sequence'
    case 115:                       // 'function'
    case 122:                       // 'if'
    case 130:                       // 'item'
    case 140:                       // 'namespace-node'
    case 145:                       // 'node'
    case 165:                       // 'schema-attribute'
    case 166:                       // 'schema-element'
    case 174:                       // 'switch'
    case 182:                       // 'typeswitch'
    case 7183:                      // QName^Token '#'
    case 7238:                      // 'ancestor' '#'
    case 7239:                      // 'ancestor-or-self' '#'
    case 7240:                      // 'and' '#'
    case 7242:                      // 'ascending' '#'
    case 7244:                      // 'attribute' '#'
    case 7248:                      // 'case' '#'
    case 7249:                      // 'cast' '#'
    case 7250:                      // 'castable' '#'
    case 7252:                      // 'child' '#'
    case 7253:                      // 'collation' '#'
    case 7254:                      // 'comment' '#'
    case 7258:                      // 'count' '#'
    case 7261:                      // 'declare' '#'
    case 7262:                      // 'default' '#'
    case 7263:                      // 'descendant' '#'
    case 7264:                      // 'descendant-or-self' '#'
    case 7265:                      // 'descending' '#'
    case 7267:                      // 'div' '#'
    case 7268:                      // 'document' '#'
    case 7270:                      // 'element' '#'
    case 7271:                      // 'else' '#'
    case 7272:                      // 'empty' '#'
    case 7275:                      // 'end' '#'
    case 7276:                      // 'eq' '#'
    case 7277:                      // 'every' '#'
    case 7278:                      // 'except' '#'
    case 7280:                      // 'following' '#'
    case 7281:                      // 'following-sibling' '#'
    case 7282:                      // 'for' '#'
    case 7284:                      // 'ge' '#'
    case 7286:                      // 'group' '#'
    case 7288:                      // 'gt' '#'
    case 7289:                      // 'idiv' '#'
    case 7291:                      // 'import' '#'
    case 7295:                      // 'instance' '#'
    case 7296:                      // 'intersect' '#'
    case 7297:                      // 'is' '#'
    case 7300:                      // 'le' '#'
    case 7302:                      // 'let' '#'
    case 7303:                      // 'lt' '#'
    case 7305:                      // 'mod' '#'
    case 7306:                      // 'module' '#'
    case 7307:                      // 'namespace' '#'
    case 7309:                      // 'ne' '#'
    case 7315:                      // 'only' '#'
    case 7317:                      // 'or' '#'
    case 7318:                      // 'order' '#'
    case 7319:                      // 'ordered' '#'
    case 7321:                      // 'parent' '#'
    case 7325:                      // 'preceding' '#'
    case 7326:                      // 'preceding-sibling' '#'
    case 7329:                      // 'processing-instruction' '#'
    case 7330:                      // 'return' '#'
    case 7331:                      // 'satisfies' '#'
    case 7335:                      // 'self' '#'
    case 7337:                      // 'some' '#'
    case 7338:                      // 'stable' '#'
    case 7339:                      // 'start' '#'
    case 7343:                      // 'text' '#'
    case 7345:                      // 'to' '#'
    case 7346:                      // 'treat' '#'
    case 7347:                      // 'try' '#'
    case 7351:                      // 'union' '#'
    case 7352:                      // 'unordered' '#'
    case 7353:                      // 'validate' '#'
    case 7357:                      // 'where' '#'
    case 7359:                      // 'xquery' '#'
      parse_FunctionItemExpr();
      break;
    default:
      parse_Constructor();
    }
    eventHandler.endNonterminal("PrimaryExpr", e0);
  }

  private void try_PrimaryExpr()
  {
    switch (l1)
    {
    case 139:                       // 'namespace'
      lookahead2W(127);             // NCName^Token | S^WS | '#' | '(' | '(:' | 'and' | 'ascending' | 'case' | 'cast' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '{'
      break;
    case 161:                       // 'processing-instruction'
      lookahead2W(125);             // NCName^Token | S^WS | '#' | '(:' | 'and' | 'ascending' | 'case' | 'cast' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '{'
      break;
    case 76:                        // 'attribute'
    case 102:                       // 'element'
      lookahead2W(159);             // URIQualifiedName | QName^Token | S^WS | '#' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '{'
      break;
    case 86:                        // 'comment'
    case 175:                       // 'text'
      lookahead2W(62);              // S^WS | '#' | '(:' | '{'
      break;
    case 100:                       // 'document'
    case 151:                       // 'ordered'
    case 184:                       // 'unordered'
      lookahead2W(94);              // S^WS | '#' | '(' | '(:' | '{'
      break;
    case 15:                        // QName^Token
    case 70:                        // 'ancestor'
    case 71:                        // 'ancestor-or-self'
    case 72:                        // 'and'
    case 74:                        // 'ascending'
    case 80:                        // 'case'
    case 81:                        // 'cast'
    case 82:                        // 'castable'
    case 84:                        // 'child'
    case 85:                        // 'collation'
    case 90:                        // 'count'
    case 93:                        // 'declare'
    case 94:                        // 'default'
    case 95:                        // 'descendant'
    case 96:                        // 'descendant-or-self'
    case 97:                        // 'descending'
    case 99:                        // 'div'
    case 103:                       // 'else'
    case 104:                       // 'empty'
    case 107:                       // 'end'
    case 108:                       // 'eq'
    case 109:                       // 'every'
    case 110:                       // 'except'
    case 112:                       // 'following'
    case 113:                       // 'following-sibling'
    case 114:                       // 'for'
    case 116:                       // 'ge'
    case 118:                       // 'group'
    case 120:                       // 'gt'
    case 121:                       // 'idiv'
    case 123:                       // 'import'
    case 127:                       // 'instance'
    case 128:                       // 'intersect'
    case 129:                       // 'is'
    case 132:                       // 'le'
    case 134:                       // 'let'
    case 135:                       // 'lt'
    case 137:                       // 'mod'
    case 138:                       // 'module'
    case 141:                       // 'ne'
    case 147:                       // 'only'
    case 149:                       // 'or'
    case 150:                       // 'order'
    case 153:                       // 'parent'
    case 157:                       // 'preceding'
    case 158:                       // 'preceding-sibling'
    case 162:                       // 'return'
    case 163:                       // 'satisfies'
    case 167:                       // 'self'
    case 169:                       // 'some'
    case 170:                       // 'stable'
    case 171:                       // 'start'
    case 177:                       // 'to'
    case 178:                       // 'treat'
    case 179:                       // 'try'
    case 183:                       // 'union'
    case 185:                       // 'validate'
    case 189:                       // 'where'
    case 191:                       // 'xquery'
      lookahead2W(61);              // S^WS | '#' | '(' | '(:'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 1:                         // IntegerLiteral
    case 2:                         // DecimalLiteral
    case 3:                         // DoubleLiteral
    case 4:                         // StringLiteral
      try_Literal();
      break;
    case 30:                        // '$'
      try_VarRef();
      break;
    case 33:                        // '('
      try_ParenthesizedExpr();
      break;
    case 42:                        // '.'
      try_ContextItemExpr();
      break;
    case 8463:                      // QName^Token '('
    case 8518:                      // 'ancestor' '('
    case 8519:                      // 'ancestor-or-self' '('
    case 8520:                      // 'and' '('
    case 8522:                      // 'ascending' '('
    case 8528:                      // 'case' '('
    case 8529:                      // 'cast' '('
    case 8530:                      // 'castable' '('
    case 8532:                      // 'child' '('
    case 8533:                      // 'collation' '('
    case 8538:                      // 'count' '('
    case 8541:                      // 'declare' '('
    case 8542:                      // 'default' '('
    case 8543:                      // 'descendant' '('
    case 8544:                      // 'descendant-or-self' '('
    case 8545:                      // 'descending' '('
    case 8547:                      // 'div' '('
    case 8548:                      // 'document' '('
    case 8551:                      // 'else' '('
    case 8552:                      // 'empty' '('
    case 8555:                      // 'end' '('
    case 8556:                      // 'eq' '('
    case 8557:                      // 'every' '('
    case 8558:                      // 'except' '('
    case 8560:                      // 'following' '('
    case 8561:                      // 'following-sibling' '('
    case 8562:                      // 'for' '('
    case 8564:                      // 'ge' '('
    case 8566:                      // 'group' '('
    case 8568:                      // 'gt' '('
    case 8569:                      // 'idiv' '('
    case 8571:                      // 'import' '('
    case 8575:                      // 'instance' '('
    case 8576:                      // 'intersect' '('
    case 8577:                      // 'is' '('
    case 8580:                      // 'le' '('
    case 8582:                      // 'let' '('
    case 8583:                      // 'lt' '('
    case 8585:                      // 'mod' '('
    case 8586:                      // 'module' '('
    case 8587:                      // 'namespace' '('
    case 8589:                      // 'ne' '('
    case 8595:                      // 'only' '('
    case 8597:                      // 'or' '('
    case 8598:                      // 'order' '('
    case 8599:                      // 'ordered' '('
    case 8601:                      // 'parent' '('
    case 8605:                      // 'preceding' '('
    case 8606:                      // 'preceding-sibling' '('
    case 8610:                      // 'return' '('
    case 8611:                      // 'satisfies' '('
    case 8615:                      // 'self' '('
    case 8617:                      // 'some' '('
    case 8618:                      // 'stable' '('
    case 8619:                      // 'start' '('
    case 8625:                      // 'to' '('
    case 8626:                      // 'treat' '('
    case 8627:                      // 'try' '('
    case 8631:                      // 'union' '('
    case 8632:                      // 'unordered' '('
    case 8633:                      // 'validate' '('
    case 8637:                      // 'where' '('
    case 8639:                      // 'xquery' '('
      try_FunctionCall();
      break;
    case 49559:                     // 'ordered' '{'
      try_OrderedExpr();
      break;
    case 49592:                     // 'unordered' '{'
      try_UnorderedExpr();
      break;
    case 5:                         // URIQualifiedName
    case 31:                        // '%'
    case 101:                       // 'document-node'
    case 105:                       // 'empty-sequence'
    case 115:                       // 'function'
    case 122:                       // 'if'
    case 130:                       // 'item'
    case 140:                       // 'namespace-node'
    case 145:                       // 'node'
    case 165:                       // 'schema-attribute'
    case 166:                       // 'schema-element'
    case 174:                       // 'switch'
    case 182:                       // 'typeswitch'
    case 7183:                      // QName^Token '#'
    case 7238:                      // 'ancestor' '#'
    case 7239:                      // 'ancestor-or-self' '#'
    case 7240:                      // 'and' '#'
    case 7242:                      // 'ascending' '#'
    case 7244:                      // 'attribute' '#'
    case 7248:                      // 'case' '#'
    case 7249:                      // 'cast' '#'
    case 7250:                      // 'castable' '#'
    case 7252:                      // 'child' '#'
    case 7253:                      // 'collation' '#'
    case 7254:                      // 'comment' '#'
    case 7258:                      // 'count' '#'
    case 7261:                      // 'declare' '#'
    case 7262:                      // 'default' '#'
    case 7263:                      // 'descendant' '#'
    case 7264:                      // 'descendant-or-self' '#'
    case 7265:                      // 'descending' '#'
    case 7267:                      // 'div' '#'
    case 7268:                      // 'document' '#'
    case 7270:                      // 'element' '#'
    case 7271:                      // 'else' '#'
    case 7272:                      // 'empty' '#'
    case 7275:                      // 'end' '#'
    case 7276:                      // 'eq' '#'
    case 7277:                      // 'every' '#'
    case 7278:                      // 'except' '#'
    case 7280:                      // 'following' '#'
    case 7281:                      // 'following-sibling' '#'
    case 7282:                      // 'for' '#'
    case 7284:                      // 'ge' '#'
    case 7286:                      // 'group' '#'
    case 7288:                      // 'gt' '#'
    case 7289:                      // 'idiv' '#'
    case 7291:                      // 'import' '#'
    case 7295:                      // 'instance' '#'
    case 7296:                      // 'intersect' '#'
    case 7297:                      // 'is' '#'
    case 7300:                      // 'le' '#'
    case 7302:                      // 'let' '#'
    case 7303:                      // 'lt' '#'
    case 7305:                      // 'mod' '#'
    case 7306:                      // 'module' '#'
    case 7307:                      // 'namespace' '#'
    case 7309:                      // 'ne' '#'
    case 7315:                      // 'only' '#'
    case 7317:                      // 'or' '#'
    case 7318:                      // 'order' '#'
    case 7319:                      // 'ordered' '#'
    case 7321:                      // 'parent' '#'
    case 7325:                      // 'preceding' '#'
    case 7326:                      // 'preceding-sibling' '#'
    case 7329:                      // 'processing-instruction' '#'
    case 7330:                      // 'return' '#'
    case 7331:                      // 'satisfies' '#'
    case 7335:                      // 'self' '#'
    case 7337:                      // 'some' '#'
    case 7338:                      // 'stable' '#'
    case 7339:                      // 'start' '#'
    case 7343:                      // 'text' '#'
    case 7345:                      // 'to' '#'
    case 7346:                      // 'treat' '#'
    case 7347:                      // 'try' '#'
    case 7351:                      // 'union' '#'
    case 7352:                      // 'unordered' '#'
    case 7353:                      // 'validate' '#'
    case 7357:                      // 'where' '#'
    case 7359:                      // 'xquery' '#'
      try_FunctionItemExpr();
      break;
    default:
      try_Constructor();
    }
  }

  private void parse_Literal()
  {
    eventHandler.startNonterminal("Literal", e0);
    switch (l1)
    {
    case 4:                         // StringLiteral
      shift(4);                     // StringLiteral
      break;
    default:
      parse_NumericLiteral();
    }
    eventHandler.endNonterminal("Literal", e0);
  }

  private void try_Literal()
  {
    switch (l1)
    {
    case 4:                         // StringLiteral
      shiftT(4);                    // StringLiteral
      break;
    default:
      try_NumericLiteral();
    }
  }

  private void parse_NumericLiteral()
  {
    eventHandler.startNonterminal("NumericLiteral", e0);
    switch (l1)
    {
    case 1:                         // IntegerLiteral
      shift(1);                     // IntegerLiteral
      break;
    case 2:                         // DecimalLiteral
      shift(2);                     // DecimalLiteral
      break;
    default:
      shift(3);                     // DoubleLiteral
    }
    eventHandler.endNonterminal("NumericLiteral", e0);
  }

  private void try_NumericLiteral()
  {
    switch (l1)
    {
    case 1:                         // IntegerLiteral
      shiftT(1);                    // IntegerLiteral
      break;
    case 2:                         // DecimalLiteral
      shiftT(2);                    // DecimalLiteral
      break;
    default:
      shiftT(3);                    // DoubleLiteral
    }
  }

  private void parse_VarRef()
  {
    eventHandler.startNonterminal("VarRef", e0);
    shift(30);                      // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_VarName();
    eventHandler.endNonterminal("VarRef", e0);
  }

  private void try_VarRef()
  {
    shiftT(30);                     // '$'
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_VarName();
  }

  private void parse_VarName()
  {
    eventHandler.startNonterminal("VarName", e0);
    parse_EQName();
    eventHandler.endNonterminal("VarName", e0);
  }

  private void try_VarName()
  {
    try_EQName();
  }

  private void parse_ParenthesizedExpr()
  {
    eventHandler.startNonterminal("ParenthesizedExpr", e0);
    shift(33);                      // '('
    lookahead1W(168);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | ')' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      whitespace();
      parse_Expr();
    }
    shift(36);                      // ')'
    eventHandler.endNonterminal("ParenthesizedExpr", e0);
  }

  private void try_ParenthesizedExpr()
  {
    shiftT(33);                     // '('
    lookahead1W(168);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | ')' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      try_Expr();
    }
    shiftT(36);                     // ')'
  }

  private void parse_ContextItemExpr()
  {
    eventHandler.startNonterminal("ContextItemExpr", e0);
    shift(42);                      // '.'
    eventHandler.endNonterminal("ContextItemExpr", e0);
  }

  private void try_ContextItemExpr()
  {
    shiftT(42);                     // '.'
  }

  private void parse_OrderedExpr()
  {
    eventHandler.startNonterminal("OrderedExpr", e0);
    shift(151);                     // 'ordered'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("OrderedExpr", e0);
  }

  private void try_OrderedExpr()
  {
    shiftT(151);                    // 'ordered'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(197);                    // '}'
  }

  private void parse_UnorderedExpr()
  {
    eventHandler.startNonterminal("UnorderedExpr", e0);
    shift(184);                     // 'unordered'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("UnorderedExpr", e0);
  }

  private void try_UnorderedExpr()
  {
    shiftT(184);                    // 'unordered'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(197);                    // '}'
  }

  private void parse_FunctionCall()
  {
    eventHandler.startNonterminal("FunctionCall", e0);
    parse_FunctionName();
    lookahead1W(22);                // S^WS | '(' | '(:'
    whitespace();
    parse_ArgumentList();
    eventHandler.endNonterminal("FunctionCall", e0);
  }

  private void try_FunctionCall()
  {
    try_FunctionName();
    lookahead1W(22);                // S^WS | '(' | '(:'
    try_ArgumentList();
  }

  private void parse_Argument()
  {
    eventHandler.startNonterminal("Argument", e0);
    switch (l1)
    {
    case 62:                        // '?'
      parse_ArgumentPlaceholder();
      break;
    default:
      parse_ExprSingle();
    }
    eventHandler.endNonterminal("Argument", e0);
  }

  private void try_Argument()
  {
    switch (l1)
    {
    case 62:                        // '?'
      try_ArgumentPlaceholder();
      break;
    default:
      try_ExprSingle();
    }
  }

  private void parse_ArgumentPlaceholder()
  {
    eventHandler.startNonterminal("ArgumentPlaceholder", e0);
    shift(62);                      // '?'
    eventHandler.endNonterminal("ArgumentPlaceholder", e0);
  }

  private void try_ArgumentPlaceholder()
  {
    shiftT(62);                     // '?'
  }

  private void parse_Constructor()
  {
    eventHandler.startNonterminal("Constructor", e0);
    switch (l1)
    {
    case 51:                        // '<'
    case 52:                        // '<!--'
    case 57:                        // '<?'
      parse_DirectConstructor();
      break;
    default:
      parse_ComputedConstructor();
    }
    eventHandler.endNonterminal("Constructor", e0);
  }

  private void try_Constructor()
  {
    switch (l1)
    {
    case 51:                        // '<'
    case 52:                        // '<!--'
    case 57:                        // '<?'
      try_DirectConstructor();
      break;
    default:
      try_ComputedConstructor();
    }
  }

  private void parse_DirectConstructor()
  {
    eventHandler.startNonterminal("DirectConstructor", e0);
    switch (l1)
    {
    case 51:                        // '<'
      parse_DirElemConstructor();
      break;
    case 52:                        // '<!--'
      parse_DirCommentConstructor();
      break;
    default:
      parse_DirPIConstructor();
    }
    eventHandler.endNonterminal("DirectConstructor", e0);
  }

  private void try_DirectConstructor()
  {
    switch (l1)
    {
    case 51:                        // '<'
      try_DirElemConstructor();
      break;
    case 52:                        // '<!--'
      try_DirCommentConstructor();
      break;
    default:
      try_DirPIConstructor();
    }
  }

  private void parse_DirElemConstructor()
  {
    eventHandler.startNonterminal("DirElemConstructor", e0);
    shift(51);                      // '<'
    parse_QName();
    parse_DirAttributeList();
    switch (l1)
    {
    case 46:                        // '/>'
      shift(46);                    // '/>'
      break;
    default:
      shift(59);                    // '>'
      for (;;)
      {
        lookahead1(115);            // PredefinedEntityRef | ElementContentChar | CharRef | '<' | '<!--' | '<![CDATA[' |
                                    // '</' | '<?' | '{' | '{{' | '}}'
        if (l1 == 54)               // '</'
        {
          break;
        }
        parse_DirElemContent();
      }
      shift(54);                    // '</'
      parse_QName();
      lookahead1(13);               // S | '>'
      if (l1 == 16)                 // S
      {
        shift(16);                  // S
      }
      lookahead1(8);                // '>'
      shift(59);                    // '>'
    }
    eventHandler.endNonterminal("DirElemConstructor", e0);
  }

  private void try_DirElemConstructor()
  {
    shiftT(51);                     // '<'
    try_QName();
    try_DirAttributeList();
    switch (l1)
    {
    case 46:                        // '/>'
      shiftT(46);                   // '/>'
      break;
    default:
      shiftT(59);                   // '>'
      for (;;)
      {
        lookahead1(115);            // PredefinedEntityRef | ElementContentChar | CharRef | '<' | '<!--' | '<![CDATA[' |
                                    // '</' | '<?' | '{' | '{{' | '}}'
        if (l1 == 54)               // '</'
        {
          break;
        }
        try_DirElemContent();
      }
      shiftT(54);                   // '</'
      try_QName();
      lookahead1(13);               // S | '>'
      if (l1 == 16)                 // S
      {
        shiftT(16);                 // S
      }
      lookahead1(8);                // '>'
      shiftT(59);                   // '>'
    }
  }

  private void parse_DirAttributeList()
  {
    eventHandler.startNonterminal("DirAttributeList", e0);
    for (;;)
    {
      lookahead1(19);               // S | '/>' | '>'
      if (l1 != 16)                 // S
      {
        break;
      }
      shift(16);                    // S
      lookahead1(156);              // QName^Token | S | '/>' | '>' | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      if (l1 != 16                  // S
       && l1 != 46                  // '/>'
       && l1 != 59)                 // '>'
      {
        parse_QName();
        lookahead1(12);             // S | '='
        if (l1 == 16)               // S
        {
          shift(16);                // S
        }
        lookahead1(7);              // '='
        shift(58);                  // '='
        lookahead1(18);             // S | '"' | "'"
        if (l1 == 16)               // S
        {
          shift(16);                // S
        }
        parse_DirAttributeValue();
      }
    }
    eventHandler.endNonterminal("DirAttributeList", e0);
  }

  private void try_DirAttributeList()
  {
    for (;;)
    {
      lookahead1(19);               // S | '/>' | '>'
      if (l1 != 16)                 // S
      {
        break;
      }
      shiftT(16);                   // S
      lookahead1(156);              // QName^Token | S | '/>' | '>' | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
      if (l1 != 16                  // S
       && l1 != 46                  // '/>'
       && l1 != 59)                 // '>'
      {
        try_QName();
        lookahead1(12);             // S | '='
        if (l1 == 16)               // S
        {
          shiftT(16);               // S
        }
        lookahead1(7);              // '='
        shiftT(58);                 // '='
        lookahead1(18);             // S | '"' | "'"
        if (l1 == 16)               // S
        {
          shiftT(16);               // S
        }
        try_DirAttributeValue();
      }
    }
  }

  private void parse_DirAttributeValue()
  {
    eventHandler.startNonterminal("DirAttributeValue", e0);
    lookahead1(15);                 // '"' | "'"
    switch (l1)
    {
    case 27:                        // '"'
      shift(27);                    // '"'
      for (;;)
      {
        lookahead1(110);            // PredefinedEntityRef | EscapeQuot | QuotAttrContentChar | CharRef | '"' | '{' |
                                    // '{{' | '}}'
        if (l1 == 27)               // '"'
        {
          break;
        }
        switch (l1)
        {
        case 7:                     // EscapeQuot
          shift(7);                 // EscapeQuot
          break;
        default:
          parse_QuotAttrValueContent();
        }
      }
      shift(27);                    // '"'
      break;
    default:
      shift(32);                    // "'"
      for (;;)
      {
        lookahead1(111);            // PredefinedEntityRef | EscapeApos | AposAttrContentChar | CharRef | "'" | '{' |
                                    // '{{' | '}}'
        if (l1 == 32)               // "'"
        {
          break;
        }
        switch (l1)
        {
        case 8:                     // EscapeApos
          shift(8);                 // EscapeApos
          break;
        default:
          parse_AposAttrValueContent();
        }
      }
      shift(32);                    // "'"
    }
    eventHandler.endNonterminal("DirAttributeValue", e0);
  }

  private void try_DirAttributeValue()
  {
    lookahead1(15);                 // '"' | "'"
    switch (l1)
    {
    case 27:                        // '"'
      shiftT(27);                   // '"'
      for (;;)
      {
        lookahead1(110);            // PredefinedEntityRef | EscapeQuot | QuotAttrContentChar | CharRef | '"' | '{' |
                                    // '{{' | '}}'
        if (l1 == 27)               // '"'
        {
          break;
        }
        switch (l1)
        {
        case 7:                     // EscapeQuot
          shiftT(7);                // EscapeQuot
          break;
        default:
          try_QuotAttrValueContent();
        }
      }
      shiftT(27);                   // '"'
      break;
    default:
      shiftT(32);                   // "'"
      for (;;)
      {
        lookahead1(111);            // PredefinedEntityRef | EscapeApos | AposAttrContentChar | CharRef | "'" | '{' |
                                    // '{{' | '}}'
        if (l1 == 32)               // "'"
        {
          break;
        }
        switch (l1)
        {
        case 8:                     // EscapeApos
          shiftT(8);                // EscapeApos
          break;
        default:
          try_AposAttrValueContent();
        }
      }
      shiftT(32);                   // "'"
    }
  }

  private void parse_QuotAttrValueContent()
  {
    eventHandler.startNonterminal("QuotAttrValueContent", e0);
    switch (l1)
    {
    case 10:                        // QuotAttrContentChar
      shift(10);                    // QuotAttrContentChar
      break;
    default:
      parse_CommonContent();
    }
    eventHandler.endNonterminal("QuotAttrValueContent", e0);
  }

  private void try_QuotAttrValueContent()
  {
    switch (l1)
    {
    case 10:                        // QuotAttrContentChar
      shiftT(10);                   // QuotAttrContentChar
      break;
    default:
      try_CommonContent();
    }
  }

  private void parse_AposAttrValueContent()
  {
    eventHandler.startNonterminal("AposAttrValueContent", e0);
    switch (l1)
    {
    case 11:                        // AposAttrContentChar
      shift(11);                    // AposAttrContentChar
      break;
    default:
      parse_CommonContent();
    }
    eventHandler.endNonterminal("AposAttrValueContent", e0);
  }

  private void try_AposAttrValueContent()
  {
    switch (l1)
    {
    case 11:                        // AposAttrContentChar
      shiftT(11);                   // AposAttrContentChar
      break;
    default:
      try_CommonContent();
    }
  }

  private void parse_DirElemContent()
  {
    eventHandler.startNonterminal("DirElemContent", e0);
    switch (l1)
    {
    case 51:                        // '<'
    case 52:                        // '<!--'
    case 57:                        // '<?'
      parse_DirectConstructor();
      break;
    case 53:                        // '<![CDATA['
      parse_CDataSection();
      break;
    case 9:                         // ElementContentChar
      shift(9);                     // ElementContentChar
      break;
    default:
      parse_CommonContent();
    }
    eventHandler.endNonterminal("DirElemContent", e0);
  }

  private void try_DirElemContent()
  {
    switch (l1)
    {
    case 51:                        // '<'
    case 52:                        // '<!--'
    case 57:                        // '<?'
      try_DirectConstructor();
      break;
    case 53:                        // '<![CDATA['
      try_CDataSection();
      break;
    case 9:                         // ElementContentChar
      shiftT(9);                    // ElementContentChar
      break;
    default:
      try_CommonContent();
    }
  }

  private void parse_CommonContent()
  {
    eventHandler.startNonterminal("CommonContent", e0);
    switch (l1)
    {
    case 6:                         // PredefinedEntityRef
      shift(6);                     // PredefinedEntityRef
      break;
    case 13:                        // CharRef
      shift(13);                    // CharRef
      break;
    case 194:                       // '{{'
      shift(194);                   // '{{'
      break;
    case 198:                       // '}}'
      shift(198);                   // '}}'
      break;
    default:
      parse_EnclosedExpr();
    }
    eventHandler.endNonterminal("CommonContent", e0);
  }

  private void try_CommonContent()
  {
    switch (l1)
    {
    case 6:                         // PredefinedEntityRef
      shiftT(6);                    // PredefinedEntityRef
      break;
    case 13:                        // CharRef
      shiftT(13);                   // CharRef
      break;
    case 194:                       // '{{'
      shiftT(194);                  // '{{'
      break;
    case 198:                       // '}}'
      shiftT(198);                  // '}}'
      break;
    default:
      try_EnclosedExpr();
    }
  }

  private void parse_DirCommentConstructor()
  {
    eventHandler.startNonterminal("DirCommentConstructor", e0);
    shift(52);                      // '<!--'
    lookahead1(2);                  // DirCommentContents
    shift(21);                      // DirCommentContents
    lookahead1(6);                  // '-->'
    shift(41);                      // '-->'
    eventHandler.endNonterminal("DirCommentConstructor", e0);
  }

  private void try_DirCommentConstructor()
  {
    shiftT(52);                     // '<!--'
    lookahead1(2);                  // DirCommentContents
    shiftT(21);                     // DirCommentContents
    lookahead1(6);                  // '-->'
    shiftT(41);                     // '-->'
  }

  private void parse_DirPIConstructor()
  {
    eventHandler.startNonterminal("DirPIConstructor", e0);
    shift(57);                      // '<?'
    lookahead1(0);                  // PITarget
    shift(12);                      // PITarget
    lookahead1(14);                 // S | '?>'
    if (l1 == 16)                   // S
    {
      shift(16);                    // S
      lookahead1(3);                // DirPIContents
      shift(22);                    // DirPIContents
    }
    lookahead1(9);                  // '?>'
    shift(63);                      // '?>'
    eventHandler.endNonterminal("DirPIConstructor", e0);
  }

  private void try_DirPIConstructor()
  {
    shiftT(57);                     // '<?'
    lookahead1(0);                  // PITarget
    shiftT(12);                     // PITarget
    lookahead1(14);                 // S | '?>'
    if (l1 == 16)                   // S
    {
      shiftT(16);                   // S
      lookahead1(3);                // DirPIContents
      shiftT(22);                   // DirPIContents
    }
    lookahead1(9);                  // '?>'
    shiftT(63);                     // '?>'
  }

  private void parse_CDataSection()
  {
    eventHandler.startNonterminal("CDataSection", e0);
    shift(53);                      // '<![CDATA['
    lookahead1(4);                  // CDataSectionContents
    shift(23);                      // CDataSectionContents
    lookahead1(10);                 // ']]>'
    shift(68);                      // ']]>'
    eventHandler.endNonterminal("CDataSection", e0);
  }

  private void try_CDataSection()
  {
    shiftT(53);                     // '<![CDATA['
    lookahead1(4);                  // CDataSectionContents
    shiftT(23);                     // CDataSectionContents
    lookahead1(10);                 // ']]>'
    shiftT(68);                     // ']]>'
  }

  private void parse_ComputedConstructor()
  {
    eventHandler.startNonterminal("ComputedConstructor", e0);
    switch (l1)
    {
    case 100:                       // 'document'
      parse_CompDocConstructor();
      break;
    case 102:                       // 'element'
      parse_CompElemConstructor();
      break;
    case 76:                        // 'attribute'
      parse_CompAttrConstructor();
      break;
    case 139:                       // 'namespace'
      parse_CompNamespaceConstructor();
      break;
    case 175:                       // 'text'
      parse_CompTextConstructor();
      break;
    case 86:                        // 'comment'
      parse_CompCommentConstructor();
      break;
    default:
      parse_CompPIConstructor();
    }
    eventHandler.endNonterminal("ComputedConstructor", e0);
  }

  private void try_ComputedConstructor()
  {
    switch (l1)
    {
    case 100:                       // 'document'
      try_CompDocConstructor();
      break;
    case 102:                       // 'element'
      try_CompElemConstructor();
      break;
    case 76:                        // 'attribute'
      try_CompAttrConstructor();
      break;
    case 139:                       // 'namespace'
      try_CompNamespaceConstructor();
      break;
    case 175:                       // 'text'
      try_CompTextConstructor();
      break;
    case 86:                        // 'comment'
      try_CompCommentConstructor();
      break;
    default:
      try_CompPIConstructor();
    }
  }

  private void parse_CompDocConstructor()
  {
    eventHandler.startNonterminal("CompDocConstructor", e0);
    shift(100);                     // 'document'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("CompDocConstructor", e0);
  }

  private void try_CompDocConstructor()
  {
    shiftT(100);                    // 'document'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(197);                    // '}'
  }

  private void parse_CompElemConstructor()
  {
    eventHandler.startNonterminal("CompElemConstructor", e0);
    shift(102);                     // 'element'
    lookahead1W(158);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      shift(193);                   // '{'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_Expr();
      shift(197);                   // '}'
      break;
    default:
      whitespace();
      parse_EQName();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(170);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '}'
    if (l1 != 197)                  // '}'
    {
      whitespace();
      parse_ContentExpr();
    }
    shift(197);                     // '}'
    eventHandler.endNonterminal("CompElemConstructor", e0);
  }

  private void try_CompElemConstructor()
  {
    shiftT(102);                    // 'element'
    lookahead1W(158);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      shiftT(193);                  // '{'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_Expr();
      shiftT(197);                  // '}'
      break;
    default:
      try_EQName();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(170);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '}'
    if (l1 != 197)                  // '}'
    {
      try_ContentExpr();
    }
    shiftT(197);                    // '}'
  }

  private void parse_ContentExpr()
  {
    eventHandler.startNonterminal("ContentExpr", e0);
    parse_Expr();
    eventHandler.endNonterminal("ContentExpr", e0);
  }

  private void try_ContentExpr()
  {
    try_Expr();
  }

  private void parse_CompAttrConstructor()
  {
    eventHandler.startNonterminal("CompAttrConstructor", e0);
    shift(76);                      // 'attribute'
    lookahead1W(158);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      shift(193);                   // '{'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_Expr();
      shift(197);                   // '}'
      break;
    default:
      whitespace();
      parse_EQName();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(170);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '}'
    if (l1 != 197)                  // '}'
    {
      whitespace();
      parse_Expr();
    }
    shift(197);                     // '}'
    eventHandler.endNonterminal("CompAttrConstructor", e0);
  }

  private void try_CompAttrConstructor()
  {
    shiftT(76);                     // 'attribute'
    lookahead1W(158);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      shiftT(193);                  // '{'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_Expr();
      shiftT(197);                  // '}'
      break;
    default:
      try_EQName();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(170);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '}'
    if (l1 != 197)                  // '}'
    {
      try_Expr();
    }
    shiftT(197);                    // '}'
  }

  private void parse_CompNamespaceConstructor()
  {
    eventHandler.startNonterminal("CompNamespaceConstructor", e0);
    shift(139);                     // 'namespace'
    lookahead1W(123);               // NCName^Token | S^WS | '(:' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      shift(193);                   // '{'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_PrefixExpr();
      shift(197);                   // '}'
      break;
    default:
      whitespace();
      parse_Prefix();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_URIExpr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("CompNamespaceConstructor", e0);
  }

  private void try_CompNamespaceConstructor()
  {
    shiftT(139);                    // 'namespace'
    lookahead1W(123);               // NCName^Token | S^WS | '(:' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      shiftT(193);                  // '{'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_PrefixExpr();
      shiftT(197);                  // '}'
      break;
    default:
      try_Prefix();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_URIExpr();
    shiftT(197);                    // '}'
  }

  private void parse_Prefix()
  {
    eventHandler.startNonterminal("Prefix", e0);
    parse_NCName();
    eventHandler.endNonterminal("Prefix", e0);
  }

  private void try_Prefix()
  {
    try_NCName();
  }

  private void parse_PrefixExpr()
  {
    eventHandler.startNonterminal("PrefixExpr", e0);
    parse_Expr();
    eventHandler.endNonterminal("PrefixExpr", e0);
  }

  private void try_PrefixExpr()
  {
    try_Expr();
  }

  private void parse_URIExpr()
  {
    eventHandler.startNonterminal("URIExpr", e0);
    parse_Expr();
    eventHandler.endNonterminal("URIExpr", e0);
  }

  private void try_URIExpr()
  {
    try_Expr();
  }

  private void parse_CompTextConstructor()
  {
    eventHandler.startNonterminal("CompTextConstructor", e0);
    shift(175);                     // 'text'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("CompTextConstructor", e0);
  }

  private void try_CompTextConstructor()
  {
    shiftT(175);                    // 'text'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(197);                    // '}'
  }

  private void parse_CompCommentConstructor()
  {
    eventHandler.startNonterminal("CompCommentConstructor", e0);
    shift(86);                      // 'comment'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_Expr();
    shift(197);                     // '}'
    eventHandler.endNonterminal("CompCommentConstructor", e0);
  }

  private void try_CompCommentConstructor()
  {
    shiftT(86);                     // 'comment'
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(166);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_Expr();
    shiftT(197);                    // '}'
  }

  private void parse_CompPIConstructor()
  {
    eventHandler.startNonterminal("CompPIConstructor", e0);
    shift(161);                     // 'processing-instruction'
    lookahead1W(123);               // NCName^Token | S^WS | '(:' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      shift(193);                   // '{'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_Expr();
      shift(197);                   // '}'
      break;
    default:
      whitespace();
      parse_NCName();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shift(193);                     // '{'
    lookahead1W(170);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '}'
    if (l1 != 197)                  // '}'
    {
      whitespace();
      parse_Expr();
    }
    shift(197);                     // '}'
    eventHandler.endNonterminal("CompPIConstructor", e0);
  }

  private void try_CompPIConstructor()
  {
    shiftT(161);                    // 'processing-instruction'
    lookahead1W(123);               // NCName^Token | S^WS | '(:' | 'and' | 'ascending' | 'case' | 'cast' | 'castable' |
                                    // 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' | 'empty' |
                                    // 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' | 'instance' |
                                    // 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' | 'or' |
                                    // 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '{'
    switch (l1)
    {
    case 193:                       // '{'
      shiftT(193);                  // '{'
      lookahead1W(166);             // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_Expr();
      shiftT(197);                  // '}'
      break;
    default:
      try_NCName();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    shiftT(193);                    // '{'
    lookahead1W(170);               // IntegerLiteral | DecimalLiteral | DoubleLiteral | StringLiteral |
                                    // URIQualifiedName | QName^Token | S^WS | Wildcard | '$' | '%' | '(' | '(#' |
                                    // '(:' | '+' | '-' | '.' | '..' | '/' | '//' | '<' | '<!--' | '<?' | '@' |
                                    // 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' |
                                    // 'cast' | 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery' | '}'
    if (l1 != 197)                  // '}'
    {
      try_Expr();
    }
    shiftT(197);                    // '}'
  }

  private void parse_FunctionItemExpr()
  {
    eventHandler.startNonterminal("FunctionItemExpr", e0);
    switch (l1)
    {
    case 115:                       // 'function'
      lookahead2W(61);              // S^WS | '#' | '(' | '(:'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 31:                        // '%'
    case 8563:                      // 'function' '('
      parse_InlineFunctionExpr();
      break;
    default:
      parse_NamedFunctionRef();
    }
    eventHandler.endNonterminal("FunctionItemExpr", e0);
  }

  private void try_FunctionItemExpr()
  {
    switch (l1)
    {
    case 115:                       // 'function'
      lookahead2W(61);              // S^WS | '#' | '(' | '(:'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 31:                        // '%'
    case 8563:                      // 'function' '('
      try_InlineFunctionExpr();
      break;
    default:
      try_NamedFunctionRef();
    }
  }

  private void parse_NamedFunctionRef()
  {
    eventHandler.startNonterminal("NamedFunctionRef", e0);
    parse_EQName();
    lookahead1W(20);                // S^WS | '#' | '(:'
    shift(28);                      // '#'
    lookahead1W(16);                // IntegerLiteral | S^WS | '(:'
    shift(1);                       // IntegerLiteral
    eventHandler.endNonterminal("NamedFunctionRef", e0);
  }

  private void try_NamedFunctionRef()
  {
    try_EQName();
    lookahead1W(20);                // S^WS | '#' | '(:'
    shiftT(28);                     // '#'
    lookahead1W(16);                // IntegerLiteral | S^WS | '(:'
    shiftT(1);                      // IntegerLiteral
  }

  private void parse_InlineFunctionExpr()
  {
    eventHandler.startNonterminal("InlineFunctionExpr", e0);
    for (;;)
    {
      lookahead1W(65);              // S^WS | '%' | '(:' | 'function'
      if (l1 != 31)                 // '%'
      {
        break;
      }
      whitespace();
      parse_Annotation();
    }
    shift(115);                     // 'function'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(63);                // S^WS | '$' | '(:' | ')'
    if (l1 == 30)                   // '$'
    {
      whitespace();
      parse_ParamList();
    }
    shift(36);                      // ')'
    lookahead1W(76);                // S^WS | '(:' | 'as' | '{'
    if (l1 == 73)                   // 'as'
    {
      shift(73);                    // 'as'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      whitespace();
      parse_SequenceType();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    whitespace();
    parse_FunctionBody();
    eventHandler.endNonterminal("InlineFunctionExpr", e0);
  }

  private void try_InlineFunctionExpr()
  {
    for (;;)
    {
      lookahead1W(65);              // S^WS | '%' | '(:' | 'function'
      if (l1 != 31)                 // '%'
      {
        break;
      }
      try_Annotation();
    }
    shiftT(115);                    // 'function'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(63);                // S^WS | '$' | '(:' | ')'
    if (l1 == 30)                   // '$'
    {
      try_ParamList();
    }
    shiftT(36);                     // ')'
    lookahead1W(76);                // S^WS | '(:' | 'as' | '{'
    if (l1 == 73)                   // 'as'
    {
      shiftT(73);                   // 'as'
      lookahead1W(160);             // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
      try_SequenceType();
    }
    lookahead1W(58);                // S^WS | '(:' | '{'
    try_FunctionBody();
  }

  private void parse_SingleType()
  {
    eventHandler.startNonterminal("SingleType", e0);
    parse_SimpleTypeName();
    lookahead1W(136);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | '?' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '|' | '||' | '}'
    if (l1 == 62)                   // '?'
    {
      shift(62);                    // '?'
    }
    eventHandler.endNonterminal("SingleType", e0);
  }

  private void try_SingleType()
  {
    try_SimpleTypeName();
    lookahead1W(136);               // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ';' | '<' | '<<' |
                                    // '<=' | '=' | '>' | '>=' | '>>' | '?' | ']' | 'and' | 'ascending' | 'case' |
                                    // 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' | 'else' |
                                    // 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' | 'only' |
                                    // 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' | 'treat' |
                                    // 'union' | 'where' | '|' | '||' | '}'
    if (l1 == 62)                   // '?'
    {
      shiftT(62);                   // '?'
    }
  }

  private void parse_TypeDeclaration()
  {
    eventHandler.startNonterminal("TypeDeclaration", e0);
    shift(73);                      // 'as'
    lookahead1W(160);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_SequenceType();
    eventHandler.endNonterminal("TypeDeclaration", e0);
  }

  private void try_TypeDeclaration()
  {
    shiftT(73);                     // 'as'
    lookahead1W(160);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_SequenceType();
  }

  private void parse_SequenceType()
  {
    eventHandler.startNonterminal("SequenceType", e0);
    switch (l1)
    {
    case 105:                       // 'empty-sequence'
      lookahead2W(143);             // S^WS | EOF | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | ':=' | ';' | '<' |
                                    // '<<' | '<=' | '=' | '>' | '>=' | '>>' | '?' | ']' | 'allowing' | 'and' |
                                    // 'ascending' | 'at' | 'case' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'external' | 'for' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'in' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'union' | 'where' | '{' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 8553:                      // 'empty-sequence' '('
      shift(105);                   // 'empty-sequence'
      lookahead1W(22);              // S^WS | '(' | '(:'
      shift(33);                    // '('
      lookahead1W(23);              // S^WS | '(:' | ')'
      shift(36);                    // ')'
      break;
    default:
      parse_ItemType();
      lookahead1W(139);             // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ':=' | ';' | '<' |
                                    // '<<' | '<=' | '=' | '>' | '>=' | '>>' | '?' | ']' | 'allowing' | 'and' |
                                    // 'ascending' | 'at' | 'case' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'external' | 'for' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'in' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'union' | 'where' | '{' | '|' | '||' | '}'
      switch (l1)
      {
      case 37:                      // '*'
      case 38:                      // '+'
      case 62:                      // '?'
        whitespace();
        parse_OccurrenceIndicator();
        break;
      default:
        break;
      }
    }
    eventHandler.endNonterminal("SequenceType", e0);
  }

  private void try_SequenceType()
  {
    switch (l1)
    {
    case 105:                       // 'empty-sequence'
      lookahead2W(143);             // S^WS | EOF | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | ':=' | ';' | '<' |
                                    // '<<' | '<=' | '=' | '>' | '>=' | '>>' | '?' | ']' | 'allowing' | 'and' |
                                    // 'ascending' | 'at' | 'case' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'external' | 'for' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'in' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'union' | 'where' | '{' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 8553:                      // 'empty-sequence' '('
      shiftT(105);                  // 'empty-sequence'
      lookahead1W(22);              // S^WS | '(' | '(:'
      shiftT(33);                   // '('
      lookahead1W(23);              // S^WS | '(:' | ')'
      shiftT(36);                   // ')'
      break;
    default:
      try_ItemType();
      lookahead1W(139);             // S^WS | EOF | '!=' | '(:' | ')' | '*' | '+' | ',' | '-' | ':=' | ';' | '<' |
                                    // '<<' | '<=' | '=' | '>' | '>=' | '>>' | '?' | ']' | 'allowing' | 'and' |
                                    // 'ascending' | 'at' | 'case' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'external' | 'for' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'in' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'union' | 'where' | '{' | '|' | '||' | '}'
      switch (l1)
      {
      case 37:                      // '*'
      case 38:                      // '+'
      case 62:                      // '?'
        try_OccurrenceIndicator();
        break;
      default:
        break;
      }
    }
  }

  private void parse_OccurrenceIndicator()
  {
    eventHandler.startNonterminal("OccurrenceIndicator", e0);
    switch (l1)
    {
    case 62:                        // '?'
      shift(62);                    // '?'
      break;
    case 37:                        // '*'
      shift(37);                    // '*'
      break;
    default:
      shift(38);                    // '+'
    }
    eventHandler.endNonterminal("OccurrenceIndicator", e0);
  }

  private void try_OccurrenceIndicator()
  {
    switch (l1)
    {
    case 62:                        // '?'
      shiftT(62);                   // '?'
      break;
    case 37:                        // '*'
      shiftT(37);                   // '*'
      break;
    default:
      shiftT(38);                   // '+'
    }
  }

  private void parse_ItemType()
  {
    eventHandler.startNonterminal("ItemType", e0);
    switch (l1)
    {
    case 76:                        // 'attribute'
    case 86:                        // 'comment'
    case 101:                       // 'document-node'
    case 102:                       // 'element'
    case 115:                       // 'function'
    case 130:                       // 'item'
    case 140:                       // 'namespace-node'
    case 145:                       // 'node'
    case 161:                       // 'processing-instruction'
    case 165:                       // 'schema-attribute'
    case 166:                       // 'schema-element'
    case 175:                       // 'text'
      lookahead2W(143);             // S^WS | EOF | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | ':=' | ';' | '<' |
                                    // '<<' | '<=' | '=' | '>' | '>=' | '>>' | '?' | ']' | 'allowing' | 'and' |
                                    // 'ascending' | 'at' | 'case' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'external' | 'for' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'in' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'union' | 'where' | '{' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 8524:                      // 'attribute' '('
    case 8534:                      // 'comment' '('
    case 8549:                      // 'document-node' '('
    case 8550:                      // 'element' '('
    case 8588:                      // 'namespace-node' '('
    case 8593:                      // 'node' '('
    case 8609:                      // 'processing-instruction' '('
    case 8613:                      // 'schema-attribute' '('
    case 8614:                      // 'schema-element' '('
    case 8623:                      // 'text' '('
      parse_KindTest();
      break;
    case 8578:                      // 'item' '('
      shift(130);                   // 'item'
      lookahead1W(22);              // S^WS | '(' | '(:'
      shift(33);                    // '('
      lookahead1W(23);              // S^WS | '(:' | ')'
      shift(36);                    // ')'
      break;
    case 31:                        // '%'
    case 8563:                      // 'function' '('
      parse_FunctionTest();
      break;
    case 33:                        // '('
      parse_ParenthesizedItemType();
      break;
    default:
      parse_AtomicOrUnionType();
    }
    eventHandler.endNonterminal("ItemType", e0);
  }

  private void try_ItemType()
  {
    switch (l1)
    {
    case 76:                        // 'attribute'
    case 86:                        // 'comment'
    case 101:                       // 'document-node'
    case 102:                       // 'element'
    case 115:                       // 'function'
    case 130:                       // 'item'
    case 140:                       // 'namespace-node'
    case 145:                       // 'node'
    case 161:                       // 'processing-instruction'
    case 165:                       // 'schema-attribute'
    case 166:                       // 'schema-element'
    case 175:                       // 'text'
      lookahead2W(143);             // S^WS | EOF | '!=' | '(' | '(:' | ')' | '*' | '+' | ',' | '-' | ':=' | ';' | '<' |
                                    // '<<' | '<=' | '=' | '>' | '>=' | '>>' | '?' | ']' | 'allowing' | 'and' |
                                    // 'ascending' | 'at' | 'case' | 'collation' | 'count' | 'default' | 'descending' |
                                    // 'div' | 'else' | 'empty' | 'end' | 'eq' | 'except' | 'external' | 'for' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'in' | 'instance' | 'intersect' | 'is' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'ne' | 'only' | 'or' | 'order' | 'return' | 'satisfies' |
                                    // 'stable' | 'start' | 'to' | 'union' | 'where' | '{' | '|' | '||' | '}'
      break;
    default:
      lk = l1;
    }
    switch (lk)
    {
    case 8524:                      // 'attribute' '('
    case 8534:                      // 'comment' '('
    case 8549:                      // 'document-node' '('
    case 8550:                      // 'element' '('
    case 8588:                      // 'namespace-node' '('
    case 8593:                      // 'node' '('
    case 8609:                      // 'processing-instruction' '('
    case 8613:                      // 'schema-attribute' '('
    case 8614:                      // 'schema-element' '('
    case 8623:                      // 'text' '('
      try_KindTest();
      break;
    case 8578:                      // 'item' '('
      shiftT(130);                  // 'item'
      lookahead1W(22);              // S^WS | '(' | '(:'
      shiftT(33);                   // '('
      lookahead1W(23);              // S^WS | '(:' | ')'
      shiftT(36);                   // ')'
      break;
    case 31:                        // '%'
    case 8563:                      // 'function' '('
      try_FunctionTest();
      break;
    case 33:                        // '('
      try_ParenthesizedItemType();
      break;
    default:
      try_AtomicOrUnionType();
    }
  }

  private void parse_AtomicOrUnionType()
  {
    eventHandler.startNonterminal("AtomicOrUnionType", e0);
    parse_EQName();
    eventHandler.endNonterminal("AtomicOrUnionType", e0);
  }

  private void try_AtomicOrUnionType()
  {
    try_EQName();
  }

  private void parse_KindTest()
  {
    eventHandler.startNonterminal("KindTest", e0);
    switch (l1)
    {
    case 101:                       // 'document-node'
      parse_DocumentTest();
      break;
    case 102:                       // 'element'
      parse_ElementTest();
      break;
    case 76:                        // 'attribute'
      parse_AttributeTest();
      break;
    case 166:                       // 'schema-element'
      parse_SchemaElementTest();
      break;
    case 165:                       // 'schema-attribute'
      parse_SchemaAttributeTest();
      break;
    case 161:                       // 'processing-instruction'
      parse_PITest();
      break;
    case 86:                        // 'comment'
      parse_CommentTest();
      break;
    case 175:                       // 'text'
      parse_TextTest();
      break;
    case 140:                       // 'namespace-node'
      parse_NamespaceNodeTest();
      break;
    default:
      parse_AnyKindTest();
    }
    eventHandler.endNonterminal("KindTest", e0);
  }

  private void try_KindTest()
  {
    switch (l1)
    {
    case 101:                       // 'document-node'
      try_DocumentTest();
      break;
    case 102:                       // 'element'
      try_ElementTest();
      break;
    case 76:                        // 'attribute'
      try_AttributeTest();
      break;
    case 166:                       // 'schema-element'
      try_SchemaElementTest();
      break;
    case 165:                       // 'schema-attribute'
      try_SchemaAttributeTest();
      break;
    case 161:                       // 'processing-instruction'
      try_PITest();
      break;
    case 86:                        // 'comment'
      try_CommentTest();
      break;
    case 175:                       // 'text'
      try_TextTest();
      break;
    case 140:                       // 'namespace-node'
      try_NamespaceNodeTest();
      break;
    default:
      try_AnyKindTest();
    }
  }

  private void parse_AnyKindTest()
  {
    eventHandler.startNonterminal("AnyKindTest", e0);
    shift(145);                     // 'node'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("AnyKindTest", e0);
  }

  private void try_AnyKindTest()
  {
    shiftT(145);                    // 'node'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_DocumentTest()
  {
    eventHandler.startNonterminal("DocumentTest", e0);
    shift(101);                     // 'document-node'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(98);                // S^WS | '(:' | ')' | 'element' | 'schema-element'
    if (l1 != 36)                   // ')'
    {
      switch (l1)
      {
      case 102:                     // 'element'
        whitespace();
        parse_ElementTest();
        break;
      default:
        whitespace();
        parse_SchemaElementTest();
      }
    }
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("DocumentTest", e0);
  }

  private void try_DocumentTest()
  {
    shiftT(101);                    // 'document-node'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(98);                // S^WS | '(:' | ')' | 'element' | 'schema-element'
    if (l1 != 36)                   // ')'
    {
      switch (l1)
      {
      case 102:                     // 'element'
        try_ElementTest();
        break;
      default:
        try_SchemaElementTest();
      }
    }
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_TextTest()
  {
    eventHandler.startNonterminal("TextTest", e0);
    shift(175);                     // 'text'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("TextTest", e0);
  }

  private void try_TextTest()
  {
    shiftT(175);                    // 'text'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_CommentTest()
  {
    eventHandler.startNonterminal("CommentTest", e0);
    shift(86);                      // 'comment'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("CommentTest", e0);
  }

  private void try_CommentTest()
  {
    shiftT(86);                     // 'comment'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_NamespaceNodeTest()
  {
    eventHandler.startNonterminal("NamespaceNodeTest", e0);
    shift(140);                     // 'namespace-node'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("NamespaceNodeTest", e0);
  }

  private void try_NamespaceNodeTest()
  {
    shiftT(140);                    // 'namespace-node'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_PITest()
  {
    eventHandler.startNonterminal("PITest", e0);
    shift(161);                     // 'processing-instruction'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(124);               // StringLiteral | NCName^Token | S^WS | '(:' | ')' | 'and' | 'ascending' | 'case' |
                                    // 'cast' | 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' |
                                    // 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' |
                                    // 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' |
                                    // 'only' | 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' |
                                    // 'treat' | 'union' | 'where'
    if (l1 != 36)                   // ')'
    {
      switch (l1)
      {
      case 4:                       // StringLiteral
        shift(4);                   // StringLiteral
        break;
      default:
        whitespace();
        parse_NCName();
      }
    }
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("PITest", e0);
  }

  private void try_PITest()
  {
    shiftT(161);                    // 'processing-instruction'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(124);               // StringLiteral | NCName^Token | S^WS | '(:' | ')' | 'and' | 'ascending' | 'case' |
                                    // 'cast' | 'castable' | 'collation' | 'count' | 'default' | 'descending' | 'div' |
                                    // 'else' | 'empty' | 'end' | 'eq' | 'except' | 'for' | 'ge' | 'group' | 'gt' |
                                    // 'idiv' | 'instance' | 'intersect' | 'is' | 'le' | 'let' | 'lt' | 'mod' | 'ne' |
                                    // 'only' | 'or' | 'order' | 'return' | 'satisfies' | 'stable' | 'start' | 'to' |
                                    // 'treat' | 'union' | 'where'
    if (l1 != 36)                   // ')'
    {
      switch (l1)
      {
      case 4:                       // StringLiteral
        shiftT(4);                  // StringLiteral
        break;
      default:
        try_NCName();
      }
    }
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_AttributeTest()
  {
    eventHandler.startNonterminal("AttributeTest", e0);
    shift(76);                      // 'attribute'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(161);               // URIQualifiedName | QName^Token | S^WS | '(:' | ')' | '*' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      whitespace();
      parse_AttribNameOrWildcard();
      lookahead1W(67);              // S^WS | '(:' | ')' | ','
      if (l1 == 39)                 // ','
      {
        shift(39);                  // ','
        lookahead1W(155);           // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
        whitespace();
        parse_TypeName();
      }
    }
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("AttributeTest", e0);
  }

  private void try_AttributeTest()
  {
    shiftT(76);                     // 'attribute'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(161);               // URIQualifiedName | QName^Token | S^WS | '(:' | ')' | '*' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      try_AttribNameOrWildcard();
      lookahead1W(67);              // S^WS | '(:' | ')' | ','
      if (l1 == 39)                 // ','
      {
        shiftT(39);                 // ','
        lookahead1W(155);           // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
        try_TypeName();
      }
    }
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_AttribNameOrWildcard()
  {
    eventHandler.startNonterminal("AttribNameOrWildcard", e0);
    switch (l1)
    {
    case 37:                        // '*'
      shift(37);                    // '*'
      break;
    default:
      parse_AttributeName();
    }
    eventHandler.endNonterminal("AttribNameOrWildcard", e0);
  }

  private void try_AttribNameOrWildcard()
  {
    switch (l1)
    {
    case 37:                        // '*'
      shiftT(37);                   // '*'
      break;
    default:
      try_AttributeName();
    }
  }

  private void parse_SchemaAttributeTest()
  {
    eventHandler.startNonterminal("SchemaAttributeTest", e0);
    shift(165);                     // 'schema-attribute'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_AttributeDeclaration();
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("SchemaAttributeTest", e0);
  }

  private void try_SchemaAttributeTest()
  {
    shiftT(165);                    // 'schema-attribute'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_AttributeDeclaration();
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_AttributeDeclaration()
  {
    eventHandler.startNonterminal("AttributeDeclaration", e0);
    parse_AttributeName();
    eventHandler.endNonterminal("AttributeDeclaration", e0);
  }

  private void try_AttributeDeclaration()
  {
    try_AttributeName();
  }

  private void parse_ElementTest()
  {
    eventHandler.startNonterminal("ElementTest", e0);
    shift(102);                     // 'element'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(161);               // URIQualifiedName | QName^Token | S^WS | '(:' | ')' | '*' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      whitespace();
      parse_ElementNameOrWildcard();
      lookahead1W(67);              // S^WS | '(:' | ')' | ','
      if (l1 == 39)                 // ','
      {
        shift(39);                  // ','
        lookahead1W(155);           // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
        whitespace();
        parse_TypeName();
        lookahead1W(68);            // S^WS | '(:' | ')' | '?'
        if (l1 == 62)               // '?'
        {
          shift(62);                // '?'
        }
      }
    }
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("ElementTest", e0);
  }

  private void try_ElementTest()
  {
    shiftT(102);                    // 'element'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(161);               // URIQualifiedName | QName^Token | S^WS | '(:' | ')' | '*' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      try_ElementNameOrWildcard();
      lookahead1W(67);              // S^WS | '(:' | ')' | ','
      if (l1 == 39)                 // ','
      {
        shiftT(39);                 // ','
        lookahead1W(155);           // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
        try_TypeName();
        lookahead1W(68);            // S^WS | '(:' | ')' | '?'
        if (l1 == 62)               // '?'
        {
          shiftT(62);               // '?'
        }
      }
    }
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_ElementNameOrWildcard()
  {
    eventHandler.startNonterminal("ElementNameOrWildcard", e0);
    switch (l1)
    {
    case 37:                        // '*'
      shift(37);                    // '*'
      break;
    default:
      parse_ElementName();
    }
    eventHandler.endNonterminal("ElementNameOrWildcard", e0);
  }

  private void try_ElementNameOrWildcard()
  {
    switch (l1)
    {
    case 37:                        // '*'
      shiftT(37);                   // '*'
      break;
    default:
      try_ElementName();
    }
  }

  private void parse_SchemaElementTest()
  {
    eventHandler.startNonterminal("SchemaElementTest", e0);
    shift(166);                     // 'schema-element'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    whitespace();
    parse_ElementDeclaration();
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("SchemaElementTest", e0);
  }

  private void try_SchemaElementTest()
  {
    shiftT(166);                    // 'schema-element'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(155);               // URIQualifiedName | QName^Token | S^WS | '(:' | 'ancestor' | 'ancestor-or-self' |
                                    // 'and' | 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    try_ElementDeclaration();
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_ElementDeclaration()
  {
    eventHandler.startNonterminal("ElementDeclaration", e0);
    parse_ElementName();
    eventHandler.endNonterminal("ElementDeclaration", e0);
  }

  private void try_ElementDeclaration()
  {
    try_ElementName();
  }

  private void parse_AttributeName()
  {
    eventHandler.startNonterminal("AttributeName", e0);
    parse_EQName();
    eventHandler.endNonterminal("AttributeName", e0);
  }

  private void try_AttributeName()
  {
    try_EQName();
  }

  private void parse_ElementName()
  {
    eventHandler.startNonterminal("ElementName", e0);
    parse_EQName();
    eventHandler.endNonterminal("ElementName", e0);
  }

  private void try_ElementName()
  {
    try_EQName();
  }

  private void parse_SimpleTypeName()
  {
    eventHandler.startNonterminal("SimpleTypeName", e0);
    parse_TypeName();
    eventHandler.endNonterminal("SimpleTypeName", e0);
  }

  private void try_SimpleTypeName()
  {
    try_TypeName();
  }

  private void parse_TypeName()
  {
    eventHandler.startNonterminal("TypeName", e0);
    parse_EQName();
    eventHandler.endNonterminal("TypeName", e0);
  }

  private void try_TypeName()
  {
    try_EQName();
  }

  private void parse_FunctionTest()
  {
    eventHandler.startNonterminal("FunctionTest", e0);
    for (;;)
    {
      lookahead1W(65);              // S^WS | '%' | '(:' | 'function'
      if (l1 != 31)                 // '%'
      {
        break;
      }
      whitespace();
      parse_Annotation();
    }
    switch (l1)
    {
    case 115:                       // 'function'
      lookahead2W(22);              // S^WS | '(' | '(:'
      break;
    default:
      lk = l1;
    }
    lk = memoized(3, e0);
    if (lk == 0)
    {
      int b0A = b0; int e0A = e0; int l1A = l1;
      int b1A = b1; int e1A = e1; int l2A = l2;
      int b2A = b2; int e2A = e2;
      try
      {
        try_AnyFunctionTest();
        lk = -1;
      }
      catch (ParseException p1A)
      {
        lk = -2;
      }
      b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
      b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
      b2 = b2A; e2 = e2A; end = e2A; }}
      memoize(3, e0, lk);
    }
    switch (lk)
    {
    case -1:
      whitespace();
      parse_AnyFunctionTest();
      break;
    default:
      whitespace();
      parse_TypedFunctionTest();
    }
    eventHandler.endNonterminal("FunctionTest", e0);
  }

  private void try_FunctionTest()
  {
    for (;;)
    {
      lookahead1W(65);              // S^WS | '%' | '(:' | 'function'
      if (l1 != 31)                 // '%'
      {
        break;
      }
      try_Annotation();
    }
    switch (l1)
    {
    case 115:                       // 'function'
      lookahead2W(22);              // S^WS | '(' | '(:'
      break;
    default:
      lk = l1;
    }
    lk = memoized(3, e0);
    if (lk == 0)
    {
      int b0A = b0; int e0A = e0; int l1A = l1;
      int b1A = b1; int e1A = e1; int l2A = l2;
      int b2A = b2; int e2A = e2;
      try
      {
        try_AnyFunctionTest();
        memoize(3, e0A, -1);
        lk = -3;
      }
      catch (ParseException p1A)
      {
        lk = -2;
        b0 = b0A; e0 = e0A; l1 = l1A; if (l1 == 0) {end = e0A;} else {
        b1 = b1A; e1 = e1A; l2 = l2A; if (l2 == 0) {end = e1A;} else {
        b2 = b2A; e2 = e2A; end = e2A; }}
        memoize(3, e0A, -2);
      }
    }
    switch (lk)
    {
    case -1:
      try_AnyFunctionTest();
      break;
    case -3:
      break;
    default:
      try_TypedFunctionTest();
    }
  }

  private void parse_AnyFunctionTest()
  {
    eventHandler.startNonterminal("AnyFunctionTest", e0);
    shift(115);                     // 'function'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(24);                // S^WS | '(:' | '*'
    shift(37);                      // '*'
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("AnyFunctionTest", e0);
  }

  private void try_AnyFunctionTest()
  {
    shiftT(115);                    // 'function'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(24);                // S^WS | '(:' | '*'
    shiftT(37);                     // '*'
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_TypedFunctionTest()
  {
    eventHandler.startNonterminal("TypedFunctionTest", e0);
    shift(115);                     // 'function'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shift(33);                      // '('
    lookahead1W(163);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | ')' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      whitespace();
      parse_SequenceType();
      for (;;)
      {
        lookahead1W(67);            // S^WS | '(:' | ')' | ','
        if (l1 != 39)               // ','
        {
          break;
        }
        shift(39);                  // ','
        lookahead1W(160);           // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
        whitespace();
        parse_SequenceType();
      }
    }
    shift(36);                      // ')'
    lookahead1W(30);                // S^WS | '(:' | 'as'
    shift(73);                      // 'as'
    lookahead1W(160);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_SequenceType();
    eventHandler.endNonterminal("TypedFunctionTest", e0);
  }

  private void try_TypedFunctionTest()
  {
    shiftT(115);                    // 'function'
    lookahead1W(22);                // S^WS | '(' | '(:'
    shiftT(33);                     // '('
    lookahead1W(163);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | ')' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    if (l1 != 36)                   // ')'
    {
      try_SequenceType();
      for (;;)
      {
        lookahead1W(67);            // S^WS | '(:' | ')' | ','
        if (l1 != 39)               // ','
        {
          break;
        }
        shiftT(39);                 // ','
        lookahead1W(160);           // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
        try_SequenceType();
      }
    }
    shiftT(36);                     // ')'
    lookahead1W(30);                // S^WS | '(:' | 'as'
    shiftT(73);                     // 'as'
    lookahead1W(160);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_SequenceType();
  }

  private void parse_ParenthesizedItemType()
  {
    eventHandler.startNonterminal("ParenthesizedItemType", e0);
    shift(33);                      // '('
    lookahead1W(160);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    whitespace();
    parse_ItemType();
    lookahead1W(23);                // S^WS | '(:' | ')'
    shift(36);                      // ')'
    eventHandler.endNonterminal("ParenthesizedItemType", e0);
  }

  private void try_ParenthesizedItemType()
  {
    shiftT(33);                     // '('
    lookahead1W(160);               // URIQualifiedName | QName^Token | S^WS | '%' | '(' | '(:' | 'ancestor' |
                                    // 'ancestor-or-self' | 'and' | 'ascending' | 'attribute' | 'case' | 'cast' |
                                    // 'castable' | 'child' | 'collation' | 'comment' | 'count' | 'declare' |
                                    // 'default' | 'descendant' | 'descendant-or-self' | 'descending' | 'div' |
                                    // 'document' | 'document-node' | 'element' | 'else' | 'empty' | 'empty-sequence' |
                                    // 'end' | 'eq' | 'every' | 'except' | 'following' | 'following-sibling' | 'for' |
                                    // 'function' | 'ge' | 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' |
                                    // 'intersect' | 'is' | 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' |
                                    // 'namespace' | 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' |
                                    // 'ordered' | 'parent' | 'preceding' | 'preceding-sibling' |
                                    // 'processing-instruction' | 'return' | 'satisfies' | 'schema-attribute' |
                                    // 'schema-element' | 'self' | 'some' | 'stable' | 'start' | 'switch' | 'text' |
                                    // 'to' | 'treat' | 'try' | 'typeswitch' | 'union' | 'unordered' | 'validate' |
                                    // 'where' | 'xquery'
    try_ItemType();
    lookahead1W(23);                // S^WS | '(:' | ')'
    shiftT(36);                     // ')'
  }

  private void parse_URILiteral()
  {
    eventHandler.startNonterminal("URILiteral", e0);
    shift(4);                       // StringLiteral
    eventHandler.endNonterminal("URILiteral", e0);
  }

  private void try_URILiteral()
  {
    shiftT(4);                      // StringLiteral
  }

  private void parse_EQName()
  {
    eventHandler.startNonterminal("EQName", e0);
    lookahead1(153);                // URIQualifiedName | QName^Token | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    switch (l1)
    {
    case 5:                         // URIQualifiedName
      shift(5);                     // URIQualifiedName
      break;
    default:
      parse_QName();
    }
    eventHandler.endNonterminal("EQName", e0);
  }

  private void try_EQName()
  {
    lookahead1(153);                // URIQualifiedName | QName^Token | 'ancestor' | 'ancestor-or-self' | 'and' |
                                    // 'ascending' | 'attribute' | 'case' | 'cast' | 'castable' | 'child' |
                                    // 'collation' | 'comment' | 'count' | 'declare' | 'default' | 'descendant' |
                                    // 'descendant-or-self' | 'descending' | 'div' | 'document' | 'document-node' |
                                    // 'element' | 'else' | 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' |
                                    // 'except' | 'following' | 'following-sibling' | 'for' | 'function' | 'ge' |
                                    // 'group' | 'gt' | 'idiv' | 'if' | 'import' | 'instance' | 'intersect' | 'is' |
                                    // 'item' | 'le' | 'let' | 'lt' | 'mod' | 'module' | 'namespace' |
                                    // 'namespace-node' | 'ne' | 'node' | 'only' | 'or' | 'order' | 'ordered' |
                                    // 'parent' | 'preceding' | 'preceding-sibling' | 'processing-instruction' |
                                    // 'return' | 'satisfies' | 'schema-attribute' | 'schema-element' | 'self' |
                                    // 'some' | 'stable' | 'start' | 'switch' | 'text' | 'to' | 'treat' | 'try' |
                                    // 'typeswitch' | 'union' | 'unordered' | 'validate' | 'where' | 'xquery'
    switch (l1)
    {
    case 5:                         // URIQualifiedName
      shiftT(5);                    // URIQualifiedName
      break;
    default:
      try_QName();
    }
  }

  private void parse_QName()
  {
    eventHandler.startNonterminal("QName", e0);
    lookahead1(152);                // QName^Token | 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' |
                                    // 'attribute' | 'case' | 'cast' | 'castable' | 'child' | 'collation' | 'comment' |
                                    // 'count' | 'declare' | 'default' | 'descendant' | 'descendant-or-self' |
                                    // 'descending' | 'div' | 'document' | 'document-node' | 'element' | 'else' |
                                    // 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' | 'except' | 'following' |
                                    // 'following-sibling' | 'for' | 'function' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'if' | 'import' | 'instance' | 'intersect' | 'is' | 'item' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'module' | 'namespace' | 'namespace-node' | 'ne' | 'node' |
                                    // 'only' | 'or' | 'order' | 'ordered' | 'parent' | 'preceding' |
                                    // 'preceding-sibling' | 'processing-instruction' | 'return' | 'satisfies' |
                                    // 'schema-attribute' | 'schema-element' | 'self' | 'some' | 'stable' | 'start' |
                                    // 'switch' | 'text' | 'to' | 'treat' | 'try' | 'typeswitch' | 'union' |
                                    // 'unordered' | 'validate' | 'where' | 'xquery'
    switch (l1)
    {
    case 76:                        // 'attribute'
      shift(76);                    // 'attribute'
      break;
    case 86:                        // 'comment'
      shift(86);                    // 'comment'
      break;
    case 101:                       // 'document-node'
      shift(101);                   // 'document-node'
      break;
    case 102:                       // 'element'
      shift(102);                   // 'element'
      break;
    case 105:                       // 'empty-sequence'
      shift(105);                   // 'empty-sequence'
      break;
    case 115:                       // 'function'
      shift(115);                   // 'function'
      break;
    case 122:                       // 'if'
      shift(122);                   // 'if'
      break;
    case 130:                       // 'item'
      shift(130);                   // 'item'
      break;
    case 140:                       // 'namespace-node'
      shift(140);                   // 'namespace-node'
      break;
    case 145:                       // 'node'
      shift(145);                   // 'node'
      break;
    case 161:                       // 'processing-instruction'
      shift(161);                   // 'processing-instruction'
      break;
    case 165:                       // 'schema-attribute'
      shift(165);                   // 'schema-attribute'
      break;
    case 166:                       // 'schema-element'
      shift(166);                   // 'schema-element'
      break;
    case 174:                       // 'switch'
      shift(174);                   // 'switch'
      break;
    case 175:                       // 'text'
      shift(175);                   // 'text'
      break;
    case 182:                       // 'typeswitch'
      shift(182);                   // 'typeswitch'
      break;
    default:
      parse_FunctionName();
    }
    eventHandler.endNonterminal("QName", e0);
  }

  private void try_QName()
  {
    lookahead1(152);                // QName^Token | 'ancestor' | 'ancestor-or-self' | 'and' | 'ascending' |
                                    // 'attribute' | 'case' | 'cast' | 'castable' | 'child' | 'collation' | 'comment' |
                                    // 'count' | 'declare' | 'default' | 'descendant' | 'descendant-or-self' |
                                    // 'descending' | 'div' | 'document' | 'document-node' | 'element' | 'else' |
                                    // 'empty' | 'empty-sequence' | 'end' | 'eq' | 'every' | 'except' | 'following' |
                                    // 'following-sibling' | 'for' | 'function' | 'ge' | 'group' | 'gt' | 'idiv' |
                                    // 'if' | 'import' | 'instance' | 'intersect' | 'is' | 'item' | 'le' | 'let' |
                                    // 'lt' | 'mod' | 'module' | 'namespace' | 'namespace-node' | 'ne' | 'node' |
                                    // 'only' | 'or' | 'order' | 'ordered' | 'parent' | 'preceding' |
                                    // 'preceding-sibling' | 'processing-instruction' | 'return' | 'satisfies' |
                                    // 'schema-attribute' | 'schema-element' | 'self' | 'some' | 'stable' | 'start' |
                                    // 'switch' | 'text' | 'to' | 'treat' | 'try' | 'typeswitch' | 'union' |
                                    // 'unordered' | 'validate' | 'where' | 'xquery'
    switch (l1)
    {
    case 76:                        // 'attribute'
      shiftT(76);                   // 'attribute'
      break;
    case 86:                        // 'comment'
      shiftT(86);                   // 'comment'
      break;
    case 101:                       // 'document-node'
      shiftT(101);                  // 'document-node'
      break;
    case 102:                       // 'element'
      shiftT(102);                  // 'element'
      break;
    case 105:                       // 'empty-sequence'
      shiftT(105);                  // 'empty-sequence'
      break;
    case 115:                       // 'function'
      shiftT(115);                  // 'function'
      break;
    case 122:                       // 'if'
      shiftT(122);                  // 'if'
      break;
    case 130:                       // 'item'
      shiftT(130);                  // 'item'
      break;
    case 140:                       // 'namespace-node'
      shiftT(140);                  // 'namespace-node'
      break;
    case 145:                       // 'node'
      shiftT(145);                  // 'node'
      break;
    case 161:                       // 'processing-instruction'
      shiftT(161);                  // 'processing-instruction'
      break;
    case 165:                       // 'schema-attribute'
      shiftT(165);                  // 'schema-attribute'
      break;
    case 166:                       // 'schema-element'
      shiftT(166);                  // 'schema-element'
      break;
    case 174:                       // 'switch'
      shiftT(174);                  // 'switch'
      break;
    case 175:                       // 'text'
      shiftT(175);                  // 'text'
      break;
    case 182:                       // 'typeswitch'
      shiftT(182);                  // 'typeswitch'
      break;
    default:
      try_FunctionName();
    }
  }

  private void parse_FunctionName()
  {
    eventHandler.startNonterminal("FunctionName", e0);
    switch (l1)
    {
    case 15:                        // QName^Token
      shift(15);                    // QName^Token
      break;
    case 70:                        // 'ancestor'
      shift(70);                    // 'ancestor'
      break;
    case 71:                        // 'ancestor-or-self'
      shift(71);                    // 'ancestor-or-self'
      break;
    case 72:                        // 'and'
      shift(72);                    // 'and'
      break;
    case 74:                        // 'ascending'
      shift(74);                    // 'ascending'
      break;
    case 80:                        // 'case'
      shift(80);                    // 'case'
      break;
    case 81:                        // 'cast'
      shift(81);                    // 'cast'
      break;
    case 82:                        // 'castable'
      shift(82);                    // 'castable'
      break;
    case 84:                        // 'child'
      shift(84);                    // 'child'
      break;
    case 85:                        // 'collation'
      shift(85);                    // 'collation'
      break;
    case 90:                        // 'count'
      shift(90);                    // 'count'
      break;
    case 93:                        // 'declare'
      shift(93);                    // 'declare'
      break;
    case 94:                        // 'default'
      shift(94);                    // 'default'
      break;
    case 95:                        // 'descendant'
      shift(95);                    // 'descendant'
      break;
    case 96:                        // 'descendant-or-self'
      shift(96);                    // 'descendant-or-self'
      break;
    case 97:                        // 'descending'
      shift(97);                    // 'descending'
      break;
    case 99:                        // 'div'
      shift(99);                    // 'div'
      break;
    case 100:                       // 'document'
      shift(100);                   // 'document'
      break;
    case 103:                       // 'else'
      shift(103);                   // 'else'
      break;
    case 104:                       // 'empty'
      shift(104);                   // 'empty'
      break;
    case 107:                       // 'end'
      shift(107);                   // 'end'
      break;
    case 108:                       // 'eq'
      shift(108);                   // 'eq'
      break;
    case 109:                       // 'every'
      shift(109);                   // 'every'
      break;
    case 110:                       // 'except'
      shift(110);                   // 'except'
      break;
    case 112:                       // 'following'
      shift(112);                   // 'following'
      break;
    case 113:                       // 'following-sibling'
      shift(113);                   // 'following-sibling'
      break;
    case 114:                       // 'for'
      shift(114);                   // 'for'
      break;
    case 116:                       // 'ge'
      shift(116);                   // 'ge'
      break;
    case 118:                       // 'group'
      shift(118);                   // 'group'
      break;
    case 120:                       // 'gt'
      shift(120);                   // 'gt'
      break;
    case 121:                       // 'idiv'
      shift(121);                   // 'idiv'
      break;
    case 123:                       // 'import'
      shift(123);                   // 'import'
      break;
    case 127:                       // 'instance'
      shift(127);                   // 'instance'
      break;
    case 128:                       // 'intersect'
      shift(128);                   // 'intersect'
      break;
    case 129:                       // 'is'
      shift(129);                   // 'is'
      break;
    case 132:                       // 'le'
      shift(132);                   // 'le'
      break;
    case 134:                       // 'let'
      shift(134);                   // 'let'
      break;
    case 135:                       // 'lt'
      shift(135);                   // 'lt'
      break;
    case 137:                       // 'mod'
      shift(137);                   // 'mod'
      break;
    case 138:                       // 'module'
      shift(138);                   // 'module'
      break;
    case 139:                       // 'namespace'
      shift(139);                   // 'namespace'
      break;
    case 141:                       // 'ne'
      shift(141);                   // 'ne'
      break;
    case 147:                       // 'only'
      shift(147);                   // 'only'
      break;
    case 149:                       // 'or'
      shift(149);                   // 'or'
      break;
    case 150:                       // 'order'
      shift(150);                   // 'order'
      break;
    case 151:                       // 'ordered'
      shift(151);                   // 'ordered'
      break;
    case 153:                       // 'parent'
      shift(153);                   // 'parent'
      break;
    case 157:                       // 'preceding'
      shift(157);                   // 'preceding'
      break;
    case 158:                       // 'preceding-sibling'
      shift(158);                   // 'preceding-sibling'
      break;
    case 162:                       // 'return'
      shift(162);                   // 'return'
      break;
    case 163:                       // 'satisfies'
      shift(163);                   // 'satisfies'
      break;
    case 167:                       // 'self'
      shift(167);                   // 'self'
      break;
    case 169:                       // 'some'
      shift(169);                   // 'some'
      break;
    case 170:                       // 'stable'
      shift(170);                   // 'stable'
      break;
    case 171:                       // 'start'
      shift(171);                   // 'start'
      break;
    case 177:                       // 'to'
      shift(177);                   // 'to'
      break;
    case 178:                       // 'treat'
      shift(178);                   // 'treat'
      break;
    case 179:                       // 'try'
      shift(179);                   // 'try'
      break;
    case 183:                       // 'union'
      shift(183);                   // 'union'
      break;
    case 184:                       // 'unordered'
      shift(184);                   // 'unordered'
      break;
    case 185:                       // 'validate'
      shift(185);                   // 'validate'
      break;
    case 189:                       // 'where'
      shift(189);                   // 'where'
      break;
    default:
      shift(191);                   // 'xquery'
    }
    eventHandler.endNonterminal("FunctionName", e0);
  }

  private void try_FunctionName()
  {
    switch (l1)
    {
    case 15:                        // QName^Token
      shiftT(15);                   // QName^Token
      break;
    case 70:                        // 'ancestor'
      shiftT(70);                   // 'ancestor'
      break;
    case 71:                        // 'ancestor-or-self'
      shiftT(71);                   // 'ancestor-or-self'
      break;
    case 72:                        // 'and'
      shiftT(72);                   // 'and'
      break;
    case 74:                        // 'ascending'
      shiftT(74);                   // 'ascending'
      break;
    case 80:                        // 'case'
      shiftT(80);                   // 'case'
      break;
    case 81:                        // 'cast'
      shiftT(81);                   // 'cast'
      break;
    case 82:                        // 'castable'
      shiftT(82);                   // 'castable'
      break;
    case 84:                        // 'child'
      shiftT(84);                   // 'child'
      break;
    case 85:                        // 'collation'
      shiftT(85);                   // 'collation'
      break;
    case 90:                        // 'count'
      shiftT(90);                   // 'count'
      break;
    case 93:                        // 'declare'
      shiftT(93);                   // 'declare'
      break;
    case 94:                        // 'default'
      shiftT(94);                   // 'default'
      break;
    case 95:                        // 'descendant'
      shiftT(95);                   // 'descendant'
      break;
    case 96:                        // 'descendant-or-self'
      shiftT(96);                   // 'descendant-or-self'
      break;
    case 97:                        // 'descending'
      shiftT(97);                   // 'descending'
      break;
    case 99:                        // 'div'
      shiftT(99);                   // 'div'
      break;
    case 100:                       // 'document'
      shiftT(100);                  // 'document'
      break;
    case 103:                       // 'else'
      shiftT(103);                  // 'else'
      break;
    case 104:                       // 'empty'
      shiftT(104);                  // 'empty'
      break;
    case 107:                       // 'end'
      shiftT(107);                  // 'end'
      break;
    case 108:                       // 'eq'
      shiftT(108);                  // 'eq'
      break;
    case 109:                       // 'every'
      shiftT(109);                  // 'every'
      break;
    case 110:                       // 'except'
      shiftT(110);                  // 'except'
      break;
    case 112:                       // 'following'
      shiftT(112);                  // 'following'
      break;
    case 113:                       // 'following-sibling'
      shiftT(113);                  // 'following-sibling'
      break;
    case 114:                       // 'for'
      shiftT(114);                  // 'for'
      break;
    case 116:                       // 'ge'
      shiftT(116);                  // 'ge'
      break;
    case 118:                       // 'group'
      shiftT(118);                  // 'group'
      break;
    case 120:                       // 'gt'
      shiftT(120);                  // 'gt'
      break;
    case 121:                       // 'idiv'
      shiftT(121);                  // 'idiv'
      break;
    case 123:                       // 'import'
      shiftT(123);                  // 'import'
      break;
    case 127:                       // 'instance'
      shiftT(127);                  // 'instance'
      break;
    case 128:                       // 'intersect'
      shiftT(128);                  // 'intersect'
      break;
    case 129:                       // 'is'
      shiftT(129);                  // 'is'
      break;
    case 132:                       // 'le'
      shiftT(132);                  // 'le'
      break;
    case 134:                       // 'let'
      shiftT(134);                  // 'let'
      break;
    case 135:                       // 'lt'
      shiftT(135);                  // 'lt'
      break;
    case 137:                       // 'mod'
      shiftT(137);                  // 'mod'
      break;
    case 138:                       // 'module'
      shiftT(138);                  // 'module'
      break;
    case 139:                       // 'namespace'
      shiftT(139);                  // 'namespace'
      break;
    case 141:                       // 'ne'
      shiftT(141);                  // 'ne'
      break;
    case 147:                       // 'only'
      shiftT(147);                  // 'only'
      break;
    case 149:                       // 'or'
      shiftT(149);                  // 'or'
      break;
    case 150:                       // 'order'
      shiftT(150);                  // 'order'
      break;
    case 151:                       // 'ordered'
      shiftT(151);                  // 'ordered'
      break;
    case 153:                       // 'parent'
      shiftT(153);                  // 'parent'
      break;
    case 157:                       // 'preceding'
      shiftT(157);                  // 'preceding'
      break;
    case 158:                       // 'preceding-sibling'
      shiftT(158);                  // 'preceding-sibling'
      break;
    case 162:                       // 'return'
      shiftT(162);                  // 'return'
      break;
    case 163:                       // 'satisfies'
      shiftT(163);                  // 'satisfies'
      break;
    case 167:                       // 'self'
      shiftT(167);                  // 'self'
      break;
    case 169:                       // 'some'
      shiftT(169);                  // 'some'
      break;
    case 170:                       // 'stable'
      shiftT(170);                  // 'stable'
      break;
    case 171:                       // 'start'
      shiftT(171);                  // 'start'
      break;
    case 177:                       // 'to'
      shiftT(177);                  // 'to'
      break;
    case 178:                       // 'treat'
      shiftT(178);                  // 'treat'
      break;
    case 179:                       // 'try'
      shiftT(179);                  // 'try'
      break;
    case 183:                       // 'union'
      shiftT(183);                  // 'union'
      break;
    case 184:                       // 'unordered'
      shiftT(184);                  // 'unordered'
      break;
    case 185:                       // 'validate'
      shiftT(185);                  // 'validate'
      break;
    case 189:                       // 'where'
      shiftT(189);                  // 'where'
      break;
    default:
      shiftT(191);                  // 'xquery'
    }
  }

  private void parse_NCName()
  {
    eventHandler.startNonterminal("NCName", e0);
    switch (l1)
    {
    case 14:                        // NCName^Token
      shift(14);                    // NCName^Token
      break;
    case 72:                        // 'and'
      shift(72);                    // 'and'
      break;
    case 74:                        // 'ascending'
      shift(74);                    // 'ascending'
      break;
    case 80:                        // 'case'
      shift(80);                    // 'case'
      break;
    case 81:                        // 'cast'
      shift(81);                    // 'cast'
      break;
    case 82:                        // 'castable'
      shift(82);                    // 'castable'
      break;
    case 85:                        // 'collation'
      shift(85);                    // 'collation'
      break;
    case 90:                        // 'count'
      shift(90);                    // 'count'
      break;
    case 94:                        // 'default'
      shift(94);                    // 'default'
      break;
    case 97:                        // 'descending'
      shift(97);                    // 'descending'
      break;
    case 99:                        // 'div'
      shift(99);                    // 'div'
      break;
    case 103:                       // 'else'
      shift(103);                   // 'else'
      break;
    case 104:                       // 'empty'
      shift(104);                   // 'empty'
      break;
    case 107:                       // 'end'
      shift(107);                   // 'end'
      break;
    case 108:                       // 'eq'
      shift(108);                   // 'eq'
      break;
    case 110:                       // 'except'
      shift(110);                   // 'except'
      break;
    case 114:                       // 'for'
      shift(114);                   // 'for'
      break;
    case 116:                       // 'ge'
      shift(116);                   // 'ge'
      break;
    case 118:                       // 'group'
      shift(118);                   // 'group'
      break;
    case 120:                       // 'gt'
      shift(120);                   // 'gt'
      break;
    case 121:                       // 'idiv'
      shift(121);                   // 'idiv'
      break;
    case 127:                       // 'instance'
      shift(127);                   // 'instance'
      break;
    case 128:                       // 'intersect'
      shift(128);                   // 'intersect'
      break;
    case 129:                       // 'is'
      shift(129);                   // 'is'
      break;
    case 132:                       // 'le'
      shift(132);                   // 'le'
      break;
    case 134:                       // 'let'
      shift(134);                   // 'let'
      break;
    case 135:                       // 'lt'
      shift(135);                   // 'lt'
      break;
    case 137:                       // 'mod'
      shift(137);                   // 'mod'
      break;
    case 141:                       // 'ne'
      shift(141);                   // 'ne'
      break;
    case 147:                       // 'only'
      shift(147);                   // 'only'
      break;
    case 149:                       // 'or'
      shift(149);                   // 'or'
      break;
    case 150:                       // 'order'
      shift(150);                   // 'order'
      break;
    case 162:                       // 'return'
      shift(162);                   // 'return'
      break;
    case 163:                       // 'satisfies'
      shift(163);                   // 'satisfies'
      break;
    case 170:                       // 'stable'
      shift(170);                   // 'stable'
      break;
    case 171:                       // 'start'
      shift(171);                   // 'start'
      break;
    case 177:                       // 'to'
      shift(177);                   // 'to'
      break;
    case 178:                       // 'treat'
      shift(178);                   // 'treat'
      break;
    case 183:                       // 'union'
      shift(183);                   // 'union'
      break;
    default:
      shift(189);                   // 'where'
    }
    eventHandler.endNonterminal("NCName", e0);
  }

  private void try_NCName()
  {
    switch (l1)
    {
    case 14:                        // NCName^Token
      shiftT(14);                   // NCName^Token
      break;
    case 72:                        // 'and'
      shiftT(72);                   // 'and'
      break;
    case 74:                        // 'ascending'
      shiftT(74);                   // 'ascending'
      break;
    case 80:                        // 'case'
      shiftT(80);                   // 'case'
      break;
    case 81:                        // 'cast'
      shiftT(81);                   // 'cast'
      break;
    case 82:                        // 'castable'
      shiftT(82);                   // 'castable'
      break;
    case 85:                        // 'collation'
      shiftT(85);                   // 'collation'
      break;
    case 90:                        // 'count'
      shiftT(90);                   // 'count'
      break;
    case 94:                        // 'default'
      shiftT(94);                   // 'default'
      break;
    case 97:                        // 'descending'
      shiftT(97);                   // 'descending'
      break;
    case 99:                        // 'div'
      shiftT(99);                   // 'div'
      break;
    case 103:                       // 'else'
      shiftT(103);                  // 'else'
      break;
    case 104:                       // 'empty'
      shiftT(104);                  // 'empty'
      break;
    case 107:                       // 'end'
      shiftT(107);                  // 'end'
      break;
    case 108:                       // 'eq'
      shiftT(108);                  // 'eq'
      break;
    case 110:                       // 'except'
      shiftT(110);                  // 'except'
      break;
    case 114:                       // 'for'
      shiftT(114);                  // 'for'
      break;
    case 116:                       // 'ge'
      shiftT(116);                  // 'ge'
      break;
    case 118:                       // 'group'
      shiftT(118);                  // 'group'
      break;
    case 120:                       // 'gt'
      shiftT(120);                  // 'gt'
      break;
    case 121:                       // 'idiv'
      shiftT(121);                  // 'idiv'
      break;
    case 127:                       // 'instance'
      shiftT(127);                  // 'instance'
      break;
    case 128:                       // 'intersect'
      shiftT(128);                  // 'intersect'
      break;
    case 129:                       // 'is'
      shiftT(129);                  // 'is'
      break;
    case 132:                       // 'le'
      shiftT(132);                  // 'le'
      break;
    case 134:                       // 'let'
      shiftT(134);                  // 'let'
      break;
    case 135:                       // 'lt'
      shiftT(135);                  // 'lt'
      break;
    case 137:                       // 'mod'
      shiftT(137);                  // 'mod'
      break;
    case 141:                       // 'ne'
      shiftT(141);                  // 'ne'
      break;
    case 147:                       // 'only'
      shiftT(147);                  // 'only'
      break;
    case 149:                       // 'or'
      shiftT(149);                  // 'or'
      break;
    case 150:                       // 'order'
      shiftT(150);                  // 'order'
      break;
    case 162:                       // 'return'
      shiftT(162);                  // 'return'
      break;
    case 163:                       // 'satisfies'
      shiftT(163);                  // 'satisfies'
      break;
    case 170:                       // 'stable'
      shiftT(170);                  // 'stable'
      break;
    case 171:                       // 'start'
      shiftT(171);                  // 'start'
      break;
    case 177:                       // 'to'
      shiftT(177);                  // 'to'
      break;
    case 178:                       // 'treat'
      shiftT(178);                  // 'treat'
      break;
    case 183:                       // 'union'
      shiftT(183);                  // 'union'
      break;
    default:
      shiftT(189);                  // 'where'
    }
  }

  private void try_Whitespace()
  {
    switch (l1)
    {
    case 17:                        // S^WS
      shiftT(17);                   // S^WS
      break;
    default:
      try_Comment();
    }
  }

  private void try_Comment()
  {
    shiftT(35);                     // '(:'
    for (;;)
    {
      lookahead1(59);               // CommentContents | '(:' | ':)'
      if (l1 == 47)                 // ':)'
      {
        break;
      }
      switch (l1)
      {
      case 18:                      // CommentContents
        shiftT(18);                 // CommentContents
        break;
      default:
        try_Comment();
      }
    }
    shiftT(47);                     // ':)'
  }

  private void shift(int t)
  {
    if (l1 == t)
    {
      whitespace();
      eventHandler.terminal(TOKEN[l1], b1, e1 > size ? size : e1);
      b0 = b1; e0 = e1; l1 = l2; if (l1 != 0) {
      b1 = b2; e1 = e2; l2 = 0; }
    }
    else
    {
      error(b1, e1, 0, l1, t);
    }
  }

  private void shiftT(int t)
  {
    if (l1 == t)
    {
      b0 = b1; e0 = e1; l1 = l2; if (l1 != 0) {
      b1 = b2; e1 = e2; l2 = 0; }
    }
    else
    {
      error(b1, e1, 0, l1, t);
    }
  }

  private void skip(int code)
  {
    int b0W = b0; int e0W = e0; int l1W = l1;
    int b1W = b1; int e1W = e1;

    l1 = code; b1 = begin; e1 = end;
    l2 = 0;

    try_Whitespace();

    b0 = b0W; e0 = e0W; l1 = l1W; if (l1 != 0) {
    b1 = b1W; e1 = e1W; }
  }

  private void whitespace()
  {
    if (e0 != b1)
    {
      b0 = e0;
      e0 = b1;
      eventHandler.whitespace(b0, e0);
    }
  }

  private int matchW(int set)
  {
    int code;
    for (;;)
    {
      code = match(set);
      if (code != 17)               // S^WS
      {
        if (code != 35)             // '(:'
        {
          break;
        }
        skip(code);
      }
    }
    return code;
  }

  private void lookahead1W(int set)
  {
    if (l1 == 0)
    {
      l1 = matchW(set);
      b1 = begin;
      e1 = end;
    }
  }

  private void lookahead2W(int set)
  {
    if (l2 == 0)
    {
      l2 = matchW(set);
      b2 = begin;
      e2 = end;
    }
    lk = (l2 << 8) | l1;
  }

  private void lookahead1(int set)
  {
    if (l1 == 0)
    {
      l1 = match(set);
      b1 = begin;
      e1 = end;
    }
  }

  private int error(int b, int e, int s, int l, int t)
  {
    if (e > ex)
    {
      bx = b;
      ex = e;
      sx = s;
      lx = l;
      tx = t;
    }
    throw new ParseException(bx, ex, sx, lx, tx);
  }


  private void memoize(int i, int e, int v)
  {
    memo.put((e << 2) + i, v);
  }

  private int memoized(int i, int e)
  {
    Integer v = memo.get((e << 2) + i);
    return v == null ? 0 : v;
  }

  private int lk, b0, e0;
  private int l1, b1, e1;
  private int l2, b2, e2;
  private int bx, ex, sx, lx, tx;
  private EventHandler eventHandler = null;
  private java.util.Map<Integer, Integer> memo = new java.util.HashMap<Integer, Integer>();
  private CharSequence input = null;
  private int size = 0;
  private int begin = 0;
  private int end = 0;

  private int match(int tokenSetId)
  {
    boolean nonbmp = false;
    begin = end;
    int current = end;
    int result = INITIAL[tokenSetId];
    int state = 0;

    for (int code = result & 2047; code != 0; )
    {
      int charclass;
      int c0 = current < size ? input.charAt(current) : 0;
      ++current;
      if (c0 < 0x80)
      {
        charclass = MAP0[c0];
      }
      else if (c0 < 0xd800)
      {
        int c1 = c0 >> 4;
        charclass = MAP1[(c0 & 15) + MAP1[(c1 & 31) + MAP1[c1 >> 5]]];
      }
      else
      {
        if (c0 < 0xdc00)
        {
          int c1 = current < size ? input.charAt(current) : 0;
          if (c1 >= 0xdc00 && c1 < 0xe000)
          {
            nonbmp = true;
            ++current;
            c0 = ((c0 & 0x3ff) << 10) + (c1 & 0x3ff) + 0x10000;
          }
          else
          {
            c0 = -1;
          }
        }

        int lo = 0, hi = 5;
        for (int m = 3; ; m = (hi + lo) >> 1)
        {
          if (MAP2[m] > c0) {hi = m - 1;}
          else if (MAP2[6 + m] < c0) {lo = m + 1;}
          else {charclass = MAP2[12 + m]; break;}
          if (lo > hi) {charclass = 0; break;}
        }
      }

      state = code;
      int i0 = (charclass << 11) + code - 1;
      code = TRANSITION[(i0 & 15) + TRANSITION[i0 >> 4]];

      if (code > 2047)
      {
        result = code;
        code &= 2047;
        end = current;
      }
    }

    result >>= 11;
    if (result == 0)
    {
      end = current - 1;
      int c1 = end < size ? input.charAt(end) : 0;
      if (c1 >= 0xdc00 && c1 < 0xe000)
      {
        --end;
      }
      return error(begin, end, state, -1, -1);
    }
    else if (nonbmp)
    {
      for (int i = result >> 8; i > 0; --i)
      {
        --end;
        int c1 = end < size ? input.charAt(end) : 0;
        if (c1 >= 0xdc00 && c1 < 0xe000)
        {
          --end;
        }
      }
    }
    else
    {
      end -= result >> 8;
    }

    return (result & 255) - 1;
  }

  private static String[] getTokenSet(int tokenSetId)
  {
    java.util.ArrayList<String> expected = new java.util.ArrayList<String>();
    int s = tokenSetId < 0 ? - tokenSetId : INITIAL[tokenSetId] & 2047;
    for (int i = 0; i < 199; i += 32)
    {
      int j = i;
      int i0 = (i >> 5) * 1886 + s - 1;
      int i1 = i0 >> 2;
      int i2 = i1 >> 2;
      int f = EXPECTED[(i0 & 3) + EXPECTED[(i1 & 3) + EXPECTED[(i2 & 7) + EXPECTED[i2 >> 3]]]];
      for ( ; f != 0; f >>>= 1, ++j)
      {
        if ((f & 1) != 0)
        {
          expected.add(TOKEN[j]);
        }
      }
    }
    return expected.toArray(new String[]{});
  }

  private static final int[] MAP0 = new int[128];
  static
  {
    final String s1[] =
    {
      /*   0 */ "68, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2",
      /*  34 */ "3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 18, 19, 20",
      /*  61 */ "21, 22, 23, 24, 25, 26, 27, 28, 29, 26, 30, 30, 30, 30, 30, 31, 32, 33, 30, 30, 34, 30, 30, 35, 30",
      /*  86 */ "30, 30, 36, 30, 30, 37, 38, 39, 38, 30, 38, 40, 41, 42, 43, 44, 45, 46, 47, 48, 30, 30, 49, 50, 51",
      /* 111 */ "52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 38, 38"
    };
    String[] s2 = java.util.Arrays.toString(s1).replaceAll("[ \\[\\]]", "").split(",");
    for (int i = 0; i < 128; ++i) {MAP0[i] = Integer.parseInt(s2[i]);}
  }

  private static final int[] MAP1 = new int[456];
  static
  {
    final String s1[] =
    {
      /*   0 */ "108, 124, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 156, 181, 181, 181",
      /*  20 */ "181, 181, 214, 215, 213, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214",
      /*  40 */ "214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214",
      /*  60 */ "214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214",
      /*  80 */ "214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214, 214",
      /* 100 */ "214, 214, 214, 214, 214, 214, 214, 214, 247, 261, 277, 293, 309, 355, 371, 387, 423, 423, 423, 415",
      /* 120 */ "339, 331, 339, 331, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339",
      /* 140 */ "440, 440, 440, 440, 440, 440, 440, 324, 339, 339, 339, 339, 339, 339, 339, 339, 401, 423, 423, 424",
      /* 160 */ "422, 423, 423, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339",
      /* 180 */ "339, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423",
      /* 200 */ "423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 423, 338, 339, 339, 339, 339, 339, 339",
      /* 220 */ "339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339, 339",
      /* 240 */ "339, 339, 339, 339, 339, 339, 423, 68, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 269 */ "0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 17, 17, 17, 17, 17",
      /* 299 */ "17, 17, 17, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 26, 30, 30, 30, 30, 30, 31, 32, 33",
      /* 324 */ "30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 38, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30",
      /* 349 */ "30, 30, 30, 30, 30, 30, 30, 34, 30, 30, 35, 30, 30, 30, 36, 30, 30, 37, 38, 39, 38, 30, 38, 40, 41",
      /* 374 */ "42, 43, 44, 45, 46, 47, 48, 30, 30, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64",
      /* 399 */ "65, 66, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 30, 30, 38, 38, 38, 38, 38, 38, 38, 67, 38",
      /* 424 */ "38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 38, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67",
      /* 449 */ "67, 67, 67, 67, 67, 67, 67"
    };
    String[] s2 = java.util.Arrays.toString(s1).replaceAll("[ \\[\\]]", "").split(",");
    for (int i = 0; i < 456; ++i) {MAP1[i] = Integer.parseInt(s2[i]);}
  }

  private static final int[] MAP2 = new int[18];
  static
  {
    final String s1[] =
    {
      /*  0 */ "57344, 63744, 64976, 65008, 65536, 983040, 63743, 64975, 65007, 65533, 983039, 1114111, 38, 30, 38, 30",
      /* 16 */ "30, 38"
    };
    String[] s2 = java.util.Arrays.toString(s1).replaceAll("[ \\[\\]]", "").split(",");
    for (int i = 0; i < 18; ++i) {MAP2[i] = Integer.parseInt(s2[i]);}
  }

  private static final int[] INITIAL = new int[175];
  static
  {
    final String s1[] =
    {
      /*   0 */ "1, 2, 45059, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27",
      /*  27 */ "28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52",
      /*  52 */ "53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77",
      /*  77 */ "78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102",
      /* 102 */ "103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122",
      /* 122 */ "123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142",
      /* 142 */ "143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162",
      /* 162 */ "163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175"
    };
    String[] s2 = java.util.Arrays.toString(s1).replaceAll("[ \\[\\]]", "").split(",");
    for (int i = 0; i < 175; ++i) {INITIAL[i] = Integer.parseInt(s2[i]);}
  }

  private static final int[] TRANSITION = new int[27047];
  static
  {
    final String s1[] =
    {
      /*     0 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*    14 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*    28 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*    42 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*    56 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*    70 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*    84 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*    98 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*   112 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*   126 */ "21955, 21955, 8832, 8877, 8929, 8897, 8929, 8929, 8881, 8925, 8929, 8909, 8930, 8946, 21955, 10742",
      /*   142 */ "21954, 20492, 21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772, 8983, 9016, 21955, 13727",
      /*   157 */ "21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955",
      /*   172 */ "14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082",
      /*   188 */ "23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632",
      /*   204 */ "9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964",
      /*   220 */ "9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164",
      /*   235 */ "10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955",
      /*   249 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 21955, 21955, 26654, 21955, 21955, 25942",
      /*   263 */ "10352, 10402, 10418, 10665, 10442, 21955, 10742, 21954, 20492, 18465, 20513, 9265, 21955, 21955",
      /*   277 */ "22379, 9864, 26798, 10473, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278",
      /*   292 */ "9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332",
      /*   307 */ "26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546",
      /*   323 */ "9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786",
      /*   339 */ "9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061",
      /*   355 */ "10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983",
      /*   369 */ "10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*   383 */ "21955, 10489, 10505, 21955, 18293, 21955, 14949, 10529, 10524, 21955, 21955, 14097, 10545, 21955",
      /*   397 */ "10742, 21954, 20492, 10594, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10076, 8983, 9016, 21955",
      /*   412 */ "13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201",
      /*   427 */ "21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420",
      /*   443 */ "9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694",
      /*   459 */ "9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917",
      /*   475 */ "9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148",
      /*   490 */ "10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955",
      /*   504 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10611, 10660, 21955, 11409, 21955, 26262",
      /*   518 */ "25942, 10639, 10652, 10681, 11097, 10697, 21955, 10742, 24773, 20492, 10740, 20513, 9265, 21955",
      /*   532 */ "21955, 22379, 9864, 26798, 9995, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996",
      /*   547 */ "10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311",
      /*   562 */ "9332, 26798, 10758, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978",
      /*   578 */ "9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709",
      /*   594 */ "9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027",
      /*   610 */ "10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231",
      /*   624 */ "26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*   638 */ "21955, 21955, 10331, 13235, 21955, 13733, 13240, 21125, 14019, 10788, 21955, 10820, 10808, 10442",
      /*   652 */ "21955, 10742, 21954, 20492, 21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772, 8983, 9016",
      /*   667 */ "21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955",
      /*   682 */ "9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385",
      /*   698 */ "9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664",
      /*   714 */ "9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901",
      /*   730 */ "9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130",
      /*   745 */ "10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955",
      /*   759 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 21955, 21955, 26654, 10892",
      /*   773 */ "21955, 10843, 10788, 10859, 21955, 10879, 10442, 21955, 10742, 21954, 20492, 21955, 20513, 9265",
      /*   787 */ "21955, 21955, 22379, 9864, 26798, 10772, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114",
      /*   802 */ "8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281",
      /*   817 */ "9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525",
      /*   833 */ "10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765",
      /*   849 */ "9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011",
      /*   865 */ "10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798",
      /*   879 */ "10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*   893 */ "21955, 21955, 21955, 10331, 21955, 21955, 26654, 21955, 21955, 15828, 10910, 21955, 21955, 21955",
      /*   907 */ "10930, 21955, 10742, 21954, 20492, 21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772",
      /*   921 */ "8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 10967, 10271, 9178",
      /*   936 */ "11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337",
      /*   951 */ "9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972",
      /*   967 */ "9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885",
      /*   983 */ "9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114",
      /*   998 */ "14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504",
      /*  1012 */ "13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10999, 11015",
      /*  1026 */ "21955, 10578, 21955, 15025, 11039, 11034, 21955, 21955, 16137, 11055, 21955, 10742, 21954, 20492",
      /*  1040 */ "11092, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 11603, 8983, 9016, 21955, 13727, 21955, 20518",
      /*  1055 */ "9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221",
      /*  1070 */ "24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457",
      /*  1086 */ "9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770",
      /*  1102 */ "22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941",
      /*  1118 */ "9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180",
      /*  1132 */ "9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955",
      /*  1146 */ "21955, 21955, 21955, 21955, 21955, 21955, 10331, 11113, 11139, 11120, 11136, 11141, 11213, 11157",
      /*  1160 */ "11171, 11200, 11184, 10442, 21955, 10508, 11229, 20492, 21955, 20513, 9265, 21955, 21955, 22379",
      /*  1174 */ "9864, 26798, 10772, 8983, 11246, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185",
      /*  1189 */ "9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798",
      /*  1204 */ "9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562",
      /*  1220 */ "9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807",
      /*  1236 */ "23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098",
      /*  1252 */ "10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217",
      /*  1266 */ "10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  1280 */ "10331, 11983, 21955, 10951, 11262, 21955, 11264, 11280, 11318, 11326, 11350, 11388, 21955, 10742",
      /*  1294 */ "11425, 20492, 21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772, 8983, 11442, 21955",
      /*  1308 */ "13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201",
      /*  1323 */ "21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420",
      /*  1339 */ "9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694",
      /*  1355 */ "9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917",
      /*  1371 */ "9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148",
      /*  1386 */ "10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955",
      /*  1400 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 11464, 21955, 26654, 21955, 21955",
      /*  1414 */ "25942, 11458, 11480, 11488, 11504, 10442, 21955, 10742, 21954, 20492, 21955, 20513, 9265, 21955",
      /*  1428 */ "21955, 22379, 9864, 26798, 10772, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996",
      /*  1443 */ "10278, 9185, 11555, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311",
      /*  1458 */ "9332, 26798, 11589, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978",
      /*  1474 */ "9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709",
      /*  1490 */ "9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027",
      /*  1506 */ "10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231",
      /*  1520 */ "26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  1534 */ "21955, 21955, 10331, 21955, 21955, 26654, 21955, 21955, 25942, 11619, 11687, 16293, 16286, 10442",
      /*  1548 */ "21955, 10742, 21954, 20492, 21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772, 8983, 9016",
      /*  1563 */ "21955, 13877, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955",
      /*  1578 */ "9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385",
      /*  1594 */ "9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664",
      /*  1610 */ "9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901",
      /*  1626 */ "9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130",
      /*  1641 */ "10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955",
      /*  1655 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 19064, 21955, 26654, 11714",
      /*  1669 */ "21955, 11718, 11703, 11734, 11742, 17386, 10442, 21955, 10742, 21954, 20492, 21955, 20513, 9265",
      /*  1683 */ "21955, 21955, 22379, 9864, 26798, 10772, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114",
      /*  1698 */ "8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281",
      /*  1713 */ "9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525",
      /*  1729 */ "10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765",
      /*  1745 */ "9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011",
      /*  1761 */ "10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798",
      /*  1775 */ "10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  1789 */ "21955, 21955, 21955, 11766, 21955, 21955, 26654, 21955, 21955, 25942, 11789, 11826, 11834, 16361",
      /*  1803 */ "12022, 21955, 10742, 21954, 20492, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597",
      /*  1817 */ "21955, 20805, 11293, 13877, 10945, 12202, 15704, 15822, 21955, 23315, 14176, 14176, 25299, 9747",
      /*  1831 */ "9747, 22895, 21955, 21955, 21955, 21851, 18908, 12202, 15704, 15826, 14175, 14176, 14176, 9239",
      /*  1845 */ "9747, 9747, 9747, 17625, 11403, 21955, 21955, 26298, 22280, 22286, 25133, 14176, 14176, 14329, 9747",
      /*  1860 */ "9747, 9748, 21955, 21955, 21955, 12195, 15704, 20892, 23138, 14176, 26838, 18129, 9747, 22554",
      /*  1874 */ "21955, 18287, 12203, 23314, 25507, 16004, 9747, 11858, 11880, 26904, 20925, 14176, 13594, 9747",
      /*  1888 */ "17783, 17963, 14317, 11898, 11917, 22552, 15299, 25159, 18732, 22555, 11942, 17828, 11958, 24508",
      /*  1902 */ "13593, 20153, 25483, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  1916 */ "21955, 21955, 21955, 21955, 10331, 21955, 21955, 26654, 21955, 21955, 21108, 10788, 21955, 21955",
      /*  1930 */ "12007, 18539, 21955, 10742, 21954, 17235, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747",
      /*  1944 */ "12059, 21955, 20805, 21955, 13727, 21955, 12202, 15704, 15822, 21955, 23315, 14176, 14176, 25299",
      /*  1958 */ "9747, 9747, 9749, 21955, 21955, 21955, 21851, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 9239",
      /*  1973 */ "9747, 9747, 9747, 25705, 21955, 21955, 21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747",
      /*  1988 */ "9747, 9748, 21955, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955",
      /*  2003 */ "21955, 12203, 23314, 14176, 16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706",
      /*  2017 */ "21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593",
      /*  2031 */ "17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  2045 */ "21955, 21955, 21955, 10331, 12156, 21955, 26654, 21955, 21955, 25942, 12180, 12219, 13551, 13545",
      /*  2059 */ "10442, 21955, 10742, 21954, 20492, 12277, 20513, 9047, 21955, 21955, 22379, 9864, 26798, 10772",
      /*  2073 */ "8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178",
      /*  2088 */ "11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337",
      /*  2103 */ "9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972",
      /*  2119 */ "9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885",
      /*  2135 */ "9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114",
      /*  2150 */ "14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504",
      /*  2164 */ "13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 12256",
      /*  2178 */ "21955, 26654, 21955, 21955, 21438, 10788, 21955, 21955, 17366, 18272, 21955, 10742, 21954, 14908",
      /*  2192 */ "21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17945, 21955, 20805, 21955, 10367, 21955",
      /*  2206 */ "12202, 15704, 15822, 21955, 23315, 14176, 14176, 25299, 9747, 9747, 15901, 12273, 21955, 21955",
      /*  2220 */ "10712, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 9239, 9747, 9747, 9747, 12293, 21955, 21955",
      /*  2235 */ "24392, 21955, 22280, 22286, 25133, 14176, 14176, 20987, 9747, 9747, 25432, 15293, 21955, 21955",
      /*  2249 */ "15295, 15704, 20892, 14176, 14176, 12319, 9747, 9747, 22554, 21955, 21955, 12203, 23314, 14176",
      /*  2263 */ "25817, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746",
      /*  2277 */ "22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470",
      /*  2291 */ "20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331",
      /*  2305 */ "18037, 21955, 16158, 11530, 21955, 12352, 12347, 16048, 12368, 11018, 12388, 12428, 24776, 12445",
      /*  2319 */ "20492, 21955, 20513, 9265, 21955, 21955, 14493, 12486, 12998, 12681, 8983, 12464, 21955, 13727",
      /*  2333 */ "21955, 20518, 9032, 9098, 9114, 12480, 12502, 13134, 12518, 12529, 12545, 12671, 21955, 9201, 21955",
      /*  2348 */ "14863, 9221, 24569, 9255, 9281, 12574, 12972, 12484, 12628, 12848, 12660, 13003, 12642, 9385, 9420",
      /*  2363 */ "9082, 23210, 9457, 9488, 13048, 12697, 13143, 12942, 12713, 12741, 12768, 13972, 9578, 9617, 9664",
      /*  2378 */ "9694, 12792, 13299, 13057, 22770, 12823, 13172, 12864, 9786, 9807, 23345, 12930, 12958, 12988",
      /*  2392 */ "12752, 13019, 9917, 9964, 13035, 12600, 13073, 12838, 13089, 10027, 13105, 12588, 13159, 12725",
      /*  2406 */ "14050, 13120, 13188, 13271, 13204, 13220, 14499, 12776, 14503, 12558, 12612, 13257, 13287, 12807",
      /*  2420 */ "13315, 13331, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 14350",
      /*  2434 */ "21955, 26654, 13625, 21955, 25942, 13368, 13394, 13402, 14929, 10442, 21955, 10742, 21954, 20492",
      /*  2448 */ "21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772, 8983, 9016, 21955, 13727, 21955, 20518",
      /*  2463 */ "9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 23173, 9221",
      /*  2478 */ "24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 13439, 9385, 9420, 24966, 23210, 9457",
      /*  2494 */ "9488, 9557, 9525, 10978, 9546, 9562, 9530, 13426, 13450, 9578, 9617, 9664, 9694, 9632, 9648, 9770",
      /*  2510 */ "13466, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 13494, 9880, 9901, 9917, 9964, 9980, 9941",
      /*  2526 */ "9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180",
      /*  2540 */ "9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955",
      /*  2554 */ "21955, 21955, 21955, 21955, 21955, 21955, 10331, 21955, 21955, 26654, 21955, 21955, 21955, 13530",
      /*  2568 */ "13567, 13575, 13610, 10442, 21955, 10742, 21954, 20492, 21955, 20513, 9678, 21955, 21955, 22379",
      /*  2582 */ "9864, 26798, 13660, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185",
      /*  2597 */ "9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798",
      /*  2612 */ "9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562",
      /*  2628 */ "9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807",
      /*  2644 */ "23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098",
      /*  2660 */ "10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217",
      /*  2674 */ "10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  2688 */ "13676, 13846, 21955, 26654, 21955, 21955, 25942, 13712, 13749, 13757, 15216, 10442, 13781, 10742",
      /*  2702 */ "21954, 20492, 21955, 20513, 9472, 13799, 21955, 22379, 9864, 26798, 13816, 8983, 9016, 21955, 13727",
      /*  2717 */ "21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955",
      /*  2732 */ "14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082",
      /*  2748 */ "23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632",
      /*  2764 */ "9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964",
      /*  2780 */ "9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164",
      /*  2795 */ "10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955",
      /*  2809 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 13832, 13842, 21955, 26654, 21955, 21955, 25942",
      /*  2823 */ "13862, 13900, 13908, 15460, 13924, 21955, 10742, 21954, 20492, 21955, 20513, 9265, 13940, 21955",
      /*  2837 */ "22379, 9864, 26798, 10244, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278",
      /*  2852 */ "9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332",
      /*  2867 */ "26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546",
      /*  2883 */ "9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786",
      /*  2899 */ "9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061",
      /*  2915 */ "10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983",
      /*  2929 */ "10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  2943 */ "21955, 13957, 21955, 21955, 26654, 14014, 21955, 25942, 10788, 19293, 21955, 10386, 14035, 21955",
      /*  2957 */ "10742, 21954, 20492, 12372, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 14066, 8983, 9016, 21955",
      /*  2972 */ "13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201",
      /*  2987 */ "21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420",
      /*  3003 */ "9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694",
      /*  3019 */ "9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917",
      /*  3035 */ "9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148",
      /*  3050 */ "10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955",
      /*  3064 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 21955, 21955, 26654, 21955, 21955",
      /*  3078 */ "25942, 10788, 21955, 21955, 14082, 10442, 21955, 10742, 21954, 20492, 21955, 20513, 9265, 21955",
      /*  3092 */ "21955, 22379, 9864, 26798, 10772, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996",
      /*  3107 */ "10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311",
      /*  3122 */ "9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978",
      /*  3138 */ "9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709",
      /*  3154 */ "9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027",
      /*  3170 */ "10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231",
      /*  3184 */ "26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  3198 */ "21955, 21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668, 19408, 16122",
      /*  3212 */ "21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597, 21955",
      /*  3226 */ "20805, 21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747",
      /*  3240 */ "9749, 21955, 21955, 21955, 24394, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747",
      /*  3254 */ "9747, 9747, 15285, 21955, 21955, 24392, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747",
      /*  3269 */ "25432, 15293, 21955, 21955, 14193, 15704, 20892, 14176, 14176, 14216, 9747, 9747, 22554, 21955",
      /*  3283 */ "21955, 12203, 23314, 14176, 25817, 9747, 18822, 21955, 17954, 20925, 14176, 13594, 9747, 25706",
      /*  3297 */ "21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593",
      /*  3311 */ "17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  3325 */ "21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668, 19408",
      /*  3339 */ "16122, 21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597",
      /*  3353 */ "21955, 20805, 21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747",
      /*  3367 */ "9747, 9749, 21955, 21955, 21955, 24394, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087",
      /*  3381 */ "9747, 9747, 9747, 15285, 21955, 21955, 24392, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747",
      /*  3396 */ "9747, 25432, 15293, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 14216, 9747, 9747, 22554",
      /*  3410 */ "21955, 21955, 12203, 23314, 14176, 25817, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747",
      /*  3424 */ "25706, 21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508",
      /*  3438 */ "13593, 17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  3452 */ "21955, 21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668",
      /*  3466 */ "19408, 16122, 21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747",
      /*  3480 */ "17597, 21955, 20805, 21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169",
      /*  3494 */ "9747, 9747, 9749, 21955, 21955, 21955, 24394, 9396, 12202, 15704, 15826, 14175, 14176, 14176, 23087",
      /*  3509 */ "9747, 9747, 9747, 15285, 21955, 21955, 24392, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747",
      /*  3524 */ "9747, 25432, 15293, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 14216, 9747, 9747, 22554",
      /*  3538 */ "21955, 21955, 12203, 23314, 14176, 25817, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747",
      /*  3552 */ "25706, 21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508",
      /*  3566 */ "13593, 17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  3580 */ "21955, 21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668",
      /*  3594 */ "19408, 16122, 21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747",
      /*  3608 */ "17597, 21955, 20805, 21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169",
      /*  3622 */ "9747, 9747, 9749, 21955, 21955, 21955, 24394, 21955, 12202, 15704, 15826, 14175, 14176, 14176",
      /*  3636 */ "23087, 9747, 9747, 9747, 15285, 21955, 21955, 24392, 14252, 22280, 22286, 25133, 14176, 14176",
      /*  3650 */ "14329, 9747, 9747, 25432, 15293, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 14216, 9747, 9747",
      /*  3665 */ "22554, 21955, 21955, 12203, 23314, 14176, 25817, 9747, 18822, 21955, 21955, 20925, 14176, 13594",
      /*  3679 */ "9747, 25706, 21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589",
      /*  3693 */ "24508, 13593, 17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955",
      /*  3707 */ "21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955",
      /*  3721 */ "25668, 19408, 16122, 21955, 10742, 21954, 10566, 21955, 15297, 22283, 21955, 21955, 13583, 25135",
      /*  3735 */ "9747, 17597, 21955, 20805, 21955, 10572, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176",
      /*  3749 */ "17169, 9747, 9747, 9749, 21955, 21955, 21955, 24394, 21955, 12202, 15704, 15826, 14175, 14176",
      /*  3763 */ "14176, 23087, 9747, 9747, 9747, 15285, 21955, 21955, 24392, 21955, 22280, 22286, 25133, 14176",
      /*  3777 */ "14176, 14329, 9747, 9747, 25432, 15293, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 14216",
      /*  3791 */ "9747, 9747, 22554, 21955, 21955, 12203, 23314, 14176, 25817, 9747, 18822, 21955, 21955, 20925",
      /*  3805 */ "14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684",
      /*  3819 */ "18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955",
      /*  3833 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942",
      /*  3847 */ "14128, 21955, 25668, 19408, 16122, 21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955",
      /*  3861 */ "13583, 25135, 9747, 17597, 21955, 20805, 21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172",
      /*  3875 */ "14176, 14176, 17169, 9747, 9747, 9749, 21955, 21955, 21955, 21955, 21955, 12202, 15704, 15826",
      /*  3889 */ "14175, 14176, 14176, 23087, 9747, 9747, 9747, 25705, 21955, 21955, 21955, 21955, 22280, 22286",
      /*  3903 */ "25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955, 21955, 21955, 15295, 15704, 20892, 14176",
      /*  3917 */ "14176, 26838, 9747, 9747, 22554, 21955, 21955, 12203, 23314, 14176, 16004, 9747, 18822, 21955",
      /*  3931 */ "21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745",
      /*  3945 */ "22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955",
      /*  3959 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955",
      /*  3973 */ "21955, 25942, 14128, 21955, 25668, 19408, 16122, 21955, 10742, 21954, 21955, 21955, 15297, 22283",
      /*  3987 */ "21955, 21955, 13583, 25135, 9747, 19284, 21955, 20805, 21955, 21955, 21955, 12202, 15704, 15822",
      /*  4001 */ "21955, 14172, 14176, 14176, 17169, 9747, 9747, 9749, 21955, 21955, 21955, 21955, 21955, 12202",
      /*  4015 */ "15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747, 9747, 25705, 21955, 21955, 21955, 21955",
      /*  4029 */ "22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955, 21955, 21955, 15295, 15704",
      /*  4043 */ "20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 21955, 12203, 23314, 14176, 16004, 9747",
      /*  4057 */ "18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746, 22552, 15299",
      /*  4071 */ "14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470, 20949, 23751",
      /*  4085 */ "12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955",
      /*  4099 */ "26654, 21955, 21955, 25942, 14128, 21955, 25668, 19408, 14272, 21955, 10742, 21954, 21955, 21955",
      /*  4113 */ "15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597, 21955, 20805, 21955, 21955, 21955, 12202",
      /*  4127 */ "15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747, 9749, 21955, 21955, 21955, 21955",
      /*  4141 */ "21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747, 9747, 25705, 21955, 21955",
      /*  4155 */ "21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955, 21955, 21955",
      /*  4169 */ "15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 21955, 12203, 23314, 14176",
      /*  4183 */ "16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746",
      /*  4197 */ "22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470",
      /*  4211 */ "20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381",
      /*  4225 */ "21955, 21955, 26654, 21955, 21955, 25942, 14303, 21955, 25668, 19408, 16122, 21955, 10742, 21954",
      /*  4239 */ "21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597, 21955, 20805, 21955, 21955",
      /*  4253 */ "16819, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747, 9749, 21955, 21955",
      /*  4267 */ "21955, 21955, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747, 9747, 25705",
      /*  4281 */ "21955, 21955, 21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955",
      /*  4295 */ "21955, 21955, 15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 21955, 12203",
      /*  4309 */ "23314, 14176, 16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269",
      /*  4323 */ "14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760",
      /*  4337 */ "20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  4351 */ "21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25737, 25751, 16122, 21955",
      /*  4365 */ "10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597, 21955, 20805",
      /*  4379 */ "21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747, 9749",
      /*  4393 */ "21955, 21955, 21955, 21955, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747",
      /*  4407 */ "9747, 25705, 21955, 21955, 21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748",
      /*  4422 */ "21955, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 21955",
      /*  4436 */ "12203, 23314, 14176, 16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955",
      /*  4450 */ "23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835",
      /*  4464 */ "23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  4478 */ "21955, 21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668, 19408, 16122",
      /*  4492 */ "21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597, 21955",
      /*  4506 */ "20805, 21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747",
      /*  4520 */ "9749, 21955, 21955, 21955, 21955, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747",
      /*  4534 */ "9747, 9747, 25705, 21955, 21955, 21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747",
      /*  4549 */ "9748, 21955, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955",
      /*  4563 */ "10426, 12203, 23314, 14176, 16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706",
      /*  4577 */ "21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593",
      /*  4591 */ "17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  4605 */ "21955, 21955, 21955, 14345, 21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668, 19408",
      /*  4619 */ "16122, 21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597",
      /*  4633 */ "21955, 20805, 21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747",
      /*  4647 */ "9747, 9749, 21955, 21955, 21955, 21955, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087",
      /*  4661 */ "9747, 9747, 9747, 25705, 21955, 21955, 21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747",
      /*  4676 */ "9747, 9748, 21955, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955",
      /*  4691 */ "21955, 12203, 23314, 14176, 16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706",
      /*  4705 */ "21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593",
      /*  4719 */ "17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  4733 */ "21955, 21955, 21955, 10331, 21955, 21955, 26654, 21955, 21955, 25942, 14366, 14402, 14444, 15943",
      /*  4747 */ "10442, 21955, 10742, 21954, 20492, 21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772",
      /*  4761 */ "8983, 9016, 21955, 13727, 16152, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178",
      /*  4776 */ "11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337",
      /*  4791 */ "9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972",
      /*  4807 */ "9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885",
      /*  4823 */ "9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 14475, 10061, 10098, 10092, 10114",
      /*  4838 */ "14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504",
      /*  4852 */ "13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 21955",
      /*  4866 */ "21955, 26654, 21955, 21955, 25942, 10788, 21955, 21955, 21955, 10442, 21955, 10742, 21954, 20492",
      /*  4880 */ "21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772, 8983, 9016, 21955, 13727, 21955, 20518",
      /*  4895 */ "9032, 9098, 9114, 8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221",
      /*  4910 */ "24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457",
      /*  4926 */ "9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770",
      /*  4942 */ "22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941",
      /*  4958 */ "9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180",
      /*  4972 */ "9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955",
      /*  4986 */ "21955, 21955, 21955, 21955, 21955, 21955, 14519, 21955, 21955, 26654, 21955, 21955, 25942, 14546",
      /*  5000 */ "14572, 14580, 16870, 14604, 21955, 10742, 21954, 20492, 21955, 20513, 9265, 21955, 21955, 22379",
      /*  5014 */ "9864, 26798, 14635, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185",
      /*  5029 */ "9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798",
      /*  5044 */ "9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562",
      /*  5060 */ "9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807",
      /*  5076 */ "23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098",
      /*  5092 */ "10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217",
      /*  5106 */ "10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  5120 */ "17381, 9369, 21955, 26654, 17335, 21955, 14651, 14667, 14683, 14699, 14713, 16122, 14729, 14774",
      /*  5134 */ "21954, 20034, 14753, 25113, 23381, 14769, 14790, 14806, 21822, 14834, 14887, 21955, 20805, 14924",
      /*  5148 */ "21955, 14945, 24820, 15704, 14965, 21955, 14985, 14176, 26633, 17169, 9747, 9747, 24902, 21955",
      /*  5162 */ "15004, 15021, 24394, 21955, 10315, 15705, 26614, 15041, 14176, 20006, 20699, 21548, 9747, 22929",
      /*  5176 */ "15285, 15069, 21955, 11634, 21955, 15088, 11372, 15104, 21343, 14176, 14329, 15132, 15155, 25432",
      /*  5190 */ "15176, 15211, 12131, 15232, 15704, 20892, 14176, 24488, 14216, 9747, 24105, 15255, 13345, 21955",
      /*  5204 */ "12203, 23314, 23503, 25817, 23804, 18822, 21955, 21955, 20925, 25968, 9739, 15271, 25706, 21955",
      /*  5218 */ "23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 21308, 15315, 21413, 13593, 15994",
      /*  5232 */ "23760, 15336, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  5246 */ "21955, 21955, 17381, 21956, 15365, 26654, 21955, 21955, 25942, 15383, 15435, 25668, 19408, 16122",
      /*  5260 */ "21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597, 21955",
      /*  5274 */ "20805, 21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747",
      /*  5288 */ "9749, 21955, 21955, 20588, 24394, 15455, 19617, 19989, 15826, 14175, 14176, 22458, 23087, 9747",
      /*  5302 */ "9747, 21581, 15285, 21955, 21955, 24392, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747",
      /*  5316 */ "9747, 25432, 15293, 21955, 17024, 12644, 15704, 15476, 14176, 14176, 15511, 9747, 9747, 22554",
      /*  5330 */ "21955, 21955, 12203, 23314, 14176, 25817, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747",
      /*  5344 */ "25706, 21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508",
      /*  5358 */ "15527, 15543, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  5372 */ "21955, 21955, 21955, 21955, 17381, 21955, 15567, 26654, 21955, 21955, 17786, 15591, 15607, 15623",
      /*  5386 */ "15637, 16122, 21955, 9135, 21954, 10037, 21955, 15297, 22283, 21955, 21955, 13583, 19031, 18859",
      /*  5400 */ "17597, 19203, 20805, 15653, 21955, 12068, 15669, 15702, 19647, 21955, 15721, 15756, 14176, 11671",
      /*  5414 */ "15775, 9747, 9749, 13696, 25707, 21955, 24394, 12429, 15801, 15704, 15826, 15844, 21884, 15857",
      /*  5428 */ "23087, 23839, 15880, 15899, 15285, 21955, 21955, 13884, 21955, 22280, 22286, 25133, 14176, 21508",
      /*  5442 */ "14329, 9747, 9747, 26533, 15293, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 14216, 9747, 9747",
      /*  5457 */ "22554, 21955, 21955, 17060, 15917, 20424, 25817, 17476, 25194, 15938, 21955, 22980, 24413, 13594",
      /*  5471 */ "20561, 25706, 21955, 23269, 26578, 9746, 15959, 15299, 14176, 9745, 22555, 26684, 18823, 13589",
      /*  5485 */ "22200, 15981, 17835, 23760, 20894, 20470, 20949, 16014, 16030, 21955, 21955, 21955, 21955, 21955",
      /*  5499 */ "21955, 21955, 21955, 21955, 21955, 17381, 21955, 26870, 26654, 10894, 15439, 25942, 16064, 16080",
      /*  5513 */ "16091, 16107, 16122, 21955, 10742, 21954, 21955, 21955, 15297, 15815, 17874, 17869, 16174, 16196",
      /*  5527 */ "24130, 17597, 18803, 16244, 9441, 21955, 21955, 21636, 24827, 26139, 16272, 16309, 14176, 16325",
      /*  5541 */ "19510, 23914, 21083, 9749, 21955, 19946, 24015, 24394, 21955, 12202, 15704, 15826, 14175, 14176",
      /*  5555 */ "14176, 23087, 9747, 9747, 9747, 16484, 21955, 13636, 16347, 21955, 22280, 22286, 23278, 14176",
      /*  5569 */ "14176, 16377, 16433, 9747, 17286, 15293, 21955, 21955, 14236, 15704, 23078, 14176, 16452, 16470",
      /*  5583 */ "9747, 18122, 22554, 23479, 21955, 16500, 24855, 25334, 25817, 16533, 18822, 21955, 21955, 20925",
      /*  5597 */ "14176, 13594, 9747, 25706, 16592, 23269, 16454, 9746, 17228, 15299, 14176, 9745, 22555, 26684",
      /*  5611 */ "18823, 19388, 24508, 17768, 17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955",
      /*  5625 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 12914, 26654, 27015, 16609, 16627",
      /*  5639 */ "16643, 16659, 16675, 16689, 16122, 20103, 14737, 16256, 24997, 21694, 16705, 16721, 10457, 24807",
      /*  5653 */ "13998, 16755, 16793, 17597, 22642, 17972, 16809, 16843, 16865, 12202, 15704, 22847, 18030, 16886",
      /*  5667 */ "16928, 16409, 16977, 23878, 24084, 23847, 21626, 19157, 17013, 17048, 21955, 17076, 17092, 20284",
      /*  5681 */ "17125, 17147, 17185, 17213, 17251, 20395, 17267, 17302, 17326, 13800, 24392, 17351, 17402, 25082",
      /*  5695 */ "18446, 14176, 17437, 17457, 17492, 22949, 18329, 17310, 26016, 17527, 15295, 16517, 20892, 17569",
      /*  5709 */ "15419, 14216, 16953, 17585, 17622, 21858, 17641, 19227, 17660, 21336, 25817, 17688, 18822, 10863",
      /*  5723 */ "17716, 17738, 17802, 17851, 17890, 17906, 10336, 14142, 23713, 17930, 22552, 17988, 11901, 9745",
      /*  5737 */ "18017, 18053, 18094, 16180, 19436, 18110, 18145, 19274, 20894, 20570, 18161, 23751, 12894, 21955",
      /*  5751 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 26654, 18185",
      /*  5765 */ "21955, 18225, 18241, 18198, 18209, 18257, 16122, 21955, 9062, 21954, 21955, 21955, 15297, 22283",
      /*  5779 */ "21955, 21955, 13583, 18309, 24438, 17597, 16777, 20805, 18345, 21955, 11810, 18365, 15704, 15822",
      /*  5793 */ "21955, 18393, 14176, 14176, 17169, 18420, 9747, 9749, 21955, 21955, 21955, 24394, 21955, 12202",
      /*  5807 */ "15704, 15826, 14175, 14176, 15740, 23087, 9747, 9747, 17280, 15285, 21955, 21955, 24392, 21955",
      /*  5821 */ "22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 25432, 15293, 21955, 21955, 22050, 19624",
      /*  5835 */ "20892, 14176, 24230, 14216, 9747, 26094, 22554, 21955, 21955, 12203, 23314, 14176, 25817, 9747",
      /*  5849 */ "18822, 21955, 21955, 20925, 14176, 13594, 9747, 12907, 21955, 23269, 14176, 9746, 22552, 15299",
      /*  5863 */ "14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 21388, 18438, 25827",
      /*  5877 */ "12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955",
      /*  5891 */ "26654, 21955, 18462, 25942, 18481, 18497, 18508, 18524, 16122, 21955, 10742, 21954, 21955, 22258",
      /*  5905 */ "15297, 22283, 21955, 21955, 13583, 25135, 9747, 17700, 21955, 20805, 21955, 21955, 24309, 12202",
      /*  5919 */ "15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747, 9749, 21955, 21955, 21955, 21955",
      /*  5933 */ "21955, 12202, 15704, 15826, 14175, 14176, 14176, 21010, 9747, 9747, 9747, 25705, 21955, 21955",
      /*  5947 */ "21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955, 21955, 21955",
      /*  5961 */ "15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 18576, 12203, 23314, 14176",
      /*  5975 */ "16004, 9747, 18822, 12403, 18593, 18628, 14176, 13594, 9747, 25706, 11334, 18676, 22995, 18705",
      /*  5989 */ "22552, 11539, 24475, 23032, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470",
      /*  6003 */ "15349, 18721, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381",
      /*  6017 */ "21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668, 19408, 16122, 21955, 17722, 21954",
      /*  6031 */ "21955, 21259, 15297, 17109, 21955, 21955, 16827, 23143, 18748, 18767, 21955, 13378, 10560, 18800",
      /*  6045 */ "21955, 12202, 15704, 15822, 21955, 14172, 14176, 16331, 17169, 9747, 9747, 18819, 21955, 21955",
      /*  6059 */ "21955, 21955, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747, 9747, 25705",
      /*  6073 */ "18839, 21955, 21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955",
      /*  6087 */ "26781, 21955, 15295, 15704, 20892, 14176, 21671, 26838, 9747, 20320, 22554, 21955, 21955, 12203",
      /*  6101 */ "23314, 14176, 16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269",
      /*  6115 */ "14176, 9746, 22552, 15299, 14176, 18857, 18875, 26684, 18823, 13589, 24508, 13593, 17835, 23760",
      /*  6129 */ "20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  6143 */ "21955, 17381, 21955, 12448, 26654, 26375, 18894, 14287, 18924, 18940, 18956, 18970, 16122, 21955",
      /*  6157 */ "17644, 21954, 21955, 21955, 18986, 22283, 26834, 26829, 9404, 25135, 19002, 17597, 21955, 20805",
      /*  6171 */ "19529, 21955, 21955, 12202, 25549, 18377, 19195, 19022, 21892, 15759, 22704, 20447, 9747, 22407",
      /*  6185 */ "11426, 24301, 19144, 19047, 19115, 11365, 21179, 15826, 14175, 14176, 15495, 17816, 9747, 9747",
      /*  6199 */ "19080, 25705, 21955, 12095, 19096, 21955, 22280, 22286, 21503, 14176, 14176, 14818, 9747, 9747",
      /*  6213 */ "9748, 21955, 19131, 19185, 19219, 15704, 20892, 22877, 14176, 26838, 24945, 9747, 17503, 21955",
      /*  6227 */ "21955, 19243, 21724, 19259, 19309, 19798, 19348, 21955, 9791, 19376, 15487, 19404, 19424, 15965",
      /*  6241 */ "10914, 23269, 14176, 9746, 18660, 15299, 14176, 9745, 22555, 26684, 18823, 20937, 19462, 13593",
      /*  6255 */ "17835, 23760, 22210, 19490, 20949, 23751, 16211, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  6269 */ "21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955, 19526, 16849, 19545, 19561, 19572, 19588",
      /*  6283 */ "16122, 13783, 10742, 21954, 19604, 22258, 13644, 19640, 21955, 10045, 19663, 25135, 18324, 12331",
      /*  6297 */ "9295, 20805, 21955, 19699, 9294, 19719, 24328, 15822, 21955, 19761, 23607, 16900, 16942, 19787",
      /*  6311 */ "22130, 19822, 12043, 21955, 12257, 21955, 21955, 16997, 15704, 15826, 19850, 15734, 14176, 21010",
      /*  6325 */ "19884, 19906, 9747, 25705, 10827, 21955, 19923, 19944, 22280, 19730, 25133, 23461, 24542, 14329",
      /*  6339 */ "9747, 20190, 19962, 11773, 21955, 21955, 15295, 19986, 20892, 20005, 14176, 26838, 25363, 9747",
      /*  6353 */ "11864, 21955, 20022, 20050, 20074, 14176, 23005, 9747, 18822, 19107, 25904, 20925, 14176, 13594",
      /*  6367 */ "9747, 25706, 20099, 23269, 21363, 9746, 20119, 15299, 14176, 9745, 22555, 26684, 18823, 13589",
      /*  6381 */ "24508, 13593, 22418, 20142, 12879, 20470, 18066, 20169, 12894, 21955, 21955, 21955, 21955, 21955",
      /*  6395 */ "21955, 21955, 21955, 21955, 21955, 17381, 21955, 10595, 26654, 21955, 14381, 25942, 20214, 20230",
      /*  6409 */ "20241, 20257, 14272, 13241, 10742, 21954, 10195, 21955, 14588, 20273, 21955, 14452, 14459, 20300",
      /*  6423 */ "19324, 17597, 21955, 20336, 16611, 18560, 21955, 12202, 15704, 15822, 21955, 20375, 14176, 20411",
      /*  6437 */ "17169, 20446, 21589, 20463, 20486, 20508, 21955, 21955, 21955, 12202, 15704, 15826, 20534, 17441",
      /*  6451 */ "14176, 23087, 20550, 16961, 9747, 25705, 9128, 20586, 12104, 20604, 22280, 22286, 20957, 14176",
      /*  6465 */ "24535, 16912, 23921, 9747, 20639, 21955, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 26838",
      /*  6479 */ "9747, 9747, 22554, 21955, 21955, 12203, 23314, 14176, 16004, 9747, 18822, 20659, 21955, 20925",
      /*  6493 */ "14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 24664, 20676",
      /*  6507 */ "24642, 13589, 24508, 13593, 17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955",
      /*  6521 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 20727, 21955, 20715, 10724",
      /*  6535 */ "20743, 20759, 20770, 20786, 16122, 21955, 20802, 20348, 20821, 21955, 20838, 20880, 20910, 23689",
      /*  6549 */ "20973, 21026, 21062, 21099, 9501, 22321, 21955, 15367, 16228, 12202, 15704, 15822, 17511, 14172",
      /*  6563 */ "25682, 14176, 17169, 25638, 9747, 9749, 12077, 25389, 21955, 21955, 21124, 25018, 15704, 15826",
      /*  6577 */ "21141, 14176, 14176, 23087, 24940, 9747, 9747, 25705, 21955, 26166, 21955, 14530, 21175, 20058",
      /*  6591 */ "21195, 14176, 21215, 15053, 9747, 25577, 22355, 18784, 21231, 21255, 12140, 24188, 21275, 21324",
      /*  6605 */ "21359, 26838, 21379, 21404, 22554, 21429, 21454, 12203, 20864, 14176, 16004, 21470, 18822, 14901",
      /*  6619 */ "21955, 21490, 21524, 21571, 21605, 16224, 20660, 22620, 21652, 24634, 21687, 21710, 25327, 18651",
      /*  6633 */ "22555, 21750, 21779, 18640, 21807, 26326, 21874, 23760, 20894, 11926, 21908, 23751, 21837, 21955",
      /*  6647 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 21943, 21955",
      /*  6661 */ "18776, 15072, 21972, 21988, 21999, 22015, 16122, 23665, 22031, 25059, 21955, 8847, 12303, 22066",
      /*  6675 */ "17606, 22094, 8861, 22110, 22146, 17597, 21955, 20805, 11076, 21955, 22162, 12202, 17104, 15822",
      /*  6689 */ "21955, 14172, 19861, 21151, 22189, 24275, 15883, 25369, 21955, 21955, 22226, 22244, 21955, 22276",
      /*  6703 */ "15704, 22302, 14175, 17131, 14176, 22337, 9747, 22923, 21474, 19834, 25249, 22371, 21955, 21955",
      /*  6717 */ "22280, 22286, 25133, 19677, 14176, 14329, 25993, 9747, 9748, 21955, 15195, 16739, 15295, 15704",
      /*  6731 */ "20892, 14176, 14176, 22395, 9747, 9747, 22554, 12234, 21955, 12203, 24599, 22434, 19771, 20198",
      /*  6745 */ "20643, 21955, 21955, 23592, 22454, 15320, 9747, 25706, 21955, 23269, 14176, 9746, 23887, 15299",
      /*  6759 */ "22474, 22491, 17863, 22510, 22549, 19474, 22571, 13593, 17835, 23760, 20894, 26335, 20689, 23751",
      /*  6773 */ "14849, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955",
      /*  6787 */ "26654, 21955, 25009, 19169, 22606, 21955, 16562, 16576, 16122, 21955, 19360, 21954, 21955, 21955",
      /*  6801 */ "15297, 22283, 21955, 22636, 13583, 25135, 9747, 17597, 11302, 20805, 21955, 22658, 21955, 14200",
      /*  6815 */ "15704, 15822, 17914, 22689, 20430, 14176, 22720, 22349, 22747, 21555, 22765, 21955, 12086, 18349",
      /*  6829 */ "21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747, 9747, 11972, 21955, 21955",
      /*  6843 */ "22786, 22805, 22833, 25454, 25133, 22869, 14176, 18078, 18751, 22893, 9748, 15005, 21955, 21955",
      /*  6857 */ "15295, 15704, 20892, 24347, 14176, 22911, 9747, 22945, 22554, 21955, 21955, 12203, 23314, 14176",
      /*  6871 */ "16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746",
      /*  6885 */ "22552, 15299, 14176, 9745, 22965, 26684, 18823, 23021, 24508, 13593, 17835, 23760, 20894, 20470",
      /*  6899 */ "20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381",
      /*  6913 */ "21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668, 19408, 16122, 21955, 10742, 21954",
      /*  6927 */ "21955, 23048, 15297, 23066, 23103, 23108, 23124, 22438, 20315, 23159, 21955, 20805, 21955, 21955",
      /*  6941 */ "21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747, 9749, 21955, 21955",
      /*  6955 */ "21955, 21955, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 25790, 9747, 9747, 9747, 25705",
      /*  6969 */ "21955, 21955, 21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955",
      /*  6983 */ "21955, 21955, 15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 21955, 12203",
      /*  6997 */ "23314, 14176, 16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269",
      /*  7011 */ "23189, 22523, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760",
      /*  7025 */ "20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  7039 */ "21955, 17381, 21955, 21955, 25048, 23209, 13410, 25942, 23226, 14417, 14428, 23242, 16122, 21955",
      /*  7053 */ "9817, 9601, 17032, 15575, 23258, 23302, 23634, 23331, 14112, 14156, 22731, 17597, 21955, 20805",
      /*  7067 */ "23361, 23397, 23422, 12202, 23438, 15822, 21955, 23457, 22586, 21663, 17169, 19890, 26113, 9749",
      /*  7081 */ "21955, 21955, 8967, 21955, 11070, 23374, 23441, 23477, 23495, 23519, 23193, 23543, 16436, 9747",
      /*  7095 */ "22533, 23559, 23050, 23577, 23627, 21955, 22280, 23650, 25133, 21159, 23705, 23729, 9747, 23783",
      /*  7109 */ "22494, 15187, 16593, 18554, 10308, 15704, 22078, 14176, 14176, 26838, 23799, 9747, 16770, 18577",
      /*  7123 */ "21955, 12203, 23314, 14176, 23820, 9747, 19332, 26956, 21955, 22673, 14176, 23527, 9747, 18878",
      /*  7137 */ "21955, 23269, 14176, 9746, 22552, 15299, 14176, 9745, 14228, 23863, 21616, 13589, 24909, 23903",
      /*  7151 */ "21000, 23760, 20894, 19970, 25780, 15785, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  7165 */ "21955, 21955, 21955, 17381, 21955, 21955, 9590, 21955, 14619, 23937, 23953, 23969, 23980, 23996",
      /*  7179 */ "16122, 24012, 10742, 21954, 19703, 21955, 21791, 20852, 22314, 23677, 24031, 24069, 24121, 17597",
      /*  7193 */ "24154, 20805, 21955, 21955, 21955, 24174, 15678, 25415, 22260, 24216, 15922, 14176, 25602, 24251",
      /*  7207 */ "24272, 9749, 24158, 21955, 24291, 21955, 21955, 12202, 15704, 14969, 14175, 14176, 14176, 23087",
      /*  7221 */ "9747, 9747, 9747, 25705, 25924, 21955, 18347, 12113, 24325, 26606, 24344, 21199, 18689, 15116, 9747",
      /*  7236 */ "19806, 24363, 24389, 21955, 21955, 15295, 15686, 25128, 22590, 24410, 26838, 9747, 24429, 22554",
      /*  7250 */ "20623, 23561, 12203, 23314, 24462, 20385, 24099, 24504, 21955, 20615, 20925, 14176, 13594, 9747",
      /*  7264 */ "25706, 20359, 24524, 25687, 9746, 24558, 24585, 14176, 9745, 22555, 24621, 24658, 13589, 25490",
      /*  7278 */ "19868, 17835, 24680, 19446, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  7292 */ "21955, 21955, 21955, 21955, 17381, 21955, 21955, 12412, 21955, 20126, 22853, 24696, 24712, 24728",
      /*  7306 */ "24742, 16122, 21955, 24758, 9077, 24792, 10201, 12240, 24843, 22817, 22812, 24871, 24887, 24925",
      /*  7320 */ "17597, 24961, 24982, 21955, 18349, 25034, 25075, 25274, 25098, 25230, 25151, 24235, 23286, 21927",
      /*  7334 */ "9747, 25175, 25191, 25210, 25246, 26896, 21955, 22228, 17421, 25265, 18001, 25290, 25315, 23611",
      /*  7348 */ "23087, 25350, 26087, 24446, 25705, 25385, 11519, 13941, 11804, 25405, 26559, 18169, 17160, 19683",
      /*  7362 */ "17672, 17470, 25431, 23742, 9434, 21955, 13986, 15295, 25448, 25470, 15408, 25506, 26506, 25523",
      /*  7376 */ "21302, 24373, 14871, 10792, 25539, 26435, 14176, 26486, 25572, 26244, 11991, 21955, 20925, 25593",
      /*  7390 */ "21537, 25618, 16043, 21955, 15397, 18404, 21763, 25654, 11648, 17753, 26237, 25703, 26684, 18823",
      /*  7404 */ "13589, 24508, 13593, 16390, 25723, 25767, 25806, 20949, 23751, 16548, 21955, 21955, 21955, 21955",
      /*  7418 */ "21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955, 19056, 25942, 25843",
      /*  7432 */ "25859, 25870, 25886, 16122, 21955, 10742, 25902, 25920, 21955, 15297, 22283, 21955, 21955, 9724",
      /*  7446 */ "25135, 21074, 17597, 25940, 20805, 21955, 22789, 13689, 23406, 15704, 15822, 21955, 25958, 14176",
      /*  7460 */ "14176, 24042, 25992, 9747, 26354, 26009, 19745, 21955, 21955, 26032, 12202, 24200, 19738, 14988",
      /*  7474 */ "16417, 21918, 23087, 15139, 26076, 26110, 25705, 10378, 21955, 21955, 21955, 26129, 22286, 15551",
      /*  7488 */ "14176, 14176, 17197, 23830, 9747, 9748, 26162, 21955, 21955, 15295, 15704, 20892, 14176, 14176",
      /*  7502 */ "26838, 9747, 9747, 16988, 21955, 18604, 12203, 11662, 14176, 21734, 9747, 18822, 21955, 21955",
      /*  7516 */ "20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746, 22552, 15299, 19501, 20180, 22555",
      /*  7530 */ "26684, 18823, 13589, 24508, 13593, 17835, 26182, 20894, 20470, 26697, 26226, 12894, 21955, 21955",
      /*  7544 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 26654, 21955, 26260",
      /*  7558 */ "26278, 14128, 26294, 26046, 26060, 16122, 21955, 10742, 21954, 21955, 21955, 15297, 22283, 21955",
      /*  7572 */ "21955, 13765, 25135, 21041, 17597, 21955, 20805, 21955, 21955, 21955, 15239, 15704, 17412, 21955",
      /*  7586 */ "26314, 14176, 14176, 17169, 26351, 9747, 9749, 21955, 21955, 21955, 26370, 21955, 12202, 16511",
      /*  7600 */ "16732, 14175, 15864, 14176, 23087, 9747, 25633, 9747, 25705, 21955, 21955, 21955, 21955, 22280",
      /*  7614 */ "22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955, 21955, 21955, 15295, 15704, 20892",
      /*  7628 */ "14176, 14176, 26838, 9747, 9747, 22554, 9205, 21955, 12203, 23314, 14176, 16004, 9747, 18822, 21955",
      /*  7643 */ "21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746, 23767, 15299, 14176, 9745",
      /*  7657 */ "22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470, 20949, 23751, 12894, 21955",
      /*  7671 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381, 21955, 21955, 26936, 21955",
      /*  7685 */ "25224, 10623, 26391, 17542, 17553, 26407, 16122, 21955, 10742, 21954, 21955, 21955, 15297, 22283",
      /*  7699 */ "21955, 21955, 13583, 20083, 22749, 17597, 21955, 20805, 21955, 21955, 21955, 12202, 15704, 15822",
      /*  7713 */ "21955, 14172, 14176, 14176, 17169, 9747, 9747, 9749, 21955, 21955, 21955, 21955, 21955, 12202",
      /*  7727 */ "15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747, 9747, 25705, 21955, 21955, 21239, 21955",
      /*  7741 */ "22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955, 12037, 21955, 15295, 15704",
      /*  7755 */ "20892, 16400, 24605, 26838, 21046, 18422, 22554, 21955, 21955, 12203, 23314, 14176, 16004, 9747",
      /*  7769 */ "18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746, 22552, 15299",
      /*  7783 */ "14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470, 20949, 23751",
      /*  7797 */ "12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 14345, 21955, 21955",
      /*  7811 */ "26654, 21955, 21955, 25942, 14128, 21955, 26196, 26210, 16122, 21955, 10742, 22046, 21955, 21955",
      /*  7825 */ "15297, 26423, 26451, 26456, 26472, 25135, 22125, 17597, 21955, 20805, 14485, 14556, 21955, 12202",
      /*  7839 */ "15704, 15822, 21955, 14172, 14176, 14177, 17169, 9747, 9747, 24053, 26502, 21955, 21955, 21955",
      /*  7853 */ "21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747, 9747, 25705, 21955, 21955",
      /*  7867 */ "21955, 21955, 22280, 22286, 25133, 14176, 14176, 21289, 9747, 9747, 9748, 12122, 21955, 21955",
      /*  7881 */ "15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 21955, 12203, 23314, 14176",
      /*  7895 */ "16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746",
      /*  7909 */ "22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470",
      /*  7923 */ "20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 17381",
      /*  7937 */ "21955, 21955, 26654, 21955, 21955, 25942, 14128, 21955, 25668, 19408, 16122, 8961, 10742, 21954",
      /*  7951 */ "11230, 21955, 15297, 22283, 21955, 21955, 9230, 25976, 19907, 17597, 21955, 20805, 21955, 21955",
      /*  7965 */ "21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 26522, 9747, 9747, 24138, 18841, 21955",
      /*  7979 */ "21955, 21955, 21955, 26549, 25556, 15826, 14175, 14176, 26575, 23087, 9747, 19006, 9747, 25705",
      /*  7993 */ "9509, 21955, 21955, 21955, 26594, 22286, 25133, 26630, 14176, 14329, 24256, 9747, 9748, 21955",
      /*  8007 */ "21955, 21955, 15295, 15704, 20892, 14176, 22475, 26838, 9747, 9747, 26649, 21955, 21955, 12203",
      /*  8021 */ "23314, 14176, 16004, 9747, 15160, 21955, 13352, 20925, 14176, 13594, 9747, 25706, 21955, 23269",
      /*  8035 */ "14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760",
      /*  8049 */ "20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  8063 */ "21955, 17381, 21955, 21955, 26654, 21955, 21955, 25942, 26670, 21955, 25668, 19408, 16122, 21955",
      /*  8077 */ "10742, 21954, 21955, 21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597, 21955, 20805",
      /*  8091 */ "21955, 21955, 21955, 12202, 15704, 15822, 21955, 14172, 14176, 14176, 17169, 9747, 9747, 9749",
      /*  8105 */ "21955, 21955, 21955, 21955, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 23087, 9747, 9747",
      /*  8119 */ "9747, 25705, 21955, 21955, 21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748",
      /*  8134 */ "21955, 21955, 21955, 15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 21955",
      /*  8148 */ "12203, 23314, 14176, 16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955",
      /*  8162 */ "23269, 14176, 9746, 22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835",
      /*  8176 */ "23760, 20894, 20470, 20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  8190 */ "21955, 21955, 10331, 21955, 21955, 11750, 26739, 26727, 26746, 26713, 26146, 26762, 19928, 10442",
      /*  8204 */ "21955, 10742, 21954, 20492, 26778, 20513, 9265, 21955, 21955, 22379, 9000, 26797, 10772, 8983, 9016",
      /*  8219 */ "21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 11573, 10271, 9178, 11566, 21955",
      /*  8234 */ "9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385",
      /*  8250 */ "9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664",
      /*  8266 */ "9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901",
      /*  8282 */ "9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130",
      /*  8297 */ "10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955",
      /*  8311 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 21955, 21955, 26654, 21955",
      /*  8325 */ "18612, 25942, 26814, 26854, 26862, 14386, 10442, 21955, 10742, 21954, 20492, 21955, 20513, 9265",
      /*  8339 */ "14256, 21955, 22379, 9864, 26798, 10772, 8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114",
      /*  8354 */ "8996, 10278, 9185, 9151, 10271, 9178, 11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281",
      /*  8369 */ "9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525",
      /*  8385 */ "10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765",
      /*  8401 */ "9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011",
      /*  8417 */ "10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798",
      /*  8431 */ "10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  8445 */ "21955, 21955, 21955, 10331, 21955, 21955, 26654, 21955, 21955, 11882, 26886, 26920, 26928, 11842",
      /*  8459 */ "10442, 21955, 10742, 21954, 20492, 26952, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 10772",
      /*  8473 */ "8983, 9016, 21955, 13727, 21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 26972, 10271, 9178",
      /*  8488 */ "11566, 21955, 9201, 21955, 14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337",
      /*  8503 */ "9838, 9367, 9385, 9420, 9082, 23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972",
      /*  8519 */ "9578, 9617, 9664, 9694, 9632, 9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885",
      /*  8535 */ "9854, 9880, 9901, 9917, 9964, 9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114",
      /*  8550 */ "14050, 10132, 10130, 10148, 10164, 10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504",
      /*  8564 */ "13514, 10294, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 10331, 21955",
      /*  8578 */ "21955, 26654, 21955, 21955, 25942, 10788, 21955, 21955, 20822, 16122, 21955, 10742, 21954, 21955",
      /*  8592 */ "21955, 15297, 22283, 21955, 21955, 13583, 25135, 9747, 17597, 21955, 20805, 21955, 21955, 21955",
      /*  8606 */ "12202, 15704, 15822, 21955, 23315, 14176, 14176, 25299, 9747, 9747, 9749, 21955, 21955, 21955",
      /*  8620 */ "21955, 21955, 12202, 15704, 15826, 14175, 14176, 14176, 9239, 9747, 9747, 9747, 25705, 21955, 21955",
      /*  8635 */ "21955, 21955, 22280, 22286, 25133, 14176, 14176, 14329, 9747, 9747, 9748, 21955, 21955, 21955",
      /*  8649 */ "15295, 15704, 20892, 14176, 14176, 26838, 9747, 9747, 22554, 21955, 21955, 12203, 23314, 14176",
      /*  8663 */ "16004, 9747, 18822, 21955, 21955, 20925, 14176, 13594, 9747, 25706, 21955, 23269, 14176, 9746",
      /*  8677 */ "22552, 15299, 14176, 9745, 22555, 26684, 18823, 13589, 24508, 13593, 17835, 23760, 20894, 20470",
      /*  8691 */ "20949, 23751, 12894, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955, 21955",
      /*  8705 */ "21955, 21955, 21955, 21955, 12160, 21955, 12164, 26999, 27007, 22173, 20491, 21955, 21955, 21955",
      /*  8719 */ "20492, 21955, 20513, 9265, 21955, 21955, 22379, 9864, 26798, 13478, 8983, 27031, 21955, 13727",
      /*  8733 */ "21955, 20518, 9032, 9098, 9114, 8996, 10278, 9185, 11573, 10271, 9178, 11566, 21955, 9201, 21955",
      /*  8748 */ "14863, 9221, 24569, 9255, 9281, 9311, 9332, 26798, 9353, 9316, 9337, 9838, 9367, 9385, 9420, 9082",
      /*  8764 */ "23210, 9457, 9488, 9557, 9525, 10978, 9546, 9562, 9530, 10983, 13972, 9578, 9617, 9664, 9694, 9632",
      /*  8780 */ "9648, 9770, 22376, 9643, 9765, 9709, 9786, 9807, 23345, 9833, 9885, 9854, 9880, 9901, 9917, 9964",
      /*  8796 */ "9980, 9941, 9932, 9948, 10011, 10027, 10061, 10098, 10092, 10114, 14050, 10132, 10130, 10148, 10164",
      /*  8811 */ "10180, 9861, 9162, 26798, 10231, 26983, 10217, 10260, 13504, 13514, 10294, 21955, 21955, 21955",
      /*  8825 */ "21955, 21955, 21955, 21955, 21955, 21955, 21955, 0, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 35002, 35002",
      /*  8845 */ "35002, 35002, 0, 0, 0, 0, 0, 402, 463, 464, 465, 0, 0, 0, 0, 0, 471, 0, 0, 0, 0, 534, 0, 0, 534",
      /*  8870 */ "346, 346, 554, 557, 346, 564, 346, 37051, 37051, 35002, 35002, 37051, 37051, 37051, 37051, 37051",
      /*  8886 */ "37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 22528, 24576, 37051, 37051, 37051",
      /*  8900 */ "37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 223, 37051, 37051, 37051, 37051, 37051",
      /*  8914 */ "37051, 37051, 37051, 0, 0, 35002, 37051, 35002, 37051, 37051, 37051, 37051, 37051, 37051, 20480",
      /*  8929 */ "37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051, 37051",
      /*  8943 */ "37051, 37051, 0, 0, 2, 45059, 4, 5, 0, 0, 0, 0, 35002, 37051, 0, 528384, 190, 191, 0, 0, 0, 0, 0",
      /*  8966 */ "403, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 946, 0, 0, 0, 0, 0, 0, 688128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /*  8996 */ "0, 780288, 0, 0, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /*  9010 */ "557056, 557056, 557056, 557656, 0, 557056, 0, 0, 825344, 0, 0, 0, 0, 0, 0, 0, 0, 223, 223, 0, 0",
      /*  9031 */ "679936, 747520, 555008, 555008, 763904, 555008, 772096, 555008, 555008, 790528, 796672, 802816",
      /*  9043 */ "555008, 815104, 555008, 831488, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008",
      /*  9055 */ "555008, 555008, 555008, 555008, 555008, 0, 94208, 0, 0, 0, 0, 0, 419, 0, 0, 0, 0, 0, 0, 0, 0, 223",
      /*  9077 */ "223, 0, 0, 0, 432, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 897024, 0, 0, 0, 555008, 555008, 888832",
      /*  9101 */ "555008, 555008, 555008, 0, 0, 0, 747520, 0, 763904, 772096, 0, 0, 790528, 796672, 802816, 0, 815104",
      /*  9118 */ "831488, 888832, 0, 0, 0, 0, 831488, 0, 796672, 831488, 0, 0, 0, 0, 0, 0, 1131, 0, 0, 0, 0, 0, 0, 0",
      /*  9142 */ "0, 0, 0, 424, 0, 0, 0, 223, 223, 888832, 557056, 557056, 557056, 557056, 557056, 557056, 600, 0, 0",
      /*  9161 */ "603, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 0, 704512, 0, 0, 0, 0, 0, 0, 557056",
      /*  9178 */ "776192, 557056, 557056, 790528, 557056, 796672, 802816, 557056, 557056, 815104, 557056, 557056",
      /*  9190 */ "831488, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 0, 0, 0",
      /*  9204 */ "745472, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1455, 0, 0, 0, 759808, 0, 800768, 0, 0, 0, 0, 659456",
      /*  9230 */ "0, 0, 0, 0, 0, 0, 0, 0, 458, 346, 346, 346, 346, 346, 346, 346, 0, 0, 43850, 0, 0, 368, 368, 368",
      /*  9254 */ "368, 555008, 745472, 555008, 759808, 555008, 555008, 555008, 555008, 800768, 806912, 555008, 555008",
      /*  9267 */ "555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 0, 0, 0",
      /*  9281 */ "555008, 555008, 673792, 0, 729088, 0, 0, 0, 0, 806912, 0, 0, 806912, 0, 0, 0, 0, 0, 0, 668, 0, 0, 0",
      /*  9304 */ "0, 0, 0, 0, 0, 0, 0, 557056, 557056, 673792, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /*  9321 */ "557056, 557056, 557056, 729088, 557056, 557056, 557056, 557056, 745472, 557056, 557056, 557056",
      /*  9333 */ "557056, 745472, 557056, 557056, 557056, 759808, 557056, 557056, 557056, 557056, 557056, 557056",
      /*  9345 */ "557056, 800768, 806912, 557056, 557056, 557056, 557056, 557056, 892928, 557056, 557056, 557056",
      /*  9357 */ "557056, 557056, 557056, 0, 0, 0, 0, 0, 557056, 673792, 557056, 557056, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /*  9379 */ "0, 0, 0, 0, 195, 0, 690176, 0, 0, 0, 0, 0, 0, 0, 0, 0, 792576, 0, 0, 0, 0, 0, 0, 0, 964, 0, 0, 0, 0",
      /*  9408 */ "0, 0, 0, 0, 0, 346, 346, 346, 556, 346, 346, 346, 0, 0, 886784, 911360, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /*  9433 */ "817152, 0, 0, 0, 0, 0, 0, 1304, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 699, 0, 0, 0, 0, 0, 827392, 0, 0",
      /*  9460 */ "555008, 690176, 692224, 555008, 555008, 555008, 555008, 737280, 555008, 555008, 555008, 774144",
      /*  9472 */ "555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008",
      /*  9484 */ "555008, 55296, 0, 116736, 555008, 827392, 555008, 555008, 555008, 555008, 555008, 555008, 555008",
      /*  9497 */ "555008, 692224, 0, 774144, 0, 0, 0, 0, 0, 0, 669, 670, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1134, 0, 0, 0, 0",
      /*  9523 */ "0, 0, 737280, 557056, 557056, 557056, 557056, 557056, 557056, 774144, 557056, 557056, 557056",
      /*  9536 */ "792576, 557056, 557056, 823296, 827392, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /*  9548 */ "557056, 557056, 0, 0, 0, 0, 536576, 0, 0, 0, 0, 557056, 557056, 557056, 690176, 692224, 557056",
      /*  9565 */ "557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 737280, 557056, 557056, 557056",
      /*  9577 */ "557056, 0, 833536, 0, 0, 0, 0, 0, 0, 0, 0, 0, 798720, 0, 0, 0, 0, 0, 0, 219, 0, 0, 0, 0, 223, 0, 0",
      /*  9604 */ "0, 0, 0, 0, 0, 436, 0, 0, 0, 0, 0, 442, 443, 0, 0, 880640, 0, 0, 0, 0, 0, 0, 0, 0, 0, 710656",
      /*  9630 */ "768000, 0, 913408, 0, 0, 0, 0, 890880, 901120, 557056, 557056, 557056, 557056, 698368, 557056",
      /*  9645 */ "557056, 710656, 557056, 557056, 557056, 557056, 557056, 739328, 749568, 557056, 557056, 557056",
      /*  9657 */ "768000, 557056, 557056, 557056, 557056, 557056, 833536, 0, 0, 727040, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /*  9677 */ "876544, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008",
      /*  9689 */ "555008, 555008, 0, 0, 114688, 555008, 710656, 555008, 555008, 739328, 555008, 768000, 555008",
      /*  9702 */ "555008, 833536, 555008, 555008, 555008, 876544, 890880, 901120, 557056, 557056, 913408, 557056, 0",
      /*  9715 */ "0, 0, 0, 0, 0, 0, 0, 0, 808960, 0, 0, 0, 0, 0, 447, 0, 0, 0, 346, 346, 346, 346, 346, 565, 346, 346",
      /*  9741 */ "346, 346, 1597, 346, 346, 346, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /*  9761 */ "368, 368, 0, 0, 557056, 557056, 557056, 557056, 833536, 557056, 557056, 557056, 557056, 557056",
      /*  9775 */ "557056, 557056, 876544, 557056, 890880, 557056, 901120, 557056, 557056, 913408, 557056, 829440, 0",
      /*  9788 */ "858112, 862208, 915456, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1563, 0, 0, 0, 0, 0, 0, 778240, 0, 0, 0",
      /*  9814 */ "0, 878592, 874496, 0, 0, 0, 0, 0, 0, 0, 421, 422, 0, 0, 0, 0, 0, 223, 223, 874496, 0, 751616, 0, 0",
      /*  9838 */ "557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 892928",
      /*  9850 */ "557056, 557056, 557056, 557056, 874496, 882688, 557056, 557056, 557056, 917504, 0, 0, 0, 0, 557056",
      /*  9865 */ "557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /*  9877 */ "557056, 0, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 751616, 557056, 557056, 778240",
      /*  9890 */ "557056, 557056, 808960, 557056, 557056, 839680, 557056, 557056, 858112, 557056, 557056, 557056",
      /*  9902 */ "557056, 858112, 557056, 557056, 874496, 882688, 557056, 557056, 557056, 917504, 0, 0, 0, 0, 706560",
      /*  9917 */ "0, 0, 718848, 735232, 0, 0, 0, 0, 0, 909312, 0, 784384, 0, 0, 0, 835584, 557056, 557056, 557056",
      /*  9936 */ "557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 702464, 716800, 718848",
      /*  9948 */ "557056, 557056, 735232, 557056, 557056, 557056, 557056, 557056, 557056, 835584, 557056, 557056",
      /*  9960 */ "557056, 557056, 557056, 557056, 0, 870400, 0, 716800, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 845824, 0, 0",
      /*  9982 */ "0, 0, 555008, 555008, 555008, 718848, 555008, 555008, 555008, 555008, 0, 0, 0, 557056, 557056",
      /*  9997 */ "557056, 557056, 0, 0, 0, 0, 5, 0, 0, 0, 656, 660, 0, 675840, 557056, 684032, 0, 0, 0, 0, 0, 0",
      /* 10019 */ "837632, 761856, 753664, 743424, 765952, 0, 0, 851968, 0, 894976, 907264, 0, 667648, 854016, 0, 0, 0",
      /* 10036 */ "782336, 0, 0, 0, 0, 0, 0, 0, 451, 0, 0, 0, 0, 0, 0, 0, 0, 0, 484, 0, 0, 0, 0, 0, 0, 0, 555008",
      /* 10063 */ "694272, 555008, 555008, 786432, 555008, 555008, 694272, 786432, 0, 669696, 557056, 557056, 694272",
      /* 10076 */ "557056, 557056, 557056, 557056, 0, 0, 0, 0, 5, 0, 0, 190, 0, 0, 0, 675840, 905216, 669696, 557056",
      /* 10095 */ "557056, 694272, 557056, 557056, 557056, 731136, 557056, 557056, 761856, 786432, 557056, 557056",
      /* 10107 */ "557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /* 10119 */ "557056, 905216, 0, 700416, 0, 0, 0, 811008, 0, 0, 903168, 557056, 903168, 557056, 677888, 681984",
      /* 10135 */ "700416, 557056, 557056, 557056, 557056, 755712, 788480, 811008, 847872, 557056, 860160, 557056",
      /* 10147 */ "557056, 557056, 557056, 557056, 903168, 0, 0, 0, 0, 819200, 0, 0, 0, 0, 804864, 0, 919552, 724992",
      /* 10165 */ "724992, 557056, 720896, 724992, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /* 10177 */ "899072, 557056, 720896, 724992, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /* 10189 */ "899072, 0, 0, 0, 0, 821248, 0, 0, 0, 0, 0, 449, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 468, 0, 0, 0, 0",
      /* 10217 */ "712704, 866304, 0, 0, 0, 557056, 557056, 741376, 557056, 813056, 557056, 557056, 557056, 866304",
      /* 10231 */ "557056, 557056, 557056, 0, 0, 0, 0, 0, 0, 0, 557056, 557056, 733184, 557056, 557056, 557056, 557056",
      /* 10248 */ "0, 0, 0, 0, 0, 86016, 141312, 0, 0, 0, 0, 675840, 741376, 557056, 813056, 557056, 557056, 557056",
      /* 10266 */ "866304, 708608, 0, 0, 0, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 747520, 557056",
      /* 10280 */ "557056, 557056, 557056, 763904, 557056, 772096, 557056, 776192, 557056, 557056, 790528, 557056",
      /* 10292 */ "796672, 802816, 557056, 770048, 722944, 557056, 722944, 557056, 557056, 557056, 557056, 557056",
      /* 10304 */ "557056, 557056, 856064, 856064, 0, 0, 0, 0, 0, 0, 1344, 0, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285",
      /* 10326 */ "285, 985, 285, 285, 285, 0, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1638, 0, 0, 0, 0, 0",
      /* 10354 */ "0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53550, 0, 0, 0, 0, 0, 454, 711, 0, 0, 0, 715, 0, 0, 0, 0, 0",
      /* 10383 */ "0, 0, 1132, 0, 0, 0, 0, 0, 0, 0, 0, 0, 129024, 0, 129024, 0, 0, 0, 0, 53550, 53550, 53550, 53550",
      /* 10406 */ "302, 302, 302, 302, 302, 302, 53550, 302, 53550, 53550, 53550, 302, 53550, 53550, 53550, 53550",
      /* 10422 */ "53550, 53550, 53550, 53550, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1465, 0, 0, 0, 0, 0, 0, 2, 45059, 4, 5, 0",
      /* 10448 */ "0, 0, 0, 0, 0, 0, 528384, 190, 191, 0, 0, 0, 0, 0, 524, 0, 529, 0, 532, 0, 0, 0, 0, 0, 532, 557056",
      /* 10474 */ "557056, 557056, 557056, 0, 655, 655, 0, 5, 0, 0, 0, 0, 0, 0, 675840, 0, 2, 45059, 4, 5, 0, 0, 0, 0",
      /* 10498 */ "0, 0, 0, 0, 0, 0, 57344, 0, 190, 57344, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 563628, 563628, 0",
      /* 10525 */ "0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 190, 0, 0, 0, 0, 0, 0, 57601, 24576, 0, 2, 45059, 4, 5, 0, 0",
      /* 10552 */ "0, 0, 0, 0, 0, 528384, 10636, 191, 0, 0, 0, 0, 0, 694, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 455, 0, 0",
      /* 10580 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 223, 191, 0, 0, 0, 16384, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 10610 */ "212, 0, 565426, 45059, 4, 5, 182, 0, 0, 0, 0, 0, 182, 0, 0, 0, 0, 0, 0, 221, 0, 0, 0, 221, 0, 221",
      /* 10636 */ "0, 22528, 24576, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 0, 59392, 59392, 59392, 59392, 0, 0, 0, 0",
      /* 10660 */ "0, 0, 0, 0, 59392, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 53550, 53550, 53550, 0, 59392, 0, 59392",
      /* 10684 */ "59392, 59392, 59392, 59392, 59392, 0, 0, 0, 0, 0, 0, 0, 59392, 0, 565426, 45059, 4, 5, 0, 0, 0, 0",
      /* 10706 */ "0, 0, 0, 528384, 190, 191, 0, 0, 0, 0, 0, 711, 711, 0, 0, 0, 715, 958, 0, 0, 0, 0, 0, 0, 237, 0, 0",
      /* 10733 */ "0, 237, 0, 237, 0, 22528, 24576, 0, 459, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 223, 223, 892928",
      /* 10759 */ "557056, 557056, 557056, 557056, 557056, 557056, 1061, 0, 0, 1066, 0, 557056, 673792, 557056, 557056",
      /* 10774 */ "557056, 557056, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 675840, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 10801 */ "0, 0, 0, 0, 1468, 0, 0, 0, 0, 63488, 0, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 63488, 0",
      /* 10821 */ "0, 63488, 0, 0, 0, 63488, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1135, 0, 0, 0, 0, 65536, 0, 0, 0, 0, 0",
      /* 10849 */ "0, 0, 65536, 0, 0, 0, 0, 65536, 22528, 24576, 0, 0, 65536, 65536, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 10874 */ "0, 0, 1551, 1552, 0, 65536, 0, 65536, 65536, 65536, 65536, 65536, 65536, 65536, 65536, 65536, 65536",
      /* 10891 */ "0, 0, 65536, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 231, 0, 0, 0, 0, 258, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 10922 */ "0, 0, 0, 0, 0, 1639, 0, 0, 0, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 0, 528384, 397, 398, 0, 0, 0, 0, 0",
      /* 10950 */ "727, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 223, 0, 0, 0, 75776, 888832, 557056, 557056, 557056, 557056",
      /* 10972 */ "557056, 557056, 840, 0, 0, 843, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 868352",
      /* 10986 */ "872448, 557056, 557056, 557056, 884736, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 0",
      /* 10999 */ "0, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 67584, 0, 191, 67584, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 11028 */ "0, 0, 0, 193, 0, 0, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 191, 0, 0, 0, 0, 0, 0, 22528, 67845, 0",
      /* 11056 */ "2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 0, 528384, 190, 10639, 0, 0, 0, 0, 0, 963, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 11084 */ "0, 0, 0, 700, 0, 0, 0, 0, 0, 0, 0, 0, 18432, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 59392, 59392, 0, 0",
      /* 11113 */ "188, 188, 0, 0, 188, 188, 69820, 188, 188, 188, 188, 188, 188, 188, 188, 188, 188, 188, 224, 188",
      /* 11133 */ "69820, 188, 188, 188, 188, 227, 188, 188, 188, 188, 188, 188, 188, 188, 188, 188, 188, 188, 188",
      /* 11152 */ "188, 188, 188, 69820, 188, 188, 188, 188, 20480, 188, 188, 188, 188, 188, 188, 188, 188, 188, 188",
      /* 11171 */ "69820, 69820, 69820, 69820, 188, 188, 188, 188, 188, 188, 188, 188, 188, 69820, 188, 69820, 69820",
      /* 11188 */ "69820, 69820, 69859, 69859, 69859, 69859, 69859, 69859, 69820, 69820, 69820, 0, 69820, 69820, 69820",
      /* 11203 */ "69820, 69820, 69820, 69820, 69820, 0, 0, 0, 188, 0, 188, 188, 188, 188, 188, 188, 188, 188, 69820",
      /* 11222 */ "188, 188, 188, 188, 188, 22528, 24576, 563628, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 458, 0",
      /* 11247 */ "0, 825344, 0, 0, 0, 0, 0, 0, 0, 0, 563628, 563628, 0, 0, 679936, 0, 0, 0, 75776, 75776, 0, 0, 0, 0",
      /* 11271 */ "0, 0, 0, 0, 0, 0, 0, 22528, 24576, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 75776, 0, 0, 75776, 0, 0, 0, 0, 0",
      /* 11298 */ "0, 695, 0, 697, 0, 0, 0, 0, 0, 0, 0, 0, 671, 0, 0, 0, 675, 0, 0, 0, 0, 0, 0, 0, 75776, 75776, 75776",
      /* 11325 */ "75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 75776, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1636",
      /* 11345 */ "0, 0, 0, 1640, 0, 0, 75776, 0, 75776, 0, 0, 0, 0, 75776, 0, 0, 75776, 75776, 75776, 75776, 0, 0, 0",
      /* 11368 */ "0, 0, 978, 0, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 1202, 0, 0, 1204, 0, 0, 0, 0, 45059",
      /* 11391 */ "4, 5, 61440, 0, 0, 0, 0, 0, 0, 528384, 190, 191, 0, 0, 0, 0, 0, 1130, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 11419 */ "0, 223, 0, 59392, 59392, 0, 98304, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 919, 0, 0, 825344",
      /* 11445 */ "0, 0, 0, 0, 0, 0, 0, 0, 223, 0, 0, 0, 679936, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 77824",
      /* 11473 */ "0, 0, 0, 0, 0, 0, 0, 77824, 77824, 77824, 77824, 77824, 77824, 77824, 77824, 77824, 77824, 77824",
      /* 11491 */ "77824, 77824, 77824, 77824, 77824, 0, 0, 0, 0, 0, 43375, 0, 0, 0, 77824, 0, 0, 43375, 43375, 43375",
      /* 11511 */ "43375, 43375, 43375, 43375, 43375, 77824, 77824, 43375, 0, 0, 0, 0, 0, 1143, 0, 0, 0, 1147, 1148, 0",
      /* 11531 */ "0, 0, 0, 0, 0, 194, 194, 194, 0, 0, 0, 0, 0, 0, 0, 0, 1697, 0, 285, 285, 1698, 285, 285, 0, 888832",
      /* 11556 */ "557056, 557056, 557056, 557056, 557056, 557056, 600, 0, 43008, 603, 557056, 557056, 557056, 557056",
      /* 11570 */ "557056, 557056, 557056, 888832, 557056, 557056, 557056, 557056, 557056, 557056, 0, 0, 0, 0, 557056",
      /* 11585 */ "557056, 557056, 557056, 557056, 892928, 557056, 557056, 557056, 557056, 557056, 557056, 0, 0, 0, 0",
      /* 11600 */ "43008, 557056, 673792, 557056, 557056, 557056, 557056, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 191, 675840, 0",
      /* 11620 */ "0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 79872, 0, 0, 0, 0, 0, 1158, 0, 1160, 0, 0, 0, 0, 0, 958",
      /* 11648 */ "0, 0, 0, 0, 0, 0, 1696, 0, 0, 0, 285, 285, 285, 285, 285, 0, 0, 0, 0, 346, 346, 1490, 346, 346, 346",
      /* 11673 */ "346, 346, 346, 346, 346, 600, 43850, 785, 603, 845, 847, 368, 368, 368, 79872, 79872, 79872, 79872",
      /* 11691 */ "79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 79872, 0, 0, 0, 20480",
      /* 11707 */ "81920, 81920, 81920, 0, 81920, 81920, 0, 0, 0, 0, 81920, 0, 81920, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 11731 */ "0, 22528, 24576, 81920, 81920, 81920, 81920, 81920, 81920, 81920, 81920, 81920, 81920, 81920, 81920",
      /* 11746 */ "81920, 81920, 81920, 81920, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 397312, 223, 0, 0, 397312, 0, 0, 2, 179",
      /* 11769 */ "4, 5, 0, 183, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1309, 0, 0, 0, 0, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0",
      /* 11799 */ "0, 0, 0, 0, 83968, 0, 0, 0, 0, 0, 1171, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 733, 0, 0, 0, 0, 83968",
      /* 11827 */ "83968, 83968, 83968, 83968, 83968, 83968, 83968, 83968, 83968, 83968, 83968, 83968, 83968, 83968",
      /* 11841 */ "83968, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 405504, 0, 405504, 405504, 405504, 0, 368, 368, 368, 368, 1534",
      /* 11863 */ "368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 0, 0, 1442, 0, 0, 0, 0, 0, 1543, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 11890 */ "0, 0, 0, 0, 0, 0, 260, 260, 346, 346, 1655, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 11912 */ "346, 346, 346, 346, 1709, 346, 1667, 368, 368, 368, 368, 368, 368, 1673, 368, 368, 368, 368, 368",
      /* 11931 */ "368, 368, 0, 0, 0, 0, 346, 346, 1853, 1854, 346, 0, 285, 346, 1733, 346, 346, 346, 346, 346, 346",
      /* 11952 */ "1739, 346, 346, 346, 368, 1743, 0, 0, 0, 1760, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1770",
      /* 11972 */ "368, 368, 0, 0, 903, 0, 0, 0, 0, 0, 909, 0, 0, 0, 0, 0, 0, 0, 75776, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 12001 */ "1549, 0, 0, 0, 0, 0, 0, 0, 0, 0, 88453, 88453, 88453, 88453, 88453, 88453, 88453, 88453, 0, 0",
      /* 12021 */ "88453, 26800, 2, 0, 4, 5, 0, 394, 0, 0, 0, 0, 0, 528384, 190, 191, 0, 0, 0, 0, 0, 1317, 0, 0, 0, 0",
      /* 12047 */ "0, 0, 0, 0, 0, 0, 0, 915, 0, 0, 0, 0, 368, 368, 368, 368, 90112, 0, 0, 26800, 5, 0, 0, 0, 0, 0, 0",
      /* 12074 */ "0, 0, 730, 0, 0, 0, 0, 0, 0, 0, 0, 912, 0, 0, 0, 0, 0, 0, 0, 0, 943, 0, 0, 0, 0, 0, 0, 0, 0, 1146",
      /* 12104 */ "0, 0, 0, 0, 0, 0, 0, 0, 1161, 0, 0, 0, 0, 0, 0, 0, 0, 1174, 0, 0, 0, 0, 0, 0, 0, 0, 1306, 0, 0, 0",
      /* 12134 */ "0, 0, 0, 0, 0, 1334, 0, 0, 0, 0, 0, 0, 0, 0, 1346, 0, 0, 1349, 0, 0, 285, 285, 0, 0, 0, 192, 0, 0",
      /* 12162 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51200, 0, 0, 0, 0, 51200, 0, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 0",
      /* 12191 */ "0, 0, 0, 92463, 0, 0, 0, 0, 0, 1343, 0, 0, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285, 285, 285, 285, 285",
      /* 12217 */ "285, 285, 92463, 92463, 92463, 92463, 0, 0, 0, 0, 0, 0, 92463, 0, 92463, 92463, 92463, 0, 0, 0, 0",
      /* 12238 */ "0, 1447, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 487, 285, 285, 285, 285, 189, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 12266 */ "0, 0, 0, 0, 0, 0, 950, 0, 0, 0, 905, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 112640, 0, 0, 368, 368",
      /* 12295 */ "899, 1120, 0, 0, 0, 0, 905, 1122, 0, 0, 0, 0, 0, 0, 0, 471, 0, 0, 0, 0, 285, 285, 491, 285, 1249",
      /* 12320 */ "1399, 0, 0, 0, 0, 1255, 1401, 0, 0, 0, 0, 368, 368, 368, 368, 0, 0, 0, 176, 5, 0, 0, 0, 658, 662, 0",
      /* 12346 */ "0, 0, 0, 0, 20480, 0, 0, 0, 0, 194, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22528, 24576, 0, 193, 0, 193, 0",
      /* 12373 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 118784, 0, 0, 0, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 73728",
      /* 12400 */ "528384, 190, 191, 0, 0, 0, 0, 0, 1545, 0, 0, 1548, 0, 0, 0, 0, 0, 0, 0, 220, 0, 0, 0, 223, 0, 0, 0",
      /* 12427 */ "0, 100352, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 972, 563629, 0, 73728, 0, 0, 0, 0, 0, 0, 0",
      /* 12455 */ "0, 0, 0, 0, 0, 0, 210, 211, 0, 0, 0, 825344, 0, 0, 0, 0, 0, 0, 0, 0, 0, 563629, 0, 0, 679936, 0",
      /* 12481 */ "780288, 0, 0, 557604, 557604, 557604, 557604, 557604, 557604, 557604, 557604, 557604, 557604",
      /* 12494 */ "557604, 557604, 557604, 557604, 557604, 557604, 601, 557658, 748068, 557604, 557604, 557604, 557604",
      /* 12507 */ "764452, 557604, 772644, 557604, 776740, 557604, 557604, 791076, 557604, 797220, 803364, 889380",
      /* 12519 */ "557604, 557604, 557604, 557604, 557604, 557604, 600, 0, 0, 603, 557658, 557658, 557658, 557658",
      /* 12533 */ "557658, 557658, 557658, 748122, 557658, 557658, 557658, 557658, 764506, 557658, 772698, 557658",
      /* 12545 */ "776794, 557658, 557658, 791130, 557658, 797274, 803418, 557658, 557658, 815706, 557658, 557658",
      /* 12557 */ "832090, 557658, 557658, 557658, 0, 0, 0, 0, 0, 0, 0, 557604, 557604, 733732, 557604, 557604, 557604",
      /* 12574 */ "557056, 557604, 674340, 557604, 557604, 557604, 557604, 557604, 557604, 557604, 557604, 557604",
      /* 12586 */ "557604, 729636, 557604, 557604, 731684, 557604, 557604, 762404, 786980, 557604, 557604, 557604",
      /* 12598 */ "557604, 557604, 557604, 557604, 557604, 557604, 703012, 717348, 719396, 557604, 557604, 735780",
      /* 12610 */ "557604, 557604, 557604, 557604, 557604, 557604, 557658, 557658, 733786, 557658, 557658, 557658",
      /* 12622 */ "557658, 557658, 557658, 557658, 686080, 0, 893476, 557604, 557604, 557604, 557604, 557604, 557604",
      /* 12635 */ "0, 0, 0, 0, 0, 557658, 674394, 557658, 557658, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 285, 1352",
      /* 12660 */ "557658, 760410, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 801370, 807514, 557658",
      /* 12672 */ "557658, 557658, 557658, 557658, 557658, 557658, 889434, 557658, 557658, 557658, 557658, 557658",
      /* 12684 */ "557658, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 675840, 737828, 557604, 557604, 557604, 557604, 557604",
      /* 12703 */ "557604, 774692, 557604, 557604, 557604, 793124, 557604, 557604, 823844, 827940, 690778, 692826",
      /* 12715 */ "557658, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 737882, 557658, 557658",
      /* 12727 */ "557658, 557658, 557658, 557658, 905818, 0, 700416, 0, 0, 0, 811008, 0, 0, 903168, 557658, 557658",
      /* 12743 */ "774746, 557658, 557658, 557658, 793178, 557658, 557658, 823898, 827994, 557658, 557658, 557658",
      /* 12755 */ "557658, 557658, 557658, 752218, 557658, 557658, 778842, 557658, 557658, 809562, 557658, 557658",
      /* 12767 */ "840282, 557658, 557658, 868954, 873050, 557658, 557658, 557658, 885338, 557658, 557658, 557658",
      /* 12779 */ "557658, 557658, 557658, 557658, 0, 704512, 0, 0, 0, 0, 0, 0, 557604, 913408, 0, 0, 0, 0, 890880",
      /* 12798 */ "901120, 557604, 557604, 557604, 557604, 698916, 557604, 557604, 711204, 557604, 557658, 557658",
      /* 12810 */ "557658, 557658, 557658, 557658, 0, 0, 0, 672292, 557604, 557604, 557604, 557604, 864804, 698970",
      /* 12824 */ "557658, 557658, 711258, 557658, 557658, 557658, 557658, 557658, 739930, 750170, 557658, 557658",
      /* 12836 */ "557658, 768602, 557658, 557658, 735834, 557658, 557658, 557658, 557658, 557658, 557658, 836186",
      /* 12848 */ "557658, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 729690, 557658, 557658, 557658",
      /* 12860 */ "557658, 746074, 557658, 557658, 901722, 557658, 557658, 914010, 557658, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 12878 */ "808960, 0, 0, 0, 0, 0, 1836, 346, 346, 346, 346, 346, 346, 346, 346, 1842, 368, 0, 346, 346, 368",
      /* 12899 */ "368, 346, 368, 346, 368, 346, 368, 346, 368, 0, 0, 0, 0, 0, 1628, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 207",
      /* 12925 */ "208, 209, 0, 0, 0, 874496, 0, 751616, 0, 0, 557604, 557604, 557604, 557604, 557604, 557604, 557604",
      /* 12942 */ "557604, 557604, 557604, 557604, 0, 0, 0, 0, 536576, 0, 0, 0, 0, 557658, 557658, 557658, 557604",
      /* 12959 */ "752164, 557604, 557604, 778788, 557604, 557604, 809508, 557604, 557604, 840228, 557604, 557604",
      /* 12971 */ "858660, 557604, 557604, 746020, 557604, 557604, 557604, 760356, 557604, 557604, 557604, 557604",
      /* 12983 */ "557604, 557604, 557604, 801316, 807460, 875044, 883236, 557604, 557604, 557604, 918052, 0, 0, 0, 0",
      /* 12998 */ "557658, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 557658",
      /* 13010 */ "557658, 557658, 557658, 557658, 893530, 557658, 557658, 557658, 557658, 557658, 557658, 858714",
      /* 13022 */ "557658, 557658, 875098, 883290, 557658, 557658, 557658, 918106, 0, 0, 0, 0, 706560, 0, 0, 0, 0",
      /* 13039 */ "555008, 555008, 555008, 718848, 555008, 555008, 555008, 555008, 0, 0, 0, 557604, 557604, 557604",
      /* 13053 */ "690724, 692772, 557604, 557604, 557604, 557604, 557604, 557604, 557604, 557604, 557604, 877092",
      /* 13065 */ "557604, 891428, 557604, 901668, 557604, 557604, 913956, 557604, 836132, 557604, 557604, 557604",
      /* 13077 */ "557604, 557604, 557604, 557604, 557658, 557658, 557658, 557658, 557658, 703066, 717402, 719450",
      /* 13089 */ "557658, 684032, 0, 0, 0, 0, 0, 0, 837632, 761856, 753664, 743424, 765952, 0, 0, 851968, 0, 555008",
      /* 13107 */ "694272, 555008, 555008, 786432, 555008, 555008, 694272, 786432, 0, 670244, 557604, 557604, 694820",
      /* 13120 */ "557604, 678436, 682532, 700964, 557604, 557604, 557604, 557604, 756260, 789028, 811556, 848420",
      /* 13132 */ "557604, 860708, 557604, 557604, 815652, 557604, 557604, 832036, 557604, 557604, 557604, 557604",
      /* 13144 */ "557604, 557604, 557604, 557604, 557604, 557604, 868900, 872996, 557604, 557604, 557604, 885284",
      /* 13156 */ "557604, 557604, 557604, 905764, 670298, 557658, 557658, 694874, 557658, 557658, 557658, 731738",
      /* 13168 */ "557658, 557658, 762458, 787034, 557658, 557658, 557658, 557658, 834138, 557658, 557658, 557658",
      /* 13180 */ "557658, 557658, 557658, 557658, 877146, 557658, 891482, 557658, 557604, 903716, 557658, 678490",
      /* 13192 */ "682586, 701018, 557658, 557658, 557658, 557658, 756314, 789082, 811610, 848474, 557658, 860762",
      /* 13204 */ "724992, 724992, 557604, 721444, 725540, 557604, 557604, 557604, 557604, 557604, 557604, 557604",
      /* 13216 */ "557604, 899620, 557658, 721498, 725594, 557658, 557658, 557658, 557658, 557658, 557658, 557658",
      /* 13228 */ "557658, 899674, 0, 0, 0, 0, 821248, 0, 0, 0, 0, 0, 63488, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 13255 */ "0, 413, 712704, 866304, 0, 0, 0, 557604, 557604, 741924, 557604, 813604, 557604, 557604, 557604",
      /* 13270 */ "866852, 557658, 557658, 557658, 903770, 0, 0, 0, 0, 819200, 0, 0, 0, 0, 804864, 0, 919552, 741978",
      /* 13288 */ "557658, 813658, 557658, 557658, 557658, 866906, 708608, 0, 0, 0, 557604, 557604, 557604, 557604",
      /* 13302 */ "557604, 739876, 750116, 557604, 557604, 557604, 768548, 557604, 557604, 557604, 557604, 557604",
      /* 13314 */ "834084, 672346, 557658, 557658, 557658, 557658, 864858, 714752, 0, 841728, 557604, 758308, 850468",
      /* 13327 */ "557604, 557658, 758362, 850522, 557658, 770048, 723492, 557604, 723546, 557658, 557604, 557658",
      /* 13339 */ "557604, 557658, 557604, 557658, 856612, 856666, 0, 0, 0, 0, 0, 0, 1448, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 13361 */ "0, 0, 1562, 0, 0, 0, 0, 0, 0, 0, 20480, 0, 0, 0, 104448, 0, 104448, 0, 0, 0, 0, 0, 0, 0, 684, 0, 0",
      /* 13388 */ "0, 223, 223, 0, 0, 0, 0, 0, 0, 0, 104448, 104448, 104448, 104448, 104448, 104448, 104448, 104448",
      /* 13406 */ "104448, 104448, 104448, 104448, 0, 0, 0, 0, 0, 0, 0, 0, 0, 218, 0, 0, 0, 0, 0, 0, 557056, 557056",
      /* 13428 */ "868352, 872448, 557056, 557056, 557056, 884736, 557056, 557056, 557056, 557056, 557056, 557056",
      /* 13440 */ "557056, 190, 0, 0, 0, 190, 0, 191, 0, 0, 0, 191, 0, 0, 0, 696320, 0, 0, 0, 0, 0, 0, 0, 739328, 0, 0",
      /* 13466 */ "600, 0, 0, 0, 600, 0, 603, 0, 0, 0, 603, 0, 557056, 557056, 557056, 557056, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 13490 */ "0, 0, 0, 675840, 874496, 882688, 557056, 557056, 557056, 917504, 600, 0, 603, 0, 557056, 557056",
      /* 13506 */ "557056, 557056, 557056, 557056, 557056, 0, 0, 0, 671744, 557056, 557056, 557056, 557056, 864256",
      /* 13520 */ "714752, 0, 841728, 557056, 757760, 849920, 557056, 557056, 757760, 849920, 0, 0, 0, 106766, 0, 0, 0",
      /* 13537 */ "0, 0, 0, 0, 0, 0, 0, 106800, 0, 0, 0, 0, 0, 92463, 92463, 92463, 92463, 92463, 92463, 92463, 92463",
      /* 13558 */ "92463, 0, 0, 0, 0, 192, 0, 0, 0, 106800, 106800, 106800, 106800, 106800, 106800, 106800, 106800",
      /* 13575 */ "106800, 106800, 106800, 106800, 106800, 106800, 106800, 106800, 0, 0, 0, 0, 0, 0, 0, 0, 0, 346, 346",
      /* 13594 */ "346, 346, 346, 346, 346, 346, 346, 346, 368, 368, 368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 106886",
      /* 13615 */ "106886, 106886, 106886, 106886, 106886, 106886, 106886, 106800, 106800, 106887, 0, 0, 0, 0, 0",
      /* 13630 */ "104448, 104448, 0, 0, 104448, 104448, 0, 0, 0, 0, 0, 0, 0, 1145, 0, 0, 0, 0, 0, 0, 0, 0, 0, 484, 0",
      /* 13655 */ "0, 285, 285, 285, 285, 557056, 557056, 557056, 557056, 0, 0, 114688, 0, 5, 0, 0, 0, 0, 0, 0, 675840",
      /* 13676 */ "0, 2, 45059, 4, 5, 0, 0, 120832, 0, 0, 0, 0, 120832, 0, 0, 0, 0, 0, 0, 721, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 13704 */ "0, 0, 914, 0, 0, 0, 0, 0, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 120832, 0, 0, 0, 0, 0",
      /* 13732 */ "530432, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 223, 0, 0, 0, 63488, 120832, 120832, 120832, 120832",
      /* 13753 */ "120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 120832, 0",
      /* 13766 */ "0, 0, 0, 0, 0, 0, 0, 0, 346, 346, 346, 346, 562, 346, 346, 0, 102400, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 13793 */ "0, 0, 0, 0, 412, 0, 124928, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1152, 557056, 557056",
      /* 13818 */ "557056, 557056, 0, 0, 116736, 0, 5, 0, 0, 0, 0, 0, 0, 675840, 0, 2, 45059, 4, 5, 0, 0, 0, 122880, 0",
      /* 13842 */ "0, 0, 0, 122880, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 120832, 0, 0, 0, 0, 0, 20480, 0, 0, 0, 0, 0",
      /* 13871 */ "0, 0, 0, 0, 0, 123185, 0, 0, 0, 0, 0, 530432, 710, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1164, 0, 958, 0",
      /* 13899 */ "0, 123185, 123185, 123185, 123185, 123185, 123185, 123185, 123185, 123185, 123185, 123185, 123185",
      /* 13912 */ "123185, 123185, 123185, 123185, 0, 0, 0, 0, 122880, 0, 0, 0, 0, 2, 45059, 0, 5, 0, 0, 131072, 0, 0",
      /* 13934 */ "0, 0, 528384, 190, 191, 96256, 126976, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1165, 0, 2",
      /* 13959 */ "45059, 571572, 5, 0, 0, 0, 0, 184, 0, 0, 0, 0, 184, 0, 0, 0, 0, 0, 696320, 0, 0, 0, 0, 0, 0, 0",
      /* 13985 */ "739328, 0, 0, 0, 0, 0, 0, 1332, 0, 0, 0, 0, 1337, 0, 0, 0, 0, 0, 0, 541, 0, 0, 346, 346, 346, 555",
      /* 14011 */ "346, 346, 566, 0, 0, 0, 0, 129024, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63488, 0, 22528, 24576, 0, 2",
      /* 14037 */ "45059, 571572, 5, 0, 0, 0, 0, 0, 0, 0, 528384, 190, 191, 0, 0, 0, 0, 0, 843776, 0, 677888, 0",
      /* 14059 */ "860160, 677888, 700416, 555008, 788480, 860160, 788480, 557056, 557056, 557056, 557056, 0, 118784",
      /* 14072 */ "118784, 0, 5, 0, 0, 0, 0, 0, 0, 675840, 0, 0, 0, 0, 133120, 133120, 133120, 133120, 133120, 133120",
      /* 14092 */ "133120, 133120, 0, 0, 133120, 0, 0, 0, 0, 190, 190, 190, 190, 190, 190, 190, 190, 0, 0, 190, 0, 0",
      /* 14114 */ "0, 0, 0, 535, 0, 0, 535, 346, 346, 346, 346, 346, 346, 567, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 285",
      /* 14139 */ "285, 285, 285, 0, 285, 285, 285, 285, 285, 285, 285, 0, 0, 0, 346, 346, 1651, 346, 346, 346, 346",
      /* 14160 */ "582, 584, 346, 346, 594, 346, 346, 346, 346, 346, 0, 368, 0, 0, 0, 785, 346, 346, 346, 346, 346",
      /* 14181 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 831, 1339, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 14205 */ "0, 0, 285, 285, 285, 285, 285, 285, 285, 753, 285, 0, 1399, 0, 0, 0, 0, 0, 1401, 0, 0, 0, 0, 368",
      /* 14229 */ "368, 368, 368, 0, 0, 0, 1726, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1347, 1348, 0, 0, 0, 1351, 285, 0, 0, 0",
      /* 14255 */ "1169, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 403456, 0, 0, 27016, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0",
      /* 14283 */ "0, 0, 190, 191, 0, 0, 0, 0, 210, 0, 0, 0, 0, 210, 0, 0, 0, 0, 22528, 24576, 0, 0, 0, 20480, 0, 0, 0",
      /* 14310 */ "272, 0, 0, 285, 285, 285, 285, 0, 285, 285, 285, 285, 285, 285, 285, 0, 0, 0, 1649, 346, 346, 346",
      /* 14332 */ "346, 0, 0, 0, 0, 1065, 0, 0, 0, 0, 368, 368, 368, 26801, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 14359 */ "0, 0, 0, 104448, 0, 0, 0, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 137216, 0, 0, 0, 0, 212, 0",
      /* 14387 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 401726, 401726, 401726, 0, 137216, 137216, 137216, 137216, 0, 0, 0",
      /* 14409 */ "0, 0, 0, 137216, 0, 137216, 137216, 137216, 0, 0, 0, 0, 218, 218, 218, 218, 218, 218, 218, 218, 218",
      /* 14430 */ "218, 218, 218, 297, 218, 218, 359, 359, 359, 359, 359, 382, 359, 359, 137216, 137216, 137216",
      /* 14447 */ "137216, 137216, 137216, 137216, 137216, 0, 0, 0, 0, 0, 0, 0, 0, 0, 413, 0, 0, 0, 0, 0, 0, 346, 346",
      /* 14470 */ "346, 346, 559, 346, 346, 0, 894976, 907264, 0, 667648, 854016, 110592, 0, 0, 782336, 0, 0, 0, 0, 0",
      /* 14490 */ "0, 0, 696, 0, 0, 0, 0, 0, 0, 0, 0, 0, 557604, 557604, 557604, 557604, 557604, 557604, 557604",
      /* 14509 */ "557604, 557604, 557604, 557658, 557658, 557658, 557658, 557658, 557658, 557658, 0, 2, 45059, 4, 181",
      /* 14524 */ "0, 0, 0, 0, 0, 185, 0, 0, 0, 0, 0, 0, 0, 1173, 0, 0, 0, 0, 0, 1179, 0, 0, 0, 0, 0, 20480, 0, 0, 0",
      /* 14553 */ "0, 0, 139264, 0, 0, 0, 0, 0, 0, 0, 712, 0, 0, 716, 0, 0, 0, 0, 0, 0, 0, 0, 0, 139264, 139264",
      /* 14578 */ "139264, 139264, 139264, 139264, 139264, 139264, 139264, 139264, 139264, 139264, 0, 0, 0, 0, 0, 0, 0",
      /* 14595 */ "0, 0, 413, 0, 0, 285, 285, 285, 285, 0, 2, 45059, 4, 1098121, 0, 0, 0, 395, 0, 0, 0, 528384, 190",
      /* 14618 */ "191, 0, 0, 0, 0, 219, 0, 0, 0, 242, 0, 243, 0, 0, 0, 0, 243, 557056, 557056, 557056, 557056, 0, 0",
      /* 14641 */ "0, 0, 1098121, 0, 0, 0, 0, 0, 0, 675840, 0, 195, 0, 195, 249, 195, 0, 0, 0, 253, 229, 0, 229, 0",
      /* 14665 */ "22528, 24576, 0, 0, 0, 20480, 0, 0, 0, 0, 195, 279, 286, 286, 286, 286, 306, 286, 306, 306, 306",
      /* 14686 */ "306, 327, 327, 327, 327, 327, 327, 327, 338, 327, 327, 327, 338, 327, 327, 327, 327, 327, 286, 327",
      /* 14706 */ "327, 347, 347, 347, 347, 347, 370, 347, 347, 347, 347, 370, 370, 370, 370, 370, 370, 370, 370, 347",
      /* 14726 */ "347, 370, 26800, 0, 0, 0, 401, 0, 0, 404, 405, 0, 0, 0, 0, 0, 0, 0, 0, 0, 423, 0, 0, 0, 0, 223, 223",
      /* 14753 */ "0, 460, 0, 0, 0, 401, 0, 0, 0, 0, 0, 0, 0, 0, 0, 472, 0, 0, 520, 0, 0, 0, 0, 0, 0, 418, 0, 0, 0, 0",
      /* 14783 */ "0, 0, 0, 0, 0, 223, 223, 0, 520, 0, 520, 0, 0, 0, 404, 0, 0, 0, 0, 0, 0, 0, 543, 544, 0, 0, 0, 486",
      /* 14811 */ "0, 456, 486, 0, 346, 346, 552, 346, 346, 346, 346, 0, 0, 0, 0, 1065, 0, 0, 0, 0, 368, 368, 1263",
      /* 14834 */ "368, 368, 607, 368, 368, 368, 368, 368, 368, 368, 633, 368, 638, 368, 641, 368, 0, 346, 346, 368",
      /* 14854 */ "368, 346, 368, 1883, 1884, 346, 368, 346, 368, 0, 0, 0, 0, 0, 0, 532480, 794624, 0, 0, 0, 0, 0, 0",
      /* 14877 */ "0, 0, 0, 0, 1452, 0, 0, 0, 0, 0, 368, 652, 368, 368, 0, 0, 0, 26800, 5, 0, 0, 0, 657, 661, 0, 0, 0",
      /* 14904 */ "0, 0, 0, 1546, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 454, 252, 0, 0, 0, 0, 0, 0, 0, 0, 693, 0, 0, 0, 0, 0",
      /* 14934 */ "0, 0, 0, 0, 0, 0, 0, 104448, 104448, 104448, 0, 0, 0, 0, 725, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 14961 */ "190, 0, 0, 0, 285, 767, 285, 285, 285, 285, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 916, 0, 0, 0, 0, 743",
      /* 14988 */ "785, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1023, 346, 920, 0, 0, 0, 0, 0",
      /* 15010 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1312, 0, 0, 0, 938, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 191, 0, 0, 0",
      /* 15041 */ "785, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1021, 346, 346, 346, 346, 0, 0, 0, 0, 1065",
      /* 15062 */ "0, 0, 0, 0, 368, 1262, 368, 0, 0, 1127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 215, 22528, 24576, 0",
      /* 15089 */ "0, 0, 285, 285, 1184, 1185, 285, 285, 285, 285, 285, 285, 285, 285, 1192, 0, 1202, 346, 346, 346",
      /* 15109 */ "346, 1211, 346, 1213, 346, 346, 1216, 346, 346, 346, 346, 0, 0, 0, 0, 1065, 0, 0, 0, 0, 1261, 368",
      /* 15131 */ "368, 368, 1264, 368, 1266, 368, 368, 1269, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1082, 368",
      /* 15150 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 1281, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 15170 */ "368, 0, 1539, 0, 0, 0, 0, 1122, 0, 0, 1303, 0, 0, 0, 0, 0, 1308, 0, 0, 0, 0, 0, 0, 0, 1305, 0, 0, 0",
      /* 15198 */ "0, 0, 0, 0, 0, 0, 1321, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1316, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 15228 */ "120832, 120832, 120832, 0, 0, 1340, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285, 285",
      /* 15251 */ "751, 285, 285, 285, 368, 368, 1434, 368, 368, 0, 0, 0, 0, 0, 1441, 0, 0, 0, 0, 1445, 1607, 368, 368",
      /* 15274 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1619, 368, 368, 0, 1120, 0, 0, 0, 0, 0, 1122, 0",
      /* 15296 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285, 285, 0, 1757, 0, 1759, 346, 346, 346",
      /* 15321 */ "346, 346, 346, 346, 346, 346, 346, 368, 368, 368, 368, 1606, 368, 368, 368, 0, 0, 0, 1834, 0, 346",
      /* 15342 */ "346, 346, 346, 346, 346, 346, 346, 346, 368, 368, 368, 368, 368, 368, 0, 0, 0, 346, 346, 1867, 1868",
      /* 15363 */ "346, 346, 197, 198, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 669, 0, 262, 262, 0, 20480, 0, 0, 0",
      /* 15390 */ "0, 0, 0, 285, 285, 285, 285, 0, 285, 285, 285, 285, 285, 1646, 285, 0, 0, 1648, 346, 346, 346, 346",
      /* 15412 */ "346, 346, 1378, 346, 346, 346, 1381, 346, 346, 346, 346, 346, 346, 1393, 346, 346, 346, 346, 346",
      /* 15431 */ "1396, 346, 346, 346, 0, 0, 0, 262, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 206, 245, 0, 0, 0, 0, 0, 0",
      /* 15459 */ "962, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 123185, 123185, 123185, 0, 285, 1362, 0, 0, 0, 0, 0, 346",
      /* 15484 */ "346, 1368, 1369, 346, 346, 346, 346, 346, 346, 346, 1586, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 15503 */ "1046, 346, 346, 346, 346, 346, 346, 346, 0, 1399, 0, 0, 0, 0, 0, 1401, 0, 0, 0, 0, 368, 368, 1405",
      /* 15526 */ "1406, 346, 346, 346, 1791, 346, 1793, 346, 1795, 346, 368, 368, 368, 368, 1801, 368, 1803, 368",
      /* 15544 */ "1805, 368, 0, 0, 0, 0, 0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1217, 346, 346",
      /* 15566 */ "346, 0, 0, 199, 200, 201, 202, 203, 204, 0, 0, 0, 0, 0, 0, 0, 0, 0, 466, 0, 0, 0, 0, 0, 0, 263, 263",
      /* 15593 */ "264, 20480, 264, 271, 271, 0, 271, 280, 287, 287, 287, 287, 307, 287, 307, 307, 322, 324, 328, 328",
      /* 15613 */ "328, 336, 336, 337, 337, 328, 337, 337, 337, 328, 337, 337, 337, 337, 337, 287, 337, 337, 348, 348",
      /* 15633 */ "348, 348, 348, 371, 348, 348, 348, 348, 371, 371, 371, 371, 371, 371, 371, 371, 348, 348, 371",
      /* 15652 */ "26800, 690, 672, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 704, 738, 0, 0, 0, 0, 0, 0, 285, 745, 285",
      /* 15679 */ "285, 285, 285, 285, 285, 285, 759, 285, 285, 285, 285, 285, 285, 285, 285, 1358, 285, 285, 285, 285",
      /* 15699 */ "285, 285, 285, 285, 755, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285",
      /* 15719 */ "285, 1000, 774, 0, 0, 785, 786, 788, 346, 346, 346, 346, 795, 346, 799, 346, 346, 346, 346, 346",
      /* 15739 */ "1029, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1048, 346, 346, 346, 346, 346, 346, 346",
      /* 15758 */ "805, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 830, 346, 368, 854, 368",
      /* 15778 */ "858, 368, 368, 368, 368, 368, 864, 368, 368, 368, 368, 368, 368, 0, 1874, 0, 346, 346, 346, 346",
      /* 15798 */ "368, 368, 368, 0, 0, 975, 0, 0, 0, 0, 285, 285, 285, 285, 285, 285, 986, 285, 285, 285, 503, 285",
      /* 15820 */ "285, 285, 285, 285, 285, 285, 285, 285, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 258, 258, 785",
      /* 15845 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1022, 346, 346, 346, 346, 346, 1043, 1044",
      /* 15864 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 1032, 346, 346, 346, 346, 346, 346, 368, 368, 1089",
      /* 15883 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 882, 368, 1102, 1103, 368",
      /* 15902 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 0, 899, 285, 0, 0, 1486, 0, 346",
      /* 15923 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 812, 346, 346, 346, 346, 0, 0, 0, 0, 1544, 0, 0",
      /* 15945 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 137216, 137216, 0, 0, 368, 368, 368, 368, 1683, 368, 368, 0, 0, 0, 0",
      /* 15970 */ "0, 0, 0, 0, 0, 0, 0, 0, 1630, 0, 0, 346, 346, 1790, 346, 346, 346, 346, 346, 346, 368, 368, 368",
      /* 15993 */ "1800, 368, 368, 368, 0, 0, 0, 0, 1811, 0, 1813, 346, 346, 346, 346, 346, 346, 0, 0, 0, 0, 368, 368",
      /* 16016 */ "368, 368, 368, 368, 0, 0, 0, 346, 346, 346, 1876, 368, 368, 368, 1878, 0, 346, 346, 368, 368, 346",
      /* 16037 */ "368, 346, 368, 346, 368, 346, 368, 0, 0, 0, 1626, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 194, 0, 0, 193",
      /* 16063 */ "194, 231, 231, 0, 20480, 0, 0, 0, 273, 0, 281, 288, 288, 288, 288, 308, 288, 308, 308, 308, 325",
      /* 16084 */ "329, 329, 329, 329, 329, 329, 329, 329, 329, 329, 329, 329, 288, 329, 329, 349, 349, 349, 349, 349",
      /* 16104 */ "372, 349, 349, 349, 349, 349, 349, 372, 372, 372, 372, 372, 372, 372, 372, 349, 349, 372, 26800, 2",
      /* 16124 */ "45059, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 190, 191, 0, 0, 0, 0, 191, 191, 191, 191, 191, 191, 191, 191",
      /* 16149 */ "0, 0, 191, 0, 0, 0, 0, 0, 728, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 225, 0, 0, 0, 0, 0, 0, 0, 526, 0, 0",
      /* 16180 */ "0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1769, 368, 368, 368, 569, 346, 346, 346, 346",
      /* 16201 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 0, 368, 0, 346, 346, 368, 368, 1881, 1882, 346, 368",
      /* 16221 */ "346, 368, 346, 368, 0, 0, 1625, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 734, 0, 0, 0, 678, 0, 0, 0, 681",
      /* 16249 */ "682, 0, 0, 0, 0, 0, 223, 223, 0, 0, 0, 0, 0, 434, 435, 0, 437, 0, 0, 440, 0, 0, 0, 0, 0, 778, 0, 0",
      /* 16277 */ "0, 0, 0, 781, 0, 682, 0, 0, 681, 0, 0, 0, 0, 0, 0, 79872, 79872, 79872, 79872, 79872, 79872, 79872",
      /* 16299 */ "79872, 79872, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 785, 787, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 16322 */ "346, 346, 803, 816, 346, 346, 818, 346, 820, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 826",
      /* 16342 */ "346, 346, 346, 346, 346, 0, 1154, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 958, 0, 0, 0, 0, 0, 0, 83968",
      /* 16368 */ "83968, 83968, 83968, 83968, 83968, 83968, 83968, 83968, 26800, 1245, 1246, 346, 346, 0, 0, 0, 0",
      /* 16385 */ "1065, 0, 0, 0, 0, 368, 368, 368, 0, 0, 1809, 1810, 0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 1379",
      /* 16408 */ "346, 346, 346, 346, 346, 346, 346, 346, 822, 346, 346, 346, 346, 346, 346, 346, 346, 1031, 346, 346",
      /* 16428 */ "346, 346, 346, 346, 1037, 368, 368, 1265, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 16447 */ "368, 368, 368, 1086, 368, 346, 1388, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 16466 */ "346, 346, 346, 1666, 0, 1399, 0, 0, 0, 0, 0, 1401, 0, 0, 0, 0, 368, 1404, 368, 368, 0, 1120, 0, 0",
      /* 16490 */ "0, 0, 0, 1122, 0, 0, 0, 0, 0, 1125, 0, 0, 0, 0, 1475, 0, 285, 285, 285, 285, 1481, 285, 285, 285",
      /* 16514 */ "285, 285, 991, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 1361, 285, 285, 285, 368",
      /* 16534 */ "368, 1521, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1530, 368, 0, 346, 1879, 368",
      /* 16553 */ "1880, 346, 368, 346, 368, 346, 368, 346, 368, 0, 0, 0, 0, 0, 285, 0, 0, 358, 358, 358, 358, 358",
      /* 16575 */ "381, 358, 358, 358, 358, 381, 381, 381, 381, 381, 381, 381, 381, 358, 358, 381, 26800, 1632, 0, 0",
      /* 16595 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1326, 230, 228, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 16625 */ "703, 0, 0, 0, 207, 228, 0, 228, 0, 0, 0, 0, 0, 0, 0, 0, 22528, 24576, 0, 0, 0, 20480, 0, 0, 208, 0",
      /* 16651 */ "0, 282, 289, 289, 289, 289, 309, 289, 319, 309, 309, 309, 330, 330, 330, 330, 330, 330, 330, 339",
      /* 16671 */ "330, 330, 330, 339, 330, 330, 330, 330, 330, 289, 330, 330, 350, 350, 350, 350, 350, 373, 350, 350",
      /* 16691 */ "350, 350, 373, 373, 373, 373, 373, 373, 373, 373, 350, 350, 373, 26800, 473, 0, 0, 0, 479, 480, 0",
      /* 16712 */ "0, 483, 0, 0, 0, 285, 285, 285, 492, 285, 285, 500, 285, 506, 285, 509, 285, 512, 285, 285, 285",
      /* 16733 */ "285, 0, 0, 0, 0, 1005, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1336, 0, 0, 0, 0, 0, 346, 575, 346, 579, 346",
      /* 16760 */ "346, 585, 588, 592, 346, 346, 346, 346, 346, 0, 368, 368, 368, 368, 368, 1436, 1437, 0, 0, 0, 0, 0",
      /* 16782 */ "0, 0, 0, 0, 0, 673, 0, 0, 0, 0, 0, 368, 368, 368, 610, 368, 368, 621, 368, 630, 368, 634, 368, 368",
      /* 16806 */ "640, 643, 647, 0, 0, 0, 692, 0, 0, 0, 0, 0, 698, 0, 0, 0, 0, 0, 0, 0, 729, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 16835 */ "0, 346, 346, 553, 346, 346, 346, 346, 0, 0, 0, 0, 709, 455, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 254, 0",
      /* 16862 */ "0, 22528, 24576, 0, 0, 0, 0, 726, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 139264, 139264, 139264, 0, 0",
      /* 16887 */ "0, 0, 785, 346, 346, 346, 346, 346, 346, 346, 346, 346, 800, 346, 346, 346, 346, 819, 346, 346, 346",
      /* 16908 */ "346, 346, 346, 827, 346, 346, 346, 346, 0, 1251, 0, 0, 1065, 0, 1257, 0, 0, 368, 368, 368, 346, 804",
      /* 16930 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 814, 346, 346, 346, 346, 837, 346, 346, 600",
      /* 16950 */ "43850, 785, 603, 368, 368, 368, 368, 368, 368, 368, 1412, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 16969 */ "1095, 368, 368, 368, 368, 368, 368, 368, 346, 832, 346, 346, 346, 838, 346, 600, 43850, 785, 603",
      /* 16988 */ "368, 368, 368, 368, 368, 0, 0, 0, 1439, 0, 0, 0, 0, 0, 0, 0, 285, 285, 285, 983, 285, 285, 285, 285",
      /* 17012 */ "285, 935, 0, 0, 0, 0, 940, 0, 0, 0, 0, 945, 0, 0, 0, 0, 0, 0, 0, 1333, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 17041 */ "453, 0, 0, 0, 0, 0, 0, 0, 0, 953, 0, 0, 0, 0, 0, 0, 957, 0, 958, 0, 0, 0, 0, 0, 0, 285, 285, 285",
      /* 17069 */ "285, 285, 285, 1482, 285, 285, 285, 0, 974, 0, 976, 0, 0, 0, 285, 980, 981, 285, 285, 285, 285, 285",
      /* 17091 */ "987, 285, 285, 989, 285, 285, 285, 285, 993, 285, 285, 285, 995, 285, 285, 285, 285, 757, 285, 285",
      /* 17111 */ "285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 518, 0, 0, 0, 785, 1011, 346, 1012, 346, 1014",
      /* 17131 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1033, 346, 346, 346, 346, 346, 1025, 346, 346",
      /* 17150 */ "346, 1028, 346, 346, 346, 346, 346, 346, 346, 1035, 346, 346, 346, 346, 346, 1225, 346, 346, 346",
      /* 17169 */ "346, 346, 346, 346, 346, 346, 346, 600, 43850, 785, 603, 368, 368, 368, 368, 368, 1038, 1039, 346",
      /* 17188 */ "1041, 1042, 346, 346, 346, 346, 1047, 346, 1049, 346, 346, 346, 346, 0, 0, 0, 1254, 1065, 0, 0, 0",
      /* 17209 */ "1260, 368, 368, 368, 346, 1055, 346, 346, 346, 346, 1060, 0, 1065, 43850, 0, 1065, 1070, 368, 1071",
      /* 17228 */ "368, 368, 368, 368, 368, 1684, 368, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 528838, 0, 0, 0, 0, 1073, 368",
      /* 17253 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 1084, 368, 368, 368, 1087, 368, 368, 368, 368, 1106",
      /* 17272 */ "368, 1108, 368, 368, 368, 368, 368, 1114, 368, 368, 368, 368, 368, 1107, 368, 368, 368, 368, 368",
      /* 17291 */ "368, 368, 368, 368, 368, 368, 1298, 1299, 368, 368, 1120, 368, 1119, 0, 1120, 0, 0, 0, 0, 0, 1122",
      /* 17312 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1311, 0, 0, 0, 0, 0, 1129, 0, 0, 0, 1133, 0, 0, 0, 0, 0, 0, 0",
      /* 17342 */ "195, 0, 229, 0, 195, 195, 229, 0, 0, 0, 0, 1168, 0, 0, 0, 0, 0, 0, 1175, 0, 1177, 0, 0, 1180, 0, 0",
      /* 17368 */ "0, 0, 252, 252, 252, 252, 252, 252, 252, 252, 0, 0, 252, 26800, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 0",
      /* 17393 */ "0, 0, 0, 0, 0, 81920, 81920, 81920, 0, 0, 0, 0, 285, 285, 285, 285, 285, 285, 1188, 285, 285, 285",
      /* 17415 */ "285, 285, 285, 0, 0, 773, 0, 0, 0, 0, 0, 0, 0, 285, 285, 982, 285, 285, 285, 285, 285, 285, 346",
      /* 17438 */ "346, 1234, 1235, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1036, 346, 346",
      /* 17457 */ "346, 346, 1247, 346, 0, 0, 0, 0, 1065, 0, 0, 0, 0, 368, 368, 368, 368, 368, 1268, 368, 368, 368",
      /* 17479 */ "368, 368, 368, 368, 368, 368, 368, 1527, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1267, 368",
      /* 17498 */ "368, 368, 1271, 368, 1273, 368, 368, 368, 368, 368, 0, 0, 1438, 0, 0, 0, 0, 0, 0, 0, 0, 0, 782, 0",
      /* 17522 */ "0, 0, 0, 0, 0, 1327, 1328, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1338, 0, 0, 0, 0, 269, 269, 269, 269",
      /* 17550 */ "269, 269, 269, 269, 269, 269, 269, 269, 301, 269, 269, 364, 364, 364, 364, 364, 387, 364, 364, 346",
      /* 17570 */ "346, 1375, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1384, 346, 1386, 368, 368, 1421, 368",
      /* 17589 */ "1423, 368, 368, 368, 368, 368, 368, 1430, 368, 368, 368, 368, 0, 0, 0, 26800, 5, 0, 0, 0, 0, 0, 0",
      /* 17612 */ "0, 0, 531, 0, 0, 534, 0, 0, 0, 0, 368, 1433, 368, 368, 368, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 17639 */ "1124, 0, 0, 0, 1460, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 427, 223, 223, 285, 0, 0, 0, 1487, 346",
      /* 17666 */ "346, 346, 346, 346, 346, 1494, 346, 346, 346, 346, 0, 0, 1253, 0, 1065, 0, 0, 1259, 0, 368, 368",
      /* 17687 */ "368, 1519, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1528, 368, 368, 368, 368, 0, 0, 0",
      /* 17707 */ "26800, 5, 0, 0, 0, 658, 662, 0, 0, 1553, 0, 0, 0, 1555, 1556, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 425",
      /* 17734 */ "426, 0, 223, 223, 0, 0, 0, 1569, 285, 1571, 285, 285, 285, 1574, 285, 1576, 1577, 1578, 0, 346, 346",
      /* 17755 */ "346, 346, 1700, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1708, 346, 346, 346, 346, 1792, 346",
      /* 17774 */ "346, 346, 346, 368, 368, 368, 368, 368, 1802, 368, 0, 1624, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 17799 */ "203, 22528, 24576, 346, 346, 1583, 346, 346, 346, 346, 346, 346, 346, 1588, 346, 346, 1591, 346",
      /* 17817 */ "346, 346, 346, 1058, 346, 346, 0, 1065, 43850, 0, 1065, 368, 368, 368, 368, 368, 368, 1749, 368",
      /* 17836 */ "368, 368, 0, 0, 0, 0, 0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1596, 1598, 346, 1600",
      /* 17858 */ "1601, 368, 368, 368, 1605, 368, 368, 368, 368, 0, 1724, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 526, 0, 0",
      /* 17883 */ "0, 0, 0, 0, 0, 0, 0, 368, 368, 368, 1610, 368, 368, 1613, 368, 368, 368, 368, 368, 1618, 1620, 368",
      /* 17905 */ "1622, 1623, 0, 0, 0, 0, 1627, 0, 1629, 0, 0, 0, 0, 0, 0, 0, 0, 0, 671, 0, 0, 0, 0, 0, 0, 346, 368",
      /* 17932 */ "368, 1669, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1677, 368, 368, 368, 368, 454, 0, 0",
      /* 17952 */ "26800, 5, 0, 0, 0, 0, 0, 0, 0, 0, 1559, 0, 0, 0, 0, 0, 0, 0, 0, 1635, 0, 0, 0, 0, 0, 0, 0, 0, 685",
      /* 17981 */ "686, 0, 223, 223, 0, 0, 0, 1691, 1692, 0, 0, 1695, 0, 0, 0, 0, 0, 285, 285, 285, 285, 285, 0, 1003",
      /* 18005 */ "0, 0, 0, 1006, 0, 0, 0, 0, 0, 917, 1010, 1003, 368, 1721, 368, 368, 0, 0, 0, 0, 0, 1727, 0, 0, 1730",
      /* 18030 */ "0, 0, 0, 0, 0, 0, 779, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 193, 194, 0, 0, 0, 0, 0, 285, 346, 346, 346",
      /* 18058 */ "346, 1735, 346, 346, 346, 346, 346, 346, 346, 368, 368, 368, 368, 368, 368, 0, 0, 0, 346, 1866, 346",
      /* 18079 */ "346, 346, 346, 0, 1252, 0, 0, 1065, 0, 1258, 0, 0, 368, 368, 368, 368, 368, 1745, 368, 368, 368",
      /* 18100 */ "368, 368, 368, 368, 0, 0, 0, 0, 0, 1756, 346, 1789, 346, 346, 346, 346, 346, 346, 346, 368, 368",
      /* 18121 */ "1799, 368, 368, 368, 368, 368, 368, 1425, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1414, 368",
      /* 18140 */ "368, 368, 368, 368, 368, 368, 368, 368, 1807, 1808, 0, 0, 0, 0, 0, 1814, 346, 346, 1816, 346, 1818",
      /* 18161 */ "1856, 368, 1858, 368, 368, 368, 1862, 0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 346, 1215, 346",
      /* 18181 */ "346, 346, 346, 346, 0, 226, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 226, 226, 265, 265, 265, 265",
      /* 18206 */ "265, 265, 265, 265, 265, 265, 265, 265, 290, 265, 265, 351, 351, 351, 351, 351, 374, 351, 351, 226",
      /* 18226 */ "0, 0, 0, 0, 0, 0, 0, 226, 0, 0, 0, 0, 226, 22528, 24576, 0, 0, 265, 20480, 265, 265, 265, 0, 265",
      /* 18250 */ "265, 290, 290, 290, 290, 0, 290, 351, 351, 351, 351, 374, 374, 374, 374, 374, 374, 374, 374, 351",
      /* 18270 */ "351, 374, 26800, 2, 45059, 4, 5, 0, 0, 0, 0, 0, 0, 0, 189, 190, 191, 0, 0, 0, 0, 0, 1462, 0, 0, 0",
      /* 18296 */ "0, 0, 0, 0, 0, 0, 0, 0, 223, 190, 0, 0, 0, 570, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 18320 */ "346, 346, 346, 0, 368, 368, 368, 368, 613, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 18340 */ "368, 368, 1300, 368, 1120, 0, 673, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 719, 0, 0, 0, 0, 0",
      /* 18367 */ "673, 0, 0, 0, 0, 285, 285, 285, 285, 749, 285, 285, 285, 285, 769, 285, 0, 0, 0, 0, 0, 0, 0, 775, 0",
      /* 18392 */ "0, 0, 0, 0, 785, 346, 346, 346, 346, 346, 346, 796, 346, 346, 346, 346, 346, 346, 346, 1658, 346",
      /* 18413 */ "346, 346, 346, 1663, 346, 346, 346, 368, 855, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 18433 */ "368, 368, 368, 368, 1432, 346, 1857, 368, 368, 368, 368, 368, 0, 0, 0, 346, 346, 346, 346, 346, 346",
      /* 18454 */ "346, 1214, 346, 346, 346, 1218, 346, 1220, 0, 0, 233, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 470, 0",
      /* 18480 */ "0, 0, 0, 266, 20480, 266, 266, 266, 274, 266, 266, 291, 291, 291, 291, 310, 291, 310, 310, 310, 310",
      /* 18501 */ "331, 331, 331, 331, 331, 331, 331, 331, 331, 331, 331, 331, 291, 331, 331, 352, 352, 352, 352, 352",
      /* 18521 */ "375, 352, 352, 352, 352, 352, 352, 375, 375, 375, 375, 375, 375, 375, 375, 352, 352, 375, 26800, 2",
      /* 18541 */ "45059, 4, 5, 0, 0, 0, 0, 0, 0, 0, 528384, 190, 191, 0, 0, 0, 0, 0, 1331, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 18569 */ "0, 0, 717, 0, 0, 0, 0, 1458, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1457, 0, 0, 1554, 0, 0, 0",
      /* 18599 */ "1557, 0, 0, 0, 1561, 0, 0, 0, 0, 0, 0, 0, 1464, 0, 0, 0, 0, 0, 0, 0, 0, 0, 401408, 0, 401408, 0, 0",
      /* 18626 */ "0, 0, 1566, 0, 0, 0, 285, 285, 285, 285, 285, 285, 285, 285, 0, 0, 0, 346, 346, 346, 1763, 346, 346",
      /* 18649 */ "346, 346, 346, 346, 368, 368, 368, 368, 368, 368, 1714, 368, 368, 368, 368, 368, 368, 368, 0, 0",
      /* 18669 */ "1686, 0, 0, 0, 0, 0, 0, 0, 1643, 285, 285, 285, 285, 285, 285, 0, 0, 0, 346, 1650, 346, 346, 346",
      /* 18692 */ "346, 346, 1237, 346, 346, 346, 346, 346, 346, 346, 346, 1243, 346, 346, 368, 1668, 368, 368, 368",
      /* 18711 */ "368, 368, 368, 368, 1675, 368, 368, 368, 368, 1678, 368, 368, 1871, 1872, 368, 368, 0, 0, 0, 346",
      /* 18731 */ "346, 346, 346, 368, 368, 368, 368, 368, 368, 368, 368, 1716, 368, 1717, 1718, 368, 368, 368, 368",
      /* 18750 */ "608, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1276, 368, 368, 368, 653",
      /* 18770 */ "368, 0, 0, 0, 26800, 5, 0, 0, 0, 0, 0, 0, 0, 240, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1307, 0, 0, 1310, 0, 0",
      /* 18799 */ "0, 0, 0, 707, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 676, 0, 0, 368, 885, 368, 368, 368, 368, 368",
      /* 18826 */ "368, 368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 0, 0, 0, 1126, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 18854 */ "0, 918, 0, 1710, 346, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 642",
      /* 18874 */ "368, 368, 368, 1722, 368, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1631, 0, 0, 0, 0, 235, 0, 0, 0, 0",
      /* 18902 */ "0, 0, 0, 0, 0, 246, 0, 0, 0, 0, 0, 0, 108544, 0, 0, 0, 0, 0, 0, 0, 0, 971, 0, 0, 0, 20480, 0, 0, 0",
      /* 18931 */ "275, 0, 0, 292, 292, 292, 292, 311, 292, 311, 311, 311, 311, 332, 311, 311, 311, 311, 311, 311, 340",
      /* 18952 */ "311, 311, 311, 340, 311, 311, 311, 311, 311, 292, 311, 311, 353, 353, 353, 353, 353, 376, 353, 353",
      /* 18972 */ "353, 353, 376, 376, 376, 376, 376, 376, 376, 376, 353, 353, 376, 26800, 474, 0, 0, 477, 0, 0, 0, 0",
      /* 18994 */ "0, 0, 0, 0, 285, 285, 285, 493, 368, 368, 368, 611, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 19015 */ "368, 368, 368, 368, 1099, 368, 368, 0, 0, 0, 785, 346, 346, 346, 346, 791, 346, 346, 346, 346, 346",
      /* 19036 */ "346, 346, 587, 346, 346, 346, 346, 346, 346, 0, 368, 0, 952, 0, 0, 0, 0, 0, 0, 956, 0, 0, 0, 0, 0",
      /* 19061 */ "0, 0, 241, 0, 0, 0, 0, 0, 0, 0, 0, 0, 81920, 0, 0, 0, 0, 0, 0, 368, 368, 368, 1105, 368, 368, 368",
      /* 19087 */ "368, 368, 368, 368, 368, 368, 368, 368, 1117, 0, 0, 0, 1156, 0, 0, 0, 0, 0, 0, 1163, 0, 0, 0, 0, 0",
      /* 19112 */ "0, 0, 1547, 0, 0, 0, 0, 0, 0, 0, 0, 0, 965, 966, 0, 968, 0, 0, 0, 0, 0, 1314, 0, 0, 0, 1318, 0",
      /* 19139 */ "1320, 0, 0, 0, 1323, 0, 0, 0, 0, 0, 0, 941, 0, 0, 0, 0, 0, 947, 0, 0, 0, 0, 0, 0, 925, 0, 927, 928",
      /* 19167 */ "0, 930, 0, 0, 0, 0, 0, 0, 250, 0, 0, 0, 250, 0, 250, 0, 22528, 24576, 0, 0, 0, 0, 1330, 0, 0, 0, 0",
      /* 19194 */ "1335, 0, 0, 0, 0, 0, 0, 0, 780, 0, 0, 0, 0, 0, 0, 0, 0, 0, 672, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1341",
      /* 19223 */ "1342, 0, 0, 1345, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285, 285, 285, 285, 1483, 285, 285, 0, 0, 1473",
      /* 19246 */ "1474, 0, 1476, 1477, 285, 1479, 285, 285, 285, 285, 285, 285, 1484, 346, 346, 1500, 346, 346, 346",
      /* 19265 */ "346, 346, 346, 346, 346, 1506, 346, 346, 1508, 346, 346, 346, 346, 1823, 368, 368, 1825, 368, 1827",
      /* 19284 */ "368, 368, 368, 368, 0, 0, 0, 176, 5, 0, 0, 0, 0, 0, 0, 0, 0, 129024, 0, 0, 129024, 0, 0, 0, 129024",
      /* 19309 */ "346, 346, 1510, 346, 346, 346, 0, 0, 0, 0, 368, 1514, 368, 368, 1517, 368, 368, 368, 368, 614, 368",
      /* 19330 */ "368, 626, 368, 368, 368, 368, 368, 368, 368, 368, 1536, 368, 368, 0, 0, 0, 0, 0, 1531, 368, 368",
      /* 19351 */ "1533, 368, 368, 368, 1535, 368, 368, 368, 1538, 0, 0, 0, 0, 0, 0, 420, 0, 0, 0, 0, 0, 0, 0, 223",
      /* 19375 */ "223, 0, 0, 1568, 0, 285, 285, 285, 285, 1573, 285, 285, 285, 0, 0, 0, 346, 346, 1762, 346, 346, 346",
      /* 19397 */ "346, 346, 346, 346, 368, 368, 1772, 346, 346, 1595, 346, 346, 346, 346, 346, 368, 368, 368, 368",
      /* 19416 */ "368, 368, 368, 368, 346, 346, 368, 26800, 1608, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 19435 */ "1617, 368, 368, 368, 368, 368, 368, 1779, 0, 0, 0, 0, 0, 0, 0, 0, 346, 1837, 346, 346, 346, 346",
      /* 19457 */ "346, 346, 346, 368, 1843, 368, 1774, 368, 1776, 1777, 1778, 368, 0, 0, 0, 0, 0, 0, 0, 0, 346, 1761",
      /* 19479 */ "346, 346, 346, 1765, 346, 346, 346, 346, 368, 1771, 368, 368, 1844, 368, 1845, 368, 368, 368, 0, 0",
      /* 19499 */ "0, 0, 346, 346, 346, 346, 346, 346, 346, 1703, 346, 346, 346, 346, 346, 346, 346, 346, 600, 43850",
      /* 19519 */ "785, 603, 846, 368, 368, 368, 368, 0, 0, 234, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 702, 0, 0, 0",
      /* 19546 */ "0, 267, 20480, 267, 267, 267, 0, 267, 267, 293, 293, 293, 293, 312, 293, 312, 312, 312, 312, 333",
      /* 19566 */ "333, 333, 333, 333, 333, 333, 333, 333, 333, 333, 333, 293, 333, 343, 354, 354, 354, 354, 354, 377",
      /* 19586 */ "354, 354, 354, 354, 354, 354, 377, 377, 377, 377, 377, 377, 377, 377, 354, 354, 377, 26800, 0, 0",
      /* 19606 */ "446, 0, 0, 0, 0, 0, 452, 0, 0, 0, 452, 0, 0, 0, 0, 0, 0, 962, 285, 285, 285, 285, 285, 285, 285",
      /* 19631 */ "285, 285, 285, 285, 1360, 285, 285, 285, 285, 494, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285",
      /* 19651 */ "285, 285, 0, 0, 0, 0, 774, 0, 0, 0, 0, 0, 0, 452, 484, 0, 446, 0, 0, 0, 0, 346, 346, 346, 346, 558",
      /* 19677 */ "346, 346, 346, 346, 1224, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1240, 346, 346",
      /* 19696 */ "1242, 346, 346, 0, 0, 0, 708, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 400, 0, 0, 0, 0, 0, 0, 0, 741, 0",
      /* 19725 */ "0, 285, 285, 285, 747, 285, 285, 285, 285, 285, 1197, 285, 285, 285, 285, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 19749 */ "0, 924, 0, 0, 0, 0, 929, 0, 0, 0, 0, 0, 0, 0, 0, 785, 346, 346, 346, 346, 346, 792, 346, 346, 346",
      /* 19774 */ "346, 346, 346, 0, 0, 0, 0, 1513, 368, 368, 368, 368, 368, 851, 368, 368, 368, 368, 368, 368, 368",
      /* 19795 */ "368, 368, 865, 368, 368, 368, 368, 368, 368, 368, 1525, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 19814 */ "1284, 368, 368, 368, 368, 368, 368, 368, 368, 368, 886, 368, 368, 368, 368, 368, 368, 368, 368, 896",
      /* 19834 */ "368, 368, 0, 0, 0, 1121, 0, 658, 0, 0, 0, 1123, 0, 662, 0, 0, 785, 346, 346, 346, 346, 346, 1016",
      /* 19857 */ "1017, 346, 346, 1020, 346, 346, 346, 346, 346, 346, 809, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 19876 */ "346, 1797, 368, 368, 368, 368, 368, 368, 368, 1075, 1076, 368, 368, 1079, 368, 368, 368, 368, 368",
      /* 19895 */ "368, 368, 368, 368, 368, 866, 368, 368, 368, 368, 368, 1088, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 19915 */ "368, 368, 368, 368, 368, 368, 368, 650, 0, 0, 0, 0, 1157, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 19940 */ "397312, 397312, 0, 0, 0, 1167, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 933, 0, 368, 368, 368, 368",
      /* 19966 */ "1292, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 346, 346, 346, 346, 1855, 285",
      /* 19987 */ "285, 1354, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 998, 285, 1373",
      /* 20006 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1054, 0, 0, 0, 0, 1461",
      /* 20027 */ "0, 0, 0, 0, 0, 0, 1466, 0, 0, 0, 0, 0, 0, 450, 0, 0, 0, 0, 0, 0, 456, 0, 0, 1471, 0, 0, 0, 0, 0",
      /* 20056 */ "285, 1478, 285, 285, 285, 285, 285, 285, 285, 285, 1200, 285, 0, 0, 0, 0, 0, 0, 285, 1485, 0, 0, 0",
      /* 20079 */ "346, 346, 346, 1491, 346, 346, 346, 346, 346, 346, 346, 591, 346, 346, 346, 346, 346, 346, 0, 368",
      /* 20099 */ "0, 0, 0, 1633, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 410, 411, 0, 0, 368, 368, 368, 1682, 368, 368",
      /* 20125 */ "368, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 244, 0, 0, 0, 0, 244, 1819, 346, 346, 346, 368, 368, 368, 368",
      /* 20150 */ "1826, 368, 1828, 368, 368, 368, 0, 0, 0, 0, 0, 0, 0, 346, 1815, 346, 346, 346, 346, 368, 1870, 368",
      /* 20172 */ "368, 368, 368, 0, 0, 0, 346, 346, 346, 346, 368, 368, 368, 368, 368, 368, 368, 1715, 368, 368, 368",
      /* 20193 */ "368, 368, 368, 368, 1283, 368, 368, 368, 368, 368, 368, 368, 368, 1526, 368, 368, 368, 368, 368",
      /* 20212 */ "368, 368, 0, 0, 0, 20480, 0, 0, 0, 276, 0, 0, 294, 294, 294, 294, 313, 294, 313, 321, 313, 313, 313",
      /* 20235 */ "313, 313, 313, 313, 313, 313, 313, 313, 313, 313, 313, 294, 313, 313, 355, 355, 355, 355, 355, 378",
      /* 20255 */ "355, 355, 355, 355, 355, 355, 378, 378, 378, 378, 378, 378, 378, 378, 355, 355, 378, 26800, 495",
      /* 20274 */ "285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 0, 0, 0, 1004, 0, 0, 1007, 0, 0, 0, 0",
      /* 20297 */ "0, 913, 913, 571, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 0, 368, 368, 368",
      /* 20318 */ "368, 616, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1431, 368, 368, 0, 679",
      /* 20338 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 223, 223, 0, 0, 0, 0, 433, 0, 0, 0, 0, 438, 0, 0, 0, 0, 0, 0, 0, 1634, 0",
      /* 20368 */ "0, 0, 1637, 0, 0, 0, 0, 0, 0, 0, 785, 346, 346, 346, 346, 346, 793, 346, 346, 346, 346, 346, 346, 0",
      /* 20392 */ "1253, 0, 1259, 368, 368, 368, 368, 368, 368, 368, 1094, 368, 368, 368, 1097, 1098, 368, 1100, 1101",
      /* 20411 */ "346, 817, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 828, 346, 346, 346, 346, 346, 1502, 346",
      /* 20431 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 811, 346, 346, 346, 346, 346, 852, 368, 368, 368, 368",
      /* 20451 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 869, 368, 368, 368, 887, 368, 368, 368, 368",
      /* 20471 */ "368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 346, 346, 346, 346, 346, 901, 0, 0, 0, 907, 0, 0, 0, 0, 0",
      /* 20496 */ "0, 0, 0, 0, 0, 0, 0, 528384, 0, 0, 0, 0, 0, 921, 0, 0, 923, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 20525 */ "555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 555008, 785, 346, 346, 346, 346",
      /* 20539 */ "346, 346, 346, 1018, 346, 346, 346, 346, 346, 346, 1024, 368, 368, 368, 1077, 368, 368, 368, 368",
      /* 20558 */ "368, 368, 1083, 368, 368, 368, 368, 368, 368, 368, 1614, 1615, 368, 368, 368, 368, 368, 368, 368, 0",
      /* 20578 */ "0, 0, 0, 346, 1852, 346, 346, 346, 0, 1141, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 949, 0, 0, 0",
      /* 20606 */ "0, 0, 1170, 0, 0, 0, 0, 0, 1176, 0, 0, 0, 0, 0, 0, 0, 1558, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1451, 0, 0",
      /* 20635 */ "1454, 0, 0, 0, 368, 1291, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 0, 0",
      /* 20656 */ "1540, 0, 0, 1542, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1641, 0, 285, 346, 346, 346, 346",
      /* 20682 */ "346, 346, 346, 346, 346, 346, 1741, 346, 368, 368, 368, 368, 368, 368, 0, 1864, 0, 346, 346, 346",
      /* 20702 */ "346, 346, 346, 346, 1062, 1065, 43850, 1067, 1065, 368, 368, 368, 368, 0, 0, 0, 236, 0, 237, 238, 0",
      /* 20723 */ "0, 0, 0, 0, 213, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 223, 213, 0, 0, 0, 0, 213, 0, 20480, 0, 0, 0, 0, 0",
      /* 20752 */ "0, 295, 295, 295, 295, 314, 295, 320, 320, 320, 314, 320, 320, 320, 320, 320, 320, 320, 320, 320",
      /* 20772 */ "320, 320, 320, 295, 320, 320, 356, 356, 356, 356, 356, 379, 356, 356, 356, 356, 356, 356, 379, 379",
      /* 20792 */ "379, 379, 379, 379, 379, 379, 356, 356, 379, 26800, 414, 415, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 20816 */ "223, 223, 0, 0, 0, 444, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 26800, 0, 0, 476, 0, 0, 0, 0",
      /* 20845 */ "0, 0, 414, 485, 0, 285, 488, 285, 285, 285, 505, 285, 285, 285, 285, 285, 285, 285, 285, 285, 0, 0",
      /* 20867 */ "0, 0, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1497, 346, 496, 285, 285, 504, 285, 285, 285",
      /* 20887 */ "510, 285, 285, 285, 517, 285, 0, 0, 0, 0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 346, 346, 368",
      /* 20909 */ "368, 0, 519, 0, 0, 0, 0, 527, 0, 0, 0, 0, 0, 536, 0, 433, 0, 0, 0, 0, 285, 285, 285, 285, 285, 285",
      /* 20935 */ "285, 285, 0, 0, 0, 346, 346, 346, 346, 1764, 346, 1766, 1767, 1768, 346, 368, 368, 368, 368, 368",
      /* 20955 */ "368, 0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1219, 346, 0, 519, 414",
      /* 20976 */ "546, 0, 0, 0, 0, 0, 346, 549, 346, 346, 560, 346, 346, 346, 346, 1249, 0, 0, 0, 1065, 1255, 0, 0, 0",
      /* 21000 */ "368, 368, 368, 0, 0, 0, 0, 0, 1812, 0, 346, 346, 346, 346, 346, 346, 346, 1063, 1065, 43850, 1068",
      /* 21021 */ "1065, 368, 368, 368, 368, 572, 346, 346, 346, 581, 346, 346, 346, 346, 596, 346, 346, 346, 346, 0",
      /* 21041 */ "368, 368, 368, 368, 617, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1416, 368, 368",
      /* 21061 */ "368, 368, 604, 368, 368, 615, 368, 368, 627, 368, 368, 368, 636, 368, 368, 368, 368, 368, 620, 368",
      /* 21081 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 875, 368, 368, 877, 368, 879, 368, 368, 368, 651, 368",
      /* 21101 */ "368, 368, 0, 0, 0, 26800, 5, 0, 0, 0, 0, 0, 0, 0, 251, 0, 0, 0, 0, 0, 0, 22528, 24576, 960, 0, 0, 0",
      /* 21128 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 63488, 785, 346, 346, 346, 346, 346, 346, 346, 346, 1019, 346",
      /* 21152 */ "346, 346, 346, 346, 346, 346, 823, 346, 346, 346, 346, 346, 346, 346, 346, 1227, 346, 1229, 346",
      /* 21171 */ "346, 346, 346, 346, 0, 0, 0, 1183, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285",
      /* 21192 */ "997, 285, 285, 1207, 0, 346, 1209, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 21212 */ "1231, 346, 346, 346, 1233, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1244",
      /* 21231 */ "1313, 0, 0, 1315, 0, 0, 0, 1319, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1162, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1329",
      /* 21259 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 469, 0, 0, 0, 285, 0, 0, 1364, 0, 0, 0, 346, 346, 346, 346, 346",
      /* 21287 */ "346, 1371, 346, 346, 346, 346, 1250, 0, 0, 0, 1065, 1256, 0, 0, 0, 368, 368, 368, 368, 368, 1424",
      /* 21308 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1752, 0, 1754, 0, 0, 0, 346, 1374, 346, 1376, 346",
      /* 21329 */ "346, 346, 346, 346, 346, 346, 1382, 346, 346, 346, 346, 346, 346, 1503, 346, 346, 346, 346, 346",
      /* 21348 */ "346, 346, 346, 346, 1228, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1390, 346, 346, 346, 346",
      /* 21367 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 1664, 346, 346, 368, 368, 1408, 368, 368, 368, 1411",
      /* 21386 */ "368, 1413, 368, 368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 1851, 346, 346, 346, 346, 1419, 368, 368",
      /* 21407 */ "368, 368, 368, 368, 368, 1427, 368, 368, 368, 368, 368, 368, 368, 0, 0, 0, 1782, 0, 0, 1785, 0, 346",
      /* 21429 */ "0, 1446, 0, 0, 0, 0, 0, 1449, 1450, 0, 0, 0, 0, 0, 0, 0, 252, 0, 0, 0, 0, 0, 0, 22528, 24576, 0",
      /* 21455 */ "1459, 0, 0, 0, 0, 1463, 0, 0, 0, 0, 0, 1467, 0, 0, 1470, 368, 368, 368, 1522, 368, 368, 368, 368",
      /* 21478 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 1115, 368, 368, 0, 1567, 0, 0, 1570, 285, 285, 285",
      /* 21498 */ "285, 285, 285, 285, 0, 0, 0, 346, 346, 1210, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 21519 */ "1241, 346, 346, 346, 346, 1581, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1589, 1590, 346",
      /* 21538 */ "346, 346, 346, 346, 1599, 346, 346, 368, 368, 1604, 368, 368, 368, 368, 368, 368, 1080, 368, 368",
      /* 21557 */ "368, 368, 368, 368, 368, 368, 368, 893, 368, 368, 368, 368, 0, 0, 346, 1594, 346, 346, 346, 346",
      /* 21577 */ "346, 346, 368, 1603, 368, 368, 368, 368, 368, 368, 368, 1109, 368, 368, 368, 368, 368, 368, 368",
      /* 21596 */ "368, 876, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1611, 1612, 368, 368, 368, 368",
      /* 21615 */ "1616, 368, 368, 368, 368, 368, 368, 368, 1750, 368, 368, 0, 0, 0, 0, 0, 0, 0, 911, 0, 913, 0, 0, 0",
      /* 21639 */ "0, 0, 0, 0, 744, 285, 285, 285, 285, 285, 285, 285, 754, 1653, 1654, 346, 346, 346, 346, 346, 346",
      /* 21660 */ "346, 346, 1661, 346, 346, 346, 346, 346, 346, 821, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 21679 */ "1394, 346, 346, 346, 346, 346, 346, 346, 1679, 368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 21702 */ "0, 0, 467, 0, 0, 0, 0, 0, 0, 0, 0, 1694, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285, 285, 0, 0, 0, 0, 346",
      /* 21730 */ "1489, 346, 346, 1492, 346, 346, 346, 346, 346, 346, 0, 0, 0, 0, 368, 368, 1515, 368, 368, 368, 0",
      /* 21751 */ "285, 346, 346, 346, 346, 346, 346, 1737, 346, 346, 346, 346, 346, 368, 368, 368, 368, 368, 368, 368",
      /* 21771 */ "368, 368, 368, 368, 368, 1676, 368, 368, 368, 368, 368, 368, 1747, 368, 368, 368, 368, 368, 0, 1753",
      /* 21791 */ "0, 0, 0, 0, 0, 0, 481, 0, 0, 0, 0, 0, 285, 489, 285, 285, 1773, 368, 368, 368, 368, 368, 368, 0, 0",
      /* 21816 */ "0, 0, 1783, 0, 0, 0, 346, 346, 346, 578, 346, 583, 346, 586, 346, 346, 597, 346, 346, 346, 0, 368",
      /* 21838 */ "0, 346, 346, 368, 368, 346, 368, 346, 368, 1885, 1886, 346, 368, 0, 0, 0, 0, 0, 0, 532480, 0, 0, 0",
      /* 21861 */ "0, 0, 0, 0, 0, 0, 0, 0, 1453, 0, 0, 0, 0, 1804, 368, 1806, 0, 0, 0, 0, 0, 0, 0, 346, 346, 346, 346",
      /* 21888 */ "346, 346, 346, 1030, 346, 346, 346, 346, 346, 346, 346, 346, 810, 346, 346, 346, 346, 346, 346, 346",
      /* 21908 */ "346, 368, 368, 1859, 1860, 368, 368, 0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 1045, 346, 346",
      /* 21928 */ "346, 346, 346, 346, 346, 346, 600, 43850, 785, 603, 368, 368, 848, 368, 368, 0, 214, 215, 216, 217",
      /* 21948 */ "0, 0, 0, 0, 0, 0, 223, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 196, 217, 217, 216, 20480",
      /* 21976 */ "216, 216, 216, 0, 216, 283, 296, 296, 296, 296, 315, 296, 315, 315, 323, 326, 334, 334, 334, 334",
      /* 21996 */ "334, 334, 334, 334, 334, 334, 334, 334, 296, 334, 334, 357, 357, 357, 357, 357, 380, 357, 357, 357",
      /* 22016 */ "357, 357, 357, 380, 380, 380, 380, 380, 380, 380, 380, 357, 357, 380, 26800, 0, 0, 0, 417, 0, 0, 0",
      /* 22038 */ "0, 0, 0, 0, 0, 0, 0, 223, 223, 0, 0, 431, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1350, 0, 285, 285",
      /* 22066 */ "285, 499, 285, 285, 285, 508, 285, 285, 285, 285, 515, 285, 285, 0, 0, 0, 0, 0, 0, 346, 346, 346",
      /* 22088 */ "346, 346, 346, 346, 346, 1372, 537, 408, 0, 538, 0, 0, 0, 471, 0, 0, 0, 0, 0, 0, 534, 471, 346, 346",
      /* 22112 */ "577, 580, 346, 346, 346, 589, 593, 346, 346, 346, 346, 346, 0, 368, 368, 368, 368, 618, 368, 368",
      /* 22132 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 878, 368, 368, 368, 368, 368, 368, 609, 612, 368, 619",
      /* 22152 */ "368, 368, 368, 632, 635, 368, 368, 368, 644, 648, 0, 723, 0, 0, 0, 0, 0, 0, 0, 0, 732, 0, 0, 0, 0",
      /* 22177 */ "0, 0, 0, 51200, 0, 0, 0, 0, 51200, 51200, 51200, 0, 346, 346, 346, 836, 346, 346, 346, 600, 43850",
      /* 22198 */ "785, 603, 368, 368, 368, 368, 368, 368, 368, 1780, 0, 1781, 0, 0, 0, 0, 0, 346, 346, 346, 1838, 346",
      /* 22220 */ "1839, 346, 346, 346, 368, 368, 0, 936, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 970, 0, 951, 0, 0",
      /* 22247 */ "954, 0, 0, 0, 0, 0, 0, 0, 0, 0, 959, 0, 461, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 783, 784",
      /* 22276 */ "973, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 0, 0, 0, 0",
      /* 22300 */ "0, 0, 1001, 285, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1009, 0, 0, 0, 0, 0, 0, 528, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 22330 */ "0, 687, 223, 223, 688, 0, 0, 346, 346, 1056, 346, 346, 346, 346, 0, 1065, 43850, 0, 1065, 368, 368",
      /* 22351 */ "368, 368, 368, 861, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1297, 368, 368, 368, 368, 0",
      /* 22371 */ "0, 0, 0, 0, 1142, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 557056, 557056, 557056, 557056, 557056",
      /* 22393 */ "557056, 557056, 0, 0, 0, 1400, 0, 1063, 0, 0, 0, 1402, 0, 1068, 368, 368, 368, 368, 368, 889, 368",
      /* 22414 */ "368, 368, 368, 894, 368, 368, 368, 0, 0, 0, 0, 0, 0, 0, 346, 346, 346, 346, 1817, 346, 346, 346",
      /* 22436 */ "346, 1501, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 599, 346, 0, 368, 346, 346",
      /* 22456 */ "346, 1584, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1050, 346, 346, 346, 1699",
      /* 22475 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1398, 346, 346, 1711",
      /* 22494 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1301, 0, 0, 285, 346, 346",
      /* 22514 */ "346, 1734, 346, 346, 346, 346, 346, 346, 346, 346, 368, 368, 368, 368, 368, 368, 368, 368, 1674",
      /* 22533 */ "368, 368, 368, 368, 368, 368, 368, 1110, 368, 368, 368, 368, 368, 368, 1116, 368, 368, 1744, 368",
      /* 22552 */ "368, 368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 368, 368, 1775, 368, 368",
      /* 22576 */ "368, 368, 0, 0, 0, 0, 0, 0, 0, 0, 346, 346, 346, 807, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 22599 */ "346, 346, 346, 1383, 346, 346, 346, 0, 0, 0, 20480, 0, 0, 0, 277, 0, 0, 285, 285, 285, 285, 0, 285",
      /* 22622 */ "285, 1644, 1645, 285, 285, 285, 0, 0, 0, 346, 346, 346, 346, 1652, 0, 0, 420, 0, 0, 420, 0, 0, 0, 0",
      /* 22646 */ "0, 0, 0, 0, 0, 0, 0, 674, 0, 0, 0, 677, 705, 0, 0, 0, 0, 0, 0, 0, 0, 714, 0, 718, 0, 0, 671, 0, 0",
      /* 22675 */ "0, 0, 285, 285, 285, 285, 285, 285, 285, 285, 0, 0, 0, 1580, 0, 0, 0, 785, 346, 346, 346, 346, 346",
      /* 22698 */ "346, 346, 346, 346, 346, 802, 346, 346, 346, 835, 346, 346, 346, 600, 43850, 785, 603, 368, 368",
      /* 22717 */ "368, 368, 850, 346, 346, 834, 346, 346, 346, 346, 600, 43850, 785, 603, 368, 368, 368, 368, 368",
      /* 22736 */ "368, 622, 368, 368, 368, 368, 637, 639, 368, 368, 649, 368, 870, 368, 368, 368, 368, 368, 368, 368",
      /* 22756 */ "368, 368, 368, 368, 368, 368, 368, 646, 368, 902, 0, 0, 0, 908, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 22782 */ "557658, 557658, 557658, 557658, 0, 0, 1155, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 720, 0, 721",
      /* 22805 */ "1166, 0, 0, 0, 0, 0, 1172, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 525, 0, 530, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 22835 */ "0, 285, 285, 285, 285, 285, 285, 285, 285, 285, 1190, 1191, 285, 285, 285, 768, 285, 770, 0, 0, 0",
      /* 22856 */ "0, 0, 0, 0, 0, 0, 0, 0, 256, 0, 0, 22528, 24576, 346, 346, 346, 1223, 346, 346, 1226, 346, 346, 346",
      /* 22879 */ "346, 346, 346, 346, 346, 346, 1380, 346, 346, 346, 346, 346, 346, 346, 368, 1279, 368, 368, 368",
      /* 22898 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 727, 0, 0, 0, 1253, 0, 0, 0, 0, 0, 1259, 0",
      /* 22921 */ "0, 0, 368, 368, 368, 368, 368, 1092, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1113, 368",
      /* 22941 */ "368, 368, 368, 368, 368, 368, 368, 1422, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 22961 */ "368, 1287, 1288, 368, 368, 368, 368, 368, 1723, 0, 1725, 0, 0, 0, 0, 1729, 0, 0, 1731, 0, 0, 0, 0",
      /* 22984 */ "285, 285, 285, 285, 285, 285, 1575, 285, 0, 0, 1579, 346, 346, 346, 346, 1657, 346, 346, 346, 346",
      /* 23004 */ "1660, 346, 346, 346, 346, 346, 346, 0, 0, 0, 0, 368, 368, 368, 1516, 368, 368, 0, 1758, 0, 346, 346",
      /* 23026 */ "346, 346, 346, 346, 346, 346, 346, 346, 368, 368, 368, 368, 368, 1713, 368, 368, 368, 368, 368, 368",
      /* 23046 */ "1719, 368, 0, 462, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1138, 1139, 497, 285, 285, 285, 285",
      /* 23071 */ "285, 285, 285, 285, 285, 285, 285, 285, 0, 0, 0, 0, 0, 0, 346, 1367, 346, 346, 346, 346, 346, 346",
      /* 23093 */ "346, 0, 1065, 43850, 0, 1065, 368, 368, 368, 368, 0, 0, 0, 0, 522, 0, 0, 0, 0, 0, 0, 0, 0, 0, 522",
      /* 23118 */ "0, 0, 0, 0, 0, 0, 0, 0, 522, 0, 0, 0, 0, 0, 0, 346, 346, 346, 346, 561, 346, 346, 346, 346, 1377",
      /* 23143 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 598, 346, 346, 0, 368, 368, 368, 368, 654, 0",
      /* 23164 */ "0, 0, 26800, 5, 0, 0, 0, 659, 663, 0, 0, 0, 0, 0, 0, 532480, 794624, 0, 0, 28672, 0, 0, 0, 14336, 0",
      /* 23189 */ "346, 346, 346, 1656, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1051, 346, 346",
      /* 23208 */ "346, 218, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 737280, 0, 0, 218, 20480, 218, 218, 218, 0",
      /* 23234 */ "218, 218, 297, 297, 297, 297, 0, 297, 359, 359, 359, 359, 382, 382, 382, 382, 382, 382, 382, 382",
      /* 23254 */ "359, 359, 382, 26800, 0, 475, 0, 0, 0, 0, 0, 0, 0, 0, 421, 0, 285, 285, 285, 285, 285, 285, 285, 0",
      /* 23278 */ "0, 0, 346, 346, 346, 346, 346, 1212, 346, 346, 346, 346, 346, 346, 346, 346, 824, 825, 346, 346",
      /* 23298 */ "346, 346, 346, 346, 285, 285, 501, 285, 285, 285, 285, 511, 285, 285, 516, 285, 285, 0, 0, 0, 0",
      /* 23319 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 0, 0, 533, 0, 0, 539, 0, 0, 0, 0, 466",
      /* 23342 */ "0, 0, 542, 0, 0, 0, 0, 0, 0, 555008, 555008, 555008, 555008, 555008, 751616, 555008, 555008, 858112",
      /* 23360 */ "555008, 0, 0, 691, 0, 0, 0, 0, 0, 0, 0, 0, 0, 701, 0, 0, 0, 0, 0, 0, 979, 285, 285, 285, 285, 285",
      /* 23386 */ "285, 285, 285, 285, 513, 285, 285, 285, 0, 0, 0, 0, 706, 0, 0, 0, 0, 0, 0, 713, 0, 0, 0, 0, 0, 0, 0",
      /* 23413 */ "285, 285, 285, 748, 285, 285, 285, 285, 285, 722, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 736, 737",
      /* 23438 */ "285, 285, 756, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 999, 285, 0, 0",
      /* 23459 */ "713, 785, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1230, 346, 346, 346, 285",
      /* 23478 */ "1002, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1456, 0, 785, 346, 346, 346, 1013, 346, 346, 346",
      /* 23503 */ "346, 346, 346, 346, 346, 346, 346, 346, 1504, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 23522 */ "1027, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1602, 368, 368, 368, 368, 368",
      /* 23541 */ "368, 368, 346, 346, 346, 1057, 346, 1059, 346, 0, 1065, 43850, 0, 1065, 368, 368, 368, 1072, 1118",
      /* 23560 */ "368, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1469, 0, 1140, 0, 0, 0, 0, 0, 1144, 0, 0, 0, 0, 1149",
      /* 23589 */ "0, 0, 1151, 0, 0, 0, 0, 285, 285, 1572, 285, 285, 285, 285, 285, 0, 0, 0, 346, 346, 346, 806, 346",
      /* 23612 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1052, 1053, 346, 1153, 0, 0, 0, 0, 0",
      /* 23633 */ "1159, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 533, 535, 0, 0, 0, 0, 1193, 285, 1194, 1195, 285, 285, 285, 285",
      /* 23658 */ "285, 285, 0, 0, 0, 0, 1205, 0, 0, 0, 0, 402, 0, 0, 0, 406, 407, 408, 409, 0, 0, 0, 0, 0, 0, 481, 0",
      /* 23685 */ "0, 0, 0, 528, 0, 0, 0, 0, 0, 0, 519, 0, 0, 414, 0, 540, 0, 485, 0, 0, 1232, 346, 346, 346, 1236",
      /* 23710 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1659, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 23730 */ "346, 346, 1248, 0, 0, 0, 0, 1065, 0, 0, 0, 0, 368, 368, 368, 368, 368, 1293, 368, 368, 1295, 368",
      /* 23752 */ "368, 368, 368, 368, 368, 0, 0, 0, 346, 346, 346, 346, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 23773 */ "368, 0, 0, 0, 0, 0, 0, 0, 1690, 0, 368, 368, 368, 1280, 368, 1282, 368, 368, 368, 368, 368, 1285",
      /* 23795 */ "368, 368, 368, 1289, 368, 368, 368, 368, 1409, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 23814 */ "368, 368, 368, 1529, 368, 368, 346, 346, 346, 1511, 346, 346, 0, 0, 0, 0, 368, 368, 368, 368, 368",
      /* 23835 */ "368, 368, 1270, 368, 368, 368, 368, 368, 368, 368, 368, 1081, 368, 368, 368, 368, 368, 368, 368",
      /* 23854 */ "368, 891, 368, 368, 368, 897, 368, 0, 0, 0, 285, 1732, 346, 346, 346, 346, 346, 346, 346, 346, 1740",
      /* 23875 */ "346, 346, 1742, 368, 368, 368, 368, 859, 368, 368, 368, 863, 368, 368, 368, 368, 368, 368, 368, 0",
      /* 23895 */ "0, 0, 0, 1688, 0, 0, 0, 0, 1788, 346, 346, 346, 346, 346, 346, 346, 346, 368, 1798, 368, 368, 368",
      /* 23917 */ "368, 368, 368, 862, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1272, 368, 368, 368, 368, 368, 368",
      /* 23937 */ "0, 0, 248, 0, 0, 0, 0, 0, 0, 0, 0, 255, 0, 0, 22528, 24576, 0, 0, 268, 20480, 268, 268, 268, 0, 268",
      /* 23962 */ "284, 298, 298, 298, 298, 0, 298, 0, 219, 0, 0, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284",
      /* 23983 */ "284, 284, 298, 341, 344, 360, 360, 360, 360, 360, 383, 360, 360, 360, 360, 360, 360, 383, 383, 383",
      /* 24003 */ "383, 383, 383, 383, 383, 360, 360, 383, 26800, 0, 0, 400, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 24028 */ "948, 0, 0, 0, 481, 0, 528, 0, 0, 0, 0, 0, 346, 550, 346, 346, 346, 346, 346, 346, 839, 600, 43850",
      /* 24051 */ "785, 603, 368, 368, 368, 368, 368, 368, 890, 368, 368, 368, 368, 368, 368, 368, 0, 900, 573, 346",
      /* 24071 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 0, 368, 368, 368, 368, 873, 368, 368",
      /* 24091 */ "368, 368, 368, 368, 368, 368, 368, 881, 368, 368, 368, 368, 368, 1524, 368, 368, 368, 368, 368, 368",
      /* 24111 */ "368, 368, 368, 368, 1429, 368, 368, 368, 368, 368, 368, 605, 368, 368, 368, 368, 368, 628, 368, 368",
      /* 24131 */ "368, 368, 368, 368, 368, 368, 624, 368, 368, 368, 368, 368, 368, 368, 368, 892, 368, 368, 368, 368",
      /* 24151 */ "368, 0, 0, 664, 0, 0, 666, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 916, 0, 0, 0, 0, 666, 739, 740, 0, 0",
      /* 24180 */ "0, 285, 285, 746, 285, 750, 285, 752, 285, 285, 285, 1355, 285, 285, 285, 1357, 285, 285, 1359, 285",
      /* 24200 */ "285, 285, 285, 285, 990, 285, 285, 285, 285, 285, 285, 285, 996, 285, 285, 285, 0, 776, 0, 785, 346",
      /* 24221 */ "346, 346, 790, 346, 346, 797, 346, 346, 801, 346, 346, 346, 346, 1391, 346, 346, 346, 346, 346, 346",
      /* 24241 */ "346, 346, 346, 346, 346, 813, 346, 346, 815, 346, 368, 856, 368, 368, 860, 368, 368, 368, 368, 368",
      /* 24261 */ "368, 368, 368, 368, 368, 368, 368, 1274, 1275, 368, 368, 368, 368, 871, 368, 368, 368, 368, 368",
      /* 24280 */ "368, 368, 368, 368, 368, 368, 368, 368, 868, 368, 368, 0, 0, 937, 0, 939, 0, 0, 0, 0, 944, 0, 0, 0",
      /* 24304 */ "0, 0, 0, 0, 926, 0, 0, 0, 0, 0, 0, 0, 0, 0, 731, 0, 0, 0, 0, 0, 0, 0, 1181, 0, 285, 285, 285, 285",
      /* 24332 */ "285, 285, 285, 285, 285, 285, 285, 285, 285, 763, 285, 285, 0, 0, 1208, 346, 346, 346, 346, 346",
      /* 24352 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 1385, 346, 1290, 368, 368, 368, 368, 368, 368, 368",
      /* 24371 */ "368, 1296, 368, 368, 368, 368, 368, 0, 0, 0, 0, 1440, 0, 0, 1443, 1444, 0, 0, 903, 0, 909, 0, 0, 0",
      /* 24395 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 958, 0, 0, 0, 0, 346, 346, 1389, 346, 346, 346, 346, 346, 346, 346",
      /* 24420 */ "346, 346, 346, 346, 346, 346, 346, 1592, 1593, 368, 1420, 368, 368, 368, 368, 368, 1426, 368, 368",
      /* 24439 */ "368, 368, 368, 368, 368, 368, 625, 368, 368, 368, 368, 368, 368, 368, 368, 1111, 1112, 368, 368",
      /* 24458 */ "368, 368, 368, 368, 1499, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1507, 346, 346",
      /* 24477 */ "346, 346, 346, 1701, 346, 346, 346, 346, 346, 346, 1707, 346, 346, 346, 346, 346, 1392, 346, 346",
      /* 24496 */ "346, 346, 346, 346, 346, 1397, 346, 346, 368, 1532, 368, 368, 368, 368, 368, 368, 368, 368, 368, 0",
      /* 24516 */ "0, 0, 0, 0, 0, 0, 0, 346, 1642, 285, 285, 285, 285, 285, 285, 1647, 0, 0, 0, 346, 346, 346, 346",
      /* 24539 */ "346, 346, 1238, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1239, 346, 346, 346, 346, 346, 346",
      /* 24558 */ "368, 1680, 368, 368, 368, 368, 368, 1685, 0, 0, 1687, 0, 0, 0, 0, 0, 0, 0, 673792, 555008, 555008",
      /* 24579 */ "555008, 555008, 555008, 555008, 729088, 555008, 0, 0, 1693, 0, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285",
      /* 24599 */ "285, 0, 0, 0, 0, 1488, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1395, 346, 346, 346, 346",
      /* 24620 */ "346, 0, 285, 346, 346, 346, 346, 346, 1736, 346, 1738, 346, 346, 346, 346, 368, 368, 368, 368, 1670",
      /* 24640 */ "1671, 1672, 368, 368, 368, 368, 368, 368, 368, 368, 1751, 368, 0, 0, 0, 1755, 0, 0, 368, 368, 368",
      /* 24661 */ "1746, 368, 1748, 368, 368, 368, 368, 0, 0, 0, 0, 0, 0, 1728, 0, 0, 0, 0, 0, 346, 1820, 346, 346",
      /* 24684 */ "368, 368, 368, 368, 368, 368, 368, 1829, 368, 368, 0, 1832, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 299",
      /* 24707 */ "299, 299, 299, 316, 299, 316, 316, 316, 316, 335, 335, 316, 316, 316, 316, 316, 335, 316, 316, 316",
      /* 24727 */ "335, 316, 316, 316, 316, 316, 299, 342, 345, 361, 361, 361, 361, 361, 384, 361, 361, 361, 361, 384",
      /* 24747 */ "384, 384, 384, 384, 384, 384, 384, 361, 361, 384, 26800, 0, 0, 416, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 24772 */ "223, 223, 0, 71680, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 563629, 73728, 0, 445, 0, 0, 0, 0, 0",
      /* 24799 */ "0, 432, 0, 0, 0, 432, 0, 457, 0, 0, 0, 0, 434, 0, 0, 0, 483, 0, 524, 0, 541, 0, 0, 0, 0, 0, 0, 743",
      /* 24827 */ "285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 762, 285, 285, 764, 285, 285, 285, 502, 285",
      /* 24847 */ "507, 285, 285, 285, 285, 514, 285, 285, 285, 0, 0, 0, 0, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 24868 */ "1496, 346, 346, 0, 432, 0, 0, 487, 0, 530, 547, 0, 346, 551, 346, 346, 346, 346, 568, 574, 576, 346",
      /* 24890 */ "346, 346, 346, 346, 590, 346, 346, 346, 346, 346, 346, 0, 368, 368, 368, 368, 888, 368, 368, 368",
      /* 24910 */ "368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 0, 1784, 0, 1786, 346, 368, 606, 368, 368, 368, 368, 623",
      /* 24932 */ "629, 631, 368, 368, 368, 368, 368, 645, 368, 368, 368, 368, 1078, 368, 368, 368, 368, 368, 368, 368",
      /* 24952 */ "368, 368, 368, 368, 368, 368, 1417, 368, 368, 0, 0, 0, 0, 667, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 24978 */ "897024, 28672, 0, 0, 0, 0, 0, 680, 0, 0, 683, 0, 0, 0, 0, 223, 223, 0, 689, 0, 0, 0, 0, 448, 0, 0",
      /* 25004 */ "0, 0, 0, 0, 455, 0, 0, 0, 0, 0, 0, 239, 0, 239, 0, 0, 0, 0, 0, 0, 0, 285, 285, 285, 285, 984, 285",
      /* 25031 */ "285, 285, 285, 0, 0, 724, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 735, 0, 0, 0, 0, 0, 218, 0, 0, 0, 0, 0, 223",
      /* 25060 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 439, 0, 441, 0, 0, 0, 667, 0, 0, 0, 742, 0, 285, 285, 285, 285, 285",
      /* 25087 */ "285, 285, 285, 285, 1201, 0, 0, 0, 0, 0, 0, 766, 285, 285, 285, 285, 285, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 25112 */ "777, 0, 0, 0, 0, 478, 0, 0, 482, 0, 0, 0, 486, 285, 285, 490, 285, 0, 0, 0, 1365, 0, 0, 346, 346",
      /* 25137 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 0, 368, 689, 777, 0, 785, 346, 346, 789",
      /* 25158 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 1704, 346, 1705, 1706, 346, 346, 346, 346, 368, 368",
      /* 25177 */ "872, 368, 368, 874, 368, 368, 368, 368, 368, 368, 368, 368, 368, 883, 884, 368, 368, 368, 368, 368",
      /* 25197 */ "368, 368, 368, 368, 368, 368, 368, 368, 0, 0, 0, 1541, 0, 0, 903, 0, 0, 0, 909, 0, 0, 0, 0, 0, 0, 0",
      /* 25223 */ "917, 0, 0, 0, 0, 0, 221, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 777, 724, 0, 0, 0, 0, 0, 922, 0, 0, 0, 0",
      /* 25253 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 1137, 0, 0, 988, 285, 285, 285, 285, 285, 992, 285, 285, 285, 285, 285",
      /* 25277 */ "285, 285, 285, 285, 760, 285, 761, 285, 285, 285, 285, 285, 765, 785, 346, 346, 346, 346, 1015, 346",
      /* 25297 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 600, 0, 0, 603, 368, 368, 368, 368, 368, 346, 1026",
      /* 25317 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 1034, 346, 346, 346, 346, 346, 346, 1702, 346, 346",
      /* 25336 */ "346, 346, 346, 346, 346, 346, 346, 1505, 346, 346, 346, 346, 346, 346, 1074, 368, 368, 368, 368",
      /* 25355 */ "368, 368, 368, 368, 368, 368, 368, 1085, 368, 368, 368, 368, 368, 1410, 368, 368, 368, 368, 368",
      /* 25374 */ "368, 368, 368, 368, 368, 895, 368, 368, 368, 0, 0, 0, 0, 0, 1128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 25400 */ "0, 931, 932, 0, 0, 0, 0, 1182, 285, 285, 285, 285, 1186, 285, 285, 285, 285, 285, 285, 285, 285, 0",
      /* 25422 */ "772, 0, 0, 0, 0, 0, 0, 776, 0, 1278, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 25444 */ "368, 368, 368, 1120, 1353, 285, 285, 285, 285, 1356, 285, 285, 285, 285, 285, 285, 285, 285, 285",
      /* 25463 */ "285, 0, 1203, 0, 0, 0, 0, 285, 0, 1363, 0, 0, 0, 0, 1366, 346, 346, 346, 346, 1370, 346, 346, 346",
      /* 25486 */ "346, 368, 1824, 368, 368, 368, 368, 368, 368, 368, 368, 0, 0, 0, 0, 0, 0, 0, 0, 1787, 1387, 346",
      /* 25508 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 1509, 368, 1407, 368, 368",
      /* 25527 */ "368, 368, 368, 368, 368, 368, 368, 1415, 368, 368, 368, 1418, 0, 1472, 0, 0, 0, 0, 285, 285, 285",
      /* 25548 */ "1480, 285, 285, 285, 285, 285, 285, 758, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 994, 285",
      /* 25568 */ "285, 285, 285, 285, 368, 1520, 368, 368, 1523, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 25587 */ "368, 368, 1286, 368, 368, 368, 346, 1582, 346, 346, 346, 346, 346, 346, 1587, 346, 346, 346, 346",
      /* 25606 */ "346, 346, 346, 600, 43850, 785, 603, 368, 368, 368, 849, 368, 368, 1609, 368, 368, 368, 368, 368",
      /* 25625 */ "368, 368, 368, 368, 368, 368, 368, 1621, 368, 368, 368, 368, 1091, 368, 368, 368, 368, 368, 368",
      /* 25644 */ "368, 368, 368, 368, 368, 867, 368, 368, 368, 368, 368, 368, 1681, 368, 368, 368, 368, 0, 0, 0, 0, 0",
      /* 25666 */ "0, 1689, 0, 0, 0, 0, 0, 285, 0, 0, 346, 346, 346, 346, 346, 368, 346, 346, 346, 346, 808, 346, 346",
      /* 25689 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 1662, 346, 346, 346, 346, 1720, 368, 368, 368, 0, 0, 0",
      /* 25710 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 934, 346, 346, 346, 1822, 368, 368, 368, 368, 368, 368, 368",
      /* 25734 */ "368, 368, 1831, 0, 0, 0, 0, 0, 285, 0, 0, 346, 366, 366, 366, 346, 369, 366, 366, 366, 366, 369",
      /* 25756 */ "369, 369, 369, 369, 369, 369, 369, 366, 366, 369, 26800, 0, 0, 1833, 0, 1835, 346, 346, 346, 346",
      /* 25776 */ "346, 346, 1840, 1841, 346, 368, 368, 368, 368, 1861, 368, 1863, 0, 1865, 346, 346, 346, 346, 346",
      /* 25795 */ "346, 346, 1064, 1065, 43850, 1069, 1065, 368, 368, 368, 368, 368, 368, 368, 368, 1846, 1847, 368, 0",
      /* 25814 */ "0, 1849, 0, 346, 346, 346, 346, 346, 346, 1399, 0, 1401, 0, 368, 368, 368, 368, 368, 368, 0, 0, 0",
      /* 25836 */ "1875, 346, 346, 346, 1877, 368, 368, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 300, 300, 300, 300, 317, 300",
      /* 25859 */ "317, 317, 317, 317, 317, 317, 317, 317, 317, 317, 317, 317, 317, 317, 317, 317, 300, 317, 317, 362",
      /* 25879 */ "362, 362, 362, 362, 385, 362, 362, 362, 362, 362, 362, 385, 385, 385, 385, 385, 385, 385, 385, 362",
      /* 25899 */ "362, 385, 26800, 223, 430, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1565, 0, 0, 0, 0, 447, 0, 0, 0",
      /* 25927 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 1136, 0, 0, 0, 0, 665, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22528",
      /* 25957 */ "24576, 0, 0, 0, 785, 346, 346, 346, 346, 346, 794, 346, 346, 346, 346, 346, 346, 346, 1585, 346",
      /* 25977 */ "346, 346, 346, 346, 346, 346, 346, 595, 346, 346, 346, 346, 346, 0, 368, 853, 368, 368, 368, 368",
      /* 25997 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1277, 0, 0, 904, 0, 0, 0, 910, 0, 0, 0, 0, 0",
      /* 26021 */ "0, 0, 0, 0, 0, 1322, 0, 0, 0, 1325, 0, 0, 0, 961, 0, 0, 0, 0, 0, 0, 0, 0, 967, 0, 969, 0, 0, 0, 0",
      /* 26050 */ "0, 285, 0, 0, 363, 363, 363, 363, 363, 386, 363, 363, 363, 363, 386, 386, 386, 386, 386, 386, 386",
      /* 26071 */ "386, 363, 363, 386, 26800, 368, 368, 368, 1090, 368, 368, 368, 368, 368, 368, 1096, 368, 368, 368",
      /* 26090 */ "368, 368, 368, 1093, 368, 368, 368, 368, 368, 368, 368, 368, 368, 1428, 368, 368, 368, 368, 368",
      /* 26109 */ "368, 368, 368, 1104, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 368, 880, 368, 368",
      /* 26129 */ "0, 0, 0, 285, 285, 285, 285, 285, 1187, 285, 285, 285, 285, 285, 285, 285, 771, 0, 0, 0, 0, 0, 0, 0",
      /* 26153 */ "0, 0, 0, 0, 397312, 0, 0, 0, 397312, 0, 0, 0, 1302, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1150, 0, 0",
      /* 26181 */ "0, 346, 346, 1821, 346, 368, 368, 368, 368, 368, 368, 368, 368, 1830, 368, 0, 0, 0, 0, 0, 285, 0, 0",
      /* 26204 */ "365, 365, 365, 365, 365, 388, 365, 365, 365, 365, 388, 388, 388, 388, 388, 388, 388, 388, 365, 365",
      /* 26224 */ "388, 26800, 368, 368, 368, 368, 1873, 368, 0, 0, 0, 346, 346, 346, 346, 368, 368, 368, 368, 1712",
      /* 26244 */ "368, 368, 368, 368, 368, 368, 368, 368, 368, 1537, 368, 0, 0, 0, 0, 0, 232, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 26269 */ "0, 0, 0, 0, 0, 0, 0, 59392, 0, 247, 0, 0, 0, 0, 0, 0, 0, 247, 0, 0, 0, 0, 247, 22528, 24576, 232, 0",
      /* 26296 */ "247, 247, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1178, 0, 0, 0, 0, 0, 0, 785, 346, 346, 346, 346, 346",
      /* 26323 */ "346, 346, 798, 346, 346, 346, 346, 346, 346, 1794, 346, 1796, 368, 368, 368, 368, 368, 368, 368, 0",
      /* 26343 */ "1848, 0, 1850, 346, 346, 346, 346, 346, 368, 368, 857, 368, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 26363 */ "368, 368, 368, 368, 898, 0, 0, 0, 0, 0, 0, 955, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 210, 0, 210, 0, 0",
      /* 26391 */ "0, 0, 269, 20480, 269, 269, 269, 0, 269, 269, 301, 301, 301, 301, 0, 301, 364, 364, 364, 364, 387",
      /* 26412 */ "387, 387, 387, 387, 387, 387, 387, 364, 364, 387, 26800, 498, 285, 285, 285, 285, 285, 285, 285",
      /* 26431 */ "285, 285, 285, 285, 285, 0, 0, 0, 0, 346, 346, 346, 346, 346, 1493, 346, 1495, 346, 346, 1498, 0, 0",
      /* 26453 */ "0, 0, 523, 0, 0, 0, 0, 0, 0, 0, 0, 0, 523, 0, 0, 0, 0, 0, 0, 0, 0, 545, 0, 0, 0, 0, 0, 0, 346, 346",
      /* 26483 */ "346, 346, 563, 346, 346, 346, 346, 1512, 346, 0, 0, 0, 0, 368, 368, 368, 368, 368, 1518, 0, 0, 0",
      /* 26505 */ "906, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1403, 368, 368, 368, 346, 833, 346, 346, 346, 346, 346",
      /* 26529 */ "600, 43850, 785, 603, 368, 368, 368, 368, 368, 368, 1294, 368, 368, 368, 368, 368, 368, 368, 368",
      /* 26548 */ "1120, 0, 0, 0, 0, 977, 0, 0, 285, 285, 285, 285, 285, 285, 285, 285, 285, 1198, 1199, 285, 285, 0",
      /* 26570 */ "0, 0, 0, 0, 1206, 346, 346, 1040, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346",
      /* 26591 */ "346, 1665, 346, 0, 0, 0, 285, 285, 285, 285, 285, 285, 285, 285, 1189, 285, 285, 285, 285, 1196",
      /* 26611 */ "285, 285, 285, 285, 285, 0, 0, 0, 0, 0, 0, 0, 0, 1008, 0, 0, 0, 0, 0, 346, 1221, 1222, 346, 346",
      /* 26635 */ "346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 829, 346, 346, 368, 368, 368, 368, 1435, 0",
      /* 26655 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 223, 0, 0, 0, 0, 0, 0, 0, 20480, 0, 0, 0, 278, 0, 0, 285, 285, 285",
      /* 26683 */ "285, 0, 285, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 346, 368, 368, 368, 368, 368",
      /* 26703 */ "368, 0, 0, 0, 346, 346, 346, 346, 1869, 346, 0, 0, 0, 397571, 0, 0, 0, 0, 0, 0, 0, 397312, 0",
      /* 26726 */ "397312, 0, 397312, 0, 0, 0, 0, 0, 0, 0, 0, 0, 397312, 0, 0, 397312, 0, 0, 0, 0, 0, 0, 0, 0, 0",
      /* 26751 */ "397312, 0, 0, 0, 0, 0, 397312, 0, 0, 397571, 397571, 0, 0, 0, 0, 397312, 397312, 0, 397312, 0, 0, 0",
      /* 26773 */ "0, 0, 0, 397312, 397312, 0, 0, 399360, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1324, 0, 0, 557659",
      /* 26798 */ "557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /* 26810 */ "557056, 557056, 557056, 557056, 0, 0, 0, 20480, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 401726, 0, 0, 0, 0",
      /* 26833 */ "521, 0, 0, 0, 521, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 368, 368, 368, 368, 401726, 401726, 401726",
      /* 26857 */ "401726, 401726, 401726, 401726, 401726, 401726, 401726, 401726, 401726, 401726, 401726, 401726",
      /* 26869 */ "401726, 0, 0, 0, 0, 0, 0, 0, 0, 205, 206, 0, 0, 0, 0, 0, 0, 0, 0, 0, 260, 0, 0, 0, 0, 0, 405504, 0",
      /* 26897 */ "0, 0, 0, 0, 0, 0, 942, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1560, 0, 0, 0, 1564, 0, 0, 0, 0, 0, 0, 405504",
      /* 26925 */ "405504, 405504, 405504, 405504, 405504, 405504, 405504, 405504, 405504, 405504, 405504, 0, 0, 0, 0",
      /* 26940 */ "0, 0, 0, 0, 221, 222, 0, 223, 0, 0, 0, 0, 0, 0, 0, 407552, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1550",
      /* 26969 */ "0, 0, 0, 888832, 557056, 557056, 557056, 557056, 557056, 557056, 841, 0, 0, 844, 557056, 557056",
      /* 26985 */ "557056, 557056, 557056, 557056, 733184, 557056, 557056, 557056, 557056, 557056, 557056, 557056",
      /* 26997 */ "686080, 0, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200, 51200",
      /* 27011 */ "51200, 51200, 51200, 51200, 0, 0, 0, 0, 0, 0, 0, 0, 228, 0, 230, 0, 0, 0, 0, 207, 0, 0, 825344, 0",
      /* 27035 */ "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 679936"
    };
    String[] s2 = java.util.Arrays.toString(s1).replaceAll("[ \\[\\]]", "").split(",");
    for (int i = 0; i < 27047; ++i) {TRANSITION[i] = Integer.parseInt(s2[i]);}
  }

  private static final int[] EXPECTED = new int[3694];
  static
  {
    final String s1[] =
    {
      /*    0 */ "104, 112, 120, 128, 136, 151, 159, 174, 197, 182, 143, 166, 190, 215, 223, 231, 239, 254, 627, 473",
      /*   20 */ "322, 792, 204, 783, 792, 771, 504, 792, 792, 326, 269, 277, 285, 293, 308, 316, 334, 342, 574, 246",
      /*   40 */ "350, 358, 366, 374, 382, 390, 398, 406, 414, 422, 430, 438, 446, 460, 678, 559, 481, 489, 497, 261",
      /*   60 */ "512, 520, 528, 536, 544, 552, 567, 582, 590, 757, 598, 606, 614, 622, 639, 647, 655, 663, 764, 671",
      /*   80 */ "686, 694, 300, 702, 710, 718, 726, 734, 742, 750, 452, 792, 471, 792, 781, 792, 791, 631, 793, 773",
      /*  100 */ "207, 467, 792, 801, 803, 807, 814, 809, 813, 815, 819, 823, 827, 831, 835, 839, 844, 1074, 843, 1948",
      /*  120 */ "849, 2071, 1776, 844, 844, 1850, 888, 855, 860, 844, 1546, 844, 1337, 844, 1774, 911, 914, 844, 887",
      /*  139 */ "977, 977, 939, 959, 959, 902, 844, 844, 909, 922, 977, 930, 856, 864, 844, 844, 1397, 844, 1774, 911",
      /*  159 */ "915, 977, 977, 977, 870, 959, 959, 959, 969, 844, 844, 947, 977, 924, 959, 882, 844, 844, 1289, 844",
      /*  179 */ "1775, 913, 976, 844, 1962, 911, 886, 977, 977, 892, 959, 903, 844, 951, 977, 958, 970, 1963, 977",
      /*  198 */ "977, 898, 959, 959, 960, 865, 844, 844, 1039, 844, 844, 844, 844, 844, 1699, 844, 844, 967, 942, 964",
      /*  218 */ "926, 953, 873, 954, 974, 925, 1851, 895, 936, 933, 981, 1511, 985, 988, 996, 993, 989, 1000, 1004",
      /*  237 */ "1008, 1012, 1016, 2182, 844, 1193, 1020, 844, 1727, 844, 844, 1215, 1269, 844, 1244, 2151, 1255",
      /*  254 */ "1232, 844, 844, 844, 844, 844, 1025, 844, 844, 1838, 1632, 2007, 1321, 1638, 1642, 1059, 1492, 1078",
      /*  272 */ "1082, 1086, 1089, 1093, 1096, 1100, 844, 1105, 1110, 1117, 1123, 1162, 1307, 1130, 1134, 1476, 844",
      /*  289 */ "1144, 1148, 1812, 2143, 1555, 1119, 1155, 844, 1161, 1898, 1166, 844, 844, 1887, 2018, 1807, 2112",
      /*  306 */ "2052, 2059, 844, 1760, 844, 844, 844, 1174, 844, 2143, 943, 1985, 1178, 1106, 1634, 1186, 844, 844",
      /*  324 */ "844, 1745, 844, 844, 844, 844, 1050, 1441, 1101, 844, 1693, 1192, 844, 844, 1701, 1231, 844, 1197",
      /*  342 */ "1206, 1213, 1219, 1223, 844, 844, 1035, 1187, 1259, 1263, 844, 1266, 844, 1767, 2142, 1274, 1278",
      /*  359 */ "1283, 1318, 1457, 1288, 1293, 1279, 1034, 1297, 1302, 1209, 2019, 1306, 1311, 1801, 2196, 2054, 1315",
      /*  376 */ "2055, 1325, 1344, 1334, 1331, 1341, 1348, 844, 844, 844, 2043, 1467, 1354, 1378, 1358, 1362, 1366",
      /*  393 */ "1370, 1372, 844, 2042, 1654, 1376, 844, 1934, 1382, 1659, 1386, 1390, 1394, 2060, 1401, 844, 1407",
      /*  410 */ "2001, 1527, 1932, 1414, 1419, 1073, 1423, 1202, 2092, 1429, 844, 1665, 1435, 844, 844, 1157, 1753",
      /*  427 */ "1527, 1565, 1482, 1140, 1188, 1073, 2085, 1439, 844, 844, 1445, 1520, 844, 844, 1466, 1449, 1182",
      /*  444 */ "1415, 1071, 2203, 2078, 1456, 844, 844, 1461, 844, 844, 844, 2193, 917, 844, 844, 2200, 1465, 1471",
      /*  462 */ "1480, 1795, 1486, 1683, 1490, 844, 1027, 844, 844, 844, 1597, 844, 844, 844, 844, 844, 844, 1410",
      /*  480 */ "844, 1735, 1520, 1544, 1550, 1559, 1520, 1563, 1576, 1569, 1575, 1580, 2099, 1586, 1711, 1589, 1712",
      /*  497 */ "1590, 1594, 1609, 1613, 1620, 1624, 1628, 844, 1044, 844, 844, 844, 844, 844, 1452, 1645, 1649, 1652",
      /*  515 */ "844, 1837, 1658, 1663, 1053, 1170, 1669, 1721, 1673, 1677, 1818, 1994, 1681, 844, 1901, 1788, 877",
      /*  532 */ "1168, 1687, 845, 1691, 1571, 1697, 844, 1705, 844, 844, 1431, 1709, 844, 1844, 2118, 877, 1150, 1716",
      /*  550 */ "845, 1720, 1725, 844, 1731, 844, 844, 845, 1739, 844, 1055, 1519, 1021, 1525, 1531, 1553, 1538, 844",
      /*  568 */ "1751, 2179, 844, 1757, 1907, 1028, 844, 1284, 1230, 844, 1236, 2189, 1521, 1240, 1764, 844, 844, 866",
      /*  586 */ "1733, 844, 1505, 1771, 1605, 1409, 1780, 844, 1502, 844, 844, 1327, 1792, 1270, 1799, 1805, 1825",
      /*  603 */ "1600, 845, 1811, 1816, 1822, 1029, 1843, 1603, 1831, 1835, 1604, 1842, 1848, 1865, 1855, 1868, 1862",
      /*  620 */ "1869, 1873, 1880, 1884, 1891, 1895, 1905, 844, 844, 844, 1033, 844, 844, 844, 844, 916, 844, 844",
      /*  638 */ "844, 905, 1200, 1858, 1911, 1915, 1919, 1923, 1927, 1930, 844, 904, 1938, 1942, 1946, 1475, 1952",
      /*  655 */ "1956, 1961, 1967, 1747, 2070, 1971, 844, 2012, 1180, 1978, 1473, 1046, 844, 1982, 1582, 2025, 1998",
      /*  672 */ "1978, 2205, 1046, 844, 2005, 2011, 844, 1496, 844, 1151, 1515, 1065, 2172, 2036, 1402, 2016, 844",
      /*  689 */ "844, 1113, 2023, 844, 2029, 2112, 2033, 2105, 851, 2040, 844, 1350, 2047, 2064, 844, 1403, 2048, 844",
      /*  707 */ "1349, 2068, 1827, 2158, 878, 1062, 844, 2075, 844, 1126, 1425, 2134, 1052, 1054, 1540, 844, 2082",
      /*  724 */ "2089, 1226, 844, 2096, 1974, 2103, 1247, 2109, 2116, 2141, 2122, 1250, 2126, 1251, 2127, 1137, 2131",
      /*  741 */ "2138, 1616, 1742, 2147, 2150, 844, 844, 1298, 2155, 1534, 1068, 2162, 2166, 2169, 2176, 2186, 844",
      /*  758 */ "1508, 876, 1499, 918, 1040, 1787, 844, 1349, 1989, 844, 844, 1876, 1993, 844, 1451, 844, 844, 844",
      /*  776 */ "844, 844, 844, 844, 1782, 844, 1782, 844, 844, 844, 844, 844, 844, 1957, 844, 1783, 844, 844, 844",
      /*  795 */ "844, 844, 844, 844, 844, 1028, 844, 1559, 2209, 3285, 3153, 2223, 2220, 2234, 2245, 2245, 2564, 2232",
      /*  813 */ "2243, 2245, 2245, 2245, 2245, 2240, 2244, 2236, 2244, 2250, 2246, 2245, 2254, 2258, 2262, 2275, 2277",
      /*  830 */ "2272, 2269, 2266, 2890, 2281, 2285, 2290, 2289, 2294, 2209, 3285, 2302, 2309, 2836, 2312, 2312, 2312",
      /*  847 */ "2312, 2313, 2322, 2329, 2312, 2312, 2215, 3534, 2360, 2411, 2411, 2411, 2388, 2411, 2372, 3540, 2392",
      /*  864 */ "2391, 2392, 2312, 2312, 2312, 2325, 2427, 3236, 2407, 2411, 2361, 2312, 2699, 2883, 2312, 2312, 2337",
      /*  881 */ "2312, 2413, 2391, 2391, 2310, 3461, 2699, 2427, 2427, 2427, 2354, 2417, 2419, 2421, 2411, 2361, 2699",
      /*  898 */ "2427, 2417, 2420, 2410, 2411, 2363, 2312, 2312, 2312, 2330, 3472, 2312, 3456, 3458, 3458, 3458, 3458",
      /*  915 */ "3460, 2312, 2312, 2312, 2315, 2312, 2316, 3461, 3235, 2427, 2427, 2411, 2411, 2362, 2312, 2427, 3237",
      /*  932 */ "2409, 2411, 2362, 3235, 2430, 2361, 3234, 2427, 3236, 2356, 2411, 2312, 2312, 2312, 2336, 2312, 3458",
      /*  949 */ "3458, 2699, 3457, 3458, 2699, 2427, 2427, 2430, 2411, 2430, 2411, 2411, 2411, 2411, 2412, 2425, 2427",
      /*  966 */ "2427, 2429, 2411, 2411, 2411, 2361, 2312, 2312, 2361, 2312, 3234, 2427, 2427, 2427, 2427, 2447, 2434",
      /*  983 */ "2444, 2436, 2483, 2539, 2489, 2501, 2501, 2501, 2501, 2470, 2493, 2497, 2500, 2501, 2501, 2536, 3657",
      /* 1000 */ "3654, 2468, 2501, 3108, 3064, 2542, 3656, 2505, 2507, 2509, 2513, 2517, 2519, 2521, 2533, 2485, 3614",
      /* 1017 */ "2546, 2550, 2554, 2580, 2312, 2312, 2312, 2347, 2610, 2601, 2312, 2312, 2314, 2312, 2312, 2313, 3561",
      /* 1034 */ "2312, 2312, 2312, 2366, 2298, 3446, 2312, 2312, 2312, 2438, 2312, 3445, 2312, 2312, 2337, 3492, 2312",
      /* 1051 */ "3541, 2312, 2312, 2339, 2312, 2312, 2312, 2972, 2581, 2614, 3222, 2312, 2216, 2312, 2312, 2305, 2312",
      /* 1068 */ "2727, 2312, 2728, 2317, 3027, 2988, 2312, 2312, 2312, 2789, 2623, 2631, 2638, 2646, 2710, 2653, 2655",
      /* 1085 */ "2659, 2663, 2666, 2671, 2670, 2675, 2675, 2675, 2679, 2679, 2683, 2312, 2311, 2312, 2582, 2855, 3040",
      /* 1102 */ "3044, 2312, 2312, 2633, 2312, 2312, 2312, 2556, 3286, 2692, 2698, 2312, 2317, 2745, 3509, 2707, 2312",
      /* 1119 */ "2714, 2794, 2312, 2753, 2649, 3045, 2722, 2312, 2317, 3587, 3029, 3224, 3045, 3336, 2740, 2312, 3387",
      /* 1136 */ "2787, 2312, 2317, 3607, 2312, 2317, 3624, 3010, 2312, 3541, 2581, 2854, 3039, 3043, 2312, 2312, 2345",
      /* 1153 */ "2312, 2830, 2555, 2760, 2312, 2312, 2345, 3626, 2556, 2312, 2312, 2312, 2557, 2772, 2785, 2312, 2312",
      /* 1170 */ "2346, 2312, 2555, 3152, 2640, 3037, 3041, 3045, 2555, 2799, 3045, 2312, 2829, 2312, 2921, 2330, 2774",
      /* 1187 */ "2778, 2618, 2312, 2312, 2228, 2779, 2312, 2312, 2312, 2571, 3690, 2343, 3628, 2312, 2331, 2312, 2312",
      /* 1204 */ "3305, 2312, 2794, 2312, 2703, 3118, 2589, 2312, 2382, 2809, 3045, 2312, 2312, 2367, 2776, 2312, 3384",
      /* 1221 */ "2312, 2813, 2297, 2777, 2781, 2312, 2338, 2312, 2339, 2824, 3042, 2312, 2312, 2312, 2586, 3690, 2343",
      /* 1238 */ "2336, 3095, 2364, 2368, 2777, 2781, 2870, 3041, 3045, 2312, 2340, 2312, 2312, 2347, 2312, 2312, 3606",
      /* 1255 */ "2342, 3093, 2312, 3118, 2574, 2592, 2312, 2835, 2366, 2818, 2781, 2312, 2365, 2817, 2780, 2312, 2312",
      /* 1272 */ "2312, 2638, 2342, 3095, 2312, 2703, 2576, 2312, 2859, 2816, 2618, 2820, 2312, 2312, 2312, 2640, 2827",
      /* 1289 */ "2312, 2312, 2312, 2642, 2597, 2312, 3119, 2575, 2866, 2312, 2312, 2312, 2727, 2693, 2826, 2312, 3095",
      /* 1306 */ "2384, 2312, 2312, 2312, 2736, 3166, 3093, 2312, 3097, 2693, 2827, 3096, 2312, 2367, 2819, 2618, 3627",
      /* 1323 */ "2312, 2694, 3168, 3558, 2312, 2312, 2439, 3309, 2942, 3096, 2312, 3559, 2312, 3558, 2312, 2377, 2641",
      /* 1340 */ "3680, 3093, 3558, 2312, 3560, 2312, 3176, 3169, 3094, 2312, 2312, 2312, 2743, 3546, 2700, 2874, 2878",
      /* 1357 */ "2592, 3438, 2830, 2312, 2894, 2898, 2911, 2902, 2906, 2911, 2909, 2909, 2911, 2913, 2915, 2915, 2915",
      /* 1374 */ "2919, 2312, 3445, 3045, 2312, 2730, 2831, 2887, 2954, 2312, 3359, 2925, 2926, 2312, 2941, 2338, 3358",
      /* 1391 */ "2946, 2312, 2952, 2312, 3494, 2958, 2312, 2396, 2851, 3678, 2959, 2312, 2312, 2312, 2744, 3574, 2312",
      /* 1408 */ "3502, 2794, 2312, 2312, 2312, 3117, 2330, 3285, 2312, 2968, 2312, 2312, 3622, 2977, 2457, 3342, 2985",
      /* 1425 */ "2618, 2312, 2312, 2730, 3275, 3023, 2312, 2312, 2527, 2718, 2995, 3000, 3276, 2618, 3019, 3023, 2312",
      /* 1442 */ "2312, 2582, 2855, 2839, 3015, 3569, 3020, 3006, 3633, 2312, 2312, 2608, 2312, 2312, 3021, 2312, 2312",
      /* 1459 */ "2312, 2870, 2970, 3643, 3647, 3022, 2312, 3501, 2312, 2312, 2830, 2312, 3005, 2964, 2312, 2312, 2687",
      /* 1476 */ "2312, 2312, 2312, 3388, 2829, 2920, 2312, 3284, 2312, 2968, 2212, 2312, 2312, 3069, 3647, 3023, 2312",
      /* 1493 */ "2312, 2725, 2603, 3053, 3646, 3058, 2312, 2402, 2226, 2312, 2440, 3262, 2312, 2453, 2458, 2312, 2453",
      /* 1510 */ "3340, 2312, 2463, 2474, 2466, 3006, 3045, 2312, 2732, 3073, 2618, 2312, 2312, 2312, 2835, 3005, 2592",
      /* 1527 */ "2312, 2312, 2731, 2312, 2312, 3285, 2312, 2729, 2728, 2312, 3611, 2971, 3079, 2312, 2312, 2768, 3215",
      /* 1544 */ "2830, 2450, 2312, 2312, 2788, 2836, 2305, 2312, 2728, 2318, 3287, 2312, 2312, 2749, 2312, 3287, 2312",
      /* 1561 */ "2527, 3086, 3084, 3080, 2312, 2312, 2788, 3335, 2727, 2727, 2312, 2527, 3228, 2608, 3090, 2312, 2312",
      /* 1578 */ "2312, 2930, 2529, 3092, 2312, 2312, 2795, 3539, 3686, 2312, 2528, 3686, 2312, 3684, 2312, 2312, 2928",
      /* 1595 */ "3683, 3687, 2313, 2312, 2312, 2312, 2478, 3352, 2312, 2479, 2312, 2312, 2312, 3329, 3685, 2312, 2312",
      /* 1612 */ "2929, 3102, 2312, 3101, 2312, 2595, 2317, 2344, 2927, 2523, 2312, 2525, 2928, 2524, 2313, 3687, 3106",
      /* 1629 */ "2526, 2304, 3244, 2312, 3627, 2312, 2312, 2805, 2861, 3061, 3116, 2848, 3123, 3127, 3129, 3133, 3137",
      /* 1646 */ "3137, 3137, 3138, 3142, 3142, 3142, 3142, 3143, 2312, 2312, 2830, 2701, 2639, 2312, 2312, 2312, 2937",
      /* 1663 */ "3147, 2882, 2312, 2312, 2837, 2841, 3453, 2312, 2837, 3157, 3161, 2640, 3173, 3181, 2837, 3189, 2837",
      /* 1680 */ "2399, 3196, 3201, 2312, 2312, 2837, 3054, 2312, 3268, 2312, 3206, 3219, 3231, 2312, 2312, 2862, 2775",
      /* 1697 */ "3241, 3249, 2315, 2312, 2312, 2312, 2869, 3038, 2313, 2717, 3255, 3260, 3256, 3261, 2312, 2312, 2927",
      /* 1714 */ "3682, 3686, 2312, 3267, 2312, 3206, 3272, 2312, 2312, 2312, 2990, 3280, 3291, 2312, 2312, 2948, 2312",
      /* 1731 */ "2312, 3297, 3582, 3262, 2312, 2312, 2970, 3078, 3298, 3583, 3263, 2312, 2596, 2594, 2312, 2607, 2312",
      /* 1748 */ "2312, 2317, 3466, 3208, 3212, 2312, 2312, 3004, 2963, 3330, 2312, 3206, 2312, 2619, 2773, 2786, 2324",
      /* 1765 */ "3581, 3309, 2312, 2640, 2845, 3043, 2341, 3324, 2930, 2312, 2693, 3458, 3458, 3458, 2335, 3334, 2312",
      /* 1782 */ "2312, 2316, 2312, 2312, 2312, 3347, 2312, 2312, 2312, 3148, 2313, 3346, 3263, 2312, 2728, 2312, 3034",
      /* 1799 */ "2312, 3325, 2312, 2312, 3097, 2312, 2312, 3330, 2312, 2312, 3184, 2730, 3351, 2312, 2312, 2609, 2634",
      /* 1816 */ "2639, 3324, 2312, 2312, 3195, 3200, 2402, 2829, 2312, 2314, 2314, 2312, 2312, 3185, 2731, 2312, 3363",
      /* 1833 */ "2555, 2476, 2312, 3245, 2312, 2312, 3208, 2456, 2639, 2313, 3356, 2312, 2312, 2312, 3210, 3364, 2403",
      /* 1850 */ "2312, 2312, 3235, 2427, 2429, 2312, 3312, 3629, 2312, 2731, 3425, 3596, 2312, 3629, 2312, 3049, 2312",
      /* 1867 */ "2312, 3049, 2312, 3047, 2312, 2312, 3630, 3046, 3045, 2312, 2744, 3508, 2981, 3048, 2312, 2312, 2336",
      /* 1884 */ "3048, 2312, 3047, 2312, 2744, 3547, 3552, 3629, 3632, 2312, 3045, 3630, 3633, 3202, 2312, 2764, 3377",
      /* 1901 */ "2312, 2330, 2455, 2459, 3631, 3202, 2312, 2312, 3302, 2312, 3368, 3374, 2348, 3381, 3392, 2312, 3396",
      /* 1918 */ "3399, 3413, 3403, 3407, 3413, 3411, 3411, 3413, 3417, 3419, 3419, 3419, 3419, 3423, 2312, 2312, 3315",
      /* 1935 */ "2312, 3282, 3286, 3472, 2312, 2312, 2732, 2312, 3595, 3480, 3484, 2312, 3530, 2312, 2312, 3370, 2312",
      /* 1952 */ "3429, 2312, 2312, 3251, 3435, 2312, 2312, 2312, 3445, 3442, 2312, 2312, 2312, 3456, 3459, 2312, 3687",
      /* 1969 */ "2756, 3450, 2742, 3467, 3516, 2312, 2766, 3601, 2947, 3477, 3481, 3485, 2373, 2312, 3489, 2566, 2312",
      /* 1986 */ "2793, 2312, 2703, 3507, 2980, 3513, 2618, 3514, 2312, 2312, 2312, 3462, 3471, 3045, 2829, 2312, 2830",
      /* 2003 */ "2702, 2963, 2214, 3520, 2312, 2312, 3431, 3112, 3539, 2312, 2312, 2312, 3471, 3508, 3524, 3319, 2618",
      /* 2020 */ "2312, 2312, 2380, 3525, 3320, 2312, 2312, 3499, 2312, 2312, 3183, 3473, 2731, 3529, 3118, 3286, 2312",
      /* 2037 */ "2837, 2973, 3074, 2312, 3538, 2312, 2312, 3503, 2312, 2312, 3551, 3318, 3516, 2312, 2312, 3556, 2688",
      /* 2054 */ "2312, 2312, 3559, 2312, 2693, 3293, 2312, 2312, 2312, 3495, 3565, 2567, 2788, 2930, 3573, 3317, 3515",
      /* 2071 */ "2312, 2312, 2312, 3458, 2742, 3588, 3030, 2312, 2837, 3014, 3646, 2767, 3214, 2312, 2312, 2838, 3014",
      /* 2088 */ "3568, 2312, 3672, 3484, 2312, 2840, 2994, 2999, 3640, 3214, 2312, 2312, 2928, 2625, 2528, 2312, 3592",
      /* 2105 */ "2312, 2337, 3627, 2312, 2766, 3601, 2312, 2312, 2931, 3479, 3483, 2312, 3640, 2459, 2312, 2312, 3148",
      /* 2122 */ "3600, 2312, 2312, 3600, 3606, 2312, 2594, 2348, 2348, 3605, 2345, 2312, 2312, 2932, 3675, 2315, 3605",
      /* 2139 */ "2312, 2594, 2348, 2312, 2312, 2312, 3688, 2341, 2727, 2727, 3620, 2627, 2312, 2312, 2312, 3689, 2727",
      /* 2156 */ "2727, 2312, 2312, 2933, 3482, 3578, 2350, 2349, 2313, 3618, 3637, 3651, 3661, 3662, 3661, 3666, 2312",
      /* 2173 */ "3068, 2312, 3069, 2625, 2312, 2312, 2342, 2881, 2930, 2312, 2617, 2339, 2561, 3669, 2312, 2312, 2312",
      /* 2190 */ "3119, 2574, 2801, 2312, 3191, 2312, 2312, 3164, 2828, 3096, 3542, 2312, 2312, 2312, 3177, 2312, 2312",
      /* 2207 */ "2686, 2312, 4096, 524288, 2097152, 4194304, -2147483648, 0, 0, 4, 8, 1024, 0, 131074, 131088",
      /* 2222 */ "134283264, 65536, 65536, 65536, 134217728, 268435456, 0, 0, 8, 16384, 131088, 268566528, 268566528",
      /* 2235 */ "1073872896, 131072, 131072, 131072, 131102, 131088, 16908288, 268566528, 1073872896, -2147352576",
      /* 2245 */ "131072, 131072, 131072, 131072, 8768, 1073872896, -2147352576, 134227136, 10560, 131072, 16908288",
      /* 2256 */ "147456, 147456, 147472, 268582912, 386007040, 268582912, 386007040, 386007040, -1761476608",
      /* 2265 */ "-1761476608, 386007040, 386023424, 1459748864, 386007040, 117571584, 1459748864, 386007040",
      /* 2273 */ "117571584, 117571584, 84017152, 84017152, 84017152, 84017152, 117571584, 84017152, 98304, 1212448",
      /* 2283 */ "163872, 268599328, -2147319776, 163872, -1073577952, -2147319776, -1072529346, -1072529346",
      /* 2291 */ "-1072529346, -1072529346, -1055752130, 386039840, 386039840, -955088834, 4096, 65536, 393216",
      /* 2300 */ "1048576, 2097152, 0, 65536, 131072, 0, 0, 8388608, 536870912, 2, 16, 16, 0, 0, 0, 0, 1, 0, 0, 0, 2",
      /* 2321 */ "-2147483648, 128, 8256, 0, 0, 1, 4, 1024, 256, 0, 0, 0, 4, 0, 16384, 67108864, 0, 0, 0, 8, 0, 0, 0",
      /* 2344 */ "32, 0, 0, 0, 64, 0, 0, 0, 70, 70, 32768, 32800, 1048576, 1081344, 1048608, 1081344, 1081376, 1081344",
      /* 2362 */ "1081344, 1081344, 0, 0, 0, 192, 1024, 4096, 262144, 1048576, 12, 0, 0, 4096, 2097152, 0, 12, 8, 0, 0",
      /* 2382 */ "128, 1024, 4096, 2097152, -2147483648, 0, 1081344, 1081344, 0, 16, 16, 16, 16, 0, 0, 8, 8, 0, 0, 216",
      /* 2402 */ "0, 0, 256, 67108864, 0, 32, 1048576, 1048608, 1048608, 1081344, 1081344, 1081344, 1081344, 16, 16",
      /* 2417 */ "32, 32, 32, 32, 1048608, 1048608, 1048608, 1048608, 0, 16384, 32768, 32768, 32768, 32768, 1081344",
      /* 2432 */ "1081344, 1081344, 1081344, 1081344, 32768, 1081344, 0, 0, 1, 1024, 6144, 12582912, 32768, 1081344",
      /* 2446 */ "32768, 1081344, 0, 32768, 32768, 1024, 2097152, 0, 0, 1024, 2048, 262144, 1048576, 4194304, 16777216",
      /* 2461 */ "0, 0, 512, 67108864, 134217728, -2147483648, 1, 8, 8, 10, 8, 8, 152, 0, 0, 67108864, 134217728, 0, 0",
      /* 2480 */ "1, 6144, 1610612736, 1, 134234112, 8, 8, 10, 56, 65544, 131080, 262152, 67108872, 12, 152",
      /* 2495 */ "1073741848, 262280, 393224, 131080, 131080, 262152, 8, 8, 8, 8, 1032335850, 10, 1032335850",
      /* 2508 */ "1032335850, 1032585720, 1032585720, 2106327544, 1032585720, 1032598008, 2106458616, 1032598008",
      /* 2516 */ "1032598010, 1032663544, 2106458618, 1032598010, 1032663546, 1032598010, 1032598010, 0, 0, 1, 131072",
      /* 2527 */ "0, 0, 1, 2, 32, 512, 0, 8, 134234112, 8, 32776, 8, 10, 24, 40, 136, 262152, 131208, 262296, 35142990",
      /* 2547 */ "35142990, 35143006, 1108884814, 35142990, 1108884830, 1032598010, 1032663546, 1067191770, 0, 0, 0",
      /* 2558 */ "256, 393216, 0, 0, 16384, 65536, 131072, 131072, 131072, 262144, 8388608, 536870912, 0, 0, 8, 32768",
      /* 2574 */ "0, 0, 1024, 262144, 2097152, 1073741824, 12, 0, 0, 0, 512, 8192, 8192, 25165824, 805306368, 0, 0",
      /* 2591 */ "1024, 2097152, 1073741824, 0, 0, 2, 32, 0, 0, 268435456, 59768832, 0, 0, 512, 0, 512, 1048576",
      /* 2608 */ "2097152, 0, 0, 0, 2048, 34603008, 0, 2048, 0, 512, -2147483648, 0, 0, 0, 448, 2080, 512, 0, 0, 2, 2",
      /* 2629 */ "2, 2, 0, 2592, 2048, 0, 1207959552, 0, 0, 2048, 16777216, 0, 0, 0, 8192, 64, 0, 1249927168",
      /* 2647 */ "1249927168, 67108864, 0, 0, 1024, 69795840, 69206528, 1143538696, 1143407872, 1143407872, 393476",
      /* 2658 */ "1143407872, 393476, 393476, 17170692, 1267097860, 1143014664, 1143014664, 1143014664, 1143276808",
      /* 2667 */ "1143407880, 1143407884, 1143016744, 1143407884, 1143407884, 1143407884, 1143407884, 1143016744",
      /* 2675 */ "-461957696, -461957696, -461957696, -461957696, -461957695, -461957695, -461957695, -461957695",
      /* 2683 */ "-461957684, -461957684, -461957687, 0, 0, 1024, 536870912, 0, 0, 2080, 0, 0, 0, 16384, 0, 2592, 0, 0",
      /* 2701 */ "0, 32768, 0, 1024, 0, 0, 0, 24576, 41943040, 67108864, 69206016, 69206016, 268435458, 0, 0, 69206016",
      /* 2717 */ "2, 4, 80, 128, 1536, 0, 1280, 69664768, 1073741824, 536870912, 0, 0, 2, 0, 0, 0, 134217728, 0, 0, 0",
      /* 2737 */ "17170432, 0, 59113472, 69664768, 3360, 0, 0, 2, 4, 8, 96, 128, 0, 0, 41943040, 67108864, 1024",
      /* 2754 */ "589824, 1073741824, 0, 0, 3336, 1441792, 1024, 458752, 69206016, 1073741824, 0, 58720256, 0, 0, 2, 8",
      /* 2770 */ "96, 4194304, 448, 1024, 4096, 458752, 1048576, 2097152, 4194304, 67108864, 536870912, 1073741824",
      /* 2782 */ "-2147483648, 0, 0, 1048576, 73400320, -536870912, 0, 0, 0, 262144, 262144, 2, 268435456, 0, 0, 0",
      /* 2798 */ "131072, 1024, 458752, 2097152, 67108864, 1073741824, 0, 0, 25165824, 327680, 458752, 65536, 393216",
      /* 2811 */ "2097152, 67108864, 0, 262144, 192, 1024, 4096, 262144, 2097152, 4194304, 536870912, 1073741824",
      /* 2823 */ "-2147483648, 16384, 524288, 2097152, 8388608, 33554432, 134217728, 0, 0, 0, 524288, 0, 0, 262144, 0",
      /* 2838 */ "0, 0, 3, 8, 48, 192, 768, 16384, 2097152, 8388608, 16777216, 16779264, 4194368, 0, 0, 8192, 8192",
      /* 2855 */ "16384, 32768, 65536, 524288, 262144, 0, 0, 192, 256, 1024, 4096, 262144, 2097152, -2147483648, 0, 0",
      /* 2871 */ "8192, 16384, 2097152, 1024, 268435456, 0, 268435456, 0, 524352, 1024, 32768, 16384, 65536",
      /* 2884 */ "-2147483648, 8388608, 0, 64, 32768, 268435456, 32768, 32800, 98336, 163872, 4456448, 0, 4456448",
      /* 2897 */ "4456448, 4456704, 545259524, 4456448, 4458882, -2096082936, -2091624054, -2096081912, -2096082936",
      /* 2906 */ "-2095558648, -2095558648, 55859594, -2091624054, -1823155830, -2091624054, -2091624054, -2091624054",
      /* 2914 */ "-2091624054, -1889567749, -1889567749, -1889567749, -1889567749, -1889567749, 0, 0, 0, 4194304, 0",
      /* 2925 */ "22020096, -2113929216, 0, 0, 0, 8388608, 0, 0, 0, 16777216, 256, 1048576, 0, 8, 20480, 17825792",
      /* 2941 */ "21504, 0, 0, 0, 33554432, 22020096, 33554432, 0, 0, 0, 40894464, 55680, -1845493760, 0, 0, 2, 2432",
      /* 2958 */ "983040, 22020096, -1912602624, 0, 0, 0, 2097152, 0, 1073741824, 0, 2, 128, 0, 0, 3, 48, 64, 512",
      /* 2976 */ "16384, 256, 2048, 4096, 16384, 32768, 131072, 786432, 4194304, 16384, 1048576, 16777216, 33554432",
      /* 2989 */ "-2147483648, 0, 0, 3, 144, 768, 2048, 4096, 8192, 16384, 16384, 458752, 524288, 1048576, 4194304, 0",
      /* 3005 */ "524288, 32768, 1024, 0, 2097152, 16384, 262144, 4194304, 33554432, 48, 64, 128, 768, 2048, 262144",
      /* 3020 */ "524288, 4194304, 33554432, 134217728, -2147483648, 0, 0, 128, 256, 16384, 4194304, 16777216",
      /* 3032 */ "33554432, -2147483648, 0, 2, 256, 16384, 65536, 524288, 2097152, 8388608, 16777216, 33554432",
      /* 3044 */ "134217728, 1073741824, 0, 0, 0, 4096, 1073741824, 0, 0, 3, 48, 64, 768, 8192, 4194304, 134217728",
      /* 3060 */ "-2147483648, 0, 0, 16384, 8, 41418752, 136, 136, 2, 16384, -2147483648, 0, 0, 16384, 196608, 524288",
      /* 3076 */ "134217728, -2147483648, 64, 512, 196608, 524288, -2147483648, 0, 1, 2, 48, 512, 196608, 524288, 32",
      /* 3091 */ "512, 196608, 0, 0, 0, 268435456, 0, 0, 128, -2147483648, 0, 1, 512, 131072, 0, 0, 8388608, 0, 1, 8",
      /* 3111 */ "8, 1024, 16384, -2147418112, 8388608, 16384, 1048576, 0, 0, 0, 1024, 0, 4194368, 4194368, 4194368",
      /* 3126 */ "469762304, 4194368, 4718656, 6824659, 6824659, 2108051, 6824659, 2106003, 2107027, 3154579, 19933843",
      /* 3137 */ "6824659, 6824659, 6824659, 6824659, 6824667, 1659518679, 1659518679, 1659518679, 1659518679, 0, 0",
      /* 3148 */ "32, 0, 32768, 16384, 469762048, 0, 0, 0, 536936448, 208, 512, 8192, 6815744, 512, 10240, 2097152, 0",
      /* 3165 */ "0, 16384, 8388608, 33554432, 134217728, 0, 268435456, 0, 1536, 0, 3145728, 0, 0, 16384, 33554432",
      /* 3180 */ "-2147483648, 0, 19922944, 0, 0, 4, 16, 1073741824, 0, 208, 6815744, 0, 0, 4, 64, 7, 208, 1536",
      /* 3198 */ "145408, 15204352, 15204352, 1644167168, 0, 0, 0, 1073741824, 524288, 0, 0, 0, 4, 1024, 2048, 1048576",
      /* 3214 */ "4194304, 16777216, 33554432, 0, 0, 2, 80, 128, 512, 2048, 1207959552, 0, 1280, 69271552, 16, 128",
      /* 3230 */ "512, 8192, 524288, 6291456, 0, 0, 32768, 32768, 32768, 32, 32, 1536, 0, 18874368, 1, 0, 1, 0, 1, 80",
      /* 3250 */ "6291456, 0, 0, 4, 3080, 1536, 6144, 8192, 131072, 524288, 524288, 14680064, 33554432, 1610612736, 0",
      /* 3265 */ "0, 0, 0, 256, 67108864, 402653184, 0, 64, 512, 524288, 4194304, 16777216, 33554432, 67108864",
      /* 3279 */ "134217728, 1, 512, 0, 0, 4, 8388608, 536870912, 0, 0, 0, -2147483648, 1536, 0, 0, 0, 8, 2048, 1, 4",
      /* 3299 */ "64, 1536, 6144, 1, 524288, 4194304, 0, 0, 49152, -1879048192, 12582912, 33554432, 1610612736, 0, 0",
      /* 3314 */ "65536, 0, 0, 262144, 4194304, 8388608, 16777216, 33554432, 536870912, -2147483648, 0, 32768, 65536",
      /* 3327 */ "-2147483648, 8388608, 0, 256, 67108864, 134217728, 268435456, 1, 4194304, 0, 0, 0, 69533696, 1048576",
      /* 3341 */ "16777216, 0, 0, 8, 4096, 1024, 6144, 8388608, 33554432, 1610612736, 6144, 8388608, 1610612736, 0, 0",
      /* 3356 */ "6144, 1610612736, 0, 0, 10, 22912, 262144, 2048, 32768, 65536, 0, 0, 1048832, 0, 0, 0, 12, 14, 0",
      /* 3375 */ "1048832, 67108864, 0, 0, 327680, 458752, 268435457, 0, 67108864, 0, 0, 393216, 0, 0, 5568, 74907648",
      /* 3391 */ "-536870912, 268435457, 2101248, 268435457, 67108864, 536871940, 0, 536871940, 536871940, 0",
      /* 3401 */ "536871940, 536873996, 8781824, 545655820, 142999552, 8781840, 75890688, 75890688, 545393676",
      /* 3410 */ "545393676, 545655820, 545393676, 545655820, 545655820, 545655820, 545655820, 546704652, 547757068",
      /* 3419 */ "-1546727698, -1546727698, -1546727698, -1546727698, -1546727698, 0, 0, 0, 16, 268435456, 0, 3080, 0",
      /* 3432 */ "0, 32, 32768, 393216, 8388608, 536870912, 0, 0, 524288, 268435456, 0, 393216, 8388608, 0, 0, 2097152",
      /* 3448 */ "0, 0, 0, 7176, 2490368, 0, 0, 4718592, 0, 0, 16384, 16384, 16384, 16384, 0, 0, 0, 7, 4, 20200",
      /* 3468 */ "5144576, 25165824, 33554432, 4, 16, 65536, 268435456, 1073741824, 0, 0, 16777216, 8192, 256, 1048576",
      /* 3482 */ "0, 0, 67108864, 64, 0, 1, 0, 0, 4, 8, 3072, 0, 0, 0, 59, 31680, 983040, 7168, 0, 0, 0, 64, 256, 2048",
      /* 3506 */ "268435456, 96, 128, 512, 3072, 16384, 32768, 4194304, 25165824, 33554432, 536870912, -2147483648, 0",
      /* 3519 */ "0, 3072, 262144, 8388608, 536870912, 32768, 262144, 524288, 4194304, 8388608, 1, 4096, 2097152, 0, 0",
      /* 3534 */ "2048, 262144, 8388608, 536870912, 0, 262144, 8388608, 0, 0, 16, 0, 0, 96, 128, 512, 1024, 2048, 2048",
      /* 3552 */ "16384, 32768, 262144, 4194304, 1, 4096, 0, 0, 128, 0, 0, 0, 3145728, 4, 8, 1024, 2048, 8192, 16384",
      /* 3571 */ "196608, 262144, 96, 1024, 2048, 16384, 262144, 0, 1, 4096, 1024, 6144, 131072, 524288, 12582912",
      /* 3586 */ "33554432, 4, 8, 96, 1024, 16384, 0, 16777216, 64, 0, 0, 16777216, 8192, 4, 2, 32, 64, 4194304",
      /* 3604 */ "16777216, 0, 2, 32, 64, 0, 0, 0, 8, 0, 10, 26, 35130378, 35142666, 0, 32, 0, 2, 0, 2, 8, 128, 256",
      /* 3627 */ "2048, 0, 0, 0, 67108864, 0, 0, 1073741824, 0, 0, 0, 2, 24, 2, 8, 32, 64, 128, 768, 8192, 16384",
      /* 3648 */ "196608, 524288, 4194304, 24, 24, 24, 24, 131080, 8, 8, 24, 8, 8, 56, 56, 56, 56, 58, 58, 58, 56, 58",
      /* 3670 */ "58, 56, 0, 0, 16777216, 1048576, 0, 67108864, 64, 64, 64, 64, 0, 0, 1, 32, 512, 131072, 0, 0, 0",
      /* 3691 */ "536870912, 0, 0"
    };
    String[] s2 = java.util.Arrays.toString(s1).replaceAll("[ \\[\\]]", "").split(",");
    for (int i = 0; i < 3694; ++i) {EXPECTED[i] = Integer.parseInt(s2[i]);}
  }

  private static final String[] TOKEN =
  {
    "(0)",
    "IntegerLiteral",
    "DecimalLiteral",
    "DoubleLiteral",
    "StringLiteral",
    "URIQualifiedName",
    "PredefinedEntityRef",
    "'\"\"'",
    "EscapeApos",
    "ElementContentChar",
    "QuotAttrContentChar",
    "AposAttrContentChar",
    "PITarget",
    "CharRef",
    "NCName",
    "QName",
    "S",
    "S",
    "CommentContents",
    "PragmaContents",
    "Wildcard",
    "DirCommentContents",
    "DirPIContents",
    "CDataSectionContents",
    "EOF",
    "'!'",
    "'!='",
    "'\"'",
    "'#'",
    "'#)'",
    "'$'",
    "'%'",
    "''''",
    "'('",
    "'(#'",
    "'(:'",
    "')'",
    "'*'",
    "'+'",
    "','",
    "'-'",
    "'-->'",
    "'.'",
    "'..'",
    "'/'",
    "'//'",
    "'/>'",
    "':)'",
    "'::'",
    "':='",
    "';'",
    "'<'",
    "'<!--'",
    "'<![CDATA['",
    "'</'",
    "'<<'",
    "'<='",
    "'<?'",
    "'='",
    "'>'",
    "'>='",
    "'>>'",
    "'?'",
    "'?>'",
    "'@'",
    "'NaN'",
    "'['",
    "']'",
    "']]>'",
    "'allowing'",
    "'ancestor'",
    "'ancestor-or-self'",
    "'and'",
    "'as'",
    "'ascending'",
    "'at'",
    "'attribute'",
    "'base-uri'",
    "'boundary-space'",
    "'by'",
    "'case'",
    "'cast'",
    "'castable'",
    "'catch'",
    "'child'",
    "'collation'",
    "'comment'",
    "'construction'",
    "'context'",
    "'copy-namespaces'",
    "'count'",
    "'decimal-format'",
    "'decimal-separator'",
    "'declare'",
    "'default'",
    "'descendant'",
    "'descendant-or-self'",
    "'descending'",
    "'digit'",
    "'div'",
    "'document'",
    "'document-node'",
    "'element'",
    "'else'",
    "'empty'",
    "'empty-sequence'",
    "'encoding'",
    "'end'",
    "'eq'",
    "'every'",
    "'except'",
    "'external'",
    "'following'",
    "'following-sibling'",
    "'for'",
    "'function'",
    "'ge'",
    "'greatest'",
    "'group'",
    "'grouping-separator'",
    "'gt'",
    "'idiv'",
    "'if'",
    "'import'",
    "'in'",
    "'infinity'",
    "'inherit'",
    "'instance'",
    "'intersect'",
    "'is'",
    "'item'",
    "'lax'",
    "'le'",
    "'least'",
    "'let'",
    "'lt'",
    "'minus-sign'",
    "'mod'",
    "'module'",
    "'namespace'",
    "'namespace-node'",
    "'ne'",
    "'next'",
    "'no-inherit'",
    "'no-preserve'",
    "'node'",
    "'of'",
    "'only'",
    "'option'",
    "'or'",
    "'order'",
    "'ordered'",
    "'ordering'",
    "'parent'",
    "'pattern-separator'",
    "'per-mille'",
    "'percent'",
    "'preceding'",
    "'preceding-sibling'",
    "'preserve'",
    "'previous'",
    "'processing-instruction'",
    "'return'",
    "'satisfies'",
    "'schema'",
    "'schema-attribute'",
    "'schema-element'",
    "'self'",
    "'sliding'",
    "'some'",
    "'stable'",
    "'start'",
    "'strict'",
    "'strip'",
    "'switch'",
    "'text'",
    "'then'",
    "'to'",
    "'treat'",
    "'try'",
    "'tumbling'",
    "'type'",
    "'typeswitch'",
    "'union'",
    "'unordered'",
    "'validate'",
    "'variable'",
    "'version'",
    "'when'",
    "'where'",
    "'window'",
    "'xquery'",
    "'zero-digit'",
    "'{'",
    "'{{'",
    "'|'",
    "'||'",
    "'}'",
    "'}}'"
  };
}

// End

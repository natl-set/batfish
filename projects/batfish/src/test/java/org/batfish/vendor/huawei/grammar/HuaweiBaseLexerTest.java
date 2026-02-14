package org.batfish.vendor.huawei.grammar;

import static org.junit.Assert.assertEquals;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.Test;

/** Tests for {@link HuaweiBaseLexer} */
public class HuaweiBaseLexerTest {

  @Test
  public void testInitialState() {
    String input = "";
    HuaweiLexer lexer = new HuaweiLexer(CharStreams.fromString(input));

    // Initially, should be able to create lexer
    Token firstToken = lexer.nextToken();
    assertEquals(Token.EOF, firstToken.getType());
  }

  @Test
  public void testBasicTokenization() {
    String input = "sysname Router1";
    HuaweiLexer lexer = new HuaweiLexer(CharStreams.fromString(input));

    // Should tokenize sysname and hostname correctly
    Token sysnameToken = lexer.nextToken();
    assertEquals(HuaweiLexer.SYSNAME, sysnameToken.getType());

    Token routerToken = lexer.nextToken();
    assertEquals(HuaweiLexer.VARIABLE, routerToken.getType());
    assertEquals("Router1", routerToken.getText());

    Token eofToken = lexer.nextToken();
    assertEquals(Token.EOF, eofToken.getType());
  }

  @Test
  public void testPrintStateVariables() {
    String input = "";
    HuaweiLexer lexer = new HuaweiLexer(CharStreams.fromString(input));

    String state = lexer.printStateVariables();

    // Should contain both state variables
    assert (state.contains("_lastTokenType:"));
    assert (state.contains("_secondToLastTokenType:"));

    // Initial values should be -1
    assert (state.contains("_lastTokenType: -1"));
    assert (state.contains("_secondToLastTokenType: -1"));
  }
}

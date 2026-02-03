package org.batfish.grammar.f5_bigip_structured;

import static org.batfish.grammar.f5_bigip_structured.F5BigipStructuredConfigurationBuilder.unquote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests for {@link F5BigipStructuredConfigurationBuilder} */
public class F5BigipStructuredConfigurationBuilderTest {

  // ==================== unquote Tests ====================

  @Test
  public void testUnquote_quotedString() {
    assertThat(unquote("\"hello\""), equalTo("hello"));
  }

  @Test
  public void testUnquote_quotedStringWithSpaces() {
    assertThat(unquote("\"hello world\""), equalTo("hello world"));
  }

  @Test
  public void testUnquote_quotedStringWithEscapedQuotes() {
    assertThat(unquote("\"hello \\\"world\\\"\""), equalTo("hello \"world\""));
  }

  @Test
  public void testUnquote_quotedStringMultipleEscapedQuotes() {
    assertThat(unquote("\"\\\"a\\\" \\\"b\\\" \\\"c\\\"\""), equalTo("\"a\" \"b\" \"c\""));
  }

  @Test
  public void testUnquote_quotedStringEmpty() {
    assertThat(unquote("\"\""), equalTo(""));
  }

  @Test
  public void testUnquote_unquotedString() {
    assertThat(unquote("hello"), equalTo("hello"));
  }

  @Test
  public void testUnquote_unquotedStringWithSpaces() {
    assertThat(unquote("hello world"), equalTo("hello world"));
  }

  @Test
  public void testUnquote_singleQuoted() {
    // Single quotes are not treated as quotes
    assertThat(unquote("'hello'"), equalTo("'hello'"));
  }

  @Test
  public void testUnquote_onlyOpeningQuote() {
    assertThat(unquote("\"hello"), equalTo("\"hello"));
  }

  @Test
  public void testUnquote_onlyClosingQuote() {
    assertThat(unquote("hello\""), equalTo("hello\""));
  }

  @Test
  public void testUnquote_mismatchedQuotes() {
    assertThat(unquote("\"hello'"), equalTo("\"hello'"));
  }

  @Test
  public void testUnquote_singleCharacterQuoted() {
    assertThat(unquote("\"a\""), equalTo("a"));
  }

  @Test
  public void testUnquote_quotedWithBackslashAtEnd() {
    assertThat(unquote("\"test\\\""), equalTo("test\\"));
  }

  @Test
  public void testUnquote_onlyQuotes() {
    assertThat(unquote("\"\""), equalTo(""));
  }

  @Test
  public void testUnquote_quotedWithSpecialCharacters() {
    assertThat(unquote("\"hello@world!\""), equalTo("hello@world!"));
  }

  @Test
  public void testUnquote_quotedWithNumbers() {
    assertThat(unquote("\"12345\""), equalTo("12345"));
  }

  @Test
  public void testUnquote_quotedWithMixedContent() {
    assertThat(unquote("\"vlan-10/20\""), equalTo("vlan-10/20"));
  }

  @Test
  public void testUnquote_unquotedEmptyString() {
    assertThat(unquote(""), equalTo(""));
  }

  @Test
  public void testUnquote_unquotedSingleQuote() {
    assertThat(unquote("\""), equalTo("\""));
  }

  @Test
  public void testUnquote_quotedIPv4() {
    assertThat(unquote("\"192.168.1.1\""), equalTo("192.168.1.1"));
  }

  @Test
  public void testUnquote_quotedIPv6() {
    assertThat(unquote("\"2001:db8::1\""), equalTo("2001:db8::1"));
  }

  @Test
  public void testUnquote_quotedWithNewlines() {
    String input = "\"line1\nline2\"";
    // Newlines are preserved in quoted strings
    assertThat(unquote(input), equalTo("line1\nline2"));
  }

  @Test
  public void testUnquote_quotedWithTabs() {
    String input = "\"col1\tcol2\"";
    assertThat(unquote(input), equalTo("col1\tcol2"));
  }

  @Test
  public void testUnquote_onlyWhitespaceQuoted() {
    assertThat(unquote("\"   \""), equalTo("   "));
  }

  @Test
  public void testUnquote_quotedMultipleBackslashQuotes() {
    assertThat(unquote("\"a\\\"\\\"b\""), equalTo("a\"\"b"));
  }

  @Test
  public void testUnquote_quotedConsecutiveBackslashQuotes() {
    assertThat(unquote("\"\\\"\\\"\\\"\""), equalTo("\"\"\""));
  }

  @Test
  public void testUnquote_quotedWithUnderscores() {
    assertThat(unquote("\"my_vlan_10\""), equalTo("my_vlan_10"));
  }

  @Test
  public void testUnquote_quotedWithHyphens() {
    assertThat(unquote("\"my-pool-name\""), equalTo("my-pool-name"));
  }
}

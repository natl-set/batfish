package org.batfish.grammar.f5_bigip_structured;

import static org.batfish.grammar.f5_bigip_structured.F5BigipStructuredConfigurationBuilder.unquote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ip_addressContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ip_address_portContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ipv6_addressContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ipv6_address_portContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.UintContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.UnrecognizedContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.junit.Test;

/** Tests for {@link F5BigipStructuredConfigurationBuilder} */
public class F5BigipStructuredConfigurationBuilderTest {

  private static F5BigipStructuredConfigurationBuilder newBuilder() {
    return new F5BigipStructuredConfigurationBuilder(
        new F5BigipStructuredCombinedParser("", new Settings()),
        "",
        new Warnings(),
        new SilentSyntaxCollection());
  }

  private static RuntimeException invokeStaticExpectRuntime(
      String methodName, Class<?> argType, Object arg) {
    try {
      Method method =
          F5BigipStructuredConfigurationBuilder.class.getDeclaredMethod(methodName, argType);
      method.setAccessible(true);
      method.invoke(null, arg);
      throw new AssertionError("Expected runtime exception");
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException) {
        return (RuntimeException) e.getCause();
      }
      throw new AssertionError("Unexpected exception type", e.getCause());
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Reflection failed", e);
    }
  }

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

  @Test
  public void testNullGuardToIntegerUint() {
    RuntimeException e = invokeStaticExpectRuntime("toInteger", UintContext.class, null);
    assertThat(e.getMessage(), equalTo("Uint context cannot be null"));
  }

  @Test
  public void testNullGuardToIpAddressPort() {
    RuntimeException e = invokeStaticExpectRuntime("toIp", Ip_address_portContext.class, null);
    assertThat(e.getMessage(), equalTo("IP address context cannot be null"));
  }

  @Test
  public void testNullGuardToIpAddress() {
    RuntimeException e = invokeStaticExpectRuntime("toIp", Ip_addressContext.class, null);
    assertThat(e.getMessage(), equalTo("IP address context cannot be null"));
  }

  @Test
  public void testNullGuardToIp6AddressPort() {
    RuntimeException e = invokeStaticExpectRuntime("toIp6", Ipv6_address_portContext.class, null);
    assertThat(e.getMessage(), equalTo("IPv6 address context cannot be null"));
  }

  @Test
  public void testNullGuardToIp6Address() {
    RuntimeException e = invokeStaticExpectRuntime("toIp6", Ipv6_addressContext.class, null);
    assertThat(e.getMessage(), equalTo("IPv6 address context cannot be null"));
  }

  @Test
  public void testDetailedUnrecognizedMessageByLeadText() throws Exception {
    Method method =
        F5BigipStructuredConfigurationBuilder.class.getDeclaredMethod(
            "getDetailedUnrecognizedMessage", UnrecognizedContext.class, String.class);
    method.setAccessible(true);
    F5BigipStructuredConfigurationBuilder builder = newBuilder();

    String invalid = (String) method.invoke(builder, null, "bad pool virtual syntax");
    String missing = (String) method.invoke(builder, null, "missing brace");
    String duplicate = (String) method.invoke(builder, null, "duplicate section");
    String snmp = (String) method.invoke(builder, null, "snmp odd token");

    assertThat(invalid, containsString("invalid syntax"));
    assertThat(invalid, containsString("pool/virtual"));
    assertThat(missing, containsString("missing"));
    assertThat(duplicate, containsString("Duplicate"));
    assertThat(snmp, containsString("SNMP"));
  }

  @Test
  public void testDetailedErrorMessageByLeadText() throws Exception {
    Method method =
        F5BigipStructuredConfigurationBuilder.class.getDeclaredMethod(
            "getDetailedErrorMessage",
            org.antlr.v4.runtime.tree.ErrorNode.class,
            int.class,
            String.class);
    method.setAccessible(true);
    F5BigipStructuredConfigurationBuilder builder = newBuilder();

    String invalidIp = (String) method.invoke(builder, null, 42, "invalid ip");
    String missingBrace = (String) method.invoke(builder, null, 43, "missing brace");
    String unterminated = (String) method.invoke(builder, null, 44, "unterminated quote");
    String generic = (String) method.invoke(builder, null, 45, "other problem");

    assertThat(invalidIp, containsString("Invalid IP address format"));
    assertThat(missingBrace, containsString("missing closing braces"));
    assertThat(unterminated, containsString("Unterminated string or structure"));
    assertThat(generic, containsString("Syntax error detected"));
    assertThat(generic, containsString("Line 45"));
  }
}

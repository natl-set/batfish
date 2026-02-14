package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests for {@link HuaweiAcl}. */
public class HuaweiAclTest {

  @Test
  public void testConstructor() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.BASIC);
    assertThat(acl.getName(), equalTo("ACL1"));
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines(), notNullValue());
    assertThat(acl.getLines(), hasSize(0));
    assertThat(acl.isIpv6(), equalTo(false));
    assertThat(acl.getVrfName(), nullValue());
  }

  @Test
  public void testConstructorWithAdvancedType() {
    HuaweiAcl acl = new HuaweiAcl("ACL2", HuaweiAcl.AclType.ADVANCED);
    assertThat(acl.getName(), equalTo("ACL2"));
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
    assertThat(acl.getLines(), notNullValue());
    assertThat(acl.getLines(), hasSize(0));
  }

  @Test
  public void testConstructorWithL2Type() {
    HuaweiAcl acl = new HuaweiAcl("ACL3", HuaweiAcl.AclType.L2);
    assertThat(acl.getName(), equalTo("ACL3"));
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.L2));
    assertThat(acl.getLines(), notNullValue());
    assertThat(acl.getLines(), hasSize(0));
  }

  @Test
  public void testSetName() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.BASIC);
    assertThat(acl.getName(), equalTo("ACL1"));

    acl.setName("NEW_ACL");
    assertThat(acl.getName(), equalTo("NEW_ACL"));

    acl.setName("2000");
    assertThat(acl.getName(), equalTo("2000"));
  }

  @Test
  public void testSetType() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.BASIC);
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));

    acl.setType(HuaweiAcl.AclType.ADVANCED);
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));

    acl.setType(HuaweiAcl.AclType.L2);
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.L2));
  }

  @Test
  public void testGetLines() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.BASIC);
    assertThat(acl.getLines(), notNullValue());
    assertThat(acl.getLines(), hasSize(0));
  }

  @Test
  public void testSetLines() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.ADVANCED);

    List<HuaweiAclLine> lines = new ArrayList<>();
    lines.add(new HuaweiAclLine(10, "permit"));
    lines.add(new HuaweiAclLine(20, "deny"));
    lines.add(new HuaweiAclLine(30, "permit"));

    acl.setLines(lines);

    assertThat(acl.getLines(), hasSize(3));
    assertThat(acl.getLines().get(0).getSequenceNumber(), equalTo(10));
    assertThat(acl.getLines().get(1).getSequenceNumber(), equalTo(20));
    assertThat(acl.getLines().get(2).getSequenceNumber(), equalTo(30));
  }

  @Test
  public void testSetLinesToEmptyList() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.BASIC);

    List<HuaweiAclLine> lines = new ArrayList<>();
    lines.add(new HuaweiAclLine(10, "permit"));
    acl.setLines(lines);

    assertThat(acl.getLines(), hasSize(1));

    // Replace with empty list
    acl.setLines(new ArrayList<>());
    assertThat(acl.getLines(), hasSize(0));
  }

  @Test
  public void testAddLine() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.ADVANCED);

    HuaweiAclLine line1 = new HuaweiAclLine(10, "permit");
    HuaweiAclLine line2 = new HuaweiAclLine(20, "deny");
    HuaweiAclLine line3 = new HuaweiAclLine(30, "permit");

    acl.addLine(line1);
    acl.addLine(line2);
    acl.addLine(line3);

    assertThat(acl.getLines(), hasSize(3));
    assertThat(acl.getLines().get(0), equalTo(line1));
    assertThat(acl.getLines().get(1), equalTo(line2));
    assertThat(acl.getLines().get(2), equalTo(line3));
  }

  @Test
  public void testAddMultipleLines() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.BASIC);

    // Add multiple ACL lines
    for (int i = 10; i <= 100; i += 10) {
      acl.addLine(new HuaweiAclLine(i, "permit"));
    }

    assertThat(acl.getLines(), hasSize(10));
    assertThat(acl.getLines().get(0).getSequenceNumber(), equalTo(10));
    assertThat(acl.getLines().get(9).getSequenceNumber(), equalTo(100));
  }

  @Test
  public void testIsIpv6() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.BASIC);
    assertThat(acl.isIpv6(), equalTo(false));
  }

  @Test
  public void testSetIpv6() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.ADVANCED);
    assertThat(acl.isIpv6(), equalTo(false));

    acl.setIpv6(true);
    assertThat(acl.isIpv6(), equalTo(true));

    acl.setIpv6(false);
    assertThat(acl.isIpv6(), equalTo(false));
  }

  @Test
  public void testGetVrfName() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.BASIC);
    assertThat(acl.getVrfName(), nullValue());
  }

  @Test
  public void testSetVrfName() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.ADVANCED);
    assertThat(acl.getVrfName(), nullValue());

    acl.setVrfName("VRF1");
    assertThat(acl.getVrfName(), equalTo("VRF1"));

    acl.setVrfName("CUSTOMER_A");
    assertThat(acl.getVrfName(), equalTo("CUSTOMER_A"));
  }

  @Test
  public void testSetVrfNameToNull() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.L2);
    acl.setVrfName("VRF1");
    assertThat(acl.getVrfName(), equalTo("VRF1"));

    acl.setVrfName(null);
    assertThat(acl.getVrfName(), nullValue());
  }

  @Test
  public void testSerialization() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.ADVANCED);
    acl.setIpv6(true);
    acl.setVrfName("VRF1");

    HuaweiAclLine line1 = new HuaweiAclLine(10, "permit");
    line1.setProtocol("tcp");
    line1.setSource("192.168.1.0/24");
    line1.setDestination("10.0.0.0/8");

    HuaweiAclLine line2 = new HuaweiAclLine(20, "deny");
    line2.setProtocol("udp");
    line2.setSource("10.0.0.0/8");
    line2.setDestination("192.168.1.0/24");

    acl.addLine(line1);
    acl.addLine(line2);

    HuaweiAcl clone = SerializationUtils.clone(acl);

    assertThat(clone.getName(), equalTo(acl.getName()));
    assertThat(clone.getType(), equalTo(acl.getType()));
    assertThat(clone.isIpv6(), equalTo(acl.isIpv6()));
    assertThat(clone.getVrfName(), equalTo(acl.getVrfName()));
    assertThat(clone.getLines(), hasSize(2));
    assertThat(clone.getLines().get(0).getAction(), equalTo("permit"));
    assertThat(clone.getLines().get(1).getAction(), equalTo("deny"));
  }

  @Test
  public void testComplexAclConfiguration() {
    HuaweiAcl acl = new HuaweiAcl("3000", HuaweiAcl.AclType.ADVANCED);
    acl.setIpv6(false);
    acl.setVrfName("PRODUCTION_VRF");

    // Add multiple complex ACL lines
    HuaweiAclLine line1 = new HuaweiAclLine(10, "permit");
    line1.setProtocol("tcp");
    line1.setSource("10.0.0.0/8");
    line1.setSourcePort("1024-65535");
    line1.setDestination("192.168.1.0/24");
    line1.setDestinationPort("80");
    acl.addLine(line1);

    HuaweiAclLine line2 = new HuaweiAclLine(20, "permit");
    line2.setProtocol("tcp");
    line2.setSource("10.0.0.0/8");
    line2.setSourcePort("1024-65535");
    line2.setDestination("192.168.1.0/24");
    line2.setDestinationPort("443");
    acl.addLine(line2);

    HuaweiAclLine line3 = new HuaweiAclLine(30, "deny");
    line3.setProtocol("ip");
    line3.setSource("0.0.0.0/0");
    line3.setDestination("192.168.1.0/24");
    acl.addLine(line3);

    assertThat(acl.getName(), equalTo("3000"));
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
    assertThat(acl.isIpv6(), equalTo(false));
    assertThat(acl.getVrfName(), equalTo("PRODUCTION_VRF"));
    assertThat(acl.getLines(), hasSize(3));
  }

  @Test
  public void testIpv6Acl() {
    HuaweiAcl acl = new HuaweiAcl("IPv6_ACL", HuaweiAcl.AclType.ADVANCED);
    acl.setIpv6(true);
    acl.setVrfName("VRF_IPV6");

    HuaweiAclLine line1 = new HuaweiAclLine(10, "permit");
    line1.setProtocol("tcp");
    line1.setSource("2001:db8::/32");
    line1.setDestination("2001:db8:1::/64");
    acl.addLine(line1);

    assertThat(acl.isIpv6(), equalTo(true));
    assertThat(acl.getVrfName(), equalTo("VRF_IPV6"));
    assertThat(acl.getLines(), hasSize(1));
    assertThat(acl.getLines().get(0).getProtocol(), equalTo("tcp"));
  }

  @Test
  public void testBasicAcl() {
    HuaweiAcl acl = new HuaweiAcl("2000", HuaweiAcl.AclType.BASIC);

    HuaweiAclLine line1 = new HuaweiAclLine(10, "permit");
    line1.setSource("192.168.1.0/24");
    acl.addLine(line1);

    HuaweiAclLine line2 = new HuaweiAclLine(20, "deny");
    line2.setSource("10.0.0.0/8");
    acl.addLine(line2);

    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines(), hasSize(2));
  }

  @Test
  public void testL2Acl() {
    HuaweiAcl acl = new HuaweiAcl("4000", HuaweiAcl.AclType.L2);

    HuaweiAclLine line1 = new HuaweiAclLine(10, "permit");
    acl.addLine(line1);

    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.L2));
    assertThat(acl.getLines(), hasSize(1));
  }

  @Test
  public void testAclTypeEnum() {
    // Test all enum values exist
    HuaweiAcl.AclType[] types = HuaweiAcl.AclType.values();
    assertThat(types.length, equalTo(3));
    assertTrue(
        "BASIC type should exist",
        java.util.Arrays.asList(types).contains(HuaweiAcl.AclType.BASIC));
    assertTrue(
        "ADVANCED type should exist",
        java.util.Arrays.asList(types).contains(HuaweiAcl.AclType.ADVANCED));
    assertTrue(
        "L2 type should exist", java.util.Arrays.asList(types).contains(HuaweiAcl.AclType.L2));
  }

  @Test
  public void testReplaceLinesList() {
    HuaweiAcl acl = new HuaweiAcl("ACL1", HuaweiAcl.AclType.ADVANCED);

    // Add initial lines
    HuaweiAclLine line1 = new HuaweiAclLine(10, "permit");
    HuaweiAclLine line2 = new HuaweiAclLine(20, "deny");
    acl.addLine(line1);
    acl.addLine(line2);

    assertThat(acl.getLines(), hasSize(2));

    // Replace with new lines
    List<HuaweiAclLine> newLines = new ArrayList<>();
    newLines.add(new HuaweiAclLine(30, "permit"));
    newLines.add(new HuaweiAclLine(40, "deny"));
    newLines.add(new HuaweiAclLine(50, "permit"));
    acl.setLines(newLines);

    assertThat(acl.getLines(), hasSize(3));
    assertThat(acl.getLines().get(0).getSequenceNumber(), equalTo(30));
    assertThat(acl.getLines().get(1).getSequenceNumber(), equalTo(40));
    assertThat(acl.getLines().get(2).getSequenceNumber(), equalTo(50));
  }

  @Test
  public void testMinimalAcl() {
    // Minimal ACL configuration - only constructor parameters
    HuaweiAcl acl = new HuaweiAcl("100", HuaweiAcl.AclType.BASIC);

    assertThat(acl.getName(), equalTo("100"));
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines(), hasSize(0));
    assertThat(acl.isIpv6(), equalTo(false));
    assertThat(acl.getVrfName(), nullValue());
  }
}

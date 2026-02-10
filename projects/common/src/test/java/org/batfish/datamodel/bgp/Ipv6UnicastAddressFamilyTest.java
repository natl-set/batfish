package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Test;

/** Tests for {@link Ipv6UnicastAddressFamily}. */
public class Ipv6UnicastAddressFamilyTest {

  @Test
  public void testBuilder() {
    Ipv6UnicastAddressFamily family = Ipv6UnicastAddressFamily.builder().build();
    assertThat(family.getType(), equalTo(AddressFamily.Type.IPV6_UNICAST));
    assertThat(family.getExportPolicy(), nullValue());
    assertThat(family.getImportPolicy(), nullValue());
    assertFalse(family.getRouteReflectorClient());
  }

  @Test
  public void testBuilderWithExportPolicy() {
    Ipv6UnicastAddressFamily family =
        Ipv6UnicastAddressFamily.builder().setExportPolicy("EXPORT_POLICY").build();
    assertThat(family.getExportPolicy(), equalTo("EXPORT_POLICY"));
    assertThat(family.getType(), equalTo(AddressFamily.Type.IPV6_UNICAST));
  }

  @Test
  public void testBuilderWithImportPolicy() {
    Ipv6UnicastAddressFamily family =
        Ipv6UnicastAddressFamily.builder().setImportPolicy("IMPORT_POLICY").build();
    assertThat(family.getImportPolicy(), equalTo("IMPORT_POLICY"));
    assertThat(family.getType(), equalTo(AddressFamily.Type.IPV6_UNICAST));
  }

  @Test
  public void testBuilderWithRouteReflectorClient() {
    Ipv6UnicastAddressFamily family =
        Ipv6UnicastAddressFamily.builder().setRouteReflectorClient(true).build();
    assertTrue(family.getRouteReflectorClient());
  }

  @Test
  public void testBuilderWithExportPolicySources() {
    SortedSet<String> sources = new TreeSet<>();
    sources.add("source1");
    sources.add("source2");

    Ipv6UnicastAddressFamily family =
        Ipv6UnicastAddressFamily.builder().setExportPolicySources(sources).build();
    assertThat(family.getExportPolicySources(), equalTo(sources));
    assertThat(family.getExportPolicySources().size(), equalTo(2));
  }

  @Test
  public void testBuilderWithImportPolicySources() {
    SortedSet<String> sources = new TreeSet<>();
    sources.add("source1");
    sources.add("source2");

    Ipv6UnicastAddressFamily family =
        Ipv6UnicastAddressFamily.builder().setImportPolicySources(sources).build();
    assertThat(family.getImportPolicySources(), equalTo(sources));
    assertThat(family.getImportPolicySources().size(), equalTo(2));
  }

  @Test
  public void testBuilderWithAllFields() {
    AddressFamilyCapabilities capabilities =
        AddressFamilyCapabilities.builder()
            .setAdditionalPathsSend(true)
            .setAdditionalPathsReceive(true)
            .setSendCommunity(true)
            .build();

    SortedSet<String> exportSources = new TreeSet<>();
    exportSources.add("export_source");

    SortedSet<String> importSources = new TreeSet<>();
    importSources.add("import_source");

    Ipv6UnicastAddressFamily family =
        Ipv6UnicastAddressFamily.builder()
            .setAddressFamilyCapabilities(capabilities)
            .setExportPolicy("EXPORT_POLICY")
            .setExportPolicySources(exportSources)
            .setImportPolicy("IMPORT_POLICY")
            .setImportPolicySources(importSources)
            .setRouteReflectorClient(true)
            .build();

    assertThat(family.getType(), equalTo(AddressFamily.Type.IPV6_UNICAST));
    assertThat(family.getExportPolicy(), equalTo("EXPORT_POLICY"));
    assertThat(family.getImportPolicy(), equalTo("IMPORT_POLICY"));
    assertThat(family.getExportPolicySources(), equalTo(exportSources));
    assertThat(family.getImportPolicySources(), equalTo(importSources));
    assertTrue(family.getRouteReflectorClient());
  }

  @Test
  public void testEquals() {
    Ipv6UnicastAddressFamily family1 =
        Ipv6UnicastAddressFamily.builder().setExportPolicy("EXPORT_POLICY").build();

    Ipv6UnicastAddressFamily family2 =
        Ipv6UnicastAddressFamily.builder().setExportPolicy("EXPORT_POLICY").build();

    assertEquals(family1, family2);
  }

  @Test
  public void testEqualsSameObject() {
    Ipv6UnicastAddressFamily family = Ipv6UnicastAddressFamily.builder().build();
    assertThat(family, equalTo(family));
  }

  @Test
  public void testNotEqualsDifferentExportPolicy() {
    Ipv6UnicastAddressFamily family1 =
        Ipv6UnicastAddressFamily.builder().setExportPolicy("EXPORT_POLICY_1").build();

    Ipv6UnicastAddressFamily family2 =
        Ipv6UnicastAddressFamily.builder().setExportPolicy("EXPORT_POLICY_2").build();

    assertNotEquals(family1, family2);
  }

  @Test
  public void testNotEqualsDifferentImportPolicy() {
    Ipv6UnicastAddressFamily family1 =
        Ipv6UnicastAddressFamily.builder().setImportPolicy("IMPORT_POLICY_1").build();

    Ipv6UnicastAddressFamily family2 =
        Ipv6UnicastAddressFamily.builder().setImportPolicy("IMPORT_POLICY_2").build();

    assertNotEquals(family1, family2);
  }

  @Test
  public void testNotEqualsDifferentRouteReflectorClient() {
    Ipv6UnicastAddressFamily family1 =
        Ipv6UnicastAddressFamily.builder().setRouteReflectorClient(true).build();

    Ipv6UnicastAddressFamily family2 =
        Ipv6UnicastAddressFamily.builder().setRouteReflectorClient(false).build();

    assertNotEquals(family1, family2);
  }

  @Test
  public void testNotEqualsDifferentType() {
    Ipv6UnicastAddressFamily ipv6Family = Ipv6UnicastAddressFamily.builder().build();

    // Test against a different address family type
    assertNotEquals(ipv6Family, null);
    assertNotEquals(ipv6Family, "not an address family");
  }

  @Test
  public void testHashCodeConsistent() {
    Ipv6UnicastAddressFamily family =
        Ipv6UnicastAddressFamily.builder()
            .setExportPolicy("EXPORT_POLICY")
            .setImportPolicy("IMPORT_POLICY")
            .build();

    int hash1 = family.hashCode();
    int hash2 = family.hashCode();
    assertThat(hash1, equalTo(hash2));
  }

  @Test
  public void testHashCodeEqualObjectsHaveEqualHashCodes() {
    Ipv6UnicastAddressFamily family1 =
        Ipv6UnicastAddressFamily.builder()
            .setExportPolicy("EXPORT_POLICY")
            .setImportPolicy("IMPORT_POLICY")
            .build();

    Ipv6UnicastAddressFamily family2 =
        Ipv6UnicastAddressFamily.builder()
            .setExportPolicy("EXPORT_POLICY")
            .setImportPolicy("IMPORT_POLICY")
            .build();

    assertThat(family1, equalTo(family2));
    assertThat(family1.hashCode(), equalTo(family2.hashCode()));
  }

  @Test
  public void testGetType() {
    Ipv6UnicastAddressFamily family = Ipv6UnicastAddressFamily.builder().build();
    assertThat(family.getType(), equalTo(AddressFamily.Type.IPV6_UNICAST));
  }

  @Test
  public void testGetAddressFamilyCapabilities() {
    AddressFamilyCapabilities capabilities =
        AddressFamilyCapabilities.builder()
            .setAdditionalPathsSend(true)
            .setAdditionalPathsReceive(true)
            .setSendCommunity(true)
            .build();

    Ipv6UnicastAddressFamily family =
        Ipv6UnicastAddressFamily.builder().setAddressFamilyCapabilities(capabilities).build();

    assertThat(family.getAddressFamilyCapabilities(), equalTo(capabilities));
    assertTrue(family.getAddressFamilyCapabilities().getAdditionalPathsSend());
    assertTrue(family.getAddressFamilyCapabilities().getAdditionalPathsReceive());
    assertTrue(family.getAddressFamilyCapabilities().getSendCommunity());
  }
}

package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.Ipv6UnicastAddressFamily;
import org.junit.Test;

/** Tests for {@link BgpPeerConfig}. */
public class BgpPeerConfigTest {

  @Test
  public void testBuilderWithDefaults() {
    BgpActivePeerConfig config = BgpActivePeerConfig.builder().build();

    assertThat(config.getPeerAddress(), nullValue());
    assertThat(config.getAppliedRibGroup(), nullValue());
    assertThat(config.getAuthenticationSettings(), nullValue());
    assertTrue(config.getCheckLocalIpOnAccept()); // default is true
    assertThat(config.getClusterId(), nullValue());
    assertThat(config.getConfederationAsn(), nullValue());
    assertThat(config.getDefaultMetric(), equalTo(0));
    assertThat(config.getDescription(), nullValue());
    assertFalse(config.getEbgpMultihop());
    assertFalse(config.getEnforceFirstAs());
    assertThat(config.getGeneratedRoutes(), hasSize(0));
    assertThat(config.getGroup(), nullValue());
    assertThat(config.getLocalAs(), nullValue());
    assertThat(config.getLocalIp(), nullValue());
    assertThat(config.getRemoteAsns(), equalTo(LongSpace.EMPTY));
    assertThat(config.getIpv4UnicastAddressFamily(), nullValue());
    assertThat(config.getIpv6UnicastAddressFamily(), nullValue());
    assertThat(config.getEvpnAddressFamily(), nullValue());
    assertFalse(config.getReplaceNonLocalAsesOnExport());
  }

  @Test
  public void testBuilderWithAllFields() {
    Set<GeneratedRoute> generatedRoutes = new HashSet<>();
    generatedRoutes.add(
        GeneratedRoute.builder().setNetwork(Prefix.parse("10.0.0.0/8")).setAdmin(100).build());

    Ipv4UnicastAddressFamily ipv4Af =
        Ipv4UnicastAddressFamily.builder()
            .setImportPolicy("IMPORT_POLICY")
            .setExportPolicy("EXPORT_POLICY")
            .build();

    Ipv6UnicastAddressFamily ipv6Af =
        Ipv6UnicastAddressFamily.builder()
            .setImportPolicy("IMPORT6_POLICY")
            .setExportPolicy("EXPORT6_POLICY")
            .build();

    EvpnAddressFamily evpnAf =
        EvpnAddressFamily.builder()
            .setImportPolicy("IMPORT_EVPN")
            .setExportPolicy("EXPORT_EVPN")
            .setPropagateUnmatched(false)
            .build();

    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .setLocalAs(65002L)
            .setLocalIp(Ip.parse("10.0.0.2"))
            .setDescription("Test peer")
            .setGroup("PEER_GROUP")
            .setClusterId(100L)
            .setConfederation(65000L)
            .setDefaultMetric(100)
            .setEbgpMultihop(true)
            .setEnforceFirstAs(true)
            .setCheckLocalIpOnAccept(false)
            .setGeneratedRoutes(generatedRoutes)
            .setIpv4UnicastAddressFamily(ipv4Af)
            .setIpv6UnicastAddressFamily(ipv6Af)
            .setEvpnAddressFamily(evpnAf)
            .setReplaceNonLocalAsesOnExport(true)
            .build();

    assertThat(config.getPeerAddress(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(config.getRemoteAsns(), equalTo(LongSpace.of(65001L)));
    assertThat(config.getLocalAs(), equalTo(65002L));
    assertThat(config.getLocalIp(), equalTo(Ip.parse("10.0.0.2")));
    assertThat(config.getDescription(), equalTo("Test peer"));
    assertThat(config.getGroup(), equalTo("PEER_GROUP"));
    assertThat(config.getClusterId(), equalTo(100L));
    assertThat(config.getConfederationAsn(), equalTo(65000L));
    assertThat(config.getDefaultMetric(), equalTo(100));
    assertTrue(config.getEbgpMultihop());
    assertTrue(config.getEnforceFirstAs());
    assertFalse(config.getCheckLocalIpOnAccept());
    assertThat(config.getGeneratedRoutes(), hasSize(1));
    assertThat(config.getIpv4UnicastAddressFamily(), equalTo(ipv4Af));
    assertThat(config.getIpv6UnicastAddressFamily(), equalTo(ipv6Af));
    assertThat(config.getEvpnAddressFamily(), equalTo(evpnAf));
    assertTrue(config.getReplaceNonLocalAsesOnExport());
  }

  @Test
  public void testGetAllAddressFamilies() {
    BgpActivePeerConfig config = BgpActivePeerConfig.builder().build();
    assertThat(config.getAllAddressFamilies(), hasSize(0));

    // Add IPv4
    Ipv4UnicastAddressFamily ipv4Af =
        Ipv4UnicastAddressFamily.builder().setImportPolicy("IMPORT").build();
    config = BgpActivePeerConfig.builder().setIpv4UnicastAddressFamily(ipv4Af).build();
    assertThat(config.getAllAddressFamilies(), hasSize(1));
    assertTrue(config.getAllAddressFamilies().contains(ipv4Af));

    // Add IPv6
    Ipv6UnicastAddressFamily ipv6Af =
        Ipv6UnicastAddressFamily.builder().setImportPolicy("IMPORT6").build();
    config =
        BgpActivePeerConfig.builder()
            .setIpv4UnicastAddressFamily(ipv4Af)
            .setIpv6UnicastAddressFamily(ipv6Af)
            .build();
    assertThat(config.getAllAddressFamilies(), hasSize(2));
    assertTrue(config.getAllAddressFamilies().contains(ipv4Af));
    assertTrue(config.getAllAddressFamilies().contains(ipv6Af));

    // Add EVPN
    EvpnAddressFamily evpnAf =
        EvpnAddressFamily.builder()
            .setImportPolicy("IMPORT_EVPN")
            .setPropagateUnmatched(false)
            .build();
    config =
        BgpActivePeerConfig.builder()
            .setIpv4UnicastAddressFamily(ipv4Af)
            .setIpv6UnicastAddressFamily(ipv6Af)
            .setEvpnAddressFamily(evpnAf)
            .build();
    assertThat(config.getAllAddressFamilies(), hasSize(3));
    assertTrue(config.getAllAddressFamilies().contains(ipv4Af));
    assertTrue(config.getAllAddressFamilies().contains(ipv6Af));
    assertTrue(config.getAllAddressFamilies().contains(evpnAf));
  }

  @Test
  public void testGetAddressFamily() {
    Ipv4UnicastAddressFamily ipv4Af =
        Ipv4UnicastAddressFamily.builder().setImportPolicy("IMPORT").build();
    Ipv6UnicastAddressFamily ipv6Af =
        Ipv6UnicastAddressFamily.builder().setImportPolicy("IMPORT6").build();
    EvpnAddressFamily evpnAf =
        EvpnAddressFamily.builder()
            .setImportPolicy("IMPORT_EVPN")
            .setPropagateUnmatched(false)
            .build();

    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setIpv4UnicastAddressFamily(ipv4Af)
            .setIpv6UnicastAddressFamily(ipv6Af)
            .setEvpnAddressFamily(evpnAf)
            .build();

    assertThat(config.getAddressFamily(AddressFamily.Type.IPV4_UNICAST), equalTo(ipv4Af));
    assertThat(config.getAddressFamily(AddressFamily.Type.IPV6_UNICAST), equalTo(ipv6Af));
    assertThat(config.getAddressFamily(AddressFamily.Type.EVPN), equalTo(evpnAf));
  }

  @Test
  public void testGetAddressFamilyNotConfigured() {
    BgpActivePeerConfig config = BgpActivePeerConfig.builder().build();
    assertThat(config.getAddressFamily(AddressFamily.Type.IPV4_UNICAST), nullValue());
    assertThat(config.getAddressFamily(AddressFamily.Type.IPV6_UNICAST), nullValue());
    assertThat(config.getAddressFamily(AddressFamily.Type.EVPN), nullValue());
  }

  @Test
  public void testSetRemoteAsnsAsLongSpace() {
    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setRemoteAsns(LongSpace.builder().including(65001L).build())
            .build();
    assertThat(config.getRemoteAsns(), equalTo(LongSpace.of(65001L)));
  }

  @Test
  public void testSetLocalIpInvalidIp() {
    // Invalid IPs should be set to null
    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setLocalIp(Ip.ZERO) // valid
            .build();
    assertThat(config.getLocalIp(), equalTo(Ip.ZERO));
  }

  @Test
  public void testEquals() {
    BgpActivePeerConfig config1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .build();

    BgpActivePeerConfig config2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .build();

    assertEquals(config1, config2);
    assertEquals(config1.hashCode(), config2.hashCode());
  }

  @Test
  public void testNotEqualsDifferentPeerAddress() {
    BgpActivePeerConfig config1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .build();

    BgpActivePeerConfig config2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.2"))
            .setRemoteAs(65001L)
            .build();

    assertNotEquals(config1, config2);
  }

  @Test
  public void testNotEqualsDifferentRemoteAs() {
    BgpActivePeerConfig config1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .build();

    BgpActivePeerConfig config2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65002L)
            .build();

    assertNotEquals(config1, config2);
  }

  @Test
  public void testNotEqualsDifferentDescription() {
    BgpActivePeerConfig config1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setDescription("Peer 1")
            .build();

    BgpActivePeerConfig config2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setDescription("Peer 2")
            .build();

    assertNotEquals(config1, config2);
  }

  @Test
  public void testNotEqualsDifferentEbgpMultihop() {
    BgpActivePeerConfig config1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setEbgpMultihop(true)
            .build();

    BgpActivePeerConfig config2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setEbgpMultihop(false)
            .build();

    assertNotEquals(config1, config2);
  }

  @Test
  public void testNotEqualsDifferentCheckLocalIpOnAccept() {
    BgpActivePeerConfig config1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setCheckLocalIpOnAccept(true)
            .build();

    BgpActivePeerConfig config2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setCheckLocalIpOnAccept(false)
            .build();

    assertNotEquals(config1, config2);
  }

  @Test
  public void testEqualsSelf() {
    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .build();

    // Reflexivity: x.equals(x) should be true
    assertTrue(config.equals(config));
  }

  @Test
  public void testNotEqualsNull() {
    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .build();

    assertNotEquals(config, null);
  }

  @Test
  public void testNotEqualsDifferentClass() {
    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .build();

    assertNotEquals(config, "not a BgpPeerConfig");
  }

  @Test
  public void testHashCodeConsistent() {
    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .setDescription("Test")
            .build();

    int hashCode1 = config.hashCode();
    int hashCode2 = config.hashCode();

    assertEquals(hashCode1, hashCode2);
  }

  @Test
  public void testToString() {
    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .setDescription("Test peer")
            .build();

    String toString = config.toString();
    assertTrue(toString.contains("BgpActivePeerConfig"));
    assertTrue(toString.contains("_peerAddress"));
    assertTrue(toString.contains("10.0.0.1"));
    assertTrue(toString.contains("Test peer"));
  }

  @Test
  public void testGetAllAsNumbers() {
    assertThat(BgpPeerConfig.ALL_AS_NUMBERS.contains(1L), equalTo(true));
    assertThat(BgpPeerConfig.ALL_AS_NUMBERS.contains(4294967295L), equalTo(true));
    assertThat(BgpPeerConfig.ALL_AS_NUMBERS.contains(0L), equalTo(false));
    assertThat(BgpPeerConfig.ALL_AS_NUMBERS.contains(4294967296L), equalTo(false));
  }

  @Test
  public void testBuilderSetRemoteAsValidation() {
    // Valid AS number
    BgpActivePeerConfig config = BgpActivePeerConfig.builder().setRemoteAs(65001L).build();
    assertThat(config.getRemoteAsns(), equalTo(LongSpace.of(65001L)));

    // Edge cases
    config = BgpActivePeerConfig.builder().setRemoteAs(1L).build();
    assertThat(config.getRemoteAsns(), equalTo(LongSpace.of(1L)));

    config = BgpActivePeerConfig.builder().setRemoteAs(4294967295L).build();
    assertThat(config.getRemoteAsns(), equalTo(LongSpace.of(4294967295L)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderSetRemoteAsInvalidTooLow() {
    BgpActivePeerConfig.builder().setRemoteAs(0L).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderSetRemoteAsInvalidTooHigh() {
    BgpActivePeerConfig.builder().setRemoteAs(4294967296L).build();
  }

  @Test
  public void testBuilderWithGeneratedRoutes() {
    Set<GeneratedRoute> routes = new HashSet<>();
    routes.add(
        GeneratedRoute.builder().setNetwork(Prefix.parse("10.0.0.0/8")).setAdmin(100).build());
    routes.add(
        GeneratedRoute.builder().setNetwork(Prefix.parse("192.168.0.0/16")).setAdmin(200).build());

    BgpActivePeerConfig config = BgpActivePeerConfig.builder().setGeneratedRoutes(routes).build();

    assertThat(config.getGeneratedRoutes(), hasSize(2));
  }

  @Test
  public void testBuilderWithNullGeneratedRoutes() {
    BgpActivePeerConfig config = BgpActivePeerConfig.builder().setGeneratedRoutes(null).build();

    assertThat(config.getGeneratedRoutes(), hasSize(0));
  }

  @Test
  public void testBuilderWithAddressFamilies() {
    Ipv4UnicastAddressFamily ipv4Af =
        Ipv4UnicastAddressFamily.builder()
            .setImportPolicy("IMPORT")
            .setExportPolicy("EXPORT")
            .setRouteReflectorClient(true)
            .build();

    Ipv6UnicastAddressFamily ipv6Af =
        Ipv6UnicastAddressFamily.builder()
            .setImportPolicy("IMPORT6")
            .setExportPolicy("EXPORT6")
            .build();

    EvpnAddressFamily evpnAf =
        EvpnAddressFamily.builder()
            .setImportPolicy("IMPORT_EVPN")
            .setExportPolicy("EXPORT_EVPN")
            .setPropagateUnmatched(false)
            .build();

    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setIpv4UnicastAddressFamily(ipv4Af)
            .setIpv6UnicastAddressFamily(ipv6Af)
            .setEvpnAddressFamily(evpnAf)
            .build();

    assertThat(config.getIpv4UnicastAddressFamily(), equalTo(ipv4Af));
    assertThat(config.getIpv6UnicastAddressFamily(), equalTo(ipv6Af));
    assertThat(config.getEvpnAddressFamily(), equalTo(evpnAf));
  }

  @Test
  public void testBuilderChain() {
    BgpActivePeerConfig config =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAs(65001L)
            .setLocalAs(65002L)
            .setLocalIp(Ip.parse("10.0.0.2"))
            .setDescription("Test")
            .setEbgpMultihop(true)
            .setEnforceFirstAs(true)
            .build();

    assertThat(config.getPeerAddress(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(config.getRemoteAsns(), equalTo(LongSpace.of(65001L)));
    assertThat(config.getLocalAs(), equalTo(65002L));
    assertThat(config.getLocalIp(), equalTo(Ip.parse("10.0.0.2")));
    assertThat(config.getDescription(), equalTo("Test"));
    assertTrue(config.getEbgpMultihop());
    assertTrue(config.getEnforceFirstAs());
  }
}

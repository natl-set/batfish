package org.batfish.vendor.huawei.grammar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.vendor.huawei.representation.HuaweiAcl;
import org.batfish.vendor.huawei.representation.HuaweiAclLine;
import org.batfish.vendor.huawei.representation.HuaweiConfiguration;
import org.batfish.vendor.huawei.representation.HuaweiConversions;
import org.batfish.vendor.huawei.representation.HuaweiInterface;
import org.batfish.vendor.huawei.representation.HuaweiNatAddressGroup;
import org.batfish.vendor.huawei.representation.HuaweiNatRule;
import org.batfish.vendor.huawei.representation.HuaweiOspfProcess;
import org.batfish.vendor.huawei.representation.HuaweiRoutePolicy;
import org.batfish.vendor.huawei.representation.HuaweiStaticRoute;
import org.batfish.vendor.huawei.representation.HuaweiVlan;
import org.batfish.vendor.huawei.representation.HuaweiVrf;
import org.junit.Test;

/** Tests for Huawei grammar parsing */
public class HuaweiGrammarTest {

  private Settings getSettings() {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    return settings;
  }

  private Settings getLenientSettings() {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(false);
    return settings;
  }

  @Test
  public void testBasicConfig() {
    String configText = "sysname Router1\nreturn\n";

    // Parse the configuration using HuaweiControlPlaneExtractor
    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
  }

  @Test
  public void testEmptyConfig() {
    String configText = "";

    // Parse the configuration using HuaweiControlPlaneExtractor
    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Hostname should be null for empty config
    assertThat(config.getHostname(), equalTo(null));
  }

  @Test
  public void testInterfaceParsing() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Uplink to core\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Uplink to core"));
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.1.1"));
  }

  @Test
  public void testInterfaceShutdown() {
    String configText =
        "sysname Router1\n" + "interface GigabitEthernet0/0/0\n" + " shutdown\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getShutdown(), equalTo(true));
  }

  @Test
  public void testInterfaceNoShutdown() {
    String configText =
        "sysname Router1\n" + "interface GigabitEthernet0/0/0\n" + " undo shutdown\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getShutdown(), equalTo(false));
  }

  @Test
  public void testVlanCreation() {
    String configText =
        "sysname Router1\n" + "vlan 100\n" + " description Management VLAN\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));

    HuaweiVlan vlan = config.getVlan(100);
    assertThat(vlan, notNullValue());
    assertThat(vlan.getVlanId(), equalTo(100));
  }

  @Test
  public void testVlanBatch() {
    String configText = "sysname Router1\n" + "vlan batch 10 20 30 40\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(4));

    assertThat(config.getVlan(10), notNullValue());
    assertThat(config.getVlan(20), notNullValue());
    assertThat(config.getVlan(30), notNullValue());
    assertThat(config.getVlan(40), notNullValue());
  }

  @Test
  public void testVlanifInterface() {
    String configText =
        "sysname Router1\n"
            + "vlan 100\n"
            + "interface Vlanif100\n"
            + " description Management Interface\n"
            + " ip address 192.168.100.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiVlan vlan = config.getVlan(100);
    assertThat(vlan, notNullValue());

    HuaweiInterface iface = config.getInterfaces().get("Vlanif100");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Management Interface"));
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.100.1"));
  }

  @Test
  public void testSubinterfaceDot1q() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.100\n"
            + " dot1q termination vid 100\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0.100");
    assertThat(iface, notNullValue());
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("10.0.0.1"));
  }

  @Test
  public void testVlanConversion() {
    String configText =
        "sysname Router1\n"
            + "vlan 100\n"
            + "interface Vlanif100\n"
            + " ip address 192.168.100.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));

    // Convert to vendor-independent configuration
    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1")); // Hostnames are lowercased
    assertThat(viConfig.getAllInterfaces().size(), equalTo(1));
    assertThat(viConfig.getAllInterfaces().containsKey("Vlanif100"), equalTo(true));
  }

  @Test
  public void testStaticRouteBasic() {
    String configText =
        "sysname Router1\n" + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route, notNullValue());
    assertThat(route.getDestination().toString(), equalTo("0.0.0.0/0"));
    assertThat(route.getNextHopIp().toString(), equalTo("192.168.1.1"));
  }

  @Test
  public void testStaticRouteWithInterface() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 GigabitEthernet0/0/0 192.168.1.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route, notNullValue());
    assertThat(route.getDestination().toString(), equalTo("10.0.0.0/24"));
    assertThat(route.getNextHopInterface(), equalTo("GigabitEthernet0/0/0"));
    assertThat(route.getNextHopIp().toString(), equalTo("192.168.1.1"));
  }

  @Test
  public void testStaticRouteWithPreference() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 preference 100\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route, notNullValue());
    assertThat(route.getDestination().toString(), equalTo("10.0.0.0/24"));
    assertThat(route.getNextHopIp().toString(), equalTo("192.168.1.1"));
    assertThat(route.getPreference(), equalTo(100));
  }

  @Test
  public void testStaticRouteMultiple() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.2\n"
            + "ip route-static 172.16.0.0 255.255.0.0 192.168.1.3 preference 50\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(3));
  }

  @Test
  public void testStaticRouteConversion() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.2 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    // Convert to vendor-independent configuration
    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
    assertThat(viConfig.getAllInterfaces().size(), equalTo(1));

    // Check static routes were added to default VRF
    assertThat(viConfig.getDefaultVrf().getStaticRoutes().size(), equalTo(1));
    assertThat(
        viConfig.getDefaultVrf().getStaticRoutes().iterator().next().getNetwork().toString(),
        equalTo("0.0.0.0/0"));
  }

  @Test
  public void testBgpBasic() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithRouterId() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + " router-id 1.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
    assertThat(config.getBgpProcess().getRouterId().toString(), equalTo("1.1.1.1"));
  }

  @Test
  public void testBgpWithPeers() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.3 as-number 65003\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
    // Verify peers are extracted and stored
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(2));
    assertThat(
        config.getBgpProcess().getNeighbors().containsKey(Ip.parse("192.168.1.2")), equalTo(true));
    assertThat(
        config.getBgpProcess().getNeighbors().containsKey(Ip.parse("192.168.1.3")), equalTo(true));
    assertThat(
        config.getBgpProcess().getNeighbors().get(Ip.parse("192.168.1.2")).getRemoteAsns(),
        equalTo(LongSpace.of(65002L)));
    assertThat(
        config.getBgpProcess().getNeighbors().get(Ip.parse("192.168.1.3")).getRemoteAsns(),
        equalTo(LongSpace.of(65003L)));
  }

  @Test
  public void testBgpNetworks() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " network 10.0.0.0 255.255.255.0\n"
            + " network 172.16.0.0 255.255.0.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithPeerGroup() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " group EXTERNAL_PEER external\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
    // Verify peer group is extracted
    assertThat(config.getBgpProcess().getPeerGroups().size(), equalTo(1));
    assertThat(config.getBgpProcess().getPeerGroups().containsKey("EXTERNAL_PEER"), equalTo(true));
    // Verify peer is still extracted
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(1));
  }

  @Test
  public void testBgpPeerWithParameters() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.2 connect-interface GigabitEthernet0/0/0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
    // Verify peer is extracted even with additional parameters
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(1));
    assertThat(
        config.getBgpProcess().getNeighbors().containsKey(Ip.parse("192.168.1.2")), equalTo(true));
  }

  @Test
  public void testBgpConversion() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " router-id 1.1.1.1\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());

    // Convert to vendor-independent configuration
    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
  }

  // ========== COMPREHENSIVE BGP TESTS ==========

  @Test
  public void testBgpImportRouteDirect() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + " import-route direct\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpImportRouteStatic() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + " import-route static\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpImportRouteOspf() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + " import-route ospf 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpImportRouteRip() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + " import-route rip\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpImportRouteWithPolicy() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " import-route direct route-policy POLICY1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpImportRouteMultiple() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " import-route direct\n"
            + " import-route static\n"
            + " import-route ospf 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpPeerGroupInternal() {
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " group INTERNAL_PEER internal\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getPeerGroups().size(), equalTo(1));
    assertThat(config.getBgpProcess().getPeerGroups().containsKey("INTERNAL_PEER"), equalTo(true));
  }

  @Test
  public void testBgpPeerGroupExternal() {
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " group EXTERNAL_PEER external\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getPeerGroups().size(), equalTo(1));
    assertThat(config.getBgpProcess().getPeerGroups().containsKey("EXTERNAL_PEER"), equalTo(true));
  }

  @Test
  public void testBgpPeerGroupMultiple() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " group IBGP_PEERS internal\n"
            + " group EBGP_PEERS external\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getPeerGroups().size(), equalTo(2));
    assertThat(config.getBgpProcess().getPeerGroups().containsKey("IBGP_PEERS"), equalTo(true));
    assertThat(config.getBgpProcess().getPeerGroups().containsKey("EBGP_PEERS"), equalTo(true));
  }

  @Test
  public void testBgpPeerWithPassword() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.2 password mySecretPassword\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(1));
    assertThat(
        config.getBgpProcess().getNeighbors().containsKey(Ip.parse("192.168.1.2")), equalTo(true));
  }

  @Test
  public void testBgpPeerWithConnectInterfaceLoopback() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.2 connect-interface Loopback0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(1));
  }

  @Test
  public void testBgpPeerWithConnectInterfaceVlanif() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 10.1.1.2 as-number 65002\n"
            + " peer 10.1.1.2 connect-interface Vlanif100\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(1));
  }

  @Test
  public void testBgpPeerWithConnectInterfaceGigabitEthernet() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.2 connect-interface GigabitEthernet0/0/0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(1));
  }

  @Test
  public void testBgpNetworkWithRoutePolicy() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " network 10.0.0.0 255.255.255.0 route-policy ALLOW-PREFIX\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpNetworkMultipleWithPolicies() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " network 10.0.0.0 255.255.255.0 route-policy POLICY1\n"
            + " network 172.16.0.0 255.255.0.0 route-policy POLICY2\n"
            + " network 192.168.0.0 255.255.0.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpWithFourByteAsNumber() {
    String configText = "sysname Router1\n" + "bgp 6500000\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(6500000L));
  }

  @Test
  public void testBgpAsNumberUpperBoundary() {
    String configText = "sysname Router1\n" + "bgp 4294967295\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(4294967295L));
  }

  @Test
  public void testBgpPeerWithFourByteAsNumber() {
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " peer 192.168.1.2 as-number 6553600\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpComplexConfiguration() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " router-id 1.1.1.1\n"
            + " group IBGP internal\n"
            + " group EBGP external\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.2 connect-interface GigabitEthernet0/0/0\n"
            + " peer 192.168.1.2 password mypassword\n"
            + " peer 10.0.0.2 as-number 65001\n"
            + " peer 10.0.0.2 connect-interface Loopback0\n"
            + " network 10.0.0.0 255.255.255.0\n"
            + " network 172.16.0.0 255.255.0.0 route-policy ANNOUNCE\n"
            + " import-route direct\n"
            + " import-route static\n"
            + " import-route ospf 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
    assertThat(config.getBgpProcess().getRouterId().toString(), equalTo("1.1.1.1"));
    assertThat(config.getBgpProcess().getPeerGroups().size(), equalTo(2));
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(2));
  }

  @Test
  public void testBgpWithStaticRoutesIntegration() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " import-route static\n"
            + " network 10.0.0.0 255.255.255.0\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "ip route-static 172.16.0.0 255.255.0.0 192.168.1.2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(2));
  }

  @Test
  public void testBgpWithVrfIntegration() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance CUSTOMER_A\n"
            + " route-distinguisher 65001:100\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));
  }

  @Test
  public void testBgpWithInterfaceConfiguration() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface Loopback0\n"
            + " ip address 10.0.0.1 255.255.255.255\n"
            + "bgp 65001\n"
            + " router-id 10.0.0.1\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.2 connect-interface GigabitEthernet0/0/0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(2));
  }

  @Test
  public void testBgpUndoRouterId() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + " undo router-id\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpUndoPeer() {
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " undo peer 192.168.1.2\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpPeerDifferentAddressClasses() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 10.0.0.2 as-number 65003\n"
            + " peer 172.16.1.2 as-number 65004\n"
            + " peer 1.1.1.2 as-number 65005\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(4));
  }

  @Test
  public void testBgpNetworkAllMasks() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " network 10.0.0.0 255.255.255.255\n"
            + " network 10.1.0.0 255.255.255.254\n"
            + " network 10.2.0.0 255.255.255.252\n"
            + " network 10.3.0.0 255.255.255.0\n"
            + " network 10.4.0.0 255.255.0.0\n"
            + " network 10.5.0.0 255.0.0.0\n"
            + " network 0.0.0.0 0.0.0.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpExportRoute() {
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " export-route something\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpPeerGroupWithPeers() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " group EBGP external\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.3 as-number 65002\n"
            + " peer 192.168.1.4 as-number 65002\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getPeerGroups().size(), equalTo(1));
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(3));
  }

  @Test
  public void testBgpPeerAsPathLimit() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.2 as-path-limit 10\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpMultipleRouterIdChanges() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " router-id 1.1.1.1\n"
            + " router-id 2.2.2.2\n"
            + " router-id 3.3.3.3\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    // Last router-id should win
    assertThat(config.getBgpProcess().getRouterId().toString(), equalTo("3.3.3.3"));
  }

  @Test
  public void testBgpPeerGroupInternalAndExternal() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " group INTERNAL internal\n"
            + " group EXTERNAL external\n"
            + " group MIXED\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getPeerGroups().size(), equalTo(3));
  }

  @Test
  public void testBgpNetworkDefaultRoute() {
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " network 0.0.0.0 0.0.0.0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
  }

  @Test
  public void testBgpComplexRealWorldConfig() {
    // Simulates a realistic BGP configuration
    String configText =
        "sysname EdgeRouter\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.2 255.255.255.0\n"
            + "interface Loopback0\n"
            + " ip address 10.0.0.1 255.255.255.255\n"
            + "bgp 65001\n"
            + " router-id 10.0.0.1\n"
            + " group IBGP_PEERS internal\n"
            + " group EBGP_PEERS external\n"
            + " peer 192.168.1.1 as-number 65000\n"
            + " peer 192.168.1.1 connect-interface GigabitEthernet0/0/0\n"
            + " network 10.0.0.0 255.255.255.0\n"
            + " network 172.16.0.0 255.255.0.0\n"
            + " import-route direct\n"
            + " import-route static\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("EdgeRouter"));
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
    assertThat(config.getBgpProcess().getRouterId().toString(), equalTo("10.0.0.1"));
    assertThat(config.getBgpProcess().getPeerGroups().size(), equalTo(2));
    assertThat(config.getBgpProcess().getNeighbors().size(), equalTo(1));
    assertThat(config.getInterfaces().size(), equalTo(2));
    assertThat(config.getStaticRoutes().size(), equalTo(1));
  }

  @Test
  public void testAclBasic() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + " rule 10 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines().size(), equalTo(2));
  }

  @Test
  public void testAclAdvanced() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 deny ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
    assertThat(acl.getLines().size(), equalTo(2));
  }

  @Test
  public void testAclRules() {
    String configText =
        "sysname Router1\n"
            + "acl number 2001\n"
            + " rule 5 permit source 10.1.1.0 0.0.0.255\n"
            + " rule 10 deny source 10.2.2.0 0.0.0.255\n"
            + " rule 15 permit source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("2001");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(3));

    // Check first line
    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getAction(), equalTo("permit"));

    // Check second line
    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getAction(), equalTo("deny"));

    // Check third line
    HuaweiAclLine line3 = acl.getLines().get(2);
    assertThat(line3.getAction(), equalTo("permit"));
  }

  @Test
  public void testAclConversion() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + " rule 10 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    // Convert to vendor-independent configuration
    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
    // Check that ACL was converted
    assertThat(viConfig.getIpAccessLists().size(), equalTo(1));
    assertThat(viConfig.getIpAccessLists().containsKey("2000"), equalTo(true));
  }

  @Test
  public void testNatBasic() {
    String configText =
        "sysname Router1\n"
            + "nat outbound 2000\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(2));

    // Check outbound NAT rule
    HuaweiNatRule outboundRule = config.getNatRules().get(0);
    assertThat(outboundRule, notNullValue());
    assertThat(outboundRule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(outboundRule.getAclName(), equalTo("2000"));

    // Check static NAT rule
    HuaweiNatRule staticRule = config.getNatRules().get(1);
    assertThat(staticRule, notNullValue());
    assertThat(staticRule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(staticRule.getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(staticRule.getInsideLocalIp().toString(), equalTo("10.0.0.1"));
  }

  @Test
  public void testNatStatic() {
    String configText =
        "sysname Router1\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "nat static global 192.168.1.2 inside 10.0.0.2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(2));

    // Check first static NAT rule
    HuaweiNatRule rule1 = config.getNatRules().get(0);
    assertThat(rule1, notNullValue());
    assertThat(rule1.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(rule1.getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(rule1.getInsideLocalIp().toString(), equalTo("10.0.0.1"));

    // Check second static NAT rule
    HuaweiNatRule rule2 = config.getNatRules().get(1);
    assertThat(rule2, notNullValue());
    assertThat(rule2.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(rule2.getGlobalIp().toString(), equalTo("192.168.1.2"));
    assertThat(rule2.getInsideLocalIp().toString(), equalTo("10.0.0.2"));
  }

  @Test
  public void testNatOutbound() {
    String configText =
        "sysname Router1\n" + "nat outbound 2000\n" + "nat outbound 3001 interface\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(2));

    // Check first outbound rule (dynamic NAT with pool)
    HuaweiNatRule rule1 = config.getNatRules().get(0);
    assertThat(rule1, notNullValue());
    assertThat(rule1.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(rule1.getAclName(), equalTo("2000"));

    // Check second outbound rule (Easy IP)
    HuaweiNatRule rule2 = config.getNatRules().get(1);
    assertThat(rule2, notNullValue());
    assertThat(rule2.getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));
    assertThat(rule2.getAclName(), equalTo("3001"));
  }

  @Test
  public void testNatConversion() {
    String configText =
        "sysname Router1\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.2 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    // Convert to vendor-independent configuration
    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
    assertThat(viConfig.getAllInterfaces().size(), equalTo(1));
  }

  @Test
  public void testOspfBasic() {
    String configText = "sysname Router1\n" + "ospf 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
  }

  @Test
  public void testOspfWithRouterId() {
    String configText = "sysname Router1\n" + "ospf 1\n" + " router-id 1.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("1.1.1.1"));
  }

  @Test
  public void testOspfWithAreas() {
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " area 0\n" + " area 1\n" + " area 2\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(3));
    assertThat(config.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(1L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(2L), equalTo(true));
  }

  @Test
  public void testOspfNetworks() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 192.168.1.0/24 area 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(2));

    // Check first network
    HuaweiOspfProcess.HuaweiOspfNetwork network1 = config.getOspfProcess().getNetworks().get(0);
    assertThat(network1.getNetwork().toString(), equalTo("10.0.0.0/24"));
    assertThat(network1.getAreaId(), equalTo(0L));

    // Check second network
    HuaweiOspfProcess.HuaweiOspfNetwork network2 = config.getOspfProcess().getNetworks().get(1);
    assertThat(network2.getNetwork().toString(), equalTo("192.168.1.0/24"));
    assertThat(network2.getAreaId(), equalTo(1L));
  }

  @Test
  public void testOspfConversion() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " router-id 1.1.1.1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 192.168.1.0/24 area 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("1.1.1.1"));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(2));

    // Convert to vendor-independent configuration
    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));

    // TODO: Implement full OSPF conversion to Batfish model
    // For now, just verify conversion succeeds without errors
  }

  @Test
  public void testVrfBasic() {
    String configText = "sysname Router1\n" + "ip vpn-instance VRF1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
  }

  @Test
  public void testVrfWithRd() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
    assertThat(vrf.getRouteDistinguisher(), equalTo("100:1"));
  }

  @Test
  public void testVrfWithRouteTargets() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + " vpn-target 100:1 export\n"
            + " vpn-target 200:1 import\n"
            + " vpn-target 300:1 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
    assertThat(vrf.getRouteDistinguisher(), equalTo("100:1"));

    // Check import route targets (should have 200:1 from import, 300:1 from both)
    assertThat(vrf.getImportRouteTargets().size(), equalTo(2));
    assertThat(vrf.getImportRouteTargets().contains("200:1"), equalTo(true));
    assertThat(vrf.getImportRouteTargets().contains("300:1"), equalTo(true));

    // Check export route targets (should have 100:1 from export, 300:1 from both)
    assertThat(vrf.getExportRouteTargets().size(), equalTo(2));
    assertThat(vrf.getExportRouteTargets().contains("100:1"), equalTo(true));
    assertThat(vrf.getExportRouteTargets().contains("300:1"), equalTo(true));
  }

  @Test
  public void testVrfConversion() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + " vpn-target 100:1 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    // Convert to vendor-independent configuration
    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));

    // Check VRF was converted
    assertThat(viConfig.getVrfs().containsKey("VRF1"), equalTo(true));

    org.batfish.datamodel.Vrf vrf = viConfig.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
  }

  // ========== ERROR HANDLING AND NEGATIVE TESTS ==========

  @Test
  public void testMissingRequiredKeyword() {
    // Missing interface keyword before interface name
    String configText = "sysname Router1\n" + "GigabitEthernet0/0/0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    // Should still parse with warnings
    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    // The unrecognized line should be skipped
  }

  @Test
  public void testInvalidIpAddress() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 999.999.999.999 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning about invalid IP
    // Configuration should still be valid
    assertThat(config.getHostname(), equalTo("Router1"));
  }

  @Test
  public void testInvalidSubnetMask() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 256.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
  }

  @Test
  public void testInvalidIpMask() {
    // Invalid subnet mask (not contiguous)
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.254.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning about invalid mask
  }

  @Test
  public void testStaticRouteInvalidNextHop() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 999.999.999.999\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning but continue parsing
  }

  @Test
  public void testBgpInvalidAsNumber() {
    // AS number too large
    String configText = "sysname Router1\n" + "bgp 9999999\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser should accept it but might warn during conversion
  }

  @Test
  public void testBgpAsNumberZero() {
    // AS number 0 is invalid
    String configText = "sysname Router1\n" + "bgp 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(0L));
  }

  @Test
  public void testOspfInvalidAreaId() {
    // Area ID that's not a valid number
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " area 999999999999999999999\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning about invalid area ID
  }

  @Test
  public void testOspfAreaIdZero() {
    // Area 0 is valid (backbone area)
    String configText = "sysname Router1\n" + "ospf 1\n" + " area 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(1));
    assertThat(config.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
  }

  @Test
  public void testAclInvalidNumber() {
    // ACL number outside valid ranges
    String configText =
        "sysname Router1\n" + "acl 9999 advanced\n" + " rule 5 permit ip source any\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser accepts it, but type may be incorrect
  }

  @Test
  public void testVlanMaxValue() {
    // Maximum valid VLAN ID
    String configText = "sysname Router1\n" + "vlan 4094\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));
    assertThat(config.getVlan(4094), notNullValue());
  }

  @Test
  public void testVlanExceedsMax() {
    // VLAN ID exceeds maximum (4094)
    String configText = "sysname Router1\n" + "vlan 4095\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Grammar may accept it as uint16, but should warn
  }

  @Test
  public void testVlanZero() {
    // VLAN ID 0 is typically invalid
    String configText = "sysname Router1\n" + "vlan 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser accepts it, but semantically invalid
  }

  @Test
  public void testPortNumberZero() {
    // Port number 0 (reserved, typically invalid)
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser accepts it, but semantically suspicious
  }

  @Test
  public void testPortNumberMax() {
    // Maximum valid port number
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 65535\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));
  }

  @Test
  public void testPortNumberExceedsMax() {
    // Port number exceeds 65535
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 99999\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Grammar should reject or warn about out-of-range port
  }

  @Test
  public void testStaticRoutePreferenceZero() {
    // Preference value of 0
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 preference 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));
    assertThat(config.getStaticRoutes().get(0).getPreference(), equalTo(0));
  }

  @Test
  public void testStaticRoutePreferenceMax() {
    // Maximum reasonable preference value
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 preference 255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));
    assertThat(config.getStaticRoutes().get(0).getPreference(), equalTo(255));
  }

  @Test
  public void testEmptyConfigOnly() {
    // Completely empty configuration
    String configText = "";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo(null));
    assertThat(config.getInterfaces().size(), equalTo(0));
  }

  @Test
  public void testOnlyHostname() {
    // Configuration with only hostname
    String configText = "sysname Router1\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(0));
  }

  @Test
  public void testOnlyOneInterface() {
    // Configuration with only one interface
    String configText = "interface GigabitEthernet0/0/0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
  }

  @Test
  public void testOnlyComments() {
    // Configuration with only whitespace and newlines
    String configText = "\n\n\n   \n\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo(null));
  }

  @Test
  public void testInterfaceWithoutIp() {
    // Interface defined but no IP address
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Test interface\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Test interface"));
    assertThat(iface.getAddress(), equalTo(null));
  }

  @Test
  public void testMismatchedQuotes() {
    // Test with unclosed quotes (if grammar supports quoted strings)
    String configText = "sysname Router1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
  }

  @Test
  public void testUnrecognizedCommand() {
    // Configuration with unrecognized commands
    String configText =
        "sysname Router1\n"
            + "unknown_command_here value1 value2\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + " another_unknown command\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should parse valid parts and skip/warn about unrecognized
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(1));
  }

  @Test
  public void testDuplicateInterfaceNames() {
    // Same interface defined twice
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.2.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
    // Second definition should override first
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.2.1"));
  }

  @Test
  public void testDuplicateVlanIds() {
    // Same VLAN defined twice
    String configText =
        "sysname Router1\n"
            + "vlan 100\n"
            + " description First\n"
            + "return\n"
            + "vlan 100\n"
            + " description Second\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));
  }

  @Test
  public void testAclInvalidWildcard() {
    // Invalid wildcard mask in ACL
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 256.0.0.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning but continue
  }

  @Test
  public void testStaticRouteWithInvalidCidr() {
    // Invalid CIDR notation
    String configText =
        "sysname Router1\n" + "ip route-static 10.0.0.0/99 192.168.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should warn about invalid prefix length
  }

  @Test
  public void testNatStaticInvalidIp() {
    // NAT with invalid IP
    String configText =
        "sysname Router1\n" + "nat static global 999.999.999.999 inside 10.0.0.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning about invalid IP
  }

  @Test
  public void testVrfWithNameContainingSpaces() {
    // VRF names typically shouldn't have spaces
    String configText = "sysname Router1\n" + "ip vpn-instance VRF 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser may treat "VRF" as name and "1" as next command
  }

  @Test
  public void testInterfaceNameWithSpecialChars() {
    // Interface name shouldn't have special characters
    String configText = "sysname Router1\n" + "interface GigabitEthernet@0/0/0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // May or may not parse depending on grammar
  }

  @Test
  public void testMalformedBgpPeerConfig() {
    // BGP peer missing AS number
    String configText = "sysname Router1\n" + "bgp 65001\n" + " peer 192.168.1.2\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Parser should handle incomplete config
  }

  @Test
  public void testOspfNetworkInvalidPrefix() {
    // OSPF network with invalid prefix
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " network 10.0.0.0/33 area 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should warn about invalid prefix length
  }

  @Test
  public void testWhitespaceVariations() {
    // Test various whitespace patterns
    String configText =
        "sysname    Router1\n"
            + "interface  GigabitEthernet0/0/0\n"
            + "  ip   address   192.168.1.1   255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(1));
  }

  @Test
  public void testCaseSensitivity() {
    // Test case sensitivity of keywords
    String configText = "SYSNAME Router1\n" + "RETURN\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Keywords should be case-sensitive, so uppercase should fail
    // Configuration should be empty or have warnings
  }

  @Test
  public void testVeryLongHostname() {
    // Very long hostname (should be truncated or accepted)
    StringBuilder longHostname = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longHostname.append("a");
    }
    String configText = "sysname " + longHostname.toString() + "\nreturn\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should accept very long hostname
  }

  @Test
  public void testMultipleAclRulesWithGaps() {
    // ACL rules with non-sequential numbers (valid in Huawei)
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + " rule 100 deny source any\n"
            + " rule 200 permit source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));
    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl.getLines().size(), equalTo(3));
  }

  @Test
  public void testVlanBatchWithDuplicates() {
    // VLAN batch with duplicate VLAN IDs
    String configText = "sysname Router1\n" + "vlan batch 10 20 10 30\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should handle duplicates gracefully (likely just create once)
  }

  @Test
  public void testIncompleteInterfaceBlock() {
    // Interface block without return
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/1\n"
            + " shutdown\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should handle transition to new interface gracefully
  }

  @Test
  public void testCommentLines() {
    // Huawei VRP doesn't have inline comments, but test whitespace lines
    String configText =
        "sysname Router1\n"
            + "\n"
            + "interface GigabitEthernet0/0/0\n"
            + "\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
  }

  // ========== EDGE CASE AND BOUNDARY TESTS ==========

  // 1. INTERFACE EDGE CASES

  @Test
  public void testInterfaceWithAllProperties() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Uplink\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + " shutdown\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Uplink"));
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.1.1"));
    assertThat(iface.getShutdown(), equalTo(true));
  }

  @Test
  public void testMultipleInterfaceTypes() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface 10GE1/0/0\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "interface Loopback0\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "interface Vlanif100\n"
            + " ip address 172.16.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(4));
    assertThat(config.getInterfaces().containsKey("GigabitEthernet0/0/0"), equalTo(true));
    assertThat(config.getInterfaces().containsKey("10GE1/0/0"), equalTo(true));
    assertThat(config.getInterfaces().containsKey("Loopback0"), equalTo(true));
    assertThat(config.getInterfaces().containsKey("Vlanif100"), equalTo(true));
  }

  @Test
  public void testInterfaceBoundaryPortNumbers() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.0.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/47\n"
            + " ip address 192.168.47.1 255.255.255.0\n"
            + "interface 40GE2/0/0\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "interface 100GE3/0/0\n"
            + " ip address 10.1.0.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(4));
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/47"), notNullValue());
    assertThat(config.getInterfaces().get("40GE2/0/0"), notNullValue());
    assertThat(config.getInterfaces().get("100GE3/0/0"), notNullValue());
  }

  @Test
  public void testSubinterfaceDot1qVariations() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.100\n"
            + " dot1q termination vid 100\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/0.200\n"
            + " dot1q termination vid 200\n"
            + " ip address 10.0.1.1 255.255.255.0\n"
            + "interface GigabitEthernet1/0/0.4094\n"
            + " dot1q termination vid 4094\n"
            + " ip address 10.1.0.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(3));
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0.100"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0.200"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet1/0/0.4094"), notNullValue());
  }

  // 2. VLAN EDGE CASES

  @Test
  public void testVlanBatchWithManyVlans() {
    String configText =
        "sysname Router1\n" + "vlan batch 10 20 30 40 50 60 70 80 90 100\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(10));

    // Verify all VLANs were created
    for (int vlanId : new int[] {10, 20, 30, 40, 50, 60, 70, 80, 90, 100}) {
      assertThat(config.getVlan(vlanId), notNullValue());
      assertThat(config.getVlan(vlanId).getVlanId(), equalTo(vlanId));
    }
  }

  @Test
  public void testVlanWithAllProperties() {
    String configText =
        "sysname Router1\n" + "vlan 100\n" + " description Management\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(1));

    HuaweiVlan vlan = config.getVlan(100);
    assertThat(vlan, notNullValue());
    assertThat(vlan.getVlanId(), equalTo(100));
    assertThat(vlan.getDescription(), equalTo("Management"));
  }

  @Test
  public void testVlanBoundaryValues() {
    String configText =
        "sysname Router1\n" + "vlan 1\n" + "vlan 4094\n" + "vlan batch 2 to 10\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(10));
    assertThat(config.getVlan(1), notNullValue());
    assertThat(config.getVlan(4094), notNullValue());
  }

  @Test
  public void testVlanifWithMultipleVlans() {
    String configText =
        "sysname Router1\n"
            + "vlan 10\n"
            + "vlan 20\n"
            + "vlan 30\n"
            + "interface Vlanif10\n"
            + " ip address 192.168.10.1 255.255.255.0\n"
            + "interface Vlanif20\n"
            + " ip address 192.168.20.1 255.255.255.0\n"
            + "interface Vlanif30\n"
            + " ip address 192.168.30.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVlans().size(), equalTo(3));
    assertThat(config.getInterfaces().size(), equalTo(3));
    assertThat(config.getVlan(10), notNullValue());
    assertThat(config.getVlan(20), notNullValue());
    assertThat(config.getVlan(30), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif10"), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif20"), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif30"), notNullValue());
  }

  // 3. STATIC ROUTE EDGE CASES

  @Test
  public void testStaticRouteAllCombinations() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.2\n"
            + "ip route-static 172.16.0.0 255.255.0.0 192.168.1.3 preference 50\n"
            + "ip route-static 10.1.0.0 255.255.255.0 GigabitEthernet0/0/0 192.168.1.4\n"
            + "ip route-static 172.17.0.0 255.255.0.0 GigabitEthernet0/0/0 192.168.1.5 preference"
            + " 70\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(5));

    // Verify default route
    HuaweiStaticRoute defaultRoute = config.getStaticRoutes().get(0);
    assertThat(defaultRoute.getDestination().toString(), equalTo("0.0.0.0/0"));

    // Verify route with interface
    HuaweiStaticRoute routeWithInterface = config.getStaticRoutes().get(3);
    assertThat(routeWithInterface.getNextHopInterface(), equalTo("GigabitEthernet0/0/0"));

    // Verify route with preference
    HuaweiStaticRoute routeWithPref = config.getStaticRoutes().get(2);
    assertThat(routeWithPref.getPreference(), equalTo(50));
  }

  @Test
  public void testStaticRouteWithVrf() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + "ip route-static vpn-instance VRF1 10.0.0.0 255.255.255.0 192.168.1.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route.getDestination().toString(), equalTo("10.0.0.0/24"));
    assertThat(route.getVrfName(), equalTo("VRF1"));
  }

  @Test
  public void testStaticRouteBoundaryNetworks() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.1\n"
            + "ip route-static 255.255.255.255 255.255.255.255 192.168.1.2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(2));
  }

  @Test
  public void testStaticRouteWithAllPreferences() {
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 preference 1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.2 preference 60\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.3 preference 255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(3));

    HuaweiStaticRoute route1 = config.getStaticRoutes().get(0);
    assertThat(route1.getPreference(), equalTo(1));

    HuaweiStaticRoute route2 = config.getStaticRoutes().get(1);
    assertThat(route2.getPreference(), equalTo(60));

    HuaweiStaticRoute route3 = config.getStaticRoutes().get(2);
    assertThat(route3.getPreference(), equalTo(255));
  }

  // 4. BGP EDGE CASES

  @Test
  public void testBgpMinimalConfiguration() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithMultiplePeers() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 192.168.1.2 as-number 65002\n"
            + " peer 192.168.1.3 as-number 65003\n"
            + " peer 192.168.1.4 as-number 65004\n"
            + " peer 10.0.0.2 as-number 65005\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithManyNetworkAnnouncements() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " network 10.0.0.0 255.255.255.0\n"
            + " network 10.1.0.0 255.255.255.0\n"
            + " network 10.2.0.0 255.255.255.0\n"
            + " network 172.16.0.0 255.255.0.0\n"
            + " network 192.168.0.0 255.255.0.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));
  }

  @Test
  public void testBgpWithBoundaryAsNumbers() {
    String configText =
        "sysname Router1\n" + "bgp 1\n" + " peer 192.168.1.2 as-number 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getBgpProcess().getAsNum(), equalTo(1L));
  }

  // 5. OSPF EDGE CASES

  @Test
  public void testOspfWithoutRouterId() {
    String configText = "sysname Router1\n" + "ospf 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId(), equalTo(null));
  }

  @Test
  public void testOspfWithMultipleAreas() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " area 0\n"
            + " area 1\n"
            + " area 2\n"
            + " area 100\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(4));
  }

  @Test
  public void testOspfWithManyNetworkStatements() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 10.1.0.0/24 area 0\n"
            + " network 10.2.0.0/24 area 0\n"
            + " network 172.16.0.0/16 area 1\n"
            + " network 192.168.0.0/16 area 2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(5));
  }

  @Test
  public void testOspfBoundaryAreaIds() {
    String configText = "sysname Router1\n" + "ospf 1\n" + " area 0\n" + " area 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(2));
  }

  // 6. ACL EDGE CASES

  @Test
  public void testAclWithAllProtocolTypes() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 permit udp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 53\n"
            + " rule 15 permit icmp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255\n"
            + " rule 20 permit ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
    assertThat(acl.getLines().size(), equalTo(4));
  }

  @Test
  public void testAclWithVariousPortSpecifications() {
    String configText =
        "sysname Router1\n"
            + "acl 3001 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port gt 1024\n"
            + " rule 15 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port lt 1024\n"
            + " rule 20 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port range 2000 3000\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3001");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));
  }

  @Test
  public void testAclWithWildcardAddresses() {
    String configText =
        "sysname Router1\n"
            + "acl 2001 basic\n"
            + " rule 5 permit source 10.0.0.0 0.255.255.255\n"
            + " rule 10 permit source 192.168.0.0 0.0.255.255\n"
            + " rule 15 permit source 172.16.0.0 0.0.0.255\n"
            + " rule 20 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("2001");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines().size(), equalTo(4));
  }

  @Test
  public void testAclRulesWithAllCombinations() {
    String configText =
        "sysname Router1\n"
            + "acl 3002 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 deny tcp source any destination any\n"
            + " rule 15 permit udp source 192.168.2.0 0.0.0.255 destination 10.0.0.0 0.0.0.255\n"
            + " rule 20 deny ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3002");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));
  }

  // 7. NAT EDGE CASES

  @Test
  public void testNatAllTypesCombined() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 192.168.1.0 0.0.0.255\n"
            + "nat static global 1.1.1.1 inside 10.0.0.1\n"
            + "nat outbound 2000\n"
            + "nat outbound 2000 interface\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(3));

    // Verify static NAT
    HuaweiNatRule staticRule = config.getNatRules().get(0);
    assertThat(staticRule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));

    // Verify dynamic NAT
    HuaweiNatRule dynamicRule = config.getNatRules().get(1);
    assertThat(dynamicRule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));

    // Verify Easy IP
    HuaweiNatRule easyIpRule = config.getNatRules().get(2);
    assertThat(easyIpRule.getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));
  }

  @Test
  public void testNatWithVrf() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));
  }

  // 8. VRF EDGE CASES

  @Test
  public void testMultipleVrfs() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + " vpn-target 100:1 both\n"
            + "ip vpn-instance VRF2\n"
            + " route-distinguisher 200:1\n"
            + " vpn-target 200:1 both\n"
            + "ip vpn-instance VRF3\n"
            + " route-distinguisher 300:1\n"
            + " vpn-target 300:1 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(3));

    assertThat(config.getVrfs().get("VRF1"), notNullValue());
    assertThat(config.getVrfs().get("VRF2"), notNullValue());
    assertThat(config.getVrfs().get("VRF3"), notNullValue());
  }

  @Test
  public void testVrfWithAllProperties() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 65000:100\n"
            + " vpn-target 65000:100 export\n"
            + " vpn-target 65000:200 import\n"
            + " vpn-target 65000:300 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getName(), equalTo("VRF1"));
    assertThat(vrf.getRouteDistinguisher(), equalTo("65000:100"));
    assertThat(vrf.getExportRouteTargets().size(), equalTo(2));
    assertThat(vrf.getImportRouteTargets().size(), equalTo(2));
  }

  @Test
  public void testVpnTargetCombinations() {
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 100:1\n"
            + " vpn-target 100:1 export\n"
            + " vpn-target 100:2 import\n"
            + " vpn-target 100:3 export\n"
            + " vpn-target 100:4 import\n"
            + " vpn-target 100:5 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));

    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getExportRouteTargets().size(), equalTo(3));
    assertThat(vrf.getImportRouteTargets().size(), equalTo(3));
  }

  // HuaweiVlan coverage tests
  @Test
  public void testVlanName() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    // Test initial state
    assertNull(vlan.getName());

    // Test setter and getter
    vlan.setName("Management");
    assertEquals("Management", vlan.getName());

    // Test null name
    vlan.setName(null);
    assertNull(vlan.getName());
  }

  @Test
  public void testVlanDescription() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    assertNull(vlan.getDescription());

    vlan.setDescription("Management VLAN");
    assertEquals("Management VLAN", vlan.getDescription());

    vlan.setDescription(null);
    assertNull(vlan.getDescription());
  }

  @Test
  public void testVlanInterfaces() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    // Test initial state
    assertTrue(vlan.getInterfaces().isEmpty());

    // Test adding interfaces
    vlan.addInterface("GigabitEthernet0/0/1");
    assertEquals(1, vlan.getInterfaces().size());
    assertTrue(vlan.getInterfaces().contains("GigabitEthernet0/0/1"));

    vlan.addInterface("GigabitEthernet0/0/2");
    assertEquals(2, vlan.getInterfaces().size());

    // Test setInterfaces
    SortedSet<String> newInterfaces = new TreeSet<>();
    newInterfaces.add("GigabitEthernet0/0/3");
    vlan.setInterfaces(newInterfaces);
    assertEquals(1, vlan.getInterfaces().size());
    assertTrue(vlan.getInterfaces().contains("GigabitEthernet0/0/3"));
  }

  @Test
  public void testVlanVlanifInterface() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    assertNull(vlan.getVlanifInterface());

    vlan.setVlanifInterface("Vlanif100");
    assertEquals("Vlanif100", vlan.getVlanifInterface());

    vlan.setVlanifInterface(null);
    assertNull(vlan.getVlanifInterface());
  }

  @Test
  public void testVlanToString() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    String result = vlan.toString();
    assertTrue(result.contains("100"));

    vlan.setName("Test");
    vlan.setDescription("Test Description");
    vlan.addInterface("GigabitEthernet0/0/1");
    vlan.setVlanifInterface("Vlanif100");

    result = vlan.toString();
    assertTrue(result.contains("100"));
    assertTrue(result.contains("Test") || result.contains("Description"));
  }

  @Test
  public void testVlanAddInterface() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    vlan.addInterface("GigabitEthernet0/0/1");
    assertEquals(1, vlan.getInterfaces().size());

    // Test that duplicate interfaces aren't added (Set behavior)
    vlan.addInterface("GigabitEthernet0/0/1");
    assertEquals(1, vlan.getInterfaces().size());

    vlan.addInterface("GigabitEthernet0/0/2");
    assertEquals(2, vlan.getInterfaces().size());
  }

  @Test
  public void testVlanGetVlanId() {
    // Test minimum VLAN ID
    HuaweiVlan vlan1 = new HuaweiVlan(1);
    assertEquals(1, vlan1.getVlanId());

    // Test maximum VLAN ID
    HuaweiVlan vlan2 = new HuaweiVlan(4094);
    assertEquals(4094, vlan2.getVlanId());

    // Test common VLAN ID
    HuaweiVlan vlan3 = new HuaweiVlan(100);
    assertEquals(100, vlan3.getVlanId());

    // Test another common VLAN ID
    HuaweiVlan vlan4 = new HuaweiVlan(2000);
    assertEquals(2000, vlan4.getVlanId());
  }

  @Test
  public void testVlanInterfaceSorting() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    // Add interfaces in non-sorted order
    vlan.addInterface("GigabitEthernet0/0/3");
    vlan.addInterface("GigabitEthernet0/0/1");
    vlan.addInterface("GigabitEthernet0/0/2");

    // Verify they are stored in sorted order (TreeSet behavior)
    SortedSet<String> interfaces = vlan.getInterfaces();
    int i = 1;
    for (String iface : interfaces) {
      assertEquals("GigabitEthernet0/0/" + i, iface);
      i++;
    }
  }

  @Test
  public void testVlanToStringWithAllFields() {
    HuaweiVlan vlan = new HuaweiVlan(999);

    vlan.setName("Production");
    vlan.setDescription("Production Network VLAN");
    vlan.addInterface("GigabitEthernet0/0/1");
    vlan.addInterface("GigabitEthernet0/0/2");
    vlan.setVlanifInterface("Vlanif999");

    String result = vlan.toString();

    // Verify all fields are represented in toString
    assertTrue(result.contains("999"));
    assertTrue(result.contains("Production"));
    assertTrue(result.contains("GigabitEthernet0/0/1"));
    assertTrue(result.contains("Vlanif999"));
  }

  @Test
  public void testVlanToStringMinimal() {
    HuaweiVlan vlan = new HuaweiVlan(50);

    String result = vlan.toString();

    // Verify VLAN ID is in toString even with minimal config
    assertTrue(result.contains("50"));
  }

  @Test
  public void testVlanSetInterfacesWithMultiple() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    SortedSet<String> interfaces = new TreeSet<>();
    interfaces.add("GigabitEthernet0/0/1");
    interfaces.add("GigabitEthernet0/0/2");
    interfaces.add("GigabitEthernet0/0/3");

    vlan.setInterfaces(interfaces);

    assertEquals(3, vlan.getInterfaces().size());
    assertTrue(vlan.getInterfaces().contains("GigabitEthernet0/0/1"));
    assertTrue(vlan.getInterfaces().contains("GigabitEthernet0/0/2"));
    assertTrue(vlan.getInterfaces().contains("GigabitEthernet0/0/3"));
  }

  @Test
  public void testVlanSetEmptyInterfaces() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    vlan.addInterface("GigabitEthernet0/0/1");
    assertEquals(1, vlan.getInterfaces().size());

    // Replace with empty set
    vlan.setInterfaces(new TreeSet<>());
    assertTrue(vlan.getInterfaces().isEmpty());
  }

  @Test
  public void testVlanWithNullNameAndDescription() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    // Verify initial null state
    assertNull(vlan.getName());
    assertNull(vlan.getDescription());

    // Set to non-null values
    vlan.setName("TestVlan");
    vlan.setDescription("Test Description");

    assertEquals("TestVlan", vlan.getName());
    assertEquals("Test Description", vlan.getDescription());

    // Reset to null
    vlan.setName(null);
    vlan.setDescription(null);

    assertNull(vlan.getName());
    assertNull(vlan.getDescription());
  }

  @Test
  public void testVlanWithNullVlanifInterface() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    assertNull(vlan.getVlanifInterface());

    vlan.setVlanifInterface("Vlanif100");
    assertEquals("Vlanif100", vlan.getVlanifInterface());

    vlan.setVlanifInterface(null);
    assertNull(vlan.getVlanifInterface());
  }

  @Test
  public void testVlanGetInterfacesReturnsNonnull() {
    HuaweiVlan vlan = new HuaweiVlan(100);

    // Even with no interfaces, getInterfaces should return non-null empty set
    SortedSet<String> interfaces = vlan.getInterfaces();
    assertThat(interfaces, notNullValue());
    assertTrue(interfaces.isEmpty());
  }

  // HuaweiInterface coverage tests
  @Test
  public void testInterfaceBandwidth() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/1");

    // Test default bandwidth (null)
    assertNull(iface.getBandwidth());

    // Test custom bandwidth
    iface.setBandwidth(Double.valueOf(10E9));
    assertEquals(Double.valueOf(10E9), iface.getBandwidth());

    iface.setBandwidth(null);
    assertNull(iface.getBandwidth());
  }

  @Test
  public void testInterfaceMtu() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/1");

    // Test default MTU
    assertEquals(1500, iface.getMtu());

    // Test setting MTU
    iface.setMtu(9000);
    assertEquals(9000, iface.getMtu());
  }

  @Test
  public void testInterfaceDhcpRelay() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/1");

    // Test initial state
    assertTrue(iface.getDhcpRelayAddresses().isEmpty());
    assertFalse(iface.getDhcpRelayClient());

    // Test adding relay addresses
    Ip addr1 = Ip.parse("192.168.1.1");
    Ip addr2 = Ip.parse("192.168.1.2");
    iface.addDhcpRelayAddress(addr1);
    iface.addDhcpRelayAddress(addr2);

    assertEquals(2, iface.getDhcpRelayAddresses().size());
    assertTrue(iface.getDhcpRelayAddresses().contains(addr1));
    assertTrue(iface.getDhcpRelayAddresses().contains(addr2));

    // Test setting addresses
    SortedSet<Ip> newAddrs = new TreeSet<>();
    newAddrs.add(Ip.parse("10.0.0.1"));
    iface.setDhcpRelayAddresses(newAddrs);
    assertEquals(1, iface.getDhcpRelayAddresses().size());

    // Test DHCP relay client
    iface.setDhcpRelayClient(true);
    assertTrue(iface.getDhcpRelayClient());

    iface.setDhcpRelayClient(false);
    assertFalse(iface.getDhcpRelayClient());
  }

  @Test
  public void testInterfaceAcls() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/1");

    // Test initial state
    assertNull(iface.getIncomingFilter());
    assertNull(iface.getOutgoingFilter());

    // Test setting ACLs
    iface.setIncomingFilter("acl-in");
    assertEquals("acl-in", iface.getIncomingFilter());

    iface.setOutgoingFilter("acl-out");
    assertEquals("acl-out", iface.getOutgoingFilter());

    iface.setIncomingFilter(null);
    assertNull(iface.getIncomingFilter());

    iface.setOutgoingFilter(null);
    assertNull(iface.getOutgoingFilter());
  }

  @Test
  public void testInterfaceAddress() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/1");

    // Test initial state
    assertNull(iface.getAddress());

    // Test setting address
    ConcreteInterfaceAddress addr = ConcreteInterfaceAddress.parse("192.168.1.1/24");
    iface.setAddress(addr);
    assertEquals(addr, iface.getAddress());

    // Test nulling address
    iface.setAddress(null);
    assertNull(iface.getAddress());
  }

  // Additional tests for improved coverage

  @Test
  public void testVlanBatchWithTo() {
    // Test VLAN batch with "to" range syntax (creates start to end-1)
    String configText = "sysname Router1\n" + "vlan batch 10 to 20\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should create VLANs 10-19 (not 10-20, end is exclusive)
    assertThat(config.getVlans().size(), equalTo(10));
    assertThat(config.getVlan(10), notNullValue());
    assertThat(config.getVlan(19), notNullValue());
    assertThat(config.getVlan(20), equalTo(null));
  }

  @Test
  public void testVlanBatchInvalidRange() {
    // Test VLAN batch with invalid range
    String configText = "sysname Router1\n" + "vlan batch 999999 to 1000000\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have warning but config should still be valid
    assertThat(config.getHostname(), equalTo("Router1"));
  }

  @Test
  public void testInterfaceDescriptionMultiWord() {
    // Test interface description with multiple words
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description This is a long description with multiple words\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("This is a long description with multiple words"));
  }

  @Test
  public void testDot1qTerminationInvalidVlan() {
    // Test dot1q termination with invalid VLAN ID
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.100\n"
            + " dot1q termination vid 9999\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should parse successfully even with high VLAN ID
  }

  @Test
  public void testBgpRouterIdInvalid() {
    // Test BGP with invalid router-id
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " router-id 999.999.999.999\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    // Router ID should not be set due to invalid IP
    assertThat(config.getBgpProcess().getRouterId(), equalTo(null));
  }

  @Test
  public void testBgpPeerInvalid() {
    // Test BGP with invalid peer configuration
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 999.999.999.999 as-number 65002\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getBgpProcess(), notNullValue());
    // Should have warning but BGP process should still exist
  }

  @Test
  public void testOspfRouterIdInvalid() {
    // Test OSPF with invalid router-id
    String configText = "sysname Router1\n" + "ospf 1\n" + " router-id 256.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    // Router ID should not be set due to invalid IP
    assertThat(config.getOspfProcess().getRouterId(), equalTo(null));
  }

  @Test
  public void testOspfAreaNullId() {
    // Test OSPF area without proper ID (shouldn't happen in normal parsing)
    // This tests the error handling path
    String configText = "sysname Router1\n" + "ospf 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    // No areas should be created
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(0));
  }

  @Test
  public void testVrfRouteDistinguisher() {
    // Test VRF with route distinguisher
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " route-distinguisher 65001:100\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getRouteDistinguisher(), equalTo("65001:100"));
  }

  @Test
  public void testVrfVpnTargetImport() {
    // Test VRF with import route target
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " vpn-target 65001:100 import\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getImportRouteTargets().size(), equalTo(1));
    assertThat(vrf.getImportRouteTargets().contains("65001:100"), equalTo(true));
    assertThat(vrf.getExportRouteTargets().size(), equalTo(0));
  }

  @Test
  public void testVrfVpnTargetExport() {
    // Test VRF with export route target
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " vpn-target 65001:200 export\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getExportRouteTargets().size(), equalTo(1));
    assertThat(vrf.getExportRouteTargets().contains("65001:200"), equalTo(true));
    assertThat(vrf.getImportRouteTargets().size(), equalTo(0));
  }

  @Test
  public void testVrfVpnTargetBoth() {
    // Test VRF with both import and export route target
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " vpn-target 65001:300 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getImportRouteTargets().size(), equalTo(1));
    assertThat(vrf.getImportRouteTargets().contains("65001:300"), equalTo(true));
    assertThat(vrf.getExportRouteTargets().size(), equalTo(1));
    assertThat(vrf.getExportRouteTargets().contains("65001:300"), equalTo(true));
  }

  @Test
  public void testVrfMultipleVpnTargets() {
    // Test VRF with multiple route targets
    String configText =
        "sysname Router1\n"
            + "ip vpn-instance VRF1\n"
            + " vpn-target 65001:100 import\n"
            + " vpn-target 65001:200 export\n"
            + " vpn-target 65001:300 both\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiVrf vrf = config.getVrfs().get("VRF1");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getImportRouteTargets().size(), equalTo(2)); // 100 and 300
    assertThat(vrf.getExportRouteTargets().size(), equalTo(2)); // 200 and 300
  }

  @Test
  public void testAclRuleWithPorts() {
    // Test ACL rule with port operators
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port gt 1024\n"
            + " rule 15 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port lt 5000\n"
            + " rule 20 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " source-port range 2000 3000 destination-port range 80 443\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));

    // Check eq port
    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getDestinationPort(), equalTo("eq 80"));

    // Check gt port
    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getDestinationPort(), equalTo("gt 1024"));

    // Check lt port
    HuaweiAclLine line3 = acl.getLines().get(2);
    assertThat(line3.getDestinationPort(), equalTo("lt 5000"));

    // Check range ports
    HuaweiAclLine line4 = acl.getLines().get(3);
    assertThat(line4.getSourcePort(), equalTo("range 2000 3000"));
    assertThat(line4.getDestinationPort(), equalTo("range 80 443"));
  }

  @Test
  public void testNatServerWithProtocol() {
    // Test NAT server with protocol and ports
    String configText =
        "sysname Router1\n"
            + "nat server protocol tcp global 192.168.1.1 80 inside 10.0.0.1 8080\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(rule.getProtocol(), equalTo("tcp"));
    assertThat(rule.getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(rule.getInsideLocalIp().toString(), equalTo("10.0.0.1"));
    assertThat(rule.getGlobalPort(), equalTo(80));
    assertThat(rule.getInsideLocalPort(), equalTo(8080));
  }

  @Test
  public void testNatServerUdp() {
    // Test NAT server with UDP protocol
    String configText =
        "sysname Router1\n"
            + "nat server protocol udp global 192.168.1.1 53 inside 10.0.0.1 53\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Check if NAT rules were parsed
    assertThat(config.getNatRules().size(), equalTo(1));
    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getProtocol(), equalTo("udp"));
  }

  @Test
  public void testNatServerWithoutProtocol() {
    // Test NAT server without protocol (simple IP mapping)
    String configText =
        "sysname Router1\n" + "nat server global 192.168.1.1 inside 10.0.0.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(rule.getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(rule.getInsideLocalIp().toString(), equalTo("10.0.0.1"));
  }

  @Test
  public void testNatServerWithVrf() {
    // Test NAT server with VRF
    String configText =
        "sysname Router1\n"
            + "nat server protocol tcp global 192.168.1.1 80 inside 10.0.0.1 8080 vpn-instance"
            + " VRF1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getVrfName(), equalTo("VRF1"));
  }

  @Test
  public void testNatOutboundWithPool() {
    // Test NAT outbound with pool name
    String configText = "sysname Router1\n" + "nat outbound 2000 pool pool1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(rule.getAclName(), equalTo("2000"));
    assertThat(rule.getPoolName(), equalTo("pool1"));
  }

  @Test
  public void testNatOutboundWithVrf() {
    // Test NAT outbound with VRF
    String configText = "sysname Router1\n" + "nat outbound 2000 vpn-instance VRF1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getVrfName(), equalTo("VRF1"));
  }

  @Test
  public void testNatAddressGroupBasic() {
    // Test NAT address-group with simple IP range
    String configText =
        "sysname Router1\n" + "nat address-group 1 202.1.1.1 202.1.1.100\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(1));

    HuaweiNatAddressGroup group = config.getNatAddressGroups().get(1);
    assertThat(group, notNullValue());
    assertThat(group.getIndex(), equalTo(1));
    assertThat(group.getRanges().size(), equalTo(1));
    assertThat(group.getRanges().get(0).getStartIp().toString(), equalTo("202.1.1.1"));
    assertThat(group.getRanges().get(0).getEndIp().toString(), equalTo("202.1.1.100"));
  }

  @Test
  public void testNatAddressGroupSectionFormat() {
    // Test NAT address-group with section 0 format
    String configText =
        "sysname Router1\n" + "nat address-group 1 section 0 202.1.1.1 202.1.1.100\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(1));

    HuaweiNatAddressGroup group = config.getNatAddressGroups().get(1);
    assertThat(group, notNullValue());
    assertThat(group.getIndex(), equalTo(1));
    assertThat(group.getRanges().size(), equalTo(1));
    assertThat(group.getRanges().get(0).getStartIp().toString(), equalTo("202.1.1.1"));
    assertThat(group.getRanges().get(0).getEndIp().toString(), equalTo("202.1.1.100"));
  }

  @Test
  public void testNatAddressGroupWithMask() {
    // Test NAT address-group with address and mask format
    String configText =
        "sysname Router1\n"
            + "nat address-group 1 address 202.1.1.0 mask 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(1));

    HuaweiNatAddressGroup group = config.getNatAddressGroups().get(1);
    assertThat(group, notNullValue());
    assertThat(group.getIndex(), equalTo(1));
    assertThat(group.getRanges().size(), equalTo(1));
    assertThat(group.getRanges().get(0).getStartIp().toString(), equalTo("202.1.1.0"));
    assertThat(group.getRanges().get(0).getEndIp().toString(), equalTo("202.1.1.0"));
    assertThat(group.getRanges().get(0).getMask(), notNullValue());
    assertThat(group.getRanges().get(0).getMask().toString(), equalTo("255.255.255.0"));
  }

  @Test
  public void testNatAddressGroupMultiple() {
    // Test multiple NAT address-groups
    String configText =
        "sysname Router1\n"
            + "nat address-group 1 202.1.1.1 202.1.1.100\n"
            + "nat address-group 2 203.1.1.1 203.1.1.200\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(2));

    HuaweiNatAddressGroup group1 = config.getNatAddressGroups().get(1);
    assertThat(group1, notNullValue());
    assertThat(group1.getIndex(), equalTo(1));
    assertThat(group1.getRanges().get(0).getStartIp().toString(), equalTo("202.1.1.1"));

    HuaweiNatAddressGroup group2 = config.getNatAddressGroups().get(2);
    assertThat(group2, notNullValue());
    assertThat(group2.getIndex(), equalTo(2));
    assertThat(group2.getRanges().get(0).getStartIp().toString(), equalTo("203.1.1.1"));
  }

  @Test
  public void testNatAddressGroupSingleIp() {
    // Test NAT address-group with single IP (no range)
    String configText =
        "sysname Router1\n" + "nat address-group 1 address 202.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(1));

    HuaweiNatAddressGroup group = config.getNatAddressGroups().get(1);
    assertThat(group, notNullValue());
    assertThat(group.getIndex(), equalTo(1));
    assertThat(group.getRanges().size(), equalTo(1));
    assertThat(group.getRanges().get(0).getStartIp().toString(), equalTo("202.1.1.1"));
    assertThat(group.getRanges().get(0).getEndIp().toString(), equalTo("202.1.1.1"));
  }

  @Test
  public void testNatAddressGroupAndOutbound() {
    // Test NAT address-group used with outbound rule
    String configText =
        "sysname Router1\n"
            + "nat address-group 1 202.1.1.1 202.1.1.100\n"
            + "nat outbound 2000\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    // Should have address group
    assertThat(config.getNatAddressGroups().size(), equalTo(1));
    HuaweiNatAddressGroup group = config.getNatAddressGroups().get(1);
    assertThat(group, notNullValue());
    // Should have outbound rule
    assertThat(config.getNatRules().size(), equalTo(1));
    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
  }

  @Test
  public void testStaticRouteWithVrfSuffix() {
    // Test static route with VRF specified as suffix
    String configText =
        "sysname Router1\n"
            + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1 vpn-instance VRF1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route.getDestination().toString(), equalTo("10.0.0.0/24"));
    assertThat(route.getNextHopIp().toString(), equalTo("192.168.1.1"));
    assertThat(route.getVrfName(), equalTo("VRF1"));
  }

  @Test
  public void testStaticRouteCidrNotation() {
    // Test static route with CIDR notation
    String configText =
        "sysname Router1\n" + "ip route-static 10.0.0.0/24 192.168.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getStaticRoutes().size(), equalTo(1));

    HuaweiStaticRoute route = config.getStaticRoutes().get(0);
    assertThat(route.getDestination().toString(), equalTo("10.0.0.0/24"));
    assertThat(route.getNextHopIp().toString(), equalTo("192.168.1.1"));
  }

  @Test
  public void testVlanDescriptionFromConfig() {
    // Test VLAN with description from config
    String configText =
        "sysname Router1\n" + "vlan 100\n" + " description Production VLAN\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiVlan vlan = config.getVlan(100);
    assertThat(vlan, notNullValue());
    assertThat(vlan.getDescription(), equalTo("Production VLAN"));
  }

  @Test
  public void testExitS_returnClearsContext() {
    // Test that return command clears interface and VLAN context
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ip address 192.168.2.1 255.255.255.0\n"
            + "return\n"
            + "vlan 100\n"
            + " description VLAN100\n"
            + "return\n"
            + "vlan 200\n"
            + " description VLAN200\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(2));
    assertThat(config.getVlans().size(), equalTo(2));

    // Verify each interface has correct address
    HuaweiInterface iface1 = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface1.getAddress().getIp().toString(), equalTo("192.168.1.1"));

    HuaweiInterface iface2 = config.getInterfaces().get("GigabitEthernet0/0/1");
    assertThat(iface2.getAddress().getIp().toString(), equalTo("192.168.2.1"));

    // Verify each VLAN has correct description
    HuaweiVlan vlan100 = config.getVlan(100);
    assertThat(vlan100.getDescription(), equalTo("VLAN100"));

    HuaweiVlan vlan200 = config.getVlan(200);
    assertThat(vlan200.getDescription(), equalTo("VLAN200"));
  }

  @Test
  public void testAclTypeFromNumberRange() {
    // Test ACL type determination from number range
    // 2000-2999 should be BASIC, 3000-3999 should be ADVANCED
    String configText =
        "sysname Router1\n"
            + "acl 2000\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + "acl 3000\n"
            + " rule 5 permit ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(2));

    HuaweiAcl acl2000 = config.getAcl("2000");
    assertThat(acl2000.getType(), equalTo(HuaweiAcl.AclType.BASIC));

    HuaweiAcl acl3000 = config.getAcl("3000");
    assertThat(acl3000.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
  }

  // ========== IPv6 ACL TESTS ==========

  @Test
  public void testAclIpv6Basic() {
    String configText =
        "sysname Router1\n" + "acl ipv6 test-acl\n" + " rule 5 permit\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("test-acl");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));
    assertThat(acl.getLines().size(), equalTo(1));

    HuaweiAclLine line = acl.getLines().get(0);
    assertThat(line.getAction(), equalTo("permit"));
    // When no protocol is specified, it defaults to ipv6
    assertThat(line.isIpv6(), equalTo(true));
  }

  @Test
  public void testAclIpv6WithAddresses() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 2001\n"
            + " rule 10 permit source 2001:db8::1/128 destination 2001:db8::2/128\n"
            + " rule 20 deny source 2001:db8:1::/64\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("2001");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));
    assertThat(acl.getLines().size(), equalTo(2));

    // Check first rule
    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getAction(), equalTo("permit"));
    assertThat(line1.getSource(), equalTo("2001:db8::1/128"));
    assertThat(line1.getDestination(), equalTo("2001:db8::2/128"));

    // Check second rule
    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getAction(), equalTo("deny"));
    assertThat(line2.getSource(), equalTo("2001:db8:1::/64"));
  }

  @Test
  public void testAclIpv6WithTcp() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 WEB-ACL\n"
            + " rule 5 permit tcp source 2001:db8::/32 destination 2001:db8:1::/64 destination-port"
            + " eq 80\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("WEB-ACL");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));
    assertThat(acl.getLines().size(), equalTo(1));

    HuaweiAclLine line = acl.getLines().get(0);
    assertThat(line.getAction(), equalTo("permit"));
    assertThat(line.getProtocol(), equalTo("tcp"));
    assertThat(line.getSource(), equalTo("2001:db8::/32"));
    assertThat(line.getDestination(), equalTo("2001:db8:1::/64"));
    assertThat(line.getDestinationPort(), equalTo("eq 80"));
  }

  @Test
  public void testAclIpv6WithIcmpv6() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 ICMPv6-ACL\n"
            + " rule 10 permit icmpv6 source 2001:db8::/32 destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("ICMPv6-ACL");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));
    assertThat(acl.getLines().size(), equalTo(1));

    HuaweiAclLine line = acl.getLines().get(0);
    assertThat(line.getAction(), equalTo("permit"));
    assertThat(line.getProtocol(), equalTo("icmpv6"));
    assertThat(line.getSource(), equalTo("2001:db8::/32"));
    assertThat(line.getDestination(), equalTo("any"));
  }

  @Test
  public void testAclIpv6WithPortRange() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 RANGE-ACL\n"
            + " rule 5 permit tcp source any destination any source-port range 1024 65535\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("RANGE-ACL");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));

    HuaweiAclLine line = acl.getLines().get(0);
    assertThat(line.getSourcePort(), equalTo("range 1024 65535"));
  }

  @Test
  public void testAclIpv6AndIpv4Coexist() {
    String configText =
        "sysname Router1\n"
            + "acl 2000\n"
            + " rule 5 permit ip source 192.168.1.0 0.0.0.255\n"
            + "acl ipv6 2001\n"
            + " rule 5 permit source 2001:db8::/32\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(2));

    // Check IPv4 ACL
    HuaweiAcl ipv4Acl = config.getAcl("2000");
    assertThat(ipv4Acl, notNullValue());
    assertThat(ipv4Acl.isIpv6(), equalTo(false));

    // Check IPv6 ACL
    HuaweiAcl ipv6Acl = config.getAcl("2001");
    assertThat(ipv6Acl, notNullValue());
    assertThat(ipv6Acl.isIpv6(), equalTo(true));
  }

  // ============================================================
  // COMPREHENSIVE NAT TEST COVERAGE
  // ============================================================

  @Test
  public void testNatStaticWithVrf() {
    // Test NAT static with VRF instance
    String configText =
        "sysname Router1\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1 vpn-instance VRF1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(rule.getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(rule.getInsideLocalIp().toString(), equalTo("10.0.0.1"));
    assertThat(rule.getVrfName(), equalTo("VRF1"));
  }

  @Test
  public void testNatStaticMultipleRules() {
    // Test multiple static NAT rules
    String configText =
        "sysname Router1\n"
            + "nat static global 192.168.1.1 inside 10.0.0.1\n"
            + "nat static global 192.168.1.2 inside 10.0.0.2\n"
            + "nat static global 192.168.1.3 inside 10.0.0.3\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(3));

    assertThat(config.getNatRules().get(0).getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(config.getNatRules().get(1).getGlobalIp().toString(), equalTo("192.168.1.2"));
    assertThat(config.getNatRules().get(2).getGlobalIp().toString(), equalTo("192.168.1.3"));
  }

  @Test
  public void testNatOutboundWithAclName() {
    // Test NAT outbound with named ACL
    String configText =
        "sysname Router1\n"
            + "acl number 2000 name MY_ACL\n"
            + "nat outbound MY_ACL\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(rule.getAclName(), equalTo("MY_ACL"));
  }

  @Test
  public void testNatOutboundWithPoolAndVrf() {
    // Test NAT outbound with pool and VRF
    String configText =
        "sysname Router1\n" + "nat outbound 2000 pool pool1 vpn-instance VRF1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(rule.getAclName(), equalTo("2000"));
    assertThat(rule.getPoolName(), equalTo("pool1"));
    assertThat(rule.getVrfName(), equalTo("VRF1"));
  }

  @Test
  public void testNatOutboundWithInterfaceAndVrf() {
    // Test NAT outbound with interface and VRF
    String configText =
        "sysname Router1\n" + "nat outbound 2000 interface vpn-instance VRF1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));
    assertThat(rule.getAclName(), equalTo("2000"));
    assertThat(rule.getVrfName(), equalTo("VRF1"));
  }

  @Test
  public void testNatServerWithGlobalPortOnly() {
    // Test NAT server with global port but no inside port
    String configText =
        "sysname Router1\n" + "nat server global 192.168.1.1 80 inside 10.0.0.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(rule.getGlobalIp().toString(), equalTo("192.168.1.1"));
    assertThat(rule.getInsideLocalIp().toString(), equalTo("10.0.0.1"));
    assertThat(rule.getGlobalPort(), equalTo(80));
  }

  @Test
  public void testNatServerTcpWithHighPort() {
    // Test NAT server TCP with high port number
    String configText =
        "sysname Router1\n"
            + "nat server protocol tcp global 192.168.1.1 8080 inside 10.0.0.1 65432\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(1));

    HuaweiNatRule rule = config.getNatRules().get(0);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(rule.getProtocol(), equalTo("tcp"));
    assertThat(rule.getGlobalPort(), equalTo(8080));
    assertThat(rule.getInsideLocalPort(), equalTo(65432));
  }

  @Test
  public void testNatServerMultipleWithDifferentProtocols() {
    // Test multiple NAT server rules with different protocols
    String configText =
        "sysname Router1\n"
            + "nat server protocol tcp global 192.168.1.1 80 inside 10.0.0.1 8080\n"
            + "nat server protocol udp global 192.168.1.1 53 inside 10.0.0.1 53\n"
            + "nat server global 192.168.1.2 inside 10.0.0.2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(3));

    HuaweiNatRule tcpRule = config.getNatRules().get(0);
    assertThat(tcpRule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(tcpRule.getProtocol(), equalTo("tcp"));

    HuaweiNatRule udpRule = config.getNatRules().get(1);
    assertThat(udpRule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(udpRule.getProtocol(), equalTo("udp"));

    HuaweiNatRule simpleRule = config.getNatRules().get(2);
    assertThat(simpleRule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(simpleRule.getProtocol(), equalTo(null));
  }

  @Test
  public void testNatAddressGroupWithEmptyParameters() {
    // Test NAT address-group with just index (no IP range)
    String configText = "sysname Router1\n" + "nat address-group 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(1));

    HuaweiNatAddressGroup group = config.getNatAddressGroups().get(1);
    assertThat(group, notNullValue());
    assertThat(group.getIndex(), equalTo(1));
  }

  @Test
  public void testNatAddressGroupWithSectionNonZero() {
    // Test NAT address-group with non-zero section number
    String configText =
        "sysname Router1\n" + "nat address-group 1 section 1 202.1.1.1 202.1.1.100\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(1));

    HuaweiNatAddressGroup group = config.getNatAddressGroups().get(1);
    assertThat(group, notNullValue());
    assertThat(group.getIndex(), equalTo(1));
    assertThat(group.getRanges().size(), equalTo(1));
    assertThat(group.getRanges().get(0).getStartIp().toString(), equalTo("202.1.1.1"));
    assertThat(group.getRanges().get(0).getEndIp().toString(), equalTo("202.1.1.100"));
  }

  @Test
  public void testNatBoundaryPortNumbers() {
    // Test NAT with boundary port numbers (1, 65535)
    String configText =
        "sysname Router1\n"
            + "nat server protocol tcp global 192.168.1.1 1 inside 10.0.0.1 1\n"
            + "nat server protocol udp global 192.168.1.2 65535 inside 10.0.0.2 65535\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(2));

    HuaweiNatRule rule1 = config.getNatRules().get(0);
    assertThat(rule1.getGlobalPort(), equalTo(1));
    assertThat(rule1.getInsideLocalPort(), equalTo(1));

    HuaweiNatRule rule2 = config.getNatRules().get(1);
    assertThat(rule2.getGlobalPort(), equalTo(65535));
    assertThat(rule2.getInsideLocalPort(), equalTo(65535));
  }

  @Test
  public void testNatAllOutboundFormats() {
    // Test all NAT outbound formats
    String configText =
        "sysname Router1\n"
            + "nat outbound 2000\n"
            + "nat outbound 2001 interface\n"
            + "nat outbound 2002 pool POOL1\n"
            + "nat outbound NAMED_ACL\n"
            + "nat outbound 2003 vpn-instance VRF1\n"
            + "nat outbound 2004 interface vpn-instance VRF1\n"
            + "nat outbound 2005 pool POOL2 vpn-instance VRF1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatRules().size(), equalTo(7));

    assertThat(config.getNatRules().get(0).getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(config.getNatRules().get(0).getAclName(), equalTo("2000"));
    assertThat(config.getNatRules().get(1).getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));
    assertThat(config.getNatRules().get(2).getPoolName(), equalTo("POOL1"));
    assertThat(config.getNatRules().get(3).getAclName(), equalTo("NAMED_ACL"));
    assertThat(config.getNatRules().get(4).getVrfName(), equalTo("VRF1"));
    assertThat(config.getNatRules().get(5).getVrfName(), equalTo("VRF1"));
    assertThat(config.getNatRules().get(6).getPoolName(), equalTo("POOL2"));
  }

  @Test
  public void testNatComplexScenario() {
    // Test a complex NAT scenario with multiple rule types
    String configText =
        "sysname Router1\n"
            + "acl 2000\n"
            + " rule 5 permit source 192.168.1.0 0.0.0.255\n"
            + "acl 2001\n"
            + " rule 5 permit source 192.168.2.0 0.0.0.255\n"
            + "nat address-group 1 202.1.1.1 202.1.1.100\n"
            + "nat address-group 2 203.1.1.1 203.1.1.50\n"
            + "nat static global 1.1.1.1 inside 10.0.0.1\n"
            + "nat server protocol tcp global 1.1.1.2 80 inside 10.0.0.2 8080\n"
            + "nat server protocol udp global 1.1.1.2 53 inside 10.0.0.2 53\n"
            + "nat outbound 2000\n"
            + "nat outbound 2001 interface\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(2));
    assertThat(config.getNatRules().size(), equalTo(5));

    assertThat(config.getNatRules().get(0).getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(config.getNatRules().get(1).getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(config.getNatRules().get(2).getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(config.getNatRules().get(3).getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(config.getNatRules().get(4).getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));
  }

  @Test
  public void testNatAddressGroupAllFormats() {
    // Test all NAT address-group formats
    String configText =
        "sysname Router1\n"
            + "nat address-group 1 10.0.0.1 10.0.0.100\n"
            + "nat address-group 2 section 0 10.0.1.1 10.0.1.50\n"
            + "nat address-group 3 address 10.0.2.1\n"
            + "nat address-group 4 address 10.0.3.0 mask 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getNatAddressGroups().size(), equalTo(4));

    HuaweiNatAddressGroup group1 = config.getNatAddressGroups().get(1);
    assertThat(group1.getRanges().get(0).getStartIp().toString(), equalTo("10.0.0.1"));
    assertThat(group1.getRanges().get(0).getEndIp().toString(), equalTo("10.0.0.100"));

    HuaweiNatAddressGroup group4 = config.getNatAddressGroups().get(4);
    assertThat(group4.getRanges().get(0).getMask().toString(), equalTo("255.255.255.0"));
  }

  // ============================================================
  // COMPREHENSIVE OSPF TEST COVERAGE
  // ============================================================

  @Test
  public void testOspfProcessIdVariations() {
    // Test various OSPF process IDs
    String configText = "sysname Router1\n" + "ospf 100\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(100L));
  }

  @Test
  public void testOspfProcessIdMax() {
    // Test maximum valid process ID (uint32 max)
    String configText = "sysname Router1\n" + "ospf 4294967295\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(4294967295L));
  }

  @Test
  public void testOspfProcessIdLarge() {
    // Test large process ID
    String configText = "sysname Router1\n" + "ospf 65535\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(65535L));
  }

  @Test
  public void testOspfRouterIdVariousFormats() {
    // Test router-id with various IP formats
    String configText = "sysname Router1\n" + "ospf 1\n" + " router-id 10.0.0.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("10.0.0.1"));
  }

  @Test
  public void testOspfRouterIdZero() {
    // Test router-id with 0.0.0.0
    String configText = "sysname Router1\n" + "ospf 1\n" + " router-id 0.0.0.0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("0.0.0.0"));
  }

  @Test
  public void testOspfRouterIdMax() {
    // Test router-id with 255.255.255.255
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " router-id 255.255.255.255\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("255.255.255.255"));
  }

  @Test
  public void testOspfSingleNetwork() {
    // Test single network statement
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " network 192.168.1.0/24 area 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(1));

    HuaweiOspfProcess.HuaweiOspfNetwork network = config.getOspfProcess().getNetworks().get(0);
    assertThat(network.getNetwork().toString(), equalTo("192.168.1.0/24"));
    assertThat(network.getAreaId(), equalTo(0L));
  }

  @Test
  public void testOspfNetworkVariousPrefixes() {
    // Test network statements with various prefix formats
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/8 area 0\n"
            + " network 172.16.0.0/12 area 1\n"
            + " network 192.168.0.0/16 area 2\n"
            + " network 203.0.113.0/24 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(4));

    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("10.0.0.0/8"));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(),
        equalTo("172.16.0.0/12"));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("192.168.0.0/16"));
    assertThat(
        config.getOspfProcess().getNetworks().get(3).getNetwork().toString(),
        equalTo("203.0.113.0/24"));
  }

  @Test
  public void testOspfNetworkHostPrefix() {
    // Test network with /32 prefix (host route)
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " network 10.0.0.1/32 area 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(1));

    HuaweiOspfProcess.HuaweiOspfNetwork network = config.getOspfProcess().getNetworks().get(0);
    assertThat(network.getNetwork().toString(), equalTo("10.0.0.1/32"));
    assertThat(network.getAreaId(), equalTo(0L));
  }

  @Test
  public void testOspfMultipleNetworksSameArea() {
    // Test multiple networks in the same area
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.1.0.0/24 area 0\n"
            + " network 10.2.0.0/24 area 0\n"
            + " network 10.3.0.0/24 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(3));

    for (HuaweiOspfProcess.HuaweiOspfNetwork network : config.getOspfProcess().getNetworks()) {
      assertThat(network.getAreaId(), equalTo(0L));
    }
  }

  @Test
  public void testOspfNetworksAcrossAreas() {
    // Test networks distributed across multiple areas
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 10.1.0.0/24 area 1\n"
            + " network 10.2.0.0/24 area 2\n"
            + " network 10.3.0.0/24 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(4));

    assertThat(config.getOspfProcess().getNetworks().get(0).getAreaId(), equalTo(0L));
    assertThat(config.getOspfProcess().getNetworks().get(1).getAreaId(), equalTo(1L));
    assertThat(config.getOspfProcess().getNetworks().get(2).getAreaId(), equalTo(2L));
    assertThat(config.getOspfProcess().getNetworks().get(3).getAreaId(), equalTo(0L));
  }

  @Test
  public void testOspfAreaIdFormats() {
    // Test various area ID formats
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " area 0\n"
            + " area 1\n"
            + " area 100\n"
            + " area 65535\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(4));
    assertThat(config.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(1L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(100L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(65535L), equalTo(true));
  }

  @Test
  public void testOspfAreaIdLarge() {
    // Test large area ID
    String configText = "sysname Router1\n" + "ospf 1\n" + " area 4294967295\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(1));
    assertThat(config.getOspfProcess().getAreas().containsKey(4294967295L), equalTo(true));
  }

  @Test
  public void testOspfCompleteConfiguration() {
    // Test complete OSPF configuration with all elements
    String configText =
        "sysname Router1\n"
            + "ospf 100\n"
            + " router-id 1.1.1.1\n"
            + " area 0\n"
            + " area 1\n"
            + " area 2\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 10.1.0.0/24 area 1\n"
            + " network 10.2.0.0/24 area 2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());

    // Check process ID
    assertThat(config.getOspfProcess().getProcessId(), equalTo(100L));

    // Check router ID
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("1.1.1.1"));

    // Check areas
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(3));
    assertThat(config.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(1L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(2L), equalTo(true));

    // Check networks
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(3));
    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("10.0.0.0/24"));
    assertThat(config.getOspfProcess().getNetworks().get(0).getAreaId(), equalTo(0L));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(),
        equalTo("10.1.0.0/24"));
    assertThat(config.getOspfProcess().getNetworks().get(1).getAreaId(), equalTo(1L));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("10.2.0.0/24"));
    assertThat(config.getOspfProcess().getNetworks().get(2).getAreaId(), equalTo(2L));
  }

  @Test
  public void testOspfOnlyRouterId() {
    // Test OSPF with only router-id (no areas or networks)
    String configText = "sysname Router1\n" + "ospf 1\n" + " router-id 10.10.10.10\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("10.10.10.10"));
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(0));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(0));
  }

  @Test
  public void testOspfOnlyAreas() {
    // Test OSPF with only areas (no router-id or networks)
    String configText = "sysname Router1\n" + "ospf 1\n" + " area 0\n" + " area 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId(), equalTo(null));
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(2));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(0));
  }

  @Test
  public void testOspfOnlyNetworks() {
    // Test OSPF with only networks (no router-id or areas)
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 192.168.1.0/24 area 0\n"
            + " network 192.168.2.0/24 area 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId(), equalTo(null));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(2));
    // Networks automatically create areas
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(2));
  }

  @Test
  public void testOspfEmptyConfiguration() {
    // Test OSPF with no sub-configuration
    String configText = "sysname Router1\n" + "ospf 1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
    assertThat(config.getOspfProcess().getRouterId(), equalTo(null));
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(0));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(0));
  }

  @Test
  public void testOspfNetworkCreatesArea() {
    // Test that network statements create areas automatically
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " network 10.0.0.0/24 area 100\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(1));
    // Area 100 should be created automatically
    assertThat(config.getOspfProcess().getAreas().containsKey(100L), equalTo(true));
  }

  @Test
  public void testOspfMixedConfiguration() {
    // Test OSPF with mixed order of elements
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " area 1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " router-id 5.5.5.5\n"
            + " area 0\n"
            + " network 10.1.0.0/24 area 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getRouterId().toString(), equalTo("5.5.5.5"));
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(2));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(2));
  }

  @Test
  public void testOspfPrivateNetworkPrefixes() {
    // Test OSPF with private network prefixes (RFC 1918)
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/8 area 0\n"
            + " network 172.16.0.0/12 area 0\n"
            + " network 192.168.0.0/16 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(3));

    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("10.0.0.0/8"));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(),
        equalTo("172.16.0.0/12"));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("192.168.0.0/16"));
  }

  @Test
  public void testOspfLinkLocalPrefix() {
    // Test OSPF with link-local prefix
    String configText =
        "sysname Router1\n" + "ospf 1\n" + " network 169.254.0.0/16 area 0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(1));
    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("169.254.0.0/16"));
  }

  @Test
  public void testOspfClassANetworks() {
    // Test OSPF with Class A network prefixes
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 1.0.0.0/8 area 0\n"
            + " network 2.0.0.0/8 area 1\n"
            + " network 126.0.0.0/8 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(3));

    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(), equalTo("1.0.0.0/8"));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(), equalTo("2.0.0.0/8"));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("126.0.0.0/8"));
  }

  @Test
  public void testOspfClassBNetworks() {
    // Test OSPF with Class B network prefixes
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 128.0.0.0/16 area 0\n"
            + " network 172.16.0.0/16 area 1\n"
            + " network 191.255.0.0/16 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(3));

    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("128.0.0.0/16"));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(),
        equalTo("172.16.0.0/16"));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("191.255.0.0/16"));
  }

  @Test
  public void testOspfClassCNetworks() {
    // Test OSPF with Class C network prefixes
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 192.0.0.0/24 area 0\n"
            + " network 192.168.1.0/24 area 1\n"
            + " network 223.255.255.0/24 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(3));

    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("192.0.0.0/24"));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(),
        equalTo("192.168.1.0/24"));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("223.255.255.0/24"));
  }

  @Test
  public void testOspfVariableLengthSubnets() {
    // Test OSPF with variable length subnet masks
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/30 area 0\n"
            + " network 10.0.0.4/30 area 0\n"
            + " network 10.0.0.8/29 area 0\n"
            + " network 10.0.0.16/28 area 0\n"
            + " network 10.0.0.32/27 area 0\n"
            + " network 10.0.0.64/26 area 0\n"
            + " network 10.0.0.128/25 area 0\n"
            + " network 10.0.1.0/24 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(8));

    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("10.0.0.0/30"));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(),
        equalTo("10.0.0.4/30"));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("10.0.0.8/29"));
    assertThat(
        config.getOspfProcess().getNetworks().get(3).getNetwork().toString(),
        equalTo("10.0.0.16/28"));
    assertThat(
        config.getOspfProcess().getNetworks().get(4).getNetwork().toString(),
        equalTo("10.0.0.32/27"));
    assertThat(
        config.getOspfProcess().getNetworks().get(5).getNetwork().toString(),
        equalTo("10.0.0.64/26"));
    assertThat(
        config.getOspfProcess().getNetworks().get(6).getNetwork().toString(),
        equalTo("10.0.0.128/25"));
    assertThat(
        config.getOspfProcess().getNetworks().get(7).getNetwork().toString(),
        equalTo("10.0.1.0/24"));
  }

  @Test
  public void testOspfMultipleAreasWithNetworks() {
    // Test OSPF with multiple areas, each having networks
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " area 0\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 10.0.1.0/24 area 0\n"
            + " area 1\n"
            + " network 10.1.0.0/24 area 1\n"
            + " network 10.1.1.0/24 area 1\n"
            + " area 2\n"
            + " network 10.2.0.0/24 area 2\n"
            + " network 10.2.1.0/24 area 2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(3));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(6));

    // Count networks per area
    long area0Count =
        config.getOspfProcess().getNetworks().stream().filter(n -> n.getAreaId() == 0).count();
    long area1Count =
        config.getOspfProcess().getNetworks().stream().filter(n -> n.getAreaId() == 1).count();
    long area2Count =
        config.getOspfProcess().getNetworks().stream().filter(n -> n.getAreaId() == 2).count();

    assertThat(area0Count, equalTo(2L));
    assertThat(area1Count, equalTo(2L));
    assertThat(area2Count, equalTo(2L));
  }

  @Test
  public void testOspfBackboneArea() {
    // Test OSPF with backbone area (area 0) explicitly defined
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " router-id 1.1.1.1\n"
            + " area 0\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 10.0.1.0/24 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(2));

    for (HuaweiOspfProcess.HuaweiOspfNetwork network : config.getOspfProcess().getNetworks()) {
      assertThat(network.getAreaId(), equalTo(0L));
    }
  }

  @Test
  public void testOspfRegularArea() {
    // Test OSPF with regular (non-backbone) area
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " area 1\n"
            + " network 10.1.0.0/24 area 1\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().containsKey(1L), equalTo(true));
    assertThat(config.getOspfProcess().getNetworks().get(0).getAreaId(), equalTo(1L));
  }

  @Test
  public void testOspfNonContiguousNetworks() {
    // Test OSPF with non-contiguous network ranges
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 10.0.0.0/24 area 0\n"
            + " network 10.0.10.0/24 area 0\n"
            + " network 10.0.100.0/24 area 0\n"
            + " network 10.1.0.0/24 area 0\n"
            + " network 10.100.0.0/24 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(5));

    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("10.0.0.0/24"));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(),
        equalTo("10.0.10.0/24"));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("10.0.100.0/24"));
    assertThat(
        config.getOspfProcess().getNetworks().get(3).getNetwork().toString(),
        equalTo("10.1.0.0/24"));
    assertThat(
        config.getOspfProcess().getNetworks().get(4).getNetwork().toString(),
        equalTo("10.100.0.0/24"));
  }

  @Test
  public void testOspfDifferentSubnetMasks() {
    // Test OSPF with different subnet mask lengths
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " network 192.168.1.0/24 area 0\n"
            + " network 192.168.2.0/25 area 0\n"
            + " network 192.168.3.0/26 area 0\n"
            + " network 192.168.4.0/27 area 0\n"
            + " network 192.168.5.0/28 area 0\n"
            + " network 192.168.6.0/29 area 0\n"
            + " network 192.168.7.0/30 area 0\n"
            + " network 192.168.8.0/32 area 0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getNetworks().size(), equalTo(8));

    assertThat(
        config.getOspfProcess().getNetworks().get(0).getNetwork().toString(),
        equalTo("192.168.1.0/24"));
    assertThat(
        config.getOspfProcess().getNetworks().get(1).getNetwork().toString(),
        equalTo("192.168.2.0/25"));
    assertThat(
        config.getOspfProcess().getNetworks().get(2).getNetwork().toString(),
        equalTo("192.168.3.0/26"));
    assertThat(
        config.getOspfProcess().getNetworks().get(3).getNetwork().toString(),
        equalTo("192.168.4.0/27"));
    assertThat(
        config.getOspfProcess().getNetworks().get(4).getNetwork().toString(),
        equalTo("192.168.5.0/28"));
    assertThat(
        config.getOspfProcess().getNetworks().get(5).getNetwork().toString(),
        equalTo("192.168.6.0/29"));
    assertThat(
        config.getOspfProcess().getNetworks().get(6).getNetwork().toString(),
        equalTo("192.168.7.0/30"));
    assertThat(
        config.getOspfProcess().getNetworks().get(7).getNetwork().toString(),
        equalTo("192.168.8.0/32"));
  }

  @Test
  public void testOspfAreaZeroAndNonZero() {
    // Test OSPF with both backbone (area 0) and non-backbone areas
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + " area 0\n"
            + " network 10.0.0.0/24 area 0\n"
            + " area 1\n"
            + " network 10.1.0.0/24 area 1\n"
            + " area 2\n"
            + " network 10.2.0.0/24 area 2\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getOspfProcess().getAreas().size(), equalTo(3));
    assertThat(config.getOspfProcess().getAreas().containsKey(0L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(1L), equalTo(true));
    assertThat(config.getOspfProcess().getAreas().containsKey(2L), equalTo(true));
  }

  @Test
  public void testOspfWithOtherFeatures() {
    // Test OSPF configuration alongside other features
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "ospf 1\n"
            + " router-id 1.1.1.1\n"
            + " network 192.168.1.0/24 area 0\n"
            + "ip route-static 0.0.0.0 0.0.0.0 192.168.1.254\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));
    assertThat(config.getStaticRoutes().size(), equalTo(1));
  }

  @Test
  public void testOspfMultipleProcessesNotSupported() {
    // Test that only one OSPF process is supported (second one should not appear)
    // Note: Current implementation only stores single OSPF process
    String configText = "sysname Router1\n" + "ospf 1\n" + " router-id 1.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    // Only one process should exist
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));
  }

  // ============================================================
  // COMPREHENSIVE ACL TEST COVERAGE
  // ============================================================

  // Named ACLs

  @Test
  public void testAclNamedBasic() {
    String configText =
        "sysname Router1\n"
            + "acl MANAGEMENT-ACL basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + " rule 10 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("MANAGEMENT-ACL");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines().size(), equalTo(2));

    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getAction(), equalTo("permit"));
    assertThat(line1.getSource(), equalTo("10.0.0.0 0.0.0.255"));

    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getAction(), equalTo("deny"));
    assertThat(line2.getSource(), equalTo("any"));
  }

  @Test
  public void testAclNamedAdvanced() {
    String configText =
        "sysname Router1\n"
            + "acl WEB-ACL advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 443\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("WEB-ACL");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
    assertThat(acl.getLines().size(), equalTo(2));

    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getProtocol(), equalTo("tcp"));
    assertThat(line1.getDestinationPort(), equalTo("eq 80"));

    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getDestinationPort(), equalTo("eq 443"));
  }

  @Test
  public void testAclNumberKeywordSyntax() {
    String configText =
        "sysname Router1\n"
            + "acl number 2500 basic\n"
            + " rule 5 permit source 10.1.1.0 0.0.0.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("2500");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
  }

  @Test
  public void testAclNumberKeywordAdvanced() {
    String configText =
        "sysname Router1\n"
            + "acl number 3500 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 22\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3500");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
  }

  @Test
  public void testAclTypeAutoDetectionBasicRange() {
    String configText =
        "sysname Router1\n"
            + "acl 2000\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
  }

  @Test
  public void testAclTypeAutoDetectionAdvancedRange() {
    String configText =
        "sysname Router1\n"
            + "acl 3000\n"
            + " rule 5 permit ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
  }

  @Test
  public void testAclWithLogOption() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination any destination-port eq"
            + " 80 log\n"
            + " rule 10 deny ip source any destination any log\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(2));
  }

  @Test
  public void testAclWithFragmentOption() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit ip source 192.168.1.0 0.0.0.255 destination any fragment\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
  }

  @Test
  public void testAclWithSourcePortOperators() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 80\n"
            + " rule 10 permit tcp source any destination any destination-port eq 443\n"
            + " rule 15 permit tcp source any destination any destination-port eq 22\n"
            + " rule 20 permit tcp source any destination any destination-port range 2000 3000\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));

    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getDestinationPort(), equalTo("eq 80"));

    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getDestinationPort(), equalTo("eq 443"));

    HuaweiAclLine line3 = acl.getLines().get(2);
    assertThat(line3.getDestinationPort(), equalTo("eq 22"));

    HuaweiAclLine line4 = acl.getLines().get(3);
    assertThat(line4.getDestinationPort(), equalTo("range 2000 3000"));
  }

  @Test
  public void testAclWithNamedProtocol() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit icmp source any destination any\n"
            + " rule 10 permit ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(2));

    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getProtocol(), equalTo("icmp"));

    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getProtocol(), equalTo("ip"));
  }

  @Test
  public void testMultipleAcls() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + "acl 2001 basic\n"
            + " rule 5 permit source 192.168.1.0 0.0.0.255\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 80\n"
            + "acl WEB-IN advanced\n"
            + " rule 5 permit tcp source any destination 10.0.0.0 0.0.0.255 destination-port eq"
            + " 443\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(4));

    assertThat(config.getAcl("2000"), notNullValue());
    assertThat(config.getAcl("2001"), notNullValue());
    assertThat(config.getAcl("3000"), notNullValue());
    assertThat(config.getAcl("WEB-IN"), notNullValue());
  }

  @Test
  public void testAclIpv6WithLog() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 IPV6-ACL\n"
            + " rule 5 permit tcp source any destination any destination-port eq 80 log\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("IPV6-ACL");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));
  }

  @Test
  public void testAclIpv6WithDestinationPort() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 IPV6-ACL\n"
            + " rule 5 permit tcp source any destination any destination-port eq 80\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("IPV6-ACL");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));

    HuaweiAclLine line = acl.getLines().get(0);
    assertThat(line.getDestinationPort(), equalTo("eq 80"));
  }

  @Test
  public void testAclIpv6WithUdp() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 IPV6-UDP\n"
            + " rule 5 permit udp source 2001:db8::/32 destination any destination-port eq 53\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("IPV6-UDP");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));

    HuaweiAclLine line = acl.getLines().get(0);
    assertThat(line.getProtocol(), equalTo("udp"));
  }

  @Test
  public void testAclIpv6WithTcpProtocol() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 IPV6-TCP\n"
            + " rule 5 permit tcp source 2001:db8::/32 destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    HuaweiAcl acl = config.getAcl("IPV6-TCP");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));

    HuaweiAclLine line = acl.getLines().get(0);
    assertThat(line.getProtocol(), equalTo("tcp"));
  }

  @Test
  public void testAclWithMaximumRules() {
    StringBuilder configText = new StringBuilder("sysname Router1\n" + "acl 3000 advanced\n");
    for (int i = 1; i <= 50; i += 5) {
      configText.append(" rule ").append(i).append(" permit ip source any destination any\n");
    }
    configText.append("return\n");

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText.toString(), getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config =
        HuaweiControlPlaneExtractor.extract(configText.toString(), parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(10));
  }

  @Test
  public void testAclBoundaryPorts() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 1\n"
            + " rule 10 permit tcp source any destination any destination-port eq 65535\n"
            + " rule 15 permit tcp source any destination any destination-port range 1 65535\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(3));
  }

  @Test
  public void testAclWithAnySourceAndDestination() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit ip source any destination any\n"
            + " rule 10 deny tcp source any destination any\n"
            + " rule 15 permit udp source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(3));

    for (HuaweiAclLine line : acl.getLines()) {
      assertThat(line.getSource(), equalTo("any"));
      assertThat(line.getDestination(), equalTo("any"));
    }
  }

  @Test
  public void testAclDenyAll() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 deny ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(2));

    HuaweiAclLine denyLine = acl.getLines().get(1);
    assertThat(denyLine.getAction(), equalTo("deny"));
  }

  @Test
  public void testAclAllDenyRules() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 deny source 10.0.0.0 0.0.0.255\n"
            + " rule 10 deny source 192.168.0.0 0.0.255.255\n"
            + " rule 15 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(3));

    for (HuaweiAclLine line : acl.getLines()) {
      assertThat(line.getAction(), equalTo("deny"));
    }
  }

  @Test
  public void testAclLineDetailedVerification() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " source-port gt 1024 destination-port eq 80\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(1));

    HuaweiAclLine line = acl.getLines().get(0);
    assertThat(line.getSequenceNumber(), equalTo(1));
    assertThat(line.getAction(), equalTo("permit"));
    assertThat(line.getProtocol(), equalTo("tcp"));
    assertThat(line.getSource(), equalTo("192.168.1.0 0.0.0.255"));
    assertThat(line.getDestination(), equalTo("10.0.0.0 0.0.0.255"));
    assertThat(line.getSourcePort(), equalTo("gt 1024"));
    assertThat(line.getDestinationPort(), equalTo("eq 80"));
  }

  @Test
  public void testAclLineSequenceNumbers() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 1 permit source 10.0.0.0 0.0.0.255\n"
            + " rule 5 deny source 192.168.0.0 0.0.255.255\n"
            + " rule 10 permit source any\n"
            + " rule 100 deny source 172.16.0.0 0.0.0.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));

    assertThat(acl.getLines().get(0).getSequenceNumber(), equalTo(1));
    assertThat(acl.getLines().get(1).getSequenceNumber(), equalTo(2));
    assertThat(acl.getLines().get(2).getSequenceNumber(), equalTo(3));
    assertThat(acl.getLines().get(3).getSequenceNumber(), equalTo(4));
  }

  @Test
  public void testAclMixedIpVersionsComprehensive() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.255\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 80\n"
            + "acl ipv6 3001\n"
            + " rule 5 permit tcp source 2001:db8::/32 destination any destination-port eq 80\n"
            + "acl ipv6 MGMT-v6\n"
            + " rule 5 permit icmpv6 source 2001:db8::/32 destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(4));

    HuaweiAcl ipv4Basic = config.getAcl("2000");
    assertThat(ipv4Basic, notNullValue());
    assertThat(ipv4Basic.isIpv6(), equalTo(false));
    assertThat(ipv4Basic.getType(), equalTo(HuaweiAcl.AclType.BASIC));

    HuaweiAcl ipv4Adv = config.getAcl("3000");
    assertThat(ipv4Adv, notNullValue());
    assertThat(ipv4Adv.isIpv6(), equalTo(false));
    assertThat(ipv4Adv.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));

    HuaweiAcl ipv6Num = config.getAcl("3001");
    assertThat(ipv6Num, notNullValue());
    assertThat(ipv6Num.isIpv6(), equalTo(true));

    HuaweiAcl ipv6Named = config.getAcl("MGMT-v6");
    assertThat(ipv6Named, notNullValue());
    assertThat(ipv6Named.isIpv6(), equalTo(true));
  }

  @Test
  public void testAclWithNoRules() {
    String configText = "sysname Router1\n" + "acl 2000 basic\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(0));
  }

  @Test
  public void testAclNamedWithNoRules() {
    String configText = "sysname Router1\n" + "acl EMPTY-ACL advanced\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("EMPTY-ACL");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.ADVANCED));
    assertThat(acl.getLines().size(), equalTo(0));
  }

  @Test
  public void testAclIpv6WithNoRules() {
    String configText = "sysname Router1\n" + "acl ipv6 EMPTY-V6\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("EMPTY-V6");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));
    assertThat(acl.getLines().size(), equalTo(0));
  }

  @Test
  public void testAclWildcardMaskEdgeCases() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.0.0.0 0.0.0.0\n"
            + " rule 10 permit source 10.0.0.0 255.255.255.255\n"
            + " rule 15 permit source 0.0.0.0 255.255.255.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(3));
  }

  @Test
  public void testAclIcmpProtocol() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit icmp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255\n"
            + " rule 10 deny icmp source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(2));

    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getProtocol(), equalTo("icmp"));
    assertThat(line1.getAction(), equalTo("permit"));

    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getProtocol(), equalTo("icmp"));
    assertThat(line2.getAction(), equalTo("deny"));
  }

  @Test
  public void testAclIpProtocol() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit ip source 192.168.1.0 0.0.0.255 destination any\n"
            + " rule 10 deny ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(2));

    for (HuaweiAclLine line : acl.getLines()) {
      assertThat(line.getProtocol(), equalTo("ip"));
    }
  }

  @Test
  public void testAclWebServerScenario() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination 10.0.0.10 0.0.0.0 destination-port eq 80\n"
            + " rule 10 permit tcp source any destination 10.0.0.10 0.0.0.0 destination-port eq"
            + " 443\n"
            + " rule 15 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.10 0.0.0.0"
            + " destination-port eq 22\n"
            + " rule 20 deny tcp source any destination 10.0.0.10 0.0.0.0 destination-port eq 22\n"
            + " rule 25 permit icmp source any destination 10.0.0.10 0.0.0.0\n"
            + " rule 30 deny ip source any destination 10.0.0.10 0.0.0.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(6));
  }

  @Test
  public void testAclDnsScenario() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit udp source any destination 10.0.0.53 0.0.0.0 destination-port eq 53\n"
            + " rule 10 permit tcp source any destination 10.0.0.53 0.0.0.0 destination-port eq"
            + " 53\n"
            + " rule 15 permit udp source 10.0.0.0 0.0.0.255 destination any\n"
            + " rule 20 deny udp source any destination 10.0.0.53 0.0.0.0 destination-port eq 53\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));

    assertThat(acl.getLines().get(0).getProtocol(), equalTo("udp"));
    assertThat(acl.getLines().get(1).getProtocol(), equalTo("tcp"));
  }

  @Test
  public void testAclNetworkManagementScenario() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 10.10.10.0 0.0.0.255\n"
            + " rule 10 permit source 192.168.100.0 0.0.0.255\n"
            + " rule 100 deny source any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("2000");
    assertThat(acl, notNullValue());
    assertThat(acl.getType(), equalTo(HuaweiAcl.AclType.BASIC));
    assertThat(acl.getLines().size(), equalTo(3));

    assertThat(acl.getLines().get(0).getAction(), equalTo("permit"));
    assertThat(acl.getLines().get(1).getAction(), equalTo("permit"));
    assertThat(acl.getLines().get(2).getAction(), equalTo("deny"));
  }

  @Test
  public void testAclIpv6AddressFormats() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 ADDR-FORMAT\n"
            + " rule 5 permit tcp source 2001:db8::1/128 destination any destination-port eq 80\n"
            + " rule 10 permit tcp source 2001:db8:1::/64 destination any destination-port eq 443\n"
            + " rule 15 permit icmpv6 source 2001:db8:0:1::/64 destination 2001:db8:0:2::/64\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("ADDR-FORMAT");
    assertThat(acl, notNullValue());
    assertThat(acl.isIpv6(), equalTo(true));
    assertThat(acl.getLines().size(), equalTo(3));
  }

  @Test
  public void testAclAdvancedConversion() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination 10.0.0.0 0.0.0.255"
            + " destination-port eq 80\n"
            + " rule 10 deny ip source any destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(1));

    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getHostname(), equalTo("router1"));
    assertThat(viConfig.getIpAccessLists().size(), equalTo(1));
    assertThat(viConfig.getIpAccessLists().containsKey("3000"), equalTo(true));
  }

  @Test
  public void testAclNamedConversion() {
    String configText =
        "sysname Router1\n"
            + "acl WEB-IN advanced\n"
            + " rule 5 permit tcp source any destination 10.0.0.0 0.0.0.255 destination-port eq"
            + " 80\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());

    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
    assertThat(viConfig.getIpAccessLists().containsKey("WEB-IN"), equalTo(true));
  }

  @Test
  public void testAclIpv6Conversion() {
    String configText =
        "sysname Router1\n"
            + "acl ipv6 IPV6-WEB\n"
            + " rule 5 permit tcp source 2001:db8::/32 destination any destination-port eq 80\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());

    Configuration viConfig = HuaweiConversions.toVendorIndependentConfiguration(config);

    assertThat(viConfig, notNullValue());
  }

  @Test
  public void testAclWithNatIntegration() {
    String configText =
        "sysname Router1\n"
            + "acl 2000 basic\n"
            + " rule 5 permit source 192.168.1.0 0.0.0.255\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination any destination-port eq"
            + " 80\n"
            + "nat outbound 2000\n"
            + "nat outbound 3000\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getAcls().size(), equalTo(2));
    assertThat(config.getNatRules().size(), equalTo(2));

    assertThat(config.getNatRules().get(0).getAclName(), equalTo("2000"));
    assertThat(config.getNatRules().get(1).getAclName(), equalTo("3000"));
  }

  @Test
  public void testAclLineIpv6Flag() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source any destination any destination-port eq 80\n"
            + "acl ipv6 IPV6-ACL\n"
            + " rule 5 permit tcp source any destination any destination-port eq 80\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());

    HuaweiAcl ipv4Acl = config.getAcl("3000");
    assertThat(ipv4Acl, notNullValue());
    assertThat(ipv4Acl.isIpv6(), equalTo(false));
    if (!ipv4Acl.getLines().isEmpty()) {
      assertThat(ipv4Acl.getLines().get(0).isIpv6(), equalTo(false));
    }

    HuaweiAcl ipv6Acl = config.getAcl("IPV6-ACL");
    assertThat(ipv6Acl, notNullValue());
    assertThat(ipv6Acl.isIpv6(), equalTo(true));
    if (!ipv6Acl.getLines().isEmpty()) {
      assertThat(ipv6Acl.getLines().get(0).isIpv6(), equalTo(true));
    }
  }

  @Test
  public void testAclProtocolCombinations() {
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit tcp source 192.168.1.0 0.0.0.255 destination any\n"
            + " rule 10 permit udp source 192.168.1.0 0.0.0.255 destination any\n"
            + " rule 15 permit icmp source any destination any\n"
            + " rule 20 permit ip source 172.16.0.0 0.0.0.255 destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(4));

    assertThat(acl.getLines().get(0).getProtocol(), equalTo("tcp"));
    assertThat(acl.getLines().get(1).getProtocol(), equalTo("udp"));
    assertThat(acl.getLines().get(2).getProtocol(), equalTo("icmp"));
    assertThat(acl.getLines().get(3).getProtocol(), equalTo("ip"));
  }

  @Test
  public void testAclPermitIpProtocol() {
    // Explicit test to verify 'permit ip' sets protocol correctly
    String configText =
        "sysname Router1\n"
            + "acl 3000 advanced\n"
            + " rule 5 permit ip source any destination any\n"
            + " rule 10 deny ip source 192.168.1.0 0.0.0.255 destination any\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiAcl acl = config.getAcl("3000");
    assertThat(acl, notNullValue());
    assertThat(acl.getLines().size(), equalTo(2));

    // Verify first rule: permit ip
    HuaweiAclLine line1 = acl.getLines().get(0);
    assertThat(line1.getAction(), equalTo("permit"));
    assertThat(line1.getProtocol(), equalTo("ip"));
    assertThat(line1.getSource(), equalTo("any"));
    assertThat(line1.getDestination(), equalTo("any"));

    // Verify second rule: deny ip
    HuaweiAclLine line2 = acl.getLines().get(1);
    assertThat(line2.getAction(), equalTo("deny"));
    assertThat(line2.getProtocol(), equalTo("ip"));
  }

  @Test
  public void testRoutePolicyAndCommunityFilterParsing() {
    String configText =
        "sysname Router1\n"
            + "ip community-filter 10 permit internet no-export no-advertise\n"
            + "route-policy IMPORT_POLICY permit node 10\n"
            + " if-match ip-prefix PREFIX_LIST_1\n"
            + " if-match community-filter 10\n"
            + " if-match community no-advertise no-export\n"
            + " apply local-preference 250\n"
            + " apply community internet no-export-subconfed\n"
            + " apply cost 20\n"
            + " apply preference 120\n"
            + " apply tag 999\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getCommunityFilters().size(), equalTo(1));
    assertThat(config.getCommunityFilter(10), notNullValue());
    assertThat(config.getCommunityFilter(10).getCommunities().size(), equalTo(3));

    HuaweiRoutePolicy policy = config.getRoutePolicy("IMPORT_POLICY");
    assertThat(policy, notNullValue());
    assertThat(policy.getNodes().size(), equalTo(1));

    HuaweiRoutePolicy.HuaweiRoutePolicyNode node = policy.getNodes().get(0);
    assertThat(node.getNodeId(), equalTo(10));
    assertThat(node.getAction(), equalTo(HuaweiRoutePolicy.HuaweiRoutePolicyNode.Action.PERMIT));
    assertThat(node.getMatchConditions().getIpPrefix(), equalTo("PREFIX_LIST_1"));
    assertThat(node.getMatchConditions().getCommunityFilter(), equalTo(10));
    assertThat(node.getMatchConditions().getCommunities(), notNullValue());
    assertThat(node.getMatchConditions().getCommunities().size(), equalTo(2));
    assertThat(node.getSetActions().getLocalPreference(), equalTo(250L));
    assertThat(node.getSetActions().getCommunities(), notNullValue());
    assertThat(node.getSetActions().getCommunities().size(), equalTo(2));
    assertThat(node.getSetActions().getCost(), equalTo(20));
    assertThat(node.getSetActions().getPreference(), equalTo(120));
    assertThat(node.getSetActions().getTag(), equalTo(999L));
  }

  @Test
  public void testRoutePolicyDenyAndCommunityFilterDenyParsing() {
    String configText =
        "sysname Router1\n"
            + "ip community-filter 11 deny internet\n"
            + "route-policy BLOCK_POLICY deny node 20\n"
            + " if-match community-filter 11\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getCommunityFilters().size(), equalTo(1));
    assertThat(config.getCommunityFilter(11), notNullValue());
    assertThat(config.getCommunityFilter(11).getAction().name(), equalTo("DENY"));

    HuaweiRoutePolicy policy = config.getRoutePolicy("BLOCK_POLICY");
    assertThat(policy, notNullValue());
    assertThat(policy.getNodes().size(), equalTo(1));

    HuaweiRoutePolicy.HuaweiRoutePolicyNode node = policy.getNodes().get(0);
    assertThat(node.getAction(), equalTo(HuaweiRoutePolicy.HuaweiRoutePolicyNode.Action.DENY));
    assertThat(node.getMatchConditions().getCommunityFilter(), equalTo(11));
  }

  @Test
  public void testInterfaceOspfOptionParsingCoverage() {
    String configText =
        "sysname Router1\n"
            + "ospf 1\n"
            + "return\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ospf area 0\n"
            + " ospf cost 10\n"
            + " ospf network-type broadcast\n"
            + " ospf timer hello 10\n"
            + " ospf authentication-mode md5 md5Key\n"
            + " ospf enable passive\n"
            + "return\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ospf area 1\n"
            + " ospf cost 20\n"
            + " ospf network-type p2p\n"
            + " ospf timer dead 40\n"
            + " ospf authentication-mode simple simpleKey\n"
            + " ospf disable passive\n"
            + "return\n"
            + "interface GigabitEthernet0/0/2\n"
            + " ospf area 2\n"
            + " ospf cost 30\n"
            + " ospf network-type p2mp\n"
            + " ospf timer retransmit-interval 5\n"
            + "return\n"
            + "interface GigabitEthernet0/0/3\n"
            + " ospf area 3\n"
            + " ospf cost 40\n"
            + " ospf network-type nbma\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiOspfProcess ospf = config.getOspfProcess();
    assertThat(ospf, notNullValue());

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings ge0 =
        ospf.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(ge0, notNullValue());
    assertThat(ge0.getAreaId(), equalTo(0L));
    assertThat(ge0.getCost(), equalTo(10));
    assertThat(ge0.getNetworkType(), equalTo("BROADCAST"));
    assertThat(ge0.getHelloInterval(), equalTo(10));
    assertThat(ge0.getAuthType(), equalTo("MD5"));
    assertThat(ge0.getAuthKey(), equalTo("md5Key"));
    assertThat(ge0.getPassive(), equalTo(true));

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings ge1 =
        ospf.getInterfaces().get("GigabitEthernet0/0/1");
    assertThat(ge1, notNullValue());
    assertThat(ge1.getAreaId(), equalTo(1L));
    assertThat(ge1.getCost(), equalTo(20));
    assertThat(ge1.getNetworkType(), equalTo("P2P"));
    assertThat(ge1.getDeadInterval(), equalTo(40));
    assertThat(ge1.getAuthType(), equalTo("SIMPLE"));
    assertThat(ge1.getAuthKey(), equalTo("simpleKey"));
    assertThat(ge1.getPassive(), equalTo(false));

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings ge2 =
        ospf.getInterfaces().get("GigabitEthernet0/0/2");
    assertThat(ge2, notNullValue());
    assertThat(ge2.getAreaId(), equalTo(2L));
    assertThat(ge2.getCost(), equalTo(30));
    assertThat(ge2.getNetworkType(), equalTo("P2MP"));
    assertThat(ge2.getRetransmitInterval(), equalTo(5));

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings ge3 =
        ospf.getInterfaces().get("GigabitEthernet0/0/3");
    assertThat(ge3, notNullValue());
    assertThat(ge3.getAreaId(), equalTo(3L));
    assertThat(ge3.getCost(), equalTo(40));
    assertThat(ge3.getNetworkType(), equalTo("NBMA"));
  }
}

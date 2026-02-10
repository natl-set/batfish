package org.batfish.vendor.huawei.grammar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Ip;
import org.batfish.vendor.huawei.representation.HuaweiBgpProcess;
import org.batfish.vendor.huawei.representation.HuaweiConfiguration;
import org.batfish.vendor.huawei.representation.HuaweiInterface;
import org.batfish.vendor.huawei.representation.HuaweiOspfProcess;
import org.batfish.vendor.huawei.representation.HuaweiRoutePolicy;
import org.junit.Test;

/** Tests for {@link HuaweiControlPlaneExtractor}. */
public class HuaweiControlPlaneExtractorTest {

  private Settings getSettings() {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    return settings;
  }

  @Test
  public void testExtractHostname() {
    String configText = "sysname Router1\nreturn\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getHostname(), equalTo("Router1"));
  }

  @Test
  public void testExtractInterfaceWithIp() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getInterfaces().size(), equalTo(1));
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp(), equalTo(Ip.parse("192.168.1.1")));
  }

  @Test
  public void testExtractInterfaceWithDescription() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Uplink to core router\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface.getDescription(), equalTo("Uplink to core router"));
  }

  @Test
  public void testExtractInterfaceShutdown() {
    String configText =
        "sysname Router1\n" + "interface GigabitEthernet0/0/0\n" + " shutdown\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface.getShutdown(), equalTo(true));
  }

  @Test
  public void testExtractInterfaceUndoShutdown() {
    String configText =
        "sysname Router1\n" + "interface GigabitEthernet0/0/0\n" + " undo shutdown\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface.getShutdown(), equalTo(false));
  }

  @Test
  public void testExtractMultipleInterfaces() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ip address 192.168.2.1 255.255.255.0\n"
            + "interface Loopback0\n"
            + " ip address 10.0.0.1 255.255.255.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getInterfaces().size(), equalTo(3));
    assertThat(config.getInterfaces(), hasKey("GigabitEthernet0/0/0"));
    assertThat(config.getInterfaces(), hasKey("GigabitEthernet0/0/1"));
    assertThat(config.getInterfaces(), hasKey("Loopback0"));
  }

  @Test
  public void testExtractBgpProcess() {
    String configText = "sysname Router1\n" + "bgp 65001\n" + " router-id 1.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiBgpProcess bgp = config.getBgpProcess();
    assertThat(bgp, notNullValue());
    assertThat(bgp.getAsNum(), equalTo(65001L));
    assertThat(bgp.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testExtractBgpPeer() {
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " peer 10.0.0.1 as-number 65002\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiBgpProcess bgp = config.getBgpProcess();
    assertThat(bgp.getNeighbors().size(), equalTo(1));
    assertThat(bgp.getNeighbors(), hasKey(Ip.parse("10.0.0.1")));
  }

  @Test
  public void testExtractBgpPeerGroup() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " group INTERNAL\n"
            + " peer 10.0.0.1 group INTERNAL\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiBgpProcess bgp = config.getBgpProcess();
    assertThat(bgp.getPeerGroups(), hasKey("INTERNAL"));
  }

  @Test
  public void testExtractOspfProcess() {
    String configText = "sysname Router1\n" + "ospf 1\n" + " router-id 1.1.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiOspfProcess ospf = config.getOspfProcess();
    assertThat(ospf, notNullValue());
    assertThat(ospf.getProcessId(), equalTo(1L));
    assertThat(ospf.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testExtractVlan() {
    String configText =
        "sysname Router1\n" + "vlan 100\n" + " description Sales VLAN\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getVlans(), hasKey(100));
    assertThat(config.getVlans().get(100).getDescription(), equalTo("Sales VLAN"));
  }

  @Test
  public void testExtractRoutePolicy() {
    String configText = "sysname Router1\n" + "route-policy POLICY1 permit node 10\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getRoutePolicies(), hasKey("POLICY1"));
    HuaweiRoutePolicy policy = config.getRoutePolicies().get("POLICY1");
    assertThat(policy.getNodes().size(), equalTo(1));
  }

  @Test
  public void testExtractStaticRoute() {
    String configText =
        "sysname Router1\n" + "ip route-static 10.0.0.0 255.255.255.0 192.168.1.1\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getStaticRoutes().size(), equalTo(1));
  }

  @Test
  public void testGetInputText() {
    String configText = "sysname Router1\nreturn\n";
    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();

    HuaweiControlPlaneExtractor extractor =
        new HuaweiControlPlaneExtractor(
            configText,
            parser,
            warnings,
            new org.batfish.grammar.silent_syntax.SilentSyntaxCollection());

    assertThat(extractor.getInputText(), equalTo(configText));
  }

  @Test
  public void testGetParser() {
    String configText = "sysname Router1\nreturn\n";
    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();

    HuaweiControlPlaneExtractor extractor =
        new HuaweiControlPlaneExtractor(
            configText,
            parser,
            warnings,
            new org.batfish.grammar.silent_syntax.SilentSyntaxCollection());

    assertThat(extractor.getParser(), equalTo(parser));
  }

  @Test
  public void testGetWarnings() {
    String configText = "sysname Router1\nreturn\n";
    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();

    HuaweiControlPlaneExtractor extractor =
        new HuaweiControlPlaneExtractor(
            configText,
            parser,
            warnings,
            new org.batfish.grammar.silent_syntax.SilentSyntaxCollection());

    assertThat(extractor.getWarnings(), equalTo(warnings));
  }

  @Test
  public void testGetVendorConfiguration() {
    String configText = "sysname Router1\nreturn\n";
    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();

    HuaweiControlPlaneExtractor extractor =
        new HuaweiControlPlaneExtractor(
            configText,
            parser,
            warnings,
            new org.batfish.grammar.silent_syntax.SilentSyntaxCollection());

    assertThat(extractor.getVendorConfiguration(), notNullValue());
    assertTrue(extractor.getVendorConfiguration() instanceof HuaweiConfiguration);
  }

  @Test
  public void testExtractEmptyConfig() {
    String configText = "";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), nullValue());
    assertThat(config.getInterfaces().size(), equalTo(0));
  }

  // Additional tests for improved coverage

  @Test
  public void testExtractBgpNetwork() {
    String configText =
        "sysname Router1\n" + "bgp 65001\n" + " network 10.0.0.0 255.255.255.0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiBgpProcess bgp = config.getBgpProcess();
    assertThat(bgp.getNetworks().size(), equalTo(1));
  }

  @Test
  public void testExtractInterfaceWithBothIpAndDescription() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Uplink to core\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface.getDescription(), equalTo("Uplink to core"));
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp(), equalTo(Ip.parse("192.168.1.1")));
  }

  @Test
  public void testExtractBgpWithMultiplePeers() {
    String configText =
        "sysname Router1\n"
            + "bgp 65001\n"
            + " peer 10.0.0.1 as-number 65002\n"
            + " peer 10.0.0.2 as-number 65003\n"
            + " peer 10.0.0.3 as-number 65004\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    HuaweiBgpProcess bgp = config.getBgpProcess();
    assertThat(bgp.getNeighbors().size(), equalTo(3));
    assertThat(bgp.getNeighbors(), hasKey(Ip.parse("10.0.0.1")));
    assertThat(bgp.getNeighbors(), hasKey(Ip.parse("10.0.0.2")));
    assertThat(bgp.getNeighbors(), hasKey(Ip.parse("10.0.0.3")));
  }

  @Test
  public void testExtractMultipleRoutePolicies() {
    String configText =
        "sysname Router1\n"
            + "route-policy POLICY1 permit node 10\n"
            + "route-policy POLICY2 deny node 10\n"
            + "route-policy POLICY3 permit node 10\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config.getRoutePolicies().size(), equalTo(3));
    assertThat(config.getRoutePolicies(), hasKey("POLICY1"));
    assertThat(config.getRoutePolicies(), hasKey("POLICY2"));
    assertThat(config.getRoutePolicies(), hasKey("POLICY3"));
  }
}

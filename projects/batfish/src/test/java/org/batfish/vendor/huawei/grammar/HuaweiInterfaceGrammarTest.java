package org.batfish.vendor.huawei.grammar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.vendor.huawei.representation.HuaweiConfiguration;
import org.batfish.vendor.huawei.representation.HuaweiInterface;
import org.junit.Test;

/**
 * Comprehensive interface tests for Huawei grammar parsing. Tests cover various interface types,
 * configurations, and edge cases to ensure complete coverage of interface parsing functionality.
 */
public class HuaweiInterfaceGrammarTest {

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
  public void testInterfaceTypeEthernet() {
    String configText =
        "sysname Router1\n"
            + "interface Ethernet0/0/1\n"
            + " description Legacy port\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("Ethernet0/0/1");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Legacy port"));
    assertThat(iface.getAddress().getIp().toString(), equalTo("10.0.0.1"));
  }

  @Test
  public void testInterfaceTypeLoopbackLowercase() {
    String configText =
        "sysname Router1\n"
            + "interface Loopback0\n"
            + " description Router ID\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("Loopback0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Router ID"));
    assertThat(iface.getAddress().getIp().toString(), equalTo("1.1.1.1"));
  }

  @Test
  public void testInterfaceTypeLoopbackMultiple() {
    String configText =
        "sysname Router1\n"
            + "interface Loopback0\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "interface Loopback1\n"
            + " ip address 2.2.2.2 255.255.255.255\n"
            + "interface Loopback99\n"
            + " ip address 9.9.9.9 255.255.255.255\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(3));

    assertThat(config.getInterfaces().get("Loopback0"), notNullValue());
    assertThat(config.getInterfaces().get("Loopback1"), notNullValue());
    assertThat(config.getInterfaces().get("Loopback99"), notNullValue());
  }

  @Test
  public void testInterfaceTypeEthTrunk() {
    String configText =
        "sysname Router1\n"
            + "interface Eth-Trunk1\n"
            + " description Port channel to core\n"
            + " ip address 10.1.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("Eth-Trunk1");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Port channel to core"));
    assertThat(iface.getAddress().getIp().toString(), equalTo("10.1.1.1"));
  }

  @Test
  public void testInterfaceTypeEthTrunkMultiple() {
    String configText =
        "sysname Router1\n"
            + "interface Eth-Trunk1\n"
            + " ip address 10.1.1.1 255.255.255.0\n"
            + "interface Eth-Trunk10\n"
            + " ip address 10.2.1.1 255.255.255.0\n"
            + "interface Eth-Trunk100\n"
            + " ip address 10.3.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(3));

    assertThat(config.getInterfaces().get("Eth-Trunk1"), notNullValue());
    assertThat(config.getInterfaces().get("Eth-Trunk10"), notNullValue());
    assertThat(config.getInterfaces().get("Eth-Trunk100"), notNullValue());
  }

  @Test
  public void testInterfaceType10GE() {
    String configText =
        "sysname Router1\n"
            + "interface 10GE1/0/1\n"
            + " description 10G uplink\n"
            + " ip address 10.10.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("10GE1/0/1");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("10G uplink"));
  }

  @Test
  public void testInterfaceType25GE() {
    String configText =
        "sysname Router1\n"
            + "interface 25GE1/0/1\n"
            + " description 25G uplink\n"
            + " ip address 10.25.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("25GE1/0/1");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("25G uplink"));
  }

  @Test
  public void testInterfaceType40GE() {
    String configText =
        "sysname Router1\n"
            + "interface 40GE1/0/1\n"
            + " description 40G uplink\n"
            + " ip address 10.40.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("40GE1/0/1");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("40G uplink"));
  }

  @Test
  public void testInterfaceType100GE() {
    String configText =
        "sysname Router1\n"
            + "interface 100GE1/0/1\n"
            + " description 100G uplink\n"
            + " ip address 10.100.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("100GE1/0/1");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("100G uplink"));
  }

  @Test
  public void testInterfaceDescriptionWithSpaces() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description    Multiple    spaces    here\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), notNullValue());
  }

  @Test
  public void testInterfaceDescriptionEmpty() {
    String configText =
        "sysname Router1\n" + "interface GigabitEthernet0/0/0\n" + " description\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
  }

  @Test
  public void testInterfaceDescriptionWithSpecialChars() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Uplink_Router01 (Primary)\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), notNullValue());
  }

  @Test
  public void testInterfaceDescriptionWithNumbers() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Connection at port 8080\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), notNullValue());
  }

  @Test
  public void testInterfaceMultipleInSameConfig() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ip address 192.168.2.1 255.255.255.0\n"
            + "interface Vlanif100\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "interface Loopback0\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "interface Eth-Trunk1\n"
            + " ip address 10.1.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(5));

    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/1"), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif100"), notNullValue());
    assertThat(config.getInterfaces().get("Loopback0"), notNullValue());
    assertThat(config.getInterfaces().get("Eth-Trunk1"), notNullValue());
  }

  @Test
  public void testInterfaceWithoutIpOrDescription() {
    String configText = "sysname Router1\n" + "interface GigabitEthernet0/0/0\n" + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getAddress(), nullValue());
    assertThat(iface.getDescription(), nullValue());
    assertThat(iface.getShutdown(), equalTo(false));
  }

  @Test
  public void testInterfaceWithDescriptionOnly() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description No IP configured\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("No IP configured"));
    assertThat(iface.getAddress(), nullValue());
  }

  @Test
  public void testInterfaceWithIpOnly() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getDescription(), nullValue());
  }

  @Test
  public void testInterfacePortNumberEdgeCases() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.0.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/65535\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(2));

    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/65535"), notNullValue());
  }

  @Test
  public void testInterfaceCardNumberVariations() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ip address 192.168.0.1 255.255.255.0\n"
            + "interface GigabitEthernet0/1/1\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface GigabitEthernet1/0/1\n"
            + " ip address 192.168.2.1 255.255.255.0\n"
            + "interface GigabitEthernet10/10/10\n"
            + " ip address 192.168.10.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(4));

    assertThat(config.getInterfaces().get("GigabitEthernet0/0/1"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/1/1"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet1/0/1"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet10/10/10"), notNullValue());
  }

  @Test
  public void testInterfaceWithSubInterfaceNumber() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.100\n"
            + " description Subinterface for VLAN\n"
            + " ip address 10.0.100.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0.100");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Subinterface for VLAN"));
    assertThat(iface.getAddress().getIp().toString(), equalTo("10.0.100.1"));
  }

  @Test
  public void testInterfaceSubinterfaceHighNumber() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.4094\n"
            + " ip address 10.0.100.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0.4094");
    assertThat(iface, notNullValue());
  }

  @Test
  public void testInterfaceVlanifVariousIds() {
    String configText =
        "sysname Router1\n"
            + "interface Vlanif1\n"
            + " ip address 10.0.1.1 255.255.255.0\n"
            + "interface Vlanif100\n"
            + " ip address 10.0.100.1 255.255.255.0\n"
            + "interface Vlanif999\n"
            + " ip address 10.0.999.1 255.255.255.0\n"
            + "interface Vlanif4094\n"
            + " ip address 10.4.94.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(4));

    assertThat(config.getInterfaces().get("Vlanif1"), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif100"), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif999"), notNullValue());
    assertThat(config.getInterfaces().get("Vlanif4094"), notNullValue());
  }

  @Test
  public void testInterfaceShutdownExplicit() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " shutdown\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getShutdown(), equalTo(true));
    assertThat(iface.getAddress(), notNullValue());
  }

  @Test
  public void testInterfaceShutdownThenUndo() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " shutdown\n"
            + " undo shutdown\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getShutdown(), equalTo(false));
  }

  @Test
  public void testInterfaceUndoThenShutdown() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " undo shutdown\n"
            + " shutdown\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getShutdown(), equalTo(true));
  }

  @Test
  public void testInterfaceDifferentSubnetMasks() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.255\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ip address 10.0.0.1 255.255.255.252\n"
            + "interface GigabitEthernet0/0/2\n"
            + " ip address 172.16.0.1 255.255.0.0\n"
            + "interface GigabitEthernet0/0/3\n"
            + " ip address 8.8.8.8 255.255.255.254\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(4));

    HuaweiInterface iface0 = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface0.getAddress().getPrefix().getPrefixLength(), equalTo(32));

    HuaweiInterface iface1 = config.getInterfaces().get("GigabitEthernet0/0/1");
    assertThat(iface1.getAddress().getPrefix().getPrefixLength(), equalTo(30));

    HuaweiInterface iface2 = config.getInterfaces().get("GigabitEthernet0/0/2");
    assertThat(iface2.getAddress().getPrefix().getPrefixLength(), equalTo(16));

    HuaweiInterface iface3 = config.getInterfaces().get("GigabitEthernet0/0/3");
    assertThat(iface3.getAddress().getPrefix().getPrefixLength(), equalTo(31));
  }

  @Test
  public void testInterfacePrivateIpRanges() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 10.0.0.1 255.0.0.0\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ip address 172.16.0.1 255.240.0.0\n"
            + "interface GigabitEthernet0/0/2\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(3));

    assertThat(
        config.getInterfaces().get("GigabitEthernet0/0/0").getAddress().getIp().toString(),
        equalTo("10.0.0.1"));
    assertThat(
        config.getInterfaces().get("GigabitEthernet0/0/1").getAddress().getIp().toString(),
        equalTo("172.16.0.1"));
    assertThat(
        config.getInterfaces().get("GigabitEthernet0/0/2").getAddress().getIp().toString(),
        equalTo("192.168.1.1"));
  }

  @Test
  public void testInterfacePublicIpRanges() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 8.8.8.8 255.255.255.255\n"
            + "interface GigabitEthernet0/0/1\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "interface GigabitEthernet0/0/2\n"
            + " ip address 203.0.113.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(3));

    assertThat(
        config.getInterfaces().get("GigabitEthernet0/0/0").getAddress().getIp().toString(),
        equalTo("8.8.8.8"));
    assertThat(
        config.getInterfaces().get("GigabitEthernet0/0/1").getAddress().getIp().toString(),
        equalTo("1.1.1.1"));
    assertThat(
        config.getInterfaces().get("GigabitEthernet0/0/2").getAddress().getIp().toString(),
        equalTo("203.0.113.1"));
  }

  @Test
  public void testInterfaceAllAttributes() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Primary uplink to ISP\n"
            + " ip address 203.0.113.2 255.255.255.252\n"
            + " undo shutdown\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Primary uplink to ISP"));
    assertThat(iface.getAddress().getIp().toString(), equalTo("203.0.113.2"));
    assertThat(iface.getShutdown(), equalTo(false));
  }

  @Test
  public void testInterfaceNoAddressAfterShutdown() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " shutdown\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getShutdown(), equalTo(true));
    assertThat(iface.getAddress(), notNullValue());
  }

  @Test
  public void testInterfaceOrderDoesntMatter() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description Test value\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + " shutdown\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), equalTo("Test value"));
    assertThat(iface.getAddress().getIp().toString(), equalTo("192.168.1.1"));
    assertThat(iface.getShutdown(), equalTo(true));
  }

  @Test
  public void testInterfaceLargePortNumber() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/1000\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/65535\n"
            + " ip address 192.168.2.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(2));

    assertThat(config.getInterfaces().get("GigabitEthernet0/0/1000"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/65535"), notNullValue());
  }

  @Test
  public void testInterfaceZeroInName() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.0.1 255.255.255.0\n"
            + "interface GigabitEthernet0/10/0\n"
            + " ip address 192.168.10.1 255.255.255.0\n"
            + "interface GigabitEthernet10/0/0\n"
            + " ip address 192.168.20.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(3));

    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/10/0"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet10/0/0"), notNullValue());
  }

  @Test
  public void testInterfaceDot1qTerminationVid() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.100\n"
            + " dot1q termination vid 100\n"
            + " ip address 10.0.100.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(1));

    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0.100");
    assertThat(iface, notNullValue());
    assertThat(iface.getAddress(), notNullValue());
    assertThat(iface.getAddress().getIp().toString(), equalTo("10.0.100.1"));
  }

  @Test
  public void testMultipleSubinterfacesDifferentVlans() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.10\n"
            + " dot1q termination vid 10\n"
            + " ip address 10.0.10.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/0.20\n"
            + " dot1q termination vid 20\n"
            + " ip address 10.0.20.1 255.255.255.0\n"
            + "interface GigabitEthernet0/0/0.30\n"
            + " dot1q termination vid 30\n"
            + " ip address 10.0.30.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(3));

    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0.10"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0.20"), notNullValue());
    assertThat(config.getInterfaces().get("GigabitEthernet0/0/0.30"), notNullValue());
  }

  @Test
  public void testInterfaceDefaultBandwidth() {
    assertThat(HuaweiInterface.getDefaultBandwidth("GigabitEthernet0/0/0"), equalTo(1E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("10GE1/0/1"), equalTo(10E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("25GE1/0/1"), equalTo(25E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("40GE1/0/1"), equalTo(40E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("100GE1/0/1"), equalTo(100E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Ethernet0/0/1"), equalTo(100E6D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Loopback0"), equalTo(8E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Vlanif100"), equalTo(1E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Pos1/0/0"), equalTo(155E6D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Serial1/0/0"), equalTo(1.544E6D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Tunnel1"), equalTo(100E3D));
  }

  @Test
  public void testInterfaceUnknownBandwidth() {
    Double bw = HuaweiInterface.getDefaultBandwidth("UnknownInterface0/0/0");
    assertThat(bw, nullValue());
  }

  @Test
  public void testInterfaceDescriptionLong() {
    String longDesc =
        "This is a very long interface description that contains many words "
            + "and should still be parsed correctly";
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description "
            + longDesc
            + "\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getDescription(), notNullValue());
  }

  @Test
  public void testInterfaceWithNullSubstanza() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + " some-unrecognized-command value\n"
            + " description Still should work\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getLenientSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface, notNullValue());
    assertThat(iface.getAddress(), notNullValue());
  }

  @Test
  public void testSubinterfaceWithoutDot1q() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0.100\n"
            + " ip address 10.0.100.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/0.100");
    assertThat(iface, notNullValue());
    assertThat(iface.getAddress(), notNullValue());
  }

  @Test
  public void testInterfaceMixedTypesInConfig() {
    String configText =
        "sysname Router1\n"
            + "interface GigabitEthernet0/0/0\n"
            + " description WAN interface\n"
            + " ip address 203.0.113.2 255.255.255.252\n"
            + "interface GigabitEthernet0/0/1\n"
            + " description LAN interface\n"
            + " ip address 192.168.1.1 255.255.255.0\n"
            + "interface Vlanif100\n"
            + " description Management VLAN\n"
            + " ip address 10.0.0.1 255.255.255.0\n"
            + "interface Loopback0\n"
            + " description Router ID\n"
            + " ip address 1.1.1.1 255.255.255.255\n"
            + "interface Eth-Trunk1\n"
            + " description Uplink channel\n"
            + " ip address 10.1.1.1 255.255.255.0\n"
            + "return\n";

    HuaweiCombinedParser parser = new HuaweiCombinedParser(configText, getSettings());
    Warnings warnings = new Warnings();
    HuaweiConfiguration config = HuaweiControlPlaneExtractor.extract(configText, parser, warnings);

    assertThat(config, notNullValue());
    assertThat(config.getInterfaces().size(), equalTo(5));

    HuaweiInterface wanIface = config.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(wanIface.getDescription(), equalTo("WAN interface"));
    assertThat(wanIface.getAddress().getIp().toString(), equalTo("203.0.113.2"));

    HuaweiInterface lanIface = config.getInterfaces().get("GigabitEthernet0/0/1");
    assertThat(lanIface.getDescription(), equalTo("LAN interface"));

    HuaweiInterface vlanif = config.getInterfaces().get("Vlanif100");
    assertThat(vlanif.getDescription(), equalTo("Management VLAN"));

    HuaweiInterface loopback = config.getInterfaces().get("Loopback0");
    assertThat(loopback.getDescription(), equalTo("Router ID"));

    HuaweiInterface trunk = config.getInterfaces().get("Eth-Trunk1");
    assertThat(trunk.getDescription(), equalTo("Uplink channel"));
  }
}

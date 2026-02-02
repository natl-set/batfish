package org.batfish.vendor.huawei.representation;

import static org.batfish.vendor.huawei.representation.HuaweiConversions.toVendorIndependentConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

/** Tests for HuaweiConversions */
public class HuaweiConversionsTest {

  @Test
  public void testToVendorIndependentConfigurationBasic() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("test-router"));
    assertThat(config.getConfigurationFormat(), equalTo(ConfigurationFormat.HUAWEI));
    assertThat(config.getVendorFamily().getHuawei(), notNullValue());
    assertThat(config.getVrfs(), hasKey("default"));
  }

  @Test
  public void testToVendorIndependentConfigurationWithInterface() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");

    HuaweiInterface huaweiInterface = new HuaweiInterface("GigabitEthernet0/0/0");
    huaweiInterface.setShutdown(false);
    huaweiInterface.setAddress(ConcreteInterfaceAddress.parse("192.168.1.1/24"));
    huaweiInterface.setDescription("Test interface");
    huaweiInterface.setMtu(1500);

    SortedMap<String, HuaweiInterface> interfaces = new TreeMap<>();
    interfaces.put("GigabitEthernet0/0/0", huaweiInterface);
    huaweiConfig.setInterfaces(interfaces);

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    assertThat(config, notNullValue());
    assertThat(config.getAllInterfaces(), hasKey("GigabitEthernet0/0/0"));

    Interface iface = config.getAllInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface.getName(), equalTo("GigabitEthernet0/0/0"));
    assertThat(iface.getDescription(), equalTo("Test interface"));
    assertThat(iface.getAdminUp(), equalTo(true));
    assertThat(iface.getMtu(), equalTo(1500));
    assertThat(iface.getAddress(), notNullValue());
    assertThat(
        ((ConcreteInterfaceAddress) iface.getAddress()).getIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(iface.getInterfaceType(), equalTo(InterfaceType.PHYSICAL));
  }

  @Test
  public void testToVendorIndependentConfigurationWithStaticRoute() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");

    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));
    route.setNextHopIp(Ip.parse("192.168.1.1"));
    route.setPreference(100);
    huaweiConfig.getStaticRoutes().add(route);

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    assertThat(config, notNullValue());
    Vrf vrf = config.getVrfs().get("default");
    assertThat(vrf, notNullValue());
    assertThat(vrf.getStaticRoutes().size(), equalTo(1));

    org.batfish.datamodel.StaticRoute staticRoute = vrf.getStaticRoutes().iterator().next();
    assertThat(staticRoute.getNetwork(), equalTo(Prefix.parse("10.0.0.0/24")));
    assertThat(staticRoute.getNextHopIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(staticRoute.getAdministrativeCost(), equalTo(100));
  }

  @Test
  public void testToVendorIndependentConfigurationWithVrf() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");

    HuaweiVrf vrf1 = new HuaweiVrf("VRF1");
    vrf1.setRouteDistinguisher("100:1");

    SortedMap<String, HuaweiVrf> vrfs = new TreeMap<>();
    vrfs.put("VRF1", vrf1);
    huaweiConfig.setVrfs(vrfs);

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    assertThat(config, notNullValue());
    assertThat(config.getVrfs(), hasKey("VRF1"));
    assertThat(config.getVrfs().get("VRF1").getName(), equalTo("VRF1"));
  }

  @Test
  public void testToVendorIndependentConfigurationWithAcl() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");

    HuaweiAcl acl = new HuaweiAcl("2000", HuaweiAcl.AclType.BASIC);

    HuaweiAclLine line1 = new HuaweiAclLine(10, "permit");
    line1.setSource("10.0.0.0 0.0.0.255");
    acl.getLines().add(line1);

    HuaweiAclLine line2 = new HuaweiAclLine(20, "deny");
    line2.setSource("192.168.1.0 0.0.0.255");
    acl.getLines().add(line2);

    SortedMap<String, HuaweiAcl> acls = new TreeMap<>();
    acls.put("2000", acl);
    huaweiConfig.setAcls(acls);

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    assertThat(config, notNullValue());
    assertThat(config.getIpAccessLists(), hasKey("2000"));
    org.batfish.datamodel.IpAccessList ipAccessList = config.getIpAccessLists().get("2000");
    assertThat(ipAccessList.getName(), equalTo("2000"));
    assertThat(ipAccessList.getLines().size(), equalTo(2));
  }

  @Test
  public void testToVendorIndependentConfigurationWithBgp() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");

    HuaweiBgpProcess bgp = new HuaweiBgpProcess(65001);
    bgp.setRouterId(Ip.parse("1.1.1.1"));
    huaweiConfig.setBgpProcess(bgp);

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    assertThat(config, notNullValue());
    Vrf vrf = config.getVrfs().get("default");
    assertThat(vrf.getBgpProcess(), notNullValue());
    assertThat(vrf.getBgpProcess().getRouterId(), equalTo(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testToVendorIndependentConfigurationWithOspf() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");

    HuaweiOspfProcess ospf = new HuaweiOspfProcess(1L);
    ospf.setRouterId(Ip.parse("1.1.1.1"));

    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(0L);
    ospf.getAreas().put(0L, area);

    huaweiConfig.setOspfProcess(ospf);

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    assertThat(config, notNullValue());
    Vrf vrf = config.getVrfs().get("default");
    assertThat(vrf.getOspfProcesses(), notNullValue());
    assertThat(vrf.getOspfProcesses(), hasKey("1"));
  }

  @Test
  public void testToVendorIndependentConfigurationFullConversion() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");

    // Add interface
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    iface.setShutdown(false);
    iface.setAddress(ConcreteInterfaceAddress.parse("192.168.1.1/24"));
    iface.setDescription("Test interface");
    iface.setMtu(1500);

    SortedMap<String, HuaweiInterface> interfaces = new TreeMap<>();
    interfaces.put("GigabitEthernet0/0/0", iface);
    huaweiConfig.setInterfaces(interfaces);

    // Add static route
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));
    route.setNextHopIp(Ip.parse("192.168.1.254"));
    route.setPreference(100);
    huaweiConfig.getStaticRoutes().add(route);

    // Add ACL
    HuaweiAcl acl = new HuaweiAcl("2000", HuaweiAcl.AclType.BASIC);
    HuaweiAclLine line = new HuaweiAclLine(10, "permit");
    line.setSource("10.0.0.0 0.0.0.255");
    acl.getLines().add(line);

    SortedMap<String, HuaweiAcl> acls = new TreeMap<>();
    acls.put("2000", acl);
    huaweiConfig.setAcls(acls);

    // Add VRF
    HuaweiVrf vrf = new HuaweiVrf("VRF1");
    vrf.setRouteDistinguisher("100:1");

    SortedMap<String, HuaweiVrf> vrfs = new TreeMap<>();
    vrfs.put("VRF1", vrf);
    huaweiConfig.setVrfs(vrfs);

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    assertThat(config, notNullValue());
    assertThat(config.getHostname(), equalTo("test-router"));
    assertThat(config.getAllInterfaces(), hasKey("GigabitEthernet0/0/0"));
    assertThat(config.getVrfs(), hasKey("default"));
    assertThat(config.getVrfs(), hasKey("VRF1"));
    assertThat(config.getIpAccessLists(), hasKey("2000"));
    assertThat(config.getVrfs().get("default").getStaticRoutes().size(), equalTo(1));
  }

  @Test
  public void testToVendorIndependentConfigurationNoBgp() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");
    // No BGP process configured

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    Vrf vrf = config.getVrfs().get("default");
    assertNull(vrf.getBgpProcess());
  }

  @Test
  public void testToVendorIndependentConfigurationNoOspf() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");
    // No OSPF process configured

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    Vrf vrf = config.getVrfs().get("default");
    assertTrue(vrf.getOspfProcesses().isEmpty());
  }

  @Test
  public void testToVendorIndependentConfigurationNoNat() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");
    // No NAT rules configured

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    // Should not throw exception
    assertThat(config.getVendorFamily().getHuawei(), notNullValue());
  }

  @Test
  public void testToVendorIndependentConfigurationMinimalConfig() {
    HuaweiConfiguration huaweiConfig = new HuaweiConfiguration();
    huaweiConfig.setHostname("test-router");
    // Minimal config with just hostname

    Configuration config = toVendorIndependentConfiguration(huaweiConfig);

    // Should not throw exception
    assertThat(config, notNullValue());
    assertThat(config.getVrfs(), hasKey("default"));
    assertThat(config.getAllInterfaces().size(), equalTo(0));
  }
}

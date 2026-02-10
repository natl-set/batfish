package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.SortedMap;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.junit.Test;

/** Tests for {@link HuaweiConfiguration}. */
public class HuaweiConfigurationTest {

  @Test
  public void testConstructor() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getHostname(), nullValue());
    assertThat(config.getInterfaces().size(), equalTo(0));
    assertThat(config.getVlans().size(), equalTo(0));
    assertThat(config.getStaticRoutes().size(), equalTo(0));
    assertThat(config.getNatRules().size(), equalTo(0));
    assertThat(config.getNatAddressGroups().size(), equalTo(0));
    assertThat(config.getVrfs().size(), equalTo(0));
    assertThat(config.getAcls().size(), equalTo(0));
    assertThat(config.getRoutePolicies().size(), equalTo(0));
    assertThat(config.getCommunityFilters().size(), equalTo(0));
    assertThat(config.getBgpProcess(), nullValue());
    assertThat(config.getOspfProcess(), nullValue());
  }

  @Test
  public void testGetSetHostname() {
    HuaweiConfiguration config = new HuaweiConfiguration();
    assertThat(config.getHostname(), nullValue());

    config.setHostname("Router1");
    assertThat(config.getHostname(), equalTo("Router1"));

    config.setHostname(null);
    assertThat(config.getHostname(), nullValue());
  }

  @Test
  public void testAddInterface() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiInterface iface1 = new HuaweiInterface("GigabitEthernet0/0/0");
    config.addInterface("GigabitEthernet0/0/0", iface1);

    assertThat(config.getInterfaces().size(), equalTo(1));
    assertThat(config.getInterfaces(), hasKey("GigabitEthernet0/0/0"));
    assertThat(config.getInterface("GigabitEthernet0/0/0"), equalTo(iface1));
  }

  @Test
  public void testAddMultipleInterfaces() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    config.addInterface("GigabitEthernet0/0/0", new HuaweiInterface("GigabitEthernet0/0/0"));
    config.addInterface("GigabitEthernet0/0/1", new HuaweiInterface("GigabitEthernet0/0/1"));
    config.addInterface("Loopback0", new HuaweiInterface("Loopback0"));

    assertThat(config.getInterfaces().size(), equalTo(3));
    assertThat(config.getInterfaces(), hasKey("GigabitEthernet0/0/0"));
    assertThat(config.getInterfaces(), hasKey("GigabitEthernet0/0/1"));
    assertThat(config.getInterfaces(), hasKey("Loopback0"));
  }

  @Test
  public void testSetInterfaces() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    SortedMap<String, HuaweiInterface> interfaces = new java.util.TreeMap<>();
    interfaces.put("GigabitEthernet0/0/0", new HuaweiInterface("GigabitEthernet0/0/0"));
    interfaces.put("GigabitEthernet0/0/1", new HuaweiInterface("GigabitEthernet0/0/1"));

    config.setInterfaces(interfaces);

    assertThat(config.getInterfaces(), equalTo(interfaces));
    assertThat(config.getInterfaces().size(), equalTo(2));
  }

  @Test
  public void testGetInterface() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getInterface("GigabitEthernet0/0/0"), nullValue());

    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    config.addInterface("GigabitEthernet0/0/0", iface);

    assertThat(config.getInterface("GigabitEthernet0/0/0"), equalTo(iface));
    assertThat(config.getInterface("Loopback0"), nullValue());
  }

  @Test
  public void testAddVlan() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiVlan vlan = new HuaweiVlan(100);
    config.addVlan(100, vlan);

    assertThat(config.getVlans().size(), equalTo(1));
    assertThat(config.getVlans(), hasKey(100));
    assertThat(config.getVlan(100), equalTo(vlan));
  }

  @Test
  public void testAddMultipleVlans() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    config.addVlan(100, new HuaweiVlan(100));
    config.addVlan(200, new HuaweiVlan(200));
    config.addVlan(300, new HuaweiVlan(300));

    assertThat(config.getVlans().size(), equalTo(3));
    assertThat(config.getVlans(), hasKey(100));
    assertThat(config.getVlans(), hasKey(200));
    assertThat(config.getVlans(), hasKey(300));
  }

  @Test
  public void testSetVlans() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    SortedMap<Integer, HuaweiVlan> vlans = new java.util.TreeMap<>();
    vlans.put(100, new HuaweiVlan(100));
    vlans.put(200, new HuaweiVlan(200));

    config.setVlans(vlans);

    assertThat(config.getVlans(), equalTo(vlans));
    assertThat(config.getVlans().size(), equalTo(2));
  }

  @Test
  public void testGetVlan() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getVlan(100), nullValue());

    HuaweiVlan vlan = new HuaweiVlan(100);
    config.addVlan(100, vlan);

    assertThat(config.getVlan(100), equalTo(vlan));
    assertThat(config.getVlan(200), nullValue());
  }

  @Test
  public void testAddStaticRoute() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiStaticRoute route =
        new HuaweiStaticRoute(org.batfish.datamodel.Prefix.parse("10.0.0.0/24"));
    route.setNextHopIp(org.batfish.datamodel.Ip.parse("192.168.1.1"));

    config.addStaticRoute(route);

    assertThat(config.getStaticRoutes().size(), equalTo(1));
    assertThat(config.getStaticRoutes().get(0), equalTo(route));
  }

  @Test
  public void testSetStaticRoutes() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    List<HuaweiStaticRoute> routes = new java.util.ArrayList<>();
    HuaweiStaticRoute route1 =
        new HuaweiStaticRoute(org.batfish.datamodel.Prefix.parse("10.0.0.0/24"));
    route1.setNextHopIp(org.batfish.datamodel.Ip.parse("192.168.1.1"));
    routes.add(route1);

    HuaweiStaticRoute route2 =
        new HuaweiStaticRoute(org.batfish.datamodel.Prefix.parse("10.1.0.0/24"));
    route2.setNextHopIp(org.batfish.datamodel.Ip.parse("192.168.1.2"));
    routes.add(route2);

    config.setStaticRoutes(routes);

    assertThat(config.getStaticRoutes(), equalTo(routes));
    assertThat(config.getStaticRoutes().size(), equalTo(2));
  }

  @Test
  public void testAddNatRule() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiNatRule rule = new HuaweiNatRule("RULE1", HuaweiNatRule.NatType.EASY_IP);
    config.addNatRule(rule);

    assertThat(config.getNatRules().size(), equalTo(1));
    assertThat(config.getNatRules().get(0), equalTo(rule));
  }

  @Test
  public void testSetNatRules() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    List<HuaweiNatRule> rules = new java.util.ArrayList<>();
    rules.add(new HuaweiNatRule("RULE1", HuaweiNatRule.NatType.EASY_IP));
    rules.add(new HuaweiNatRule("RULE2", HuaweiNatRule.NatType.DYNAMIC));

    config.setNatRules(rules);

    assertThat(config.getNatRules(), equalTo(rules));
    assertThat(config.getNatRules().size(), equalTo(2));
  }

  @Test
  public void testGetSetBgpProcess() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getBgpProcess(), nullValue());

    HuaweiBgpProcess bgp = new HuaweiBgpProcess(65001L);
    config.setBgpProcess(bgp);

    assertThat(config.getBgpProcess(), equalTo(bgp));
    assertThat(config.getBgpProcess().getAsNum(), equalTo(65001L));

    config.setBgpProcess(null);
    assertThat(config.getBgpProcess(), nullValue());
  }

  @Test
  public void testGetSetOspfProcess() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getOspfProcess(), nullValue());

    HuaweiOspfProcess ospf = new HuaweiOspfProcess(1L);
    config.setOspfProcess(ospf);

    assertThat(config.getOspfProcess(), equalTo(ospf));
    assertThat(config.getOspfProcess().getProcessId(), equalTo(1L));

    config.setOspfProcess(null);
    assertThat(config.getOspfProcess(), nullValue());
  }

  @Test
  public void testAddVrf() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiVrf vrf = new HuaweiVrf("VRF1");
    config.addVrf("VRF1", vrf);

    assertThat(config.getVrfs().size(), equalTo(1));
    assertThat(config.getVrfs(), hasKey("VRF1"));
    assertThat(config.getVrfs().get("VRF1"), equalTo(vrf));
  }

  @Test
  public void testSetVrfs() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    SortedMap<String, HuaweiVrf> vrfs = new java.util.TreeMap<>();
    vrfs.put("VRF1", new HuaweiVrf("VRF1"));
    vrfs.put("VRF2", new HuaweiVrf("VRF2"));

    config.setVrfs(vrfs);

    assertThat(config.getVrfs(), equalTo(vrfs));
    assertThat(config.getVrfs().size(), equalTo(2));
  }

  @Test
  public void testAddAcl() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiAcl acl = new HuaweiAcl("2000", HuaweiAcl.AclType.BASIC);
    config.addAcl("2000", acl);

    assertThat(config.getAcls().size(), equalTo(1));
    assertThat(config.getAcls(), hasKey("2000"));
    assertThat(config.getAcl("2000"), equalTo(acl));
  }

  @Test
  public void testSetAcls() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    SortedMap<String, HuaweiAcl> acls = new java.util.TreeMap<>();
    acls.put("2000", new HuaweiAcl("2000", HuaweiAcl.AclType.BASIC));
    acls.put("3000", new HuaweiAcl("3000", HuaweiAcl.AclType.BASIC));

    config.setAcls(acls);

    assertThat(config.getAcls(), equalTo(acls));
    assertThat(config.getAcls().size(), equalTo(2));
  }

  @Test
  public void testGetAcl() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getAcl("2000"), nullValue());

    HuaweiAcl acl = new HuaweiAcl("2000", HuaweiAcl.AclType.BASIC);
    config.addAcl("2000", acl);

    assertThat(config.getAcl("2000"), equalTo(acl));
    assertThat(config.getAcl("3000"), nullValue());
  }

  @Test
  public void testAddRoutePolicy() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiRoutePolicy policy = new HuaweiRoutePolicy("POLICY1");
    config.addRoutePolicy("POLICY1", policy);

    assertThat(config.getRoutePolicies().size(), equalTo(1));
    assertThat(config.getRoutePolicies(), hasKey("POLICY1"));
    assertThat(config.getRoutePolicy("POLICY1"), equalTo(policy));
  }

  @Test
  public void testSetRoutePolicies() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    SortedMap<String, HuaweiRoutePolicy> policies = new java.util.TreeMap<>();
    policies.put("POLICY1", new HuaweiRoutePolicy("POLICY1"));
    policies.put("POLICY2", new HuaweiRoutePolicy("POLICY2"));

    config.setRoutePolicies(policies);

    assertThat(config.getRoutePolicies(), equalTo(policies));
    assertThat(config.getRoutePolicies().size(), equalTo(2));
  }

  @Test
  public void testGetRoutePolicy() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getRoutePolicy("POLICY1"), nullValue());

    HuaweiRoutePolicy policy = new HuaweiRoutePolicy("POLICY1");
    config.addRoutePolicy("POLICY1", policy);

    assertThat(config.getRoutePolicy("POLICY1"), equalTo(policy));
    assertThat(config.getRoutePolicy("POLICY2"), nullValue());
  }

  @Test
  public void testAddNatAddressGroup() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);
    config.addNatAddressGroup(group);

    assertThat(config.getNatAddressGroups().size(), equalTo(1));
    assertThat(config.getNatAddressGroups(), hasKey(1));
    assertThat(config.getNatAddressGroup(1), equalTo(group));
  }

  @Test
  public void testSetNatAddressGroups() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    SortedMap<Integer, HuaweiNatAddressGroup> groups = new java.util.TreeMap<>();
    groups.put(1, new HuaweiNatAddressGroup(1));
    groups.put(2, new HuaweiNatAddressGroup(2));

    config.setNatAddressGroups(groups);

    assertThat(config.getNatAddressGroups(), equalTo(groups));
    assertThat(config.getNatAddressGroups().size(), equalTo(2));
  }

  @Test
  public void testGetNatAddressGroup() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getNatAddressGroup(1), nullValue());

    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);
    config.addNatAddressGroup(group);

    assertThat(config.getNatAddressGroup(1), equalTo(group));
    assertThat(config.getNatAddressGroup(2), nullValue());
  }

  @Test
  public void testAddCommunityFilter() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiCommunityFilter filter =
        new HuaweiCommunityFilter(1, HuaweiCommunityFilter.Action.PERMIT);
    config.addCommunityFilter(1, filter);

    assertThat(config.getCommunityFilters().size(), equalTo(1));
    assertThat(config.getCommunityFilters(), hasKey(1));
    assertThat(config.getCommunityFilter(1), equalTo(filter));
  }

  @Test
  public void testSetCommunityFilters() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    SortedMap<Integer, HuaweiCommunityFilter> filters = new java.util.TreeMap<>();
    filters.put(1, new HuaweiCommunityFilter(1, HuaweiCommunityFilter.Action.PERMIT));
    filters.put(2, new HuaweiCommunityFilter(2, HuaweiCommunityFilter.Action.DENY));

    config.setCommunityFilters(filters);

    assertThat(config.getCommunityFilters(), equalTo(filters));
    assertThat(config.getCommunityFilters().size(), equalTo(2));
  }

  @Test
  public void testGetCommunityFilter() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    assertThat(config.getCommunityFilter(1), nullValue());

    HuaweiCommunityFilter filter =
        new HuaweiCommunityFilter(1, HuaweiCommunityFilter.Action.PERMIT);
    config.addCommunityFilter(1, filter);

    assertThat(config.getCommunityFilter(1), equalTo(filter));
    assertThat(config.getCommunityFilter(2), nullValue());
  }

  @Test
  public void testSetVendorDoesNothing() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    // Should not throw any exception
    config.setVendor(ConfigurationFormat.CISCO_IOS);
    config.setVendor(ConfigurationFormat.HUAWEI);
    config.setVendor(ConfigurationFormat.JUNIPER);
  }

  @Test
  public void testToVendorIndependentConfigurations() {
    HuaweiConfiguration config = new HuaweiConfiguration();
    config.setHostname("Router1");

    try {
      List<Configuration> vendorConfigs = config.toVendorIndependentConfigurations();

      assertThat(vendorConfigs, hasSize(1));
      Configuration vendorConfig = vendorConfigs.get(0);
      // Hostname is lowercased during conversion
      assertThat(vendorConfig.getHostname(), equalTo("router1"));
    } catch (VendorConversionException e) {
      fail("Should not throw VendorConversionException for basic config");
    }
  }

  @Test
  public void testFullConfiguration() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    // Set basic properties
    config.setHostname("Router1");

    // Add interfaces
    config.addInterface("GigabitEthernet0/0/0", new HuaweiInterface("GigabitEthernet0/0/0"));
    config.addInterface("Loopback0", new HuaweiInterface("Loopback0"));

    // Add VLAN
    config.addVlan(100, new HuaweiVlan(100));

    // Add static route
    HuaweiStaticRoute staticRoute =
        new HuaweiStaticRoute(org.batfish.datamodel.Prefix.parse("10.0.0.0/24"));
    staticRoute.setNextHopIp(org.batfish.datamodel.Ip.parse("192.168.1.1"));
    config.addStaticRoute(staticRoute);

    // Set BGP process
    config.setBgpProcess(new HuaweiBgpProcess(65001L));

    // Set OSPF process
    config.setOspfProcess(new HuaweiOspfProcess(1L));

    // Add VRF
    config.addVrf("VRF1", new HuaweiVrf("VRF1"));

    // Add ACL
    config.addAcl("2000", new HuaweiAcl("2000", HuaweiAcl.AclType.BASIC));

    // Add route policy
    config.addRoutePolicy("POLICY1", new HuaweiRoutePolicy("POLICY1"));

    // Add NAT address group
    config.addNatAddressGroup(new HuaweiNatAddressGroup(1));

    // Verify all settings
    assertThat(config.getHostname(), equalTo("Router1"));
    assertThat(config.getInterfaces().size(), equalTo(2));
    assertThat(config.getVlans().size(), equalTo(1));
    assertThat(config.getStaticRoutes().size(), equalTo(1));
    assertThat(config.getBgpProcess(), notNullValue());
    assertThat(config.getOspfProcess(), notNullValue());
    assertThat(config.getVrfs().size(), equalTo(1));
    assertThat(config.getAcls().size(), equalTo(1));
    assertThat(config.getRoutePolicies().size(), equalTo(1));
    assertThat(config.getNatAddressGroups().size(), equalTo(1));
  }

  @Test
  public void testAddInterfaceDuplicateKeyThrowsException() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    HuaweiInterface iface1 = new HuaweiInterface("GigabitEthernet0/0/0");
    iface1.setDescription("First description");
    config.addInterface("GigabitEthernet0/0/0", iface1);

    assertThat(config.getInterfaces().size(), equalTo(1));
    assertThat(
        config.getInterface("GigabitEthernet0/0/0").getDescription(), equalTo("First description"));

    // Adding the same key again throws IllegalArgumentException
    HuaweiInterface iface2 = new HuaweiInterface("GigabitEthernet0/0/0");
    iface2.setDescription("Second description");
    try {
      config.addInterface("GigabitEthernet0/0/0", iface2);
      fail("Expected IllegalArgumentException for duplicate interface key");
    } catch (IllegalArgumentException e) {
      // Expected - ImmutableMap doesn't allow duplicate keys
      // Just verify the exception is for duplicate entries
      assertTrue(e.getMessage().startsWith("Multiple entries with same key: GigabitEthernet0/0/0"));
    }
  }

  @Test
  public void testCollectionsAreSorted() {
    HuaweiConfiguration config = new HuaweiConfiguration();

    config.addInterface("GigabitEthernet0/0/2", new HuaweiInterface("GigabitEthernet0/0/2"));
    config.addInterface("GigabitEthernet0/0/0", new HuaweiInterface("GigabitEthernet0/0/0"));
    config.addInterface("Loopback0", new HuaweiInterface("Loopback0"));

    // Get keys and verify they're sorted
    Object[] keys = config.getInterfaces().keySet().toArray();
    assertThat(keys[0], equalTo("GigabitEthernet0/0/0"));
    assertThat(keys[1], equalTo("GigabitEthernet0/0/2"));
    assertThat(keys[2], equalTo("Loopback0"));
  }
}

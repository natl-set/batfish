package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

/** Tests for {@link HuaweiVrf}. */
public class HuaweiVrfTest {

  @Test
  public void testConstructor() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");
    assertThat(vrf.getName(), equalTo("VRF1"));
    assertThat(vrf.getImportRouteTargets(), notNullValue());
    assertThat(vrf.getExportRouteTargets(), notNullValue());
    assertThat(vrf.getInterfaces(), notNullValue());
    assertThat(vrf.getImportRouteTargets().size(), equalTo(0));
    assertThat(vrf.getExportRouteTargets().size(), equalTo(0));
    assertThat(vrf.getInterfaces().size(), equalTo(0));
  }

  @Test
  public void testSetName() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");
    assertThat(vrf.getName(), equalTo("VRF1"));

    vrf.setName("VRF2");
    assertThat(vrf.getName(), equalTo("VRF2"));
  }

  @Test
  public void testRouteDistinguisher() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Initially null
    assertThat(vrf.getRouteDistinguisher(), nullValue());

    // Set and get
    vrf.setRouteDistinguisher("65000:100");
    assertThat(vrf.getRouteDistinguisher(), equalTo("65000:100"));

    // Update
    vrf.setRouteDistinguisher("65001:200");
    assertThat(vrf.getRouteDistinguisher(), equalTo("65001:200"));

    // Set to null
    vrf.setRouteDistinguisher(null);
    assertThat(vrf.getRouteDistinguisher(), nullValue());
  }

  @Test
  public void testImportRouteTargets() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Add route targets
    vrf.addImportRouteTarget("100:1");
    vrf.addImportRouteTarget("200:1");
    vrf.addImportRouteTarget("300:1");

    assertThat(vrf.getImportRouteTargets().size(), equalTo(3));
    assertTrue(vrf.getImportRouteTargets().contains("100:1"));
    assertTrue(vrf.getImportRouteTargets().contains("200:1"));
    assertTrue(vrf.getImportRouteTargets().contains("300:1"));
  }

  @Test
  public void testSetImportRouteTargets() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    SortedSet<String> routeTargets = new TreeSet<>();
    routeTargets.add("100:1");
    routeTargets.add("200:1");

    vrf.setImportRouteTargets(routeTargets);

    assertThat(vrf.getImportRouteTargets().size(), equalTo(2));
    assertTrue(vrf.getImportRouteTargets().contains("100:1"));
    assertTrue(vrf.getImportRouteTargets().contains("200:1"));
  }

  @Test
  public void testExportRouteTargets() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Add route targets
    vrf.addExportRouteTarget("100:1");
    vrf.addExportRouteTarget("200:1");
    vrf.addExportRouteTarget("300:1");

    assertThat(vrf.getExportRouteTargets().size(), equalTo(3));
    assertTrue(vrf.getExportRouteTargets().contains("100:1"));
    assertTrue(vrf.getExportRouteTargets().contains("200:1"));
    assertTrue(vrf.getExportRouteTargets().contains("300:1"));
  }

  @Test
  public void testSetExportRouteTargets() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    SortedSet<String> routeTargets = new TreeSet<>();
    routeTargets.add("100:1");
    routeTargets.add("200:1");

    vrf.setExportRouteTargets(routeTargets);

    assertThat(vrf.getExportRouteTargets().size(), equalTo(2));
    assertTrue(vrf.getExportRouteTargets().contains("100:1"));
    assertTrue(vrf.getExportRouteTargets().contains("200:1"));
  }

  @Test
  public void testDescription() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Initially null
    assertThat(vrf.getDescription(), nullValue());

    // Set and get
    vrf.setDescription("Test VRF description");
    assertThat(vrf.getDescription(), equalTo("Test VRF description"));

    // Update
    vrf.setDescription("Updated description");
    assertThat(vrf.getDescription(), equalTo("Updated description"));

    // Set to null
    vrf.setDescription(null);
    assertThat(vrf.getDescription(), nullValue());
  }

  @Test
  public void testInterfaces() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    HuaweiInterface iface1 = new HuaweiInterface("GigabitEthernet0/0/1");
    HuaweiInterface iface2 = new HuaweiInterface("GigabitEthernet0/0/2");

    // Add interfaces
    vrf.addInterface("GigabitEthernet0/0/1", iface1);
    vrf.addInterface("GigabitEthernet0/0/2", iface2);

    assertThat(vrf.getInterfaces().size(), equalTo(2));
    assertThat(vrf.getInterfaces().containsKey("GigabitEthernet0/0/1"), equalTo(true));
    assertThat(vrf.getInterfaces().containsKey("GigabitEthernet0/0/2"), equalTo(true));
    assertThat(vrf.getInterfaces().get("GigabitEthernet0/0/1"), equalTo(iface1));
    assertThat(vrf.getInterfaces().get("GigabitEthernet0/0/2"), equalTo(iface2));
  }

  @Test
  public void testSetInterfaces() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    TreeMap<String, HuaweiInterface> interfaces = new TreeMap<>();
    HuaweiInterface iface1 = new HuaweiInterface("GigabitEthernet0/0/1");
    HuaweiInterface iface2 = new HuaweiInterface("GigabitEthernet0/0/2");
    interfaces.put("GigabitEthernet0/0/1", iface1);
    interfaces.put("GigabitEthernet0/0/2", iface2);

    vrf.setInterfaces(interfaces);

    assertThat(vrf.getInterfaces().size(), equalTo(2));
    assertThat(vrf.getInterfaces().containsKey("GigabitEthernet0/0/1"), equalTo(true));
    assertThat(vrf.getInterfaces().containsKey("GigabitEthernet0/0/2"), equalTo(true));
  }

  @Test
  public void testBgpProcess() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Initially null
    assertThat(vrf.getBgpProcess(), nullValue());

    // Set and get
    HuaweiBgpProcess bgpProcess = new HuaweiBgpProcess(65000);
    vrf.setBgpProcess(bgpProcess);
    assertThat(vrf.getBgpProcess(), equalTo(bgpProcess));

    // Update
    HuaweiBgpProcess newBgpProcess = new HuaweiBgpProcess(65001);
    vrf.setBgpProcess(newBgpProcess);
    assertThat(vrf.getBgpProcess(), equalTo(newBgpProcess));

    // Set to null
    vrf.setBgpProcess(null);
    assertThat(vrf.getBgpProcess(), nullValue());
  }

  @Test
  public void testOspfProcess() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Initially null
    assertThat(vrf.getOspfProcess(), nullValue());

    // Set and get
    HuaweiOspfProcess ospfProcess = new HuaweiOspfProcess(1);
    vrf.setOspfProcess(ospfProcess);
    assertThat(vrf.getOspfProcess(), equalTo(ospfProcess));

    // Update
    HuaweiOspfProcess newOspfProcess = new HuaweiOspfProcess(2);
    vrf.setOspfProcess(newOspfProcess);
    assertThat(vrf.getOspfProcess(), equalTo(newOspfProcess));

    // Set to null
    vrf.setOspfProcess(null);
    assertThat(vrf.getOspfProcess(), nullValue());
  }

  @Test
  public void testAddressFamily() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Initially disabled
    assertThat(vrf.isIpv4Enabled(), equalTo(false));
    assertThat(vrf.isIpv6Enabled(), equalTo(false));

    // Enable IPv4
    vrf.setIpv4Enabled(true);
    assertThat(vrf.isIpv4Enabled(), equalTo(true));
    assertThat(vrf.isIpv6Enabled(), equalTo(false));

    // Enable IPv6
    vrf.setIpv6Enabled(true);
    assertThat(vrf.isIpv4Enabled(), equalTo(true));
    assertThat(vrf.isIpv6Enabled(), equalTo(true));

    // Disable IPv4
    vrf.setIpv4Enabled(false);
    assertThat(vrf.isIpv4Enabled(), equalTo(false));
    assertThat(vrf.isIpv6Enabled(), equalTo(true));
  }

  @Test
  public void testSerialization() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");
    vrf.setRouteDistinguisher("65000:100");
    vrf.setDescription("Test VRF");
    vrf.setIpv4Enabled(true);

    HuaweiBgpProcess bgpProcess = new HuaweiBgpProcess(65000);
    vrf.setBgpProcess(bgpProcess);

    HuaweiOspfProcess ospfProcess = new HuaweiOspfProcess(1);
    vrf.setOspfProcess(ospfProcess);

    vrf.addImportRouteTarget("100:1");
    vrf.addExportRouteTarget("200:1");

    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/1");
    vrf.addInterface("GigabitEthernet0/0/1", iface);

    HuaweiVrf clone = SerializationUtils.clone(vrf);

    assertThat(clone.getName(), equalTo(vrf.getName()));
    assertThat(clone.getRouteDistinguisher(), equalTo(vrf.getRouteDistinguisher()));
    assertThat(clone.getDescription(), equalTo(vrf.getDescription()));
    assertThat(clone.isIpv4Enabled(), equalTo(vrf.isIpv4Enabled()));
    assertThat(clone.getImportRouteTargets().size(), equalTo(vrf.getImportRouteTargets().size()));
    assertThat(clone.getExportRouteTargets().size(), equalTo(vrf.getExportRouteTargets().size()));
    assertThat(clone.getInterfaces().size(), equalTo(vrf.getInterfaces().size()));
    assertThat(clone.getBgpProcess(), notNullValue());
    assertThat(clone.getOspfProcess(), notNullValue());
  }

  @Test
  public void testComplexVrfConfiguration() {
    HuaweiVrf vrf = new HuaweiVrf("CUSTOMER_A");

    // Configure all properties
    vrf.setRouteDistinguisher("65000:100");
    vrf.setDescription("Customer A VRF for VPN services");
    vrf.setIpv4Enabled(true);

    // Add multiple route targets
    vrf.addImportRouteTarget("65000:100");
    vrf.addImportRouteTarget("65000:200");
    vrf.addImportRouteTarget("65000:300");
    vrf.addExportRouteTarget("65000:100");
    vrf.addExportRouteTarget("65000:400");

    // Add interfaces
    HuaweiInterface iface1 = new HuaweiInterface("GigabitEthernet0/0/1");
    HuaweiInterface iface2 = new HuaweiInterface("GigabitEthernet0/0/2");
    vrf.addInterface("GigabitEthernet0/0/1", iface1);
    vrf.addInterface("GigabitEthernet0/0/2", iface2);

    // Set BGP process
    HuaweiBgpProcess bgpProcess = new HuaweiBgpProcess(65000);
    vrf.setBgpProcess(bgpProcess);

    // Set OSPF process
    HuaweiOspfProcess ospfProcess = new HuaweiOspfProcess(1);
    vrf.setOspfProcess(ospfProcess);

    // Verify all settings
    assertThat(vrf.getName(), equalTo("CUSTOMER_A"));
    assertThat(vrf.getRouteDistinguisher(), equalTo("65000:100"));
    assertThat(vrf.getDescription(), equalTo("Customer A VRF for VPN services"));
    assertThat(vrf.isIpv4Enabled(), equalTo(true));

    assertThat(vrf.getImportRouteTargets().size(), equalTo(3));
    assertTrue(vrf.getImportRouteTargets().contains("65000:100"));
    assertTrue(vrf.getImportRouteTargets().contains("65000:200"));
    assertTrue(vrf.getImportRouteTargets().contains("65000:300"));

    assertThat(vrf.getExportRouteTargets().size(), equalTo(2));
    assertTrue(vrf.getExportRouteTargets().contains("65000:100"));
    assertTrue(vrf.getExportRouteTargets().contains("65000:400"));

    assertThat(vrf.getInterfaces().size(), equalTo(2));
    assertTrue(vrf.getInterfaces().containsKey("GigabitEthernet0/0/1"));
    assertTrue(vrf.getInterfaces().containsKey("GigabitEthernet0/0/2"));

    assertThat(vrf.getBgpProcess(), notNullValue());
    assertThat(vrf.getOspfProcess(), notNullValue());
  }

  @Test
  public void testVrfWithOnlyName() {
    // Minimal VRF configuration
    HuaweiVrf vrf = new HuaweiVrf("MINIMAL");

    assertThat(vrf.getName(), equalTo("MINIMAL"));
    assertThat(vrf.getRouteDistinguisher(), nullValue());
    assertThat(vrf.getDescription(), nullValue());
    assertThat(vrf.isIpv4Enabled(), equalTo(false));
    assertThat(vrf.isIpv6Enabled(), equalTo(false));
    assertThat(vrf.getImportRouteTargets().size(), equalTo(0));
    assertThat(vrf.getExportRouteTargets().size(), equalTo(0));
    assertThat(vrf.getInterfaces().size(), equalTo(0));
    assertThat(vrf.getBgpProcess(), nullValue());
    assertThat(vrf.getOspfProcess(), nullValue());
  }

  @Test
  public void testReplaceRouteTargetSets() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Add initial route targets
    vrf.addImportRouteTarget("100:1");
    vrf.addExportRouteTarget("200:1");

    assertThat(vrf.getImportRouteTargets().size(), equalTo(1));
    assertThat(vrf.getExportRouteTargets().size(), equalTo(1));

    // Replace with new sets
    SortedSet<String> newImportTargets = new TreeSet<>();
    newImportTargets.add("300:1");
    newImportTargets.add("400:1");
    vrf.setImportRouteTargets(newImportTargets);

    SortedSet<String> newExportTargets = new TreeSet<>();
    newExportTargets.add("500:1");
    vrf.setExportRouteTargets(newExportTargets);

    assertThat(vrf.getImportRouteTargets().size(), equalTo(2));
    assertThat(vrf.getExportRouteTargets().size(), equalTo(1));
    assertTrue(vrf.getImportRouteTargets().contains("300:1"));
    assertTrue(vrf.getImportRouteTargets().contains("400:1"));
    assertTrue(vrf.getExportRouteTargets().contains("500:1"));
  }

  @Test
  public void testReplaceInterfaceMap() {
    HuaweiVrf vrf = new HuaweiVrf("VRF1");

    // Add initial interface
    HuaweiInterface iface1 = new HuaweiInterface("GigabitEthernet0/0/1");
    vrf.addInterface("GigabitEthernet0/0/1", iface1);

    assertThat(vrf.getInterfaces().size(), equalTo(1));

    // Replace with new map
    TreeMap<String, HuaweiInterface> newInterfaces = new TreeMap<>();
    HuaweiInterface iface2 = new HuaweiInterface("GigabitEthernet0/0/2");
    HuaweiInterface iface3 = new HuaweiInterface("GigabitEthernet0/0/3");
    newInterfaces.put("GigabitEthernet0/0/2", iface2);
    newInterfaces.put("GigabitEthernet0/0/3", iface3);
    vrf.setInterfaces(newInterfaces);

    assertThat(vrf.getInterfaces().size(), equalTo(2));
    assertTrue(vrf.getInterfaces().containsKey("GigabitEthernet0/0/2"));
    assertTrue(vrf.getInterfaces().containsKey("GigabitEthernet0/0/3"));
  }
}

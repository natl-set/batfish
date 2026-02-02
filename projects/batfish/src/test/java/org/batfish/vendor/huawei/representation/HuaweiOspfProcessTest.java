package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link HuaweiOspfProcess}. */
public class HuaweiOspfProcessTest {

  @Test
  public void testConstructor() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getProcessId(), equalTo(1L));
    assertThat(process.getRouterId(), nullValue());
    assertThat(process.getNetworks(), equalTo(new ArrayList<>()));
    assertThat(process.getAreas(), equalTo(new TreeMap<>()));
    assertThat(process.getInterfaces(), equalTo(new TreeMap<>()));
    assertThat(process.getDefaultOriginate(), equalTo(false));
    assertThat(process.getDefaultOriginateRouteMap(), nullValue());
  }

  @Test
  public void testSetProcessId() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getProcessId(), equalTo(1L));

    process.setProcessId(100L);
    assertThat(process.getProcessId(), equalTo(100L));
  }

  @Test
  public void testSetRouterId() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getRouterId(), nullValue());

    Ip routerId = Ip.parse("1.1.1.1");
    process.setRouterId(routerId);
    assertThat(process.getRouterId(), equalTo(routerId));

    process.setRouterId(null);
    assertThat(process.getRouterId(), nullValue());
  }

  @Test
  public void testSetNetworks() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getNetworks().size(), equalTo(0));

    List<HuaweiOspfProcess.HuaweiOspfNetwork> networks = new ArrayList<>();
    networks.add(new HuaweiOspfProcess.HuaweiOspfNetwork(Prefix.parse("10.0.0.0/8"), 0L));
    networks.add(new HuaweiOspfProcess.HuaweiOspfNetwork(Prefix.parse("192.168.1.0/24"), 1L));

    process.setNetworks(networks);
    assertThat(process.getNetworks(), equalTo(networks));
    assertThat(process.getNetworks().size(), equalTo(2));
  }

  @Test
  public void testAddNetwork() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getNetworks().size(), equalTo(0));

    HuaweiOspfProcess.HuaweiOspfNetwork network1 =
        new HuaweiOspfProcess.HuaweiOspfNetwork(Prefix.parse("10.0.0.0/8"), 0L);
    process.addNetwork(network1);
    assertThat(process.getNetworks().size(), equalTo(1));
    assertThat(process.getNetworks().get(0), equalTo(network1));

    HuaweiOspfProcess.HuaweiOspfNetwork network2 =
        new HuaweiOspfProcess.HuaweiOspfNetwork(Prefix.parse("192.168.1.0/24"), 1L);
    process.addNetwork(network2);
    assertThat(process.getNetworks().size(), equalTo(2));
    assertThat(process.getNetworks().get(1), equalTo(network2));
  }

  @Test
  public void testSetAreas() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getAreas().size(), equalTo(0));

    Map<Long, HuaweiOspfProcess.HuaweiOspfArea> areas = new TreeMap<>();
    areas.put(0L, new HuaweiOspfProcess.HuaweiOspfArea(0L));
    areas.put(1L, new HuaweiOspfProcess.HuaweiOspfArea(1L));

    process.setAreas(areas);
    assertThat(process.getAreas(), equalTo(areas));
    assertThat(process.getAreas().size(), equalTo(2));
  }

  @Test
  public void testGetOrCreateArea() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);

    // Creating a new area
    HuaweiOspfProcess.HuaweiOspfArea area1 = process.getOrCreateArea(0L);
    assertThat(area1.getAreaId(), equalTo(0L));
    assertThat(process.getAreas().size(), equalTo(1));
    assertThat(process.getAreas(), hasKey(0L));

    // Getting existing area
    HuaweiOspfProcess.HuaweiOspfArea area2 = process.getOrCreateArea(0L);
    assertThat(area2, equalTo(area1));
    assertThat(process.getAreas().size(), equalTo(1));

    // Creating another area
    HuaweiOspfProcess.HuaweiOspfArea area3 = process.getOrCreateArea(1L);
    assertThat(area3.getAreaId(), equalTo(1L));
    assertThat(process.getAreas().size(), equalTo(2));
    assertThat(process.getAreas(), hasKey(1L));
  }

  @Test
  public void testAddArea() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getAreas().size(), equalTo(0));

    HuaweiOspfProcess.HuaweiOspfArea area1 = new HuaweiOspfProcess.HuaweiOspfArea(0L);
    process.addArea(0L, area1);
    assertThat(process.getAreas().size(), equalTo(1));
    assertThat(process.getAreas().get(0L), equalTo(area1));

    HuaweiOspfProcess.HuaweiOspfArea area2 = new HuaweiOspfProcess.HuaweiOspfArea(1L);
    process.addArea(1L, area2);
    assertThat(process.getAreas().size(), equalTo(2));
    assertThat(process.getAreas().get(1L), equalTo(area2));
  }

  @Test
  public void testSetInterfaces() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getInterfaces().size(), equalTo(0));

    Map<String, HuaweiOspfProcess.HuaweiOspfInterfaceSettings> interfaces = new TreeMap<>();
    interfaces.put("GigabitEthernet0/0/0", new HuaweiOspfProcess.HuaweiOspfInterfaceSettings());
    interfaces.put("GigabitEthernet0/0/1", new HuaweiOspfProcess.HuaweiOspfInterfaceSettings());

    process.setInterfaces(interfaces);
    assertThat(process.getInterfaces(), equalTo(interfaces));
    assertThat(process.getInterfaces().size(), equalTo(2));
  }

  @Test
  public void testAddInterface() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getInterfaces().size(), equalTo(0));

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings1 =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();
    process.addInterface("GigabitEthernet0/0/0", settings1);
    assertThat(process.getInterfaces().size(), equalTo(1));
    assertThat(process.getInterfaces().get("GigabitEthernet0/0/0"), equalTo(settings1));

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings2 =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();
    process.addInterface("GigabitEthernet0/0/1", settings2);
    assertThat(process.getInterfaces().size(), equalTo(2));
    assertThat(process.getInterfaces().get("GigabitEthernet0/0/1"), equalTo(settings2));
  }

  @Test
  public void testSetDefaultOriginate() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getDefaultOriginate(), equalTo(false));

    process.setDefaultOriginate(true);
    assertThat(process.getDefaultOriginate(), equalTo(true));

    process.setDefaultOriginate(false);
    assertThat(process.getDefaultOriginate(), equalTo(false));
  }

  @Test
  public void testSetDefaultOriginateRouteMap() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getDefaultOriginateRouteMap(), nullValue());

    process.setDefaultOriginateRouteMap("route-map-1");
    assertThat(process.getDefaultOriginateRouteMap(), equalTo("route-map-1"));

    process.setDefaultOriginateRouteMap(null);
    assertThat(process.getDefaultOriginateRouteMap(), nullValue());
  }

  @Test
  public void testHuaweiOspfNetwork_Constructor() {
    HuaweiOspfProcess.HuaweiOspfNetwork network =
        new HuaweiOspfProcess.HuaweiOspfNetwork(Prefix.parse("10.0.0.0/8"), 0L);
    assertThat(network.getNetwork(), equalTo(Prefix.parse("10.0.0.0/8")));
    assertThat(network.getAreaId(), equalTo(0L));
  }

  @Test
  public void testHuaweiOspfNetwork_Setters() {
    HuaweiOspfProcess.HuaweiOspfNetwork network =
        new HuaweiOspfProcess.HuaweiOspfNetwork(Prefix.parse("10.0.0.0/8"), 0L);

    Prefix newPrefix = Prefix.parse("192.168.1.0/24");
    network.setNetwork(newPrefix);
    assertThat(network.getNetwork(), equalTo(newPrefix));

    network.setAreaId(1L);
    assertThat(network.getAreaId(), equalTo(1L));
  }

  @Test
  public void testHuaweiOspfArea_Constructor() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(0L);
    assertThat(area.getAreaId(), equalTo(0L));
    assertThat(area.getAreaIdStr(), nullValue());
    assertThat(area.getAreaType(), equalTo(HuaweiOspfProcess.OspfAreaType.NORMAL));
  }

  @Test
  public void testHuaweiOspfArea_Setters() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(0L);

    area.setAreaId(1L);
    assertThat(area.getAreaId(), equalTo(1L));

    area.setAreaIdStr("0.0.0.0");
    assertThat(area.getAreaIdStr(), equalTo("0.0.0.0"));

    area.setAreaIdStr(null);
    assertThat(area.getAreaIdStr(), nullValue());
  }

  @Test
  public void testHuaweiOspfArea_AreaType() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(1L);

    // Default is NORMAL
    assertThat(area.getAreaType(), equalTo(HuaweiOspfProcess.OspfAreaType.NORMAL));

    // Set to STUB
    area.setAreaType(HuaweiOspfProcess.OspfAreaType.STUB);
    assertThat(area.getAreaType(), equalTo(HuaweiOspfProcess.OspfAreaType.STUB));

    // Set to NSSA
    area.setAreaType(HuaweiOspfProcess.OspfAreaType.NSSA);
    assertThat(area.getAreaType(), equalTo(HuaweiOspfProcess.OspfAreaType.NSSA));
  }

  @Test
  public void testHuaweiOspfArea_NoSummary() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(1L);

    assertThat(area.isNoSummary(), equalTo(false));

    area.setNoSummary(true);
    assertThat(area.isNoSummary(), equalTo(true));

    area.setNoSummary(false);
    assertThat(area.isNoSummary(), equalTo(false));
  }

  @Test
  public void testHuaweiOspfArea_NoRedistribute() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(1L);

    assertThat(area.isNoRedistribute(), equalTo(false));

    area.setNoRedistribute(true);
    assertThat(area.isNoRedistribute(), equalTo(true));
  }

  @Test
  public void testHuaweiOspfArea_DefaultOriginate() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(1L);

    assertThat(area.isDefaultOriginate(), equalTo(false));

    area.setDefaultOriginate(true);
    assertThat(area.isDefaultOriginate(), equalTo(true));
  }

  @Test
  public void testHuaweiOspfArea_Authentication() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(1L);

    assertThat(area.getAuthType(), nullValue());
    assertThat(area.getAuthKey(), nullValue());

    area.setAuthType("MD5");
    area.setAuthKey("secret123");
    assertThat(area.getAuthType(), equalTo("MD5"));
    assertThat(area.getAuthKey(), equalTo("secret123"));

    area.setAuthType("SIMPLE");
    assertThat(area.getAuthType(), equalTo("SIMPLE"));
  }

  @Test
  public void testHuaweiOspfInterfaceSettings_Constructor() {
    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();
    assertThat(settings.getAreaId(), nullValue());
    assertThat(settings.getCost(), nullValue());
    assertThat(settings.getHelloInterval(), nullValue());
    assertThat(settings.getDeadInterval(), nullValue());
    assertThat(settings.getRetransmitInterval(), nullValue());
  }

  @Test
  public void testHuaweiOspfInterfaceSettings_Setters() {
    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();

    settings.setAreaId(1L);
    assertThat(settings.getAreaId(), equalTo(1L));

    settings.setCost(100);
    assertThat(settings.getCost(), equalTo(100));

    settings.setHelloInterval(10);
    assertThat(settings.getHelloInterval(), equalTo(10));

    settings.setDeadInterval(40);
    assertThat(settings.getDeadInterval(), equalTo(40));

    settings.setRetransmitInterval(5);
    assertThat(settings.getRetransmitInterval(), equalTo(5));
  }

  @Test
  public void testHuaweiOspfInterfaceSettings_SetNulls() {
    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();

    settings.setAreaId(1L);
    settings.setCost(100);
    settings.setHelloInterval(10);
    settings.setDeadInterval(40);
    settings.setRetransmitInterval(5);

    assertThat(settings.getAreaId(), equalTo(1L));
    assertThat(settings.getCost(), equalTo(100));
    assertThat(settings.getHelloInterval(), equalTo(10));
    assertThat(settings.getDeadInterval(), equalTo(40));
    assertThat(settings.getRetransmitInterval(), equalTo(5));

    settings.setAreaId(null);
    settings.setCost(null);
    settings.setHelloInterval(null);
    settings.setDeadInterval(null);
    settings.setRetransmitInterval(null);

    assertThat(settings.getAreaId(), nullValue());
    assertThat(settings.getCost(), nullValue());
    assertThat(settings.getHelloInterval(), nullValue());
    assertThat(settings.getDeadInterval(), nullValue());
    assertThat(settings.getRetransmitInterval(), nullValue());
  }

  @Test
  public void testMultipleProcesses() {
    HuaweiOspfProcess process1 = new HuaweiOspfProcess(1L);
    HuaweiOspfProcess process2 = new HuaweiOspfProcess(2L);

    assertThat(process1.getProcessId(), equalTo(1L));
    assertThat(process2.getProcessId(), equalTo(2L));

    process1.setRouterId(Ip.parse("1.1.1.1"));
    process2.setRouterId(Ip.parse("2.2.2.2"));

    assertThat(process1.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(process2.getRouterId(), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testFullConfiguration() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(100L);

    // Set router ID
    process.setRouterId(Ip.parse("1.1.1.1"));

    // Add networks
    process.addNetwork(new HuaweiOspfProcess.HuaweiOspfNetwork(Prefix.parse("10.0.0.0/8"), 0L));
    process.addNetwork(new HuaweiOspfProcess.HuaweiOspfNetwork(Prefix.parse("192.168.1.0/24"), 1L));

    // Add areas
    HuaweiOspfProcess.HuaweiOspfArea area0 = new HuaweiOspfProcess.HuaweiOspfArea(0L);
    area0.setAreaIdStr("0.0.0.0");
    process.addArea(0L, area0);

    HuaweiOspfProcess.HuaweiOspfArea area1 = new HuaweiOspfProcess.HuaweiOspfArea(1L);
    area1.setAreaIdStr("0.0.0.1");
    process.addArea(1L, area1);

    // Add interfaces
    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings1 =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();
    settings1.setAreaId(0L);
    settings1.setCost(10);
    settings1.setHelloInterval(10);
    settings1.setDeadInterval(40);
    settings1.setRetransmitInterval(5);
    process.addInterface("GigabitEthernet0/0/0", settings1);

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings2 =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();
    settings2.setAreaId(1L);
    settings2.setCost(100);
    process.addInterface("GigabitEthernet0/0/1", settings2);

    // Enable default originate
    process.setDefaultOriginate(true);
    process.setDefaultOriginateRouteMap("RMAP1");

    // Verify all settings
    assertThat(process.getProcessId(), equalTo(100L));
    assertThat(process.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(process.getNetworks().size(), equalTo(2));
    assertThat(process.getAreas().size(), equalTo(2));
    assertThat(process.getInterfaces().size(), equalTo(2));
    assertThat(process.getDefaultOriginate(), equalTo(true));
    assertThat(process.getDefaultOriginateRouteMap(), equalTo("RMAP1"));

    // Verify specific settings
    assertThat(process.getAreas().get(0L).getAreaIdStr(), equalTo("0.0.0.0"));
    assertThat(process.getAreas().get(1L).getAreaIdStr(), equalTo("0.0.0.1"));

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings iface1 =
        process.getInterfaces().get("GigabitEthernet0/0/0");
    assertThat(iface1.getAreaId(), equalTo(0L));
    assertThat(iface1.getCost(), equalTo(10));
    assertThat(iface1.getHelloInterval(), equalTo(10));
    assertThat(iface1.getDeadInterval(), equalTo(40));
    assertThat(iface1.getRetransmitInterval(), equalTo(5));

    HuaweiOspfProcess.HuaweiOspfInterfaceSettings iface2 =
        process.getInterfaces().get("GigabitEthernet0/0/1");
    assertThat(iface2.getAreaId(), equalTo(1L));
    assertThat(iface2.getCost(), equalTo(100));
  }

  @Test
  public void testGetOrCreateArea_AreaAlreadyExists() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);

    HuaweiOspfProcess.HuaweiOspfArea area1 = new HuaweiOspfProcess.HuaweiOspfArea(0L);
    area1.setAreaIdStr("0.0.0.0");
    process.addArea(0L, area1);

    // getOrCreateArea should return the existing area
    HuaweiOspfProcess.HuaweiOspfArea area2 = process.getOrCreateArea(0L);
    assertThat(area2, equalTo(area1));
    assertThat(area2.getAreaIdStr(), equalTo("0.0.0.0"));
    assertThat(process.getAreas().size(), equalTo(1));
  }

  @Test
  public void testHuaweiOspfVirtualLink_Constructor() {
    HuaweiOspfProcess.HuaweiOspfVirtualLink vlink =
        new HuaweiOspfProcess.HuaweiOspfVirtualLink(Ip.parse("1.1.1.1"));
    assertThat(vlink.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(vlink.getHelloInterval(), nullValue());
    assertThat(vlink.getDeadInterval(), nullValue());
  }

  @Test
  public void testHuaweiOspfVirtualLink_Setters() {
    HuaweiOspfProcess.HuaweiOspfVirtualLink vlink =
        new HuaweiOspfProcess.HuaweiOspfVirtualLink(Ip.parse("1.1.1.1"));

    vlink.setHelloInterval(10);
    assertThat(vlink.getHelloInterval(), equalTo(10));

    vlink.setDeadInterval(40);
    assertThat(vlink.getDeadInterval(), equalTo(40));

    vlink.setRouterId(Ip.parse("2.2.2.2"));
    assertThat(vlink.getRouterId(), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testVirtualLinks() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getVirtualLinks().size(), equalTo(0));

    HuaweiOspfProcess.HuaweiOspfVirtualLink vlink1 =
        new HuaweiOspfProcess.HuaweiOspfVirtualLink(Ip.parse("1.1.1.1"));
    vlink1.setHelloInterval(10);
    vlink1.setDeadInterval(40);
    process.addVirtualLink(vlink1);

    assertThat(process.getVirtualLinks().size(), equalTo(1));
    assertThat(process.getVirtualLinks().get(0).getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(process.getVirtualLinks().get(0).getHelloInterval(), equalTo(10));
    assertThat(process.getVirtualLinks().get(0).getDeadInterval(), equalTo(40));

    HuaweiOspfProcess.HuaweiOspfVirtualLink vlink2 =
        new HuaweiOspfProcess.HuaweiOspfVirtualLink(Ip.parse("2.2.2.2"));
    process.addVirtualLink(vlink2);

    assertThat(process.getVirtualLinks().size(), equalTo(2));
  }

  @Test
  public void testHuaweiOspfInterfaceSettings_NetworkType() {
    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();

    assertThat(settings.getNetworkType(), nullValue());

    settings.setNetworkType("BROADCAST");
    assertThat(settings.getNetworkType(), equalTo("BROADCAST"));

    settings.setNetworkType("P2P");
    assertThat(settings.getNetworkType(), equalTo("P2P"));

    settings.setNetworkType("NBMA");
    assertThat(settings.getNetworkType(), equalTo("NBMA"));
  }

  @Test
  public void testHuaweiOspfInterfaceSettings_Authentication() {
    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();

    assertThat(settings.getAuthType(), nullValue());
    assertThat(settings.getAuthKey(), nullValue());

    settings.setAuthType("MD5");
    settings.setAuthKey("secret123");
    assertThat(settings.getAuthType(), equalTo("MD5"));
    assertThat(settings.getAuthKey(), equalTo("secret123"));

    settings.setAuthType("SIMPLE");
    assertThat(settings.getAuthType(), equalTo("SIMPLE"));
  }

  @Test
  public void testHuaweiOspfInterfaceSettings_Passive() {
    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
        new HuaweiOspfProcess.HuaweiOspfInterfaceSettings();

    assertThat(settings.getPassive(), nullValue());

    settings.setPassive(true);
    assertThat(settings.getPassive(), equalTo(true));

    settings.setPassive(false);
    assertThat(settings.getPassive(), equalTo(false));
  }

  @Test
  public void testHuaweiOspfAreaRange_Constructor() {
    Prefix prefix = Prefix.parse("10.0.0.0/8");
    HuaweiOspfProcess.HuaweiOspfAreaRange range =
        new HuaweiOspfProcess.HuaweiOspfAreaRange(prefix, true, 100L);

    assertThat(range.getPrefix(), equalTo(prefix));
    assertThat(range.isAdvertise(), equalTo(true));
    assertThat(range.getCost(), equalTo(100L));
  }

  @Test
  public void testHuaweiOspfAreaRange_Setters() {
    Prefix prefix = Prefix.parse("192.168.0.0/16");
    HuaweiOspfProcess.HuaweiOspfAreaRange range =
        new HuaweiOspfProcess.HuaweiOspfAreaRange(prefix, false, null);

    assertThat(range.getPrefix(), equalTo(prefix));
    assertThat(range.isAdvertise(), equalTo(false));
    assertThat(range.getCost(), nullValue());

    Prefix newPrefix = Prefix.parse("10.0.0.0/8");
    range.setPrefix(newPrefix);
    assertThat(range.getPrefix(), equalTo(newPrefix));

    range.setAdvertise(true);
    assertThat(range.isAdvertise(), equalTo(true));

    range.setCost(200L);
    assertThat(range.getCost(), equalTo(200L));
  }

  @Test
  public void testHuaweiOspfArea_GetAreaRanges() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(1L);

    assertThat(area.getAreaRanges().size(), equalTo(0));

    Prefix prefix1 = Prefix.parse("10.0.0.0/8");
    HuaweiOspfProcess.HuaweiOspfAreaRange range1 =
        new HuaweiOspfProcess.HuaweiOspfAreaRange(prefix1, true, 100L);
    area.addAreaRange(prefix1, range1);

    assertThat(area.getAreaRanges().size(), equalTo(1));
    assertThat(area.getAreaRanges().get(prefix1), equalTo(range1));

    Prefix prefix2 = Prefix.parse("192.168.0.0/16");
    HuaweiOspfProcess.HuaweiOspfAreaRange range2 =
        new HuaweiOspfProcess.HuaweiOspfAreaRange(prefix2, false, null);
    area.addAreaRange(prefix2, range2);

    assertThat(area.getAreaRanges().size(), equalTo(2));
    assertThat(area.getAreaRanges().get(prefix2), equalTo(range2));
  }

  @Test
  public void testHuaweiOspfArea_SetAreaRanges() {
    HuaweiOspfProcess.HuaweiOspfArea area = new HuaweiOspfProcess.HuaweiOspfArea(1L);

    Map<Prefix, HuaweiOspfProcess.HuaweiOspfAreaRange> ranges = new TreeMap<>();
    ranges.put(
        Prefix.parse("10.0.0.0/8"),
        new HuaweiOspfProcess.HuaweiOspfAreaRange(Prefix.parse("10.0.0.0/8"), true, 100L));
    ranges.put(
        Prefix.parse("192.168.0.0/16"),
        new HuaweiOspfProcess.HuaweiOspfAreaRange(Prefix.parse("192.168.0.0/16"), false, null));

    area.setAreaRanges(ranges);
    assertThat(area.getAreaRanges(), equalTo(ranges));
    assertThat(area.getAreaRanges().size(), equalTo(2));
  }

  @Test
  public void testRedistributionPolicy_Constructor() {
    HuaweiOspfProcess.HuaweiOspfRedistributionPolicy policy =
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC);

    assertThat(
        policy.getSourceProtocol(), equalTo(HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC));
    assertThat(policy.getRoutePolicy(), nullValue());
    assertThat(policy.getCost(), nullValue());
    assertThat(policy.getTag(), nullValue());
  }

  @Test
  public void testRedistributionPolicy_Setters() {
    HuaweiOspfProcess.HuaweiOspfRedistributionPolicy policy =
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.BGP);

    policy.setRoutePolicy("POLICY1");
    assertThat(policy.getRoutePolicy(), equalTo("POLICY1"));

    policy.setCost(100L);
    assertThat(policy.getCost(), equalTo(100L));

    policy.setTag(200L);
    assertThat(policy.getTag(), equalTo(200L));
  }

  @Test
  public void testAddRedistributionPolicy() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);
    assertThat(process.getRedistributionPolicies().size(), equalTo(0));

    HuaweiOspfProcess.HuaweiOspfRedistributionPolicy policy1 =
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC);
    process.addRedistributionPolicy(HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC, policy1);

    assertThat(process.getRedistributionPolicies().size(), equalTo(1));
    assertThat(
        process
            .getRedistributionPolicies()
            .get(HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC),
        equalTo(policy1));

    HuaweiOspfProcess.HuaweiOspfRedistributionPolicy policy2 =
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.DIRECT);
    process.addRedistributionPolicy(HuaweiOspfProcess.HuaweiRedistributionProtocol.DIRECT, policy2);

    assertThat(process.getRedistributionPolicies().size(), equalTo(2));
  }

  @Test
  public void testRedistributionPolicy_AllProtocols() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);

    process.addRedistributionPolicy(
        HuaweiOspfProcess.HuaweiRedistributionProtocol.DIRECT,
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.DIRECT));
    process.addRedistributionPolicy(
        HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC,
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC));
    process.addRedistributionPolicy(
        HuaweiOspfProcess.HuaweiRedistributionProtocol.BGP,
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.BGP));
    process.addRedistributionPolicy(
        HuaweiOspfProcess.HuaweiRedistributionProtocol.RIP,
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.RIP));
    process.addRedistributionPolicy(
        HuaweiOspfProcess.HuaweiRedistributionProtocol.ISIS,
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.ISIS));
    process.addRedistributionPolicy(
        HuaweiOspfProcess.HuaweiRedistributionProtocol.OSPF,
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.OSPF));
    process.addRedistributionPolicy(
        HuaweiOspfProcess.HuaweiRedistributionProtocol.UNR,
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.UNR));

    assertThat(process.getRedistributionPolicies().size(), equalTo(7));
  }

  @Test
  public void testRedistributionPolicy_WithCostAndTagAndPolicy() {
    HuaweiOspfProcess process = new HuaweiOspfProcess(1L);

    HuaweiOspfProcess.HuaweiOspfRedistributionPolicy policy =
        new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(
            HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC);
    policy.setCost(100L);
    policy.setTag(42L);
    policy.setRoutePolicy("FILTER_POLICY");

    process.addRedistributionPolicy(HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC, policy);

    HuaweiOspfProcess.HuaweiOspfRedistributionPolicy retrieved =
        process
            .getRedistributionPolicies()
            .get(HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC);

    assertThat(
        retrieved.getSourceProtocol(),
        equalTo(HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC));
    assertThat(retrieved.getCost(), equalTo(100L));
    assertThat(retrieved.getTag(), equalTo(42L));
    assertThat(retrieved.getRoutePolicy(), equalTo("FILTER_POLICY"));
  }
}

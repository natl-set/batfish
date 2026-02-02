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
}

package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;
import java.util.TreeMap;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.junit.Test;

/** Tests for {@link HuaweiBgpProcess}. */
public class HuaweiBgpProcessTest {

  @Test
  public void testConstructor() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getAsNum(), equalTo(65001L));
    assertThat(process.getRouterId(), nullValue());
    assertThat(process.getNeighbors(), equalTo(new TreeMap<>()));
    assertThat(process.getPeerGroups(), equalTo(new TreeMap<>()));
    assertThat(process.getAddressFamilies(), equalTo(new TreeMap<>()));
  }

  @Test
  public void testConstructorWithDifferentAsNum() {
    HuaweiBgpProcess process1 = new HuaweiBgpProcess(1L);
    assertThat(process1.getAsNum(), equalTo(1L));

    HuaweiBgpProcess process2 = new HuaweiBgpProcess(65000L);
    assertThat(process2.getAsNum(), equalTo(65000L));

    HuaweiBgpProcess process3 = new HuaweiBgpProcess(4294967295L); // Max 32-bit AS number
    assertThat(process3.getAsNum(), equalTo(4294967295L));
  }

  @Test
  public void testSetAsNum() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getAsNum(), equalTo(65001L));

    process.setAsNum(65002L);
    assertThat(process.getAsNum(), equalTo(65002L));

    process.setAsNum(1L);
    assertThat(process.getAsNum(), equalTo(1L));
  }

  @Test
  public void testGetSetRouterId() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getRouterId(), nullValue());

    Ip routerId = Ip.parse("1.1.1.1");
    process.setRouterId(routerId);
    assertThat(process.getRouterId(), equalTo(routerId));

    process.setRouterId(null);
    assertThat(process.getRouterId(), nullValue());
  }

  @Test
  public void testSetRouterIdDifferentValues() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    process.setRouterId(Ip.parse("10.0.0.1"));
    assertThat(process.getRouterId(), equalTo(Ip.parse("10.0.0.1")));

    process.setRouterId(Ip.parse("192.168.1.1"));
    assertThat(process.getRouterId(), equalTo(Ip.parse("192.168.1.1")));

    process.setRouterId(Ip.parse("172.16.0.1"));
    assertThat(process.getRouterId(), equalTo(Ip.parse("172.16.0.1")));
  }

  @Test
  public void testSetNeighbors() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getNeighbors().size(), equalTo(0));

    Map<Ip, BgpPeerConfig> neighbors = new TreeMap<>();
    neighbors.put(
        Ip.parse("10.0.0.1"),
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAsns(LongSpace.of(65002L))
            .build());
    neighbors.put(
        Ip.parse("10.0.0.2"),
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.2"))
            .setRemoteAsns(LongSpace.of(65003L))
            .build());

    process.setNeighbors(neighbors);
    assertThat(process.getNeighbors(), equalTo(neighbors));
    assertThat(process.getNeighbors().size(), equalTo(2));
  }

  @Test
  public void testAddNeighbor() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getNeighbors().size(), equalTo(0));

    BgpActivePeerConfig neighbor1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAsns(LongSpace.of(65002L))
            .build();
    process.addNeighbor(Ip.parse("10.0.0.1"), neighbor1);
    assertThat(process.getNeighbors().size(), equalTo(1));
    assertThat(process.getNeighbors(), hasKey(Ip.parse("10.0.0.1")));
    assertThat(process.getNeighbors().get(Ip.parse("10.0.0.1")), equalTo(neighbor1));

    BgpActivePeerConfig neighbor2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.2"))
            .setRemoteAsns(LongSpace.of(65003L))
            .build();
    process.addNeighbor(Ip.parse("10.0.0.2"), neighbor2);
    assertThat(process.getNeighbors().size(), equalTo(2));
    assertThat(process.getNeighbors(), hasKey(Ip.parse("10.0.0.2")));
    assertThat(process.getNeighbors().get(Ip.parse("10.0.0.2")), equalTo(neighbor2));
  }

  @Test
  public void testAddNeighborOverwrites() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    BgpActivePeerConfig neighbor1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAsns(LongSpace.of(65002L))
            .build();
    process.addNeighbor(Ip.parse("10.0.0.1"), neighbor1);
    assertThat(process.getNeighbors().size(), equalTo(1));

    // Add neighbor with same IP should overwrite
    BgpActivePeerConfig neighbor2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAsns(LongSpace.of(65003L))
            .build();
    process.addNeighbor(Ip.parse("10.0.0.1"), neighbor2);
    assertThat(process.getNeighbors().size(), equalTo(1));
    assertThat(process.getNeighbors().get(Ip.parse("10.0.0.1")), equalTo(neighbor2));
  }

  @Test
  public void testSetPeerGroups() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getPeerGroups().size(), equalTo(0));

    Map<String, Object> peerGroups = new TreeMap<>();
    peerGroups.put("GROUP1", new Object());
    peerGroups.put("GROUP2", new Object());

    process.setPeerGroups(peerGroups);
    assertThat(process.getPeerGroups(), equalTo(peerGroups));
    assertThat(process.getPeerGroups().size(), equalTo(2));
  }

  @Test
  public void testSetPeerGroupsToEmpty() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    Map<String, Object> peerGroups = new TreeMap<>();
    peerGroups.put("GROUP1", new Object());
    process.setPeerGroups(peerGroups);
    assertThat(process.getPeerGroups().size(), equalTo(1));

    // Replace with empty map
    process.setPeerGroups(new TreeMap<>());
    assertThat(process.getPeerGroups().size(), equalTo(0));
  }

  @Test
  public void testSetAddressFamilies() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getAddressFamilies().size(), equalTo(0));

    Map<String, Object> addressFamilies = new TreeMap<>();
    addressFamilies.put("ipv4", new Object());
    addressFamilies.put("ipv6", new Object());
    addressFamilies.put("vpnv4", new Object());

    process.setAddressFamilies(addressFamilies);
    assertThat(process.getAddressFamilies(), equalTo(addressFamilies));
    assertThat(process.getAddressFamilies().size(), equalTo(3));
  }

  @Test
  public void testSetAddressFamiliesDifferentTypes() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    Map<String, Object> addressFamilies = new TreeMap<>();
    addressFamilies.put("ipv4", "IPv4 unicast");
    addressFamilies.put("ipv6", "IPv6 unicast");
    addressFamilies.put("vpnv4", "VPNv4 unicast");
    addressFamilies.put("evpn", "EVPN");

    process.setAddressFamilies(addressFamilies);
    assertThat(process.getAddressFamilies().size(), equalTo(4));
    assertThat(process.getAddressFamilies().get("ipv4"), equalTo("IPv4 unicast"));
    assertThat(process.getAddressFamilies().get("ipv6"), equalTo("IPv6 unicast"));
    assertThat(process.getAddressFamilies().get("vpnv4"), equalTo("VPNv4 unicast"));
    assertThat(process.getAddressFamilies().get("evpn"), equalTo("EVPN"));
  }

  @Test
  public void testMultipleProcesses() {
    HuaweiBgpProcess process1 = new HuaweiBgpProcess(65001L);
    HuaweiBgpProcess process2 = new HuaweiBgpProcess(65002L);

    assertThat(process1.getAsNum(), equalTo(65001L));
    assertThat(process2.getAsNum(), equalTo(65002L));

    process1.setRouterId(Ip.parse("1.1.1.1"));
    process2.setRouterId(Ip.parse("2.2.2.2"));

    assertThat(process1.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(process2.getRouterId(), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testFullConfiguration() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    // Set router ID
    process.setRouterId(Ip.parse("1.1.1.1"));

    // Add neighbors
    BgpActivePeerConfig neighbor1 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAsns(LongSpace.of(65002L))
            .build();
    process.addNeighbor(Ip.parse("10.0.0.1"), neighbor1);

    BgpActivePeerConfig neighbor2 =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.2"))
            .setRemoteAsns(LongSpace.of(65003L))
            .build();
    process.addNeighbor(Ip.parse("10.0.0.2"), neighbor2);

    // Set peer groups
    Map<String, Object> peerGroups = new TreeMap<>();
    peerGroups.put("GROUP1", new Object());
    peerGroups.put("GROUP2", new Object());
    process.setPeerGroups(peerGroups);

    // Set address families
    Map<String, Object> addressFamilies = new TreeMap<>();
    addressFamilies.put("ipv4", new Object());
    addressFamilies.put("ipv6", new Object());
    process.setAddressFamilies(addressFamilies);

    // Verify all settings
    assertThat(process.getAsNum(), equalTo(65001L));
    assertThat(process.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(process.getNeighbors().size(), equalTo(2));
    assertThat(process.getPeerGroups().size(), equalTo(2));
    assertThat(process.getAddressFamilies().size(), equalTo(2));
  }

  @Test
  public void testEmptyCollectionsAfterConstruction() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    assertThat(process.getNeighbors().size(), equalTo(0));
    assertThat(process.getPeerGroups().size(), equalTo(0));
    assertThat(process.getAddressFamilies().size(), equalTo(0));
  }

  @Test
  public void testSetAsNumMultipleTimes() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    process.setAsNum(65002L);
    assertThat(process.getAsNum(), equalTo(65002L));

    process.setAsNum(65003L);
    assertThat(process.getAsNum(), equalTo(65003L));

    process.setAsNum(1L);
    assertThat(process.getAsNum(), equalTo(1L));

    process.setAsNum(4294967295L);
    assertThat(process.getAsNum(), equalTo(4294967295L));
  }

  @Test
  public void testNeighborsMapModification() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    // Add neighbor via addNeighbor
    BgpActivePeerConfig neighbor =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAsns(LongSpace.of(65002L))
            .build();
    process.addNeighbor(Ip.parse("10.0.0.1"), neighbor);

    assertThat(process.getNeighbors().size(), equalTo(1));

    // Replace entire map
    Map<Ip, BgpPeerConfig> newNeighbors = new TreeMap<>();
    newNeighbors.put(
        Ip.parse("10.0.0.2"),
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.2"))
            .setRemoteAsns(LongSpace.of(65003L))
            .build());
    process.setNeighbors(newNeighbors);

    assertThat(process.getNeighbors().size(), equalTo(1));
    assertThat(process.getNeighbors(), hasKey(Ip.parse("10.0.0.2")));
  }

  @Test
  public void testRouterIdNullValueHandling() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    // Initial state
    assertThat(process.getRouterId(), nullValue());

    // Set to non-null
    process.setRouterId(Ip.parse("1.1.1.1"));
    assertThat(process.getRouterId(), equalTo(Ip.parse("1.1.1.1")));

    // Set back to null
    process.setRouterId(null);
    assertThat(process.getRouterId(), nullValue());

    // Set to another value
    process.setRouterId(Ip.parse("2.2.2.2"));
    assertThat(process.getRouterId(), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testGetNeighborByIp() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    BgpActivePeerConfig neighbor =
        BgpActivePeerConfig.builder()
            .setPeerAddress(Ip.parse("10.0.0.1"))
            .setRemoteAsns(LongSpace.of(65002L))
            .setDescription("Test Neighbor")
            .build();
    process.addNeighbor(Ip.parse("10.0.0.1"), neighbor);

    // Retrieve neighbor by IP
    BgpPeerConfig retrieved = process.getNeighbors().get(Ip.parse("10.0.0.1"));
    assertThat(retrieved, equalTo(neighbor));
    assertThat(retrieved.getDescription(), equalTo("Test Neighbor"));
  }

  @Test
  public void testCollectionsAreTreeMaps() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    // Verify collections are TreeMaps (sorted)
    assertThat(process.getNeighbors() instanceof TreeMap, equalTo(true));
    assertThat(process.getPeerGroups() instanceof TreeMap, equalTo(true));
    assertThat(process.getAddressFamilies() instanceof TreeMap, equalTo(true));
  }
}

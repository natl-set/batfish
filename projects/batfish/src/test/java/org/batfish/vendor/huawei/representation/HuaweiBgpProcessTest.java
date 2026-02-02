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
import org.batfish.datamodel.Prefix;
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
    assertThat(process.getNetworks().size(), equalTo(0));
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
  public void testPeerGroup() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getPeerGroups().size(), equalTo(0));

    HuaweiBgpProcess.HuaweiBgpPeerGroup group1 = process.getOrCreatePeerGroup("GROUP1");
    assertThat(group1.getName(), equalTo("GROUP1"));
    assertThat(process.getPeerGroups().size(), equalTo(1));

    // Get same group should return existing
    HuaweiBgpProcess.HuaweiBgpPeerGroup group1Again = process.getOrCreatePeerGroup("GROUP1");
    assertThat(group1Again, equalTo(group1));
    assertThat(process.getPeerGroups().size(), equalTo(1));

    // Add another group
    HuaweiBgpProcess.HuaweiBgpPeerGroup group2 = process.getOrCreatePeerGroup("GROUP2");
    group2.setType(HuaweiBgpProcess.HuaweiBgpPeerGroup.PeerType.EXTERNAL);
    group2.setRemoteAs(65002L);
    assertThat(process.getPeerGroups().size(), equalTo(2));
    assertThat(
        process.getPeerGroups().get("GROUP2").getType(),
        equalTo(HuaweiBgpProcess.HuaweiBgpPeerGroup.PeerType.EXTERNAL));
    assertThat(process.getPeerGroups().get("GROUP2").getRemoteAs(), equalTo(65002L));
  }

  @Test
  public void testPeerGroupSettings() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    HuaweiBgpProcess.HuaweiBgpPeerGroup group = process.getOrCreatePeerGroup("INTERNAL_PEERS");

    group.setType(HuaweiBgpProcess.HuaweiBgpPeerGroup.PeerType.INTERNAL);
    assertThat(group.getType(), equalTo(HuaweiBgpProcess.HuaweiBgpPeerGroup.PeerType.INTERNAL));

    group.setRemoteAs(65001L);
    assertThat(group.getRemoteAs(), equalTo(65001L));

    group.setPassword("secret");
    assertThat(group.getPassword(), equalTo("secret"));

    group.setRoutePolicyIn("POLICY_IN");
    assertThat(group.getRoutePolicyIn(), equalTo("POLICY_IN"));

    group.setRoutePolicyOut("POLICY_OUT");
    assertThat(group.getRoutePolicyOut(), equalTo("POLICY_OUT"));

    group.setRouteReflectorClient(true);
    assertThat(group.getRouteReflectorClient(), equalTo(true));

    group.setClusterId("1.1.1.1");
    assertThat(group.getClusterId(), equalTo("1.1.1.1"));

    group.setConnectInterface("Loopback0");
    assertThat(group.getConnectInterface(), equalTo("Loopback0"));
  }

  @Test
  public void testAddressFamily() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getAddressFamilies().size(), equalTo(0));

    HuaweiBgpProcess.HuaweiBgpAddressFamily ipv4 =
        new HuaweiBgpProcess.HuaweiBgpAddressFamily("ipv4");
    ipv4.setType(HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV4);
    ipv4.setUnicast(true);
    process.getAddressFamilies().put("ipv4", ipv4);

    HuaweiBgpProcess.HuaweiBgpAddressFamily ipv6 =
        new HuaweiBgpProcess.HuaweiBgpAddressFamily("ipv6");
    ipv6.setType(HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV6);
    ipv6.setUnicast(true);
    process.getAddressFamilies().put("ipv6", ipv6);

    assertThat(process.getAddressFamilies().size(), equalTo(2));
    assertThat(
        process.getAddressFamilies().get("ipv4").getType(),
        equalTo(HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV4));
    assertThat(process.getAddressFamilies().get("ipv4").isUnicast(), equalTo(true));
  }

  @Test
  public void testBgpNetwork() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getNetworks().size(), equalTo(0));

    Ip networkMask = Ip.parse("255.255.255.0");
    Prefix prefix = Prefix.parse("10.0.0.0/24");

    HuaweiBgpProcess.HuaweiBgpNetwork network =
        new HuaweiBgpProcess.HuaweiBgpNetwork(prefix, networkMask);
    network.setRoutePolicy("ROUTE_POLICY_1");

    process.addNetwork(network);
    assertThat(process.getNetworks().size(), equalTo(1));
    assertThat(process.getNetworks().get(0).getNetwork(), equalTo(prefix));
    assertThat(process.getNetworks().get(0).getMask(), equalTo(networkMask));
    assertThat(process.getNetworks().get(0).getRoutePolicy(), equalTo("ROUTE_POLICY_1"));
  }

  @Test
  public void testSetPeerGroups() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getPeerGroups().size(), equalTo(0));

    Map<String, HuaweiBgpProcess.HuaweiBgpPeerGroup> peerGroups = new TreeMap<>();
    peerGroups.put("GROUP1", new HuaweiBgpProcess.HuaweiBgpPeerGroup("GROUP1"));
    peerGroups.put("GROUP2", new HuaweiBgpProcess.HuaweiBgpPeerGroup("GROUP2"));

    process.setPeerGroups(peerGroups);
    assertThat(process.getPeerGroups(), equalTo(peerGroups));
    assertThat(process.getPeerGroups().size(), equalTo(2));
  }

  @Test
  public void testSetPeerGroupsToEmpty() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    Map<String, HuaweiBgpProcess.HuaweiBgpPeerGroup> peerGroups = new TreeMap<>();
    peerGroups.put("GROUP1", new HuaweiBgpProcess.HuaweiBgpPeerGroup("GROUP1"));
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

    Map<String, HuaweiBgpProcess.HuaweiBgpAddressFamily> addressFamilies = new TreeMap<>();
    addressFamilies.put("ipv4", new HuaweiBgpProcess.HuaweiBgpAddressFamily("ipv4"));
    addressFamilies.put("ipv6", new HuaweiBgpProcess.HuaweiBgpAddressFamily("ipv6"));
    addressFamilies.put("vpnv4", new HuaweiBgpProcess.HuaweiBgpAddressFamily("vpnv4"));

    process.setAddressFamilies(addressFamilies);
    assertThat(process.getAddressFamilies(), equalTo(addressFamilies));
    assertThat(process.getAddressFamilies().size(), equalTo(3));
  }

  @Test
  public void testSetAddressFamiliesDifferentTypes() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    Map<String, HuaweiBgpProcess.HuaweiBgpAddressFamily> addressFamilies = new TreeMap<>();
    HuaweiBgpProcess.HuaweiBgpAddressFamily ipv4 =
        new HuaweiBgpProcess.HuaweiBgpAddressFamily("ipv4");
    ipv4.setType(HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV4);
    ipv4.setUnicast(true);
    addressFamilies.put("ipv4", ipv4);

    HuaweiBgpProcess.HuaweiBgpAddressFamily ipv6 =
        new HuaweiBgpProcess.HuaweiBgpAddressFamily("ipv6");
    ipv6.setType(HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV6);
    ipv6.setUnicast(true);
    addressFamilies.put("ipv6", ipv6);

    process.setAddressFamilies(addressFamilies);
    assertThat(process.getAddressFamilies().size(), equalTo(2));
    assertThat(
        process.getAddressFamilies().get("ipv4").getType(),
        equalTo(HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV4));
    assertThat(process.getAddressFamilies().get("ipv4").isUnicast(), equalTo(true));
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
    HuaweiBgpProcess.HuaweiBgpPeerGroup group1 = new HuaweiBgpProcess.HuaweiBgpPeerGroup("GROUP1");
    group1.setType(HuaweiBgpProcess.HuaweiBgpPeerGroup.PeerType.INTERNAL);
    process.getPeerGroups().put("GROUP1", group1);

    HuaweiBgpProcess.HuaweiBgpPeerGroup group2 = new HuaweiBgpProcess.HuaweiBgpPeerGroup("GROUP2");
    group2.setType(HuaweiBgpProcess.HuaweiBgpPeerGroup.PeerType.EXTERNAL);
    process.getPeerGroups().put("GROUP2", group2);

    // Set address families
    HuaweiBgpProcess.HuaweiBgpAddressFamily ipv4 =
        new HuaweiBgpProcess.HuaweiBgpAddressFamily("ipv4");
    ipv4.setType(HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV4);
    ipv4.setUnicast(true);
    process.getAddressFamilies().put("ipv4", ipv4);

    // Add networks
    process.addNetwork(
        new HuaweiBgpProcess.HuaweiBgpNetwork(
            Prefix.parse("10.0.0.0/24"), Ip.parse("255.255.255.0")));

    // Verify all settings
    assertThat(process.getAsNum(), equalTo(65001L));
    assertThat(process.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(process.getNeighbors().size(), equalTo(2));
    assertThat(process.getPeerGroups().size(), equalTo(2));
    assertThat(process.getAddressFamilies().size(), equalTo(1));
    assertThat(process.getNetworks().size(), equalTo(1));
  }

  @Test
  public void testEmptyCollectionsAfterConstruction() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    assertThat(process.getNeighbors().size(), equalTo(0));
    assertThat(process.getPeerGroups().size(), equalTo(0));
    assertThat(process.getAddressFamilies().size(), equalTo(0));
    assertThat(process.getNetworks().size(), equalTo(0));
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

  @Test
  public void testImportRouteConstructor() {
    HuaweiBgpProcess.HuaweiBgpImportRoute importRoute =
        new HuaweiBgpProcess.HuaweiBgpImportRoute("ospf");
    assertThat(importRoute.getProtocol(), equalTo("ospf"));
    assertThat(importRoute.getRoutePolicy(), nullValue());
  }

  @Test
  public void testImportRouteWithRoutePolicy() {
    HuaweiBgpProcess.HuaweiBgpImportRoute importRoute =
        new HuaweiBgpProcess.HuaweiBgpImportRoute("static");
    importRoute.setRoutePolicy("FILTER_POLICY");
    assertThat(importRoute.getProtocol(), equalTo("static"));
    assertThat(importRoute.getRoutePolicy(), equalTo("FILTER_POLICY"));
  }

  @Test
  public void testAddImportRoute() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);
    assertThat(process.getImportRoutes().size(), equalTo(0));

    process.addImportRoute(new HuaweiBgpProcess.HuaweiBgpImportRoute("ospf"));
    assertThat(process.getImportRoutes().size(), equalTo(1));
    assertThat(process.getImportRoutes().get(0).getProtocol(), equalTo("ospf"));

    process.addImportRoute(new HuaweiBgpProcess.HuaweiBgpImportRoute("static"));
    assertThat(process.getImportRoutes().size(), equalTo(2));
  }

  @Test
  public void testImportRouteWithDifferentProtocols() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    // Test different protocol types
    process.addImportRoute(new HuaweiBgpProcess.HuaweiBgpImportRoute("direct"));
    process.addImportRoute(new HuaweiBgpProcess.HuaweiBgpImportRoute("static"));
    process.addImportRoute(new HuaweiBgpProcess.HuaweiBgpImportRoute("ospf"));
    process.addImportRoute(new HuaweiBgpProcess.HuaweiBgpImportRoute("isis"));
    process.addImportRoute(new HuaweiBgpProcess.HuaweiBgpImportRoute("rip"));

    assertThat(process.getImportRoutes().size(), equalTo(5));
    assertThat(process.getImportRoutes().get(0).getProtocol(), equalTo("direct"));
    assertThat(process.getImportRoutes().get(1).getProtocol(), equalTo("static"));
    assertThat(process.getImportRoutes().get(2).getProtocol(), equalTo("ospf"));
    assertThat(process.getImportRoutes().get(3).getProtocol(), equalTo("isis"));
    assertThat(process.getImportRoutes().get(4).getProtocol(), equalTo("rip"));
  }

  @Test
  public void testSetImportRoutes() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    java.util.List<HuaweiBgpProcess.HuaweiBgpImportRoute> importRoutes =
        new java.util.ArrayList<>();
    importRoutes.add(new HuaweiBgpProcess.HuaweiBgpImportRoute("ospf"));
    importRoutes.add(new HuaweiBgpProcess.HuaweiBgpImportRoute("static"));

    process.setImportRoutes(importRoutes);
    assertThat(process.getImportRoutes(), equalTo(importRoutes));
    assertThat(process.getImportRoutes().size(), equalTo(2));
  }

  @Test
  public void testImportRouteProtocolSetter() {
    HuaweiBgpProcess.HuaweiBgpImportRoute importRoute =
        new HuaweiBgpProcess.HuaweiBgpImportRoute("ospf");

    importRoute.setProtocol("isis");
    assertThat(importRoute.getProtocol(), equalTo("isis"));

    importRoute.setProtocol("bgp");
    assertThat(importRoute.getProtocol(), equalTo("bgp"));
  }

  @Test
  public void testImportRouteWithPolicyAndProtocol() {
    HuaweiBgpProcess process = new HuaweiBgpProcess(65001L);

    HuaweiBgpProcess.HuaweiBgpImportRoute importRoute =
        new HuaweiBgpProcess.HuaweiBgpImportRoute("ospf");
    importRoute.setRoutePolicy("REDISTRIBUTE_POLICY");

    process.addImportRoute(importRoute);

    assertThat(process.getImportRoutes().size(), equalTo(1));
    assertThat(process.getImportRoutes().get(0).getProtocol(), equalTo("ospf"));
    assertThat(process.getImportRoutes().get(0).getRoutePolicy(), equalTo("REDISTRIBUTE_POLICY"));
  }
}

package org.batfish.vendor.huawei.representation;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Names.generatedBgpMainRibIndependentNetworkPolicyName;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpAuthenticationAlgorithm;
import org.batfish.datamodel.BgpAuthenticationSettings;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.vendor_family.huawei.HuaweiFamily;

/**
 * Conversion utilities for transforming Huawei VRP configurations to Batfish's vendor-independent
 * format.
 *
 * <p>This class provides static methods to convert various aspects of Huawei configuration to
 * Batfish's abstract model, including interfaces, routing protocols, ACLs, and other features.
 */
public class HuaweiConversions {

  /**
   * Converts a Huawei configuration to a Batfish vendor-independent Configuration.
   *
   * @param huaweiConfig The Huawei configuration to convert
   * @return A Batfish Configuration object
   */
  public static @Nonnull Configuration toVendorIndependentConfiguration(
      @Nonnull HuaweiConfiguration huaweiConfig) {
    // Create new Configuration object with hostname and format
    Configuration c = new Configuration(huaweiConfig.getHostname(), ConfigurationFormat.HUAWEI);

    // Set Huawei vendor family
    c.getVendorFamily().setHuawei(new org.batfish.datamodel.vendor_family.huawei.HuaweiFamily());

    // Convert interfaces
    toConfigurationInterfaces(huaweiConfig, c);

    // Set default VRF
    c.getVrfs()
        .computeIfAbsent(
            DEFAULT_VRF_NAME,
            vrfName -> org.batfish.datamodel.Vrf.builder().setName(DEFAULT_VRF_NAME).build());

    // Convert static routes
    toConfigurationStaticRoutes(c, huaweiConfig);

    // Convert NAT rules
    toConfigurationNat(c, huaweiConfig);

    // Convert OSPF
    toConfigurationOspf(c, huaweiConfig);

    // Convert BGP
    toConfigurationBgp(c, huaweiConfig);

    // Convert VRFs
    toConfigurationVrfs(c, huaweiConfig);

    // Convert ACLs
    toConfigurationAcls(c, huaweiConfig);

    // Convert route-policies
    toConfigurationRoutePolicies(c, huaweiConfig);

    return c;
  }

  /**
   * Converts Huawei interface configurations to Batfish vendor-independent interfaces.
   *
   * @param huaweiConfig The Huawei configuration to convert from
   * @param c The Batfish Configuration object to populate
   */
  private static void toConfigurationInterfaces(
      @Nonnull HuaweiConfiguration huaweiConfig, @Nonnull Configuration c) {
    for (Entry<String, HuaweiInterface> e : huaweiConfig.getInterfaces().entrySet()) {
      String name = e.getKey();
      HuaweiInterface huaweiIface = e.getValue();
      Interface iface = toInterface(name, huaweiIface);
      c.getAllInterfaces().put(name, iface);
    }
  }

  /**
   * Converts a single Huawei interface to a Batfish vendor-independent interface.
   *
   * @param name The interface name
   * @param huaweiInterface The Huawei interface to convert
   * @return A Batfish Interface object
   */
  private static @Nonnull Interface toInterface(
      @Nonnull String name, @Nonnull HuaweiInterface huaweiInterface) {
    Interface.Builder builder = Interface.builder();

    // Set name and type based on interface name
    builder.setName(name).setType(getInterfaceType(name));

    // Set description
    if (huaweiInterface.getDescription() != null) {
      builder.setDescription(huaweiInterface.getDescription());
    }

    // Set admin status (active if not shutdown)
    builder.setAdminUp(!huaweiInterface.getShutdown());

    // Set address
    if (huaweiInterface.getAddress() != null) {
      builder.setAddress(huaweiInterface.getAddress());
    }

    // Set MTU
    if (huaweiInterface.getMtu() != 0) {
      builder.setMtu(huaweiInterface.getMtu());
    }

    // Set bandwidth (explicit or default)
    Double bandwidth = huaweiInterface.getBandwidth();
    if (bandwidth == null) {
      bandwidth = HuaweiInterface.getDefaultBandwidth(name);
    }
    if (bandwidth != null) {
      builder.setSpeed(bandwidth);
    }

    // Set DHCP relay addresses
    if (!huaweiInterface.getDhcpRelayAddresses().isEmpty()) {
      builder.setDhcpRelayAddresses(ImmutableList.copyOf(huaweiInterface.getDhcpRelayAddresses()));
    }

    Interface iface = builder.build();

    // Set incoming filter if present (ACL name references are sufficient since
    // ACLs are already converted in toConfigurationAcls)
    if (huaweiInterface.getIncomingFilter() != null) {
      iface.setIncomingFilterName(huaweiInterface.getIncomingFilter());
    }

    // Set outgoing filter if present (ACL name references are sufficient since
    // ACLs are already converted in toConfigurationAcls)
    if (huaweiInterface.getOutgoingFilter() != null) {
      iface.setOutgoingFilterName(huaweiInterface.getOutgoingFilter());
    }

    return iface;
  }

  /**
   * Converts Huawei interface configurations to Batfish vendor-independent interfaces.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   * @param vrf The VRF to attach interfaces to
   */
  public static void toConfigurationInterfaces(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg, @Nonnull Vrf vrf) {

    for (Map.Entry<String, HuaweiInterface> entry : huaweiCfg.getInterfaces().entrySet()) {
      HuaweiInterface huaweiIface = entry.getValue();

      // Building with owner=c automatically adds to c.getAllInterfaces()
      toInterface(huaweiIface, vrf, c);
    }
  }

  /**
   * Converts a single Huawei interface to a Batfish vendor-independent interface.
   *
   * @param huaweiIface The Huawei interface to convert
   * @param vrf The VRF to attach the interface to
   * @param c The Configuration that owns the interface
   * @return A Batfish Interface object
   */
  public static @Nonnull Interface toInterface(
      @Nonnull HuaweiInterface huaweiIface, @Nonnull Vrf vrf, @Nonnull Configuration c) {
    String name = huaweiIface.getName();
    // Use builder pattern
    Interface.Builder builder =
        Interface.builder()
            .setName(name)
            .setType(getInterfaceType(name))
            .setVrf(vrf)
            .setVrf(vrf)
            .setOwner(c)
            .setAdminUp(!huaweiIface.getShutdown())
            .setMtu(huaweiIface.getMtu());

    // Set address if present
    if (huaweiIface.getAddress() != null) {
      builder.setAddress(huaweiIface.getAddress());
    }

    // Set description if present
    if (huaweiIface.getDescription() != null) {
      builder.setDescription(huaweiIface.getDescription());
    }

    // Set bandwidth if present
    if (huaweiIface.getBandwidth() != null) {
      builder.setSpeed(huaweiIface.getBandwidth());
    } else {
      // Set default bandwidth based on interface type
      Double defaultSpeed = HuaweiInterface.getDefaultBandwidth(huaweiIface.getName());
      if (defaultSpeed != null) {
        builder.setSpeed(defaultSpeed);
      }
    }

    // Set DHCP relay addresses
    if (!huaweiIface.getDhcpRelayAddresses().isEmpty()) {
      builder.setDhcpRelayAddresses(ImmutableList.copyOf(huaweiIface.getDhcpRelayAddresses()));
    }

    Interface iface = builder.build();

    // Set incoming filter if present
    if (huaweiIface.getIncomingFilter() != null) {
      iface.setIncomingFilterName(huaweiIface.getIncomingFilter());
    }

    // Set outgoing filter if present
    if (huaweiIface.getOutgoingFilter() != null) {
      iface.setOutgoingFilterName(huaweiIface.getOutgoingFilter());
    }

    return iface;
    // Set DHCP relay client flag
  }

  /**
   * Determines the Batfish InterfaceType for a Huawei interface based on its name.
   *
   * <p>Maps Huawei interface types to Batfish vendor-independent interface types:
   *
   * <ul>
   *   <li>GigabitEthernet, 10GE, 25GE, 40GE, 100GE, Ethernet, Pos, Serial → PHYSICAL
   *   <li>Vlanif → VLAN
   *   <li>LoopBack → LOOPBACK
   *   <li>Eth-Trunk → AGGREGATED
   *   <li>Subinterfaces (GigabitEthernet0/0/0.1, etc.) → LOGICAL or AGGREGATE_CHILD
   *   <li>Tunnel → TUNNEL
   *   <li>Null → NULL
   * </ul>
   *
   * @param interfaceName The Huawei interface name
   * @return The corresponding Batfish InterfaceType
   */
  private static @Nonnull InterfaceType getInterfaceType(@Nonnull String interfaceName) {
    // Check for subinterfaces (contain a dot with number after)
    if (interfaceName.contains(".")) {
      String[] parts = interfaceName.split("\\\\.");
      if (parts.length == 2) {
        try {
          // If there's a number after the dot, it's a subinterface
          Integer.parseInt(parts[1]);
          // Determine if parent is aggregate
          String parentName = parts[0];
          if (parentName.startsWith("Eth-Trunk") || parentName.startsWith("Port-channel")) {
            return InterfaceType.AGGREGATE_CHILD;
          }
          return InterfaceType.LOGICAL;
        } catch (NumberFormatException e) {
          // Not a subinterface, continue checking
        }
      }
    }

    // Check for specific interface types
    if (interfaceName.startsWith("GigabitEthernet")
        || interfaceName.startsWith("10GE")
        || interfaceName.startsWith("25GE")
        || interfaceName.startsWith("40GE")
        || interfaceName.startsWith("100GE")
        || interfaceName.startsWith("GE")
        || interfaceName.startsWith("Ethernet")
        || interfaceName.startsWith("FastEthernet")
        || interfaceName.startsWith("Pos")
        || interfaceName.startsWith("Serial")
        || interfaceName.startsWith("XG")) {
      return InterfaceType.PHYSICAL;
    }

    if (interfaceName.startsWith("Vlanif")) {
      return InterfaceType.VLAN;
    }

    if (interfaceName.startsWith("LoopBack") || interfaceName.startsWith("Loopback")) {
      return InterfaceType.LOOPBACK;
    }

    if (interfaceName.startsWith("Eth-Trunk") || interfaceName.startsWith("Port-channel")) {
      return InterfaceType.AGGREGATED;
    }

    if (interfaceName.startsWith("Tunnel") || interfaceName.startsWith("Gre")) {
      return InterfaceType.TUNNEL;
    }

    if (interfaceName.startsWith("Inherit-Vlan") || interfaceName.startsWith("Dot1q")) {
      return InterfaceType.AGGREGATE_CHILD;
    }

    if (interfaceName.startsWith("Null")) {
      return InterfaceType.NULL;
    }

    // Default to PHYSICAL for unknown types
    return InterfaceType.PHYSICAL;
  }

  /**
   * Converts Huawei BGP process to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationBgp(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    HuaweiBgpProcess huaweiBgp = huaweiCfg.getBgpProcess();
    if (huaweiBgp == null) {
      return;
    }

    // Get default VRF
    Vrf vrf = c.getVrfs().get(DEFAULT_VRF_NAME);
    if (vrf == null) {
      return;
    }

    // Build BGP process
    BgpProcess.Builder bgpBuilder = BgpProcess.builder();

    // Set router ID (use loopback address if router ID not configured)
    Ip routerId = huaweiBgp.getRouterId();
    if (routerId == null) {
      // Try to find loopback interface
      routerId =
          c.getAllInterfaces().values().stream()
              .filter(iface -> iface.getName().contains("Loopback"))
              .filter(iface -> iface.getAddress() != null)
              .map(
                  iface -> {
                    InterfaceAddress addr = iface.getAddress();
                    if (addr instanceof org.batfish.datamodel.ConcreteInterfaceAddress) {
                      return ((org.batfish.datamodel.ConcreteInterfaceAddress) addr).getIp();
                    }
                    return Ip.ZERO;
                  })
              .findFirst()
              .orElse(Ip.ZERO);
    }
    bgpBuilder.setRouterId(routerId);

    // Set administrative costs (Huawei defaults: eBGP=20, iBGP=255)
    bgpBuilder.setEbgpAdminCost(20).setIbgpAdminCost(255).setLocalAdminCost(255);

    // Set tie-breakers
    bgpBuilder
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP);

    // Build and set BGP process
    BgpProcess bgpProcess = bgpBuilder.build();
    vrf.setBgpProcess(bgpProcess);

    // Convert BGP neighbors/peers
    // Note: HuaweiBgpProcess already stores neighbors as BgpPeerConfig objects
    // We need to apply peer group settings to peers that reference a group
    huaweiBgp
        .getNeighbors()
        .forEach(
            (peerIp, peerConfig) -> {
              // If it's already an active peer config, apply peer group settings
              if (peerConfig instanceof BgpActivePeerConfig) {
                BgpActivePeerConfig activePeer = (BgpActivePeerConfig) peerConfig;

                // Apply peer group settings if this peer references a group
                String groupName = activePeer.getGroup();
                if (groupName != null) {
                  activePeer = applyPeerGroupSettings(activePeer, groupName, huaweiBgp);
                }

                bgpProcess.getActiveNeighbors().put(peerIp, activePeer);
              } else {
                // Otherwise create a new active peer config
                BgpActivePeerConfig.Builder builder = BgpActivePeerConfig.builder();
                builder.setPeerAddress(peerIp);
                // Additional peer settings would be copied here if the peerConfig has them
                bgpProcess.getActiveNeighbors().put(peerIp, builder.build());
              }
            });

    // Convert network announcements
    // Network statements in Huawei BGP specify which networks to advertise into BGP
    // The syntax is: network <ip> <mask> [route-policy <policy>]
    if (!huaweiBgp.getNetworks().isEmpty()) {
      convertBgpNetworks(c, DEFAULT_VRF_NAME, bgpProcess, huaweiBgp);
    }

    // Convert import-route (redistribution) configurations
    // Import-route statements redistribute routes from other protocols into BGP
    // The syntax is: import-route <protocol> [route-policy <policy>]
    if (!huaweiBgp.getImportRoutes().isEmpty()) {
      convertBgpImportRoutes(c, DEFAULT_VRF_NAME, bgpProcess, huaweiBgp);
    }

    // Convert address families
    // Address family extraction is partially implemented but conversion is not yet complete
    // This is tracked in the parsing documentation as state 3 (in grammar, not implemented)
    if (!huaweiBgp.getAddressFamilies().isEmpty()) {
      convertBgpAddressFamilies(c, bgpProcess, huaweiBgp);
    }

    // Route policies are now converted with support for:
    // - Match conditions: ip-prefix, community-filter
    // - Set actions: local-preference, tag, cost, preference
    // Community matching and ACL matching are not yet implemented

    // TODO: Implement route-policy community matching using MatchCommunities
    // TODO: Implement route-policy ACL matching (if-match acl)
  }

  /**
   * Converts Huawei BGP address families to Batfish vendor-independent format.
   *
   * <p>Address families in Huawei BGP specify which address families are enabled for BGP. The
   * syntax is: ipv4 [unicast|multicast] or ipv6 [unicast|multicast]
   *
   * <p>This conversion:
   *
   * <ul>
   *   <li>Creates Ipv4UnicastAddressFamily for IPv4 unicast address families
   *   <li>Creates Ipv6UnicastAddressFamily for IPv6 unicast address families
   *   <li>Applies import/export policies if configured
   *   <li>Sets route-reflector-client flag if configured
   * </ul>
   *
   * @param c The Batfish Configuration
   * @param bgpProcess The BgpProcess to update
   * @param huaweiBgp The Huawei BGP process containing address families
   */
  private static void convertBgpAddressFamilies(
      Configuration c, BgpProcess bgpProcess, HuaweiBgpProcess huaweiBgp) {
    // Store address family configurations to apply to peers
    org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily ipv4Af = null;
    org.batfish.datamodel.bgp.Ipv6UnicastAddressFamily ipv6Af = null;

    // Process each address family
    for (HuaweiBgpProcess.HuaweiBgpAddressFamily af : huaweiBgp.getAddressFamilies().values()) {
      // Handle IPv4 unicast
      if (af.getType() == HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV4
          && af.isUnicast()) {
        // Build IPv4 unicast address family
        org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily.Builder afBuilder =
            org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily.builder();

        // Set address family capabilities
        afBuilder.setAddressFamilyCapabilities(
            org.batfish.datamodel.bgp.AddressFamilyCapabilities.builder()
                .setSendCommunity(true)
                .build());

        // Set import policy if configured
        if (af.getImportPolicy() != null) {
          afBuilder.setImportPolicy(af.getImportPolicy());
        }

        // Set export policy if configured
        if (af.getExportPolicy() != null) {
          afBuilder.setExportPolicy(af.getExportPolicy());
        }

        ipv4Af = afBuilder.build();
      }
      // Handle IPv6 unicast
      else if (af.getType() == HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV6
          && af.isUnicast()) {
        // Build IPv6 unicast address family
        org.batfish.datamodel.bgp.Ipv6UnicastAddressFamily.Builder afBuilder =
            org.batfish.datamodel.bgp.Ipv6UnicastAddressFamily.builder();

        // Set address family capabilities
        afBuilder.setAddressFamilyCapabilities(
            org.batfish.datamodel.bgp.AddressFamilyCapabilities.builder()
                .setSendCommunity(true)
                .build());

        // Set import policy if configured
        if (af.getImportPolicy() != null) {
          afBuilder.setImportPolicy(af.getImportPolicy());
        }

        // Set export policy if configured
        if (af.getExportPolicy() != null) {
          afBuilder.setExportPolicy(af.getExportPolicy());
        }

        ipv6Af = afBuilder.build();
      }
      // TODO: Add support for multicast address families
      // TODO: Add support for VPN address families
    }

    // Apply the address family configuration to all active neighbors
    // In Huawei, address family configuration is typically applied to peers
    // that have the address family enabled. We need to rebuild each peer
    // since BgpActivePeerConfig is immutable.
    for (Entry<Ip, BgpActivePeerConfig> entry : bgpProcess.getActiveNeighbors().entrySet()) {
      BgpActivePeerConfig peer = entry.getValue();
      BgpActivePeerConfig.Builder newPeerBuilder =
          BgpActivePeerConfig.builder()
              .setPeerAddress(peer.getPeerAddress())
              .setGroup(peer.getGroup())
              .setRemoteAsns(peer.getRemoteAsns())
              .setLocalAs(peer.getLocalAs())
              .setDescription(peer.getDescription())
              .setAuthenticationSettings(peer.getAuthenticationSettings())
              .setIpv4UnicastAddressFamily(
                  peer.getIpv4UnicastAddressFamily() != null
                      ? peer.getIpv4UnicastAddressFamily()
                      : ipv4Af)
              .setIpv6UnicastAddressFamily(
                  peer.getIpv6UnicastAddressFamily() != null
                      ? peer.getIpv6UnicastAddressFamily()
                      : ipv6Af)
              .setEvpnAddressFamily(peer.getEvpnAddressFamily());

      BgpActivePeerConfig newPeer = newPeerBuilder.build();
      bgpProcess.getActiveNeighbors().put(entry.getKey(), newPeer);
    }
  }

  /**
   * Converts Huawei OSPF process to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationOspf(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    HuaweiOspfProcess huaweiOspf = huaweiCfg.getOspfProcess();
    if (huaweiOspf == null) {
      return;
    }

    // Get default VRF
    Vrf vrf = c.getVrfs().get(DEFAULT_VRF_NAME);
    if (vrf == null) {
      return;
    }

    // Build OSPF process
    OspfProcess.Builder ospfBuilder = OspfProcess.builder();

    // Set process ID (use string representation)
    ospfBuilder.setProcessId(String.valueOf(huaweiOspf.getProcessId()));

    // Set router ID (use loopback if not configured)
    Ip routerId = huaweiOspf.getRouterId();
    if (routerId == null) {
      // Try to find loopback interface
      routerId =
          c.getAllInterfaces().values().stream()
              .filter(iface -> iface.getName().contains("Loopback"))
              .filter(iface -> iface.getAddress() != null)
              .map(
                  iface -> {
                    InterfaceAddress addr = iface.getAddress();
                    if (addr instanceof org.batfish.datamodel.ConcreteInterfaceAddress) {
                      return ((org.batfish.datamodel.ConcreteInterfaceAddress) addr).getIp();
                    }
                    return Ip.ZERO;
                  })
              .findFirst()
              .orElse(Ip.ZERO);
    }
    ospfBuilder.setRouterId(routerId);

    // Set reference bandwidth (Huawei default: 100 Mbps)
    ospfBuilder.setReferenceBandwidth(100000000.0);

    // Set VRF
    ospfBuilder.setVrf(vrf);

    // Convert OSPF areas
    ImmutableMap.Builder<Long, OspfArea> areasBuilder = ImmutableMap.builder();
    for (HuaweiOspfProcess.HuaweiOspfArea huaweiArea : huaweiOspf.getAreas().values()) {
      OspfArea area = toOspfArea(huaweiArea, c);
      areasBuilder.put(huaweiArea.getAreaId(), area);
    }
    ospfBuilder.setAreas(areasBuilder.build());

    // Convert OSPF virtual links
    // TODO: Convert virtual links to OspfProcess
    // Virtual links require area ID and remote router ID
    // Need to determine which area the virtual link belongs to
    if (!huaweiOspf.getVirtualLinks().isEmpty()) {
      for (HuaweiOspfProcess.HuaweiOspfVirtualLink vlink : huaweiOspf.getVirtualLinks()) {
        // Virtual link extraction is implemented but conversion is not yet complete
        // This is tracked in the parsing documentation as state 3 (in grammar, not implemented)
      }
    }

    // Convert OSPF interface settings
    String ospfProcessId = String.valueOf(huaweiOspf.getProcessId());
    for (Entry<String, HuaweiOspfProcess.HuaweiOspfInterfaceSettings> entry :
        huaweiOspf.getInterfaces().entrySet()) {
      String ifaceName = entry.getKey();
      HuaweiOspfProcess.HuaweiOspfInterfaceSettings huaweiIfaceSettings = entry.getValue();

      // Find the corresponding interface in the Batfish configuration
      Interface iface = c.getAllInterfaces().get(ifaceName);
      if (iface != null) {
        OspfInterfaceSettings.Builder ospfIfaceSettings = OspfInterfaceSettings.builder();

        // Set OSPF process
        ospfIfaceSettings.setProcess(ospfProcessId);

        // Set area ID if configured
        if (huaweiIfaceSettings.getAreaId() != null) {
          ospfIfaceSettings.setAreaName(huaweiIfaceSettings.getAreaId());
          ospfIfaceSettings.setEnabled(true);
        } else {
          // Interface is enabled for OSPF but no area specified
          ospfIfaceSettings.setEnabled(false);
        }

        // Set cost if configured
        if (huaweiIfaceSettings.getCost() != null) {
          ospfIfaceSettings.setCost(huaweiIfaceSettings.getCost());
        } else if (iface.isLoopback()) {
          // Default cost for loopback interfaces
          ospfIfaceSettings.setCost(0);
        }

        // Set hello interval if configured (default is 10 seconds)
        int helloInterval =
            huaweiIfaceSettings.getHelloInterval() != null
                ? huaweiIfaceSettings.getHelloInterval()
                : 10;
        ospfIfaceSettings.setHelloInterval(helloInterval);

        // Set dead interval if configured (default is 40 seconds)
        int deadInterval =
            huaweiIfaceSettings.getDeadInterval() != null
                ? huaweiIfaceSettings.getDeadInterval()
                : 40;
        ospfIfaceSettings.setDeadInterval(deadInterval);

        // Set network type if configured
        OspfNetworkType networkType = toOspfNetworkType(huaweiIfaceSettings.getNetworkType());
        ospfIfaceSettings.setNetworkType(networkType);

        // Set passive mode
        boolean passive =
            huaweiIfaceSettings.getPassive() != null && huaweiIfaceSettings.getPassive();
        ospfIfaceSettings.setPassive(passive);

        // Set OSPF settings on the interface
        iface.setOspfSettings(ospfIfaceSettings.build());
      }
    }

    // Set OSPF default originate if configured
    if (huaweiOspf.getDefaultOriginate()) {
      // Create a generated default route for OSPF to advertise
      org.batfish.datamodel.GeneratedRoute.Builder defaultRouteBuilder =
          org.batfish.datamodel.GeneratedRoute.builder()
              .setNetwork(Prefix.ZERO)
              .setAdmin(
                  org.batfish.datamodel.RoutingProtocol.OSPF.getDefaultAdministrativeCost(
                      org.batfish.datamodel.ConfigurationFormat.HUAWEI));

      // If a route-map is specified, use it as the generation policy
      String routeMap = huaweiOspf.getDefaultOriginateRouteMap();
      if (routeMap != null) {
        defaultRouteBuilder.setGenerationPolicy(routeMap);
      }

      ospfBuilder.setGeneratedRoutes(ImmutableSet.of(defaultRouteBuilder.build()));
    }

    // Convert OSPF redistribution policies
    if (!huaweiOspf.getRedistributionPolicies().isEmpty()) {
      String ospfExportPolicyName = computeOspfExportPolicyName(ospfProcessId);
      RoutingPolicy ospfExportPolicy = new RoutingPolicy(ospfExportPolicyName, c);
      c.getRoutingPolicies().put(ospfExportPolicyName, ospfExportPolicy);
      List<Statement> ospfExportStatements = ospfExportPolicy.getStatements();
      ospfBuilder.setExportPolicyName(ospfExportPolicyName);

      // Set default metric type to E2 (Huawei default)
      ospfExportStatements.add(new SetOspfMetricType(OspfMetricType.E2));
      // Set default metric to 0 (will be overridden by specific redistribution if set)
      ospfExportStatements.add(new SetMetric(new LiteralLong(0L)));

      // Convert each redistribution policy
      for (HuaweiOspfProcess.HuaweiOspfRedistributionPolicy policy :
          huaweiOspf.getRedistributionPolicies().values()) {
        If statement = convertOspfRedistributionPolicy(policy, huaweiCfg);
        if (statement != null) {
          ospfExportStatements.add(statement);
        }
      }
    }

    // Build and set OSPF process
    OspfProcess ospfProcess = ospfBuilder.build();
    vrf.setOspfProcesses(ImmutableSortedMap.of(ospfProcess.getProcessId(), ospfProcess));
  }

  /**
   * Converts Huawei static routes to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationStaticRoutes(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    for (HuaweiStaticRoute huaweiRoute : huaweiCfg.getStaticRoutes()) {
      // Build the static route
      StaticRoute.Builder builder =
          StaticRoute.builder()
              .setNetwork(huaweiRoute.getDestination())
              .setNextHopIp(huaweiRoute.getNextHopIp());

      // Set next-hop interface if present
      if (huaweiRoute.getNextHopInterface() != null) {
        builder.setNextHopInterface(huaweiRoute.getNextHopInterface());
      }

      // Set administrative distance (preference)
      builder.setAdministrativeCost(huaweiRoute.getPreference());

      // Build the route
      StaticRoute route = builder.build();

      // Add to appropriate VRF
      String vrfName = huaweiRoute.getVrfName();
      if (vrfName == null) {
        vrfName = DEFAULT_VRF_NAME;
      }

      // Get or create VRF
      Vrf vrf = c.getVrfs().get(vrfName);
      if (vrf == null) {
        vrf = Vrf.builder().setName(vrfName).build();
        c.getVrfs().put(vrfName, vrf);
      }

      // Add static route to VRF
      vrf.getStaticRoutes().add(route);
    }
  }

  /**
   * Converts Huawei ACLs to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationAcls(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    for (HuaweiAcl huaweiAcl : huaweiCfg.getAcls().values()) {
      IpAccessList ipAccessList = toIpAccessList(huaweiAcl);
      c.getIpAccessLists().put(ipAccessList.getName(), ipAccessList);
    }
  }

  /**
   * Converts a Huawei ACL to a Batfish IpAccessList.
   *
   * @param huaweiAcl The Huawei ACL to convert
   * @return A Batfish IpAccessList
   */
  private static @Nonnull IpAccessList toIpAccessList(@Nonnull HuaweiAcl huaweiAcl) {
    // Convert each ACL line to ExprAclLine
    ImmutableList.Builder<AclLine> linesBuilder = ImmutableList.builder();
    for (HuaweiAclLine huaweiLine : huaweiAcl.getLines()) {
      ExprAclLine line = toAclLine(huaweiLine);
      if (line != null) {
        linesBuilder.add(line);
      }
    }

    return IpAccessList.builder()
        .setName(huaweiAcl.getName())
        .setLines(linesBuilder.build())
        .setSourceName(huaweiAcl.getName())
        .setSourceType("Huawei ACL")
        .build();
  }

  /**
   * Converts a Huawei ACL line to a Batfish ExprAclLine.
   *
   * @param huaweiLine The Huawei ACL line to convert
   * @return A Batfish ExprAclLine, or null if conversion fails
   */
  private static @Nullable ExprAclLine toAclLine(@Nonnull HuaweiAclLine huaweiLine) {
    // Convert action (permit/deny)
    LineAction action;
    String actionStr = huaweiLine.getAction().toLowerCase();
    if ("permit".equals(actionStr)) {
      action = LineAction.PERMIT;
    } else if ("deny".equals(actionStr)) {
      action = LineAction.DENY;
    } else {
      // Unknown action, skip this line
      return null;
    }

    // Build match conditions
    ImmutableList.Builder<AclLineMatchExpr> matchConditions = ImmutableList.builder();

    // Match protocol if specified
    if (huaweiLine.getProtocol() != null) {
      IpProtocol ipProtocol = toIpProtocol(huaweiLine.getProtocol());
      if (ipProtocol != null) {
        matchConditions.add(matchIpProtocol(ipProtocol));
      }
    }

    // Build HeaderSpace for IP addresses and ports
    HeaderSpace.Builder headerSpaceBuilder = HeaderSpace.builder();

    // Source IP
    if (huaweiLine.getSource() != null) {
      Prefix srcPrefix = parsePrefix(huaweiLine.getSource());
      if (srcPrefix != null) {
        headerSpaceBuilder.setSrcIps(srcPrefix.toIpSpace());
      }
    }

    // Destination IP
    if (huaweiLine.getDestination() != null) {
      Prefix dstPrefix = parsePrefix(huaweiLine.getDestination());
      if (dstPrefix != null) {
        headerSpaceBuilder.setDstIps(dstPrefix.toIpSpace());
      }
    }

    // Source port - convert to SubRange
    if (huaweiLine.getSourcePort() != null) {
      SubRange srcPortRange = parsePortSpecToSubRange(huaweiLine.getSourcePort());
      if (srcPortRange != null) {
        headerSpaceBuilder.setSrcPorts(srcPortRange);
      }
    }

    // Destination port - convert to SubRange
    if (huaweiLine.getDestinationPort() != null) {
      SubRange dstPortRange = parsePortSpecToSubRange(huaweiLine.getDestinationPort());
      if (dstPortRange != null) {
        headerSpaceBuilder.setDstPorts(dstPortRange);
      }
    }

    // If we have IP/port conditions, wrap in MatchHeaderSpace
    AclLineMatchExpr matchCondition;
    List<AclLineMatchExpr> conditions = matchConditions.build();

    if (headerSpaceBuilder.build().getSrcIps() != null
        || headerSpaceBuilder.build().getDstIps() != null
        || headerSpaceBuilder.build().getSrcPorts() != null
        || headerSpaceBuilder.build().getDstPorts() != null) {
      // Add HeaderSpace to conditions
      matchConditions.add(new MatchHeaderSpace(headerSpaceBuilder.build()));
    }

    // Combine all conditions with AND
    if (conditions.isEmpty()) {
      // No specific conditions, match all
      matchCondition = org.batfish.datamodel.acl.TrueExpr.INSTANCE;
    } else if (conditions.size() == 1) {
      matchCondition = conditions.get(0);
    } else {
      matchCondition = and(conditions);
    }

    // Build the ExprAclLine
    return ExprAclLine.builder()
        .setAction(action)
        .setMatchCondition(matchCondition)
        .setName(String.valueOf(huaweiLine.getSequenceNumber()))
        .build();
  }

  /**
   * Parses a Huawei ACL prefix string (e.g., "192.168.1.0 0.0.0.255" or "10.1.1.0/24").
   *
   * @param prefixStr The prefix string to parse
   * @return A Prefix object, or null if parsing fails
   */
  private static @Nullable Prefix parsePrefix(@Nonnull String prefixStr) {
    try {
      // Try CIDR notation first (e.g., "10.1.1.0/24")
      if (prefixStr.contains("/")) {
        String[] parts = prefixStr.split("/");
        if (parts.length == 2) {
          Ip addr = Ip.parse(parts[0]);
          int prefixLen = Integer.parseInt(parts[1]);
          return Prefix.create(addr, prefixLen);
        }
      }

      // Try wildcard mask notation (e.g., "192.168.1.0 0.0.0.255")
      String[] parts = prefixStr.trim().split("\\s+");
      if (parts.length == 2) {
        Ip addr = Ip.parse(parts[0]);
        Ip wildcard = Ip.parse(parts[1]);
        // Convert wildcard mask to prefix length
        long wildcardLong = wildcard.asLong() & 0xFFFFFFFFL;
        int prefixLen = 32 - Long.bitCount(wildcardLong);
        return Prefix.create(addr, prefixLen);
      }

      // Try simple IP address
      return Prefix.create(Ip.parse(prefixStr), 32);
    } catch (Exception e) {
      // Failed to parse, return null
      return null;
    }
  }

  /**
   * Parses a Huawei port specification (e.g., "eq 80", "range 100 200", "gt 1023") to a SubRange.
   *
   * @param portSpec The port specification string
   * @return A SubRange representing the ports, or null if parsing fails
   */
  private static @Nullable SubRange parsePortSpecToSubRange(@Nonnull String portSpec) {
    try {
      String spec = portSpec.trim().toLowerCase();

      if (spec.startsWith("eq ")) {
        // Equal to a specific port
        int port = Integer.parseInt(spec.substring(3).trim());
        return new SubRange(port, port);
      } else if (spec.startsWith("gt ")) {
        // Greater than a port
        int port = Integer.parseInt(spec.substring(3).trim()) + 1;
        return new SubRange(port, 65535);
      } else if (spec.startsWith("lt ")) {
        // Less than a port
        int port = Integer.parseInt(spec.substring(3).trim()) - 1;
        return new SubRange(0, port);
      } else if (spec.startsWith("range ")) {
        // Range of ports
        String[] rangeParts = spec.substring(6).trim().split("\\s+");
        if (rangeParts.length == 2) {
          int start = Integer.parseInt(rangeParts[0]);
          int end = Integer.parseInt(rangeParts[1]);
          return new SubRange(start, end);
        }
        // Invalid range format, return all ports
        return new SubRange(0, 65535);
      } else if (spec.startsWith("neq ")) {
        // Not equal to a port - this doesn't map well to SubRange
        // Return all ports as fallback
        return new SubRange(0, 65535);
      } else {
        // Try parsing as a single port number
        int port = Integer.parseInt(spec);
        return new SubRange(port, port);
      }
    } catch (Exception e) {
      // If parsing fails, return all ports
      return new SubRange(0, 65535);
    }
  }

  /**
   * Converts a Huawei protocol string to Batfish IpProtocol.
   *
   * @param protocol The protocol string (e.g., "tcp", "udp", "icmp", "ip")
   * @return An IpProtocol object, or null if unknown
   */
  private static @Nullable IpProtocol toIpProtocol(@Nonnull String protocol) {
    String proto = protocol.toLowerCase().trim();
    switch (proto) {
      case "tcp":
        return IpProtocol.TCP;
      case "udp":
        return IpProtocol.UDP;
      case "icmp":
        return IpProtocol.ICMP;
      case "ip":
      case "":
        return null; // IP protocol means "any", so return null to match all
      case "gre":
        return IpProtocol.GRE;
      case "ospf":
        return IpProtocol.OSPF;
      case "pim":
        return IpProtocol.PIM;
      case "sctp":
        return IpProtocol.SCTP;
      case "ah":
        // AH is not a standard constant in IpProtocol, use number 51
        return IpProtocol.fromNumber(51);
      default:
        // Try to parse as protocol number
        try {
          int protoNum = Integer.parseInt(proto);
          if (protoNum >= 0 && protoNum <= 255) {
            return IpProtocol.fromNumber(protoNum);
          }
        } catch (NumberFormatException e) {
          // Not a number, return null
        }
        return null;
    }
  }

  /**
   * Converts Huawei NAT rules to Batfish vendor-independent format.
   *
   * <p>Converts Huawei NAT rules including static NAT, dynamic NAT, Easy IP, and NAT server
   * configurations.
   *
   * <p>Note: Batfish doesn't have a generic NAT model in the vendor-independent Configuration
   * class. NAT rules are kept in their original form in the HuaweiConfiguration for reference. In
   * the future, specific NAT rules could be converted to generic Batfish structures (e.g., service
   * forwarding rules for NAT Server).
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationNat(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    if (huaweiCfg.getNatRules().isEmpty()) {
      return;
    }

    // Indicate that NAT is configured in the vendor family
    HuaweiFamily huaweiFamily = c.getVendorFamily().getHuawei();
    if (huaweiFamily == null) {
      huaweiFamily = new HuaweiFamily();
      c.getVendorFamily().setHuawei(huaweiFamily);
    }

    // TODO: In the future, consider converting some NAT rules to generic Batfish structures:
    // - Static NAT: Could be represented as static IP mappings in vendor-independent format
    // - NAT Server (port forwarding): Could be represented as service forwarding rules
    // - For now, NAT rules are preserved in their original Huawei-specific representation
    // and can be accessed via huaweiCfg.getNatRules()
  }

  /**
   * Converts Huawei VRF configurations to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationVrfs(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    for (HuaweiVrf huaweiVrf : huaweiCfg.getVrfs().values()) {
      String vrfName = huaweiVrf.getName();

      // Skip default VRF as it's already created
      if (DEFAULT_VRF_NAME.equals(vrfName)) {
        continue;
      }

      // Create VRF builder
      Vrf.Builder vrfBuilder = Vrf.builder().setName(vrfName);

      // Set route distinguisher if present
      // TODO: Parse and convert RD string to RouteDistinguisher object
      // if (huaweiVrf.getRouteDistinguisher() != null) {
      //   RouteDistinguisher rd = RouteDistinguisher.parse(huaweiVrf.getRouteDistinguisher());
      //   vrfBuilder.setRouteDistinguisher(rd);
      // }

      // Build the VRF
      Vrf vrf = vrfBuilder.build();

      // Add to configuration
      c.getVrfs().put(vrfName, vrf);
    }
  }

  /**
   * Converts a Huawei OSPF area to Batfish OspfArea.
   *
   * @param huaweiArea The Huawei OSPF area
   * @param c The Batfish Configuration
   * @return A Batfish OspfArea
   */
  @SuppressWarnings("unused") // Configuration parameter will be used in future implementation
  private static @Nonnull OspfArea toOspfArea(
      @Nonnull HuaweiOspfProcess.HuaweiOspfArea huaweiArea, @Nonnull Configuration c) {

    OspfArea.Builder builder = OspfArea.builder();

    // Set area number
    builder.setNumber(huaweiArea.getAreaId());

    // Convert area type
    switch (huaweiArea.getAreaType()) {
      case STUB:
        // For stub area, use StubSettings with suppressType3 based on configuration
        if (huaweiArea.isNoSummary()) {
          // Totally stubby area - suppress Type 3 summary LSAs
          builder.setStub(
              org.batfish.datamodel.ospf.StubSettings.builder().setSuppressType3(true).build());
        } else {
          // Regular stub area - allow Type 3 summary LSAs
          builder.setStub(
              org.batfish.datamodel.ospf.StubSettings.builder().setSuppressType3(false).build());
        }
        break;
      case NSSA:
        // For NSSA area, use NssaSettings with suppressType3 and default originate
        org.batfish.datamodel.ospf.NssaSettings.Builder nssaBuilder =
            org.batfish.datamodel.ospf.NssaSettings.builder();
        if (huaweiArea.isNoSummary()) {
          // Totally stubby NSSA - suppress Type 3 summary LSAs
          nssaBuilder.setSuppressType3(true);
        }
        if (huaweiArea.isNoRedistribute()) {
          // Suppress Type 7 LSAs from being redistributed
          nssaBuilder.setSuppressType7(true);
        }
        // Set default originate type for NSSA
        // Huawei's default-information-originate in NSSA translates to INTER_AREA
        if (huaweiArea.isDefaultOriginate()) {
          nssaBuilder.setDefaultOriginateType(
              org.batfish.datamodel.ospf.OspfDefaultOriginateType.INTER_AREA);
        } else {
          nssaBuilder.setDefaultOriginateType(
              org.batfish.datamodel.ospf.OspfDefaultOriginateType.NONE);
        }
        builder.setNssa(nssaBuilder.build());
        break;
      case NORMAL:
        // Normal area - no special settings
        break;
    }

    // Convert area ranges (abr-summary) to OspfAreaSummary
    if (!huaweiArea.getAreaRanges().isEmpty()) {
      for (HuaweiOspfProcess.HuaweiOspfAreaRange huaweiRange :
          huaweiArea.getAreaRanges().values()) {
        // Determine the summary route behavior
        OspfAreaSummary.SummaryRouteBehavior behavior;
        if (huaweiRange.isAdvertise()) {
          behavior = OspfAreaSummary.SummaryRouteBehavior.ADVERTISE_AND_INSTALL_DISCARD;
        } else {
          behavior = OspfAreaSummary.SummaryRouteBehavior.NOT_ADVERTISE_AND_NO_DISCARD;
        }

        // Create OspfAreaSummary with optional cost
        OspfAreaSummary summary = new OspfAreaSummary(behavior, huaweiRange.getCost());

        // Add to the area
        builder.addSummary(huaweiRange.getPrefix(), summary);
      }
    }

    // TODO: Convert area authentication - extracted but not converted to Batfish model
    // Area authentication extraction is implemented in grammar as state 3 (in grammar, not
    // implemented)
    // TODO: Convert OSPF virtual links - Batfish datamodel does not support virtual links
    // Virtual link extraction is implemented but conversion cannot be completed without datamodel
    // support. This is tracked in the parsing documentation as state 3.

    return builder.build();
  }

  /**
   * Converts a Huawei OSPF network type string to Batfish OspfNetworkType enum.
   *
   * @param networkType The Huawei network type string (e.g., "BROADCAST", "P2P", "NBMA", "P2MP")
   * @return The corresponding OspfNetworkType, or null if unknown or null
   */
  private static @Nullable OspfNetworkType toOspfNetworkType(@Nullable String networkType) {
    if (networkType == null) {
      // Default for Ethernet interfaces is BROADCAST
      return OspfNetworkType.BROADCAST;
    }
    switch (networkType.toUpperCase()) {
      case "BROADCAST":
        return OspfNetworkType.BROADCAST;
      case "P2P":
      case "POINT-TO-POINT":
        return OspfNetworkType.POINT_TO_POINT;
      case "NBMA":
        return OspfNetworkType.NON_BROADCAST_MULTI_ACCESS;
      case "P2MP":
      case "POINT-TO-MULTIPOINT":
        return OspfNetworkType.POINT_TO_MULTIPOINT;
      default:
        // Unknown network type, return null to use default
        return null;
    }
  }

  /**
   * Applies peer group settings to a BGP peer configuration.
   *
   * <p>This method implements inheritance of settings from a peer group to an individual peer. The
   * peer's own settings take precedence over peer group settings.
   *
   * @param peer The peer configuration to modify
   * @param groupName The name of the peer group to inherit from
   * @param huaweiBgp The Huawei BGP process containing peer groups
   * @return A new BgpActivePeerConfig with peer group settings applied
   */
  private static @Nonnull BgpActivePeerConfig applyPeerGroupSettings(
      @Nonnull BgpActivePeerConfig peer,
      @Nonnull String groupName,
      @Nonnull HuaweiBgpProcess huaweiBgp) {
    HuaweiBgpProcess.HuaweiBgpPeerGroup peerGroup = huaweiBgp.getPeerGroups().get(groupName);
    if (peerGroup == null) {
      // Peer group not found, return original peer
      return peer;
    }

    // Build a new peer config with inherited settings
    BgpActivePeerConfig.Builder builder = BgpActivePeerConfig.builder();

    // Peer's own settings take precedence
    builder.setPeerAddress(peer.getPeerAddress());

    // Remote AS: use peer's AS if set, otherwise inherit from peer group
    if (peer.getRemoteAsns() != null && !peer.getRemoteAsns().isEmpty()) {
      builder.setRemoteAsns(peer.getRemoteAsns());
    } else if (peerGroup.getRemoteAs() != null) {
      builder.setRemoteAsns(LongSpace.of(peerGroup.getRemoteAs()));
    }

    // Group name (already set)
    builder.setGroup(groupName);

    // Description: peer's description takes precedence
    builder.setDescription(peer.getDescription());

    // Local AS: use peer's local AS if set, otherwise inherit from peer group
    if (peer.getLocalAs() != null) {
      builder.setLocalAs(peer.getLocalAs());
    } else if (peerGroup.getLocalAs() != null) {
      builder.setLocalAs(peerGroup.getLocalAs().longValue());
    }

    // Password: use peer's authentication settings if set, otherwise inherit from peer group
    BgpAuthenticationSettings authSettings = peer.getAuthenticationSettings();
    if (authSettings == null && peerGroup.getPassword() != null) {
      // Inherit password from peer group
      authSettings = toBgpAuthenticationSettings(peerGroup.getPassword());
    }
    // Keep peer's existing authentication settings if already set
    if (authSettings != null) {
      builder.setAuthenticationSettings(authSettings);
    } else if (peer.getAuthenticationSettings() != null) {
      builder.setAuthenticationSettings(peer.getAuthenticationSettings());
    }

    // Cluster ID for route reflector: use peer's cluster ID if set, otherwise inherit
    if (peer.getClusterId() != null) {
      builder.setClusterId(peer.getClusterId());
    } else if (peerGroup.getClusterId() != null) {
      try {
        // Parse cluster ID as IP address, convert to Long
        Ip clusterIp = Ip.parse(peerGroup.getClusterId());
        builder.setClusterId(clusterIp.asLong());
      } catch (Exception e) {
        // Invalid cluster ID format, skip
      }
    }

    // Route reflector client setting
    // If peer group is a route reflector client, enable it
    // Note: This would need to be set in the address family configuration
    // For now, we inherit the cluster ID which enables route reflection

    // Route policies: peer's policies take precedence, but for IPv4 unicast
    // we would need to set them in the Ipv4UnicastAddressFamily
    // This is a simplified version that preserves the peer's existing AF config
    org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily.Builder ipv4AfBuilder =
        org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily.builder();

    // Set route reflector client status from peer or peer group
    boolean isRouteReflectorClient = false;
    if (peer.getIpv4UnicastAddressFamily() != null
        && peer.getIpv4UnicastAddressFamily().getRouteReflectorClient()) {
      isRouteReflectorClient = true;
    } else if (peerGroup.getRouteReflectorClient() != null) {
      isRouteReflectorClient = peerGroup.getRouteReflectorClient();
    }

    // Import/export policies from peer
    String importPolicy = null;
    String exportPolicy = null;
    if (peer.getIpv4UnicastAddressFamily() != null) {
      importPolicy = peer.getIpv4UnicastAddressFamily().getImportPolicy();
      exportPolicy = peer.getIpv4UnicastAddressFamily().getExportPolicy();
    }

    // Set route policies from peer group if not set on peer
    if (importPolicy == null && peerGroup.getRoutePolicyIn() != null) {
      importPolicy = peerGroup.getRoutePolicyIn();
    }
    if (exportPolicy == null && peerGroup.getRoutePolicyOut() != null) {
      exportPolicy = peerGroup.getRoutePolicyOut();
    }

    ipv4AfBuilder
        .setRouteReflectorClient(isRouteReflectorClient)
        .setImportPolicy(importPolicy)
        .setExportPolicy(exportPolicy);

    // Build the new peer config
    BgpActivePeerConfig newPeer =
        builder.setIpv4UnicastAddressFamily(ipv4AfBuilder.build()).build();

    return newPeer;
  }

  /**
   * Converts Huawei BGP network announcements to Batfish vendor-independent format.
   *
   * <p>Network statements in Huawei BGP specify which networks to advertise into BGP. The syntax
   * is: network <ip> <mask> [route-policy <policy>]
   *
   * <p>This conversion:
   *
   * <ul>
   *   <li>Adds networks to the BGP process's origination space
   *   <li>Adds networks as unconditional network statements (advertised regardless of RIB presence)
   *   <li>Creates a main RIB independent network policy that matches the configured networks
   * </ul>
   *
   * @param c The Batfish Configuration
   * @param vrfName The VRF name
   * @param bgpProcess The BgpProcess to update
   * @param huaweiBgp The Huawei BGP process containing network announcements
   */
  private static void convertBgpNetworks(
      Configuration c, String vrfName, BgpProcess bgpProcess, HuaweiBgpProcess huaweiBgp) {
    // Add each network to origination space and as unconditional statement
    for (HuaweiBgpProcess.HuaweiBgpNetwork network : huaweiBgp.getNetworks()) {
      Prefix prefix = network.getNetwork();
      if (prefix != null) {
        bgpProcess.addToOriginationSpace(prefix);
        bgpProcess.addUnconditionalNetworkStatements(prefix);
      }
    }

    // Create main RIB independent network policy
    // This policy will permit routes matching the configured network prefixes
    String networkPolicyName = generatedBgpMainRibIndependentNetworkPolicyName(vrfName);
    RoutingPolicy.Builder policyBuilder =
        RoutingPolicy.builder().setOwner(c).setName(networkPolicyName);

    // Build disjunction of all network prefixes
    ImmutableList.Builder<BooleanExpr> networkDisjuncts = ImmutableList.builder();
    for (HuaweiBgpProcess.HuaweiBgpNetwork network : huaweiBgp.getNetworks()) {
      Prefix prefix = network.getNetwork();
      if (prefix != null) {
        ImmutableList.Builder<BooleanExpr> matchConditions = ImmutableList.builder();

        // Match the network prefix
        matchConditions.add(
            new MatchPrefixSet(
                DestinationNetwork.instance(),
                new ExplicitPrefixSet(
                    new PrefixSpace(org.batfish.datamodel.PrefixRange.fromPrefix(prefix)))));

        // Exclude BGP/IBGP/AGGREGATE routes (only originate non-BGP routes)
        matchConditions.add(
            new Not(
                new MatchProtocol(
                    RoutingProtocol.BGP, RoutingProtocol.IBGP, RoutingProtocol.AGGREGATE)));

        // If route-policy is specified, call it
        String routePolicy = network.getRoutePolicy();
        if (routePolicy != null) {
          matchConditions.add(new org.batfish.datamodel.routing_policy.expr.CallExpr(routePolicy));
        }

        networkDisjuncts.add(new Conjunction(matchConditions.build()));
      }
    }

    // Add the policy statement
    BooleanExpr matchExpr = new Disjunction(networkDisjuncts.build());
    policyBuilder.addStatement(
        new If(
            "Add Huawei BGP network statement routes to BGP",
            matchExpr,
            ImmutableList.of(
                new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                Statements.ExitAccept.toStaticStatement())));

    // Reject all other routes
    policyBuilder.addStatement(Statements.ExitReject.toStaticStatement());

    // Build and register the policy
    RoutingPolicy networkPolicy = policyBuilder.build();
    c.getRoutingPolicies().put(networkPolicyName, networkPolicy);

    // Set the policy on the BGP process
    bgpProcess.setMainRibIndependentNetworkPolicy(networkPolicyName);
  }

  /**
   * Converts Huawei BGP import-route (redistribution) configurations to Batfish vendor-independent
   * format.
   *
   * <p>Import-route statements in Huawei BGP redistribute routes from other protocols into BGP. The
   * syntax is: import-route <protocol> [route-policy <policy>]
   *
   * <p>Supported protocols include: direct, static, rip, ospf, isis, bgp
   *
   * <p>This conversion:
   *
   * <ul>
   *   <li>Creates a redistribution policy that matches routes from the specified protocols
   *   <li>Applies route-policy filter if configured
   *   <li>Sets the redistribution policy on the BGP process
   * </ul>
   *
   * @param c The Batfish Configuration
   * @param vrfName The VRF name
   * @param bgpProcess The BgpProcess to update
   * @param huaweiBgp The Huawei BGP process containing import-route configurations
   */
  private static void convertBgpImportRoutes(
      Configuration c, String vrfName, BgpProcess bgpProcess, HuaweiBgpProcess huaweiBgp) {
    // Create redistribution policy name
    String redistributionPolicyName = "~BGP_REDISTRIBUTE_POLICY~" + vrfName + "~";
    RoutingPolicy.Builder policyBuilder =
        RoutingPolicy.builder().setOwner(c).setName(redistributionPolicyName);

    // Build disjunction of all import-route (redistribution) configurations
    ImmutableList.Builder<BooleanExpr> redistributionDisjuncts = ImmutableList.builder();
    for (HuaweiBgpProcess.HuaweiBgpImportRoute importRoute : huaweiBgp.getImportRoutes()) {
      String protocol = importRoute.getProtocol().toLowerCase();

      // Map Huawei protocol name to Batfish RoutingProtocol
      RoutingProtocol routingProtocol = mapHuaweiProtocolToRoutingProtocol(protocol);
      if (routingProtocol == null) {
        // Unknown protocol, skip with a warning (could log warning here)
        continue;
      }

      ImmutableList.Builder<BooleanExpr> matchConditions = ImmutableList.builder();

      // Match the protocol
      matchConditions.add(new MatchProtocol(routingProtocol));

      // If route-policy is specified, call it
      String routePolicy = importRoute.getRoutePolicy();
      if (routePolicy != null) {
        matchConditions.add(new org.batfish.datamodel.routing_policy.expr.CallExpr(routePolicy));
      }

      redistributionDisjuncts.add(new Conjunction(matchConditions.build()));
    }

    // Add the redistribution policy statement
    BooleanExpr matchExpr = new Disjunction(redistributionDisjuncts.build());
    policyBuilder.addStatement(
        new If(
            "Redistribute routes from other protocols into BGP",
            matchExpr,
            ImmutableList.of(
                new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                Statements.ExitAccept.toStaticStatement())));

    // Reject all other routes
    policyBuilder.addStatement(Statements.ExitReject.toStaticStatement());

    // Build and register the policy
    RoutingPolicy redistributionPolicy = policyBuilder.build();
    c.getRoutingPolicies().put(redistributionPolicyName, redistributionPolicy);

    // Set the redistribution policy on the BGP process
    bgpProcess.setRedistributionPolicy(redistributionPolicyName);
  }

  /**
   * Maps a Huawei protocol name (from import-route) to a Batfish RoutingProtocol enum.
   *
   * <p>Huawei BGP import-route supports the following protocols:
   *
   * <ul>
   *   <li>direct - Direct routes (connected interfaces)
   *   <li>static - Static routes
   *   <li>rip - RIP routes
   *   <li>ospf - OSPF routes
   *   <li>isis - ISIS routes
   *   <li>bgp - BGP routes (used for route reflection)
   * </ul>
   *
   * @param protocol The Huawei protocol name
   * @return The corresponding Batfish RoutingProtocol, or null if unknown
   */
  private static @Nullable RoutingProtocol mapHuaweiProtocolToRoutingProtocol(String protocol) {
    switch (protocol.toLowerCase()) {
      case "direct":
      case "connected":
        return RoutingProtocol.CONNECTED;
      case "static":
        return RoutingProtocol.STATIC;
      case "rip":
        return RoutingProtocol.RIP;
      case "ospf":
        return RoutingProtocol.OSPF;
      case "isis":
        return RoutingProtocol.ISIS_ANY;
      case "bgp":
        return RoutingProtocol.BGP;
      default:
        return null; // Unknown protocol
    }
  }

  /**
   * Converts a Huawei BGP password string to Batfish BgpAuthenticationSettings.
   *
   * <p>Huawei BGP uses MD5 authentication for BGP peers. The password is configured using the "peer
   * x.x.x.x password cipher" command.
   *
   * <p>This method creates a BgpAuthenticationSettings object with:
   *
   * <ul>
   *   <li>Algorithm: TCP_SIGNATURE_MD5 (MD5 authentication used by Huawei)
   *   <li>Key: The provided password string
   * </ul>
   *
   * @param password The password string from Huawei configuration
   * @return A BgpAuthenticationSettings object, or null if password is null or empty
   */
  private static @Nullable BgpAuthenticationSettings toBgpAuthenticationSettings(
      @Nullable String password) {
    if (password == null || password.isEmpty()) {
      return null;
    }
    BgpAuthenticationSettings authSettings = new BgpAuthenticationSettings();
    authSettings.setAuthenticationAlgorithm(BgpAuthenticationAlgorithm.TCP_SIGNATURE_MD5);
    authSettings.setAuthenticationKey(password);
    return authSettings;
  }

  /**
   * Computes the OSPF export policy name for a given OSPF process.
   *
   * @param ospfProcessId The OSPF process ID
   * @return The policy name
   */
  private static String computeOspfExportPolicyName(String ospfProcessId) {
    return String.format("~OSPF_EXPORT_POLICY:%s~", ospfProcessId);
  }

  /**
   * Converts a Huawei OSPF redistribution policy to a Batfish If statement.
   *
   * <p>OSPF redistribution in Huawei uses the "import-route" command to redistribute routes from
   * other protocols into OSPF. The syntax is: import-route <protocol> [cost <value>] [tag <value>]
   * [route-policy <policy>]
   *
   * <p>This conversion:
   *
   * <ul>
   *   <li>Creates a match condition for the source protocol
   *   <li>Applies route-policy filter if configured
   *   <li>Sets metric/cost if configured
   *   <li>Accepts matching routes for redistribution
   * </ul>
   *
   * @param policy The Huawei OSPF redistribution policy
   * @param huaweiCfg The Huawei configuration (for warnings)
   * @return An If statement representing the redistribution policy, or null if conversion fails
   */
  private static @Nullable If convertOspfRedistributionPolicy(
      HuaweiOspfProcess.HuaweiOspfRedistributionPolicy policy, HuaweiConfiguration huaweiCfg) {
    HuaweiOspfProcess.HuaweiRedistributionProtocol sourceProtocol = policy.getSourceProtocol();

    // Map Huawei protocol to Batfish RoutingProtocol(s)
    Set<RoutingProtocol> routingProtocols = mapHuaweiOspfRedistributionProtocol(sourceProtocol);
    if (routingProtocols.isEmpty()) {
      // Unknown protocol - skip this redistribution
      return null;
    }

    // Build match conditions
    Conjunction ospfExportConditions = new Conjunction();

    // Match the protocol(s)
    ospfExportConditions.getConjuncts().add(new MatchProtocol(routingProtocols));

    // If a route-policy filter is present, honor it
    String exportRouteMapName = policy.getRoutePolicy();
    if (exportRouteMapName != null) {
      ospfExportConditions.getConjuncts().add(new CallExpr(exportRouteMapName));
    }

    // Build export statements
    ImmutableList.Builder<Statement> ospfExportStatements = ImmutableList.builder();

    // Set metric if configured
    if (policy.getCost() != null) {
      ospfExportStatements.add(new SetMetric(new LiteralLong(policy.getCost())));
    }

    // Accept the route
    ospfExportStatements.add(Statements.ExitAccept.toStaticStatement());

    // Construct the policy and return
    return new If(
        "OSPF export routes for " + sourceProtocol.name(),
        ospfExportConditions,
        ospfExportStatements.build(),
        ImmutableList.of());
  }

  /**
   * Maps a Huawei OSPF redistribution protocol to Batfish RoutingProtocol set.
   *
   * <p>Huawei OSPF import-route supports the following protocols:
   *
   * <ul>
   *   <li>direct - Direct routes (connected interfaces)
   *   <li>static - Static routes
   *   <li>bgp - BGP routes (both iBGP and eBGP)
   *   <li>rip - RIP routes
   *   <li>isis - IS-IS routes (all levels)
   *   <li>ospf - Other OSPF processes
   *   <li>unr - User network routes
   * </ul>
   *
   * @param protocol The Huawei redistribution protocol
   * @return A set of Batfish RoutingProtocols corresponding to the Huawei protocol
   */
  private static Set<RoutingProtocol> mapHuaweiOspfRedistributionProtocol(
      HuaweiOspfProcess.HuaweiRedistributionProtocol protocol) {
    switch (protocol) {
      case DIRECT:
        return ImmutableSet.of(RoutingProtocol.CONNECTED);
      case STATIC:
        return ImmutableSet.of(RoutingProtocol.STATIC);
      case BGP:
        return ImmutableSet.of(RoutingProtocol.BGP, RoutingProtocol.IBGP);
      case RIP:
        return ImmutableSet.of(RoutingProtocol.RIP);
      case ISIS:
        return ImmutableSet.of(
            RoutingProtocol.ISIS_ANY,
            RoutingProtocol.ISIS_EL1,
            RoutingProtocol.ISIS_EL2,
            RoutingProtocol.ISIS_L1,
            RoutingProtocol.ISIS_L2);
      case OSPF:
        // Redistributing from one OSPF process to another
        return ImmutableSet.of(
            RoutingProtocol.OSPF,
            RoutingProtocol.OSPF_E1,
            RoutingProtocol.OSPF_E2,
            RoutingProtocol.OSPF_IA);
      case UNR:
        // User Network Routes - map to STATIC as approximation
        // UNR is a Huawei-specific feature for user-defined routes
        return ImmutableSet.of(RoutingProtocol.STATIC);
      default:
        return ImmutableSet.of();
    }
  }

  /**
   * Converts Huawei route-policies to Batfish vendor-independent routing policies.
   *
   * @param c The Batfish Configuration object
   * @param huaweiConfig The Huawei configuration containing route-policies
   */
  private static void toConfigurationRoutePolicies(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiConfig) {
    for (HuaweiRoutePolicy huaweiPolicy : huaweiConfig.getRoutePolicies().values()) {
      RoutingPolicy routingPolicy = toRoutingPolicy(huaweiPolicy, c);
      c.getRoutingPolicies().put(routingPolicy.getName(), routingPolicy);
    }
  }

  /**
   * Converts a Huawei route-policy to a Batfish vendor-independent routing policy.
   *
   * @param huaweiPolicy The Huawei route-policy to convert
   * @param c The Batfish Configuration object
   * @return A Batfish RoutingPolicy object
   */
  private static RoutingPolicy toRoutingPolicy(
      @Nonnull HuaweiRoutePolicy huaweiPolicy, @Nonnull Configuration c) {
    ImmutableList.Builder<Statement> statements = ImmutableList.builder();

    // Convert each node (statement) in the route-policy
    for (HuaweiRoutePolicy.HuaweiRoutePolicyNode node : huaweiPolicy.getNodes()) {
      ImmutableList.Builder<Statement> nodeStatements = ImmutableList.builder();

      // Build match conditions (if-match clauses)
      BooleanExpr matchCondition = BooleanExprs.TRUE;

      // Match IP prefix list: if-match ip-prefix <prefix-list-name>
      if (node.getMatchConditions().getIpPrefix() != null) {
        String prefixList = node.getMatchConditions().getIpPrefix();
        // Create a MatchPrefixSet expression using NamedPrefixSet
        matchCondition =
            new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(prefixList));
      }

      // Add set actions (apply clauses)
      HuaweiRoutePolicy.HuaweiRoutePolicySetActions setActions = node.getSetActions();

      // Set local preference
      if (setActions.getLocalPreference() != null) {
        nodeStatements.add(
            new SetLocalPreference(new LiteralLong(setActions.getLocalPreference())));
      }

      // Set tag
      if (setActions.getTag() != null) {
        nodeStatements.add(new SetTag(new LiteralLong(setActions.getTag())));
      }

      // Set metric/cost
      if (setActions.getCost() != null) {
        nodeStatements.add(new SetMetric(new LiteralLong(setActions.getCost())));
      }

      // Set preference (used in some protocols)
      if (setActions.getPreference() != null) {
        nodeStatements.add(new SetMetric(new LiteralLong((long) setActions.getPreference())));
      }

      // Determine the action (permit or deny)
      // Permit: accept the route, Deny: reject the route
      if (node.getAction() == HuaweiRoutePolicy.HuaweiRoutePolicyNode.Action.PERMIT) {
        // Add true branch that accepts
        nodeStatements.add(Statements.ExitAccept.toStaticStatement());
        // Add false branch that falls through (to next node)
        nodeStatements.add(Statements.ReturnFalse.toStaticStatement());
      } else {
        // DENY action: reject the route
        nodeStatements.add(Statements.ExitReject.toStaticStatement());
      }

      // Wrap in an If statement with the match condition
      // If the match condition is true, execute the node statements
      // Otherwise, fall through to the next node
      statements.add(
          new If(
              matchCondition,
              nodeStatements.build(),
              ImmutableList.of(Statements.ReturnFalse.toStaticStatement())));
    }

    // Build the routing policy
    return RoutingPolicy.builder()
        .setName(huaweiPolicy.getName())
        .setOwner(c)
        .setStatements(statements.build())
        .build();
  }

  private HuaweiConversions() {
    // Prevent instantiation
  }
}

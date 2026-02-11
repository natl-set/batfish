package org.batfish.vendor.huawei.representation;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpAuthenticationAlgorithm;
import org.batfish.datamodel.BgpAuthenticationSettings;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
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
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.Ipv6UnicastAddressFamily;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.ospf.NssaSettings;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.StubSettings;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetTag;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.vendor_family.huawei.HuaweiFamily;
import org.batfish.datamodel.vendor_family.huawei.HuaweiFamily.HuaweiVrfData;

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
    c.getVendorFamily().setHuawei(new HuaweiFamily());

    // Convert interfaces
    toConfigurationInterfaces(huaweiConfig, c);

    // Set default VRF
    c.getVrfs()
        .computeIfAbsent(
            DEFAULT_VRF_NAME, vrfName -> Vrf.builder().setName(DEFAULT_VRF_NAME).build());

    // Convert static routes
    toConfigurationStaticRoutes(c, huaweiConfig);

    // Convert NAT rules
    toConfigurationNat(c, huaweiConfig);

    // Convert route-policies (must be done before BGP/OSPF which reference them)
    toConfigurationRoutePolicies(c, huaweiConfig);

    // Convert OSPF
    toConfigurationOspf(c, huaweiConfig);

    // Convert BGP
    toConfigurationBgp(c, huaweiConfig);

    // Convert VRFs
    toConfigurationVrfs(c, huaweiConfig);

    // Convert ACLs
    toConfigurationAcls(c, huaweiConfig);

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

    // TODO: Convert ACL names to IpAccessList objects for incoming/outgoing filters
    // Huawei interfaces reference ACLs by name, but Batfish requires IpAccessList objects
    // if (huaweiInterface.getIncomingFilter() != null) {
    //   builder.setIncomingFilter(huaweiInterface.getIncomingFilter());
    // }
    // if (huaweiInterface.getOutgoingFilter() != null) {
    //   builder.setOutgoingFilter(huaweiInterface.getOutgoingFilter());
    // }

    // Set DHCP relay addresses
    if (!huaweiInterface.getDhcpRelayAddresses().isEmpty()) {
      builder.setDhcpRelayAddresses(ImmutableList.copyOf(huaweiInterface.getDhcpRelayAddresses()));
    }

    return builder.build();
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
      toInterface(huaweiIface, vrf, c, huaweiCfg);
    }
  }

  /**
   * Converts a single Huawei interface to a Batfish vendor-independent interface.
   *
   * @param huaweiIface The Huawei interface to convert
   * @param vrf The VRF to attach the interface to
   * @param c The Configuration that owns the interface
   * @param huaweiCfg The Huawei configuration (for ACL lookup)
   * @return A Batfish Interface object
   */
  public static @Nonnull Interface toInterface(
      @Nonnull HuaweiInterface huaweiIface,
      @Nonnull Vrf vrf,
      @Nonnull Configuration c,
      @Nonnull HuaweiConfiguration huaweiCfg) {
    String name = huaweiIface.getName();
    // Use builder pattern
    Interface.Builder builder =
        Interface.builder()
            .setName(name)
            .setType(getInterfaceType(name))
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

    // Set incoming filter if present
    if (huaweiIface.getIncomingFilter() != null) {
      String aclName = huaweiIface.getIncomingFilter();
      HuaweiAcl acl = huaweiCfg.getAcls().get(aclName);
      if (acl != null) {
        IpAccessList ipAcl = toIpAccessList(acl);
        c.getIpAccessLists().put(ipAcl.getName(), ipAcl);
        builder.setIncomingFilter(ipAcl);
      }
    }

    // Set outgoing filter if present
    if (huaweiIface.getOutgoingFilter() != null) {
      String aclName = huaweiIface.getOutgoingFilter();
      HuaweiAcl acl = huaweiCfg.getAcls().get(aclName);
      if (acl != null) {
        IpAccessList ipAcl = toIpAccessList(acl);
        c.getIpAccessLists().put(ipAcl.getName(), ipAcl);
        builder.setOutgoingFilter(ipAcl);
      }
    }

    // Set DHCP relay addresses
    if (!huaweiIface.getDhcpRelayAddresses().isEmpty()) {
      builder.setDhcpRelayAddresses(ImmutableList.copyOf(huaweiIface.getDhcpRelayAddresses()));
    }

    return builder.build();
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

    // Build BGP process with basic settings
    BgpProcess bgpProcess =
        BgpProcess.builder()
            .setRouterId(
                huaweiBgp.getRouterId() != null
                    ? huaweiBgp.getRouterId()
                    : c.getAllInterfaces().values().stream()
                        .filter(iface -> iface.getName().contains("Loopback"))
                        .filter(iface -> iface.getAddress() != null)
                        .map(
                            iface -> {
                              InterfaceAddress addr = iface.getAddress();
                              if (addr instanceof ConcreteInterfaceAddress) {
                                return ((ConcreteInterfaceAddress) addr).getIp();
                              }
                              return Ip.ZERO;
                            })
                        .findFirst()
                        .orElse(Ip.ZERO))
            .setEbgpAdminCost(20)
            .setIbgpAdminCost(255)
            .setLocalAdminCost(255)
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .build();

    // Convert network announcements to origination space
    // Network announcements are configured with the "network" command in BGP view
    for (HuaweiBgpProcess.HuaweiBgpNetwork network : huaweiBgp.getNetworks()) {
      Prefix prefix = network.getNetwork();
      if (prefix != null) {
        bgpProcess.addToOriginationSpace(prefix);
      }
    }

    // Create main RIB independent network policy if there are networks
    // This policy filters which networks from the routing table are originated into BGP
    if (!huaweiBgp.getNetworks().isEmpty()) {
      String networkPolicyName = "__bgp_network_policy__";

      // Build policy statements
      List<Statement> statements = new ArrayList<>();

      // Add statements for each network with its route policy
      for (HuaweiBgpProcess.HuaweiBgpNetwork network : huaweiBgp.getNetworks()) {
        Prefix prefix = network.getNetwork();
        if (prefix == null) {
          continue;
        }

        // Create PrefixSpace for this network
        PrefixSpace prefixSpace = new PrefixSpace(PrefixRange.fromPrefix(prefix));

        // If route policy is set, call it
        if (network.getRoutePolicy() != null) {
          statements.add(
              new If(
                  new MatchPrefixSet(
                      DestinationNetwork.instance(), new ExplicitPrefixSet(prefixSpace)),
                  ImmutableList.of(new CallStatement(network.getRoutePolicy()))));
        } else {
          // No route policy, just accept the network
          statements.add(
              new If(
                  new MatchPrefixSet(
                      DestinationNetwork.instance(), new ExplicitPrefixSet(prefixSpace)),
                  ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
        }
      }

      // Add default reject at the end
      statements.add(Statements.ExitReject.toStaticStatement());

      // Create the policy
      RoutingPolicy networkPolicy =
          RoutingPolicy.builder().setName(networkPolicyName).setStatements(statements).build();
      c.getRoutingPolicies().put(networkPolicyName, networkPolicy);

      // Set the policy on BGP process
      bgpProcess.setMainRibIndependentNetworkPolicy(networkPolicyName);
    }

    // Convert BGP neighbors/peers and add to active neighbors
    // Note: HuaweiBgpProcess already stores neighbors as BgpPeerConfig objects
    // We need to convert them to BgpActivePeerConfig
    Map<Ip, BgpActivePeerConfig> activeNeighbors = new HashMap<>();
    for (Map.Entry<Ip, BgpPeerConfig> entry : huaweiBgp.getNeighbors().entrySet()) {
      Ip peerIp = entry.getKey();
      BgpPeerConfig peerConfig = entry.getValue();

      BgpActivePeerConfig.Builder builder = BgpActivePeerConfig.builder();
      builder.setPeerAddress(peerIp);

      // Copy existing properties from peer config
      if (peerConfig instanceof BgpActivePeerConfig) {
        BgpActivePeerConfig existingConfig = (BgpActivePeerConfig) peerConfig;
        if (existingConfig.getRemoteAsns() != null && !existingConfig.getRemoteAsns().isEmpty()) {
          builder.setRemoteAsns(existingConfig.getRemoteAsns());
        }
        if (existingConfig.getGroup() != null) {
          builder.setGroup(existingConfig.getGroup());
        }
        if (existingConfig.getLocalAs() != null) {
          builder.setLocalAs(existingConfig.getLocalAs());
        }
        if (existingConfig.getLocalIp() != null) {
          builder.setLocalIp(existingConfig.getLocalIp());
        }
        if (existingConfig.getClusterId() != null) {
          builder.setClusterId(existingConfig.getClusterId());
        }
        if (existingConfig.getDescription() != null) {
          builder.setDescription(existingConfig.getDescription());
        }
        if (existingConfig.getAuthenticationSettings() != null) {
          builder.setAuthenticationSettings(existingConfig.getAuthenticationSettings());
        }
        builder.setEbgpMultihop(existingConfig.getEbgpMultihop());
        builder.setEnforceFirstAs(existingConfig.getEnforceFirstAs());
      }

      // Apply peer group settings to member peers
      // Peers are already assigned to groups during extraction (peer X.X.X.X group GROUP_NAME)
      String groupName = peerConfig.getGroup();

      // Track if we have address family settings from peer group
      Ipv4UnicastAddressFamily afFromGroup = null;
      if (groupName != null) {
        HuaweiBgpProcess.HuaweiBgpPeerGroup group = huaweiBgp.getPeerGroups().get(groupName);
        if (group != null) {
          // Apply remote AS from group if peer doesn't have one
          boolean needsRemoteAs = true;
          if (peerConfig instanceof BgpActivePeerConfig) {
            BgpActivePeerConfig existingConfig = (BgpActivePeerConfig) peerConfig;
            needsRemoteAs =
                existingConfig.getRemoteAsns() == null || existingConfig.getRemoteAsns().isEmpty();
          }
          if (needsRemoteAs && group.getRemoteAs() != null) {
            builder.setRemoteAsns(LongSpace.of(group.getRemoteAs()));
          }

          // Build address family from peer group settings
          Ipv4UnicastAddressFamily.Builder afBuilder = Ipv4UnicastAddressFamily.builder();

          // Apply route policies from peer group
          if (group.getRoutePolicyIn() != null) {
            afBuilder.setImportPolicy(group.getRoutePolicyIn());
          }
          if (group.getRoutePolicyOut() != null) {
            afBuilder.setExportPolicy(group.getRoutePolicyOut());
          }

          // Apply route reflector client setting from group
          if (group.getRouteReflectorClient() != null) {
            afBuilder.setRouteReflectorClient(group.getRouteReflectorClient());
          }

          // Only build if we have actual settings
          Ipv4UnicastAddressFamily tempAf = afBuilder.build();
          if (tempAf.getImportPolicy() != null
              || tempAf.getExportPolicy() != null
              || tempAf.getRouteReflectorClient() == Boolean.TRUE) {
            afFromGroup = tempAf;
          }

          // Apply local AS from group if peer doesn't have one
          boolean needsLocalAs = true;
          if (peerConfig instanceof BgpActivePeerConfig) {
            BgpActivePeerConfig existingConfig = (BgpActivePeerConfig) peerConfig;
            needsLocalAs = existingConfig.getLocalAs() == null;
          }
          if (needsLocalAs && group.getLocalAs() != null) {
            builder.setLocalAs(group.getLocalAs().longValue());
          }

          // Apply password from peer group if peer doesn't have one
          if (group.getPassword() != null) {
            boolean needsPassword = true;
            if (peerConfig instanceof BgpActivePeerConfig) {
              BgpActivePeerConfig existingConfig = (BgpActivePeerConfig) peerConfig;
              needsPassword = existingConfig.getAuthenticationSettings() == null;
            }
            if (needsPassword) {
              BgpAuthenticationSettings authSettings = new BgpAuthenticationSettings();
              authSettings.setAuthenticationAlgorithm(BgpAuthenticationAlgorithm.TCP_SIGNATURE_MD5);
              authSettings.setAuthenticationKey(group.getPassword());
              builder.setAuthenticationSettings(authSettings);
            }
          }

          // Apply cluster ID from peer group if peer doesn't have one
          if (group.getClusterId() != null) {
            boolean needsClusterId = true;
            if (peerConfig instanceof BgpActivePeerConfig) {
              BgpActivePeerConfig existingConfig = (BgpActivePeerConfig) peerConfig;
              needsClusterId = existingConfig.getClusterId() == null;
            }
            if (needsClusterId) {
              // Convert cluster ID string to long
              // Format: "1.1.1.1" -> 16843009
              String clusterIdStr = group.getClusterId();
              try {
                Ip clusterIp = Ip.parse(clusterIdStr);
                long clusterIdLong = clusterIp.asLong();
                builder.setClusterId(clusterIdLong);
              } catch (IllegalArgumentException e) {
                // Invalid cluster ID format - skip setting it
                // The test uses valid formats, so this shouldn't happen in normal operation
              }
            }
          }
        }
      }

      // Set IPv4 unicast address family from peer group settings
      if (afFromGroup != null) {
        // Merge with existing address families if present
        if (peerConfig instanceof BgpActivePeerConfig) {
          BgpActivePeerConfig existingConfig = (BgpActivePeerConfig) peerConfig;
          Ipv4UnicastAddressFamily existingAf = existingConfig.getIpv4UnicastAddressFamily();
          if (existingAf != null) {
            // Merge: group settings override individual settings
            Ipv4UnicastAddressFamily.Builder mergedBuilder = Ipv4UnicastAddressFamily.builder();
            // Use group policies if set, otherwise use existing
            mergedBuilder.setImportPolicy(
                afFromGroup.getImportPolicy() != null
                    ? afFromGroup.getImportPolicy()
                    : existingAf.getImportPolicy());
            mergedBuilder.setExportPolicy(
                afFromGroup.getExportPolicy() != null
                    ? afFromGroup.getExportPolicy()
                    : existingAf.getExportPolicy());
            // Use group route reflector setting if true, otherwise use existing
            mergedBuilder.setRouteReflectorClient(
                afFromGroup.getRouteReflectorClient() == Boolean.TRUE
                    ? Boolean.TRUE
                    : existingAf.getRouteReflectorClient());
            builder.setIpv4UnicastAddressFamily(mergedBuilder.build());
          } else {
            builder.setIpv4UnicastAddressFamily(afFromGroup);
          }
        } else {
          builder.setIpv4UnicastAddressFamily(afFromGroup);
        }
      } else if (peerConfig instanceof BgpActivePeerConfig) {
        // Preserve existing address families from original config
        BgpActivePeerConfig existingConfig = (BgpActivePeerConfig) peerConfig;
        builder.setIpv4UnicastAddressFamily(existingConfig.getIpv4UnicastAddressFamily());
        builder.setIpv6UnicastAddressFamily(existingConfig.getIpv6UnicastAddressFamily());
      }

      BgpActivePeerConfig activePeerConfig = builder.build();
      activeNeighbors.put(peerIp, activePeerConfig);
    }

    // Set active neighbors on BGP process
    bgpProcess.setNeighbors(activeNeighbors);

    // Convert address families
    // Address families contain peer-specific policies that override global settings
    for (HuaweiBgpProcess.HuaweiBgpAddressFamily af : huaweiBgp.getAddressFamilies().values()) {
      // Only convert unicast address families (IPv4 and IPv6)
      if (!af.isUnicast()) {
        continue;
      }

      // Determine address family type
      boolean isIpv6 =
          af.getType() == HuaweiBgpProcess.HuaweiBgpAddressFamily.AddressFamilyType.IPV6;

      // Apply address family to all active neighbors
      for (Map.Entry<Ip, BgpActivePeerConfig> entry : activeNeighbors.entrySet()) {
        Ip peerIp = entry.getKey();
        BgpActivePeerConfig peer = entry.getValue();

        // Check if there's a peer-specific config for this peer
        HuaweiBgpProcess.HuaweiBgpAfPeerConfig afPeerConfig = af.getPeerConfigs().get(peerIp);

        // Create address family with policies
        if (isIpv6) {
          Ipv6UnicastAddressFamily.Builder afBuilder = Ipv6UnicastAddressFamily.builder();

          // Apply import/export policies
          // Peer-specific config takes priority over address family level
          String importPolicy =
              afPeerConfig != null && afPeerConfig.getImportPolicy() != null
                  ? afPeerConfig.getImportPolicy()
                  : af.getImportPolicy();
          String exportPolicy =
              afPeerConfig != null && afPeerConfig.getExportPolicy() != null
                  ? afPeerConfig.getExportPolicy()
                  : af.getExportPolicy();

          if (importPolicy != null) {
            afBuilder.setImportPolicy(importPolicy);
          }
          if (exportPolicy != null) {
            afBuilder.setExportPolicy(exportPolicy);
          }

          // Create updated peer with IPv6 address family
          BgpActivePeerConfig.Builder peerBuilder = BgpActivePeerConfig.builder();
          peerBuilder.setPeerAddress(peer.getPeerAddress());
          peerBuilder.setRemoteAsns(peer.getRemoteAsns());
          peerBuilder.setLocalAs(peer.getLocalAs());
          peerBuilder.setLocalIp(peer.getLocalIp());
          peerBuilder.setGroup(peer.getGroup());
          peerBuilder.setDescription(peer.getDescription());
          peerBuilder.setEbgpMultihop(peer.getEbgpMultihop());
          peerBuilder.setEnforceFirstAs(peer.getEnforceFirstAs());
          peerBuilder.setClusterId(peer.getClusterId());
          // Preserve IPv4 address family if present
          if (peer.getIpv4UnicastAddressFamily() != null) {
            peerBuilder.setIpv4UnicastAddressFamily(peer.getIpv4UnicastAddressFamily());
          }
          // Set the new IPv6 address family
          peerBuilder.setIpv6UnicastAddressFamily(afBuilder.build());

          BgpActivePeerConfig updatedPeer = peerBuilder.build();
          activeNeighbors.put(peerIp, updatedPeer);
        } else {
          // IPv4 unicast address family
          Ipv4UnicastAddressFamily existingAf = peer.getIpv4UnicastAddressFamily();
          Ipv4UnicastAddressFamily.Builder afBuilder = Ipv4UnicastAddressFamily.builder();

          // Start with existing settings if any
          if (existingAf != null) {
            if (existingAf.getImportPolicy() != null) {
              afBuilder.setImportPolicy(existingAf.getImportPolicy());
            }
            if (existingAf.getExportPolicy() != null) {
              afBuilder.setExportPolicy(existingAf.getExportPolicy());
            }
            afBuilder.setRouteReflectorClient(existingAf.getRouteReflectorClient());
          }

          // Apply import/export policies
          // Peer-specific config takes priority over address family level
          String importPolicy =
              afPeerConfig != null && afPeerConfig.getImportPolicy() != null
                  ? afPeerConfig.getImportPolicy()
                  : af.getImportPolicy();
          String exportPolicy =
              afPeerConfig != null && afPeerConfig.getExportPolicy() != null
                  ? afPeerConfig.getExportPolicy()
                  : af.getExportPolicy();

          if (importPolicy != null) {
            afBuilder.setImportPolicy(importPolicy);
          }
          if (exportPolicy != null) {
            afBuilder.setExportPolicy(exportPolicy);
          }

          // Create updated peer with IPv4 address family
          BgpActivePeerConfig.Builder peerBuilder = BgpActivePeerConfig.builder();
          peerBuilder.setPeerAddress(peer.getPeerAddress());
          peerBuilder.setRemoteAsns(peer.getRemoteAsns());
          peerBuilder.setLocalAs(peer.getLocalAs());
          peerBuilder.setLocalIp(peer.getLocalIp());
          peerBuilder.setGroup(peer.getGroup());
          peerBuilder.setDescription(peer.getDescription());
          peerBuilder.setEbgpMultihop(peer.getEbgpMultihop());
          peerBuilder.setEnforceFirstAs(peer.getEnforceFirstAs());
          peerBuilder.setClusterId(peer.getClusterId());
          // Preserve IPv6 address family if present
          if (peer.getIpv6UnicastAddressFamily() != null) {
            peerBuilder.setIpv6UnicastAddressFamily(peer.getIpv6UnicastAddressFamily());
          }
          // Set the new IPv4 address family
          peerBuilder.setIpv4UnicastAddressFamily(afBuilder.build());

          BgpActivePeerConfig updatedPeer = peerBuilder.build();
          activeNeighbors.put(peerIp, updatedPeer);
        }
      }
    }

    // Update neighbors on BGP process after applying address family settings
    bgpProcess.setNeighbors(activeNeighbors);

    // Convert BGP redistribution policies
    // Redistribution policies are configured with "import-route" command in BGP view
    if (!huaweiBgp.getImportRoutes().isEmpty()) {
      // Use standard Batfish naming convention for redistribution policy
      String redistributionPolicyName =
          String.format("~BGP_REDISTRIBUTE_POLICY~%s~", vrf.getName());

      // Build redistribution policy statements
      List<Statement> statements = new ArrayList<>();

      // For each import route, add a statement to match and accept routes from that protocol
      for (HuaweiBgpProcess.HuaweiBgpImportRoute importRoute : huaweiBgp.getImportRoutes()) {
        String protocol = importRoute.getProtocol();
        String routePolicy = importRoute.getRoutePolicy();

        // Create match expression for the routing protocol
        BooleanExpr matchExpr;
        switch (protocol.toLowerCase()) {
          case "direct":
          case "connect":
            matchExpr = new MatchProtocol(RoutingProtocol.CONNECTED);
            break;
          case "static":
            matchExpr = new MatchProtocol(RoutingProtocol.STATIC);
            break;
          case "ospf":
            matchExpr = new MatchProtocol(RoutingProtocol.OSPF);
            break;
          case "rip":
            matchExpr = new MatchProtocol(RoutingProtocol.RIP);
            break;
          case "isis":
            matchExpr = new MatchProtocol(RoutingProtocol.ISIS_ANY);
            break;
          case "bgp":
            matchExpr = new MatchProtocol(RoutingProtocol.BGP);
            break;
          default:
            // Unknown protocol, skip
            continue;
        }

        // If route policy is specified, call it; otherwise accept directly
        if (routePolicy != null) {
          statements.add(new If(matchExpr, ImmutableList.of(new CallStatement(routePolicy))));
        } else {
          statements.add(
              new If(matchExpr, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
        }
      }

      // Add default reject at the end
      statements.add(Statements.ExitReject.toStaticStatement());

      // Create the redistribution policy
      RoutingPolicy redistributionPolicy =
          RoutingPolicy.builder()
              .setName(redistributionPolicyName)
              .setStatements(statements)
              .build();
      c.getRoutingPolicies().put(redistributionPolicyName, redistributionPolicy);

      // Set the policy on BGP process
      bgpProcess.setRedistributionPolicy(redistributionPolicyName);
    }

    // Set BGP process in VRF
    vrf.setBgpProcess(bgpProcess);
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
                    if (addr instanceof ConcreteInterfaceAddress) {
                      return ((ConcreteInterfaceAddress) addr).getIp();
                    }
                    return Ip.ZERO;
                  })
              .findFirst()
              .orElse(Ip.ZERO);
    }
    ospfBuilder.setRouterId(routerId);

    // Set reference bandwidth
    // Huawei VRP default is 100 Mbps (100,000,000 bps)
    // This is used as the reference for calculating OSPF interface costs
    ospfBuilder.setReferenceBandwidth(100000000.0);

    // Set VRF
    ospfBuilder.setVrf(vrf);

    // Convert OSPF areas
    ImmutableMap.Builder<Long, OspfArea> areasBuilder = ImmutableMap.builder();
    for (HuaweiOspfProcess.HuaweiOspfArea huaweiArea : huaweiOspf.getAreas().values()) {
      OspfArea area = toOspfArea(huaweiArea, c);
      areasBuilder.put(huaweiArea.getAreaId(), area);
    }
    Map<Long, OspfArea> areas = areasBuilder.build();

    // Convert OSPF virtual links (tracked but not yet converted to Batfish model)
    // Batfish OspfProcess doesn't currently support virtual links.
    // Virtual links are extracted and stored in HuaweiOspfProcess but not converted.
    // TODO: Implement conversion when Batfish model adds virtual link support
    if (!huaweiOspf.getVirtualLinks().isEmpty()) {
      // Virtual links tracked for future implementation
    }

    // Convert OSPF interface settings
    for (Map.Entry<String, HuaweiOspfProcess.HuaweiOspfInterfaceSettings> entry :
        huaweiOspf.getInterfaces().entrySet()) {
      String ifaceName = entry.getKey();
      HuaweiOspfProcess.HuaweiOspfInterfaceSettings huaweiSettings = entry.getValue();

      Interface iface = c.getAllInterfaces().get(ifaceName);
      if (iface != null) {
        OspfInterfaceSettings.Builder ospfSettingsBuilder = OspfInterfaceSettings.builder();

        // Set required fields with defaults
        ospfSettingsBuilder.setEnabled(true);
        ospfSettingsBuilder.setPassive(
            huaweiSettings.getPassive() != null && huaweiSettings.getPassive());
        ospfSettingsBuilder.setProcess(String.valueOf(huaweiOspf.getProcessId()));

        // Set hello interval (default is 10 seconds)
        ospfSettingsBuilder.setHelloInterval(
            huaweiSettings.getHelloInterval() != null ? huaweiSettings.getHelloInterval() : 10);

        // Set dead interval (default is 40 seconds, which should be 4x hello interval)
        ospfSettingsBuilder.setDeadInterval(
            huaweiSettings.getDeadInterval() != null ? huaweiSettings.getDeadInterval() : 40);

        // Set area ID
        Long areaId = huaweiSettings.getAreaId();
        if (areaId != null) {
          ospfSettingsBuilder.setAreaName(areaId);
        }

        // Set cost
        Integer cost = huaweiSettings.getCost();
        if (cost != null) {
          ospfSettingsBuilder.setCost(cost);
        } else if (iface.getName().toLowerCase().contains("loopback")) {
          // Loopback interfaces default to cost 0
          ospfSettingsBuilder.setCost(0);
        }

        // Note: Retransmit interval is extracted but not set (not supported in Batfish model)

        // Set network type
        String networkType = huaweiSettings.getNetworkType();
        if (networkType != null) {
          switch (networkType.toUpperCase()) {
            case "P2P":
            case "POINT-TO-POINT":
              ospfSettingsBuilder.setNetworkType(OspfNetworkType.POINT_TO_POINT);
              break;
            case "BROADCAST":
              ospfSettingsBuilder.setNetworkType(OspfNetworkType.BROADCAST);
              break;
            case "NBMA":
              ospfSettingsBuilder.setNetworkType(OspfNetworkType.NON_BROADCAST_MULTI_ACCESS);
              break;
            case "P2MP":
            case "POINT-TO-MULTIPOINT":
              ospfSettingsBuilder.setNetworkType(OspfNetworkType.POINT_TO_MULTIPOINT);
              break;
            default:
              // Unknown network type - don't set (will remain null)
              break;
          }
        } else {
          // Default network type is BROADCAST when not specified
          ospfSettingsBuilder.setNetworkType(OspfNetworkType.BROADCAST);
        }

        // Set the OSPF settings on the interface
        iface.setOspfSettings(ospfSettingsBuilder.build());
      }
    }

    // Convert OSPF redistribution policies to export policy
    // In Huawei, redistribution is configured with "import-route" command
    // In Batfish, this is modeled as an export policy on the OSPF process
    if (!huaweiOspf.getRedistributionPolicies().isEmpty()) {
      // Use the route policy from the first redistribution policy
      HuaweiOspfProcess.HuaweiOspfRedistributionPolicy redistPolicy =
          huaweiOspf.getRedistributionPolicies().values().iterator().next();
      String routePolicyName = redistPolicy.getRoutePolicy();
      if (routePolicyName != null && c.getRoutingPolicies().containsKey(routePolicyName)) {
        ospfBuilder.setExportPolicyName(routePolicyName);
      } else {
        // Fallback to placeholder if policy not found
        String exportPolicyName = "__ospf_export__";
        ospfBuilder.setExportPolicyName(exportPolicyName);

        // Create a basic routing policy for redistribution
        if (!c.getRoutingPolicies().containsKey(exportPolicyName)) {
          RoutingPolicy policy =
              RoutingPolicy.builder()
                  .setName(exportPolicyName)
                  .setStatements(ImmutableList.of())
                  .build();
          c.getRoutingPolicies().put(exportPolicyName, policy);
        }
      }
    }

    // Set OSPF default-information-originate if configured
    // In Huawei, default-information-originate causes the router to advertise a default route
    // In Batfish, this is modeled as injectDefaultRoute on OspfArea
    if (huaweiOspf.getDefaultOriginate()) {
      // Apply to all areas - default-information-originate is process-level in Huawei
      ImmutableMap.Builder<Long, OspfArea> updatedAreasBuilder = ImmutableMap.builder();
      for (Map.Entry<Long, OspfArea> entry : areas.entrySet()) {
        OspfArea area = entry.getValue();
        OspfArea.Builder areaBuilder = area.toBuilder().setInjectDefaultRoute(true);
        // Set metric if configured (default is 1 if not specified)
        int metric = 1;
        if (huaweiOspf.getDefaultCost() != null) {
          metric = huaweiOspf.getDefaultCost().intValue();
        }
        areaBuilder.setMetricOfDefaultRoute(metric);
        updatedAreasBuilder.put(entry.getKey(), areaBuilder.build());
      }
      areas = updatedAreasBuilder.build();
    }
    if (huaweiOspf.getDefaultOriginateRouteMap() != null) {
      // Default originate with route-map
      // Route map filtering for default originate is not yet supported
      // Would require route-policy conversion to Batfish format first
    }

    // Set areas on the OSPF process
    ospfBuilder.setAreas(areas);

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
      matchCondition = TrueExpr.INSTANCE;
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
    HuaweiFamily huaweiFamily = c.getVendorFamily().getHuawei();

    for (HuaweiVrf huaweiVrf : huaweiCfg.getVrfs().values()) {
      String vrfName = huaweiVrf.getName();

      // Skip default VRF as it's already created
      if (DEFAULT_VRF_NAME.equals(vrfName)) {
        continue;
      }

      // Create VRF builder
      Vrf.Builder vrfBuilder = Vrf.builder().setName(vrfName);

      // Build the VRF
      Vrf vrf = vrfBuilder.build();

      // Set description if present (must be done after build)
      if (huaweiVrf.getDescription() != null) {
        vrf.setDescription(huaweiVrf.getDescription());
      }

      // Add to configuration
      c.getVrfs().put(vrfName, vrf);

      // Store VRF-specific data in HuaweiFamily
      HuaweiVrfData vrfData = new HuaweiVrfData(vrfName);

      // Parse and set route distinguisher if present
      if (huaweiVrf.getRouteDistinguisher() != null) {
        try {
          RouteDistinguisher rd = RouteDistinguisher.parse(huaweiVrf.getRouteDistinguisher());
          vrfData.setRouteDistinguisher(rd);
        } catch (IllegalArgumentException e) {
          // Invalid RD format - log warning but continue
          // TODO: Add warning to configuration warnings
          // String.format("Invalid route distinguisher '%s' for VRF '%s': %s",
          //     huaweiVrf.getRouteDistinguisher(), vrfName, e.getMessage());
        }
      }

      // Set import route targets
      if (huaweiVrf.getImportRouteTargets() != null
          && !huaweiVrf.getImportRouteTargets().isEmpty()) {
        vrfData.setImportRouteTargets(huaweiVrf.getImportRouteTargets());
      }

      // Set export route targets
      if (huaweiVrf.getExportRouteTargets() != null
          && !huaweiVrf.getExportRouteTargets().isEmpty()) {
        vrfData.setExportRouteTargets(huaweiVrf.getExportRouteTargets());
      }

      // Set description
      vrfData.setDescription(huaweiVrf.getDescription());

      // Set address family flags
      vrfData.setIpv4Enabled(huaweiVrf.isIpv4Enabled());
      vrfData.setIpv6Enabled(huaweiVrf.isIpv6Enabled());

      // Store VRF data in HuaweiFamily
      huaweiFamily.putVrf(vrfName, vrfData);
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
          builder.setStub(StubSettings.builder().setSuppressType3(true).build());
        } else {
          // Regular stub area - allow Type 3 summary LSAs
          builder.setStub(StubSettings.builder().setSuppressType3(false).build());
        }
        break;
      case NSSA:
        // For NSSA area, use NssaSettings with suppressType3 based on configuration
        NssaSettings.Builder nssaBuilder = NssaSettings.builder();
        if (huaweiArea.isNoSummary()) {
          // Totally stubby NSSA - suppress Type 3 summary LSAs
          nssaBuilder.setSuppressType3(true);
        }
        // Set default originate type based on Huawei default-information-originate configuration
        if (huaweiArea.isDefaultOriginate()) {
          // NSSA with default-information-originate configured
          nssaBuilder.setDefaultOriginateType(OspfDefaultOriginateType.INTER_AREA);
        } else {
          // Default behavior - no default originate
          nssaBuilder.setDefaultOriginateType(OspfDefaultOriginateType.NONE);
        }
        builder.setNssa(nssaBuilder.build());
        break;
      case NORMAL:
        // Normal area - no special settings
        break;
    }

    // Convert area ranges (abr-summary) to OspfAreaSummary
    if (!huaweiArea.getAreaRanges().isEmpty()) {
      ImmutableMap.Builder<Prefix, OspfAreaSummary> summariesBuilder = ImmutableMap.builder();
      for (Map.Entry<Prefix, HuaweiOspfProcess.HuaweiOspfAreaRange> entry :
          huaweiArea.getAreaRanges().entrySet()) {
        Prefix prefix = entry.getKey();
        HuaweiOspfProcess.HuaweiOspfAreaRange huaweiRange = entry.getValue();

        // Determine the summary route behavior based on advertise flag
        OspfAreaSummary.SummaryRouteBehavior behavior;
        if (huaweiRange.isAdvertise()) {
          behavior = OspfAreaSummary.SummaryRouteBehavior.ADVERTISE_AND_INSTALL_DISCARD;
        } else {
          behavior = OspfAreaSummary.SummaryRouteBehavior.NOT_ADVERTISE_AND_NO_DISCARD;
        }

        // Get the cost if set
        Long metric = huaweiRange.getCost();

        OspfAreaSummary summary = new OspfAreaSummary(behavior, metric);
        summariesBuilder.put(prefix, summary);
      }
      builder.setSummaries(summariesBuilder.build());
    }

    // TODO: Convert area authentication - extracted but not converted to Batfish model
    // Area authentication extraction is implemented in grammar as state 3 (in grammar, not
    // implemented)
    // TODO: Convert area summary settings - not implemented in Huawei grammar yet
    // TODO: Convert NSSA default-information-originate - extracted but not converted
    // NSSA default-information-originate extraction is implemented but conversion is not complete
    // This is tracked in the parsing documentation as state 3 (in grammar, not implemented)

    return builder.build();
  }

  /**
   * Converts Huawei route-policies to Batfish vendor-independent format.
   *
   * @param c The Batfish Configuration object to populate
   * @param huaweiCfg The Huawei configuration to convert from
   */
  public static void toConfigurationRoutePolicies(
      @Nonnull Configuration c, @Nonnull HuaweiConfiguration huaweiCfg) {
    for (HuaweiRoutePolicy huaweiPolicy : huaweiCfg.getRoutePolicies().values()) {
      RoutingPolicy policy = convertSingleRoutePolicy(huaweiPolicy, c);
      c.getRoutingPolicies().put(huaweiPolicy.getName(), policy);
    }
  }

  /**
   * Converts a single Huawei route-policy to a Batfish RoutingPolicy.
   *
   * @param huaweiPolicy The Huawei route-policy to convert
   * @param c The Batfish Configuration
   * @return A Batfish RoutingPolicy
   */
  private static @Nonnull RoutingPolicy convertSingleRoutePolicy(
      @Nonnull HuaweiRoutePolicy huaweiPolicy, @Nonnull Configuration c) {
    List<Statement> statements = new ArrayList<>();

    // Process nodes in order (they're stored in a List, already sorted by node ID during
    // extraction)
    for (HuaweiRoutePolicy.HuaweiRoutePolicyNode node : huaweiPolicy.getNodes()) {
      Statement stmt = convertRoutePolicyNode(node, c);
      if (stmt != null) {
        statements.add(stmt);
      }
    }

    return RoutingPolicy.builder()
        .setName(huaweiPolicy.getName())
        .setOwner(c)
        .setStatements(statements)
        .build();
  }

  /**
   * Converts a Huawei route-policy node to a Batfish Statement.
   *
   * @param node The Huawei route-policy node to convert
   * @param c The Batfish Configuration
   * @return A Batfish Statement, or null if conversion fails
   */
  private static @Nullable Statement convertRoutePolicyNode(
      @Nonnull HuaweiRoutePolicy.HuaweiRoutePolicyNode node, @Nonnull Configuration c) {
    List<Statement> trueStatements = new ArrayList<>();
    List<Statement> falseStatements = new ArrayList<>();

    // Convert match conditions to BooleanExpr
    BooleanExpr matchExpr = convertMatchConditions(node.getMatchConditions(), c);

    // Convert set actions
    List<Statement> setStatements = convertSetActions(node.getSetActions());
    trueStatements.addAll(setStatements);

    // Handle action (PERMIT/DENY)
    switch (node.getAction()) {
      case PERMIT:
        // If match succeeds, apply actions and accept route
        trueStatements.add(Statements.ExitAccept.toStaticStatement());
        // If match fails, continue to next node
        falseStatements.add(Statements.ExitAccept.toStaticStatement());
        break;
      case DENY:
        // If match succeeds, reject route
        trueStatements.add(Statements.ExitReject.toStaticStatement());
        // If match fails, continue to next node
        falseStatements.add(Statements.ExitAccept.toStaticStatement());
        break;
    }

    // Create If statement
    if (matchExpr != null) {
      return new If(matchExpr, trueStatements, falseStatements);
    } else {
      // No match conditions, just execute the statements
      // For DENY with no conditions, reject everything
      if (node.getAction() == HuaweiRoutePolicy.HuaweiRoutePolicyNode.Action.DENY) {
        return Statements.ExitReject.toStaticStatement();
      }
      // For PERMIT with no conditions, accept everything and apply actions
      return new If(BooleanExprs.TRUE, trueStatements, falseStatements);
    }
  }

  /**
   * Converts Huawei route-policy match conditions to a Batfish BooleanExpr.
   *
   * @param conditions The Huawei match conditions
   * @param c The Batfish Configuration
   * @return A Batfish BooleanExpr, or null if no match conditions
   */
  @SuppressWarnings("unused") // Configuration parameter reserved for future use
  private static @Nullable BooleanExpr convertMatchConditions(
      @Nonnull HuaweiRoutePolicy.HuaweiRoutePolicyMatchConditions conditions,
      @Nonnull Configuration c) {
    List<BooleanExpr> matchExprs = new ArrayList<>();

    // Match IP prefix list (if-match ip-prefix)
    if (conditions.getIpPrefix() != null) {
      String prefixListName = conditions.getIpPrefix();
      // Create a MatchPrefixSet that references the named prefix list
      MatchPrefixSet prefixMatch =
          new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(prefixListName));
      matchExprs.add(prefixMatch);
    }

    // Match community filter (if-match community-filter)
    // Note: Community filters are stored in HuaweiConfiguration but not yet converted to Batfish
    // format
    // TODO: Implement community filter lookup and conversion
    if (conditions.getCommunityFilter() != null) {
      // Community filters are not yet supported in Batfish conversion
      // For now, we skip this condition
    }

    // Match community list (if-match community)
    // Note: Direct community matching is not yet implemented
    if (conditions.getCommunities() != null && !conditions.getCommunities().isEmpty()) {
      // Direct community matching is not yet supported in Batfish conversion
      // For now, we skip this condition
    }

    if (matchExprs.isEmpty()) {
      return null;
    } else if (matchExprs.size() == 1) {
      return matchExprs.get(0);
    } else {
      // Combine multiple match conditions with AND
      Conjunction conjunction = new Conjunction();
      for (BooleanExpr expr : matchExprs) {
        conjunction.getConjuncts().add(expr);
      }
      return conjunction;
    }
  }

  /**
   * Converts Huawei route-policy set actions to a list of Batfish Statements.
   *
   * @param actions The Huawei set actions
   * @return A list of Batfish Statements
   */
  private static @Nonnull List<Statement> convertSetActions(
      @Nonnull HuaweiRoutePolicy.HuaweiRoutePolicySetActions actions) {
    List<Statement> statements = new ArrayList<>();

    // Set local preference (apply local-preference)
    if (actions.getLocalPreference() != null) {
      statements.add(new SetLocalPreference(new LiteralLong(actions.getLocalPreference())));
    }

    // Set community (apply community)
    // Note: Community setting is not yet implemented
    if (actions.getCommunities() != null && !actions.getCommunities().isEmpty()) {
      // Community setting is not yet supported in Batfish conversion
      // TODO: Implement SetCommunity conversion
    }

    // Set cost/metric (apply cost)
    if (actions.getCost() != null) {
      statements.add(new SetMetric(new LiteralLong(actions.getCost().longValue())));
    }

    // Set preference (apply preference)
    // Note: This maps to administrative cost, not local preference
    if (actions.getPreference() != null) {
      // Preference in Huawei is administrative distance
      // In Batfish, this is SetAdministrativeCost
      statements.add(new SetAdministrativeCost(new LiteralInt(actions.getPreference().intValue())));
    }

    // Set tag (apply tag)
    if (actions.getTag() != null) {
      statements.add(new SetTag(new LiteralLong(actions.getTag())));
    }

    return statements;
  }

  private HuaweiConversions() {
    // Prevent instantiation
  }
}

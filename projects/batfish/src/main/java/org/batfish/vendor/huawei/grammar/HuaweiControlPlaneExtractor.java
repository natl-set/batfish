package org.batfish.vendor.huawei.grammar;

import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Apply_communityContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Apply_costContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Apply_local_preferenceContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Apply_preferenceContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Apply_tagContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Area_abr_summaryContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Area_authenticationContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Area_nssaContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Area_stubContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_importContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_networkContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_peerContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_peer_groupContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_router_idContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Description_lineContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_dot1q_terminationContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_ip_addressContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_match_communityContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_match_community_filterContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_match_ip_prefixContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_ospf_areaContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_ospf_authenticationContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_ospf_costContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_ospf_network_typeContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_ospf_passiveContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_ospf_timersContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_shutdownContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_areaContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_defaultContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_default_costContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_default_originateContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_default_tagContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_default_typeContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_import_costContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_import_routeContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_import_route_policyContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_import_tagContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_import_typeContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_networkContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_router_idContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_virtual_linkContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_aclContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_bgpContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_interfaceContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_natContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_ospfContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_returnContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_route_policyContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_static_routeContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_sysnameContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_vlanContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_vrfContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.V_descriptionContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.V_nameContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Vrf_route_distinguisherContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Vrf_vpn_targetContext;
import org.batfish.vendor.huawei.representation.HuaweiAcl;
import org.batfish.vendor.huawei.representation.HuaweiAcl.AclType;
import org.batfish.vendor.huawei.representation.HuaweiAclLine;
import org.batfish.vendor.huawei.representation.HuaweiBgpProcess;
import org.batfish.vendor.huawei.representation.HuaweiConfiguration;
import org.batfish.vendor.huawei.representation.HuaweiInterface;
import org.batfish.vendor.huawei.representation.HuaweiNatAddressGroup;
import org.batfish.vendor.huawei.representation.HuaweiNatRule;
import org.batfish.vendor.huawei.representation.HuaweiNatRule.NatType;
import org.batfish.vendor.huawei.representation.HuaweiOspfProcess;
import org.batfish.vendor.huawei.representation.HuaweiRoutePolicy;
import org.batfish.vendor.huawei.representation.HuaweiRoutePolicy.HuaweiRoutePolicyNode;
import org.batfish.vendor.huawei.representation.HuaweiRoutePolicy.HuaweiRoutePolicyNode.Action;
import org.batfish.vendor.huawei.representation.HuaweiStaticRoute;
import org.batfish.vendor.huawei.representation.HuaweiVlan;
import org.batfish.vendor.huawei.representation.HuaweiVrf;

/**
 * Control plane extractor for Huawei VRP configurations.
 *
 * <p>This class extracts configuration data from Huawei VRP parse trees using ANTLR listener
 * pattern. It processes system settings (hostname), interfaces, and other configuration elements.
 */
public class HuaweiControlPlaneExtractor extends HuaweiParserBaseListener
    implements ControlPlaneExtractor {

  private final HuaweiConfiguration _configuration;
  private final String _text;
  private final HuaweiCombinedParser _parser;
  private final Warnings _w;
  private String _currentInterfaceName;
  private HuaweiAcl _currentAcl;
  private HuaweiVrf _currentVrf;
  private Integer _currentVlanId;
  private String _pendingVlanDescription;
  private Long _currentOspfAreaId;
  private HuaweiRoutePolicyNode _currentRoutePolicyNode;

  public HuaweiControlPlaneExtractor(
      String text, HuaweiCombinedParser parser, Warnings w, SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _w = w;
    _configuration = new HuaweiConfiguration();
    _currentInterfaceName = null;
    _currentAcl = null;
    _currentVrf = null;
    _currentVlanId = null;
    _pendingVlanDescription = null;
  }

  public String getInputText() {
    return _text;
  }

  public HuaweiCombinedParser getParser() {
    return _parser;
  }

  public Warnings getWarnings() {
    return _w;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker.DEFAULT.walk(this, tree);
  }

  /**
   * Extracts configuration from a Huawei configuration text.
   *
   * @param text The configuration text to parse
   * @param parser The combined parser to use
   * @param w Warnings object to collect parsing warnings
   * @return A populated HuaweiConfiguration object
   */
  public static HuaweiConfiguration extract(String text, HuaweiCombinedParser parser, Warnings w) {
    return extract(text, parser, w, new SilentSyntaxCollection());
  }

  /**
   * Extracts configuration from a Huawei configuration text.
   *
   * @param text The configuration text to parse
   * @param parser The combined parser to use
   * @param w Warnings object to collect parsing warnings
   * @param silentSyntax Collection of silent syntax patterns
   * @return A populated HuaweiConfiguration object
   */
  public static HuaweiConfiguration extract(
      String text, HuaweiCombinedParser parser, Warnings w, SilentSyntaxCollection silentSyntax) {
    HuaweiParser.Huawei_configurationContext tree = parser.parse();
    HuaweiControlPlaneExtractor extractor =
        new HuaweiControlPlaneExtractor(text, parser, w, silentSyntax);
    ParseTreeWalker.DEFAULT.walk(extractor, tree);
    return extractor._configuration;
  }

  /**
   * Process exit from s_sysname rule - extract hostname.
   *
   * <p>Extracts hostname from the sysname command (e.g., "sysname Router1").
   */
  @Override
  public void exitS_sysname(S_sysnameContext ctx) {
    if (ctx.hostname != null) {
      String hostname = ctx.hostname.getText();
      _configuration.setHostname(hostname);
    }
  }

  /**
   * Process entry to s_interface rule - begin tracking a new interface.
   *
   * <p>Extracts the interface name and prepares to collect interface-specific configuration.
   */
  @Override
  public void enterS_interface(S_interfaceContext ctx) {
    if (ctx.iname != null) {
      _currentInterfaceName = ctx.iname.getText();
      // Create or get the interface
      HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
      if (iface == null) {
        iface = new HuaweiInterface(_currentInterfaceName);
        _configuration.addInterface(_currentInterfaceName, iface);
      }
    }
  }

  /**
   * Process exit from if_ip_address rule - extract interface IP address.
   *
   * <p>Extracts IPv4 address and subnet mask from the "ip address A.B.C.D A.B.C.D" command.
   */
  @Override
  public void exitIf_ip_address(If_ip_addressContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
    if (iface == null || ctx.addr == null || ctx.mask == null) {
      return;
    }

    try {
      String addrStr = ctx.addr.getText();
      String maskStr = ctx.mask.getText();

      org.batfish.datamodel.Ip addr = org.batfish.datamodel.Ip.parse(addrStr);
      org.batfish.datamodel.Ip mask = org.batfish.datamodel.Ip.parse(maskStr);

      // Create interface address using IP and subnet mask
      org.batfish.datamodel.ConcreteInterfaceAddress address =
          org.batfish.datamodel.ConcreteInterfaceAddress.create(addr, mask);

      iface.setAddress(address);
    } catch (IllegalArgumentException e) {
      // Invalid IP address or mask - record warning and continue
      String warning =
          String.format(
              "Invalid IP address configuration on interface %s at line %d: %s",
              _currentInterfaceName, ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from if_description rule - extract interface description.
   *
   * <p>Extracts the description text from the "description" command.
   */
  @Override
  public void exitIf_description(HuaweiParser.If_descriptionContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
    if (iface == null) {
      return;
    }

    Description_lineContext descCtx = ctx.description_line();
    if (descCtx != null && descCtx.text != null) {
      // Get all VARIABLE tokens and join them with spaces
      StringBuilder description = new StringBuilder();
      if (descCtx.text.getStart() != null && descCtx.text.getStop() != null) {
        org.antlr.v4.runtime.TokenStream tokens = _parser.getParser().getTokenStream();
        int start = descCtx.text.getStart().getTokenIndex();
        int stop = descCtx.text.getStop().getTokenIndex();
        for (int i = start; i <= stop; i++) {
          org.antlr.v4.runtime.Token token = tokens.get(i);
          if (token.getChannel() == org.antlr.v4.runtime.Token.DEFAULT_CHANNEL) {
            if (description.length() > 0) {
              description.append(" ");
            }
            description.append(token.getText());
          }
        }
      }
      iface.setDescription(description.toString());
    }
  }

  /**
   * Process exit from if_shutdown rule - track interface admin status.
   *
   * <p>Sets the shutdown flag when "shutdown" command is present.
   */
  @Override
  public void exitIf_shutdown(If_shutdownContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
    if (iface == null) {
      return;
    }

    // If SHUTDOWN token is present, the interface is shutdown
    // If UNDO SHUTDOWN, the interface is not shutdown (enabled)
    boolean isShutdown = ctx.SHUTDOWN() != null && ctx.UNDO() == null;
    iface.setShutdown(isShutdown);
  }

  /**
   * Process exit from if_ospf_area rule - extract OSPF area for interface.
   *
   * <p>Extracts OSPF area ID from "ospf area {@code <area-id>}" command on interface.
   */
  @Override
  public void exitIf_ospf_area(If_ospf_areaContext ctx) {
    if (_currentInterfaceName == null || ctx.area_id == null) {
      return;
    }

    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    try {
      long areaId = Long.parseLong(ctx.area_id.getText());
      HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
          ospfProcess
              .getInterfaces()
              .computeIfAbsent(
                  _currentInterfaceName, k -> new HuaweiOspfProcess.HuaweiOspfInterfaceSettings());
      settings.setAreaId(areaId);
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid OSPF area ID at line %d: %s",
              ctx.area_id.getStart().getLine(), ctx.area_id.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from if_ospf_cost rule - extract OSPF cost for interface.
   *
   * <p>Extracts OSPF cost from "ospf cost {@code <value>}" command on interface.
   */
  @Override
  public void exitIf_ospf_cost(If_ospf_costContext ctx) {
    if (_currentInterfaceName == null || ctx.cost == null) {
      return;
    }

    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    try {
      int cost = Integer.parseInt(ctx.cost.getText());
      HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
          ospfProcess
              .getInterfaces()
              .computeIfAbsent(
                  _currentInterfaceName, k -> new HuaweiOspfProcess.HuaweiOspfInterfaceSettings());
      settings.setCost(cost);
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid OSPF cost at line %d: %s",
              ctx.cost.getStart().getLine(), ctx.cost.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from if_ospf_network_type rule - extract OSPF network type for interface.
   *
   * <p>Extracts OSPF network type from "ospf network-type {@code <type>}" command on interface.
   */
  @Override
  public void exitIf_ospf_network_type(If_ospf_network_typeContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    String networkType = null;
    if (ctx.BROADCAST() != null) {
      networkType = "BROADCAST";
    } else if (ctx.P2P() != null) {
      networkType = "P2P";
    } else if (ctx.P2MP() != null) {
      networkType = "P2MP";
    } else if (ctx.NBMA() != null) {
      networkType = "NBMA";
    }

    if (networkType != null) {
      HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
          ospfProcess
              .getInterfaces()
              .computeIfAbsent(
                  _currentInterfaceName, k -> new HuaweiOspfProcess.HuaweiOspfInterfaceSettings());
      settings.setNetworkType(networkType);
    }
  }

  /**
   * Process exit from if_ospf_timers rule - extract OSPF timers for interface.
   *
   * <p>Extracts OSPF timers from "ospf timer {@code <type>} {@code <seconds>}" command on
   * interface.
   */
  @Override
  public void exitIf_ospf_timers(If_ospf_timersContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    try {
      HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
          ospfProcess
              .getInterfaces()
              .computeIfAbsent(
                  _currentInterfaceName, k -> new HuaweiOspfProcess.HuaweiOspfInterfaceSettings());

      if (ctx.h != null) {
        settings.setHelloInterval(Integer.parseInt(ctx.h.getText()));
      } else if (ctx.d != null) {
        settings.setDeadInterval(Integer.parseInt(ctx.d.getText()));
      } else if (ctx.r != null) {
        settings.setRetransmitInterval(Integer.parseInt(ctx.r.getText()));
      }
    } catch (NumberFormatException e) {
      String warning =
          String.format("Invalid OSPF timer value at line %d", ctx.getStart().getLine());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from if_ospf_authentication rule - extract OSPF authentication for interface.
   *
   * <p>Extracts OSPF authentication from "ospf authentication-mode {@code <type>} {@code <key>}"
   * command on interface.
   */
  @Override
  public void exitIf_ospf_authentication(If_ospf_authenticationContext ctx) {
    if (_currentInterfaceName == null || ctx.key == null) {
      return;
    }

    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    String authType = null;
    if (ctx.MD5() != null) {
      authType = "MD5";
    } else if (ctx.SIMPLE() != null) {
      authType = "SIMPLE";
    }

    if (authType != null) {
      HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
          ospfProcess
              .getInterfaces()
              .computeIfAbsent(
                  _currentInterfaceName, k -> new HuaweiOspfProcess.HuaweiOspfInterfaceSettings());
      settings.setAuthType(authType);
      settings.setAuthKey(ctx.key.getText());
    }
  }

  /**
   * Process exit from if_ospf_passive rule - extract OSPF passive setting for interface.
   *
   * <p>Extracts OSPF passive setting from "ospf [enable|disable] passive" command on interface.
   */
  @Override
  public void exitIf_ospf_passive(If_ospf_passiveContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    boolean passive = ctx.ENABLE() != null;
    HuaweiOspfProcess.HuaweiOspfInterfaceSettings settings =
        ospfProcess
            .getInterfaces()
            .computeIfAbsent(
                _currentInterfaceName, k -> new HuaweiOspfProcess.HuaweiOspfInterfaceSettings());
    settings.setPassive(passive);
  }

  /**
   * Process exit from s_return rule - clear current interface context.
   *
   * <p>Called when exiting an interface configuration block (return command).
   */
  @Override
  public void exitS_return(S_returnContext ctx) {
    // Clear the current interface context when we exit the interface block
    _currentInterfaceName = null;
    // Also clear current VLAN context
    _currentVlanId = null;
    // Clear pending VLAN description
    _pendingVlanDescription = null;
  }

  /**
   * Process entry to s_vlan rule - track current VLAN context.
   *
   * <p>Sets the current VLAN ID when entering a VLAN configuration block (not for batch).
   */
  @Override
  public void enterS_vlan(S_vlanContext ctx) {
    // Only track current VLAN for single VLAN configuration (not batch)
    if (ctx.vlan_id != null) {
      try {
        _currentVlanId = Integer.parseInt(ctx.vlan_id.getText());
      } catch (NumberFormatException e) {
        // Invalid VLAN ID will be handled in exitS_vlan
        _currentVlanId = null;
      }
    } else {
      // For "vlan batch", don't set current VLAN context
      _currentVlanId = null;
    }
  }

  /**
   * Process exit from s_vlan rule - extract VLAN configuration.
   *
   * <p>Extracts VLAN ID from "vlan {@code <id>}" command and creates HuaweiVlan object. For "vlan
   * batch" commands, creates multiple VLANs.
   */
  @Override
  public void exitS_vlan(S_vlanContext ctx) {
    // Handle "vlan batch" command (create multiple VLANs)
    if (ctx.vlan_batch_range() != null) {
      // Check if this is a range specification with "to" keyword
      if (ctx.vlan_batch_range().TO() != null) {
        // Handle "vlan batch X to Y" - create range from X to Y-1 (exclusive at end)
        // "2 to 10" creates VLANs 2-9, not 2-10
        List<HuaweiParser.Uint8Context> uint8Contexts = ctx.vlan_batch_range().uint8();
        if (uint8Contexts.size() >= 2) {
          try {
            int startVlan = Integer.parseInt(uint8Contexts.get(0).getText());
            int endVlan = Integer.parseInt(uint8Contexts.get(uint8Contexts.size() - 1).getText());
            // Create VLANs from start to end-1 (exclusive at end)
            for (int vlanId = startVlan; vlanId < endVlan; vlanId++) {
              HuaweiVlan vlan = _configuration.getVlan(vlanId);
              if (vlan == null) {
                vlan = new HuaweiVlan(vlanId);
                _configuration.addVlan(vlanId, vlan);
              }
            }
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid VLAN ID range at line %d",
                    ctx.vlan_batch_range().getStart().getLine());
            _w.redFlag(warning);
          }
        }
      } else {
        // Handle "vlan batch X Y Z" - create specific VLANs
        for (HuaweiParser.Uint8Context uint8Ctx : ctx.vlan_batch_range().uint8()) {
          try {
            int vlanId = Integer.parseInt(uint8Ctx.getText());
            HuaweiVlan vlan = _configuration.getVlan(vlanId);
            if (vlan == null) {
              vlan = new HuaweiVlan(vlanId);
              _configuration.addVlan(vlanId, vlan);
            }
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid VLAN ID at line %d: %s",
                    uint8Ctx.getStart().getLine(), uint8Ctx.getText());
            _w.redFlag(warning);
          }
        }
      }
    }
    // Handle individual "vlan <id>" command
    else if (ctx.vlan_id != null) {
      try {
        int vlanId = Integer.parseInt(ctx.vlan_id.getText());
        HuaweiVlan vlan = _configuration.getVlan(vlanId);
        if (vlan == null) {
          vlan = new HuaweiVlan(vlanId);
          // Apply pending description if exists
          if (_pendingVlanDescription != null) {
            vlan.setDescription(_pendingVlanDescription);
            _pendingVlanDescription = null;
          }
          _configuration.addVlan(vlanId, vlan);
        }
      } catch (NumberFormatException e) {
        String warning =
            String.format(
                "Invalid VLAN ID at line %d: %s",
                ctx.vlan_id.getStart().getLine(), ctx.vlan_id.getText());
        _w.redFlag(warning);
      }
    }
  }

  /**
   * Process exit from v_name rule - extract VLAN name.
   *
   * <p>Extracts VLAN name from the "name" command within a VLAN configuration block.
   */
  @Override
  public void exitV_name(V_nameContext ctx) {
    // We need to find which VLAN we're currently configuring
    // This is tricky because the grammar doesn't give us direct context
    // We'll need to track the current VLAN similar to how we track current interface
    // For now, this is a stub that will be enhanced when we add full VLAN tracking
  }

  /**
   * Process exit from v_description rule - extract VLAN description.
   *
   * <p>Extracts description from the "description" command within a VLAN configuration block.
   */
  @Override
  public void exitV_description(V_descriptionContext ctx) {
    if (_currentVlanId == null) {
      return;
    }

    // Extract description text and store it temporarily
    // It will be applied to the VLAN when exitS_vlan creates the VLAN object
    if (ctx.description_line() != null && ctx.description_line().text != null) {
      // Get the original text including whitespace between tokens
      ParserRuleContext rule = ctx.description_line().text;
      String text =
          _text.substring(rule.getStart().getStartIndex(), rule.getStop().getStopIndex() + 1);
      if (!text.isEmpty()) {
        _pendingVlanDescription = text.trim();
      }
    }
  }

  /** Process entry to v_description rule - for debugging */
  @Override
  public void enterV_description(V_descriptionContext ctx) {
    // Debug: check if we have a current VLAN ID
    if (_currentVlanId == null) {
      // No current VLAN - this might be the problem
    }
  }

  /**
   * Process exit from if_dot1q_termination rule - extract subinterface VLAN assignment.
   *
   * <p>Extracts VLAN ID from "dot1q termination vid {@code <vid>}" command on subinterfaces.
   */
  @Override
  public void exitIf_dot1q_termination(If_dot1q_terminationContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
    if (iface == null || ctx.vid == null) {
      return;
    }

    try {
      // Store the VLAN ID for this subinterface
      // This can be used later to associate the subinterface with a VLAN
      // For now, we just note it - the actual VLAN-to-subinterface mapping
      // will be done during conversion to Batfish model
      Integer.parseInt(ctx.vid.getText());
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid VLAN ID in dot1q termination on interface %s at line %d: %s",
              _currentInterfaceName, ctx.getStart().getLine(), ctx.vid.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from s_static_route rule - extract static route configuration.
   *
   * <p>Extracts static route information including destination, next-hop, preference, etc.
   */
  @Override
  public void exitS_static_route(S_static_routeContext ctx) {
    if (ctx.static_route_body() == null) {
      return;
    }

    HuaweiParser.Static_route_bodyContext body = ctx.static_route_body();

    try {
      HuaweiStaticRoute route = null;

      // Extract destination and next-hop based on format
      if (body.dest_prefix != null) {
        // CIDR notation: ip route-static 10.0.0.0/24 192.168.1.1
        Prefix destPrefix = Prefix.parse(body.dest_prefix.getText());
        route = new HuaweiStaticRoute(destPrefix);

        if (body.next_hop != null) {
          Ip nextHop = Ip.parse(body.next_hop.getText());
          route.setNextHopIp(nextHop);
        }
      } else if (body.dest_addr != null) {
        // Traditional notation with mask
        Ip destIp = Ip.parse(body.dest_addr.getText());

        if (body.dest_mask != null) {
          Ip mask = Ip.parse(body.dest_mask.getText());
          Prefix destPrefix = Prefix.create(destIp, mask);
          route = new HuaweiStaticRoute(destPrefix);
        } else {
          // No mask - treat as /32
          Prefix destPrefix = Prefix.create(destIp, 32);
          route = new HuaweiStaticRoute(destPrefix);
        }

        // Set next-hop
        if (body.next_hop != null) {
          Ip nextHop = Ip.parse(body.next_hop.getText());
          route.setNextHopIp(nextHop);
        } else if (body.next_hop2 != null) {
          Ip nextHop = Ip.parse(body.next_hop2.getText());
          route.setNextHopIp(nextHop);
        }

        // Set outgoing interface if present
        if (body.out_if != null) {
          route.setNextHopInterface(body.out_if.getText());
        }
      } else if (body.dest_addr2 != null) {
        // Alternative format with interface
        Ip destIp = Ip.parse(body.dest_addr2.getText());

        if (body.dest_mask2 != null) {
          Ip mask = Ip.parse(body.dest_mask2.getText());
          Prefix destPrefix = Prefix.create(destIp, mask);
          route = new HuaweiStaticRoute(destPrefix);
        } else {
          Prefix destPrefix = Prefix.create(destIp, 32);
          route = new HuaweiStaticRoute(destPrefix);
        }

        // Set outgoing interface
        if (body.out_if != null) {
          route.setNextHopInterface(body.out_if.getText());
        }

        // Set next-hop
        if (body.next_hop2 != null) {
          org.batfish.datamodel.Ip nextHop =
              org.batfish.datamodel.Ip.parse(body.next_hop2.getText());
          route.setNextHopIp(nextHop);
        }
      }

      // Set preference if present
      if (route != null && body.pref != null) {
        try {
          int preference = Integer.parseInt(body.pref.getText());
          route.setPreference(preference);
        } catch (NumberFormatException e) {
          String warning =
              String.format(
                  "Invalid preference value at line %d: %s",
                  body.pref.getStart().getLine(), body.pref.getText());
          _w.redFlag(warning);
        }
      }

      // Set VRF if present (either prefix or suffix form)
      if (route != null) {
        if (body.vrf != null) {
          route.setVrfName(body.vrf.getText());
        } else if (body.vrf_suffix != null) {
          route.setVrfName(body.vrf_suffix.getText());
        }
      }

      // Add route to configuration
      if (route != null) {
        _configuration.addStaticRoute(route);
      }
    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing static route at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_bgp rule - create BGP process.
   *
   * <p>Creates HuaweiBgpProcess object with AS number.
   */
  @Override
  public void enterS_bgp(S_bgpContext ctx) {
    if (ctx.as_num != null) {
      try {
        long asNum = Long.parseLong(ctx.as_num.getText());
        HuaweiBgpProcess bgpProcess = new HuaweiBgpProcess(asNum);
        _configuration.setBgpProcess(bgpProcess);
      } catch (NumberFormatException e) {
        String warning =
            String.format(
                "Invalid BGP AS number at line %d: %s",
                ctx.as_num.getStart().getLine(), ctx.as_num.getText());
        _w.redFlag(warning);
      }
    }
  }

  /**
   * Process exit from bgp_router_id rule - extract router ID.
   *
   * <p>Extracts BGP router ID from the "router-id" command.
   */
  @Override
  public void exitBgp_router_id(Bgp_router_idContext ctx) {
    HuaweiBgpProcess bgpProcess = _configuration.getBgpProcess();
    if (bgpProcess == null || ctx.router_ip == null) {
      return;
    }

    try {
      Ip routerId = Ip.parse(ctx.router_ip.getText());
      bgpProcess.setRouterId(routerId);
    } catch (Exception e) {
      String warning =
          String.format(
              "Invalid BGP router ID at line %d: %s",
              ctx.router_ip.getStart().getLine(), ctx.router_ip.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from bgp_peer rule - extract BGP peer configuration.
   *
   * <p>Extracts BGP peer IP address and AS number, and stores the peer configuration in the BGP
   * process.
   */
  @Override
  public void exitBgp_peer(Bgp_peerContext ctx) {
    HuaweiBgpProcess bgpProcess = _configuration.getBgpProcess();
    if (bgpProcess == null || ctx.peer_ip == null) {
      return;
    }

    try {
      // Parse peer IP address
      Ip peerIp = Ip.parse(ctx.peer_ip.getText());

      // Parse peer AS number (optional in grammar, but required for valid configuration)
      Long peerAs = null;
      if (ctx.peer_as != null) {
        peerAs = Long.parseLong(ctx.peer_as.getText());
      }

      // Create BGP active peer configuration
      BgpActivePeerConfig.Builder peerBuilder =
          BgpActivePeerConfig.builder().setPeerAddress(peerIp);

      if (peerAs != null) {
        peerBuilder.setRemoteAsns(LongSpace.of(peerAs));
      }

      // Extract optional peer parameters (e.g., connect-interface, password, group)
      String groupName = null;
      if (ctx.bgp_peer_param() != null) {
        for (org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_peer_paramContext paramCtx :
            ctx.bgp_peer_param()) {
          if (paramCtx.iface != null) {
            // Connect-interface parameter - logged for future use
            // The BgpActivePeerConfig doesn't have a direct way to store this,
            // it would need to be resolved to an IP address or stored separately
          } else if (paramCtx.group_name != null) {
            // Peer group assignment
            groupName = paramCtx.group_name.getText();
            peerBuilder.setGroup(groupName);
          }
          // Password and other parameters are ignored for now
        }
      }

      // Add the peer to the BGP process
      bgpProcess.addNeighbor(peerIp, peerBuilder.build());

    } catch (Exception e) {
      String warning =
          String.format(
              "Invalid BGP peer configuration at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from bgp_peer_group rule - extract BGP peer group configuration.
   *
   * <p>Extracts BGP peer group name and type (internal/external).
   */
  @Override
  public void exitBgp_peer_group(Bgp_peer_groupContext ctx) {
    HuaweiBgpProcess bgpProcess = _configuration.getBgpProcess();
    if (bgpProcess == null || ctx.group_name == null) {
      return;
    }

    try {
      String groupName = ctx.group_name.getText();
      HuaweiBgpProcess.HuaweiBgpPeerGroup peerGroup = bgpProcess.getOrCreatePeerGroup(groupName);

      // Extract type (internal/external)
      for (org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_group_paramContext paramCtx :
          ctx.bgp_group_param()) {
        if (paramCtx.INTERNAL() != null) {
          peerGroup.setType(HuaweiBgpProcess.HuaweiBgpPeerGroup.PeerType.INTERNAL);
        } else if (paramCtx.EXTERNAL() != null) {
          peerGroup.setType(HuaweiBgpProcess.HuaweiBgpPeerGroup.PeerType.EXTERNAL);
        } else if (paramCtx.as_num != null) {
          peerGroup.setRemoteAs(Long.parseLong(paramCtx.as_num.getText()));
        } else if (paramCtx.password != null) {
          peerGroup.setPassword(paramCtx.password.getText());
        } else if (paramCtx.policy != null) {
          String policy = paramCtx.policy.getText();
          if (paramCtx.IMPORT() != null) {
            peerGroup.setRoutePolicyIn(policy);
          } else if (paramCtx.EXPORT() != null) {
            peerGroup.setRoutePolicyOut(policy);
          } else {
            // Default to import if not specified
            peerGroup.setRoutePolicyIn(policy);
          }
        } else if (paramCtx.ROUTE_REFLECTOR_CLIENT() != null) {
          peerGroup.setRouteReflectorClient(true);
          if (paramCtx.id != null) {
            peerGroup.setClusterId(paramCtx.id.getText());
          }
        }
      }

    } catch (Exception e) {
      String warning =
          String.format(
              "Invalid BGP peer group configuration at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from bgp_network rule - extract BGP network announcement.
   *
   * <p>Extracts network prefix and optional route-map.
   */
  @Override
  public void exitBgp_network(Bgp_networkContext ctx) {
    HuaweiBgpProcess bgpProcess = _configuration.getBgpProcess();
    if (bgpProcess == null || ctx.network_addr == null || ctx.network_mask == null) {
      return;
    }

    try {
      Ip networkAddr = Ip.parse(ctx.network_addr.getText());
      Ip networkMask = Ip.parse(ctx.network_mask.getText());

      // Create prefix from address and mask
      Prefix network = Prefix.create(networkAddr, networkMask);

      HuaweiBgpProcess.HuaweiBgpNetwork bgpNetwork =
          new HuaweiBgpProcess.HuaweiBgpNetwork(network, networkMask);

      if (ctx.policy != null) {
        bgpNetwork.setRoutePolicy(ctx.policy.getText());
      }

      bgpProcess.addNetwork(bgpNetwork);
    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing BGP network at line %d: %s", ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from bgp_import rule - extract BGP import-route (redistribution) configuration.
   *
   * <p>Extracts the protocol name and optional route-policy for redistribution into BGP.
   */
  @Override
  public void exitBgp_import(Bgp_importContext ctx) {
    HuaweiBgpProcess bgpProcess = _configuration.getBgpProcess();
    if (bgpProcess == null || ctx.protocol == null) {
      return;
    }

    try {
      String protocol = ctx.protocol.getText().toLowerCase();
      HuaweiBgpProcess.HuaweiBgpImportRoute importRoute =
          new HuaweiBgpProcess.HuaweiBgpImportRoute(protocol);

      if (ctx.policy != null) {
        importRoute.setRoutePolicy(ctx.policy.getText());
      }

      bgpProcess.addImportRoute(importRoute);
    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing BGP import-route at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_acl rule - create ACL object.
   *
   * <p>Creates HuaweiAcl object with name/number and type.
   */
  @Override
  public void enterS_acl(S_aclContext ctx) {
    String aclName = null;
    AclType aclType = AclType.ADVANCED; // Default to advanced

    // Extract ACL name/number
    if (ctx.acl_name != null) {
      aclName = ctx.acl_name.getText();
    } else if (ctx.acl_num != null) {
      aclName = ctx.acl_num.getText();
    }

    // Determine ACL type based on keyword or number range
    if (ctx.acl_type != null) {
      aclType = ctx.acl_type.getText().equals("basic") ? AclType.BASIC : AclType.ADVANCED;
    } else if (ctx.acl_num != null) {
      // Determine type from ACL number range
      try {
        int aclNum = Integer.parseInt(ctx.acl_num.getText());
        if (aclNum >= 2000 && aclNum < 3000) {
          aclType = AclType.BASIC;
        } else if (aclNum >= 3000 && aclNum < 4000) {
          aclType = AclType.ADVANCED;
        }
      } catch (NumberFormatException e) {
        // Invalid ACL number - will be handled as warning
      }
    }

    if (aclName != null) {
      _currentAcl = new HuaweiAcl(aclName, aclType);
      _configuration.addAcl(aclName, _currentAcl);
    }
  }

  /**
   * Process exit from s_acl rule - clear current ACL context.
   *
   * <p>Called when exiting an ACL configuration block.
   */
  @Override
  public void exitS_acl(S_aclContext ctx) {
    _currentAcl = null;
  }

  /**
   * Process exit from acl_rule rule - extract permit/deny rule.
   *
   * <p>Extracts ACL rule information including action, protocol, source, destination, and ports.
   */
  @Override
  public void exitAcl_rule(HuaweiParser.Acl_ruleContext ctx) {
    if (_currentAcl == null) {
      return;
    }

    try {
      // Extract action (permit/deny)
      String action = "deny"; // Default to deny
      if (ctx.action != null) {
        action = ctx.action.getText().toLowerCase();
      }

      // Create ACL line with sequence number (use size of existing lines + 1)
      int seqNum = _currentAcl.getLines().size() + 1;
      HuaweiAclLine line = new HuaweiAclLine(seqNum, action);

      // Extract protocol
      String protocol = "ip"; // Default to IP (any protocol)
      if (ctx.TCP() != null) {
        protocol = "tcp";
      } else if (ctx.UDP() != null) {
        protocol = "udp";
      } else if (ctx.ICMP() != null) {
        protocol = "icmp";
      } else if (ctx.IP() != null) {
        protocol = "ip";
      } else if (!ctx.variable().isEmpty()) {
        protocol = ctx.variable(0).getText().toLowerCase();
      }
      line.setProtocol(protocol);

      // Extract source address
      if (ctx.src_addr != null) {
        String srcAddr = ctx.src_addr.getText();
        // Handle wildcard format
        if (ctx.src_wildcard != null) {
          // Convert address+wildcard to prefix format
          // For now, store as "address wildcard"
          line.setSource(srcAddr + " " + ctx.src_wildcard.getText());
        } else if (ctx.src_prefix_len != null) {
          // CIDR notation
          line.setSource(srcAddr + "/" + ctx.src_prefix_len.getText());
        } else {
          line.setSource(srcAddr);
        }
      } else if (ctx.src_any != null) {
        line.setSource("any");
      }

      // Extract destination address
      if (ctx.dest_addr != null) {
        String destAddr = ctx.dest_addr.getText();
        // Handle wildcard format
        if (ctx.dest_wildcard != null) {
          // Convert address+wildcard to prefix format
          line.setDestination(destAddr + " " + ctx.dest_wildcard.getText());
        } else if (ctx.dest_prefix_len != null) {
          // CIDR notation
          line.setDestination(destAddr + "/" + ctx.dest_prefix_len.getText());
        } else {
          line.setDestination(destAddr);
        }
      } else if (ctx.dest_any != null) {
        line.setDestination("any");
      }

      // Extract source port
      if (ctx.src_port != null || ctx.src_port_start != null) {
        String portOp = "";
        if (ctx.eq != null) {
          portOp = "eq ";
        } else if (ctx.gt != null) {
          portOp = "gt ";
        } else if (ctx.lt != null) {
          portOp = "lt ";
        } else if (ctx.range != null && ctx.src_port_start != null && ctx.src_port_end != null) {
          portOp = "range " + ctx.src_port_start.getText() + " ";
          line.setSourcePort(portOp + ctx.src_port_end.getText());
        }
        if (!portOp.isEmpty() && ctx.src_port != null) {
          line.setSourcePort(portOp + ctx.src_port.getText());
        }
      }

      // Extract destination port
      if (ctx.dest_port != null || ctx.dest_port_start != null) {
        String portOp = "";
        if (ctx.eq2 != null) {
          portOp = "eq ";
        } else if (ctx.gt2 != null) {
          portOp = "gt ";
        } else if (ctx.lt2 != null) {
          portOp = "lt ";
        } else if (ctx.range2 != null && ctx.dest_port_start != null && ctx.dest_port_end != null) {
          portOp = "range " + ctx.dest_port_start.getText() + " ";
          line.setDestinationPort(portOp + ctx.dest_port_end.getText());
        }
        if (!portOp.isEmpty() && ctx.dest_port != null) {
          line.setDestinationPort(portOp + ctx.dest_port.getText());
        }
      }

      // Extract ICMP type
      if (ctx.icmp_type != null) {
        try {
          line.setIcmpType(Integer.parseInt(ctx.icmp_type.getText()));
        } catch (NumberFormatException e) {
          // Invalid ICMP type, ignore
        }
      }

      // Extract ICMP code (requires icmp-type)
      if (ctx.icmp_code != null) {
        try {
          line.setIcmpCode(Integer.parseInt(ctx.icmp_code.getText()));
        } catch (NumberFormatException e) {
          // Invalid ICMP code, ignore
        }
      }

      // Extract TCP established flag
      if (ctx.established != null) {
        line.setEstablished(true);
      }

      // Extract fragment flag
      if (ctx.frag != null) {
        line.setFragment(true);
      }

      // Extract logging flag
      if (ctx.log != null) {
        line.setLogging(true);
      }

      // Extract time-range name
      if (ctx.time_range_name != null) {
        line.setTimeRange(ctx.time_range_name.getText());
      }

      // Add the line to the ACL
      _currentAcl.addLine(line);

    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing ACL rule at line %d: %s", ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_acl_ipv6 rule - create IPv6 ACL object.
   *
   * <p>Creates HuaweiAcl object with name/number for IPv6 ACL.
   */
  @Override
  public void enterS_acl_ipv6(HuaweiParser.S_acl_ipv6Context ctx) {
    String aclName = null;

    // Extract ACL name/number
    if (ctx.acl_name_ipv6 != null) {
      aclName = ctx.acl_name_ipv6.getText();
    } else if (ctx.acl_num_ipv6 != null) {
      aclName = ctx.acl_num_ipv6.getText();
    }

    if (aclName != null) {
      _currentAcl = new HuaweiAcl(aclName, AclType.ADVANCED); // IPv6 ACLs are always advanced
      _currentAcl.setIpv6(true);
      _configuration.addAcl(aclName, _currentAcl);
    }
  }

  /**
   * Process exit from s_acl_ipv6 rule - clear current ACL context.
   *
   * <p>Called when exiting an IPv6 ACL configuration block.
   */
  @Override
  public void exitS_acl_ipv6(HuaweiParser.S_acl_ipv6Context ctx) {
    _currentAcl = null;
  }

  /**
   * Process exit from acl_ipv6_rule rule - extract permit/deny rule for IPv6.
   *
   * <p>Extracts IPv6 ACL rule information including action, protocol, source, destination, and
   * ports.
   */
  @Override
  public void exitAcl_ipv6_rule(HuaweiParser.Acl_ipv6_ruleContext ctx) {
    if (_currentAcl == null) {
      return;
    }

    try {
      // Extract action (permit/deny)
      String action = "deny"; // Default to deny
      if (ctx.action != null) {
        action = ctx.action.getText().toLowerCase();
      }

      // Create ACL line with sequence number (use size of existing lines + 1)
      int seqNum = _currentAcl.getLines().size() + 1;
      HuaweiAclLine line = new HuaweiAclLine(seqNum, action);
      line.setIpv6(true);

      // Extract protocol
      String protocol = "ipv6"; // Default to IPv6 (any protocol)
      if (ctx.TCP() != null) {
        protocol = "tcp";
      } else if (ctx.UDP() != null) {
        protocol = "udp";
      } else if (ctx.ICMPV6() != null) {
        protocol = "icmpv6";
      } else if (!ctx.variable().isEmpty()) {
        protocol = ctx.variable(0).getText().toLowerCase();
      }
      line.setProtocol(protocol);

      // Extract source IPv6 address (IPV6_PREFIX already includes prefix length)
      if (ctx.src_addr_ipv6 != null) {
        line.setSource(ctx.src_addr_ipv6.getText());
      } else if (ctx.src_any_ipv6 != null) {
        line.setSource("any");
      }

      // Extract destination IPv6 address (IPV6_PREFIX already includes prefix length)
      if (ctx.dest_addr_ipv6 != null) {
        line.setDestination(ctx.dest_addr_ipv6.getText());
      } else if (ctx.dest_any_ipv6 != null) {
        line.setDestination("any");
      }

      // Extract source port
      if (ctx.src_port_ipv6 != null || ctx.src_port_start_ipv6 != null) {
        String portOp = "";
        if (ctx.eq != null) {
          portOp = "eq ";
        } else if (ctx.gt != null) {
          portOp = "gt ";
        } else if (ctx.lt != null) {
          portOp = "lt ";
        } else if (ctx.range != null
            && ctx.src_port_start_ipv6 != null
            && ctx.src_port_end_ipv6 != null) {
          portOp = "range " + ctx.src_port_start_ipv6.getText() + " ";
          line.setSourcePort(portOp + ctx.src_port_end_ipv6.getText());
        }
        if (!portOp.isEmpty() && ctx.src_port_ipv6 != null) {
          line.setSourcePort(portOp + ctx.src_port_ipv6.getText());
        }
      }

      // Extract destination port
      if (ctx.dest_port_ipv6 != null || ctx.dest_port_start_ipv6 != null) {
        String portOp = "";
        if (ctx.eq2 != null) {
          portOp = "eq ";
        } else if (ctx.gt2 != null) {
          portOp = "gt ";
        } else if (ctx.lt2 != null) {
          portOp = "lt ";
        } else if (ctx.range2 != null
            && ctx.dest_port_start_ipv6 != null
            && ctx.dest_port_end_ipv6 != null) {
          portOp = "range " + ctx.dest_port_start_ipv6.getText() + " ";
          line.setDestinationPort(portOp + ctx.dest_port_end_ipv6.getText());
        }
        if (!portOp.isEmpty() && ctx.dest_port_ipv6 != null) {
          line.setDestinationPort(portOp + ctx.dest_port_ipv6.getText());
        }
      }

      // Extract ICMPv6 type
      if (ctx.icmp_type_ipv6 != null) {
        try {
          line.setIcmpType(Integer.parseInt(ctx.icmp_type_ipv6.getText()));
        } catch (NumberFormatException e) {
          // Invalid ICMP type, ignore
        }
      }

      // Extract ICMPv6 code (requires icmp-type)
      if (ctx.icmp_code_ipv6 != null) {
        try {
          line.setIcmpCode(Integer.parseInt(ctx.icmp_code_ipv6.getText()));
        } catch (NumberFormatException e) {
          // Invalid ICMP code, ignore
        }
      }

      // Extract TCP established flag
      if (ctx.established_ipv6 != null) {
        line.setEstablished(true);
      }

      // Extract fragment flag
      if (ctx.frag != null) {
        line.setFragment(true);
      }

      // Extract logging flag
      if (ctx.log != null) {
        line.setLogging(true);
      }

      // Extract time-range name
      if (ctx.time_range_name_ipv6 != null) {
        line.setTimeRange(ctx.time_range_name_ipv6.getText());
      }

      // Add the line to the ACL
      _currentAcl.addLine(line);

    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing IPv6 ACL rule at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from s_nat rule - extract NAT configuration.
   *
   * <p>Extracts NAT configuration including address groups, outbound rules, static NAT, and NAT
   * server.
   */
  @Override
  public void exitS_nat(S_natContext ctx) {
    // Check if this is a "no nat" command (undo NAT)
    if (ctx.NO() != null) {
      // Undo NAT - ignore for now
      return;
    }

    try {
      // Handle nat server (port forwarding) - check first since it's the first alternative
      if (ctx.SERVER() != null) {
        // Create NAT rule for NAT server
        String ruleName = "server_" + System.currentTimeMillis();
        HuaweiNatRule natRule = new HuaweiNatRule(ruleName, NatType.NAT_SERVER);

        // Check if protocol specified
        if (ctx.PROTOCOL() != null) {
          if (ctx.TCP() != null) {
            natRule.setProtocol("tcp");
          } else if (ctx.UDP() != null) {
            natRule.setProtocol("udp");
          }

          // Extract ports if protocol specified
          if (ctx.global_port_proto != null) {
            try {
              natRule.setGlobalPort(Integer.parseInt(ctx.global_port_proto.getText()));
            } catch (NumberFormatException e) {
              // Invalid port number
            }
          }
          if (ctx.inside_port_proto != null) {
            try {
              natRule.setInsideLocalPort(Integer.parseInt(ctx.inside_port_proto.getText()));
            } catch (NumberFormatException e) {
              // Invalid port number
            }
          }

          // Extract IPs
          if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
            Ip globalIp = Ip.parse(ctx.ip_address(0).getText());
            natRule.setGlobalIp(globalIp);
          }
          if (ctx.ip_address() != null && ctx.ip_address().size() > 1) {
            Ip insideIp = Ip.parse(ctx.ip_address(1).getText());
            natRule.setInsideLocalIp(insideIp);
          }
        } else {
          // No protocol - check for simple IP mapping or single port
          if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
            Ip globalIp = Ip.parse(ctx.ip_address(0).getText());
            natRule.setGlobalIp(globalIp);
          }
          if (ctx.ip_address() != null && ctx.ip_address().size() > 1) {
            Ip insideIp = Ip.parse(ctx.ip_address(1).getText());
            natRule.setInsideLocalIp(insideIp);
          }

          // Check for single port (format without protocol keyword)
          if (ctx.global_port_simple != null) {
            try {
              natRule.setGlobalPort(Integer.parseInt(ctx.global_port_simple.getText()));
            } catch (NumberFormatException e) {
              // Invalid port number
            }
          }
        }

        // Extract VRF name if present
        if (ctx.VPN_INSTANCE() != null && ctx.VARIABLE() != null) {
          natRule.setVrfName(ctx.VARIABLE().getText());
        }

        _configuration.addNatRule(natRule);
      }

      // Handle nat address-group
      else if (ctx.ADDRESS_GROUP() != null) {
        // Extract address group index
        if (ctx.group_index != null) {
          try {
            int groupIndex = Integer.parseInt(ctx.group_index.getText());
            HuaweiNatAddressGroup addressGroup = new HuaweiNatAddressGroup(groupIndex);

            // Check if we have IP addresses to add
            if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
              // Check if SECTION format (section 0 start-ip end-ip)
              if (ctx.SECTION() != null) {
                // SECTION format: section 0 <start-ip> <end-ip>
                if (ctx.ip_address().size() >= 2) {
                  Ip startIp = Ip.parse(ctx.ip_address(0).getText());
                  Ip endIp = Ip.parse(ctx.ip_address(1).getText());
                  addressGroup.addRange(startIp, endIp);
                }
              } else if (ctx.ADDRESS() != null) {
                // ADDRESS format: address <ip> [mask <mask>]
                Ip startIp = Ip.parse(ctx.ip_address(0).getText());
                Ip endIp = startIp; // Single IP, so start=end
                Ip mask = null;

                // Check if MASK is provided (second IP address in the list)
                if (ctx.MASK() != null && ctx.ip_address().size() >= 2) {
                  mask = Ip.parse(ctx.ip_address(1).getText());
                }

                addressGroup.addRange(startIp, endIp, mask);
              }
              // Simple format: just IP address (no explicit ADDRESS keyword)
              else if (!ctx.ip_address().isEmpty()) {
                Ip ip = Ip.parse(ctx.ip_address(0).getText());
                // Check if second IP is a mask or end of range
                if (ctx.ip_address().size() >= 2) {
                  // Determine if it's a mask or range based on context
                  Ip secondIp = Ip.parse(ctx.ip_address(1).getText());
                  // If SECTION was used, second IP is end of range
                  // Otherwise assume it's a range
                  addressGroup.addRange(ip, secondIp);
                } else {
                  addressGroup.addRange(ip, ip);
                }
              }
            }

            _configuration.addNatAddressGroup(addressGroup);
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid NAT address-group index at line %d: %s",
                    ctx.group_index.getStart().getLine(), ctx.group_index.getText());
            _w.redFlag(warning);
          }
        }
      }

      // Handle nat outbound (dynamic NAT / Easy IP)
      else if (ctx.OUTBOUND() != null) {
        // Create NAT rule for outbound
        String ruleName = "outbound_" + System.currentTimeMillis();
        HuaweiNatRule natRule = new HuaweiNatRule(ruleName, NatType.DYNAMIC);

        // Extract ACL number/name
        if (ctx.acl_num != null) {
          natRule.setAclName(ctx.acl_num.getText());
        } else if (ctx.acl_name != null) {
          natRule.setAclName(ctx.acl_name.getText());
        }

        // Check if using interface (Easy IP)
        if (ctx.INTERFACE() != null) {
          natRule.setType(NatType.EASY_IP);
        }
        // Check if using pool name
        else if (ctx.pool_name != null) {
          natRule.setPoolName(ctx.pool_name.getText());
        }

        // Extract VRF name if present
        if (ctx.vrf_name != null) {
          natRule.setVrfName(ctx.vrf_name.getText());
        }

        _configuration.addNatRule(natRule);
      }

      // Handle nat static (static one-to-one NAT)
      else if (ctx.STATIC() != null) {
        // Create NAT rule for static NAT
        String ruleName = "static_" + System.currentTimeMillis();
        HuaweiNatRule natRule = new HuaweiNatRule(ruleName, NatType.STATIC);

        // Extract global IP (first IP address)
        if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
          Ip globalIp = Ip.parse(ctx.ip_address(0).getText());
          natRule.setGlobalIp(globalIp);
        }

        // Extract inside IP (second IP address)
        if (ctx.ip_address() != null && ctx.ip_address().size() > 1) {
          Ip insideIp = Ip.parse(ctx.ip_address(1).getText());
          natRule.setInsideLocalIp(insideIp);
        }

        // Extract VRF name if present
        if (ctx.VPN_INSTANCE() != null && ctx.VARIABLE() != null) {
          natRule.setVrfName(ctx.VARIABLE().getText());
        }

        _configuration.addNatRule(natRule);
      }

    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing NAT configuration at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_ospf rule - create OSPF process.
   *
   * <p>Creates HuaweiOspfProcess object with process ID.
   */
  @Override
  public void enterS_ospf(S_ospfContext ctx) {
    // Only create OSPF process if it doesn't already exist
    // This prevents resetting the areas map if enterS_ospf is called multiple times
    if (_configuration.getOspfProcess() == null && ctx.process_id != null) {
      try {
        long processId = Long.parseLong(ctx.process_id.getText());
        HuaweiOspfProcess ospfProcess = new HuaweiOspfProcess(processId);
        _configuration.setOspfProcess(ospfProcess);
      } catch (NumberFormatException e) {
        String warning =
            String.format(
                "Invalid OSPF process ID at line %d: %s",
                ctx.process_id.getStart().getLine(), ctx.process_id.getText());
        _w.redFlag(warning);
      }
    }
  }

  /**
   * Process exit from ospf_area rule - extract OSPF area configuration.
   *
   * <p>Extracts area ID from the "area" command and creates area in OSPF process.
   */
  @Override
  public void enterOspf_area(Ospf_areaContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || ctx.area_id == null) {
      return;
    }

    try {
      long areaId = Long.parseLong(ctx.area_id.getText());
      _currentOspfAreaId = areaId;
      // Create the area immediately so sub-stanzas can access it
      ospfProcess.getOrCreateArea(areaId);
    } catch (NumberFormatException e) {
      // Will be handled in exitOspf_area
    }
  }

  @Override
  public void exitOspf_area(Ospf_areaContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      // OSPF process not initialized yet - shouldn't happen if config is well-formed
      String warning =
          String.format(
              "OSPF process not initialized when processing area at line %d",
              ctx.getStart().getLine());
      _w.redFlag(warning);
      return;
    }

    if (ctx.area_id == null) {
      // No area ID in parse tree - this is a grammar issue
      String warning = String.format("OSPf area ID is null at line %d", ctx.getStart().getLine());
      _w.redFlag(warning);
      return;
    }

    try {
      long areaId = Long.parseLong(ctx.area_id.getText());
      // Create or get the area
      HuaweiOspfProcess.HuaweiOspfArea area = ospfProcess.getOrCreateArea(areaId);
      // Track this as the current area for subsequent area sub-stanza processing
      _currentOspfAreaId = areaId;
      // Verify area was added
      if (area != null) {
        // Successfully created/retrieved area
      }
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid OSPF area ID at line %d: %s",
              ctx.area_id.getStart().getLine(), ctx.area_id.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from ospf_network rule - extract OSPF network configuration.
   *
   * <p>Extracts network prefix and area ID from "network {@code <prefix>} area {@code <area-id>}"
   * command.
   */
  @Override
  public void exitOspf_network(Ospf_networkContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || ctx.ip == null || ctx.area_id == null) {
      return;
    }

    try {
      Prefix network = Prefix.parse(ctx.ip.getText());
      long areaId = Long.parseLong(ctx.area_id.getText());

      // Create OSPF network object
      HuaweiOspfProcess.HuaweiOspfNetwork ospfNetwork =
          new HuaweiOspfProcess.HuaweiOspfNetwork(network, areaId);
      ospfProcess.addNetwork(ospfNetwork);

      // Also ensure the area exists
      ospfProcess.getOrCreateArea(areaId);
    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing OSPF network at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from ospf_router_id rule - extract OSPF router ID.
   *
   * <p>Extracts router ID from the "router-id" command.
   */
  @Override
  public void exitOspf_router_id(Ospf_router_idContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || ctx.router_ip == null) {
      return;
    }

    try {
      Ip routerId = Ip.parse(ctx.router_ip.getText());
      ospfProcess.setRouterId(routerId);
    } catch (Exception e) {
      String warning =
          String.format(
              "Invalid OSPF router ID at line %d: %s",
              ctx.router_ip.getStart().getLine(), ctx.router_ip.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from area_stub rule - extract stub area configuration.
   *
   * <p>Extracts stub area settings including no-summary option.
   */
  @Override
  public void exitArea_stub(Area_stubContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || _currentOspfAreaId == null) {
      return;
    }

    HuaweiOspfProcess.HuaweiOspfArea area = ospfProcess.getAreas().get(_currentOspfAreaId);
    if (area != null) {
      area.setAreaType(HuaweiOspfProcess.OspfAreaType.STUB);
      area.setNoSummary(ctx.no_summary != null);
    }
  }

  /**
   * Process exit from area_nssa rule - extract NSSA area configuration.
   *
   * <p>Extracts NSSA area settings including no-summary, no-redistribute, and
   * default-information-originate options.
   */
  @Override
  public void exitArea_nssa(Area_nssaContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || _currentOspfAreaId == null) {
      return;
    }

    HuaweiOspfProcess.HuaweiOspfArea area = ospfProcess.getAreas().get(_currentOspfAreaId);
    if (area != null) {
      area.setAreaType(HuaweiOspfProcess.OspfAreaType.NSSA);
      area.setNoSummary(ctx.NO_SUMMARY() != null);
      area.setNoRedistribute(ctx.NO_REDISTRIBUTE() != null);
      area.setDefaultOriginate(ctx.DEFAULT_INFORMATION_ORIGINATE() != null);
    }
  }

  /**
   * Process exit from area_authentication rule - extract area authentication.
   *
   * <p>Extracts authentication type (MD5 or SIMPLE) and key.
   */
  @Override
  public void exitArea_authentication(Area_authenticationContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || _currentOspfAreaId == null) {
      return;
    }

    HuaweiOspfProcess.HuaweiOspfArea area = ospfProcess.getAreas().get(_currentOspfAreaId);
    if (area != null) {
      if (ctx.MD5() != null) {
        area.setAuthType("MD5");
        if (ctx.key != null) {
          area.setAuthKey(ctx.key.getText());
        }
      } else if (ctx.SIMPLE() != null) {
        area.setAuthType("SIMPLE");
        if (ctx.key != null) {
          area.setAuthKey(ctx.key.getText());
        }
      }
    }
  }

  /**
   * Process exit from area_abr_summary rule - extract area route summarization.
   *
   * <p>Extracts ABR route summarization (abr-summary) settings including prefix, advertise status,
   * and optional cost value. This is Huawei's equivalent to Cisco's "area range" command.
   */
  @Override
  public void exitArea_abr_summary(Area_abr_summaryContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || _currentOspfAreaId == null) {
      return;
    }

    HuaweiOspfProcess.HuaweiOspfArea area = ospfProcess.getAreas().get(_currentOspfAreaId);
    if (area == null || ctx.ip_addr == null || ctx.ip_mask == null) {
      return;
    }

    try {
      // Parse IP address and mask to create a prefix
      Ip addr = Ip.parse(ctx.ip_addr.getText());
      Ip mask = Ip.parse(ctx.ip_mask.getText());
      Prefix prefix = Prefix.create(addr, mask);

      // Determine if this summary should be advertised
      boolean advertise = ctx.NOT_ADVERTISE() == null;

      // Parse optional cost value
      Long cost = null;
      if (ctx.cost_value != null) {
        cost = Long.parseLong(ctx.cost_value.getText());
      }

      // Create and add the area range
      HuaweiOspfProcess.HuaweiOspfAreaRange areaRange =
          new HuaweiOspfProcess.HuaweiOspfAreaRange(prefix, advertise, cost);
      area.addAreaRange(prefix, areaRange);
    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing OSPF area abr-summary at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from ospf_default_originate rule - extract default route origination.
   *
   * <p>Extracts default-information originate settings with optional route map.
   */
  @Override
  public void exitOspf_default_originate(Ospf_default_originateContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    ospfProcess.setDefaultOriginate(true);
    if (ctx.route_map != null) {
      ospfProcess.setDefaultOriginateRouteMap(ctx.route_map.getText());
    }
  }

  /**
   * Process exit from ospf_virtual_link rule - extract virtual link configuration.
   *
   * <p>Extracts virtual link settings including router ID and optional timers.
   */
  @Override
  public void exitOspf_virtual_link(Ospf_virtual_linkContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || ctx.router_id == null) {
      return;
    }

    try {
      Ip routerId = Ip.parse(ctx.router_id.getText());
      HuaweiOspfProcess.HuaweiOspfVirtualLink virtualLink =
          new HuaweiOspfProcess.HuaweiOspfVirtualLink(routerId);

      if (ctx.h != null) {
        virtualLink.setHelloInterval(Integer.parseInt(ctx.h.getText()));
      }
      if (ctx.d != null) {
        virtualLink.setDeadInterval(Integer.parseInt(ctx.d.getText()));
      }

      ospfProcess.addVirtualLink(virtualLink);
    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing OSPF virtual link at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from ospf_import_route rule - extract route redistribution configuration.
   *
   * <p>Extracts import-route settings which configure redistribution of other routing protocols
   * into OSPF.
   */
  @Override
  public void exitOspf_import_route(Ospf_import_routeContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    try {
      // Determine the protocol being redistributed
      HuaweiOspfProcess.HuaweiRedistributionProtocol protocol;
      if (ctx.DIRECT() != null) {
        protocol = HuaweiOspfProcess.HuaweiRedistributionProtocol.DIRECT;
      } else if (ctx.STATIC() != null) {
        protocol = HuaweiOspfProcess.HuaweiRedistributionProtocol.STATIC;
      } else if (ctx.OSPF() != null) {
        protocol = HuaweiOspfProcess.HuaweiRedistributionProtocol.OSPF;
      } else if (ctx.BGP() != null) {
        protocol = HuaweiOspfProcess.HuaweiRedistributionProtocol.BGP;
      } else if (ctx.RIP() != null) {
        protocol = HuaweiOspfProcess.HuaweiRedistributionProtocol.RIP;
      } else if (ctx.ISIS() != null) {
        protocol = HuaweiOspfProcess.HuaweiRedistributionProtocol.ISIS;
      } else if (ctx.UNR() != null) {
        protocol = HuaweiOspfProcess.HuaweiRedistributionProtocol.UNR;
      } else {
        // Unknown protocol - should not happen given grammar
        String warning =
            String.format("Unknown redistribution protocol at line %d", ctx.getStart().getLine());
        _w.redFlag(warning);
        return;
      }

      // Create the redistribution policy
      HuaweiOspfProcess.HuaweiOspfRedistributionPolicy policy =
          new HuaweiOspfProcess.HuaweiOspfRedistributionPolicy(protocol);

      // Extract values from child rules
      for (Ospf_import_costContext costCtx : ctx.ospf_import_cost()) {
        if (costCtx.cost != null) {
          try {
            policy.setCost(Long.parseLong(costCtx.cost.getText()));
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid OSPF redistribution cost at line %d: %s",
                    costCtx.cost.getStart().getLine(), costCtx.cost.getText());
            _w.redFlag(warning);
          }
        }
      }

      for (Ospf_import_typeContext typeCtx : ctx.ospf_import_type()) {
        if (typeCtx.type_value != null) {
          try {
            policy.setType(Integer.parseInt(typeCtx.type_value.getText()));
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid OSPF redistribution type at line %d: %s",
                    typeCtx.type_value.getStart().getLine(), typeCtx.type_value.getText());
            _w.redFlag(warning);
          }
        }
      }

      for (Ospf_import_tagContext tagCtx : ctx.ospf_import_tag()) {
        if (tagCtx.tag != null) {
          try {
            policy.setTag(Long.parseLong(tagCtx.tag.getText()));
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid OSPF redistribution tag at line %d: %s",
                    tagCtx.tag.getStart().getLine(), tagCtx.tag.getText());
            _w.redFlag(warning);
          }
        }
      }

      for (Ospf_import_route_policyContext policyCtx : ctx.ospf_import_route_policy()) {
        if (policyCtx.route_policy != null) {
          policy.setRoutePolicy(policyCtx.route_policy.getText());
        }
      }

      // Add the redistribution policy to the OSPF process
      ospfProcess.addRedistributionPolicy(protocol, policy);

    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing OSPF import-route at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from ospf_default rule - extract default cost, tag, and type values.
   *
   * <p>Extracts "default cost", "default tag", and "default type" commands which set global
   * defaults for redistributed routes.
   */
  @Override
  public void exitOspf_default(Ospf_defaultContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      return;
    }

    try {
      // Extract optional default cost
      for (Ospf_default_costContext costCtx : ctx.ospf_default_cost()) {
        if (costCtx.cost != null) {
          try {
            ospfProcess.setDefaultCost(Long.parseLong(costCtx.cost.getText()));
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid OSPF default cost at line %d: %s",
                    costCtx.cost.getStart().getLine(), costCtx.cost.getText());
            _w.redFlag(warning);
          }
        }
      }

      // Extract optional default tag
      for (Ospf_default_tagContext tagCtx : ctx.ospf_default_tag()) {
        if (tagCtx.tag != null) {
          try {
            ospfProcess.setDefaultTag(Long.parseLong(tagCtx.tag.getText()));
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid OSPF default tag at line %d: %s",
                    tagCtx.tag.getStart().getLine(), tagCtx.tag.getText());
            _w.redFlag(warning);
          }
        }
      }

      // Extract optional default type (1 or 2 for external routes)
      for (Ospf_default_typeContext typeCtx : ctx.ospf_default_type()) {
        if (typeCtx.type_value != null) {
          try {
            ospfProcess.setDefaultType(Integer.parseInt(typeCtx.type_value.getText()));
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid OSPF default type at line %d: %s",
                    typeCtx.type_value.getStart().getLine(), typeCtx.type_value.getText());
            _w.redFlag(warning);
          }
        }
      }

    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing OSPF default command at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_vrf rule - create VRF object.
   *
   * <p>Creates HuaweiVrf object with VRF name.
   */
  @Override
  public void enterS_vrf(S_vrfContext ctx) {
    if (ctx.vrf_name != null) {
      String vrfName = ctx.vrf_name.getText();
      HuaweiVrf vrf = new HuaweiVrf(vrfName);
      _configuration.addVrf(vrfName, vrf);
      _currentVrf = vrf;
    }
  }

  /**
   * Process exit from vrf_route_distinguisher rule - extract RD value.
   *
   * <p>Extracts route distinguisher from "route-distinguisher {@code <rd>}" command.
   */
  @Override
  public void exitVrf_route_distinguisher(Vrf_route_distinguisherContext ctx) {
    if (_currentVrf == null || ctx.rd == null) {
      return;
    }

    String rd = ctx.rd.getText();
    _currentVrf.setRouteDistinguisher(rd);
  }

  /**
   * Process exit from vrf_vpn_target rule - extract route target values.
   *
   * <p>Extracts VPN target (route target) from "vpn-target <rt> import/export/both" command.
   */
  @Override
  public void exitVrf_vpn_target(Vrf_vpn_targetContext ctx) {
    if (_currentVrf == null || ctx.rt_value == null) {
      return;
    }

    String rt = ctx.rt_value.getText();

    // Determine if this is import, export, or both
    boolean isImport = ctx.IMPORT() != null || ctx.BOTH() != null;
    boolean isExport = ctx.EXPORT() != null || ctx.BOTH() != null;

    if (isImport) {
      _currentVrf.addImportRouteTarget(rt);
    }
    if (isExport) {
      _currentVrf.addExportRouteTarget(rt);
    }
  }

  /**
   * Process exit from s_vrf rule - clear current VRF context.
   *
   * <p>Called when exiting a VRF configuration block.
   */
  @Override
  public void exitS_vrf(S_vrfContext ctx) {
    _currentVrf = null;
  }

  /**
   * Process entry to s_route_policy rule - create route-policy node.
   *
   * <p>Creates HuaweiRoutePolicy and HuaweiRoutePolicyNode objects for the route-policy definition.
   * Each route-policy node is defined with: route-policy &lt;name&gt; permit|deny node
   * &lt;node-id&gt;
   */
  @Override
  public void enterS_route_policy(S_route_policyContext ctx) {
    if (ctx.name == null || ctx.node_id == null) {
      return;
    }

    try {
      String policyName = ctx.name.getText();
      int nodeId = Integer.parseInt(ctx.node_id.getText());

      // Determine action (permit or deny)
      Action action = Action.PERMIT;
      if (ctx.action != null && ctx.action.DENY() != null) {
        action = Action.DENY;
      }

      // Get or create the route-policy
      HuaweiRoutePolicy policy = _configuration.getRoutePolicy(policyName);
      if (policy == null) {
        policy = new HuaweiRoutePolicy(policyName);
        _configuration.addRoutePolicy(policyName, policy);
      }

      // Create the route-policy node and set it as current
      HuaweiRoutePolicyNode node = new HuaweiRoutePolicyNode(nodeId, action);
      policy.addNode(node);
      _currentRoutePolicyNode = node;

    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid route-policy node ID at line %d: %s",
              ctx.node_id.getStart().getLine(), ctx.node_id.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from s_route_policy rule - clear current route-policy node context.
   *
   * <p>Called when exiting a route-policy node definition.
   */
  @Override
  public void exitS_route_policy(S_route_policyContext ctx) {
    _currentRoutePolicyNode = null;
  }

  /**
   * Process exit from if_match_ip_prefix rule.
   *
   * <p>Extracts IP prefix list name from "if-match ip-prefix &lt;prefix-list-name&gt;" command.
   */
  @Override
  public void exitIf_match_ip_prefix(If_match_ip_prefixContext ctx) {
    if (_currentRoutePolicyNode == null || ctx.prefix_list == null) {
      return;
    }

    String prefixList = ctx.prefix_list.getText();
    _currentRoutePolicyNode.getMatchConditions().setIpPrefix(prefixList);
  }

  /**
   * Process exit from if_match_community_filter rule.
   *
   * <p>Extracts community filter number from "if-match community-filter &lt;number&gt;" command.
   */
  @Override
  public void exitIf_match_community_filter(If_match_community_filterContext ctx) {
    if (_currentRoutePolicyNode == null || ctx.filter_num == null) {
      return;
    }

    try {
      int filterNum = Integer.parseInt(ctx.filter_num.getText());
      _currentRoutePolicyNode.getMatchConditions().setCommunityFilter(filterNum);
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid community-filter number at line %d: %s",
              ctx.filter_num.getStart().getLine(), ctx.filter_num.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from if_match_community rule.
   *
   * <p>Extracts community list from "if-match community &lt;communities&gt;" command.
   */
  @Override
  public void exitIf_match_community(If_match_communityContext ctx) {
    if (_currentRoutePolicyNode == null || ctx.community_list == null) {
      return;
    }

    // Store the raw community list text for later parsing
    // Full community parsing would require additional context
    String communityText = ctx.community_list.getText();
    // TODO: Parse community list text into Community objects
  }

  /**
   * Process exit from apply_local_preference rule.
   *
   * <p>Extracts local preference value from "apply local-preference &lt;value&gt;" command.
   */
  @Override
  public void exitApply_local_preference(Apply_local_preferenceContext ctx) {
    if (_currentRoutePolicyNode == null || ctx.pref == null) {
      return;
    }

    try {
      long localPref = Long.parseLong(ctx.pref.getText());
      _currentRoutePolicyNode.getSetActions().setLocalPreference(localPref);
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid local-preference value at line %d: %s",
              ctx.pref.getStart().getLine(), ctx.pref.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from apply_community rule.
   *
   * <p>Extracts community value from "apply community &lt;community-value&gt;" command.
   */
  @Override
  public void exitApply_community(Apply_communityContext ctx) {
    if (_currentRoutePolicyNode == null || ctx.community_val == null) {
      return;
    }

    // Store the raw community value text for later parsing
    // TODO: Parse community value text into Community objects
    String communityText = ctx.community_val.getText();
  }

  /**
   * Process exit from apply_cost rule.
   *
   * <p>Extracts cost value from "apply cost &lt;value&gt;" command.
   */
  @Override
  public void exitApply_cost(Apply_costContext ctx) {
    if (_currentRoutePolicyNode == null || ctx.cost == null) {
      return;
    }

    try {
      int cost = Integer.parseInt(ctx.cost.getText());
      _currentRoutePolicyNode.getSetActions().setCost(cost);
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid cost value at line %d: %s",
              ctx.cost.getStart().getLine(), ctx.cost.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from apply_preference rule.
   *
   * <p>Extracts preference value from "apply preference &lt;value&gt;" command.
   */
  @Override
  public void exitApply_preference(Apply_preferenceContext ctx) {
    if (_currentRoutePolicyNode == null || ctx.preference == null) {
      return;
    }

    try {
      int preference = Integer.parseInt(ctx.preference.getText());
      _currentRoutePolicyNode.getSetActions().setPreference(preference);
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid preference value at line %d: %s",
              ctx.preference.getStart().getLine(), ctx.preference.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from apply_tag rule.
   *
   * <p>Extracts tag value from "apply tag &lt;value&gt;" command.
   */
  @Override
  public void exitApply_tag(Apply_tagContext ctx) {
    if (_currentRoutePolicyNode == null || ctx.tag == null) {
      return;
    }

    try {
      long tag = Long.parseLong(ctx.tag.getText());
      _currentRoutePolicyNode.getSetActions().setTag(tag);
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid tag value at line %d: %s", ctx.tag.getStart().getLine(), ctx.tag.getText());
      _w.redFlag(warning);
    }
  }
}

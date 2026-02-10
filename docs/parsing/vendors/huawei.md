# Huawei-Specific Parsing and Extraction

This document covers the unique aspects of parsing and extracting Huawei VRP (Versatile Routing Platform) configurations in Batfish.

## Huawei VRP Configuration Structure

Huawei VRP configurations have several unique characteristics:

1. **Command Hierarchy**: Configurations are organized using nested configuration modes with indentation
2. **Stanza-Based**: Top-level commands (stanzas) like `interface`, `bgp`, `ospf` enter their respective configuration modes
3. **Return/Quit**: The `return` or `quit` command exits the current configuration mode
4. **Undo Command**: The `undo` prefix removes configuration (similar to `no` in Cisco IOS)

## Huawei Grammar Structure

The Huawei grammar is split into several files:

- `HuaweiLexer.g4`: Defines tokens for the lexer
- `HuaweiParser.g4`: Main parser file with entry point and stanza ordering
- `Huawei_common.g4`: Common types and patterns (ip_address, interface_name, null_rest_of_line)
- `Huawei_system.g4`: System configuration (sysname)
- `Huawei_interface.g4`: Interface-specific grammar
- `Huawei_bgp.g4`: BGP protocol grammar
- `Huawei_ospf.g4`: OSPF protocol grammar
- `Huawei_static.g4`: Static routes grammar
- `Huawei_acl.g4`: ACL (access control list) grammar
- `Huawei_nat.g4`: NAT configuration grammar
- `Huawei_vlan.g4`: VLAN configuration grammar
- `Huawei_vrf.g4`: VRF/VPN instance grammar
- `Huawei_route_policy.g4`: Route-policy grammar
- `Huawei_community_filter.g4`: Community filter grammar
- `Huawei_ignored.g4`: Catch-all for unsupported commands

## Common Huawei Parsing Patterns

### Stanza Ordering in s_stanza

The order of alternatives in `s_stanza` is critical. More specific rules must come before less specific ones:

```antlr
s_stanza
:
   s_sysname      // Must be early
   | s_vlan
   | s_bgp
   | s_ospf
   | s_static_route
   | s_acl
   | s_nat
   | s_vrf
   | s_return      // Must come BEFORE s_interface
   | s_interface   // Must come BEFORE s_ignored
   | s_ignored     // Must be LAST (catchall)
;
```

Key ordering rules:
- `s_return` must come before `s_interface` to prevent the keyword `return` from being consumed as an interface name in description text
- `s_interface` must come before `s_ignored` to properly match interface declarations
- `s_ignored` must be last as it's a catchall with `VARIABLE+`

### null_rest_of_line Pattern

The `null_rest_of_line` rule is used to consume the rest of a line when parsing commands that should be recognized but not fully extracted:

```antlr
if_description
:
   DESCRIPTION text = null_rest_of_line
;
```

The `null_rest_of_line` matches `null_token+`, where `null_token` includes tokens that can appear in descriptions and parameter values, but explicitly excludes tokens that start new stanzas (IP, ACL, BGP, OSPF, NAT, VLAN, SYSNAME, RETURN).

**Important**: The `null_token` list deliberately includes some keywords like `INTERFACE`, `DESCRIPTION`, `PERMIT`, `DENY` to allow them to appear within description text, but excludes stanza-starting keywords to prevent them from being consumed mid-line.

### Interface Name Handling

Huawei interface names have several patterns:

```antlr
interface_name
:
   // GigabitEthernet interfaces: GigabitEthernet0/0/1
   name = GIGABITETHERNET (sub = uint16)? FORWARD_SLASH (card = uint16)? FORWARD_SLASH port = uint16
   |
   // Ethernet interfaces: Ethernet0/0/1
   name = ETHERNET (sub = uint16)? FORWARD_SLASH (card = uint16)? FORWARD_SLASH port = uint16
   |
   // VLAN interfaces: Vlanif100 (no separator)
   name = VLANIF vlan = uint16
   |
   // Loopback interfaces: Loopback0
   name = LOOPBACK num = uint16
   |
   // Eth-Trunk interfaces: Eth-Trunk1
   name = ETH_TRUNK num = uint16
   |
   // Multi-part interface names (contain /, -, or .)
   name = VARIABLE (FORWARD_SLASH | DASH | PERIOD) VARIABLE
   |
   // Fallback for other interface types
   name = VARIABLE
;
```

The fallback `VARIABLE` pattern is necessary for less common interface types, but care is taken to avoid matching single-word configuration keywords like "nat", "bgp", "acl" as interface names.

### Flattened NAT Grammar Pattern

Huawei NAT commands use a flattened grammar structure where each NAT command is a standalone alternative within `s_nat`:

```antlr
s_nat
:
   NAT
   (
      // NAT address-group
      ADDRESS_GROUP uint16 (ADDRESS ip_address | MASK ip_address)*
      |
      // NAT outbound
      NO? OUTBOUND (acl_num = uint16 | acl_name = variable)
         (INTERFACE | pool_name = variable)?
         (VPN_INSTANCE vrf_name = variable)?
      |
      // NAT static
      NO? STATIC (GLOBAL ip_address INSIDE ip_address | ...)
      |
      // NAT server
      NO? SERVER ...
      |
      // Other NAT commands (ignored)
      null_rest_of_line
   )
;
```

This flattened structure (instead of separate `nat_substanza` rules) prevents keyword leakage where tokens like `INTERFACE` from NAT commands would be unconsumed and incorrectly matched by `s_interface`.

### Interface Sub-Stanza Pattern

Interface configuration uses a sub-stanza pattern:

```antlr
s_interface
:
   INTERFACE iname = interface_name
   (
      if_substanza
   )*
;

if_substanza
:
   if_description
   | if_ip_address
   | if_shutdown
   | if_dot1q_termination
   | if_ospf
   | if_ignored
;
```

Each `if_*` rule matches specific interface configuration commands. The `if_ignored` rule consumes unrecognized interface commands while preventing the `INTERFACE` keyword from being consumed (which would cause the next interface declaration to fail).

## Implementation Decision Guide for Huawei

When implementing a new Huawei command, consider:

1. **Which configuration section does it belong to?** (interface, bgp, ospf, etc.)
2. **Does it affect the data model?** If not, use `null_rest_of_line` or `s_ignored`
3. **Is it a new top-level stanza?** Add to `s_stanza` in the correct position
4. **Could keywords leak to other stanzas?** Ensure all tokens are consumed within the rule

### Example: Adding a New Interface Command

To add support for a new interface command:

1. **Add the rule to `Huawei_interface.g4`**:

   ```antlr
   // IPv6 address on interface
   if_ip_address6
   :
      IPV6_ADDRESS ipv6_addr = ipv6_address
      (
         SUBNET prefix_len = uint16
      )?
   ;
   ```

2. **Add it to `if_substanza`**:

   ```antlr
   if_substanza
   :
      if_description
      | if_ip_address
      | if_ip_address6
      | if_shutdown
      ...
   ;
   ```

3. **Add extraction code** in `HuaweiControlPlaneExtractor.java`:

   ```java
   @Override
   public void exitIf_ip_address6(If_ip_address6Context ctx) {
     String ifaceName = ctx.getParent().getParent()
         .getRuleContext(S_interfaceContext.class).iname.getText();
     HuaweiInterface iface = _configuration.getInterfaces().computeIfAbsent(
         ifaceName, k -> new HuaweiInterface(ifaceName));
     // Set IPv6 address...
   }
   ```

4. **Write tests** - Create test configurations in `testconfigs/huawei/` directory

## Testing Huawei Configurations

When testing Huawei configurations:

1. **Create minimal test configurations** that focus on the feature being tested
2. **Use `parseHuaweiConfig`** to parse the configuration and access the vendor-specific objects
3. **Verify extraction** by checking that the appropriate properties are set
4. **Test edge cases** like invalid values or interactions with other features

### Example Test

```java
@Test
public void testInterfaceExtraction() {
  HuaweiConfiguration config = parseHuaweiConfig("interface-test");
  HuaweiInterface iface = config.getInterfaces().get("GigabitEthernet0/0/1");

  assertEquals("10.0.0.1/24", iface.getPrimaryAddress());
  assertEquals("Test interface", iface.getDescription());
}
```

## BGP Routing Policies

Huawei BGP supports route policies for controlling route advertisement and acceptance. Route policies in Huawei are similar to route-maps in Cisco IOS.

### Route Policy Structure

Huawei route policies have the following structure:

```huawei
route-policy <name> permit|deny node <node-id>
  if-match <condition>
  apply <action>
```

### Supported Match Conditions

- `if-match ip-prefix <prefix-list>`: Match routes by IP prefix list
- `if-match community-filter <number>`: Match routes by community filter (numbered)
- `if-match community <communities>`: Match routes by community list

### Supported Set Actions

- `apply local-preference <value>`: Set BGP local preference
- `apply cost <value>`: Set route cost/metric
- `apply preference <value>`: Set route preference
- `apply tag <value>`: Set route tag
- `apply community <community>`: Set BGP community attribute

### Route Policy Conversion

Huawei route policies are converted to Batfish vendor-independent `RoutingPolicy` format:

- **Match Conditions**: Converted to `AclLineMatchExpr` objects
  - IP prefix matching → `MatchPrefixSet` with `NamedPrefixSet`
  - Community matching → `MatchCommunity` (future implementation)

- **Set Actions**: Converted to Batfish statement objects
  - Local preference → `SetLocalPreference`
  - Cost/metric → `SetMetric`
  - Preference → `SetAdministrativeCost`
  - Tag → `SetTag`
  - Community → `SetCommunity` (future implementation)

- **Policy Nodes**: Each node becomes a `RoutePolicyStatement` with:
  - PERMIT action → Continue to next node or accept route
  - DENY action → Reject matching routes

### Example Route Policy

```huawei
route-policy FILTER_ROUTES permit node 10
  if-match ip-prefix PREFIX_LIST
  apply local-preference 200
```

Converts to Batfish `RoutingPolicy` with statements that match the prefix set and set local preference.

## BGP Address Families

Huawei BGP supports address family configuration for different types of routing information:

### Supported Address Families

- **ipv4-family**: Standard IPv4 unicast routing
- **ipv4-family vpnv4**: IPv4 VPN routes (MPLS VPN)
- **ipv4-family multicast**: IPv4 multicast routing (parsed, not yet converted)
- **ipv6-family**: IPv6 unicast routing (parsed, not yet converted)

### Address Family Configuration

```huawei
bgp 65000
 ipv4-family
  peer 10.0.0.1 route-policy IMPORT_POLICY import
  peer 10.0.0.1 advertise-community
 #
 ipv4-family vpnv4
  peer 10.0.0.2 route-policy EXPORT_POLICY export
```

### Address Family Features

Within address families, Huawei supports:

- **Route Policies**: Import/export policies per peer
  - `peer <ip> route-policy <name> import`
  - `peer <ip> route-policy <name> export`

- **Community Advertisement**: Control community propagation
  - `peer <ip> advertise-community`

- **Peer Groups**: Apply policies to groups of peers
  - `peer <group-name> route-policy <name> import`

### Conversion Status

- **Parsing**: Full support for all address family commands
- **Extraction**: Peer policies are extracted into `HuaweiBgpAddressFamily` objects
- **Conversion**: IPv4 unicast policies are converted to Batfish `Ipv4UnicastAddressFamily`
- **TODO**: IPv6, multicast, and VPNv4 address families need conversion implementation

## Community Filters

Huawei supports numbered community filters for matching BGP communities:

### Community Filter Syntax

```huawei
ip community-filter <number> permit <community-value>
ip community-filter <number> deny <community-value>
```

### Community Value Formats

- **Standard Format**: `AA:NN` (e.g., `65000:100`)
- **Well-Known Communities**: `internet`, `no-export`, `no-advertise`, `no-export-subconfed`
- **Multiple Communities**: Can list multiple communities in a single filter

### Example

```huawei
ip community-filter 1 permit 65000:100
ip community-filter 2 deny internet
ip community-filter 3 permit 65000:100 65000:200
```

### Integration with Route Policies

Community filters are referenced in route policies:

```huawei
route-policy MATCH_COMMUNITY permit node 10
  if-match community-filter 1
  apply local-preference 200
```

### Implementation Status

- **Parsing**: Fully supported in `Huawei_community_filter.g4`
- **Extraction**: Community filters extracted to `HuaweiCommunityFilter` objects
- **Storage**: Stored in `HuaweiConfiguration._communityFilters` map
- **Conversion**: Parsing and extraction complete; conversion to Batfish format pending

## OSPF Area Types

Huawei OSPF supports different area types with specific behaviors:

### Supported Area Types

- **NORMAL**: Standard OSPF area (default)
- **STUB**: Stub area - no Type 5 LSAs
  - `area <id> stub` - Regular stub
  - `area <id> stub no-summary` - Totally stubby (no Type 3 LSAs)

- **NSSA**: Not-So-Stubby Area
  - `area <id> nssa` - Standard NSSA
  - `area <id> nssa no-summary` - Totally stubby NSSA (no Type 3 LSAs)
  - `area <id> nssa default-information-originate` - NSSA with default route

### NSSA Default Originate

NSSA areas support the `default-information-originate` command:

```huawei
area 1 nssa default-information-originate
```

This converts to Batfish `OspfDefaultOriginateType`:
- **Without default-information-originate**: `NONE`
- **With default-information-originate**: `INTER_AREA`

### Conversion Mapping

| Huawei Area Type | Batfish Setting |
|-----------------|------------------|
| `area <id> stub` | `StubSettings` with `suppressType3=false` |
| `area <id> stub no-summary` | `StubSettings` with `suppressType3=true` |
| `area <id> nssa` | `NssaSettings` with `defaultOriginateType=NONE` |
| `area <id> nssa no-summary` | `NssaSettings` with `suppressType3=true` |
| `area <id> nssa default-information-originate` | `NssaSettings` with `defaultOriginateType=INTER_AREA` |

## OSPF Area Ranges

Huawei supports OSPF area ranges (abr-summary) for route aggregation:

```huawei
area 1
  abr-summary 10.1.0.0 255.255.0.0 advertise
  abr-summary 10.2.0.0 255.255.0.0 not-advertise
```

### Conversion

- **Advertise**: `OspfAreaSummary.SummaryRouteBehavior.ADVERTISE`
- **Not-Advertise**: `OspfAreaSummary.SummaryRouteBehavior.DO_NOT_ADVERTISE`

## OSPF Redistribution

Huawei OSPF supports redistributing routes from other protocols:

```huawei
ospf 1
  import-route direct route-policy FILTER_DIRECT
  import-route static
  import-route bgp
```

### Conversion

Redistribution policies are converted to OSPF export policies in Batfish:
- If a route-policy is specified, it's used as the OSPF process export policy
- Otherwise, a placeholder policy is created
- The route-policy must be defined elsewhere in the configuration

## Implementation Status Summary

### Fully Implemented (Parsing + Extraction + Conversion)

- **Route Policies**: All match conditions and set actions converted to Batfish format
- **BGP IPv4 Unicast Address Families**: Peer policies fully supported
- **Community Filters**: Parsing and extraction complete
- **OSPF Area Types**: All area types (STUB, NSSA) with proper settings
- **OSPF Area Ranges**: Full support with advertise/not-advertise
- **OSPF Interface Settings**: Hello/dead intervals, costs, authentication
- **OSPF Redistribution**: Route policy references tracked

### Partially Implemented (Parsing + Extraction, Conversion Pending)

- **Community Matching**: `if-match community-filter` parsed, conversion pending
- **Community Setting**: `apply community` parsed, conversion pending
- **BGP IPv6 Address Families**: Parsed and extracted, conversion pending
- **BGP Multicast Address Families**: Parsed, extraction and conversion pending
- **BGP VPNv4/VPNv6**: Parsed, conversion pending
- **BGP Network Policies**: Parsed, policy application pending

### Not Yet Implemented

- **BGP Route Reflector**: Cluster ID and client settings extracted but not applied
- **OSPF Virtual Links**: Extracted but not converted (Batfish model limitation)
- **OSPF Authentication**: MD5/SIMPLE authentication parsed but not converted
- **BGP Password**: MD5 authentication parsed but not converted

## Data Structures

### HuaweiBgpProcess

```java
public class HuaweiBgpProcess {
  // Process-level settings
  private Long _asNum;
  private Ip _routerId;

  // Neighbors (peer-level, pre-address family)
  private Map<Ip, BgpPeerConfig> _neighbors;

  // Peer groups
  private Map<String, HuaweiBgpPeerGroup> _peerGroups;

  // Address families (ipv4-family, ipv4-family vpnv4, etc.)
  private Map<String, HuaweiBgpAddressFamily> _addressFamilies;

  // BGP networks
  private Map<Prefix, HuaweiBgpNetwork> _networks;

  // Redistribution
  private Map<FrrRoutingProtocol, HuaweiBgpImportRoute> _redistributionPolicies;
}
```

### HuaweiBgpAddressFamily

```java
public static class HuaweiBgpAddressFamily {
  private String _name; // "ipv4-family", "ipv4-family vpnv4", etc.
  private AddressFamilyType _type; // IPV4, IPV6
  private Boolean _multicast;
  private Boolean _vpn;

  // Peer-specific configs within this address family
  private Map<Ip, HuaweiBgpAfPeerConfig> _peerConfigs;

  // Peer group configs within this address family
  private Map<String, HuaweiBgpAfPeerGroupConfig> _peerGroupConfigs;
}
```

### HuaweiRoutePolicy

```java
public class HuaweiRoutePolicy {
  private String _name;

  // Nodes are stored in order by sequence number
  private SortedMap<Integer, HuaweiRoutePolicyNode> _nodes;

  public static class HuaweiRoutePolicyNode {
    private int _nodeId;
    private LineAction _action; // PERMIT or DENY

    private HuaweiRoutePolicyMatchConditions _matchConditions;
    private HuaweiRoutePolicySetActions _setActions;
  }
}
```

### HuaweiCommunityFilter

```java
public class HuaweiCommunityFilter {
  private int _filterNumber;
  private LineAction _action; // PERMIT or DENY
  private List<Community> _communities;
}
```

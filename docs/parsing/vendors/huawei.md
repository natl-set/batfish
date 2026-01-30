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

# Cisco ACI Vendor Implementation

This directory contains the Batfish vendor implementation for Cisco ACI (Application Centric Infrastructure). ACI is Cisco's software-defined networking solution for data centers that uses a policy-based architecture.

## Overview

Cisco ACI differs significantly from traditional network devices in its configuration model:

- **Policy-Based**: Configuration is defined through policies (contracts) between endpoint groups rather than per-device ACLs
- **Hierarchical Object Model**: Uses a Management Information Tree (MIT) with `polUni` as the root
- **Fabric-Wide Configuration**: A single configuration applies to the entire fabric (spines and leaves)
- **Abstracted Networking**: Network policies are separated from physical topology

The Batfish ACI implementation:
1. Parses ACI JSON/XML exports
2. Converts ACI objects to Batfish's vendor-independent model
3. Maps fabric nodes to individual Configuration objects
4. Transforms contracts to ACLs for analysis

## Supported ACI Objects

The following ACI objects are currently supported:

| ACI Class | ACI Name | Batfish Representation |
|-----------|----------|------------------------|
| `fvTenant` | Tenant | Organizational container for policies |
| `fvCtx` | VRF Context | `Vrf` objects for L3 isolation |
| `fvBD` | Bridge Domain | VLAN interfaces with subnets |
| `fvAEPg` | Endpoint Group | Logical groupings for policy application |
| `vzBrCP` | Contract | `IpAccessList` objects |
| `fabricNodePEp` | Fabric Node | Individual `Configuration` objects |

### Object Details

#### Tenants (`fvTenant`)
Tenants are the primary container for application policies in ACI. They contain:
- VRF contexts
- Bridge domains
- Application profiles
- Endpoint groups
- Contracts

```java
// Creating a tenant
AciConfiguration.Tenant tenant = config.getOrCreateTenant("web-tier");
```

#### VRF Contexts (`fvCtx`)
VRF contexts (also called private networks) define Layer 3 forwarding domains:

```java
// VRF configuration
AciConfiguration.Vrf vrf = new AciConfiguration.Vrf("prod_vrf");
vrf.setTenant("web-tier");
vrf.setDescription("Production VRF");
```

#### Bridge Domains (`fvBD`)
Bridge domains are Layer 2 forwarding domains that:
- Contain IP subnets for gateway addresses
- Associate with VRFs for inter-VLAN routing
- Link to endpoint groups for policy enforcement

```java
// Bridge domain with subnets
AciConfiguration.BridgeDomain bd = new AciConfiguration.BridgeDomain("web_bd");
bd.setVrf("prod_vrf");
bd.setSubnets(ImmutableList.of("10.1.1.0/24", "10.1.2.0/24"));
```

#### Endpoint Groups (`fvAEPg`)
EPGs are logical groupings of endpoints that require similar policies:

```java
// Endpoint group
AciConfiguration.Epg epg = new AciConfiguration.Epg("web_servers");
epg.setTenant("web-tier");
epg.setBridgeDomain("web_bd");
epg.setProvidedContracts(ImmutableList.of("web_contract"));
```

#### Contracts (`vzBrCP`)
Contracts define allowed communication between EPGs:

```java
// Contract with subjects and filters
AciConfiguration.Contract contract = new AciConfiguration.Contract("web_contract");
AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
AciConfiguration.Contract.Filter filter = new AciConfiguration.Contract.Filter();
filter.setIpProtocol("tcp");
filter.setDestinationPorts(ImmutableList.of("80", "443"));
subject.setFilters(ImmutableList.of(filter));
contract.setSubjects(ImmutableList.of(subject));
```

## ACI JSON/XML Parsing

### JSON Structure

ACI configurations are exported as JSON with `polUni` (Policy Universe) as the root:

```json
{
  "polUni": {
    "attributes": {"dn": "uni"},
    "children": [
      {
        "fvTenant": {
          "attributes": {"name": "tenant1", "descr": "Example Tenant"},
          "children": [
            {"fvCtx": {"attributes": {"name": "vrf1"}}},
            {"fvBD": {"attributes": {"name": "bd1"}}},
            {"vzBrCP": {"attributes": {"name": "contract1"}}}
          ]
        }
      },
      {
        "fabricInst": {
          "children": [
            {"fabricProtPol": {
              "children": [
                {"fabricExplicitGEp": {
                  "children": [
                    {"fabricNodePEp": {
                      "attributes": {"id": "1001", "name": "spine1", "role": "spine"}
                    }}
                  ]
                }}
              ]
            }}
          ]
        }
      }
    ]
  }
}
```

### Parsing Flow

1. **Root Deserialization**: `AciConfiguration.fromJson()` parses the JSON
2. **polUni Processing**: `parsePolUni()` extracts top-level children
3. **Tenant Parsing**: `parseTenant()` processes each tenant's configuration
4. **Fabric Node Parsing**: `parseFabricNodes()` extracts fabric topology
5. **Finalization**: `finalizeStructures()` makes structures immutable

```java
// Example: Parsing ACI JSON
AciConfiguration config = AciConfiguration.fromJson(
    "aci-config.json",
    jsonText,
    warnings
);
```

### Custom Deserializer

The `AciPolUniDeserializer` handles the heterogeneous children structure:

```java
public static class AciPolUniDeserializer extends JsonDeserializer<AciPolUni> {
    @Override
    public AciPolUni deserialize(JsonParser p, DeserializationContext ctxt) {
        // Handles dynamic child types (fvTenant, fabricInst, etc.)
        // Converts nested structures to AciConfiguration model
    }
}
```

### Heterogeneous Children Handling

ACI JSON uses a dynamic structure where children objects have variable keys:

```java
// In parseTenant(), children are checked dynamically
for (Object childObj : tenant.getChildren()) {
    if (childObj instanceof Map) {
        Map<String, Object> childMap = (Map<String, Object>) childObj;
        if (childMap.containsKey("fvCtx")) {
            parseVrfFromMap(childMap.get("fvCtx"), tenantName, warnings);
        } else if (childMap.containsKey("fvBD")) {
            parseBridgeDomainFromMap(childMap.get("fvBD"), tenantName, warnings);
        }
        // ... more object types
    }
}
```

## Fabric Node to Configuration Mapping

Each fabric node in the ACI configuration becomes a separate Batfish `Configuration` object:

```java
// In AciConversion.toVendorIndependentConfigurations()
for (AciConfiguration.FabricNode node : aciConfig.getFabricNodes().values()) {
    Configuration c = convertNode(node, aciConfig, warnings);
    configs.put(node.getName(), c);
}
```

### Conversion Process

For each fabric node, the conversion:

1. **Creates Configuration Object**: With hostname = node name
2. **Initializes VRFs**: Creates default VRF and any tenant VRFs
3. **Converts Interfaces**: Physical, VLAN, loopback, port-channel
4. **Processes Bridge Domains**: Creates VLAN interfaces with subnets
5. **Applies Contracts**: Converts to ACLs on relevant interfaces
6. **Handles Path Attachments**: Associates EPGs with physical interfaces

```java
private static Configuration convertNode(
    AciConfiguration.FabricNode node,
    AciConfiguration aciConfig,
    Warnings warnings) {
    String hostname = node.getName();
    Configuration c = new Configuration(hostname, ConfigurationFormat.CISCO_ACI);
    c.setDeviceModel(DeviceModel.CISCO_ACI);
    // ... VRF, interface, contract, EPG conversion
    return c;
}
```

### Interface Type Mapping

| ACI Type | Batfish InterfaceType |
|----------|----------------------|
| `ethernet` / `physical` | `PHYSICAL` |
| `vlan` | `VLAN` |
| `loopback` | `LOOPBACK` |
| `portchannel` / `aggregated` | `AGGREGATED` |

```java
private static InterfaceType toInterfaceType(String type) {
    switch (type.toLowerCase()) {
        case "physical":
        case "ethernet":
            return InterfaceType.PHYSICAL;
        case "vlan":
            return InterfaceType.VLAN;
        case "loopback":
            return InterfaceType.LOOPBACK;
        case "portchannel":
        case "aggregated":
            return InterfaceType.AGGREGATED;
        default:
            return InterfaceType.PHYSICAL;
    }
}
```

## Contract to ACL Conversion

Contracts define communication policies between EPGs and are converted to ACLs:

### Conversion Logic

1. Each contract becomes an `IpAccessList` with name `~CONTRACT~<contract-name>`
2. Each subject filter becomes an ACL line
3. Protocol and port specifications translate to ACL match conditions
4. Implicit deny is added at the end

```java
private static void convertContracts(AciConfiguration aciConfig, Configuration c, Warnings warnings) {
    for (AciConfiguration.Contract contract : aciConfig.getContracts().values()) {
        String contractName = contract.getName();
        String aclName = getContractAclName(contractName);

        IpAccessList.Builder aclBuilder = IpAccessList.builder()
            .setOwner(c)
            .setName(aclName);

        ImmutableList.Builder<ExprAclLine> lines = ImmutableList.builder();

        for (AciConfiguration.Contract.Subject subject : contract.getSubjects()) {
            for (AciConfiguration.Contract.Filter filter : subject.getFilters()) {
                lines.addAll(toAclLines(filter, contractName, c));
            }
        }

        // Default deny
        lines.add(new ExprAclLine(LineAction.DENY, AclLineMatchExprs.TRUE,
            "Default deny for contract " + contractName, ...));

        aclBuilder.setLines(lines.build());
    }
}
```

### Filter to ACL Line Mapping

| Contract Filter | ACL Match Expression |
|-----------------|---------------------|
| `ipProtocol: "tcp"` | `AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)` |
| `destinationPorts: ["80", "443"]` | `AclLineMatchExprs.matchDstPort(IntegerSpace.builder().including(80, 443).build())` |

```java
private static List<ExprAclLine> toAclLines(
    AciConfiguration.Contract.Filter filter,
    String contractName,
    Configuration c) {

    ImmutableList.Builder<AclLineMatchExpr> matchExprs = ImmutableList.builder();

    if (filter.getIpProtocol() != null) {
        matchExprs.add(AclLineMatchExprs.matchIpProtocol(
            toIpProtocol(filter.getIpProtocol())));
    }

    if (filter.getDestinationPorts() != null) {
        IntegerSpace.Builder portSpace = IntegerSpace.builder();
        for (String portStr : filter.getDestinationPorts()) {
            portSpace.including(Integer.parseInt(portStr));
        }
        matchExprs.add(AclLineMatchExprs.matchDstPort(portSpace.build()));
    }

    AclLineMatchExpr matchExpr = matchExprs.build().isEmpty()
        ? AclLineMatchExprs.TRUE
        : AclLineMatchExprs.and(matchExprs.build());

    return ImmutableList.of(new ExprAclLine(
        LineAction.PERMIT,
        matchExpr,
        String.format("Contract %s filter %s", contractName, filter.getName()),
        ...));
}
```

### Example Contract Conversion

**ACI Contract:**
```json
{
  "vzBrCP": {
    "attributes": {"name": "web_allow"},
    "children": [{
      "vzSubj": {
        "attributes": {"name": "http_traffic"},
        "children": [{
          "vzRsSubjFiltAtt": {
            "attributes": {
              "tnVzFilterName": "tcp_80_443",
              "action": "permit"
            }
          }
        }]
      }
    }]
  }
}
```

**Resulting ACL:**
```
IP Access List ~CONTRACT~web_allow
  permit tcp any any dst 80
  permit tcp any any dst 443
  deny ip any any  ; implicit deny
```

## Known Limitations

The following features are partially implemented or not yet supported:

1. **L3Out Conversion**: The `convertL3Outs()` method is a stub. External connectivity (BGP, OSPF, static routes) defined in L3Out configurations is not yet converted.

2. **EPG to Interface Binding**: The `convertPathAttachments()` method has basic implementation but may not handle all EPG-to-interface binding scenarios.

3. **Filter Entries**: Full filter entry parsing (with source/destination IP ranges, ether types) is not complete.

4. **Contract Scope**: Global and application profile contract scopes are treated the same as tenant-scoped contracts.

5. **QoS and Service Graphs**: QoS policies and service graph redirection are not modeled.

6. **Endpoint Learning**: Dynamic endpoint learning and IP address migration are not represented.

7. **Multicast**: Multicast policies and configurations are not converted.

8. **BGP Route Maps**: L3Out BGP policies use simplified conversion; complex route-maps may not be fully represented.

9. **VXLAN Tunnel Encapsulation**: ACI's use of VXLAN for fabric overlay is modeled as standard VLAN interfaces.

10. **FEX and Virtual Port Channels**: Fabric Extender and vPC configurations need additional handling.

## TODO Items

Based on the code, here are the key areas for future development:

### High Priority
- [ ] Complete L3Out conversion (BGP peers, OSPF, static routes)
- [ ] Implement full filter entry parsing with IP ranges
- [ ] Improve EPG path attachment handling

### Medium Priority
- [ ] Add support for contract subject `action` attribute (deny filters)
- [ ] Model QoS policies from contracts
- [ ] Handle service graph configurations
- [ ] Add support for multicast configurations

### Low Priority
- [ ] Implement endpoint discovery from active endpoints
- [ ] Add support for FEX (Fabric Extender) configurations
- [ ] Model vPC (Virtual Port Channel) relationships
- [ ] Add support for vzFilter (reusable filter) objects

## Adding Support for Additional ACI Objects

To add support for a new ACI object type:

### 1. Define the Data Model

Create a new POJO class in `representation/`:

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AciNewObject implements Serializable {
    @JsonProperty("attributes")
    private @Nullable AciNewObjectAttributes attributes;

    @JsonProperty("children")
    private @Nullable List<Object> children;

    public static class AciNewObjectAttributes implements Serializable {
        @JsonProperty("name")
        private @Nullable String name;

        @JsonProperty("descr")
        private @Nullable String description;

        // Add object-specific attributes
    }
}
```

### 2. Add to AciConfiguration

Add storage and getter/setter methods to `AciConfiguration`:

```java
// In AciConfiguration class
private Map<String, NewObject> _newObjects = new TreeMap<>();

@Nonnull
public Map<String, NewObject> getNewObjects() {
    return _newObjects;
}

@Nonnull
public NewObject getOrCreateNewObject(String name) {
    return _newObjects.computeIfAbsent(name, NewObject::new);
}
```

### 3. Parse from JSON

Add parsing logic in `parseTenant()` or create a dedicated parse method:

```java
private void parseTenant(AciTenant tenant, Warnings warnings) {
    // ... existing parsing

    for (Object childObj : tenant.getChildren()) {
        if (childObj instanceof Map) {
            Map<String, Object> childMap = (Map<String, Object>) childObj;
            if (childMap.containsKey("newObject")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> objMap = (Map<String, Object>) childMap.get("newObject");
                parseNewObjectFromMap(objMap, tenantName, warnings);
            }
        }
    }
}
```

### 4. Convert to Vendor-Independent Model

Add conversion logic in `AciConversion`:

```java
private static void convertNewObjects(
    AciConfiguration aciConfig,
    Configuration c,
    Warnings warnings) {

    for (AciConfiguration.NewObject newObj : aciConfig.getNewObjects().values()) {
        // Convert to Batfish structures
        // e.g., create ACLs, interfaces, routes, etc.
    }
}
```

### 5. Add Structure Type

If the object is a top-level structure, add to `AciStructureType`:

```java
public enum AciStructureType implements StructureType {
    // ...
    NEW_OBJECT("New Object");
}
```

### 6. Write Tests

Add tests in `AciConfigurationTest` and `AciConversionTest`:

```java
@Test
public void testParseConfig_newObject() throws IOException {
    String configText = loadTestJson("test-config-with-new-object.json");
    AciConfiguration config = AciConfiguration.fromJson(
        "test.json", configText, new Warnings());

    assertThat(config.getNewObjects(), hasKey("test-object"));
    AciConfiguration.NewObject obj = config.getNewObjects().get("test-object");
    assertThat(obj.getName(), equalTo("test-object"));
}
```

## Testing

The implementation includes comprehensive tests:

- **AciConfigurationTest**: Tests JSON parsing and object model construction
- **AciConversionTest**: Tests conversion to vendor-independent model

### Test Resources

Test JSON files are located in:
```
projects/batfish/src/test/resources/org/batfish/vendor/cisco_aci/
```

### Running Tests

```bash
# Run all ACI tests
./bazel test //projects/batfish/src/test/java/org/batfish/vendor/cisco_aci/...

# Run specific test class
./bazel test //projects/batfish/src/test/java/org/batfish/vendor/cisco_aci:AciConversionTest
```

## References

- [Cisco ACI Policy Model Guide](https://www.cisco.com/c/en/us/td/docs/switches/datacenter/aci/apic/sw/5-x/Configuring_ACI_using_the_ACI_Policy_Model_Guide.html)
- [ACI Object Model](https://www.cisco.com/c/en/us/td/docs/switches/datacenter/aci/apic/sw/5-x/aci-fundamentals-config-guide.html)
- [Batfish Developer Guide](https://github.com/batfish/batfish/blob/master/README.developer.md)

## File Structure

```
org/batfish/vendor/cisco_aci/
├── representation/
│   ├── AciConfiguration.java       # Main vendor-specific configuration class
│   ├── AciConversion.java          # Conversion to vendor-independent model
│   ├── AciPolUni.java              # Root polUni object
│   ├── AciTenant.java              # Tenant object
│   ├── AciVrf.java                 # VRF context object
│   ├── AciBridgeDomain.java        # Bridge domain object
│   ├── AciEndpointGroup.java       # EPG object
│   ├── AciContract.java            # Contract object
│   ├── AciFabricNode.java          # Fabric node object
│   ├── AciInterface.java           # Interface object
│   ├── AciChild.java               # Generic child object
│   ├── AciAttributes.java          # Generic attributes object
│   ├── AciStructureType.java       # Structure type enum
│   ├── AciStructureUsage.java      # Structure usage enum
│   └── package-info.java
├── Vrf.java                        # VRF model class
└── README.md                       # This file
```

## Contributing

When adding new features or fixing bugs:

1. Follow Batfish coding conventions
2. Add unit tests for new functionality
3. Update this README if adding new object types
4. Ensure all tests pass before submitting
5. Add test resources in the appropriate directory

## License

This code is part of the Batfish project and is licensed under the Apache 2.0 License.

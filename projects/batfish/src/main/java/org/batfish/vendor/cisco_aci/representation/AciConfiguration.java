package org.batfish.vendor.cisco_aci.representation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

/**
 * Datamodel class representing a Cisco ACI fabric configuration.
 *
 * <p>This class stores the ACI-specific configuration elements including tenants, bridge domains,
 * VRFs, EPGs (End Point Groups), contracts, and fabric nodes. It provides conversion to the
 * vendor-independent Batfish configuration model.
 *
 * <p>This class supports parsing ACI configurations from both JSON and XML formats. Use {@link
 * #fromFile} for automatic format detection, or use {@link #fromJson} or {@link #fromXml} for
 * specific format parsing.
 *
 * <p>The native ACI JSON/XML structure is hierarchical with polUni as the root:
 *
 * <pre>
 * polUni
 * ├── attributes
 * └── children[]
 *     ├── fvTenant (tenants)
 *     │   └── children[]
 *     │       ├── fvCtx (VRFs)
 *     │       ├── fvBD (bridge domains)
 *     │       ├── fvAp (application profiles)
 *     │       │   └── children[]
 *     │       │       └── fvAEPg (EPGs)
 *     │       └── vzBrCP (contracts)
 *     └── fabricInst (fabric configuration)
 *         └── children[]
 *             └── fabricProtPol
 *                 └── children[]
 *                     └── fabricExplicitGEp
 *                         └── children[]
 *                             └── fabricNodePEp (fabric nodes)
 * </pre>
 */
public final class AciConfiguration extends VendorConfiguration {

  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_TENANTS = "tenants";
  private static final String PROP_BRIDGE_DOMAINS = "bridgeDomains";
  private static final String PROP_VRFS = "vrfs";
  private static final String PROP_EPGS = "epgs";
  private static final String PROP_CONTRACTS = "contracts";
  private static final String PROP_FABRIC_NODES = "fabricNodes";
  private static final String PROP_L3_OUTS = "l3Outs";

  /**
   * Creates an {@link AciConfiguration} from JSON text.
   *
   * <p>This method parses the native ACI JSON structure with polUni as the root and extracts
   * tenants, VRFs, bridge domains, EPGs, contracts, and fabric nodes from the nested children
   * arrays.
   *
   * @param filename The filename of the configuration file
   * @param text The JSON text content
   * @param warnings Warnings object for collecting conversion warnings
   * @return A new AciConfiguration instance
   * @throws IOException If JSON deserialization fails
   */
  public static AciConfiguration fromJson(String filename, String text, Warnings warnings)
      throws IOException {
    // Parse the native ACI JSON structure (polUni root)
    // First, read as JsonNode to handle the wrapper object
    JsonNode rootNode = BatfishObjectMapper.mapper().readTree(text);

    // Extract the polUni object if it's wrapped (common in ACI exports)
    JsonNode polUniNode = rootNode.has("polUni") ? rootNode.get("polUni") : rootNode;

    // Now deserialize the polUni object
    AciPolUniInternal polUni =
        BatfishObjectMapper.mapper().treeToValue(polUniNode, AciPolUniInternal.class);

    // Create a new AciConfiguration and populate it from the parsed structure
    AciConfiguration aciConfiguration = new AciConfiguration();
    aciConfiguration.setWarnings(warnings);
    aciConfiguration.setFilename(filename);

    // Extract hostname from polUni attributes or use default
    String hostname = extractHostname(polUni);
    aciConfiguration.setHostname(hostname);

    // Parse all elements from the nested structure
    aciConfiguration.parsePolUni(polUni, warnings);

    // Finalize the structures
    aciConfiguration.finalizeStructures();

    return aciConfiguration;
  }

  /**
   * Creates an {@link AciConfiguration} from XML text.
   *
   * <p>This method parses the native ACI XML structure with polUni as the root and extracts
   * tenants, VRFs, bridge domains, EPGs, contracts, and fabric nodes from the nested children
   * arrays. The XML structure is equivalent to the JSON structure but uses XML tags instead of JSON
   * keys.
   *
   * @param filename The filename of the configuration file
   * @param text The XML text content
   * @param warnings Warnings object for collecting conversion warnings
   * @return A new AciConfiguration instance
   * @throws IOException If XML deserialization fails
   */
  public static AciConfiguration fromXml(String filename, String text, Warnings warnings)
      throws IOException {
    // Parse the native ACI XML structure (polUni root) using XmlMapper
    AciPolUniInternal polUni =
        BatfishObjectMapper.xmlMapper().readValue(text, AciPolUniInternal.class);

    // Create a new AciConfiguration and populate it from the parsed structure
    AciConfiguration aciConfiguration = new AciConfiguration();
    aciConfiguration.setWarnings(warnings);
    aciConfiguration.setFilename(filename);

    // Extract hostname from polUni attributes or use default
    String hostname = extractHostname(polUni);
    aciConfiguration.setHostname(hostname);

    // Parse all elements from the nested structure
    aciConfiguration.parsePolUni(polUni, warnings);

    // Finalize the structures
    aciConfiguration.finalizeStructures();

    return aciConfiguration;
  }

  /**
   * Creates an {@link AciConfiguration} from file content with automatic format detection.
   *
   * <p>This method automatically detects whether the input is JSON or XML based on the first
   * non-whitespace character (JSON starts with '{', XML starts with '<'). It then calls the
   * appropriate parser method.
   *
   * @param filename The filename of the configuration file
   * @param text The file content (JSON or XML format)
   * @param warnings Warnings object for collecting conversion warnings
   * @return A new AciConfiguration instance
   * @throws IOException If deserialization fails or the format is not recognized
   */
  public static AciConfiguration fromFile(String filename, String text, Warnings warnings)
      throws IOException {
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      throw new IOException("Empty configuration file: " + filename);
    }

    char firstChar = trimmed.charAt(0);
    if (firstChar == '{') {
      // JSON format
      return fromJson(filename, text, warnings);
    } else if (firstChar == '<') {
      // XML format
      return fromXml(filename, text, warnings);
    } else {
      throw new IOException(
          "Unrecognized configuration format. Expected JSON (starting with '{') or "
              + "XML (starting with '<'), but file starts with '"
              + firstChar
              + "': "
              + filename);
    }
  }

  /** Extracts a hostname from the polUni structure. Uses a default if not found. */
  private static String extractHostname(AciPolUniInternal polUni) {
    if (polUni.getAttributes() != null && polUni.getAttributes().getName() != null) {
      return polUni.getAttributes().getName();
    }
    return "aci-fabric";
  }

  private String _hostname;

  /** Map of tenant names to tenant configurations */
  private Map<String, Tenant> _tenants;

  /** Map of bridge domain names to bridge domain configurations */
  private Map<String, BridgeDomain> _bridgeDomains;

  /** Map of VRF names to VRF configurations */
  private Map<String, AciVrfModel> _vrfs;

  /** Map of EPG names to EPG configurations */
  private Map<String, Epg> _epgs;

  /** Map of contract names to contract configurations */
  private Map<String, Contract> _contracts;

  /** Map of fabric node IDs to fabric node configurations */
  private Map<String, FabricNode> _fabricNodes;

  /** Map of L3Out names to L3Out configurations */
  private Map<String, L3Out> _l3Outs;

  /** The vendor format for this configuration */
  private ConfigurationFormat _vendor;

  public AciConfiguration() {
    _tenants = new TreeMap<>();
    _bridgeDomains = new TreeMap<>();
    _vrfs = new TreeMap<>();
    _epgs = new TreeMap<>();
    _contracts = new TreeMap<>();
    _fabricNodes = new TreeMap<>();
    _l3Outs = new TreeMap<>();
  }

  /**
   * Parses the native ACI polUni structure and extracts all configuration elements.
   *
   * <p>This method traverses the hierarchical JSON structure and extracts:
   *
   * <ul>
   *   <li>fvTenant - Tenants and their contained elements
   *   <li>fvCtx - VRF contexts within tenants
   *   <li>fvBD - Bridge domains within tenants
   *   <li>fvAEPg - Endpoint Groups within application profiles
   *   <li>vzBrCP - Contracts within tenants
   *   <li>fabricNodePEp - Fabric nodes within fabricInst
   * </ul>
   *
   * @param polUni The parsed polUni structure
   * @param warnings Warnings object for collecting parsing warnings
   */
  private void parsePolUni(AciPolUniInternal polUni, Warnings warnings) {
    if (polUni == null || polUni.getChildren() == null) {
      return;
    }

    // First pass: collect all fabric nodes from fabricInst
    parseFabricNodes(polUni, warnings);

    // Second pass: process tenants and their contents
    for (AciPolUniInternal.PolUniChild child : polUni.getChildren()) {
      if (child.getFvTenant() != null) {
        parseTenant(child.getFvTenant(), warnings);
      }
    }
  }

  /** Parses fabric nodes from the fabricInst hierarchy. */
  private void parseFabricNodes(AciPolUniInternal polUni, Warnings warnings) {
    for (AciPolUniInternal.PolUniChild child : polUni.getChildren()) {
      if (child.getFabricInst() != null) {
        AciFabricInst fabricInst = child.getFabricInst();
        if (fabricInst.getChildren() != null) {
          for (AciFabricInst.FabricInstChild instChild : fabricInst.getChildren()) {
            if (instChild.getFabricProtPol() != null) {
              AciFabricProtPol protPol = instChild.getFabricProtPol();
              if (protPol.getChildren() != null) {
                for (AciFabricProtPol.FabricProtPolChild protChild : protPol.getChildren()) {
                  if (protChild.getFabricExplicitGEp() != null) {
                    AciFabricExplicitGEp explicitEp = protChild.getFabricExplicitGEp();
                    if (explicitEp.getChildren() != null) {
                      for (AciFabricExplicitGEp.FabricExplicitGEpChild expChild :
                          explicitEp.getChildren()) {
                        if (expChild.getFabricNodePEp() != null) {
                          parseFabricNode(expChild.getFabricNodePEp(), warnings);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /** Parses a single fabric node from fabricNodePEp. */
  private void parseFabricNode(AciFabricNodePEp nodePep, Warnings warnings) {
    if (nodePep.getAttributes() == null) {
      return;
    }

    AciFabricNodePEp.AciFabricNodePEpAttributes attrs = nodePep.getAttributes();
    String nodeId = attrs.getId();
    String name = attrs.getName();
    String podId = attrs.getPodId();
    String role = attrs.getRole();

    // Use nodeId as key, fallback to name
    String key = nodeId != null ? nodeId : (name != null ? name : "unknown");

    FabricNode fabricNode = new FabricNode();
    fabricNode.setNodeId(nodeId);
    fabricNode.setName(name != null ? name : "aci-node-" + nodeId);
    fabricNode.setPodId(podId);
    fabricNode.setRole(role);

    _fabricNodes.put(key, fabricNode);
  }

  /** Parses a tenant and all its contained elements. */
  private void parseTenant(AciTenant tenant, Warnings warnings) {
    if (tenant == null || tenant.getAttributes() == null) {
      return;
    }

    AciTenant.AciTenantAttributes attrs = tenant.getAttributes();
    String tenantName = attrs.getName();
    if (tenantName == null || tenantName.isEmpty()) {
      return;
    }

    // Create or get the tenant
    Tenant t = getOrCreateTenant(tenantName);
    if (attrs.getDescription() != null) {
      // Note: Tenant class doesn't have description field, skip for now
    }

    // Parse tenant children for VRFs, BDs, EPGs, and contracts
    if (tenant.getChildren() == null) {
      return;
    }

    for (Object childObj : tenant.getChildren()) {
      // Children are heterogenous - need to check type
      if (childObj instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> childMap = (Map<String, Object>) childObj;

        // Check for fvCtx (VRF)
        if (childMap.containsKey("fvCtx")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> vrfMap = (Map<String, Object>) childMap.get("fvCtx");
          parseVrfFromMap(vrfMap, tenantName, warnings);
        }
        // Check for fvBD (Bridge Domain)
        else if (childMap.containsKey("fvBD")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> bdMap = (Map<String, Object>) childMap.get("fvBD");
          parseBridgeDomainFromMap(bdMap, tenantName, warnings);
        }
        // Check for fvAp (Application Profile - contains EPGs)
        else if (childMap.containsKey("fvAp")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> apMap = (Map<String, Object>) childMap.get("fvAp");
          parseApplicationProfileFromMap(apMap, tenantName, warnings);
        }
        // Check for vzBrCP (Contract)
        else if (childMap.containsKey("vzBrCP")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> contractMap = (Map<String, Object>) childMap.get("vzBrCP");
          parseContractFromMap(contractMap, tenantName, warnings);
        }
        // Check for fvAEPg directly under tenant (uncommon but possible)
        else if (childMap.containsKey("fvAEPg")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> epgMap = (Map<String, Object>) childMap.get("fvAEPg");
          parseEpgFromMap(epgMap, tenantName, null, warnings);
        }
      }
    }
  }

  /** Parses a VRF from a raw map structure. */
  private void parseVrfFromMap(Map<String, Object> vrfMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) vrfMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String vrfName = (String) attrs.get("name");
    if (vrfName == null || vrfName.isEmpty()) {
      return;
    }

    // Create VRF with fully qualified name (tenant:vrf)
    String fqVrfName = tenantName + ":" + vrfName;
    AciVrfModel vrf = getOrCreateVrf(fqVrfName);
    vrf.setTenant(tenantName);
    vrf.setDescription((String) attrs.get("descr"));

    // Also add VRF to tenant's VRF map (using fully-qualified name for global uniqueness)
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getVrfs().put(fqVrfName, vrf);
  }

  /** Parses a Bridge Domain from a raw map structure. */
  private void parseBridgeDomainFromMap(
      Map<String, Object> bdMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) bdMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String bdName = (String) attrs.get("name");
    if (bdName == null || bdName.isEmpty()) {
      return;
    }

    // Create Bridge Domain with fully qualified name
    String fqBdName = tenantName + ":" + bdName;
    BridgeDomain bd = getOrCreateBridgeDomain(fqBdName);
    bd.setTenant(tenantName);
    bd.setDescription((String) attrs.get("descr"));

    // Parse VRF association from children (fvRsCtx) and subnets (fvSubnet)
    if (bdMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) bdMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          if (childMap.containsKey("fvRsCtx")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsCtxMap = (Map<String, Object>) childMap.get("fvRsCtx");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsCtxAttrs = (Map<String, Object>) rsCtxMap.get("attributes");
            if (rsCtxAttrs != null) {
              String tnFvCtxName = (String) rsCtxAttrs.get("tnFvCtxName");
              if (tnFvCtxName != null) {
                // Store the fully-qualified VRF name
                bd.setVrf(tenantName + ":" + tnFvCtxName);
              }
            }
          } else if (childMap.containsKey("fvSubnet")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> subnetMap = (Map<String, Object>) childMap.get("fvSubnet");
            @SuppressWarnings("unchecked")
            Map<String, Object> subnetAttrs = (Map<String, Object>) subnetMap.get("attributes");
            if (subnetAttrs != null) {
              String ip = (String) subnetAttrs.get("ip");
              if (ip != null && !ip.isEmpty()) {
                bd.getSubnets().add(ip);
              }
            }
          }
        }
      }
    }

    // Also add BridgeDomain to tenant's BD map (using fully-qualified name for global uniqueness)
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getBridgeDomains().put(fqBdName, bd);
  }

  /** Parses an Application Profile and its contained EPGs. */
  private void parseApplicationProfileFromMap(
      Map<String, Object> apMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) apMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String apName = (String) attrs.get("name");

    // Parse children for EPGs
    if (apMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) apMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          if (childMap.containsKey("fvAEPg")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> epgMap = (Map<String, Object>) childMap.get("fvAEPg");
            parseEpgFromMap(epgMap, tenantName, apName, warnings);
          }
        }
      }
    }
  }

  /** Parses an EPG from a raw map structure. */
  private void parseEpgFromMap(
      Map<String, Object> epgMap, String tenantName, @Nullable String apName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) epgMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String epgName = (String) attrs.get("name");
    if (epgName == null || epgName.isEmpty()) {
      return;
    }

    // Create EPG with fully qualified name
    String fqEpgName = tenantName + ":" + (apName != null ? apName + ":" : "") + epgName;
    Epg epg = getOrCreateEpg(fqEpgName);
    epg.setTenant(tenantName);
    epg.setDescription((String) attrs.get("descr"));

    // Parse children for bridge domain association and contract references
    if (epgMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) epgMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // Bridge domain association
          if (childMap.containsKey("fvRsBd")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsBdMap = (Map<String, Object>) childMap.get("fvRsBd");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsBdAttrs = (Map<String, Object>) rsBdMap.get("attributes");
            if (rsBdAttrs != null) {
              String tnFvBDName = (String) rsBdAttrs.get("tnFvBDName");
              if (tnFvBDName != null) {
                // Store the fully-qualified BD name
                epg.setBridgeDomain(tenantName + ":" + tnFvBDName);
              }
            }
          }

          // Provided contracts
          if (childMap.containsKey("fvRsProv")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvMap = (Map<String, Object>) childMap.get("fvRsProv");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvAttrs = (Map<String, Object>) rsProvMap.get("attributes");
            if (rsProvAttrs != null) {
              String tnVzBrCPName = (String) rsProvAttrs.get("tnVzBrCPName");
              if (tnVzBrCPName != null) {
                String fqContractName = tenantName + ":" + tnVzBrCPName;
                epg.getProvidedContracts().add(fqContractName);
              }
            }
          }

          // Consumed contracts
          if (childMap.containsKey("fvRsCons")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsMap = (Map<String, Object>) childMap.get("fvRsCons");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsAttrs = (Map<String, Object>) rsConsMap.get("attributes");
            if (rsConsAttrs != null) {
              String tnVzBrCPName = (String) rsConsAttrs.get("tnVzBrCPName");
              if (tnVzBrCPName != null) {
                String fqContractName = tenantName + ":" + tnVzBrCPName;
                epg.getConsumedContracts().add(fqContractName);
              }
            }
          }
        }
      }
    }

    // Also add EPG to tenant's EPG map (using fully-qualified name for global uniqueness)
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getEpgs().put(fqEpgName, epg);
  }

  /** Parses a Contract from a raw map structure. */
  private void parseContractFromMap(
      Map<String, Object> contractMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) contractMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String contractName = (String) attrs.get("name");
    if (contractName == null || contractName.isEmpty()) {
      return;
    }

    // Create Contract with fully qualified name
    String fqContractName = tenantName + ":" + contractName;
    Contract contract = getOrCreateContract(fqContractName);
    contract.setTenant(tenantName);
    contract.setDescription((String) attrs.get("descr"));
    contract.setScope((String) attrs.get("scope"));

    // Parse children for subjects
    if (contractMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) contractMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          if (childMap.containsKey("vzSubj")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> subjMap = (Map<String, Object>) childMap.get("vzSubj");
            parseContractSubjectFromMap(subjMap, contract, warnings);
          }
        }
      }
    }

    // Also add Contract to tenant's contract map (using fully-qualified name for global uniqueness)
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getContracts().put(fqContractName, contract);
  }

  /** Parses a contract subject from a raw map structure. */
  private void parseContractSubjectFromMap(
      Map<String, Object> subjMap, Contract contract, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) subjMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String subjName = (String) attrs.get("name");
    Contract.Subject subject = new Contract.Subject();
    subject.setName(subjName);

    // Parse children for filter references
    if (subjMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) subjMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // Filter reference (vzRsSubjFiltAtt)
          if (childMap.containsKey("vzRsSubjFiltAtt")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> filtMap = (Map<String, Object>) childMap.get("vzRsSubjFiltAtt");
            @SuppressWarnings("unchecked")
            Map<String, Object> filtAttrs = (Map<String, Object>) filtMap.get("attributes");
            if (filtAttrs != null) {
              String tnVzFilterName = (String) filtAttrs.get("tnVzFilterName");
              String action = (String) filtAttrs.get("action");

              Contract.Filter filter = new Contract.Filter();
              filter.setName(tnVzFilterName);
              if ("deny".equalsIgnoreCase(action)) {
                // Action is deny - mark on filter
              }
              subject.getFilters().add(filter);
            }
          }
        }
      }
    }

    contract.getSubjects().add(subject);
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_HOSTNAME)
  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  /**
   * Returns the map of tenant configurations.
   *
   * @return An immutable map of tenant names to tenant configurations
   */
  public @Nonnull Map<String, Tenant> getTenants() {
    return _tenants;
  }

  @JsonProperty(PROP_TENANTS)
  public void setTenants(Map<String, Tenant> tenants) {
    _tenants = new TreeMap<>(tenants);
  }

  /**
   * Returns the map of bridge domain configurations.
   *
   * @return An immutable map of bridge domain names to bridge domain configurations
   */
  public @Nonnull Map<String, BridgeDomain> getBridgeDomains() {
    return _bridgeDomains;
  }

  @JsonProperty(PROP_BRIDGE_DOMAINS)
  public void setBridgeDomains(Map<String, BridgeDomain> bridgeDomains) {
    _bridgeDomains = new TreeMap<>(bridgeDomains);
  }

  /**
   * Returns the map of VRF configurations.
   *
   * @return An immutable map of VRF names to VRF configurations
   */
  public @Nonnull Map<String, AciVrfModel> getVrfs() {
    return _vrfs;
  }

  @JsonProperty(PROP_VRFS)
  public void setVrfs(Map<String, AciVrfModel> vrfs) {
    _vrfs = new TreeMap<>(vrfs);
  }

  /**
   * Returns the map of EPG configurations.
   *
   * @return An immutable map of EPG names to EPG configurations
   */
  public @Nonnull Map<String, Epg> getEpgs() {
    return _epgs;
  }

  @JsonProperty(PROP_EPGS)
  public void setEpgs(Map<String, Epg> epgs) {
    _epgs = new TreeMap<>(epgs);
  }

  /**
   * Returns the map of contract configurations.
   *
   * @return An immutable map of contract names to contract configurations
   */
  public @Nonnull Map<String, Contract> getContracts() {
    return _contracts;
  }

  @JsonProperty(PROP_CONTRACTS)
  public void setContracts(Map<String, Contract> contracts) {
    _contracts = new TreeMap<>(contracts);
  }

  /**
   * Returns the map of fabric node configurations.
   *
   * @return An immutable map of node IDs to fabric node configurations
   */
  public @Nonnull Map<String, FabricNode> getFabricNodes() {
    return _fabricNodes;
  }

  @JsonProperty(PROP_FABRIC_NODES)
  public void setFabricNodes(Map<String, FabricNode> fabricNodes) {
    _fabricNodes = new TreeMap<>(fabricNodes);
  }

  /**
   * Returns the map of L3Out configurations.
   *
   * @return An immutable map of L3Out names to L3Out configurations
   */
  public @Nonnull Map<String, L3Out> getL3Outs() {
    return _l3Outs;
  }

  @JsonProperty(PROP_L3_OUTS)
  public void setL3Outs(Map<String, L3Out> l3Outs) {
    _l3Outs = new TreeMap<>(l3Outs);
  }

  /**
   * Gets or creates a tenant with the given name.
   *
   * @param name The tenant name
   * @return The existing or newly created tenant
   */
  public @Nonnull Tenant getOrCreateTenant(String name) {
    return _tenants.computeIfAbsent(name, Tenant::new);
  }

  /**
   * Gets or creates a VRF with the given name.
   *
   * @param name The VRF name
   * @return The existing or newly created VRF
   */
  public @Nonnull AciVrfModel getOrCreateVrf(String name) {
    return _vrfs.computeIfAbsent(name, AciVrfModel::new);
  }

  /**
   * Gets or creates a bridge domain with the given name.
   *
   * @param name The bridge domain name
   * @return The existing or newly created bridge domain
   */
  public @Nonnull BridgeDomain getOrCreateBridgeDomain(String name) {
    return _bridgeDomains.computeIfAbsent(name, BridgeDomain::new);
  }

  /**
   * Gets or creates an EPG with the given name.
   *
   * @param name The EPG name
   * @return The existing or newly created EPG
   */
  public @Nonnull Epg getOrCreateEpg(String name) {
    return _epgs.computeIfAbsent(name, Epg::new);
  }

  /**
   * Gets or creates a contract with the given name.
   *
   * @param name The contract name
   * @return The existing or newly created contract
   */
  public @Nonnull Contract getOrCreateContract(String name) {
    return _contracts.computeIfAbsent(name, Contract::new);
  }

  /**
   * Gets or creates an L3Out with the given name.
   *
   * @param name The L3Out name
   * @return The existing or newly created L3Out
   */
  public @Nonnull L3Out getOrCreateL3Out(String name) {
    return _l3Outs.computeIfAbsent(name, L3Out::new);
  }

  /**
   * Converts this ACI configuration to vendor-independent Batfish configurations.
   *
   * <p>This method creates the vendor-independent Configuration objects that represent the ACI
   * fabric's network topology and policies in Batfish's vendor-independent model.
   *
   * <p>The conversion is delegated to the {@link AciConversion} utility class which handles the
   * detailed mapping from ACI structures to Batfish Configuration objects.
   *
   * @return A list of Configuration objects representing this ACI fabric
   * @throws VendorConversionException If conversion fails
   */
  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    // Delegate to the AciConversion utility class
    Warnings warnings = getWarnings();
    if (warnings == null) {
      warnings = new Warnings();
      setWarnings(warnings);
    }

    // Convert to VI configurations
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(this, warnings);

    // Return as a list
    return ImmutableList.copyOf(configs.values());
  }

  /**
   * Finalizes the configuration structures after parsing is complete.
   *
   * <p>This method makes internal data structures immutable and should be called once at the end of
   * parsing and extraction.
   */
  public void finalizeStructures() {
    _tenants = ImmutableMap.copyOf(_tenants);
    _bridgeDomains = ImmutableMap.copyOf(_bridgeDomains);
    _vrfs = ImmutableMap.copyOf(_vrfs);
    _epgs = ImmutableMap.copyOf(_epgs);
    _contracts = ImmutableMap.copyOf(_contracts);
    _fabricNodes = ImmutableMap.copyOf(_fabricNodes);
    _l3Outs = ImmutableMap.copyOf(_l3Outs);
  }

  /**
   * ACI Tenant configuration.
   *
   * <p>A tenant is a logical container for application policies in ACI. It contains bridge domains,
   * VRFs, EPGs, and contracts.
   */
  public static class Tenant {
    private final String _name;
    private Map<String, BridgeDomain> _bridgeDomains;
    private Map<String, AciVrfModel> _vrfs;
    private Map<String, Epg> _epgs;
    private Map<String, Contract> _contracts;

    public Tenant(String name) {
      _name = name;
      _bridgeDomains = new TreeMap<>();
      _vrfs = new TreeMap<>();
      _epgs = new TreeMap<>();
      _contracts = new TreeMap<>();
    }

    public String getName() {
      return _name;
    }

    public Map<String, BridgeDomain> getBridgeDomains() {
      return _bridgeDomains;
    }

    public void setBridgeDomains(Map<String, BridgeDomain> bridgeDomains) {
      _bridgeDomains = new TreeMap<>(bridgeDomains);
    }

    public Map<String, AciVrfModel> getVrfs() {
      return _vrfs;
    }

    public void setVrfs(Map<String, AciVrfModel> vrfs) {
      _vrfs = new TreeMap<>(vrfs);
    }

    public Map<String, Epg> getEpgs() {
      return _epgs;
    }

    public void setEpgs(Map<String, Epg> epgs) {
      _epgs = new TreeMap<>(epgs);
    }

    public Map<String, Contract> getContracts() {
      return _contracts;
    }

    public void setContracts(Map<String, Contract> contracts) {
      _contracts = new TreeMap<>(contracts);
    }
  }

  /**
   * ACI Bridge Domain configuration.
   *
   * <p>A bridge domain is a Layer 2 forwarding domain within a tenant. It contains subnets and can
   * be associated with a VRF.
   */
  public static class BridgeDomain {
    private final String _name;
    private String _vrf;
    private String _tenant;
    private List<String> _subnets;
    private String _description;

    public BridgeDomain(String name) {
      _name = name;
      _subnets = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getVrf() {
      return _vrf;
    }

    public void setVrf(String vrf) {
      _vrf = vrf;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public List<String> getSubnets() {
      return _subnets;
    }

    public void setSubnets(List<String> subnets) {
      _subnets = new ArrayList<>(subnets);
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }
  }

  /**
   * ACI End Point Group (EPG) configuration.
   *
   * <p>An EPG is a collection of endpoints that share similar policy requirements. EPGs are the
   * fundamental building blocks for ACI policy application.
   */
  public static class Epg {
    private final String _name;
    private String _tenant;
    private String _bridgeDomain;
    private String _description;
    private List<String> _providedContracts;
    private List<String> _consumedContracts;

    public Epg(String name) {
      _name = name;
      _providedContracts = new ArrayList<>();
      _consumedContracts = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getBridgeDomain() {
      return _bridgeDomain;
    }

    public void setBridgeDomain(String bridgeDomain) {
      _bridgeDomain = bridgeDomain;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public List<String> getProvidedContracts() {
      return _providedContracts;
    }

    public void setProvidedContracts(List<String> providedContracts) {
      _providedContracts = new ArrayList<>(providedContracts);
    }

    public List<String> getConsumedContracts() {
      return _consumedContracts;
    }

    public void setConsumedContracts(List<String> consumedContracts) {
      _consumedContracts = new ArrayList<>(consumedContracts);
    }
  }

  /**
   * ACI Contract configuration.
   *
   * <p>A contract defines the allowed communication between EPGs. It contains subjects and filters
   * that specify the protocols and ports for communication.
   */
  public static class Contract {
    private final String _name;
    private String _tenant;
    private String _description;
    private List<Subject> _subjects;
    private String _scope;

    public Contract(String name) {
      _name = name;
      _subjects = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public List<Subject> getSubjects() {
      return _subjects;
    }

    public void setSubjects(List<Subject> subjects) {
      _subjects = new ArrayList<>(subjects);
    }

    public @Nullable String getScope() {
      return _scope;
    }

    public void setScope(String scope) {
      _scope = scope;
    }

    /** A contract subject contains filters that define specific traffic rules. */
    public static class Subject {
      private String _name;
      private List<Filter> _filters;

      public Subject() {
        _filters = new ArrayList<>();
      }

      public @Nullable String getName() {
        return _name;
      }

      public void setName(String name) {
        _name = name;
      }

      public List<Filter> getFilters() {
        return _filters;
      }

      public void setFilters(List<Filter> filters) {
        _filters = new ArrayList<>(filters);
      }
    }

    /** A contract filter defines specific traffic matching criteria (protocols, ports). */
    public static class Filter {
      private String _name;
      private String _etherType;
      private String _ipProtocol;
      private List<String> _sourcePorts;
      private List<String> _destinationPorts;
      private String _sourceAddress;
      private String _destinationAddress;
      private String _icmpType;
      private String _icmpCode;
      private String _arpOpcode;
      private Boolean _stateful;

      public Filter() {
        _sourcePorts = new ArrayList<>();
        _destinationPorts = new ArrayList<>();
      }

      public @Nullable String getName() {
        return _name;
      }

      public void setName(String name) {
        _name = name;
      }

      public @Nullable String getEtherType() {
        return _etherType;
      }

      public void setEtherType(String etherType) {
        _etherType = etherType;
      }

      public @Nullable String getIpProtocol() {
        return _ipProtocol;
      }

      public void setIpProtocol(String ipProtocol) {
        _ipProtocol = ipProtocol;
      }

      public List<String> getSourcePorts() {
        return _sourcePorts;
      }

      public void setSourcePorts(List<String> sourcePorts) {
        _sourcePorts = new ArrayList<>(sourcePorts);
      }

      public List<String> getDestinationPorts() {
        return _destinationPorts;
      }

      public void setDestinationPorts(List<String> destinationPorts) {
        _destinationPorts = new ArrayList<>(destinationPorts);
      }

      public @Nullable String getSourceAddress() {
        return _sourceAddress;
      }

      public void setSourceAddress(String sourceAddress) {
        _sourceAddress = sourceAddress;
      }

      public @Nullable String getDestinationAddress() {
        return _destinationAddress;
      }

      public void setDestinationAddress(String destinationAddress) {
        _destinationAddress = destinationAddress;
      }

      public @Nullable String getIcmpType() {
        return _icmpType;
      }

      public void setIcmpType(String icmpType) {
        _icmpType = icmpType;
      }

      public @Nullable String getIcmpCode() {
        return _icmpCode;
      }

      public void setIcmpCode(String icmpCode) {
        _icmpCode = icmpCode;
      }

      public @Nullable String getArpOpcode() {
        return _arpOpcode;
      }

      public void setArpOpcode(String arpOpcode) {
        _arpOpcode = arpOpcode;
      }

      public @Nullable Boolean getStateful() {
        return _stateful;
      }

      public void setStateful(Boolean stateful) {
        _stateful = stateful;
      }
    }
  }

  /**
   * ACI Fabric Node configuration.
   *
   * <p>A fabric node represents a physical or virtual switch in the ACI fabric. It contains
   * interface and connectivity information.
   */
  public static class FabricNode {
    private String _nodeId;
    private String _name;
    private String _role;
    private String _podId;
    private Map<String, Interface> _interfaces;

    public FabricNode() {
      _interfaces = new TreeMap<>();
    }

    public @Nullable String getNodeId() {
      return _nodeId;
    }

    public void setNodeId(String nodeId) {
      _nodeId = nodeId;
    }

    public @Nullable String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public @Nullable String getRole() {
      return _role;
    }

    public void setRole(String role) {
      _role = role;
    }

    public @Nullable String getPodId() {
      return _podId;
    }

    public void setPodId(String podId) {
      _podId = podId;
    }

    public Map<String, Interface> getInterfaces() {
      return _interfaces;
    }

    public void setInterfaces(Map<String, Interface> interfaces) {
      _interfaces = new TreeMap<>(interfaces);
    }

    /** Interface configuration on a fabric node. */
    public static class Interface {
      private String _name;
      private String _type;
      private String _description;
      private boolean _enabled;
      private String _epg;
      private String _vlan;

      public Interface() {
        _enabled = true;
      }

      public @Nullable String getName() {
        return _name;
      }

      public void setName(String name) {
        _name = name;
      }

      public @Nullable String getType() {
        return _type;
      }

      public void setType(String type) {
        _type = type;
      }

      public @Nullable String getDescription() {
        return _description;
      }

      public void setDescription(String description) {
        _description = description;
      }

      public boolean isEnabled() {
        return _enabled;
      }

      public void setEnabled(boolean enabled) {
        _enabled = enabled;
      }

      public @Nullable String getEpg() {
        return _epg;
      }

      public void setEpg(String epg) {
        _epg = epg;
      }

      public @Nullable String getVlan() {
        return _vlan;
      }

      public void setVlan(String vlan) {
        _vlan = vlan;
      }
    }
  }

  /*
   * ========================================================================
   * Native ACI JSON POJOs
   * ========================================================================
   *
   * The following classes represent the native ACI JSON structure with polUni
   * as the root and nested children arrays. These are used by Jackson to
   * deserialize the ACI JSON export.
   */

  /**
   * Root element of the ACI JSON structure. The polUni (Policy Universe) is the top-level container
   * for all ACI configuration.
   *
   * <p>This is an internal version with PolUniChild for deserialization. The standalone {@link
   * AciPolUni} uses AciChild for a more generic structure.
   */
  @JsonDeserialize(using = AciPolUniDeserializer.class)
  public static class AciPolUniInternal {
    private AciPolUniInternalAttributes _attributes;
    private List<PolUniChild> _children;

    public @Nullable AciPolUniInternalAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciPolUniInternalAttributes attributes) {
      _attributes = attributes;
    }

    public @Nullable List<PolUniChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<PolUniChild> children) {
      _children = children;
    }

    /** Attributes of the polUni root element. */
    public static class AciPolUniInternalAttributes {
      @JsonProperty("annotation")
      private @Nullable String _annotation;

      @JsonProperty("dn")
      private @Nullable String _distinguishedName;

      @JsonProperty("name")
      private @Nullable String _name;

      @JsonProperty("nameAlias")
      private @Nullable String _nameAlias;

      @JsonProperty("userdom")
      private @Nullable String _userDomain;

      public @Nullable String getName() {
        return _name;
      }

      public void setName(@Nullable String name) {
        _name = name;
      }
    }

    /** Child elements at the polUni level. */
    public static class PolUniChild {
      private @Nullable AciTenant _fvTenant;

      private @Nullable AciFabricInst _fabricInst;

      @JsonProperty("fvTenant")
      public @Nullable AciTenant getFvTenant() {
        return _fvTenant;
      }

      @JsonProperty("fvTenant")
      public void setFvTenant(@Nullable AciTenant fvTenant) {
        _fvTenant = fvTenant;
      }

      @JsonProperty("fabricInst")
      public @Nullable AciFabricInst getFabricInst() {
        return _fabricInst;
      }

      @JsonProperty("fabricInst")
      public void setFabricInst(@Nullable AciFabricInst fabricInst) {
        _fabricInst = fabricInst;
      }
    }
  }

  /**
   * Custom deserializer for AciPolUniInternal that handles the heterogenous children array
   * structure.
   */
  public static class AciPolUniDeserializer extends JsonDeserializer<AciPolUniInternal> {
    @Override
    public AciPolUniInternal deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      AciPolUniInternal polUni = new AciPolUniInternal();
      JsonNode node = p.getCodec().readTree(p);

      // Parse attributes
      JsonNode attributesNode = node.get("attributes");
      if (attributesNode != null) {
        AciPolUniInternal.AciPolUniInternalAttributes attributes =
            new AciPolUniInternal.AciPolUniInternalAttributes();
        if (attributesNode.has("name")) {
          attributes.setName(attributesNode.get("name").asText());
        }
        polUni.setAttributes(attributes);
      }

      // Parse children
      JsonNode childrenNode = node.get("children");
      if (childrenNode != null && childrenNode.isArray()) {
        com.google.common.collect.ImmutableList.Builder<AciPolUniInternal.PolUniChild> children =
            com.google.common.collect.ImmutableList.builder();
        for (JsonNode childNode : childrenNode) {
          AciPolUniInternal.PolUniChild child = new AciPolUniInternal.PolUniChild();

          // Parse fvTenant
          JsonNode fvTenantNode = childNode.get("fvTenant");
          if (fvTenantNode != null) {
            child.setFvTenant(parseFvTenant(fvTenantNode));
          }

          // Parse fabricInst
          JsonNode fabricInstNode = childNode.get("fabricInst");
          if (fabricInstNode != null) {
            child.setFabricInst(parseFabricInst(fabricInstNode));
          }

          children.add(child);
        }
        polUni.setChildren(children.build());
      }

      return polUni;
    }

    private AciTenant parseFvTenant(JsonNode node) {
      AciTenant tenant = new AciTenant();

      // Parse attributes
      JsonNode attrsNode = node.get("attributes");
      if (attrsNode != null) {
        AciTenant.AciTenantAttributes attrs = new AciTenant.AciTenantAttributes();
        if (attrsNode.has("name")) {
          attrs.setName(attrsNode.get("name").asText());
        }
        if (attrsNode.has("descr")) {
          attrs.setDescription(attrsNode.get("descr").asText());
        }
        tenant.setAttributes(attrs);
      }

      // Parse children as raw list
      JsonNode childrenNode = node.get("children");
      if (childrenNode != null && childrenNode.isArray()) {
        com.google.common.collect.ImmutableList.Builder<Object> children =
            com.google.common.collect.ImmutableList.builder();
        for (JsonNode childNode : childrenNode) {
          // Keep as raw map to preserve structure
          children.add(convertNodeToMap(childNode));
        }
        tenant.setChildren(children.build());
      }

      return tenant;
    }

    private AciFabricInst parseFabricInst(JsonNode node) {
      AciFabricInst fabricInst = new AciFabricInst();

      JsonNode attrsNode = node.get("attributes");
      if (attrsNode != null) {
        AciFabricInst.AciFabricInstAttributes attrs = new AciFabricInst.AciFabricInstAttributes();
        if (attrsNode.has("dn")) {
          attrs.setDistinguishedName(attrsNode.get("dn").asText());
        }
        fabricInst.setAttributes(attrs);
      }

      JsonNode childrenNode = node.get("children");
      if (childrenNode != null && childrenNode.isArray()) {
        com.google.common.collect.ImmutableList.Builder<AciFabricInst.FabricInstChild> children =
            com.google.common.collect.ImmutableList.builder();
        for (JsonNode childNode : childrenNode) {
          AciFabricInst.FabricInstChild child = new AciFabricInst.FabricInstChild();

          JsonNode protPolNode = childNode.get("fabricProtPol");
          if (protPolNode != null) {
            child.setFabricProtPol(parseFabricProtPol(protPolNode));
          }

          children.add(child);
        }
        fabricInst.setChildren(children.build());
      }

      return fabricInst;
    }

    private AciFabricProtPol parseFabricProtPol(JsonNode node) {
      AciFabricProtPol protPol = new AciFabricProtPol();

      JsonNode childrenNode = node.get("children");
      if (childrenNode != null && childrenNode.isArray()) {
        com.google.common.collect.ImmutableList.Builder<AciFabricProtPol.FabricProtPolChild>
            children = com.google.common.collect.ImmutableList.builder();
        for (JsonNode childNode : childrenNode) {
          AciFabricProtPol.FabricProtPolChild child = new AciFabricProtPol.FabricProtPolChild();

          JsonNode explicitNode = childNode.get("fabricExplicitGEp");
          if (explicitNode != null) {
            child.setFabricExplicitGEp(parseFabricExplicitGEp(explicitNode));
          }

          children.add(child);
        }
        protPol.setChildren(children.build());
      }

      return protPol;
    }

    private AciFabricExplicitGEp parseFabricExplicitGEp(JsonNode node) {
      AciFabricExplicitGEp explicitEp = new AciFabricExplicitGEp();

      JsonNode childrenNode = node.get("children");
      if (childrenNode != null && childrenNode.isArray()) {
        com.google.common.collect.ImmutableList.Builder<AciFabricExplicitGEp.FabricExplicitGEpChild>
            children = com.google.common.collect.ImmutableList.builder();
        for (JsonNode childNode : childrenNode) {
          AciFabricExplicitGEp.FabricExplicitGEpChild child =
              new AciFabricExplicitGEp.FabricExplicitGEpChild();

          JsonNode nodePepNode = childNode.get("fabricNodePEp");
          if (nodePepNode != null) {
            child.setFabricNodePEp(parseFabricNodePEp(nodePepNode));
          }

          children.add(child);
        }
        explicitEp.setChildren(children.build());
      }

      return explicitEp;
    }

    private AciFabricNodePEp parseFabricNodePEp(JsonNode node) {
      AciFabricNodePEp nodePep = new AciFabricNodePEp();

      JsonNode attrsNode = node.get("attributes");
      if (attrsNode != null) {
        AciFabricNodePEp.AciFabricNodePEpAttributes attrs =
            new AciFabricNodePEp.AciFabricNodePEpAttributes();
        if (attrsNode.has("id")) {
          attrs.setId(attrsNode.get("id").asText());
        }
        if (attrsNode.has("name")) {
          attrs.setName(attrsNode.get("name").asText());
        }
        if (attrsNode.has("podId")) {
          attrs.setPodId(attrsNode.get("podId").asText());
        }
        if (attrsNode.has("role")) {
          attrs.setRole(attrsNode.get("role").asText());
        }
        if (attrsNode.has("descr")) {
          attrs.setDescription(attrsNode.get("descr").asText());
        }
        nodePep.setAttributes(attrs);
      }

      return nodePep;
    }

    private Map<String, Object> convertNodeToMap(JsonNode node) {
      Map<String, Object> map = new HashMap<>();
      Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> entry = fields.next();
        map.put(entry.getKey(), convertJsonNode(entry.getValue()));
      }
      return map;
    }

    private Object convertJsonNode(JsonNode node) {
      if (node.isObject()) {
        return convertNodeToMap(node);
      } else if (node.isArray()) {
        List<Object> list = new ArrayList<>();
        for (JsonNode item : node) {
          list.add(convertJsonNode(item));
        }
        return list;
      } else if (node.isTextual()) {
        return node.asText();
      } else if (node.isInt() || node.isShort()) {
        return node.asInt();
      } else if (node.isLong()) {
        return node.asLong();
      } else if (node.isBoolean()) {
        return node.asBoolean();
      } else if (node.isNull()) {
        return null;
      } else if (node.isDouble() || node.isFloat()) {
        return node.asDouble();
      } else {
        return node.toString();
      }
    }
  }

  /** Represents the fabricInst element containing fabric-wide configuration. */
  public static class AciFabricInst {
    private AciFabricInstAttributes _attributes;
    private List<FabricInstChild> _children;

    public @Nullable AciFabricInstAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciFabricInstAttributes attributes) {
      _attributes = attributes;
    }

    public @Nullable List<FabricInstChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<FabricInstChild> children) {
      _children = children;
    }

    /** Attributes of fabricInst. */
    public static class AciFabricInstAttributes {
      private @Nullable String _distinguishedName;

      private @Nullable String _name;

      @JsonProperty("dn")
      public @Nullable String getDistinguishedName() {
        return _distinguishedName;
      }

      @JsonProperty("dn")
      public void setDistinguishedName(@Nullable String distinguishedName) {
        _distinguishedName = distinguishedName;
      }

      @JsonProperty("name")
      public @Nullable String getName() {
        return _name;
      }

      @JsonProperty("name")
      public void setName(@Nullable String name) {
        _name = name;
      }
    }

    /** Child elements of fabricInst. */
    public static class FabricInstChild {
      private @Nullable AciFabricProtPol _fabricProtPol;

      @JsonProperty("fabricProtPol")
      public @Nullable AciFabricProtPol getFabricProtPol() {
        return _fabricProtPol;
      }

      @JsonProperty("fabricProtPol")
      public void setFabricProtPol(@Nullable AciFabricProtPol fabricProtPol) {
        _fabricProtPol = fabricProtPol;
      }
    }
  }

  /** Represents the fabricProtPol element containing fabric protection policies. */
  public static class AciFabricProtPol {
    private List<FabricProtPolChild> _children;

    public @Nullable List<FabricProtPolChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<FabricProtPolChild> children) {
      _children = children;
    }

    /** Child elements of fabricProtPol. */
    public static class FabricProtPolChild {
      private @Nullable AciFabricExplicitGEp _fabricExplicitGEp;

      @JsonProperty("fabricExplicitGEp")
      public @Nullable AciFabricExplicitGEp getFabricExplicitGEp() {
        return _fabricExplicitGEp;
      }

      @JsonProperty("fabricExplicitGEp")
      public void setFabricExplicitGEp(@Nullable AciFabricExplicitGEp fabricExplicitGEp) {
        _fabricExplicitGEp = fabricExplicitGEp;
      }
    }
  }

  /** Represents the fabricExplicitGEp element containing explicit fabric endpoints. */
  public static class AciFabricExplicitGEp {
    private List<FabricExplicitGEpChild> _children;

    public @Nullable List<FabricExplicitGEpChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<FabricExplicitGEpChild> children) {
      _children = children;
    }

    /** Child elements of fabricExplicitGEp. */
    public static class FabricExplicitGEpChild {
      private @Nullable AciFabricNodePEp _fabricNodePEp;

      @JsonProperty("fabricNodePEp")
      public @Nullable AciFabricNodePEp getFabricNodePEp() {
        return _fabricNodePEp;
      }

      @JsonProperty("fabricNodePEp")
      public void setFabricNodePEp(@Nullable AciFabricNodePEp fabricNodePEp) {
        _fabricNodePEp = fabricNodePEp;
      }
    }
  }

  /** Represents a fabric node endpoint (fabricNodePEp). */
  public static class AciFabricNodePEp {
    private AciFabricNodePEpAttributes _attributes;

    public @Nullable AciFabricNodePEpAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciFabricNodePEpAttributes attributes) {
      _attributes = attributes;
    }

    /** Attributes of a fabric node. */
    public static class AciFabricNodePEpAttributes {
      private @Nullable String _annotation;
      private @Nullable String _description;
      private @Nullable String _distinguishedName;
      private @Nullable String _id;
      private @Nullable String _name;
      private @Nullable String _nameAlias;
      private @Nullable String _podId;
      private @Nullable String _role;
      private @Nullable String _userDomain;

      @JsonProperty("annotation")
      public @Nullable String getAnnotation() {
        return _annotation;
      }

      @JsonProperty("annotation")
      public void setAnnotation(@Nullable String annotation) {
        _annotation = annotation;
      }

      @JsonProperty("descr")
      public @Nullable String getDescription() {
        return _description;
      }

      @JsonProperty("descr")
      public void setDescription(@Nullable String description) {
        _description = description;
      }

      @JsonProperty("dn")
      public @Nullable String getDistinguishedName() {
        return _distinguishedName;
      }

      @JsonProperty("dn")
      public void setDistinguishedName(@Nullable String distinguishedName) {
        _distinguishedName = distinguishedName;
      }

      @JsonProperty("id")
      public @Nullable String getId() {
        return _id;
      }

      @JsonProperty("id")
      public void setId(@Nullable String id) {
        _id = id;
      }

      @JsonProperty("name")
      public @Nullable String getName() {
        return _name;
      }

      @JsonProperty("name")
      public void setName(@Nullable String name) {
        _name = name;
      }

      @JsonProperty("nameAlias")
      public @Nullable String getNameAlias() {
        return _nameAlias;
      }

      @JsonProperty("nameAlias")
      public void setNameAlias(@Nullable String nameAlias) {
        _nameAlias = nameAlias;
      }

      @JsonProperty("podId")
      public @Nullable String getPodId() {
        return _podId;
      }

      @JsonProperty("podId")
      public void setPodId(@Nullable String podId) {
        _podId = podId;
      }

      @JsonProperty("role")
      public @Nullable String getRole() {
        return _role;
      }

      @JsonProperty("role")
      public void setRole(@Nullable String role) {
        _role = role;
      }

      @JsonProperty("userdom")
      public @Nullable String getUserDomain() {
        return _userDomain;
      }

      @JsonProperty("userdom")
      public void setUserDomain(@Nullable String userDomain) {
        _userDomain = userDomain;
      }
    }
  }

  /**
   * ACI L3Out (Layer 3 Outside) configuration.
   *
   * <p>An L3Out defines external connectivity for a tenant, including BGP peering, static routes,
   * OSPF configuration, and external EPGs (L3ExtEpg).
   */
  public static class L3Out {
    private final String _name;
    private String _tenant;
    private String _vrf;
    private String _description;
    private BgpProcess _bgpProcess;
    private List<BgpPeer> _bgpPeers;
    private List<StaticRoute> _staticRoutes;
    private OspfConfig _ospfConfig;
    private List<ExternalEpg> _externalEpgs;

    public L3Out(String name) {
      _name = name;
      _bgpPeers = new ArrayList<>();
      _staticRoutes = new ArrayList<>();
      _externalEpgs = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getVrf() {
      return _vrf;
    }

    public void setVrf(String vrf) {
      _vrf = vrf;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable BgpProcess getBgpProcess() {
      return _bgpProcess;
    }

    public void setBgpProcess(BgpProcess bgpProcess) {
      _bgpProcess = bgpProcess;
    }

    public List<BgpPeer> getBgpPeers() {
      return _bgpPeers;
    }

    public void setBgpPeers(List<BgpPeer> bgpPeers) {
      _bgpPeers = new ArrayList<>(bgpPeers);
    }

    public List<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
    }

    public void setStaticRoutes(List<StaticRoute> staticRoutes) {
      _staticRoutes = new ArrayList<>(staticRoutes);
    }

    public @Nullable OspfConfig getOspfConfig() {
      return _ospfConfig;
    }

    public void setOspfConfig(OspfConfig ospfConfig) {
      _ospfConfig = ospfConfig;
    }

    public List<ExternalEpg> getExternalEpgs() {
      return _externalEpgs;
    }

    public void setExternalEpgs(List<ExternalEpg> externalEpgs) {
      _externalEpgs = new ArrayList<>(externalEpgs);
    }
  }

  /**
   * BGP process configuration for L3Out.
   *
   * <p>Defines BGP process-level settings for an L3Out including AS number, router ID,
   * administrative distances, and BGP timers.
   */
  public static class BgpProcess {
    private Long _as;
    private String _routerId;
    private Integer _ebgpAdminCost;
    private Integer _ibgpAdminCost;
    private Integer _vrfAdminCost;
    private Integer _keepalive;
    private Integer _holdTime;

    public @Nullable Long getAs() {
      return _as;
    }

    public void setAs(Long as) {
      _as = as;
    }

    public @Nullable String getRouterId() {
      return _routerId;
    }

    public void setRouterId(String routerId) {
      _routerId = routerId;
    }

    public @Nullable Integer getEbgpAdminCost() {
      return _ebgpAdminCost;
    }

    public void setEbgpAdminCost(Integer ebgpAdminCost) {
      _ebgpAdminCost = ebgpAdminCost;
    }

    public @Nullable Integer getIbgpAdminCost() {
      return _ibgpAdminCost;
    }

    public void setIbgpAdminCost(Integer ibgpAdminCost) {
      _ibgpAdminCost = ibgpAdminCost;
    }

    public @Nullable Integer getVrfAdminCost() {
      return _vrfAdminCost;
    }

    public void setVrfAdminCost(Integer vrfAdminCost) {
      _vrfAdminCost = vrfAdminCost;
    }

    public @Nullable Integer getKeepalive() {
      return _keepalive;
    }

    public void setKeepalive(Integer keepalive) {
      _keepalive = keepalive;
    }

    public @Nullable Integer getHoldTime() {
      return _holdTime;
    }

    public void setHoldTime(Integer holdTime) {
      _holdTime = holdTime;
    }
  }

  /**
   * BGP peer configuration for L3Out.
   *
   * <p>Defines a BGP peer within an L3Out including peer address, AS numbers, policies, and route
   * target (route-map) configurations.
   */
  public static class BgpPeer {
    private String _peerAddress;
    private String _remoteAs;
    private String _localAs;
    private String _updateSourceInterface;
    private String _password;
    private String _description;
    private Boolean _ebgpMultihop;
    private Integer _ttl;
    private Boolean _routeReflectorClient;
    private Boolean _nextHopSelf;
    private Boolean _sendCommunities;
    private String _localPreference;
    private String _med;
    private String _importRouteMap;
    private String _exportRouteMap;
    private List<String> _routeTargets;
    private Integer _keepalive;
    private Integer _holdTime;

    public BgpPeer() {
      _routeTargets = new ArrayList<>();
    }

    public @Nullable String getPeerAddress() {
      return _peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
      _peerAddress = peerAddress;
    }

    public @Nullable String getRemoteAs() {
      return _remoteAs;
    }

    public void setRemoteAs(String remoteAs) {
      _remoteAs = remoteAs;
    }

    public @Nullable String getLocalAs() {
      return _localAs;
    }

    public void setLocalAs(String localAs) {
      _localAs = localAs;
    }

    public @Nullable String getUpdateSourceInterface() {
      return _updateSourceInterface;
    }

    public void setUpdateSourceInterface(String updateSourceInterface) {
      _updateSourceInterface = updateSourceInterface;
    }

    public @Nullable String getPassword() {
      return _password;
    }

    public void setPassword(String password) {
      _password = password;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable Boolean getEbgpMultihop() {
      return _ebgpMultihop;
    }

    public void setEbgpMultihop(Boolean ebgpMultihop) {
      _ebgpMultihop = ebgpMultihop;
    }

    public @Nullable Integer getTtl() {
      return _ttl;
    }

    public void setTtl(Integer ttl) {
      _ttl = ttl;
    }

    public @Nullable Boolean getRouteReflectorClient() {
      return _routeReflectorClient;
    }

    public void setRouteReflectorClient(Boolean routeReflectorClient) {
      _routeReflectorClient = routeReflectorClient;
    }

    public @Nullable Boolean getNextHopSelf() {
      return _nextHopSelf;
    }

    public void setNextHopSelf(Boolean nextHopSelf) {
      _nextHopSelf = nextHopSelf;
    }

    public @Nullable Boolean getSendCommunities() {
      return _sendCommunities;
    }

    public void setSendCommunities(Boolean sendCommunities) {
      _sendCommunities = sendCommunities;
    }

    public @Nullable String getLocalPreference() {
      return _localPreference;
    }

    public void setLocalPreference(String localPreference) {
      _localPreference = localPreference;
    }

    public @Nullable String getMed() {
      return _med;
    }

    public void setMed(String med) {
      _med = med;
    }

    public @Nullable String getImportRouteMap() {
      return _importRouteMap;
    }

    public void setImportRouteMap(String importRouteMap) {
      _importRouteMap = importRouteMap;
    }

    public @Nullable String getExportRouteMap() {
      return _exportRouteMap;
    }

    public void setExportRouteMap(String exportRouteMap) {
      _exportRouteMap = exportRouteMap;
    }

    /**
     * Returns the list of route targets (RTs) configured for this BGP peer. Route targets are used
     * in BGP route-maps to control route import/export.
     *
     * @return List of route target strings (e.g., "route-target:65000:1")
     */
    public List<String> getRouteTargets() {
      return _routeTargets;
    }

    public void setRouteTargets(List<String> routeTargets) {
      _routeTargets = new ArrayList<>(routeTargets);
    }

    public void addRouteTarget(String routeTarget) {
      if (_routeTargets == null) {
        _routeTargets = new ArrayList<>();
      }
      _routeTargets.add(routeTarget);
    }

    public @Nullable Integer getKeepalive() {
      return _keepalive;
    }

    public void setKeepalive(Integer keepalive) {
      _keepalive = keepalive;
    }

    public @Nullable Integer getHoldTime() {
      return _holdTime;
    }

    public void setHoldTime(Integer holdTime) {
      _holdTime = holdTime;
    }
  }

  /**
   * Static route configuration for L3Out.
   *
   * <p>Defines a static route within an L3Out including prefix, next hop, and associated
   * parameters.
   */
  public static class StaticRoute {
    private String _prefix;
    private String _nextHop;
    private String _nextHopInterface;
    private String _administrativeDistance;
    private String _tag;
    private String _track;

    public @Nullable String getPrefix() {
      return _prefix;
    }

    public void setPrefix(String prefix) {
      _prefix = prefix;
    }

    public @Nullable String getNextHop() {
      return _nextHop;
    }

    public void setNextHop(String nextHop) {
      _nextHop = nextHop;
    }

    public @Nullable String getNextHopInterface() {
      return _nextHopInterface;
    }

    public void setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
    }

    public @Nullable String getAdministrativeDistance() {
      return _administrativeDistance;
    }

    public void setAdministrativeDistance(String administrativeDistance) {
      _administrativeDistance = administrativeDistance;
    }

    public @Nullable String getTag() {
      return _tag;
    }

    public void setTag(String tag) {
      _tag = tag;
    }

    public @Nullable String getTrack() {
      return _track;
    }

    public void setTrack(String track) {
      _track = track;
    }
  }

  /**
   * OSPF configuration for L3Out.
   *
   * <p>Defines OSPF process settings and areas for an L3Out.
   */
  public static class OspfConfig {
    private String _processId;
    private Map<String, OspfArea> _areas;

    public OspfConfig() {
      _areas = new TreeMap<>();
    }

    public @Nullable String getProcessId() {
      return _processId;
    }

    public void setProcessId(String processId) {
      _processId = processId;
    }

    public Map<String, OspfArea> getAreas() {
      return _areas;
    }

    public void setAreas(Map<String, OspfArea> areas) {
      _areas = new TreeMap<>(areas);
    }
  }

  /**
   * OSPF area configuration for L3Out.
   *
   * <p>Defines an OSPF area within an L3Out OSPF configuration.
   */
  public static class OspfArea {
    private String _areaId;
    private List<String> _networks;
    private String _areaType;

    public OspfArea() {
      _networks = new ArrayList<>();
    }

    public @Nullable String getAreaId() {
      return _areaId;
    }

    public void setAreaId(String areaId) {
      _areaId = areaId;
    }

    public List<String> getNetworks() {
      return _networks;
    }

    public void setNetworks(List<String> networks) {
      _networks = new ArrayList<>(networks);
    }

    public @Nullable String getAreaType() {
      return _areaType;
    }

    public void setAreaType(String areaType) {
      _areaType = areaType;
    }
  }

  /**
   * External EPG (L3ExtEpg) configuration for L3Out.
   *
   * <p>Defines an external endpoint group for external connectivity, including subnets and
   * associated interfaces.
   */
  public static class ExternalEpg {
    private final String _name;
    private List<String> _subnets;
    private String _nextHop;
    private String _interface;
    private String _description;

    public ExternalEpg(String name) {
      _name = name;
      _subnets = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public List<String> getSubnets() {
      return _subnets;
    }

    public void setSubnets(List<String> subnets) {
      _subnets = new ArrayList<>(subnets);
    }

    public @Nullable String getNextHop() {
      return _nextHop;
    }

    public void setNextHop(String nextHop) {
      _nextHop = nextHop;
    }

    public @Nullable String getInterface() {
      return _interface;
    }

    public void setInterface(String iface) {
      _interface = iface;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }
  }
}

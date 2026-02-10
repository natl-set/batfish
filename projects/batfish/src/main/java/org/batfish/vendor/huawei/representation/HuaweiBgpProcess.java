package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * Represents a BGP process on a Huawei VRP device.
 *
 * <p>This class stores BGP configuration information including AS number, neighbors, peer groups,
 * network announcements, and other BGP-specific settings.
 */
public class HuaweiBgpProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  /** BGP AS number */
  private long _asNum;

  /** Router ID */
  private @Nullable Ip _routerId;

  /** BGP neighbors: IP address to neighbor configuration */
  private Map<Ip, BgpPeerConfig> _neighbors;

  /** BGP peer groups */
  private Map<String, HuaweiBgpPeerGroup> _peerGroups;

  /** BGP address families */
  private Map<String, HuaweiBgpAddressFamily> _addressFamilies;

  /** BGP network announcements */
  private List<HuaweiBgpNetwork> _networks;

  /** BGP import-route (redistribution) configurations */
  private List<HuaweiBgpImportRoute> _importRoutes;

  public HuaweiBgpProcess(long asNum) {
    _asNum = asNum;
    _neighbors = new TreeMap<>();
    _peerGroups = new TreeMap<>();
    _addressFamilies = new TreeMap<>();
    _networks = new ArrayList<>();
    _importRoutes = new ArrayList<>();
  }

  /**
   * Gets the BGP AS number.
   *
   * @return The AS number
   */
  public long getAsNum() {
    return _asNum;
  }

  /**
   * Sets the BGP AS number.
   *
   * @param asNum The AS number to set
   */
  public void setAsNum(long asNum) {
    _asNum = asNum;
  }

  /**
   * Gets the router ID.
   *
   * @return The router ID, or null if not set
   */
  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  /**
   * Sets the router ID.
   *
   * @param routerId The router ID to set
   */
  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  /**
   * Gets the BGP neighbors.
   *
   * @return A map of neighbor IP addresses to neighbor configurations
   */
  public @Nonnull Map<Ip, BgpPeerConfig> getNeighbors() {
    return _neighbors;
  }

  /**
   * Sets the BGP neighbors.
   *
   * @param neighbors The map of neighbor IP addresses to neighbor configurations
   */
  public void setNeighbors(@Nonnull Map<Ip, BgpPeerConfig> neighbors) {
    _neighbors = neighbors;
  }

  /**
   * Adds a BGP neighbor.
   *
   * @param ip The neighbor IP address
   * @param neighbor The neighbor configuration
   */
  public void addNeighbor(Ip ip, BgpPeerConfig neighbor) {
    _neighbors.put(ip, neighbor);
  }

  /**
   * Gets the BGP peer groups.
   *
   * @return A map of peer group names to configurations
   */
  public @Nonnull Map<String, HuaweiBgpPeerGroup> getPeerGroups() {
    return _peerGroups;
  }

  /**
   * Sets the BGP peer groups.
   *
   * @param peerGroups The map of peer group names to configurations
   */
  public void setPeerGroups(@Nonnull Map<String, HuaweiBgpPeerGroup> peerGroups) {
    _peerGroups = peerGroups;
  }

  /**
   * Gets or creates a BGP peer group.
   *
   * @param name The peer group name
   * @return The peer group configuration
   */
  public @Nonnull HuaweiBgpPeerGroup getOrCreatePeerGroup(String name) {
    return _peerGroups.computeIfAbsent(name, HuaweiBgpPeerGroup::new);
  }

  /**
   * Gets the BGP address families.
   *
   * @return A map of address family names to configurations
   */
  public @Nonnull Map<String, HuaweiBgpAddressFamily> getAddressFamilies() {
    return _addressFamilies;
  }

  /**
   * Sets the BGP address families.
   *
   * @param addressFamilies The map of address family names to configurations
   */
  public void setAddressFamilies(@Nonnull Map<String, HuaweiBgpAddressFamily> addressFamilies) {
    _addressFamilies = addressFamilies;
  }

  /**
   * Gets the BGP network announcements.
   *
   * @return A list of network announcements
   */
  public @Nonnull List<HuaweiBgpNetwork> getNetworks() {
    return _networks;
  }

  /**
   * Sets the BGP network announcements.
   *
   * @param networks The list of network announcements
   */
  public void setNetworks(@Nonnull List<HuaweiBgpNetwork> networks) {
    _networks = networks;
  }

  /**
   * Adds a BGP network announcement.
   *
   * @param network The network announcement to add
   */
  public void addNetwork(HuaweiBgpNetwork network) {
    _networks.add(network);
  }

  /**
   * Gets the BGP import-route (redistribution) configurations.
   *
   * @return A list of import-route configurations
   */
  public @Nonnull List<HuaweiBgpImportRoute> getImportRoutes() {
    return _importRoutes;
  }

  /**
   * Sets the BGP import-route (redistribution) configurations.
   *
   * @param importRoutes The list of import-route configurations
   */
  public void setImportRoutes(@Nonnull List<HuaweiBgpImportRoute> importRoutes) {
    _importRoutes = importRoutes;
  }

  /**
   * Adds a BGP import-route (redistribution) configuration.
   *
   * @param importRoute The import-route configuration to add
   */
  public void addImportRoute(HuaweiBgpImportRoute importRoute) {
    _importRoutes.add(importRoute);
  }

  /** Represents a BGP peer group configuration. */
  public static class HuaweiBgpPeerGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String _name;
    private PeerType _type;
    private Long _remoteAs;
    private String _routePolicyIn;
    private String _routePolicyOut;
    private String _password;
    private Integer _localAs;
    private Boolean _routeReflectorClient;
    private String _clusterId;
    private String _connectInterface;

    public enum PeerType {
      INTERNAL,
      EXTERNAL
    }

    public HuaweiBgpPeerGroup(String name) {
      _name = name;
    }

    public String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public PeerType getType() {
      return _type;
    }

    public void setType(PeerType type) {
      _type = type;
    }

    public Long getRemoteAs() {
      return _remoteAs;
    }

    public void setRemoteAs(Long remoteAs) {
      _remoteAs = remoteAs;
    }

    public String getRoutePolicyIn() {
      return _routePolicyIn;
    }

    public void setRoutePolicyIn(String routePolicyIn) {
      _routePolicyIn = routePolicyIn;
    }

    public String getRoutePolicyOut() {
      return _routePolicyOut;
    }

    public void setRoutePolicyOut(String routePolicyOut) {
      _routePolicyOut = routePolicyOut;
    }

    public String getPassword() {
      return _password;
    }

    public void setPassword(String password) {
      _password = password;
    }

    public Integer getLocalAs() {
      return _localAs;
    }

    public void setLocalAs(Integer localAs) {
      _localAs = localAs;
    }

    public Boolean getRouteReflectorClient() {
      return _routeReflectorClient;
    }

    public void setRouteReflectorClient(Boolean routeReflectorClient) {
      _routeReflectorClient = routeReflectorClient;
    }

    public String getClusterId() {
      return _clusterId;
    }

    public void setClusterId(String clusterId) {
      _clusterId = clusterId;
    }

    public String getConnectInterface() {
      return _connectInterface;
    }

    public void setConnectInterface(String connectInterface) {
      _connectInterface = connectInterface;
    }
  }

  /** Represents a BGP address family configuration. */
  public static class HuaweiBgpAddressFamily implements Serializable {
    private static final long serialVersionUID = 1L;

    private String _name;
    private AddressFamilyType _type;
    private boolean _unicast;
    private boolean _multicast;
    private boolean _vpn;
    private String _importPolicy;
    private String _exportPolicy;

    /** Peer-specific route-policies: IP -> (import policy, export policy, advertise-community) */
    private Map<Ip, HuaweiBgpProcess.HuaweiBgpAfPeerConfig> _peerConfigs;

    /** Peer-group-specific route-policies: group name -> policy configuration */
    private Map<String, HuaweiBgpProcess.HuaweiBgpAfPeerGroupConfig> _peerGroupConfigs;

    public enum AddressFamilyType {
      IPV4,
      IPV6
    }

    public HuaweiBgpAddressFamily(String name) {
      _name = name;
      _peerConfigs = new TreeMap<>();
      _peerGroupConfigs = new TreeMap<>();
    }

    public String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public AddressFamilyType getType() {
      return _type;
    }

    public void setType(AddressFamilyType type) {
      _type = type;
    }

    public boolean isUnicast() {
      return _unicast;
    }

    public void setUnicast(boolean unicast) {
      _unicast = unicast;
    }

    public boolean isMulticast() {
      return _multicast;
    }

    public void setMulticast(boolean multicast) {
      _multicast = multicast;
    }

    public boolean isVpn() {
      return _vpn;
    }

    public void setVpn(boolean vpn) {
      _vpn = vpn;
    }

    public String getImportPolicy() {
      return _importPolicy;
    }

    public void setImportPolicy(String importPolicy) {
      _importPolicy = importPolicy;
    }

    public String getExportPolicy() {
      return _exportPolicy;
    }

    public void setExportPolicy(String exportPolicy) {
      _exportPolicy = exportPolicy;
    }

    public Map<Ip, HuaweiBgpAfPeerConfig> getPeerConfigs() {
      return _peerConfigs;
    }

    public void setPeerConfigs(Map<Ip, HuaweiBgpAfPeerConfig> peerConfigs) {
      _peerConfigs = peerConfigs;
    }

    public HuaweiBgpAfPeerConfig getOrCreatePeerConfig(Ip peerIp) {
      return _peerConfigs.computeIfAbsent(peerIp, HuaweiBgpAfPeerConfig::new);
    }

    public Map<String, HuaweiBgpAfPeerGroupConfig> getPeerGroupConfigs() {
      return _peerGroupConfigs;
    }

    public void setPeerGroupConfigs(Map<String, HuaweiBgpAfPeerGroupConfig> peerGroupConfigs) {
      _peerGroupConfigs = peerGroupConfigs;
    }

    public HuaweiBgpAfPeerGroupConfig getOrCreatePeerGroupConfig(String groupName) {
      return _peerGroupConfigs.computeIfAbsent(groupName, HuaweiBgpAfPeerGroupConfig::new);
    }
  }

  /** Represents BGP address family peer-specific configuration. */
  public static class HuaweiBgpAfPeerConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Ip _peerIp;
    private String _importPolicy;
    private String _exportPolicy;
    private Boolean _advertiseCommunity;

    public HuaweiBgpAfPeerConfig(Ip peerIp) {
      _peerIp = peerIp;
    }

    public Ip getPeerIp() {
      return _peerIp;
    }

    public void setPeerIp(Ip peerIp) {
      _peerIp = peerIp;
    }

    public String getImportPolicy() {
      return _importPolicy;
    }

    public void setImportPolicy(String importPolicy) {
      _importPolicy = importPolicy;
    }

    public String getExportPolicy() {
      return _exportPolicy;
    }

    public void setExportPolicy(String exportPolicy) {
      _exportPolicy = exportPolicy;
    }

    public Boolean getAdvertiseCommunity() {
      return _advertiseCommunity;
    }

    public void setAdvertiseCommunity(Boolean advertiseCommunity) {
      _advertiseCommunity = advertiseCommunity;
    }
  }

  /** Represents BGP address family peer-group-specific configuration. */
  public static class HuaweiBgpAfPeerGroupConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private String _groupName;
    private String _importPolicy;
    private String _exportPolicy;
    private Boolean _advertiseCommunity;

    public HuaweiBgpAfPeerGroupConfig(String groupName) {
      _groupName = groupName;
    }

    public String getGroupName() {
      return _groupName;
    }

    public void setGroupName(String groupName) {
      _groupName = groupName;
    }

    public String getImportPolicy() {
      return _importPolicy;
    }

    public void setImportPolicy(String importPolicy) {
      _importPolicy = importPolicy;
    }

    public String getExportPolicy() {
      return _exportPolicy;
    }

    public void setExportPolicy(String exportPolicy) {
      _exportPolicy = exportPolicy;
    }

    public Boolean getAdvertiseCommunity() {
      return _advertiseCommunity;
    }

    public void setAdvertiseCommunity(Boolean advertiseCommunity) {
      _advertiseCommunity = advertiseCommunity;
    }
  }

  /** Represents a BGP network announcement. */
  public static class HuaweiBgpNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    private Prefix _network;
    private Ip _mask;
    private String _routePolicy;

    public HuaweiBgpNetwork(Prefix network, Ip mask) {
      _network = network;
      _mask = mask;
    }

    public Prefix getNetwork() {
      return _network;
    }

    public void setNetwork(Prefix network) {
      _network = network;
    }

    public Ip getMask() {
      return _mask;
    }

    public void setMask(Ip mask) {
      _mask = mask;
    }

    public String getRoutePolicy() {
      return _routePolicy;
    }

    public void setRoutePolicy(String routePolicy) {
      _routePolicy = routePolicy;
    }
  }

  /** Represents a BGP import-route (redistribution) configuration. */
  public static class HuaweiBgpImportRoute implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The protocol to redistribute from */
    private String _protocol;

    /** The route-policy to filter redistribution (optional) */
    private String _routePolicy;

    public HuaweiBgpImportRoute(String protocol) {
      _protocol = protocol;
    }

    public String getProtocol() {
      return _protocol;
    }

    public void setProtocol(String protocol) {
      _protocol = protocol;
    }

    public String getRoutePolicy() {
      return _routePolicy;
    }

    public void setRoutePolicy(String routePolicy) {
      _routePolicy = routePolicy;
    }
  }
}

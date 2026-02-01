package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * Represents an OSPF process on a Huawei VRP device.
 *
 * <p>This class stores OSPF configuration information including process ID, router ID, areas,
 * networks, interfaces, redistribution policies, and other OSPF-specific settings.
 */
public class HuaweiOspfProcess implements Serializable {

  private static final long serialVersionUID = 1L;

  /** OSPF process ID */
  private long _processId;

  /** Router ID */
  private @Nullable Ip _routerId;

  /** OSPF networks: list of network advertisements with associated area IDs */
  private @Nonnull List<HuaweiOspfNetwork> _networks;

  /** OSPF areas: area ID to area configuration */
  private @Nonnull Map<Long, HuaweiOspfArea> _areas;

  /** OSPF interfaces */
  private @Nonnull Map<String, HuaweiOspfInterfaceSettings> _interfaces;

  /** OSPF virtual links */
  private @Nonnull List<HuaweiOspfVirtualLink> _virtualLinks;

  /** Default originate enabled */
  private boolean _defaultOriginate;

  /** Default originate route map */
  private @Nullable String _defaultOriginateRouteMap;

  /** Default cost for redistributed routes (set via "default cost" command) */
  private @Nullable Long _defaultCost;

  /** Default tag for redistributed routes (set via "default tag" command) */
  private @Nullable Long _defaultTag;

  /** Default type for external routes (set via "default type" command) */
  private @Nullable Integer _defaultType;

  /** Redistribution policies: protocol to redistribution policy */
  private @Nonnull Map<HuaweiRedistributionProtocol, HuaweiOspfRedistributionPolicy>
      _redistributionPolicies;

  public HuaweiOspfProcess(long processId) {
    _processId = processId;
    _networks = new ArrayList<>();
    _areas = new TreeMap<>();
    _interfaces = new TreeMap<>();
    _virtualLinks = new ArrayList<>();
    _defaultOriginate = false;
    _redistributionPolicies = new EnumMap<>(HuaweiRedistributionProtocol.class);
  }

  /**
   * Gets the OSPF process ID.
   *
   * @return The process ID
   */
  public long getProcessId() {
    return _processId;
  }

  /**
   * Sets the OSPF process ID.
   *
   * @param processId The process ID to set
   */
  public void setProcessId(long processId) {
    _processId = processId;
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
   * Gets the OSPF networks.
   *
   * @return A list of network advertisements
   */
  public @Nonnull List<HuaweiOspfNetwork> getNetworks() {
    return _networks;
  }

  /**
   * Sets the OSPF networks.
   *
   * @param networks The list of network advertisements
   */
  public void setNetworks(@Nonnull List<HuaweiOspfNetwork> networks) {
    _networks = networks;
  }

  /**
   * Adds an OSPF network.
   *
   * @param network The network to add
   */
  public void addNetwork(HuaweiOspfNetwork network) {
    _networks.add(network);
  }

  /**
   * Gets the OSPF areas.
   *
   * @return A map of area IDs to area configurations
   */
  public @Nonnull Map<Long, HuaweiOspfArea> getAreas() {
    return _areas;
  }

  /**
   * Sets the OSPF areas.
   *
   * @param areas The map of area IDs to area configurations
   */
  public void setAreas(@Nonnull Map<Long, HuaweiOspfArea> areas) {
    _areas = areas;
  }

  /**
   * Gets or creates an OSPF area.
   *
   * @param areaId The area ID
   * @return The area configuration
   */
  public @Nonnull HuaweiOspfArea getOrCreateArea(long areaId) {
    return _areas.computeIfAbsent(areaId, HuaweiOspfArea::new);
  }

  /**
   * Adds an OSPF area.
   *
   * @param areaId The area ID
   * @param area The area configuration
   */
  public void addArea(Long areaId, HuaweiOspfArea area) {
    _areas.put(areaId, area);
  }

  /**
   * Gets the OSPF interfaces.
   *
   * @return A map of interface names to configurations
   */
  public @Nonnull Map<String, HuaweiOspfInterfaceSettings> getInterfaces() {
    return _interfaces;
  }

  /**
   * Sets the OSPF interfaces.
   *
   * @param interfaces The map of interface names to configurations
   */
  public void setInterfaces(@Nonnull Map<String, HuaweiOspfInterfaceSettings> interfaces) {
    _interfaces = interfaces;
  }

  /**
   * Adds an OSPF interface setting.
   *
   * @param ifaceName The interface name
   * @param settings The interface settings
   */
  public void addInterface(String ifaceName, HuaweiOspfInterfaceSettings settings) {
    _interfaces.put(ifaceName, settings);
  }

  /**
   * Gets the OSPF virtual links.
   *
   * @return A list of virtual links
   */
  public @Nonnull List<HuaweiOspfVirtualLink> getVirtualLinks() {
    return _virtualLinks;
  }

  /**
   * Sets the OSPF virtual links.
   *
   * @param virtualLinks The list of virtual links
   */
  public void setVirtualLinks(@Nonnull List<HuaweiOspfVirtualLink> virtualLinks) {
    _virtualLinks = virtualLinks;
  }

  /**
   * Adds an OSPF virtual link.
   *
   * @param virtualLink The virtual link to add
   */
  public void addVirtualLink(HuaweiOspfVirtualLink virtualLink) {
    _virtualLinks.add(virtualLink);
  }

  /**
   * Checks if default originate is enabled.
   *
   * @return true if default originate is enabled, false otherwise
   */
  public boolean getDefaultOriginate() {
    return _defaultOriginate;
  }

  /**
   * Sets whether default originate is enabled.
   *
   * @param defaultOriginate true to enable default originate, false to disable
   */
  public void setDefaultOriginate(boolean defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  /**
   * Gets the default originate route map.
   *
   * @return The route map name, or null if not set
   */
  public @Nullable String getDefaultOriginateRouteMap() {
    return _defaultOriginateRouteMap;
  }

  /**
   * Sets the default originate route map.
   *
   * @param defaultOriginateRouteMap The route map name
   */
  public void setDefaultOriginateRouteMap(@Nullable String defaultOriginateRouteMap) {
    _defaultOriginateRouteMap = defaultOriginateRouteMap;
  }

  /**
   * Gets the default cost for redistributed routes.
   *
   * @return The default cost, or null if not set
   */
  public @Nullable Long getDefaultCost() {
    return _defaultCost;
  }

  /**
   * Sets the default cost for redistributed routes.
   *
   * @param defaultCost The default cost
   */
  public void setDefaultCost(@Nullable Long defaultCost) {
    _defaultCost = defaultCost;
  }

  /**
   * Gets the default tag for redistributed routes.
   *
   * @return The default tag, or null if not set
   */
  public @Nullable Long getDefaultTag() {
    return _defaultTag;
  }

  /**
   * Sets the default tag for redistributed routes.
   *
   * @param defaultTag The default tag
   */
  public void setDefaultTag(@Nullable Long defaultTag) {
    _defaultTag = defaultTag;
  }

  /**
   * Gets the default type for external routes.
   *
   * @return The default type (1 or 2), or null if not set
   */
  public @Nullable Integer getDefaultType() {
    return _defaultType;
  }

  /**
   * Sets the default type for external routes.
   *
   * @param defaultType The default type (1 or 2)
   */
  public void setDefaultType(@Nullable Integer defaultType) {
    _defaultType = defaultType;
  }

  /**
   * Gets the redistribution policies.
   *
   * @return A map of redistribution protocols to policies
   */
  public @Nonnull Map<HuaweiRedistributionProtocol, HuaweiOspfRedistributionPolicy>
      getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  /**
   * Sets the redistribution policies.
   *
   * @param redistributionPolicies The map of redistribution policies
   */
  public void setRedistributionPolicies(
      @Nonnull
          Map<HuaweiRedistributionProtocol, HuaweiOspfRedistributionPolicy>
              redistributionPolicies) {
    _redistributionPolicies = redistributionPolicies;
  }

  /**
   * Adds a redistribution policy.
   *
   * @param protocol The protocol to redistribute
   * @param policy The redistribution policy
   */
  public void addRedistributionPolicy(
      HuaweiRedistributionProtocol protocol, HuaweiOspfRedistributionPolicy policy) {
    _redistributionPolicies.put(protocol, policy);
  }

  /** Enum representing protocols that can be redistributed into OSPF. */
  public enum HuaweiRedistributionProtocol {
    /** Directly connected routes */
    DIRECT,
    /** Static routes */
    STATIC,
    /** BGP routes */
    BGP,
    /** RIP routes */
    RIP,
    /** IS-IS routes */
    ISIS,
    /** Other OSPF processes */
    OSPF,
    /** User network routes (UNR) */
    UNR
  }

  /** Represents an OSPF route redistribution policy. */
  public static class HuaweiOspfRedistributionPolicy implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Source protocol to redistribute */
    private HuaweiRedistributionProtocol _sourceProtocol;

    /** Optional route policy to filter redistributed routes */
    private @Nullable String _routePolicy;

    /** Optional metric/cost for redistributed routes */
    private @Nullable Long _cost;

    /** Optional tag for redistributed routes */
    private @Nullable Long _tag;

    /** Optional type (1 or 2) for external routes */
    private @Nullable Integer _type;

    public HuaweiOspfRedistributionPolicy(HuaweiRedistributionProtocol sourceProtocol) {
      _sourceProtocol = sourceProtocol;
    }

    public HuaweiRedistributionProtocol getSourceProtocol() {
      return _sourceProtocol;
    }

    public void setSourceProtocol(HuaweiRedistributionProtocol sourceProtocol) {
      _sourceProtocol = sourceProtocol;
    }

    public @Nullable String getRoutePolicy() {
      return _routePolicy;
    }

    public void setRoutePolicy(@Nullable String routePolicy) {
      _routePolicy = routePolicy;
    }

    public @Nullable Long getCost() {
      return _cost;
    }

    public void setCost(@Nullable Long cost) {
      _cost = cost;
    }

    public @Nullable Long getTag() {
      return _tag;
    }

    public void setTag(@Nullable Long tag) {
      _tag = tag;
    }

    public @Nullable Integer getType() {
      return _type;
    }

    public void setType(@Nullable Integer type) {
      _type = type;
    }
  }

  /** Represents an OSPF network advertisement with associated area. */
  public static class HuaweiOspfNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    private Prefix _network;
    private long _areaId;

    public HuaweiOspfNetwork(Prefix network, long areaId) {
      _network = network;
      _areaId = areaId;
    }

    public Prefix getNetwork() {
      return _network;
    }

    public void setNetwork(Prefix network) {
      _network = network;
    }

    public long getAreaId() {
      return _areaId;
    }

    public void setAreaId(long areaId) {
      _areaId = areaId;
    }
  }

  /** Enum representing OSPF area types. */
  public enum OspfAreaType {
    NORMAL,
    STUB,
    NSSA
  }

  /** Represents an OSPF area range (abr-summary) configuration. */
  public static class HuaweiOspfAreaRange implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The summary prefix */
    private Prefix _prefix;

    /** Whether to advertise this summary (false if not-advertise is set) */
    private boolean _advertise;

    /** Optional cost value for the summary route */
    private Long _cost;

    public HuaweiOspfAreaRange(Prefix prefix, boolean advertise, @Nullable Long cost) {
      _prefix = prefix;
      _advertise = advertise;
      _cost = cost;
    }

    public Prefix getPrefix() {
      return _prefix;
    }

    public void setPrefix(Prefix prefix) {
      _prefix = prefix;
    }

    public boolean isAdvertise() {
      return _advertise;
    }

    public void setAdvertise(boolean advertise) {
      _advertise = advertise;
    }

    public @Nullable Long getCost() {
      return _cost;
    }

    public void setCost(@Nullable Long cost) {
      _cost = cost;
    }
  }

  /** Represents an OSPF area configuration. */
  public static class HuaweiOspfArea implements Serializable {
    private static final long serialVersionUID = 1L;

    private long _areaId;
    private String _areaIdStr;
    private OspfAreaType _areaType;
    private boolean _noSummary;
    private boolean _noRedistribute;
    private boolean _defaultOriginate;
    private String _authKey;
    private String _authType; // MD5, SIMPLE

    /** OSPF area range (abr-summary) configurations: prefix to summary settings */
    private @Nonnull Map<Prefix, HuaweiOspfAreaRange> _areaRanges;

    public HuaweiOspfArea(long areaId) {
      _areaId = areaId;
      _areaType = OspfAreaType.NORMAL;
      _areaRanges = new TreeMap<>();
    }

    public long getAreaId() {
      return _areaId;
    }

    public void setAreaId(long areaId) {
      _areaId = areaId;
    }

    public String getAreaIdStr() {
      return _areaIdStr;
    }

    public void setAreaIdStr(String areaIdStr) {
      _areaIdStr = areaIdStr;
    }

    public OspfAreaType getAreaType() {
      return _areaType;
    }

    public void setAreaType(OspfAreaType areaType) {
      _areaType = areaType;
    }

    public boolean isNoSummary() {
      return _noSummary;
    }

    public void setNoSummary(boolean noSummary) {
      _noSummary = noSummary;
    }

    public boolean isNoRedistribute() {
      return _noRedistribute;
    }

    public void setNoRedistribute(boolean noRedistribute) {
      _noRedistribute = noRedistribute;
    }

    public boolean isDefaultOriginate() {
      return _defaultOriginate;
    }

    public void setDefaultOriginate(boolean defaultOriginate) {
      _defaultOriginate = defaultOriginate;
    }

    public String getAuthKey() {
      return _authKey;
    }

    public void setAuthKey(String authKey) {
      _authKey = authKey;
    }

    public String getAuthType() {
      return _authType;
    }

    public void setAuthType(String authType) {
      _authType = authType;
    }

    /**
     * Gets the OSPF area range (abr-summary) configurations.
     *
     * @return A map of prefixes to area range settings
     */
    public @Nonnull Map<Prefix, HuaweiOspfAreaRange> getAreaRanges() {
      return _areaRanges;
    }

    /**
     * Sets the OSPF area range (abr-summary) configurations.
     *
     * @param areaRanges The map of prefixes to area range settings
     */
    public void setAreaRanges(@Nonnull Map<Prefix, HuaweiOspfAreaRange> areaRanges) {
      _areaRanges = areaRanges;
    }

    /**
     * Adds an area range (abr-summary) configuration.
     *
     * @param prefix The summary prefix
     * @param range The area range settings
     */
    public void addAreaRange(Prefix prefix, HuaweiOspfAreaRange range) {
      _areaRanges.put(prefix, range);
    }
  }

  /** Represents an OSPF virtual link. */
  public static class HuaweiOspfVirtualLink implements Serializable {
    private static final long serialVersionUID = 1L;

    private Ip _routerId;
    private Integer _helloInterval;
    private Integer _deadInterval;

    public HuaweiOspfVirtualLink(Ip routerId) {
      _routerId = routerId;
    }

    public Ip getRouterId() {
      return _routerId;
    }

    public void setRouterId(Ip routerId) {
      _routerId = routerId;
    }

    public Integer getHelloInterval() {
      return _helloInterval;
    }

    public void setHelloInterval(Integer helloInterval) {
      _helloInterval = helloInterval;
    }

    public Integer getDeadInterval() {
      return _deadInterval;
    }

    public void setDeadInterval(Integer deadInterval) {
      _deadInterval = deadInterval;
    }
  }

  /** Represents OSPF interface settings. */
  public static class HuaweiOspfInterfaceSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long _areaId;
    private Integer _cost;
    private Integer _helloInterval;
    private Integer _deadInterval;
    private Integer _retransmitInterval;
    private String _networkType; // BROADCAST, P2P, P2MP, NBMA
    private String _authType; // MD5, SIMPLE
    private String _authKey;
    private Boolean _passive; // true for passive enabled

    public Long getAreaId() {
      return _areaId;
    }

    public void setAreaId(Long areaId) {
      _areaId = areaId;
    }

    public Integer getCost() {
      return _cost;
    }

    public void setCost(Integer cost) {
      _cost = cost;
    }

    public Integer getHelloInterval() {
      return _helloInterval;
    }

    public void setHelloInterval(Integer helloInterval) {
      _helloInterval = helloInterval;
    }

    public Integer getDeadInterval() {
      return _deadInterval;
    }

    public void setDeadInterval(Integer deadInterval) {
      _deadInterval = deadInterval;
    }

    public Integer getRetransmitInterval() {
      return _retransmitInterval;
    }

    public void setRetransmitInterval(Integer retransmitInterval) {
      _retransmitInterval = retransmitInterval;
    }

    public String getNetworkType() {
      return _networkType;
    }

    public void setNetworkType(String networkType) {
      _networkType = networkType;
    }

    public String getAuthType() {
      return _authType;
    }

    public void setAuthType(String authType) {
      _authType = authType;
    }

    public String getAuthKey() {
      return _authKey;
    }

    public void setAuthKey(String authKey) {
      _authKey = authKey;
    }

    public Boolean getPassive() {
      return _passive;
    }

    public void setPassive(Boolean passive) {
      _passive = passive;
    }
  }
}

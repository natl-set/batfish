package org.batfish.datamodel.vendor_family.huawei;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;

/**
 * Vendor-specific configuration data for Huawei devices.
 *
 * <p>This class stores Huawei-specific configuration information that doesn't fit into the
 * vendor-independent model. This includes VRF-specific data such as route distinguishers and route
 * targets.
 */
public class HuaweiFamily implements Serializable {

  private static final long serialVersionUID = 1L;

  /** VRF-specific data mapped by VRF name */
  private @Nonnull SortedMap<String, HuaweiVrfData> _vrfs;

  public HuaweiFamily() {
    _vrfs = new TreeMap<>();
  }

  @JsonCreator
  private static HuaweiFamily jsonCreator() {
    return new HuaweiFamily();
  }

  /**
   * Gets the map of VRF data.
   *
   * @return A sorted map of VRF names to VRF data
   */
  @Nonnull
  public SortedMap<String, HuaweiVrfData> getVrfs() {
    return _vrfs;
  }

  /**
   * Sets the map of VRF data.
   *
   * @param vrfs The sorted map of VRF names to VRF data
   */
  public void setVrfs(@Nonnull SortedMap<String, HuaweiVrfData> vrfs) {
    _vrfs = vrfs;
  }

  /**
   * Gets VRF data for a specific VRF.
   *
   * @param vrfName The VRF name
   * @return The VRF data, or null if not found
   */
  @Nullable
  public HuaweiVrfData getVrf(@Nonnull String vrfName) {
    return _vrfs.get(vrfName);
  }

  /**
   * Adds or updates VRF data.
   *
   * @param vrfName The VRF name
   * @param vrfData The VRF data to add
   */
  public void putVrf(@Nonnull String vrfName, @Nonnull HuaweiVrfData vrfData) {
    _vrfs.put(vrfName, vrfData);
  }

  /**
   * Huawei VRF-specific configuration data.
   *
   * <p>Stores VRF information such as route distinguisher and route targets that are specific to
   * Huawei's implementation.
   */
  public static class HuaweiVrfData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** VRF name */
    private @Nonnull String _name;

    /** Route distinguisher */
    private @Nullable RouteDistinguisher _routeDistinguisher;

    /** Import route targets */
    private @Nullable SortedSet<String> _importRouteTargets;

    /** Export route targets */
    private @Nullable SortedSet<String> _exportRouteTargets;

    /** VRF description */
    private @Nullable String _description;

    /** IPv4 address family enabled */
    private boolean _ipv4Enabled;

    /** IPv6 address family enabled */
    private boolean _ipv6Enabled;

    public HuaweiVrfData(@Nonnull String name) {
      _name = name;
      _importRouteTargets = new java.util.TreeSet<>();
      _exportRouteTargets = new java.util.TreeSet<>();
    }

    public @Nonnull String getName() {
      return _name;
    }

    public void setName(@Nonnull String name) {
      _name = name;
    }

    public @Nullable RouteDistinguisher getRouteDistinguisher() {
      return _routeDistinguisher;
    }

    public void setRouteDistinguisher(@Nullable RouteDistinguisher routeDistinguisher) {
      _routeDistinguisher = routeDistinguisher;
    }

    public @Nullable SortedSet<String> getImportRouteTargets() {
      return _importRouteTargets;
    }

    public void setImportRouteTargets(@Nullable SortedSet<String> importRouteTargets) {
      _importRouteTargets = importRouteTargets;
    }

    public void addImportRouteTarget(@Nonnull String routeTarget) {
      if (_importRouteTargets == null) {
        _importRouteTargets = new java.util.TreeSet<>();
      }
      _importRouteTargets.add(routeTarget);
    }

    public @Nullable SortedSet<String> getExportRouteTargets() {
      return _exportRouteTargets;
    }

    public void setExportRouteTargets(@Nullable SortedSet<String> exportRouteTargets) {
      _exportRouteTargets = exportRouteTargets;
    }

    public void addExportRouteTarget(@Nonnull String routeTarget) {
      if (_exportRouteTargets == null) {
        _exportRouteTargets = new java.util.TreeSet<>();
      }
      _exportRouteTargets.add(routeTarget);
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(@Nullable String description) {
      _description = description;
    }

    public boolean isIpv4Enabled() {
      return _ipv4Enabled;
    }

    public void setIpv4Enabled(boolean ipv4Enabled) {
      _ipv4Enabled = ipv4Enabled;
    }

    public boolean isIpv6Enabled() {
      return _ipv6Enabled;
    }

    public void setIpv6Enabled(boolean ipv6Enabled) {
      _ipv6Enabled = ipv6Enabled;
    }
  }
}

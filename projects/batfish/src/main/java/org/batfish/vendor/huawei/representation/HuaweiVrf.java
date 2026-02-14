package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Virtual Routing and Forwarding (VRF) instance on a Huawei VRP device.
 *
 * <p>VRFs allow multiple routing tables to coexist on the same router. Each VRF has its own:
 *
 * <ul>
 *   <li>Route distinguisher (RD) - identifies VRF in BGP VPN updates
 *   <li>Route targets (RT) - controls import/export of routes between VRFs
 *   <li>Address families - IPv4 and/or IPv6
 *   <li>Interfaces bound to the VRF
 * </ul>
 */
public class HuaweiVrf implements Serializable {

  private static final long serialVersionUID = 1L;

  /** VRF name */
  private @Nonnull String _name;

  /** Route distinguisher (format: ASN:NN or IP:NN) */
  private @Nullable String _routeDistinguisher;

  /** Import route targets */
  private @Nonnull SortedSet<String> _importRouteTargets;

  /** Export route targets */
  private @Nonnull SortedSet<String> _exportRouteTargets;

  /** VRF description */
  private @Nullable String _description;

  /** Interfaces in this VRF (mapped by interface name) */
  private @Nonnull TreeMap<String, HuaweiInterface> _interfaces;

  /** IPv4 address family enabled */
  private boolean _ipv4Enabled;

  /** IPv6 address family enabled */
  private boolean _ipv6Enabled;

  /** VRF-specific BGP process */
  private @Nullable HuaweiBgpProcess _bgpProcess;

  /** VRF-specific OSPF process */
  private @Nullable HuaweiOspfProcess _ospfProcess;

  public HuaweiVrf(@Nonnull String name) {
    _name = name;
    _importRouteTargets = new TreeSet<>();
    _exportRouteTargets = new TreeSet<>();
    _interfaces = new TreeMap<>();
    _ipv4Enabled = false;
    _ipv6Enabled = false;
  }

  /**
   * Gets the VRF name.
   *
   * @return The VRF name
   */
  public @Nonnull String getName() {
    return _name;
  }

  /**
   * Sets the VRF name.
   *
   * @param name The VRF name to set
   */
  public void setName(@Nonnull String name) {
    _name = name;
  }

  /**
   * Gets the route distinguisher.
   *
   * @return The route distinguisher, or null if not set
   */
  public @Nullable String getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  /**
   * Sets the route distinguisher.
   *
   * @param routeDistinguisher The route distinguisher to set
   */
  public void setRouteDistinguisher(@Nullable String routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }

  /**
   * Gets the import route targets.
   *
   * @return A sorted set of import route target strings
   */
  public @Nonnull SortedSet<String> getImportRouteTargets() {
    return _importRouteTargets;
  }

  /**
   * Sets the import route targets.
   *
   * @param importRouteTargets The sorted set of import route targets to set
   */
  public void setImportRouteTargets(@Nonnull SortedSet<String> importRouteTargets) {
    _importRouteTargets = importRouteTargets;
  }

  /**
   * Adds an import route target.
   *
   * @param routeTarget The route target to add (format: ASN:NN or IP:NN)
   */
  public void addImportRouteTarget(@Nonnull String routeTarget) {
    _importRouteTargets.add(routeTarget);
  }

  /**
   * Gets the export route targets.
   *
   * @return A sorted set of export route target strings
   */
  public @Nonnull SortedSet<String> getExportRouteTargets() {
    return _exportRouteTargets;
  }

  /**
   * Sets the export route targets.
   *
   * @param exportRouteTargets The sorted set of export route targets to set
   */
  public void setExportRouteTargets(@Nonnull SortedSet<String> exportRouteTargets) {
    _exportRouteTargets = exportRouteTargets;
  }

  /**
   * Adds an export route target.
   *
   * @param routeTarget The route target to add (format: ASN:NN or IP:NN)
   */
  public void addExportRouteTarget(@Nonnull String routeTarget) {
    _exportRouteTargets.add(routeTarget);
  }

  /**
   * Adds a route target for both import and export.
   *
   * @param routeTarget The route target to add (format: ASN:NN or IP:NN)
   */
  public void addBothRouteTarget(@Nonnull String routeTarget) {
    addImportRouteTarget(routeTarget);
    addExportRouteTarget(routeTarget);
  }

  /**
   * Gets the VRF description.
   *
   * @return The description, or null if not set
   */
  public @Nullable String getDescription() {
    return _description;
  }

  /**
   * Sets the VRF description.
   *
   * @param description The description to set
   */
  public void setDescription(@Nullable String description) {
    _description = description;
  }

  /**
   * Gets the interfaces in this VRF.
   *
   * @return A map of interface names to interface configurations
   */
  public @Nonnull TreeMap<String, HuaweiInterface> getInterfaces() {
    return _interfaces;
  }

  /**
   * Sets the interfaces in this VRF.
   *
   * @param interfaces The map of interface names to configurations
   */
  public void setInterfaces(@Nonnull TreeMap<String, HuaweiInterface> interfaces) {
    _interfaces = interfaces;
  }

  /**
   * Adds an interface to this VRF.
   *
   * @param name The interface name
   * @param iface The interface configuration
   */
  public void addInterface(@Nonnull String name, @Nonnull HuaweiInterface iface) {
    _interfaces.put(name, iface);
  }

  /**
   * Checks if IPv4 address family is enabled for this VRF.
   *
   * @return true if IPv4 address family is enabled
   */
  public boolean isIpv4Enabled() {
    return _ipv4Enabled;
  }

  /**
   * Sets whether IPv4 address family is enabled for this VRF.
   *
   * @param ipv4Enabled true to enable IPv4 address family
   */
  public void setIpv4Enabled(boolean ipv4Enabled) {
    _ipv4Enabled = ipv4Enabled;
  }

  /**
   * Checks if IPv6 address family is enabled for this VRF.
   *
   * @return true if IPv6 address family is enabled
   */
  public boolean isIpv6Enabled() {
    return _ipv6Enabled;
  }

  /**
   * Sets whether IPv6 address family is enabled for this VRF.
   *
   * @param ipv6Enabled true to enable IPv6 address family
   */
  public void setIpv6Enabled(boolean ipv6Enabled) {
    _ipv6Enabled = ipv6Enabled;
  }

  /**
   * Gets the VRF-specific BGP process.
   *
   * @return The BGP process, or null if not configured
   */
  public @Nullable HuaweiBgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  /**
   * Sets the VRF-specific BGP process.
   *
   * @param bgpProcess The BGP process to set
   */
  public void setBgpProcess(@Nullable HuaweiBgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  /**
   * Gets the VRF-specific OSPF process.
   *
   * @return The OSPF process, or null if not configured
   */
  public @Nullable HuaweiOspfProcess getOspfProcess() {
    return _ospfProcess;
  }

  /**
   * Sets the VRF-specific OSPF process.
   *
   * @param ospfProcess The OSPF process to set
   */
  public void setOspfProcess(@Nullable HuaweiOspfProcess ospfProcess) {
    _ospfProcess = ospfProcess;
  }
}

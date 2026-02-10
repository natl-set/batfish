package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an Access Control List (ACL) on a Huawei VRP device.
 *
 * <p>This is a stub class for future ACL implementation. It will store ACL configuration including
 * ACL name, type (basic, advanced, or layer-2), and ACL entries.
 */
public class HuaweiAcl implements Serializable {

  private static final long serialVersionUID = 1L;

  /** ACL type enumeration */
  public enum AclType {
    BASIC, // Basic ACL (source IP only)
    ADVANCED, // Advanced ACL (source, destination, protocol, ports)
    L2 // Layer-2 ACL (MAC addresses)
  }

  /** ACL name or number */
  private final @Nonnull String _name;

  /** ACL type */
  private final @Nonnull AclType _type;

  /** ACL entries/lines */
  private @Nonnull List<HuaweiAclLine> _lines;

  /** Whether this is an IPv6 ACL */
  private final boolean _ipv6;

  /** VRF name for this ACL (if applicable) */
  private @Nullable String _vrfName;

  public HuaweiAcl(@Nonnull String name, @Nonnull AclType type, boolean ipv6) {
    _name = name;
    _type = type;
    _lines = new ArrayList<>();
    _ipv6 = ipv6;
  }

  /**
   * Gets the ACL name.
   *
   * @return The ACL name
   */
  public @Nonnull String getName() {
    return _name;
  }

  /**
   * Gets the ACL type.
   *
   * @return The ACL type
   */
  public @Nonnull AclType getType() {
    return _type;
  }

  /**
   * Gets the ACL lines/entries.
   *
   * @return A list of ACL lines
   */
  public @Nonnull List<HuaweiAclLine> getLines() {
    return _lines;
  }

  /**
   * Adds an ACL line/entry.
   *
   * @param line The ACL line to add
   */
  public void addLine(@Nonnull HuaweiAclLine line) {
    _lines.add(line);
  }

  /**
   * Checks if this is an IPv6 ACL.
   *
   * @return true if this is an IPv6 ACL, false otherwise
   */
  public boolean isIpv6() {
    return _ipv6;
  }

  /**
   * Gets the VRF name for this ACL.
   *
   * @return The VRF name, or null if not applicable
   */
  public @Nullable String getVrfName() {
    return _vrfName;
  }

  /**
   * Sets the VRF name for this ACL.
   *
   * @param vrfName The VRF name to set
   */
  public void setVrfName(@Nullable String vrfName) {
    _vrfName = vrfName;
  }
}

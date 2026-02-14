package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Represents a NAT address group on a Huawei VRP device.
 *
 * <p>NAT address groups define pools of IP addresses used for dynamic NAT. Each address group has
 * an index number and can contain one or more IP address ranges.
 */
public class HuaweiNatAddressGroup implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Address group index (numeric identifier) */
  private final int _index;

  /** List of IP address ranges in this address group */
  private @Nonnull List<IpRange> _ranges;

  /** Represents an IP address range with optional mask */
  public static class IpRange implements Serializable {
    private static final long serialVersionUID = 1L;

    private final @Nonnull Ip _startIp;
    private final @Nonnull Ip _endIp;
    private final @Nullable Ip _mask;

    public IpRange(@Nonnull Ip startIp, @Nonnull Ip endIp, @Nullable Ip mask) {
      _startIp = startIp;
      _endIp = endIp;
      _mask = mask;
    }

    public @Nonnull Ip getStartIp() {
      return _startIp;
    }

    public @Nonnull Ip getEndIp() {
      return _endIp;
    }

    public @Nullable Ip getMask() {
      return _mask;
    }

    @Override
    public String toString() {
      if (_mask != null) {
        return _startIp + " " + _mask;
      }
      return _startIp + " " + _endIp;
    }
  }

  /**
   * Creates a new NAT address group with the specified index.
   *
   * @param index The address group index
   */
  public HuaweiNatAddressGroup(int index) {
    _index = index;
    _ranges = new ArrayList<>();
  }

  /**
   * Gets the address group index.
   *
   * @return The index of this address group
   */
  public int getIndex() {
    return _index;
  }

  /**
   * Gets the list of IP address ranges in this address group.
   *
   * @return The list of IP ranges
   */
  public @Nonnull List<IpRange> getRanges() {
    return _ranges;
  }

  /**
   * Sets the list of IP address ranges.
   *
   * @param ranges The list of IP ranges to set
   */
  public void setRanges(@Nonnull List<IpRange> ranges) {
    _ranges = ranges;
  }

  /**
   * Adds an IP range to this address group.
   *
   * @param startIp The starting IP address of the range
   * @param endIp The ending IP address of the range
   * @param mask The optional subnet mask
   */
  public void addRange(@Nonnull Ip startIp, @Nonnull Ip endIp, @Nullable Ip mask) {
    _ranges.add(new IpRange(startIp, endIp, mask));
  }

  /**
   * Adds an IP range to this address group without a mask.
   *
   * @param startIp The starting IP address of the range
   * @param endIp The ending IP address of the range
   */
  public void addRange(@Nonnull Ip startIp, @Nonnull Ip endIp) {
    addRange(startIp, endIp, null);
  }

  @Override
  public String toString() {
    return "HuaweiNatAddressGroup{" + "_index=" + _index + ", _ranges=" + _ranges + '}';
  }
}

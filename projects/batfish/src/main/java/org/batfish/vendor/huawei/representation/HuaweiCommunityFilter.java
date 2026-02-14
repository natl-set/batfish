package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.Community;

/**
 * Represents an IP community filter on a Huawei VRP device.
 *
 * <p>Community filters are used to match BGP communities in route-policies. A community filter
 * consists of:
 *
 * <ul>
 *   <li>A filter number (used to reference it in route-policies)
 *   <li>An action: permit or deny
 *   <li>A list of community values to match
 * </ul>
 *
 * <p>Community values can be:
 *
 * <ul>
 *   <li>AA:NN format (e.g., 65000:100) - where AA is the AS number and NN is the local value
 *   <li>Well-known communities: internet, no-export, no-advertise, no-export-subconfed
 * </ul>
 *
 * <p>Example configuration:
 *
 * <pre>
 * ip community-filter 1 permit 65000:100
 * ip community-filter 2 deny internet
 * ip community-filter 3 permit 65000:100 65000:200
 * </pre>
 *
 * <p>Usage in route-policy:
 *
 * <pre>
 * route-policy POLICY permit node 10
 *   if-match community-filter 1
 * </pre>
 */
public class HuaweiCommunityFilter implements Serializable {

  private static final long serialVersionUID = 1L;

  /** The filter number */
  private final int _filterNumber;

  /** The action: permit or deny */
  private final @Nonnull Action _action;

  /** The list of community values to match */
  private final @Nonnull List<Community> _communities;

  /** The action type for a community filter */
  public enum Action {
    /** Permit matching routes */
    PERMIT,
    /** Deny matching routes */
    DENY
  }

  /**
   * Creates a new community filter.
   *
   * @param filterNumber The filter number
   * @param action The action (permit or deny)
   */
  public HuaweiCommunityFilter(int filterNumber, @Nonnull Action action) {
    _filterNumber = filterNumber;
    _action = action;
    _communities = new ArrayList<>();
  }

  /**
   * Gets the filter number.
   *
   * @return The filter number
   */
  public int getFilterNumber() {
    return _filterNumber;
  }

  /**
   * Gets the action.
   *
   * @return The action
   */
  public @Nonnull Action getAction() {
    return _action;
  }

  /**
   * Gets the list of community values.
   *
   * @return A list of communities
   */
  public @Nonnull List<Community> getCommunities() {
    return _communities;
  }

  /**
   * Adds a community value to this filter.
   *
   * @param community The community to add
   */
  public void addCommunity(@Nonnull Community community) {
    _communities.add(community);
  }
}

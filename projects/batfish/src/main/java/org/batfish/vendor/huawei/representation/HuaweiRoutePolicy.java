package org.batfish.vendor.huawei.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;

/**
 * Represents a Huawei route-policy definition.
 *
 * <p>Huawei route-policies are used to filter and modify routing information. A route-policy
 * contains one or more nodes (similar to ACLs or route-maps in other vendors), where each node has:
 *
 * <ul>
 *   <li>A sequence number (node ID)
 *   <li>An action: permit or deny
 *   <li>Optional match conditions (if-match clauses)
 *   <li>Optional set actions (apply clauses)
 * </ul>
 *
 * <p>Example configuration:
 *
 * <pre>
 * route-policy local_pre permit node 10
 *   if-match community-filter 1
 *   apply local-preference 200
 * </pre>
 */
public class HuaweiRoutePolicy implements Serializable {

  private static final long serialVersionUID = 1L;

  /** The name of the route-policy */
  private final @Nonnull String _name;

  /** The nodes (statements) that make up this route-policy */
  private final @Nonnull List<HuaweiRoutePolicyNode> _nodes;

  public HuaweiRoutePolicy(@Nonnull String name) {
    _name = name;
    _nodes = new ArrayList<>();
  }

  /**
   * Gets the name of the route-policy.
   *
   * @return The route-policy name
   */
  public @Nonnull String getName() {
    return _name;
  }

  /**
   * Gets the nodes (statements) in this route-policy.
   *
   * @return A list of route-policy nodes
   */
  @Nonnull
  public List<HuaweiRoutePolicyNode> getNodes() {
    return _nodes;
  }

  /**
   * Adds a node to this route-policy.
   *
   * @param node The node to add
   */
  public void addNode(@Nonnull HuaweiRoutePolicyNode node) {
    _nodes.add(node);
  }

  /**
   * Represents a single node (statement) in a route-policy.
   *
   * <p>Each node has a sequence number, an action (permit/deny), and optional match conditions and
   * set actions.
   */
  public static class HuaweiRoutePolicyNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The sequence number of this node */
    private final int _nodeId;

    /** The action: permit or deny */
    private final @Nonnull Action _action;

    /** Match conditions (if-match clauses) */
    private final @Nonnull HuaweiRoutePolicyMatchConditions _matchConditions;

    /** Set actions (apply clauses) */
    private final @Nonnull HuaweiRoutePolicySetActions _setActions;

    public HuaweiRoutePolicyNode(int nodeId, @Nonnull Action action) {
      _nodeId = nodeId;
      _action = action;
      _matchConditions = new HuaweiRoutePolicyMatchConditions();
      _setActions = new HuaweiRoutePolicySetActions();
    }

    /**
     * Gets the node ID (sequence number).
     *
     * @return The node ID
     */
    public int getNodeId() {
      return _nodeId;
    }

    /**
     * Gets the action (permit or deny).
     *
     * @return The action
     */
    public @Nonnull Action getAction() {
      return _action;
    }

    /**
     * Gets the match conditions for this node.
     *
     * @return The match conditions
     */
    @Nonnull
    public HuaweiRoutePolicyMatchConditions getMatchConditions() {
      return _matchConditions;
    }

    /**
     * Gets the set actions for this node.
     *
     * @return The set actions
     */
    @Nonnull
    public HuaweiRoutePolicySetActions getSetActions() {
      return _setActions;
    }

    /** The action type for a route-policy node. */
    public enum Action {
      /** Permit matching routes */
      PERMIT,
      /** Deny matching routes */
      DENY
    }
  }

  /**
   * Represents match conditions (if-match clauses) for a route-policy node.
   *
   * <p>Supported match conditions include:
   *
   * <ul>
   *   <li>IP prefix list: if-match ip-prefix &lt;prefix-list-name&gt;
   *   <li>Community filter: if-match community-filter &lt;number&gt;
   *   <li>Community list: if-match community &lt;communities&gt;
   * </ul>
   */
  public static class HuaweiRoutePolicyMatchConditions implements Serializable {

    private static final long serialVersionUID = 1L;

    /** IP prefix list name to match (if-match ip-prefix) */
    private @Nullable String _ipPrefix;

    /** Community filter to match (if-match community-filter) */
    private @Nullable Integer _communityFilter;

    /** Community list to match (if-match community &lt;communities&gt;) */
    private @Nullable List<Community> _communities;

    public @Nullable String getIpPrefix() {
      return _ipPrefix;
    }

    public void setIpPrefix(@Nullable String ipPrefix) {
      _ipPrefix = ipPrefix;
    }

    public @Nullable Integer getCommunityFilter() {
      return _communityFilter;
    }

    public void setCommunityFilter(@Nullable Integer communityFilter) {
      _communityFilter = communityFilter;
    }

    public @Nullable List<Community> getCommunities() {
      return _communities;
    }

    public void setCommunities(@Nullable List<Community> communities) {
      _communities = communities;
    }
  }

  /**
   * Represents set actions (apply clauses) for a route-policy node.
   *
   * <p>Supported set actions include:
   *
   * <ul>
   *   <li>Local preference: apply local-preference &lt;value&gt;
   *   <li>Community: apply community &lt;community-value&gt;
   *   <li>Cost: apply cost &lt;value&gt;
   *   <li>Preference: apply preference &lt;value&gt;
   *   <li>Tag: apply tag &lt;value&gt;
   * </ul>
   */
  public static class HuaweiRoutePolicySetActions implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Local preference (apply local-preference) */
    private @Nullable Long _localPreference;

    /** Community to set (apply community) */
    private @Nullable List<Community> _communities;

    /** Cost value (apply cost) */
    private @Nullable Integer _cost;

    /** Preference value (apply preference) */
    private @Nullable Integer _preference;

    /** Tag value (apply tag) */
    private @Nullable Long _tag;

    public @Nullable Long getLocalPreference() {
      return _localPreference;
    }

    public void setLocalPreference(@Nullable Long localPreference) {
      _localPreference = localPreference;
    }

    public @Nullable List<Community> getCommunities() {
      return _communities;
    }

    public void setCommunities(@Nullable List<Community> communities) {
      _communities = communities;
    }

    public @Nullable Integer getCost() {
      return _cost;
    }

    public void setCost(@Nullable Integer cost) {
      _cost = cost;
    }

    public @Nullable Integer getPreference() {
      return _preference;
    }

    public void setPreference(@Nullable Integer preference) {
      _preference = preference;
    }

    public @Nullable Long getTag() {
      return _tag;
    }

    public void setTag(@Nullable Long tag) {
      _tag = tag;
    }
  }
}

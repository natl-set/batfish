package org.batfish.representation.f5_bigip;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a firewall rule-list in F5 BIG-IP security configuration. */
@ParametersAreNonnullByDefault
public final class FirewallRuleList implements Serializable {

  private final @Nonnull String _name;
  private final @Nonnull List<FirewallRule> _rules;

  public FirewallRuleList(@Nonnull String name) {
    _name = checkNotNull(name);
    _rules = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull List<FirewallRule> getRules() {
    return Collections.unmodifiableList(_rules);
  }

  public void addRule(@Nonnull FirewallRule rule) {
    _rules.add(checkNotNull(rule));
  }
}

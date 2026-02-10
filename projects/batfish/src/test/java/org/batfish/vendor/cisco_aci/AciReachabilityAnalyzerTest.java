package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciReachabilityAnalyzer;
import org.batfish.vendor.cisco_aci.representation.ReachabilityFinding;
import org.junit.Test;

/** Tests for {@link AciReachabilityAnalyzer}. */
public final class AciReachabilityAnalyzerTest {

  @Test
  public void testAnalyzeEpgReachabilityFindsNoContractAcrossBridgeDomains() {
    AciConfiguration config = new AciConfiguration();
    AciConfiguration.Tenant tenant = new AciConfiguration.Tenant("tenant1");

    AciConfiguration.Epg epg1 = new AciConfiguration.Epg("epg1");
    epg1.setTenant("tenant1");
    epg1.setBridgeDomain("tenant1:bd1");
    tenant.getEpgs().put("tenant1:epg1", epg1);

    AciConfiguration.Epg epg2 = new AciConfiguration.Epg("epg2");
    epg2.setTenant("tenant1");
    epg2.setBridgeDomain("tenant1:bd2");
    tenant.getEpgs().put("tenant1:epg2", epg2);

    config.getTenants().put("tenant1", tenant);

    List<ReachabilityFinding> findings = AciReachabilityAnalyzer.analyzeEpgReachability(config);
    List<ReachabilityFinding.Category> categories =
        findings.stream().map(ReachabilityFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(ReachabilityFinding.Category.NO_CONTRACT));
  }

  @Test
  public void testAnalyzeEpgReachabilityFindsInvalidContractAndMissingPath() {
    AciConfiguration config = new AciConfiguration();
    AciConfiguration.Tenant tenant = new AciConfiguration.Tenant("tenant1");

    AciConfiguration.Epg epg = new AciConfiguration.Epg("epg1");
    epg.setTenant("tenant1");
    epg.setProvidedContracts(ImmutableList.of("tenant1:missing-contract"));
    // No bridge domain set -> should trigger MISSING_PATH
    tenant.getEpgs().put("tenant1:epg1", epg);

    config.getTenants().put("tenant1", tenant);

    List<ReachabilityFinding> findings = AciReachabilityAnalyzer.analyzeEpgReachability(config);
    List<ReachabilityFinding.Category> categories =
        findings.stream().map(ReachabilityFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(ReachabilityFinding.Category.INVALID_CONTRACT_REFERENCE));
    assertThat(categories, hasItem(ReachabilityFinding.Category.MISSING_PATH));
  }
}

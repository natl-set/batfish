package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciVrfIsolationAnalyzer;
import org.batfish.vendor.cisco_aci.representation.AciVrfModel;
import org.batfish.vendor.cisco_aci.representation.VrfIsolationFinding;
import org.junit.Test;

/** Tests for {@link AciVrfIsolationAnalyzer}. */
public final class AciVrfIsolationAnalyzerTest {

  @Test
  public void testAnalyzeVrfIsolationFindsSubnetOverlapAcrossVrfs() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.10.10.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    bd2.setSubnets(ImmutableList.of("10.10.10.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);
    List<VrfIsolationFinding.Category> categories =
        findings.stream().map(VrfIsolationFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(VrfIsolationFinding.Category.SUBNET_OVERLAP));
  }

  @Test
  public void testAnalyzeVrfIsolationFindsCrossVrfContractAndUnusedVrf() {
    AciConfiguration config = new AciConfiguration();

    AciVrfModel vrf1 = new AciVrfModel("tenant1:vrf1");
    vrf1.setTenant("tenant1");
    AciVrfModel vrf2 = new AciVrfModel("tenant1:vrf2");
    vrf2.setTenant("tenant1");
    AciVrfModel vrf3 = new AciVrfModel("tenant1:vrf3");
    vrf3.setTenant("tenant1");
    config.getVrfs().put(vrf1.getName(), vrf1);
    config.getVrfs().put(vrf2.getName(), vrf2);
    config.getVrfs().put(vrf3.getName(), vrf3);

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    AciConfiguration.Epg provider = new AciConfiguration.Epg("provider");
    provider.setTenant("tenant1");
    provider.setBridgeDomain("bd1");
    provider.setProvidedContracts(ImmutableList.of("tenant1:c1"));
    config.getEpgs().put("tenant1:provider", provider);

    AciConfiguration.Epg consumer = new AciConfiguration.Epg("consumer");
    consumer.setTenant("tenant1");
    consumer.setBridgeDomain("bd2");
    consumer.setConsumedContracts(ImmutableList.of("tenant1:c1"));
    config.getEpgs().put("tenant1:consumer", consumer);

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    config.getContracts().put("tenant1:c1", contract);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);
    List<VrfIsolationFinding.Category> categories =
        findings.stream().map(VrfIsolationFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(VrfIsolationFinding.Category.CROSS_VRF_CONTRACT));
    assertThat(categories, hasItem(VrfIsolationFinding.Category.UNUSED_VRF));
  }
}

package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciSecurityAnalyzer;
import org.batfish.vendor.cisco_aci.representation.SecurityFinding;
import org.junit.Test;

/** Tests for {@link AciSecurityAnalyzer}. */
public final class AciSecurityAnalyzerTest {

  @Test
  public void testAnalyzeContractsFindsAnyAnyAndBroadPortRange() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef = new AciConfiguration.Contract.Filter();
    filterRef.setName("tenant1:f1");
    subject.setFilters(ImmutableList.of(filterRef));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:c1", contract);

    AciConfiguration.Filter filter = new AciConfiguration.Filter("tenant1:f1");
    AciConfiguration.Filter.Entry anyAny = new AciConfiguration.Filter.Entry();
    anyAny.setName("anyAny");

    AciConfiguration.Filter.Entry broadRange = new AciConfiguration.Filter.Entry();
    broadRange.setName("broadRange");
    broadRange.setProtocol("tcp");
    broadRange.setDestinationFromPort("1");
    broadRange.setDestinationToPort("65535");

    filter.setEntries(ImmutableList.of(anyAny, broadRange));
    config.getFilters().put("tenant1:f1", filter);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);
    List<SecurityFinding.Category> categories =
        findings.stream().map(SecurityFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(SecurityFinding.Category.ANY_ANY));
    assertThat(categories, hasItem(SecurityFinding.Category.BROAD_PORT_RANGE));
    assertThat(categories, hasItem(SecurityFinding.Category.MISSING_DENY));
  }

  @Test
  public void testAnalyzeContractsFindsMissingFilterReference() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter missingFilterRef = new AciConfiguration.Contract.Filter();
    missingFilterRef.setName("tenant1:missing");
    subject.setFilters(ImmutableList.of(missingFilterRef));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:c1", contract);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);
    List<SecurityFinding.Category> categories =
        findings.stream().map(SecurityFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(SecurityFinding.Category.OVERLY_PERMISSIVE));
  }
}

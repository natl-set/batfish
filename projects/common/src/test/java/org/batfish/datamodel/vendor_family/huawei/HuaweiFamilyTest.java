package org.batfish.datamodel.vendor_family.huawei;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.junit.Test;

/** Tests for {@link HuaweiFamily}. */
public class HuaweiFamilyTest {

  @Test
  public void testConstructor() {
    HuaweiFamily family = new HuaweiFamily();
    assertThat(family.getVrfs().size(), equalTo(0));
  }

  @Test
  public void testGetSetVrfs() {
    HuaweiFamily family = new HuaweiFamily();
    assertThat(family.getVrfs().size(), equalTo(0));

    SortedMap<String, HuaweiFamily.HuaweiVrfData> vrfs = new TreeMap<>();
    vrfs.put("VRF1", new HuaweiFamily.HuaweiVrfData("VRF1"));
    vrfs.put("VRF2", new HuaweiFamily.HuaweiVrfData("VRF2"));

    family.setVrfs(vrfs);
    assertThat(family.getVrfs(), equalTo(vrfs));
    assertThat(family.getVrfs().size(), equalTo(2));
  }

  @Test
  public void testPutVrf() {
    HuaweiFamily family = new HuaweiFamily();
    assertThat(family.getVrfs().size(), equalTo(0));

    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    family.putVrf("VRF1", vrfData);

    assertThat(family.getVrfs().size(), equalTo(1));
    assertThat(family.getVrfs(), hasKey("VRF1"));
    assertThat(family.getVrfs().get("VRF1"), equalTo(vrfData));
  }

  @Test
  public void testGetVrf() {
    HuaweiFamily family = new HuaweiFamily();
    assertThat(family.getVrf("VRF1"), nullValue());

    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    family.putVrf("VRF1", vrfData);

    assertThat(family.getVrf("VRF1"), equalTo(vrfData));
    assertThat(family.getVrf("NONEXISTENT"), nullValue());
  }

  @Test
  public void testPutVrfOverwrites() {
    HuaweiFamily family = new HuaweiFamily();

    HuaweiFamily.HuaweiVrfData vrfData1 = new HuaweiFamily.HuaweiVrfData("VRF1");
    vrfData1.setDescription("First description");
    family.putVrf("VRF1", vrfData1);

    assertThat(family.getVrfs().size(), equalTo(1));
    assertThat(family.getVrf("VRF1").getDescription(), equalTo("First description"));

    HuaweiFamily.HuaweiVrfData vrfData2 = new HuaweiFamily.HuaweiVrfData("VRF1");
    vrfData2.setDescription("Second description");
    family.putVrf("VRF1", vrfData2);

    assertThat(family.getVrfs().size(), equalTo(1));
    assertThat(family.getVrf("VRF1").getDescription(), equalTo("Second description"));
  }

  @Test
  public void testHuaweiVrfDataConstructor() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    assertThat(vrfData.getName(), equalTo("VRF1"));
    assertThat(vrfData.getRouteDistinguisher(), nullValue());
    assertThat(vrfData.getDescription(), nullValue());
    assertFalse(vrfData.isIpv4Enabled());
    assertFalse(vrfData.isIpv6Enabled());
  }

  @Test
  public void testHuaweiVrfDataGetSetName() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    assertThat(vrfData.getName(), equalTo("VRF1"));

    vrfData.setName("VRF2");
    assertThat(vrfData.getName(), equalTo("VRF2"));
  }

  @Test
  public void testHuaweiVrfDataGetSetRouteDistinguisher() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    assertThat(vrfData.getRouteDistinguisher(), nullValue());

    RouteDistinguisher rd = RouteDistinguisher.parse("65000:100");
    vrfData.setRouteDistinguisher(rd);
    assertThat(vrfData.getRouteDistinguisher(), equalTo(rd));
  }

  @Test
  public void testHuaweiVrfDataGetSetDescription() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    assertThat(vrfData.getDescription(), nullValue());

    vrfData.setDescription("Test VRF");
    assertThat(vrfData.getDescription(), equalTo("Test VRF"));

    vrfData.setDescription(null);
    assertThat(vrfData.getDescription(), nullValue());
  }

  @Test
  public void testHuaweiVrfDataGetSetIpv4Enabled() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    assertFalse(vrfData.isIpv4Enabled());

    vrfData.setIpv4Enabled(true);
    assertTrue(vrfData.isIpv4Enabled());

    vrfData.setIpv4Enabled(false);
    assertFalse(vrfData.isIpv4Enabled());
  }

  @Test
  public void testHuaweiVrfDataGetSetIpv6Enabled() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    assertFalse(vrfData.isIpv6Enabled());

    vrfData.setIpv6Enabled(true);
    assertTrue(vrfData.isIpv6Enabled());

    vrfData.setIpv6Enabled(false);
    assertFalse(vrfData.isIpv6Enabled());
  }

  @Test
  public void testHuaweiVrfDataGetSetImportRouteTargets() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");

    SortedSet<String> importTargets = new TreeSet<>();
    importTargets.add("65000:100");
    importTargets.add("65000:200");

    vrfData.setImportRouteTargets(importTargets);
    assertThat(vrfData.getImportRouteTargets(), equalTo(importTargets));
    assertThat(vrfData.getImportRouteTargets().size(), equalTo(2));
  }

  @Test
  public void testHuaweiVrfDataAddImportRouteTarget() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");

    vrfData.addImportRouteTarget("65000:100");
    assertThat(vrfData.getImportRouteTargets().size(), equalTo(1));
    assertTrue(vrfData.getImportRouteTargets().contains("65000:100"));

    vrfData.addImportRouteTarget("65000:200");
    assertThat(vrfData.getImportRouteTargets().size(), equalTo(2));
    assertTrue(vrfData.getImportRouteTargets().contains("65000:200"));
  }

  @Test
  public void testHuaweiVrfDataAddImportRouteTargetInitializesCollection() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    vrfData.setImportRouteTargets(null);

    vrfData.addImportRouteTarget("65000:100");
    assertThat(vrfData.getImportRouteTargets().size(), equalTo(1));
    assertTrue(vrfData.getImportRouteTargets().contains("65000:100"));
  }

  @Test
  public void testHuaweiVrfDataGetSetExportRouteTargets() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");

    SortedSet<String> exportTargets = new TreeSet<>();
    exportTargets.add("65000:100");
    exportTargets.add("65000:200");

    vrfData.setExportRouteTargets(exportTargets);
    assertThat(vrfData.getExportRouteTargets(), equalTo(exportTargets));
    assertThat(vrfData.getExportRouteTargets().size(), equalTo(2));
  }

  @Test
  public void testHuaweiVrfDataAddExportRouteTarget() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");

    vrfData.addExportRouteTarget("65000:100");
    assertThat(vrfData.getExportRouteTargets().size(), equalTo(1));
    assertTrue(vrfData.getExportRouteTargets().contains("65000:100"));

    vrfData.addExportRouteTarget("65000:200");
    assertThat(vrfData.getExportRouteTargets().size(), equalTo(2));
    assertTrue(vrfData.getExportRouteTargets().contains("65000:200"));
  }

  @Test
  public void testHuaweiVrfDataAddExportRouteTargetInitializesCollection() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");
    vrfData.setExportRouteTargets(null);

    vrfData.addExportRouteTarget("65000:100");
    assertThat(vrfData.getExportRouteTargets().size(), equalTo(1));
    assertTrue(vrfData.getExportRouteTargets().contains("65000:100"));
  }

  @Test
  public void testHuaweiVrfDataFullConfiguration() {
    HuaweiFamily.HuaweiVrfData vrfData = new HuaweiFamily.HuaweiVrfData("VRF1");

    vrfData.setDescription("Customer VRF");
    vrfData.setRouteDistinguisher(RouteDistinguisher.parse("65000:100"));

    vrfData.addImportRouteTarget("65000:100");
    vrfData.addImportRouteTarget("65000:200");

    vrfData.addExportRouteTarget("65000:100");
    vrfData.addExportRouteTarget("65000:300");

    vrfData.setIpv4Enabled(true);
    vrfData.setIpv6Enabled(true);

    assertThat(vrfData.getName(), equalTo("VRF1"));
    assertThat(vrfData.getDescription(), equalTo("Customer VRF"));
    assertThat(vrfData.getRouteDistinguisher(), equalTo(RouteDistinguisher.parse("65000:100")));
    assertThat(vrfData.getImportRouteTargets().size(), equalTo(2));
    assertThat(vrfData.getExportRouteTargets().size(), equalTo(2));
    assertTrue(vrfData.isIpv4Enabled());
    assertTrue(vrfData.isIpv6Enabled());
  }

  @Test
  public void testMultipleVrfs() {
    HuaweiFamily family = new HuaweiFamily();

    HuaweiFamily.HuaweiVrfData vrf1 = new HuaweiFamily.HuaweiVrfData("VRF1");
    vrf1.setDescription("VRF 1");
    family.putVrf("VRF1", vrf1);

    HuaweiFamily.HuaweiVrfData vrf2 = new HuaweiFamily.HuaweiVrfData("VRF2");
    vrf2.setDescription("VRF 2");
    family.putVrf("VRF2", vrf2);

    HuaweiFamily.HuaweiVrfData vrf3 = new HuaweiFamily.HuaweiVrfData("VRF3");
    vrf3.setDescription("VRF 3");
    family.putVrf("VRF3", vrf3);

    assertThat(family.getVrfs().size(), equalTo(3));
    assertThat(family.getVrf("VRF1").getDescription(), equalTo("VRF 1"));
    assertThat(family.getVrf("VRF2").getDescription(), equalTo("VRF 2"));
    assertThat(family.getVrf("VRF3").getDescription(), equalTo("VRF 3"));
  }

  @Test
  public void testVrfsIsTreeMap() {
    HuaweiFamily family = new HuaweiFamily();
    assertTrue(family.getVrfs() instanceof TreeMap);
  }

  @Test
  public void testReplaceVrfs() {
    HuaweiFamily family = new HuaweiFamily();

    HuaweiFamily.HuaweiVrfData vrf1 = new HuaweiFamily.HuaweiVrfData("VRF1");
    family.putVrf("VRF1", vrf1);

    assertThat(family.getVrfs().size(), equalTo(1));

    // Replace with new map
    SortedMap<String, HuaweiFamily.HuaweiVrfData> newVrfs = new TreeMap<>();
    newVrfs.put("VRF2", new HuaweiFamily.HuaweiVrfData("VRF2"));
    newVrfs.put("VRF3", new HuaweiFamily.HuaweiVrfData("VRF3"));

    family.setVrfs(newVrfs);

    assertThat(family.getVrfs().size(), equalTo(2));
    assertThat(family.getVrfs(), hasKey("VRF2"));
    assertThat(family.getVrfs(), hasKey("VRF3"));
    assertThat(family.getVrfs().get("VRF1"), nullValue());
  }
}

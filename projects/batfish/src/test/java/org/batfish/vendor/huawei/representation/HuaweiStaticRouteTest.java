package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Tests for {@link HuaweiStaticRoute}. */
public class HuaweiStaticRouteTest {

  @Test
  public void testConstructor() {
    Prefix dest = Prefix.parse("10.0.0.0/24");
    HuaweiStaticRoute route = new HuaweiStaticRoute(dest);

    assertThat(route.getDestination(), equalTo(dest));
    assertThat(route.getPreference(), equalTo(60)); // Default preference
    assertThat(route.isDefaultRoute(), equalTo(false));
    assertThat(route.getNextHopIp(), nullValue());
    assertThat(route.getNextHopInterface(), nullValue());
    assertThat(route.getVrfName(), nullValue());
  }

  @Test
  public void testSetDestination() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));
    assertThat(route.getDestination(), equalTo(Prefix.parse("10.0.0.0/24")));

    route.setDestination(Prefix.parse("192.168.1.0/24"));
    assertThat(route.getDestination(), equalTo(Prefix.parse("192.168.1.0/24")));

    route.setDestination(Prefix.parse("0.0.0.0/0"));
    assertThat(route.getDestination(), equalTo(Prefix.parse("0.0.0.0/0")));
  }

  @Test
  public void testSetNextHopIp() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));

    // Initially null
    assertThat(route.getNextHopIp(), nullValue());

    // Set next hop IP
    route.setNextHopIp(Ip.parse("192.168.1.1"));
    assertThat(route.getNextHopIp(), equalTo(Ip.parse("192.168.1.1")));

    // Update next hop IP
    route.setNextHopIp(Ip.parse("10.1.1.1"));
    assertThat(route.getNextHopIp(), equalTo(Ip.parse("10.1.1.1")));

    // Set to null
    route.setNextHopIp(null);
    assertThat(route.getNextHopIp(), nullValue());
  }

  @Test
  public void testSetNextHopInterface() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));

    // Initially null
    assertThat(route.getNextHopInterface(), nullValue());

    // Set next hop interface
    route.setNextHopInterface("GigabitEthernet0/0/0");
    assertThat(route.getNextHopInterface(), equalTo("GigabitEthernet0/0/0"));

    // Update next hop interface
    route.setNextHopInterface("GigabitEthernet0/0/1");
    assertThat(route.getNextHopInterface(), equalTo("GigabitEthernet0/0/1"));

    // Set to null
    route.setNextHopInterface(null);
    assertThat(route.getNextHopInterface(), nullValue());
  }

  @Test
  public void testSetPreference() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));

    // Default preference is 60
    assertThat(route.getPreference(), equalTo(60));

    // Set different preferences
    route.setPreference(1);
    assertThat(route.getPreference(), equalTo(1));

    route.setPreference(100);
    assertThat(route.getPreference(), equalTo(100));

    route.setPreference(255);
    assertThat(route.getPreference(), equalTo(255));

    // Set back to default
    route.setPreference(60);
    assertThat(route.getPreference(), equalTo(60));
  }

  @Test
  public void testSetDefaultRoute() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));

    // Initially false
    assertThat(route.isDefaultRoute(), equalTo(false));

    // Set to true
    route.setDefaultRoute(true);
    assertThat(route.isDefaultRoute(), equalTo(true));

    // Set to false
    route.setDefaultRoute(false);
    assertThat(route.isDefaultRoute(), equalTo(false));
  }

  @Test
  public void testSetVrfName() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));

    // Initially null
    assertThat(route.getVrfName(), nullValue());

    // Set VRF name
    route.setVrfName("VRF1");
    assertThat(route.getVrfName(), equalTo("VRF1"));

    // Update VRF name
    route.setVrfName("CUSTOMER_A");
    assertThat(route.getVrfName(), equalTo("CUSTOMER_A"));

    // Set to null
    route.setVrfName(null);
    assertThat(route.getVrfName(), nullValue());
  }

  @Test
  public void testToString() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));
    route.setNextHopIp(Ip.parse("192.168.1.1"));
    route.setNextHopInterface("GigabitEthernet0/0/0");
    route.setPreference(100);
    route.setDefaultRoute(false);
    route.setVrfName("VRF1");

    String toString = route.toString();

    assertTrue(toString.contains("10.0.0.0/24"));
    assertTrue(toString.contains("192.168.1.1"));
    assertTrue(toString.contains("GigabitEthernet0/0/0"));
    assertTrue(toString.contains("preference=100"));
    assertTrue(toString.contains("defaultRoute=false"));
    assertTrue(toString.contains("vrfName=VRF1"));
  }

  @Test
  public void testToString_DefaultValues() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));

    String toString = route.toString();

    assertTrue(toString.contains("10.0.0.0/24"));
    assertTrue(toString.contains("preference=60"));
    assertTrue(toString.contains("defaultRoute=false"));
  }

  @Test
  public void testSerialization() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));
    route.setNextHopIp(Ip.parse("192.168.1.1"));
    route.setNextHopInterface("GigabitEthernet0/0/0");
    route.setPreference(100);
    route.setDefaultRoute(true);
    route.setVrfName("VRF1");

    HuaweiStaticRoute clone = SerializationUtils.clone(route);

    assertThat(clone, notNullValue());
    assertThat(clone.getDestination(), equalTo(route.getDestination()));
    assertThat(clone.getNextHopIp(), equalTo(route.getNextHopIp()));
    assertThat(clone.getNextHopInterface(), equalTo(route.getNextHopInterface()));
    assertThat(clone.getPreference(), equalTo(route.getPreference()));
    assertThat(clone.isDefaultRoute(), equalTo(route.isDefaultRoute()));
    assertThat(clone.getVrfName(), equalTo(route.getVrfName()));
  }

  @Test
  public void testSerialization_NullFields() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));
    // Keep nextHopIp, nextHopInterface, and vrfName as null

    HuaweiStaticRoute clone = SerializationUtils.clone(route);

    assertThat(clone, notNullValue());
    assertThat(clone.getDestination(), equalTo(route.getDestination()));
    assertThat(clone.getNextHopIp(), nullValue());
    assertThat(clone.getNextHopInterface(), nullValue());
    assertThat(clone.getVrfName(), nullValue());
    assertThat(clone.getPreference(), equalTo(60));
    assertThat(clone.isDefaultRoute(), equalTo(false));
  }

  @Test
  public void testDefaultRouteConfiguration() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("0.0.0.0/0"));
    route.setNextHopIp(Ip.parse("192.168.1.1"));
    route.setDefaultRoute(true);

    assertThat(route.getDestination(), equalTo(Prefix.parse("0.0.0.0/0")));
    assertThat(route.isDefaultRoute(), equalTo(true));
    assertThat(route.getNextHopIp(), equalTo(Ip.parse("192.168.1.1")));
  }

  @Test
  public void testRouteWithOnlyNextHopIp() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("192.168.2.0/24"));
    route.setNextHopIp(Ip.parse("10.1.1.1"));

    assertThat(route.getDestination(), equalTo(Prefix.parse("192.168.2.0/24")));
    assertThat(route.getNextHopIp(), equalTo(Ip.parse("10.1.1.1")));
    assertThat(route.getNextHopInterface(), nullValue());
  }

  @Test
  public void testRouteWithOnlyNextHopInterface() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("192.168.2.0/24"));
    route.setNextHopInterface("GigabitEthernet0/0/1");

    assertThat(route.getDestination(), equalTo(Prefix.parse("192.168.2.0/24")));
    assertThat(route.getNextHopInterface(), equalTo("GigabitEthernet0/0/1"));
    assertThat(route.getNextHopIp(), nullValue());
  }

  @Test
  public void testRouteWithBothNextHopTypes() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("192.168.2.0/24"));
    route.setNextHopIp(Ip.parse("10.1.1.1"));
    route.setNextHopInterface("GigabitEthernet0/0/1");

    assertThat(route.getDestination(), equalTo(Prefix.parse("192.168.2.0/24")));
    assertThat(route.getNextHopIp(), equalTo(Ip.parse("10.1.1.1")));
    assertThat(route.getNextHopInterface(), equalTo("GigabitEthernet0/0/1"));
  }

  @Test
  public void testRouteWithVrf() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.1.0.0/16"));
    route.setNextHopIp(Ip.parse("192.168.1.1"));
    route.setVrfName("CUSTOMER_A");

    assertThat(route.getDestination(), equalTo(Prefix.parse("10.1.0.0/16")));
    assertThat(route.getNextHopIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(route.getVrfName(), equalTo("CUSTOMER_A"));
  }

  @Test
  public void testMultipleRoutesWithDifferentPreferences() {
    HuaweiStaticRoute route1 = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));
    route1.setNextHopIp(Ip.parse("192.168.1.1"));
    route1.setPreference(60);

    HuaweiStaticRoute route2 = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));
    route2.setNextHopIp(Ip.parse("192.168.1.2"));
    route2.setPreference(100);

    assertThat(route1.getPreference(), equalTo(60));
    assertThat(route2.getPreference(), equalTo(100));
    assertThat(route1.getNextHopIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(route2.getNextHopIp(), equalTo(Ip.parse("192.168.1.2")));
  }

  @Test
  public void testComplexRouteConfiguration() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("172.16.0.0/16"));
    route.setNextHopIp(Ip.parse("10.255.255.1"));
    route.setNextHopInterface("10GE1/0/1");
    route.setPreference(50);
    route.setDefaultRoute(false);
    route.setVrfName("VPN_CUSTOMER");

    assertThat(route.getDestination(), equalTo(Prefix.parse("172.16.0.0/16")));
    assertThat(route.getNextHopIp(), equalTo(Ip.parse("10.255.255.1")));
    assertThat(route.getNextHopInterface(), equalTo("10GE1/0/1"));
    assertThat(route.getPreference(), equalTo(50));
    assertThat(route.isDefaultRoute(), equalTo(false));
    assertThat(route.getVrfName(), equalTo("VPN_CUSTOMER"));

    // Verify toString contains all fields
    String toString = route.toString();
    assertTrue(toString.contains("172.16.0.0/16"));
    assertTrue(toString.contains("10.255.255.1"));
    assertTrue(toString.contains("10GE1/0/1"));
    assertTrue(toString.contains("preference=50"));
    assertTrue(toString.contains("VPN_CUSTOMER"));
  }

  @Test
  public void testRoutePreferenceBoundaryValues() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));

    route.setPreference(0);
    assertThat(route.getPreference(), equalTo(0));

    route.setPreference(1);
    assertThat(route.getPreference(), equalTo(1));

    route.setPreference(255);
    assertThat(route.getPreference(), equalTo(255));

    route.setPreference(Integer.MAX_VALUE);
    assertThat(route.getPreference(), equalTo(Integer.MAX_VALUE));
  }

  @Test
  public void testUpdateAllFields() {
    HuaweiStaticRoute route = new HuaweiStaticRoute(Prefix.parse("10.0.0.0/24"));

    // Set initial values
    route.setNextHopIp(Ip.parse("192.168.1.1"));
    route.setNextHopInterface("GigabitEthernet0/0/0");
    route.setPreference(60);
    route.setDefaultRoute(false);
    route.setVrfName("VRF1");

    assertThat(route.getNextHopIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(route.getNextHopInterface(), equalTo("GigabitEthernet0/0/0"));
    assertThat(route.getPreference(), equalTo(60));
    assertThat(route.isDefaultRoute(), equalTo(false));
    assertThat(route.getVrfName(), equalTo("VRF1"));

    // Update all values
    route.setDestination(Prefix.parse("20.0.0.0/24"));
    route.setNextHopIp(Ip.parse("192.168.2.1"));
    route.setNextHopInterface("GigabitEthernet0/0/1");
    route.setPreference(100);
    route.setDefaultRoute(true);
    route.setVrfName("VRF2");

    assertThat(route.getDestination(), equalTo(Prefix.parse("20.0.0.0/24")));
    assertThat(route.getNextHopIp(), equalTo(Ip.parse("192.168.2.1")));
    assertThat(route.getNextHopInterface(), equalTo("GigabitEthernet0/0/1"));
    assertThat(route.getPreference(), equalTo(100));
    assertThat(route.isDefaultRoute(), equalTo(true));
    assertThat(route.getVrfName(), equalTo("VRF2"));
  }
}

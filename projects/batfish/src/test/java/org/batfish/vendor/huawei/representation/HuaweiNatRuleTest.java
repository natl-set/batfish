package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link HuaweiNatRule}. */
public class HuaweiNatRuleTest {

  @Test
  public void testConstructor() {
    HuaweiNatRule rule = new HuaweiNatRule("test-rule", HuaweiNatRule.NatType.STATIC);
    assertThat(rule.getName(), equalTo("test-rule"));
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(rule.getAddressPool(), hasSize(0)); // Empty list initialized in constructor
  }

  @Test
  public void testConstructorWithDifferentTypes() {
    HuaweiNatRule staticRule = new HuaweiNatRule("static", HuaweiNatRule.NatType.STATIC);
    assertThat(staticRule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));

    HuaweiNatRule dynamicRule = new HuaweiNatRule("dynamic", HuaweiNatRule.NatType.DYNAMIC);
    assertThat(dynamicRule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));

    HuaweiNatRule easyIpRule = new HuaweiNatRule("easy-ip", HuaweiNatRule.NatType.EASY_IP);
    assertThat(easyIpRule.getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));

    HuaweiNatRule natServerRule = new HuaweiNatRule("nat-server", HuaweiNatRule.NatType.NAT_SERVER);
    assertThat(natServerRule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));

    HuaweiNatRule natAlgRule = new HuaweiNatRule("nat-alg", HuaweiNatRule.NatType.NAT_ALG);
    assertThat(natAlgRule.getType(), equalTo(HuaweiNatRule.NatType.NAT_ALG));
  }

  @Test
  public void testGetSetName() {
    HuaweiNatRule rule = new HuaweiNatRule("original", HuaweiNatRule.NatType.STATIC);
    assertThat(rule.getName(), equalTo("original"));

    rule.setName("updated");
    assertThat(rule.getName(), equalTo("updated"));
  }

  @Test
  public void testGetSetType() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.STATIC);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));

    rule.setType(HuaweiNatRule.NatType.DYNAMIC);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
  }

  @Test
  public void testGetSetAclName() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);
    assertThat(rule.getAclName(), nullValue());

    rule.setAclName("2000");
    assertThat(rule.getAclName(), equalTo("2000"));

    rule.setAclName(null);
    assertThat(rule.getAclName(), nullValue());
  }

  @Test
  public void testGetSetPoolName() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);
    assertThat(rule.getPoolName(), nullValue());

    rule.setPoolName("pool1");
    assertThat(rule.getPoolName(), equalTo("pool1"));

    rule.setPoolName(null);
    assertThat(rule.getPoolName(), nullValue());
  }

  @Test
  public void testGetSetGlobalIp() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.STATIC);
    assertThat(rule.getGlobalIp(), nullValue());

    Ip globalIp = Ip.parse("192.168.1.1");
    rule.setGlobalIp(globalIp);
    assertThat(rule.getGlobalIp(), equalTo(globalIp));

    rule.setGlobalIp(null);
    assertThat(rule.getGlobalIp(), nullValue());
  }

  @Test
  public void testGetSetGlobalPort() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.NAT_SERVER);
    assertThat(rule.getGlobalPort(), nullValue());

    rule.setGlobalPort(80);
    assertThat(rule.getGlobalPort(), equalTo(80));

    rule.setGlobalPort(443);
    assertThat(rule.getGlobalPort(), equalTo(443));

    rule.setGlobalPort(null);
    assertThat(rule.getGlobalPort(), nullValue());
  }

  @Test
  public void testGetSetInsideLocalIp() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.STATIC);
    assertThat(rule.getInsideLocalIp(), nullValue());

    Ip insideIp = Ip.parse("10.0.0.1");
    rule.setInsideLocalIp(insideIp);
    assertThat(rule.getInsideLocalIp(), equalTo(insideIp));

    rule.setInsideLocalIp(null);
    assertThat(rule.getInsideLocalIp(), nullValue());
  }

  @Test
  public void testGetSetInsideLocalPort() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.NAT_SERVER);
    assertThat(rule.getInsideLocalPort(), nullValue());

    rule.setInsideLocalPort(8080);
    assertThat(rule.getInsideLocalPort(), equalTo(8080));

    rule.setInsideLocalPort(null);
    assertThat(rule.getInsideLocalPort(), nullValue());
  }

  @Test
  public void testGetSetInterfaceName() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.EASY_IP);
    assertThat(rule.getInterfaceName(), nullValue());

    rule.setInterfaceName("GigabitEthernet0/0/1");
    assertThat(rule.getInterfaceName(), equalTo("GigabitEthernet0/0/1"));

    rule.setInterfaceName(null);
    assertThat(rule.getInterfaceName(), nullValue());
  }

  @Test
  public void testGetSetProtocol() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.NAT_SERVER);
    assertThat(rule.getProtocol(), nullValue());

    rule.setProtocol("tcp");
    assertThat(rule.getProtocol(), equalTo("tcp"));

    rule.setProtocol("udp");
    assertThat(rule.getProtocol(), equalTo("udp"));

    rule.setProtocol(null);
    assertThat(rule.getProtocol(), nullValue());
  }

  @Test
  public void testGetSetVrfName() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);
    assertThat(rule.getVrfName(), nullValue());

    rule.setVrfName("vrf1");
    assertThat(rule.getVrfName(), equalTo("vrf1"));

    rule.setVrfName(null);
    assertThat(rule.getVrfName(), nullValue());
  }

  @Test
  public void testGetSetAddressPool() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);

    // Initial state - empty list from constructor
    assertThat(rule.getAddressPool(), hasSize(0));

    // Set address pool with IPs
    List<Ip> pool = new ArrayList<>();
    pool.add(Ip.parse("192.168.1.1"));
    pool.add(Ip.parse("192.168.1.2"));
    pool.add(Ip.parse("192.168.1.3"));

    rule.setAddressPool(pool);
    assertThat(rule.getAddressPool(), hasSize(3));
    assertThat(rule.getAddressPool().get(0), equalTo(Ip.parse("192.168.1.1")));
    assertThat(rule.getAddressPool().get(1), equalTo(Ip.parse("192.168.1.2")));
    assertThat(rule.getAddressPool().get(2), equalTo(Ip.parse("192.168.1.3")));

    // Set to null
    rule.setAddressPool(null);
    assertThat(rule.getAddressPool(), nullValue());
  }

  @Test
  public void testAddAddressPoolIp() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);

    // Add first IP to empty list
    rule.addAddressPoolIp(Ip.parse("192.168.1.1"));
    assertThat(rule.getAddressPool(), hasSize(1));
    assertThat(rule.getAddressPool().get(0), equalTo(Ip.parse("192.168.1.1")));

    // Add more IPs
    rule.addAddressPoolIp(Ip.parse("192.168.1.2"));
    rule.addAddressPoolIp(Ip.parse("192.168.1.3"));
    assertThat(rule.getAddressPool(), hasSize(3));
  }

  @Test
  public void testAddAddressPoolIpWhenPoolIsNull() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);

    // Set pool to null first
    rule.setAddressPool(null);
    assertThat(rule.getAddressPool(), nullValue());

    // Add IP should initialize the list
    rule.addAddressPoolIp(Ip.parse("192.168.1.1"));
    assertThat(rule.getAddressPool(), hasSize(1));
    assertThat(rule.getAddressPool().get(0), equalTo(Ip.parse("192.168.1.1")));
  }

  @Test
  public void testSerialization() {
    HuaweiNatRule rule = new HuaweiNatRule("test-rule", HuaweiNatRule.NatType.STATIC);
    rule.setAclName("2000");
    rule.setPoolName("pool1");
    rule.setGlobalIp(Ip.parse("192.168.1.1"));
    rule.setGlobalPort(80);
    rule.setInsideLocalIp(Ip.parse("10.0.0.1"));
    rule.setInsideLocalPort(8080);
    rule.setInterfaceName("GigabitEthernet0/0/1");
    rule.setProtocol("tcp");
    rule.setVrfName("vrf1");

    List<Ip> pool = new ArrayList<>();
    pool.add(Ip.parse("192.168.1.10"));
    pool.add(Ip.parse("192.168.1.11"));
    rule.setAddressPool(pool);

    HuaweiNatRule clone = SerializationUtils.clone(rule);

    assertThat(clone.getName(), equalTo(rule.getName()));
    assertThat(clone.getType(), equalTo(rule.getType()));
    assertThat(clone.getAclName(), equalTo(rule.getAclName()));
    assertThat(clone.getPoolName(), equalTo(rule.getPoolName()));
    assertThat(clone.getGlobalIp(), equalTo(rule.getGlobalIp()));
    assertThat(clone.getGlobalPort(), equalTo(rule.getGlobalPort()));
    assertThat(clone.getInsideLocalIp(), equalTo(rule.getInsideLocalIp()));
    assertThat(clone.getInsideLocalPort(), equalTo(rule.getInsideLocalPort()));
    assertThat(clone.getInterfaceName(), equalTo(rule.getInterfaceName()));
    assertThat(clone.getProtocol(), equalTo(rule.getProtocol()));
    assertThat(clone.getVrfName(), equalTo(rule.getVrfName()));
    assertThat(clone.getAddressPool(), equalTo(rule.getAddressPool()));
  }

  @Test
  public void testEquality() {
    Ip ip1 = Ip.parse("192.168.1.1");
    Ip ip2 = Ip.parse("192.168.1.2");

    HuaweiNatRule rule1 = new HuaweiNatRule("rule1", HuaweiNatRule.NatType.STATIC);
    rule1.setAclName("2000");
    rule1.setGlobalIp(ip1);
    rule1.setInsideLocalIp(ip2);

    HuaweiNatRule rule2 = rule1; // Same reference
    HuaweiNatRule rule3 = new HuaweiNatRule("rule1", HuaweiNatRule.NatType.STATIC);
    rule3.setAclName("2000");
    rule3.setGlobalIp(ip1);
    rule3.setInsideLocalIp(ip2);

    // Test reference equality (default equals behavior)
    assertEquals(rule1, rule2);
    // Different objects with same values are not equal (no equals override)
    assertNotEquals(rule1, rule3);
  }

  @Test
  public void testStaticNatRule() {
    HuaweiNatRule rule = new HuaweiNatRule("static-nat", HuaweiNatRule.NatType.STATIC);
    rule.setGlobalIp(Ip.parse("192.168.1.1"));
    rule.setInsideLocalIp(Ip.parse("10.0.0.1"));
    rule.setVrfName("public");

    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.STATIC));
    assertThat(rule.getGlobalIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(rule.getInsideLocalIp(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(rule.getVrfName(), equalTo("public"));
  }

  @Test
  public void testDynamicNatRule() {
    HuaweiNatRule rule = new HuaweiNatRule("dynamic-nat", HuaweiNatRule.NatType.DYNAMIC);
    rule.setAclName("2000");
    rule.setPoolName("pool1");

    List<Ip> pool = new ArrayList<>();
    pool.add(Ip.parse("192.168.1.1"));
    pool.add(Ip.parse("192.168.1.2"));
    rule.setAddressPool(pool);

    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.DYNAMIC));
    assertThat(rule.getAclName(), equalTo("2000"));
    assertThat(rule.getPoolName(), equalTo("pool1"));
    assertThat(rule.getAddressPool(), hasSize(2));
  }

  @Test
  public void testEasyIpRule() {
    HuaweiNatRule rule = new HuaweiNatRule("easy-ip", HuaweiNatRule.NatType.EASY_IP);
    rule.setAclName("3001");
    rule.setInterfaceName("GigabitEthernet0/0/1");

    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.EASY_IP));
    assertThat(rule.getAclName(), equalTo("3001"));
    assertThat(rule.getInterfaceName(), equalTo("GigabitEthernet0/0/1"));
  }

  @Test
  public void testNatServerRule() {
    HuaweiNatRule rule = new HuaweiNatRule("nat-server", HuaweiNatRule.NatType.NAT_SERVER);
    rule.setGlobalIp(Ip.parse("192.168.1.1"));
    rule.setGlobalPort(80);
    rule.setInsideLocalIp(Ip.parse("10.0.0.1"));
    rule.setInsideLocalPort(8080);
    rule.setProtocol("tcp");

    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.NAT_SERVER));
    assertThat(rule.getGlobalIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(rule.getGlobalPort(), equalTo(80));
    assertThat(rule.getInsideLocalIp(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(rule.getInsideLocalPort(), equalTo(8080));
    assertThat(rule.getProtocol(), equalTo("tcp"));
  }

  @Test
  public void testNatAlgRule() {
    HuaweiNatRule rule = new HuaweiNatRule("nat-alg", HuaweiNatRule.NatType.NAT_ALG);
    assertThat(rule.getType(), equalTo(HuaweiNatRule.NatType.NAT_ALG));
  }

  @Test
  public void testEmptyAddressPoolInitialState() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);
    // Constructor initializes empty ArrayList
    assertThat(rule.getAddressPool(), hasSize(0));
    assertTrue(rule.getAddressPool().isEmpty());
  }

  @Test
  public void testMultipleAddressPoolOperations() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);

    // Add IPs individually
    rule.addAddressPoolIp(Ip.parse("192.168.1.1"));
    rule.addAddressPoolIp(Ip.parse("192.168.1.2"));
    assertThat(rule.getAddressPool(), hasSize(2));

    // Replace with new list
    List<Ip> newPool = new ArrayList<>();
    newPool.add(Ip.parse("10.0.0.1"));
    rule.setAddressPool(newPool);
    assertThat(rule.getAddressPool(), hasSize(1));
    assertThat(rule.getAddressPool().get(0), equalTo(Ip.parse("10.0.0.1")));

    // Set to null and add again
    rule.setAddressPool(null);
    rule.addAddressPoolIp(Ip.parse("172.16.1.1"));
    assertThat(rule.getAddressPool(), hasSize(1));
    assertThat(rule.getAddressPool().get(0), equalTo(Ip.parse("172.16.1.1")));
  }

  @Test
  public void testNullSetterOperations() {
    HuaweiNatRule rule = new HuaweiNatRule("test", HuaweiNatRule.NatType.DYNAMIC);

    // Set all optional fields to non-null values
    rule.setAclName("2000");
    rule.setPoolName("pool1");
    rule.setGlobalIp(Ip.parse("192.168.1.1"));
    rule.setGlobalPort(80);
    rule.setInsideLocalIp(Ip.parse("10.0.0.1"));
    rule.setInsideLocalPort(8080);
    rule.setInterfaceName("GigabitEthernet0/0/1");
    rule.setProtocol("tcp");
    rule.setVrfName("vrf1");

    // Verify they're set
    assertThat(rule.getAclName(), equalTo("2000"));
    assertThat(rule.getPoolName(), equalTo("pool1"));
    assertThat(rule.getGlobalIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(rule.getGlobalPort(), equalTo(80));
    assertThat(rule.getInsideLocalIp(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(rule.getInsideLocalPort(), equalTo(8080));
    assertThat(rule.getInterfaceName(), equalTo("GigabitEthernet0/0/1"));
    assertThat(rule.getProtocol(), equalTo("tcp"));
    assertThat(rule.getVrfName(), equalTo("vrf1"));

    // Set all to null
    rule.setAclName(null);
    rule.setPoolName(null);
    rule.setGlobalIp(null);
    rule.setGlobalPort(null);
    rule.setInsideLocalIp(null);
    rule.setInsideLocalPort(null);
    rule.setInterfaceName(null);
    rule.setProtocol(null);
    rule.setVrfName(null);

    // Verify they're null
    assertNull(rule.getAclName());
    assertNull(rule.getPoolName());
    assertNull(rule.getGlobalIp());
    assertNull(rule.getGlobalPort());
    assertNull(rule.getInsideLocalIp());
    assertNull(rule.getInsideLocalPort());
    assertNull(rule.getInterfaceName());
    assertNull(rule.getProtocol());
    assertNull(rule.getVrfName());
  }
}

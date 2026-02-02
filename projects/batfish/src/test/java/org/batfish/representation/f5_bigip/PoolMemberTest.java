package org.batfish.representation.f5_bigip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.junit.Test;

/** Tests for {@link PoolMember} */
public class PoolMemberTest {

  @Test
  public void testConstructorValid() {
    PoolMember pm = new PoolMember("name", "node", 80);
    assertEquals("name", pm.getName());
    assertEquals("node", pm.getNode());
    assertEquals(80, pm.getPort());
  }

  @Test
  public void testConstructorMinValidPort() {
    PoolMember pm = new PoolMember("name", "node", 1);
    assertEquals(1, pm.getPort());
  }

  @Test
  public void testConstructorMaxValidPort() {
    PoolMember pm = new PoolMember("name", "node", 65535);
    assertEquals(65535, pm.getPort());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorInvalidPortNegative() {
    new PoolMember("name", "node", -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorInvalidPortTooLarge() {
    new PoolMember("name", "node", 65536);
  }

  @Test
  public void testGetSetAddress() {
    PoolMember pm = new PoolMember("name", "node", 80);
    assertNull(pm.getAddress());

    Ip addr = Ip.parse("1.2.3.4");
    pm.setAddress(addr);
    assertEquals(addr, pm.getAddress());
    assertSame(addr, pm.getAddress());
  }

  @Test
  public void testGetSetAddress6() {
    PoolMember pm = new PoolMember("name", "node", 80);
    assertNull(pm.getAddress6());

    Ip6 addr6 = Ip6.parse("::1");
    pm.setAddress6(addr6);
    assertEquals(addr6, pm.getAddress6());
    assertSame(addr6, pm.getAddress6());
  }

  @Test
  public void testGetSetDescription() {
    PoolMember pm = new PoolMember("name", "node", 80);
    assertNull(pm.getDescription());

    pm.setDescription("test description");
    assertEquals("test description", pm.getDescription());
  }

  @Test
  public void testSetAddressNull() {
    PoolMember pm = new PoolMember("name", "node", 80);
    pm.setAddress(Ip.parse("1.2.3.4"));
    pm.setAddress(null);
    assertNull(pm.getAddress());
  }

  @Test
  public void testSetAddress6Null() {
    PoolMember pm = new PoolMember("name", "node", 80);
    pm.setAddress6(Ip6.parse("::1"));
    pm.setAddress6(null);
    assertNull(pm.getAddress6());
  }

  @Test
  public void testSetDescriptionNull() {
    PoolMember pm = new PoolMember("name", "node", 80);
    pm.setDescription("test");
    pm.setDescription(null);
    assertNull(pm.getDescription());
  }
}

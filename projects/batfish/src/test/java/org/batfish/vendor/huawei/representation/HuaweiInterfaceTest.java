package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link HuaweiInterface}. */
public class HuaweiInterfaceTest {

  @Test
  public void testConstructor() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    assertThat(iface.getName(), equalTo("GigabitEthernet0/0/0"));
    assertThat(iface.getDescription(), nullValue());
    assertThat(iface.getAddress(), nullValue());
    assertThat(iface.getShutdown(), equalTo(false));
    assertThat(iface.getMtu(), equalTo(1500));
    assertThat(iface.getBandwidth(), nullValue());
    assertThat(iface.getIncomingFilter(), nullValue());
    assertThat(iface.getOutgoingFilter(), nullValue());
    assertThat(iface.getDhcpRelayAddresses(), equalTo(new TreeSet<>()));
    assertThat(iface.getDhcpRelayClient(), equalTo(false));
  }

  @Test
  public void testSetName() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    iface.setName("GigabitEthernet0/0/1");
    assertThat(iface.getName(), equalTo("GigabitEthernet0/0/1"));
  }

  @Test
  public void testSetDescription() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    iface.setDescription("Test interface");
    assertThat(iface.getDescription(), equalTo("Test interface"));

    iface.setDescription(null);
    assertThat(iface.getDescription(), nullValue());
  }

  @Test
  public void testSetAddress() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    ConcreteInterfaceAddress address = ConcreteInterfaceAddress.parse("192.168.1.1/24");
    iface.setAddress(address);
    assertThat(iface.getAddress(), equalTo(address));
    assertThat(iface.getAddress().getIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(iface.getAddress().getNetworkBits(), equalTo(24));

    iface.setAddress(null);
    assertThat(iface.getAddress(), nullValue());
  }

  @Test
  public void testSetShutdown() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    assertThat(iface.getShutdown(), equalTo(false));

    iface.setShutdown(true);
    assertThat(iface.getShutdown(), equalTo(true));

    iface.setShutdown(false);
    assertThat(iface.getShutdown(), equalTo(false));
  }

  @Test
  public void testSetMtu() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    assertThat(iface.getMtu(), equalTo(1500));

    iface.setMtu(9000);
    assertThat(iface.getMtu(), equalTo(9000));
  }

  @Test
  public void testSetBandwidth() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    assertThat(iface.getBandwidth(), nullValue());

    iface.setBandwidth(1E9D);
    assertThat(iface.getBandwidth(), equalTo(1E9D));

    iface.setBandwidth(null);
    assertThat(iface.getBandwidth(), nullValue());
  }

  @Test
  public void testSetIncomingFilter() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    assertThat(iface.getIncomingFilter(), nullValue());

    iface.setIncomingFilter("acl_in");
    assertThat(iface.getIncomingFilter(), equalTo("acl_in"));

    iface.setIncomingFilter(null);
    assertThat(iface.getIncomingFilter(), nullValue());
  }

  @Test
  public void testSetOutgoingFilter() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    assertThat(iface.getOutgoingFilter(), nullValue());

    iface.setOutgoingFilter("acl_out");
    assertThat(iface.getOutgoingFilter(), equalTo("acl_out"));

    iface.setOutgoingFilter(null);
    assertThat(iface.getOutgoingFilter(), nullValue());
  }

  @Test
  public void testSetDhcpRelayAddresses() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    SortedSet<Ip> addresses = new TreeSet<>();
    addresses.add(Ip.parse("192.168.1.1"));
    addresses.add(Ip.parse("192.168.1.2"));

    iface.setDhcpRelayAddresses(addresses);
    assertThat(iface.getDhcpRelayAddresses().size(), equalTo(2));
    assertTrue(iface.getDhcpRelayAddresses().contains(Ip.parse("192.168.1.1")));
    assertTrue(iface.getDhcpRelayAddresses().contains(Ip.parse("192.168.1.2")));
  }

  @Test
  public void testAddDhcpRelayAddress() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    assertThat(iface.getDhcpRelayAddresses().size(), equalTo(0));

    iface.addDhcpRelayAddress(Ip.parse("192.168.1.1"));
    assertThat(iface.getDhcpRelayAddresses().size(), equalTo(1));
    assertTrue(iface.getDhcpRelayAddresses().contains(Ip.parse("192.168.1.1")));

    iface.addDhcpRelayAddress(Ip.parse("192.168.1.2"));
    assertThat(iface.getDhcpRelayAddresses().size(), equalTo(2));
    assertTrue(iface.getDhcpRelayAddresses().contains(Ip.parse("192.168.1.2")));
  }

  @Test
  public void testSetDhcpRelayClient() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    assertThat(iface.getDhcpRelayClient(), equalTo(false));

    iface.setDhcpRelayClient(true);
    assertThat(iface.getDhcpRelayClient(), equalTo(true));

    iface.setDhcpRelayClient(false);
    assertThat(iface.getDhcpRelayClient(), equalTo(false));
  }

  @Test
  public void testGetDefaultBandwidth_GigabitEthernet() {
    assertThat(HuaweiInterface.getDefaultBandwidth("GigabitEthernet0/0/0"), equalTo(1E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("GigabitEthernet1/0/1"), equalTo(1E9D));
  }

  @Test
  public void testGetDefaultBandwidth_10GE() {
    assertThat(HuaweiInterface.getDefaultBandwidth("10GE0/0/0"), equalTo(10E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("10GE1/0/1"), equalTo(10E9D));
  }

  @Test
  public void testGetDefaultBandwidth_25GE() {
    assertThat(HuaweiInterface.getDefaultBandwidth("25GE0/0/0"), equalTo(25E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("25GE1/0/1"), equalTo(25E9D));
  }

  @Test
  public void testGetDefaultBandwidth_40GE() {
    assertThat(HuaweiInterface.getDefaultBandwidth("40GE0/0/0"), equalTo(40E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("40GE1/0/1"), equalTo(40E9D));
  }

  @Test
  public void testGetDefaultBandwidth_100GE() {
    assertThat(HuaweiInterface.getDefaultBandwidth("100GE0/0/0"), equalTo(100E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("100GE1/0/1"), equalTo(100E9D));
  }

  @Test
  public void testGetDefaultBandwidth_Ethernet() {
    assertThat(HuaweiInterface.getDefaultBandwidth("Ethernet0/0/0"), equalTo(100E6D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Ethernet1/0/1"), equalTo(100E6D));
  }

  @Test
  public void testGetDefaultBandwidth_Loopback() {
    assertThat(HuaweiInterface.getDefaultBandwidth("Loopback0"), equalTo(8E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Loopback1"), equalTo(8E9D));
  }

  @Test
  public void testGetDefaultBandwidth_Vlanif() {
    assertThat(HuaweiInterface.getDefaultBandwidth("Vlanif100"), equalTo(1E9D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Vlanif200"), equalTo(1E9D));
  }

  @Test
  public void testGetDefaultBandwidth_Pos() {
    assertThat(HuaweiInterface.getDefaultBandwidth("Pos0/0/0"), equalTo(155E6D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Pos1/0/1"), equalTo(155E6D));
  }

  @Test
  public void testGetDefaultBandwidth_Serial() {
    assertThat(HuaweiInterface.getDefaultBandwidth("Serial0/0/0"), equalTo(1.544E6D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Serial1/0/1"), equalTo(1.544E6D));
  }

  @Test
  public void testGetDefaultBandwidth_Tunnel() {
    assertThat(HuaweiInterface.getDefaultBandwidth("Tunnel0"), equalTo(100E3D));
    assertThat(HuaweiInterface.getDefaultBandwidth("Tunnel1"), equalTo(100E3D));
  }

  @Test
  public void testGetDefaultBandwidth_Unknown() {
    assertThat(HuaweiInterface.getDefaultBandwidth("UnknownInterface0"), nullValue());
    assertThat(HuaweiInterface.getDefaultBandwidth("SomeOtherInterface"), nullValue());
  }

  @Test
  public void testToString() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    iface.setDescription("Test interface");
    iface.setAddress(ConcreteInterfaceAddress.parse("192.168.1.1/24"));
    iface.setShutdown(true);
    iface.setMtu(9000);

    String toString = iface.toString();
    assertTrue(toString.contains("GigabitEthernet0/0/0"));
    assertTrue(toString.contains("Test interface"));
    assertTrue(toString.contains("192.168.1.1/24"));
    assertTrue(toString.contains("shutdown=true"));
    assertTrue(toString.contains("mtu=9000"));
  }

  @Test
  public void testToString_DefaultValues() {
    HuaweiInterface iface = new HuaweiInterface("GigabitEthernet0/0/0");
    String toString = iface.toString();
    assertTrue(toString.contains("GigabitEthernet0/0/0"));
    assertTrue(toString.contains("shutdown=false"));
    assertTrue(toString.contains("mtu=1500"));
  }
}

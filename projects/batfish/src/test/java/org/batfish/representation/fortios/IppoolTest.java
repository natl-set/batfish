package org.batfish.representation.fortios;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Ip;
import org.junit.Test;

public class IppoolTest {
  private static final BatfishUUID UUID = new BatfishUUID(1);

  @Test
  public void testConstructor() {
    Ippool ippool = new Ippool("test-pool", UUID);
    assertThat(ippool.getName(), equalTo("test-pool"));
    assertThat(ippool.getBatfishUUID(), equalTo(UUID));
  }

  @Test
  public void testGettersSetters() {
    Ippool ippool = new Ippool("test-pool", UUID);

    // Test initial values
    assertThat(ippool.getAssociatedInterface(), nullValue());
    assertThat(ippool.getComments(), nullValue());
    assertThat(ippool.getEndip(), nullValue());
    assertThat(ippool.getStartip(), nullValue());
    assertThat(ippool.getType(), nullValue());

    // Set and verify each value
    ippool.setAssociatedInterface("port1");
    assertThat(ippool.getAssociatedInterface(), equalTo("port1"));

    ippool.setComments("Test comment");
    assertThat(ippool.getComments(), equalTo("Test comment"));

    Ip startIp = Ip.parse("192.168.1.1");
    ippool.setStartip(startIp);
    assertThat(ippool.getStartip(), equalTo(startIp));

    Ip endIp = Ip.parse("192.168.1.100");
    ippool.setEndip(endIp);
    assertThat(ippool.getEndip(), equalTo(endIp));

    ippool.setType(Ippool.Type.ONE_TO_ONE);
    assertThat(ippool.getType(), equalTo(Ippool.Type.ONE_TO_ONE));
  }

  @Test
  public void testAllTypes() {
    Ippool ippool = new Ippool("test-pool", UUID);

    ippool.setType(Ippool.Type.FIXED_PORT_RANGE);
    assertThat(ippool.getType(), equalTo(Ippool.Type.FIXED_PORT_RANGE));

    ippool.setType(Ippool.Type.ONE_TO_ONE);
    assertThat(ippool.getType(), equalTo(Ippool.Type.ONE_TO_ONE));

    ippool.setType(Ippool.Type.OVERLOAD);
    assertThat(ippool.getType(), equalTo(Ippool.Type.OVERLOAD));

    ippool.setType(Ippool.Type.PORT_BLOCK_ALLOCATION);
    assertThat(ippool.getType(), equalTo(Ippool.Type.PORT_BLOCK_ALLOCATION));
  }

  @Test
  public void testSetName() {
    Ippool ippool = new Ippool("old-name", UUID);
    assertThat(ippool.getName(), equalTo("old-name"));

    ippool.setName("new-name");
    assertThat(ippool.getName(), equalTo("new-name"));
  }

  @Test
  public void testIpRange() {
    Ippool ippool = new Ippool("test-pool", UUID);

    ippool.setStartip(Ip.parse("10.0.0.1"));
    ippool.setEndip(Ip.parse("10.0.0.255"));

    assertThat(ippool.getStartip(), equalTo(Ip.parse("10.0.0.1")));
    assertThat(ippool.getEndip(), equalTo(Ip.parse("10.0.0.255")));
  }

  @Test
  public void testNullability() {
    Ippool ippool = new Ippool("test-pool", UUID);

    // All fields should accept null
    ippool.setAssociatedInterface(null);
    ippool.setComments(null);
    ippool.setStartip(null);
    ippool.setEndip(null);
    ippool.setType(null);

    assertThat(ippool.getAssociatedInterface(), nullValue());
    assertThat(ippool.getComments(), nullValue());
    assertThat(ippool.getStartip(), nullValue());
    assertThat(ippool.getEndip(), nullValue());
    assertThat(ippool.getType(), nullValue());
  }
}

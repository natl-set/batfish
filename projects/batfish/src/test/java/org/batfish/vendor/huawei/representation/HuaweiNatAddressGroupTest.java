package org.batfish.vendor.huawei.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Tests for {@link HuaweiNatAddressGroup}. */
public class HuaweiNatAddressGroupTest {

  @Test
  public void testConstructor() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);
    assertThat(group.getIndex(), equalTo(1));
    assertThat(group.getRanges(), hasSize(0));
  }

  @Test
  public void testConstructorWithDifferentIndices() {
    HuaweiNatAddressGroup group1 = new HuaweiNatAddressGroup(0);
    HuaweiNatAddressGroup group2 = new HuaweiNatAddressGroup(100);
    HuaweiNatAddressGroup group3 = new HuaweiNatAddressGroup(65535);

    assertThat(group1.getIndex(), equalTo(0));
    assertThat(group2.getIndex(), equalTo(100));
    assertThat(group3.getIndex(), equalTo(65535));
  }

  @Test
  public void testAddRangeWithoutMask() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);
    assertThat(group.getRanges(), hasSize(0));

    group.addRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"));

    assertThat(group.getRanges(), hasSize(1));
    HuaweiNatAddressGroup.IpRange range = group.getRanges().get(0);
    assertThat(range.getStartIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(range.getEndIp(), equalTo(Ip.parse("192.168.1.100")));
    assertThat(range.getMask(), nullValue());
  }

  @Test
  public void testAddRangeWithMask() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);

    group.addRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.1"), Ip.parse("255.255.255.0"));

    assertThat(group.getRanges(), hasSize(1));
    HuaweiNatAddressGroup.IpRange range = group.getRanges().get(0);
    assertThat(range.getStartIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(range.getEndIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(range.getMask(), equalTo(Ip.parse("255.255.255.0")));
  }

  @Test
  public void testAddMultipleRanges() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);

    group.addRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"));
    group.addRange(Ip.parse("192.168.2.1"), Ip.parse("192.168.2.100"));
    group.addRange(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.100"));

    assertThat(group.getRanges(), hasSize(3));
    assertThat(group.getRanges().get(0).getStartIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(group.getRanges().get(1).getStartIp(), equalTo(Ip.parse("192.168.2.1")));
    assertThat(group.getRanges().get(2).getStartIp(), equalTo(Ip.parse("10.0.0.1")));
  }

  @Test
  public void testSetRanges() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);

    List<HuaweiNatAddressGroup.IpRange> ranges = new ArrayList<>();
    ranges.add(
        new HuaweiNatAddressGroup.IpRange(
            Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"), null));
    ranges.add(
        new HuaweiNatAddressGroup.IpRange(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.100"), null));

    group.setRanges(ranges);

    assertThat(group.getRanges(), equalTo(ranges));
    assertThat(group.getRanges(), hasSize(2));
  }

  @Test
  public void testSetRangesReplacesExisting() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);

    group.addRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"));
    assertThat(group.getRanges(), hasSize(1));

    List<HuaweiNatAddressGroup.IpRange> newRanges = new ArrayList<>();
    newRanges.add(
        new HuaweiNatAddressGroup.IpRange(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.100"), null));

    group.setRanges(newRanges);

    assertThat(group.getRanges(), hasSize(1));
    assertThat(group.getRanges().get(0).getStartIp(), equalTo(Ip.parse("10.0.0.1")));
  }

  @Test
  public void testIpRangeConstructor() {
    HuaweiNatAddressGroup.IpRange range =
        new HuaweiNatAddressGroup.IpRange(
            Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"), Ip.parse("255.255.255.0"));

    assertThat(range.getStartIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(range.getEndIp(), equalTo(Ip.parse("192.168.1.100")));
    assertThat(range.getMask(), equalTo(Ip.parse("255.255.255.0")));
  }

  @Test
  public void testIpRangeConstructorWithNullMask() {
    HuaweiNatAddressGroup.IpRange range =
        new HuaweiNatAddressGroup.IpRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"), null);

    assertThat(range.getStartIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(range.getEndIp(), equalTo(Ip.parse("192.168.1.100")));
    assertThat(range.getMask(), nullValue());
  }

  @Test
  public void testIpRangeToStringWithoutMask() {
    HuaweiNatAddressGroup.IpRange range =
        new HuaweiNatAddressGroup.IpRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"), null);

    assertThat(range.toString(), equalTo("192.168.1.1 192.168.1.100"));
  }

  @Test
  public void testIpRangeToStringWithMask() {
    HuaweiNatAddressGroup.IpRange range =
        new HuaweiNatAddressGroup.IpRange(
            Ip.parse("192.168.1.1"), Ip.parse("192.168.1.1"), Ip.parse("255.255.255.0"));

    assertThat(range.toString(), equalTo("192.168.1.1 255.255.255.0"));
  }

  @Test
  public void testToString() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);
    group.addRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"));

    String str = group.toString();
    assertThat(
        str, equalTo("HuaweiNatAddressGroup{_index=1, _ranges=[192.168.1.1 192.168.1.100]}"));
  }

  @Test
  public void testToStringWithMultipleRanges() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(5);
    group.addRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"));
    group.addRange(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.100"), Ip.parse("255.255.255.0"));

    String str = group.toString();
    assertThat(
        str,
        equalTo(
            "HuaweiNatAddressGroup{_index=5, _ranges=[192.168.1.1 192.168.1.100, 10.0.0.1"
                + " 255.255.255.0]}"));
  }

  @Test
  public void testAddRangeWithSameStartAndEndIp() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);

    group.addRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.1"));

    assertThat(group.getRanges(), hasSize(1));
    HuaweiNatAddressGroup.IpRange range = group.getRanges().get(0);
    assertThat(range.getStartIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(range.getEndIp(), equalTo(Ip.parse("192.168.1.1")));
  }

  @Test
  public void testFullConfiguration() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(10);

    group.addRange(Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"));
    group.addRange(Ip.parse("192.168.2.1"), Ip.parse("192.168.2.100"));
    group.addRange(Ip.parse("10.0.0.1"), Ip.parse("10.0.0.1"), Ip.parse("255.255.255.255"));

    assertThat(group.getIndex(), equalTo(10));
    assertThat(group.getRanges(), hasSize(3));
    assertThat(group.getRanges().get(0).getStartIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(group.getRanges().get(0).getEndIp(), equalTo(Ip.parse("192.168.1.100")));
    assertThat(group.getRanges().get(2).getMask(), equalTo(Ip.parse("255.255.255.255")));
  }

  @Test
  public void testRangesListIsMutable() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);
    List<HuaweiNatAddressGroup.IpRange> ranges = group.getRanges();

    assertThat(ranges, hasSize(0));

    ranges.add(
        new HuaweiNatAddressGroup.IpRange(
            Ip.parse("192.168.1.1"), Ip.parse("192.168.1.100"), null));

    assertThat(group.getRanges(), hasSize(1));
  }

  @Test
  public void testEmptyRangesToString() {
    HuaweiNatAddressGroup group = new HuaweiNatAddressGroup(1);

    String str = group.toString();
    assertThat(str, equalTo("HuaweiNatAddressGroup{_index=1, _ranges=[]}"));
  }
}

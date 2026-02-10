package org.batfish.representation.fortios;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class BfdSettingsTest {
  private static final BatfishUUID UUID = new BatfishUUID(1);

  @Test
  public void testGetters() {
    BfdSettings bfd = new BfdSettings();
    bfd.setInterval(100);
    bfd.setMinRx(200);
    bfd.setMinTx(300);
    bfd.setMultiplier(5);

    assertThat(bfd.getInterval(), equalTo(100));
    assertThat(bfd.getMinRx(), equalTo(200));
    assertThat(bfd.getMinTx(), equalTo(300));
    assertThat(bfd.getMultiplier(), equalTo(5));
  }

  @Test
  public void testGettersDefault() {
    BfdSettings bfd = new BfdSettings();

    assertThat(bfd.getInterval(), nullValue());
    assertThat(bfd.getMinRx(), nullValue());
    assertThat(bfd.getMinTx(), nullValue());
    assertThat(bfd.getMultiplier(), nullValue());
  }

  @Test
  public void testGetIntervalEffective() {
    BfdSettings bfd = new BfdSettings();

    // Default value when not set
    assertThat(bfd.getIntervalEffective(), equalTo(BfdSettings.DEFAULT_INTERVAL));

    // Explicit value when set
    bfd.setInterval(100);
    assertThat(bfd.getIntervalEffective(), equalTo(100));
  }

  @Test
  public void testGetMinRxEffective() {
    BfdSettings bfd = new BfdSettings();

    // Default value when not set
    assertThat(bfd.getMinRxEffective(), equalTo(BfdSettings.DEFAULT_MIN_RX));

    // Explicit value when set
    bfd.setMinRx(200);
    assertThat(bfd.getMinRxEffective(), equalTo(200));
  }

  @Test
  public void testGetMinTxEffective() {
    BfdSettings bfd = new BfdSettings();

    // Default value when not set
    assertThat(bfd.getMinTxEffective(), equalTo(BfdSettings.DEFAULT_MIN_TX));

    // Explicit value when set
    bfd.setMinTx(300);
    assertThat(bfd.getMinTxEffective(), equalTo(300));
  }

  @Test
  public void testGetMultiplierEffective() {
    BfdSettings bfd = new BfdSettings();

    // Default value when not set
    assertThat(bfd.getMultiplierEffective(), equalTo(BfdSettings.DEFAULT_MULTIPLIER));

    // Explicit value when set
    bfd.setMultiplier(5);
    assertThat(bfd.getMultiplierEffective(), equalTo(5));
  }

  @Test
  public void testSetters() {
    BfdSettings bfd = new BfdSettings();

    bfd.setInterval(100);
    bfd.setMinRx(200);
    bfd.setMinTx(300);
    bfd.setMultiplier(5);

    assertThat(bfd.getInterval(), equalTo(100));
    assertThat(bfd.getMinRx(), equalTo(200));
    assertThat(bfd.getMinTx(), equalTo(300));
    assertThat(bfd.getMultiplier(), equalTo(5));
  }

  @Test
  public void testSetNull() {
    BfdSettings bfd = new BfdSettings();

    bfd.setInterval(100);
    bfd.setInterval(null);
    assertThat(bfd.getInterval(), nullValue());
    assertThat(bfd.getIntervalEffective(), equalTo(BfdSettings.DEFAULT_INTERVAL));
  }
}

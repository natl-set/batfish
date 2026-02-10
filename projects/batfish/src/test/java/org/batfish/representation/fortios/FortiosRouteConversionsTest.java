package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.junit.Test;

public class FortiosRouteConversionsTest {
  private static final String FILENAME = "test-config";

  @Test
  public void testConvertStaticRoute_enabledWithGateway() {
    StaticRoute route = new StaticRoute("1");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.0.0/24"));
    route.setGateway(org.batfish.datamodel.Ip.parse("192.168.1.1"));

    Optional<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoute(route);

    assertTrue("Route should be converted", result.isPresent());
    org.batfish.datamodel.StaticRoute converted = result.get();
    assertThat(converted.getNetwork(), equalTo(Prefix.parse("10.0.0.0/24")));
    assertThat(
        converted.getNextHop().getGateway(),
        equalTo(org.batfish.datamodel.Ip.parse("192.168.1.1")));
    assertThat(converted.getAdmin(), equalTo(10)); // DEFAULT_DISTANCE
  }

  @Test
  public void testConvertStaticRoute_enabledWithInterfaceOnly() {
    StaticRoute route = new StaticRoute("2");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port2");
    route.setDst(Prefix.parse("10.0.1.0/24"));
    route.setGateway(null);

    Optional<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoute(route);

    assertTrue("Route should be converted", result.isPresent());
    org.batfish.datamodel.StaticRoute converted = result.get();
    assertThat(converted.getNetwork(), equalTo(Prefix.parse("10.0.1.0/24")));
    assertThat(converted.getNextHop().getInterface(), equalTo("port2"));
  }

  @Test
  public void testConvertStaticRoute_disabledRoute() {
    StaticRoute route = new StaticRoute("3");
    route.setStatus(StaticRoute.Status.DISABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.2.0/24"));

    Optional<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoute(route);

    assertFalse("Disabled route should not be converted", result.isPresent());
  }

  @Test
  public void testConvertStaticRoute_withCustomDistance() {
    StaticRoute route = new StaticRoute("4");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.3.0/24"));
    route.setGateway(org.batfish.datamodel.Ip.parse("192.168.1.1"));
    route.setDistance(50);

    Optional<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoute(route);

    assertTrue("Route should be converted", result.isPresent());
    org.batfish.datamodel.StaticRoute converted = result.get();
    assertThat(converted.getAdmin(), equalTo(50));
  }

  @Test
  public void testConvertStaticRoute_sdwanRoute() {
    StaticRoute route = new StaticRoute("5");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.4.0/24"));
    route.setGateway(org.batfish.datamodel.Ip.parse("192.168.1.1"));
    route.setSdwanEnabled(true);

    Optional<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoute(route);

    assertTrue("Route should be converted", result.isPresent());
    org.batfish.datamodel.StaticRoute converted = result.get();
    assertThat(converted.getAdmin(), equalTo(1)); // DEFAULT_DISTANCE_SDWAN
  }

  @Test
  public void testConvertStaticRoutes_singleRoute() {
    StaticRoute route = new StaticRoute("1");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.0.0/24"));
    route.setGateway(org.batfish.datamodel.Ip.parse("192.168.1.1"));

    java.util.SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(ImmutableMap.of("route1", route));

    assertThat(result.size(), equalTo(1));
  }

  @Test
  public void testConvertStaticRoutes_multipleRoutes() {
    StaticRoute route1 = new StaticRoute("1");
    route1.setStatus(StaticRoute.Status.ENABLE);
    route1.setDevice("port1");
    route1.setDst(Prefix.parse("10.0.0.0/24"));
    route1.setGateway(org.batfish.datamodel.Ip.parse("192.168.1.1"));

    StaticRoute route2 = new StaticRoute("2");
    route2.setStatus(StaticRoute.Status.ENABLE);
    route2.setDevice("port2");
    route2.setDst(Prefix.parse("10.0.1.0/24"));
    route2.setGateway(org.batfish.datamodel.Ip.parse("192.168.2.1"));

    java.util.SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(
            ImmutableMap.of("route1", route1, "route2", route2));

    assertThat(result.size(), equalTo(2));
  }

  @Test
  public void testConvertStaticRoutes_filtersDisabled() {
    StaticRoute route1 = new StaticRoute("1");
    route1.setStatus(StaticRoute.Status.ENABLE);
    route1.setDevice("port1");
    route1.setDst(Prefix.parse("10.0.0.0/24"));
    route1.setGateway(org.batfish.datamodel.Ip.parse("192.168.1.1"));

    StaticRoute route2 = new StaticRoute("2");
    route2.setStatus(StaticRoute.Status.DISABLE);
    route2.setDevice("port2");
    route2.setDst(Prefix.parse("10.0.1.0/24"));

    java.util.SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(
            ImmutableMap.of("route1", route1, "route2", route2));

    // Only enabled route should be converted
    assertThat(result.size(), equalTo(1));
  }

  @Test
  public void testConvertStaticRoutes_empty() {
    java.util.SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(ImmutableMap.of());

    assertThat(result.size(), equalTo(0));
  }
}

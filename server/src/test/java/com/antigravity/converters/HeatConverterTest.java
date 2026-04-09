package com.antigravity.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.antigravity.models.Driver;
import com.antigravity.models.HeatScoring;
import com.antigravity.models.Team;
import com.antigravity.proto.DriverHeatData;
import com.antigravity.proto.Heat;
import com.antigravity.race.RaceParticipant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class HeatConverterTest {

  @Test
  public void testToProto_PopulatesActualDriver() {
    // Setup
    Driver driver =
        new Driver(
            "driver1",
            "Driver One",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "d1",
            null);
    // Team constructor: name, avatarUrl, driverIds, entityId, id
    Team team = new Team("Team One", null, new ArrayList<>(), "t1", null);

    RaceParticipant participant = new RaceParticipant(team);
    com.antigravity.race.DriverHeatData heatData =
        new com.antigravity.race.DriverHeatData(participant);
    heatData.setActualDriver(driver);

    List<com.antigravity.race.DriverHeatData> heatDrivers = new ArrayList<>();
    heatDrivers.add(heatData);

    HeatScoring scoring = new HeatScoring();
    com.antigravity.race.Heat heat = new com.antigravity.race.Heat(1, heatDrivers, scoring);
    Set<String> sentObjectIds = new HashSet<>();

    // Execute
    Heat proto = HeatConverter.toProto(heat, sentObjectIds);

    // Verify
    assertNotNull(proto);
    assertEquals(1, proto.getHeatDriversCount());
    DriverHeatData driverProto = proto.getHeatDrivers(0);

    assertEquals("d1", driverProto.getDriverId());
    assertTrue(driverProto.hasActualDriver());
    assertEquals("driver1", driverProto.getActualDriver().getName());
  }
}

package com.antigravity.race;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.antigravity.models.HeatRotationType;
import com.antigravity.models.Race;
import com.antigravity.models.Track;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ConstructorTest {

  @Test
  public void test() {
    Race model = mock(Race.class);
    when(model.getHeatRotationType()).thenReturn(HeatRotationType.RoundRobin);
    List<RaceParticipant> drivers = new ArrayList<>();
    drivers.add(
        new RaceParticipant(
            new com.antigravity.models.Driver(
                "d1", "Driver 1", null, null, null, null, null, null, null, null, null, "d1", null),
            "p1"));
    Track track = mock(Track.class);
    com.antigravity.race.Race race =
        new com.antigravity.race.Race.Builder()
            .model(model)
            .drivers(drivers)
            .track(track)
            .isDemoMode(true)
            .build();
  }
}

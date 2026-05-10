package com.antigravity.race;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.antigravity.context.DatabaseContext;
import com.antigravity.models.*;
import com.antigravity.race.states.Racing;
import com.antigravity.race.states.Starting;
import com.antigravity.service.ServerConfigService;
import java.util.Collections;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

public class StartingStateTest {

  private com.antigravity.race.Race race;
  private ServerConfigService configService;

  @Before
  public void setUp() throws Exception {
    configService = mock(ServerConfigService.class);
    when(configService.getStartDelay()).thenReturn(0.0);
    when(configService.getRestartDelay()).thenReturn(0.0);

    DatabaseContext dbContext = mock(DatabaseContext.class);
    when(dbContext.getConfigService()).thenReturn(configService);

    ClientSubscriptionManager manager = ClientSubscriptionManager.getInstance();
    manager.setDatabaseContext(dbContext);

    Track mockTrack = mock(Track.class);
    when(mockTrack.getLanes()).thenReturn(Collections.singletonList(mock(Lane.class)));

    Driver mockDriver = new Driver("Test Driver", "D1", "driver1", new ObjectId());
    RaceParticipant mockParticipant = mock(RaceParticipant.class);
    when(mockParticipant.getDriver()).thenReturn(mockDriver);
    when(mockParticipant.getObjectId()).thenReturn("p1");

    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder().withStartTime(1.0).withRestartTime(2.0).build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(mockTrack)
            .drivers(Collections.singletonList(mockParticipant))
            .isDemoMode(true)
            .build();

    manager.setRace(race);
  }

  @Test
  public void testStartingUsesStartTimeForNewHeat() throws Exception {
    race.setHasRacedInCurrentHeat(false);
    Starting starting = new Starting();

    // We can't easily "spy" on the ticker since it's an anonymous class inside
    // enter()
    // but we can verify the state transitions.

    starting.enter(race);

    // Countdown is 1.0s (10 ticks). In each tick (100ms) it decrements.
    // 1.0s + some buffer.

    // After ~1s, it should transition to Racing
    long start = System.currentTimeMillis();
    while (!(race.getState() instanceof Racing) && (System.currentTimeMillis() - start) < 3000) {
      Thread.sleep(100);
    }

    assertTrue("Race should be in Racing state after startTime", race.getState() instanceof Racing);
    long duration = System.currentTimeMillis() - start;
    assertTrue(
        "Duration should be around 1000ms, was " + duration, duration >= 900 && duration < 2000);
  }

  @Test
  public void testStartingUsesRestartTimeForRestart() throws Exception {
    race.setHasRacedInCurrentHeat(true);
    Starting starting = new Starting();

    starting.enter(race);

    // After ~2s, it should transition to Racing
    long start = System.currentTimeMillis();
    while (!(race.getState() instanceof Racing) && (System.currentTimeMillis() - start) < 4000) {
      Thread.sleep(100);
    }

    assertTrue(
        "Race should be in Racing state after restartTime", race.getState() instanceof Racing);
    long duration = System.currentTimeMillis() - start;
    assertTrue(
        "Duration should be around 2000ms, was " + duration, duration >= 1900 && duration < 3000);
  }

  @Test
  public void testStartingWaitRandomDelay() throws Exception {
    // Set 1s countdown + 2s random delay in the model
    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder().withStartTime(1.0).withStartDelay(2.0).build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(race.getTrack())
            .drivers(race.getDrivers())
            .isDemoMode(true)
            .build();

    race.setHasRacedInCurrentHeat(false);

    Starting starting = new Starting();
    starting.enter(race);

    // Total wait should be between 1.0s and 3.0s (countdown + random delay)
    long start = System.currentTimeMillis();
    while (!(race.getState() instanceof Racing) && (System.currentTimeMillis() - start) < 5000) {
      Thread.sleep(100);
    }

    assertTrue("Race should be in Racing state", race.getState() instanceof Racing);
    long duration = System.currentTimeMillis() - start;
    // startTime is 1.0s. randomDelay is 0.1-2.0s.
    assertTrue("Duration should be at least 1000ms, was " + duration, duration >= 1000);
    assertTrue("Duration should be less than 4000ms, was " + duration, duration < 4000);
  }

  @Test
  public void testStartingUsesRestartDelayForRestart() throws Exception {
    // Set 0.5s countdown + 1.5s random restart delay
    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder()
            .withRestartTime(0.5)
            .withRestartDelay(1.5)
            .build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(race.getTrack())
            .drivers(race.getDrivers())
            .isDemoMode(true)
            .build();

    race.setHasRacedInCurrentHeat(true); // Is a restart

    Starting starting = new Starting();
    starting.enter(race);

    long start = System.currentTimeMillis();
    while (!(race.getState() instanceof Racing) && (System.currentTimeMillis() - start) < 5000) {
      Thread.sleep(100);
    }

    assertTrue("Race should be in Racing state", race.getState() instanceof Racing);
    long duration = System.currentTimeMillis() - start;
    // restartTime is 0.5s. randomDelay is 0.1-1.5s.
    assertTrue("Duration should be at least 500ms, was " + duration, duration >= 500);
    assertTrue("Duration should be less than 3000ms, was " + duration, duration < 3000);
  }

  @Test
  public void testFalseStartDetection() throws Exception {
    // Setup race with penalties
    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder()
            .withFalseStartLapPenalty(1.0)
            .withFalseStartTimePenalty(5.0)
            .build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(race.getTrack())
            .drivers(race.getDrivers())
            .isDemoMode(true)
            .build();

    Starting starting = new Starting();
    starting.enter(race);

    // Simulate a lap during Starting state (false start)
    boolean result = starting.onLap(0, 0.5, 1, false);

    // Verify
    assertTrue("onLap should return false for false start", !result);
    DriverHeatData dhd = race.getCurrentHeat().getDrivers().get(0);
    assertTrue("False starts should be incremented", dhd.getFalseStarts() == 1);
    assertTrue("Penalty laps should be set", dhd.getPenaltyLaps() == 1.0);
    assertTrue("Time penalty should be set", dhd.getRemainingFalseStartTimePenalty() == 5.0);
  }

  @Test
  public void testFalseStartRestart() throws Exception {
    // Setup race with restart on false start
    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder().withRestartOnFalseStart(true).build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(race.getTrack())
            .drivers(race.getDrivers())
            .isDemoMode(true)
            .build();

    Starting starting = new Starting();
    starting.enter(race);

    // Simulate false start
    starting.onLap(0, 0.5, 1, false);

    // Verify state changed to NotStarted
    assertTrue(
        "Race should transition to NotStarted on false start if configured",
        race.getState() instanceof com.antigravity.race.states.NotStarted);
  }

  @Test
  public void testHotStart() throws Exception {
    // Setup race with hot start
    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder()
            .from(race.getRaceModel())
            .withHotStart(true)
            .withStartTime(0.5)
            .build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(race.getTrack())
            .drivers(race.getDrivers())
            .isDemoMode(true)
            .build();

    Starting starting = new Starting();
    starting.enter(race);

    // Verify power is ON immediately upon entering Starting state
    assertTrue("Main power should be ON if hotStart is enabled", race.isMainPower());

    // Wait for transition to Racing
    long start = System.currentTimeMillis();
    while (!(race.getState() instanceof Racing) && (System.currentTimeMillis() - start) < 2000) {
      Thread.sleep(100);
    }

    assertTrue("Race should be in Racing state", race.getState() instanceof Racing);
    assertTrue("Main power should still be ON in Racing state", race.isMainPower());
  }

  @Test
  public void testFalseStartDuringHotStart() throws Exception {
    // Setup race with hot start and penalties
    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder()
            .from(race.getRaceModel())
            .withHotStart(true)
            .withFalseStartTimePenalty(5.0)
            .build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(race.getTrack())
            .drivers(race.getDrivers())
            .isDemoMode(true)
            .build();

    Starting starting = new Starting();
    starting.enter(race);

    // Verify power is ON
    assertTrue("Main power should be ON if hotStart is enabled", race.isMainPower());

    // Simulate false start on lane 0
    starting.onLap(0, 0.5, 1, false);

    // Verify penalty applied in DriverHeatData
    DriverHeatData dhd = race.getCurrentHeat().getDrivers().get(0);
    assertTrue("False starts should be 1", dhd.getFalseStarts() == 1);
    assertTrue("Time penalty should be 5.0", dhd.getRemainingFalseStartTimePenalty() == 5.0);
  }

  @Test
  public void testFlagTypeDuringStarting() throws Exception {
    Starting starting = new Starting();

    // 1) Initial start: should be RED
    race.setHasRacedInCurrentHeat(false);
    starting.enter(race);
    assertTrue(
        "Flag should be RED for initial start",
        starting.getFlagType(race) == com.antigravity.proto.RaceFlag.RED);
    starting.exit(race);

    // 2) Restart: should be YELLOW
    race.setHasRacedInCurrentHeat(true);
    starting.enter(race);
    assertTrue(
        "Flag should be YELLOW for restart",
        starting.getFlagType(race) == com.antigravity.proto.RaceFlag.YELLOW);
    starting.exit(race);
  }

  @Test
  public void testFalseStartLaneFlag() throws Exception {
    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder().withFalseStartTimePenalty(5.0).build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(race.getTrack())
            .drivers(race.getDrivers())
            .isDemoMode(true)
            .build();

    Starting starting = new Starting();
    starting.enter(race);

    // Initial lane flag should be base flag (RED)
    assertTrue(
        "Initial lane flag should be RED",
        starting.getLaneFlagType(race, 0) == com.antigravity.proto.RaceFlag.RED);

    // Simulate false start
    starting.onLap(0, 0.5, 1, false);

    // Lane flag should now be BLACK
    assertTrue(
        "Lane flag should be BLACK after false start penalty",
        starting.getLaneFlagType(race, 0) == com.antigravity.proto.RaceFlag.BLACK);
  }

  @Test
  public void testFalseStartRecordedAsZeroReactionTime() throws Exception {
    // Setup track with per-lane relays
    com.antigravity.protocols.IProtocol mockProtocol =
        mock(com.antigravity.protocols.IProtocol.class);
    when(mockProtocol.hasPerLaneRelays()).thenReturn(true);
    when(mockProtocol.getNumLanes()).thenReturn(1);

    com.antigravity.protocols.ProtocolDelegate delegate =
        new com.antigravity.protocols.ProtocolDelegate(Collections.singletonList(mockProtocol));

    com.antigravity.models.Race raceModel =
        new com.antigravity.models.Race.Builder().withFalseStartTimePenalty(5.0).build();

    race =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .track(race.getTrack())
            .drivers(race.getDrivers())
            .isDemoMode(true)
            .build();

    // Use reflection or a test-only setter to inject the delegate if needed,
    // but Race constructor actually takes the track and creates the delegate.
    // However, we can use a spy on the race or just use the new helper method we
    // added.
    // Wait, the Race.Builder creates the delegate.

    // Let's use the helper we added to confirm it works with the mock
    // Actually, we can't easily mock the protocols inside Race without more
    // refactoring,
    // but we can test the logic in Starting.onLap by mocking the Race object.

    com.antigravity.race.Race spyRace = spy(race);
    when(spyRace.hasPerLaneRelays()).thenReturn(true);

    Starting starting = new Starting();
    starting.enter(spyRace);

    DriverHeatData dhd = spyRace.getCurrentHeat().getDrivers().get(0);
    assertEquals(-1.0, dhd.getReactionTime(), 0.001);

    // Simulate false start
    starting.onLap(0, 0.5, 1, false);

    // Verify reaction time is 0.0
    assertEquals(0.0, dhd.getReactionTime(), 0.001);
    assertEquals(0.0, dhd.getPendingLapTime(), 0.001);

    // Now test without per-lane relays
    dhd.reset();
    assertEquals(-1.0, dhd.getReactionTime(), 0.001);
    when(spyRace.hasPerLaneRelays()).thenReturn(false);

    starting.onLap(0, 0.5, 1, false);

    // Reaction time should STILL be -1.0, and lapTime should be in pending
    assertEquals(-1.0, dhd.getReactionTime(), 0.001);
    assertEquals(0.5, dhd.getPendingLapTime(), 0.001);
  }
}

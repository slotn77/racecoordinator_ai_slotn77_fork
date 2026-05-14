package com.antigravity.race;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.antigravity.converters.HeatConverter;
import com.antigravity.converters.RaceParticipantConverter;
import com.antigravity.models.Driver;
import com.antigravity.models.HeatRotationType;
import com.antigravity.models.HeatScoring;
import com.antigravity.models.Lane;
import com.antigravity.models.OverallScoring;
import com.antigravity.models.Team;
import com.antigravity.models.Track;
import com.antigravity.proto.ModifyHeatsRequest;
import com.antigravity.proto.ModifyHeatsResponse;
import com.antigravity.protocols.arduino.ArduinoConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

public class RaceModifyHeatsTest {

  private com.antigravity.race.Race testRace;
  private List<RaceParticipant> participants;
  private Track track;
  private com.antigravity.models.Race raceModel;

  @Before
  public void setUp() throws Exception {
    List<ArduinoConfig> mockConfig = Collections.singletonList(mock(ArduinoConfig.class));

    List<Lane> lanes = new ArrayList<>();
    lanes.add(new Lane("red", "black", 100));
    lanes.add(new Lane("blue", "white", 101));

    track = new Track("Test Track", lanes, mockConfig, "track1", new ObjectId());

    HeatScoring mockHeatScoring = mock(HeatScoring.class);
    when(mockHeatScoring.getHeatRanking()).thenReturn(HeatScoring.HeatRanking.LAP_COUNT);
    when(mockHeatScoring.getHeatRankingTiebreaker())
        .thenReturn(HeatScoring.HeatRankingTiebreaker.FASTEST_LAP_TIME);
    when(mockHeatScoring.getFinishMethod()).thenReturn(HeatScoring.FinishMethod.Timed);
    when(mockHeatScoring.getFinishValue()).thenReturn(100L);

    OverallScoring mockOverallScoring = mock(OverallScoring.class);
    when(mockOverallScoring.getRankingMethod()).thenReturn(OverallScoring.OverallRanking.LAP_COUNT);
    when(mockOverallScoring.getTiebreaker())
        .thenReturn(OverallScoring.OverallRankingTiebreaker.FASTEST_LAP_TIME);

    raceModel =
        new com.antigravity.models.Race.Builder()
            .withName("Test Race")
            .withTrackEntityId("track1")
            .withHeatRotationType(HeatRotationType.RoundRobin)
            .withHeatScoring(mockHeatScoring)
            .withOverallScoring(mockOverallScoring)
            .withEntityId("race1")
            .withId(new ObjectId())
            .build();

    participants = new ArrayList<>();
    participants.add(new RaceParticipant(new Driver("Driver 1", "D1", "d1", new ObjectId()), "p1"));
    participants.add(new RaceParticipant(new Driver("Driver 2", "D2", "d2", new ObjectId()), "p2"));
    participants.add(new RaceParticipant(new Driver("Driver 3", "D3", "d3", new ObjectId()), "p3"));

    List<DriverHeatData> heat1Drivers = new ArrayList<>();
    heat1Drivers.add(new DriverHeatData(participants.get(0)));
    heat1Drivers.add(new DriverHeatData(participants.get(1)));
    Heat heat1 = new Heat(1, heat1Drivers, mockHeatScoring);
    heat1.setObjectId("heat1");

    List<DriverHeatData> heat2Drivers = new ArrayList<>();
    heat2Drivers.add(new DriverHeatData(participants.get(2)));
    heat2Drivers.add(new DriverHeatData(new RaceParticipant(Driver.EMPTY_DRIVER)));
    Heat heat2 = new Heat(2, heat2Drivers, mockHeatScoring);
    heat2.setObjectId("heat2");

    List<Heat> heats = new ArrayList<>(Arrays.asList(heat1, heat2));

    testRace =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .drivers(participants)
            .track(track)
            .heats(heats)
            .isDemoMode(true)
            .build();
  }

  @Test
  public void testModifyHeats_ValidChange() {
    // Swap drivers in Heat 1 (which is NOT started)
    Heat heat1 = testRace.getHeats().get(0);
    List<DriverHeatData> drivers = new ArrayList<>();
    drivers.add(new DriverHeatData(participants.get(1))); // p2
    drivers.add(new DriverHeatData(participants.get(0))); // p1

    Heat modifiedHeat1 = new Heat(1, drivers, raceModel.getHeatScoring());
    modifiedHeat1.setObjectId(heat1.getObjectId());

    ModifyHeatsRequest request =
        createRequest(participants, Arrays.asList(modifiedHeat1, testRace.getHeats().get(1)));
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertTrue("Modify heats should succeed: " + response.getErrorMessage(), response.getSuccess());
    assertEquals(
        "Heat 1 driver 0 should be p2",
        "p2",
        testRace.getHeats().get(0).getDrivers().get(0).getDriver().getObjectId());
    assertEquals(
        "Heat 1 driver 1 should be p1",
        "p1",
        testRace.getHeats().get(0).getDrivers().get(1).getDriver().getObjectId());
  }

  @Test
  public void testModifyHeats_DeleteStartedHeat_Fails() {
    // Start Heat 1
    testRace.getHeats().get(0).setStarted(true);

    // Request with ONLY Heat 2 (effectively deleting Heat 1)
    ModifyHeatsRequest request =
        createRequest(participants, Collections.singletonList(testRace.getHeats().get(1)));
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail to delete started heat", response.getSuccess());
    assertTrue(
        response.getErrorMessage().contains("Cannot delete a heat that has already been started"));
  }

  @Test
  public void testModifyHeats_RemoveParticipantFromStartedHeat_Fails() {
    // Start Heat 1 (contains p1 and p2)
    testRace.getHeats().get(0).setStarted(true);

    // Request removing p1 from participants list
    List<RaceParticipant> newParticipants = new ArrayList<>(participants);
    newParticipants.remove(0); // remove p1

    // Update Heat 2 to not use p1 (p1 is in Heat 1)
    ModifyHeatsRequest request = createRequest(newParticipants, testRace.getHeats());
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail to remove participant who has raced", response.getSuccess());
    assertTrue(
        response
            .getErrorMessage()
            .contains("cannot be removed because they have already participated"));
  }

  @Test
  public void testModifyHeats_ChangeDriverInStartedHeat_Fails() {
    // Start Heat 1
    testRace.getHeats().get(0).setStarted(true);

    // Try to swap drivers in Heat 1
    Heat heat1 = testRace.getHeats().get(0);
    List<DriverHeatData> drivers = new ArrayList<>();
    drivers.add(new DriverHeatData(participants.get(1)));
    drivers.add(new DriverHeatData(participants.get(0)));

    Heat modifiedHeat1 = new Heat(1, drivers, raceModel.getHeatScoring());
    modifiedHeat1.setObjectId(heat1.getObjectId());
    modifiedHeat1.setStarted(true);

    ModifyHeatsRequest request =
        createRequest(participants, Arrays.asList(modifiedHeat1, testRace.getHeats().get(1)));
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail to change participants in started heat", response.getSuccess());
    assertTrue(response.getErrorMessage().contains("Cannot change participants in a started heat"));
  }

  @Test
  public void testModifyHeats_ChangeLanesInStartedHeat_Fails() {
    // Start Heat 1
    testRace.getHeats().get(0).setStarted(true);

    // Try to remove a lane from Heat 1 (make it 1 lane instead of 2)
    Heat heat1 = testRace.getHeats().get(0);
    List<DriverHeatData> drivers = new ArrayList<>();
    drivers.add(new DriverHeatData(participants.get(0)));

    Heat modifiedHeat1 = new Heat(1, drivers, raceModel.getHeatScoring());
    modifiedHeat1.setObjectId(heat1.getObjectId());
    modifiedHeat1.setStarted(true);

    ModifyHeatsRequest request =
        createRequest(participants, Arrays.asList(modifiedHeat1, testRace.getHeats().get(1)));
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail to change number of lanes in started heat", response.getSuccess());
    assertTrue(
        response.getErrorMessage().contains("Cannot change number of lanes in a started heat"));
  }

  @Test
  public void testModifyHeats_ChangeEmptyToDriverInStartedHeat_Fails() {
    // Start Heat 2 (contains p3 and EMPTY)
    testRace.getHeats().get(1).setStarted(true);

    // Try to change EMPTY to p1 in Heat 2
    Heat heat2 = testRace.getHeats().get(1);
    List<DriverHeatData> drivers = new ArrayList<>();
    drivers.add(new DriverHeatData(participants.get(2))); // p3
    drivers.add(new DriverHeatData(participants.get(0))); // p1

    Heat modifiedHeat2 = new Heat(2, drivers, raceModel.getHeatScoring());
    modifiedHeat2.setObjectId(heat2.getObjectId());
    modifiedHeat2.setStarted(true);

    ModifyHeatsRequest request =
        createRequest(participants, Arrays.asList(testRace.getHeats().get(0), modifiedHeat2));
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse(
        "Should fail to change empty lane to driver in started heat", response.getSuccess());
    assertTrue(response.getErrorMessage().contains("Cannot change participants in a started heat"));
  }

  @Test
  public void testModifyHeats_AddNewParticipant() {
    // Add a new participant p4
    RaceParticipant p4 =
        new RaceParticipant(new Driver("Driver 4", "D4", "d4", new ObjectId()), "p4");
    List<RaceParticipant> newParticipants = new ArrayList<>(participants);
    newParticipants.add(p4);

    // Replace EMPTY in Heat 2 with p4
    Heat heat2 = testRace.getHeats().get(1);
    List<DriverHeatData> drivers = new ArrayList<>();
    drivers.add(new DriverHeatData(participants.get(2))); // p3
    drivers.add(new DriverHeatData(p4)); // p4

    Heat modifiedHeat2 = new Heat(2, drivers, raceModel.getHeatScoring());
    modifiedHeat2.setObjectId(heat2.getObjectId());

    ModifyHeatsRequest request =
        createRequest(newParticipants, Arrays.asList(testRace.getHeats().get(0), modifiedHeat2));
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertTrue(
        "Adding new participant should succeed: " + response.getErrorMessage(),
        response.getSuccess());
    assertEquals("Total participants should be 4", 4, testRace.getDrivers().size());
    assertEquals(
        "Heat 2 driver 1 should be p4",
        "p4",
        testRace.getHeats().get(1).getDrivers().get(1).getDriver().getObjectId());
  }

  @Test
  public void testModifyHeats_ReorderHeats() {
    // Swap Heat 1 and Heat 2 (unstarted)
    List<Heat> newHeats = new ArrayList<>();
    newHeats.add(testRace.getHeats().get(1));
    newHeats.add(testRace.getHeats().get(0));

    // Update heat numbers to match new order
    newHeats.get(0).setHeatNumber(1);
    newHeats.get(1).setHeatNumber(2);

    ModifyHeatsRequest request = createRequest(participants, newHeats);
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertTrue("Reordering heats should succeed", response.getSuccess());
    assertEquals(
        "Heat 1 should now be the old Heat 2", "heat2", testRace.getHeats().get(0).getObjectId());
    assertEquals(
        "Heat 2 should now be the old Heat 1", "heat1", testRace.getHeats().get(1).getObjectId());
  }

  @Test
  public void testRacingStateStartsHeat() {
    Heat heat = testRace.getHeats().get(0);
    testRace.setCurrentHeat(heat);
    assertFalse("Heat should not be started before Racing state", heat.isStarted());

    testRace.changeState(new com.antigravity.race.states.Racing());
    assertTrue("Heat should be started after entering Racing state", heat.isStarted());
  }

  @Test
  public void testRegenerateHeats_StartedHeat_Fails() {
    // Start a heat
    testRace.getHeats().get(0).setStarted(true);

    com.antigravity.proto.RegenerateHeatsRequest request =
        com.antigravity.proto.RegenerateHeatsRequest.newBuilder().build();
    com.antigravity.proto.RegenerateHeatsResponse response = testRace.regenerateHeats(request);

    assertFalse("Should fail to regenerate if any heat is started", response.getSuccess());
    assertTrue(response.getErrorMessage().contains("One or more heats have already been started"));
  }

  @Test
  public void testModifyHeats_RaceOver_Fails() {
    // Set race to over
    testRace.changeState(new com.antigravity.race.states.RaceOver());

    ModifyHeatsRequest request = createRequest(participants, testRace.getHeats());
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail to modify heats when race is over", response.getSuccess());
    assertTrue(response.getErrorMessage().contains("Cannot modify heats when the race is over"));
  }

  @Test
  public void testRegenerateHeats_RaceOver_Fails() {
    // Set race to over
    testRace.changeState(new com.antigravity.race.states.RaceOver());

    com.antigravity.proto.RegenerateHeatsRequest request =
        com.antigravity.proto.RegenerateHeatsRequest.newBuilder().build();
    com.antigravity.proto.RegenerateHeatsResponse response = testRace.regenerateHeats(request);

    assertFalse("Should fail to regenerate heats when race is over", response.getSuccess());
    assertTrue(
        response.getErrorMessage().contains("Cannot regenerate heats when the race is over"));
  }

  private ModifyHeatsRequest createRequest(List<RaceParticipant> participants, List<Heat> heats) {
    ModifyHeatsRequest.Builder builder = ModifyHeatsRequest.newBuilder();
    for (RaceParticipant p : participants) {
      builder.addParticipants(RaceParticipantConverter.toProto(p, new HashSet<>()));
    }
    for (Heat h : heats) {
      builder.addHeats(HeatConverter.toProto(h, new HashSet<>()));
    }
    return builder.build();
  }

  @Test
  public void testModifyHeats_DuplicateDriverInHeat_Fails() {
    // Assign p1 to both lanes in Heat 1
    Heat heat1 = testRace.getHeats().get(0);
    List<DriverHeatData> drivers = new ArrayList<>();
    drivers.add(new DriverHeatData(participants.get(0))); // p1
    drivers.add(new DriverHeatData(participants.get(0))); // p1 (duplicate!)

    Heat modifiedHeat1 = new Heat(1, drivers, raceModel.getHeatScoring());
    modifiedHeat1.setObjectId(heat1.getObjectId());

    ModifyHeatsRequest request =
        createRequest(participants, Arrays.asList(modifiedHeat1, testRace.getHeats().get(1)));
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail if same driver is in multiple lanes", response.getSuccess());
    assertTrue(response.getErrorMessage().contains("is assigned to multiple lanes in Heat 1"));
  }

  @Test
  public void testModifyHeats_DuplicateParticipant_Fails() {
    // Request with p1 added twice
    List<RaceParticipant> dupeParticipants = new ArrayList<>(participants);
    dupeParticipants.add(participants.get(0)); // p1 again

    ModifyHeatsRequest request = createRequest(dupeParticipants, testRace.getHeats());
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail if duplicate participant is added", response.getSuccess());
    assertTrue(response.getErrorMessage().contains("is added more than once"));
  }

  @Test
  public void testModifyHeats_OverlappingDriverInTeam_Fails() {
    // Create a team containing Driver 1 (p1)
    Team team1 = new Team("Team 1", "url", Collections.singletonList("d1"), "t1", new ObjectId());
    RaceParticipant teamParticipant = new RaceParticipant(team1);
    teamParticipant.setObjectId("pt1");

    // Request with both p1 AND team1 (which contains p1)
    List<RaceParticipant> overlappingParticipants = new ArrayList<>(participants);
    overlappingParticipants.add(teamParticipant);

    ModifyHeatsRequest request = createRequest(overlappingParticipants, testRace.getHeats());
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail if driver is also in a team", response.getSuccess());
    assertTrue(response.getErrorMessage().contains("Overlap detected"));
    assertTrue(
        response.getErrorMessage().contains("Driver in team Team 1 is already a participant"));
  }

  @Test
  public void testModifyHeats_OverlappingTeams_Fails() {
    // Create two teams sharing Driver 1
    Team team1 = new Team("Team 1", "url", Collections.singletonList("d1"), "t1", new ObjectId());
    RaceParticipant pt1 = new RaceParticipant(team1);
    pt1.setObjectId("pt1");

    Team team2 = new Team("Team 2", "url", Collections.singletonList("d1"), "t2", new ObjectId());
    RaceParticipant pt2 = new RaceParticipant(team2);
    pt2.setObjectId("pt2");

    List<RaceParticipant> overlappingParticipants = new ArrayList<>();
    overlappingParticipants.add(pt1);
    overlappingParticipants.add(pt2);

    ModifyHeatsRequest request = createRequest(overlappingParticipants, testRace.getHeats());
    ModifyHeatsResponse response = testRace.modifyHeats(request);

    assertFalse("Should fail if two teams share a driver", response.getSuccess());
    assertTrue(response.getErrorMessage().contains("Overlap detected"));
  }
}

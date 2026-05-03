package com.antigravity.race.states;

import com.antigravity.proto.RaceFlag;
import com.antigravity.protocols.CarData;
import com.antigravity.race.Race;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Paused implements IRaceState {
  private static final Logger logger = LoggerFactory.getLogger(Paused.class);

  @Override
  public RaceFlag getFlagType(Race race) {
    return RaceFlag.YELLOW;
  }

  private Race race;
  private long pauseStartTimeMillis;

  @Override
  public void enter(Race race) {
    this.race = race;
    logger.info("Paused state entered. Race paused.");
    race.setMainPower(false);
    this.pauseStartTimeMillis = System.currentTimeMillis();
  }

  @Override
  public void exit(Race race) {
    long duration = System.currentTimeMillis() - pauseStartTimeMillis;
    race.getStatistics().addPausedTime(duration);
    logger.info("Paused state exited.");
  }

  @Override
  public void nextHeat(Race race) {
    throw new IllegalStateException(
        "Cannot move to next heat from state: " + this.getClass().getSimpleName());
  }

  @Override
  public void start(Race race) {
    logger.info("Paused.start() called. Resuming from Paused state.");
    race.changeState(new Starting());
  }

  @Override
  public void pause(Race race) {
    throw new IllegalStateException("Cannot pause race: Race is already in Paused state.");
  }

  @Override
  public void restartHeat(Race race) {
    logger.info("Paused.restartHeat() called. Resetting current heat.");
    race.resetCurrentHeat();
    race.changeState(new NotStarted());
  }

  @Override
  public void skipHeat(Race race) {
    logger.info("Paused.skipHeat() called. Advancing to HeatOver.");
    race.changeState(new HeatOver());
  }

  @Override
  public void deferHeat(Race race) {
    throw new IllegalStateException(
        "Cannot defer heat from state: " + this.getClass().getSimpleName());
  }

  @Override
  public boolean onLap(int lane, double lapTime, int interfaceId, boolean isDrift) {
    if (race != null && race.getRaceModel() != null) {
      double driftTime = race.getRaceModel().getDriftTime();
      if (driftTime > 0) {
        long elapsedMillis = System.currentTimeMillis() - pauseStartTimeMillis;
        if (elapsedMillis <= driftTime * 1000) {
          logger.info(
              "Paused: Counting lap during drift time. Elapsed: {}ms, Drift: {}ms",
              elapsedMillis,
              (driftTime * 1000));
          return handleLap(race, lane, lapTime, interfaceId, true);
        } else {
          logger.info(
              "Paused: Drift time expired. Lap ignored. Elapsed: {}ms, Drift: {}ms",
              elapsedMillis,
              (driftTime * 1000));
        }
      }
    }
    return false;
  }

  @Override
  public void onSegment(int lane, double segmentTime, int interfaceId) {
    // Not while paused
  }

  @Override
  public void onCarData(CarData carData) {}

  @Override
  public void onCallbutton(Race race, int lane) {
    logger.info("Paused.onCallbutton() called. Resuming race.");
    race.startRace();
  }
}

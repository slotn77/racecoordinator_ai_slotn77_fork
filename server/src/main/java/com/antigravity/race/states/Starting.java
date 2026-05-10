package com.antigravity.race.states;

import com.antigravity.models.Driver;
import com.antigravity.proto.Lap;
import com.antigravity.proto.RaceData;
import com.antigravity.proto.RaceFlag;
import com.antigravity.proto.RaceState;
import com.antigravity.protocols.CarData;
import com.antigravity.race.DriverHeatData;
import com.antigravity.race.Race;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Starting implements IRaceState {
  private static final Logger logger = LoggerFactory.getLogger(Starting.class);

  @Override
  public RaceFlag getFlagType(Race race) {
    if (race != null && race.hasRacedInCurrentHeat()) {
      return RaceFlag.YELLOW;
    }
    return RaceFlag.RED;
  }

  private ScheduledExecutorService scheduler;
  private ScheduledFuture<?> timerHandle;
  private Race race;

  @Override
  public void enter(Race race) {
    this.race = race;
    logger.info("Starting state entered. Countdown initiating.");
    race.broadcastFlag(getFlagType(race));

    if (race.getRaceModel().isHotStart() && !race.hasRacedInCurrentHeat()) {
      logger.info("Hot start enabled. Turning main power ON for countdown.");
      race.setMainPower(true);
    }

    if (!race.hasRacedInCurrentHeat()) {
      race.prepareHeat();
    }
    race.setAutoStartFired(true);

    final double startTimeVal =
        race.hasRacedInCurrentHeat()
            ? race.getRaceModel().getRestartTime()
            : race.getRaceModel().getStartTime();
    final double delayLimitVal =
        race.hasRacedInCurrentHeat()
            ? race.getRaceModel().getRestartDelay()
            : race.getRaceModel().getStartDelay();

    final int randomTicks =
        delayLimitVal > 0 ? new java.util.Random().nextInt((int) (delayLimitVal * 10)) + 1 : 0;

    race.setAutoStartRemaining(startTimeVal);
    logger.debug(
        "Starting state: startTimeVal={}, delayLimitVal={}, randomTicks={}",
        startTimeVal,
        delayLimitVal,
        randomTicks);

    scheduler = Executors.newScheduledThreadPool(1);
    final Runnable ticker =
        new Runnable() {
          int countdown = (int) (startTimeVal * 10);
          int remainingRandomTicks = randomTicks;

          @Override
          public void run() {
            try {
              float displayTime = Math.max(0, countdown) / 10.0f;
              race.setAutoStartRemaining(displayTime);
              race.broadcastTime();
              race.setHeatProgress(0.0);
              race.setRaceState(RaceState.STARTING, getFlagType(race), (double) displayTime);

              if (countdown > 0) {
                countdown--;
              } else if (remainingRandomTicks > 0) {
                remainingRandomTicks--;
              } else {
                race.changeState(new Racing());
              }
            } catch (Exception e) {
              logger.error("Error in Starting timer", e);
            }
          }
        };
    timerHandle = scheduler.scheduleAtFixedRate(ticker, 0, 100, TimeUnit.MILLISECONDS);
  }

  @Override
  public void exit(Race race) {
    if (timerHandle != null) {
      timerHandle.cancel(false);
    }
    if (scheduler != null) {
      scheduler.shutdown();
    }
    race.setAutoStartRemaining(0);
    logger.info("Starting state exited.");
  }

  @Override
  public void nextHeat(Race race) {
    throw new IllegalStateException(
        "Cannot move to next heat from state: " + this.getClass().getSimpleName());
  }

  @Override
  public void start(Race race) {
    throw new IllegalStateException("Cannot start race: Race is already in Starting state.");
  }

  @Override
  public void pause(Race race) {
    logger.info("Starting.pause() called. Cancelling start.");
    race.clearAutoTimers();
    if (race.hasRacedInCurrentHeat()) {
      race.changeState(new Paused());
    } else {
      race.resetRaceTime();
      race.changeState(new NotStarted());
    }
  }

  @Override
  public void restartHeat(Race race) {
    throw new IllegalStateException(
        "Cannot restart heat from state: " + this.getClass().getSimpleName());
  }

  @Override
  public void skipHeat(Race race) {
    throw new IllegalStateException(
        "Cannot skip heat from state: " + this.getClass().getSimpleName());
  }

  @Override
  public void deferHeat(Race race) {
    throw new IllegalStateException(
        "Cannot defer heat from state: " + this.getClass().getSimpleName());
  }

  @Override
  public boolean onLap(int lane, double lapTime, int interfaceId, boolean isDrift) {
    if (race == null || race.getCurrentHeat() == null) {
      return false;
    }

    List<DriverHeatData> drivers = race.getCurrentHeat().getDrivers();
    if (lane < 0 || lane >= drivers.size()) {
      return false;
    }

    DriverHeatData dhd = drivers.get(lane);
    if (dhd == null
        || dhd.getActualDriver() == null
        || dhd.getActualDriver() == Driver.EMPTY_DRIVER) {
      return false;
    }

    logger.info("False start detected on lane {}", lane);
    dhd.incrementFalseStarts();
    dhd.setPenaltyLaps(dhd.getPenaltyLaps() + race.getRaceModel().getFalseStartLapPenalty());
    dhd.setRemainingFalseStartTimePenalty(race.getRaceModel().getFalseStartTimePenalty());
    dhd.addPendingLapTime(lapTime);

    // Broadcast update so UI sees the false start
    Lap lapMsg =
        Lap.newBuilder()
            .setObjectId(dhd.getObjectId())
            .setLapNumber(dhd.getLapCount())
            .setAdjustedLapCount(dhd.getAdjustedLapCount())
            .setDriverId(dhd.getActualDriver() != null ? dhd.getActualDriver().getEntityId() : "")
            .setFuelLevel(dhd.getDriver().getFuelLevel())
            .setType(Lap.LapType.FALSE_START)
            .setFlag(getLaneFlagType(race, lane))
            .build();
    dhd.setFlag(lapMsg.getFlag());
    race.broadcast(RaceData.newBuilder().setLap(lapMsg).build());
    race.updateAndBroadcastOverallStandings();

    if (race.getRaceModel().isRestartOnFalseStart()) {
      logger.info("Restart on false start enabled. Going back to NotStarted.");
      race.changeState(new NotStarted());
    }

    return false;
  }

  @Override
  public void onSegment(int lane, double segmentTime, int interfaceId) {
    // Not while starting
  }

  @Override
  public void onCarData(CarData carData) {
    if (this.race == null) {
      return;
    }

    int lane = carData.getLane();
    // Broadcast the CarData to clients
    com.antigravity.proto.CarData.Builder dataBuilder = // fqn-collision
        com.antigravity.proto.CarData.newBuilder() // fqn-collision
            .setLane(carData.getLane())
            .setControllerThrottlePct(carData.getControllerThrottlePCT())
            .setCarThrottlePct(carData.getCarThrottlePCT())
            .setLocation(carData.getLocation().getValue())
            .setLocationId(carData.getLocationId());

    if (race.getCurrentHeat() != null && race.getCurrentHeat().getDrivers() != null) {
      if (lane >= 0 && lane < race.getCurrentHeat().getDrivers().size()) {
        DriverHeatData driverData = race.getCurrentHeat().getDrivers().get(lane);
        if (driverData != null) {
          driverData.setCurrentLocation(carData.getLocation());
          if (driverData.getDriver() != null) {
            dataBuilder.setFuelLevel(driverData.getDriver().getFuelLevel());
          }
          RaceFlag laneFlag = getLaneFlagType(race, lane);
          driverData.setFlag(laneFlag);
          dataBuilder.setFlag(laneFlag);
        }
      }
    }

    com.antigravity.proto.CarData protoCarData = dataBuilder.build(); // fqn-collision
    RaceData raceDataMsg = RaceData.newBuilder().setCarData(protoCarData).build();

    race.broadcast(raceDataMsg);
  }

  @Override
  public void onCallbutton(Race race, int lane) {
    logger.info("Starting.onCallbutton() called. Pausing race start.");
    pause(race);
  }
}

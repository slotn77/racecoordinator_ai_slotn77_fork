package com.antigravity.race.states;

import com.antigravity.proto.RaceFlag;
import com.antigravity.protocols.CarData;
import com.antigravity.race.DriverHeatData;
import com.antigravity.race.Race;
import java.util.List;

public interface IRaceState {

  RaceFlag getFlagType(Race race);

  default RaceFlag getLaneFlagType(Race race, int lane) {
    RaceFlag baseFlag = getFlagType(race);
    if (race == null || race.getCurrentHeat() == null) return baseFlag;

    List<DriverHeatData> drivers = race.getCurrentHeat().getDrivers();
    if (lane < 0 || lane >= drivers.size()) return baseFlag;

    DriverHeatData dhd = drivers.get(lane);
    if (dhd == null) return baseFlag;

    // 1) Out of fuel
    if (dhd.getDriver() != null && dhd.getDriver().getFuelLevel() <= 0) {
      // Only if fuel is enabled
      if (race.getHeatExecutionManager().isAnalogFuelEnabled()
          || race.getHeatExecutionManager().isDigitalFuelEnabled()) {
        return RaceFlag.BLACK;
      }
    }

    // 2) False start penalty
    if (dhd.getRemainingFalseStartTimePenalty() > 0) {
      return RaceFlag.BLACK;
    }

    return baseFlag;
  }

  void enter(Race race);

  void exit(Race race);

  void start(Race race);

  void pause(Race race);

  void nextHeat(Race race);

  void restartHeat(Race race);

  void skipHeat(Race race);

  void deferHeat(Race race);

  // From the protocol listener
  boolean onLap(int lane, double lapTime, int interfaceId, boolean isDrift);

  default boolean handleLap(Race race, int lane, double lapTime, int interfaceId, boolean isDrift) {
    if (race != null && race.getHeatExecutionManager() != null) {
      return race.getHeatExecutionManager().onLap(lane, lapTime, interfaceId, false, true, isDrift);
    }
    return false;
  }

  void onSegment(int lane, double segmentTime, int interfaceId);

  void onCarData(CarData carData);

  void onCallbutton(Race race, int lane);
}

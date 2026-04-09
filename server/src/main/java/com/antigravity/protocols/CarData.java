package com.antigravity.protocols;

public class CarData {

  private final int lane;
  private final double time;
  private final double controllerThrottlePCT; // [0, 1]
  private final double carThrottlePCT; // [0, 1]
  private final boolean canRefuel;
  private final CarLocation location;
  private final CarLocation lastLocation;
  private final int locationId;

  public CarData(
      int lane,
      double time,
      double controllerThrottlePCT,
      double carThrottlePCT,
      boolean canRefuel,
      CarLocation location,
      CarLocation lastLocation,
      int locationId) {
    this.lane = lane;
    this.time = time;
    this.controllerThrottlePCT = controllerThrottlePCT;
    this.carThrottlePCT = carThrottlePCT;
    this.canRefuel = canRefuel;
    this.location = location;
    this.lastLocation = lastLocation;
    this.locationId = locationId;
  }

  public int getLane() {
    return lane;
  }

  public double getTime() {
    return time;
  }

  public double getControllerThrottlePCT() {
    return controllerThrottlePCT;
  }

  public double getCarThrottlePCT() {
    return carThrottlePCT;
  }

  public boolean getCanRefuel() {
    return canRefuel;
  }

  public CarLocation getLocation() {
    return location;
  }

  public CarLocation getLastLocation() {
    return lastLocation;
  }

  public int getLocationId() {
    return locationId;
  }
}

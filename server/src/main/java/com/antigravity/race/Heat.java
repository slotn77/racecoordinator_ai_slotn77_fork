package com.antigravity.race;

import com.antigravity.models.HeatScoring;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

public class Heat extends ServerToClientObject {

  private int heatNumber;
  private List<DriverHeatData> drivers;
  private RaceHeatStatistics statistics = new RaceHeatStatistics();
  private HeatStandings heatStandings;

  public Heat(int heatNumber, List<DriverHeatData> drivers, HeatScoring scoring) {
    super();
    this.heatNumber = heatNumber;
    this.drivers = drivers;
    HeatScoring safeScoring = scoring != null ? scoring : new HeatScoring();
    this.heatStandings = new HeatStandings(drivers, safeScoring);
  }

  public Heat() {
    super();
  }

  public void initializeStandings(HeatScoring scoring) {
    HeatScoring safeScoring = scoring != null ? scoring : new HeatScoring();
    this.heatStandings = new HeatStandings(this.drivers, safeScoring);
  }

  public int getHeatNumber() {
    return heatNumber;
  }

  public List<DriverHeatData> getDrivers() {
    return drivers;
  }

  @JsonIgnore
  public List<String> getStandings() {
    return heatStandings != null ? heatStandings.getStandings() : new ArrayList<>();
  }

  @JsonIgnore
  public HeatStandings getHeatStandings() {
    return heatStandings;
  }

  public RaceHeatStatistics getStatistics() {
    return statistics;
  }

  public void setStatistics(RaceHeatStatistics statistics) {
    this.statistics = statistics;
  }

  public void setHeatNumber(int heatNumber) {
    this.heatNumber = heatNumber;
  }

  public int getActiveDriverCount() {
    int count = 0;
    for (DriverHeatData driverData : drivers) {
      if (driverData != null
          && driverData.getDriver() != null
          && driverData.getDriver().getDriver() != null
          && driverData.getDriver().getDriver().getEntityId() != null) {
        count++;
      }
    }
    return count;
  }
}

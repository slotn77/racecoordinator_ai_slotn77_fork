package com.antigravity.race;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RaceHeatStatistics {

  private String startTime;
  private String endTime;
  private long startMillis;
  private long durationMillis;

  public RaceHeatStatistics() {}

  @JsonProperty("startTime")
  public String getStartTime() {
    return startTime;
  }

  @JsonProperty("startTime")
  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  @JsonProperty("endTime")
  public String getEndTime() {
    return endTime;
  }

  @JsonProperty("endTime")
  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  @JsonProperty("startMillis")
  public long getStartMillis() {
    return startMillis;
  }

  @JsonProperty("startMillis")
  public void setStartMillis(long startMillis) {
    this.startMillis = startMillis;
  }

  @JsonProperty("durationMillis")
  public long getDurationMillis() {
    return durationMillis;
  }

  @JsonProperty("durationMillis")
  public void setDurationMillis(long durationMillis) {
    this.durationMillis = durationMillis;
  }
}

package com.antigravity.converters;

import com.antigravity.proto.DriverHeatData;
import com.antigravity.proto.DriverModel;
import com.antigravity.proto.Heat;
import com.antigravity.proto.LapData;
import java.util.Set;
import java.util.stream.Collectors;

public class HeatConverter {

  public static final String PARTICIPANT_PREFIX = "Participant_";

  public static Heat toProto(
      com.antigravity.race.Heat heat, Set<String> sentObjectIds) { // fqn-collision
    return Heat.newBuilder()
        .setObjectId(heat.getObjectId())
        .addAllHeatDrivers(
            heat.getDrivers().stream()
                .map(d -> toProto(d, sentObjectIds))
                .collect(Collectors.toList()))
        .setHeatNumber(heat.getHeatNumber())
        .addAllStandings(heat.getStandings())
        .build();
  }

  public static DriverHeatData toProto(
      com.antigravity.race.DriverHeatData data, Set<String> sentObjectIds) { // fqn-collision
    return DriverHeatData.newBuilder()
        .setObjectId(data.getObjectId())
        .setDriver(RaceParticipantConverter.toProto(data.getDriver(), sentObjectIds))
        .setDriverId(
            data.getActualDriver() != null && data.getActualDriver().getEntityId() != null
                ? data.getActualDriver().getEntityId()
                : "")
        .setActualDriver(
            data.getActualDriver() != null
                ? DriverConverter.toProto(data.getActualDriver(), sentObjectIds)
                : DriverModel.getDefaultInstance())
        .setGapLeader(data.getGapLeader())
        .setGapPosition(data.getGapPosition())
        .addAllSegments(data.getSegments())
        .addAllLaps(
            data.getLaps().stream()
                .map(
                    l ->
                        LapData.newBuilder()
                            .setLapTime(l.getLapTime())
                            .setDriverId(l.getDriverId())
                            .setIsDrift(l.isDrift())
                            .build())
                .collect(Collectors.toList()))
        .setPenaltyLaps(data.getPenaltyLaps())
        .setUserLaps(data.getUserLaps())
        .setAutoCalculatedLaps(data.getAutoCalculatedLaps())
        .setAdjustedLapCount(data.getAdjustedLapCount())
        .setAverageLapTime(data.getAverageLapTime())
        .setMedianLapTime(data.getMedianLapTime())
        .setBestLapTime(data.getBestLapTime())
        .setReactionTime(data.getReactionTime())
        .setGapLeader(data.getGapLeader())
        .setGapPosition(data.getGapPosition())
        .setIsRefueling(data.isRefueling())
        .setCurrentLocation(
            data.getCurrentLocation() != null ? data.getCurrentLocation().getValue() : -1)
        .setInitialFuelLevel(data.getInitialFuelLevel())
        .setFalseStarts(data.getFalseStarts())
        .setFlag(data.getFlag())
        .build();
  }
}

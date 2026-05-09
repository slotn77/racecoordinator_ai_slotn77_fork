package com.antigravity.converters;

import com.antigravity.models.Driver;
import com.antigravity.proto.AudioConfig;
import com.antigravity.proto.DriverModel;
import com.antigravity.proto.Model;
import java.util.Set;

public class DriverConverter {

  public static DriverModel toProto(Driver driver, Set<String> sentObjectIds) {
    if (driver == null) {
      return null;
    }
    AudioConfig lapAudio =
        AudioConfig.newBuilder()
            .setType(driver.getLapAudio() != null ? driver.getLapAudio().getType() : "preset")
            .setUrl(
                driver.getLapAudio() != null && driver.getLapAudio().getUrl() != null
                    ? driver.getLapAudio().getUrl()
                    : "")
            .setText(
                driver.getLapAudio() != null && driver.getLapAudio().getText() != null
                    ? driver.getLapAudio().getText()
                    : "")
            .build();

    AudioConfig bestLapAudio =
        AudioConfig.newBuilder()
            .setType(
                driver.getBestLapAudio() != null ? driver.getBestLapAudio().getType() : "preset")
            .setUrl(
                driver.getBestLapAudio() != null && driver.getBestLapAudio().getUrl() != null
                    ? driver.getBestLapAudio().getUrl()
                    : "")
            .setText(
                driver.getBestLapAudio() != null && driver.getBestLapAudio().getText() != null
                    ? driver.getBestLapAudio().getText()
                    : "")
            .build();

    AudioConfig penaltyAudio =
        AudioConfig.newBuilder()
            .setType(
                driver.getPenaltyAudio() != null ? driver.getPenaltyAudio().getType() : "preset")
            .setUrl(
                driver.getPenaltyAudio() != null && driver.getPenaltyAudio().getUrl() != null
                    ? driver.getPenaltyAudio().getUrl()
                    : "")
            .setText(
                driver.getPenaltyAudio() != null && driver.getPenaltyAudio().getText() != null
                    ? driver.getPenaltyAudio().getText()
                    : "")
            .build();

    return DriverModel.newBuilder()
        .setName(driver.getName())
        .setNickname(driver.getNickname() != null ? driver.getNickname() : "")
        .setAvatarUrl(driver.getAvatarUrl() != null ? driver.getAvatarUrl() : "")
        .setLapAudio(lapAudio)
        .setBestLapAudio(bestLapAudio)
        .setPenaltyAudio(penaltyAudio)
        .setModel(
            Model.newBuilder()
                .setEntityId(driver.getEntityId() != null ? driver.getEntityId() : "")
                .build())
        .build();
  }
}

package com.antigravity.converters;

import com.antigravity.models.Lane;
import com.antigravity.proto.LaneModel;
import java.util.Set;

public class LaneConverter {

  public static LaneModel toProto(Lane lane, Set<String> sentObjectIds) {
    String key = "Lane_" + lane.getObjectId();
    if (sentObjectIds.contains(key)) {
      return LaneModel.newBuilder().setObjectId(lane.getObjectId()).build();
    } else {
      sentObjectIds.add(key);
      return LaneModel.newBuilder()
          .setObjectId(lane.getObjectId())
          .setForegroundColor(lane.getForeground_color())
          .setBackgroundColor(lane.getBackground_color())
          .setLength(lane.getLength())
          .build();
    }
  }
}

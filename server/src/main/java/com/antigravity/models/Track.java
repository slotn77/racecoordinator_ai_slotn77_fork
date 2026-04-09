package com.antigravity.models;

import com.antigravity.proto.PinBehavior;
import com.antigravity.protocols.arduino.ArduinoConfig;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Track extends Model {

  private final String name;
  private final List<Lane> lanes;
  private final List<ArduinoConfig> arduinoConfigs;

  @BsonCreator
  @JsonCreator
  public Track(
      @BsonProperty("name") @JsonProperty("name") String name,
      @BsonProperty("lanes") @JsonProperty("lanes") List<Lane> lanes,
      @BsonProperty("arduino_configs") @JsonProperty("arduino_configs")
          List<ArduinoConfig> arduinoConfigs,
      @BsonProperty("entity_id") @JsonProperty("entity_id") String entityId,
      @BsonId @JsonProperty("_id") ObjectId id) {
    super(id, entityId);
    this.name = name;
    this.lanes = lanes != null ? Collections.unmodifiableList(lanes) : Collections.emptyList();
    this.arduinoConfigs =
        arduinoConfigs != null
            ? Collections.unmodifiableList(arduinoConfigs)
            : Collections.emptyList();
  }

  public Track(String name, List<Lane> lanes, String entityId, ObjectId id) {
    this(name, lanes, null, entityId, id);
  }

  // Legacy constructor or convenience
  public Track(String name, List<Lane> lanes) {
    this(name, lanes, null, null, null);
  }

  public String getName() {
    return name;
  }

  @JsonProperty("has_digital_fuel")
  public boolean hasDigitalFuel() {
    int base = PinBehavior.BEHAVIOR_VOLTAGE_LEVEL_BASE.getNumber();
    int max = base + Math.max(1, this.lanes.size());

    for (ArduinoConfig config : this.arduinoConfigs) {
      if (config != null && config.analogIds != null) {
        for (Integer code : config.analogIds) {
          if (code != null && code >= base && code < max) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public List<Lane> getLanes() {
    return lanes;
  }

  @JsonProperty("arduino_configs")
  @BsonProperty("arduino_configs")
  public List<ArduinoConfig> getArduinoConfigs() {
    return arduinoConfigs;
  }
}

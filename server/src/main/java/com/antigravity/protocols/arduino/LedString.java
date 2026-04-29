package com.antigravity.protocols.arduino;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LedString {

  public int pin;
  public List<Integer> leds;
  public int numUsedLeds;
  public int addressableLeds;
  public int brightness;
  public int ledType;
  public int colorOrder;
  public double flagFlashRate;
  public List<String> ledLaneColorOverrides;

  public LedString() {
    this.leds = new ArrayList<>();
    this.ledLaneColorOverrides = new ArrayList<>();
    this.brightness = 32;
    this.ledType = 1;
    this.colorOrder = 0;
    this.flagFlashRate = 2.0;
    this.pin = 0;
  }

  @JsonCreator
  public LedString(
      @JsonProperty("pin") int pin,
      @JsonProperty("leds") List<Integer> leds,
      @JsonProperty("brightness") int brightness,
      @JsonProperty("ledType") int ledType,
      @JsonProperty("colorOrder") int colorOrder,
      @JsonProperty("flagFlashRate") double flagFlashRate,
      @JsonProperty("ledLaneColorOverrides") List<String> ledLaneColorOverrides) {
    this.pin = pin;
    this.leds = leds != null ? leds : new ArrayList<>();
    this.brightness = brightness;
    this.ledType = ledType;
    this.colorOrder = colorOrder;
    this.flagFlashRate = flagFlashRate;
    this.ledLaneColorOverrides =
        ledLaneColorOverrides != null ? ledLaneColorOverrides : new ArrayList<>();

    this.numUsedLeds = 0;
    this.addressableLeds = this.leds.size();
    for (Integer behavior : this.leds) {
      if (behavior != null && behavior != 0) { // 0 = RgbLedBehavior.RGB_LED_BEHAVIOR_UNUSED_VALUE
        this.numUsedLeds++;
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LedString that = (LedString) o;
    return pin == that.pin
        && brightness == that.brightness
        && ledType == that.ledType
        && colorOrder == that.colorOrder
        && Double.compare(that.flagFlashRate, flagFlashRate) == 0
        && Objects.equals(leds, that.leds)
        && Objects.equals(ledLaneColorOverrides, that.ledLaneColorOverrides);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        pin, leds, brightness, ledType, colorOrder, flagFlashRate, ledLaneColorOverrides);
  }
}

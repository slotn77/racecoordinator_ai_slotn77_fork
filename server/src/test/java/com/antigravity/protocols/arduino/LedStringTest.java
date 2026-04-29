package com.antigravity.protocols.arduino;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class LedStringTest {

  @Test
  public void testLedStringInitialization() {
    List<Integer> behaviors =
        Arrays.asList(
            1, // Used
            0, // Unused
            2, // Used
            0 // Unused
            );

    LedString ledString = new LedString(0, behaviors, 255, 1, 0, 5.0, null);

    // After ceb463a, addressableLeds should be the full size of the behaviors list
    assertEquals("Addressable LEDs should be the full list size", 4, ledString.addressableLeds);
    assertEquals("Num used LEDs should count only non-zero behaviors", 2, ledString.numUsedLeds);
  }

  @Test
  public void testEmptyLedString() {
    LedString ledString = new LedString(0, Arrays.asList(), 255, 1, 0, 5.0, null);
    assertEquals(0, ledString.addressableLeds);
    assertEquals(0, ledString.numUsedLeds);
  }
}

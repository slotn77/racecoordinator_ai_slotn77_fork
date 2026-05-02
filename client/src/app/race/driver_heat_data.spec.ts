import { Driver } from "@app/models/driver";

import { DriverHeatData } from "./driver_heat_data";
import { RaceParticipant } from "./race_participant";

describe("DriverHeatData", () => {
  let participant: RaceParticipant;
  let driver: Driver;
  let heatData: DriverHeatData;

  beforeEach(() => {
    driver = new Driver("Test Driver", "TD", "test-driver", "driver-id" as any);
    participant = new RaceParticipant("participant-id", driver);
    heatData = new DriverHeatData("object-id", participant, 0);
  });

  it("should initialize with empty segments", () => {
    expect(heatData.currentLapSegments).toEqual([]);
    expect(heatData.lastSegmentTime).toBe(0);
  });

  it("should add segment times", () => {
    heatData.addSegmentTime(0, 1.234);
    expect(heatData.currentLapSegments).toEqual([1.234]);
    expect(heatData.lastSegmentTime).toBe(1.234);

    heatData.addSegmentTime(1, 2.345);
    expect(heatData.currentLapSegments).toEqual([1.234, 2.345]);
    expect(heatData.lastSegmentTime).toBe(2.345);
  });

  it("should handle non-sequential segment arrivals", () => {
    // Slot index 2 arrives before 0 or 1
    heatData.addSegmentTime(2, 3.456);
    expect(heatData.currentLapSegments[2]).toBe(3.456);
    expect(heatData.currentLapSegments[0]).toBe(0);
    expect(heatData.currentLapSegments[1]).toBe(0);
    expect(heatData.lastSegmentTime).toBe(3.456);

    // Slot 0 arrives later
    heatData.addSegmentTime(0, 1.111);
    expect(heatData.currentLapSegments[0]).toBe(1.111);
    expect(heatData.currentLapSegments[2]).toBe(3.456);
  });

  it("should update existing segment times", () => {
    heatData.addSegmentTime(0, 1.0);
    heatData.addSegmentTime(0, 1.5);
    expect(heatData.currentLapSegments[0]).toBe(1.5);
  });

  it("should clear segments when a lap is added", () => {
    heatData.addSegmentTime(0, 1.234);
    heatData.addSegmentTime(1, 2.345);
    expect(heatData.currentLapSegments.length).toBe(2);

    heatData.addLapTime(1, 10.0, 10.0, 10.0, 10.0, 1);
    expect(heatData.currentLapSegments).toEqual([]);
    expect(heatData.lastSegmentTime).toBe(0);
  });

  it("should reset segments when reset() is called", () => {
    heatData.addSegmentTime(0, 1.234);
    heatData.reset();
    expect(heatData.currentLapSegments).toEqual([]);
  });

  it("should return 0 for lastSegmentTime if no segments added", () => {
    expect(heatData.lastSegmentTime).toBe(0);
  });

  it("should calculate lapCount correctly with penalties, user, and auto laps", () => {
    heatData.addLapTime(1, 10.0, 10.0, 10.0, 10.0, 1); // 1 lap
    expect(heatData.lapCount).toBe(1);

    heatData.penaltyLaps = -0.5;
    heatData.userLaps = 1.0;
    heatData.autoCalculatedLaps = 0.25;

    // Since _adjustedLapCount is 1 (from addLapTime), it returns 1.
    expect(heatData.lapCount).toBe(1);

    // If we update adjustedLapCount directly
    heatData.adjustedLapCount = 1.75;
    expect(heatData.lapCount).toBe(1.75);

    // If adjustedLapCount is 0, it uses the fallback formula
    heatData.adjustedLapCount = 0;
    expect(heatData.lapCount).toBe(1 + -0.5 + 1.0 + 0.25); // 1.75
  });
});

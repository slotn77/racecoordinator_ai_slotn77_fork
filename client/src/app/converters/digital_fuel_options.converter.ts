import { DigitalFuelOptions } from "src/app/models/digital_fuel_options";
import { FuelUsageType } from "src/app/models/fuel_options";
import { com } from "src/app/proto/message";

export class DigitalFuelOptionsConverter {
  static fromProto(
    proto?: com.antigravity.IDigitalFuelOptions | null,
  ): DigitalFuelOptions {
    if (!proto) {
      return new DigitalFuelOptions();
    }

    const p = proto as any;

    let usageType = FuelUsageType.LINEAR;
    if (typeof p.usageType === "number") {
      const types = [
        FuelUsageType.LINEAR,
        FuelUsageType.QUADRATIC,
        FuelUsageType.CUBIC,
      ];
      usageType = types[p.usageType] || FuelUsageType.LINEAR;
    } else if (typeof p.usageType === "string") {
      usageType = p.usageType as FuelUsageType;
    }

    return new DigitalFuelOptions(
      p.enabled ?? p.enabled ?? false,
      p.resetFuelAtHeatStart ?? p.reset_fuel_at_heat_start ?? false,
      p.endHeatOnOutOfFuel ?? p.end_heat_on_out_of_fuel ?? false,
      p.capacity ?? p.capacity ?? 100,
      usageType,
      p.usageRate ?? p.usage_rate ?? 4.0,
      p.startLevel ?? p.start_level ?? 100,
      p.refuelRate ?? p.refuel_rate ?? 10,
      p.pitStopDelay ?? p.pit_stop_delay ?? 2.0,
    );
  }
}

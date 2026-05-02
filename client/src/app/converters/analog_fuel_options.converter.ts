import { AnalogFuelOptions } from "@app/models/analog_fuel_options";
import { FuelUsageType } from "@app/models/fuel_options";
import { IAnalogFuelOptions } from "@app/proto/antigravity";

export class AnalogFuelOptionsConverter {
  static fromProto(proto?: IAnalogFuelOptions | null): AnalogFuelOptions {
    if (!proto) {
      return new AnalogFuelOptions();
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

    return new AnalogFuelOptions(
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

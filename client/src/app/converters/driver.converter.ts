import { Driver, EMPTY_DRIVER_ID } from "@app/models/driver";
import { IDriverModel } from "@app/proto/antigravity";

import { ConverterCache } from "./converter_cache";

export class DriverConverter {
  private static cache = new ConverterCache<Driver>();

  static clearCache() {
    this.cache.clear();
  }

  static get(id: string): Driver | undefined {
    return this.cache.get(id);
  }

  static getEmptyDriver(): Driver {
    return new Driver(
      EMPTY_DRIVER_ID,
      "Empty",
      "",
      undefined,
      { type: "preset" },
      { type: "preset" },
      { type: "preset", url: "/assets/default_penalty_Penalty" },
    );
  }

  static fromProto(proto: IDriverModel): Driver {
    if (!proto) {
      return this.getEmptyDriver();
    }

    const objectId = proto.model?.entityId;

    // Is Reference if name is missing but objectId is present
    const isReference = !proto.name && !!objectId;

    if (isReference) {
      const cached = this.cache.get(objectId);
      if (cached) return cached;
      if (objectId === EMPTY_DRIVER_ID) return this.getEmptyDriver();
    }

    // TODO(aufderheide): Here's a name check validating empty lane.  This isn't the worst
    // because it's looking for an empty string which is not a valid name, but it's not great.
    const finalId = objectId || (proto.name ? "" : EMPTY_DRIVER_ID);
    return this.cache.process(finalId, isReference, () => {
      return new Driver(
        finalId,
        proto.name || (finalId === EMPTY_DRIVER_ID ? "Empty" : "Unknown"),
        proto.nickname || "",
        proto.avatarUrl || undefined,
        {
          type: proto.lapAudio?.type === "tts" ? "tts" : "preset",
          url: proto.lapAudio?.url || undefined,
          text: proto.lapAudio?.text || undefined,
        },
        {
          type: (proto.bestLapAudio?.type as any) || "preset",
          url: proto.bestLapAudio?.url || undefined,
          text: proto.bestLapAudio?.text || undefined,
        },
        {
          type: (proto.penaltyAudio?.type as any) || "preset",
          url: proto.penaltyAudio?.url || undefined,
          text: proto.penaltyAudio?.text || undefined,
        },
      );
    });
  }

  static fromJSON(json: any): Driver {
    const d = new Driver(
      json.entity_id || json.id || "", // Handle typical JSON id fields
      json.name || "",
      json.nickname || "",
      json.avatarUrl,
      json.lapAudio,
      json.bestLapAudio,
      json.penaltyAudio,
    );
    return d;
  }

  static register(driver: Driver): void {
    if (!driver || !driver.entity_id) return;

    const existing = this.cache.get(driver.entity_id);
    if (existing) {
      // Update in place to preserve references
      existing.name = driver.name;
      existing.nickname = driver.nickname;
      existing.avatarUrl = driver.avatarUrl;
      existing.lapAudio = driver.lapAudio;
      existing.bestLapAudio = driver.bestLapAudio;
      existing.penaltyAudio = driver.penaltyAudio;
    } else {
      // Manually populate cache using process to ensure valid state
      // access private cache if possible, or use a workaround.
      // Since `process` is the main entry, we can use it with isRef=false
      this.cache.process(driver.entity_id, false, () => driver);
    }
  }
}

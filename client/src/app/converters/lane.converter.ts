import { Lane } from "@app/models/lane";
import { ILaneModel } from "@app/proto/antigravity";

import { ConverterCache } from "./converter_cache";

export class LaneConverter {
  private static cache = new ConverterCache<Lane>();

  static clearCache() {
    this.cache.clear();
  }

  static fromProto(proto: ILaneModel): Lane {
    const objectId = proto.objectId;
    const isReference =
      !proto.foregroundColor &&
      !proto.backgroundColor &&
      proto.length === undefined;

    return this.cache.process(objectId, isReference, () => {
      return new Lane(
        objectId || "unset",
        proto.foregroundColor || "",
        proto.backgroundColor || "",
        proto.length || 0,
      );
    });
  }
}

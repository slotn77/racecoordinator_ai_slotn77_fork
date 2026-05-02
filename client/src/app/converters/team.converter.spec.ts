import { ITeamModel } from "@app/proto/antigravity";

import { TeamConverter } from "./team.converter";

describe("TeamConverter", () => {
  beforeEach(() => {
    TeamConverter.clearCache();
  });

  it("should convert proto to Team object", () => {
    const proto: ITeamModel = {
      model: { entityId: "t1" },
      name: "Team Alpha",
      avatarUrl: "avatar_url",
      driverIds: ["d1", "d2"],
    };

    const team = TeamConverter.fromProto(proto);
    expect(team.entity_id).toBe("t1");
    expect(team.name).toBe("Team Alpha");
    expect(team.avatarUrl).toBe("avatar_url");
    expect(team.driverIds).toEqual(["d1", "d2"]);
  });

  it("should handle references from cache", () => {
    const proto: ITeamModel = {
      model: { entityId: "t1" },
      name: "Team Alpha",
    };
    TeamConverter.fromProto(proto);

    const refProto: ITeamModel = {
      model: { entityId: "t1" },
      // No name, so it's a reference
    };

    const team = TeamConverter.fromProto(refProto);
    expect(team.name).toBe("Team Alpha");
  });
});

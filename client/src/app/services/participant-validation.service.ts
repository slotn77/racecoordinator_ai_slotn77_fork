import { Injectable } from "@angular/core";
import { Driver } from "@app/models/driver";
import { RaceParticipant } from "@app/models/race_participant";
import { Team } from "@app/models/team";

export interface ValidationResult {
  isValid: boolean;
  errorCode?: "DUPE_INDIVIDUAL_TEAM" | "DUPE_MULTIPLE_TEAMS";
  driverName?: string;
  teamNames?: string[];
}

@Injectable({
  providedIn: "root",
})
export class ParticipantValidationService {
  validate(
    participants: (Driver | Team | RaceParticipant)[],
    allTeams: Team[],
    allDrivers: Driver[],
  ): ValidationResult {
    const individualDriverIds = new Set<string>();
    const driverToTeamNames = new Map<string, string[]>(); // Driver ID -> Team Names
    const EMPTY_LANE_ID = "EMPTY_LANE";

    // 1. Collect all participating driver IDs and their associations
    for (const p of participants) {
      if (this.isDriver(p)) {
        const id = p.entity_id || (p as any).objectId || (p as any).entityId;
        if (id && id !== EMPTY_LANE_ID) {
          if (individualDriverIds.has(id)) {
            return {
              isValid: false,
              errorCode: "DUPE_INDIVIDUAL_TEAM", // Reusing this for individual-individual too
              driverName: p.name,
              teamNames: [],
            };
          }
          individualDriverIds.add(id);
        }
      } else if (this.isTeam(p)) {
        for (const dId of p.driverIds) {
          if (dId && dId !== EMPTY_LANE_ID) {
            if (!driverToTeamNames.has(dId)) {
              driverToTeamNames.set(dId, []);
            }
            driverToTeamNames.get(dId)!.push(p.name);
          }
        }
      } else if (this.isRaceParticipant(p)) {
        if (p.team) {
          // Team participant
          for (const dId of p.team.driverIds) {
            if (dId && dId !== EMPTY_LANE_ID) {
              if (!driverToTeamNames.has(dId)) {
                driverToTeamNames.set(dId, []);
              }
              driverToTeamNames.get(dId)!.push(p.team.name);
            }
          }
        } else {
          // Individual participant
          const dId =
            p.driver.entity_id ||
            p.driver.objectId ||
            (p.driver as any).entityId;
          if (dId && dId !== EMPTY_LANE_ID) {
            if (individualDriverIds.has(dId)) {
              return {
                isValid: false,
                errorCode: "DUPE_INDIVIDUAL_TEAM",
                driverName: p.driver.name,
                teamNames: [],
              };
            }
            individualDriverIds.add(dId);
          }
        }
      }
    }

    // 2. Rule 1: Individual vs Team
    for (const dId of individualDriverIds) {
      if (driverToTeamNames.has(dId)) {
        const driver = allDrivers.find(
          (d) =>
            d.entity_id === dId ||
            d.objectId === dId ||
            (d as any).entityId === dId,
        );
        return {
          isValid: false,
          errorCode: "DUPE_INDIVIDUAL_TEAM",
          driverName: driver ? driver.name : dId,
          teamNames: driverToTeamNames.get(dId),
        };
      }
    }

    // 3. Rule 2: Multiple Teams
    for (const [dId, teamNames] of driverToTeamNames.entries()) {
      if (teamNames.length > 1) {
        const driver = allDrivers.find(
          (d) =>
            d.entity_id === dId ||
            d.objectId === dId ||
            (d as any).entityId === dId,
        );
        return {
          isValid: false,
          errorCode: "DUPE_MULTIPLE_TEAMS",
          driverName: driver ? driver.name : dId,
          teamNames: teamNames,
        };
      }
    }

    return { isValid: true };
  }

  getErrorMessage(result: ValidationResult, translationService: any): string {
    if (result.isValid || !result.errorCode) return "";

    const fixDescription = translationService.translate(
      "RDS_ERR_START_RACE_FIX_DESCRIPTION",
    );
    let messageKey = "";
    let messageParams = {};

    if (result.errorCode === "DUPE_INDIVIDUAL_TEAM") {
      messageKey = "RDS_ERR_DRIVER_DUPE_IND_TEAM";
      messageParams = {
        driver: result.driverName,
        team: result.teamNames?.join(", "),
      };
    } else if (result.errorCode === "DUPE_MULTIPLE_TEAMS") {
      messageKey = "RDS_ERR_DRIVER_DUPE_TEAMS";
      messageParams = {
        driver: result.driverName,
        teams: result.teamNames?.join(", "),
      };
    }

    if (messageKey) {
      const translatedMessage = translationService.translate(
        messageKey,
        messageParams,
      );
      return translatedMessage + "\n\n" + fixDescription;
    }

    return "";
  }

  private isDriver(p: any): p is Driver {
    return p instanceof Driver || (p && "nickname" in p && !("driver" in p));
  }

  private isTeam(p: any): p is Team {
    return p instanceof Team || (p && "driverIds" in p);
  }

  private isRaceParticipant(p: any): p is RaceParticipant {
    return (
      p instanceof RaceParticipant || (p && "driver" in p && "objectId" in p)
    );
  }
}

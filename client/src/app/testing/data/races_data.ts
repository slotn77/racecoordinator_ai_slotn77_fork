import { FuelUsageType } from "../../models/fuel_options";
import { Race } from "../../models/race";
import { Track } from "../../models/track";

export const MOCK_RACES = [
  {
    entity_id: "r1",
    name: "Grand Prix",
    track_entity_id: "t1",
    heat_rotation_type: "RoundRobin",
    heat_scoring: {
      finish_method: "Lap",
      finish_value: 10,
      heat_ranking: "LAP_COUNT",
      heat_ranking_tiebreaker: "FASTEST_LAP_TIME",
      allow_finish: "None",
    },
    overall_scoring: {
      dropped_heats: 0,
      ranking_method: "LAP_COUNT",
      tiebreaker: "FASTEST_LAP_TIME",
    },
    fuel_options: {
      enabled: false,
      capacity: 100,
      usage_type: FuelUsageType.LINEAR,
      usage_rate: 4.0,
      start_level: 100,
      refuel_rate: 10,
      pit_stop_delay: 2.0,
      reference_time: 6.0,
    },
    digital_fuel_options: {
      enabled: false,
      capacity: 100,
      usage_type: FuelUsageType.LINEAR,
      usage_rate: 4.0,
      start_level: 100,
      refuel_rate: 10,
      pit_stop_delay: 2.0,
    },
    team_options: {
      heat_lap_limit: 0,
      heat_time_limit: 0,
      overall_lap_limit: 0,
      overall_time_limit: 0,
      require_pit_stop_change_driver: false,
    },
  },
  {
    entity_id: "r2",
    name: "Endurance Challenge",
    track_entity_id: "t1",
    heat_rotation_type: "RoundRobin",
    heat_scoring: {
      finish_method: "Time",
      finish_value: 300,
      heat_ranking: "LAP_COUNT",
      heat_ranking_tiebreaker: "FASTEST_LAP_TIME",
      allow_finish: "None",
    },
    overall_scoring: {
      dropped_heats: 1,
      ranking_method: "LAP_COUNT",
      tiebreaker: "FASTEST_LAP_TIME",
    },
  },
  {
    entity_id: "r3",
    name: "Digital Sprint",
    track_entity_id: "t1",
    heat_rotation_type: "RoundRobin",
    heat_scoring: {
      finish_method: "Lap",
      finish_value: 5,
      heat_ranking: "LAP_COUNT",
      heat_ranking_tiebreaker: "FASTEST_LAP_TIME",
      allow_finish: "None",
    },
    overall_scoring: {
      dropped_heats: 0,
      ranking_method: "LAP_COUNT",
      tiebreaker: "FASTEST_LAP_TIME",
    },
  },
];

export const MOCK_RACE_INSTANCES = MOCK_RACES.map(
  (r: any) =>
    new Race(
      r.entity_id,
      r.name,
      new Track(r.track_entity_id || "", "", []),
      (r as any).heat_scoring,
      (r as any).overall_scoring,
      (r as any).fuel_options,
      (r as any).digital_fuel_options,
      (r as any).team_options,
    ),
);

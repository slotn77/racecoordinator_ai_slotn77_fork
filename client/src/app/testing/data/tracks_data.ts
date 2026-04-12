import { Lane } from "../../models/lane";
import { Track } from "../../models/track";

export const MOCK_TRACKS = [
  {
    entity_id: "t1",
    name: "Classic Circuit",
    lanes: [
      {
        entity_id: "l1",
        length: 12.5,
        backgroundColor: "#ff0000",
        foregroundColor: "#ffffff",
      },
      {
        entity_id: "l2",
        length: 12.5,
        backgroundColor: "#0000ff",
        foregroundColor: "#ffffff",
      },
    ],
    arduino_configs: [
      {
        name: "Arduino 1",
        commPort: "COM3",
        baudRate: 115200,
        debounceUs: 5000,
        hardwareType: 1, // Mega
        digitalIds: new Array(60).fill(0),
        analogIds: new Array(16).fill(0),
        normallyClosedLaneSensors: false,
        normallyClosedRelays: true,
        globalInvertLights: 0,
        useLapsForPits: 0,
        useLapsForPitEnd: 0,
        usePitsAsLaps: false,
        useLapsForSegments: true,
        ledStrings: null,
        ledLaneColorOverrides: null,
        lapPinPitBehavior: 3,
      },
    ],
  },
  {
    entity_id: "t2",
    name: "Speedway",
    lanes: [
      {
        entity_id: "l1",
        length: 15.0,
        backgroundColor: "#ffff00",
        foregroundColor: "#000000",
      },
      {
        entity_id: "l2",
        length: 15.0,
        backgroundColor: "#00ff00",
        foregroundColor: "#000000",
      },
      {
        entity_id: "l3",
        length: 15.0,
        backgroundColor: "#ff00ff",
        foregroundColor: "#ffffff",
      },
      {
        entity_id: "l4",
        length: 15.0,
        backgroundColor: "#00ffff",
        foregroundColor: "#000000",
      },
    ],
    arduino_configs: [
      {
        name: "Arduino 2",
        commPort: "COM4",
        baudRate: 115200,
        debounceUs: 5000,
        hardwareType: 0, // Uno
        digitalIds: new Array(60).fill(0),
        analogIds: new Array(16).fill(0),
        normallyClosedLaneSensors: false,
        normallyClosedRelays: true,
        globalInvertLights: 0,
        useLapsForPits: 0,
        useLapsForPitEnd: 0,
        usePitsAsLaps: false,
        useLapsForSegments: true,
        ledStrings: null,
        ledLaneColorOverrides: null,
        lapPinPitBehavior: 3,
      },
    ],
  },
];

export const MOCK_TRACK_INSTANCES = MOCK_TRACKS.map(
  (t: any) =>
    new Track(
      t.entity_id,
      t.name,
      t.lanes.map(
        (l: any) =>
          new Lane(l.entity_id, l.length, l.backgroundColor, l.foregroundColor),
      ),
      t.has_digital_fuel || false,
      t.arduino_configs,
    ),
);

export const MOCK_FACTORY_SETTINGS = {
  lanes: [
    { background_color: "#ef4444", foreground_color: "black", length: 10 },
    { background_color: "#ffffff", foreground_color: "black", length: 10 },
    { background_color: "#3b82f6", foreground_color: "black", length: 10 },
    { background_color: "#fbbf24", foreground_color: "black", length: 10 },
  ],
  arduino_configs: [
    {
      name: "Arduino 1",
      commPort: "",
      baudRate: 115200,
      debounceUs: 5000,
      hardwareType: 0,
      digitalIds: new Array(60).fill(0),
      analogIds: new Array(16).fill(0),
      normallyClosedLaneSensors: false,
      normallyClosedRelays: true,
      globalInvertLights: 0,
      useLapsForPits: 0,
      useLapsForPitEnd: 0,
      usePitsAsLaps: false,
      useLapsForSegments: true,
      ledStrings: null,
      ledLaneColorOverrides: null,
      lapPinPitBehavior: 3,
      voltageConfigs: {},
    },
  ],
};

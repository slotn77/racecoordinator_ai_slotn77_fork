export const MOCK_HEATS = [
  {
    heatNumber: 1,
    heatDrivers: [
      {
        objectId: "hd1",
        laneIndex: 0,
        driver: { entity_id: "d1", name: "Alice", nickname: "The Rocket" },
        participant: { fuelLevel: 100 },
      },
      {
        objectId: "hd2",
        laneIndex: 1,
        driver: { entity_id: "d2", name: "Bob", nickname: "Drift King" },
        participant: { fuelLevel: 100 },
      },
    ],
  },
  {
    heatNumber: 2,
    heatDrivers: [
      {
        objectId: "hd3",
        laneIndex: 0,
        driver: { entity_id: "d3", name: "Charlie", nickname: "Speedy" },
        participant: { fuelLevel: 100 },
      },
      {
        objectId: "hd4",
        laneIndex: 1,
        driver: { entity_id: "d4", name: "Dave", nickname: "Noob" },
        participant: { fuelLevel: 100 },
      },
    ],
  },
];

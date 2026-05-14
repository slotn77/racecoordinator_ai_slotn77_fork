export abstract class ModifyHeatsModalHarnessBase {
  static readonly hostSelector = "app-modify-heats-modal";

  static readonly selectors = {
    driverItem: ".driver-item",
    heatCard: ".heat-card",
    lockedOverlay: ".locked-overlay",
    databaseDrivers: "#database-drivers",
    driverPool: "#driver-pool",
    heatGrid: ".heats-grid",
    driverName: ".driver-name",
  };

  abstract getDriverItemCount(): Promise<number>;
  abstract getHeatCardCount(): Promise<number>;
  abstract getLockedOverlayCount(): Promise<number>;
  abstract isDriverVisibleInDatabase(name: string): Promise<boolean>;
  abstract isDriverVisibleInPool(name: string): Promise<boolean>;
}

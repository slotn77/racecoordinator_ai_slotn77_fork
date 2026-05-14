import { Locator } from "@playwright/test";

import { ModifyHeatsModalHarnessBase } from "./modify-heats-modal.harness.base";

export class ModifyHeatsModalHarnessE2e implements ModifyHeatsModalHarnessBase {
  constructor(private locator: Locator) {}

  private get base() {
    return ModifyHeatsModalHarnessBase;
  }

  private get driverItems() {
    return this.locator.locator(this.base.selectors.driverItem);
  }
  private get heatCards() {
    return this.locator.locator(this.base.selectors.heatCard);
  }
  private get lockedOverlays() {
    return this.locator.locator(this.base.selectors.lockedOverlay);
  }
  private get databaseDrivers() {
    return this.locator.locator(this.base.selectors.databaseDrivers);
  }
  private get driverPool() {
    return this.locator.locator(this.base.selectors.driverPool);
  }

  async getDatabaseDriverCount(): Promise<number> {
    return await this.databaseDrivers
      .locator(this.base.selectors.driverItem)
      .count();
  }

  async getDriverItemCount(): Promise<number> {
    return await this.driverItems.count();
  }

  async getHeatCardCount(): Promise<number> {
    return await this.heatCards.count();
  }

  async getLockedOverlayCount(): Promise<number> {
    return await this.lockedOverlays.count();
  }

  async isDriverVisibleInDatabase(name: string): Promise<boolean> {
    const driver = this.databaseDrivers.locator(
      this.base.selectors.driverItem,
      { hasText: name },
    );
    return await driver.isVisible();
  }

  async isDriverVisibleInPool(name: string): Promise<boolean> {
    const driver = this.driverPool.locator(this.base.selectors.driverItem, {
      hasText: name,
    });
    return await driver.isVisible();
  }
}

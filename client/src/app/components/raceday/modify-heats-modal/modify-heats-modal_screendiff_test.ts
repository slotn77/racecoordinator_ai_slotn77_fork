import { expect, test } from "@playwright/test";
import { DefaultRacedayHarnessE2e } from "@app/components/raceday/testing/default-raceday.harness.e2e";
import { RaceState } from "@app/proto/antigravity";
import { TestSetupHelper } from "@app/testing/test-setup_helper";

import { ModifyHeatsModalHarnessE2e } from "./testing/modify-heats-modal.harness.e2e";

test.describe("Modify Heats Modal Visuals", () => {
  test.beforeEach(async ({ page }) => {
    await TestSetupHelper.setupStandardMocks(page);
    await TestSetupHelper.setupRaceWebSocketMocks(page);
    await TestSetupHelper.setupAssetMocks(page);
    await TestSetupHelper.disableAnimations(page);

    await page.setViewportSize({ width: 1600, height: 900 });

    await TestSetupHelper.waitForLocalization(
      page,
      "en",
      page.goto("/default-raceday"),
    );

    await expect(page.locator(".dashboard-wrapper")).toBeVisible();
  });

  test("should show initial state with available and racing drivers, along with a few heats V2", async ({
    page,
  }) => {
    const racedayHarness = new DefaultRacedayHarnessE2e(
      page.locator(".dashboard-wrapper"),
    );

    // Use correct protobuf field names (IRaceData -> IRace -> IRaceModel, ITrackModel, etc.)
    const raceData = {
      race: {
        race: {
          model: { entityId: "r1" },
          name: "Screendiff Race",
          track: {
            model: { entityId: "t1" },
            name: "Test Track",
            lanes: [
              {
                objectId: "l1",
                backgroundColor: "#ff0000",
                foregroundColor: "#ffffff",
                length: 10,
              },
              {
                objectId: "l2",
                backgroundColor: "#00ff00",
                foregroundColor: "#000000",
                length: 10,
              },
              {
                objectId: "l3",
                backgroundColor: "#0000ff",
                foregroundColor: "#ffffff",
                length: 10,
              },
              {
                objectId: "l4",
                backgroundColor: "#ffff00",
                foregroundColor: "#000000",
                length: 10,
              },
            ],
          },
        },
        drivers: [
          {
            objectId: "rp1",
            seed: 1,
            driver: { model: { entityId: "d3" }, name: "Charlie" },
          },
          {
            objectId: "rp2",
            seed: 2,
            driver: { model: { entityId: "d4" }, name: "Dave" },
          },
        ],
        heats: [
          {
            objectId: "h1",
            heatNumber: 1,
            heatDrivers: [
              { objectId: "hd1", driverId: "rp1", driver: { objectId: "rp1" } },
              { objectId: "hd2", driverId: "rp2", driver: { objectId: "rp2" } },
            ],
          },
          {
            objectId: "h2",
            heatNumber: 2,
            heatDrivers: [],
          },
        ],
        currentHeat: { objectId: "h1", heatNumber: 1 },
        state: RaceState.NOT_STARTED,
      },
    };

    // Track API responses for debugging
    const apiResponses: string[] = [];
    page.on("response", async (resp) => {
      const url = resp.url();
      if (url.includes("/api/")) {
        const status = resp.status();
        let body = "";
        try {
          body = (await resp.text()).substring(0, 100);
        } catch {
          body = "BODY_ERROR";
        }
        apiResponses.push(`${url} -> ${status}: ${body}`);
      }
    });

    await TestSetupHelper.mockRaceData(page, raceData);

    await racedayHarness.clickMenuButton("Race Director");
    await racedayHarness.clickMenuItem("Modify Heats");

    const modalHarness = new ModifyHeatsModalHarnessE2e(
      page.locator("app-modify-heats-modal"),
    );
    await expect(page.locator("app-modify-heats-modal")).toBeVisible();

    // Wait for data to settle
    await expect
      .poll(
        async () => {
          const count = await modalHarness.getDatabaseDriverCount();
          const badges = await page.locator(".badge").allTextContents();
          const headers = await page
            .locator(".section-header h3")
            .allTextContents();

          const internalState = await page.evaluate(() => {
            const item = (window as any).tempModifyHeats;
            if (!item) return "NO_COMPONENT_YET";

            return JSON.stringify({
              instanceId: item.instanceId,
              allDrivers: item.allDrivers?.length ?? -1,
              databaseParticipants: item.databaseParticipants?.length ?? -1,
              driverIds: item.allDrivers
                ?.map((d: any) => d.entity_id || d.objectId)
                .slice(0, 5),
              participantIds: item.localParticipants
                ?.map(
                  (p: any) =>
                    p.driver?.entity_id || p.driver?.objectId || p.objectId,
                )
                .slice(0, 5),
              databaseDriverIds: item.databaseDrivers
                ?.map((d: any) => d.entity_id || d.objectId)
                .slice(0, 5),
            });
          });

          console.log(
            `POLL: dbCount=${count}, badges=[${badges}], headers=[${headers}], internal=${internalState}, responses=[${apiResponses.join(" | ")}]`,
          );
          return count > 0;
        },
        { timeout: 15000 },
      )
      .toBeTruthy();

    // Verify Charlie is visible in the racing pool (as a participant)
    await expect
      .poll(async () => await modalHarness.isDriverVisibleInPool("Charlie"), {
        timeout: 15000,
      })
      .toBeTruthy();

    await expect(page).toHaveScreenshot("modify-heats-initial-state.png");
  });

  test("should show when three heats have been started and cannot be edited, along with an editable heat", async ({
    page,
  }) => {
    const racedayHarness = new DefaultRacedayHarnessE2e(
      page.locator(".dashboard-wrapper"),
    );

    const raceData = {
      race: {
        race: {
          model: { entityId: "r1" },
          name: "Started Heats Race",
          track: {
            model: { entityId: "t1" },
            name: "Test Track",
            lanes: [
              {
                objectId: "l1",
                backgroundColor: "#ff0000",
                foregroundColor: "#ffffff",
                length: 10,
              },
              {
                objectId: "l2",
                backgroundColor: "#00ff00",
                foregroundColor: "#000000",
                length: 10,
              },
            ],
          },
        },
        drivers: [
          {
            objectId: "rp1",
            seed: 1,
            driver: { model: { entityId: "d1" }, name: "Alice" },
          },
          {
            objectId: "rp2",
            seed: 2,
            driver: { model: { entityId: "d2" }, name: "Bob" },
          },
        ],
        heats: [
          {
            objectId: "h1",
            heatNumber: 1,
            started: true,
            heatDrivers: [
              { objectId: "hd1", driverId: "rp1", driver: { objectId: "rp1" } },
            ],
          },
          {
            objectId: "h2",
            heatNumber: 2,
            started: true,
            heatDrivers: [
              { objectId: "hd2", driverId: "rp2", driver: { objectId: "rp2" } },
            ],
          },
          {
            objectId: "h3",
            heatNumber: 3,
            heatDrivers: [],
          },
          {
            objectId: "h4",
            heatNumber: 4,
            heatDrivers: [],
          },
        ],
        currentHeat: { objectId: "h3", heatNumber: 3 },
        state: RaceState.PAUSED,
      },
    };

    await TestSetupHelper.mockRaceData(page, raceData);

    await racedayHarness.clickMenuButton("Race Director");
    await racedayHarness.clickMenuItem("Modify Heats");

    const modalHarness = new ModifyHeatsModalHarnessE2e(
      page.locator("app-modify-heats-modal"),
    );
    await expect(page.locator("app-modify-heats-modal")).toBeVisible();

    await expect
      .poll(async () => await modalHarness.getLockedOverlayCount(), {
        timeout: 15000,
      })
      .toBe(3);

    await expect(page).toHaveScreenshot("modify-heats-started-state.png");
  });
});

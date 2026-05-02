import { test } from "@playwright/test";
import { TestSetupHelper } from "@app/testing/test-setup_helper";

import {} from "./testing/input-dialog.harness.e2e";

test.describe("Input Dialog Visuals", () => {
  test.beforeEach(async ({ page }) => {
    // Setup standard mocks
    await TestSetupHelper.setupStandardMocks(page);
    await TestSetupHelper.setupRaceWebSocketMocks(page);
    await TestSetupHelper.setupAssetMocks(page);
  });
});

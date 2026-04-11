import { of, Subject } from "rxjs";

import { Settings } from "../models/settings";

export const mockDataService = {
  listAssets: jasmine.createSpy("listAssets").and.returnValue(of([])),
  deleteAsset: jasmine.createSpy("deleteAsset").and.returnValue(of(true)),
  renameAsset: jasmine.createSpy("renameAsset").and.returnValue(of(true)),
  getDrivers: jasmine.createSpy("getDrivers").and.returnValue(of([])),
  getTeams: jasmine.createSpy("getTeams").and.returnValue(of([])),
  createTeam: jasmine
    .createSpy("createTeam")
    .and.returnValue(of({ entity_id: "new_t" })),
  updateTeam: jasmine
    .createSpy("updateTeam")
    .and.returnValue(of({ entity_id: "t1" })),
  deleteTeam: jasmine.createSpy("deleteTeam").and.returnValue(of(true)),
  uploadAsset: jasmine.createSpy("uploadAsset").and.returnValue(of(true)),
  getCurrentDatabase: jasmine
    .createSpy("getCurrentDatabase")
    .and.returnValue(of({ name: "test_db" })),
  getServerVersion: jasmine
    .createSpy("getServerVersion")
    .and.returnValue(of("1.0.0")),
  connectToRaceDataSocket: jasmine.createSpy("connectToRaceDataSocket"),
  getRaceUpdate: jasmine
    .createSpy("getRaceUpdate")
    .and.returnValue(new Subject().asObservable()),
  getServerAnalyticsConfig: jasmine
    .createSpy("getServerAnalyticsConfig")
    .and.returnValue(of({ measurementId: "G-TEST", clientId: "test-client" })),
  serverUrl: "http://localhost:7070",
};

export const mockTranslationService = {
  translate: jasmine.createSpy("translate").and.callFake((key: string) => key),
  getTranslationsLoaded: jasmine
    .createSpy("getTranslationsLoaded")
    .and.returnValue(of(true)),
};

export const mockRouter = {
  navigate: jasmine.createSpy("navigate"),
  events: new Subject().asObservable(),
  serializeUrl: jasmine.createSpy("serializeUrl").and.returnValue("mock-url"),
  createUrlTree: jasmine.createSpy("createUrlTree").and.returnValue({}),
};

export const mockAnalyticsService = {
  isEnabled: jasmine.createSpy("isEnabled").and.returnValue(true),
  toggleAnalytics: jasmine
    .createSpy("toggleAnalytics")
    .and.returnValue(of({ success: true })),
  initTracking: jasmine.createSpy("initTracking"),
  updateOptOutStatus: jasmine.createSpy("updateOptOutStatus"),
  trackClick: jasmine.createSpy("trackClick"),
  trackPageView: jasmine.createSpy("trackPageView"),
};

export function createTestSettings(
  overrides: Partial<Settings> = {},
): Settings {
  const s = new Settings();
  s.racedaySetupWalkthroughSeen = true;
  s.trackManagerHelpShown = true;
  s.trackEditorHelpShown = true;
  s.driverEditorHelpShown = true;
  s.driverManagerHelpShown = true;
  s.teamManagerHelpShown = true;
  s.teamEditorHelpShown = true;
  s.assetManagerHelpShown = true;
  s.raceManagerHelpShown = true;
  s.raceEditorHelpShown = true;
  return Object.assign(s, overrides);
}

export const mockSettingsService = {
  getSettings: jasmine
    .createSpy("getSettings")
    .and.callFake(() => createTestSettings()),
  saveSettings: jasmine.createSpy("saveSettings"),
};

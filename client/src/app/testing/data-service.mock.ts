import { of } from "rxjs";
import { InitializeRaceResponse } from "@app/proto/antigravity";

export class DataServiceMock {
  getDrivers = jasmine.createSpy("getDrivers").and.returnValue(
    of([
      { entity_id: "d1", name: "Alice", nickname: "The Rocket" },
      { entity_id: "d2", name: "Bob", nickname: "Drift King" },
    ]),
  );

  getRaces = jasmine.createSpy("getRaces").and.returnValue(
    of([
      { entity_id: "r1", name: "Grand Prix" },
      { entity_id: "r2", name: "Time Trial" },
    ]),
  );

  getTracks = jasmine.createSpy("getTracks").and.returnValue(of([]));

  initializeRace = jasmine
    .createSpy("initializeRace")
    .and.returnValue(of(InitializeRaceResponse.create({ success: true })));

  getCurrentDatabase = jasmine
    .createSpy("getCurrentDatabase")
    .and.returnValue(of({ name: "test_db" }));
}

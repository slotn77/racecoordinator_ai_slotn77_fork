import { TestBed } from "@angular/core/testing";
import { BehaviorSubject } from "rxjs";
import { RaceFlag } from "@app/proto/antigravity";

import { RaceConnectionService } from "./race-connection.service";
import { RaceFlagService } from "./race-flag.service";

describe("RaceFlagService", () => {
  let service: RaceFlagService;
  let raceFlagSubject: BehaviorSubject<RaceFlag>;

  beforeEach(() => {
    raceFlagSubject = new BehaviorSubject<RaceFlag>(RaceFlag.RED);

    const raceConnectionSpy = jasmine.createSpyObj(
      "RaceConnectionService",
      [],
      {
        raceFlag$: raceFlagSubject.asObservable(),
      },
    );

    TestBed.configureTestingModule({
      providers: [
        RaceFlagService,
        { provide: RaceConnectionService, useValue: raceConnectionSpy },
      ],
    });
    service = TestBed.inject(RaceFlagService);
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should return RED flag type and color initially", () => {
    expect(service.getFlagType()).toBe("red");
    expect(service.getFlagColor()).toBe("red");
  });

  it("should update flag type and color when RaceConnectionService emits", () => {
    raceFlagSubject.next(RaceFlag.GREEN);
    expect(service.getFlagType()).toBe("green");
    expect(service.getFlagColor()).toBe("green");

    raceFlagSubject.next(RaceFlag.YELLOW);
    expect(service.getFlagType()).toBe("yellow");
    expect(service.getFlagColor()).toBe("yellow");

    raceFlagSubject.next(RaceFlag.WHITE);
    expect(service.getFlagType()).toBe("white");
    expect(service.getFlagColor()).toBe("white");

    raceFlagSubject.next(RaceFlag.CHECKERED);
    expect(service.getFlagType()).toBe("checkered");
    expect(service.getFlagColor()).toBe("checkered");

    raceFlagSubject.next(RaceFlag.GREEN_YELLOW);
    expect(service.getFlagType()).toBe("green_yellow");
    expect(service.getFlagColor()).toBe("green");
  });

  it("should return translatable flag names", () => {
    raceFlagSubject.next(RaceFlag.RED);
    expect(service.getFlagNameKey()).toBe("RACE_FLAG_RED");

    raceFlagSubject.next(RaceFlag.GREEN);
    expect(service.getFlagNameKey()).toBe("RACE_FLAG_GREEN");
  });
});

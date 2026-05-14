import { DragDropModule } from "@angular/cdk/drag-drop";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { ActivatedRoute, Router } from "@angular/router";
import { of } from "rxjs";
import { DataService } from "@app/data.service";
import { Driver } from "@app/models/driver";
import { Race } from "@app/models/race";
import { RaceParticipant } from "@app/models/race_participant";
import { Team } from "@app/models/team";
import { Track } from "@app/models/track";
import { AvatarUrlPipe } from "@app/pipes/avatar-url.pipe";
import { TranslatePipe } from "@app/pipes/translate.pipe";
import { RaceState } from "@app/proto/antigravity";
import { DriverHeatData } from "@app/race/driver_heat_data";
import { Heat } from "@app/race/heat";
import { ParticipantValidationService } from "@app/services/participant-validation.service";
import { TranslationService } from "@app/services/translation.service";

import { ModifyHeatsModalComponent } from "./modify-heats-modal.component";

describe("ModifyHeatsModalComponent", () => {
  let component: ModifyHeatsModalComponent;
  let fixture: ComponentFixture<ModifyHeatsModalComponent>;
  let mockDataService: any;
  let mockTranslationService: any;
  let mockValidationService: any;

  beforeEach(async () => {
    mockDataService = jasmine.createSpyObj("DataService", [
      "getDrivers",
      "getTeams",
      "regenerateHeats",
      "modifyHeats",
    ]);
    mockDataService.getDrivers.and.returnValue(of([]));
    mockDataService.getTeams.and.returnValue(of([]));
    mockDataService.regenerateHeats.and.returnValue(
      of({ success: true, heats: [] }),
    );
    mockDataService.modifyHeats.and.returnValue(of({ success: true }));

    mockTranslationService = jasmine.createSpyObj("TranslationService", [
      "translate",
    ]);
    mockTranslationService.translate.and.callFake((key: string) => key);

    mockValidationService = jasmine.createSpyObj(
      "ParticipantValidationService",
      ["validate", "getErrorMessage"],
    );

    const mockActivatedRoute = {
      queryParams: of({}),
      snapshot: {
        queryParamMap: {
          get: jasmine.createSpy("get").and.returnValue(null),
        },
      },
    };

    const mockRouter = {
      navigate: jasmine.createSpy("navigate"),
      events: of(),
    };

    await TestBed.configureTestingModule({
      imports: [
        ModifyHeatsModalComponent,
        DragDropModule,
        TranslatePipe,
        AvatarUrlPipe,
        NoopAnimationsModule,
      ],
      providers: [
        { provide: DataService, useValue: mockDataService },
        { provide: TranslationService, useValue: mockTranslationService },
        {
          provide: ParticipantValidationService,
          useValue: mockValidationService,
        },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: mockRouter },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ModifyHeatsModalComponent);
    component = fixture.componentInstance;

    // Provide required inputs to prevent NG0950 errors
    const track = createMockTrack();
    fixture.componentRef.setInput("track", track);
    fixture.componentRef.setInput("race", createMockRace(track));
  });

  const createMockRace = (track: Track) => {
    return new Race("race-1", "Test Race", track);
  };

  const createMockTrack = () => {
    return new Track("track-1", "Test Track", 100, [
      {
        lane_index: 0,
        background_color: "red",
        foreground_color: "white",
      } as any,
      {
        lane_index: 1,
        background_color: "blue",
        foreground_color: "white",
      } as any,
    ]);
  };

  it("should show all participants in RACING pool even if they are assigned to a heat", () => {
    const driver = new Driver("d1", "Austin", "Austin");
    const participant = new RaceParticipant(
      "rp1",
      driver,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      100,
    );
    const heat = new Heat("h1", 1, [
      new DriverHeatData("dhd1", participant, 0),
    ]);

    const track = createMockTrack();
    fixture.componentRef.setInput("race", createMockRace(track));
    fixture.componentRef.setInput("track", track);
    fixture.componentRef.setInput("participants", [participant]);
    fixture.componentRef.setInput("heats", [heat]);
    fixture.componentRef.setInput("raceState", RaceState.NOT_STARTED);

    fixture.detectChanges();

    // Verify driverPool contains the participant despite being in heat 1
    expect(component["driverPool"].length).toBe(1);
    expect(component["driverPool"][0].objectId).toBe("rp1");
  });

  it("should show team participants in RACING pool even if they are assigned to a heat", () => {
    const team = new Team("t1", "The Girls", undefined, ["d1", "d2"]);
    const participant = new RaceParticipant(
      "rp-team",
      new Driver("EMPTY_LANE", "Empty", "Empty"),
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      100,
      team,
    );
    const heat = new Heat("h1", 1, [
      new DriverHeatData("dhd-team", participant, 0),
    ]);

    const track = createMockTrack();
    fixture.componentRef.setInput("race", createMockRace(track));
    fixture.componentRef.setInput("track", track);
    fixture.componentRef.setInput("participants", [participant]);
    fixture.componentRef.setInput("heats", [heat]);

    fixture.detectChanges();

    expect(component["driverPool"].length).toBe(1);
    expect(component["driverPool"][0].team?.name).toBe("The Girls");
  });

  it("should exclude empty lane placeholders from the RACING pool", () => {
    const placeholder = new RaceParticipant(
      "rp-empty",
      new Driver("EMPTY_LANE", "Empty", "Empty"),
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      100,
    );

    const track = createMockTrack();
    fixture.componentRef.setInput("race", createMockRace(track));
    fixture.componentRef.setInput("track", track);
    fixture.componentRef.setInput("participants", [placeholder]);
    fixture.componentRef.setInput("heats", []);

    fixture.detectChanges();

    expect(component["driverPool"].length).toBe(0);
  });

  describe("database participant filtering", () => {
    let d1: Driver, d2: Driver, t1: Team, t2: Team;

    beforeEach(() => {
      d1 = new Driver("d1", "Driver 1", "D1");
      d2 = new Driver("d2", "Driver 2", "D2");
      t1 = new Team("t1", "Team 1", undefined, ["d1"]);
      t2 = new Team("t2", "Team 2", undefined, ["d2"]);

      component["allDrivers"] = [d1, d2];
      component["allTeams"] = [t1, t2];
    });

    it("should filter out drivers already in the race", () => {
      const p1 = new RaceParticipant("rp1", d1, 0, 0, 0, 0, 0, 0, 0, 0, 100);
      component["localParticipants"] = [p1];

      component["updateDatabaseParticipants"]();

      expect(component["databaseDrivers"]).not.toContain(d1);
      expect(component["databaseDrivers"]).toContain(d2);
    });

    it("should filter out teams already in the race", () => {
      const p1 = new RaceParticipant(
        "rp-t1",
        new Driver("EMPTY_LANE", "Empty", "Empty"),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        t1,
      );
      component["localParticipants"] = [p1];

      component["updateDatabaseParticipants"]();

      expect(component["databaseTeams"]).not.toContain(t1);
      expect(component["databaseTeams"]).toContain(t2);
    });

    it("should filter out drivers who are in teams already in the race", () => {
      // t1 contains d1
      const p1 = new RaceParticipant(
        "rp-t1",
        new Driver("EMPTY_LANE", "Empty", "Empty"),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        t1,
      );
      component["localParticipants"] = [p1];

      component["updateDatabaseParticipants"]();

      expect(component["databaseDrivers"]).not.toContain(d1);
    });

    it("should filter out teams that contain drivers already in the race", () => {
      // t1 contains d1
      const p1 = new RaceParticipant("rp1", d1, 0, 0, 0, 0, 0, 0, 0, 0, 100);
      component["localParticipants"] = [p1];

      component["updateDatabaseParticipants"]();

      expect(component["databaseTeams"]).not.toContain(t1);
    });
  });

  describe("heat locking logic", () => {
    it("should consider heat started if heat.started is true", () => {
      const heat = new Heat("h1", 1, [], [], true);
      expect(component["isHeatStarted"](heat)).toBeTrue();
    });

    it("should consider heat started if heat number is less than current heat number", () => {
      fixture.componentRef.setInput("currentHeatNumber", 2);
      const heat = new Heat("h1", 1, [], [], false);
      expect(component["isHeatStarted"](heat)).toBeTrue();
    });

    it("should consider heat started if race state is RACE_OVER", () => {
      fixture.componentRef.setInput("raceState", RaceState.RACE_OVER);
      const heat = new Heat("h1", 1, [], [], false);
      expect(component["isHeatStarted"](heat)).toBeTrue();
    });

    it("should consider current heat started if race is in active state", () => {
      fixture.componentRef.setInput("currentHeatNumber", 1);
      fixture.componentRef.setInput("raceState", RaceState.RACING);
      const heat = new Heat("h1", 1, [], [], false);
      expect(component["isHeatStarted"](heat)).toBeTrue();
    });

    it("should NOT consider current heat started if race is NOT_STARTED", () => {
      fixture.componentRef.setInput("currentHeatNumber", 1);
      fixture.componentRef.setInput("raceState", RaceState.NOT_STARTED);
      const heat = new Heat("h1", 1, [], [], false);
      expect(component["isHeatStarted"](heat)).toBeFalse();
    });
  });

  describe("actions", () => {
    it("should add a new heat when onAddHeat is called", () => {
      const initialHeatsCount = component["localHeats"].length;
      component["onAddHeat"]();
      expect(component["localHeats"].length).toBe(initialHeatsCount + 1);
      expect(component["localHeats"][initialHeatsCount].heatNumber).toBe(
        initialHeatsCount + 1,
      );
    });

    it("should remove a heat when onRemoveHeat is called on an unstarted heat", () => {
      const heat1 = new Heat("h1", 1, [], [], false);
      const heat2 = new Heat("h2", 2, [], [], false);
      component["localHeats"] = [heat1, heat2];

      component["onRemoveHeat"](0);

      expect(component["localHeats"].length).toBe(1);
      expect(component["localHeats"][0].objectId).toBe("h2");
      expect(component["localHeats"][0].heatNumber).toBe(1); // Renumbered
    });

    it("should NOT remove a heat if it is started", () => {
      const heat1 = new Heat("h1", 1, [], [], true);
      component["localHeats"] = [heat1];

      component["onRemoveHeat"](0);

      expect(component["localHeats"].length).toBe(1);
    });
  });

  describe("validation", () => {
    it("should return error if a started heat was modified", () => {
      const p1 = new RaceParticipant(
        "p1",
        new Driver("d1", "D1", "D1"),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
      );
      const p2 = new RaceParticipant(
        "p2",
        new Driver("d2", "D2", "D2"),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
      );
      const originalHeat = new Heat(
        "h1",
        1,
        [new DriverHeatData("dhd1", p1, 0)],
        [],
        true,
      );

      fixture.componentRef.setInput("heats", [originalHeat]);
      component["localHeats"] = [
        new Heat("h1", 1, [new DriverHeatData("dhd1", p2, 0)], [], true),
      ];

      const error = component["getValidationError"]();
      expect(error).toBe("RD_ERR_STARTED_HEAT_MODIFIED");
    });

    it("should return error if a participant from a started heat was removed", () => {
      const p1 = new RaceParticipant(
        "p1",
        new Driver("d1", "D1", "D1"),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
      );
      const originalHeat = new Heat(
        "h1",
        1,
        [new DriverHeatData("dhd1", p1, 0)],
        [],
        true,
      );

      fixture.componentRef.setInput("heats", [originalHeat]);
      fixture.componentRef.setInput("participants", [p1]);
      component["localParticipants"] = []; // p1 removed

      const error = component["getValidationError"]();
      expect(error).toBe("RD_ERR_STARTED_PARTICIPANT_REMOVED");
    });

    it("should return null if no validation errors", () => {
      const p1 = new RaceParticipant(
        "p1",
        new Driver("d1", "D1", "D1"),
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
      );
      const originalHeat = new Heat(
        "h1",
        1,
        [new DriverHeatData("dhd1", p1, 0)],
        [],
        false,
      ); // not started

      fixture.componentRef.setInput("heats", [originalHeat]);
      fixture.componentRef.setInput("participants", [p1]);
      component["localHeats"] = [new Heat("h1", 1, [], [], false)]; // modified but unstarted
      component["localParticipants"] = [p1];

      const error = component["getValidationError"]();
      expect(error).toBeNull();
    });
  });
});

import { TestBed } from "@angular/core/testing";
import { ReportingService } from "@app/services/reporting.service";

import { GuideStep, HelpService } from "./help.service";

describe("HelpService", () => {
  let service: HelpService;
  let ReportingServiceSpy: jasmine.SpyObj<ReportingService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj("ReportingService", ["trackClick"]);

    TestBed.configureTestingModule({
      providers: [HelpService, { provide: ReportingService, useValue: spy }],
    });
    service = TestBed.inject(HelpService);
    ReportingServiceSpy = TestBed.inject(
      ReportingService,
    ) as jasmine.SpyObj<ReportingService>;
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should track help_started when guide starts", () => {
    const steps: GuideStep[] = [
      { title: "Step 1", content: "Content 1" },
      { title: "Step 2", content: "Content 2" },
    ];
    service.startGuide(steps);
    expect(ReportingServiceSpy.trackClick).toHaveBeenCalledWith(
      "help_started",
      { guide_name: "Step 1" },
    );
  });

  it("should track help_ended_early with step details when ended before the last step", () => {
    const steps: GuideStep[] = [
      { title: "Step 1", content: "Content 1" },
      { title: "Step 2", content: "Content 2" },
    ];
    service.startGuide(steps);
    ReportingServiceSpy.trackClick.calls.reset();

    service.endGuide();
    expect(ReportingServiceSpy.trackClick).toHaveBeenCalledWith(
      "help_ended_early",
      {
        guide_name: "Step 1",
        step_index: 0,
        step_title: "Step 1",
      },
    );
  });

  it("should track help_completed when ended on the last step", () => {
    const steps: GuideStep[] = [
      { title: "Step 1", content: "Content 1" },
      { title: "Step 2", content: "Content 2" },
    ];
    service.startGuide(steps);
    service.nextStep(); // moves to Step 2, index 1 (the last step)
    ReportingServiceSpy.trackClick.calls.reset();

    service.endGuide();
    expect(ReportingServiceSpy.trackClick).toHaveBeenCalledWith(
      "help_completed",
      { guide_name: "Step 1" },
    );
  });

  it("should automatically complete when nextStep is called on the last step", () => {
    const steps: GuideStep[] = [
      { title: "Step 1", content: "Content 1" },
      { title: "Step 2", content: "Content 2" },
    ];
    service.startGuide(steps);
    service.nextStep(); // Move to Step 2
    ReportingServiceSpy.trackClick.calls.reset();

    service.nextStep(); // Calling next on the last step ends the guide
    expect(ReportingServiceSpy.trackClick).toHaveBeenCalledWith(
      "help_completed",
      { guide_name: "Step 1" },
    );
  });
});

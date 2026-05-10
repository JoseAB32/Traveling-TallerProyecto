import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ModifyItineraryComponent } from './modify-itinerary.component';

describe('ModifyItineraryComponent', () => {
  let component: ModifyItineraryComponent;
  let fixture: ComponentFixture<ModifyItineraryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ModifyItineraryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ModifyItineraryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

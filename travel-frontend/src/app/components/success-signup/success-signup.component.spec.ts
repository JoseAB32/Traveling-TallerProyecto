import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SuccessSignupComponent } from './success-signup.component';

describe('SuccessSignupComponent', () => {
  let component: SuccessSignupComponent;
  let fixture: ComponentFixture<SuccessSignupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SuccessSignupComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SuccessSignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

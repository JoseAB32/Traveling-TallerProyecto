import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InicioLogueadoComponent } from './inicio-logueado.component';

describe('InicioLogueadoComponent', () => {
  let component: InicioLogueadoComponent;
  let fixture: ComponentFixture<InicioLogueadoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InicioLogueadoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InicioLogueadoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

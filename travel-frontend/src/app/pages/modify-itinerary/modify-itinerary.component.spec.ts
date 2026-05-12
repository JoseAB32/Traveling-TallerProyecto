/// <reference types="jest" />

import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { ModifyItineraryComponent } from './modify-itinerary.component';
import { ItineraryService } from '../../services/itinerary/itinerary.service';
import { PlaceService } from '../../services/place/place.service';
import { RoutingService } from '../../services/routing/routing.service';
import { TranslocoService } from '@jsverse/transloco';
import { Place } from '../../models/place/place';

describe('ModifyItineraryComponent', () => {
  let fixture: ComponentFixture<ModifyItineraryComponent>;
  let component: ModifyItineraryComponent;

  let routerEvents$: Subject<unknown>;

  let itineraryServiceMock: {
    getItineraryById: jest.Mock;
    getMyDraft: jest.Mock;
    updateItinerary: jest.Mock;
  };

  let placeServiceMock: {
    getPlacesByDepartment: jest.Mock;
  };

  let routingServiceMock: {
    getRoute: jest.Mock;
  };

  let routerMock: {
    events: Subject<unknown>;
    navigate: jest.Mock;
  };

  let translocoServiceMock: {
    translate: jest.Mock;
  };

  let routeId: string | null;

  const city = {
    id: 1,
    name: 'Cochabamba',
    state: true
  };

  const placeOne = {
    id: 1,
    name: 'Cristo de la Concordia',
    latitude: -17.384,
    longitude: -66.156,
    start_date: '08:00',
    end_date: '10:00',
    city
  } as unknown as Place;

  const placeTwo = {
    id: 2,
    name: 'Laguna Alalay',
    latitude: -17.402,
    longitude: -66.145,
    start_date: '11:00',
    end_date: '13:00',
    city
  } as unknown as Place;

  const otherCityPlace = {
    id: 3,
    name: 'Valle de la Luna',
    latitude: -16.567,
    longitude: -68.093,
    start_date: '14:00',
    end_date: '16:00',
    city: {
      id: 2,
      name: 'La Paz',
      state: true
    }
  } as unknown as Place;

  const itineraryResponse = {
    tripId: 10,
    userId: 1,
    name: 'Itinerario Cochabamba',
    startDate: '2026-05-12',
    endDate: '2026-05-13',
    places: [placeOne, placeTwo]
  };

  function createComponent(): void {
    fixture = TestBed.createComponent(ModifyItineraryComponent);
    component = fixture.componentInstance;
  }

  beforeEach(async () => {
    routeId = '10';
    routerEvents$ = new Subject<unknown>();

    itineraryServiceMock = {
      getItineraryById: jest.fn().mockReturnValue(of(itineraryResponse)),
      getMyDraft: jest.fn().mockReturnValue(of(itineraryResponse)),
      updateItinerary: jest.fn().mockReturnValue(of(itineraryResponse))
    };

    placeServiceMock = {
      getPlacesByDepartment: jest.fn().mockReturnValue(of([placeOne, placeTwo]))
    };

    routingServiceMock = {
      getRoute: jest.fn().mockReturnValue(
        of({
          distanceKm: 2,
          durationMinutes: 10,
          coordinates: [
            [-17.384, -66.156],
            [-17.402, -66.145]
          ]
        })
      )
    };

    routerMock = {
      events: routerEvents$,
      navigate: jest.fn()
    };

    translocoServiceMock = {
      translate: jest.fn().mockReturnValue('El nombre es obligatorio')
    };

    await TestBed.configureTestingModule({
      imports: [ModifyItineraryComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: jest.fn(() => routeId)
              }
            }
          }
        },
        {
          provide: Router,
          useValue: routerMock
        },
        {
          provide: ItineraryService,
          useValue: itineraryServiceMock
        },
        {
          provide: PlaceService,
          useValue: placeServiceMock
        },
        {
          provide: RoutingService,
          useValue: routingServiceMock
        },
        {
          provide: TranslocoService,
          useValue: translocoServiceMock
        }
      ]
    })
      .overrideComponent(ModifyItineraryComponent, {
        set: {
          template: '',
          imports: []
        }
      })
      .compileComponents();

    sessionStorage.clear();
    jest.spyOn(console, 'error').mockImplementation(() => {});
    jest.spyOn(console, 'warn').mockImplementation(() => {});
  });

  afterEach(() => {
    sessionStorage.clear();
    jest.restoreAllMocks();
  });

  it('should create the component', () => {
    createComponent();

    expect(component).toBeTruthy();
  });

  it('should initialize with route id and load itinerary when no stored UI state exists', () => {
    createComponent();

    component.ngOnInit();

    expect(component.tripId).toBe(10);
    expect(itineraryServiceMock.getItineraryById).toHaveBeenCalledWith(10);
  });

  it('should not load itinerary when route id does not exist', () => {
    routeId = null;

    createComponent();

    component.ngOnInit();

    expect(console.error).toHaveBeenCalledWith('No se encontró el id en la URL');
    expect(itineraryServiceMock.getItineraryById).not.toHaveBeenCalled();
  });

  it('should set invalid itinerary id message when tripId is invalid', () => {
    createComponent();

    component.tripId = 0;

    (component as any).loadItineraryById();

    expect(component.errorMessage).toBe('ID de itinerario inválido');
    expect(itineraryServiceMock.getItineraryById).not.toHaveBeenCalled();
  });

  // it('should load itinerary by id and fill component state', async () => {
  //   createComponent();

  //   component.tripId = 10;

  //   (component as any).loadItineraryById();

  //   await Promise.resolve();

  //   expect(component.itinerary).toEqual(itineraryResponse);
  //   expect(component.nameItinerary).toBe('Itinerario Cochabamba');
  //   expect(component.selectedPlaces).toEqual([placeOne, placeTwo]);
  //   expect(component.startDate).toBe('2026-05-12');
  //   expect(component.endDate).toBe('2026-05-13');
  //   expect(component.generatedItinerary).toEqual([placeOne, placeTwo]);
  //   expect(component.cityId).toBe(1);
  //   expect(component.isLoading).toBe(false);
  // });

  it('should set unauthorized message when loading itinerary returns 401', () => {
    itineraryServiceMock.getItineraryById.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 401 }))
    );

    createComponent();

    component.tripId = 10;

    (component as any).loadItineraryById();

    expect(component.errorMessage).toBe('No autorizado. Inicia sesión nuevamente.');
    expect(component.isLoading).toBe(false);
  });

  it('should set not found message when loading itinerary returns 404', () => {
    itineraryServiceMock.getItineraryById.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 404 }))
    );

    createComponent();

    component.tripId = 10;

    (component as any).loadItineraryById();

    expect(component.errorMessage).toBe('No se encontró el itinerario solicitado.');
  });

  it('should set generic error message when loading itinerary fails with other status', () => {
    itineraryServiceMock.getItineraryById.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 500 }))
    );

    createComponent();

    component.tripId = 10;

    (component as any).loadItineraryById();

    expect(component.errorMessage).toBe('No se pudo recuperar el itinerario.');
    expect(console.error).toHaveBeenCalled();
  });

  it('should load draft and fill selected places and dates', () => {
    createComponent();

    component.loadDraft();

    expect(component.selectedPlaces).toEqual([placeOne, placeTwo]);
    expect(component.startDate).toBe('2026-05-12');
    expect(component.endDate).toBe('2026-05-13');
    expect(component.hasPendingChanges).toBe(false);
    expect(component.saveMessage).toBe('Borrador cargado');
  });

  it('should do nothing when draft response is null', () => {
    itineraryServiceMock.getMyDraft.mockReturnValue(of(null));

    createComponent();

    component.loadDraft();

    expect(component.selectedPlaces).toEqual([]);
    expect(component.saveMessage).toBe('Sin cambios por guardar');
  });

  it('should set expired session message when loadDraft returns 403', () => {
    itineraryServiceMock.getMyDraft.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 403 }))
    );

    createComponent();

    component.loadDraft();

    expect(component.saveMessage).toBe('Tu sesion expiro. Vuelve a iniciar sesion.');
  });

  it('should set no draft message when loadDraft returns another error', () => {
    itineraryServiceMock.getMyDraft.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 404 }))
    );

    createComponent();

    component.loadDraft();

    expect(component.saveMessage).toBe('No existe borrador previo');
  });

  it('should not load places when cityId is null', () => {
    createComponent();

    component.cityId = null;

    component.loadPlacesByCity();

    expect(console.warn).toHaveBeenCalledWith(
      'No se puede cargar lugares porque cityId es null'
    );
    expect(placeServiceMock.getPlacesByDepartment).not.toHaveBeenCalled();
  });

  it('should load places by city', () => {
    createComponent();

    component.cityId = 1;

    component.loadPlacesByCity();

    expect(placeServiceMock.getPlacesByDepartment).toHaveBeenCalledWith(1);
    expect(component.cityPlaces).toEqual([placeOne, placeTwo]);
    expect(component.isLoadingPlaces).toBe(false);
  });

  it('should clear places when loadPlacesByCity fails', () => {
    placeServiceMock.getPlacesByDepartment.mockReturnValue(
      throwError(() => new Error('Error'))
    );

    createComponent();

    component.cityId = 1;
    component.selectedPlace = placeOne;

    component.loadPlacesByCity();

    expect(component.cityPlaces).toEqual([]);
    expect(component.selectedPlace).toBeNull();
    expect(component.isLoadingPlaces).toBe(false);
  });

  it('should set selected place when map place is clicked', () => {
    createComponent();

    component.onMapPlaceClick(placeOne);

    expect(component.selectedPlace).toEqual(placeOne);
  });

  it('should not add selected place when selectedPlace is null', () => {
    createComponent();

    component.selectedPlace = null;

    component.addSelectedPlace();

    expect(component.selectedPlaces).toEqual([]);
  });

  it('should add selected place and persist UI state', () => {
    createComponent();

    component.selectedPlace = placeOne;

    component.addSelectedPlace();

    expect(component.selectedPlaces).toEqual([placeOne]);
    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toBeTruthy();
  });

  it('should not add duplicated selected place', () => {
    createComponent();

    component.selectedPlaces = [placeOne];
    component.selectedPlace = placeOne;

    component.addSelectedPlace();

    expect(component.selectedPlaces.length).toBe(1);
  });

  it('should not add place from different city', () => {
    createComponent();

    component.selectedPlaces = [placeOne];
    component.selectedPlace = otherCityPlace;

    component.addSelectedPlace();

    expect(component.selectedPlaces).toEqual([placeOne]);
    expect(component.saveMessage).toBe('No puedes seleccionar lugares de diferentes ciudades');
  });

  it('should remove selected place and persist UI state', () => {
    createComponent();

    component.selectedPlaces = [placeOne, placeTwo];
    component.generatedItinerary = [placeOne, placeTwo];
    component.generatedRouteCoordinates = [[-17.384, -66.156]];

    component.removeSelectedPlace(1);

    expect(component.selectedPlaces).toEqual([placeTwo]);
    expect(component.generatedItinerary).toEqual([]);
    expect(component.generatedRouteCoordinates).toEqual([]);
    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toBeTruthy();
  });

  it('should navigate to place detail preserving modify itinerary return params', () => {
    createComponent();

    component.tripId = 10;

    component.goToPlaceDetail(1);

    expect(routerMock.navigate).toHaveBeenCalledWith(['/place', 1], {
      queryParams: {
        returnTo: 'modify-itinerary',
        itineraryId: 10
      }
    });

    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toBeTruthy();
  });

  it('should persist UI state when date changes', () => {
    createComponent();

    component.startDate = '2026-05-12';

    component.onDateChange();

    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toContain('2026-05-12');
  });

  it('should persist UI state when name changes', () => {
    createComponent();

    component.nameItinerary = 'Nuevo itinerario';

    component.onNameChange();

    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toContain('Nuevo itinerario');
  });

  it('should restore UI state from sessionStorage', () => {
    sessionStorage.setItem(
      'UpdateitineraryDraftUiState',
      JSON.stringify({
        name: 'Recuperado',
        cityId: 1,
        selectedPlaces: [placeOne],
        startDate: '2026-05-12',
        endDate: '2026-05-13',
        hasPendingChanges: true,
        saveMessage: 'Borrador recuperado',
        generatedItinerary: [placeOne],
        generatedRouteCoordinates: [[-17.384, -66.156]]
      })
    );

    createComponent();

    const result = (component as any).restoreUiState();

    expect(result).toBe(true);
    expect(component.nameItinerary).toBe('Recuperado');
    expect(component.cityId).toBe(1);
    expect(component.selectedPlaces).toEqual([placeOne]);
    expect(component.startDate).toBe('2026-05-12');
    expect(component.endDate).toBe('2026-05-13');
    expect(component.hasPendingChanges).toBe(true);
    expect(component.generatedItinerary).toEqual([placeOne]);
  });

  it('should return false when there is no UI state to restore', () => {
    createComponent();

    const result = (component as any).restoreUiState();

    expect(result).toBe(false);
  });

  it('should remove invalid UI state and return false', () => {
    sessionStorage.setItem('UpdateitineraryDraftUiState', 'invalid-json');

    createComponent();

    const result = (component as any).restoreUiState();

    expect(result).toBe(false);
    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toBeNull();
  });

  it('should not generate itinerary with less than two places', async () => {
    createComponent();

    component.selectedPlaces = [placeOne];

    await component.generateItinerary();

    expect(component.saveMessage).toBe(
      'Selecciona al menos dos lugares para generar el itinerario'
    );
  });

  it('should not generate itinerary without dates', async () => {
    createComponent();

    component.selectedPlaces = [placeOne, placeTwo];

    await component.generateItinerary();

    expect(component.saveMessage).toBe('Selecciona fecha de inicio y fin');
  });

  it('should not generate itinerary when start date is greater than end date', async () => {
    createComponent();

    component.selectedPlaces = [placeOne, placeTwo];
    component.startDate = '2026-05-20';
    component.endDate = '2026-05-10';

    await component.generateItinerary();

    expect(component.saveMessage).toBe('La fecha de inicio no puede ser mayor a la fecha fin');
  });

  it('should not generate itinerary when a place has invalid coordinates', async () => {
    const invalidPlace = {
      ...placeTwo,
      latitude: Number.NaN
    } as unknown as Place;

    createComponent();

    component.selectedPlaces = [placeOne, invalidPlace];
    component.startDate = '2026-05-10';
    component.endDate = '2026-05-20';

    await component.generateItinerary();

    expect(component.saveMessage).toBe(
      'Algunos lugares no tienen coordenadas u horarios válidos'
    );
  });

  it('should not generate itinerary when a place has invalid schedule', async () => {
    const invalidPlace = {
      ...placeTwo,
      start_date: null
    } as unknown as Place;

    createComponent();

    component.selectedPlaces = [placeOne, invalidPlace];
    component.startDate = '2026-05-10';
    component.endDate = '2026-05-20';

    await component.generateItinerary();

    expect(component.saveMessage).toBe(
      'Algunos lugares no tienen coordenadas u horarios válidos'
    );
  });

  it('should generate itinerary correctly', async () => {
    createComponent();

    component.selectedPlaces = [placeOne, placeTwo];
    component.startDate = '2026-05-10';
    component.endDate = '2026-05-20';

    await component.generateItinerary();

    expect(component.generatedItinerary.length).toBe(2);
    expect(component.generatedRouteCoordinates.length).toBe(2);
    expect(component.saveMessage).toBe('Itinerario generado correctamente');
    expect(component.isGeneratingItinerary).toBe(false);
  });

  it('should handle error while generating itinerary', async () => {
    routingServiceMock.getRoute.mockReturnValue(
      throwError(() => new Error('Routing error'))
    );

    createComponent();

    component.selectedPlaces = [placeOne, placeTwo];
    component.startDate = '2026-05-10';
    component.endDate = '2026-05-20';

    await component.generateItinerary();

    expect(component.saveMessage).toBe('No se pudo generar el itinerario con rutas reales');
    expect(component.isGeneratingItinerary).toBe(false);
    expect(console.error).toHaveBeenCalled();
  });

  it('should return empty ordered itinerary when places array is empty', async () => {
    createComponent();

    const result = await (component as any).orderPlacesByScheduleAndRealDistance([]);

    expect(result).toEqual([]);
  });

  it('should save itinerary using generated itinerary when available', () => {
    createComponent();

    component.tripId = 10;
    component.nameItinerary = 'Itinerario generado';
    component.startDate = '2026-05-10';
    component.endDate = '2026-05-20';
    component.selectedPlaces = [placeOne];
    component.generatedItinerary = [placeTwo];

    component.saveItinerary();

    expect(itineraryServiceMock.updateItinerary).toHaveBeenCalledWith(10, {
      name: 'Itinerario generado',
      startDate: '2026-05-10',
      endDate: '2026-05-20',
      placeIds: [2]
    });

    expect(sessionStorage.getItem('justSaved')).toBe('true');
    expect(routerMock.navigate).toHaveBeenCalledWith(['/my-itineraries']);
    expect(component.saveMessage).toBe('Itinerario actualizado correctamente');
  });

  it('should not save itinerary with invalid trip id', () => {
    createComponent();

    component.tripId = 0;

    component.saveItinerary();

    expect(component.saveMessage).toBe('ID de itinerario inválido');
    expect(itineraryServiceMock.updateItinerary).not.toHaveBeenCalled();
  });

  it('should not save itinerary without name', () => {
    createComponent();

    component.tripId = 10;
    component.nameItinerary = '   ';

    component.saveItinerary();

    expect(component.itineraryNameError).toBe('El nombre es obligatorio');
    expect(translocoServiceMock.translate).toHaveBeenCalledWith(
      'createItinerary.textErrorRequiredName'
    );
    expect(itineraryServiceMock.updateItinerary).not.toHaveBeenCalled();
  });

  it('should not save itinerary without selected places', () => {
    createComponent();

    component.tripId = 10;
    component.nameItinerary = 'Itinerario vacío';
    component.selectedPlaces = [];
    component.generatedItinerary = [];

    component.saveItinerary();

    expect(component.saveMessage).toBe('Debes seleccionar al menos un lugar');
    expect(itineraryServiceMock.updateItinerary).not.toHaveBeenCalled();
  });

  it('should set unauthorized message when save returns 401', () => {
    itineraryServiceMock.updateItinerary.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 401 }))
    );

    createComponent();

    component.tripId = 10;
    component.nameItinerary = 'Itinerario';
    component.selectedPlaces = [placeOne];

    component.saveItinerary();

    expect(component.saveMessage).toBe('No autorizado. Inicia sesión nuevamente.');
    expect(component.isSavingDraft).toBe(false);
  });

  it('should set not found message when save returns 404', () => {
    itineraryServiceMock.updateItinerary.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 404 }))
    );

    createComponent();

    component.tripId = 10;
    component.nameItinerary = 'Itinerario';
    component.selectedPlaces = [placeOne];

    component.saveItinerary();

    expect(component.saveMessage).toBe('No se encontró el itinerario a modificar.');
  });

  it('should set generic save error message when save fails with another status', () => {
    itineraryServiceMock.updateItinerary.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 500 }))
    );

    createComponent();

    component.tripId = 10;
    component.nameItinerary = 'Itinerario';
    component.selectedPlaces = [placeOne];

    component.saveItinerary();

    expect(component.saveMessage).toBe('No se pudo actualizar el itinerario');
    expect(console.error).toHaveBeenCalled();
  });

  it('should remove UI state when navigating away from place detail return flow', () => {
    createComponent();

    component.tripId = 10;
    sessionStorage.setItem('UpdateitineraryDraftUiState', 'saved');

    (component as any).listenRouteChanges();

    routerEvents$.next(new NavigationStart(1, '/my-itineraries'));

    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toBeNull();
  });

  it('should keep UI state when navigating to place detail from modify itinerary', () => {
    createComponent();

    component.tripId = 10;
    sessionStorage.setItem('UpdateitineraryDraftUiState', 'saved');

    (component as any).listenRouteChanges();

    routerEvents$.next(
      new NavigationStart(
        1,
        '/place/1?returnTo=modify-itinerary&itineraryId=10'
      )
    );

    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toBe('saved');
  });

  it('should ignore router events that are not NavigationStart', () => {
    createComponent();

    component.tripId = 10;
    sessionStorage.setItem('UpdateitineraryDraftUiState', 'saved');

    (component as any).listenRouteChanges();

    routerEvents$.next({ type: 'OtherEvent' });

    expect(sessionStorage.getItem('UpdateitineraryDraftUiState')).toBe('saved');
  });

  it('should unsubscribe on destroy', () => {
    createComponent();

    const unsubscribeSpy = jest.fn();

    (component as any).routerSubscription = {
      unsubscribe: unsubscribeSpy
    };

    component.ngOnDestroy();

    expect(unsubscribeSpy).toHaveBeenCalled();
  });
});
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoTestingModule } from '@jsverse/transloco';
import { BehaviorSubject } from 'rxjs';

import { PlaceDetailComponent } from './place-detail.component';
import { PlaceService } from '../../services/place/place.service';
import { AuthService } from '../../services/auth/auth.service';
import { FavoriteService } from '../../services/favorite/favorite.service';
import { ReviewService } from '../../services/review/review.service';
import { FeatureService } from '../../services/features/feature.service';

describe('PlaceDetailComponent', () => {
  let component: PlaceDetailComponent;
  let fixture: ComponentFixture<PlaceDetailComponent>;

  const placeServiceMock = {
    getPlaceById: jasmine.createSpy('getPlaceById'),
    getPlaces: jasmine.createSpy('getPlaces')
  };

  const currentUserSubject = new BehaviorSubject<any>({ id: 1, userName: 'peter', correo: 'p@t.com' });

  const authServiceMock = {
    getCurrentUser: jasmine.createSpy('getCurrentUser'),
    isAuthenticated: jasmine.createSpy('isAuthenticated'),
    isAdmin: jasmine.createSpy('isAdmin'),
    logout: jasmine.createSpy('logout'),
    currentUser$: currentUserSubject.asObservable()
  };

  const favoriteServiceMock = {
    getUserFavorites: jasmine.createSpy('getUserFavorites'),
    addFavorite: jasmine.createSpy('addFavorite'),
    removeFavorite: jasmine.createSpy('removeFavorite')
  };

  const reviewServiceMock = {
    getPlaceReviews: jasmine.createSpy('getPlaceReviews'),
    getReviewReplies: jasmine.createSpy('getReviewReplies'),
    createReview: jasmine.createSpy('createReview')
  };

  const featureServiceMock = {
    isEnabled: jasmine.createSpy('isEnabled').and.returnValue(true)
  };

  beforeEach(async () => {
    placeServiceMock.getPlaceById.and.returnValue(of({
      id: 5,
      name: 'Lugar Test',
      description: 'Descripción',
      address: 'Dirección',
      rating: 4,
      price: 10,
      latitude: 0,
      longitude: 0,
      place_type: 'Natural',
      city: null,
      city_id: 1,
      is_event: false,
      start_date: null,
      end_date: null,
      imageUrl: 'https://image.test/lugar.jpg',
      state: true
    }));

    authServiceMock.getCurrentUser.and.returnValue({ id: 1, userName: 'peter', correo: 'p@t.com' });
    authServiceMock.isAuthenticated.and.returnValue(true);
    authServiceMock.isAdmin.and.returnValue(false);
    favoriteServiceMock.getUserFavorites.and.returnValue(of([]));
    placeServiceMock.getPlaces.and.returnValue(of([]));
    reviewServiceMock.getPlaceReviews.and.returnValue(of({
      content: [{
        id: 10,
        comment: 'Reseña inicial',
        score: 4,
        createdAt: '2026-05-08T16:00:00Z',
        state: true,
        user: {
          id: 2,
          userName: 'erika',
          correo: 'e@e.com',
          pass: '',
          birthday: '',
          city_id: null,
          state: true
        },
        place: {
          id: 5,
          name: '',
          description: '',
          address: '',
          rating: 0,
          price: 0,
          latitude: 0,
          longitude: 0,
          place_type: '',
          city: null,
          city_id: 0,
          is_event: false,
          start_date: null,
          end_date: null,
          imageUrl: '',
          state: true
        }
      }],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      hasNext: false
    }));
    reviewServiceMock.getReviewReplies.and.returnValue(of({
      content: [],
      page: 0,
      size: 2,
      totalElements: 0,
      totalPages: 0,
      hasNext: false
    }));

    await TestBed.configureTestingModule({
      imports: [
        PlaceDetailComponent,
        TranslocoTestingModule.forRoot({
          langs: { es: {} },
          translocoConfig: {
            availableLangs: ['es'],
            defaultLang: 'es'
          }
        })
      ],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: (_: string) => null
              },
              paramMap: {
                get: (key: string) => (key === 'id' ? '5' : null)
              }
            }
          }
        },
        { provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate']) },
        { provide: PlaceService, useValue: placeServiceMock },
        { provide: AuthService, useValue: authServiceMock },
        { provide: FavoriteService, useValue: favoriteServiceMock },
        { provide: ReviewService, useValue: reviewServiceMock },
        { provide: FeatureService, useValue: featureServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PlaceDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load reviews when place detail loads', () => {
    expect(reviewServiceMock.getPlaceReviews).toHaveBeenCalledWith(5, 0, 10);
    expect(reviewServiceMock.getReviewReplies).toHaveBeenCalledWith(10, 0, 2);
    expect(component.reviews.length).toBe(1);
  });

  it('should open composer and publish reply', () => {
    component.toggleReplyComposer(10);
    component.getRepliesState(10).replyComment = 'Respuesta directa';

    reviewServiceMock.createReview.and.returnValue(of({
      id: 200,
      comment: 'Respuesta directa',
      createdAt: '2026-05-10T08:00:00Z',
      state: true,
      user: {
        id: 1,
        userName: 'peter',
        correo: 'p@t.com',
        pass: '',
        birthday: '',
        city_id: null,
        state: true
      },
      place: {
        id: 5,
        name: '',
        description: '',
        address: '',
        rating: 0,
        price: 0,
        latitude: 0,
        longitude: 0,
        place_type: '',
        city: null,
        city_id: 0,
        is_event: false,
        start_date: null,
        end_date: null,
        imageUrl: '',
        state: true
      }
    }));

    component.publishReply(component.reviews[0]);

    expect(reviewServiceMock.createReview).toHaveBeenCalled();
    expect(component.visibleReplies(10)[0].id).toBe(200);
    expect(component.getRepliesState(10).composerOpen).toBeFalse();
  });

  it('should disable publishing when comment or score are missing', () => {
    component.reviewComment = '';
    component.reviewScore = 0;

    expect(component.canPublishReview).toBeFalse();
  });

  it('should prepend created review to list', () => {
    component.reviewComment = 'Nueva reseña';
    component.reviewScore = 5;

    reviewServiceMock.createReview.and.returnValue(of({
      id: 99,
      comment: 'Nueva reseña',
      score: 5,
      createdAt: '2026-05-09T10:00:00Z',
      state: true,
      user: {
        id: 1,
        userName: 'peter',
        correo: 'p@t.com',
        pass: '',
        birthday: '',
        city_id: null,
        state: true
      },
      place: {
        id: 5,
        name: '',
        description: '',
        address: '',
        rating: 0,
        price: 0,
        latitude: 0,
        longitude: 0,
        place_type: '',
        city: null,
        city_id: 0,
        is_event: false,
        start_date: null,
        end_date: null,
        imageUrl: '',
        state: true
      }
    }));

    component.publishReview();

    expect(component.reviews[0].id).toBe(99);
    expect(component.reviewComment).toBe('');
    expect(component.reviewScore).toBe(0);
  });

  it('should set error message when publish fails', () => {
    component.reviewComment = 'Falla';
    component.reviewScore = 3;

    reviewServiceMock.createReview.and.returnValue(throwError(() => ({ error: { message: 'Error API' } })));

    component.publishReview();

    expect(component.reviewError).toBe('Error API');
    expect(component.publishingReview).toBeFalse();
  });
});

/// <reference types="jest" />

import { of, throwError } from 'rxjs';

import { PlaceDetailComponent } from './place-detail.component';
import { Place } from '../../models/place/place';
import { Review } from '../../models/review/review';

import { TestBed } from '@angular/core/testing';
import { FeatureService } from '../../services/features/feature.service';

describe('PlaceDetailComponent', () => {
  let component: PlaceDetailComponent;

  let routeMock: any;
  let routerMock: any;
  let placeServiceMock: any;
  let authServiceMock: any;
  let favoriteServiceMock: any;
  let reviewServiceMock: any;
  let translocoServiceMock: any;
  let featureServiceMock: any;

  const createPlace = (): Place => {
    const place = new Place();

    place.id = 1;
    place.name = 'Cristo de la Concordia';
    place.description = 'Lugar turístico representativo de Cochabamba';
    place.address = 'Av. de la Concordia';
    place.rating = 4.8;
    place.price = 15;
    place.latitude = -17.384;
    place.longitude = -66.156;
    place.place_type = 'Monumento';
    place.city_id = 1;
    place.images = [
      {
        id: 1,
        image_url: 'https://res.cloudinary.com/test/image/upload/cristo1.jpg',
        public_id: 'cristo1',
        alt_text: 'Cristo de la Concordia',
        display_order: 1,
        is_main: true
      },
      {
        id: 2,
        image_url: 'https://res.cloudinary.com/test/image/upload/cristo2.jpg',
        public_id: 'cristo2',
        alt_text: 'Vista del Cristo',
        display_order: 2,
        is_main: false
      }
    ];
    place.state = true;

    return place;
  };

  const createReview = (id: number, comment: string, score = 5): Review => {
    return {
      id,
      comment,
      score,
      createdAt: '2026-05-20T10:00:00',
      userName: 'Usuario Test'
    } as unknown as Review;
  };

  const createPaginatedReviewsResponse = (content: Review[] = []) => {
    return {
      content,
      page: 0,
      size: 10,
      totalElements: content.length,
      totalPages: content.length > 0 ? 1 : 0,
      hasNext: false
    };
  };

  const createRepliesResponse = (content: Review[] = []) => {
    return {
      content,
      page: 0,
      size: 2,
      totalElements: content.length,
      totalPages: content.length > 0 ? 1 : 0,
      hasNext: false
    };
  };

    beforeEach(() => {
    routeMock = {
        snapshot: {
        paramMap: {
            get: jest.fn((key: string) => {
            if (key === 'id') {
                return '1';
            }

            return null;
            })
        },
        queryParamMap: {
            get: jest.fn(() => null)
        }
        }
    };

    routerMock = {
        navigate: jest.fn()
    };

    placeServiceMock = {
        getPlaceById: jest.fn()
    };

    authServiceMock = {
        getCurrentUser: jest.fn(),
        isAuthenticated: jest.fn()
    };

    favoriteServiceMock = {
        getUserFavorites: jest.fn(),
        addFavorite: jest.fn(),
        removeFavorite: jest.fn()
    };

    reviewServiceMock = {
        getPlaceReviews: jest.fn(),
        getReviewReplies: jest.fn(),
        createReview: jest.fn()
    };

    translocoServiceMock = {
        translate: jest.fn((key: string) => key)
    };

    featureServiceMock = {
        isFeatureEnabled: jest.fn().mockReturnValue(true),
        getFeatures: jest.fn()
    };

    TestBed.configureTestingModule({
        providers: [
        {
            provide: FeatureService,
            useValue: featureServiceMock
        }
        ]
    });

    component = TestBed.runInInjectionContext(() =>
        new PlaceDetailComponent(
        routeMock,
        routerMock,
        placeServiceMock,
        authServiceMock,
        favoriteServiceMock,
        reviewServiceMock,
        translocoServiceMock
        )
    );
    });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit should load place, images and reviews when id exists', () => {
    const place = createPlace();

    placeServiceMock.getPlaceById.mockReturnValue(of(place));
    authServiceMock.getCurrentUser.mockReturnValue({ id: 1, userName: 'Vanessa' });
    favoriteServiceMock.getUserFavorites.mockReturnValue(of([]));
    reviewServiceMock.getPlaceReviews.mockReturnValue(
      of(createPaginatedReviewsResponse([]))
    );

    component.ngOnInit();

    expect(placeServiceMock.getPlaceById).toHaveBeenCalledWith(1);
    expect(component.place).toEqual(place);
    expect(component.images).toEqual([
      'https://res.cloudinary.com/test/image/upload/cristo1.jpg',
      'https://res.cloudinary.com/test/image/upload/cristo2.jpg'
    ]);
    expect(component.currentImageIndex).toBe(0);
    expect(component.loading).toBe(false);
    expect(reviewServiceMock.getPlaceReviews).toHaveBeenCalledWith(1, 0, 10);
  });

  it('ngOnInit should use default image when place has no images', () => {
    const place = createPlace();
    place.images = [];

    placeServiceMock.getPlaceById.mockReturnValue(of(place));
    authServiceMock.getCurrentUser.mockReturnValue(null);
    reviewServiceMock.getPlaceReviews.mockReturnValue(
      of(createPaginatedReviewsResponse([]))
    );

    component.ngOnInit();

    expect(component.images).toEqual(['assets/images/default-place.jpg']);
    expect(component.loading).toBe(false);
  });

  it('ngOnInit should ignore empty image urls and use valid urls only', () => {
    const place = createPlace();
    place.images = [
      {
        id: 1,
        image_url: '',
        public_id: 'empty',
        alt_text: 'Empty',
        display_order: 1,
        is_main: false
      },
      {
        id: 2,
        image_url: '   ',
        public_id: 'blank',
        alt_text: 'Blank',
        display_order: 2,
        is_main: false
      },
      {
        id: 3,
        image_url: 'https://res.cloudinary.com/test/image/upload/valid.jpg',
        public_id: 'valid',
        alt_text: 'Valid',
        display_order: 3,
        is_main: true
      }
    ];

    placeServiceMock.getPlaceById.mockReturnValue(of(place));
    authServiceMock.getCurrentUser.mockReturnValue(null);
    reviewServiceMock.getPlaceReviews.mockReturnValue(
      of(createPaginatedReviewsResponse([]))
    );

    component.ngOnInit();

    expect(component.images).toEqual([
      'https://res.cloudinary.com/test/image/upload/valid.jpg'
    ]);
  });

  it('ngOnInit should stop loading when place service fails', () => {
    placeServiceMock.getPlaceById.mockReturnValue(
      throwError(() => new Error('Error loading place'))
    );

    component.ngOnInit();

    expect(component.loading).toBe(false);
    expect(component.place).toBeNull();
  });

  it('checkIfFavorite should set favorite true when current place is in user favorites', () => {
    const place = createPlace();
    component.place = place;

    authServiceMock.getCurrentUser.mockReturnValue({ id: 1, userName: 'Vanessa' });
    favoriteServiceMock.getUserFavorites.mockReturnValue(
      of([
        {
          id: 10,
          place: {
            id: 1
          }
        }
      ])
    );

    component.checkIfFavorite();

    expect(component.isFavorite).toBe(true);
  });

  it('checkIfFavorite should set favorite false when user is not authenticated', () => {
    component.place = createPlace();

    authServiceMock.getCurrentUser.mockReturnValue(null);

    component.checkIfFavorite();

    expect(component.isFavorite).toBe(false);
    expect(favoriteServiceMock.getUserFavorites).not.toHaveBeenCalled();
  });

  it('toggleFavorite should add favorite when place is not favorite', () => {
    component.place = createPlace();
    component.isFavorite = false;

    authServiceMock.getCurrentUser.mockReturnValue({ id: 1 });
    favoriteServiceMock.addFavorite.mockReturnValue(of({}));

    component.toggleFavorite();

    expect(favoriteServiceMock.addFavorite).toHaveBeenCalledWith(1);
    expect(component.isFavorite).toBe(true);
  });

  it('toggleFavorite should remove favorite when place is already favorite', () => {
    component.place = createPlace();
    component.isFavorite = true;

    authServiceMock.getCurrentUser.mockReturnValue({ id: 1 });
    favoriteServiceMock.removeFavorite.mockReturnValue(of({}));

    component.toggleFavorite();

    expect(favoriteServiceMock.removeFavorite).toHaveBeenCalledWith(1);
    expect(component.isFavorite).toBe(false);
  });

  it('nextImage should move to the next image', () => {
    component.images = ['image1.jpg', 'image2.jpg'];
    component.currentImageIndex = 0;

    component.nextImage();

    expect(component.currentImageIndex).toBe(1);
  });

  it('nextImage should return to first image after last image', () => {
    component.images = ['image1.jpg', 'image2.jpg'];
    component.currentImageIndex = 1;

    component.nextImage();

    expect(component.currentImageIndex).toBe(0);
  });

  it('prevImage should move to the previous image', () => {
    component.images = ['image1.jpg', 'image2.jpg'];
    component.currentImageIndex = 1;

    component.prevImage();

    expect(component.currentImageIndex).toBe(0);
  });

  it('prevImage should move to last image when current image is first', () => {
    component.images = ['image1.jpg', 'image2.jpg'];
    component.currentImageIndex = 0;

    component.prevImage();

    expect(component.currentImageIndex).toBe(1);
  });

  it('getStars should return filled and empty stars based on rating', () => {
    expect(component.getStars(4.8)).toBe('★★★★☆');
    expect(component.getStars(3)).toBe('★★★☆☆');
    expect(component.getStars(0)).toBe('☆☆☆☆☆');
  });

  it('getReviewStars should normalize score between 0 and 5', () => {
    expect(component.getReviewStars(5)).toBe('★★★★★');
    expect(component.getReviewStars(3)).toBe('★★★☆☆');
    expect(component.getReviewStars(10)).toBe('★★★★★');
    expect(component.getReviewStars(-1)).toBe('☆☆☆☆☆');
    expect(component.getReviewStars(undefined)).toBe('☆☆☆☆☆');
  });

  it('formatReviewDate should return yyyy-mm-dd when date is valid', () => {
    expect(component.formatReviewDate('2026-05-20T10:30:00')).toBe('2026-05-20');
  });

  it('formatReviewDate should return empty string when date is missing', () => {
    expect(component.formatReviewDate()).toBe('');
  });

  it('formatReviewDate should return original value when date is invalid', () => {
    expect(component.formatReviewDate('invalid-date')).toBe('invalid-date');
  });

  it('canPublishReview should return true when user is authenticated and form is valid', () => {
    authServiceMock.isAuthenticated.mockReturnValue(true);

    component.reviewComment = 'Muy buen lugar turístico';
    component.reviewScore = 5;
    component.publishingReview = false;

    expect(component.canPublishReview).toBe(true);
  });

  it('canPublishReview should return false when comment is empty', () => {
    authServiceMock.isAuthenticated.mockReturnValue(true);

    component.reviewComment = '   ';
    component.reviewScore = 5;
    component.publishingReview = false;

    expect(component.canPublishReview).toBe(false);
  });

  it('publishReview should create review when data is valid', () => {
    const place = createPlace();
    const createdReview = createReview(1, 'Excelente lugar', 5);

    component.place = place;
    component.reviewComment = ' Excelente lugar ';
    component.reviewScore = 5;
    component.currentReviewsPage = 0;
    component.reviews = [];
    component.reviewsTotal = 0;

    authServiceMock.isAuthenticated.mockReturnValue(true);
    authServiceMock.getCurrentUser.mockReturnValue({ id: 1, userName: 'Vanessa' });
    reviewServiceMock.createReview.mockReturnValue(of(createdReview));

    component.publishReview();

    expect(reviewServiceMock.createReview).toHaveBeenCalledWith({
      placeId: 1,
      parentId: null,
      comment: 'Excelente lugar',
      score: 5
    });

    expect(component.reviews).toEqual([createdReview]);
    expect(component.reviewsTotal).toBe(1);
    expect(component.reviewComment).toBe('');
    expect(component.reviewScore).toBe(0);
    expect(component.publishingReview).toBe(false);
  });

  it('publishReview should show error when user does not have id', () => {
    component.place = createPlace();
    component.reviewComment = 'Excelente lugar';
    component.reviewScore = 5;

    authServiceMock.isAuthenticated.mockReturnValue(true);
    authServiceMock.getCurrentUser.mockReturnValue(null);

    component.publishReview();

    expect(component.reviewError).toBe('placeDetail.textLoginToReview');
    expect(reviewServiceMock.createReview).not.toHaveBeenCalled();
  });

  it('publishReview should show backend error message when create review fails', () => {
    component.place = createPlace();
    component.reviewComment = 'Excelente lugar';
    component.reviewScore = 5;

    authServiceMock.isAuthenticated.mockReturnValue(true);
    authServiceMock.getCurrentUser.mockReturnValue({ id: 1 });
    reviewServiceMock.createReview.mockReturnValue(
      throwError(() => ({
        error: {
          message: 'No puedes publicar esta reseña'
        }
      }))
    );

    component.publishReview();

    expect(component.reviewError).toBe('No puedes publicar esta reseña');
    expect(component.publishingReview).toBe(false);
  });

  it('loadPlaceReviews should load reviews and initialize replies state', () => {
    const review = createReview(1, 'Muy bueno', 5);

    component.place = createPlace();

    reviewServiceMock.getPlaceReviews.mockReturnValue(
      of(createPaginatedReviewsResponse([review]))
    );
    reviewServiceMock.getReviewReplies.mockReturnValue(
      of(createRepliesResponse([]))
    );

    component.loadPlaceReviews(true);

    expect(component.reviews).toEqual([review]);
    expect(component.currentReviewsPage).toBe(0);
    expect(component.reviewsTotal).toBe(1);
    expect(component.reviewsLoading).toBe(false);
    expect(component.repliesStateMap[1]).toBeTruthy();
    expect(reviewServiceMock.getReviewReplies).toHaveBeenCalledWith(1, 0, 2);
  });

  it('toggleReplyComposer should open and close reply composer', () => {
    const reviewId = 1;

    component.toggleReplyComposer(reviewId);

    expect(component.repliesStateMap[reviewId].composerOpen).toBe(true);

    component.toggleReplyComposer(reviewId);

    expect(component.repliesStateMap[reviewId].composerOpen).toBe(false);
  });

  it('publishReply should create reply when comment is valid', () => {
    const parentReview = createReview(1, 'Comentario padre', 5);
    const createdReply = createReview(2, 'Respuesta', 0);

    component.place = createPlace();

    const state = component.getRepliesState(parentReview.id);
    state.replyComment = ' Respuesta ';
    state.expanded = false;

    authServiceMock.getCurrentUser.mockReturnValue({ id: 1 });
    reviewServiceMock.createReview.mockReturnValue(of(createdReply));

    component.publishReply(parentReview);

    expect(reviewServiceMock.createReview).toHaveBeenCalledWith({
      placeId: 1,
      parentId: 1,
      comment: 'Respuesta',
      score: null
    });

    expect(state.replyComment).toBe('');
    expect(state.composerOpen).toBe(false);
    expect(state.publishing).toBe(false);
    expect(state.totalElements).toBe(1);
    expect(state.replies).toEqual([createdReply]);
  });

  it('goToReviewsPage should load selected page when page is valid', () => {
    component.place = createPlace();
    component.currentReviewsPage = 0;
    component.reviewsTotalPages = 3;
    component.reviewsLoading = false;

    reviewServiceMock.getPlaceReviews.mockReturnValue(
      of(createPaginatedReviewsResponse([]))
    );

    component.goToReviewsPage(1);

    expect(component.reviewsPage).toBe(1);
    expect(reviewServiceMock.getPlaceReviews).toHaveBeenCalledWith(1, 1, 10);
  });

  it('reviewsPageNumbers should return empty array when there is only one page', () => {
    component.reviewsTotalPages = 1;

    expect(component.reviewsPageNumbers).toEqual([]);
  });

  it('reviewsPageNumbers should return page window when there are multiple pages', () => {
    component.reviewsTotalPages = 6;
    component.currentReviewsPage = 3;

    expect(component.reviewsPageNumbers).toEqual([1, 2, 3, 4, 5]);
  });

  it('backToItinerary should navigate to itinerarios', () => {
    component.backToItinerary();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/itinerarios']);
  });

  it('backToDepartment should navigate to department when department id exists', () => {
    component.backDepartmentId = 5;

    component.backToDepartment();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/department', 5]);
  });

  it('backToModifyItinerary should navigate to modify itinerary when itinerary id exists', () => {
    component.backItineraryId = 10;

    component.backToModifyItinerary();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/modify-itinerario', 10]);
  });

  it('backToModifyItinerary should navigate to itinerarios when itinerary id does not exist', () => {
    component.backItineraryId = null;

    component.backToModifyItinerary();

    expect(routerMock.navigate).toHaveBeenCalledWith(['/itinerarios']);
  });
});
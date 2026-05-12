/// <reference types="jest" />

import {
  Review,
  CreateReviewRequest,
  ReviewPageResponse
} from './review';

import { User } from '../user/user';
import { Place } from '../place/place';

describe('Review model interfaces', () => {
  const mockUser = {
    id: 1,
    correo: 'ana@example.com',
    userName: 'ana',
    pass: '123456',
    birthday: '2000-01-01',
    city_id: 1,
    city: null,
    state: true
  } as User;

  const mockPlace = {
    id: 10,
    name: 'Cristo de la Concordia'
  } as unknown as Place;

  it('should create a Review object with required fields', () => {
    const review: Review = {
      id: 1,
      user: mockUser,
      comment: 'Muy buen lugar turístico',
      state: true
    };

    expect(review.id).toBe(1);
    expect(review.user).toEqual(mockUser);
    expect(review.comment).toBe('Muy buen lugar turístico');
    expect(review.state).toBe(true);
  });

  it('should create a Review object with optional fields', () => {
    const reply: Review = {
      id: 2,
      user: mockUser,
      comment: 'Gracias por tu reseña',
      state: true
    };

    const review: Review = {
      id: 1,
      user: mockUser,
      place: mockPlace,
      comment: 'Excelente experiencia',
      score: 5,
      createdAt: '2026-05-12T10:00:00',
      state: true,
      replies: [reply]
    };

    expect(review.place).toEqual(mockPlace);
    expect(review.score).toBe(5);
    expect(review.createdAt).toBe('2026-05-12T10:00:00');
    expect(review.replies).toEqual([reply]);
    expect(review.replies?.length).toBe(1);
  });

  it('should create a CreateReviewRequest with required fields', () => {
    const request: CreateReviewRequest = {
      placeId: 10,
      comment: 'Me gustó mucho el lugar'
    };

    expect(request.placeId).toBe(10);
    expect(request.comment).toBe('Me gustó mucho el lugar');
  });

  it('should create a CreateReviewRequest with optional fields', () => {
    const request: CreateReviewRequest = {
      placeId: 10,
      parentId: 1,
      comment: 'Estoy respondiendo a una reseña',
      score: null
    };

    expect(request.placeId).toBe(10);
    expect(request.parentId).toBe(1);
    expect(request.comment).toBe('Estoy respondiendo a una reseña');
    expect(request.score).toBeNull();
  });

  it('should create a CreateReviewRequest with null parentId', () => {
    const request: CreateReviewRequest = {
      placeId: 10,
      parentId: null,
      comment: 'Reseña principal',
      score: 4
    };

    expect(request.parentId).toBeNull();
    expect(request.score).toBe(4);
  });

  it('should create a ReviewPageResponse object', () => {
    const review: Review = {
      id: 1,
      user: mockUser,
      place: mockPlace,
      comment: 'Buen lugar',
      score: 4,
      createdAt: '2026-05-12T10:00:00',
      state: true,
      replies: []
    };

    const pageResponse: ReviewPageResponse = {
      content: [review],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      hasNext: false
    };

    expect(pageResponse.content).toEqual([review]);
    expect(pageResponse.content.length).toBe(1);
    expect(pageResponse.page).toBe(0);
    expect(pageResponse.size).toBe(10);
    expect(pageResponse.totalElements).toBe(1);
    expect(pageResponse.totalPages).toBe(1);
    expect(pageResponse.hasNext).toBe(false);
  });
});
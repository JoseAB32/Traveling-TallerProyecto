/// <reference types="jest" />

import { Place } from './place';
import { City } from '../city/city';

describe('Place', () => {
  it('should create an instance with default values', () => {
    const place = new Place();

    expect(place).toBeTruthy();

    expect(place.id).toBe(0);
    expect(place.name).toBe('');
    expect(place.description).toBe('');
    expect(place.address).toBe('');

    expect(place.rating).toBe(5.0);
    expect(place.price).toBe(0.0);
    expect(place.latitude).toBe(0.0);
    expect(place.longitude).toBe(0.0);

    expect(place.place_type).toBe('');

    expect(place.city).toBeNull();
    expect(place.city_id).toBe(0);

    expect(place.is_event).toBe(false);

    expect(place.start_date).toBeNull();
    expect(place.end_date).toBeNull();

    expect(place.imageUrl).toBe('');
    expect(place.state).toBe(true);
  });

  it('should allow assigning values to properties', () => {
    const place = new Place();

    const mockCity = {
      id: 1,
      name: 'Cochabamba'
    } as unknown as City;

    place.id = 10;
    place.name = 'Cristo de la Concordia';
    place.description = 'Lugar turístico representativo de Cochabamba';
    place.address = 'Av. de la Concordia';

    place.rating = 4.8;
    place.price = 15.5;
    place.latitude = -17.384;
    place.longitude = -66.156;

    place.place_type = 'Monumento';

    place.city = mockCity;
    place.city_id = 1;

    place.is_event = true;

    place.start_date = '2026-05-12';
    place.end_date = '2026-05-13';

    place.imageUrl = 'https://example.com/cristo.jpg';
    place.state = false;

    expect(place.id).toBe(10);
    expect(place.name).toBe('Cristo de la Concordia');
    expect(place.description).toBe('Lugar turístico representativo de Cochabamba');
    expect(place.address).toBe('Av. de la Concordia');

    expect(place.rating).toBe(4.8);
    expect(place.price).toBe(15.5);
    expect(place.latitude).toBe(-17.384);
    expect(place.longitude).toBe(-66.156);

    expect(place.place_type).toBe('Monumento');

    expect(place.city).toEqual(mockCity);
    expect(place.city_id).toBe(1);

    expect(place.is_event).toBe(true);

    expect(place.start_date).toBe('2026-05-12');
    expect(place.end_date).toBe('2026-05-13');

    expect(place.imageUrl).toBe('https://example.com/cristo.jpg');
    expect(place.state).toBe(false);
  });
});
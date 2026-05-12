/// <reference types="jest" />

import { City } from './city';

describe('City', () => {
  it('should create an instance with default values', () => {
    const city = new City();

    expect(city).toBeTruthy();

    expect(city.id).toBeNull();
    expect(city.name).toBeNull();
    expect(city.state).toBe(true);
  });

  it('should allow assigning values to properties', () => {
    const city = new City();

    city.id = 1;
    city.name = 'Cochabamba';
    city.state = false;

    expect(city.id).toBe(1);
    expect(city.name).toBe('Cochabamba');
    expect(city.state).toBe(false);
  });

  it('should allow id and name to remain null', () => {
    const city = new City();

    city.id = null;
    city.name = null;

    expect(city.id).toBeNull();
    expect(city.name).toBeNull();
  });

  it('should allow state to be true', () => {
    const city = new City();

    city.state = true;

    expect(city.state).toBe(true);
  });
});
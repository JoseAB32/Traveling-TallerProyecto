/// <reference types="jest" />

import { User } from './user';

describe('User', () => {
  it('should create an instance with default values', () => {
    const user = new User();

    expect(user).toBeTruthy();
    expect(user.id).toBe(0);
    expect(user.correo).toBe('');
    expect(user.userName).toBe('');
    expect(user.pass).toBe('');
    expect(user.birthday).toBe('');
    expect(user.city_id).toBeNull();
    expect(user.city).toBeNull();
    expect(user.state).toBe(true);
  });

  it('should allow assigning values to properties', () => {
    const user = new User();

    user.id = 1;
    user.correo = 'ana@example.com';
    user.userName = 'ana';
    user.pass = '123456';
    user.birthday = '2000-01-01';
    user.city_id = 2;
    user.city = {
      id: 2,
      name: 'Cochabamba'
    } as any;
    user.state = false;

    expect(user.id).toBe(1);
    expect(user.correo).toBe('ana@example.com');
    expect(user.userName).toBe('ana');
    expect(user.pass).toBe('123456');
    expect(user.birthday).toBe('2000-01-01');
    expect(user.city_id).toBe(2);
    expect(user.city).toEqual({
      id: 2,
      name: 'Cochabamba'
    });
    expect(user.state).toBe(false);
  });
});
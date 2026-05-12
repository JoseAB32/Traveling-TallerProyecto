/// <reference types="jest" />

import { Logger } from './logger';

describe('Logger', () => {
  it('should create an instance with default values', () => {
    const logger = new Logger();

    expect(logger).toBeTruthy();

    expect(logger.id).toBeUndefined();
    expect(logger.timestamp).toBe('');
    expect(logger.module).toBe('');
    expect(logger.level).toBe('');
    expect(logger.message).toBe('');
    expect(logger.userId).toBeNull();
  });

  it('should allow assigning values to properties', () => {
    const logger = new Logger();

    logger.id = 1;
    logger.timestamp = '2026-05-12T10:30:00';
    logger.module = 'PLACES';
    logger.level = 'INFO';
    logger.message = 'Lugar turístico creado correctamente';
    logger.userId = 15;

    expect(logger.id).toBe(1);
    expect(logger.timestamp).toBe('2026-05-12T10:30:00');
    expect(logger.module).toBe('PLACES');
    expect(logger.level).toBe('INFO');
    expect(logger.message).toBe('Lugar turístico creado correctamente');
    expect(logger.userId).toBe(15);
  });

  it('should allow userId to be null', () => {
    const logger = new Logger();

    logger.userId = null;

    expect(logger.userId).toBeNull();
  });

  it('should allow id to remain undefined', () => {
    const logger = new Logger();

    expect(logger.id).toBeUndefined();
  });
});
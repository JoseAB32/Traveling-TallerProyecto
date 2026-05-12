/// <reference types="jest" />

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  provideHttpClientTesting,
  HttpTestingController
} from '@angular/common/http/testing';

import { LoggerService } from './logger.service';
import { Logger } from '../../models/logger/logger';
import { CONSTANTS } from '../../utils/constants';

describe('LoggerService', () => {
  let service: LoggerService;
  let httpMock: HttpTestingController;

  const baseURL = CONSTANTS.API.BASE_URL + CONSTANTS.API.LOGS;

  const mockLogs = [
    {
      id: 1,
      module: 'AUTH',
      level: 'INFO',
      message: 'Usuario inició sesión',
      timestamp: '2026-05-12T10:00:00'
    },
    {
      id: 2,
      module: 'TRIPS',
      level: 'ERROR',
      message: 'Error al crear itinerario',
      timestamp: '2026-05-12T11:00:00'
    }
  ] as unknown as Logger[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LoggerService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(LoggerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get all logs', () => {
    service.getAllLogs().subscribe((logs) => {
      expect(logs).toEqual(mockLogs);
      expect(logs.length).toBe(2);
    });

    const req = httpMock.expectOne(baseURL);

    expect(req.request.method).toBe('GET');

    req.flush(mockLogs);
  });

  it('should get filtered logs with query params', () => {
    const module = 'AUTH';
    const level = 'INFO';
    const start = '2026-05-01';
    const end = '2026-05-12';

    service.getFilteredLogs(module, level, start, end).subscribe((logs) => {
      expect(logs).toEqual(mockLogs);
      expect(logs.length).toBe(2);
    });

    const req = httpMock.expectOne((request) => {
      return request.url === `${baseURL}/filter`;
    });

    expect(req.request.method).toBe('GET');

    expect(req.request.params.get('module')).toBe(module);
    expect(req.request.params.get('level')).toBe(level);
    expect(req.request.params.get('startDate')).toBe(start);
    expect(req.request.params.get('endDate')).toBe(end);

    req.flush(mockLogs);
  });
});
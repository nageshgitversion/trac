import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [AuthService]
    });
    service  = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('isLoggedIn() returns false when no token stored', () => {
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('login() stores tokens on success and sets user signal', () => {
    const mockResponse = {
      success: true,
      data: {
        accessToken:  'mock.access.token',
        refreshToken: 'mock-refresh-token',
        tokenType:    'Bearer',
        expiresIn:    900,
        user: { id: 1, name: 'Arjun Kumar', email: 'arjun@test.in',
                emailVerified: false, riskProfile: 'MODERATE', taxRegime: 'NEW' }
      },
      timestamp: '2026-03-16T09:41:00Z'
    };

    service.login({ email: 'arjun@test.in', password: 'Password@123' }).subscribe(res => {
      expect(res.success).toBeTrue();
      expect(service.isLoggedIn()).toBeTrue();
      expect(service.userName()).toBe('Arjun Kumar');
      expect(service.getAccessToken()).toBe('mock.access.token');
    });

    const req = httpMock.expectOne(r => r.url.includes('/auth/login'));
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('login() does not store tokens when success=false', () => {
    const mockResponse = {
      success: false,
      message: 'Invalid credentials',
      errorCode: 'WLTH-1001',
      timestamp: '2026-03-16T09:41:00Z'
    };

    service.login({ email: 'arjun@test.in', password: 'wrong' }).subscribe(res => {
      expect(res.success).toBeFalse();
      expect(service.isLoggedIn()).toBeFalse();
      expect(service.getAccessToken()).toBeNull();
    });

    httpMock.expectOne(r => r.url.includes('/auth/login')).flush(mockResponse);
  });

  it('register() stores tokens on success', () => {
    const mockResponse = {
      success: true,
      data: {
        accessToken: 'new.token', refreshToken: 'new-refresh',
        tokenType: 'Bearer', expiresIn: 900,
        user: { id: 2, name: 'New User', email: 'new@test.in',
                emailVerified: false, riskProfile: 'MODERATE', taxRegime: 'NEW' }
      },
      timestamp: '2026-03-16T09:41:00Z'
    };

    service.register({ name: 'New User', email: 'new@test.in', password: 'Pass@123' })
      .subscribe(res => {
        expect(res.success).toBeTrue();
        expect(service.isLoggedIn()).toBeTrue();
        expect(service.userName()).toBe('New User');
      });

    httpMock.expectOne(r => r.url.includes('/auth/register')).flush(mockResponse);
  });

  it('logout() clears tokens and sets user to null', () => {
    localStorage.setItem('investrac_access_token', 'some.token');
    localStorage.setItem('investrac_refresh_token', 'some-refresh');
    localStorage.setItem('investrac_user', JSON.stringify({ id: 1, name: 'Test', email: 't@t.in' }));

    service.logout();

    expect(service.isLoggedIn()).toBeFalse();
    expect(service.getAccessToken()).toBeNull();
    expect(localStorage.getItem('investrac_access_token')).toBeNull();
  });

  it('isTokenExpiringSoon() returns true when no token', () => {
    expect(service.isTokenExpiringSoon()).toBeTrue();
  });

  it('userId() returns null when not logged in', () => {
    expect(service.userId()).toBeNull();
  });

  it('getRefreshToken() returns stored refresh token', () => {
    localStorage.setItem('investrac_refresh_token', 'test-refresh');
    expect(service.getRefreshToken()).toBe('test-refresh');
  });
});

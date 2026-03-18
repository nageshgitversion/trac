import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse } from '../models/api-response.model';
import { Notification, NotificationPage } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService extends ApiService {

  private _unreadCount = signal<number>(0);
  readonly unreadCount = this._unreadCount.asReadonly();

  getNotifications(page = 0, size = 20): Observable<ApiResponse<NotificationPage>> {
    return this.get<NotificationPage>('/notifications', { page, size })
      .pipe(tap(res => {
        if (res.success && res.data) {
          this._unreadCount.set(res.data.unreadCount);
        }
      }));
  }

  getUnreadCount(): Observable<ApiResponse<number>> {
    return this.get<number>('/notifications/unread-count')
      .pipe(tap(res => {
        if (res.success && res.data !== undefined) this._unreadCount.set(res.data);
      }));
  }

  markRead(id: number): Observable<ApiResponse<void>> {
    return this.patch<void>(`/notifications/${id}/read`);
  }

  markAllRead(): Observable<ApiResponse<void>> {
    return this.patch<void>('/notifications/read-all');
  }

  updateFcmToken(fcmToken: string): Observable<ApiResponse<void>> {
    return this.put<void>('/notifications/preferences', { fcmToken });
  }
}

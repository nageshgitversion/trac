export type NotificationType = 'TRANSACTION_COMPLETED' | 'TRANSACTION_FAILED' |
  'WALLET_LOW_BALANCE' | 'EMI_DUE' | 'PORTFOLIO_SYNCED' | 'AI_INSIGHT' | 'WELCOME' | 'GENERAL';

export interface Notification {
  id: number;
  userId: number;
  title: string;
  body: string;
  type: NotificationType;
  channel: string;
  dataJson?: string;
  read: boolean;
  sent: boolean;
  sentAt?: string;
  createdAt: string;
}

export interface NotificationPage {
  content: Notification[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  unreadCount: number;
  last: boolean;
}

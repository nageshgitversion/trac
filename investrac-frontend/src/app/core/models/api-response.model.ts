export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  errorCode?: string;
  errors?: Record<string, string>;
  traceId?: string;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

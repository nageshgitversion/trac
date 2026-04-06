export type AccountType = 'FD' | 'RD' | 'SIP' | 'LOAN';
export type AccountStatus = 'ACTIVE' | 'MATURED' | 'CLOSED';

export interface Account {
  id: number;
  userId: number;
  type: AccountType;
  name: string;
  principal: number;
  interestRate?: number;
  tenureMonths?: number;
  startDate?: string;
  maturityDate?: string;
  status: AccountStatus;
  note?: string;
  createdAt: string;
}

export interface CreateAccountRequest {
  type: AccountType;
  name: string;
  principal: number;
  interestRate?: number;
  tenureMonths?: number;
  startDate?: string;
  note?: string;
}

export interface UpdateAccountRequest { note?: string; status?: AccountStatus; }

export interface AccountProjection {
  maturityAmount: number;
  totalInterest: number;
  monthlyEmi?: number;
}

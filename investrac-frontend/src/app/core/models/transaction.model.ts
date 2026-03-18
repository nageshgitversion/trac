export type TransactionType   = 'INCOME' | 'EXPENSE' | 'INVESTMENT' | 'SAVINGS' | 'TRANSFER';
export type TransactionStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
export type TransactionSource = 'MANUAL' | 'VOICE' | 'OCR' | 'SCHEDULED';

export interface Transaction {
  id: number;
  userId: number;
  walletId?: number;
  type: TransactionType;
  category: string;
  name: string;
  amount: number;
  envelopeKey?: string;
  txDate: string;
  note?: string;
  source: TransactionSource;
  status: TransactionStatus;
  failureReason?: string;
  createdAt: string;
}

export interface CreateTransactionRequest {
  type: TransactionType;
  category: string;
  name: string;
  amount: number;
  txDate: string;
  note?: string;
  walletId?: number;
  envelopeKey?: string;
}

export interface MonthSummary {
  year: number;
  month: number;
  monthLabel: string;
  totalIncome: number;
  totalExpense: number;
  totalInvestment: number;
  totalSavings: number;
  netSavings: number;
  savingsRatePercent: number;
  expenseBreakdown: CategoryBreakdown[];
}

export interface CategoryBreakdown {
  category: string;
  amount: number;
  percentOfTotal: number;
}

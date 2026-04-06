export type TransactionType = 'CREDIT' | 'DEBIT';
export type TransactionCategory = 'FOOD' | 'TRANSPORT' | 'SALARY' | 'SHOPPING' | 'INVESTMENT' | 'OTHER';
export type TransactionStatus = 'COMPLETED' | 'FAILED';

export interface Transaction {
  id: number;
  userId: number;
  type: TransactionType;
  category: TransactionCategory;
  name: string;
  amount: number;
  txDate: string;
  note?: string;
  status: TransactionStatus;
  createdAt: string;
}

export interface CreateTransactionRequest {
  type: TransactionType;
  category: TransactionCategory;
  name: string;
  amount: number;
  txDate: string;
  note?: string;
}

export interface UpdateTransactionRequest {
  name?: string;
  category?: TransactionCategory;
  note?: string;
}

export interface TransactionSummary {
  totalIncome: number;
  totalExpense: number;
  netSavings: number;
  year: number;
  month: number;
}

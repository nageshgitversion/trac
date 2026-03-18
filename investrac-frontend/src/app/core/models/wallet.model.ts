export interface Wallet {
  id: number;
  userId: number;
  month: string;
  income: number;
  topup: number;
  balance: number;
  committed: number;
  freeToSpend: number;
  usedPercent: number;
  active: boolean;
  envelopes: WalletEnvelope[];
  createdAt: string;
}

export interface WalletEnvelope {
  id: number;
  envelopeKey: string;
  categoryName: string;
  icon: string;
  budget: number;
  spent: number;
  remaining: number;
  overBudget: boolean;
  usedPercent: number;
}

export interface CreateWalletRequest {
  month: string;
  income: number;
  topup?: number;
  envelopes?: Record<string, number>;
}

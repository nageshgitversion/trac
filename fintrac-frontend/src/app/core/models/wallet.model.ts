export interface WalletResponse {
  id: number;
  userId: number;
  balance: number;
  currency: string;
  updatedAt: string;
}
export interface WalletOperationRequest { amount: number; note?: string; }

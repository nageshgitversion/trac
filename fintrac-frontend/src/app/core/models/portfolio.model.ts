export type HoldingType = 'MF' | 'STOCK' | 'GOLD' | 'FD';

export interface Holding {
  id: number;
  userId: number;
  type: HoldingType;
  name: string;
  symbol?: string;
  units?: number;
  buyPrice?: number;
  currentPrice?: number;
  invested: number;
  currentValue: number;
  returnPct: number;
  note?: string;
  createdAt: string;
}

export interface CreateHoldingRequest {
  type: HoldingType;
  name: string;
  symbol?: string;
  units?: number;
  buyPrice?: number;
  currentPrice?: number;
  note?: string;
}

export interface UpdateHoldingRequest { units?: number; currentPrice?: number; note?: string; }

export interface PortfolioSummary {
  totalInvested: number;
  totalCurrentValue: number;
  totalReturnPct: number;
  byType: Record<string, number>;
}

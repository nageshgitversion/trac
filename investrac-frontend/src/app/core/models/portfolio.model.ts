export type HoldingType = 'EQUITY_MF' | 'STOCKS' | 'DEBT_MF' | 'NPS_PPF' | 'GOLD_SGB' | 'FD' | 'OTHER';

export interface Holding {
  id: number;
  userId: number;
  type: HoldingType;
  name: string;
  symbol?: string;
  units: number;
  buyPrice: number;
  currentPrice: number;
  invested: number;
  currentValue: number;
  xirr: number;
  sipAmount: number;
  isUpdatable: boolean;
  lastSynced?: string;
  note?: string;
  active: boolean;
}

export interface PortfolioSummary {
  totalInvested: number;
  totalValue: number;
  totalReturnPercent: number;
  weightedXirr: number;
  assetAllocation: AssetAllocation[];
  holdings: Holding[];
}

export interface AssetAllocation {
  type: string;
  value: number;
  percent: number;
  color: string;
}

export interface CreateHoldingRequest {
  type: HoldingType;
  name: string;
  symbol?: string;
  units?: number;
  buyPrice?: number;
  invested: number;
  currentValue?: number;
  xirr?: number;
  sipAmount?: number;
  note?: string;
}

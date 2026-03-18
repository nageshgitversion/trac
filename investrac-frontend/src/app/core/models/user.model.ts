export interface UserProfile {
  id?: number;
  email?: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  kycVerified?: boolean;
  riskProfile?: string;
  taxRegime?: string;
  monthlyIncome?: number;
  panNumber?: string;
  dateOfBirth?: string;
  createdAt?: string;
}

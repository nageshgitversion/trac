import { InrFormatPipe } from './inr-format.pipe';

describe('InrFormatPipe', () => {
  const pipe = new InrFormatPipe();

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('formats 1150000 as ₹11,50,000 (Indian numbering)', () => {
    expect(pipe.transform(1150000)).toBe('₹11,50,000');
  });

  it('formats 115000 as ₹1,15,000', () => {
    expect(pipe.transform(115000)).toBe('₹1,15,000');
  });

  it('formats 1500 as ₹1,500', () => {
    expect(pipe.transform(1500)).toBe('₹1,500');
  });

  it('formats 999 as ₹999 (no comma under 1000)', () => {
    expect(pipe.transform(999)).toBe('₹999');
  });

  it('formats negative value with minus sign', () => {
    expect(pipe.transform(-5000)).toBe('-₹5,000');
  });

  it('formats null as ₹0', () => {
    expect(pipe.transform(null)).toBe('₹0');
  });

  it('formats undefined as ₹0', () => {
    expect(pipe.transform(undefined)).toBe('₹0');
  });

  it('showLakhs: formats 1860000 as ₹18.6L', () => {
    expect(pipe.transform(1860000, true)).toBe('₹18.6L');
  });

  it('showLakhs: formats 28400000 as ₹2.84Cr', () => {
    expect(pipe.transform(28400000, true)).toBe('₹2.84Cr');
  });

  it('showLakhs: formats 5000 as ₹5K', () => {
    expect(pipe.transform(5000, true)).toBe('₹5K');
  });

  it('showLakhs: formats 500000 as ₹5L (no trailing .0)', () => {
    expect(pipe.transform(500000, true)).toBe('₹5L');
  });
});

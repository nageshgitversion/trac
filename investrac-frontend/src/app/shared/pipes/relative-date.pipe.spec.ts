import { RelativeDatePipe } from './relative-date.pipe';

describe('RelativeDatePipe', () => {
  const pipe = new RelativeDatePipe();

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('returns Today for today\'s date', () => {
    const today = new Date().toISOString().slice(0, 10);
    expect(pipe.transform(today)).toBe('Today');
  });

  it('returns Yesterday for yesterday', () => {
    const d = new Date();
    d.setDate(d.getDate() - 1);
    expect(pipe.transform(d.toISOString().slice(0, 10))).toBe('Yesterday');
  });

  it('returns "N days ago" for recent dates', () => {
    const d = new Date();
    d.setDate(d.getDate() - 4);
    expect(pipe.transform(d.toISOString().slice(0, 10))).toBe('4 days ago');
  });

  it('returns formatted date for older dates', () => {
    const result = pipe.transform('2026-01-01');
    expect(result).toContain('Jan');
    expect(result).toContain('1');
  });

  it('returns empty string for null', () => {
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
  });
});

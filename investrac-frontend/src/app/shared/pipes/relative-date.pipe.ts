import { Pipe, PipeTransform } from '@angular/core';

/** Converts ISO date string to "Today", "Yesterday", "Mar 14", etc. */
@Pipe({ name: 'relativeDate', standalone: true, pure: true })
export class RelativeDatePipe implements PipeTransform {

  transform(dateStr: string | null | undefined): string {
    if (!dateStr) return '';
    const date  = new Date(dateStr + 'T00:00:00');
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const diff  = Math.floor((today.getTime() - date.getTime()) / 86_400_000);

    if (diff === 0) return 'Today';
    if (diff === 1) return 'Yesterday';
    if (diff < 7)   return `${diff} days ago`;

    return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });
  }
}

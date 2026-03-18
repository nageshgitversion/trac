import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NgClass } from '@angular/common';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  activeIcon: string;
}

@Component({
  selector: 'app-bottom-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, NgClass],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <nav class="bottom-nav" role="navigation" aria-label="Main navigation">
      @for (item of navItems; track item.path) {
        <a
          [routerLink]="item.path"
          routerLinkActive="active"
          class="nav-item"
          [attr.aria-label]="item.label">
          <span class="nav-icon" aria-hidden="true">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </a>
      }
    </nav>
  `,
  styles: [`
    .bottom-nav {
      position: fixed;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 100%;
      max-width: 430px;
      background: var(--color-card);
      border-top: 1.5px solid var(--color-border);
      display: flex;
      padding: 6px 2px env(safe-area-inset-bottom, 16px);
      z-index: 100;
      box-shadow: 0 -4px 20px rgba(79, 70, 229, 0.07);
    }
    .nav-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 3px;
      padding: 7px 2px 3px;
      cursor: pointer;
      border-radius: 14px;
      text-decoration: none;
      color: var(--color-text-muted);
      transition: all 0.2s;
      pointer-events: auto;
    }
    .nav-item.active {
      color: var(--color-primary);
    }
    .nav-item.active .nav-icon {
      background: var(--color-primary-light);
      border-radius: 13px;
    }
    .nav-icon {
      width: 42px;
      height: 42px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      pointer-events: none;
    }
    .nav-label {
      font-family: var(--font-heading);
      font-size: 9px;
      font-weight: 800;
      letter-spacing: 0.1px;
      pointer-events: none;
    }
  `]
})
export class BottomNavComponent {
  navItems: NavItem[] = [
    { path: '/home',         label: 'Home',       icon: '🏠', activeIcon: '🏠' },
    { path: '/transactions', label: 'Expenses',   icon: '💳', activeIcon: '💳' },
    { path: '/wallet',       label: 'Wallet',     icon: '👛', activeIcon: '👛' },
    { path: '/portfolio',    label: 'Portfolio',  icon: '📈', activeIcon: '📈' },
    { path: '/settings',     label: 'Settings',   icon: '⚙️', activeIcon: '⚙️' },
  ];
}

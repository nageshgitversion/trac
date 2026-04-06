import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, SidebarComponent, CommonModule],
  template: `
    <div style="display:flex;flex-direction:column;min-height:100vh">
      <app-navbar />
      <div style="display:flex;flex:1">
        <app-sidebar />
        <main style="flex:1;padding:24px;background:#f5f5f5;overflow-y:auto">
          <router-outlet />
        </main>
      </div>
    </div>
  `
})
export class LayoutComponent {}

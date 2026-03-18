# INVESTRAC Frontend — Angular 17 PWA

Mobile-first Progressive Web App for the INVESTRAC fintech platform.

## Stack
Angular 17 · Standalone Components · Signals · OnPush · SCSS

## Features
- 🔐 JWT auth with token refresh (RS256)
- 👛 Monthly wallet with envelope budgeting
- 💳 Transaction CRUD with SAGA integration
- 📈 Portfolio tracker with live price sync
- 📊 Monthly income/expense summaries
- 🔔 Real-time notifications via FCM
- ⚙️ KYC, user profile, preferences
- 📱 PWA — installable on Android/iOS

## Start Development
```bash
npm install
npm start          # → http://localhost:4200
```

## Proxy
All `/api` calls are proxied to `http://localhost:8080` (API Gateway).
Make sure the backend is running first.

## Build Production
```bash
npm run build:prod
```

## Tests
```bash
npm test
```

## Architecture
- `core/models`       — TypeScript interfaces mirroring Spring DTOs
- `core/services`     — API service layer (Signals-based state)
- `core/interceptors` — JWT attachment + auto-refresh on 401
- `core/guards`       — authGuard (protect routes), guestGuard (auth pages)
- `features/`         — Lazy-loaded feature modules
- `shared/pipes`      — `inr` (Indian rupee format), `relativeDate`
- `shared/components` — BottomNav, Toast, LoadingSpinner

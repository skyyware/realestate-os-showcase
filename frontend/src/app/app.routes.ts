import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: 'set-password', children: [] },
  { path: '**', redirectTo: '' }
];

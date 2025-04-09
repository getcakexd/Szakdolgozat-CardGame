import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {authInterceptor} from '../auth.interceptor';
import {provideAnimations} from '@angular/platform-browser/animations';
import {socialAuthServiceConfig} from '../environments/social-auth.config';

export const AppConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    {
      provide: "SocialAuthServiceConfig",
      useValue: socialAuthServiceConfig,
    },
    provideAnimations(),
    provideHttpClient(withInterceptors([authInterceptor]))]
};

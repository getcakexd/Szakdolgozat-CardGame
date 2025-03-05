import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {authInterceptor} from './auth.interceptor';
import {AppRoutingModule} from './app/app.routes';
import {AppConfig} from './app/app.config';

bootstrapApplication(AppComponent, AppConfig).catch((err) => console.error(err));

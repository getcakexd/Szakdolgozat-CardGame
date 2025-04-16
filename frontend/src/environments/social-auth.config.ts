import { type SocialAuthServiceConfig, GoogleLoginProvider } from "@abacritt/angularx-social-login"
import {GOOGLE_CLIENT_ID_LOCAL} from './secrets';

declare global {
  interface Window {
    env: {
      GOOGLE_CLIENT_ID: string;
    };
  }
}

const GOOGLE_CLIENT_ID = window.env?.GOOGLE_CLIENT_ID || GOOGLE_CLIENT_ID_LOCAL;

export const socialAuthServiceConfig: SocialAuthServiceConfig = {
  autoLogin: false,
  providers: [
    {
      id: GoogleLoginProvider.PROVIDER_ID,
      provider: new GoogleLoginProvider(
        GOOGLE_CLIENT_ID,
        {
          oneTapEnabled: false,
          prompt: "select_account",
        },
      ),
    },
  ],
  onError: (err) => {
    console.error(err)
  },
}

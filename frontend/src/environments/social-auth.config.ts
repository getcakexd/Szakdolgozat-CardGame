import { type SocialAuthServiceConfig, GoogleLoginProvider } from "@abacritt/angularx-social-login"
//FOR LOCAL DEV
//import {GOOGLE_CLIENT_ID} from './secrets';

type EnvConfig = {
  GOOGLE_CLIENT_ID: string;
};

// For production
// Remove for DEV
const env = (window as any).env as EnvConfig | undefined;
const GOOGLE_CLIENT_ID = env?.GOOGLE_CLIENT_ID || '';


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

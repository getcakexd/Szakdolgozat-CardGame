import { type SocialAuthServiceConfig, GoogleLoginProvider } from "@abacritt/angularx-social-login"
import {IS_DEV} from './api-config';

export const socialAuthServiceConfig: SocialAuthServiceConfig = {
  autoLogin: false,
  providers: [
    {
      id: GoogleLoginProvider.PROVIDER_ID,
      provider: new GoogleLoginProvider(
        (window as any).GOOGLE_CLIENT_ID || '',
        {
          oneTapEnabled: false,
          prompt: "select_account",
        },
      ),
    },
  ],
  onError: (err) => {
    if(IS_DEV) console.error(err)
  },
}

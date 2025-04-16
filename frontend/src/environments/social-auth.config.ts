import { type SocialAuthServiceConfig, GoogleLoginProvider } from "@abacritt/angularx-social-login"

const GOOGLE_CLIENT_ID = process.env['GOOGLE_CLIENT_ID'] || '';

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

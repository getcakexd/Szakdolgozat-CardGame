import { Component, OnInit } from "@angular/core"
import { CommonModule } from "@angular/common"
import { MatButtonModule } from "@angular/material/button"
import { MatCardModule } from "@angular/material/card"
import { MatDividerModule } from "@angular/material/divider"
import { MatExpansionModule } from "@angular/material/expansion"
import { MatIconModule } from "@angular/material/icon"
import { MatDialogModule, MatDialog } from "@angular/material/dialog"
import { MatSnackBar } from "@angular/material/snack-bar"
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner"
import {Router, RouterLink} from "@angular/router"
import { TranslateModule, TranslateService } from "@ngx-translate/core"
import { GdprService } from "../../services/gdpr/gdpr.service"
import { AuthService } from "../../services/auth/auth.service"
import { Observable } from "rxjs"

@Component({
  selector: "app-privacy-policy",
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatExpansionModule,
    MatIconModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    TranslateModule,
    RouterLink,
  ],
  templateUrl: "./privacy-policy.component.html",
  styleUrls: ["./privacy-policy.component.scss"],
})
export class PrivacyPolicyComponent implements OnInit {
  isLoggedIn = false
  dataExportInProgress$: Observable<boolean>

  constructor(
    private gdprService: GdprService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private translate: TranslateService,
    private router: Router,
  ) {
    this.dataExportInProgress$ = this.gdprService.dataExportInProgress$
  }

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn()

    if (this.isLoggedIn) {
      this.gdprService.logDataAccess("privacy_policy", "view").subscribe()
    }
  }

  exportUserData(): void {
    if (!this.isLoggedIn) {
      this.showLoginRequiredMessage()
      return
    }

    this.gdprService.exportUserData().subscribe(
      (data) => {
        const dataStr = JSON.stringify(data, null, 2)
        const dataBlob = new Blob([dataStr], { type: "application/json" })

        const url = URL.createObjectURL(dataBlob)
        const link = document.createElement("a")
        link.href = url
        link.download = "my-cardhub-data.json"
        link.click()

        URL.revokeObjectURL(url)

        this.snackBar.open(this.translate.instant("GDPR.DATA_EXPORT_SUCCESS"), this.translate.instant("GDPR.CLOSE"), {
          duration: 5000,
        })

        this.gdprService.logDataAccess("user_data", "export").subscribe()
      },
      (error) => {
        console.error("Error exporting user data:", error)
        this.snackBar.open(this.translate.instant("GDPR.DATA_EXPORT_ERROR"), this.translate.instant("GDPR.CLOSE"), {
          duration: 5000,
          panelClass: ["error-snackbar"],
        })
      },
    )
  }

  private showLoginRequiredMessage(): void {
    this.snackBar.open(this.translate.instant("GDPR.LOGIN_REQUIRED"), this.translate.instant("GDPR.CLOSE"), {
      duration: 5000,
    })
  }

  goToSupport() {
    this.router.navigate(["/support"])
  }

  goToProfile() {
    this.router.navigate(["/profile"])
  }
}

import { Component, OnInit, ViewChild, Inject } from "@angular/core"
import { FormBuilder, FormGroup, ReactiveFormsModule } from "@angular/forms"
import { MatPaginator, PageEvent } from "@angular/material/paginator"
import { MatSort } from "@angular/material/sort"
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
} from "@angular/material/table"
import { TranslatePipe, TranslateService } from "@ngx-translate/core"
import { AuditLog, AuditLogFilter } from "../../models/audit-log.model"
import { AuditLogService } from "../../services/audit-log/audit-log.service"
import { MatProgressSpinner } from "@angular/material/progress-spinner"
import {NgClass, NgForOf, DatePipe, NgIf} from "@angular/common"
import { MatFormField, MatInput, MatLabel } from "@angular/material/input"
import { MatOption } from "@angular/material/core"
import { MatSelect } from "@angular/material/select"
import { MatCard, MatCardContent } from "@angular/material/card"
import { MatButton } from "@angular/material/button"
import { MatIcon } from "@angular/material/icon"
import { MatTooltip } from "@angular/material/tooltip"
import { MatSnackBar } from "@angular/material/snack-bar"
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialogContent,
  MatDialogTitle, MatDialogActions, MatDialogClose
} from "@angular/material/dialog"
import { IS_DEV } from "../../../environments/api-config"

@Component({
  selector: "app-audit-logs",
  templateUrl: "./audit-logs.component.html",
  styleUrls: ["./audit-logs.component.scss"],
  imports: [
    MatHeaderCellDef,
    MatHeaderCell,
    MatCellDef,
    MatCell,
    TranslatePipe,
    MatHeaderRowDef,
    MatHeaderRow,
    MatRowDef,
    MatRow,
    MatColumnDef,
    MatPaginator,
    MatTable,
    MatProgressSpinner,
    MatInput,
    MatLabel,
    MatFormField,
    ReactiveFormsModule,
    NgForOf,
    MatOption,
    MatSelect,
    MatCardContent,
    MatCard,
    MatButton,
    MatSort,
    MatIcon,
    NgClass,
    MatTooltip,
    DatePipe,
    NgIf,
  ],
  standalone: true,
})
export class AuditLogsComponent implements OnInit {
  displayedColumns: string[] = ["id", "action", "user", "timestamp", "details"]
  allLogs: AuditLog[] = []
  filteredLogs: AuditLog[] = []
  displayedLogs: AuditLog[] = []
  filterForm: FormGroup
  loading = false
  actions: string[] = []
  isFiltering = false
  noResults = false
  pageSize = 10
  pageSizeOptions: number[] = [5, 10, 25, 50, 100]
  pageIndex = 0
  totalItems = 0
  isMobile = window.innerWidth < 768
  isMobileSmall = window.innerWidth < 480
  @ViewChild(MatSort) sort!: MatSort
  Math = Math

  months: string[] = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ]

  currentDate = new Date()

  constructor(
    private auditLogService: AuditLogService,
    private fb: FormBuilder,
    public translate: TranslateService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) {
    this.filterForm = this.fb.group({
      userId: [""],
      action: [""],
      startDate: [null],
      endDate: [null],
      startDay: [null],
      startMonth: [null],
      startYear: [null],
      endDay: [null],
      endMonth: [null],
      endYear: [null],
      searchTerm: [""],
    })
  }

  ngOnInit(): void {
    this.loadAuditLogs()
    this.loadActions()
    this.updateDisplayColumns()
  }

  onResize(event: any): void {
    this.isMobile = window.innerWidth < 768
    this.isMobileSmall = window.innerWidth < 480
    this.updateDisplayColumns()
  }

  updateDisplayColumns(): void {
    if (this.isMobileSmall) {
      this.displayedColumns = ["action", "user", "timestamp"]
    } else if (this.isMobile) {
      this.displayedColumns = ["action", "user", "timestamp", "details"]
    } else {
      this.displayedColumns = ["id", "action", "user", "timestamp", "details"]
    }
  }

  loadAuditLogs(): void {
    this.loading = true
    this.noResults = false

    this.auditLogService.getAllLogs().subscribe({
      next: (logs) => {
        this.allLogs = logs
        this.filteredLogs = [...logs]
        this.totalItems = logs.length
        this.noResults = logs.length === 0
        this.updateDisplayedLogs()
        this.loading = false
      },
      error: (error) => {
        if (IS_DEV) console.error("Error fetching audit logs", error)
        this.showError(this.translate.instant("AUDIT_LOGS.ERROR_LOADING"))
        this.loading = false
      },
    })
  }

  loadActions(): void {
    this.auditLogService.getAllActions().subscribe({
      next: (actions) => {
        this.actions = actions
      },
      error: (error) => {
        if (IS_DEV) console.error("Error fetching actions", error)
      },
    })
  }

  getDaysInMonth(year: number, month: number): number[] {
    if (year === null || month === null) {
      return Array.from({ length: 31 }, (_, i) => i + 1)
    }

    const daysInMonth = new Date(year, month + 1, 0).getDate()
    return Array.from({ length: daysInMonth }, (_, i) => i + 1)
  }

  updateDaysInStartMonth(): void {
    const year = this.filterForm.get("startYear")?.value
    const month = this.filterForm.get("startMonth")?.value
    const day = this.filterForm.get("startDay")?.value

    if (year !== null && month !== null) {
      const daysInMonth = new Date(year, month + 1, 0).getDate()
      if (day > daysInMonth) {
        this.filterForm.get("startDay")?.setValue(daysInMonth)
      }
    }

    this.updateStartDate()
  }

  updateDaysInEndMonth(): void {
    const year = this.filterForm.get("endYear")?.value
    const month = this.filterForm.get("endMonth")?.value
    const day = this.filterForm.get("endDay")?.value

    if (year !== null && month !== null) {
      const daysInMonth = new Date(year, month + 1, 0).getDate()
      if (day > daysInMonth) {
        this.filterForm.get("endDay")?.setValue(daysInMonth)
      }
    }

    this.updateEndDate()
  }

  updateStartDate(): void {
    const year = this.filterForm.get("startYear")?.value
    const month = this.filterForm.get("startMonth")?.value
    const day = this.filterForm.get("startDay")?.value

    if (year !== null && month !== null && day !== null) {
      const date = new Date(year, month, day)
      date.setHours(0, 0, 0, 0)
      this.filterForm.get("startDate")?.setValue(date)

      const endDate = this.filterForm.get("endDate")?.value
      if (endDate && endDate < date) {
        this.filterForm.patchValue({
          endYear: year,
          endMonth: month,
          endDay: day,
          endDate: new Date(year, month, day, 23, 59, 59, 999),
        })
      }
    } else {
      this.filterForm.get("startDate")?.setValue(null)
    }
  }

  updateEndDate(): void {
    const year = this.filterForm.get("endYear")?.value
    const month = this.filterForm.get("endMonth")?.value
    const day = this.filterForm.get("endDay")?.value

    if (year !== null && month !== null && day !== null) {
      const date = new Date(year, month, day)
      date.setHours(23, 59, 59, 999)
      this.filterForm.get("endDate")?.setValue(date)

      const startDate = this.filterForm.get("startDate")?.value
      if (startDate && date < startDate) {
        this.showError(this.translate.instant("AUDIT_LOGS.END_DATE_BEFORE_START"))
      }
    } else {
      this.filterForm.get("endDate")?.setValue(null)
    }
  }

  applyFilters(): void {
    const filter: AuditLogFilter = {}
    this.isFiltering = true
    this.loading = true
    this.noResults = false

    const userId = this.filterForm.get("userId")?.value
    if (userId) {
      filter.userId = Number.parseInt(userId, 10)
    }

    const action = this.filterForm.get("action")?.value
    if (action) {
      filter.action = action
    }

    const startDate = this.filterForm.get("startDate")?.value
    if (startDate) {
      filter.startDate = new Date(startDate)
    }

    const endDate = this.filterForm.get("endDate")?.value
    if (endDate) {
      filter.endDate = new Date(endDate)
    }

    if (Object.keys(filter).length === 0) {
      this.filteredLogs = [...this.allLogs]
      this.applySearchFilter()
      this.updateDisplayedLogs()
      this.isFiltering = false
      this.loading = false
      return
    }

    this.auditLogService.getFilteredLogs(filter).subscribe({
      next: (logs) => {
        this.filteredLogs = logs
        this.totalItems = logs.length
        this.noResults = logs.length === 0
        this.applySearchFilter()
        this.pageIndex = 0
        this.updateDisplayedLogs()
        this.loading = false
        this.isFiltering = false
      },
      error: (error) => {
        if (IS_DEV) console.error("Error fetching filtered audit logs", error)
        this.showError(this.translate.instant("AUDIT_LOGS.ERROR_FILTERING"))
        this.loading = false
        this.isFiltering = false
      },
    })
  }

  resetFilters(): void {
    this.filterForm.reset()
    this.filteredLogs = [...this.allLogs]
    this.totalItems = this.allLogs.length
    this.pageIndex = 0
    this.updateDisplayedLogs()
  }

  applySearchFilter(): void {
    const searchTerm = this.filterForm.get("searchTerm")?.value
    if (!searchTerm) {
      return
    }

    const term = searchTerm.toLowerCase()
    this.filteredLogs = this.filteredLogs.filter((log) => {
      const searchStr =
        (log.id?.toString() || "") +
        (log.action || "") +
        (log.userId?.toString() || "") +
        (log.details || "") +
        (new Date(log.timestamp).toLocaleString() || "")

      return searchStr.toLowerCase().includes(term)
    })

    this.totalItems = this.filteredLogs.length
    this.noResults = this.filteredLogs.length === 0
    this.pageIndex = 0
  }

  applyTableFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value
    this.filterForm.get("searchTerm")?.setValue(filterValue)
    this.applyFilters()
  }

  updateDisplayedLogs(): void {
    const startIndex = this.pageIndex * this.pageSize
    const endIndex = startIndex + this.pageSize
    this.displayedLogs = this.filteredLogs.slice(startIndex, endIndex)
  }

  handlePageEvent(event: PageEvent): void {
    this.pageSize = event.pageSize
    this.pageIndex = event.pageIndex
    this.updateDisplayedLogs()
  }

  getActionClass(action: string): string {
    action = action.toLowerCase()
    if (action.includes("login")) return "action-login"
    if (action.includes("logout")) return "action-logout"
    if (action.includes("create")) return "action-create"
    if (action.includes("update") || action.includes("edit")) return "action-update"
    if (action.includes("delete") || action.includes("remove")) return "action-delete"
    return "action-other"
  }

  getShortActionName(action: string): string {
    if (action.length > 20) {
      return action.substring(0, 17) + "..."
    }
    return action
  }

  getActionDisplayText(action: string): string {
    return action
      .replace(/_/g, " ")
      .split(" ")
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(" ")
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleString()
  }

  setDefaultYear(fieldName: "startYear" | "endYear"): void {
    const currentValue = this.filterForm.get(fieldName)?.value
    if (currentValue === null || currentValue === undefined) {
      const currentYear = new Date().getFullYear()
      this.filterForm.get(fieldName)?.setValue(currentYear)

      if (fieldName === "startYear") {
        this.updateDaysInStartMonth()
      } else {
        this.updateDaysInEndMonth()
      }
    }
  }

  setDateRange(range: string): void {
    const today = new Date()
    let startDate: Date | null = null
    let endDate: Date | null = null

    switch (range) {
      case "today":
        startDate = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0)
        endDate = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59, 999)
        break
      case "yesterday":
        startDate = new Date(today)
        startDate.setDate(startDate.getDate() - 1)
        startDate.setHours(0, 0, 0, 0)
        endDate = new Date(today)
        endDate.setDate(endDate.getDate() - 1)
        endDate.setHours(23, 59, 59, 999)
        break
      case "last7days":
        startDate = new Date(today)
        startDate.setDate(startDate.getDate() - 6)
        startDate.setHours(0, 0, 0, 0)
        endDate = new Date(today)
        endDate.setHours(23, 59, 59, 999)
        break
      case "last30days":
        startDate = new Date(today)
        startDate.setDate(startDate.getDate() - 29)
        startDate.setHours(0, 0, 0, 0)
        endDate = new Date(today)
        endDate.setHours(23, 59, 59, 999)
        break
      case "thisMonth":
        startDate = new Date(today.getFullYear(), today.getMonth(), 1)
        endDate = new Date(today.getFullYear(), today.getMonth() + 1, 0)
        endDate.setHours(23, 59, 59, 999)
        break
    }

    if (startDate && endDate) {
      this.filterForm.patchValue({
        startDate: startDate,
        endDate: endDate,
        startDay: startDate.getDate(),
        startMonth: startDate.getMonth(),
        startYear: startDate.getFullYear(),
        endDay: endDate.getDate(),
        endMonth: endDate.getMonth(),
        endYear: endDate.getFullYear(),
      })
    }
  }

  clearDateRange(): void {
    this.filterForm.patchValue({
      startDate: null,
      endDate: null,
      startDay: null,
      startMonth: null,
      startYear: null,
      endDay: null,
      endMonth: null,
      endYear: null,
    })
  }

  showLogDetails(log: AuditLog): void {
    if (this.isMobileSmall) {
      this.dialog.open(LogDetailsDialogComponent, {
        width: "95%",
        maxWidth: "500px",
        data: log,
      })
    }
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant("COMMON.CLOSE"), {
      duration: 5000,
      panelClass: "error-snackbar",
    })
  }
}

@Component({
  selector: "log-details-dialog",
  template: `
    <h2 mat-dialog-title>{{ 'AUDIT_LOGS.LOG_DETAILS' | translate }}</h2>
    <mat-dialog-content>
      <div class="log-detail-item">
        <span class="detail-label">{{ 'AUDIT_LOGS.ID' | translate }}:</span>
        <span class="detail-value">{{ data.id }}</span>
      </div>
      <div class="log-detail-item">
        <span class="detail-label">{{ 'AUDIT_LOGS.ACTION' | translate }}:</span>
        <span class="detail-value">{{ data.action }}</span>
      </div>
      <div class="log-detail-item">
        <span class="detail-label">{{ 'AUDIT_LOGS.USER' | translate }}:</span>
        <span class="detail-value">{{ data.userId || 'N/A' }}</span>
      </div>
      <div class="log-detail-item">
        <span class="detail-label">{{ 'AUDIT_LOGS.TIMESTAMP' | translate }}:</span>
        <span class="detail-value">{{ data.timestamp | date:'medium' }}</span>
      </div>
      <div class="log-detail-item">
        <span class="detail-label">{{ 'AUDIT_LOGS.DETAILS' | translate }}:</span>
        <span class="detail-value">{{ data.details }}</span>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>{{ 'COMMON.CLOSE' | translate }}</button>
    </mat-dialog-actions>
  `,
  styles: [
    `
    .log-detail-item {
      margin-bottom: 12px;
    }
    .detail-label {
      font-weight: 500;
      display: block;
      margin-bottom: 4px;
      color: var(--mat-sys-on-surface-variant);
    }
    .detail-value {
      display: block;
      word-break: break-word;
    }
  `,
  ],
  standalone: true,
  imports: [MatButton, TranslatePipe, DatePipe, MatDialogContent, MatDialogTitle, MatDialogActions, MatDialogClose],
})
export class LogDetailsDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<LogDetailsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AuditLog
  ) {}
}

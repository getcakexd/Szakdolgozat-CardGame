import { Component, OnInit, ViewChild } from "@angular/core"
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
import { NgClass, NgForOf, NgIf } from "@angular/common"
import { MatFormField, MatInput, MatLabel } from "@angular/material/input"
import { MatDatepicker, MatDatepickerInput, MatDatepickerToggle } from "@angular/material/datepicker"
import { MatOption, provideNativeDateAdapter } from "@angular/material/core"
import { MatSelect } from "@angular/material/select"
import { MatCard, MatCardContent } from "@angular/material/card"
import { MatButton } from "@angular/material/button"
import { MatIcon } from "@angular/material/icon"
import { MatTooltip } from "@angular/material/tooltip"
import { MatHint } from "@angular/material/form-field"
import { MatSnackBar } from "@angular/material/snack-bar"

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
    NgIf,
    MatInput,
    MatLabel,
    MatFormField,
    MatDatepickerToggle,
    MatDatepicker,
    MatDatepickerInput,
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
    MatHint,
  ],
  providers: [provideNativeDateAdapter()],
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
  @ViewChild(MatSort) sort!: MatSort
  Math = Math

  constructor(
    private auditLogService: AuditLogService,
    private fb: FormBuilder,
    public translate: TranslateService,
    private snackBar: MatSnackBar,
  ) {
    this.filterForm = this.fb.group({
      userId: [""],
      action: [""],
      startDate: [null],
      endDate: [null],
      searchTerm: [""],
    })
  }

  ngOnInit(): void {
    this.loadAuditLogs()
    this.loadActions()
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
        console.error("Error fetching audit logs", error)
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
        console.error("Error fetching actions", error)
      },
    })
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
        console.error("Error fetching filtered audit logs", error)
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

  // New method to get alternative text for action badges without underscores
  getActionDisplayText(action: string): string {
    // Replace underscores with spaces and capitalize each word
    return action
      .replace(/_/g, ' ')
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleString()
  }

  private showError(message: string): void {
    this.snackBar.open(message, this.translate.instant("COMMON.CLOSE"), {
      duration: 5000,
      panelClass: "error-snackbar",
    })
  }
}

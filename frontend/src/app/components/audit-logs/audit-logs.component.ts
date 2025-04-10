import { Component, OnInit, ViewChild } from "@angular/core"
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms"
import { MatPaginator } from "@angular/material/paginator"
import { MatSort } from "@angular/material/sort"
import {
  MatCell,
  MatCellDef, MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef, MatHeaderRow,
  MatHeaderRowDef, MatNoDataRow, MatRow, MatRowDef, MatTable,
  MatTableDataSource
} from "@angular/material/table"
import {TranslatePipe, TranslateService} from "@ngx-translate/core"
import { debounceTime } from "rxjs/operators"
import { AuditLog, AuditLogFilter } from "../../models/audit-log.model"
import { AuditLogService } from "../../services/audit-log/audit-log.service"
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {NgForOf, NgIf} from '@angular/common';
import {MatFormField, MatInput, MatLabel} from '@angular/material/input';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from '@angular/material/datepicker';
import {MatOption} from '@angular/material/core';
import {MatSelect} from '@angular/material/select';
import {MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from '@angular/material/card';
import {MatButton} from '@angular/material/button';

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
    MatNoDataRow,
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
    MatExpansionPanelTitle,
    MatExpansionPanelHeader,
    MatExpansionPanel,
    MatCardContent,
    MatCardTitle,
    MatCardHeader,
    MatCard,
    MatButton,
    MatSort
  ],
  standalone: true
})
export class AuditLogsComponent implements OnInit {
  displayedColumns: string[] = ["id", "action", "user", "timestamp", "details"]
  dataSource = new MatTableDataSource<AuditLog>([])
  filterForm: FormGroup
  loading = false
  actions: string[] = []

  @ViewChild(MatPaginator) paginator!: MatPaginator
  @ViewChild(MatSort) sort!: MatSort

  constructor(
    private auditLogService: AuditLogService,
    private fb: FormBuilder,
    public translate: TranslateService,
  ) {
    this.filterForm = this.fb.group({
      userId: [""],
      action: [""],
      startDate: [""],
      endDate: [""],
    })
  }

  ngOnInit(): void {
    this.loadAuditLogs()

    this.filterForm.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      this.applyFilters()
    })
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator
    this.dataSource.sort = this.sort
  }

  loadAuditLogs(): void {
    this.loading = true
    this.auditLogService.getAllLogs().subscribe({
      next: (logs) => {
        this.dataSource.data = logs
        this.extractUniqueActions()
        this.loading = false
      },
      error: (error) => {
        console.error("Error fetching audit logs", error)
        this.loading = false
      },
    })
  }

  applyFilters(): void {
    const filter: AuditLogFilter = {}

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

    // If no filters are applied, load all logs
    if (Object.keys(filter).length === 0) {
      this.loadAuditLogs()
      return
    }

    this.loading = true
    this.auditLogService.getFilteredLogs(filter).subscribe({
      next: (logs) => {
        this.dataSource.data = logs
        this.loading = false
      },
      error: (error) => {
        console.error("Error fetching filtered audit logs", error)
        this.loading = false
      },
    })
  }

  resetFilters(): void {
    this.filterForm.reset()
    this.loadAuditLogs()
  }

  applyTableFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value
    this.dataSource.filter = filterValue.trim().toLowerCase()

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage()
    }
  }

  private extractUniqueActions(): void {
    this.actions = [...new Set(this.dataSource.data.map((log) => log.action))]
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleString()
  }
}

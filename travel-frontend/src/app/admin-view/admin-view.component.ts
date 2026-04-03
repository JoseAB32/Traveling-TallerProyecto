import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';
import { LoggerService } from '../logger.service';
import { Logger } from '../logger';

@Component({
  selector: 'app-admin-view',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent],
  templateUrl: './admin-view.component.html',
  styleUrl: './admin-view.component.css'
})
export class AdminViewComponent implements OnInit, OnDestroy {
  private loggerService = inject(LoggerService);
  private logSub!: Subscription;

  logs: Logger[] = [];
  activeTab: string = 'logs'; // Control del mini-menu
  
  // Modal de error
  showErrorModal: boolean = false;
  modalMessage: string = '';

  modules = ['AUTH', 'USERS', 'PLACES', 'FAVORITES', 'SYSTEM'];
  levels = ['INFO', 'WARN', 'ERROR', 'DEBUG'];

  filterModule: string = '';
  filterLevel: string = '';
  startDate: string = '';
  endDate: string = '';

  ngOnInit(): void {
    this.setDefaultDates();
    this.loadAllLogs();
  }

  // Establece Fin (hoy) y Inicio (hace 5 días)
  setDefaultDates(): void {
    const now = new Date();
    const fiveDaysAgo = new Date();
    fiveDaysAgo.setDate(now.getDate() - 5);

    this.endDate = this.formatDateForInput(now);
    this.startDate = this.formatDateForInput(fiveDaysAgo);
  }

  // Helper para formatear fecha al estilo 'YYYY-MM-DDTHH:mm' que requiere el input
  formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  loadAllLogs(): void {
    this.logSub = this.loggerService.getAllLogs().subscribe({
      next: (data) => this.logs = data.slice(0, 20),
      error: (err) => console.error('Error cargando logs', err)
    });
  }

  applyFilters(): void {
    if (this.filterModule && this.filterLevel && this.startDate && this.endDate) {
      this.loggerService.getFilteredLogs(this.filterModule, this.filterLevel, this.startDate, this.endDate)
        .subscribe(data => this.logs = data.slice(0, 20));
    } else {
      // Cambio de alert a Modal
      this.modalMessage = 'Todos los campos de filtro son obligatorios para realizar la búsqueda precisa.';
      this.showErrorModal = true;
    }
  }

  ngOnDestroy(): void {
    if (this.logSub) this.logSub.unsubscribe();
  }
}
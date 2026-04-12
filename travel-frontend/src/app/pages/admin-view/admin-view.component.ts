import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { LoggerService } from '../../services/logger/logger.service';
import { FeatureService } from '../../services/features/feature.service'; // Nuevo servicio
import { Logger } from '../../models/logger/logger';
import { CONSTANTS } from '../../utils/constants';

@Component({
  selector: 'app-admin-view',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent],
  templateUrl: './admin-view.component.html',
  styleUrl: './admin-view.component.css'
})
export class AdminViewComponent implements OnInit, OnDestroy {
  private loggerService = inject(LoggerService);
  private featureService = inject(FeatureService);
  private logSub!: Subscription;

  // Datos de Logs
  logs: Logger[] = [];
  activeTab: string = 'logs';
  
  // Datos de Features (JSON dinámico)
  featuresData: { [key: string]: boolean } = {};

  // UI State
  showErrorModal: boolean = false;
  modalMessage: string = '';

  modules = ['CIUDADES', 'USERS', 'PLACES', 'FAVORITES'];
  levels = ['INFO', 'WARN', 'ERROR', 'DEBUG'];

  filterModule: string = '';
  filterLevel: string = '';
  startDate: string = '';
  endDate: string = '';

  ngOnInit(): void {
    this.setDefaultDates();
    this.loadAllLogs();
    this.loadFeatures();
  }

  // Carga inicial de Toggles desde el JSON del Backend
  loadFeatures(): void {
    this.featureService.getFeatures().subscribe({
      next: (data) => this.featuresData = data,
      error: (err) => console.error('Error cargando features dinámicos', err)
    });
  }

  // Cambiar estado de un feature y guardar en el JSON
  toggleFeature(featureKey: string): void {
    this.featuresData[featureKey] = !this.featuresData[featureKey];
    
    this.featureService.updateFeatures(this.featuresData).subscribe({
      next: () => console.log(`Feature ${featureKey} actualizado.`),
      error: () => {
        this.modalMessage = 'Error al guardar el cambio en el servidor.';
        this.showErrorModal = true;
        this.featuresData[featureKey] = !this.featuresData[featureKey]; // Revertir
      }
    });
  }

  // Lógica de Logs
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
      this.modalMessage = CONSTANTS.MESSAGES.ERROR.FILTER_REQUIRED;
      this.showErrorModal = true;
    }
  }

  setDefaultDates(): void {
    const now = new Date();
    const fiveDaysAgo = new Date();
    fiveDaysAgo.setDate(now.getDate() - 5);
    this.endDate = this.formatDateForInput(now);
    this.startDate = this.formatDateForInput(fiveDaysAgo);
  }

  formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  ngOnDestroy(): void {
    if (this.logSub) this.logSub.unsubscribe();
  }
}
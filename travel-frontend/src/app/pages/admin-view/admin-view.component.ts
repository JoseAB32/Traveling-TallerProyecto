import { Component, inject, OnInit, OnDestroy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { HeaderComponent } from '../../components/header/header.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { LoggerService } from '../../services/logger/logger.service';
import { FeatureService, Features } from '../../services/features/feature.service';
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

  logs: Logger[] = [];
  activeTab: string = 'logs';

  // En lugar de featuresData local, exponemos el signal del servicio al template
  readonly featuresData = this.featureService.features;

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

  loadFeatures(): void {
    // loadFeatures() actualiza el signal internamente — no necesitamos guardar nada aquí
    this.featureService.loadFeatures().subscribe({
      error: (err) => console.error('Error cargando features', err)
    });
  }

  toggleFeature(featureKey: keyof Features): void {
    // Construimos el nuevo estado a partir del signal actual
    const updated: Features = {
      ...this.featuresData(),         // leemos el signal con ()
      [featureKey]: !this.featuresData()[featureKey]
    };

    this.featureService.updateFeatures(updated).subscribe({
      next: () => console.log(`Feature '${featureKey}' actualizado.`),
      error: () => {
        this.modalMessage = 'Error al guardar el cambio en el servidor.';
        this.showErrorModal = true;
        // No necesitamos revertir manualmente — el signal no se actualizó
        // porque updateFeatures solo hace set() en el tap() del next
      }
    });
  }

  loadAllLogs(): void {
    this.logSub = this.loggerService.getAllLogs().subscribe({
      next: (data) => this.logs = data.slice(0, 20),
      error: (err) => console.error('Error cargando logs', err)
    });
  }

  applyFilters(): void {
    if (this.filterModule && this.filterLevel && this.startDate && this.endDate) {
      this.loggerService
        .getFilteredLogs(this.filterModule, this.filterLevel, this.startDate, this.endDate)
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
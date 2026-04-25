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
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'app-admin-view',
  standalone: true,
  imports: [CommonModule, FormsModule, HeaderComponent, FooterComponent, TranslocoModule],
  templateUrl: './admin-view.component.html',
  styleUrl: './admin-view.component.css'
})
export class AdminViewComponent implements OnInit, OnDestroy {
  private loggerService = inject(LoggerService);
  private featureService = inject(FeatureService);
  private translocoService = inject(TranslocoService);
  private logSub!: Subscription;

  logs: Logger[] = [];
  activeTab: string = 'logs';

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
    // loadFeatures() actualiza el signal internamente 
    this.featureService.loadFeatures().subscribe({
      error: (err) => console.error('Error cargando features', err)
    });
  }

  toggleFeature(featureKey: keyof Features): void {
    // Construimos el nuevo estado a partir del signal actual
    const updated: Features = {
      ...this.featuresData(),        
      [featureKey]: !this.featuresData()[featureKey]
    };

    this.featureService.updateFeatures(updated).subscribe({
      error: () => {
        this.modalMessage = this.translocoService.translate('adminConfiguration.modal.textErrorServer');
        console.log(this.modalMessage);
        this.showErrorModal = true;
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
      this.modalMessage = this.translocoService.translate('adminConfiguration.modal.textErrorFilter');
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
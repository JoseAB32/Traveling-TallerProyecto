import { 
  Component, Input, AfterViewInit, Output, EventEmitter, 
  OnChanges, SimpleChanges, OnDestroy, inject 
} from '@angular/core';
import * as L from 'leaflet';
import { FeatureService } from '../../services/features/feature.service';

// Fix Leaflet icons
const iconDefault = L.icon({
  iconRetinaUrl: 'marker-icon-2x.png',
  iconUrl:       'marker-icon.png',
  shadowUrl:     'marker-shadow.png',
  iconSize:    [25, 41],
  iconAnchor:  [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize:  [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [],
  templateUrl: './map.component.html',
  styleUrl: './map.component.css'
})
export class MapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() places: any[] = []; // Lista de los lugares
  @Output() placeSelected = new EventEmitter<any>(); // Evento para enviar el lugar seleccionado al componente padre
  @Output() placeClicked = new EventEmitter<any>();

  private map: L.Map | null = null;
  private markerLayer = L.layerGroup();

  private featureService = inject(FeatureService);

  ngAfterViewInit(): void {
    this.initMap();
    this.renderMarkers();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['places'] && this.map) {
      this.renderMarkers();
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  private initMap(): void {
    this.map = L.map('map');
    this.markerLayer.addTo(this.map);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    this.map.setView([-17.3895, -66.1568], 13);
  }

  private renderMarkers(): void {
    if (!this.map) return;

    this.markerLayer.clearLayers();
    const bounds: L.LatLngTuple[] = [];

    if (!this.places?.length) {
      this.map.setView([-17.3895, -66.1568], 13);
      return;
    }

    this.places.forEach(place => {
      if (!place.latitude || !place.longitude) return;

      const latLng: L.LatLngTuple = [place.latitude, place.longitude];
      const marker = L.marker(latLng).addTo(this.markerLayer);

      marker.bindPopup(`
        <b>${place.name}</b><br>
        ${place.address}<br>
        ⭐ ${place.rating}
      `);

      marker.on('mouseover', () => {
        marker.openPopup();
        this.placeSelected.emit(place);
      });

      marker.on('mouseout', () => {
        marker.closePopup();
        this.placeSelected.emit(null);
      });

      marker.on('click', () => {
        if (this.featureService.isEnabled('pinRedirection')) {
          this.placeClicked.emit(place);
        }
      });

      bounds.push(latLng);
    });

    if (bounds.length > 0) {
      this.map.fitBounds(bounds);
    }
  }
}
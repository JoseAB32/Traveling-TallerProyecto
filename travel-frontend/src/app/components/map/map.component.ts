import { Component, Input, AfterViewInit, Output, EventEmitter, OnChanges, SimpleChanges, OnDestroy, inject, ElementRef, ViewChild } from '@angular/core';
import * as L from 'leaflet';
import { FeatureService } from '../../services/features/feature.service';
//Para que arregle Leaflet
const iconRetinaUrl = 'marker-icon-2x.png';
const iconUrl = 'marker-icon.png';
const shadowUrl = 'marker-shadow.png';
const iconDefault = L.icon({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
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
  @ViewChild('mapContainer') mapContainer!: ElementRef<HTMLDivElement>;
  @Input() places: any[] = []; // Lista de los lugares
  // @Input() routePlaces: any[] = [];
  @Input() routeCoordinates: L.LatLngTuple[] = [];
  @Input() showRoute = false;
  @Output() placeSelected = new EventEmitter<any>(); // Evento para enviar el lugar seleccionado al componente padre
  @Output() placeClicked = new EventEmitter<any>();
  private map: L.Map | null = null;
  private markerLayer = L.layerGroup();
  private routeLayer = L.layerGroup();

  featureService = inject(FeatureService);
  features: any = {};

  ngAfterViewInit(): void {
    this.initMap();
    this.featureService.getFeatures().subscribe((data: any) => this.features = data);
    this.renderMarkers();
    this.renderRoute();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      this.map &&
      (
        changes['places'] ||
        // changes['routePlaces'] ||
        changes['routeCoordinates'] ||
        changes['showRoute']
      )
    ) {
      this.renderMarkers();
      this.renderRoute();
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  private initMap(): void {
    // Inicializa el mapa
    // this.map = L.map('map');
    this.map = L.map(this.mapContainer.nativeElement);
    this.markerLayer.addTo(this.map);
    this.routeLayer.addTo(this.map);

    // Añade la capa de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Coordenadas por defecto: Cochabamba, Bolivia
    this.map.setView([-17.3895, -66.1568], 13);
  }

  private renderMarkers(): void {
    if (!this.map) {
      return;
    }

    this.markerLayer.clearLayers();
    const bounds: L.LatLngTuple[] = [];

    if (!this.places || this.places.length === 0) {
      this.map.setView([-17.3895, -66.1568], 13);
      return;
    }

    this.places.forEach(place => {
      if (!place.latitude || !place.longitude) {
        return;
      }

      const latLng: L.LatLngTuple = [place.latitude, place.longitude];
      const marker = L.marker(latLng).addTo(this.markerLayer);

      const popupContent = `
        <b>${place.name}</b><br>
        ${place.address}<br>
        ⭐ ${place.rating}
      `;

      marker.bindPopup(popupContent);

      marker.on('mouseover', () => {
        marker.openPopup();
        this.placeSelected.emit(place);
      });

      marker.on('mouseout', () => {
        marker.closePopup();
        this.placeSelected.emit(null);
      });

      marker.on('click', () => {
        this.placeClicked.emit(place);
      });

      bounds.push(latLng);
    });

    if (bounds.length > 0) {
      this.map.fitBounds(bounds);
    }
  }

  private renderRoute(): void {
    if (!this.map) {
      return;
    }

    this.routeLayer.clearLayers();

    if (!this.showRoute || !this.routeCoordinates || this.routeCoordinates.length < 2) {
      return;
    }

    const routeLine = L.polyline(this.routeCoordinates, {
      color: '#FF6B35',
      weight: 5,
      opacity: 0.9
    });

    routeLine.addTo(this.routeLayer);

    this.map.fitBounds(routeLine.getBounds(), {
      padding: [40, 40]
    });
  }
}

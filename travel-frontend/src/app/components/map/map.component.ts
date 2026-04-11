import { Component, Input, AfterViewInit, Output, EventEmitter } from '@angular/core';
import * as L from 'leaflet';
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
export class MapComponent implements AfterViewInit {
  @Input() places: any[] = []; // Lista de los lugares
  @Output() placeSelected = new EventEmitter<any>(); // Evento para enviar el lugar seleccionado al componente padre
  @Output() placeClicked = new EventEmitter<any>();
  private map: any;

  ngAfterViewInit(): void {
    this.initMap();
  }

  private initMap(): void {
    // Inicializa el mapa
    this.map = L.map('map');

    // Añade la capa de OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    // Si hay lugares, se ponen los pines
    if (this.places && this.places.length > 0) {
      const bounds: L.LatLngTuple[] = []; // Para centrar el mapa automáticamente

      this.places.forEach(place => {
        if (place.latitude && place.longitude) {
          const latLng: L.LatLngTuple = [place.latitude, place.longitude];
          
          // Crea el pin y el popup con la información
          const marker = L.marker(latLng).addTo(this.map);

          // 3. Prepara el contenido del Popup
          const popupContent = `
            <b>${place.name}</b><br>
            ${place.address}<br>
            ⭐ ${place.rating}
          `;

          // 4. Bindea el popup pero no dejes que el clic lo maneje solo
          marker.bindPopup(popupContent);

          // 5. Agrega los eventos de HOVER
          marker.on('mouseover', (e) => {
            marker.openPopup(); // 'this' hace referencia al marcador
            this.placeSelected.emit(place);
          });

          marker.on('mouseout', (e) => {
            marker.closePopup();
            this.placeSelected.emit(null);
          });

          marker.on('click', (e) => {
            this.placeClicked.emit(place);
          });

          bounds.push(latLng);
        }
      });

      if (bounds.length > 0) {
        this.map.fitBounds(bounds);
      }
   } else {
      // Coordenadas por defecto: Cochabamba, Bolivia
      this.map.setView([-17.3895, -66.1568], 13);
    }
  }
}

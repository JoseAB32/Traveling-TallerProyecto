import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-place-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './place-card.component.html',
  styleUrl: './place-card.component.css'
})
export class PlaceCardComponent {
  @Input() place: any;
  @Input() isBlue: boolean = true;

  @Output() onRemoveFavorite = new EventEmitter<number>();

  private router = inject(Router);

  triggerRemove() {
    this.onRemoveFavorite.emit();
  }
  goToDetails() {
    if (this.place && this.place.id) {
      //Nota: Esto hay que modificar, no sé cómo es el manejo del lugar real, cuidao se me olvide
      this.router.navigate(['/lugar', this.place.id]); 
    }
  }
}

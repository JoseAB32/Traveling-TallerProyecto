import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

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

  triggerRemove() {
    this.onRemoveFavorite.emit();
  }
}

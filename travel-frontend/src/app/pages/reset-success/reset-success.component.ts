import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-reset-success',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './reset-success.component.html',
  styleUrls: ['./reset-success.component.css']
})
export class ResetSuccessComponent {

  constructor(private router: Router) {}

  goToLanding() {
    this.router.navigate(['/']);
  }
}
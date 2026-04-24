import { Component } from '@angular/core';
import { RouterLink } from "@angular/router";
import { TranslocoModule } from '@jsverse/transloco';

@Component({
  selector: 'app-success-signup',
  standalone: true,
  imports: [RouterLink, TranslocoModule],
  templateUrl: './success-signup.component.html',
  styleUrl: './success-signup.component.css'
})
export class SuccessSignupComponent {

}

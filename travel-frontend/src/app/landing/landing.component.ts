import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HeaderComponent } from '../header/header.component';
import { FooterComponent } from '../footer/footer.component';
import { CONSTANTS } from '../utils/constants';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [HeaderComponent,FooterComponent],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent implements OnInit {
  constants = CONSTANTS;

  constructor (private router: Router) {
  }

  ngOnInit(): void {
      
  }

  irALogIn() {
    this.router.navigate(['/login']);
  }

  irASignUp() {
    this.router.navigate(['/sign-up']);
  }
}

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css'
})
export class LandingComponent implements OnInit {
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

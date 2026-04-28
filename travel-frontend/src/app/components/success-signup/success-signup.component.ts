import { Component, OnInit } from '@angular/core';
import { RouterLink } from "@angular/router";
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'app-success-signup',
  standalone: true,
  imports: [RouterLink, TranslocoModule],
  templateUrl: './success-signup.component.html',
  styleUrl: './success-signup.component.css'
})
export class SuccessSignupComponent implements OnInit {
  constructor(private translocoService: TranslocoService) {}

  ngOnInit(): void {
    const savedLang = localStorage.getItem('lang') || 'es';
    this.translocoService.setActiveLang(savedLang);
  }
}

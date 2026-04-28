import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';

@Component({
  selector: 'app-reset-success',
  standalone: true,
  imports: [RouterModule, TranslocoModule],
  templateUrl: './reset-success.component.html',
  styleUrls: ['./reset-success.component.css']
})
export class ResetSuccessComponent implements OnInit {

  constructor(private translocoService: TranslocoService) {}

  ngOnInit(): void {
    const savedLang = localStorage.getItem('lang') || 'es';
    this.translocoService.setActiveLang(savedLang);
  }
}
import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from "../header/header.component";
import { FooterComponent } from "../footer/footer.component";

@Component({
  selector: 'app-inicio-logueado',
  standalone: true,
  imports: [HeaderComponent, FooterComponent],
  templateUrl: './inicio-logueado.component.html',
  styleUrl: './inicio-logueado.component.css'
})
export class InicioLogueadoComponent{

}

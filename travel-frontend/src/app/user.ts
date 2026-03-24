import { City } from './city';

export class User {
    id: number = 0;
    correo: string = "";
    userName: string = "";
    pass: string = "";
    birthday: string = "";
    city_id: number | null = null;
    city?: City | null = null;
    state: boolean = true;
}

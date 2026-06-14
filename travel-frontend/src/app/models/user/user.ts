import { City } from '../city/city';

export type Role = 'USER' | 'ADMIN' | 'SUPERADMIN';

export class User {
    id: number = 0;
    correo: string = "";
    userName: string = "";
    pass?: string = "";
    birthday: string = "";
    city_id?: number | null = null;
    city?: City | null = null;
    profilePictureUrl?: string = "";
    state: boolean = true;
    role: Role = 'USER';
}
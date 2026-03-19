export class Place {
    id: number = 0;
    name: string = "";
    description: string = "";
    address: string = "";
    rating: number = 0.0;
    city_id: number = 0; // Relación con la tabla cities
    is_event: boolean = false; 
    start_date: string | null = null; // (ISO format apra fechas)
    end_date: string | null = null;
    image_url: string = "";
    state: boolean = true;
}
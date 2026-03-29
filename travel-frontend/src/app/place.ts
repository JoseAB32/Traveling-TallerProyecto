export class Place {
    id: number = 0;
    name: string = "";
    description: string = "";
    address: string = "";
    rating: number = 0.0;
    city_id: number = 0;
    is_event: boolean = false;
    start_date: string | null = null;
    end_date: string | null = null;
    imageUrl: string = "";  // 🔥 OJO: el backend envía "imageUrl" (camelCase)
    state: boolean = true;
    price?: number;
    latitude?: number;
    longitude?: number;
    placeType?: string;
}
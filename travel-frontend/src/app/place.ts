import { City } from './city';

export class Place {
    id: number = 0; 
    name: string = "";
    description: string = "";
    address: string = "";
    
    rating: number = 5.0;
    price: number = 0.0; 
    latitude: number = 0.0;
    longitude: number = 0.0;
    
    place_type: string = ""; 
    
    city: City | null = null; 
    city_id: number = 0;      
    
    is_event: boolean = false;
    
    start_date: string | null = null;
    end_date: string | null = null;
    
    imageUrl: string = "";
    state: boolean = true;
}
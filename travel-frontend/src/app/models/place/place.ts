import { City } from '../city/city';

export interface PlaceImage {
  id: number;
  image_url: string;
  public_id?: string;
  alt_text?: string;
  display_order?: number;
  is_main?: boolean;
}

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
    images: PlaceImage[] = [];
    state: boolean = true;

    // bestReview: Review;
}
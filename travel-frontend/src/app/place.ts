export class Place {
  id: number = 0;
  name: string = "";
  description: string = "";
  address: string = "";
  rating: number = 0;
  price: number = 0;
  latitude: number = 0;
  longitude: number = 0;
  place_type: string = "";
  is_event: boolean = false;
  start_date: string | null = null;
  end_date: string | null = null;
  image_url: string = ""; 
}
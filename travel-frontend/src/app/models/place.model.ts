export interface Place {
  id: number;
  name: string;
  location: string;
  description: string;
  fullDescription?: string;
  rating: number;
  imageUrl: string;
  images?: string[]; // Para el carrusel
  phone?: string;
  price?: number;
  type?: string;
  schedule?: string;
  ministerio?: string;
  quote?: string;
}
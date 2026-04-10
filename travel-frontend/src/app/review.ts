// Si tienes estos modelos ya creados, expórtalos/impórtalos aquí. 
// Si no, puedes dejarlos como 'any' temporalmente.
import { User } from './user'; 
import { Place } from './place';

export interface Review {
  id: number;
  user: User;
  place: Place;
  comment: string;
  
  score?: number; 
  
  createdAt?: string; 
  
  state: boolean;
  replies?: Review[]; 
}
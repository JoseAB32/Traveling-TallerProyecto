import { User } from '../user/user'; 
import { Place } from '../place/place';

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
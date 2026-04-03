export class Logger {
  id?: number;          
  timestamp: string = "";    // Viene como string ISO desde Java
  module: string = "";       // "PLACES", "FAVORITES", etc.
  level: string = "";        // "INFO", "WARN", "ERROR"
  message: string = "";
  userId: number | null = null;
}

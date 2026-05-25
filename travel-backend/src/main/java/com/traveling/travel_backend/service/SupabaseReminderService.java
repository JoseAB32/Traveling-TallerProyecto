package com.traveling.travel_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class SupabaseReminderService {

    @Value("${SUPABASE_URL}")
    private String SUPABASE_URL;

    @Value("${SUPABASE_API_KEY}")  // ← CAMBIADO: antes era SUPABASE_SECRET_KEY
    private String SUPABASE_API_KEY;  // ← CAMBIADO

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public void createReminder(
        String email,
        String userName,
        String tripName,
        String startDate,
        Long tripId
) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate tripDate = LocalDate.parse(startDate, formatter);
        LocalDateTime reminderDate = tripDate.minusDays(1).atTime(8, 0);

        Map<String, Object> body = new HashMap<>();
        body.put("trip_id", tripId);                    // ← AGREGAR ESTA LÍNEA
        body.put("email", email);
        body.put("user_name", userName);
        body.put("trip_name", tripName);
        body.put("trip_link", "http://localhost:4200/trips/" + tripId);
        body.put("start_date", startDate);
        body.put("reminder_date", reminderDate.toString());
        body.put("sent", false);

        String json = mapper.writeValueAsString(body);
        System.out.println("📤 Enviando a Supabase: " + json);

        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/trip_reminders")
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("✅ Recordatorio creado en Supabase para: " + tripName);
                System.out.println("   Trip ID: " + tripId);
                System.out.println("   Fecha viaje: " + startDate);
                System.out.println("   Recordatorio: " + reminderDate);
            } else {
                System.err.println("❌ Error Supabase: " + response.code());
                String errorBody = response.body() != null ? response.body().string() : "null";
                System.err.println("Respuesta: " + errorBody);
            }
        }

    } catch (Exception e) {
        System.err.println("❌ Error creando recordatorio: " + e.getMessage());
        e.printStackTrace();
    }
  }
}
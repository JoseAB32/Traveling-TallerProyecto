import { createClient } from "npm:@supabase/supabase-js";

Deno.serve(async () => {

  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
  );

  const now = new Date();

  const { data, error } = await supabase
    .from("trip_reminders")
    .select("*")
    .eq("sent", false);

  if(error){

    return new Response(
      JSON.stringify(error),
      { status:500 }
    );
  }

  for(const reminder of data ?? []){

    const reminderDate = new Date(
      reminder.reminder_date
    );

    if(reminderDate <= now){

      await fetch(
        "https://api.resend.com/emails",
        {
          method:"POST",

          headers:{
            "Authorization":
            `Bearer ${Deno.env.get("RESEND_API_KEY")}`,

            "Content-Type":"application/json"
          },

          body:JSON.stringify({

            from:"Traveling <onboarding@resend.dev>",

            to: reminder.email,

            subject:"Tu viaje inicia mañana",

            html:`
            <h2>Hola ${reminder.user_name}</h2>

            <p>
              Tu itinerario
              <strong>${reminder.trip_name}</strong>
              inicia mañana.
            </p>

            <a href="${reminder.trip_link}">
              Ver itinerario
            </a>
            `
          })
        }
      );

      await supabase
        .from("trip_reminders")
        .update({
          sent:true
        })
        .eq("id", reminder.id);
    }
  }

  return new Response(
    JSON.stringify({
      success:true
    }),
    { status:200 }
  );
});
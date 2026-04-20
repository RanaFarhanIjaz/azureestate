package com.example.azureestate;

public class SupabaseConfig {
    // Your Supabase Project URL
    public static final String PROJECT_URL = "https://jxqcfjdozymhihekbkla.supabase.co";
    
    // TEMPORARY: Using Service Role Key (bypasses RLS for testing)
    // WARNING: Use only for testing! Switch back to anon key after
    public static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imp4cWNmamRvenltaGloZWtia2xhIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc3NjYxMzM5OSwiZXhwIjoyMDkyMTg5Mzk5fQ.xfPelfTvSc-a--zL8nbtGLtebzeUK64i4sixr1pzi-8";
    
    // Storage bucket name
    public static final String BUCKET_NAME = "ss";
    
    // Derived endpoints
    public static final String STORAGE_URL  = PROJECT_URL + "/storage/v1/object/" + BUCKET_NAME + "/";
    public static final String PUBLIC_URL   = PROJECT_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/";
    public static final String REST_URL     = PROJECT_URL + "/rest/v1/";
    public static final String REALTIME_URL = "wss://" + PROJECT_URL.replace("https://", "") + "/realtime/v1/websocket";
}
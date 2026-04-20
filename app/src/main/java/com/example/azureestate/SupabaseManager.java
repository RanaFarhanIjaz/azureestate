package com.example.azureestate;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Singleton manager for all Supabase REST + Realtime operations.
 *
 * Usage:
 *   SupabaseManager.getInstance(context).fetchListings(callback);
 *   SupabaseManager.getInstance(context).uploadPropertyListing(data, photos, callback);
 */
public class SupabaseManager {

    private static final String TAG = "SupabaseManager";
    private static SupabaseManager instance;

    private final OkHttpClient http;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final Context context;

    // Realtime websocket
    private WebSocket realtimeSocket;
    private RealtimeListener realtimeListener;
    private int heartbeatRef = 0;

    // ── Callbacks ─────────────────────────────────────────────

    public interface ListingsCallback {
        void onSuccess(List<ListingData> listings);
        void onError(String error);
    }

    public interface UploadCallback {
        void onProgress(int photoIndex, int total);
        void onSuccess(String listingId);
        void onError(String error);
    }

    public interface RealtimeListener {
        void onNewListing(ListingData listing);
        void onListingUpdated(ListingData listing);
    }

    // ─────────────────────────────────────────────────────────

    private SupabaseManager(Context ctx) {
        this.context     = ctx.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executor    = Executors.newFixedThreadPool(3);
        this.http = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized SupabaseManager getInstance(Context ctx) {
        if (instance == null) instance = new SupabaseManager(ctx);
        return instance;
    }

    // ══════════════════════════════════════════════════════════
    //  1. UPLOAD PROPERTY LISTING
    //     Step A: Upload each photo to Supabase Storage
    //     Step B: Insert listing row into Supabase DB with photo URLs
    // ══════════════════════════════════════════════════════════
    public void uploadPropertyListing(ListingData data, List<Uri> photoUris,
                                      UploadCallback callback) {
        executor.execute(() -> {
            try {
                // A. Upload photos one by one
                List<String> photoUrls = new ArrayList<>();
                for (int i = 0; i < photoUris.size(); i++) {
                    final int idx = i;
                    mainHandler.post(() -> callback.onProgress(idx + 1, photoUris.size()));

                    String url = uploadPhoto(photoUris.get(i), data.ownerId);
                    if (url != null) photoUrls.add(url);
                }
                data.photos = photoUrls;

                // B. Insert listing to Supabase database
                String listingId = insertListing(data);
                if (listingId != null) {
                    mainHandler.post(() -> callback.onSuccess(listingId));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to save listing to database"));
                }

            } catch (Exception e) {
                Log.e(TAG, "Upload error", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    //  Upload single photo → returns public URL or null
    // ─────────────────────────────────────────────────────────
    private String uploadPhoto(Uri uri, String ownerId) {
        try {
            // Read file bytes from URI
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return null;
            byte[] bytes = is.readAllBytes();
            is.close();

            // Detect MIME type
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "image/jpeg";

            // Build storage path
            String ext      = mimeType.contains("png") ? "png" : "jpg";
            String filePath = ownerId + "/" + UUID.randomUUID() + "." + ext;
            String uploadUrl= SupabaseConfig.STORAGE_URL + filePath;

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .addHeader("apikey",        SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                    .addHeader("Content-Type",  mimeType)
                    .addHeader("cache-control", "3600")
                    .addHeader("x-upsert",      "true")
                    .post(RequestBody.create(bytes, MediaType.parse(mimeType)))
                    .build();

            Response response = http.newCall(request).execute();
            if (response.isSuccessful()) {
                // Return the public URL
                return SupabaseConfig.PUBLIC_URL + filePath;
            } else {
                Log.e(TAG, "Photo upload failed: " + response.code()
                        + " " + (response.body() != null ? response.body().string() : ""));
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Photo upload exception", e);
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Insert listing row into Supabase → returns listing id
    // ─────────────────────────────────────────────────────────
    private String insertListing(ListingData data) throws Exception {
        JSONObject body = new JSONObject();
        body.put("owner_id",    data.ownerId);
        body.put("owner_email", data.ownerEmail);
        body.put("owner_phone", data.ownerPhone);
        body.put("title",       data.title);
        body.put("price",       data.price);
        body.put("address",     data.address);
        body.put("category",    data.category);
        body.put("beds",        data.beds);
        body.put("baths",       data.baths);
        body.put("sqft",        data.sqft);
        body.put("garages",     data.garages);
        body.put("description", data.description);
        body.put("badge",       data.badge != null ? data.badge : "");
        body.put("rating",      4.5);
        body.put("status",      "active");

        // Photos as JSON array
        JSONArray photosArr = new JSONArray();
        for (String url : data.photos) photosArr.put(url);
        body.put("photos", photosArr);

        Request request = new Request.Builder()
                .url(SupabaseConfig.REST_URL + "listings")
                .addHeader("apikey",        SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Content-Type",  "application/json")
                .addHeader("Prefer",        "return=representation")
                .post(RequestBody.create(body.toString(),
                        MediaType.parse("application/json")))
                .build();

        Response response = http.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            String respStr = response.body().string();
            JSONArray arr  = new JSONArray(respStr);
            if (arr.length() > 0) {
                return arr.getJSONObject(0).optString("id", null);
            }
        } else {
            Log.e(TAG, "Insert listing failed: " + response.code()
                    + " " + (response.body() != null ? response.body().string() : ""));
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════
    //  2. FETCH LISTINGS (with optional search + filter params)
    // ══════════════════════════════════════════════════════════
    public void fetchListings(FetchParams params, ListingsCallback callback) {
        executor.execute(() -> {
            try {
                StringBuilder url = new StringBuilder(SupabaseConfig.REST_URL + "listings?");
                url.append("status=eq.active");
                url.append("&order=created_at.desc");

                // Category filter
                if (params.category != null && !params.category.equals("All Properties")) {
                    url.append("&category=eq.").append(params.category);
                }

                // Search by title or address (ilike = case insensitive LIKE)
                if (params.query != null && !params.query.isEmpty()) {
                    String q = params.query.replace(" ", "%20");
                    url.append("&or=(title.ilike.*").append(q)
                            .append("*,address.ilike.*").append(q).append("*)");
                }

                // Price range
                if (params.priceMin > 0) {
                    // Strip non-numeric from price and filter
                    // Stored as text so we use Supabase text comparison workaround
                    // Better: store price as bigint — see setup notes
                }

                // Beds filter
                if (params.minBeds > 0) {
                    url.append("&beds=gte.").append(params.minBeds);
                }

                // Limit
                url.append("&limit=").append(params.limit > 0 ? params.limit : 50);

                Request request = new Request.Builder()
                        .url(url.toString())
                        .addHeader("apikey",        SupabaseConfig.ANON_KEY)
                        .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                        .addHeader("Accept",        "application/json")
                        .get()
                        .build();

                Response response = http.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    List<ListingData> listings = parseListings(json);
                    mainHandler.post(() -> callback.onSuccess(listings));
                } else {
                    String err = response.body() != null ? response.body().string() : "HTTP " + response.code();
                    mainHandler.post(() -> callback.onError(err));
                }

            } catch (Exception e) {
                Log.e(TAG, "Fetch error", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // ─────────────────────────────────────────────────────────
    //  Parse JSON array → List<ListingData>
    // ─────────────────────────────────────────────────────────
    private List<ListingData> parseListings(String json) throws JSONException {
        List<ListingData> list = new ArrayList<>();
        JSONArray arr = new JSONArray(json);

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            ListingData d  = new ListingData();

            d.id          = obj.optString("id");
            d.ownerId     = obj.optString("owner_id");
            d.ownerEmail  = obj.optString("owner_email");
            d.ownerPhone  = obj.optString("owner_phone");
            d.title       = obj.optString("title");
            d.price       = obj.optString("price");
            d.address     = obj.optString("address");
            d.category    = obj.optString("category");
            d.beds        = obj.optInt("beds", 0);
            d.baths       = (float) obj.optDouble("baths", 0);
            d.sqft        = obj.optString("sqft");
            d.garages     = obj.optInt("garages", 0);
            d.description = obj.optString("description");
            d.badge       = obj.optString("badge", "");
            d.rating      = (float) obj.optDouble("rating", 4.5);
            d.status      = obj.optString("status");
            d.createdAt   = obj.optString("created_at");

            // Photos array
            d.photos = new ArrayList<>();
            JSONArray photos = obj.optJSONArray("photos");
            if (photos != null) {
                for (int j = 0; j < photos.length(); j++) {
                    d.photos.add(photos.getString(j));
                }
            }

            list.add(d);
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════
    //  3. REALTIME — WebSocket subscription to listings table
    //     Supabase Realtime protocol (Phoenix channels)
    // ══════════════════════════════════════════════════════════
    public void subscribeToListings(RealtimeListener listener) {
        this.realtimeListener = listener;

        String wsUrl = "wss://jxqcfjdozymhihekbkla.supabase.co/realtime/v1/websocket"
                + "?apikey=" + SupabaseConfig.ANON_KEY
                + "&vsn=1.0.0";

        Request request = new Request.Builder().url(wsUrl).build();

        realtimeSocket = http.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                Log.d(TAG, "Realtime connected");
                // Send join message to listen to "listings" table changes
                sendJoin(webSocket);
                // Start heartbeat every 30s
                startHeartbeat(webSocket);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Realtime msg: " + text);
                handleRealtimeMessage(text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                Log.e(TAG, "Realtime failure: " + t.getMessage());
                // Reconnect after 5s
                mainHandler.postDelayed(() -> subscribeToListings(realtimeListener), 5000);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "Realtime closed: " + reason);
            }
        });
    }

    private void sendJoin(WebSocket ws) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("topic",   "realtime:public:listings");
            msg.put("event",   "phx_join");
            msg.put("ref",     String.valueOf(++heartbeatRef));

            JSONObject payload = new JSONObject();
            JSONObject config  = new JSONObject();
            JSONObject broadcast   = new JSONObject();
            broadcast.put("self",      false);
            broadcast.put("ack",       false);
            JSONObject presence = new JSONObject();
            presence.put("key", "");
            JSONObject postgres = new JSONObject();
            postgres.put("event", "*");   // INSERT + UPDATE + DELETE

            config.put("broadcast", broadcast);
            config.put("presence",  presence);
            config.put("postgres_changes",
                    new JSONArray().put(new JSONObject()
                            .put("event",  "*")
                            .put("schema", "public")
                            .put("table",  "listings")));

            payload.put("config", config);
            msg.put("payload", payload);

            ws.send(msg.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Join error", e);
        }
    }

    private void startHeartbeat(WebSocket ws) {
        mainHandler.postDelayed(new Runnable() {
            @Override public void run() {
                if (realtimeSocket != null) {
                    try {
                        JSONObject hb = new JSONObject();
                        hb.put("topic",   "phoenix");
                        hb.put("event",   "heartbeat");
                        hb.put("payload", new JSONObject());
                        hb.put("ref",     String.valueOf(++heartbeatRef));
                        ws.send(hb.toString());
                    } catch (JSONException ignored) {}
                    mainHandler.postDelayed(this, 30_000);
                }
            }
        }, 30_000);
    }

    private void handleRealtimeMessage(String text) {
        try {
            JSONObject msg = new JSONObject(text);
            String event   = msg.optString("event");

            if ("postgres_changes".equals(event) || "INSERT".equals(event)
                    || "UPDATE".equals(event)) {

                JSONObject payload = msg.optJSONObject("payload");
                if (payload == null) return;

                JSONObject record = payload.optJSONObject("record");
                if (record == null) record = payload.optJSONObject("new");
                if (record == null) return;

                ListingData d  = new ListingData();
                d.id          = record.optString("id");
                d.ownerId     = record.optString("owner_id");
                d.ownerPhone  = record.optString("owner_phone");
                d.title       = record.optString("title");
                d.price       = record.optString("price");
                d.address     = record.optString("address");
                d.category    = record.optString("category");
                d.beds        = record.optInt("beds", 0);
                d.baths       = (float) record.optDouble("baths", 0);
                d.sqft        = record.optString("sqft");
                d.garages     = record.optInt("garages", 0);
                d.description = record.optString("description");
                d.badge       = record.optString("badge", "");
                d.rating      = (float) record.optDouble("rating", 4.5);

                d.photos = new ArrayList<>();
                JSONArray photos = record.optJSONArray("photos");
                if (photos != null) {
                    for (int j = 0; j < photos.length(); j++) {
                        d.photos.add(photos.getString(j));
                    }
                }

                String eventType = payload.optString("eventType", event);
                ListingData finalD = d;

                mainHandler.post(() -> {
                    if (realtimeListener != null) {
                        if ("INSERT".equals(eventType)) {
                            realtimeListener.onNewListing(finalD);
                        } else {
                            realtimeListener.onListingUpdated(finalD);
                        }
                    }
                });
            }

        } catch (JSONException e) {
            Log.e(TAG, "Realtime parse error", e);
        }
    }

    public void unsubscribe() {
        if (realtimeSocket != null) {
            realtimeSocket.close(1000, "Fragment destroyed");
            realtimeSocket = null;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Data Models
    // ══════════════════════════════════════════════════════════

    public static class ListingData {
        public String       id;
        public String       ownerId;
        public String       ownerEmail;
        public String       ownerPhone;
        public String       title;
        public String       price;        // stored as text e.g. "2500000"
        public String       address;
        public String       category;
        public int          beds;
        public float        baths;
        public String       sqft;
        public int          garages;
        public String       description;
        public String       badge;
        public float        rating;
        public String       status;
        public String       createdAt;
        public List<String> photos = new ArrayList<>();

        /** Convert to display price format */
        public String getFormattedPrice() {
            try {
                long val = Long.parseLong(price.replaceAll("[^0-9]", ""));
                return String.format(java.util.Locale.US, "$%,d", val);
            } catch (Exception e) { return "$" + price; }
        }

        /** First photo URL, or null */
        public String getCoverPhotoUrl() {
            return (photos != null && !photos.isEmpty()) ? photos.get(0) : null;
        }
    }

    public static class FetchParams {
        public String query;
        public String category;
        public float  priceMin;
        public float  priceMax;
        public float  areaMin;
        public float  areaMax;
        public int    minBeds;
        public String sortBy = "created_at.desc";
        public int    limit  = 50;
    }
}
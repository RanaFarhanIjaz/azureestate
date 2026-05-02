package com.example.azureestate;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.azureestate.models.StockModel;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockMarketManager {
    private static final String TAG = "StockMarketManager";
    private static StockMarketManager instance;
    private final OkHttpClient client;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private static final String API_KEY = "d7nmn61r01qm36371nh0d7nmn61r01qm36371nhg";
    
    private final String[] topSymbols = {"O", "PLD", "AMT", "SPG", "PSA", "Z", "CBRE"};
    private final String[] companyNames = {"Realty Income", "Prologis", "American Tower", "Simon Property", "Public Storage", "Zillow Group", "CBRE Group"};

    private StockMarketManager() {
        client = new OkHttpClient();
        executor = Executors.newFixedThreadPool(4);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized StockMarketManager getInstance() {
        if (instance == null) {
            instance = new StockMarketManager();
        }
        return instance;
    }

    public interface StockCallback {
        void onSuccess(List<StockModel> stocks);
        void onError(String error);
    }

    public void fetchTopStocks(StockCallback callback) {
        executor.execute(() -> {
            List<StockModel> stocks = new ArrayList<>();
            try {
                for (int i = 0; i < topSymbols.length; i++) {
                    String symbol = topSymbols[i];
                    String name = companyNames[i];
                    
                    // Fetch Price & Change
                    StockData data = fetchQuote(symbol);
                    // Fetch Market Cap
                    String marketCap = fetchMarketCap(symbol);
                    
                    stocks.add(new StockModel(
                        symbol,
                        name,
                        data.price,
                        data.change,
                        data.changePercent,
                        marketCap
                    ));
                }
                
                mainHandler.post(() -> callback.onSuccess(stocks));
            } catch (Exception e) {
                Log.e(TAG, "Error fetching stocks", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private StockData fetchQuote(String symbol) throws IOException {
        String url = "https://finnhub.io/api/v1/quote?symbol=" + symbol + "&token=" + API_KEY;
        Request request = new Request.Builder().url(url).build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            
            String jsonData = response.body().string();
            JSONObject json = new JSONObject(jsonData);
            
            double price = json.optDouble("c", 0.0);
            double change = json.optDouble("d", 0.0);
            double changePercent = json.optDouble("dp", 0.0);
            
            return new StockData(price, change, changePercent);
        } catch (Exception e) {
            return new StockData(0, 0, 0);
        }
    }

    private String fetchMarketCap(String symbol) {
        String url = "https://finnhub.io/api/v1/stock/profile2?symbol=" + symbol + "&token=" + API_KEY;
        Request request = new Request.Builder().url(url).build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) return "N/A";
            
            String jsonData = response.body().string();
            JSONObject json = new JSONObject(jsonData);
            
            double mc = json.optDouble("marketCapitalization", 0.0);
            if (mc >= 1000000) {
                return String.format("%.2fT", mc / 1000000.0);
            } else if (mc >= 1000) {
                return String.format("%.2fB", mc / 1000.0);
            } else {
                return String.format("%.2fM", mc);
            }
        } catch (Exception e) {
            return "N/A";
        }
    }

    private static class StockData {
        double price;
        double change;
        double changePercent;

        StockData(double price, double change, double changePercent) {
            this.price = price;
            this.change = change;
            this.changePercent = changePercent;
        }
    }
}

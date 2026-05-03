package com.example.azureestate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.azureestate.R;
import com.example.azureestate.models.StockModel;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private final List<StockModel> stocks;

    public StockAdapter(List<StockModel> stocks) {
        this.stocks = stocks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockModel stock = stocks.get(position);
        holder.tvSymbol.setText(stock.getSymbol());
        holder.tvName.setText(stock.getName());
        holder.tvPrice.setText(String.format(Locale.US, "$%.2f", stock.getPrice()));
        
        double change = stock.getChangePercent();
        String changeText = String.format(Locale.US, "%s%.2f%%", change >= 0 ? "+" : "", change);
        holder.tvChange.setText(changeText);
        holder.tvChange.setTextColor(change >= 0 ? 0xFF3DB8A8 : 0xFFE63946);
        
        holder.tvMarketCap.setText("Cap: " + stock.getMarketCap());
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol, tvName, tvPrice, tvChange, tvMarketCap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymbol = itemView.findViewById(R.id.tvStockSymbol);
            tvName = itemView.findViewById(R.id.tvStockName);
            tvPrice = itemView.findViewById(R.id.tvStockPrice);
            tvChange = itemView.findViewById(R.id.tvStockChange);
            tvMarketCap = itemView.findViewById(R.id.tvMarketCap);
        }
    }
}


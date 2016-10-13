package com.logan19gp.search.Utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.logan19gp.search.R;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    List<Product> products = new ArrayList<>();

    public void addProducts(List<Product> newProducts) {
        products.addAll(newProducts);
        notifyDataSetChanged();
    }

    public void clearProducts() {
        products.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Product product = products.get(position);
        holder.titleText.setText(product.getDescription());
        holder.descriptionText.setText(product.getBrand() + " - " + product.getManufacturer());
        holder.upcText.setText(product.getId() + " - " + product.getUpc());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView upcText;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_card, parent, false));

            titleText = (TextView) itemView.findViewById(R.id.title_text);
            descriptionText = (TextView) itemView.findViewById(R.id.brand);
            upcText = (TextView) itemView.findViewById(R.id.upc_text);
        }
    }
}

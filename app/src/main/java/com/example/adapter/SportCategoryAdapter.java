package com.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.item.ItemSportCategory;
import com.example.util.GradientGenerator;
import com.example.util.PopUpAds;
import com.example.util.RvOnClickListener;
import com.example.videostreamingapp.R;

import java.util.ArrayList;

public class SportCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemSportCategory> dataList;
    private final Context mContext;
    private RvOnClickListener clickListener;
    private int row_index = -1;

    public SportCategoryAdapter(Context context, ArrayList<ItemSportCategory> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_language_genre_item_new, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemRowHolder holder = (ItemRowHolder) viewHolder;
        final ItemSportCategory singleItem = dataList.get(position);
        holder.text.setText(singleItem.getCategoryName());

        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpAds.showInterstitialAds(mContext, holder.getBindingAdapterPosition(), clickListener);
            }
        });
        holder.gradientLayout.setBackground(GradientGenerator.build(mContext, position));
        if (row_index > -1) {
            if (row_index == position) {
                holder.selectLayout.setBackgroundResource(R.drawable.image_genre_select_bg);
            } else {
                holder.selectLayout.setBackgroundResource(R.drawable.image_genre_bg);
            }

        }

    }

    public void select(int position) {
        row_index = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    static class ItemRowHolder extends RecyclerView.ViewHolder {
        TextView text;
        RelativeLayout rootLayout, selectLayout, gradientLayout;

        ItemRowHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            rootLayout = itemView.findViewById(R.id.rootLayout);
            selectLayout = itemView.findViewById(R.id.lytSelect);
            gradientLayout = itemView.findViewById(R.id.lytGradient);
        }
    }

}
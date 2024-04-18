package com.example.adapter;

import static android.content.Context.UI_MODE_SERVICE;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.item.ItemHome;
import com.example.item.ItemHomeContent;
import com.example.util.LinearLayoutPagerManager;
import com.example.util.RvOnClickListener;
import com.example.videostreamingapp.MovieDetailsActivity;
import com.example.videostreamingapp.R;
import com.example.videostreamingapp.ShowDetailsActivity;
import com.example.videostreamingapp.SportDetailsActivity;
import com.example.videostreamingapp.TVDetailsActivity;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemHome> dataList;
    private final Context mContext;
    private RvOnClickListener clickListener;

    public HomeAdapter(Context context, ArrayList<ItemHome> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vMovie = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_row_item, parent, false);
        return new ItemRowHolder(vMovie);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemRowHolder holder = (ItemRowHolder) viewHolder;
        final ItemHome singleItem = dataList.get(position);

        holder.tvHomeTitle.setText(singleItem.getHomeTitle());

        holder.rvContent.setHasFixedSize(true);
        if(isAndroidTV()){
            holder.rvContent.setLayoutManager(new GridLayoutManager(mContext,4));
        }else {
            holder.rvContent.setLayoutManager(new LinearLayoutPagerManager(mContext, LinearLayoutManager.HORIZONTAL, false,
                    singleItem.getHomeType().equals("Movie") ? 3.3 : 2.15));
        }
        holder.rvContent.setNestedScrollingEnabled(false);
        HomeContentAdapter homeContentAdapter = new HomeContentAdapter(mContext, singleItem.getItemHomeContents(), false);
        holder.rvContent.setAdapter(homeContentAdapter);
        homeContentAdapter.setOnItemClickListener(new RvOnClickListener() {
            @Override
            public void onItemClick(int position) {
                ItemHomeContent itemHomeContent = singleItem.getItemHomeContents().get(position);
                Class<?> aClass;
                Intent intent = new Intent();
                switch (itemHomeContent.getVideoType()) {
                    case "Movie":
                    default:
                        aClass = MovieDetailsActivity.class;
                        break;
                    case "Shows":
                        aClass = ShowDetailsActivity.class;
                        if (itemHomeContent.getHomeType().equals("Recent")) {
                            intent.putExtra("episodeRedirect", true);
                            intent.putExtra("episodeId", itemHomeContent.getEpisodeId());
                            intent.putExtra("seasonId", itemHomeContent.getSeasonId());
                        }
                        break;
                    case "Sports":
                        aClass = SportDetailsActivity.class;
                        break;
                    case "LiveTV":
                        aClass = TVDetailsActivity.class;
                        break;
                }
                intent.setClass(mContext, aClass);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Id", itemHomeContent.getVideoId());
                mContext.startActivity(intent);

            }
        });
        holder.tvHomeTitleViewAll.setVisibility(singleItem.getHomeId().equals("-1") ? View.GONE : View.VISIBLE);
        holder.tvHomeTitleViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClick(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    static class ItemRowHolder extends RecyclerView.ViewHolder {
        TextView tvHomeTitle, tvHomeTitleViewAll;
        RecyclerView rvContent;

        ItemRowHolder(View itemView) {
            super(itemView);
            tvHomeTitle = itemView.findViewById(R.id.tvHomeTitle);
            tvHomeTitleViewAll = itemView.findViewById(R.id.tvHomeTitleViewAll);
            rvContent = itemView.findViewById(R.id.rv_content);
        }
    }

    private boolean isAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) mContext.getSystemService(UI_MODE_SERVICE);
        return uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

}



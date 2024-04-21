package com.example.item;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ItemTV implements Parcelable {
    private String tvId;
    private String tvName;
    private String tvImage;
    private String tvDescription;
    private String tvType;
    private String tvURL;
    private String tvURL2;
    private String tvURL3;
    private String tvCategory;
    private boolean isPremium = false;
    private String tvView;
    private String tvShareLink;

    protected ItemTV(Parcel in) {
        tvId = in.readString();
        tvName = in.readString();
        tvImage = in.readString();
        tvDescription = in.readString();
        tvType = in.readString();
        tvURL = in.readString();
        tvURL2 = in.readString();
        tvURL3 = in.readString();
        tvCategory = in.readString();
        isPremium = in.readByte() != 0;
        tvView = in.readString();
        tvShareLink = in.readString();
    }

    public ItemTV() {
        // You can initialize default values here if needed
    }

    public static final Creator<ItemTV> CREATOR = new Creator<ItemTV>() {
        @Override
        public ItemTV createFromParcel(Parcel in) {
            return new ItemTV(in);
        }

        @Override
        public ItemTV[] newArray(int size) {
            return new ItemTV[size];
        }
    };

    public String getTvId() {
        return tvId;
    }

    public void setTvId(String tvId) {
        this.tvId = tvId;
    }

    public String getTvName() {
        return tvName;
    }

    public void setTvName(String tvName) {
        this.tvName = tvName;
    }

    public String getTvImage() {
        return tvImage;
    }

    public void setTvImage(String tvImage) {
        this.tvImage = tvImage;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }


    public String getTvDescription() {
        return tvDescription;
    }

    public void setTvDescription(String tvDescription) {
        this.tvDescription = tvDescription;
    }

    public String getTvType() {
        return tvType;
    }

    public void setTvType(String tvType) {
        this.tvType = tvType;
    }

    public String getTvURL() {
        return tvURL;
    }

    public void setTvURL(String tvURL) {
        this.tvURL = tvURL;
    }

    public String getTvCategory() {
        return tvCategory;
    }

    public void setTvCategory(String tvCategory) {
        this.tvCategory = tvCategory;
    }

    public String getTvView() {
        return tvView;
    }

    public void setTvView(String tvView) {
        this.tvView = tvView;
    }

    public String getTvShareLink() {
        return tvShareLink;
    }

    public void setTvShareLink(String tvShareLink) {
        this.tvShareLink = tvShareLink;
    }

    public String getTvURL2() {
        return tvURL2;
    }

    public void setTvURL2(String tvURL2) {
        this.tvURL2 = tvURL2;
    }

    public String getTvURL3() {
        return tvURL3;
    }

    public void setTvURL3(String tvURL3) {
        this.tvURL3 = tvURL3;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(tvId);
        dest.writeString(tvName);
        dest.writeString(tvImage);
        dest.writeString(tvDescription);
        dest.writeString(tvType);
        dest.writeString(tvURL);
        dest.writeString(tvURL2);
        dest.writeString(tvURL3);
        dest.writeString(tvCategory);
        dest.writeByte((byte) (isPremium ? 1 : 0));
        dest.writeString(tvView);
        dest.writeString(tvShareLink);
    }
}

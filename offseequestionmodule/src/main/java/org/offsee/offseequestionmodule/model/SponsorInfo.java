package org.offsee.offseequestionmodule.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hamed on 3/30/2017.
 */

public class SponsorInfo implements Parcelable {
    public int id;
    public String name;
    public String description;
    public String siteUrl;
    public String imageUrl;

    public SponsorInfo(){

    }

    protected SponsorInfo(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        siteUrl = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<SponsorInfo> CREATOR = new Creator<SponsorInfo>() {
        @Override
        public SponsorInfo createFromParcel(Parcel in) {
            return new SponsorInfo(in);
        }

        @Override
        public SponsorInfo[] newArray(int size) {
            return new SponsorInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(siteUrl);
        dest.writeString(imageUrl);
    }
}

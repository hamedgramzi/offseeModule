package org.offsee.offseequestionmodule.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hamed on 3/8/2017.
 */

public class Question implements Parcelable {
    public int id;
    public String content;
    public String answer1;
    public String answer2;
    public String answer3;
    public String answer4;
    public int score = 1;
    public int subjectId;
    public boolean isAdvertise;
    public int sponsorId;
    public SponsorInfo sponsorInfo;
    public String imageUrl;

    public Question() {

    }

    public Question(String content, String ans1, String ans2, String ans3, String ans4) {
        this.content = content;
        this.answer1 = ans1;
        this.answer2 = ans2;
        this.answer3 = ans3;
        this.answer4 = ans4;
    }

    protected Question(Parcel in) {
        id = in.readInt();
        content = in.readString();
        answer1 = in.readString();
        answer2 = in.readString();
        answer3 = in.readString();
        answer4 = in.readString();
        score = in.readInt();
        subjectId = in.readInt();
        isAdvertise = in.readByte() != 0;
        sponsorId = in.readInt();
        sponsorInfo = in.readParcelable(SponsorInfo.class.getClassLoader());
        imageUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(content);
        dest.writeString(answer1);
        dest.writeString(answer2);
        dest.writeString(answer3);
        dest.writeString(answer4);
        dest.writeInt(score);
        dest.writeInt(subjectId);
        dest.writeByte((byte) (isAdvertise ? 1 : 0));
        dest.writeInt(sponsorId);
        dest.writeParcelable(sponsorInfo, flags);
        dest.writeString(imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
}

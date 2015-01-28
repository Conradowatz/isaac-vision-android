package de.conradowatz.isaacvision;


import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable{

    private String title;
    private String pickup;
    private String description;
    private String extraInfo;
    private String tags;
    private Boolean specialItem;
    private Bitmap image;
    private String colorID;
    private Float alphabetID;
    private Integer gameID;

    public Item(String title, String pickup, String description, String extraInfo, String tags, Boolean specialItem, Bitmap image, String colorID, Float alphabetID, Integer gameID) {
        this.title = title;
        this.pickup = pickup;
        this.description = description;
        this.extraInfo = extraInfo;
        this.tags = tags;
        this.specialItem = specialItem;
        this.image = image;
        this.colorID = colorID;
        this.alphabetID = alphabetID;
        this.gameID = gameID;
    }

    public Item() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getSpecialItem() {
        return specialItem;
    }

    public void setSpecialItem(Boolean specialItem) {
        this.specialItem = specialItem;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getColorID() {
        return colorID;
    }

    public void setColorID(String colorID) {
        this.colorID = colorID;
    }

    public Float getAlphabetID() {
        return alphabetID;
    }

    public void setAlphabetID(Float alphabetID) {
        this.alphabetID = alphabetID;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(pickup);
        out.writeString(description);
        out.writeString(extraInfo);
        out.writeString(tags);
        if (specialItem) {
            out.writeInt(1);
        } else {
            out.writeInt(0);
        }
        out.writeParcelable(image, flags);
        out.writeString(colorID);
        out.writeFloat(alphabetID);
        out.writeInt(gameID);
    }

    public void readFromParcel(Parcel in) {
        title = in.readString();
        pickup = in.readString();
        description = in.readString();
        extraInfo = in.readString();
        tags = in.readString();
        if (in.readInt()==1) {
            specialItem = true;
        } else {
            specialItem = false;
        }
        image = in.readParcelable(getClass().getClassLoader());
        colorID = in.readString();
        alphabetID = in.readFloat();
        gameID = in.readInt();
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Item createFromParcel(Parcel in) {
                    Item item = new Item();
                    item.readFromParcel(in);
                    return item;
                }

                public Item[] newArray(int size) {
                    return new Item[size];
                }
            };
}
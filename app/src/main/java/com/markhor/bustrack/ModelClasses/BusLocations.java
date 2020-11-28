package com.markhor.bustrack.ModelClasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class BusLocations implements Parcelable {
    private GeoPoint geoPoint;
    private @ServerTimestamp Date timestamp;
    private DriverInformation driverInformation;

    public BusLocations() {
    }

    public BusLocations(GeoPoint geoPoint, Date timestamp, DriverInformation driverInformation) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.driverInformation = driverInformation;
    }

    protected BusLocations(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BusLocations> CREATOR = new Creator<BusLocations>() {
        @Override
        public BusLocations createFromParcel(Parcel in) {
            return new BusLocations(in);
        }

        @Override
        public BusLocations[] newArray(int size) {
            return new BusLocations[size];
        }
    };

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public DriverInformation getDriverInformation() {
        return driverInformation;
    }

    public void setDriverInformation(DriverInformation driverInformation) {
        this.driverInformation = driverInformation;
    }
}

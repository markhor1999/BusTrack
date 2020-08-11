package com.markhor.bustrack.ModelClasses;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class BusLocations {
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

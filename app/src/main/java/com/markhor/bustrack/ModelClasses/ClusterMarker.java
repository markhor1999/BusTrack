package com.markhor.bustrack.ModelClasses;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String title ;
    private String snippet;
    private DriverInformation driverInformation;

    public ClusterMarker() {
    }

    public ClusterMarker(LatLng position, String title, String snippet, DriverInformation driverInformation) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.driverInformation = driverInformation;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public DriverInformation getDriverInformation() {
        return driverInformation;
    }

    public void setDriverInformation(DriverInformation driverInformation) {
        this.driverInformation = driverInformation;
    }
}

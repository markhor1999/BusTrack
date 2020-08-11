package com.markhor.bustrack.Driver;

import android.app.Application;

import com.markhor.bustrack.ModelClasses.DriverInformation;

public class DriverClient extends Application {
    private DriverInformation driver = null;

    public DriverInformation getDriver() {
        return driver;
    }

    public void setDriver(DriverInformation driver) {
        this.driver = driver;
    }
}

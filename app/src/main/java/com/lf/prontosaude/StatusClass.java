package com.lf.prontosaude;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;


/**
 * Created by ferna on 13/10/2016.
 */

public class StatusClass {

    public Activity activity;

    public StatusClass(Activity activity){
        this.activity = activity;
    }
    public Boolean getStatusGPS(){
        Boolean status = false;

        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            status = true;
        }
        return status;
    }

    public Boolean getStatusInternet(){
        Boolean status = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isAvailable() &&
                connectivityManager.getActiveNetworkInfo().isConnected()) {

            status = true;
        }

        return status;
    }

}

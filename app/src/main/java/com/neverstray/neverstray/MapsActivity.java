package com.neverstray.neverstray;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String LOG_TAG = "Google Places";
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapterForSource;
    private PlaceAutocompleteAdapter mAdapterForDestination;
    private Place sourcePlace;
    private Place destinationPlace;
    private Map map;

    private static final LatLngBounds BOUNDS = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(15, 75));

    @Override
    public void onLocationChanged(Location location) {
        Log.i("Location", "Has changed");
        map.markAndZoom(location);
        map.checkIfOnPath(location);
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationRequest mLocationRequest = createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Location", "Connection suspended to google maps");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
        Toast.makeText(this, "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mGoogleApiClient = getGoogleApiClient();
        map = Map.from(R.id.map, this);
        setUpAutocomplete();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    private GoogleApiClient getGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void setUpAutocomplete() {
        AutoCompleteTextView mAutocompleteViewForSource = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewSource);
        mAutocompleteViewForSource.setOnItemClickListener(mAutocompleteClickListenerForSource);

        mAdapterForSource = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS, null);
        mAutocompleteViewForSource.setAdapter(mAdapterForSource);

        AutoCompleteTextView mAutocompleteViewForDestination = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestination);
        mAutocompleteViewForDestination.setOnItemClickListener(mAutocompleteClickListenerForDestination);

        mAdapterForDestination = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS, null);
        mAutocompleteViewForDestination.setAdapter(mAdapterForDestination);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListenerForSource = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAdapterForSource.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallbackForSource);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallbackForDestination = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            destinationPlace = places.get(0);
            updateMap();
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallbackForSource = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            sourcePlace = places.get(0);
            updateMap();
        }
    };

    private AdapterView.OnItemClickListener mAutocompleteClickListenerForDestination = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAdapterForDestination.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallbackForDestination);
        }
    };

    private void updateMap() {
        if (sourcePlace == null || destinationPlace == null) {
            return;
        }

        map.renderRoute(sourcePlace, destinationPlace);
    }
}

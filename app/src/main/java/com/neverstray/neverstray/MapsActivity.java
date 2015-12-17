package com.neverstray.neverstray;

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
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG_TAG = "Google Places";
    protected GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapterForSource;
    private PlaceAutocompleteAdapter mAdapterForDestination;
    private Place sourcePlace;
    private Place destinationPlace;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(15, 75));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        setContentView(R.layout.activity_maps);

        AutoCompleteTextView mAutocompleteViewForSource = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewSource);
        mAutocompleteViewForSource.setOnItemClickListener(mAutocompleteClickListenerForSource);

        mAdapterForSource = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutocompleteViewForSource.setAdapter(mAdapterForSource);

        AutoCompleteTextView mAutocompleteViewForDestination = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewDestination);
        mAutocompleteViewForDestination.setOnItemClickListener(mAutocompleteClickListenerForDestination);

        mAdapterForDestination = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutocompleteViewForDestination.setAdapter(mAdapterForDestination);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListenerForSource = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAdapterForSource.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallbackForSource);
        }
    };

    private AdapterView.OnItemClickListener mAutocompleteClickListenerForDestination = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mAdapterForDestination.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallbackForDestination);
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
            addMarkerOn(sourcePlace);
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
            addMarkerOn(destinationPlace);

            DirectionsRenderer directionsRenderer = new DirectionsRenderer(sourcePlace.getLatLng(), destinationPlace.getLatLng(), getMap());
            directionsRenderer.foo();
        }
    };

    private void addMarkerOn(Place place) {
        LatLng placeLatLong = place.getLatLng();
        GoogleMap mMap = getMap();
        mMap.addMarker(new MarkerOptions().position(placeLatLong));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(placeLatLong));
    };

    private GoogleMap getMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        return mapFragment.getMap();
    };

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}

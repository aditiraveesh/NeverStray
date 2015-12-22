package com.neverstray.neverstray;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Map {
    private GoogleMap googleMap;
    private DirectionsRenderer directionsRenderer;

    protected static Map from(int mapId, FragmentActivity fragmentActivity) {
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentActivity.getSupportFragmentManager().findFragmentById(mapId);
        return new Map(mapFragment.getMap());
    }

    protected Map(GoogleMap map) {
        this.googleMap = map;
        this.directionsRenderer = new DirectionsRenderer(map);
    }

    protected void checkIfOnPath(Location location) {
        LatLng locationLatLng = (new LatLng(location.getLatitude(), location.getLongitude()));
        if (directionsRenderer.isShowingPath()) {
            boolean isOnPath = new PathJSONParser().isLocationOnEdgeOrPath(locationLatLng, directionsRenderer.polylinePoints, false, 1000);
            Log.i("On path", String.valueOf(isOnPath));
        }
    }

    protected void renderRoute(Place source, Place destination) {
        clear();
        directionsRenderer = new DirectionsRenderer(this.googleMap);
        directionsRenderer.renderRoute(source.getLatLng(), destination.getLatLng());
    }

    protected void markAndZoom(Location location) {
        LatLng locationLatLng = (new LatLng(location.getLatitude(), location.getLongitude()));
        this.googleMap.addMarker(new MarkerOptions().position(locationLatLng));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(locationLatLng));
    }

    private void clear() {
        this.googleMap.clear();
    }
}

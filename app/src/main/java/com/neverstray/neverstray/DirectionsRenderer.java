package com.neverstray.neverstray;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsRenderer {
    private GoogleMap googleMap;
    protected ArrayList polylinePoints;

    DirectionsRenderer(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    protected void renderRoute(LatLng origin, LatLng destination) {
        String url = getMapsApiDirectionsUrl(origin, destination);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
        addMarkers(origin, destination);
    }

    private void addMarkers(LatLng origin, LatLng destination) {
        if (googleMap != null) {
            googleMap.addMarker(new MarkerOptions().position(origin));
            googleMap.addMarker(new MarkerOptions().position(destination));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(origin.latitude, origin.longitude), 17));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(origin.latitude, origin.longitude))
                    .zoom(17)
                    .bearing(90)
                    .tilt(40)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private String getMapsApiDirectionsUrl(LatLng origin, LatLng destination) {
        String sensor = "sensor=false";
        String params = sensor + "&origin=" + origin.latitude + "," + origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + params;
    }

    protected boolean isShowingPath() {
        return polylinePoints != null;
    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            PolylineOptions polyLineOptions = new PolylineOptions();

            for (int i = 0; i < routes.size(); i++) {
                polylinePoints = new ArrayList<LatLng>();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    polylinePoints.add(position);
                }

                polyLineOptions.addAll(polylinePoints);
                polyLineOptions.width(8);
                polyLineOptions.color(Color.BLUE);
            }

            googleMap.addPolyline(polyLineOptions);
        }
    }
}

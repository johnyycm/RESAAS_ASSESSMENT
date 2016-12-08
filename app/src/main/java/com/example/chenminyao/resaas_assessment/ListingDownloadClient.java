package com.example.chenminyao.resaas_assessment;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenminyao on 2016-12-07.
 */

public class ListingDownloadClient {
    private static RequestQueue queue;
    private ArrayList<Listing> shopListings;
    private ListingListener mListingListener;
    private Context mContext;


    public ListingDownloadClient(Context context, ListingListener listingListener) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        mContext = context;
        mListingListener = listingListener;

    }

    public void getListings(double lon, double lat){
        final String API_KEY = "465c3516cb028c80c44712539773c6fe";
        String pizzaCuisineId = "82";
        double radius = 100;
//        String url = "https://developers.zomato.com/api/v2.1/search?lat=49.280392&lon=-123.120177&radius=100&cuisines=82";
        String url = "https://developers.zomato.com/api/v2.1/search?";
        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append("lat="+String.valueOf(lat));
        stringBuilder.append("&lon="+lon);
        stringBuilder.append("&radius="+String.valueOf(radius));
        stringBuilder.append("&cuisines="+pizzaCuisineId);
        shopListings = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, stringBuilder.toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("Response", response.toString());
                        try {
                            int resultsFound = response.getInt("results_found");
//                            JSONArray results = response.getJSONArray("restaurants");
//                            Log.d("Response", results.toString());
//                            for (int i = 0; i < results.length(); i++) {
//                                JSONObject restaurantsObject = results.getJSONObject(i);
//                                JSONObject restaurantObject = restaurantsObject.getJSONObject("restaurant");
//                                Listing restaurant = new Listing();
//                                restaurant.setName(restaurantObject.getString("name"));
//                                JSONObject location = restaurantObject.getJSONObject("location");
//                                restaurant.setAddress(location.getString("address"));
//                                JSONObject userRating = restaurantObject.getJSONObject("user_rating");
//                                restaurant.setUserRating(userRating.getString("aggregate_rating"));
//                                shopListings.add(restaurant);
//                            }
                            if (resultsFound>0)
                                mListingListener.downloadFinished(response.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user-key", API_KEY);
                params.put("Accept", "application/json");

                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }
}

package com.example.ulrich.ipmap;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * REST client to interface with Freegeoip.net
 */
public class FreegeoipRestClient implements ILocationRestClient {
    private final Context mContext;
    private final RequestQueue mQueue;

    public FreegeoipRestClient(Context c) {
        // Instantiate the RequestQueue.
        mContext = c;
        mQueue = Volley.newRequestQueue(c);
    }


    @Override
    public void GetLocationByIp(String  ip, final LocationRequestListener callback)
    {
        String url = "http://freegeoip.net/json/" + ip;

        // Request a json response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        if(callback != null && jsonObject != null){
                            try{
                                String reqIp = jsonObject.getString("ip");
                                float lat = jsonObject.getLong("latitude");
                                float lon = jsonObject.getLong("longitude");
                                callback.LocationFound(reqIp, lat, lon);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.ErrorOccured(-2, e.toString());
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if(callback != null) {
                            callback.ErrorOccured(volleyError.networkResponse.statusCode, volleyError.networkResponse.toString());
                        }
                    }
                }
        );
        jsonRequest.setTag(mContext);

        //Add to queue.  Requests are made on the background thread pool.  Results are returned on the main thread.
        mQueue.add(jsonRequest);
    }

    @Override
    public void StopAll() {
        mQueue.cancelAll(mContext);
    }
}

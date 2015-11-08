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

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Created by ulrich on 11/8/15.
 */
public class FreegeoipRestClient implements ILocationRestClient {
    private Context mContext;
    private RequestQueue mQueue;

    //freegeoip.net/{format}/{IP_or_hostname}
    private String API_BASE_URL = "http://freegeoip.net/";

    public FreegeoipRestClient(Context c) {
        // Instantiate the RequestQueue.
        mContext = c;
        mQueue = Volley.newRequestQueue(c);
    }

    /**
     * Get the latitude and longitude for a given ip
     * @param ip
     * @param callback
     */
    @Override
    public void GetLocationByIp(String  ip, final LocationRequestListener callback)
    {
        String url = API_BASE_URL + "json/" + ip;

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
        if (mQueue != null) {
            mQueue.cancelAll(mContext);
        }
    }
}

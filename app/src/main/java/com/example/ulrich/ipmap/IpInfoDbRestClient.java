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
 * Rest client for interfacing with IpInfoDB API.
 */
public class IpInfoDbRestClient implements ILocationRestClient {



    private Context mContext;
    private RequestQueue mQueue;
    private String API_KEY = "30a033154c777cb016afa6227044f57be4bb46e9eedb4086bb7d86414522fed1";
    private String API_BASE_URL = "http://api.ipinfodb.com/v3/ip-city/?key=" + API_KEY;

    public IpInfoDbRestClient(Context c) {
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
    public void GetLocationByIp(String ip, final LocationRequestListener callback)
    {
        String url = API_BASE_URL + "&ip=" + ip + "&format=json";

        // Request a json response from the provided URL.
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    if(callback != null && jsonObject != null){
                        try{
                            String status =jsonObject.getString("statusCode");
                            if(status.equals("OK")){
                                long lon = jsonObject.getLong("longitude");
                                long lat = jsonObject.getLong("latitude");
                                String reqIp = jsonObject.getString("ipAddress");
                                callback.LocationFound(reqIp, lat, lon);
                            } else {
                                if(status != null)
                                    status += ": " + jsonObject.getString("statusMessage");
                                callback.ErrorOccured(-1, status);
                            }
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

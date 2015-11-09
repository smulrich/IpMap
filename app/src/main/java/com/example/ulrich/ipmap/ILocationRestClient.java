package com.example.ulrich.ipmap;

import java.net.Inet4Address;

/**
 * A client which interfaces to a REST API (or some other means) to make the requested calls.
 */
public interface ILocationRestClient {
    public final int ERROR_REQUEST_TOO_LARGE = -100;
    public final int ERROR_REQUEST_INVALID = -101;
    public final int ERROR_CLIENT_NULL = -102;
    public final int ERROR_RESPONSE_STATUS = -200;
    public final int ERROR_RESPONSE_PARSE_FAILURE = -201;


    /**
     * Get the latitude and longitude for a given ip
     * @param ip string representatin of the ip
     * @param callback listener to receivce callbacks for resolved locations or errors
     */
    void GetLocationByIp(String ip, LocationRequestListener callback);

    /**
     * Stops all queued requests from this client.
     */
    void StopAll();
}


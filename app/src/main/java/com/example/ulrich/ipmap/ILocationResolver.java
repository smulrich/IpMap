package com.example.ulrich.ipmap;

import java.net.Inet4Address;

/**
 * Created by ulrich on 11/7/15.
 */
public interface ILocationResolver {
    void GetLocationByIp(Inet4Address ip, LocationRequestListener callback);

    void StopAll();
}


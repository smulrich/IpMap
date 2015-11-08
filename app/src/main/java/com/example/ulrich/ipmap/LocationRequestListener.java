package com.example.ulrich.ipmap;

import java.net.Inet4Address;

public interface LocationRequestListener{
    void LocationFound(Inet4Address ip, float latitude, float longitude);
    void ErrorOccured(int code, String message);
}

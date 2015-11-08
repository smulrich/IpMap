package com.example.ulrich.ipmap;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Handles interaction with the REST client to resolve IP addresses into locations.
 */
public class LocationResolver {
    private final ILocationRestClient mRestClient;

    public LocationResolver(ILocationRestClient restClient){
        mRestClient = restClient;
    }

    private int toHex(byte[] values){
        return values[0] << 24 | values[1] << 16 | values[2] << 8 | values[3] << 0;
    }

    private void setBytes(byte[] target, int value){
        for (int i = 0; i < 4; i++) {
            target[i] = (byte)(value >>> (i * 8));
        }
    }

    private Inet4Address toIp(int value){
        byte[] b = new byte[4];
        String s;
        for (int i = 3; i >= 0; i--) {
            b[3-i] = (byte)(value >>> (i * 8));
        }
        try {
            return (Inet4Address) Inet4Address.getByAddress(b);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Given a range of ip addresses, attempt to resolve each IP in the range to a latitude/longitude.
     * Each time an IP is resolved, the given callback will be executed with the new point.
     * @param start starting IP address (inclusive)
     * @param end ending IP address (inclusive, must be greater than start
     * @param callback
     */
    public void getLocations(final Inet4Address start, final Inet4Address end, final LocationRequestListener callback){
        if(mRestClient == null) return;

//        byte[] startBytes = start.getAddress();
//        byte[] endBytes = end.getAddress();
//        byte[] currentBytes = startBytes;

        Integer startHex = toHex(start.getAddress());
        Integer endHex = toHex(end.getAddress());
        Inet4Address currentIP = start;

        for(Integer hexval = startHex; hexval <= endHex; hexval++){
            currentIP = toIp(hexval);
            if(isPublicIp(currentIP)){
                mRestClient.GetLocationByIp(currentIP.getHostAddress(), callback);
            }
            currentIP = null;
        }
    }

    /**
     * Test is an IP address is publicly visible.
     * @param ip
     * @return
     */
    public static boolean isPublicIp(Inet4Address ip){
        if(ip == null) return false;

        return (!(ip.isLinkLocalAddress() || ip.isSiteLocalAddress() || ip.isLoopbackAddress() || ip.isAnyLocalAddress()
        || ip.isMulticastAddress() || ip.isMCLinkLocal() || ip.isMCNodeLocal() || ip.isMCOrgLocal() || ip.isMCSiteLocal()));
    }
}

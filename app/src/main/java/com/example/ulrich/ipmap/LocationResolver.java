package com.example.ulrich.ipmap;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Handles interaction with the REST client to resolve IP addresses into locations.
 */
public class LocationResolver {
    private final ILocationRestClient mRestClient;
    private int mThrottle = 0;
    private long mMaxQueries = 0;

    public LocationResolver(ILocationRestClient restClient){
        mRestClient = restClient;
    }

    /**
     * Set a minimum time between API calls
     * @param milliseconds min time between calls
     */
    public void setThrottle(int milliseconds){
        mThrottle = milliseconds;
    }

    /**
     * Set maximum number of queries to execute in a given call
     * @param numQueries
     */
    public void setMaxQueries(long numQueries){
        mMaxQueries = numQueries;
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
        mRestClient.StopAll();

        CityIpAddress startIp = new CityIpAddress(start);
        CityIpAddress endIp = new CityIpAddress(end);
        CityIpAddress currentIp = null;
        Inet4Address currentInetAddr;

        int count = 0;
        do{
            if(currentIp == null){
                currentIp = startIp;
            } else {
                currentIp.increment();
            }
            currentInetAddr = currentIp.toInet4Address();
            if(isPublicIp(currentInetAddr)){
                mRestClient.GetLocationByIp(currentIp.toString(), callback);
            }
            //noinspection UnusedAssignment
            currentInetAddr = null;
            count++;
            if(mThrottle > 0) {
                try {
                    Thread.sleep(mThrottle);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        } while(!currentIp.isGreaterOrEqual(endIp) && count <= mMaxQueries);

        if(count >= mMaxQueries && callback != null){
            callback.ErrorOccured(ILocationRestClient.ERROR_REQUEST_TOO_LARGE, "Cannot process more than " + mMaxQueries + " addresses.");
        }
    }

    /**
     * Cancel all queued requests.
     */
    public void CancellAll(){
        if(mRestClient == null) return;
        mRestClient.StopAll();
    }

    /**
     * Test if an IP address is publicly visible.  If the address is private, we can avoid wasting a hit to the API.
     * @param ip
     * @return
     */
    public static boolean isPublicIp(Inet4Address ip) {
        return ip != null &&
                (!(ip.isLinkLocalAddress() || ip.isSiteLocalAddress() || ip.isLoopbackAddress() || ip.isAnyLocalAddress()
                        || ip.isMulticastAddress() || ip.isMCLinkLocal() || ip.isMCNodeLocal() || ip.isMCOrgLocal() || ip.isMCSiteLocal()));
    }

    /**
     * Convenience class for more easily handling ipaddress.
     * For speed, the LSB is set to zero as GPS coords from IpInfoDb API only ever change when a value in top 3 bytes change.
     */
    private class CityIpAddress {
        private long mValue;

        public CityIpAddress(Inet4Address inetip){
            if(inetip != null) {
                mValue = toHex(inetip.getAddress());
            } else {
                mValue = 0;
            }
        }

        private int getOctet(int i) {
            if( i<0 || i>=4 ) throw new IndexOutOfBoundsException();

            return (int) ((mValue >> (i*8)) & 0x00000000000000FFL);
        }

        private long toHex(byte[] values){
            return 0x00000000FFFFF00L & ((long)(values[0] << 24 & 0xFF000000) | (values[1] << 16 & 0x00FF0000) | (values[2] << 8 & 0x0000FF00) | (values[3] & 0x000000FF));
        }

        public void increment() {
            mValue = (mValue + 256) & 0x00000000FFFFFFFFL;
        }

        public Inet4Address toInet4Address(){
            byte[] b = new byte[4];
            for (int i = 3; i >= 0; i--) {
                b[3-i] = (byte)(mValue >>> (i * 8));
            }
            try {
                return (Inet4Address) Inet4Address.getByAddress(b);
            } catch (UnknownHostException e) {
                return null;
            }
        }

        @Override
        public String toString() {
            return String.format("%d.%d.%d.%d",getOctet(3), getOctet(2), getOctet(1), getOctet(0));
        }

        @Override
        public boolean equals(Object obj) {
            if( obj != null && obj instanceof CityIpAddress) {
                return mValue==((CityIpAddress)obj).mValue;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (int)mValue;
        }

        //Since this is a private class, this is faster than implementing Comparable<T>
        public boolean isGreaterOrEqual(CityIpAddress other){
            return other == null || mValue >= other.mValue;
        }
    }
}

package com.example.ulrich.ipmap;

import android.location.Location;
import android.test.AndroidTestCase;

import junit.framework.Assert;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Created by ulrich on 11/8/15.
 */
public class LocationResolverTest extends AndroidTestCase {
    private LocationResolver mResolver;
    private ILocationRestClient mMockClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMockClient = Mockito.mock(ILocationRestClient.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(mMockClient != null)
            Mockito.reset(mMockClient);
        mMockClient = null;
    }

    public void testGetLocationByIp_StartEqualsEnd_CallsRestOnce() throws Exception {
        final String[] ipResult = new String[1];
        final boolean[] callbackHappened = {false};
        final float[] latlong = new float[] {0,0};
        LocationRequestListener testListener = new LocationRequestListener() {
            @Override
            public void LocationFound(String ip, float latitude, float longitude) {
                ipResult[0] = ip;
                callbackHappened[0] = true;
                latlong[0] = latitude;
                latlong[1] = longitude;
            }

            @Override
            public void ErrorOccured(int code, String message) {
                callbackHappened[0] = true;
            }
        };


        String testIPString = "174.100.100.100";
        Inet4Address testIP = (Inet4Address) Inet4Address.getByName(testIPString);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
//                Object[] args = invocationOnMock.getArguments();
//                String iparg = (String) args[0];
//                LocationRequestListener callbackarg = (LocationRequestListener) args[1];
//                callbackarg.LocationFound(iparg, 100, 100);

                return null;
            }
        }).when(mMockClient).GetLocationByIp(anyString(), isA(LocationRequestListener.class));

        mResolver = new LocationResolver(mMockClient);
        mResolver.getLocations(testIP, testIP, testListener);

        Mockito.verify(mMockClient, times(1)).GetLocationByIp(testIPString, testListener);
    }

    public void testGetLocationByIp_StartLessThanEndPositive_CallsRestOverRange() throws Exception {

        LocationRequestListener testListener = new LocationRequestListener() {
            @Override
            public void LocationFound(String ip, float latitude, float longitude) {

            }

            @Override
            public void ErrorOccured(int code, String message) {

            }
        };

        String testStartIPString = "14.100.100.100";
        Inet4Address testStartIP = (Inet4Address) Inet4Address.getByName(testStartIPString);

        String testEndIPString = "14.100.100.102";
        Inet4Address testEndIP = (Inet4Address) Inet4Address.getByName(testEndIPString);

        final ArrayList<String> calledIps = new ArrayList<String>();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                calledIps.add((String)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(mMockClient).GetLocationByIp(anyString(), isA(LocationRequestListener.class));

        mResolver = new LocationResolver(mMockClient);
        mResolver.getLocations(testStartIP, testEndIP, testListener);

        Mockito.verify(mMockClient, times(3)).GetLocationByIp(anyString(), eq(testListener));
    }

    public void testGetLocationByIp_StartLessThanEndNegative_CallsRestOverRange() throws Exception {

        LocationRequestListener testListener = new LocationRequestListener() {
            @Override
            public void LocationFound(String ip, float latitude, float longitude) {

            }

            @Override
            public void ErrorOccured(int code, String message) {

            }
        };

        String testStartIPString = "174.100.100.100";
        Inet4Address testStartIP = (Inet4Address) Inet4Address.getByName(testStartIPString);

        String testEndIPString = "174.100.100.102";
        Inet4Address testEndIP = (Inet4Address) Inet4Address.getByName(testEndIPString);

        final ArrayList<String> calledIps = new ArrayList<String>();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                calledIps.add((String)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(mMockClient).GetLocationByIp(anyString(), isA(LocationRequestListener.class));

        mResolver = new LocationResolver(mMockClient);
        mResolver.getLocations(testStartIP, testEndIP, testListener);

        Mockito.verify(mMockClient, times(3)).GetLocationByIp(anyString(), eq(testListener));
    }

    public void testGetLocationByIp_StartGreaterThanEnd_NoCallbacks() throws Exception {

        LocationRequestListener testListener = new LocationRequestListener() {
            @Override
            public void LocationFound(String ip, float latitude, float longitude) {

            }

            @Override
            public void ErrorOccured(int code, String message) {

            }
        };

        String testStartIPString = "174.100.100.102";
        Inet4Address testStartIP = (Inet4Address) Inet4Address.getByName(testStartIPString);

        String testEndIPString = "174.100.100.100";
        Inet4Address testEndIP = (Inet4Address) Inet4Address.getByName(testEndIPString);

        final ArrayList<String> calledIps = new ArrayList<String>();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                calledIps.add((String)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(mMockClient).GetLocationByIp(anyString(), isA(LocationRequestListener.class));

        mResolver = new LocationResolver(mMockClient);
        mResolver.getLocations(testStartIP, testEndIP, testListener);

        Mockito.verify(mMockClient, times(0)).GetLocationByIp(anyString(), eq(testListener));
    }

    public void testGetLocationByIp_RangeHasPrivateIP_SkipsPrivateIP() throws Exception {

        LocationRequestListener testListener = new LocationRequestListener() {
            @Override
            public void LocationFound(String ip, float latitude, float longitude) {

            }

            @Override
            public void ErrorOccured(int code, String message) {

            }
        };

        String testStartIPString = "192.167.255.254";
        Inet4Address testStartIP = (Inet4Address) Inet4Address.getByName(testStartIPString);

        String testEndIPString = "192.168.0.0";
        Inet4Address testEndIP = (Inet4Address) Inet4Address.getByName(testEndIPString);

        final ArrayList<String> calledIps = new ArrayList<String>();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                calledIps.add((String)invocationOnMock.getArguments()[0]);
                return null;
            }
        }).when(mMockClient).GetLocationByIp(anyString(), isA(LocationRequestListener.class));

        mResolver = new LocationResolver(mMockClient);
        mResolver.getLocations(testStartIP, testEndIP, testListener);

        Mockito.verify(mMockClient, times(2)).GetLocationByIp(anyString(), eq(testListener));
    }
}

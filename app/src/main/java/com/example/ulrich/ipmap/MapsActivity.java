package com.example.ulrich.ipmap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public LocationResolver mLocationResolver;
    private Context mContext;

    //Inpit filter to only allow valid ip as input
    private InputFilter[] mIpInputFilters = new InputFilter[]{
            new InputFilter() {
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    if (end > start) {
                        String destTxt = dest.toString();
                        String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);
                        if (!resultingTxt.matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                            return "";
                        } else {
                            String[] splits = resultingTxt.split("\\.");
                            for (int i = 0; i < splits.length; i++) {
                                if (Integer.valueOf(splits[i]) > 255) {
                                    return "";
                                }
                            }
                        }
                    }
                    return null;
                }
            }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        mLocationResolver = new FreegeoipRestClient(this);
        mLocationResolver = new LocationResolver(new IpInfoDbRestClient(this));
        mContext = this;
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

//        final MenuItem searchMenuItem = menu.findItem(R.id.action_add);
//        if (searchMenuItem != null) {
//            final SearchView searchView = (SearchView) searchMenuItem.getActionView();
//            if (searchView != null) {
//                EditText searchEditTxt = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//                searchEditTxt.setFilters(mIpInputFilters);
//                searchEditTxt.setInputType(InputType.TYPE_CLASS_PHONE);
//                searchEditTxt.setHint(R.string.ip_address_hint);
//
//                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//                    @Override
//                    public boolean onQueryTextSubmit(String s) {
//                        searchView.clearFocus();
//
//                        MenuItemCompat.collapseActionView(searchMenuItem);
//                        Inet4Address address = null;
//                        try {
//                             address = (Inet4Address) Inet4Address.getByName(s);
//                        } catch (UnknownHostException e) {
//                            //Do nothing.  We will know that an error occurred since address will be null
//                        }
//                        if(address == null)
//                            Toast.makeText(mContext, R.string.invalidip, Toast.LENGTH_SHORT).show();
//                        else
//                            addIp(address);
//
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onQueryTextChange(String s) {
//                        return true;
//                    }
//                });
//            }
//        }
        return true;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

    }

    public void addIpInRange(Inet4Address start,Inet4Address end){
        if(mLocationResolver != null)
            mLocationResolver.getLocations(start, end, new LocationRequestListener() {
                @Override
                public void LocationFound(String ip, float latitude, float longitude) {
                    LatLng location = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(ip)
                    );
                    //TODO once start and end results found, animate camera to fill screen.
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
                }

                @Override
                public void ErrorOccured(int code, String message) {
                    Toast.makeText(mContext, String.format("Error(%d):%s", code, message), Toast.LENGTH_SHORT).show();
                }
            });
    }

//    public void addIp(Inet4Address address){
//        if(mLocationResolver != null) {
//            mLocationResolver.GetLocationByIp(address, new LocationRequestListener() {
//                @Override
//                public void LocationFound(Inet4Address resultIp, float latitude, float longitude) {
//                    LatLng location = new LatLng(latitude, longitude);
//                    mMap.addMarker(new MarkerOptions()
//                                    .position(location)
//                                    .title(resultIp.getHostAddress())
//                    );
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
//                }
//
//                @Override
//                public void ErrorOccured(int code, String message) {
//                    Toast.makeText(mContext, String.format("Error(%d):%s", code, message), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
}

package com.example.ulrich.ipmap;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Main activity of this application.
 */
public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    public LocationResolver mLocationResolver;
    private Context mContext;

    //Input filter to only allow valid ip as input
    private final InputFilter[] mIpInputFilters = new InputFilter[]{
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

        mContext = this;
        mLocationResolver = new LocationResolver(new IpInfoDbRestClient(mContext));

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
     * This is where we can add markers or lines, add listeners or move the camera.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(35.875214, -78.840746)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_add:
                showRangeDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showRangeDialog() {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.ipdialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setView(promptsView);

        final EditText editStartIp = (EditText) promptsView.findViewById(R.id.editTextDialogStartIP);
        editStartIp.setFilters(mIpInputFilters);
        editStartIp.setInputType(InputType.TYPE_CLASS_PHONE);
        final EditText editEndIp = (EditText) promptsView.findViewById(R.id.editTextDialogEndIP);
        editEndIp.setFilters(mIpInputFilters);
        editEndIp.setInputType(InputType.TYPE_CLASS_PHONE);

        //Click handlers
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {

                                //Get start and end text and pass to LocationResolver
                                String startip = editStartIp.getText().toString();
                                String endip = editEndIp.getText().toString();
                                addIpInRange(startip, endip);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void addIpInRange(String startIp,String endIp){
        new AddIpRangeTask().execute(new IpRange(startIp, endIp));
    }


    /**
     * AsyncTask for handling the LocationResolver (done in async task to enable throttling)
     */
    private class AddIpRangeTask extends AsyncTask<IpRange, Integer, Void>{

        @Override
        protected Void doInBackground(IpRange... ipRanges) {
            if(mLocationResolver != null) {
                mLocationResolver.CancellAll();
                try {
                    Inet4Address start = (Inet4Address) Inet4Address.getByName(ipRanges[0].start);
                    Inet4Address end = (Inet4Address) Inet4Address.getByName(ipRanges[0].end);
                    mLocationResolver.setThrottle(600);
                    mLocationResolver.getLocations(start, end, mLocationRequestListener);
                } catch (UnknownHostException e) {
                    Toast.makeText(mContext, R.string.invalidip, Toast.LENGTH_SHORT).show();
                    AppLog.e(e, "Invalid ip");
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    /**
     * Simple class for containing the start and end ip as strings.
     */
    private class IpRange {
        public final String start;
        public final String end;
        public IpRange(String start, String end){
            this.start = start;
            this.end = end;
        }
    }

    /**
     * Handles callbacks from LocationResolver
     */
    private final LocationRequestListener mLocationRequestListener = new LocationRequestListener() {
        private int lastErrorCode = 0;
        private boolean isFirst = true;

        @Override
        public void LocationFound(final String ip, float latitude, float longitude) {

            final LatLng location = new LatLng(latitude, longitude);

            // Get a handler that can be used to post to the main thread
            Handler mainHandler = new Handler(mContext.getMainLooper());
            mainHandler.post( new Runnable() {
                                  @Override
                                  public void run() {
                                      mMap.addMarker(new MarkerOptions().position(location).title(ip));
                                      if(isFirst){
                                          mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10.0f));
                                          isFirst = false;
                                      }
                                  }
                              }
            );
        }

        @Override
        public void ErrorOccured(int code, String message) {
            String msg = String.format("%s (%d)", message, code);
            if(code != lastErrorCode) {  //We'll just show the first error for simplicity.
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            }
            lastErrorCode = code;
            AppLog.e(null, msg);
        }
    };
}

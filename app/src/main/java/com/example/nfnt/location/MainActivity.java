package com.example.nfnt.location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements GetAddress.onTaskDone{

    private static final int REQUEST_LOCATION =1 ;

    Button btnFindLoc,btnFindAddress,btnTracking;
    TextView textLocation;
    private AnimatorSet myRotateAnim;
    private Location myLocation;
    private ImageView myAnimImageView;
    private LocationCallback myLocCallback;
    private FusedLocationProviderClient mFusedLocation;
    private boolean mytrackingLoc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAnimImageView = (ImageView) findViewById(R.id.image_map);

        btnFindLoc = (Button) findViewById(R.id.btn_location);
        btnFindAddress = (Button) findViewById(R.id.btn_address);
        textLocation = (TextView) findViewById(R.id.text_location);
        btnTracking = (Button) findViewById(R.id.btn_tracking);
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        myRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this,R.animator.rotate);
        myRotateAnim.setTarget(myAnimImageView);

        myLocCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
            if(mytrackingLoc)
            {
                new GetAddress(MainActivity.this,MainActivity.this).execute(locationResult.getLastLocation());
            }
            }
        };

        btnFindLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getlocation();
            }
        });
        btnFindAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getadress();
            }
        });
        btnTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mytrackingLoc)
                {
                    trackinglocation();
                }
                else
                {
                    stopTracking();
                }
            }
        });

    }

    private void getadress() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        }
        else
        {
            Log.d("GET_PERMISSION","getlocation: permission are granted");
            mFusedLocation.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null)
                    {
                        new GetAddress(MainActivity.this,MainActivity.this).execute(location);
                        Log.d("GET_PERMISSION","founded");
                    }
                    else
                    {
                        textLocation.setText("Your Location Unavailable");
                    }
                }
            });
        }
        textLocation.setText(getString(R.string.address_text,"System Search Your Address", System.currentTimeMillis()));
    }
    private void getlocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
            }
        else
            {
                Log.d("GET_PERMISSION","getlocation: permission are granted");
                mFusedLocation.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null)
                        {
                            myLocation=location;
                            textLocation.setText(getString(R.string.location_text,
                                    myLocation.getLatitude(),
                                    myLocation.getLongitude(),
                                    myLocation.getTime()));
                        }
                        else
                            {
                                textLocation.setText("Your Location Unavailable");
                            }
                    }
                });
            }

    }
    private void trackinglocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        }
        else
        {
            Log.d("GET_PERMISSION","getlocation: permission are granted");
            mFusedLocation.requestLocationUpdates(getLocation(),myLocCallback,null);
            textLocation.setText(getString(R.string.address_text,"Find Your Address", System.currentTimeMillis()));
            mytrackingLoc = true;
            btnTracking.setText("Stop Tracking");
            myRotateAnim.start();
        }

    }
    private  void stopTracking(){
        if (mytrackingLoc)
        {
            mFusedLocation.removeLocationUpdates(myLocCallback);
            mytrackingLoc =false;
            btnTracking.setText("I Track You");
            textLocation.setText("Tracking Di Hentikan");
            myRotateAnim.end();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case REQUEST_LOCATION:
                if(grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED){getlocation();}
                else{
                    Toast.makeText(this,"you dont have permission",Toast.LENGTH_SHORT).show();}
                break;
        }
    }

    @Override
    public void onTaskCompleted(String result) {
        //textLocation.setText(result);
        textLocation.setText(getString(R.string.address_text,result, System.currentTimeMillis()));
    }

    private LocationRequest getLocation()
    {
        LocationRequest locationReq = new LocationRequest();
        locationReq.setInterval(10000);
        locationReq.setFastestInterval(5000);
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationReq;
    }
}

package com.example.nfnt.location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements GetAddress.onTaskDone, GoogleApiClient.OnConnectionFailedListener {

    private static final int GOOGLE_API_CLIENT_ID = 0; //Static var untuk googleApi Client
    private static final int REQUEST_PICK_PLACE = 0; //Static variabel untu intent builder place

    private static String NAME_PLACE ="" ;//static variabel untuk digunakan sebagai instance save agar saat berubah rotasi tidak hilang data sebelumnya
    private static String ADDRESS_PLACE = "";//static variabel untuk digunakan sebagai instance save agar saat berubah rotasi tidak hilang data sebelumnya
    private static int IMG_PLACE=-1;//static variabel untuk digunakan sebagai instance save agar saat berubah rotasi tidak hilang data sebelumnya

    private GoogleApiClient googleApiClient; // object yang digunakan untuk mendapatkan API client

    private static final int REQUEST_LOCATION =1 ;

    Button btnFindLoc,btnFindAddress,btnTracking,btnPicker;
    TextView textLocation;
    private AnimatorSet myRotateAnim;
    private Location myLocation;
    private ImageView myAnimImageView;
    private LocationCallback myLocCallback;
    private FusedLocationProviderClient mFusedLocation;
    private boolean mytrackingLoc;

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) { //function digunakan agar data dari alamat sebelumnya disimpan ke dalaman Save Instance State
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("placeName",NAME_PLACE);
        savedInstanceState.putString("placeAddress",ADDRESS_PLACE);
        savedInstanceState.putInt("placeImage",IMG_PLACE);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) { // function digunakn saat merestore data yang ada dalam Instancestate kedalam object yandg ditentukan
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getString("placeName")=="")
        {
            textLocation.setText("Tekan Button dibawah ini untuk mendapatkan lokasi anda");
        }
        else
            {
                textLocation.setText(getString(R.string.address_text,savedInstanceState.getString("placeName"),savedInstanceState.getString("placeAddress"), System.currentTimeMillis()));
                myAnimImageView.setImageResource(savedInstanceState.getInt("placeImage"));

            }
     }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API).enableAutoManage(this, GOOGLE_API_CLIENT_ID,this)
                .build();//deklarasi GoogleApiClient untuk mendapatkan API client

        myAnimImageView = (ImageView) findViewById(R.id.image_map);

        btnFindLoc = (Button) findViewById(R.id.btn_location);
        btnFindAddress = (Button) findViewById(R.id.btn_address);
        textLocation = (TextView) findViewById(R.id.text_location);
        btnTracking = (Button) findViewById(R.id.btn_tracking);
        btnPicker = (Button) findViewById(R.id.btn_picker);
        btnFindLoc.setVisibility(View.GONE);
        btnFindAddress.setVisibility(View.GONE);
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

        btnPicker.setOnClickListener(new View.OnClickListener() {//function dijalankan untuk Intent builder place picker, intent untuk membuka halaman map berisi tempat tempat nanti akan muncul
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder pickBuild = new PlacePicker.IntentBuilder();//create PlacePicker Activity
                try {
                    startActivityForResult(pickBuild.build(MainActivity.this),REQUEST_PICK_PLACE); // menjalankan PlacePicker Activity
                }
                catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e)
                {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK)// jika hasil dari placePicket builder adalah OK
        {
            Place place = PlacePicker.getPlace(this,data); // masukan data dari hasil PlacePicker Builder kedalam object Place
            textLocation.setText(getString(R.string.address_text,place.getName(),place.getAddress(), System.currentTimeMillis())); // update nama location text
            NAME_PLACE = place.getName().toString(); // masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
            ADDRESS_PLACE = place.getAddress().toString();// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
            IMG_PLACE = setTypeLocation(place);// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
            myAnimImageView.setImageResource(IMG_PLACE);
        }
        else
        {
            textLocation.setText("Location Not Selected");//jika tidak dipilih maka update text view menjadi text berikut
        }
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
        textLocation.setText("Find Your Address");
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
            textLocation.setText("find your location");
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
    public void onTaskCompleted(String result) throws SecurityException{ //digunakan saat tracking lokasi saat mendapatkan koordinat terbaru merubah menjadi alamat, diberikan throws karena saat mrncari nama place terdekat harus mengagkses API google yang membutuhkan test koneksi
            PendingResult<PlaceLikelihoodBuffer> placeResult = Places.PlaceDetectionApi.getCurrentPlace(googleApiClient, null); //mendapatkan nama tempat dari curent place saat ini
            placeResult.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                    for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                        textLocation.setText(getString(R.string.address_text,
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getPlace().getAddress(),
                                System.currentTimeMillis()));//mengupdate text nama lokasi dengan data yang didapatkan
                        NAME_PLACE = placeLikelihood.getPlace().getName().toString();// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
                        ADDRESS_PLACE = placeLikelihood.getPlace().getAddress().toString();// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
                        IMG_PLACE = setTypeLocation(placeLikelihood.getPlace());// masukan data data tersebut kedalam statid variabel untuk di saveinstance agar tidak hilang
                        myAnimImageView.setImageResource(IMG_PLACE);
                    }
                    placeLikelihoods.release();
                }
            });

    }

    private LocationRequest getLocation()
    {
        LocationRequest locationReq = new LocationRequest();
        locationReq.setInterval(10000);
        locationReq.setFastestInterval(5000);
        locationReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationReq;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { //overide methode dari GoogleAPI client untuk memeriksa koneksi dari API yang dugunakan
        Log.e("CON_API", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    private int setTypeLocation(Place currentPlace)//methode yang digunakan untuk mendapatkan gambar sesuai dengan tempat yang dipilih
    {
        int drawId = -1;// set aawal dari variabel
        for (Integer placeType : currentPlace.getPlaceTypes())//melakukan perulangan untuk mendapatkan jenis tempat yang sesuai dari current place
        {
            switch (placeType)
            {
                case  Place.TYPE_UNIVERSITY:
                    drawId= R.drawable.school;
                    break;
                case  Place.TYPE_CAFE:
                    drawId= R.drawable.coffeeshop;
                    break;
                case  Place.TYPE_SHOPPING_MALL:
                    drawId= R.drawable.mall;
                    break;
                case  Place.TYPE_MOVIE_THEATER:
                    drawId= R.drawable.cinema;
                    break;

                case  Place.TYPE_CEMETERY:
                    drawId= R.drawable.tombstone;
                    break;
                case  Place.TYPE_NIGHT_CLUB:
                    drawId= R.drawable.haram;
                    break;
                case  Place.TYPE_MOSQUE:
                    drawId= R.drawable.mosque;
                    break;
            }
            if(drawId<0)
            {
                drawId=R.drawable.notfound; //jika tidak ditemukan maka variabel akan diganti dengan gambar not found
            }
        }

        return drawId; //mengembalikan nilai dari hasil seleksi kondisi untuk diakses drawabale
    }
}


package com.example.tracking_app_v2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;

import org.w3c.dom.Text;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationActivity extends AppCompatActivity {

    boolean buttonSendOnceActive;

    private Button buttonSendOnce,
            buttonSendContinuous,
            buttonStopSend;
    //private TextView textViewCoordinates;
    private LocationManager locationManager;
    private LocationListener locationListener;

    //creez cele 2 variabile globale pentru transmiterea de date pe server
    private Retrofit retrofit;
    private RetrofitInterface retrofitInterface;

    private String Base_URL = LoginActivity.getBase_URL();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        //instantiem obiectul retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(Base_URL)
                //folosim urmatoarea comanda pentru a seta conevertorul JSON to Java object
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //instantiem obiectul retrofitInterface
        retrofitInterface = retrofit.create(RetrofitInterface.class);

        //afisam datele user-ului logat
        final JWTResult result = LoginActivity.getResult();
        final TextView textViewUser = findViewById(R.id.textViewUser);
        JWT jwt = new JWT(result.getJwt());
        Claim claim  = jwt.getClaim("firstName");
        String firstName = claim.asString();
        claim  = jwt.getClaim("lastName");
        String lastName = claim.asString();
        claim  = jwt.getClaim("email");
        final String email = claim.asString();

        textViewUser.setText(firstName + " " + lastName + "\n" + email);

        buttonSendOnceActive = false;

        buttonSendOnce = findViewById(R.id.locationOnce);
        buttonSendContinuous = findViewById(R.id.locationContinuous);
        buttonStopSend = findViewById(R.id.locationContinuousStop);
        //textViewCoordinates = findViewById(R.id.textViewCoordinates);

        //aici setez locationManager (folosesc service-ul de locatie)
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //configurez obiectul location listener (el implementeaza toate metodele din interfata Location Listener)
        //pe noi ne intereseaza prima si ultima metoda
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //textViewCoordinates.append("\n" + location.getLatitude() + " " + location.getLongitude());
                //verificam daca a fost activat butonul de sendOnce, caz in care oprim trimiterea de coordonate
                if (buttonSendOnceActive) {
                    locationManager.removeUpdates(this);
                    buttonSendOnceActive = false;
                }

                HashMap<String, String> map = new HashMap<>();

                map.put("email", email);
                map.put("lat", Double.toString(location.getLatitude()));
                map.put("long", Double.toString(location.getLongitude()));
                map.put("jwt", result.getJwt());

                Call<Void> call = retrofitInterface.executeSendLocation(email, map);

                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(LocationActivity.this, "Coordonate trimise cu succes!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(LocationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //de aici trimitem user-ul sa activeze locatia
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        configureLocation();
    }

    //trebuie de asemenea sa setez rezultatul permisiunii (de mai sus)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //caut codul dat de mine mai sus
        switch (requestCode) {
            case 7:
                //daca am obtinut cu succes permisiunea mergem la configure location
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    configureLocation();
                return;
        }
    }

    private void configureLocation() {
        //verificam daca avem permisiunea pentru locationManager (SDK >= 23)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                        //mai jos la request code avem nevoie de un integer (eu am pus 7)
                }, 7);
                return;
            }
            /* cu 'locationManager.requestLocationUpdates()' cerem locatia
            parametru 1) e "provider" care e "gps"
            parametru 2) e minTime (interval de refresh) si la noi e 180 000 (3 minute)
            parametru 3) e minDistance (distanta minima pt trigger) pe care il punem pe 0 (vrem update chiar daca stam pe loc)
            parametru 4) e locationListener si il punem ce cel initializat de noi mai sus */

            buttonSendOnce.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonSendOnceActive = true;
                    locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
                }
            });

            buttonSendContinuous.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    locationManager.requestLocationUpdates("gps", 180000, 0, locationListener);
                    //folosim urmatorul flag pentru a tine aplicatia pornita (ecranul nu se inchide)
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    buttonStopSend.setVisibility(View.VISIBLE);
                }
            });
            buttonStopSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    locationManager.removeUpdates(locationListener);
                    //folosim urmatorul flag pentru a permite ecranului sa se inchida
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    buttonStopSend.setVisibility(View.GONE);
                }
            });
        }
    }
}
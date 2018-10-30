package com.example.jcv.locatemypet;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BarcodeResultActivity extends AppCompatActivity{
    TextView petCode;
    ImageView petType;
    TextView petName;
    TextView petCharacteristics;
    TextView ownerName;

    JSONArray jsonArray;
    JSONObject jsonObject;

    String JsonString = "";

    boolean petFound = false;

    String mToken;
    String nameOfPet;

    Location location;
    LocationManager locationManager;
    LocationListener locationListener;

    String currLoc;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_result);
        petCode = (TextView) findViewById(R.id.petCode);
        petType = (ImageView) findViewById(R.id.petType);
        petName = (TextView) findViewById(R.id.petName);
        petCharacteristics = (TextView) findViewById(R.id.petCharacteristics);
        ownerName = (TextView) findViewById(R.id.ownerName);

        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
        String ff = FirebaseInstanceId.getInstance().getToken();

        final String token = sharedPreferences.getString(getString(R.string.FCM_TOKEN), ff);

        mToken = token;

        String url = MyConfig.URL_STR + "fcm_insert.php";
        //Toast.makeText(BarcodeResultActivity.this, mToken, Toast.LENGTH_SHORT).show();

        //mToken = token;

        //new insertFcm().execute();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(BarcodeResultActivity.this, "OnResponse!", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(BarcodeResultActivity.this, "OnERRORResponse!", Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put("fcm_token", mToken);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                return params;
            }
        };

        MySingleton.getmInstance(BarcodeResultActivity.this).addToRequestque(stringRequest);

        //GETTING
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(BarcodeResultActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(BarcodeResultActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //PERMISSION NOT CHECKED
            Toast.makeText(BarcodeResultActivity.this, "Please Check Permission First", Toast.LENGTH_SHORT).show();
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        }

        new getData().execute();

    }


    //-------------------------------------------------JSON PROCESSES--------------------------------------------------------------------
    //===================================================================================================================================
    //-------------------------------------------------JSON PROCESSES--------------------------------------------------------------------



    public void parseJSON(String str){
        ArrayList<Pet> pets = new ArrayList<>();

        Intent data = getIntent();
        Barcode barcode = data.getParcelableExtra("barcode");
        String barcodeValue = barcode.displayValue;

        try {
            jsonObject = new JSONObject(str);
            jsonArray = jsonObject.getJSONArray("pet");
            int i = 0;
            while (jsonArray.length() > i) {
                jsonObject = jsonArray.getJSONObject(i);

                int id = jsonObject.getInt("id");
                String code = jsonObject.getString("code");
                String type = jsonObject.getString("type");
                String name = jsonObject.getString("name");
                String charac = jsonObject.getString("characteristics");
                String owner_name = jsonObject.getString("owner_name");
                String owner_contact = jsonObject.getString("owner_contact");

                pets.add(new Pet(id, code, type, name, charac, owner_name, owner_contact));
                i++;

                if(barcodeValue.equals(code)){
                    //if the code is present in the DATABASE
                    petCode.setText("Pet Code : " + code);
                    petType.setImageResource(determinePet(type));
                    petName.setText("Hello! My name is " + name);
                    petCharacteristics.setText(charac);
                    ownerName.setText(owner_name + " - " + owner_contact);
                    nameOfPet = name;
                    return;
                }
            }
            setContentView(R.layout.activity_connection_failed);
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }

    public int determinePet(String str){
        return str.equals("D") ? R.drawable.dog : R.drawable.cat;
    }

    public void sendCoordinate(View view) {
        insertFcm fcmIn = new insertFcm();
        fcmIn.execute();
        Toast.makeText(this, "Your location is added to the map", Toast.LENGTH_SHORT).show();
    }

    class insertFcm extends AsyncTask<Void, Void, Integer> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(BarcodeResultActivity.this);
            dialog.setMessage("Loading");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try{
                URL url = new URL(MyConfig.URL_STR + "send_notification.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                ContentValues cv = new ContentValues();


                String message = nameOfPet + " has been found in " + String.valueOf(latitude) + ", " + String.valueOf(longitude);
                String title = "A pet has been found!";


                cv.put("petName", nameOfPet);
                cv.put("latitude", latitude);
                cv.put("longitude", longitude);
                cv.put("title", title);
                cv.put("message", message);

                bw.write(createPostString(cv));
                bw.flush();
                bw.close();
                os.close();
                int rc = httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
                return rc;

            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer strJson) {
            if((dialog != null)&& dialog.isShowing()){
                dialog.dismiss();
            }

        }
    }

    public String createPostString(ContentValues cv) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        boolean flag = true;

        for(Map.Entry<String, Object> v : cv.valueSet()){
            if(flag){
                flag = false;
            }
            else{
                sb.append("&");
            }

            sb.append(URLEncoder.encode(v.getKey(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(v.getValue().toString(), "UTF-8"));
        }
        return sb.toString();
    }

    class getData extends AsyncTask<Void, Void, String> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(BarcodeResultActivity.this);
            dialog.setMessage("Loading");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try{
                URL url = new URL(MyConfig.URL_STR + "petJson.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder sb = new StringBuilder();
                String str = "";
                while((str = bufferedReader.readLine()) != null){
                    sb.append(str);
                }
                JsonString = sb.toString();
                return sb.toString();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            dialog.dismiss();
            parseJSON(strJson);
        }
    }

}

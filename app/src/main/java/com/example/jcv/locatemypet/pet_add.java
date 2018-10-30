package com.example.jcv.locatemypet;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

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
import java.util.Map;

public class pet_add extends AppCompatActivity {

    EditText pet_name;
    EditText pet_char;
    EditText owner_name;
    EditText owner_contact;

    String add_pet_name;
    String add_pet_type = "D";
    String add_pet_char;
    String add_owner_name;
    String add_owner_contact;

    JSONArray jsonArray;
    JSONObject jsonObject;

    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_add);

        pet_name = (EditText) findViewById(R.id.add_pet_name);
        pet_char = (EditText) findViewById(R.id.add_pet_char);
        owner_name = (EditText) findViewById(R.id.add_owner_name);
        owner_contact = (EditText) findViewById(R.id.add_owner_contact);

    }

    public boolean petTypeIsEmpty(){
        RadioButton dog_selected = (RadioButton) findViewById(R.id.dog_selected);
        RadioButton cat_selected = (RadioButton) findViewById(R.id.cat_selected);
        return !(dog_selected.isSelected() || cat_selected.isSelected());
    }

    public void addPet(View view) {
        add_pet_name = String.valueOf(pet_name.getText());
        add_pet_char = String.valueOf(pet_char.getText());
        add_owner_name = String.valueOf(owner_name.getText());
        add_owner_contact = String.valueOf(owner_contact.getText());

        if(add_pet_name.equals("") && petTypeIsEmpty() && add_pet_char.equals("") && add_owner_name.equals("") && add_owner_contact.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please fill up every fields.").setTitle("Missing Fields").setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //DO Nothing
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else{
            new pet_add.getData().execute();
            Toast.makeText(this, "Pet Added", Toast.LENGTH_SHORT).show();
        }
        /*String str = "PET NAME : " + add_pet_name + "\n"
                    + "PET IMAGETYPE : " + add_pet_type + "\n"
                    + "PET CHAR : " + add_pet_char + "\n"
                    + "OWNER NAME : " + add_owner_name + "\n"
                    + "OWNER NAME : " + add_owner_contact;
        */
    }

    public void doCheckPetType(View view) {
        boolean checked = ((RadioButton)view).isChecked();
        switch(view.getId()){
            case R.id.dog_selected:
                if(checked)
                    add_pet_type = "D";
                break;
            case R.id.cat_selected:
                add_pet_type = "C";
                break;
        }
    }

    class getCurrentId extends AsyncTask<Void, Void, String>{

        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(pet_add.this);
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
            finish();
        }
    }

    public void parseJSON(String str){
        try{
            jsonObject = new JSONObject(str);
            jsonArray = jsonObject.getJSONArray("pet");
            int i = 0;
            while(jsonArray.length()>i){
                jsonObject = jsonArray.getJSONObject(i);
                int count = jsonObject.getInt("id");
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    class getData extends AsyncTask<Void, Void, Integer> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(pet_add.this);
            dialog.setMessage("Loading");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try{
                URL url = new URL(MyConfig.URL_STR + "petInsert.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                ContentValues cv = new ContentValues();



                cv.put("post_pet_name", add_pet_name);
                cv.put("post_pet_type", add_pet_type);
                cv.put("post_pet_char", add_pet_char);
                cv.put("post_owner_name", add_owner_name);
                cv.put("post_owner_contact", add_owner_contact);

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
            finish();
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

}

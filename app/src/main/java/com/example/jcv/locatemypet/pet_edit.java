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

public class pet_edit extends AppCompatActivity {

    JSONArray jsonArray;
    JSONObject jsonObject;

    int selectedPos;

    EditText pet_name;
    EditText pet_char;
    EditText owner_name;
    EditText owner_contact;

    int edit_pet_id = 0;

    String edit_pet_name;
    String edit_pet_type;
    String edit_pet_char;
    String edit_owner_name;
    String edit_owner_contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_edit);

        pet_name = (EditText) findViewById(R.id.add_pet_name);
        pet_char = (EditText) findViewById(R.id.add_pet_char);
        owner_name = (EditText) findViewById(R.id.add_owner_name);
        owner_contact = (EditText) findViewById(R.id.add_owner_contact);

        selectedPos = getIntent().getIntExtra("pos", -1);

        new retainData().execute();

    }

    public void doEdit(View view) {
        edit_pet_name = pet_name.getText().toString();
        edit_pet_char = pet_char.getText().toString();
        edit_owner_name = owner_name.getText().toString();
        edit_owner_contact = owner_contact.getText().toString();

        if(edit_pet_name.equals("") && edit_pet_char.equals("") && edit_owner_name.equals("") && edit_owner_contact.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please fill up every fields.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do nothing
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }else{
            String str = "PET ID : " + edit_pet_id + "\n"
                    + "PET NAME : " + edit_pet_name + "\n"
                    + "PET IMAGETYPE : " + edit_pet_type + "\n"
                    + "PET CHAR : " + edit_pet_char + "\n"
                    + "OWNER NAME : " + edit_owner_name + "\n"
                    + "OWNER NAME : " + edit_owner_contact;

            new pet_edit.getData().execute();
            finish();
        }
    }

    public void doCheckPetType(View view) {
        boolean checked = ((RadioButton)view).isChecked();
        switch(view.getId()){
            case R.id.dog_selected:
                if(checked)
                    edit_pet_type = "D";
                break;
            case R.id.cat_selected:
                edit_pet_type = "C";
                break;
        }
    }

    public void determinePet(String str){
        if(str.equals("D")){
            RadioButton dog_selected = (RadioButton) findViewById(R.id.dog_selected);
            dog_selected.setChecked(true);
            edit_pet_type = "D";
        }else{
            RadioButton cat_selected = (RadioButton) findViewById(R.id.cat_selected);
            cat_selected.setChecked(true);
            edit_pet_type = "C";
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

    class getData extends AsyncTask<Void, Void, Integer> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(pet_edit.this);
            dialog.setMessage("Loading");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try{
                URL url = new URL(MyConfig.URL_STR + "petUpdate.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                ContentValues cv = new ContentValues();

                cv.put("post_pet_id_edit", edit_pet_id);
                cv.put("post_pet_name_edit", edit_pet_name);
                cv.put("post_pet_type_edit", edit_pet_type);
                cv.put("post_pet_char_edit", edit_pet_char);
                cv.put("post_owner_name_edit", edit_owner_name);
                cv.put("post_owner_contact_edit", edit_owner_contact);

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
            dialog.dismiss();
        }
    }

    class retainData extends AsyncTask<Void, Void, String> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(pet_edit.this);
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
        }
    }

    public void parseJSON(String str){
        try{
            jsonObject = new JSONObject(str);
            jsonArray = jsonObject.getJSONArray("pet");
            int i = 0;
            while(jsonArray.length()>i){
                jsonObject = jsonArray.getJSONObject(i);

                if(i == selectedPos){

                    String pet_id_str = String.valueOf(jsonObject.getInt("id"));
                    String data = jsonObject.getString("name");
                    String imageId_str = jsonObject.getString("type");
                    String characteristics_str = jsonObject.getString("characteristics");
                    String owner_name_str = jsonObject.getString("owner_name");
                    String owner_contact_str = jsonObject.getString("owner_contact");

                    determinePet(imageId_str);
                    edit_pet_id = Integer.parseInt(pet_id_str);
                    pet_name.setText(data);
                    pet_char.setText(characteristics_str);
                    owner_name.setText(owner_name_str);
                    owner_contact.setText(owner_contact_str);

                    String str2 = "PET ID : " + edit_pet_id + "\n"
                            + "PET NAME : " + edit_pet_name + "\n"
                            + "PET IMAGETYPE : " + edit_pet_type + "\n"
                            + "PET CHAR : " + edit_pet_char + "\n"
                            + "OWNER NAME : " + edit_owner_name + "\n"
                            + "OWNER NAME : " + edit_owner_contact;

                    Toast.makeText(this,str,Toast.LENGTH_SHORT);
                    break;
                }
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

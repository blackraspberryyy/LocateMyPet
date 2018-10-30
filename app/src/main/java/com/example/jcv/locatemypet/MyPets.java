package com.example.jcv.locatemypet;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

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
import java.util.Map;

public class MyPets extends AppCompatActivity {

    JSONArray jsonArray;
    JSONObject jsonObject;

    String JsonString;

    GridView grid;

    int selectedPos = 0;
    int pet_remove_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pets);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new getData().execute();
    }

    public void addPet(View view) {
        startActivity(new Intent(this, pet_add.class));
    }


    class getData extends AsyncTask<Void, Void, String> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MyPets.this);
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

    public void parseJSON(String str){
        ArrayList<Pet> pets = new ArrayList<>();
        ArrayList<String> names_list = new ArrayList<>();
        ArrayList<Integer> imageId_list = new ArrayList<>();

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

                names_list.add(name);
                imageId_list.add(determinePet(type));
                i++;
            }


            CustomGrid adapter = new CustomGrid(MyPets.this, names_list, imageId_list);

            grid = (GridView) findViewById(R.id.petGrid);

            //grid.setAdapter(null);
            grid.setAdapter(adapter);

            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MyPets.this, pet_preview.class);
                    intent.putExtra("pos", position);
                    startActivity(intent);
                }
            });
            registerForContextMenu(grid);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public int determinePet(String str){
        return str.equals("D") ? R.drawable.dog : R.drawable.cat;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem itm){
        Intent intent;
        AdapterView.AdapterContextMenuInfo info;
        int listPosition;

        switch(itm.getItemId()){
            case (R.id.remove):
                info = (AdapterView.AdapterContextMenuInfo) itm.getMenuInfo();
                selectedPos = info.position;
                pet_remove_id = getIdFromParseJSON(JsonString);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Warning").setMessage("Are you sure you want to remove this pet?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new deleteData().execute();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //DO NOTHING
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case (R.id.view):
                info = (AdapterView.AdapterContextMenuInfo) itm.getMenuInfo();
                listPosition = info.position;
                intent = new Intent(MyPets.this, pet_preview.class);
                intent.putExtra("pos", listPosition);
                startActivity(intent);
                return true;
            case (R.id.edit):
                info = (AdapterView.AdapterContextMenuInfo) itm.getMenuInfo();
                listPosition = info.position;
                intent = new Intent(MyPets.this, pet_edit.class);
                intent.putExtra("pos", listPosition);
                startActivity(intent);

                return true;
            default: return super.onContextItemSelected(itm);
        }
    }

    public int getIdFromParseJSON(String str){
        int  pet_id= -1;
        try{
            jsonObject = new JSONObject(str);
            jsonArray = jsonObject.getJSONArray("pet");
            int i = 0;
            while(jsonArray.length()>i){
                jsonObject = jsonArray.getJSONObject(i);

                if(i == selectedPos){
                    String id_str = String.valueOf(jsonObject.getInt("id"));
                    pet_id = Integer.parseInt(id_str);
                    break;
                }
                i++;
            }
            return pet_id;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return pet_id;
    }

    class deleteData extends AsyncTask<Void, Void, Integer> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MyPets.this);
            dialog.setMessage("Loading");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try{
                URL url = new URL(MyConfig.URL_STR + "petDelete.php");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                ContentValues cv = new ContentValues();

                cv.put("post_pet_id_edit", pet_remove_id);

                bw.write(createPostString(cv));
                bw.flush();
                bw.close();
                os.close();
                int rc = httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
                Log.i("RESPONSE CODE : ", String.valueOf(rc));
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
            finish();
            startActivity(getIntent());
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

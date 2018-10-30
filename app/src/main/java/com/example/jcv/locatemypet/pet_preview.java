package com.example.jcv.locatemypet;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class pet_preview extends AppCompatActivity {

    JSONArray jsonArray;
    JSONObject jsonObject;

    Context context;

    int selectedPos;

    ImageView petType;
    TextView petName;
    TextView petCode;
    TextView petChar;
    TextView petOwner;

    ImageView imageView;
    public final static int QRcodeWidth = 500 ;
    Bitmap bitmap ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_preview);

        imageView = (ImageView)findViewById(R.id.imageView);

        selectedPos = getIntent().getIntExtra("pos",-1);

        petType = (ImageView) findViewById(R.id.prev_petType);
        petName = (TextView) findViewById(R.id.prev_petName);
        petCode = (TextView) findViewById(R.id.prev_petCode);
        petChar = (TextView) findViewById(R.id.prev_petChar);
        petOwner = (TextView) findViewById(R.id.prev_petOwner);

        new getData().execute();
    }

    public int determinePet(String str){
        return str.equals("D") ? R.drawable.dog : R.drawable.cat;
    }

    public void parseJSON(String str){
        try{
            jsonObject = new JSONObject(str);
            jsonArray = jsonObject.getJSONArray("pet");
            int i = 0;
            while(jsonArray.length()>i){
                jsonObject = jsonArray.getJSONObject(i);
                if(i == selectedPos){

                    int id = jsonObject.getInt("id");
                    String code = jsonObject.getString("code");
                    String type = jsonObject.getString("type");
                    String name = jsonObject.getString("name");
                    String charac = jsonObject.getString("characteristics");
                    String owner_name = jsonObject.getString("owner_name");
                    String owner_contact = jsonObject.getString("owner_contact");



                    petType.setImageResource(determinePet(type));
                    petName.setText(name);
                    petCode.setText("Pet Code : " + code);
                    petChar.setText(charac);
                    petOwner.setText(owner_name + " - " + owner_contact);

                    try {
                        bitmap = TextToImageEncode(code);

                        imageView.setImageBitmap(bitmap);

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }

                    break;
                }
                i++;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saveBmp(View view) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Android/data/com.example.jcv.locatemypet/qrCode");

        myDir.mkdir();
        String fname = petName.getText().toString() + "-" + petCode.getText().toString() + ".jpg";
        File file = new File(myDir, fname);
        Log.i("FILE NAME : ", "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            addImageToGallery(file.getAbsolutePath(), pet_preview.this);
            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show();
            //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    class getData extends AsyncTask<Void, Void, String> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(pet_preview.this);
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


    //QR CODE GENERATOR
    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }


}

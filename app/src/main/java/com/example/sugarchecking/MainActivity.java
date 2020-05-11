package com.example.sugarchecking;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;

import Retrofit.IUploadAPI;
import Retrofit.RetrofitClient;
import Utils.Common;
import Utils.IUploadCallbacks;
import Utils.ProgressRequestBody;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity<OnActivityResult> extends AppCompatActivity implements IUploadCallbacks {
    private static final int PICK_FILE_REQUEST=1000;
    IUploadAPI mService;
    Button btnUpload;
    ImageView imageView;
    ProgressDialog dialog;
    private Uri selectedFileUri;
    TextView textView;

    private IUploadAPI getAPIUpload(){
        return RetrofitClient.getClient().create(IUploadAPI.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dexter.withActivity(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(MainActivity.this,"Permission accepted", LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText( MainActivity.this,"You should acccept the permission", LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        // create mservice
        mService = getAPIUpload();

        //InitView
        btnUpload = (Button)findViewById(R.id.btn_upload);
        imageView =(ImageView)findViewById(R.id.image_view);
        textView=(TextView)findViewById(R.id.text) ;

        //event
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== Activity.RESULT_OK){
            if (requestCode==PICK_FILE_REQUEST){
                if(data !=null){
                    selectedFileUri = data.getData();
                    if (selectedFileUri !=null && !selectedFileUri.getPath().isEmpty())
                        imageView.setImageURI(selectedFileUri);
                    else Toast.makeText(this,"File not Found", LENGTH_SHORT).show();

                }
            }
        }
    }

    private void uploadFile() {
        if (selectedFileUri != null) {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("Uploading..");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();
            File file = null;
            try {
                file = new File(Common.getFilePath(this, selectedFileUri));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if (file != null) {
                ProgressRequestBody requestBody = new ProgressRequestBody(file, this);
                final MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mService.uploadFile(body).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                dialog.dismiss();
                                String image_processed_link = new StringBuilder("http://10.0.2.2:5000/" +
                                        response.body().replace("\"", "")).toString();
                                //Picasso.get().load(image_processed_link).into(imageView);
                                String result =response.body().replace("\"", "").toString();
                                textView.setText(result);
                                Toast.makeText(MainActivity.this, "Detected!", LENGTH_SHORT).show();

                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, "" + t.getMessage(), LENGTH_SHORT);
                            }
                        });
                    }
                }).start();
            }
        }

        else {Toast.makeText(this,"Ohno!", LENGTH_SHORT).show();}
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_FILE_REQUEST);
    }

    @Override
    public void onProgressUpdate(int percent) {

    }
}
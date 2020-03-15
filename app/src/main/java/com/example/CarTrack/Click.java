package com.example.CarTrack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.ImagePickActivity;
import com.vincent.filepicker.filter.entity.ImageFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.vincent.filepicker.activity.ImagePickActivity.IS_NEED_CAMERA;

public class Click extends AppCompatActivity implements View.OnClickListener {

    ImageView camera_click_picture;
    CircleImageView file_attach;
    Button upload_btn;
    EditText et1, et2;
    Spinner sp;
    Uri photoURI;
    Asyncc progress;
    Uri tempUri;
    private Uri uri;
    GPSTracker gps;

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int REQUEST_LOCATION = 1;
    private static final String TAG = "CapturePicture";
    static final int REQUEST_PICTURE_CAPTURE = 1;
    private ImageView image;
    private String pictureFilePath;
    private String deviceIdentifier;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    Bitmap imageBitmap;
    byte[] dataBAOS;
    int rresultCode;

    DatabaseReference databaseReference, databaseReference1;
    public String zoneImageURI = null;

    File mPhotoFile;
    String ZoneTitle;
    String uid;

    LocationManager locationManager;
    String lattitude, longitude;
    public String Lat, Logg;


    FirebaseAutoMLRemoteModel remoteModel;
    FirebaseAutoMLLocalModel localModel;
    FirebaseVisionImageLabeler labeler;
   // FirebaseVisionImage image_model;


    SharedPreferences pref;
    SharedPreferences.Editor editor;


    String text;
    float confidence;

    String picture_url;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        camera_click_picture = findViewById(R.id.camera_click);
        file_attach = findViewById(R.id.image);
        upload_btn = findViewById(R.id.upload_button);
        progressBar = findViewById(R.id.progressBar);



        // mProgress = new ProgressDialog(this);
        //  mProgress=(ProgressBar)findViewById(R.id.progressBar);

        upload_btn.setOnClickListener(this);
        upload_btn.setClickable(false);
        upload_btn.setBackgroundColor(getResources().getColor(R.color.grey_100));
        camera_click_picture.setOnClickListener(this);

        remoteModel = new FirebaseAutoMLRemoteModel.Builder("car_label").build();


        localModel = new FirebaseAutoMLLocalModel.Builder()
                .setAssetFilePath("model/manifest.json")
                .build();

        downloadmodel();

    }

    private void downloadmodel() {
        FirebaseModelManager.getInstance().isModelDownloaded( remoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder;
                        if (isDownloaded) {
                            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
                        } else {
                            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel);
                        }
                        FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                                .setConfidenceThreshold(0.6f)  // Evaluate your model in the Firebase console
                                // to determine an appropriate threshold.
                                .build();

                        try {
                             labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
                        } catch (FirebaseMLException e) {
                            // Error.
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {


        if (view == upload_btn) {

            //evaluate_model();
            execute_asyncc();

            // picture_url=progress.getFirebase_storage_picture();




          //  Uri picture_url=progress.getFirebase_storage_picture();
           // Log.d("Picture urlll",picture_url+"");
            upload_btn.setClickable(false);
            upload_btn.setBackgroundColor(getResources().getColor(R.color.grey_100));


        }


        if (view == camera_click_picture) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {

                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // Error occurred while creating the File
                }
                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);

                    Toast.makeText(this, photoURI+"", Toast.LENGTH_SHORT).show();

                    mPhotoFile = photoFile;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    //  startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);


                    startActivityForResult(intent, CAMERA_REQUEST_CODE);

                }


            }
        }
    }

    public void execute_asyncc() {

        progress = new Asyncc(this, photoURI, progressBar,labeler);
        progress.execute();

    }

    public void evaluate_model(Uri photoURI, final Context ctx,FirebaseVisionImageLabeler labeler) {
       final SharedPreferences pref = ctx.getSharedPreferences("MyPref", 0);
        final SharedPreferences.Editor sp_editor=pref.edit();

        FirebaseVisionImage image_model = null;
        try {
            image_model = FirebaseVisionImage.fromFilePath(ctx, photoURI);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //   Toast.makeText(this, image_model+"", Toast.LENGTH_SHORT).show();


        labeler.processImage(image_model)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        // Task completed successfully

                        for (FirebaseVisionImageLabel label: labels) {
                             String text_label = label.getText();
                            Log.d("ML_OUTPUT", text);
                           String label_confidence = label.getConfidence()+"";
                          //  Toast.makeText(this, "qfwe", Toast.LENGTH_SHORT).show();

                            Toast.makeText(ctx, "The car is "+text_label+" , with a confidence of "+label_confidence, Toast.LENGTH_LONG).show();


                            sp_editor.putString("confidence",label_confidence+"");
                            sp_editor.putString("label",text_label);
                            sp_editor.commit();

                        }
                        // ...
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });
    }


    //   Log.d("bataBAOS0", String.valueOf(dataBAOS[0]));


    public void uploadphoto (Uri dataBAOS){

        FirebaseStorage storage;
        StorageReference storageRef;
        storage = FirebaseStorage.getInstance();


        // progress.onPreExecute();

        //    byte[] data = baos.toByteArray();

        storageRef = storage.getReference().child("" + new Date().getTime());

        UploadTask uploadTask = storageRef.putFile(dataBAOS);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...


            }
        });
        //   mProgress.dismiss();
        // progress.onPostExecute(null);

    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            // Bundle extras = data.getExtras();
            //imageBitmap = (Bitmap) extras.get("data");

            //  tempUri = data.getData();
            //   mSelectImage.setImageURI(mImageUri);
            //tempUri = getImageUri(getApplicationContext(), imageBitmap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            //compress size commented

            //     imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            //     dataBAOS = baos.toByteArray();

            rresultCode = resultCode;

            file_attach.setImageURI(photoURI);


            // file_attach.setImageBitmap(imageBitmap);

            upload_btn.setClickable(true);
            upload_btn.setBackgroundColor(getResources().getColor(R.color.blue400));

            super.onActivityResult(requestCode, resultCode, data);

            //  super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode==Constant.REQUEST_CODE_PICK_IMAGE)
        {
            if (resultCode == RESULT_OK) {
                ArrayList<ImageFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_IMAGE);
               // Log.d("LISTTTT",list.get(0).toString());

                // Uri path = Uri.fromFile(list.get(0));

                //Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(list.get(0)));
                Log.d(  "pathhh",list.get(0).getPath());
                photoURI= Uri.fromFile(new File(list.get(0).getPath()));

                file_attach.setImageURI(Uri.fromFile(new File(list.get(0).getPath())));
                upload_btn.setClickable(true);
                upload_btn.setBackgroundColor(getResources().getColor(R.color.blue400));

                //file_attach.setImageDrawable(Drawable.createFromPath(list.get(0).toString()));

            }
        }
    }


    private File createImageFile () throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }


    public void attach_file(View view) {

        Intent intent1 = new Intent(this, ImagePickActivity.class);
        intent1.putExtra(IS_NEED_CAMERA, true);
        intent1.putExtra(Constant.MAX_NUMBER, 1);
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


   public void get_LatLong(Context ctx) throws IOException {

        gps = new GPSTracker(ctx);
        if(gps.canGetLocation())
        {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            String address=getAddress(ctx,latitude,longitude);
            update_firestore(ctx,address,latitude,longitude);

            // \n is for new line
          //  Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            Toast.makeText(getApplicationContext(), "Cannot get location", Toast.LENGTH_SHORT).show();

        }

    }

    public String getAddress(Context ctx, double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(ctx, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

        String ADDRESS = address+" , "+city+" , "+state+" , "+country+" , "+postalCode;

        Toast.makeText(ctx, city+state+postalCode+address, Toast.LENGTH_SHORT).show();

        return ADDRESS;
    }

    public void update_firestore(final Context ctx, String address, double latitude, double longitude) {
        pref = ctx.getSharedPreferences("MyPref", 0);
        String user_email=pref.getString("email", null); // getting String
        String last_upload_url=pref.getString("last_upload_url", null);
        String ml_label=pref.getString("label", null);
        String ml_confidence=pref.getString("confidence", null);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        Map<String, Object> car_details = new HashMap<>();
        car_details.put("TimeStamp", tsLong);
        car_details.put("accuracy", ml_confidence);
        car_details.put("Uploader",user_email);
        car_details.put("latitude",latitude);
        car_details.put("longitude", longitude);
        car_details.put("Addresss",address);
        car_details.put("Image_Url",last_upload_url);
        car_details.put("label",ml_label);


//        DocumentReference document =
//                db.collection("users").document(user_email).collection("image_uploads").document(ts);


        DocumentReference document = db.collection("Cars").document(ts);

        document.set(car_details)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ctx, "Successfully car updated firestore", Toast.LENGTH_SHORT).show();

                        // get_LatLong();

                        //  Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Log.w(TAG, "Error writing document", e);



                    }
                });
    }
}

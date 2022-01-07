package com.example.covid19tracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.covid19tracker.DataModels.User;
import com.example.covid19tracker.DBData.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imageVieweditImage;
    private ImageView imageViewprofileImage;
    private EditText editTextPersonName;
    private EditText editTextPersonSurname;
    private Button doneButton;

    private static final int PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE = 101;
    static final int REQUEST_TAKE_PHOTO = 202;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_GALLERY = 2;

    int tmp;

    String currentPhotoPath;
    Uri imageUri;
    String firestorageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
        {
            case Configuration.UI_MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
        setContentView(R.layout.activity_edit_profile);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imageViewprofileImage=findViewById(R.id.imageViewEditActProfPic);
        editTextPersonName=findViewById(R.id.editProfileTextPersonName);
        editTextPersonSurname=findViewById(R.id.editProfileSurname);

        sharedPreferences=getApplicationContext().getSharedPreferences("Userdata",Context.MODE_PRIVATE);
        User loggedUser=UserData.getInstance().getUser(sharedPreferences.getString(getString(R.string.loggedUser_email),""));
        editTextPersonName.setText(loggedUser.name);
        editTextPersonSurname.setText(loggedUser.surname);

        String image=sharedPreferences.getString(getString(R.string.loggedUser_image),"");
        if (image != null && !(image.compareTo("") == 0)) {
            Glide.with(this).load(image).into(imageViewprofileImage);
        }
        else {
            imageViewprofileImage.setImageResource(R.drawable.ic_baseline_account_box_24);
        }

        imageVieweditImage=findViewById(R.id.imageViewEditPicture);
        imageVieweditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Izaberite izvor:");

                String[] opts = {"Kamera", "Galerija"};
                builder.setItems(opts, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0)
                            tmp=1;
                        else
                            tmp=2;
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        if(tmp==1)
                        {
                            if (ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                                    || ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            {
                                ActivityCompat.requestPermissions(EditProfileActivity.this,
                                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_TAKE_PHOTO);
                            }
                            else
                                openCameraTocaptureImage();

                        }
                        if(tmp==2)
                        {
                            if (ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(EditProfileActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            {
                                ActivityCompat.requestPermissions(EditProfileActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE);
                            }
                            else
                                openGalleryToChooseImage();
                        }
                    }
                });
                dialog.show();
            }
        });

        doneButton=findViewById(R.id.buttonDoneEdit);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                uploadTextData();
                if (imageUri != null)
                    uploadImage();
                else
                    finish();
            }
        });
    }

    private void uploadImage()
    {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Čuva se...");
        progressDialog.show();

        StorageReference ref = storageReference.child("imagesUsers/" + UUID.randomUUID().toString());
        ref.putFile(imageUri)
                .addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        firestorageUri = String.valueOf(uri);

                                        Context context = getApplicationContext();
                                        SharedPreferences sharedPref = context.getSharedPreferences("Userdata", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString(getString(R.string.loggedUser_image), firestorageUri);
                                        editor.commit();

                                        UserData.getInstance().updateUserImage(
                                                sharedPref.getString(getString(R.string.loggedUser_username),""),firestorageUri);

                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("img", firestorageUri);
                                        setResult(Activity.RESULT_OK, resultIntent);
                                        finish();

                                    }
                                });

                                progressDialog.dismiss();
                                Toast.makeText(EditProfileActivity.this, "Slika sačuvana", Toast.LENGTH_SHORT).show();

                            }
                        })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Čuvanje neuspešno" + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(
                        new OnProgressListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Sačuvano " + (int) progress + "%");
                            }
                        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    openGalleryToChooseImage();

                else
                    Toast.makeText(this, "Pristup nije odobren!", Toast.LENGTH_SHORT).show();

                return;
            }
            case REQUEST_TAKE_PHOTO:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    openCameraTocaptureImage();
                else
                    Toast.makeText(this, "Pristup nije odobren!", Toast.LENGTH_SHORT).show();

                return;
            }
        }
    }

    private void openCameraTocaptureImage() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                                "com.example.covid19tracker", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGalleryToChooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Izaberite sliku"), REQUEST_GALLERY);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            File f = new File(currentPhotoPath);
            imageUri = Uri.fromFile(f);
            setPic();
        }
        else if(requestCode==REQUEST_GALLERY && resultCode==RESULT_OK && data!=null && data.getData() != null)
            imageUri=data.getData();

        imageViewprofileImage.setImageURI(imageUri);
    }

    private void setPic()
    {
        int targetW = imageViewprofileImage.getWidth();
        int targetH = imageViewprofileImage.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        imageViewprofileImage.setImageBitmap(bitmap);
    }

    private void uploadTextData()
    {
        UserData.getInstance().updateUserProfileExceptImage(sharedPreferences.getString(getString(R.string.loggedUser_username),""),
                editTextPersonName.getText().toString(),
                editTextPersonSurname.getText().toString());
    }

}
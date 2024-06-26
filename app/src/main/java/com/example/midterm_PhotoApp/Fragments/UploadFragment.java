package com.example.midterm_PhotoApp.Fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UploadFragment extends Fragment {

    private FloatingActionButton uploadButton, previousButton, forwardButton;
    private ImageView uploadImage;
    EditText uploadCaption;
    ProgressBar progressBar;
    private ArrayList<Uri> arrayImageUri;
    int position;
    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
    final private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload,container,false);
        uploadButton = view.findViewById(R.id.uploadButton);
        previousButton = view.findViewById(R.id.previousButton);
        forwardButton = view.findViewById(R.id.forwardButton);
        uploadCaption = view.findViewById(R.id.uploadCaption);
        uploadImage = view.findViewById(R.id.uploadImage);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        arrayImageUri = new ArrayList<Uri>();

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && null != result.getData()) {
                            if (result.getData().getClipData() != null){
                                ClipData mClipData = result.getData().getClipData();
                                int cout = mClipData.getItemCount();
                                for (int i = 0; i < cout; i++) {
                                    // adding imageuri in array
                                    Uri imageurl = mClipData.getItemAt(i).getUri();
                                    arrayImageUri.add(imageurl);
                                }
                                position = 0;
                                uploadImage.setImageURI(arrayImageUri.get(position));
                                forwardButton.setVisibility(View.VISIBLE);
                                forwardButton.setEnabled(true);
                                previousButton.setVisibility(View.VISIBLE);
                                previousButton.setEnabled(true);
                        }
                        } else {
                            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent();
                photoPicker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                photoPicker.setType("image/*");
                arrayImageUri.clear();
                activityResultLauncher.launch(photoPicker);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(arrayImageUri.size()> 0) {
                    Log.d("UPLOADING IMG", "START UPLOADING");
                    double fileSizeMbs = 0;
                    for(int i = 0; i < arrayImageUri.size(); i++){
                        InputStream fileInputStream= null;
                        try {
                            fileInputStream = getContext().getContentResolver().openInputStream(arrayImageUri.get(i));
                            fileSizeMbs = fileInputStream.available();
                            fileSizeMbs = fileSizeMbs / (1024 * 1024);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        /*File file = new File(arrayImageUri.get(i).getPath());
                        double fileSizeInMBs = (double) file.length() / (1024 * 1024);
                        String calString=Double.toString(fileSizeInMBs);*/
                        long start = System.currentTimeMillis();
                        uploadToFirebase(arrayImageUri.get(i), fileSizeMbs, start);
                    }
                    Log.d("UPLOADING IMG", "FINISHED UPLOADING");
                    forwardButton.setVisibility(View.INVISIBLE);
                    forwardButton.setEnabled(false);
                    previousButton.setVisibility(View.INVISIBLE);
                    previousButton.setEnabled(false);
                } else {
                    Toast.makeText(getContext(), "Please select image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == 0)
                    position = arrayImageUri.size() - 1;
                else
                    position--;
                uploadImage.setImageURI(arrayImageUri.get(position));
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == arrayImageUri.size() - 1)
                    position = 0;
                else
                    position++;
                uploadImage.setImageURI(arrayImageUri.get(position));
            }
        });

        return view;
    }


    private void uploadToFirebase(Uri uri, Double size, long start) {
        String caption = uploadCaption.getText().toString();
        final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        imageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DataClass dataClass = new DataClass(uri.toString(), caption);
                        String key = databaseReference.push().getKey();
                        databaseReference.child(key).setValue(dataClass).addOnSuccessListener((Activity) getContext(),new OnSuccessListener<Void>(){
                            @Override
                            public void onSuccess(Void aVoid){
                                // SUCCESS
                                Log.d("UPLOADING IMG UPDATE", "FINISHED UPLOADING IMG SIZE " + size + "Mbs total time " + (System.currentTimeMillis() - start));
                                // Log the details
                                Log.d("FirebaseData","user data uploaded successfully");
                                // Make a toast
                                Toast.makeText(getContext(), "caption, url uploaded successfully", Toast.LENGTH_LONG).show();
                                uploadCaption.setText("");
                                uploadImage.setImageDrawable(getResources().getDrawable(R.drawable.uploadicon));
                            }

                        }).addOnFailureListener((Activity) getContext(),new OnFailureListener(){
                            @Override
                            public void onFailure(@NonNull Exception e){
                                // FAILURE

                                // Log the details
                                Log.d("FirebaseData","user data upload failed");
                                // Make a toast
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }});
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri fileUri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }
}
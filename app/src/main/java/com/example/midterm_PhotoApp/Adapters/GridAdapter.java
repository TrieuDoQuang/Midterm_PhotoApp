package com.example.midterm_PhotoApp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;

import java.time.Duration;
import java.time.Instant;

public class GridAdapter extends BaseAdapter {
    private ArrayList<DataClass> dataList;
    private Context context;
    LayoutInflater layoutInflater;
    private double totalSize = 0;

    private HashSet<String> processedUrls = new HashSet<>();

    FirebaseStorage storage = FirebaseStorage.getInstance();

    public GridAdapter(Context context, ArrayList<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @Override
    public int getCount() {
        return dataList.size();
    }
    @Override
    public Object getItem(int i) {
        return null;
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        long start = System.currentTimeMillis();
        if (layoutInflater == null){
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null){
            view = layoutInflater.inflate(R.layout.grid_item, null);
        }
        ImageView gridImage = view.findViewById(R.id.gridImage);
        TextView gridCaption = view.findViewById(R.id.gridCaption);
        Glide.with(context).load(dataList.get(i).getImageURL()).into(gridImage);
        Log.d("GRID ADAPTER", "LOADING IMAGE NO." + i);
        getImageSize(dataList.get(i).getImageURL());



        gridCaption.setText(dataList.get(i).getCaption());
        long end = System.currentTimeMillis();
        Log.d("GRID ADAPTER", "TIME " + (end-start) + " Millisecond");

        return view;

    }

    private void getImageSize(String imageUrl) {
        if (processedUrls.contains(imageUrl)) {
            // Skip this URL because it's already been processed
            return;
        }
        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);

        imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                long size = storageMetadata.getSizeBytes();
                double sizeInMB = (double) size / (1024 * 1024);
                totalSize += sizeInMB;
                Log.d("STAGGER ADAPTER", "Image size: " + sizeInMB + " MB");
                Log.d("STAGGER ADAPTER", "Total size: " + totalSize + " MB");
                processedUrls.add(imageUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }


}

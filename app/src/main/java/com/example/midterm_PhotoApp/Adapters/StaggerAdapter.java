package com.example.midterm_PhotoApp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashSet;

public class StaggerAdapter extends RecyclerView.Adapter<StaggerAdapter.MyViewHolder> {
    ArrayList<DataClass> dataList;
    Context context;

    private double totalSize = 0;

    private HashSet<String> processedUrls = new HashSet<>();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    public StaggerAdapter(ArrayList<DataClass> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stagger_item, parent, false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(dataList.get(position).getImageURL()).into(holder.staggeredImages);
        Log.d("STAGGER ADAPTER", "LOADING IMAGE NO." + position);
        getImageSize(dataList.get(position).getImageURL());

    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{
        RoundedImageView staggeredImages;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            staggeredImages = itemView.findViewById(R.id.staggeredImages);
        }
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

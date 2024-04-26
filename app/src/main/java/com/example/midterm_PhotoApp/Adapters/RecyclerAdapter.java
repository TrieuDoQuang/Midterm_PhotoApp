package com.example.midterm_PhotoApp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private ArrayList<DataClass> dataList;
    private Context context;
    private double totalSize = 0;

    private HashSet<String> processedUrls = new HashSet<>();

    FirebaseStorage storage = FirebaseStorage.getInstance();

    public RecyclerAdapter(Context context, ArrayList<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(dataList.get(position).getImageURL()).into(holder.recyclerImage);
        Log.d("RECYCLER ADAPTER", "LOADING IMAGE NO." + position);
        holder.recyclerCaption.setText(dataList.get(position).getCaption());
        getImageSize(dataList.get(position).getImageURL());

    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public Context getContext() {
        return context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView recyclerImage;
        TextView recyclerCaption;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerImage = itemView.findViewById(R.id.recyclerImage);
            recyclerCaption = itemView.findViewById(R.id.recyclerCaption);
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
                Log.d("RECYCLER ADAPTER", "Image size: " + sizeInMB + " MB");
                Log.d("RECYCLER ADAPTER", "Total size: " + totalSize + " MB");
                processedUrls.add(imageUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    public void deleteRecycler(int position) {
        if (position >= 0 && position < dataList.size()) {

            dataList.remove(position);

            notifyItemRemoved(position);
        }
    }

    public void updateRecyclerTitle(int position) {
        if (position >= 0 && position < dataList.size()) {
            DataClass data = dataList.get(position);
            data.setTitle("Updated Title");
            notifyItemChanged(position);
        }
    }
}

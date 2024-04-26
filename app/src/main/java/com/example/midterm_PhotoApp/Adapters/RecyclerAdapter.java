package com.example.midterm_PhotoApp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.ListResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private ArrayList<DataClass> dataList;
    private Context context;

    private static final int BATCH_SIZE = 20;
    private int startIndex = 0;


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
        holder.recyclerCaption.setText(dataList.get(position).getCaption());

        // Load the image for this position
        String imageUrl = dataList.get(position).getImageURL();
        Glide.with(context).load(imageUrl).into(holder.recyclerImage);
        Log.d("RECYCLER ADAPTER", "LOADING IMAGE NO." + position);
        Log.d("RECYCLER ADAPTER", "LOADING IMAGE URL " + imageUrl);

        holder.recyclerImage.setVisibility(View.VISIBLE); // Make the ImageView visible

        // Check if it's time to fetch the next batch of images
        if (position == startIndex + BATCH_SIZE - 1) {
            fetchNextBatch();
        }
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
            DataClass data = dataList.get(position);
            String imageUrl = data.getImageURL();

            // Remove the item from the list
            dataList.remove(position);

            // Delete the image from Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d("RecyclerAdapter", "Image deleted successfully from Firebase Storage");

                    // Remove the imageUrl from processedUrls after successful deletion
                    processedUrls.remove(imageUrl);
                    Log.d("RecyclerAdapter", "Image URL removed from processedUrls: " + imageUrl);

                    // Clear the cache if the image is stored locally
                    clearCacheForImageUrl(imageUrl);

                    // Notify the adapter about the item removal
                    notifyItemRemoved(position);

                    // Notify the adapter data set has changed
                    notifyDataSetChanged();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // An error occurred!
                    Log.e("RecyclerAdapter", "Error deleting image from Firebase Storage", exception);
                }
            });
        }
    }

    private void clearCacheForImageUrl(String imageUrl) {
        // Determine the cache directory where the image might be stored
        File cacheDir = context.getCacheDir();

        // Construct the file path of the cached image based on the imageUrl
        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        File cachedImageFile = new File(cacheDir, fileName);

        // Check if the cached image file exists and delete it
        if (cachedImageFile.exists()) {
            if (cachedImageFile.delete()) {
                Log.d("RecyclerAdapter", "Cached image deleted successfully: " + cachedImageFile.getAbsolutePath());
            } else {
                Log.e("RecyclerAdapter", "Failed to delete cached image: " + cachedImageFile.getAbsolutePath());
            }
        }
    }

    public void updateRecyclerTitle(int position) {
        if (position >= 0 && position < dataList.size()) {
            DataClass data = dataList.get(position);
            data.setTitle("Updated Title");
            notifyItemChanged(position);
        }
    }
    
    private void sendBroadcast(List<String> imageUrls) {
        Intent broadcastIntent = new Intent("IMAGE_FETCH_COMPLETE");
        broadcastIntent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUrls));
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }

    private void fetchImageUrls(int startIndex) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Images");
        storageRef.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                if (task.isSuccessful()) {
                    List<String> imageUrls = new ArrayList<>();
                    List<StorageReference> items = task.getResult().getItems();
                    int endIndex = Math.min(startIndex + BATCH_SIZE, items.size());
                    for (int i = startIndex; i < endIndex; i++) {
                        StorageReference item = items.get(i);
                        item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> uriTask) {
                                if (uriTask.isSuccessful()) {
                                    Uri uri = uriTask.getResult();
                                    imageUrls.add(uri.toString());
                                    if (imageUrls.size() == BATCH_SIZE) {
                                        sendBroadcast(imageUrls);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void fetchNextBatch(){
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Find the index of the next image URL that hasn't been processed yet
                int nextImageIndex = getNextImageIndex();
                // Fetch the next batch starting from the next image URL
                fetchImageUrls(nextImageIndex);
            }
        });
    }

    private int getNextImageIndex() {
        // Find the index of the next image URL that hasn't been processed yet
        for (int i = startIndex; i < dataList.size(); i++) {
            String imageUrl = dataList.get(i).getImageURL();
            if (!processedUrls.contains(imageUrl)) {
                // Update startIndex to the next unprocessed image index
                startIndex = i + 1;
                return i;
            }
        }
        // If we reach the end of the data list, reset the startIndex and try again
        startIndex = 0;
        return getNextImageIndex();
    }

}

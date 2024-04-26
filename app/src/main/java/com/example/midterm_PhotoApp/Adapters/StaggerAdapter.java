package com.example.midterm_PhotoApp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StaggerAdapter extends RecyclerView.Adapter<StaggerAdapter.MyViewHolder> {
    ArrayList<DataClass> dataList;
    Context context;
    private double totalSize = 0;
    private static final int BATCH_SIZE = 20;

    private int startIndex = 0;



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
        // Load the image for this position
        String imageUrl = dataList.get(position).getImageURL();
        Glide.with(context).load(imageUrl).into(holder.staggeredImages);
        Log.d("STAGGER ADAPTER", "LOADING IMAGE NO." + position);
        holder.staggeredImages.setVisibility(View.VISIBLE); // Make the ImageView visible

        // Check if it's time to fetch the next batch of images
        if (position == dataList.size() - 1 && position % BATCH_SIZE == 0) {
            fetchNextBatch();
        }

        holder.staggeredImages.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(imageUrl, position);
            return true;
        });
    }

    private void sendBroadcast(List<String> imageUrls) {
        Intent broadcastIntent = new Intent("IMAGE_FETCH_COMPLETE");
        broadcastIntent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUrls));
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
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

            }
        });
    }
    public Context getContext(){
        return context;
    }

    private void showDeleteConfirmationDialog(String imageUrl, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Image");
        builder.setMessage("Are you sure you want to delete this image?");
        builder.setPositiveButton("Yes", (dialog, which) -> deleteImageFromStorageAndDatabase(imageUrl, position));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteImageFromStorageAndDatabase(String imageUrl, int position) {
        // Get references to Firebase Storage and Realtime Database
        FirebaseStorage storage = FirebaseStorage.getInstance();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Images");

        // Delete image from storage
        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image deleted successfully
                Log.d("STAGGER ADAPTER", "Image deleted successfully.");

                // Remove the image data from the adapter's list
                removeImageFromDataList(position);

                // Query the database for the record with the matching imageUrl
                Query query = databaseRef.orderByChild("imageURL").equalTo(imageUrl);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Delete the specific record
                            snapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("STAGGER ADAPTER", "Error querying data in Firebase Realtime Database", databaseError.toException());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // An error occurred!
                Log.e("STAGGER ADAPTER", "Error deleting image from Firebase Storage", exception);
            }
        });
    }


    private void removeImageFromDataList(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, dataList.size());
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




}

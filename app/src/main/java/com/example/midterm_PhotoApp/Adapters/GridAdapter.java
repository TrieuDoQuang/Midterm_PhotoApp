package com.example.midterm_PhotoApp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashSet;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GridAdapter extends BaseAdapter {
    private ArrayList<DataClass> dataList;
    private Context context;
    LayoutInflater layoutInflater;
    private static final int BATCH_SIZE = 20;
    private int startIndex = 0;


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
        if (layoutInflater == null){
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (view == null){
            view = layoutInflater.inflate(R.layout.grid_item, null);
        }

        ImageView gridImage = view.findViewById(R.id.gridImage);
        TextView gridCaption = view.findViewById(R.id.gridCaption);

        // Load the image for this position
        String imageUrl = dataList.get(i).getImageURL();
        Glide.with(context).load(imageUrl).into(gridImage);
        Log.d("GRID ADAPTER", "LOADING IMAGE NO." + i);

        gridCaption.setText(dataList.get(i).getCaption());

        // Check if it's time to fetch the next batch of images
        if (i == dataList.size() - 1 && i % BATCH_SIZE == 0) {
            fetchNextBatch();
        }

        gridImage.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(imageUrl);
            return true;
        });

        return view;
    }
    private void sendBroadcast(List<String> imageUrls) {
        Intent broadcastIntent = new Intent("IMAGE_FETCH_COMPLETE");
        broadcastIntent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUrls));
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
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

    private void showDeleteConfirmationDialog(String imageUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this image?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImageFromStorage(imageUrl);
            }
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteImageFromStorage(String imageUrl) {
        StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("GRID ADAPTER", "Image deleted successfully.");
                removeImageFromDataList(imageUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("GRID ADAPTER", "Failed to delete image.", exception);
            }
        });
    }

    private void removeImageFromDataList(String imageUrl) {
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i).getImageURL().equals(imageUrl)) {
                dataList.remove(i);
                break;
            }
        }
        notifyDataSetChanged(); // Refresh the grid view
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

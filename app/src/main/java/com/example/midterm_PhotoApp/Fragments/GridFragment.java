package com.example.midterm_PhotoApp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.midterm_PhotoApp.Adapters.GridAdapter;
import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GridFragment extends Fragment {
    private GridView gridView;
    private ArrayList<DataClass> dataList;
    private GridAdapter adapter;
    private DatabaseReference databaseReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grid,container,false);
        Log.d("GRID VIEW", "CONNECTING TO FIREBASE");
        databaseReference = FirebaseDatabase.getInstance().getReference("Images");
        Log.d("GRID VIEW", "CONNECTING TO FIREBASE");
        gridView = view.findViewById(R.id.gridView);
        dataList = new ArrayList<>();
        adapter = new GridAdapter(getContext(), dataList);
        gridView.setAdapter(adapter);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                Log.d("GRID VIEW", "GRID VIEW IS FETCHING URLS");
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    dataList.add(dataClass);
                }
                Log.d("GRID VIEW", "GRID VIEW HAS FINISHED FETCHING URLS");
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return view;
    }
}
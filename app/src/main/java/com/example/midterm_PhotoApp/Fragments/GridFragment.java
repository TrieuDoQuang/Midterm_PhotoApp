package com.example.midterm_PhotoApp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
    final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_grid,container,false);

        gridView = view.findViewById(R.id.gridView);
        dataList = new ArrayList<>();
        adapter = new GridAdapter(getContext(), dataList);
        gridView.setAdapter(adapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    dataList.add(dataClass);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return view;
    }
}
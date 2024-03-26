package com.example.semsemgallery.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.semsemgallery.fragments.PictureViewFragment;
import com.example.semsemgallery.models.Picture;

import java.util.ArrayList;

public class PictureAdapter extends FragmentStateAdapter {
    private ArrayList<Picture> pictures;
    private int position;
    public PictureAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<Picture> pictures, int position) {
        super(fragmentActivity);
        this.pictures = pictures;
        this.position = position;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Picture picture = pictures.get(position);
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putParcelable("picture", picture);

        PictureViewFragment pictureView = new PictureViewFragment();
        pictureView.setArguments(args);
        return pictureView;
    }

    @Override
    public int getItemCount() {
        if(pictures != null) {
            return pictures.size();
        }
        return 0;
    }
}
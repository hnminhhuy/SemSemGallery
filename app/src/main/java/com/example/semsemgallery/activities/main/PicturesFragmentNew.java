package com.example.semsemgallery.activities.main;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.main2.adapter.GalleryAdapter;
import com.example.semsemgallery.activities.main2.viewholder.DateHeaderItem;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class PicturesFragmentNew extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    Uri finalUri;
    private Context context;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getExtras() != null) {
                        try {
                            InputStream inputStream = getActivity().getContentResolver().openInputStream(finalUri);
                            Bitmap highQualityBitmap = BitmapFactory.decodeStream(inputStream);
                            createImage(getActivity().getApplicationContext(), highQualityBitmap, finalUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // Image capture failed or was canceleds
                    Toast.makeText(getActivity(), "Image capture failed", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }

    private boolean isDateEqual(Date value1, Date value2) {
        // Create calendar instances for both dates
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(value1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(value2);

        // Clear time parts from both calendars
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        // Compare the dates
        return cal1.equals(cal2);
    }

    private final TreeSet<GalleryItem> galleryItems = new TreeSet<>(Comparator.reverseOrder());
    private final TreeSet<Long> header = new TreeSet<>(Comparator.reverseOrder());
    private List<GalleryItem> dataList = null;
    private ObservableGridMode<GalleryItem> observableGridMode = null;
    private GalleryAdapter adapter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.picture_recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL); // Adjust the span count as needed
        recyclerView.setLayoutManager(layoutManager);


        PictureLoader loader = new PictureLoader(context) {
            @Override
            public void onProcessUpdate(Picture... pictures) {
                galleryItems.add(new GalleryItem(pictures[0]));
                header.add(pictures[0].getDateInMillis());
            }

            @Override
            public void postExecute(Boolean res) {
                List<Long> temp = new ArrayList<>(header);
//
//                for (Long i : temp) {
//                    Log.e("Picture", "H" + i + " - " + new Date( i * 1000 * 86400).toString());
//                }

                for (Long item : temp) {
                    galleryItems.add(new GalleryItem(new DateHeaderItem(new Date(item * (1000 * 86400)))));
                }
                dataList = new ArrayList<>(galleryItems);
//                for(GalleryItem item: dataList) {
//                    if (item.getData() instanceof Picture) {
//                        Log.d("Picture", "P - " + item.getTime());
//                    } else {
//                        Log.e("Picture", "H - " + item.getTime());
//                    }
//                }
                observableGridMode = new ObservableGridMode<>(dataList, GridMode.NORMAL);
                adapter = new GalleryAdapter(context, observableGridMode, null);
                recyclerView.setAdapter(adapter);

            }
        };

        loader.execute(PictureLoadMode.ALL.toString());

        return view;
    }

    public static void createImage(Context context, Bitmap bitmap, Uri finalUri) {
        OutputStream outputStream;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            outputStream = contentResolver.openOutputStream(Objects.requireNonNull(finalUri));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Objects.requireNonNull(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

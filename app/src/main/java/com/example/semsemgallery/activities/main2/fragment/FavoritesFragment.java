package com.example.semsemgallery.activities.main2.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.semsemgallery.R;
import com.example.semsemgallery.activities.base.GridMode;
import com.example.semsemgallery.activities.base.GridModeEvent;
import com.example.semsemgallery.activities.base.GridModeListener;
import com.example.semsemgallery.activities.base.ObservableGridMode;
import com.example.semsemgallery.activities.cloudbackup.CloudActivity;
import com.example.semsemgallery.activities.main2.MainActivity;
import com.example.semsemgallery.activities.main2.adapter.FavoriteAdapter;
import com.example.semsemgallery.activities.main2.viewholder.GalleryItem;
import com.example.semsemgallery.activities.pictureview.PictureViewActivity;
import com.example.semsemgallery.activities.search.SearchViewActivity;
import com.example.semsemgallery.domain.Picture.GarbagePictureCollector;
import com.example.semsemgallery.domain.Picture.PictureLoadMode;
import com.example.semsemgallery.domain.Picture.PictureLoader;
import com.example.semsemgallery.models.Picture;
import com.example.semsemgallery.domain.MediaRetriever;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements GridModeListener {
    private final String TAG = "FavoritesFragment";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    private Context context;
    private ObservableGridMode<Picture> data;
    private FavoriteAdapter adapter;
    private PictureLoader loader;
    private LinearLayout actionBar;
    private MaterialToolbar selectingTopBar;
    private MaterialToolbar topBar;
    private boolean isSelectingAll = false;
    private MainActivity mainActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        data = new ObservableGridMode<>(null, GridMode.NORMAL);
        adapter = new FavoriteAdapter(context, data);
        loader = new PictureLoader(context) {
            @Override
            public void onProcessUpdate(Picture... pictures) {
                data.addData(pictures[0]);
                adapter.notifyItemInserted(data.getDataSize() - 1);
            }

            @Override
            public void postExecute(Boolean res) {

            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        data.addObserver(this);
        data.setMaster(this);
        mainActivity.getOnBackPressedDispatcher().addCallback(mainActivity, backHandler);
    }

    private OnBackPressedCallback backHandler = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Log.d("BackPressed", data.getCurrentMode().toString());
            if (data.getCurrentMode() == GridMode.SELECTING) {
                data.fireSelectionChangeForAll(false);
                data.setGridMode(GridMode.NORMAL);
                isSelectingAll = false;
            } else {
                // If not in selecting mode, finish the activity
                mainActivity.finish();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.gallery_recycler);
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        topBar = view.findViewById(R.id.topAppBar);
        selectingTopBar = view.findViewById(R.id.selecting_top_bar);
        actionBar = view.findViewById(R.id.action_bar);
        loader.execute(PictureLoadMode.FAVORITE.toString());
        if (auth.getCurrentUser() == null) {
            Menu menu = topBar.getMenu();
            menu.removeItem(R.id.cloud);
        }
        topBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.search) {
                startActivity(new Intent(getActivity().getApplicationContext(), SearchViewActivity.class));
                return true;
            } else if (item.getItemId() == R.id.cloud) {
                startActivity(new Intent(getActivity().getApplicationContext(), CloudActivity.class));
                return true;
            } else if (item.getItemId() == R.id.edit) {
                data.setGridMode(GridMode.SELECTING);
                return true;
            } else if (item.getItemId() == R.id.select_all) {
                data.setGridMode(GridMode.SELECTING);
                data.fireSelectionChangeForAll(true);
                isSelectingAll = false;
                return true;
            } else return false;
        });
        SetFunctionForActionBar();
        return view;
    }

    @Override
    public void onModeChange(GridModeEvent event) {
        mainActivity.sendMsgToMain(TAG, event.getGridMode().toString());
        if (event.getGridMode() == GridMode.NORMAL) {
            actionBar.setVisibility(View.GONE);
            topBar.setVisibility(View.VISIBLE);
            selectingTopBar.setVisibility(View.INVISIBLE);
        } else if (event.getGridMode() == GridMode.SELECTING) {
            actionBar.setVisibility(View.VISIBLE);
            topBar.setVisibility(View.INVISIBLE);
            selectingTopBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSelectionChange(GridModeEvent event) {
        long quantity = data.getNumberOfSelected();
        selectingTopBar.setTitle(quantity == 0 ? "Select items" : quantity + "selected");
    }

    @Override
    public void onSelectingAll(GridModeEvent event) {
        if (event.getNewSelectionForAll()) {
            selectingTopBar.setTitle(data.getDataSize() + " selected");
        } else {
            selectingTopBar.setTitle(String.valueOf(0) + " selected");
        }
    }

    private void renderMoreMenu(View v, int res) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.inflate(res);
        MenuItem btnSelectAll = popupMenu.getMenu().findItem(R.id.btnSelectAll);
        if (isSelectingAll) btnSelectAll.setTitle(getString(R.string.unselect_all));
        else btnSelectAll.setTitle(R.string.select_all);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.btnMoveToAlbum) {
                //#TODO
                return true;
            } else if (id == R.id.btnCopyToAlbum) {
                //#TODO
                return true;
            } else if (id == R.id.btnSelectAll) {
                isSelectingAll = !isSelectingAll;
                data.fireSelectionChangeForAll(isSelectingAll);
                return true;
            } else return false;
        });
        popupMenu.show();
    }

    private AlertDialog createDialog(String titleText, boolean isCancel, View.OnClickListener cancelCallback) {
        //=Prepare dialog
        View dialogView = getLayoutInflater().inflate(R.layout.component_loading_dialog, null);
        TextView title = dialogView.findViewById(R.id.component_loading_dialog_title);
        title.setText(titleText);
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(context).setView(dialogView);

        AlertDialog loadingDialog = dialogBuilder.create();
        loadingDialog.setCanceledOnTouchOutside(false);

        Button cancelButton = dialogView.findViewById(R.id.component_loading_dialog_cancelButton);
        cancelButton.setVisibility(isCancel ? View.VISIBLE : View.INVISIBLE);
        if (cancelCallback != null) {
            cancelButton.setOnClickListener(cancelCallback);
        }
        return loadingDialog;
    }

    private void ProcessTrashPicture() {
        AlertDialog loadingDialog = createDialog("Moving images to Trash", false, null);
        //== Prepare data and handler
        List<ObservableGridMode<Picture>.DataItem> temp = data.getSelectedDataItem();
        Long[] deleteIds = new Long[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            deleteIds[i] = temp.get(i).data.getPictureId();
        }
        final boolean[] canExecute = {false};
        boolean isStorageManager = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            isStorageManager = Environment.isExternalStorageManager();
        }

        if (isStorageManager) {
            // Your app already has storage management permissions
            // You can proceed with file operations
            canExecute[0] = true;
        } else {
            // Your app does not have storage management permissions
            // Guide the user to the system settings page to grant permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);

            startActivity(intent);
        }

        GarbagePictureCollector.TrashPictureHandler collector = new GarbagePictureCollector.TrashPictureHandler(getContext()) {
            @Override
            public void preExecute(Long... longs) {
                Log.i("TrashImage", "Prepare trash");
                loadingDialog.show();
            }

            @Override
            public void onProcessUpdate(Integer... integers) {
                if (integers == null) return;
                Log.i("TrashImage", "Trashed " + integers[0] + " / " + temp.size());
                int index = data.getObservedObjects().indexOf(temp.get(integers[0] - 1));
                data.getObservedObjects().remove(temp.get(integers[0] - 1));
                adapter.notifyItemRemoved(index);
                ProgressBar progressBar = loadingDialog.findViewById(R.id.component_loading_dialog_progressBar);
                assert progressBar != null;
                progressBar.setProgress((integers[0] * 100) / temp.size());

            }

            @Override
            public void postExecute(Void res) {
                Log.i("TrashImage", "Completely trashed");
                data.setGridMode(GridMode.NORMAL);
                isSelectingAll = false;
                loadingDialog.dismiss();
            }
        };
        if (canExecute[0]) collector.execute(deleteIds);
        else Toast.makeText(context, "Don't have permission", Toast.LENGTH_LONG);
    }

    private final View.OnClickListener trashPicture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (data.getNumberOfSelected() == 0) return;
            //= Loading dialog (without cancellation)
            MaterialAlertDialogBuilder confirmDialog = new MaterialAlertDialogBuilder(context)
                    .setTitle("Move " + data.getNumberOfSelected() + " to Trash?")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("Move to Trash", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProcessTrashPicture();
                        }
                    });
            confirmDialog.show();
        }
    };

    private void SetFunctionForActionBar() {
        Button btnDelete = actionBar.findViewById(R.id.btnDelete);
        Button btnShare = actionBar.findViewById(R.id.btnShare);
        Button btnAddTag = actionBar.findViewById(R.id.btnAddTag);
        Button btnMore = actionBar.findViewById(R.id.btnMore);
        btnDelete.setOnClickListener(this.trashPicture);
        btnShare.setOnClickListener(v -> {
            if (data.getNumberOfSelected() == 0) return;

            //== Retrieve the data for sharing
            List<Picture> pictures = data.getSelectedItems();
            ArrayList<Uri> shareFiles = new ArrayList<>();
            for (Picture item : pictures) {
                File shareFile = new File(item.getPath());
                Uri shareUri = FileProvider.getUriForFile(
                        context.getApplicationContext(),
                        context.getApplicationContext().getPackageName() + ".provider",
                        shareFile
                );
                shareFiles.add(shareUri);
            }

            //==Start the new activity
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType("image/*");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareFiles);
            startActivity(Intent.createChooser(shareIntent, "Share images via..."));

        });

        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnMore.setOnClickListener((v) -> {
            renderMoreMenu(v, R.menu.pictures_fragment_selecting_mode);
        });
    }
}

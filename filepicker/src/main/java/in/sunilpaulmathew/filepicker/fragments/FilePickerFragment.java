package in.sunilpaulmathew.filepicker.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.Objects;

import in.sunilpaulmathew.filepicker.R;
import in.sunilpaulmathew.filepicker.adapters.RecycleViewAdapter;
import in.sunilpaulmathew.filepicker.utils.FilePicker;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 15, 2021
 */
public class FilePickerFragment extends Fragment {

    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private RecycleViewAdapter mRecycleViewAdapter;

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_filepicker, container, false);

        AppCompatImageButton mBack = mRootView.findViewById(R.id.back);
        mTitle = mRootView.findViewById(R.id.title);
        AppCompatImageButton mSortButton = mRootView.findViewById(R.id.sort);
        FilePicker.initializeSelectCard(mRootView, R.id.select);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mBack.setOnClickListener(v -> finish());

        if (!FilePicker.isInternalPath() && FilePicker.isPermissionDenied(requireActivity())) {
            LinearLayout mPermissionLayout = mRootView.findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = mRootView.findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = mRootView.findViewById(R.id.permission_text);
            mPermissionText.setText(getString(Build.VERSION.SDK_INT >= 30 ? R.string.file_permission_request_message
                    : R.string.file_permission_request_message_legacy, FilePicker.getAppName(requireContext().getPackageName(),
                    requireActivity())));
            mPermissionText.setTextColor(FilePicker.isDarkTheme(requireActivity()) ? getResources().getColor(R.color.colorWhite) : getResources().getColor(R.color.colorBlack));
            mTitle.setText(getString(R.string.please_note));
            mSortButton.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            mPermissionLayout.setVisibility(View.VISIBLE);
            mPermissionGrant.setOnClickListener(v -> FilePicker.requestPermission(requireActivity()));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), FilePicker.getOrientation(requireActivity()) == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1));
            mRecycleViewAdapter = new RecycleViewAdapter(FilePicker.getData(requireActivity()));
            mRecyclerView.setAdapter(mRecycleViewAdapter);

            mTitle.setText(FilePicker.isStorageRoot() ? "Storage Root" : new File(FilePicker.getPath()).getName().toUpperCase());

            mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
                String mPath = FilePicker.getData(requireActivity()).get(position);
                if (new File(mPath).isDirectory()) {
                    FilePicker.setPath(mPath);
                    reload(requireActivity());
                } else {
                    if (FilePicker.isMultiFileMode() && FilePicker.isSupportedMultiFile(mPath)) {
                        if (FilePicker.getSelectedFilesList().contains(mPath)) {
                            FilePicker.getSelectedFilesList().remove(mPath);
                        } else {
                            FilePicker.getSelectedFilesList().add(mPath);
                        }
                        mRecycleViewAdapter.notifyItemChanged(position);
                        FilePicker.getSelectCard().setVisibility(FilePicker.getSelectedFilesList().isEmpty() ? View.GONE : View.VISIBLE);
                    } else {
                        Intent intent = new Intent();
                        FilePicker.setSelectedFilePath(mPath);
                        requireActivity().setResult(0, intent);
                        finish();
                    }
                }
            });

            mSortButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(requireActivity(), mSortButton);
                Menu menu = popupMenu.getMenu();
                menu.add(Menu.NONE, 0, Menu.NONE, "A-Z").setCheckable(true)
                        .setChecked(FilePicker.getBoolean("az_order", true, requireActivity()));
                popupMenu.setOnMenuItemClickListener(item -> {
                    FilePicker.saveBoolean("az_order", !FilePicker.getBoolean("az_order", true, requireActivity()), requireActivity());
                    reload(requireActivity());
                    return false;
                });
                popupMenu.show();
            });

            FilePicker.getSelectCard().setOnClickListener(v -> {
                Intent intent = new Intent();
                requireActivity().setResult(0, intent);
                finish();
            });
        }

        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void handleOnBackPressed() {
                if (FilePicker.isStorageRoot()) {
                    finish();
                } else {
                    FilePicker.setPath(Objects.requireNonNull(new File(FilePicker.getPath()).getParentFile()).getPath());
                    reload(requireActivity());
                }
            }
        });

        return mRootView;
    }

    private void finish() {
        FilePicker.setExtension(null);
        FilePicker.setPath(null);
        requireActivity().finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("StaticFieldLeak")
    private void reload(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mRecycleViewAdapter = new RecycleViewAdapter(FilePicker.getData(activity));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mTitle.setText(FilePicker.isStorageRoot() ? "Storage Root" : new File(FilePicker.getPath()).getName().toUpperCase());
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                if (FilePicker.isMultiFileMode()) {
                    FilePicker.getSelectedFilesList().clear();
                    FilePicker.getSelectCard().setVisibility(View.GONE);
                }
            }
        }.execute();
    }
    
}
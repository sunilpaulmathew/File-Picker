package in.sunilpaulmathew.filepicker.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.filepicker.R;
import in.sunilpaulmathew.filepicker.adapters.FilePickerAdapter;
import in.sunilpaulmathew.filepicker.utils.FilePicker;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 15, 2021
 */
public class FilePickerFragment extends Fragment {

    private MaterialTextView mTitle;
    private RecyclerView mRecyclerView;
    private FilePickerAdapter mRecycleViewAdapter;

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

        mTitle.setTextColor(FilePicker.getAccentColor(requireActivity()));
        mBack.setColorFilter(FilePicker.getAccentColor(requireActivity()));
        mSortButton.setColorFilter(FilePicker.getAccentColor(requireActivity()));

        mBack.setOnClickListener(v -> finish());

        if (!FilePicker.isInternalPath() && FilePicker.isPermissionDenied(requireActivity())) {
            LinearLayout mPermissionLayout = mRootView.findViewById(R.id.permission_layout);
            MaterialCardView mPermissionGrant = mRootView.findViewById(R.id.grant_card);
            MaterialTextView mPermissionText = mRootView.findViewById(R.id.permission_text);
            mPermissionText.setText(getString(Build.VERSION.SDK_INT >= 29 ? R.string.file_permission_request_message
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
            mRecycleViewAdapter = new FilePickerAdapter(FilePicker.getData(requireActivity()));
            mRecyclerView.setAdapter(mRecycleViewAdapter);

            mTitle.setText(FilePicker.isStorageRoot(requireActivity()) ? "Storage Root" : new File(FilePicker.getPath(requireActivity())).getName().toUpperCase());

            mRecycleViewAdapter.setOnItemClickListener((position, v) -> {
                String mPath = FilePicker.getData(requireActivity()).get(position);
                File mPathFile = new File(mPath);
                if (mPathFile.isDirectory()) {
                    FilePicker.saveString("path", mPath, requireActivity());
                    reload(requireActivity());
                } else {
                    if (FilePicker.isMultiFileMode() && FilePicker.isSupportedMultiFile(mPath)) {
                        if (FilePicker.getSelectedFilesList().contains(mPathFile)) {
                            FilePicker.getSelectedFilesList().remove(mPathFile);
                        } else {
                            FilePicker.getSelectedFilesList().add(mPathFile);
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
                if (FilePicker.isStorageRoot(requireActivity())) {
                    finish();
                } else {

                    FilePicker.saveString("path", Objects.requireNonNull(new File(FilePicker.getPath(requireActivity())).getParentFile()).getPath(), requireActivity());
                    reload(requireActivity());
                }
            }
        });

        return mRootView;
    }

    private void finish() {
        requireActivity().finish();
    }

    private void reload(Activity activity) {
        ExecutorService executors = Executors.newSingleThreadExecutor();
        executors.execute(() -> {
            mRecycleViewAdapter = new FilePickerAdapter(FilePicker.getData(activity));
            new Handler(Looper.getMainLooper()).post(() -> {
                mTitle.setText(FilePicker.isStorageRoot(requireActivity()) ? "Storage Root" : new File(
                        FilePicker.getPath(requireActivity())).getName().toUpperCase());
                mRecyclerView.setAdapter(mRecycleViewAdapter);
                if (FilePicker.isMultiFileMode()) {
                    FilePicker.getSelectedFilesList().clear();
                    FilePicker.getSelectCard().setVisibility(View.GONE);
                }
                if (!executors.isShutdown()) executors.shutdown();
            });
        });
    }
    
}
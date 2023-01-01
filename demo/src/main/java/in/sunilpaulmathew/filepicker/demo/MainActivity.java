package in.sunilpaulmathew.filepicker.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import in.sunilpaulmathew.filepicker.utils.FilePicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialCardView mCard = findViewById(R.id.demo_card);

        mCard.setOnClickListener(v -> {
            FilePicker filePicker = new FilePicker(filePickerResultLauncher, this);
            filePicker.setAccentColor(Integer.MIN_VALUE);
            filePicker.setExtension(null);
            filePicker.setMultiFileMode(false, null);
            filePicker.setPath(null, false);
            filePicker.launch();
        });
    }

    ActivityResultLauncher<Intent> filePickerResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && (FilePicker.getSelectedFile() != null
                        && FilePicker.getSelectedFile().exists() || FilePicker.getSelectedFilesList() != null)) {
                    File mSelectedFile = null;
                    StringBuilder sb = new StringBuilder();
                    if (FilePicker.getSelectedFile() != null && FilePicker.getSelectedFile().exists()) {
                        mSelectedFile = FilePicker.getSelectedFile();
                        sb.append("1. ").append(mSelectedFile.getName());
                    } else if (FilePicker.getSelectedFilesList() != null && FilePicker.getSelectedFilesList().get(0).exists()) {
                        mSelectedFile = FilePicker.getSelectedFilesList().get(0);
                        for (int i = 0; i < FilePicker.getSelectedFilesList().size(); i++ ) {
                            sb.append(i + 1).append(" ").append(FilePicker.getSelectedFilesList().get(i).getName()).append("\n");
                        }
                    }
                    if (mSelectedFile != null) {
                        new MaterialAlertDialogBuilder(this)
                                .setMessage(getString(R.string.selected_files_message,sb.toString()))
                                .setPositiveButton(getString(R.string.cancel), (dialogInterface, i) -> {
                                    // Do your task
                                }).show();
                    }
                }
            }
    );

}
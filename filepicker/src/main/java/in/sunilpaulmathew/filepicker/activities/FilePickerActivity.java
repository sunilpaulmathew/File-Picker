package in.sunilpaulmathew.filepicker.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import in.sunilpaulmathew.filepicker.R;
import in.sunilpaulmathew.filepicker.fragments.FilePickerFragment;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 15, 2021
 */
public class FilePickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new FilePickerFragment()).commit();
    }

}
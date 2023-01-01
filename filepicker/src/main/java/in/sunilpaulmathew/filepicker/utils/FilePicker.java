package in.sunilpaulmathew.filepicker.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.filepicker.R;
import in.sunilpaulmathew.filepicker.activities.FilePickerActivity;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 15, 2021
 */
public class FilePicker {

    private static ActivityResultLauncher<Intent> mResult = null;
    private static int mAccentColor = Integer.MIN_VALUE;
    public Context mContext;
    private static boolean mMultiFileMode = false, mInternalPath = false;
    private static List<File> mSelectedFiles = new ArrayList<>();
    private static MaterialCardView mSelectCard = null;
    private static String mExtension = null, mPath = null, mSelectedFileExtension = null, mSelectedFilePath = null;

    public FilePicker(ActivityResultLauncher<Intent> result, Context context) {
        mResult = result;
        mContext = context;
    }

    public static List<String> getData(Activity activity) {
        List<String> mData = new ArrayList<>(), mDir = new ArrayList<>(), mFiles = new ArrayList<>();
        try {
            // Add directories
            for (File mFile : Objects.requireNonNull(new File(getPath(activity)).listFiles())) {
                if (mFile.isDirectory()) {
                    mDir.add(mFile.getAbsolutePath());
                }
            }
            Collections.sort(mDir, String.CASE_INSENSITIVE_ORDER);
            if (!getBoolean("az_order", true, activity)) {
                Collections.reverse(mDir);
            }
            mData.addAll(mDir);
            // Add files
            for (File mFile : Objects.requireNonNull(new File(getPath(activity)).listFiles())) {
                if (mFile.isFile() && isSupportedFile(mFile.getAbsolutePath())) {
                    mFiles.add(mFile.getAbsolutePath());
                }
            }
            Collections.sort(mFiles, String.CASE_INSENSITIVE_ORDER);
            if (!getBoolean("az_order", true, activity)) {
                Collections.reverse(mFiles);
            }
            mData.addAll(mFiles);
        } catch (NullPointerException ignored) {
            activity.finish();
        }
        return mData;
    }

    private static ApplicationInfo getAppInfo(String packageName, Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (Exception ignored) {
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static boolean isImageFile(String path) {
        return path.endsWith(".bmp") || path.endsWith(".png") || path.endsWith(".jpg");
    }

    public static boolean isSupportedFile(String path) {
        if (mExtension == null) {
            return true;
        } else {
            if (!mExtension.startsWith(".")) {
                mExtension = "." + mExtension;
            }
            return path.endsWith(mExtension);
        }
    }

    public static boolean isSupportedMultiFile(String path) {
        if (mSelectedFileExtension == null) {
            return true;
        } else {
            if (!mSelectedFileExtension.startsWith(".")) {
                mSelectedFileExtension = "." + mSelectedFileExtension;
            }
            return path.endsWith(mSelectedFileExtension);
        }
    }

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static boolean isStorageRoot(Context context) {
        return getPath(context).equals(Environment.getExternalStorageDirectory().toString());
    }

    public static boolean isMultiFileMode() {
        return mMultiFileMode;
    }

    public static boolean isInternalPath() {
        return mInternalPath;
    }

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    public static boolean isPermissionDenied(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return !Environment.isExternalStorageManager();
        } else {
            String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
            return (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
        }
    }

    public static Drawable getAPKIcon(String path, Context context) {
        if (context.getPackageManager().getPackageArchiveInfo(path, 0) != null) {
            return context.getPackageManager().getPackageArchiveInfo(path, 0).applicationInfo.loadIcon(context.getPackageManager());
        } else {
            return null;
        }
    }

    public static Drawable getDrawable(int drawable, Context context) {
        return ContextCompat.getDrawable(context, drawable);
    }

    public static List<File> getSelectedFilesList() {
        return mSelectedFiles;
    }

    public static int getAccentColor(Context context) {
        if (mAccentColor != Integer.MIN_VALUE) {
            return mAccentColor;
        } else {
            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
            return value.data;
        }
    }

    public static int getOrientation(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode() ?
                Configuration.ORIENTATION_PORTRAIT : activity.getResources().getConfiguration().orientation;
    }

    public static String getPath(Context context) {
        if (new File(getString("path", Environment.getExternalStorageDirectory().toString(), context)).exists()) {
            return getString("path", Environment.getExternalStorageDirectory().toString(), context);
        } else if (mPath != null && new File(mPath).exists()) {
            return mPath;
        } else {
            return Environment.getExternalStorageDirectory().toString();
        }
    }

    public static String getSelectedFileExtension() {
        return mSelectedFileExtension;
    }

    public static File getSelectedFile() {
        if (mSelectedFilePath == null) {
            return null;
        }
        return new File(mSelectedFilePath);
    }

    public static String getFileSize(String path) {
        long mSize = new File(path).length() / 1024;
        long mDecimal = (new File(path).length() - 1024) / 1024;
        if (mSize > 1024) {
            return mSize / 1024 + "." + mDecimal + " MB";
        } else {
            return mSize  + " KB";
        }
    }

    public static String getString(String name, String defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(name, defaults);
    }

    public static String getAppName(String packageName, Context context) {
        return context.getPackageManager().getApplicationLabel(Objects.requireNonNull(
                getAppInfo(packageName, context))).toString();
    }

    public static String getAPKId(String apkPath, Context context) {
        PackageInfo pi = context.getPackageManager().getPackageArchiveInfo(apkPath, 0);
        if (pi != null) {
            return pi.applicationInfo.packageName;
        } else {
            return null;
        }
    }

    public static Uri getImageURI(String path) {
        File mFile = new File(path);
        if (mFile.exists()) {
            return Uri.fromFile(mFile);
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public static void setFileIcon(AppCompatImageButton icon, Drawable drawable, Context context) {
        icon.setImageDrawable(drawable);
        icon.setColorFilter(isDarkTheme(context) ? context.getResources().getColor(R.color.colorWhite) :
                context.getResources().getColor(R.color.colorBlack));
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public static void saveString(String name, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(name, value).apply();
    }

    public static void setSelectedFilePath(String path) {
        mSelectedFilePath = path;
    }

    public static void initializeSelectCard(View view, int id) {
        mSelectCard = view.findViewById(id);
        mSelectCard.setCardBackgroundColor(getAccentColor(view.getContext()));
    }

    public static MaterialCardView getSelectCard() {
        return mSelectCard;
    }

    @RequiresApi(api = Build.VERSION_CODES.DONUT)
    public static void requestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
            activity.finish();
        } else {
            ActivityCompat.requestPermissions(activity, new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            activity.finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void launch() {
        saveString("path", mPath, mContext);
        mSelectedFilePath = null;
        mSelectedFiles = new ArrayList<>();
        Intent intent = new Intent(mContext, FilePickerActivity.class);
        mResult.launch(intent);
    }

    public void setAccentColor(int color) {
        mAccentColor = color;
    }

    public void setExtension(String ext) {
        mExtension = ext;
    }

    public void setMultiFileMode(boolean enabled, String ext) {
        mMultiFileMode = enabled;
        mSelectedFileExtension = ext;
    }

    public void setPath(String path, boolean internalPath) {
        mPath = path;
        mInternalPath = internalPath;
    }

}
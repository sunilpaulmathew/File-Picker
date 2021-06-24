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
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;

import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import in.sunilpaulmathew.filepicker.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 15, 2021
 */
public class FilePicker {

    private static boolean mMultiFileMode = false, mInternalPath = false;
    private static final List<String> mSelectedFiles = new ArrayList<>();
    private static MaterialCardView mSelectCard = null;
    private static String mExtension = null, mPath = null, mSelectedFileExtension = null, mSelectedFilePath = null;

    public static List<String> getData(Activity activity) {
        List<String> mData = new ArrayList<>(), mDir = new ArrayList<>(), mFiles = new ArrayList<>();
        try {
            // Add directories
            for (File mFile : Objects.requireNonNull(new File(getPath()).listFiles())) {
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
            for (File mFile : Objects.requireNonNull(new File(getPath()).listFiles())) {
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
        return getExtFromPath(path).equals(".bmp") || getExtFromPath(path).equals(".png") || getExtFromPath(path).equals(".jpg");
    }

    public static boolean isSupportedFile(String path) {
        if (getExtension() == null) {
            return true;
        } else {
            return getExtFromPath(path).equals(getExtension());
        }
    }

    public static boolean isSupportedMultiFile(String path) {
        if (getSelectedFileExtension() != null) {
            return getExtFromPath(path).equals(getSelectedFileExtension());
        }
        return false;
    }

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static boolean isStorageRoot() {
        return getPath().equals(Environment.getExternalStorageDirectory().toString());
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

    public static List<String> getSelectedFilesList() {
        return mSelectedFiles;
    }

    public static int getOrientation(Activity activity) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && activity.isInMultiWindowMode() ?
                Configuration.ORIENTATION_PORTRAIT : activity.getResources().getConfiguration().orientation;
    }

    public static String getPath() {
        if (mPath == null) {
            return Environment.getExternalStorageDirectory().toString();
        } else {
            return mPath;
        }
    }

    public static String getExtension() {
        return mExtension;
    }

    public static String getSelectedFileExtension() {
        return mSelectedFileExtension;
    }

    public static File getSelectedFile() {
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

    public static String getExtFromPath(String path) {
        return android.webkit.MimeTypeMap.getFileExtensionFromUrl(path);
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

    public static void setPath(String path) {
        mPath = path;
    }

    public static void setExtension(String extension) {
        mExtension = extension;
    }

    public static void setSelectedFileExtension(String extension) {
        mSelectedFileExtension = extension;
    }

    public static void setMultiFileMode(boolean enabled) {
        mMultiFileMode = enabled;
    }

    public static void isInternalPath(boolean enabled) {
        mInternalPath = enabled;
    }

    public static void setSelectedFilePath(String path) {
        mSelectedFilePath = path;
    }

    public static MaterialCardView initializeSelectCard(View view, int id) {
        return mSelectCard = view.findViewById(id);
    }

    public static MaterialCardView getSelectCard() {
        return mSelectCard;
    }

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

}
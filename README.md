# File Picker

![](https://img.shields.io/github/languages/top/sunilpaulmathew/File-Picker)
![](https://img.shields.io/github/contributors/sunilpaulmathew/File-Picker)
![](https://img.shields.io/github/license/sunilpaulmathew/File-Picker)

A simple Android library to pick files from device storage.

## Pre-requirements

Declared the following permissions in the manifest file of app:
* android.permission.WRITE_EXTERNAL_STORAGE
* android.permission.MANAGE_EXTERNAL_STORAGE (if File Picker need to be used in Build.VERSION.SDK_INT >= 29)

## Download

Step 1: Add it in your root-level build.gradle at the end of repositories:
```
allprojects {
        repositories {
                ...
                maven { url 'https://jitpack.io' }
        }
}
```

Step 2: Add dependency to the app-level build.gradle:
```
dependencies {
        implementation 'com.github.sunilpaulmathew:File-Picker:Tag'
}
```
*Please Note: **Tag** should be replaced with the latest **[commit id](https://github.com/sunilpaulmathew/File-Picker/commits/main)**.*

## Tutorial

### Launch File Picker

```
            FilePicker filePicker = new FilePicker(
                    activityResultLauncher /* in which the results handled. usage: mandatory */,
                    this /* your activity or context. usage: mandatory */
            );
            /*
               Apply custom accent color for file picker.
               usage: optional; default: ContextCompat.getColor(this, R.color.colorBlu
               */
            filePicker.setAccentColor(Integer.MIN_VALUE);
            /*
               Target specific file extension.
               usage: optional; default: null
               */
            filePicker.setExtension(null);
            /*
              Set the first parameter to true to pick multiple files.
              Specify file extension as the second parameter to target specific file types.
              usage: optional; default: false, null
              */
            filePicker.setMultiFileMode(false, null);
            /*
               The first parameter specifies the path to open when launching the file picker.
               Set the second parameter to true only if the specified path is within app's internal
               storage so that no special permissions are required for accessing them.
               usage: optional; default: null, false
               */
            filePicker.setPath(null, false);
            filePicker.launch();
```

### & do something with the selected file (FilePicker.getSelectedFile()) on activityResultLauncher

```
ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        if (result.getData() != null && (FilePicker.getSelectedFile() != null && FilePicker.getSelectedFile().exists() || FilePicker.getSelectedFilesList() != null)) {
                    // Do something with the selected file(s)
                    File mSelectedFile = FilePicker.getSelectedFile();
                    List<File> mSelectedFilesList = FilePicker.getSelectedFilesList()();                   
                }
    }
);
```

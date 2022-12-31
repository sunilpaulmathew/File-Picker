package in.sunilpaulmathew.filepicker.adapters;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.filepicker.R;
import in.sunilpaulmathew.filepicker.utils.FilePicker;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 15, 2021
 */
public class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.ViewHolder> {

    private static ClickListener clickListener;

    private final List<String> data;

    public FilePickerAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_filepicker, parent, false);
        return new ViewHolder(rowItem);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (new File(this.data.get(position)).isDirectory()) {
            holder.mIcon.setImageDrawable(FilePicker.getDrawable(R.drawable.ic_folder, holder.mTitle.getContext()));
            holder.mIcon.setBackground(FilePicker.getDrawable(R.drawable.ic_circle, holder.mIcon.getContext()));
            holder.mIcon.setColorFilter(FilePicker.getAccentColor(holder.mTitle.getContext()));
            holder.mDescription.setVisibility(View.GONE);
            holder.mSize.setVisibility(View.GONE);
            if (FilePicker.isMultiFileMode()) {
                holder.mCheckBox.setVisibility(View.GONE);
            }
        } else {
            if (FilePicker.isImageFile(data.get(position))) {
                if (FilePicker.getImageURI(data.get(position)) != null) {
                    holder.mIcon.setImageURI(FilePicker.getImageURI(data.get(position)));
                } else {
                    FilePicker.setFileIcon(holder.mIcon, FilePicker.getDrawable(R.drawable.ic_image, holder.mIcon.getContext()), holder.mIcon.getContext());
                }
            } else if (data.get(position).endsWith("apk")) {
                if (FilePicker.getAPKIcon(data.get(position), holder.mIcon.getContext()) != null) {
                    holder.mIcon.setImageDrawable(FilePicker.getAPKIcon(data.get(position), holder.mIcon.getContext()));
                } else {
                    FilePicker.setFileIcon(holder.mIcon, FilePicker.getDrawable(R.drawable.ic_android, holder.mIcon.getContext()), holder.mIcon.getContext());
                }
                if (FilePicker.getAPKId(data.get(position), holder.mIcon.getContext()) != null) {
                    holder.mDescription.setText(FilePicker.getAPKId(data.get(position), holder.mIcon.getContext()));
                    holder.mDescription.setVisibility(View.VISIBLE);
                }
            } else if (data.get(position).endsWith("xml")) {
                FilePicker.setFileIcon(holder.mIcon, FilePicker.getDrawable(R.drawable.ic_xml, holder.mIcon.getContext()), holder.mIcon.getContext());
            } else if (data.get(position).endsWith("zip")) {
                FilePicker.setFileIcon(holder.mIcon, FilePicker.getDrawable(R.drawable.ic_archive, holder.mIcon.getContext()), holder.mIcon.getContext());
            } else {
                FilePicker.setFileIcon(holder.mIcon, FilePicker.getDrawable(R.drawable.ic_file, holder.mIcon.getContext()), holder.mIcon.getContext());
            }
            holder.mIcon.setBackground(null);
            holder.mSize.setText(FilePicker.getFileSize(this.data.get(position)));
            holder.mSize.setVisibility(View.VISIBLE);
            if (FilePicker.isMultiFileMode()) {
                File mFile = new File(this.data.get(position));
                holder.mCheckBox.setChecked(FilePicker.getSelectedFilesList().contains(mFile));
                holder.mCheckBox.setOnClickListener(v -> {
                    if (FilePicker.getSelectedFilesList().contains(mFile)) {
                        FilePicker.getSelectedFilesList().remove(mFile);
                    } else {
                        FilePicker.getSelectedFilesList().add(mFile);
                    }
                    FilePicker.getSelectCard().setVisibility(FilePicker.getSelectedFilesList().isEmpty() ? View.GONE : View.VISIBLE);
                });

                if (FilePicker.getSelectedFileExtension() != null) {
                    if (data.get(position).endsWith(FilePicker.getSelectedFileExtension())) {
                        holder.mCheckBox.setVisibility(View.VISIBLE);
                    } else {
                        holder.mCheckBox.setVisibility(View.GONE);
                    }
                } else {
                    holder.mCheckBox.setVisibility(View.VISIBLE);
                }
            }
        }
        holder.mTitle.setText(new File(this.data.get(position)).getName());
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final AppCompatImageButton mIcon;
        private final MaterialTextView mTitle, mDescription, mSize;
        private final MaterialCheckBox mCheckBox;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mSize = view.findViewById(R.id.size);
            this.mCheckBox = view.findViewById(R.id.checkbox);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        FilePickerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }
    
}
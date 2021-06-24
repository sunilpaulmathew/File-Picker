package in.sunilpaulmathew.filepicker.adapters;

import android.annotation.SuppressLint;
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
public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

    private static ClickListener clickListener;

    private final List<String> data;

    public RecycleViewAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_filepicker, parent, false);
        return new ViewHolder(rowItem);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (new File(this.data.get(position)).isDirectory()) {
            holder.mIcon.setImageDrawable(holder.mTitle.getContext().getResources().getDrawable(R.drawable.ic_folder));
            holder.mIcon.setBackground(holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_circle));
            holder.mIcon.setColorFilter(holder.mTitle.getContext().getResources().getColor(R.color.colorWhite));
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
                    FilePicker.setFileIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_image), holder.mIcon.getContext());
                }
            } else if (FilePicker.getExtFromPath(data.get(position)).equals("apk")) {
                if (FilePicker.getAPKIcon(data.get(position), holder.mIcon.getContext()) != null) {
                    holder.mIcon.setImageDrawable(FilePicker.getAPKIcon(data.get(position), holder.mIcon.getContext()));
                } else {
                    FilePicker.setFileIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_android), holder.mIcon.getContext());
                }
                if (FilePicker.getAPKId(data.get(position), holder.mIcon.getContext()) != null) {
                    holder.mDescription.setText(FilePicker.getAPKId(data.get(position), holder.mIcon.getContext()));
                    holder.mDescription.setVisibility(View.VISIBLE);
                }
            } else if (FilePicker.getExtFromPath(data.get(position)).equals("xml")) {
                FilePicker.setFileIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_xml), holder.mIcon.getContext());
            } else if (FilePicker.getExtFromPath(data.get(position)).equals("zip")) {
                FilePicker.setFileIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_archive), holder.mIcon.getContext());
            } else {
                FilePicker.setFileIcon(holder.mIcon, holder.mIcon.getContext().getResources().getDrawable(R.drawable.ic_file), holder.mIcon.getContext());
            }
            holder.mIcon.setBackground(null);
            holder.mSize.setText(FilePicker.getFileSize(this.data.get(position)));
            holder.mSize.setVisibility(View.VISIBLE);
            if (FilePicker.isMultiFileMode()) {
                holder.mCheckBox.setChecked(FilePicker.getSelectedFilesList().contains(this.data.get(position)));
                holder.mCheckBox.setOnClickListener(v -> {
                    if (FilePicker.getSelectedFilesList().contains(this.data.get(position))) {
                        FilePicker.getSelectedFilesList().remove(this.data.get(position));
                    } else {
                        FilePicker.getSelectedFilesList().add(this.data.get(position));
                    }
                    FilePicker.getSelectCard().setVisibility(FilePicker.getSelectedFilesList().isEmpty() ? View.GONE : View.VISIBLE);
                });

                if (FilePicker.getSelectedFileExtension() != null) {
                    if (FilePicker.getExtFromPath(data.get(position)).equals(FilePicker.getSelectedFileExtension())) {
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
        RecycleViewAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }
    
}
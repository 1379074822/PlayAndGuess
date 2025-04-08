package com.example.playguess.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playguess.R;
import com.example.playguess.model.WordLibrary;

import java.util.List;

public class WordLibraryAdapter extends RecyclerView.Adapter<WordLibraryAdapter.ViewHolder> {
    
    private static final int TYPE_CREATE_NEW = 0;
    private static final int TYPE_LIBRARY_ITEM = 1;
    
    private List<WordLibrary> libraries;
    private OnItemClickListener listener;
    private Context context;
    private int selectedPosition = 0; // 默认选中"添加新词库"
    
    public WordLibraryAdapter(Context context, List<WordLibrary> libraries) {
        this.context = context;
        this.libraries = libraries;
    }
    
    public void setData(List<WordLibrary> libraries) {
        this.libraries = libraries;
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_CREATE_NEW : TYPE_LIBRARY_ITEM;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_library, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            // "添加新词库"选项
            holder.textViewLibraryTitle.setText(R.string.create_new_library);
        } else {
            // 实际词库项
            WordLibrary library = libraries.get(position - 1);
            holder.textViewLibraryTitle.setText(library.getTitle());
        }
        
        // 设置选中状态
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_bg));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousSelected = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                
                // 更新选中状态
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                
                if (listener != null) {
                    if (selectedPosition == 0) {
                        // 点击"添加新词库"
                        listener.onCreateNewClicked();
                    } else {
                        // 点击词库项
                        listener.onItemClick(libraries.get(selectedPosition - 1));
                    }
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        // 添加"添加新词库"选项，所以总数+1
        return libraries != null ? libraries.size() + 1 : 1;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public int getSelectedPosition() {
        return selectedPosition;
    }
    
    public void setSelectedPosition(int position) {
        if (position >= 0 && position < getItemCount()) {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
        }
    }
    
    public interface OnItemClickListener {
        void onItemClick(WordLibrary library);
        void onCreateNewClicked();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLibraryTitle;
        
        ViewHolder(View itemView) {
            super(itemView);
            textViewLibraryTitle = itemView.findViewById(R.id.textViewLibraryTitle);
        }
    }
} 
package com.example.playguess.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playguess.R;
import com.example.playguess.model.WordLibrary;

import java.util.ArrayList;
import java.util.List;

public class LibraryCardAdapter extends RecyclerView.Adapter<LibraryCardAdapter.ViewHolder> {

    private static final String TAG = "LibraryCardAdapter";
    
    private Context context;
    private List<WordLibrary> libraries;
    private List<WordLibrary> filteredLibraries;
    private OnLibraryClickListener listener;
    private int screenWidth;
    private int screenHeight;
    private float density;

    public LibraryCardAdapter(Context context, List<WordLibrary> libraries) {
        this.context = context;
        this.libraries = libraries;
        this.filteredLibraries = new ArrayList<>(libraries);
        
        // 获取屏幕尺寸
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        this.screenWidth = displayMetrics.widthPixels;
        this.screenHeight = displayMetrics.heightPixels;
        this.density = displayMetrics.density;
        
        Log.d(TAG, "初始化适配器 - 屏幕尺寸: " + screenWidth + "x" + screenHeight + 
             ", 密度: " + density + 
             ", 卡片尺寸: " + context.getResources().getDimensionPixelSize(R.dimen.library_card_width) + 
             "x" + context.getResources().getDimensionPixelSize(R.dimen.library_card_height));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_card, parent, false);
                
        // 确保卡片大小适应屏幕
        adjustCardSize(view);
        
        return new ViewHolder(view);
    }
    
    /**
     * 根据屏幕尺寸调整卡片大小
     */
    private void adjustCardSize(View itemView) {
        // 获取卡片理想宽高
        int cardWidth = context.getResources().getDimensionPixelSize(R.dimen.library_card_width);
        int cardHeight = context.getResources().getDimensionPixelSize(R.dimen.library_card_height);
        
        // 检查是否需要调整大小
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        
        // 确保卡片高度不超过屏幕高度的70%
        int maxHeight = (int)(screenHeight * 0.7);
        if (cardHeight > maxHeight) {
            float ratio = (float)cardWidth / cardHeight;
            cardHeight = maxHeight;
            cardWidth = (int)(cardHeight * ratio);
            
            Log.d(TAG, "卡片尺寸调整 - 原始: " + params.width + "x" + params.height + 
                  ", 新尺寸: " + cardWidth + "x" + cardHeight);
        }
        
        // 应用新的尺寸
        params.width = cardWidth;
        params.height = cardHeight;
        itemView.setLayoutParams(params);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WordLibrary library = filteredLibraries.get(position);
        holder.textViewLibraryTitle.setText(library.getTitle());
        holder.textViewWordCount.setText(
                context.getString(R.string.word_count, library.getWords().size()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onLibraryClick(library);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredLibraries.size();
    }

    public void setData(List<WordLibrary> libraries) {
        this.libraries = libraries;
        this.filteredLibraries = new ArrayList<>(libraries);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredLibraries.clear();
        if (query.isEmpty()) {
            filteredLibraries.addAll(libraries);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (WordLibrary library : libraries) {
                if (library.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredLibraries.add(library);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface OnLibraryClickListener {
        void onLibraryClick(WordLibrary library);
    }

    public void setOnLibraryClickListener(OnLibraryClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLibraryTitle;
        TextView textViewWordCount;

        ViewHolder(View itemView) {
            super(itemView);
            textViewLibraryTitle = itemView.findViewById(R.id.textViewLibraryTitle);
            textViewWordCount = itemView.findViewById(R.id.textViewWordCount);
        }
    }
} 
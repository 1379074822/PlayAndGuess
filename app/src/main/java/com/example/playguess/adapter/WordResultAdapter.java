package com.example.playguess.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playguess.R;
import com.example.playguess.model.WordResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏结果适配器，用于在结算页面显示词语结果
 */
public class WordResultAdapter extends RecyclerView.Adapter<WordResultAdapter.ResultViewHolder> {

    private static final String TAG = "WordResultAdapter";
    private Context context;
    private List<WordResult> wordResults;

    public WordResultAdapter(Context context) {
        this.context = context;
        this.wordResults = new ArrayList<>();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 使用卡片视图来提高可视性
        CardView cardView = new CardView(context);
        cardView.setCardElevation(4f);
        cardView.setRadius(8f);
        cardView.setUseCompatPadding(true);
        
        // 设置卡片内部的文本视图
        TextView textView = new TextView(context);
        textView.setPadding(32, 16, 32, 16);
        textView.setTextSize(18);
        
        // 设置卡片布局参数
        CardView.LayoutParams layoutParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(8, 8, 8, 8);
        
        // 将文本视图添加到卡片中
        cardView.addView(textView, layoutParams);
        
        return new ResultViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        if (position < wordResults.size()) {
            WordResult result = wordResults.get(position);
            
            // 获取卡片中的文本视图
            TextView textView = holder.getTextView();
            
            // 显示词语
            textView.setText(result.getWord());
            
            // 根据结果设置不同背景和文字颜色
            CardView cardView = (CardView) holder.itemView;
            if (result.isCorrect()) {
                cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")); // 淡绿色背景
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else if (result.isSkipped()) {
                cardView.setCardBackgroundColor(Color.parseColor("#FFF3E0")); // 淡橙色背景
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // 淡红色背景
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
            }
        }
    }

    @Override
    public int getItemCount() {
        return wordResults == null ? 0 : wordResults.size();
    }

    /**
     * 设置词语结果数据
     */
    public void setData(List<WordResult> wordResults) {
        this.wordResults = wordResults != null ? wordResults : new ArrayList<>();
        notifyDataSetChanged();
        
        // 打印一下数据情况，帮助调试
        Log.d(TAG, "设置数据: 总词语数=" + this.wordResults.size() 
            + ", 猜对数=" + getCorrectCount() 
            + ", 跳过数=" + getSkippedCount());
    }

    /**
     * 获取猜对的词语数量
     */
    public int getCorrectCount() {
        int count = 0;
        for (WordResult result : wordResults) {
            if (result.isCorrect()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取跳过的词语数量
     */
    public int getSkippedCount() {
        int count = 0;
        for (WordResult result : wordResults) {
            if (result.isSkipped()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 打印所有词语结果，用于调试
     */
    public void logAllResults() {
        Log.d(TAG, "所有词语结果: ");
        for (int i = 0; i < wordResults.size(); i++) {
            WordResult result = wordResults.get(i);
            Log.d(TAG, i + ": " + result.getWord() + 
                  " 正确=" + result.isCorrect() + 
                  " 跳过=" + result.isSkipped());
        }
    }

    /**
     * 结果视图持有者
     */
    class ResultViewHolder extends RecyclerView.ViewHolder {
        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        
        /**
         * 获取卡片中的文本视图
         */
        public TextView getTextView() {
            CardView cardView = (CardView) itemView;
            return (TextView) cardView.getChildAt(0);
        }
    }
} 
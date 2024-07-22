package com.example.vibora.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.R;
import com.example.vibora.utils.CalendarUtils;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener) {
        this.days = days;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ADAPTER", "OnCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if(days.size() > 15) // Month View
            layoutParams.height = (int) (parent.getHeight() * 0.1666666666);
        else
            layoutParams.height = (int) parent.getHeight();
        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        final LocalDate date = days.get(position);
        if(date == null)
            holder.dayOfMonth.setText("");
        else{
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));
            if(date.isBefore(LocalDate.now()))
                holder.dayOfMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.separator_gray));
            if(date.equals(CalendarUtils.selectedDate))
                holder.calendar_cell_parent.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.main_green));
        }

    }

    public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final ArrayList<LocalDate> days;
        final View calendar_cell_parent;
        final TextView dayOfMonth;
        OnItemListener onItemListener;

        public CalendarViewHolder(@NonNull View itemView, OnItemListener onItemListener, ArrayList<LocalDate> days) {
            super(itemView);
            this.days = days;
            dayOfMonth = itemView.findViewById(R.id.cell_day_text);
            calendar_cell_parent = itemView.findViewById(R.id.calendar_cell);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getBindingAdapterPosition(), days.get(getBindingAdapterPosition()));
        }
    }

    public interface OnItemListener{
        void onItemClick(int position, LocalDate date);
    }
}

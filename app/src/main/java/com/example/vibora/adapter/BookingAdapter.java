package com.example.vibora.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.R;
import com.example.vibora.model.TimeSlotModel;
import com.example.vibora.utils.CalendarUtils;

import java.util.ArrayList;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.timeSlotViewHolder> {
    Context context;
    ArrayList<TimeSlotModel> time_slots_list;
    OnTimeSlotClickListener listener;

    public BookingAdapter(Context context, ArrayList<TimeSlotModel> time_slots_list, OnTimeSlotClickListener listener) {
        this.context = context;
        this.time_slots_list = time_slots_list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public timeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_cell, parent, false);
        return new timeSlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull timeSlotViewHolder holder, int position) {
        TimeSlotModel item = time_slots_list.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return time_slots_list.size();
    }

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlotModel timeSlot);
    }

    public class timeSlotViewHolder extends RecyclerView.ViewHolder{
        TextView time_slot_text, reserved_spots_xxxx, reserved_spots_text;
        View itemView, statusBar;

        public timeSlotViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            time_slot_text = itemView.findViewById(R.id.time_slot_text);
            reserved_spots_xxxx = itemView.findViewById(R.id.reserved_spots_xxxx);
            reserved_spots_text = itemView.findViewById(R.id.reserved_spots_text);
            statusBar = itemView.findViewById(R.id.time_slot_status);
        }

        public void bind(TimeSlotModel time_slot){
            time_slot_text.setText(CalendarUtils.mapIndexToTimeSlot(time_slot.getIndex()));
            time_slot_text.setTypeface(ResourcesCompat.getFont(context, R.font.baloo_bhai));
            String xxxx = "";
            for (int i = 0; i < time_slot.getReserved_spots(); i++) xxxx = xxxx + "X  ";
            reserved_spots_xxxx.setText(xxxx);
            reserved_spots_xxxx.setTypeface(ResourcesCompat.getFont(context, R.font.baloo_bhai));
            if(time_slot.isReserved() && time_slot.getReserved_spots() < 4) reserved_spots_text.setText(" L ");
            else reserved_spots_text.setText(time_slot.getReserved_spots() + " / 4");
            reserved_spots_text.setTypeface(ResourcesCompat.getFont(context, R.font.baloo_bhai));
            if(time_slot.getReserved_spots() == 0) statusBar.setBackgroundColor(context.getResources().getColor(R.color.main_green));
            else if(time_slot.isReserved()) statusBar.setBackgroundColor(context.getResources().getColor(R.color.red));
            else statusBar.setBackgroundColor(context.getResources().getColor(R.color.yellow));

            if(time_slot.isReserved()) itemView.setBackgroundTintList(context.getResources().getColorStateList(R.color.light_gray));
            else itemView.setOnClickListener(v -> listener.onTimeSlotClick(time_slot));
        }
    }
}

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
import com.example.vibora.model.BookingModel;
import com.example.vibora.utils.CalendarUtils;

import java.util.ArrayList;

public class MyBookingsAdapter extends RecyclerView.Adapter<MyBookingsAdapter.BookingViewHolder> {
    Context context;
    ArrayList<BookingModel> mybookings_list;
    OnBookingClickListener listener;

    public MyBookingsAdapter(Context context, ArrayList<BookingModel> mybookings_list, OnBookingClickListener listener) {
        this.context = context;
        this.mybookings_list = mybookings_list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.mybooking_element, parent, false);
        return new MyBookingsAdapter.BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingModel item = mybookings_list.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mybookings_list.size();
    }

    public interface OnBookingClickListener {
        void OnBookingClick(BookingModel timeSlot);
    }

    public class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView field_name_text, booking_date, booking_time_slot, reserved_spots;
        View status_bar;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            field_name_text = itemView.findViewById(R.id.field_name_text);
            booking_date = itemView.findViewById(R.id.booking_date);
            booking_time_slot = itemView.findViewById(R.id.booking_time_slot);
            reserved_spots = itemView.findViewById(R.id.reserved_spots_text);
            status_bar = itemView.findViewById(R.id.booking_status);
        }

        public void bind(BookingModel bookingModel) {
            field_name_text.setText(bookingModel.getFieldId());
            field_name_text.setTypeface(ResourcesCompat.getFont(context, R.font.baloo_bhai));
            booking_date.setText(CalendarUtils.formattedDate(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate())));
            booking_date.setTypeface(ResourcesCompat.getFont(context, R.font.baloo_bhai));
            booking_time_slot.setText(CalendarUtils.mapIndexToTimeSlot(bookingModel.getTimeSlot()));
            booking_time_slot.setTypeface(ResourcesCompat.getFont(context, R.font.baloo_bhai));
            reserved_spots.setText(bookingModel.getUserIdList().size() + " / 4");
            reserved_spots.setTypeface(ResourcesCompat.getFont(context, R.font.baloo_bhai));
            status_bar.setBackgroundColor(context.getResources().getColor(R.color.yellow));
            if(bookingModel.isReserved()) status_bar.setBackgroundColor(context.getResources().getColor(R.color.red));
            if(CalendarUtils.isCompletedMatch(bookingModel)) itemView.setBackgroundTintList(context.getResources().getColorStateList(R.color.light_gray));

            itemView.setOnClickListener(v -> listener.OnBookingClick(bookingModel));
        }
    }
}

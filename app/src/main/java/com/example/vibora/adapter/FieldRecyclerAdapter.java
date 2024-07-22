package com.example.vibora.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vibora.BookingWeek;
import com.example.vibora.LearnBookingWeek;
import com.example.vibora.MainActivity;
import com.example.vibora.R;
import com.example.vibora.model.BookingModel;
import com.example.vibora.model.FieldModel;
import com.example.vibora.model.TimeSlotModel;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FieldRecyclerAdapter extends FirestoreRecyclerAdapter<FieldModel, FieldRecyclerAdapter.FieldModelViewHolder> {
    Context context;

    public FieldRecyclerAdapter(@NonNull FirestoreRecyclerOptions<FieldModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FieldModelViewHolder viewHolder, int i, @NonNull FieldModel fieldModel) {
        viewHolder.fieldName.setText(fieldModel.getName());
        viewHolder.status_indicator.setBackgroundColor(ContextCompat.getColor(context, R.color.main_green));

        FirebaseUtils.dailyBookingsCollectionReference(fieldModel.getFieldId(), CalendarUtils.formattedDate(LocalDate.now())).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int occupied_slot_counter = 0;
                        List<TimeSlotModel> available_slots = new ArrayList<>();
                        for(int s = 0; s <= 7; s++) available_slots.add(new TimeSlotModel(s, 0));
                        Log.d("FieldRecyclerAdapter", "AvailableSlots -> " + String.valueOf(available_slots));
                        QuerySnapshot snapshots = task.getResult();
                        for (DocumentSnapshot doc : snapshots) {
                            BookingModel bookingModel = doc.toObject(BookingModel.class);
                            int s;
                            for(s = 0; s <= available_slots.size(); s++){
                                if(available_slots.get(s).getIndex() == bookingModel.getTimeSlot()){
                                    available_slots.get(s).setReserved_spots(bookingModel.getUserIdList().size());
                                    break;
                                }
                            }
                            if(bookingModel.isReserved() || isPast(bookingModel)){
                                occupied_slot_counter += 1;
                                available_slots.remove(s);
                                Log.d("AVSLOTS", "Removing " + bookingModel.getTimeSlot());
                            }
                        }

                        Log.d("FieldRecyclerAdapter", "AvailableSlots -> " + String.valueOf(available_slots));

                        viewHolder.occupied_slots.setText(occupied_slot_counter + " / 8");
                        if(occupied_slot_counter >= 7) viewHolder.status_indicator.setBackgroundColor(context.getResources().getColor(R.color.red));
                        else if(occupied_slot_counter >= 3) viewHolder.status_indicator.setBackgroundColor(context.getResources().getColor(R.color.yellow));
                        int minTimeSlot = 7;

                        removePastTimeSlots(available_slots);

                        if(available_slots.size() == 0) return;

                        int minIndex = 0;
                        for(int x = 0; x < available_slots.size(); x++){
                            TimeSlotModel ts = available_slots.get(x);
                            if(ts.getIndex() < minTimeSlot){
                                minTimeSlot = ts.getIndex();
                                minIndex = x;
                            }
                        }

                        viewHolder.first_available_slot.setText("1° Available Slot:\n" + CalendarUtils.mapIndexToTimeSlot(minTimeSlot));
                        FirebaseUtils.getFieldImageStorageRef("field" + available_slots.get(minIndex).getReserved_spots() + ".png").getDownloadUrl()
                                .addOnCompleteListener(task2 -> {
                                    if(task2.isSuccessful()){
                                        Uri uri = task2.getResult();
                                        Activity activity = (Activity) context;
                                        if(!activity.isFinishing() && !activity.isDestroyed()) Glide.with(context).load(uri).into(viewHolder.imageView);
                                    }
                                });
                    }
                });

        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent;
            if(context instanceof MainActivity)
                intent = new Intent(context, BookingWeek.class);
            else intent = new Intent(context, LearnBookingWeek.class);

            intent.putExtra("FIELD_NAME", fieldModel.getName());
            intent.putExtra("FIELD_ID", fieldModel.getFieldId());
            context.startActivity(intent);
        });
    }

    private boolean isPast(BookingModel bookingModel) {
        if(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate()).isBefore(LocalDate.now())) return true;
        if(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate()).isEqual(LocalDate.now())){
            LocalTime slotStartTime = LocalTime.of(9, 0).plusMinutes(90 * bookingModel.getTimeSlot());
            if(slotStartTime.isBefore(LocalTime.now())) return true;
        }
        return false;
    }

    private static void removePastTimeSlots(List<TimeSlotModel> timeSlots) {
        Iterator<TimeSlotModel> iterator = timeSlots.iterator();
        while (iterator.hasNext()) {
            TimeSlotModel timeSlot = iterator.next();
            String startTimeString = CalendarUtils.mapIndexToTimeSlot(timeSlot.getIndex()).split(" - ")[0];
            LocalTime startTime = LocalTime.parse(startTimeString.replace(".", ":"));
            if (startTime.isBefore(LocalTime.now())) {
                iterator.remove();
            }
        }
    }

    @NonNull
    @Override
    public FieldModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.field_list_item, parent, false);
        return new FieldModelViewHolder(view);
    }

    public class FieldModelViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView fieldName, occupied_slots, first_available_slot;
        View status_indicator;
        public FieldModelViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.field_image);
            fieldName = itemView.findViewById(R.id.field_name);
            occupied_slots = itemView.findViewById(R.id.occupied_slots_number);
            first_available_slot = itemView.findViewById(R.id.next_available_slot);
            status_indicator = itemView.findViewById(R.id.field_status_indicator);
        }
    }
}

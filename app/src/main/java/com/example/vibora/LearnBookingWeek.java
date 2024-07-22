package com.example.vibora;

import static com.example.vibora.utils.CalendarUtils.daysInWeekArray;
import static com.example.vibora.utils.CalendarUtils.monthYearFromDate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.BookingAdapter;
import com.example.vibora.adapter.CalendarAdapter;
import com.example.vibora.model.BookingModel;
import com.example.vibora.model.TimeSlotModel;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;

public class LearnBookingWeek extends AppCompatActivity implements CalendarAdapter.OnItemListener, BookingAdapter.OnTimeSlotClickListener{
    String FIELD_NAME = "", FIELD_ID = "";
    TextView monthYearText, field_name_text;
    RecyclerView calendarRecyclerView;
    RecyclerView time_slots_recycler;
    ArrayList<TimeSlotModel> time_slots_list;
    boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_learn_booking_week);

        FIELD_NAME = getIntent().getStringExtra("FIELD_NAME");
        FIELD_ID = getIntent().getStringExtra("FIELD_ID");

        initViews();

        CalendarUtils.selectedDate = LocalDate.now();
        setWeekView();
        setEventAdapter();
    }

    private void initViews() {
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view);
        monthYearText = findViewById(R.id.month_year_text);
        field_name_text = findViewById(R.id.event_field_text);
        field_name_text.setText("Booking Lesson Slot on " + FIELD_NAME);
        time_slots_recycler = findViewById(R.id.time_slots_recycler);
        Log.d("DEBUG", "Field ID: " + FIELD_ID);
    }

    //==============================================================================================

    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    public void previousWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    public void nextWeekAction(View view) {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if(date.isBefore(LocalDate.now())) return;

        CalendarUtils.selectedDate = date;
        setWeekView();
        setEventAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("EVENT", "OnResume()");
        if(!isFirstLoad) setEventAdapter();
        isFirstLoad = false;
    }

    private void setEventAdapter() {
        time_slots_list = new ArrayList<TimeSlotModel>();
        for(int i = 0; i < 8 ; i++) time_slots_list.add(new TimeSlotModel(i));

        FirebaseUtils.dailyBookingsCollectionReference(FIELD_ID, CalendarUtils.formattedDate(CalendarUtils.selectedDate)).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        for (DocumentSnapshot doc : snapshots) {
                            BookingModel bookingModel = doc.toObject(BookingModel.class);
                            int index = bookingModel.getTimeSlot();
                            ArrayList<String> users = bookingModel.getUserIdList();

                            time_slots_list.set(index, new TimeSlotModel(index, users.size(), bookingModel.isReserved()));
                        }
                    }
                    String timeslotslist = "";
                    for(TimeSlotModel ts:time_slots_list){
                        timeslotslist = timeslotslist + "(" + ts.getIndex() + ", " + ts.getReserved_spots() + ") ";
                    }
                    Log.d("ASDRUBALE", timeslotslist);

                    removePastTimeslots();
                    BookingAdapter bookingAdapter = new BookingAdapter(getApplicationContext(), time_slots_list, this);
                    time_slots_recycler.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                    time_slots_recycler.setAdapter(bookingAdapter);
                });

    }

    @Override
    public void onTimeSlotClick(TimeSlotModel timeSlot) {
        if(timeSlot.getReserved_spots() >= 4) return;

        Intent intent = new Intent(this, LearnBookingDialog.class);
        intent.putExtra("FIELD_NAME", FIELD_NAME);
        intent.putExtra("DATE", CalendarUtils.formattedDate(CalendarUtils.selectedDate));
        intent.putExtra("TIMESLOT", CalendarUtils.mapIndexToTimeSlot(timeSlot.getIndex()));
        startActivity(intent);
    }

    private void removePastTimeslots(){
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        Iterator<TimeSlotModel> iterator = time_slots_list.iterator();
        while (iterator.hasNext()) {
            TimeSlotModel ts = iterator.next();
            if (CalendarUtils.selectedDate.isEqual(today)) {
                LocalTime slotStartTime = LocalTime.of(9, 0).plusMinutes(90 * ts.getIndex());
                if(slotStartTime.isBefore(now)) {
                    iterator.remove();
                }
            }
        }
    }
}
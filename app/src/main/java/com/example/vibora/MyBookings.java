package com.example.vibora;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.adapter.MyBookingsAdapter;
import com.example.vibora.model.BookingModel;
import com.example.vibora.utils.CalendarUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyBookings extends AppCompatActivity implements MyBookingsAdapter.OnBookingClickListener{
    RecyclerView bookings_recycler;
    Button back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_bookings);
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_green));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setMyBookingsAdapter();

        back_btn.setOnClickListener(v -> {
            finish();
        });
    }

    private void initViews() {
        bookings_recycler = findViewById(R.id.bookings_recycler);
        back_btn = findViewById(R.id.back_btn);
    }

    //==============================================================================================

    private void setMyBookingsAdapter() {
        Log.d("MyBookingsDebug", "setMyBookingAdapter");
        ArrayList<BookingModel> mybookings_list = new ArrayList<>();

        FirebaseFirestore.getInstance().collectionGroup("bookings").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()){
                            Log.d("QueryDebug", doc.getId());
                            BookingModel bookingModel = doc.toObject(BookingModel.class);
                            if(!bookingModel.getUserIdList().contains(FirebaseUtils.currentUserId())) continue;

                            Log.d("QueryDebug", "MyBooking -> " + doc.getId());
                            if(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate()).isAfter(LocalDate.now().minusDays(7))) mybookings_list.add(bookingModel);
                        }

                        Collections.sort(mybookings_list, new Comparator<BookingModel>() {
                            @Override
                            public int compare(BookingModel bm1, BookingModel bm2) {
                                return bm1.getDate().compareTo(bm2.getDate());
                            }
                        });

                        MyBookingsAdapter adapter = new MyBookingsAdapter(getApplicationContext(), mybookings_list, MyBookings.this);
                        bookings_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        bookings_recycler.setAdapter(adapter);
                    }
                });
    }

    @Override
    public void OnBookingClick(BookingModel bookingModel) {
        Intent intent;
        Log.d("CalendarUtils", "isCompletedMatech -> " + CalendarUtils.isCompletedMatch(bookingModel));
        if(CalendarUtils.isCompletedMatch(bookingModel))
            intent = new Intent(this, MatchResult.class);
        else intent = new Intent(this, Booking.class);
        intent.putExtra("FIELD_NAME", bookingModel.getFieldId());
        intent.putExtra("FIELD_ID", bookingModel.getFieldId());
        intent.putExtra("DATE", CalendarUtils.formattedDate(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate())));
        intent.putExtra("TIMESLOT", CalendarUtils.mapIndexToTimeSlot(bookingModel.getTimeSlot()));
        intent.putExtra("RESERVED_SPOTS", bookingModel.getUserIdList().size());
        startActivity(intent);
    }
}
package com.example.vibora.utils;

import android.util.Log;

import com.example.vibora.model.BookingModel;
import com.google.firebase.Timestamp;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalendarUtils {
    public static LocalDate selectedDate;

    public static String formattedDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        return date.format(formatter);
    }

    public static LocalDate localDateFromString(String dateString){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        return LocalDate.parse(dateString, formatter);
    }

    public static Timestamp convertFromLocalDateToTimestamp(LocalDate localDate){
        Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return new Timestamp(date);
    }

    public static LocalDate convertFromTimestampToLocalDate(Timestamp timestamp){
        Instant instant = timestamp.toDate().toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static int mapTimeSlot(String timeSlot){
        switch (timeSlot) {
            case "09.00 - 10.30":
                return 0;
            case "10.30 - 12.00":
                return 1;
            case "12.00 - 13.30":
                return 2;
            case "13.30 - 15.00":
                return 3;
            case "15.00 - 16.30":
                return 4;
            case "16.30 - 18.00":
                return 5;
            case "18.00 - 19.30":
                return 6;
            case "19.30 - 21.00":
                return 7;
            default:
                return -1;
        }
    }

    public static String mapIndexToTimeSlot(int index) {
        switch (index) {
            case 0:
                return "09.00 - 10.30";
            case 1:
                return "10.30 - 12.00";
            case 2:
                return "12.00 - 13.30";
            case 3:
                return "13.30 - 15.00";
            case 4:
                return "15.00 - 16.30";
            case 5:
                return "16.30 - 18.00";
            case 6:
                return "18.00 - 19.30";
            case 7:
                return "19.30 - 21.00";
            default:
                return "Invalid index";
        }
    }

    public static String monthYearFromDate(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public static ArrayList<LocalDate> daysInMonthArray(LocalDate date) {
        ArrayList<LocalDate> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = CalendarUtils.selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i++){
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek){
                daysInMonthArray.add(null);
            }
            else daysInMonthArray.add(LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), i - dayOfWeek));
        }
        return daysInMonthArray;
    }

    public static ArrayList<LocalDate> daysInWeekArray(LocalDate date){
        ArrayList<LocalDate> days = new ArrayList<>();
        LocalDate current = SundayForDate(selectedDate);
        LocalDate endDate = current.plusWeeks(1);

        while (current.isBefore(endDate)){
            days.add(current);
            current = current.plusDays(1);
        }

        return days;
    }

    public static boolean isCompletedMatch(BookingModel bookingModel){
        Log.d("CalendarUtils", "booking date-> " + CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate()) + " , actual date -> " + LocalDate.now());
        if(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate()).isBefore(LocalDate.now())) return true;
        if(CalendarUtils.convertFromTimestampToLocalDate(bookingModel.getDate()).isAfter(LocalDate.now())) return false;

        String _endTime = CalendarUtils.mapIndexToTimeSlot(bookingModel.getTimeSlot()).split(" - ")[1];
        Log.d("CalendarUtils", "endTime -> " + _endTime);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH.mm");
        LocalTime endTime = LocalTime.parse(_endTime, timeFormatter);
        if(endTime.isBefore(LocalTime.now())) return true;

        return false;
    }

    private static LocalDate SundayForDate(LocalDate current) {
        LocalDate oneWeekAgo = current.minusWeeks(1);
        while (current.isAfter(oneWeekAgo)){
            if(current.getDayOfWeek() == DayOfWeek.SUNDAY) return current;
            current = current.minusDays(1);
        }
        return null;
    }
}

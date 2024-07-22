package com.example.vibora.model;

public class TimeSlotModel {
    int index;
    int reserved_spots;
    boolean reservedForLesson;

    public TimeSlotModel(int index) {
        this.index = index;
        this.reserved_spots = 0;
        this.reservedForLesson = false;
    }

    public TimeSlotModel(int index, int reserved_spots) {
        this.index = index;
        this.reserved_spots = reserved_spots;
        this.reservedForLesson = false;
    }

    public TimeSlotModel(int index, int reserved_spots, boolean reservedForLesson) {
        this.index = index;
        this.reserved_spots = reserved_spots;
        this.reservedForLesson = reservedForLesson;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getReserved_spots() {
        return reserved_spots;
    }

    public void setReserved_spots(int reserved_spots) {
        this.reserved_spots = reserved_spots;
    }

    public boolean isReserved() {
        return reservedForLesson;
    }

    public void setReservedForLesson(boolean reservedForLesson) {
        this.reservedForLesson = reservedForLesson;
    }
}

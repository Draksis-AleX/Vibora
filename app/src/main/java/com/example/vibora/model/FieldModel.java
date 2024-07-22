package com.example.vibora.model;

public class FieldModel{
    private String fieldId;
    private String name;

    public FieldModel(){};

    public FieldModel(String fieldId, String name, int first_available_slot, int occupied_slots) {
        this.fieldId = fieldId;
        this.name = name;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

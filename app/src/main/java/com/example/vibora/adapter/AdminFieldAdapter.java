package com.example.vibora.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.R;
import com.example.vibora.model.FieldModel;
import com.example.vibora.utils.AndroidUtils;
import com.example.vibora.utils.FirebaseUtils;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class AdminFieldAdapter extends RecyclerView.Adapter<AdminFieldAdapter.EditFieldViewHolder> {
    Context context;
    ArrayList<FieldModel> fields_list;

    public AdminFieldAdapter(Context context, ArrayList<FieldModel> fields_list) {
        this.context = context;
        this.fields_list = fields_list;
    }

    @NonNull
    @Override
    public EditFieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.edit_field_item, parent, false);
        return new AdminFieldAdapter.EditFieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditFieldViewHolder holder, int position) {
        FieldModel item = fields_list.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return fields_list.size();
    }

    public void updateFieldList(ArrayList<FieldModel> fields_list){
        this.fields_list = fields_list;
        notifyDataSetChanged();
    }

    public class EditFieldViewHolder extends RecyclerView.ViewHolder{
        EditText field_name;
        ImageButton update_img_btn, delete_img_btn;

        public EditFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            field_name = itemView.findViewById(R.id.field_name);
            update_img_btn = itemView.findViewById(R.id.update_img_btn);
            delete_img_btn = itemView.findViewById(R.id.delete_img_btn);
        }

        public void bind(FieldModel fieldModel) {

            field_name.setText(fieldModel.getName());
            field_name.setTypeface(ResourcesCompat.getFont(context, R.font.baloo_bhai));

            field_name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!field_name.getText().toString().equals(fieldModel.getName())){
                        update_img_btn.setColorFilter(context.getResources().getColor(R.color.main_green));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(field_name.getText().toString().equals(fieldModel.getName())){
                        update_img_btn.setColorFilter(context.getResources().getColor(R.color.separator_gray));
                    }
                }
            });

            update_img_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("EDITFIELDS", "Updating field name: " + fieldModel.getFieldId() + " -> " + field_name.getText().toString());
                    String new_field_name = field_name.getText().toString();
                    if(new_field_name.equals(fieldModel.getName())){
                        return;
                    }

                    FirebaseUtils.updateFieldName(fieldModel.getFieldId(), new_field_name)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    AndroidUtils.showToast(context, "Field Name Updated Successfully");
                                    fieldModel.setFieldId(new_field_name);
                                    fieldModel.setName(new_field_name);
                                    fields_list.set(getAbsoluteAdapterPosition(), fieldModel);
                                    updateFieldList(fields_list);
                                }
                            });
                }
            });

            delete_img_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("EDITFIELDS", "Deleting field: " + fieldModel.getFieldId());
                    FirebaseUtils.deleteField(fieldModel.getFieldId())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    AndroidUtils.showToast(context, "Field " + fieldModel.getName() + " deleted.");
                                    fields_list.remove(getAbsoluteAdapterPosition());
                                    notifyItemRemoved(getAbsoluteAdapterPosition());
                                }
                            });
                }
            });
        }
    }
}

package com.example.vibora.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibora.OtherProfile;
import com.example.vibora.R;
import com.example.vibora.model.ReportModel;
import com.example.vibora.utils.CalendarUtils;

import java.util.ArrayList;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {
    Context context;
    ArrayList<ReportModel> reports_list;

    public ReportsAdapter(Context context, ArrayList<ReportModel> reports_list) {
        this.context = context;
        this.reports_list = reports_list;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.report_recycler_row, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportModel reportModel = reports_list.get(position);
        holder.bind(reportModel);
    }

    @Override
    public int getItemCount() {
        return reports_list.size();
    }

    public class ReportViewHolder extends RecyclerView.ViewHolder{
        TextView username_txt, date_txt, reason_txt, reportedby_txt;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            username_txt = itemView.findViewById(R.id.username_txt);
            date_txt = itemView.findViewById(R.id.date_txt);
            reason_txt = itemView.findViewById(R.id.reason_txt);
            reportedby_txt = itemView.findViewById(R.id.reportedby_txt);
        }

        public void bind(ReportModel reportModel) {
            username_txt.setText("@" + reportModel.getUsername());
            date_txt.setText(CalendarUtils.formattedDate(CalendarUtils.convertFromTimestampToLocalDate(reportModel.getDate())));
            reason_txt.setText(reportModel.getReason());
            reportedby_txt.setText("By: @" + reportModel.getReporterUsername());

            itemView.setOnClickListener(v -> {
                Log.d("ReportsAdapter", "UserId -> " + reportModel.getUserId());
                Intent intent = new Intent(context, OtherProfile.class);
                intent.putExtra(OtherProfile.EXTRA_USER_ID, reportModel.getUserId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }
    }
}

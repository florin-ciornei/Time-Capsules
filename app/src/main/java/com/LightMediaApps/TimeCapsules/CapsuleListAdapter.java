package com.LightMediaApps.TimeCapsules;


import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.LightMediaApps.TimeCapsules.model.Capsule;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import androidx.recyclerview.widget.RecyclerView;

public class CapsuleListAdapter extends RecyclerView.Adapter<CapsuleListAdapter.ViewHolder> {
    private ArrayList<Capsule> capsules;
    final private OnListItemClickListener mOnListItemClickListener;

    public CapsuleListAdapter(ArrayList<Capsule> capsules, OnListItemClickListener listener) {
        this.capsules = capsules;
        mOnListItemClickListener = listener;
    }

    public void updateCapsules(ArrayList<Capsule> capsules) {
        this.capsules = capsules;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.capsule_list_element, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(capsules.get(position));
    }

    @Override
    public int getItemCount() {
        return capsules.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView openDateText, descriptionText;

        ViewHolder(View itemView) {
            super(itemView);
            openDateText = (TextView) itemView.findViewById(R.id.capsuleOpenDate);
            descriptionText = (TextView) itemView.findViewById(R.id.capsuleDescription);
        }

        //configure the capsule
        public void setData(Capsule capsule) {
            openDateText.setText(capsule.getOpenDateFormatted());
            descriptionText.setText(capsule.getDescription());
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnListItemClickListener.onListItemClick(capsules.get(getAdapterPosition()));
        }
    }

    public interface OnListItemClickListener {
        void onListItemClick(Capsule capsule);
    }
}

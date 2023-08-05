package com.poorld.badget.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.poorld.badget.R;
import com.poorld.badget.activity.AppSettingsAct;
import com.poorld.badget.entity.ItemAppEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemAppAdapter extends RecyclerView.Adapter<ItemAppAdapter.ViewHolder> {

    private Context mContext;
    private List<ItemAppEntity> mList;

    public ItemAppAdapter(Context mContext) {
        this.mContext = mContext;
        mList = new ArrayList<>();
    }

    public void setList(List<ItemAppEntity> list) {
        mList.clear();

        if (list == null) {
            notifyDataSetChanged();
            return;
        }
        mList.addAll(list);
        mList.sort(Comparator.comparing(ItemAppEntity::isHookEnabled).reversed());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.app_item_view, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemAppEntity appEntity = mList.get(position);
        holder.label.setText(appEntity.getAppName());
        holder.packageName.setText(appEntity.getPackageName());
        holder.icon.setImageDrawable(appEntity.getDrawable());
        if (appEntity.isHookEnabled()) {
            holder.enabled.setVisibility(View.VISIBLE);
        } else {
            holder.enabled.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mContext.startActivity(new Intent(mContext, AppSettingsAct.class));
                AppSettingsAct.startAct(mContext, appEntity.getPackageName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private MaterialTextView label;
        private MaterialTextView enabled;
        private MaterialTextView packageName;
        private MaterialCheckBox checkbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.icon);
            label = (MaterialTextView) itemView.findViewById(R.id.label);
            enabled = (MaterialTextView) itemView.findViewById(R.id.enabled);
            packageName = (MaterialTextView) itemView.findViewById(R.id.package_name);
            //checkbox = (MaterialCheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}

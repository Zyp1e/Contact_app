package com.example.contactapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.contactapp.databinding.ItemGroupBinding;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<String> groupList;
    private final boolean configurable;
    private String currentGroup;
    private final OnGroupActionListener onGroupAction;

    public GroupAdapter(List<String> groupList, boolean configurable, String currentGroup, OnGroupActionListener onGroupAction) {
        this.groupList = groupList != null ? groupList : List.of("全部");
        this.configurable = configurable;
        this.currentGroup = currentGroup != null ? currentGroup : "全部";
        this.onGroupAction = onGroupAction;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemGroupBinding binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        String group = groupList.get(position);
        holder.bind(group, onGroupAction);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroupBinding binding;

        public GroupViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String group, OnGroupActionListener onGroupAction) {
            binding.tvGroupName.setText(group);
            binding.radioGroup.setChecked(group.equals(currentGroup));
            binding.radioGroup.setVisibility(configurable ? View.GONE : View.VISIBLE);
            binding.btnGroupDel.setVisibility(configurable && !"全部".equals(group) ? View.VISIBLE : View.GONE);
            binding.btnGroupEdit.setVisibility(configurable && !"全部".equals(group) ? View.VISIBLE : View.GONE);

            binding.radioGroup.setOnClickListener(v -> onGroupAction.onGroupAction(group, "select"));
            binding.btnGroupEdit.setOnClickListener(v -> onGroupAction.onGroupAction(group, "edit"));
            binding.btnGroupDel.setOnClickListener(v -> onGroupAction.onGroupAction(group, "delete"));
        }
    }

    public interface OnGroupActionListener {
        void onGroupAction(String group, String action);
    }
}

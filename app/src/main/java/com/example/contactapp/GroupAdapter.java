package com.example.contactapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.contactapp.databinding.ItemGroupBinding;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private final List<String> groupList; // 分组列表数据
    private final boolean configurable; // 是否可配置
    private String currentGroup; // 当前选中的分组
    private final OnGroupActionListener onGroupAction; // 分组操作监听器

    // 构造方法，初始化适配器
    public GroupAdapter(List<String> groupList, boolean configurable, String currentGroup, OnGroupActionListener onGroupAction) {
        // 初始化分组列表，如果为空则默认为只有一个 "全部" 分组
        this.groupList = groupList != null ? groupList : List.of("全部");
        this.configurable = configurable; // 初始化是否可配置
        this.currentGroup = currentGroup != null ? currentGroup : "全部"; // 初始化当前选中的分组，默认为 "全部"
        this.onGroupAction = onGroupAction; // 初始化分组操作监听器
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 创建 ViewHolder，绑定 ItemGroupBinding 布局
        ItemGroupBinding binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GroupViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        // 绑定数据到 ViewHolder 上
        String group = groupList.get(position); // 获取当前位置的分组名称
        holder.bind(group, onGroupAction); // 绑定数据和监听器到 ViewHolder
    }

    @Override
    public int getItemCount() {
        return groupList.size(); // 返回分组列表的大小
    }

    // ViewHolder 类，用于绑定每个分组的视图
    public class GroupViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroupBinding binding; // ViewHolder 使用的视图绑定对象

        public GroupViewHolder(ItemGroupBinding binding) {
            super(binding.getRoot());
            this.binding = binding; // 初始化视图绑定对象
        }

        // 绑定方法，将数据和监听器绑定到视图上
        public void bind(String group, OnGroupActionListener onGroupAction) {
            binding.tvGroupName.setText(group); // 设置分组名称文本

            // 根据当前分组是否被选中设置 RadioButton 的选中状态
            binding.radioGroup.setChecked(group.equals(currentGroup));

            // 根据是否可配置设置 RadioGroup、编辑按钮和删除按钮的可见性
            binding.radioGroup.setVisibility(configurable ? View.GONE : View.VISIBLE);
            binding.btnGroupDel.setVisibility(configurable && !"全部".equals(group) ? View.VISIBLE : View.GONE);
            binding.btnGroupEdit.setVisibility(configurable && !"全部".equals(group) ? View.VISIBLE : View.GONE);

            // 设置点击监听器，分别处理选中、编辑和删除操作
            binding.radioGroup.setOnClickListener(v -> onGroupAction.onGroupAction(group, "select"));
            binding.btnGroupEdit.setOnClickListener(v -> onGroupAction.onGroupAction(group, "edit"));
            binding.btnGroupDel.setOnClickListener(v -> onGroupAction.onGroupAction(group, "delete"));
        }
    }

    // 分组操作监听器接口，定义了处理分组操作的方法
    public interface OnGroupActionListener {
        void onGroupAction(String group, String action); // 处理分组操作的方法
    }
}

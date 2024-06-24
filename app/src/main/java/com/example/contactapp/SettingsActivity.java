package com.example.contactapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactapp.databinding.ActivitySettingsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    private GroupAdapter groupAdapter;
    private List<String> groupList = new ArrayList<>();
    private boolean isListLayout = true;
    private boolean isDarkTheme = true;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 加载配置信息
        loadSettings();

        // 初始化视图
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("设置");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 设置是否选择暗黑模式
        binding.radioLightTheme.setChecked(!isDarkTheme);
        binding.radioDarkTheme.setChecked(isDarkTheme);

        // 设置列表布局和卡片布局的选择状态
        binding.radioListLayout.setChecked(isListLayout);
        binding.radioCardLayout.setChecked(!isListLayout);

        // 设置分组列表适配器
        groupAdapter = new GroupAdapter(groupList, true, "", (group, action) -> {
            switch (action) {
                case "edit":
                    showEditGroupDialog(group);
                    break;
                case "delete":
                    deleteGroup(group);
                    break;
            }
        });
        binding.recyclerViewGroups.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewGroups.setAdapter(groupAdapter);

        // 添加分组按钮点击事件
        binding.btnAddGroup.setOnClickListener(v -> showAddGroupDialog());

        // 监听显示模式变化，保存到 SharedPreferences
        binding.radioGroupLayout.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (checkedId == R.id.radio_list_layout) {
                editor.putBoolean("isListLayout", true);
            } else if (checkedId == R.id.radio_card_layout) {
                editor.putBoolean("isListLayout", false);
            }
            editor.apply();
        });

        // 监听主题选择变化，保存到 SharedPreferences，并重建 Activity 切换主题
        binding.radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean useDarkTheme = checkedId == R.id.radio_dark_theme;
            editor.putBoolean("isDarkTheme", useDarkTheme);
            editor.apply();
            recreate(); // 重建 Activity 切换主题
        });
    }

    // 加载 SharedPreferences 中保存的设置信息
    private void loadSettings() {
        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);
        loadTheme(isDarkTheme); // 加载主题设置
        isListLayout = sharedPreferences.getBoolean("isListLayout", true);

        // 加载分组列表
        Set<String> defaultGroupSet = new HashSet<>();
        defaultGroupSet.add("全部");
        Set<String> groupSet = sharedPreferences.getStringSet("groupList", Collections.unmodifiableSet(defaultGroupSet));
        groupList = new ArrayList<>(groupSet);
        Collections.sort(groupList); // 排序分组列表
    }

    // 加载主题设置
    private void loadTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // 保存分组列表到 SharedPreferences
    private void saveGroups() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> groupSet = new HashSet<>(groupList);
        editor.putStringSet("groupList", Collections.unmodifiableSet(groupSet));
        editor.apply();
        setResult(RESULT_OK);
    }

    // 显示添加分组的对话框
    private void showAddGroupDialog() {
        EditText input = new EditText(this);
        input.setHint("输入分组名称");
        input.setPadding(100, 0, 100, 0);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(input);
        dialog.setOnDismissListener(dialog1 -> {
            String groupName = input.getText().toString().trim();
            // 检查分组名称是否合法
            if (!groupName.isEmpty() && !groupList.contains(groupName)) {
                groupList.add(groupList.size(), groupName);
                groupAdapter.notifyItemInserted(groupList.size() - 1);
                saveGroups(); // 保存分组列表
            } else {
                Toast.makeText(this, "不允许的分组名", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    // 显示编辑分组的对话框
    private void showEditGroupDialog(String group) {
        EditText input = new EditText(this);
        input.setHint("输入分组名称");
        input.setPadding(100, 0, 100, 0);
        input.setText(group);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(input);
        dialog.setOnDismissListener(dialog1 -> {
            String newGroupName = input.getText().toString().trim();
            int position = groupList.indexOf(group);
            // 检查新的分组名称是否合法
            if (!newGroupName.isEmpty() && !groupList.contains(newGroupName) && position != -1) {
                groupList.set(position, newGroupName);
                groupAdapter.notifyItemChanged(position);
                saveGroups(); // 保存分组列表
            } else {
                Toast.makeText(this, "不允许的分组名", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    // 删除分组
    private void deleteGroup(String group) {
        int position = groupList.indexOf(group);
        if (position != -1) {
            groupList.remove(position);
            groupAdapter.notifyItemRemoved(position);
            saveGroups(); // 保存分组列表
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
        }
    }
}

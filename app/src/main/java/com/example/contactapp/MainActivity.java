package com.example.contactapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.contactapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ContactDatabaseHelper dbHelper;
    private ActivityMainBinding binding;
    private ContactAdapter contactAdapter;

    private List<Contact> contactList = new ArrayList<>();
    private List<String> groupList = new ArrayList<>();
    private ActivityResultLauncher<Intent> editContactLauncher;
    private ActivityResultLauncher<Intent> settingsLauncher;

    private boolean isListLayout = true;
    private String currentGroup = "全部";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 加载配置信息、联系人数据
        dbHelper = new ContactDatabaseHelper(this);
        loadContacts(); // 加载联系人数据
        loadSettings(); // 加载应用设置

        // 加载视图
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("联系人");

        // 设置 RecyclerView Adapter
        setAdapter();

        // 初始化 ActivityResultLauncher
        initEditContactLauncher(); // 初始化编辑联系人启动器
        initSettingsLauncher(); // 初始化设置启动器

        // 初始化字母索引视图
        initAlphabetIndexView();

        // 初始化 SearchView
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                contactAdapter.setNameFilter(query); // 提交搜索文本时过滤联系人列表
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactAdapter.setNameFilter(newText); // 当搜索文本改变时过滤联系人列表
                return false;
            }
        });
        binding.searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.searchView.onActionViewExpanded();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // 加载菜单资源文件
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理菜单项选择事件
        if (item.getItemId() == R.id.action_add) { // 点击添加联系人菜单项
            Intent intentAdd = new Intent(this, ContactDetailActivity.class);
            editContactLauncher.launch(intentAdd); // 启动添加联系人界面
            return true;
        } else if (item.getItemId() == R.id.action_filter) { // 点击筛选菜单项
            // 显示筛选对话框
            FilterBottomDialog dialog = new FilterBottomDialog(this, groupList, currentGroup, group -> {
                contactAdapter.setGroupFilter(group); // 设置联系人列表的分组筛选
                currentGroup = group; // 更新当前选择的分组
            });
            dialog.show();
            return true;
        } else if (item.getItemId() == R.id.action_more) { // 点击更多菜单项
            Intent intentMore = new Intent(this, SettingsActivity.class);
            settingsLauncher.launch(intentMore); // 启动设置界面
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSettings(); // 保存应用设置
    }

    private void initEditContactLauncher() {
        // 初始化编辑联系人的 ActivityResultLauncher
        editContactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Contact updatedContact = result.getData().getParcelableExtra("updatedContact");
                        if (updatedContact != null) {
                            int index = contactList.indexOf(findContactById(updatedContact.getId()));
                            if (index != -1) { // 如果联系人存在
                                if (!updatedContact.getName().isEmpty()) { // 如果更新后的联系人姓名不为空，更新联系人信息
                                    contactList.set(index, updatedContact); // 更新列表中的联系人信息
                                    dbHelper.updateContact(updatedContact); // 更新数据库中的联系人信息
                                    contactAdapter.updateContact(updatedContact); // 更新 RecyclerView 中的联系人显示
                                } else { // 如果更新后的联系人姓名为空，删除联系人
                                    contactList.remove(index); // 从列表中删除联系人
                                    dbHelper.deleteContact(updatedContact.getId()); // 从数据库中删除联系人
                                    contactAdapter.delContact(updatedContact); // 从 RecyclerView 中删除联系人显示
                                }
                            } else {  // 如果联系人不存在，添加新联系人
                                contactList.add(updatedContact); // 添加新联系人到列表
                                dbHelper.insertContact(updatedContact); // 插入新联系人到数据库
                                contactAdapter.insertContact(updatedContact); // 在 RecyclerView 中插入新联系人显示
                            }
                            reloadData(); // 重新加载数据
                        }
                    }
                }
        );

    }

    private void reloadData() {
        contactList = dbHelper.getAllContacts(); // 从数据库中获取所有联系人
        contactAdapter.updateContacts(contactList); // 更新适配器中的联系人数据
        // 其他操作...
    }

    private void initSettingsLauncher() {
        // 初始化设置界面的 ActivityResultLauncher
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    loadSettings(); // 重新加载应用设置
                    setAdapter(); // 更新 RecyclerView 的适配器
                }
        );
    }

    private void setAdapter() {
        // 设置 RecyclerView 的布局管理器和适配器
        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList, isListLayout, contact -> {
            Intent intent = new Intent(this, ContactDetailActivity.class);
            intent.putExtra("contact", contact);
            editContactLauncher.launch(intent); // 启动编辑联系人界面
        });
        binding.recyclerViewContacts.setAdapter(contactAdapter);

        // 加载上次筛选的分组
        contactAdapter.setGroupFilter(currentGroup);
    }

    private void loadSettings() {
        // 加载应用设置
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);
        loadTheme(isDarkTheme); // 加载主题设置

        isListLayout = sharedPreferences.getBoolean("isListLayout", true); // 加载列表布局设置
        Set<String> groupSet = sharedPreferences.getStringSet("groupList", Collections.singleton("全部"));
        groupList = new ArrayList<>(groupSet); // 加载分组列表设置
        currentGroup = sharedPreferences.getString("currentGroup", "全部"); // 加载当前分组设置
    }

    private void loadTheme(boolean isDarkTheme) {
        // 加载主题设置
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void saveSettings() {
        // 保存应用设置
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 使用 Collections.unmodifiableSet 创建不可变集合保存分组列表设置
        Set<String> immutableGroupList = Collections.unmodifiableSet(new HashSet<>(groupList));
        editor.putStringSet("groupList", immutableGroupList);

        editor.putBoolean("isListLayout", isListLayout); // 保存列表布局设置
        editor.putString("currentGroup", currentGroup); // 保存当前分组设置
        editor.apply(); // 应用设置
    }

    private void initAlphabetIndexView() {
        // 初始化字母索引视图
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        for (char letter : alphabet) {
            TextView textView = new TextView(this);
            textView.setText(String.valueOf(letter));
            textView.setTextSize(12f);
            textView.setGravity(Gravity.CENTER);
            textView.setWidth(50);
            textView.setTextColor(Color.GRAY);
            textView.setOnClickListener(v -> {
                // 清除之前的选中状态
                for (int i = 0; i < binding.alphabetIndexView.getChildCount(); i++) {
                    View child = binding.alphabetIndexView.getChildAt(i);
                    if (child instanceof TextView) {
                        child.setBackgroundResource(0);  // 移除背景
                    }
                }
                textView.setBackgroundResource(R.drawable.alphabet_item_background); // 设置选中背景
                // 滑动到指定字母的联系人位置
                int position = contactAdapter.findContactPositionByLetter(letter);
                if (position != -1) {
                    binding.recyclerViewContacts.scrollToPosition(position);
                    contactAdapter.setSelectedPosition(position); // 设置选中位置
                }
            });
            binding.alphabetIndexView.addView(textView); // 添加字母索引视图
        }
    }


    private int findContactPositionByLetter(char letter) {
        // 根据字母查找联系人在列表中的位置
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).getName().substring(0, 1).equalsIgnoreCase(String.valueOf(letter))) {
                return i;
            }
        }
        return -1;
    }

    // 模拟获取联系人数据
    private void loadContacts() {
        contactList = dbHelper.getAllContacts();
        groupList=dbHelper.getAllGroups();
        for (Contact contact : contactList) {
            int i = 1;
            if (!groupList.contains(contact.getGroup())) {
                contact.setGroup("全部");
            }
            Log.d(TAG, "now is" + i);
        }
//        contactList.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
        Collections.sort(contactList, new PinyinComparator());
    }

    private Contact findContactById(long id) {
        // 根据联系人ID查找联系人对象
        for (Contact contact : contactList) {
            if (contact.getId() == id) {
                return contact; // 返回找到的联系人对象
            }
        }
        return null;  // 如果找不到对应ID的联系人，返回null
    }

}

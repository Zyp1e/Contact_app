package com.example.contactapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
        loadContacts();
        loadSettings();

        // 加载视图
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("联系人");

        // 设置 RecyclerView Adapter
        setAdapter();

        // 初始化 ActivityResultLauncher
        initEditContactLauncher();
        initSettingsLauncher();

        // 初始化字母索引视图
        initAlphabetIndexView();

        // 初始化 SearchView
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                contactAdapter.setNameFilter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                contactAdapter.setNameFilter(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent intentAdd = new Intent(this, ContactDetailActivity.class);
            editContactLauncher.launch(intentAdd);
            return true;
        } else if (item.getItemId() == R.id.action_filter) {
            FilterBottomDialog dialog = new FilterBottomDialog(this, groupList, currentGroup, group -> {
                contactAdapter.setGroupFilter(group);
                currentGroup = group;
            });
            dialog.show();
            return true;
        } else if (item.getItemId() == R.id.action_more) {
            Intent intentMore = new Intent(this, SettingsActivity.class);
            settingsLauncher.launch(intentMore);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSettings();
    }

    private void initEditContactLauncher() {
        editContactLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Contact updatedContact = result.getData().getParcelableExtra("updatedContact");
                        if (updatedContact != null) {
                            int index = contactList.indexOf(findContactById(updatedContact.getId()));
                            if (index != -1) { //id存在
                                if (!updatedContact.getName().isEmpty()) { //name非空，修改
                                    contactList.set(index, updatedContact);
                                    dbHelper.updateContact(updatedContact);
                                    contactAdapter.updateContact(updatedContact);
                                } else { //name为空，删除
                                    contactList.remove(index);
                                    dbHelper.deleteContact(updatedContact.getId());
                                    contactAdapter.delContact(updatedContact);
                                }
                            } else {  //id不存在，添加联系人
                                contactList.add(updatedContact);
                                dbHelper.insertContact(updatedContact);
                                contactAdapter.insertContact(updatedContact);
                            }
                        }
                    }
                }
        );
    }

    private void initSettingsLauncher() {
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    loadSettings();
                    setAdapter();
                }
        );
    }

    private void setAdapter() {
        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactList, isListLayout, contact -> {
            Intent intent = new Intent(this, ContactDetailActivity.class);
            intent.putExtra("contact", contact);
            editContactLauncher.launch(intent);
        });
        binding.recyclerViewContacts.setAdapter(contactAdapter);

        // 加载上次筛选
        contactAdapter.setGroupFilter(currentGroup);
    }

    private void loadSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        boolean isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false);
        loadTheme(isDarkTheme);

        isListLayout = sharedPreferences.getBoolean("isListLayout", true);
        Set<String> groupSet = sharedPreferences.getStringSet("groupList", Collections.singleton("全部"));
        groupList = new ArrayList<>(groupSet);
        currentGroup = sharedPreferences.getString("currentGroup", "全部");
    }

    private void loadTheme(boolean isDarkTheme) {
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void saveSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 使用 Collections.unmodifiableSet 创建不可变集合
        Set<String> immutableGroupList = Collections.unmodifiableSet(new HashSet<>(groupList));
        editor.putStringSet("groupList", immutableGroupList);

        editor.putBoolean("isListLayout", isListLayout);
        editor.putString("currentGroup", currentGroup);
        editor.apply();
    }

    private void initAlphabetIndexView() {
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
                textView.setBackgroundResource(R.drawable.alphabet_item_background);
                // 滑动导航
                int position = findContactPositionByLetter(letter);
                if (position != -1) {
                    binding.recyclerViewContacts.scrollToPosition(position);
                    contactAdapter.setSelectedPosition(position); // 设置选中位置
                }
            });
            binding.alphabetIndexView.addView(textView);
        }
    }

    private int findContactPositionByLetter(char letter) {
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
        for (Contact contact : contactList) {
            int i=1;
            if (!groupList.contains(contact.getGroup())) {
                contact.setGroup("全部");
            }
            Log.d(TAG,"now is"+i);
        }
        contactList.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
    }

    private Contact findContactById(long id) {
        for (Contact contact : contactList) {
            if (contact.getId() == id) {
                return contact;
            }
        }
        return null;
    }


}

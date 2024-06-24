package com.example.contactapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactapp.databinding.ItemContactCardBinding;
import com.example.contactapp.databinding.ItemContactListBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements android.widget.Filterable {

    private List<Contact> contactList;
    private List<Contact> filteredContactList;
    private boolean isListLayout;
    private final ContactClickListener clickListener;
    private String nameFilter;
    private String groupFilter;

    private static final int VIEW_TYPE_LIST = 1;
    private static final int VIEW_TYPE_CARD = 2;

    private int selectedPosition = -1; // 用于存储选中的联系人位置

    public interface ContactClickListener {
        void onContactClick(Contact contact);
    }

    public ContactAdapter(List<Contact> contactList, boolean isListLayout, ContactClickListener clickListener) {
        this.contactList = contactList;
        this.filteredContactList = new ArrayList<>(contactList);
        this.isListLayout = isListLayout;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return isListLayout ? VIEW_TYPE_LIST : VIEW_TYPE_CARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_LIST) {
            ItemContactListBinding binding = ItemContactListBinding.inflate(inflater, parent, false);
            return new ListViewHolder(binding);
        } else {
            ItemContactCardBinding binding = ItemContactCardBinding.inflate(inflater, parent, false);
            return new CardViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Contact contact = filteredContactList.get(position);
        if (holder instanceof ListViewHolder) {
            ((ListViewHolder) holder).bind(contact, clickListener, position == selectedPosition);
        } else if (holder instanceof CardViewHolder) {
            ((CardViewHolder) holder).bind(contact, clickListener, position == selectedPosition);
        }
    }

    @Override
    public int getItemCount() {
        return filteredContactList.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        private final ItemContactListBinding binding;

        public ListViewHolder(ItemContactListBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Contact contact, final ContactClickListener clickListener, boolean isSelected) {
            binding.textViewName.setText(contact.getName());
            binding.textViewName.setTextColor(isSelected ? Color.RED : Color.BLACK);
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onContactClick(contact);
                }
            });
        }
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private final ItemContactCardBinding binding;

        public CardViewHolder(ItemContactCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Contact contact, final ContactClickListener clickListener, boolean isSelected) {
            binding.textViewName.setText(contact.getName());
            binding.textViewName.setTextColor(isSelected ? Color.RED : Color.BLACK);
            if (contact.getPhotoUri() != null) {
                binding.imgContactPhoto.setImageURI(Uri.parse(contact.getPhotoUri()));
            } else {
                Bitmap bitmap = createBitmapFromCharacter(contact.getName().charAt(0), 500, Color.WHITE, 10);
                binding.imgContactPhoto.setImageBitmap(bitmap);
            }
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onContactClick(contact);
                }
            });
        }

        private Bitmap createBitmapFromCharacter(char character, int viewSize, int color, int padding) {
            Paint paint = new Paint();
            paint.setTextSize((float) (viewSize - padding * 2));
            paint.setColor(color);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);

            Rect textBounds = new Rect();
            paint.getTextBounds(String.valueOf(character), 0, 1, textBounds);
            int width = viewSize;
            int height = viewSize;
            float baseline = (height / 2 - (paint.descent() + paint.ascent()) / 2);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            int backgroundColor = Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
            canvas.drawColor(backgroundColor);
            canvas.drawText(String.valueOf(character), (float) (width / 2), baseline, paint);

            return bitmap;
        }
    }

    @Override
    public android.widget.Filter getFilter() {
        return new android.widget.Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Contact> filteredResults = new ArrayList<>();
                for (Contact contact : contactList) {
                    boolean matchesName = (nameFilter == null || contact.getName().toLowerCase().contains(nameFilter.toLowerCase()));
                    boolean matchesGroup = (groupFilter == null || "全部".equals(groupFilter) || contact.getGroup().equals(groupFilter));
                    if (matchesName && matchesGroup) {
                        filteredResults.add(contact);
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredContactList = (List<Contact>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void setNameFilter(String name) {
        this.nameFilter = name;
        getFilter().filter(null);
    }

    public void setGroupFilter(String group) {
        this.groupFilter = group;
        getFilter().filter(null);
    }

    public void insertContact(Contact contact) {
        filteredContactList.add(0, contact);
        notifyItemInserted(0);
    }

    public void updateContact(Contact contact) {
        int index = findContactIndexById(contact.getId());
        if (index != -1) {
            filteredContactList.set(index, contact);
            notifyItemChanged(index);
        }
    }

    public void delContact(Contact contact) {
        int index = findContactIndexById(contact.getId());
        if (index != -1) {
            filteredContactList.remove(index);
            notifyItemRemoved(index);
        }
    }

    private int findContactIndexById(long id) {
        for (int i = 0; i < filteredContactList.size(); i++) {
            if (filteredContactList.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    // 新增：设置选中的联系人位置
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }
}

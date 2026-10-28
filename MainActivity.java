
package com.example.lostandfoundlist;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private DatabaseHelper databaseHelper;

    private LinearLayout itemsContainer;
    private EditText searchInput;
    private TextView itemCount;
    private TextView emptyMessage;

    private final int navy = Color.rgb(23, 35, 63);
    private final int blue = Color.rgb(49, 86, 201);
    private final int muted = Color.rgb(112, 123, 145);
    private final int background = Color.rgb(245, 247, 251);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(245, 247, 251));
        window.setNavigationBarColor(Color.WHITE);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        databaseHelper = new DatabaseHelper(this);

        itemsContainer = findViewById(R.id.itemsContainer);
        searchInput = findViewById(R.id.searchInput);
        itemCount = findViewById(R.id.itemCount);
        emptyMessage = findViewById(R.id.emptyMessage);

        Button addButton = findViewById(R.id.addItemButton);
        addButton.setOnClickListener(v -> showAddDialog());

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                loadItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadItems("");
    }

    private void loadItems(String search) {
        ArrayList<DatabaseHelper.Item> items =
                databaseHelper.getItems(search);

        itemsContainer.removeAllViews();

        int lostCount = 0;
        int foundCount = 0;

        for (DatabaseHelper.Item item : items) {
            if ("Found".equals(item.status)) {
                foundCount++;
            } else {
                lostCount++;
            }

            itemsContainer.addView(createItemCard(item));
        }

        itemCount.setText(items.size() + " items");

        if (items.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            if (search == null || search.trim().isEmpty()) {
                emptyMessage.setText(
                        "No items yet.\nTap + Add Item to get started."
                );
            } else {
                emptyMessage.setText(
                        "No matching items found.\nTry another search."
                );
            }
        } else {
            emptyMessage.setVisibility(View.GONE);
        }
    }

    private LinearLayout createItemCard(DatabaseHelper.Item item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(17), dp(16), dp(17), dp(16));
        card.setBackground(rounded(Color.WHITE, 18, Color.WHITE));
        card.setElevation(dp(2));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        cardParams.bottomMargin = dp(13);
        card.setLayoutParams(cardParams);

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextColor(navy);
        name.setTextSize(17);
        name.setTypeface(null, Typeface.BOLD);
        name.setMaxLines(2);
        name.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams nameParams =
                new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1
                );
        heading.addView(name, nameParams);

        boolean found = "Found".equals(item.status);

        TextView badge = new TextView(this);
        badge.setText(found ? "FOUND" : "LOST");
        badge.setTextSize(10);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setTextColor(found
                ? Color.rgb(25, 125, 79)
                : Color.rgb(190, 76, 55));
        badge.setPadding(dp(10), dp(6), dp(10), dp(6));
        badge.setBackground(rounded(
                found ? Color.rgb(224, 246, 234)
                        : Color.rgb(255, 235, 229),
                20,
                found ? Color.rgb(224, 246, 234)
                        : Color.rgb(255, 235, 229)
        ));

        heading.addView(badge);
        card.addView(heading);

        TextView location = new TextView(this);
        location.setText("📍  " + safeText(item.location, "Location not specified"));
        location.setTextColor(muted);
        location.setTextSize(13);
        location.setPadding(0, dp(10), 0, dp(5));
        card.addView(location);

        if (item.description != null &&
                !item.description.trim().isEmpty()) {
            TextView description = new TextView(this);
            description.setText(item.description);
            description.setTextColor(Color.rgb(82, 93, 113));
            description.setTextSize(14);
            description.setPadding(0, dp(3), 0, dp(12));
            card.addView(description);
        }

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(12), 0, 0);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(235, 238, 244));
        card.addView(divider, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)
        ));

        if (!found) {
            Button foundButton = makeActionButton(
                    "✓  Mark Found",
                    Color.rgb(224, 246, 234),
                    Color.rgb(25, 125, 79)
            );

            foundButton.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Mark as Found?")
                        .setMessage("Mark \"" + item.name +
                                "\" as found?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Yes, Found", (dialog, which) -> {
                            if (databaseHelper.markAsFound(item.id)) {
                                Toast.makeText(this,
                                        "Item marked as found",
                                        Toast.LENGTH_SHORT).show();
                                loadItems(searchInput.getText().toString());
                            }
                        })
                        .show();
            });

            actions.addView(foundButton,
                    new LinearLayout.LayoutParams(
                            0, dp(42), 1
                    ));
        }

        Button deleteButton = makeActionButton(
                "Delete",
                Color.rgb(255, 235, 235),
                Color.rgb(184, 52, 65)
        );

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        0, dp(42), 1
                );

        if (!found) {
            deleteParams.leftMargin = dp(10);
        }

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Item?")
                    .setMessage("Permanently delete \"" +
                            item.name + "\"?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (databaseHelper.deleteItem(item.id)) {
                            Toast.makeText(this,
                                    "Item deleted",
                                    Toast.LENGTH_SHORT).show();
                            loadItems(searchInput.getText().toString());
                        }
                    })
                    .show();
        });

        actions.addView(deleteButton, deleteParams);
        card.addView(actions);

        return card;
    }

    private void showAddDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(8), dp(22), 0);

        EditText nameInput = createInput(
                "Item name *", "e.g. Black wallet"
        );
        nameInput.setInputType(1);

        EditText locationInput = createInput(
                "Location *", "e.g. College library"
        );
        locationInput.setInputType(1);

        EditText descriptionInput = createInput(
                "Description (optional)", "Color, brand, details..."
        );
        descriptionInput.setInputType(0x00020001);
        descriptionInput.setMinLines(2);
        descriptionInput.setGravity(Gravity.TOP);

        form.addView(nameInput);
        form.addView(locationInput);
        form.addView(descriptionInput);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.addView(form);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Item")
                .setView(scrollView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save Item", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String name = nameInput.getText().toString().trim();
                        String location = locationInput.getText()
                                .toString().trim();
                        String description = descriptionInput.getText()
                                .toString().trim();

                        if (name.isEmpty()) {
                            nameInput.setError("Enter an item name");
                            nameInput.requestFocus();
                            return;
                        }

                        if (location.isEmpty()) {
                            locationInput.setError("Enter a location");
                            locationInput.requestFocus();
                            return;
                        }

                        long result = databaseHelper.addItem(
                                name, location, description
                        );

                        if (result != -1) {
                            Toast.makeText(this,
                                    "Item added successfully",
                                    Toast.LENGTH_SHORT).show();

                            searchInput.setText("");
                            loadItems("");
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this,
                                    "Could not save the item",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }

    private EditText createInput(String hint, String example) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(14);
        input.setSingleLine(false);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackground(rounded(
                Color.rgb(247, 249, 253),
                10,
                Color.rgb(222, 227, 237)
        ));
        input.setHintTextColor(muted);
        input.setTextColor(navy);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) input.getLayoutParams();
        params.bottomMargin = dp(12);
        input.setLayoutParams(params);

        return input;
    }

    private Button makeActionButton(
            String text, int backgroundColor, int textColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(12);
        button.setTypeface(null, Typeface.BOLD);
        button.setTextColor(textColor);
        button.setPadding(dp(5), 0, dp(5), 0);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setBackground(rounded(
                backgroundColor, 10, backgroundColor
        ));
        return button;
    }

    private GradientDrawable rounded(
            int fillColor, int radius, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty()
                ? fallback : value;
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        super.onDestroy();
    }
}

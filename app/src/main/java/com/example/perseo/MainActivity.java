package com.example.perseo;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchBar;
    private ImageView settingsBtn;
    private AppAdapter adapter;
    private LauncherPrefs prefs;
    private PackageManager packageManager;

    private List<AppInfo> allApps = new ArrayList<>();
    private List<AppInfo> filteredApps = new ArrayList<>();

    private final BroadcastReceiver packageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadApps();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new LauncherPrefs(this);
        applyTheme();

        super.onCreate(savedInstanceState);

        // Fullscreen / transparent status bar
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();
        recyclerView   = findViewById(R.id.appsRecyclerView);
        searchBar      = findViewById(R.id.searchBar);
        settingsBtn    = findViewById(R.id.settingsBtn);

        setupSearch();
        setupSettingsBtn();
        loadApps();

        // Listen for app installs/uninstalls
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(packageReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyPrefsToUi();
        loadApps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(packageReceiver);
    }

    private void applyTheme() {
        switch (prefs.getTheme()) {
            case "light":  setTheme(R.style.Theme_Perseo_Light);  break;
            case "amoled": setTheme(R.style.Theme_Perseo_Amoled); break;
            default:       setTheme(R.style.Theme_Perseo);        break;
        }
    }

    private void applyPrefsToUi() {
        // Search bar visibility
        searchBar.setVisibility(prefs.getSearchVisible() ? View.VISIBLE : View.GONE);

        // Accent color on search bar underline / settings icon
        try {
            int accent = Color.parseColor(prefs.getAccentColor());
            settingsBtn.setColorFilter(accent);
        } catch (Exception ignored) {}

        // Layout manager
        String layout = prefs.getLayout();
        if ("grid".equals(layout)) {
            int cols = prefs.getGridColumns();
            recyclerView.setLayoutManager(new GridLayoutManager(this, cols));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterApps(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSettingsBtn() {
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    void loadApps() {
        new AsyncTask<Void, Void, List<AppInfo>>() {
            @Override
            protected List<AppInfo> doInBackground(Void... voids) {
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> rawList = packageManager.queryIntentActivities(intent, 0);

                List<AppInfo> apps = new ArrayList<>();
                for (ResolveInfo info : rawList) {
                    String pkg  = info.activityInfo.packageName;
                    String act  = info.activityInfo.name;
                    String label = info.loadLabel(packageManager).toString();
                    Drawable icon = info.loadIcon(packageManager);
                    boolean fav    = prefs.isFavorite(pkg);
                    boolean hidden = prefs.isHidden(pkg);
                    String custom  = prefs.getCustomName(pkg);
                    apps.add(new AppInfo(pkg, act, label, icon, fav, hidden, custom));
                }

                // Sort
                switch (prefs.getSortOrder()) {
                    case "alpha":
                        Collections.sort(apps, (a, b) ->
                                a.displayLabel.compareToIgnoreCase(b.displayLabel));
                        break;
                }

                // Favorites first
                List<AppInfo> sorted = new ArrayList<>();
                for (AppInfo a : apps) if (a.isFavorite && !a.isHidden) sorted.add(a);
                for (AppInfo a : apps) if (!a.isFavorite && !a.isHidden) sorted.add(a);
                return sorted;
            }

            @Override
            protected void onPostExecute(List<AppInfo> apps) {
                allApps.clear();
                allApps.addAll(apps);

                String query = searchBar.getText().toString();
                filterApps(query);

                if (adapter == null) {
                    adapter = new AppAdapter(filteredApps);
                    recyclerView.setAdapter(adapter);
                    applyPrefsToUi();
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        }.execute();
    }

    private void filterApps(String query) {
        filteredApps.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredApps.addAll(allApps);
        } else {
            String q = query.toLowerCase().trim();
            for (AppInfo app : allApps) {
                if (app.displayLabel.toLowerCase().contains(q)) {
                    filteredApps.add(app);
                }
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Adapter
    // ──────────────────────────────────────────────────────────────────────
    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
        private final List<AppInfo> list;

        AppAdapter(List<AppInfo> list) { this.list = list; }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            String layout = prefs.getLayout();
            int resId = "grid".equals(layout) ? R.layout.item_app_grid : R.layout.item_app;
            View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            AppInfo app = list.get(position);

            // Icon size
            int sizeDp = prefs.getIconSizeDp();
            int sizePx = dpToPx(sizeDp);
            if (holder.appIcon != null) {
                ViewGroup.LayoutParams lp = holder.appIcon.getLayoutParams();
                lp.width = sizePx; lp.height = sizePx;
                holder.appIcon.setLayoutParams(lp);
                holder.appIcon.setImageDrawable(app.icon);
            }

            // Label
            if (holder.appName != null) {
                holder.appName.setVisibility(prefs.getShowNames() ? View.VISIBLE : View.GONE);
                holder.appName.setText(app.displayLabel);
                holder.appName.setTextSize(TypedValue.COMPLEX_UNIT_SP, prefs.getFontSizeSp());
            }

            // Favorite indicator
            if (holder.favStar != null) {
                holder.favStar.setVisibility(app.isFavorite ? View.VISIBLE : View.GONE);
                try {
                    holder.favStar.setColorFilter(Color.parseColor(prefs.getAccentColor()));
                } catch (Exception ignored) {}
            }

            // Padding
            int pad = dpToPx(prefs.getPaddingDp());
            holder.itemView.setPadding(pad, pad, pad, pad);

            // Click → launch
            holder.itemView.setOnClickListener(v -> {
                Intent launchIntent = packageManager.getLaunchIntentForPackage(app.packageName);
                if (launchIntent != null) startActivity(launchIntent);
            });

            // Long press → context menu
            holder.itemView.setOnLongClickListener(v -> {
                showAppContextMenu(app);
                return true;
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class AppViewHolder extends RecyclerView.ViewHolder {
            TextView  appName;
            ImageView appIcon;
            ImageView favStar;

            AppViewHolder(@NonNull View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.appName);
                appIcon = itemView.findViewById(R.id.appIcon);
                favStar = itemView.findViewById(R.id.favStar);
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Long-press context menu
    // ──────────────────────────────────────────────────────────────────────
    private void showAppContextMenu(AppInfo app) {
        String favLabel  = app.isFavorite ? "★ Quitar de favoritos" : "☆ Agregar a favoritos";
        String hideLabel = "Ocultar app";
        String nameLabel = "✏ Renombrar";
        String infoLabel = "ℹ Info de la app";

        new AlertDialog.Builder(this, getDialogTheme())
                .setTitle(app.displayLabel)
                .setItems(new String[]{favLabel, nameLabel, hideLabel, infoLabel}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Toggle favorite
                            prefs.toggleFavorite(app.packageName);
                            loadApps();
                            break;
                        case 1: // Rename
                            showRenameDialog(app);
                            break;
                        case 2: // Hide
                            prefs.toggleHidden(app.packageName);
                            Toast.makeText(this, "App ocultada. Puedes mostrarla en Ajustes.", Toast.LENGTH_SHORT).show();
                            loadApps();
                            break;
                        case 3: // App info
                            Intent infoIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            infoIntent.setData(android.net.Uri.parse("package:" + app.packageName));
                            startActivity(infoIntent);
                            break;
                    }
                })
                .show();
    }

    private void showRenameDialog(AppInfo app) {
        EditText input = new EditText(this);
        input.setText(app.displayLabel);
        input.selectAll();
        new AlertDialog.Builder(this, getDialogTheme())
                .setTitle("Renombrar \"" + app.originalLabel + "\"")
                .setView(input)
                .setPositiveButton("Guardar", (d, w) -> {
                    String newName = input.getText().toString().trim();
                    prefs.setCustomName(app.packageName, newName.isEmpty() ? null : newName);
                    loadApps();
                })
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Restablecer", (d, w) -> {
                    prefs.setCustomName(app.packageName, null);
                    loadApps();
                })
                .show();
    }

    private int getDialogTheme() {
        switch (prefs.getTheme()) {
            case "light": return android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
            default:      return android.R.style.Theme_DeviceDefault_Dialog_Alert;
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        if (!searchBar.getText().toString().isEmpty()) {
            searchBar.setText("");
        }
        // Don't close the launcher
    }
}
package com.example.perseo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private LauncherPrefs prefs;

    // ── Theme ──────────────────────────────────
    private RadioButton rbDark, rbLight, rbAmoled;

    // ── Layout ─────────────────────────────────
    private RadioButton rbList, rbGrid;
    private SeekBar     sbColumns;
    private TextView    tvColumnsVal;

    // ── Sizes ──────────────────────────────────
    private RadioButton rbIconSm, rbIconMd, rbIconLg;
    private RadioButton rbFontSm, rbFontMd, rbFontLg;

    // ── Display ────────────────────────────────
    private Switch      swShowNames;
    private Switch      swSearchBar;
    private RadioButton rbPadCompact, rbPadNormal, rbPadSpacious;
    private RadioButton rbSortAlpha, rbSortRecent;

    // ── Accent ─────────────────────────────────
    private View        accentPreview;
    private Button      btnPickColor;

    // ── Hidden apps ────────────────────────────
    private Button      btnManageHidden;

    private static final String[] ACCENT_COLORS = {
            "#7C4DFF","#E91E63","#00BCD4","#4CAF50",
            "#FF5722","#FFC107","#2196F3","#9C27B0",
            "#F44336","#607D8B","#FFFFFF","#FF80AB"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = new LauncherPrefs(this);
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Personalizar launcher");
        }

        bindViews();
        loadCurrentValues();
        setupListeners();
    }

    private void applyTheme() {
        switch (prefs.getTheme()) {
            case "light":  setTheme(R.style.Theme_Perseo_Light);  break;
            case "amoled": setTheme(R.style.Theme_Perseo_Amoled); break;
            default:       setTheme(R.style.Theme_Perseo);        break;
        }
    }

    private void bindViews() {
        rbDark    = findViewById(R.id.rbDark);
        rbLight   = findViewById(R.id.rbLight);
        rbAmoled  = findViewById(R.id.rbAmoled);

        rbList    = findViewById(R.id.rbList);
        rbGrid    = findViewById(R.id.rbGrid);
        sbColumns = findViewById(R.id.sbColumns);
        tvColumnsVal = findViewById(R.id.tvColumnsVal);

        rbIconSm  = findViewById(R.id.rbIconSm);
        rbIconMd  = findViewById(R.id.rbIconMd);
        rbIconLg  = findViewById(R.id.rbIconLg);

        rbFontSm  = findViewById(R.id.rbFontSm);
        rbFontMd  = findViewById(R.id.rbFontMd);
        rbFontLg  = findViewById(R.id.rbFontLg);

        swShowNames   = findViewById(R.id.swShowNames);
        swSearchBar   = findViewById(R.id.swSearchBar);

        rbPadCompact  = findViewById(R.id.rbPadCompact);
        rbPadNormal   = findViewById(R.id.rbPadNormal);
        rbPadSpacious = findViewById(R.id.rbPadSpacious);

        rbSortAlpha   = findViewById(R.id.rbSortAlpha);
        rbSortRecent  = findViewById(R.id.rbSortRecent);

        accentPreview = findViewById(R.id.accentPreview);
        btnPickColor  = findViewById(R.id.btnPickColor);
        btnManageHidden = findViewById(R.id.btnManageHidden);
    }

    private void loadCurrentValues() {
        // Theme
        switch (prefs.getTheme()) {
            case "light":  rbLight.setChecked(true);  break;
            case "amoled": rbAmoled.setChecked(true); break;
            default:       rbDark.setChecked(true);   break;
        }

        // Layout
        boolean isGrid = "grid".equals(prefs.getLayout());
        rbGrid.setChecked(isGrid);
        rbList.setChecked(!isGrid);
        sbColumns.setProgress(prefs.getGridColumns() - 2);  // range 2-5 → 0-3
        tvColumnsVal.setText(String.valueOf(prefs.getGridColumns()));
        sbColumns.setEnabled(isGrid);

        // Icon size
        switch (prefs.getIconSize()) {
            case "small": rbIconSm.setChecked(true); break;
            case "large": rbIconLg.setChecked(true); break;
            default:      rbIconMd.setChecked(true); break;
        }

        // Font size
        switch (prefs.getFontSize()) {
            case "small": rbFontSm.setChecked(true); break;
            case "large": rbFontLg.setChecked(true); break;
            default:      rbFontMd.setChecked(true); break;
        }

        // Display
        swShowNames.setChecked(prefs.getShowNames());
        swSearchBar.setChecked(prefs.getSearchVisible());

        // Padding
        switch (prefs.getPadding()) {
            case "compact":  rbPadCompact.setChecked(true);  break;
            case "spacious": rbPadSpacious.setChecked(true); break;
            default:         rbPadNormal.setChecked(true);   break;
        }

        // Sort
        if ("recent".equals(prefs.getSortOrder())) rbSortRecent.setChecked(true);
        else rbSortAlpha.setChecked(true);

        // Accent
        updateAccentPreview();
    }

    private void setupListeners() {
        // Theme
        rbDark.setOnCheckedChangeListener((b, c)   -> { if(c) saveTheme("dark"); });
        rbLight.setOnCheckedChangeListener((b, c)  -> { if(c) saveTheme("light"); });
        rbAmoled.setOnCheckedChangeListener((b, c) -> { if(c) saveTheme("amoled"); });

        // Layout
        rbList.setOnCheckedChangeListener((b, c) -> {
            if (c) { prefs.setLayout("list"); sbColumns.setEnabled(false); }
        });
        rbGrid.setOnCheckedChangeListener((b, c) -> {
            if (c) { prefs.setLayout("grid"); sbColumns.setEnabled(true); }
        });
        sbColumns.setMax(3); // 0-3 = 2-5 columns
        sbColumns.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                int cols = p + 2;
                tvColumnsVal.setText(String.valueOf(cols));
                prefs.setGridColumns(cols);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        // Icon size
        rbIconSm.setOnCheckedChangeListener((b,c) -> { if(c) prefs.setIconSize("small"); });
        rbIconMd.setOnCheckedChangeListener((b,c) -> { if(c) prefs.setIconSize("medium"); });
        rbIconLg.setOnCheckedChangeListener((b,c) -> { if(c) prefs.setIconSize("large"); });

        // Font size
        rbFontSm.setOnCheckedChangeListener((b,c) -> { if(c) prefs.setFontSize("small"); });
        rbFontMd.setOnCheckedChangeListener((b,c) -> { if(c) prefs.setFontSize("medium"); });
        rbFontLg.setOnCheckedChangeListener((b,c) -> { if(c) prefs.setFontSize("large"); });

        // Display
        swShowNames.setOnCheckedChangeListener((b,c) -> prefs.setShowNames(c));
        swSearchBar.setOnCheckedChangeListener((b,c) -> prefs.setSearchVisible(c));

        // Padding
        rbPadCompact.setOnCheckedChangeListener((b,c)  -> { if(c) prefs.setPadding("compact"); });
        rbPadNormal.setOnCheckedChangeListener((b,c)   -> { if(c) prefs.setPadding("normal"); });
        rbPadSpacious.setOnCheckedChangeListener((b,c) -> { if(c) prefs.setPadding("spacious"); });

        // Sort
        rbSortAlpha.setOnCheckedChangeListener((b,c)  -> { if(c) prefs.setSortOrder("alpha"); });
        rbSortRecent.setOnCheckedChangeListener((b,c) -> { if(c) prefs.setSortOrder("recent"); });

        // Accent color
        btnPickColor.setOnClickListener(v -> showColorPicker());

        // Hidden apps
        btnManageHidden.setOnClickListener(v -> showHiddenAppsManager());
    }

    private void saveTheme(String theme) {
        prefs.setTheme(theme);
        // Restart settings activity to apply theme
        finish();
        startActivity(new Intent(this, SettingsActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void updateAccentPreview() {
        try {
            accentPreview.setBackgroundColor(Color.parseColor(prefs.getAccentColor()));
        } catch (Exception e) {
            accentPreview.setBackgroundColor(0xFF7C4DFF);
        }
    }

    private void showColorPicker() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        GridLayout grid = dialogView.findViewById(R.id.colorGrid);

        for (String hex : ACCENT_COLORS) {
            View swatch = new View(this);
            int size = (int)(48 * getResources().getDisplayMetrics().density);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = size; lp.height = size;
            lp.setMargins(8, 8, 8, 8);
            swatch.setLayoutParams(lp);
            swatch.setBackgroundColor(Color.parseColor(hex));
            swatch.setTag(hex);
            grid.addView(swatch);
        }

        // Custom hex input
        EditText etHex = dialogView.findViewById(R.id.etHexColor);
        etHex.setText(prefs.getAccentColor());

        AlertDialog dialog = new AlertDialog.Builder(this, getDialogTheme())
                .setTitle("Color de acento")
                .setView(dialogView)
                .setPositiveButton("Aplicar", (d, w) -> {
                    String hex = etHex.getText().toString().trim();
                    if (!hex.startsWith("#")) hex = "#" + hex;
                    try {
                        Color.parseColor(hex);
                        prefs.setAccentColor(hex);
                        updateAccentPreview();
                    } catch (Exception e) {
                        Toast.makeText(this, "Color inválido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();

        // Swatch click → fill hex input
        for (int i = 0; i < grid.getChildCount(); i++) {
            View swatch = grid.getChildAt(i);
            swatch.setOnClickListener(v -> {
                etHex.setText((String) v.getTag());
            });
        }

        dialog.show();
    }

    private void showHiddenAppsManager() {
        Set<String> hidden = prefs.getHiddenApps();
        if (hidden.isEmpty()) {
            Toast.makeText(this, "No hay apps ocultas", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> pkgs = new ArrayList<>(hidden);
        String[] labels = new String[pkgs.size()];
        boolean[] checked = new boolean[pkgs.size()];

        for (int i = 0; i < pkgs.size(); i++) {
            try {
                labels[i] = getPackageManager()
                        .getApplicationLabel(getPackageManager().getApplicationInfo(pkgs.get(i), 0))
                        .toString();
            } catch (Exception e) {
                labels[i] = pkgs.get(i);
            }
            checked[i] = true; // all hidden → shown as checked = hidden
        }

        new AlertDialog.Builder(this, getDialogTheme())
                .setTitle("Apps ocultas (desmarca para mostrar)")
                .setMultiChoiceItems(labels, checked, (d, which, isChecked) -> {
                    // keep track in checked array
                    checked[which] = isChecked;
                })
                .setPositiveButton("Guardar", (d, w) -> {
                    for (int i = 0; i < pkgs.size(); i++) {
                        if (!checked[i]) prefs.toggleHidden(pkgs.get(i));
                    }
                    Toast.makeText(this, "Apps actualizadas", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private int getDialogTheme() {
        return "light".equals(prefs.getTheme())
                ? android.R.style.Theme_DeviceDefault_Light_Dialog_Alert
                : android.R.style.Theme_DeviceDefault_Dialog_Alert;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
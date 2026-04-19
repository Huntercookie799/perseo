package com.example.perseo;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class LauncherPrefs {

    private static final String PREFS_NAME = "launcher_prefs";

    // Keys
    public static final String KEY_THEME          = "theme";           // dark | light | amoled
    public static final String KEY_LAYOUT         = "layout";          // list | grid
    public static final String KEY_GRID_COLUMNS   = "grid_columns";    // 2-5
    public static final String KEY_ICON_SIZE      = "icon_size";       // small | medium | large
    public static final String KEY_FONT_SIZE      = "font_size";       // small | medium | large
    public static final String KEY_SHOW_NAMES     = "show_names";      // true | false
    public static final String KEY_ACCENT_COLOR   = "accent_color";    // hex string
    public static final String KEY_FAVORITES      = "favorites";       // Set<String> packageNames
    public static final String KEY_HIDDEN_APPS    = "hidden_apps";     // Set<String> packageNames
    public static final String KEY_CUSTOM_NAMES   = "custom_names";    // "pkg::name|pkg2::name2"
    public static final String KEY_SORT_ORDER     = "sort_order";      // alpha | recent | manual
    public static final String KEY_SEARCH_VISIBLE = "search_visible";  // true | false
    public static final String KEY_BG_BLUR        = "bg_blur";         // true | false
    public static final String KEY_PADDING         = "padding";        // compact | normal | spacious

    private final SharedPreferences prefs;

    public LauncherPrefs(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ── Theme ──────────────────────────────────────────────
    public String getTheme()              { return prefs.getString(KEY_THEME, "dark"); }
    public void   setTheme(String t)      { prefs.edit().putString(KEY_THEME, t).apply(); }

    // ── Layout ─────────────────────────────────────────────
    public String getLayout()             { return prefs.getString(KEY_LAYOUT, "list"); }
    public void   setLayout(String l)     { prefs.edit().putString(KEY_LAYOUT, l).apply(); }

    public int  getGridColumns()          { return prefs.getInt(KEY_GRID_COLUMNS, 4); }
    public void setGridColumns(int c)     { prefs.edit().putInt(KEY_GRID_COLUMNS, c).apply(); }

    // ── Sizes ──────────────────────────────────────────────
    public String getIconSize()           { return prefs.getString(KEY_ICON_SIZE, "medium"); }
    public void   setIconSize(String s)   { prefs.edit().putString(KEY_ICON_SIZE, s).apply(); }

    public String getFontSize()           { return prefs.getString(KEY_FONT_SIZE, "medium"); }
    public void   setFontSize(String s)   { prefs.edit().putString(KEY_FONT_SIZE, s).apply(); }

    // ── Display ────────────────────────────────────────────
    public boolean getShowNames()         { return prefs.getBoolean(KEY_SHOW_NAMES, true); }
    public void    setShowNames(boolean b){ prefs.edit().putBoolean(KEY_SHOW_NAMES, b).apply(); }

    public String getAccentColor()        { return prefs.getString(KEY_ACCENT_COLOR, "#7C4DFF"); }
    public void   setAccentColor(String c){ prefs.edit().putString(KEY_ACCENT_COLOR, c).apply(); }

    public boolean getSearchVisible()         { return prefs.getBoolean(KEY_SEARCH_VISIBLE, true); }
    public void    setSearchVisible(boolean b){ prefs.edit().putBoolean(KEY_SEARCH_VISIBLE, b).apply(); }

    public String getPadding()            { return prefs.getString(KEY_PADDING, "normal"); }
    public void   setPadding(String p)    { prefs.edit().putString(KEY_PADDING, p).apply(); }

    public String getSortOrder()          { return prefs.getString(KEY_SORT_ORDER, "alpha"); }
    public void   setSortOrder(String s)  { prefs.edit().putString(KEY_SORT_ORDER, s).apply(); }

    // ── Favorites ──────────────────────────────────────────
    public Set<String> getFavorites() {
        return new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }
    public void setFavorites(Set<String> favs) {
        prefs.edit().putStringSet(KEY_FAVORITES, favs).apply();
    }
    public void toggleFavorite(String pkg) {
        Set<String> favs = getFavorites();
        if (favs.contains(pkg)) favs.remove(pkg); else favs.add(pkg);
        setFavorites(favs);
    }
    public boolean isFavorite(String pkg) { return getFavorites().contains(pkg); }

    // ── Hidden apps ────────────────────────────────────────
    public Set<String> getHiddenApps() {
        return new HashSet<>(prefs.getStringSet(KEY_HIDDEN_APPS, new HashSet<>()));
    }
    public void setHiddenApps(Set<String> hidden) {
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, hidden).apply();
    }
    public void toggleHidden(String pkg) {
        Set<String> hidden = getHiddenApps();
        if (hidden.contains(pkg)) hidden.remove(pkg); else hidden.add(pkg);
        setHiddenApps(hidden);
    }
    public boolean isHidden(String pkg) { return getHiddenApps().contains(pkg); }

    // ── Custom names ───────────────────────────────────────
    // Stored as "pkg1::name1|pkg2::name2"
    public String getCustomName(String pkg) {
        String raw = prefs.getString(KEY_CUSTOM_NAMES, "");
        for (String entry : raw.split("\\|")) {
            String[] parts = entry.split("::", 2);
            if (parts.length == 2 && parts[0].equals(pkg)) return parts[1];
        }
        return null;
    }
    public void setCustomName(String pkg, String name) {
        String raw = prefs.getString(KEY_CUSTOM_NAMES, "");
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (String entry : raw.split("\\|")) {
            if (entry.isEmpty()) continue;
            String[] parts = entry.split("::", 2);
            if (parts.length == 2 && parts[0].equals(pkg)) {
                if (name != null && !name.isEmpty()) sb.append(pkg).append("::").append(name).append("|");
                found = true;
            } else {
                sb.append(entry).append("|");
            }
        }
        if (!found && name != null && !name.isEmpty()) sb.append(pkg).append("::").append(name).append("|");
        prefs.edit().putString(KEY_CUSTOM_NAMES, sb.toString()).apply();
    }

    // ── Icon size in dp ────────────────────────────────────
    public int getIconSizeDp() {
        switch (getIconSize()) {
            case "small":  return 36;
            case "large":  return 72;
            default:       return 52;
        }
    }

    // ── Font size in sp ────────────────────────────────────
    public float getFontSizeSp() {
        switch (getFontSize()) {
            case "small":  return 12f;
            case "large":  return 18f;
            default:       return 15f;
        }
    }

    // ── Padding in dp ──────────────────────────────────────
    public int getPaddingDp() {
        switch (getPadding()) {
            case "compact":   return 4;
            case "spacious":  return 20;
            default:          return 10;
        }
    }
}
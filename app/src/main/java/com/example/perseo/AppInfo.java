package com.example.perseo;

import android.graphics.drawable.Drawable;

public class AppInfo {
    public String packageName;
    public String activityName;
    public String originalLabel;
    public String displayLabel;   // custom name or originalLabel
    public Drawable icon;
    public boolean isFavorite;
    public boolean isHidden;

    public AppInfo(String packageName, String activityName, String originalLabel,
                   Drawable icon, boolean isFavorite, boolean isHidden, String customName) {
        this.packageName  = packageName;
        this.activityName = activityName;
        this.originalLabel = originalLabel;
        this.displayLabel  = (customName != null && !customName.isEmpty()) ? customName : originalLabel;
        this.icon          = icon;
        this.isFavorite    = isFavorite;
        this.isHidden      = isHidden;
    }
}
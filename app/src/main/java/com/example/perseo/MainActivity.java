package com.example.perseo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();
        recyclerView = findViewById(R.id.appsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener la lista de apps instaladas
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, 0);

        // Configurar el adaptador
        AppAdapter adapter = new AppAdapter(availableActivities);
        recyclerView.setAdapter(adapter);
    }

    // Clase interna para el Adaptador
    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
        private List<ResolveInfo> appsList;

        public AppAdapter(List<ResolveInfo> appsList) {
            this.appsList = appsList;
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            ResolveInfo info = appsList.get(position);

            holder.appName.setText(info.loadLabel(packageManager));
            holder.appIcon.setImageDrawable(info.loadIcon(packageManager));

            // Al hacer clic, abrimos la aplicación seleccionada
            holder.itemView.setOnClickListener(v -> {
                Intent launchIntent = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return appsList.size();
        }

        class AppViewHolder extends RecyclerView.ViewHolder {
            TextView appName;
            ImageView appIcon;

            public AppViewHolder(@NonNull View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.appName);
                appIcon = itemView.findViewById(R.id.appIcon);
            }
        }
    }

    // Bloqueamos el botón atrás para que no cierre el Launcher
    @Override
    public void onBackPressed() {
        // No hace nada, el usuario debe quedarse en el home
    }
}
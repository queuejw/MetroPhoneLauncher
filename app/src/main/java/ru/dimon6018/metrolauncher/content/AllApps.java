package ru.dimon6018.metrolauncher.content;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.dimon6018.metrolauncher.R;

public class AllApps extends Fragment {

    private List<Apps> appsList;

    public AllApps(){
        super(R.layout.all_apps_screen);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpApps();
        RecyclerView recyclerView = view.findViewById(R.id.app_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new AppAdapter(getContext(), appsList));
        Button back = view.findViewById(R.id.return_back);
        back.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        });
    }
    public void setUpApps() {
        PackageManager pManager = getContext().getPackageManager();
        appsList = new ArrayList();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = pManager.queryIntentActivities(i, 0);

        for (ResolveInfo ri : allApps) {
            Apps app = new Apps();
            app.app_label = ri.loadLabel(pManager);
            app.app_package = ri.activityInfo.packageName;

            app.app_icon = ri.activityInfo.loadIcon(pManager);
            appsList.add(app);
        }
    }
    public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppHolder> {

        private final LayoutInflater inflater;
        private static List<Apps> appsList;

        AppAdapter(Context context, List<Apps> apps) {
            appsList = apps;
            this.inflater = LayoutInflater.from(context);
        }
        @NonNull
        @Override
        public AppAdapter.AppHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.app, parent, false);
            return new AppHolder(view);
        }
        @Override
        public void onBindViewHolder(AppAdapter.AppHolder holder, int position) {
            Apps apps = appsList.get(position);
            Drawable icon = apps.getDrawable();
            icon.setBounds(0, 0, getResources().getDimensionPixelSize(R.dimen.app_icon_size), getResources().getDimensionPixelSize(R.dimen.app_icon_size));
            holder.icon.setImageDrawable(icon);
            holder.label.setText(apps.getLabel());
            holder.itemView.setOnClickListener(view -> {
                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage((String) apps.getPackagel());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            holder.itemView.setOnLongClickListener(view -> {
                showMenu(holder.itemView, R.menu.app_menu_list);
                return true;
            });
        }
        private void showMenu(View v, @MenuRes int menuRes) {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(menuRes, popup.getMenu());
            popup.setOnMenuItemClickListener(
                    menuItem -> {
                        if(menuItem.getItemId() == R.id.add_to_start) {
                        }
                        else if(menuItem.getItemId() == R.id.delete_app) {
                        }
                        return true;
                    });
            popup.show();
        }
        @Override
        public int getItemCount() {
            return appsList.size();
        }
        public static class AppHolder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView label;

            public AppHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.app_icon);
                label = itemView.findViewById(R.id.app_label);
            }
        }
    }
}
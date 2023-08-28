package ru.dimon6018.metrolauncher;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.elevation.SurfaceColors;

import ru.dimon6018.metrolauncher.content.Start;
import ru.dimon6018.metrolauncher.content.AllApps;
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider;

public class Main extends AppCompatActivity {
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_laucnher);
        getWindow().setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(this));
        BottomNavigationView navbar = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        navbar.setOnItemSelectedListener(item -> {
               if(item.getItemId() == R.id.start_win) {
                   viewPager.setCurrentItem(0);
                   return true;
               } else if(item.getItemId() == R.id.start_apps) {
                    viewPager.setCurrentItem(1);
                    return true;
               } else {
                    return false;
               }
        });
    }
    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
    public AbstractDataProvider getDataProvider() {
        final Start fragment = new Start();
        return fragment.getDataProvider();
    }
    private static class ScreenAdapter extends FragmentStateAdapter {
        public ScreenAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment f;
            if (position == 1) {
               f = new AllApps();
            } else {
              f =  new Start();
            }
            return f;
        }
        @Override
        public int getItemCount() {
            return 2;
        }
    }
}


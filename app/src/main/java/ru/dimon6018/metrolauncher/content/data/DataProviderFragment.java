package ru.dimon6018.metrolauncher.content.data;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import ru.dimon6018.metrolauncher.helpers.AbstractDataProvider;

public class DataProviderFragment extends Fragment {
    private AbstractDataProvider mDataProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);  // keep the mDataProvider instance
        mDataProvider = new DataProvider();
    }

    public AbstractDataProvider getDataProvider() {
        return mDataProvider;
    }
}
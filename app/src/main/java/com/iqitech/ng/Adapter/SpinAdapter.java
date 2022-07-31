package com.iqitech.ng.Adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.iqitech.ng.Models.StateModel;

import java.util.List;

public class SpinAdapter extends ArrayAdapter<StateModel> {

    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private List<StateModel> values;

    public SpinAdapter(Context context, int textViewResourceId,
                       List<StateModel> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public StateModel getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}

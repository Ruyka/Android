package hhp.pdfreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by hhphat on 7/29/2015.
 */
public class ControlPanelListAdapter extends ArrayAdapter<String> {

    public ControlPanelListAdapter(Context context, String[] listItems) {
        super(context,R.layout.row_layout_of_control_panel_list,listItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater theinflater = LayoutInflater.from(getContext());

        final View row = theinflater.inflate(R.layout.row_layout_of_control_panel_list, parent, false);

        String name = getItem(position);

        TextView tmp = (TextView)row.findViewById(R.id.textView_controlPanel);
        tmp.setText(name);

        ImageView img = (ImageView) row.findViewById(R.id.imageView);
        img.setImageResource(getContext().getResources().getIdentifier(name.toLowerCase(),
                "drawable", getContext().getPackageName()));

        row.setTag(name);

        return row;
    }
}

package cn.ommiao.bestchoice;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChoiceAdapter extends ArrayAdapter<Choice> {

    private int itemResourceId;

    public ChoiceAdapter(Context context, int textViewResourceId, List<Choice> objects) {
        super(context, textViewResourceId, objects);
        this.itemResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Choice choice = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(itemResourceId, parent, false);
        TextView tvDesc = view.findViewById(R.id.tv_desc);
        tvDesc.setText(choice.getDesc());
        tvDesc.setSelected(true);
        TextView tvWeight = view.findViewById(R.id.tv_weight);
        tvWeight.setText(String.valueOf(choice.getWeight()));
        tvWeight.setTextColor(Color.parseColor(choice.getColor()));
        ImageView ivColor = view.findViewById(R.id.iv_color);
        ivColor.setColorFilter(Color.parseColor(choice.getColor()));
        return view;
    }
}

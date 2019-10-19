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

public class ChoiceAdapter extends ArrayAdapter<Choice> implements View.OnClickListener{

    public ChoiceAdapter(Context context, List<Choice> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Choice choice = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_choice, parent, false);
        int color = Color.parseColor(choice.getColor());
        TextView tvDesc = view.findViewById(R.id.tv_desc);
        tvDesc.setTextColor(color);
        tvDesc.setText(choice.getDesc());
        tvDesc.setSelected(true);
        TextView tvWeight = view.findViewById(R.id.tv_weight);
        tvWeight.setText(String.valueOf(choice.getWeight()));
        tvWeight.setTextColor(color);
        ImageView ivColor = view.findViewById(R.id.iv_color);
        ivColor.setColorFilter(color);
        ImageView ivDelete = view.findViewById(R.id.iv_delete);
        ivDelete.setColorFilter(color);

        tvDesc.setTag(position);
        tvWeight.setTag(position);
        ivColor.setTag(position);
        ivDelete.setTag(position);

        tvDesc.setOnClickListener(this);
        tvWeight.setOnClickListener(this);
        ivColor.setOnClickListener(this);
        ivDelete.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if(onChoiceClickListener == null){
            return;
        }
        int pos = (int) view.getTag();
        if(view.getId() == R.id.iv_delete){
            onChoiceClickListener.onChoiceDeleteClick(pos);
        } else {
            onChoiceClickListener.onChoiceContentClick(getItem(pos));
        }
    }

    private OnChoiceClickListener onChoiceClickListener;

    public void setOnChoiceClickListener(OnChoiceClickListener onChoiceClickListener) {
        this.onChoiceClickListener = onChoiceClickListener;
    }

    public interface OnChoiceClickListener {
        void onChoiceContentClick(Choice choice);
        void onChoiceDeleteClick(int i);
    }
}

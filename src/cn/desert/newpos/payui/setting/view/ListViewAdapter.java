package cn.desert.newpos.payui.setting.view;

import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.android.newpos.pay.R;


/**
 * @author zhouqiang
 * @email wy1376359644@163.com
 */
public class ListViewAdapter extends BaseAdapter {

    private Context context;
    private int[] imgs;
    private int selected ;
    HashMap<String, Boolean> states = new HashMap<>();

    static class ViewHolder {
          ImageView iv;
          RadioButton rb;
    }

    public ListViewAdapter(Context context, int selected , int[] imgs) {
          // TODO Auto-generated constructor stub
          this.imgs = imgs;
          this.selected = selected ;
          this.context = context;
          for (int i = 0 ; i < this.imgs.length ; i++){
              if(selected == i){
                  states.put(String.valueOf(selected) , true);
              }else {
                  states.put(String.valueOf(i) , false);
              }
          }
    }

    @Override
    public int getCount() {
      // TODO Auto-generated method stub
      return imgs.length;
    }

    @Override
    public Object getItem(int position) {
      // TODO Auto-generated method stub
      return imgs[position];
    }

    @Override
    public long getItemId(int position) {
      // TODO Auto-generated method stub
      return position;
    }

    @Override
    public View getView(final int position,
            View convertView, ViewGroup parent) {
      ViewHolder holder;
      LayoutInflater inflater = LayoutInflater.from(context);
      if (convertView == null) {
        convertView = inflater.inflate(R.layout.setting_bank_list_item, null);
        holder = new ViewHolder();
        holder.iv = (ImageView) convertView.findViewById(R.id.setting_bank_list_iv);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      holder.iv.setImageResource(imgs[position]);
      final RadioButton radio =(RadioButton) convertView.findViewById(R.id.setting_bank_list_rb);
      holder.rb = radio;
      holder.rb.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          for (String key : states.keySet()) {
            states.put(key, false);
          }
          states.put(String.valueOf(position), radio.isChecked());
          notifyDataSetChanged();
        }
      });
      boolean res ;
      if (states.get(String.valueOf(position)) == null
          || states.get(String.valueOf(position)) == false) {
        res = false;
        states.put(String.valueOf(position), false);
      } else{
          res = true;
      }
      holder.rb.setChecked(res);
      return convertView;
    }

    public int getSelectedItemPosition(){
        int positions = 0 ;
        for (int i = 0 ; i < states.size() ; i++){
            if(states.get(String.valueOf(i))){
                positions = i ;
                break;
            }
        }
        return positions ;
    }
}

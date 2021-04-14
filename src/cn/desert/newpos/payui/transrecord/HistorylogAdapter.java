package cn.desert.newpos.payui.transrecord;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.newpos.pay.R;
import com.newpos.libpay.device.printer.PrintRes;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.trans.finace.ServiceEntryMode;
import com.newpos.libpay.trans.translog.TransLogData;
import com.newpos.libpay.utils.PAYUtils;


/**
 * Create by Andy Yuan
 * HistorylogAdapter
 */
public class HistorylogAdapter extends ListAdapter<TransLogData> {

    private TMConfig config;

    public HistorylogAdapter(Activity context) {
        super(context);
        config = TMConfig.getInstance();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHold viewHold = null;
        TransLogData item = null;
        if (mList != null && mList.size() > 0) {
            item = mList.get(position);
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.trans_history_loglist_item, null);
            viewHold = new ViewHold();
            viewHold.tv_pan = (TextView) convertView.findViewById(R.id.tv_pan);
            viewHold.tv_amount = (TextView) convertView.findViewById(R.id.tv_amount);
            viewHold.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            viewHold.tv_voucherno = (TextView) convertView.findViewById(R.id.tv_voucherno);
            viewHold.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
            viewHold.tv_right_top = (TextView) convertView.findViewById(R.id.status_flag);
            convertView.setTag(viewHold);
        } else {
            viewHold = (ViewHold) convertView.getTag();
        }

        if(item == null){
            return null;
        }

        if (item.getIsVoided()) {
            viewHold.tv_right_top.setVisibility(View.VISIBLE);
        } else {
            viewHold.tv_right_top.setVisibility(View.GONE);
        }

        if(!PAYUtils.isNullWithTrim(item.getLocalDate()) && !PAYUtils.isNullWithTrim(item.getLocalTime())){
            String timeStr = PAYUtils.StringPattern(item.getLocalDate() + item.getLocalTime(), "yyyyMMddHHmmss", "yyyy/MM/dd  HH:mm:ss");
            viewHold.tv_date.setText(PrintRes.CH.DATE_TIME+" " + timeStr);
        }else{
            viewHold.tv_date.setVisibility(View.INVISIBLE);
        }

        if(!PAYUtils.isNullWithTrim(item.getTraceNo())){
            viewHold.tv_voucherno.setText(PrintRes.CH.VOUCHER_NO + " " + item.getTraceNo());
        }else{
            viewHold.tv_voucherno.setVisibility(View.INVISIBLE);
        }

        String type = PAYUtils.formatTranstype(item.getEName());
        if(!PAYUtils.isNullWithTrim(type)) {
            viewHold.tv_status.setText(PrintRes.CH.TRANS_TYPE + " " + type);
        }else{
            viewHold.tv_status.setVisibility(View.INVISIBLE);
        }

        String pan = item.getPan() ;
        if (!PAYUtils.isNullWithTrim(pan)) {
            if(item.getMode() == ServiceEntryMode.QRC){
                viewHold.tv_pan.setText(PrintRes.CH.SCANCODE + " " +pan);
            }else {
                viewHold.tv_pan.setText(PrintRes.CH.CARD_NO + " " +pan);
            }
        }else{
            viewHold.tv_pan.setVisibility(View.INVISIBLE);
        }

        String amount = item.getAmount().toString() ;
        if (!PAYUtils.isNullWithTrim(amount)) {
            viewHold.tv_amount.setText(PrintRes.CH.AMOUNT + " " + PrintRes.CH.RMB +" " + PAYUtils.TwoWei(amount));
        }else{
            viewHold.tv_amount.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }


    class ViewHold {
        TextView tv_pan;
        TextView tv_amount;
        TextView tv_date;
        TextView tv_voucherno;
        TextView tv_status;
        TextView tv_right_top;
    }
}

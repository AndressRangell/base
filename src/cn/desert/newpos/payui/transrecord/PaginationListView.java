package cn.desert.newpos.payui.transrecord;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.newpos.pay.R;

import cn.desert.newpos.payui.UIUtils;


/**
 * Creado por Andy Yuan el 18/1/2018.
 */
public class PaginationListView extends ListView implements OnScrollListener {

    int totalItemCount = 0;

    int lastVisibleItem = 0;

    boolean isLoading = false;

    private View footerView;
    private TextView tv;
    private ProgressBar pb;
    private OnLoadListener onLoadListener;

    private int totalItem =0;
    private boolean finishFlag = false;

    private String notice=null;

    /**
     * constructor PaginationList recibe como parametro el contexto
     * @param context
     */
    public PaginationListView(Context context) {
        super(context);
        initView(context);
    }

    /**
     * constructor PaginationList recibe como parametro el contexto y una coleccion de atributos
     * @param context
     * @param attrs
     */
    public PaginationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    /**
     * constructor PaginationList recibe como parametro el contexto, una coleccion de atributos y un estilo
     * @param context
     * @param attrs
     * @param defStyle
     */
    public PaginationListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    /**
     * método para inicializar los datos de la vista de detalles
     * @param context
     */
    private void initView(Context context) {
        LayoutInflater mInflater = LayoutInflater.from(context);
        footerView = mInflater.inflate(R.layout.rongpay_detail_footer, null);
        footerView.setVisibility(View.GONE);
        tv = (TextView) footerView.findViewById(R.id.footer_tv);
        pb = (ProgressBar) footerView.findViewById(R.id.footer_progress);
        this.setOnScrollListener(this);

        this.addFooterView(footerView);
        notice = UIUtils.getStringByInt(context, R.string.trans_load_item);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(!finishFlag){

            if (lastVisibleItem == totalItemCount && scrollState == SCROLL_STATE_IDLE) {
                if (!isLoading ) {
                    isLoading = true;

                    footerView.setVisibility(View.VISIBLE);

                    onLoadListener.onLoad();
                }
            }
        }
    }

    /**
     * método para inicializar los parametros de la vista
     */
    public void initParameters(){
        this.finishFlag = false;
        this.isLoading = false;
        footerView.setVisibility(View.GONE);
    }

    /**
     * método para establecer una señal de finalizacion de la vista
     * @param finishFlag
     */
    public void setFinishFlag(boolean finishFlag){
        this.finishFlag = finishFlag;
        footerView.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);

        if(notice == null){
            tv.setText(""+totalItem+"of the data has beed loaded");
        }else{
            tv.setText(""+totalItem+" "+notice);
        }
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        this.lastVisibleItem = firstVisibleItem + visibleItemCount;
        this.totalItemCount = totalItemCount;
    }

    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public void setTotalItem(int totalItem){
        this.totalItem = totalItem;
    }

    public void loadComplete() {
        //Log.d("YUAN", "finishFlag=" + finishFlag);
        if(!finishFlag) {
            footerView.setVisibility(View.GONE);
        }
        isLoading = false;
        this.invalidate();

    }

    public interface OnLoadListener {
        void onLoad();
    }


}

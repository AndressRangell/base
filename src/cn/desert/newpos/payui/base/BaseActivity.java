package cn.desert.newpos.payui.base;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.fragment.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;

import com.newpos.libpay.Logger;
import com.newpos.libpay.global.TMConfig;

/**
 * clase base de actividad
 * @author zhouqiang
 */
public class BaseActivity extends FragmentActivity {

	protected TopNavigation mDefaultTopNavigation;
	private LinearLayout mActivityLayout;
	private CDT cdt ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		mDefaultTopNavigation = new TopNavigation(this);
		mDefaultTopNavigation.getLeftLayout().setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						back();
					}
				});

		mDefaultTopNavigation.setOnLeftIconClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						back();
					}
				});
		PayApplication.getInstance().addActivity(this);
	}

	/**
	 * método para inflar una vista Linear Layout referenciada por ID,
	 * @param layoutResID recibimos el ID de referencia de la vista que se va a inflar
	 */
	@Override
	public void setContentView(int layoutResID) {
		View content = LayoutInflater.from(this).inflate(layoutResID, null);
		if (mActivityLayout != null) {
			mActivityLayout.removeAllViews();
		}
		mActivityLayout = new LinearLayout(this);
		mActivityLayout.setOrientation(LinearLayout.VERTICAL);
		initNavigationByConfig();
		LinearLayout.LayoutParams layoutParams;
		mActivityLayout.addView(mDefaultTopNavigation);
		layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mActivityLayout.addView(content, layoutParams);
		super.setContentView(mActivityLayout);
	}

	/**
	 * método para inflar una vista Linear Layout
	 * @param view recibimos la vista que se va a inflar
	 */
	@Override
	public void setContentView(View view) {
		if (mActivityLayout != null) {
			mActivityLayout.removeAllViews();
		}
		mActivityLayout = new LinearLayout(this);
		mActivityLayout.setOrientation(LinearLayout.VERTICAL);
		initNavigationByConfig();
		LinearLayout.LayoutParams layoutParams;
		mActivityLayout.addView(mDefaultTopNavigation);
		layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mActivityLayout.addView(view, layoutParams);
		super.setContentView(mActivityLayout);
	}

	/**
	 *método para confirmar si se presiona la tecla back
	 * @param event se recibe la llave de la tecla que se presiona
	 * @return retorna true: si dicha tecla return está bloqueada; false: si está activa
	 */
	@SuppressLint("RestrictedApi")
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if(TMConfig.isLockReturn()){
				return true ;
			}else {
				finish();
				return false ;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * método para capturar datos de eventos de movimiento como mouse, bolígrafo, dedo, trackball
	 * @param ev evento de movimiento
	 * @return boolean
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	/**
	 *
	 */
	private void initNavigationByConfig() {
		NavigationConfig navigationConfig = getClass().getAnnotation(NavigationConfig.class);
		if (navigationConfig != null) {
			if (navigationConfig.leftIconId() != -1) {
				mDefaultTopNavigation.setLeftIcon(navigationConfig.leftIconId());
			}
			if (navigationConfig.rightIconId() != -1) {
				mDefaultTopNavigation.setRightIcon(navigationConfig.rightIconId());
			}
			if (navigationConfig.titleId() != -1) {
				mDefaultTopNavigation.setTitle(navigationConfig.titleId());
			}else if (navigationConfig.titleValue() != null) {
				mDefaultTopNavigation.setTitle(navigationConfig.titleValue());
			}
		}
	}

	/**
	 * método para escuchar evento de click derecho
	 * @param listener oyente de click
	 */
	protected void setRightClickListener(OnClickListener listener){
		mDefaultTopNavigation.setOnRightIconClickListener(listener);
	}

	/**
	 * método para establecer texto a la derecha
	 * @param rid id del texto registrado en values.string
	 */
	protected void setRightText(int rid){
		mDefaultTopNavigation.setRightContent(rid);
	}

	protected void setRightVisiblity(int v){
		mDefaultTopNavigation.setRightContentVisiblity(v);
	}

	protected void setReturnVisible(int visible){
		mDefaultTopNavigation.setLeftIconVisible(visible);
		if(visible == View.VISIBLE){
			mDefaultTopNavigation.setOnLeftIconClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					back();
				}
			});
		}
	}

	protected void setSaveVisible(int visible){
		mDefaultTopNavigation.setRightContentVisiblity(View.INVISIBLE);
		if(visible == View.VISIBLE){
			mDefaultTopNavigation.setOnLeftIconClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					back();
				}
			});
		}
	}

	protected void setNaviTitle(int rid){
		mDefaultTopNavigation.setTitle(rid);
	}

	protected void setNaviTitle(String str){
		mDefaultTopNavigation.setTitle(str);
	}

	//añadido por Andy Yuan
	protected void setNaviTitle(String str, int color) {
		mDefaultTopNavigation.setTitle(str,color);
	}

	/**
	 * clase heredada de CountDownTimer para llevar una cuenta regresiva para alguna animacion o vista
	 */
	private final class CDT extends CountDownTimer{

		public CDT(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			long left = millisUntilFinished/1000 ;
			if(left <= 30){
				mDefaultTopNavigation.setTimeColor(Color.RED);
			}else{
				mDefaultTopNavigation.setTimeColor(Color.GREEN);
			}
			mDefaultTopNavigation.setTime(String.valueOf(millisUntilFinished/1000));
			mDefaultTopNavigation.setTimeVisible(View.VISIBLE);
		}

		@Override
		public void onFinish() {
			back();
		}
	}

	/**
	 * metodo para iniciar un temporizador
	 * @param s numero de segundos para el temporizador
	 */
	protected void startTimer(int s){
		mDefaultTopNavigation.setTime("");
		mDefaultTopNavigation.setTimeVisible(View.VISIBLE);
		if(cdt!=null) {
			cdt.cancel();
		}
		cdt = new CDT(s , 1000);
		cdt.start();
	}

	/**
	 * método para detener el temporizador
	 */
	protected void stopTimer(){
		if(cdt!=null) {
			cdt.cancel();
		}
		mDefaultTopNavigation.setTime("");
		mDefaultTopNavigation.setTimeVisible(View.GONE);
	}

	/**
	 * método para reiniciar el temporizador pasandole de nuevo la cantidad de segundos
	 * @param s numero de segundos para el temporizador
	 */
	protected void refreashTimer(int s){
		mDefaultTopNavigation.setTime("");
		mDefaultTopNavigation.setTimeVisible(View.VISIBLE);
		if(cdt!=null) {
			cdt.cancel();
		}
		cdt = new CDT(s , 1000);
		cdt.start();
	}

	public TopNavigation getTopNavigation() {
		return mDefaultTopNavigation;
	}

	protected void back() {
		new Thread() {
			@Override
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					Logger.error("Exception when onBack"+e.toString());
				}
			}
		}.start();
	}
}

package cn.desert.newpos.payui.splash;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.content.Intent;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.newpos.pay.R;
import com.newpos.libpay.global.TMConfig;

import cn.desert.newpos.payui.IItem;

import cn.desert.newpos.payui.UIUtils;
import cn.desert.newpos.payui.base.PayApplication;
import cn.desert.newpos.payui.setting.view.IPEditText;
import cn.desert.newpos.payui.setting.view.ListViewAdapter;

public class Welcome extends Activity {

	private ViewPager mViewPager;
	private ImageView mPage0;
	private ImageView mPage1;
	private ImageView mPage2;
	private ImageView mPage3;
	private ImageView mPageDoor;

	private View view0 ;
	private View view1 ;
	private View view2 ;
	private View view3 ;
	private View viewDoor ;

	private PagerAdapter adapter ;
	final ArrayList<View> mList = new ArrayList<>();

	private int currIndex = 0;
	private LinearLayout ipportLL ;
	private IPEditText ip ;
	private EditText port ;
	private ListViewAdapter lva ;
	private TMConfig tmConfig ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if(PayApplication.getInstance().isRunned()){
			startHome();
		}
		tmConfig = TMConfig.getInstance();
        setContentView(R.layout.welcome_viewpager);
        mViewPager = (ViewPager)findViewById(R.id.whatsnew_viewpager);
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        
        mPage0 = (ImageView)findViewById(R.id.page0);
        mPage1 = (ImageView)findViewById(R.id.page1);
        mPage2 = (ImageView)findViewById(R.id.page2);
        mPage3 = (ImageView)findViewById(R.id.page3);
        mPageDoor = (ImageView)findViewById(R.id.pageDoor);

        LayoutInflater mLi = LayoutInflater.from(this);
		view0 = mLi.inflate(R.layout.welcome_whats1, null);
		mList.add(view0);
		initView1(view0);

		view1 = mLi.inflate(R.layout.welcome_whats2, null);
		mList.add(view1);
		initView2(view1);

		view2 = mLi.inflate(R.layout.welcome_whats3, null);
		mList.add(view2);
		initView3(view2);

		view3 = mLi.inflate(R.layout.welcome_whats4, null);
		mList.add(view3);
		initView4(view3);

		viewDoor = mLi.inflate(R.layout.welcome_door , null );
		mList.add(viewDoor);
		initViewDoor(viewDoor);
		adapter = new SplashAdapter(mList);
		mViewPager.setAdapter(adapter);
    }    
    

    public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:
				mPage0.setImageDrawable(getResources().getDrawable(R.drawable.icon_now_page));
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.icon_past_page));
				break;
			case 1:
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.icon_now_page));
				mPage0.setImageDrawable(getResources().getDrawable(R.drawable.icon_past_page));
				mPage2.setImageDrawable(getResources().getDrawable(R.drawable.icon_past_page));
				break;
			case 2:
				mPage2.setImageDrawable(getResources().getDrawable(R.drawable.icon_now_page));
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.icon_past_page));
				break;
			case 3:
				mPageDoor.setImageDrawable(getResources().getDrawable(R.drawable.icon_now_page));
				mPage2.setImageDrawable(getResources().getDrawable(R.drawable.icon_past_page));
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.icon_past_page));
				break;
			}
			currIndex = arg0;
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/*
	call it on the welcome_whats_title.xml layout
	 */
	public void next_step(View v){
		if(0 == currIndex){
			if(tmConfig.isOnline()){
				if(ip.isOk()){
					String iptext = ip.getIPText() ;
					String porttext = port.getText().toString() ;
					tmConfig.setIp2(iptext).setPort2(porttext);
					//TODO
					mViewPager.setCurrentItem(currIndex+1 , true);
				}else {
					UIUtils.toast(Welcome.this , false ,R.string.input_err_reinput);
				}
			}else {
				//TODO
				mViewPager.setCurrentItem(currIndex+1 , true);
			}
		}else {
			mViewPager.setCurrentItem(currIndex+1 , true);
		}
	}

	private RadioButton online ;
	private RadioButton offline ;
	private void initView1(View v){
		ipportLL = (LinearLayout) v.findViewById(R.id.welcome_1_ll);
		port = (EditText) v.findViewById(R.id.welcome_1_port);
		ip = (IPEditText) v.findViewById(R.id.welcome_1_ip);
		ip.setIPText(tmConfig.getIP2().split("\\."));
		port.setText(tmConfig.getPort2());
		online = (RadioButton) v.findViewById(R.id.welcome_1_rb_online);
		offline = (RadioButton) v.findViewById(R.id.welcome_1_rb_offline);
		if(tmConfig.isOnline()){
			online.setChecked(true);
			offline.setChecked(false);
		}else {
			online.setChecked(false);
			offline.setChecked(true);
		}
	}

	private void initView2(View v){
		RadioButton debug = (RadioButton) v.findViewById(R.id.welcome_2_debug);
		RadioButton nodebug = (RadioButton) v.findViewById(R.id.welcome_2_nodebug);
		debug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				tmConfig.setDebug(b);
			}
		});
		if(tmConfig.isDebug()){
			debug.setChecked(true);
			nodebug.setChecked(false);
		}else {
			debug.setChecked(false);
			nodebug.setChecked(true);
		}
	}

	private void initView3(View v){
		ListView listView = (ListView) v.findViewById(R.id.welcome_3_lv);
		listView.setSelected(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lva = new ListViewAdapter(Welcome.this, tmConfig.getBankid() , IItem.BankList.IMGS);
		listView.setAdapter(lva);
	}

	private void initView4(View v){
		final RadioButton classical = (RadioButton) v.findViewById(R.id.welcome_4_classical);
		final RadioButton simple = (RadioButton) v.findViewById(R.id.welcome_4_simple);
		if(PayApplication.getInstance().isClassical()){
			classical.setChecked(true);
			simple.setChecked(false);
		}else {
			classical.setChecked(false);
			simple.setChecked(true);
		}
		classical.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b){
					simple.setChecked(false);
					PayApplication.getInstance().setClassical(true);
				}
			}
		});
		simple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b){
					classical.setChecked(false);
					PayApplication.getInstance().setClassical(false);
				}
			}
		});
		ImageView clascical = (ImageView) v.findViewById(R.id.welcome_4_class_iv);
		ImageView simpleiv = (ImageView) v.findViewById(R.id.welcome_4_simple_iv);
		if(!Locale.getDefault().getLanguage().equals("zh")){
			clascical.setImageResource(R.drawable.launcher_classical_en);
			simpleiv.setImageResource(R.drawable.launcher_simple_en);
		}
	}

	/*
	Play animation
	 */
	private void initViewDoor(View v){
		final ImageView mLeft = (ImageView) v.findViewById(R.id.imageLeft);
		final ImageView mRight = (ImageView) v.findViewById(R.id.imageRight);
		final TextView mText = (TextView) v.findViewById(R.id.anim_text);

		final  AnimationSet anim = new AnimationSet(true);
		TranslateAnimation mytranslateanim = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,-1f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f);
		mytranslateanim.setDuration(2000);
		anim.setStartOffset(800);
		anim.addAnimation(mytranslateanim);
		anim.setFillAfter(true);

		final  AnimationSet anim1 = new AnimationSet(true);
		TranslateAnimation mytranslateanim1 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,+1f,
				Animation.RELATIVE_TO_SELF,0f,
				Animation.RELATIVE_TO_SELF,0f);
		mytranslateanim1.setDuration(1500);
		anim1.addAnimation(mytranslateanim1);
		anim1.setStartOffset(800);
		anim1.setFillAfter(true);

		final  AnimationSet anim2 = new AnimationSet(true);
		ScaleAnimation myscaleanim = new ScaleAnimation(
				1f,3f,1f,3f,
				Animation.RELATIVE_TO_SELF,0.5f,
				Animation.RELATIVE_TO_SELF,0.5f);
		myscaleanim.setDuration(1000);
		AlphaAnimation myalphaanim = new AlphaAnimation(1,0.0001f);
		myalphaanim.setDuration(1500);
		anim2.addAnimation(myscaleanim);
		anim2.addAnimation(myalphaanim);
		anim2.setFillAfter(true);

		mText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				tmConfig.setOnline(online.isChecked());
				tmConfig.save();
				mLeft.startAnimation(anim);
				mRight.startAnimation(anim1);
				mText.startAnimation(anim2);
//				new Handler().postDelayed(new Runnable() {
//					@Override
//					public void run() {
						startHome();
//					}
//				} , 400);
			}
		});
	}

	/*
	Después de la configuración, ingrese a HomeActivity.
	 */
	private void startHome(){
		Intent intent = new Intent();
		String clszz = "com.android.newpos.pay.HomeActivity" ;
		ComponentName name = new ComponentName(Welcome.this , clszz);
		intent.setComponent(name);
		startActivity(intent);
		Welcome.this.finish();
	}
}

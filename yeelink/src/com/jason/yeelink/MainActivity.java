package com.jason.yeelink;


import java.util.Calendar;
import java.util.Vector;
import com.jason.yeelink.DataAdapter.DevLsAdapter;
import com.jason.yeelink.base.Device;
import com.jason.yeelink.base.Devices;
import com.jason.yeelink.base.User;
import com.jason.yeelink.common.AppConf;
import com.jason.yeelink.database.DBHelper;
import com.jason.yeelink.database.MyDataBase;
import com.jason.yeelink.util.PullToRefreshView;
import com.jason.yeelink.util.SlideMenu;
import com.jason.yeelink.util.PullToRefreshView.OnHeaderRefreshListener;
import com.jason.yeelink.util.YeelinkAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements android.view.View.OnClickListener{
	private PullToRefreshView mPullToRefreshView;
	private User currentUser;//当前用户
	private Toast toast;		//用来提示用户再次按返回键时退出
	private ListView lv_dev;
	private DBHelper dbHelper;
	private SQLiteDatabase db;
	private ImageView iv_setting;
	private Button bn_about;
	private Button bn_login_out;
	private TextView tv_username;
	private TextView tv_nodata;
	private ImageView iv_loginState;
	private Vector<String> temp=new Vector<String>(4);
	private DevLsAdapter adapter;
    private SlideMenu slideMenu;
	private Thread getDataThread;
	private MainHandler handler;
	private Dialog dialog;
	private static final int FLUSH_LIST=0x02;
	private int first=0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper=new DBHelper(this);
		db=dbHelper.getWritableDatabase();
		setContentView(R.layout.main_yeelink);
		/**
		 * listview添加表都方案暂时取消
		 */
		/*TextView tvHeader=new TextView(MainActivity.this);
		tvHeader.setHeight(50);
		tvHeader.setText("城市列表头");   
		tvHeader.setTextColor(getResources().getColor(R.color.black));
		tvHeader.setGravity(Gravity.CENTER_VERTICAL);
		tvHeader.setBackgroundColor(getResources().getColor(R.color.graywhite));
		lv_dev=(ListView)this.findViewById(R.id.list);
		listView.addHeaderView(tvHeader);*/
		/*temp.add("足球");
		temp.add("篮球");
		temp.add("足球");
		temp.add("篮球");*/
        // 设定ListView 的接收器, 做为选项的来源  
		//adapter=new DevLsAdapter(this, temp, R.drawable.ic_launcher);
       // listView.setAdapter (adapter);  
       
		
		//初始化侧边栏
		init_menu();
		init_view();
		//初始化并读取配置文件
		AppConf.InitSharePreferences(MainActivity.this);	//初始化并读取配置文件
		checkUser();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private void init_view(){
	//	toast=Toast.makeText(MainActivity.this, getString(R.string.exit_toast), Toast.LENGTH_LONG);
		lv_dev=(ListView)findViewById(R.id.lv_dev);
		tv_nodata=(TextView)findViewById(R.id.tv_mainlistnodata);
		handler=new MainHandler();
		//下拉view
    	mPullToRefreshView=(PullToRefreshView)findViewById(R.id.main_pull_refresh_view);
    	dialog = new Dialog(MainActivity.this,R.style.LodingDialog);  
    	
	    dialog.setContentView(R.layout.loading);
	
	    dialog.setCanceledOnTouchOutside(false);
		mPullToRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {
			
			@Override
			public void onHeaderRefresh(PullToRefreshView view) {
				// TODO Auto-generated method stub
				if(currentUser!=null){
					getDevicesData();
					
				}else{
					
					Toast.makeText(MainActivity.this, "请先登陆", Toast.LENGTH_SHORT).show();
					
				}
			}
		});
		lv_dev.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Device dev=(Device)lv_dev.getAdapter().getItem(position);
				Intent intent=new Intent(MainActivity.this,SensorsActivity.class);
				Bundle bundle=new Bundle();
				bundle.putSerializable("device", dev);
				bundle.putString("apikey", currentUser.getApikey());
				intent.putExtra("Bundle", bundle);
				startActivity(intent);
			}
		});
	}
	/*
	 * 更新下拉时间
	 */
	private void refreshComplete(){
		String datetime=getDateTime();
		mPullToRefreshView.onHeaderRefreshComplete("更新于"+datetime);
		AppConf.WriteDateTime("更新于"+datetime);
	}
	/*
	 * 获取当前时间
	 */
	private String getDateTime(){
		Time t=new Time();
        t.setToNow();
       // int year=t.year;
        int month=t.month+1;
        int day=t.monthDay;
        int hour=t.hour;
        int minute=t.minute;
        int sec=t.second;
		return month+"-"+day+" "+hour+":"+minute+":"+sec;
	}
	/*
	 *初始化侧边栏 
	 */
	private void init_menu(){
		
		bn_about=(Button)findViewById(R.id.bn_about);
		bn_login_out=(Button)findViewById(R.id.bn_login_out);
		tv_username=(TextView)findViewById(R.id.tv_username);
		iv_loginState=(ImageView)findViewById(R.id.iv_loginState);
		
		bn_about.setOnClickListener(new MenuLister());
		bn_login_out.setOnClickListener(new MenuLister());
		//滑动侧边栏
		slideMenu = (SlideMenu) findViewById(R.id.slide_menu);
		//实现互动和最上角按键同时进行侧边栏的切换
		ImageView menuImg = (ImageView) findViewById(R.id.title_bar_menu_btn);
		menuImg.setOnClickListener(this);
	}
	class MainHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case FLUSH_LIST:	//刷新列表
					flushDevList((Devices) msg.obj);
					break;
			}
		}
		
	}
	/*
	 * 检测配置文件中包存的当前用户
	 */
	private void checkUser(){
		if(!"".equals(AppConf.currentUser)){	//配置文件中保存有用户信息
			currentUser=MyDataBase.getUser(db, AppConf.currentUser);
			if(currentUser!=null){
				setViewLogin();
				return;
			}
			
		}
		//配置文件中未保存有配置信息,或者数据库中未有用户信息
			setViewNoLogin();
		
	}
	/*
	 * 将Menu中的控件设置为未登录状态
	 */
	private void setViewNoLogin(){
		tv_username.setText(getString(R.string.none_currentUser));
		bn_login_out.setText(getString(R.string.login));
		iv_loginState.setImageDrawable(getResources().getDrawable(R.drawable.login_state_0));
		slideMenu.openMenu();
		currentUser=null;
		lv_dev.setAdapter(null);
		tv_nodata.setVisibility(View.VISIBLE);
		lv_dev.setVisibility(View.GONE);
		Toast.makeText(MainActivity.this, "请登录", Toast.LENGTH_SHORT).show();
	}
	/*
	 * 刷新设备列表，刷新前先验证
	 */
	private void flushDevList(Devices dev){
		if(dev.state.equals("success")){	//获取数据成功，刷新列表
			DevLsAdapter ada=new DevLsAdapter(MainActivity.this,dev.devList);
			lv_dev.setAdapter(ada);
			
			
		}else if("fail".equals(dev.state)){
			Toast.makeText(MainActivity.this, "获取设备列表数据失败，请尝试重新登陆或重新获取APIKEY.", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(MainActivity.this, "网络异常,获取设备列表数据失败", Toast.LENGTH_SHORT).show();
		}
		showNodata(dev);
		refreshComplete();
		if(first==0){
			dialog.cancel();
			first+=1;
		}
	}
	private void showNodata(Devices dev){
		if(!dev.state.equals("success")||dev.devList.size()==0){
			tv_nodata.setVisibility(View.VISIBLE);
			lv_dev.setVisibility(View.GONE);
		}else{
			tv_nodata.setVisibility(View.GONE);
			lv_dev.setVisibility(View.VISIBLE);
		}
	}
/*	Runnable runnable=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mThread.sleep(1000);
				key++;
			} catch (Exception e) {
				// TODO: handle exception
				mHandler.obtainMessage(MSG_FAILURE).sendToTarget();
				return;
			}
			if(key%2==0){
				mHandler.obtainMessage(MSG_FAILURE).sendToTarget();
			}else{
				mHandler.obtainMessage(MSG_SUCCESS).sendToTarget();
			}
			
		}
	};*/

	/**
	 * 左上角打开侧边栏按钮
	 * 
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_bar_menu_btn:
			if (slideMenu.isMainScreenShowing()) {
				slideMenu.openMenu();
			} else {
				slideMenu.closeMenu();
			}
			break;
		}
	}
/**
 * 侧边栏滑动按键事件
 * 
 * @author Jason
 *
 */
	class MenuLister implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.bn_about:
				AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setMessage("关于测试");
				builder.setTitle("关于");
				builder.setPositiveButton("确认", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					    dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
				        dialog.dismiss();
				    }
				});

				builder.create().show();
				break;
			case R.id.bn_login_out:
				//Toast.makeText(MainActivity.this, "登录登出", Toast.LENGTH_SHORT).show();
				if(bn_login_out.getText().toString().equals("登录")){
					//Toast.makeText(MainActivity.this, "登录", Toast.LENGTH_SHORT).show();
					Login();
				}else{
					AppConf.WriteCurrentUser("");	//注销时用来清楚配置文件中保存的当前用户
					setViewNoLogin();
					Login();
				}
				break;
		}
		}
		
	}
	/*
	 * 从网络中获取设备数据
	 */
	private void getDevicesData(){
		//判断一下，防止出现多条相同获取数据的功能线程的情况
		if(getDataThread==null||getDataThread.getState()!=Thread.State.RUNNABLE){
		getDataThread=new Thread(new Runnable() {
						@Override
						public void run() {
							Message msg=new Message();
							msg.arg1=FLUSH_LIST;
							msg.obj=YeelinkAdapter.getDevices(currentUser.getUserName(), currentUser.getApikey());
							handler.sendMessage(msg);
							
						}
					});
		getDataThread.start();
		}
		
	}

	/*
	 *跳转到登陆界面 
	 */
	private void Login(){
		Intent intent=new Intent(MainActivity.this,LoginActivity.class);
		startActivityForResult(intent,0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (resultCode) {
		case RESULT_OK:
			Bundle bundle=data.getExtras();
			User user=(User)bundle.getSerializable("user");
			if(user!=null){
				currentUser=user;
				AppConf.WriteCurrentUser(user.getUserName());
				setViewLogin();
				Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
				slideMenu.closeMenu();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	/*
	 * 将Menu中的控件设置为登录状态
	 */
	private void setViewLogin(){
		tv_username.setText(AppConf.currentUser);
		bn_login_out.setText(getString(R.string.logout));
		iv_loginState.setImageDrawable(getResources().getDrawable(R.drawable.login_state_1));
		//获取数据
		dialog.show();
		getDevicesData();
	}

}

package com.jason.yeelink;





import com.jason.yeelink.R.string;
import com.jason.yeelink.DataAdapter.SenLsAdapter;
import com.jason.yeelink.base.Device;
import com.jason.yeelink.base.Sensor;
import com.jason.yeelink.base.Sensors;
import com.jason.yeelink.common.AppConf;
import com.jason.yeelink.util.PullToRefreshView;
import com.jason.yeelink.util.PullToRefreshView.OnHeaderRefreshListener;
import com.jason.yeelink.util.YeelinkAdapter;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SensorsActivity extends Activity{
	private static final int FLUSH_LIST=0x00;
	public static String apikey;
	public static Device dev;
	private ListView lv_sensors;
	private PullToRefreshView mPullToRefreshView;
	private TextView nodata;
	private Handler sensorHandler;
	private Thread thread;
	private int first=0;
	private Dialog dialog;
	private View localView;
	private SeekBar seekBar;   
	private TextView textDisp;
	private AlertDialog myDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sensors_yeelink);
		getDateByIntent();
		initView();
	}
	private void getDateByIntent(){
		Bundle bundle=this.getIntent().getBundleExtra("Bundle");
		apikey=bundle.getString("apikey");
		dev=(Device)bundle.getSerializable("device");
		
	}
	private void initView(){
		TextView tv_title=(TextView)findViewById(R.id.sensors_title_bar_name);
		lv_sensors=(ListView)findViewById(R.id.lv_sensors);
		nodata=(TextView)findViewById(R.id.tv_sensorslistnodata);
		mPullToRefreshView=(PullToRefreshView)findViewById(R.id.sensors_pull_refresh_view);
		dialog = new Dialog(SensorsActivity.this,R.style.LodingDialog);  
	    dialog.setContentView(R.layout.loading);
	    dialog.setCanceledOnTouchOutside(false);
	    dialog.show();
		mPullToRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {
			
			@Override
			public void onHeaderRefresh(PullToRefreshView view) {
				// TODO Auto-generated method stub
				getSensorsData();
			}
		});
		tv_title.setText(dev.getTitle());
		sensorHandler=new SensorHandler();
		getSensorsData();
		
	}
	class SensorHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
				switch(msg.arg1){
				case FLUSH_LIST:
					flushList((Sensors) msg.obj);
					break;
			}
		}
		
	}

	private void flushList(final Sensors sen){
		if(sen.state.equals("success")){
			//获取成功
			final SenLsAdapter ada=new SenLsAdapter(this, sen.senList);
			lv_sensors.setAdapter(ada);
			lv_sensors.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						final int position, long arg3) {
					// TODO Auto-generated method stub
					localView=LayoutInflater.from(SensorsActivity.this).inflate(R.layout.dialog, null);
					seekBar=(SeekBar)localView.findViewById(R.id.seekBar);
					textDisp=(TextView)localView.findViewById(R.id.description);
				    myDialog=new AlertDialog.Builder(SensorsActivity.this).setTitle("修改").setView(localView).setPositiveButton("确定", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String ssString=YeelinkAdapter.changeGen(apikey,dev.getId(), sen.senList.get(position).getId(), String.valueOf(seekBar.getProgress()));
							sen.senList.get(position).setLast_data_gen(YeelinkAdapter.setLastGen(SensorsActivity.apikey, String.valueOf(seekBar.getProgress())));
							ada.notifyDataSetChanged();
						}
					}).setNegativeButton("取消", null).show();
					
				    seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
						
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							//textDisp.setText("当前值："+progress);
						}
						
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
							// TODO Auto-generated method stub
							//textDisp.setText("当前值："+progress);
						}
						
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress,
								boolean fromUser) {
							// TODO Auto-generated method stub
							textDisp.setText("当前值："+progress);
						}
					});
				  //提醒设置界面各个控件的值
				    if(sen.senList.get(position).getLast_data_gen()==null){
				    	textDisp.setText("当前值："+0);
				    	seekBar.setProgress(0);
				    }else{
				    	int disp=Integer.valueOf(YeelinkAdapter.getGen(sen.senList.get(position).getLast_data_gen()));
				    	textDisp.setText("当前值："+disp);
				    	seekBar.setProgress(disp);
				    }
				}
			});
		}else if(sen.state.equals("fail")){	//apikey有误
			Toast.makeText(this, "获取传感器列表数据失败，请尝试重新登陆或重新获取APIKEY.", Toast.LENGTH_SHORT).show();
		}else{//网络异常
			Toast.makeText(this, "网络异常,获取设备列表数据失败", Toast.LENGTH_SHORT).show();
		}
		showNodata(sen);
		refreshComplete();
		if(first==0){
			dialog.cancel();
		}
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
	private void showNodata(Sensors sen){
		if(!sen.state.equals("success")||sen.senList.size()==0){
			nodata.setVisibility(View.VISIBLE);
			lv_sensors.setVisibility(View.GONE);
		}else{
			nodata.setVisibility(View.GONE);
			lv_sensors.setVisibility(View.VISIBLE);
		}
	}
	private void getSensorsData(){
		//判断一下，防止出现多条相同获取数据的功能线程的情况
		if(thread==null||thread.getState()!=Thread.State.RUNNABLE){
			thread=	new Thread(new Runnable() {
					
					@Override
					public void run() {
						Message msg=new Message();
						msg.arg1=FLUSH_LIST;
						msg.obj=YeelinkAdapter.getSensors(dev.getId(), apikey);
						sensorHandler.sendMessage(msg);
					}
				});
			thread.start();
		}
	}
}

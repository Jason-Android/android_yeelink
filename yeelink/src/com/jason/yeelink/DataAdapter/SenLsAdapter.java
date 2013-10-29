package com.jason.yeelink.DataAdapter;

import java.util.List;
import com.jason.yeelink.R;
import com.jason.yeelink.SensorsActivity;
import com.jason.yeelink.base.Sensor;
import com.jason.yeelink.util.YeelinkAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SenLsAdapter extends BaseAdapter{
	
	private static final int SWITCH_RES=0x00;
	private Context context;
	private List<Sensor> list;
	private Dialog dialog;
//	public static int deviceId;
//	public static String apikey;
	private Thread thread;
	private Handler handler;
	private CheckBox currentCB;		//当前点击的CB

	public SenLsAdapter(Context context, List<Sensor> list) {
		super();
		this.context = context;
		this.list = list;
	//	this.deviceId=device_id;
	//	this.apikey=apikey;
		
		handler=new MyHandler();
		dialog = new Dialog(context,R.style.LodingDialog);  
	    dialog.setContentView(R.layout.loading);
	    dialog.setCanceledOnTouchOutside(false);
	   
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		//return super.getItemViewType(position);
		return list.get(position).getType();
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return list.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Sensor sensor=list.get(position);
		//Log.v("type", sensor.getType()+"");
		switch(sensor.getType()){//根据不同类型显示不同
			case 0://数值型传感器
				convertView=setNumView(sensor,convertView);
				break;
			case 5://开关型传感器
				convertView=setSwitchView(sensor,convertView);
				break;
			case 6://GPS
				convertView=setGPSview(sensor, convertView);
				break;
			case 8://泛型传感器
				convertView=setGenview(sensor, convertView);
				break;
			case 9://图像传感器
				convertView=setPicview(sensor, convertView);
				break;
			case 10://微博抓取器
				convertView=setWBview(sensor, convertView);
				break;
	/*		default:
				convertView=setWBview(sensor,convertView);
				break*/
		}
		return convertView;
	}
	
	//数值型传感器
	private View setNumView(Sensor sensor,View convertView){
		NormalViewHolder nholder=null;
		if(convertView==null)
		{
			convertView= View.inflate(context,R.layout.sensor_normal_item, null);
			nholder=new NormalViewHolder();
			nholder.itemt_layout=(RelativeLayout)convertView.findViewById(R.id.listitem_senosr_layout);
			nholder.list_image=(ImageView)convertView.findViewById(R.id.listitem_senosr_image);
			nholder.main_text=(TextView)convertView.findViewById(R.id.listitem_senosr_main_text);
			nholder.about_text=(TextView)convertView.findViewById(R.id.listitem_senosr_about_text);
			convertView.setTag(nholder);
		}else{
			
			nholder=(NormalViewHolder)convertView.getTag();
		}
		nholder.itemt_layout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.item_bg0));
		nholder.list_image.setImageDrawable(context.getResources().getDrawable(R.drawable.charts_icon));		
		nholder.main_text.setText(sensor.getTitle());
		nholder.about_text.setText(sensor.getLast_data());
		return convertView;
	}
	//开关型传感器
	private View setSwitchView(final Sensor sensor,View convertView){
		SwitchViewHolder sholder=null;
		if(convertView==null)
		{
			convertView=View.inflate(context,R.layout.sensor_switch_item, null);
			sholder=new SwitchViewHolder();
			sholder.main_text=(TextView)convertView.findViewById(R.id.listitem_senosr_switch_main_text);
			sholder.about_text=(TextView)convertView.findViewById(R.id.listitem_senosr_switch_about_text);
			sholder.switch_bn=(CheckBox)convertView.findViewById(R.id.listitem_senosr_switch_btn);
			convertView.setTag(sholder);
		}else{
			sholder=(SwitchViewHolder)convertView.getTag();
		}
		sholder.main_text.setText(sensor.getTitle());
		sholder.about_text.setText(sensor.getAbout());
		if(sensor.getLast_data()!=null&&sensor.getLast_data().equals("1")){
			sholder.switch_bn.setChecked(true);
		}else{
			sholder.switch_bn.setChecked(false);
		}
		
		sholder.switch_bn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				currentCB=(CheckBox)v;
				boolean state=currentCB.isChecked();	//发送的开关指令
				dialog.show();
				thread=new Thread(new switchRunnable(sensor.getId(), state));
				thread.start();
				currentCB.setChecked(!state);	//开关先恢复，等命令成功执行后再改变开关状态
				//Log.v("led", sensor.getLast_data());
				
			}
		});
		
		sholder.switch_bn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(sensor.getLast_data()=="0"){
					sensor.setLast_data("1");
					
					//sensor.setLast_data(last_data)
				}else{
					sensor.setLast_data("0");
					//currentCB.setChecked(true);
				}
				Log.v("led", "改了");
				notifyDataSetChanged();
				//notifyDataSetInvalidated()
			}
		});
		return convertView;
	}

	//GPS传感器VIew
	private View setGPSview(Sensor sensor,View convertView){
		NormalViewHolder nholder=null;
		if(convertView==null)
		{
			convertView= View.inflate(context,R.layout.sensor_normal_item, null);
			nholder=new NormalViewHolder();
			nholder.itemt_layout=(RelativeLayout)convertView.findViewById(R.id.listitem_senosr_layout);
			nholder.list_image=(ImageView)convertView.findViewById(R.id.listitem_senosr_image);
			nholder.main_text=(TextView)convertView.findViewById(R.id.listitem_senosr_main_text);
			nholder.about_text=(TextView)convertView.findViewById(R.id.listitem_senosr_about_text);
			convertView.setTag(nholder);
		}else{
			
			nholder=(NormalViewHolder)convertView.getTag();
		}
		nholder.itemt_layout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.item_bg0));
		nholder.list_image.setImageDrawable(context.getResources().getDrawable(R.drawable.gps_icon));		
		nholder.main_text.setText(sensor.getTitle());
		nholder.about_text.setText(sensor.getAbout());
		return convertView;
	}
	
	//泛型传感器VIew
	private View setGenview(final Sensor sensor,View convertView){
		NormalViewHolder nholder=null;
		if(convertView==null)
		{
			convertView= View.inflate(context,R.layout.sensor_normal_item, null);
			nholder=new NormalViewHolder();
			nholder.itemt_layout=(RelativeLayout)convertView.findViewById(R.id.listitem_senosr_layout);
			nholder.list_image=(ImageView)convertView.findViewById(R.id.listitem_senosr_image);
			nholder.main_text=(TextView)convertView.findViewById(R.id.listitem_senosr_main_text);
			nholder.about_text=(TextView)convertView.findViewById(R.id.listitem_senosr_about_text);
			convertView.setTag(nholder);
		}else{
			
			nholder=(NormalViewHolder)convertView.getTag();
		}
		
		nholder.itemt_layout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.item_bg0));
		nholder.list_image.setImageDrawable(context.getResources().getDrawable(R.drawable.keyval));		
		nholder.main_text.setText(sensor.getTitle());
		nholder.about_text.setText(sensor.getAbout());
		//String ssString=YeelinkAdapter.changeGen(SensorsActivity.apikey,SensorsActivity.dev.getId(), sensor.getId(), "ok");
		//Log.v("lastgen", sensor.getLast_data_gen());
		//Log.v("last", sensor.getLast_data());
		//notifyDataSetChanged();
		//Log.v("result", YeelinkAdapter.getGen(sensor.getLast_data_gen()));
		
		return convertView;
	}
	//图像传感器VIew
	private View setPicview(Sensor sensor,View convertView){
		NormalViewHolder nholder=null;
		if(convertView==null)
		{
			convertView= View.inflate(context,R.layout.sensor_normal_item, null);
			nholder=new NormalViewHolder();
			nholder.itemt_layout=(RelativeLayout)convertView.findViewById(R.id.listitem_senosr_layout);
			nholder.list_image=(ImageView)convertView.findViewById(R.id.listitem_senosr_image);
			nholder.main_text=(TextView)convertView.findViewById(R.id.listitem_senosr_main_text);
			nholder.about_text=(TextView)convertView.findViewById(R.id.listitem_senosr_about_text);
			convertView.setTag(nholder);
		}else{
			
			nholder=(NormalViewHolder)convertView.getTag();
		}
		nholder.itemt_layout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.item_bg0));
		nholder.list_image.setImageDrawable(context.getResources().getDrawable(R.drawable.pic_icon));		
		nholder.main_text.setText(sensor.getTitle());
		nholder.about_text.setText(sensor.getAbout());
		return convertView;
	}
	//微博传感器VIew
	private View setWBview(Sensor sensor,View convertView){
		NormalViewHolder nholder=null;
		if(convertView==null)
		{
			convertView= View.inflate(context,R.layout.sensor_normal_item, null);
			nholder=new NormalViewHolder();
			nholder.itemt_layout=(RelativeLayout)convertView.findViewById(R.id.listitem_senosr_layout);
			nholder.list_image=(ImageView)convertView.findViewById(R.id.listitem_senosr_image);
			nholder.main_text=(TextView)convertView.findViewById(R.id.listitem_senosr_main_text);
			nholder.about_text=(TextView)convertView.findViewById(R.id.listitem_senosr_about_text);
			convertView.setTag(nholder);
		}else{
			
			nholder=(NormalViewHolder)convertView.getTag();
		}
		nholder.itemt_layout.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.item_bg0));
		nholder.list_image.setImageDrawable(context.getResources().getDrawable(R.drawable.bired));		
		nholder.main_text.setText(sensor.getTitle());
		nholder.about_text.setText(sensor.getAbout());
		return convertView;
	}
	/*
	 * 改变开关状态
	 */
	private void changeSwitchState(){
		if(currentCB.isChecked()){
			currentCB.setChecked(false);
		}else{
			currentCB.setChecked(true);
		}
	}
	class switchRunnable implements Runnable{
		private int sensorId;
		private boolean state;
		public switchRunnable(int sensorId,boolean state){
			this.sensorId=sensorId;
			this.state=state;
		}
		@Override
		public void run() {
			String result=YeelinkAdapter.changeSwitchState(SensorsActivity.apikey,SensorsActivity.dev.getId(), sensorId, state);
			Message msg=new Message();
			msg.arg1=SWITCH_RES;
			msg.obj=result;
			handler.sendMessage(msg);
			
		}
		
	};
	class MyHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			switch(msg.arg1){
				case SWITCH_RES:
					dealSwitchResult((String)msg.obj);
					break;
			}
		}
		
	}
	private void dealSwitchResult(String result){
		if(result.equals("")){		
			changeSwitchState();		
		}else if(result.equals("error")){
			Toast.makeText(context, "网络出错，请重试", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(context, "APIKEY有误", Toast.LENGTH_SHORT).show();
		}
		dialog.cancel();
		currentCB=null;
	}
	
	class NormalViewHolder {
		RelativeLayout itemt_layout;
		ImageView list_image;
		TextView main_text;
		TextView about_text;
	} 
	class SwitchViewHolder {
		TextView main_text;
		TextView about_text;
		CheckBox switch_bn;
	}
}

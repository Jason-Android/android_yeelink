package com.jason.yeelink.DataAdapter;

import java.text.DecimalFormat;
import java.util.List;
import com.jason.yeelink.R;
import com.jason.yeelink.base.Device;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DevLsAdapter extends BaseAdapter{
	private Context context;
	private List<Device> list;

	
	public DevLsAdapter(Context context,List<Device> list){
		this.list = list;
		this.context=context;
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
		ViewHolder holder;
		if(convertView==null)
		{
			convertView=View.inflate(context, R.layout.device_item, null);
			holder=new ViewHolder();
			holder.tvDevNum=(TextView)convertView.findViewById(R.id.listitem_dev_num);
			holder.tvDevTitle=(TextView)convertView.findViewById(R.id.listitem_dev_title);
			holder.tvDevAbout=(TextView)convertView.findViewById(R.id.listitem_dev_about);
			convertView.setTag(holder);
			
		}else{
			
			holder=(ViewHolder)convertView.getTag();
		}
		holder.tvDevNum.setText((new DecimalFormat("000").format(position+1)));
		holder.tvDevTitle.setText(list.get(position).getTitle());
		holder.tvDevAbout.setText(list.get(position).getAbout());
		return convertView;
	}
	class ViewHolder{
	    TextView tvDevNum;
		TextView tvDevTitle;
		TextView tvDevAbout;
	}
}

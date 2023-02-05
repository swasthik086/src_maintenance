//package com.suzuki.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.suzuki.R;
//
//import java.util.List;
//
//public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.MyViewHolder> {
//
//    Context mContext;
//    List<RecentData> mData;
//
//    public ViewAdapter(Context mContext,List<RecentData> mData){
//        this.mContext = mContext;
//        this.mData = mData;
//    }
//
//
//
////    @NonNull
//    @Override
//    public MyViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
//
//        View v = LayoutInflater.from(mContext).inflate(R.layout.recent_adapter, viewGroup ,false);
//        MyViewHolder viewHolder = new MyViewHolder(v);
//
//        return viewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
//
////        myViewHolder.tv_name.setText(mData.get(i).getName());
////        myViewHolder.tv_name1.setText(mData.get(i).getName1());
////        myViewHolder.tv_name2.setText(mData.get(i).getName2());
////        myViewHolder.tv_name3.setText(mData.get(i).getName3());
////        myViewHolder.tv_name4.setText(mData.get(i).getName4());
////        myViewHolder.tv_name5.setText(mData.get(i).getName5());
//    }
//
//    @Override
//    public int getItemCount() {
//        return mData.size();
//    }
//
//    public static class MyViewHolder extends RecyclerView.ViewHolder{
//
////        private ImageView img;
////        public TextView tv_name;
////        public TextView tv_name1;
////        public TextView tv_name2;
////        public TextView tv_name3;
////        public TextView tv_name4;
////        public TextView tv_name5;
//
//        public MyViewHolder( View itemView) {
//            super(itemView);
//
//            //img = (ImageView ) itemView.findViewById(R.id.logo);
////        tv_name = (TextView) itemView.findViewById(R.id.Trip1);
////        tv_name1 = (TextView) itemView.findViewById(R.id.Time1);
////        tv_name2 = (TextView) itemView.findViewById(R.id.Cal1);
////        tv_name3 = (TextView) itemView.findViewById(R.id.distance);
////        tv_name4 = (TextView) itemView.findViewById(R.id.start);
////        tv_name5 = (TextView) itemView.findViewById(R.id.end);
//
//        }
//    }
//
//
//
//
//}

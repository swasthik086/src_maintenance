package com.suzuki.adapter;

import android.content.Context;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mappls.sdk.services.api.directions.models.LegStep;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.mappls.sdk.navigation.NavigationFormatter;
import com.mappls.sdk.navigation.routing.NavigationStep;
import com.mappls.sdk.navigation.ui.views.turnlane.TurnLaneAdapter;
import com.suzuki.R;
import com.suzuki.application.SuzukiApplication;

import com.suzuki.model.Stop;
import com.suzuki.utils.Utils;

import java.util.List;

public class NavigationPagerAdapter extends PagerAdapter {

    private List<NavigationStep> mAdvises;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int selectedPosition = 0;
    public static long distance = 0;
    private Stop endPoint;
    public static int meters;


    public NavigationPagerAdapter(Context context, List<NavigationStep> mAdvises, Stop endPoint) {
        this.mContext = context;
        this.mAdvises = mAdvises;
        this.endPoint = endPoint;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }


    @Override
    public int getCount() {
        return mAdvises == null ? 0 : mAdvises.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.navigation_strip_item, container, false);

        container.addView(itemView);

        MyViewHolder holder = new MyViewHolder(itemView);

        NavigationStep trip = mAdvises.get(position);
        int resID = mContext.getResources().getIdentifier("step_" + trip.getManeuverID(), "drawable", mContext.getPackageName());
      /*  if (resID != 0) {
            holder.directionPreviewIcon.setImageResource(resID);
        } else
            holder.directionPreviewIcon.setImageResource(android.R.color.transparent);*/

        holder.directionPreviewText.setText(trip.getDescriptionRoutePartHTML());
        if (selectedPosition == position) {
            meters = (distance > 0 ? 10 * (Math.round(distance / 10)) : 0);
//            if (meters >= 100 * 1000) {
//                meters = (int) (meters / 1000 + 0.5);
//            }

            String fff = NavigationFormatter.getFormattedDistance(meters, (SuzukiApplication) mContext.getApplicationContext());
//            Intent i = new Intent("shortdist").putExtra("shortdist", fff);
//            mContext.sendBroadcast(i);

            holder.directionPreviewDist.setText(NavigationFormatter.getFormattedDistance(meters, (SuzukiApplication) mContext.getApplicationContext()));
        } else {
            if (position > 0) {
                NavigationStep _trip = mAdvises.get(position - 1);


                holder.directionPreviewDist.setText(NavigationFormatter.getFormattedDistance(_trip.distance, (SuzukiApplication) mContext.getApplicationContext()));

            } else {
                holder.directionPreviewDist.setText("");
            }

        }
        holder.container.setBackgroundColor((position == selectedPosition) ? mContext.getResources().getColor(R.color.app_blue) : mContext.getResources().getColor(R.color.colorGray700));
        holder.repeatCurrentInstructionsLayout.setTag(position);
        holder.repeatCurrentInstructionsLayout.setOnClickListener(view -> {
/*
            try {
                if (Integer.parseInt(view.getTag().toString()) == selectedPosition) {
                    ((NavApplication) mContext.getApplicationContext())
                            .getRoutingHelper()
                            .getVoiceRouter()
                            .announceCurrentDirection(((NavApplication) mContext.getApplicationContext())
                                    .getLocationProvider()
                                    .getLastKnownLocation());
                }
            } catch (NumberFormatException e) {
                //ignore
            }*/

        });
        holder.directionPreviewDist.setVisibility((mAdvises.size() - 1 == position) ? View.GONE : View.VISIBLE);

        if ((mAdvises.size() - 1 == position) && trip.getManeuverID() == 8) {
            holder.directionPreviewText.setText(endPoint.getName());
        }



        //   holder.directionPreviewIcon.setVisibility(View.GONE);

        if (trip.getExtraInfo() instanceof LegStep) {
            LegStep legStep = (LegStep) trip.getExtraInfo();
            LegStep nextLegStep=null;

            if (mAdvises.size() > position + 1) {
                if(legStep.maneuver().type().equalsIgnoreCase("roundabout")||legStep.maneuver().type().equalsIgnoreCase("rotary")) {
                    nextLegStep = (LegStep) mAdvises.get(position + 1).getExtraInfo();
               //     float angle = Utils.roundaboutAngle(legStep, nextLegStep);
                //    trip.setManeuverID(Utils.getManeuverID(angle));
                }
            }
            holder.maneuverViewImageView.setImageResource(getDrawableResId(trip.getManeuverID()));

            if (legStep.intersections().size() > 0 && legStep.intersections().get(0).lanes() != null
                    && !TextUtils.isEmpty(legStep.maneuver().modifier())) {
                holder.rvTurnLanes.setBackgroundColor((position == selectedPosition) ? mContext.getResources().getColor(R.color.app_blue) : mContext.getResources().getColor(R.color.colorGray700));

                holder.turnLaneAdapter.addTurnLanes(legStep.intersections().get(0).lanes(), legStep.maneuver().modifier());
                holder.laneGuidanceContainer.setVisibility(View.VISIBLE);
            } else {
                holder.laneGuidanceContainer.setVisibility(View.GONE);

            }
        }

        return itemView;
    }


    int getDrawableResId(int maneuverId) {
        return mContext.getResources().getIdentifier("ic_step_" + maneuverId, "drawable", mContext.getPackageName());
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    public void setDistance(long distance) {
        if (distance > 0) {
            this.distance = 10 * (Math.round(distance / 10));
        } else {
            this.distance = distance;
        }

    }

    public SuzukiApplication getMyApplication() {
        return ((SuzukiApplication) mContext.getApplicationContext());
    }

    class MyViewHolder {
        private final RecyclerView rvTurnLanes;
        private ImageView maneuverViewImageView;

        private TextView directionPreviewText;
        private TextView directionPreviewDist;
        private View container;
        private LinearLayout repeatCurrentInstructionsLayout;
        private TurnLaneAdapter turnLaneAdapter;
        private View laneGuidanceContainer;

        public MyViewHolder(View view) {

            maneuverViewImageView = view.findViewById(R.id.maneuver_image_view);
            directionPreviewText = view.findViewById(R.id.navigation_strip_text);
            directionPreviewDist = view.findViewById(R.id.navigation_strip_dist);
            container = view.findViewById(R.id.strip_item_container);
            repeatCurrentInstructionsLayout = view.findViewById(R.id.repeat_current_instructions_layout);
            rvTurnLanes = view.findViewById(R.id.rvTurnLanes);
            laneGuidanceContainer = view.findViewById(R.id.lane_guidance_container);
            turnLaneAdapter = new TurnLaneAdapter();
            rvTurnLanes.setAdapter(turnLaneAdapter);
            rvTurnLanes.setHasFixedSize(true);
            rvTurnLanes.setLayoutManager(new LinearLayoutManager(view.getContext(),
                    LinearLayoutManager.HORIZONTAL, false));
        }
    }
}
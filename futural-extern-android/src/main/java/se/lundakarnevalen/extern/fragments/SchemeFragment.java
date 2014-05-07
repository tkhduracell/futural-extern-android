package se.lundakarnevalen.extern.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import se.lundakarnevalen.extern.android.ContentActivity;
import se.lundakarnevalen.extern.android.R;
import se.lundakarnevalen.extern.scheme.Event;
import se.lundakarnevalen.extern.scheme.Events;
import se.lundakarnevalen.extern.widget.BounceListView;
import se.lundakarnevalen.extern.widget.LKSchemeAdapter;

import static se.lundakarnevalen.extern.util.ViewUtil.get;

/**
 * Created by Markus on 2014-04-16.
 */
@SuppressWarnings("ResourceType")
public class SchemeFragment extends LKFragment {

    private final int ID = 3;
    private ArrayList<Event> fridayEvents;
 //   private float lastOff = 0;
    private ArrayList<Event> saturdayEvents;

    private ArrayList<Event> sundayEvents;
    RelativeLayout leftArrowLayout;
    RelativeLayout rightArrowLayout;

    // Every time you switch to this fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scheme, container, false);

        ContentActivity activity = ContentActivity.class.cast(getActivity());
        activity.allBottomsUnfocus();
        activity.focusBottomItem(ID);

        leftArrowLayout = get(view, R.id.left_arrow, RelativeLayout.class);
        rightArrowLayout = get(view, R.id.right_arrow, RelativeLayout.class);
        final TextView header = get(view, R.id.dayText, TextView.class);
        final ViewPager vp = get(view, R.id.scheme_viewpager, ViewPager.class);

        int currentDay = getCurrentDay();
        Log.d("currentDay","cur: "+currentDay);
        vp.setBackgroundColor(Color.TRANSPARENT);
        vp.setAdapter(new SchemeViewAdapter());
        vp.setCurrentItem(currentDay);
        vp.setPageTransformer(true, new ZoomOutPageTransformer());

        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                /*
                if(Build.VERSION.SDK_INT < 11) return;
                float absOffset = Math.abs(positionOffset);
                if(absOffset < 0.5f) {

                    if(lastOff > absOffset && header.getTag().equals(Integer.valueOf(position))) {
                        setDayText(header, position + (positionOffset > 0 ? 1 : -1) );
                    }

                    header.setAlpha(1 - absOffset * 2);
                }
                if(absOffset > 0.5f) {
                    if(lastOff < absOffset && header.getTag().equals(Integer.valueOf(position))) {
                        setDayText(header, position + (positionOffset > 0 ? 1 : -1) );
                   }
                    header.setAlpha(1 - (1 - absOffset) * 2);
                }
                lastOff = absOffset;
                */
            }

            @Override
            public void onPageSelected(int position) {
                setDayText(header, position);
                updateLeftArrow(leftArrowLayout, vp);
                updateRightArrow(rightArrowLayout, vp);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}

        });

        leftArrowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLeftArrow(view, vp);
                vp.setCurrentItem(vp.getCurrentItem()-1, true);
            }
        });

        rightArrowLayout.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                updateRightArrow(view, vp);
                vp.setCurrentItem(vp.getCurrentItem()+1, true);
            }
        });

        setDayText(header, currentDay);
        updateRightArrow(rightArrowLayout, vp);
        updateLeftArrow(leftArrowLayout, vp);

        return view;
    }

    private void updateLeftArrow(View layout, ViewPager vp) {
        ImageView arrowPlaceholder = get(layout, R.id.left_arrow_text, ImageView.class);
        int currentItem = vp.getCurrentItem();
        if(currentItem - 1 < 0){
            arrowPlaceholder.setEnabled(false);
            //arrowPlaceholder.setBackgroundResource(R.drawable.arrow_empty);
            leftArrowLayout.setVisibility(View.INVISIBLE);
        } else {
            leftArrowLayout.setVisibility(View.VISIBLE);
            arrowPlaceholder.setEnabled(true);
            //arrowPlaceholder.setBackgroundResource(R.drawable.arrow_left);
        }
    }

    private void updateRightArrow(View layout, ViewPager vp) {
        ImageView arrowPlaceholder = get(layout, R.id.right_arrow_text, ImageView.class);
        int count = vp.getAdapter().getCount();
        int currentItem = vp.getCurrentItem();
        if(currentItem + 1 == count){
            arrowPlaceholder.setEnabled(false);
         //   arrowPlaceholder.setBackgroundResource(R.drawable.arrow_empty);
            rightArrowLayout.setVisibility(View.INVISIBLE);
        } else {
            rightArrowLayout.setVisibility(View.VISIBLE);
            arrowPlaceholder.setEnabled(true);
           // arrowPlaceholder.setBackgroundResource(R.drawable.arrow_right);
        }
    }

    public void setDayText(TextView header, int day) {
        switch(day)
        {
            case 0:
                header.setText(R.string.friday); break;
            case 1:
                header.setText(R.string.saturday); break;
            case 2:
                header.setText(R.string.sunday); break;
        }
        header.setTag(day);
    }
    /**
     * Sets up the ListView in the navigationdrawer menu.
     */
    private ArrayList<LKSchemeAdapter.LKSchemeItem> getSchemeForDay(int day) {
        //Calendar startOfScheme = getStartingDate();

        HashSet<String> activated = getActiveNotifications();
        ArrayList<LKSchemeAdapter.LKSchemeItem> listItems = new ArrayList<LKSchemeAdapter.LKSchemeItem>();

        listItems.add(new LKSchemeAdapter.LKSchemeItem());

        if (day == 0) {
            if (fridayEvents == null) {
                fridayEvents = new ArrayList<Event>();
                Events.getFridayEvents(fridayEvents, getContext());
            }
            for (Event e : fridayEvents) {
                LKSchemeAdapter.LKSchemeItem item = new LKSchemeAdapter.LKSchemeItem(e.place, e.title, e.image, e.startDate, e.endDate, activated, e.id);
                listItems.add(item);
            }

        } else if (day == 1) {
            if (saturdayEvents == null) {
                saturdayEvents = new ArrayList<Event>();
                Events.getSaturdayEvents(saturdayEvents, getContext());
            }
            for (Event e : saturdayEvents) {
                LKSchemeAdapter.LKSchemeItem item = new LKSchemeAdapter.LKSchemeItem(e.place, e.title, e.image, e.startDate, e.endDate, activated, e.id);
                listItems.add(item);
            }
        } else {
            if (sundayEvents == null) {
                sundayEvents = new ArrayList<Event>();
                Events.getSundayEvents(sundayEvents, getContext());
            }
            for (Event e : sundayEvents) {
                LKSchemeAdapter.LKSchemeItem item = new LKSchemeAdapter.LKSchemeItem(e.place, e.title, e.image, e.startDate, e.endDate, activated, e.id);
                listItems.add(item);
            }

        }
        listItems.add(new LKSchemeAdapter.LKSchemeItem());
        return listItems;
    }



    private HashSet<String> getActiveNotifications() {
        SharedPreferences sharedPref = getContext().getSharedPreferences("lundkarnevalen", Context.MODE_PRIVATE);
        String set = sharedPref.getString("notifications", "");
        String split[] = set.split(";");
        HashSet<String> activated = new HashSet<String>();

        for (int i = 0; i < split.length; i++) {
            activated.add(split[i]);
        }
        return activated;
    }

    private int getCurrentDay() {
        Date d = new Date();
        Log.d("date:",""+d.getDate());
        switch(d.getDate()) {
            case 16:
                return 0;
            case 17:
                return 1;
            case 18:
                return 2;
            default:
                return 0;
        }
        //long millisNow = Calendar.getInstance().getTimeInMillis();
        //long millisStart = startOfScheme.getTimeInMillis();
        //return Math.max((int) ((millisNow - millisStart) % (3600000L * 24L)), 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private class SchemeViewAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(container.getContext());

            View view = inflater.inflate(R.layout.scheme_list, container, false);
            container.addView(view);

            BounceListView lv = get(view, R.id.scheme_list, BounceListView.class);
            lv.setCacheColorHint(0); //For keeping the background (not black) while scrolling on API 10
            LKSchemeAdapter schemeAdapter = new LKSchemeAdapter(container.getContext(), getSchemeForDay(position));
            lv.setAdapter(schemeAdapter);
            lv.setOnItemClickListener(schemeAdapter);
            view.setTag(position);
            return position;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(container.findViewWithTag(object));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.getTag() == object;
        }
    }

    /**
     * Fancy animation needs android 11+
     */
    @SuppressLint("NewApi")
    private static class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}

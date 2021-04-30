package com.hyphenate.easeui.widget;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.widget.emojicon.EaseEmojiconIndicatorView;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-03-23 16:04
 */
public class NiuBiChatExtendMenu extends LinearLayout {

    private List<Menu> mExtendMenus;
    private OnMenuItemClickListener mItemClickListener;

    private ViewPager mViewPager;
    private EaseEmojiconIndicatorView mIndicatorView;

    public NiuBiChatExtendMenu(Context context) {
        this(context, null);
    }

    public NiuBiChatExtendMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.ease_widget_chat_extend, this);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mIndicatorView = (EaseEmojiconIndicatorView) findViewById(R.id.indicator_view);
    }

    public void setOnExtendMenuListener(OnMenuItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void initialize() {
        if (CommonUtil.isEmptyList(mExtendMenus)) {
            return;
        }

        List<List<Menu>> extendMenus = new ArrayList<>();
        List<Menu> subMenus = null;
        for (int i = 0; i < mExtendMenus.size(); i++) {
            if (i % 8 == 0) {
                subMenus = new ArrayList<>();
                extendMenus.add(subMenus);
            }
            subMenus.add(mExtendMenus.get(i));
        }

        List<GridView> gridViews = new ArrayList<>(extendMenus.size());
        int defaultPaddingTop = (int) (DevicesUtil.getKeyBoardHeight() / 8.5F);
        for (List<Menu> menus : extendMenus) {
            GridView gridView = new GridView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            gridView.setLayoutParams(params);
            gridView.setPadding(0, defaultPaddingTop, 0, 0);
            gridView.setNumColumns(4);
            gridView.setGravity(Gravity.CENTER_VERTICAL);
            gridView.setVerticalSpacing(PixelUtil.dipToPx(24));
            gridView.setAdapter(new GridViewAdapter(getContext(), menus));
            gridViews.add(gridView);
        }
        mViewPager.setAdapter(new NiuBiAdapter(gridViews));
        mIndicatorView.init(gridViews.size());
        mIndicatorView.setVisibility(gridViews.size() > 1 ? View.VISIBLE : View.GONE);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override public void onPageSelected(int position) {
                mIndicatorView.selectTo(position);
            }

            @Override public void onPageScrollStateChanged(int state) { }
        });
    }

    public void addMenu(int id, int imgRes, String name) {
        if (mExtendMenus == null) {
            mExtendMenus = new ArrayList<>();
        }
        mExtendMenus.add(new Menu(id, imgRes, name));
    }

    public class Menu {
        public int id;
        public int imgRes;
        public String name;

        public Menu(int id, int imgRes, String name) {
            this.id = id;
            this.imgRes = imgRes;
            this.name = name;
        }
    }

    public interface OnMenuItemClickListener {
        void onItemClick(int id, View view);
    }

    public class NiuBiAdapter extends PagerAdapter {

        private List<GridView> gridViewLists;

        public NiuBiAdapter(List<GridView> array) {
            this.gridViewLists = array;
        }

        @Override public int getCount() {
            return CommonUtil.isEmptyList(gridViewLists) ? 0 : gridViewLists.size();
        }

        @Override public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override public Object instantiateItem(ViewGroup container, int position) {
            View view = gridViewLists.get(position);
            container.addView(view);
            return view;
        }

        @Override public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public class GridViewAdapter extends ArrayAdapter<Menu> {
        private Context context;

        public GridViewAdapter(Context context, List<Menu> objects) {
            super(context, 1, objects);
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ExtendMenuItem menuItem = null;
            if (convertView == null) {
                convertView = new ExtendMenuItem(context);
            }

            Menu menu = getItem(position);
            menuItem = (ExtendMenuItem) convertView;
            menuItem.imageView.setBackgroundResource(menu.imgRes);
            menuItem.textView.setText(menu.name);
            menuItem.setOnClickListener(v -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(menu.id, v);
                }
            });
            return convertView;
        }
    }

    public class ExtendMenuItem extends LinearLayout {
        public ImageView imageView;
        public TextView textView;

        public ExtendMenuItem(Context context) {
            this(context, null);
        }

        public ExtendMenuItem(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ExtendMenuItem(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            LayoutInflater.from(context).inflate(R.layout.ease_chat_menu_item, this);
            imageView = (ImageView) findViewById(R.id.image);
            textView = (TextView) findViewById(R.id.text);
        }
    }
}
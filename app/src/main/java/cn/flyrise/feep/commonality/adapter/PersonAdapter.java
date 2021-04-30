package cn.flyrise.feep.commonality.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView;
import cn.flyrise.android.library.view.addressbooklistview.adapter.AddressBookBaseAdapter;
import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.view.Avatar;
import cn.flyrise.feep.core.common.FEToast;

/*--人员适配器,绑定头像和姓名--*/
public final class PersonAdapter extends AddressBookBaseAdapter {
    private final Context context;
    private int viewType = Avatar.NAMERIGHT;
    private final List<AddressBookListItem> persons = new ArrayList<>();
    private final List<AddressBookItem> models = new ArrayList<>();
    private Handler myHandler;
    private boolean isSearch = false;


    public PersonAdapter(Context context, Handler myHandler, boolean isSearch, int viewtype) {
        this(context);
        this.myHandler = myHandler;
        this.isSearch = isSearch;
        this.viewType = viewtype;
    }

    private PersonAdapter(Context context) {
        this.context = context;
        this.viewType = Avatar.NAMERIGHT;
    }

    @Override
    public int getCount() {
        return persons == null ? 0 : persons.size();
    }

    @Override
    public AddressBookListItem getItem(int position) {
        return persons == null ? null : persons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Avatar avatar;
        if (convertView == null) {
            avatar = new Avatar(viewType, context);
            convertView = new FrameLayout(context);
            FrameLayout.LayoutParams fl;
            if (isSearch) {
                fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.mdp_60));
            }
            else {
                fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.mdp_48));
            }
            ((FrameLayout) convertView).addView(avatar, fl);
        }
        else {
            avatar = (Avatar) ((FrameLayout) convertView).getChildAt(0);
        }

        int type = models.get(position).getType();
        String username = models.get(position).getName();
        avatar.setName(stringFilter(username));
//        avatar.setAvatarFace(type, userId);
        avatar.setAvatarFace(type, models.get(position));
        return convertView;
    }

    @Override
    public void refreshAdapter(ArrayList<AddressBookListItem> listDatas) {
        if (listDatas != null) {
            if (listDatas.size() == 0 && (viewType == Avatar.NAMERIGHT || viewType == Avatar.DEPARTMENT_SEARCH_TYPE)) {
                if (myHandler != null) {
                    myHandler.sendEmptyMessage(10011);
                }
                if (!isSearch) {
                    FEToast.showMessage(context.getResources().getString(R.string.result_not_found));
                }
            }
            else if ((listDatas.size() == 0 && viewType == Avatar.NAMERIGHT_RLONG_TEXT) && !AddressBookListView.isSearchRequest) {
                if (myHandler != null) {
                    myHandler.sendEmptyMessage(10011);
                }
                if (!isSearch) {
                    FEToast.showMessage(context.getResources().getString(R.string.result_not_found));
                }
            }
            else if (listDatas.size() == 0 && AddressBookListView.isSearchRequest) {
                persons.clear();
                models.clear();
            }
            else {
                persons.clear();
                models.clear();
                persons.addAll(listDatas);
                for (final AddressBookListItem listItem : listDatas) {
                    models.add(listItem.getAddressBookItem());
                }
            }
        }
        else {
            persons.clear();
            models.clear();
        }
        notifyDataSetChanged();
    }

    public static String stringFilter(String str) {
        str = str.replaceAll("【", "[").replaceAll("】", "]")
                .replaceAll("！", "!").replaceAll("：", ":");// 替换中文标号
        String regEx = "[『』]"; // 清除掉特殊字符
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
}

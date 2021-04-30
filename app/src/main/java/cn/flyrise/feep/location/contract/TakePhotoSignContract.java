package cn.flyrise.feep.location.contract;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 新建：陈冕;
 * 日期： 2017-11-20-9:42.
 */

public interface TakePhotoSignContract {

    interface View {

        void setTime(String text);

        TextView getTime();

        void setTitle(String text);

        String getTitleText();

        String getDescribeText();

        void setEdTitleMaxLen(); //监听标题数量

        void setTitleNumberVisibile(boolean isVisibile);

        void setTitleEnabled(boolean isEnabled);

        void setAddress(String text);

        void setTimeVisible(boolean isVisile);

        void setAddressVisible(boolean isVisile);

        void setmImgPhotoView(Bitmap bitmap);

        BitmapDrawable getmImgPhotoView();

        void setDeleteViewVisible(boolean isVisible);
    }

    int WORKING_TAKE_PHOTO = 1203; //考勤组强制拍照(时间、地点、地址、描述)

    int POIITEM_SEARCH_ERROR = 1204;//搜索周边异常(地点、描述)

    int WORKING_POIITEM_SEARCH_ERROR = 1205;//考勤组搜索周边异常（有考勤时间、地点、描述）

    interface Presenter {

        void getIntentData(Intent intent);

        void getSavedTakePhoto(Bundle savedInstanceState);

        void setSavedTakePhoto(Bundle outState);

        void submit(String text);

        void openTakePhoto();

        void clickDeleteView();

        void deletePhoto();

        void onActivityResult(int requestCode, int resultCode, Intent data);

        void locationPermissionGranted();

        void hasPhoto();

        void onDestroy();

        boolean isWorkingTimeNotAllowSign();
    }
}

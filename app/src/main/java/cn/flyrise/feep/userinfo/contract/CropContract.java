package cn.flyrise.feep.userinfo.contract;

import android.net.Uri;

/**
 * Created by Administrator on 2017-5-15.
 */

public interface CropContract {

    interface Presenter{

        void handleCropResult(Uri uri);

        void cancleUploader();
    }

    interface  View{

        void showLoading();

        void showProgress(int progress);

        void hideLoading();

        void modifySuccess();

        void modifyFailure();
    }
}

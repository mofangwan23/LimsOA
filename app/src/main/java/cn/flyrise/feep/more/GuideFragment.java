package cn.flyrise.feep.more;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.InputStream;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016/5/31 09:04
 */
public class GuideFragment extends Fragment {

    private ImageView mImageView;
    private int mBackgroundImageId;

    public static GuideFragment newInstance(int resId) {
        GuideFragment guideFragment = new GuideFragment();
        guideFragment.setBackgroundImageId(resId);
        return guideFragment;
    }

    public void setBackgroundImageId(int resId) {
        this.mBackgroundImageId = resId;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.use_guide, container, false);
        mImageView =  view.findViewById(R.id.use_guide_img);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        options.inInputShareable = true;

        InputStream inputStream = getActivity().getResources().openRawResource(mBackgroundImageId);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        mImageView.setImageBitmap(bitmap);
        return view;
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            ((BitmapDrawable)mImageView.getDrawable()).getBitmap().recycle();
        }
    }

}

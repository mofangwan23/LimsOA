package cn.flyrise.feep.form.widget.handWritting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import com.google.android.apps.brushes.Slate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FEWrittingCombo extends RelativeLayout {

	private FESlate slate;
	private ImageView whiteboard;
	private SlateConfigView scv;
	private LinearLayout view;
	private Bitmap bitmap;
	private Handler handler = new Handler();

	public FEWrittingCombo(Context context) {
		this(context, null);
	}

	public FEWrittingCombo(Context context, AttributeSet attrs) {
		super(context, attrs);
		view = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.writting_combo, null);
		whiteboard = view.findViewById(R.id.writting_show);
		slate = view.findViewById(R.id.writting_slate);
		scv = new SlateConfigView(context);
		this.addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dipToPx(250)));
		init();
	}

	private void init() {
		slate.setOnWhiteboardUpdateListener(() -> whiteboard.invalidate());

		slate.setPenSize(PixelUtil.dipToPx(5), PixelUtil.dipToPx(5));// 笔触的大小设为6dp
		slate.setPenType(Slate.TYPE_WHITEBOARD);
		slate.setPenColor(Color.rgb(0, 0, 0));
		slate.setupWhiteBoard(DevicesUtil.getScreenWidth(), PixelUtil.dipToPx(85));
		slate.bringToFront();

		bitmap = slate.getWroteBitmap();
		whiteboard.setImageBitmap(bitmap);

		LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		lp.rightMargin = PixelUtil.dipToPx(15);
		scv.setBackgroundColor(getResources().getColor(R.color.color_hei_8));
		scv.setSlate(slate);
		this.addView(scv, lp);
	}

	public void saveBitmapToFile(String savePath, String bitmapFileName) {
		this.whiteboard.getBackground();
		new Thread(() -> {
			saveBitmap(savePath, bitmapFileName);
		}).start();
	}

	private void saveBitmap(String savePath, String bitmapFileName) {
		Bitmap bitmap = this.slate.getWroteFinalBitmap();
		File path = new File(savePath);
		if (path.exists()) {
			path.delete();
		}
		try {
			path.mkdirs();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		File bitmapFile = new File(path + "/" + bitmapFileName);
		if (bitmapFile.exists()) {
			bitmapFile.delete();
		}
		try {
			bitmapFile.createNewFile();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		FileOutputStream bitmapWtriter = null;
		try {
			bitmapWtriter = new FileOutputStream(bitmapFile);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			Log.d("fileTest", "File not found");
		}
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmapWtriter);

		try {
			bitmapWtriter.flush();
			bitmapWtriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bitmapWtriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (bitmap.isRecycled()) {
			bitmap.recycle();// 释放内存
		}
	}

	public void recycleAllBitmaps() {
		FELog.d("WrittingCombo", "Recycle Bitmaps!");
		try {
			slate.recycle();
			((BitmapDrawable) (whiteboard.getBackground())).getBitmap().recycle();
			if (bitmap != null) {
				bitmap.recycle();
			}
			slate = null;
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}

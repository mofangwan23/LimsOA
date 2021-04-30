package cn.flyrise.feep.form.widget.handWritting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import com.google.android.apps.brushes.Slate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FESlate extends Slate {

	private int penColor;
	private float minPenWidth, maxPenWidth;
	private int timeGap = 400;           // 在timeGap都没有触摸事件则认为一个字输入完成 开始处理

	public int getTimeGap() {
		return timeGap;
	}

	public void setTimeGap(int timeGap) {
		this.timeGap = timeGap;
	}

	private float left = Integer.MAX_VALUE, right = 0, top = Integer.MAX_VALUE, bottom = 0; // 记录手写输入图形的边界
	private boolean hasWhiteBoard = false;

	public boolean isHasWhiteBoard() {
		return hasWhiteBoard;
	}

	private WhiteBoard whiteBoard;
	private final Handler handler = new Handler();

	private OnWhiteboardUpdateListener onWhiteboardUpdateListener;

	public interface OnWhiteboardUpdateListener {

		void onUpdate();
	}

	public void setOnWhiteboardUpdateListener(OnWhiteboardUpdateListener l) {
		this.onWhiteboardUpdateListener = l;
	}

	public FESlate(Context c) {
		super(c);
		bindListener();
	}

	public FESlate(Context c, AttributeSet as) {
		super(c, as);
		bindListener();
	}

	/**
	 * 绑定触摸事件的监听器
	 */
	@SuppressLint("ClickableViewAccessibility")
	private void bindListener() {
		this.setOnTouchListener((v, event) -> {
			switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					if (event.getRawX() < left) { // //有触摸事件时更新手写区域的范围
						left = event.getRawX();
					}
					if (event.getRawX() > right) {
						right = event.getRawX();
					}
					if (event.getRawY() < top) {
						top = event.getRawY();
					}
					if (event.getRawY() > bottom) {
						bottom = event.getRawY();
					}
					break;
				case MotionEvent.ACTION_DOWN:
					handler.removeCallbacks(done); // //如果离上一次松开距离不足timeGap，则这行会取消处理上一个字的runnable的运行， 把这两次写的合并成一个字处理
					// test0706
					if (event.getRawX() < left) { // //有触摸事件时更新手写区域的范围
						// left = event.getX();
						left = event.getRawX();
					}
					if (event.getRawX() > right) {
						right = event.getRawX();
					}
					if (event.getRawY() < top) {
						top = event.getRawY();
					}
					if (event.getRawY() > bottom) {
						bottom = event.getRawY();
					}
					break;
				case MotionEvent.ACTION_UP:
					handler.postDelayed(done, timeGap); // //写完一个字 处理
					break;
			}
			return false;
		});
	}

	/**
	 * 初始化Whiteboard
	 */
	public void setupWhiteBoard(int width, int height) {
		this.whiteBoard = new WhiteBoard(width, height);
		hasWhiteBoard = true;
		clearWhiteBoard();
	}

	private void clearWhiteBoard() {
		this.whiteBoard.clear();
	}

	/**
	 * 获取当前Whiteboard的图像
	 */
	public Bitmap getWroteBitmap() {
		if (hasWhiteBoard) {
			return whiteBoard.getResult();
		}
		return null;
	}

	private final Runnable done = new Runnable() { // 手写完一个字的计算处理
		@Override
		public void run() {
			if (Integer.MAX_VALUE == left || ((right - left) < 10 && (bottom - top) < 10)) { // 没更新边界 可能是误操作 比如只点了一下
				FESlate.this.clear();
				return;
			}
			Log.d("XPosition before", Integer.toString(FESlate.this.whiteBoard.currentXPosition));

			Bitmap charBitmap = FESlate.this.getBitmap(); // //获取保存着所写字的bitmap
			charBitmap = extractChar(charBitmap); // 提取有手写内容的区域的bitmap
			if (null == charBitmap) {
				return;
			}

			Log.d("0703 绘图区域 width height", Integer.toString(charBitmap.getWidth()) + "  " + Integer.toString(charBitmap.getHeight()));
			Log.d("0703 绘图域 比例", Double.toString(((double) charBitmap.getHeight()) / charBitmap.getWidth()));

			final float scale = FESlate.this.whiteBoard.charHeight / (float) charBitmap.getHeight(); // 缩放比例 把所写字按原比例缩放到设置的行高
			final int newHeight = FESlate.this.whiteBoard.charHeight;
			final int newWidth = (int) (charBitmap.getWidth() * scale);

			if (newHeight <= 0 || newWidth <= 0) {
				return; // 预防坑爹
			}

			Bitmap resizedBitmap = Bitmap.createScaledBitmap(charBitmap, newWidth, newHeight, true); // 调整尺寸后的图像
			FESlate.this.whiteBoard.writeChar(resizedBitmap);
			FESlate.this.clear();
			charBitmap.recycle();
			resizedBitmap.recycle();

			left = top = Integer.MAX_VALUE; // 重置手写边界区域初值
			right = bottom = 0;
			Log.d("XPosition after", Integer.toString(FESlate.this.whiteBoard.currentXPosition));

			FESlate.this.onWhiteboardUpdateListener.onUpdate(); // 写完一个字的回调函数 一般要做的就是更新下view
		}
	};

	/**
	 * 接受Slate提供的图像 并将手写区域从中提取出来
	 * @param bitmap 要处理的Slate图像
	 */
	private Bitmap extractChar(Bitmap bitmap) {
		if (bitmap == null) {
			return null;    // 预防坑爹...
		}

		int[] loc = new int[2];
		this.getLocationOnScreen(loc); // 手写板在屏幕上所处位置 （左上角的点坐标）
		int slateLeft = loc[0];
		int slateRight = slateLeft + this.getWidth();
		int slateTop = loc[1];
		int slateBottom = slateTop + this.getHeight();

		// 预防坑爹
		left = left < slateLeft ? slateLeft : left;
		top = top < slateTop ? slateTop : top;
		right = right > slateRight ? slateRight : right;
		bottom = bottom > slateBottom ? slateBottom : bottom;

		int charLeft = (int) (left - slateLeft); // 手写图形相对手写板的位置（所写图形左侧边界到手写板左侧边界的距离）
		int charWidth = (int) (right - left);
		int charTop = (int) (top - slateTop);
		int charHeight = (int) (bottom - top);

		if (charLeft < 0 || charTop < 0 || charWidth <= 0 || charHeight <= 0) {
			return null; // 预防坑爹
		}

		Bitmap result = Bitmap.createBitmap(bitmap, charLeft, charTop, charWidth, charHeight);
		if (((double) charWidth) / charHeight > 2) {
			result = adjustChar(result, (int) (((float) (charWidth)) / 2 - charHeight / 2));
			// 按计算出来的坐标、尺寸信息把手写图形提取出来，去除周围的空白部分
		}
		return result;
	}

	/**
	 * 在bitmap上下方加padding 由于截取后的字符会被按比例缩放到固定的高度（行高） 当写的字符很扁 比如“一” 将会被放大到很夸张 因此如果字符的宽高比超过某个极限 就要在extractChar返回的bitmap上下加留白 以正常显示
	 * @param original 原始的bitmap
	 * @param paddingHeight 要加留白的高度（单侧）
	 */
	private Bitmap adjustChar(Bitmap original, int paddingHeight) {
		Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight() + paddingHeight * 2, Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(result);
		Paint tempPaint = new Paint();
		tempPaint.setColor(Color.TRANSPARENT);
		tempCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		tempCanvas.drawBitmap(original, 0, paddingHeight, null);
		tempCanvas.drawRect(0, 0, result.getWidth() - 1, paddingHeight - 1, tempPaint);
		tempCanvas.drawRect(0, result.getHeight() - paddingHeight, result.getWidth() - 1, result.getHeight() - 1, tempPaint);
		return result;
	}

	/*
	 * 内部类WhiteBoard 即保存已写所有字符的“白板”
	 */
	public class WhiteBoard {

		private int charHeight;                                                // 每个字符贴图的高度
		private int charGap = 10;                                    // 字符贴图之间的水平距离
		private int rowGap = 15;                                    // 行距
		private int canvasHeight;                                              // 逐字输入时，保存所有已写字符的画布的高度
		private int canvasWidth;
		private int maxRows;                                                   // 最大行数
		private List<Integer> positions = new ArrayList<>();              // 保存当前字符序列中， 每个字符的右边界的水平位置
		private int horizontalPadding = 10;                                    // 上方和下方的留白大小
		private int initialXPosition = 10;                                    // 初始的光标X坐标 如果设为0不好看 最好留一些边距
		private int initialYPosition = horizontalPadding;
		private int currentXPosition = initialXPosition;                      // 当前“光标”所在的水平位置
		private int currentYPosition = initialYPosition;
		private int currentRow = 1;                                     // 当前所在行数
		private Bitmap wrote;                                                     // 当前所有已写字符的bitmap
		private Canvas canvas;
		private Paint eraser;                                                    // 黑板擦:)

		private String SD_PATH = CoreZygote.getPathServices().getSlateTempPath();
		private List<String> wroteBitmaps;                                               // 手写的图片地址
		private int finalWidth = 1;                                                 // 最终的宽度和高度
		private int finalHeight = 1;

		private WhiteBoard(int width, int height) {
			wrote = Bitmap.createBitmap(width, height, Config.ARGB_4444); // 用于保存已写字符序列的bitmap
			Log.d("0722wb", Integer.toString(width));
			Log.d("0722wb", Integer.toString(height));

			canvasHeight = height;
			canvasWidth = width;
			canvas = new Canvas(wrote);
			canvas.drawColor(0xffffffff); // 白色背景
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			eraser = new Paint(); // “白板擦”的paint 删除字符就是用它画矩形实现的
			eraser.setColor(0xffffffff);
			maxRows = 2; // 设定总行数
			charHeight = (int) Math
					.floor((double) (height - horizontalPadding * 2 - rowGap * (maxRows - 1)) / maxRows); // 根据白板高度 行距和行数设定字符高度
			positions.add(initialXPosition);
			wroteBitmaps = new ArrayList<>();
		}

		/**
		 * 计算应该在哪个位置画出字符 如画布上有足够位置写入，可能会改变currentXPosition currentRow的值
		 * @param width 要插入的字符图像的宽度 用于计算要不要换行
		 * @return 计算出的该字符应该被插入的位置 如果画不下了 返回的数组的第0个元素为-1
		 */

		private int[] calcPosition(int width) {
			int[] position = new int[2];

			if (currentXPosition + width > canvasWidth) { // 需要换行
				if (currentRow == maxRows) { // 画布已满
					Log.d("FESlate.WhiteBoard", "Canvas Full!");
					FEToast.showMessage(getContext().getResources().getString(R.string.handwrite_keepshort));
					position[0] = -1;
				}
				else { // 画布未满 换行后画出字符
					currentRow++;
					position[0] = 0;
					// 0723 position[1] = (currentRow - 1) * (charHeight + rowGap);
					position[1] = currentYPosition + charHeight + rowGap;
					currentYPosition = position[1];
					positions.add(0);
					currentXPosition = (charGap + width);
					Log.d("tricky", "x = charGap+width in calc 换行写字");
					positions.add(currentXPosition);
				}
				finalWidth = canvasWidth;// 如果已经换行了，则是直接给画布的宽度和高度
				finalHeight = canvasHeight;// 如果

			}
			else { // 无需换行 直接画出
				position[0] = currentXPosition;// + charGap;
				// 0723 position[1] = (currentRow - 1) * (charHeight + rowGap);
				position[1] = currentYPosition;
				currentXPosition += (charGap + width);
				Log.d("tricky", "x = charGap+width in calc 不换行写字");
				positions.add(currentXPosition);
				// 如果是是第一行，直接给光标的位置，一个字的高度

				if (currentRow == 2) {// 如果当前行是第二行，则给最大高度
					finalHeight = canvasHeight;
					finalWidth = canvasWidth;
				}
				else {
					finalWidth = currentXPosition + horizontalPadding;
					finalHeight = canvasHeight / 2 + horizontalPadding * 2;
				}
			}
			Log.d("FESlate", "CalcPos: " + Integer.toString(position[0]) + " , " + Integer.toString(position[1]));
			return position;
		}

		/**
		 * 把给定的字符图像画到白板上
		 */
		private void writeChar(Bitmap charBitmap) {
			if (charBitmap == null) {
				return;
			}

			int[] position = calcPosition(charBitmap.getWidth());
			if (position[0] < 0) {
				Log.d("FESlate", "Position[0]<0, can't draw");
				return;
			}
			canvas.drawBitmap(charBitmap, position[0], position[1], null);
			saveBitmap(charBitmap);
			Log.d("FESlate", "width,  height" + Integer.toString(charBitmap.getWidth()) + " " + Integer.toString(charBitmap.getWidth()));
		}

		/*
		 * 删除最近输入的一个字符
		 */
		private void deleteLastChar() {
			if (1 == positions.size()) { // 无字可删
				return;
			}
			int charRightPos = positions.get(positions.size() - 1); // 上一个字符的右边界位置
			int charLeftPos = positions.get(positions.size() - 2); // 再上一个字符的右边界位置
			int charWidth = charRightPos - charLeftPos;
			canvas.drawRect(charLeftPos, currentYPosition, charWidth + charLeftPos, charHeight + currentYPosition, eraser);
			if (0 == charLeftPos) {
				if (1 != currentRow) { // 所删除的字是行首且不是第一行
					positions.remove(positions.size() - 1);
					positions.remove(positions.size() - 1);
					currentRow--;
					currentYPosition -= (charHeight + rowGap);
				}
				else { // 删除的是第一行第一个字符
					positions.remove(positions.size() - 1);
				}
			}
			else { // 非行首字符
				positions.remove(positions.size() - 1);
			}
			currentXPosition = positions.get(positions.size() - 1);
			FESlate.this.onWhiteboardUpdateListener.onUpdate();

			// 删除我们已经保存的痕迹
			deleteBitmap();
		}

		/*
		 * 清空、重置白板
		 */
		private void clear() {
			this.currentRow = 1;
			this.currentXPosition = initialXPosition;
			Log.d("tricky", "x cleared to 0 in clear()");
			this.positions.clear();
			this.positions.add(0);
			this.canvas.drawRect(0, 0, this.canvasWidth, this.canvasHeight, this.eraser);
			this.currentYPosition = initialYPosition;
		}

		/**
		 * 将每次输入的图片保存到本地，以便删除和图片制作
		 */
		private void saveBitmap(Bitmap bitmap) {
			File dFile = new File(SD_PATH);
			if (!dFile.exists()) {
				dFile.mkdirs();
			}
			File file = new File(SD_PATH, (wroteBitmaps.size() + 1) + ".png");
			if (file.exists()) {
				file.delete();
			}

			try {
				// file.createNewFile();
				// 保存到本地
				FileOutputStream out = new FileOutputStream(file);
				bitmap.compress(CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				String path = SD_PATH + "/" + (wroteBitmaps.size() + 1) + ".png";
				wroteBitmaps.add(path);// 添加到数组
				FELog.d("ddd", "保存到本地");
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 删除我们已经保存的痕迹
		 */
		private void deleteBitmap() {
			String path = null;
			try {
				path = wroteBitmaps.get(wroteBitmaps.size() - 1);// 删除最后一张
			} catch (final NullPointerException e) {
				e.printStackTrace();
			}
			if (path == null) {
				return;
			}
			File file = new File(path);
			if (file.exists()) {
				file.delete();
				wroteBitmaps.remove(wroteBitmaps.size() - 1);
			}
		}

		/**
		 * 获取当前的白板的bitmap
		 */
		private Bitmap getResult() {
			return wrote;
		}

		/**
		 * 获得已经写过的手写痕迹图片结果bitmap
		 */
		private Bitmap getFinalResult() {
			System.out.println("final width,height:" + finalWidth + "," + finalHeight);
			// 最终的bitmap
			Bitmap bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Config.RGB_565);
			Canvas finalCanvas = new Canvas(bitmap);

//			finalCanvas.drawColor(Color.TRANSPARENT); // 透明背景
			finalCanvas.drawColor(Color.parseColor("#ffffff")); // 透明背景
			finalCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			// 重置一些参数
			this.currentRow = 1;
			this.currentXPosition = initialXPosition;
			this.currentYPosition = initialYPosition;
			Log.d("tricky", "x cleared to 0 in clear()");
			this.positions.clear();
			this.positions.add(0);

			BitmapFactory.Options options = new Options();
			options.inPreferredConfig = Config.RGB_565;
			// 开始画
			Bitmap sdBitmap;
			try {
				FileInputStream inputStream;
				for (String path : wroteBitmaps) {
					FELog.d("ddd", path);
					inputStream = new FileInputStream(path);
					sdBitmap = BitmapFactory.decodeStream(inputStream, null, options);
					writeFinalChar(sdBitmap, finalCanvas);
					if (sdBitmap != null && sdBitmap.isRecycled()) {
						sdBitmap.recycle();
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return bitmap;
		}

		/**
		 * 把给定的字符图像画到白板上
		 */
		private void writeFinalChar(Bitmap charBitmap, Canvas finalCanvas) {
			if (charBitmap == null) return;
			int[] position = calcFianlPosition(charBitmap.getWidth());
			if (position[0] < 0) {
				Log.d("FESlate", "Position[0]<0, can't draw");
				return;
			}
			finalCanvas.drawBitmap(charBitmap, position[0], position[1], null);
			Log.d("FESlate", "width,  height" + Integer.toString(charBitmap.getWidth()) + " " + Integer.toString(charBitmap.getWidth()));
		}

		private int[] calcFianlPosition(int width) {
			int[] position = new int[2];

			if (currentXPosition + width > finalWidth) { // 需要换行
				if (currentRow == maxRows) { // 画布已满
					Log.d("FESlate.WhiteBoard", "Canvas Full!");
					FELog.d("ddd", "已经写满， Keep it short.");
					position[0] = -1;
				}
				else { // 画布未满 换行后画出字符
					currentRow++;
					position[0] = 0;
					// 0723 position[1] = (currentRow - 1) * (charHeight + rowGap);
					position[1] = currentYPosition + charHeight + rowGap;
					currentYPosition = position[1];
					positions.add(0);
					currentXPosition = (charGap + width);
					Log.d("tricky", "x = charGap+width in calc 换行写字");
					positions.add(currentXPosition);
				}

			}
			else { // 无需换行 直接画出
				position[0] = currentXPosition;// + charGap;
				// 0723 position[1] = (currentRow - 1) * (charHeight + rowGap);
				position[1] = currentYPosition;
				currentXPosition += (charGap + width);
				Log.d("tricky", "x = charGap+width in calc 不换行写字");
				positions.add(currentXPosition);
			}
			Log.d("FESlate", "CalcPos: " + Integer.toString(position[0]) + " , " + Integer.toString(position[1]));
			return position;
		}

	}

	/**
	 * 获得最终的bitmap
	 */
	public Bitmap getWroteFinalBitmap() {
		return this.whiteBoard.getFinalResult();
	}

	public void deleteLastCharOnWhiteboard() {
		this.whiteBoard.deleteLastChar();
	}

	// test only!!!!

	@Deprecated
	public void saveBitmapToTempFile(Bitmap bitmap) {

		FileOutputStream bitmapWtriter = null;
		try {
			File bitmapFile = File.createTempFile("writtenTemp", ".png");
			bitmapWtriter = new FileOutputStream(bitmapFile);
			Log.d("0705", bitmapFile.getAbsolutePath());
		} catch (final Exception e) {
			e.printStackTrace();
			Log.d("fileTest", "File not found");
		}
		bitmap.compress(CompressFormat.PNG, 100, bitmapWtriter);
	}

	@Override
	public void setPenColor(int color) {
		super.setPenColor(color);
		this.penColor = color;
	}

	public int getPenColor() {
		return this.penColor;
	}

	@Override
	public void setPenSize(float minPenWidth, float maxPenWidth) {
		super.setPenSize(minPenWidth, maxPenWidth);
		this.minPenWidth = minPenWidth;
		this.maxPenWidth = maxPenWidth;
	}

	public float[] getPenWidth() {
		float[] penWidth = new float[2];
		penWidth[0] = this.minPenWidth;
		penWidth[1] = this.maxPenWidth;
		return penWidth;
	}

	public int getWhiteboardCharHeight() {
		return this.whiteBoard.charHeight;
	}

	/*
	 * 设置更新监听器 当输入或删除了一个字符后将回调该监听器的onUpdate()函数 一般用于更新视图
	 */
	public void setUpdateListener(OnWhiteboardUpdateListener l) {
		this.onWhiteboardUpdateListener = l;
	}

	/*
	 * 获取当前"光标"的X坐标
	 */
	public int getWhiteboardCurrentXPosition() {
		return this.whiteBoard.currentXPosition;
	}

	/*
	 * 获取当前"光标"的Y坐标
	 */
	public int getWhiteboardCurrentYPosition() {
		return this.whiteBoard.currentYPosition;
	}
}

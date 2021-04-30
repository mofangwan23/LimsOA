package cn.flyrise.feep.core.watermark;

/**
 * @author ZYP
 * @since 2017-09-06 16:09
 */
public interface IWMPaint {

	/**
	 * 绘制水印
	 */
	void draw();

	/**
	 * 更新水印位置
	 */
	void update(int x, int y);

}

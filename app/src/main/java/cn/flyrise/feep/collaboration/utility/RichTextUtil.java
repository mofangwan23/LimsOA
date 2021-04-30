package cn.flyrise.feep.collaboration.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import cn.flyrise.feep.collaboration.view.FontSizeSelectDialog;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;

/**
 * @author ZYP
 * @since 2017-04-27 18:02
 */
public class RichTextUtil {

    /**
     * Code by ZYP:
     * 对富文本编辑器中选择的图片进行压缩处理，先使用采样率对图片进行压缩，后对图片直接进行压缩。
     */
    public static String compressImageByRichEditor(String selectedImage) {
        if (TextUtils.isEmpty(selectedImage)) {
            FELog.e("The image url is null.");
            return null;
        }

        File targetFile = new File(selectedImage);
        if (!targetFile.exists()) {
            FELog.e("The target file doesn't exist.");
            return null;
        }

        String dstPath = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(targetFile), null, options);

            int actualWidth = options.outWidth;
            int actualHeight = options.outHeight;

            int maxWidth = 480;         // 图片宽高最大设置为 480 跟 800
            int maxHeight = 800;
            int inSampleSize = 1;

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                final int halfHeight = actualHeight / 2;
                final int halfWidth = actualWidth / 2;

                while ((halfHeight / inSampleSize) >= maxHeight && (halfWidth / inSampleSize) >= maxWidth) {
                    inSampleSize *= 2;
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(targetFile), null, options);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            while (bos.toByteArray().length / 1024 > 64) {      // 图片最大不能超过 64 K
                bos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                quality = quality - 10;
            }

            dstPath = CoreZygote.getPathServices().getTempFilePath() + File.separator + targetFile.getName();
            File dstFile = new File(dstPath);
            File parentFile = dstFile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            if (dstFile.exists()) {
                dstFile.delete();
            }

            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(dstFile));
            byte[] buff = bos.toByteArray();
            outputStream.write(buff, 0, buff.length);
            outputStream.flush();
            outputStream.close();

        } catch (Exception exp) {
            exp.printStackTrace();
            dstPath = null;
        }
        return dstPath;
    }

    public static String compressImageToBase64(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            FELog.e("Compress image to base64 failed, the image path is null.");
            return null;
        }
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            FELog.e("Compress image to base64 failed, the image doesn't exist.");
            return null;
        }

        String base64Image;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(imagePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            base64Image = "record:image/jpeg;base64," + Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        } catch (Exception exp) {
            exp.printStackTrace();
            base64Image = null;
        }
        return base64Image;
    }

    public static RichStyle buildRichStyle(String[] decorations) {
        RichStyle richStyle = new RichStyle();
        if (decorations != null && decorations.length > 0) {
            for (String decoration : decorations) {
                if (decoration.startsWith("RGB")) {
                    richStyle.rgbColor = decoration;
                    continue;
                }
                switch (decoration) {
                    case "BOLD":
                        richStyle.isBold = true;
                        break;
                    case "ITALIC":
                        richStyle.isItalic = true;
                        break;
                    case "UNDERLINE":
                        richStyle.isUnderLine = true;
                        break;
                    case "JUSTUFYLEFT":
                        richStyle.isAlignLeft = true;
                        break;
                    case "JUSTIFYCENTER":
                        richStyle.isAlignCenter = true;
                        break;
                    case "JUSTIFYRIGHT":
                        richStyle.isAlignRight = true;
                        break;
                    case "2":
                        richStyle.fontSize = FontSizeSelectDialog.FONT_SIZE_SMALL;
                        break;
                    case "4":
                        richStyle.fontSize = FontSizeSelectDialog.FONT_SIZE_DEFAULT;
                        break;
                    case "5":
                        richStyle.fontSize = FontSizeSelectDialog.FONT_SIZE_BIG;
                        break;
                }
            }
        }
        return richStyle;
    }


    public static String buildVoiceHtml(String text, boolean isBold, boolean isUnderLine, int fontColor, int fontSize) {
        StringBuilder voiceHtmlStr = new StringBuilder();
        voiceHtmlStr.append("<font color=\"").append(convertHexColorString(fontColor)).append("\" ")
                .append("size=\"").append(getActualFontSize(fontSize)).append("\" >");

        if (isBold) {
            voiceHtmlStr.append("<b>");
        }

        if (isUnderLine) {
            voiceHtmlStr.append("<u>");
        }

        voiceHtmlStr.append(text);

        if (isUnderLine) {
            voiceHtmlStr.append("</u>");
        }

        if (isBold) {
            voiceHtmlStr.append("</b>");
        }

        voiceHtmlStr.append("</font>");
        return voiceHtmlStr.toString();
    }

    private static int getActualFontSize(int editorFontSize) {
        int actualFontSize;
        switch (editorFontSize) {
            case 0:
                actualFontSize = 4;
                break;
            case FontSizeSelectDialog.FONT_SIZE_SMALL:
                actualFontSize = 2;
                break;
            case FontSizeSelectDialog.FONT_SIZE_DEFAULT:
                actualFontSize = 4;
                break;
            case FontSizeSelectDialog.FONT_SIZE_BIG:
                actualFontSize = 5;
                break;
            default:
                actualFontSize = 4;
        }
        return actualFontSize;
    }

    private static String convertHexColorString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

}

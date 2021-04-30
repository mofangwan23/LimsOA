package cn.flyrise.feep.collaboration.utility;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2017-04-27 15:48
 */
public class RichTextContentKeeper {

    private String mRichTextContent;
    private List<String> mCompressImagePaths;
    private Map<String, String> mLocalAndGUIDMap;

    private RichTextContentKeeper() {
    }

    private static RichTextContentKeeper sInstance;

    public static RichTextContentKeeper getInstance() {
        if (sInstance == null) {
            synchronized (RichTextContentKeeper.class) {
                if (sInstance == null) {
                    sInstance = new RichTextContentKeeper();
                }
            }
        }
        return sInstance;
    }

    public void setRichTextContent(String richTextContent) {
        this.mRichTextContent = richTextContent;
    }

    public void appendRichTextContent(String richTextContent) {
        if (TextUtils.isEmpty(mRichTextContent)) {
            mRichTextContent = richTextContent;
            return;
        }

        mRichTextContent = mRichTextContent + "<br/>" + richTextContent;

    }

    public String getRichTextContent() {
        return this.mRichTextContent;
    }

    public boolean addCompressImagePath(String compressImagePath) {
        if (mCompressImagePaths == null) {
            mCompressImagePaths = new ArrayList<>();
        }

        if (mLocalAndGUIDMap == null) {
            mLocalAndGUIDMap = new HashMap<>();
        }

        if (mCompressImagePaths.contains(compressImagePath)) {
            return false;
        }

        mCompressImagePaths.add(compressImagePath);
        mLocalAndGUIDMap.put(compressImagePath, null);
        return true;
    }

    public void removeCompressImagePath() {
        if(mCompressImagePaths != null) {
            mCompressImagePaths.clear();
        }

        if(mLocalAndGUIDMap != null) {
            mLocalAndGUIDMap.clear();
        }
    }

    public String getGUIDByLocalPath(String localPath) {
        if (mLocalAndGUIDMap == null) {
            return null;
        }

        return mLocalAndGUIDMap.get(localPath);
    }

    public void addLocalAndGUID(String localPath, String gUid) {
        if (mLocalAndGUIDMap == null) {
            mLocalAndGUIDMap = new HashMap<>();
        }

        mLocalAndGUIDMap.put(localPath, gUid);
    }

    public Map<String, String> getLocalAndGUIDMap() {
        return this.mLocalAndGUIDMap;
    }

    public List<String> getCompressImagePaths() {
        return this.mCompressImagePaths;
    }

    public boolean hasContent() {
        return !TextUtils.isEmpty(mRichTextContent);
    }

    public void removeCache() {
        this.mRichTextContent = null;
        this.mCompressImagePaths = null;
        this.mLocalAndGUIDMap = null;
    }

    public boolean isAllImageUpload() {
        if (CommonUtil.isEmptyList(mCompressImagePaths)) {
            return true;
        }

        boolean isAllImageUploadUpload = true;
        for (String path : mCompressImagePaths) {
            String gUid = mLocalAndGUIDMap.get(path);
            if (TextUtils.isEmpty(gUid)) {
                isAllImageUploadUpload = false;
                break;
            }
        }

        return isAllImageUploadUpload;
    }

}

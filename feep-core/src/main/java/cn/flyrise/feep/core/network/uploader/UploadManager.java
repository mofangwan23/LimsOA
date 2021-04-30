package cn.flyrise.feep.core.network.uploader;

import android.app.AlertDialog;
import android.content.Context;

import cn.flyrise.feep.core.network.TokenInterceptor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.FeepDecrypt;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListener;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.request.RequestContent;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.services.ISecurity;
import okhttp3.OkHttpClient;

/**
 * @author ZYP
 * @since 2016-09-06 21:27 负责附件的检查、加密解密以及附件的上传。 当前应用里面，一个上传附件的操作包含 *两个* 请求：1. 上传附件  2. 提交附件信息。 因此这里除了监听上传进度的接口 {@code
 * OnProgressUploadListener} 之外， 还需要一个处理普通 Http 请求结果的回调接口 {@code ResponseCallback}， 当 ResponseCallback 的 {@code onCompleted()}
 * 被调用时，整个附件上传操作才算完成。
 */
public class UploadManager {
    /*
        # How to use

            new UploadManager(context)
                .fileRequest(fileRequest)
                .progressUpdateListener(new OnProgressUpdateListenerImpl() {    // 1. 设置上传监听接口，开始、进度、结果
                    @Override public void onPreExecute() {    // 上传前初始化操作，UploadManager 不做任何显示提示框的事情
                        LoadingHint.show();
                    }

                    @Override public void onProgressUpdate(long currentBytes, long contentLength, boolean done) { // 进度改变
                        int progress = (int) (currentBytes * 100 / contentLength * 1.0F);
                    }
                })
                .responseCallback(new ResponseCallback<ResponseContent>() {       // 2. 设置普通 Http 请求结果回调
                    @Override public void onCompleted(ResponseContent responseContent) {  }   // 完成整个上传操作

                    @Override public void onFailure(RepositoryException repositoryException) { }
                })
                .execute();


        # Cancel upload

            UploadManager uploadManager = new UploadManager(context);
            uploadManager.cancelTask();
     */

    private int MAX_UPLOAD_SIZE;  // 最大上传文件大小，默认 30M
    private final int KNOW_MAX_UPLOAD_SIZE = 100; //知识中心最大上传文件大小 100M；
    private final int KNOW_ITEM_MAX_SIZE = 50;//知识中心单个文件最大大小为50

    private Context mContext;
    private List<String> mUploadFiles;
    private List<String> mEncryptFiles;
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(3);

    private Future mUploadFuture;
    private FileRequest mFileRequest;
    private OnProgressUpdateListener mProgressUpdateListener;
    private ResponseCallback<? extends ResponseContent> mResponseCallback;
    private UploadTask uploadTask;
    private boolean isKnowUpload;

    public UploadManager(Context context) {
        this(context, false);
    }

    public UploadManager(Context context, boolean isKnowUpload) {
        this.mContext = context;
        this.mUploadFiles = new ArrayList<>();
        this.mEncryptFiles = new ArrayList<>();
        this.isKnowUpload = isKnowUpload;
        if (isKnowUpload) {
            MAX_UPLOAD_SIZE = KNOW_MAX_UPLOAD_SIZE;
        } else {
            MAX_UPLOAD_SIZE = NetworkUtil.hasWifi(context) ? 50 : 20;
        }
    }

    public UploadManager fileRequest(FileRequest fileRequest) {
        this.mFileRequest = fileRequest;
        return this;
    }

    public UploadManager progressUpdateListener(OnProgressUpdateListener progressUpdateListener) {
        this.mProgressUpdateListener = progressUpdateListener;
        return this;
    }

    public UploadManager responseCallback(ResponseCallback<? extends ResponseContent> responseCallback) {
        this.mResponseCallback = responseCallback;
        return this;
    }

    public void execute() {
        // Note: 其实这里不应该判断是否有无附件，如果没附件的话，直接使用普通的 post 提交即可，没必要调用这个类进行处理。

        // ******************************** Update By CM ********************************
        if (mFileRequest == null) {
            return;
        }

        if (mFileRequest.getFileContent() == null) {
            if (this.mProgressUpdateListener != null) {
                this.mProgressUpdateListener.onStart();
            }
            executeHttpRequest(mFileRequest.getRequestContent());
            return;
        }
        // ******************************** Update By CM ********************************

        FileRequestContent fileRequestContent = mFileRequest.getFileContent();
        if (fileRequestContent == null || CommonUtil.isEmptyList(fileRequestContent.getFiles())) {
            if (this.mProgressUpdateListener != null) {
                this.mProgressUpdateListener.onStart();
            }
            executeHttpRequest(mFileRequest.getRequestContent());
            return;
        }

        List<String> files = fileRequestContent.getFiles();
        for (String file : files) {                                                                 // 2. 检查是否含有加密文件
            if (ISecurity.BaseSecurity.isEncrypt(file)) {
                mEncryptFiles.add(file);
            } else {
                mUploadFiles.add(file);
            }
        }

        this.tryDecryptFileToUpload();                                                              // 3. 尝试解密文件并上传
    }

    private void tryDecryptFileToUpload() {                                                        // 该方法会反复执行，直到没有加密文件为止
        if (CommonUtil.isEmptyList(mEncryptFiles)) {                                                   // 3.1 没有加密文件
            mFileRequest.getFileContent().setFiles(mUploadFiles);
            if (checkBeforeUpload(mFileRequest)) {                                                 // 3.2 检查文件大小
                executeUpload(mFileRequest);
            }
//			else {
//				if (mResponseCallback != null) {
//					mResponseCallback.onFailure(null);
//				}
//			}
            return;
        }

        for (int i = 0; i < mEncryptFiles.size(); i++) {
            String filePath = mEncryptFiles.remove(0);                                              // 3.3 存在已经解密的该文件
            File file = new File(
                    CoreZygote.getPathServices().getTempFilePath() + filePath.substring(filePath.lastIndexOf("/"), filePath.length()));
            if (file.exists()) {
                mUploadFiles.add(file.getPath());
            } else {                                                                                  // 3.4 执行解密动作
                new FeepDecrypt().decrypt(filePath, new ISecurity.IDecryptListener() {
                    @Override
                    public void onDecryptSuccess(File decryptedFile) {
                        mUploadFiles.add(decryptedFile.getParent());
                        tryDecryptFileToUpload();
                    }

                    @Override
                    public void onDecryptProgress(int progress) {
                    }

                    @Override
                    public void onDecryptFailed() {
                        FEToast.showMessage(CoreZygote.getContext().getResources().getString(R.string.core_attachment_handle_error));
                    }
                });
                return;
            }
        }

        mFileRequest.getFileContent().setFiles(mUploadFiles);                                       // 3.5 继续文件的大小检查和上传
        if (checkBeforeUpload(mFileRequest)) {
            executeUpload(mFileRequest);
        }
    }

    private boolean checkBeforeUpload(final FileRequest fileRequest) {                              // 检查上传内容的大小
        List<String> files = fileRequest.getFileContent().getFiles();
        double totalSize = FileUtil.getSize(files);
        if (totalSize > MAX_UPLOAD_SIZE) {                                                          // 上传内容过大...
            String sAgeFormat = mContext.getResources().getString(R.string.core_all_attachment_size_overflow);
            FEToast.showMessage(String.format(sAgeFormat, String.valueOf(MAX_UPLOAD_SIZE)));
            if (mResponseCallback != null) {
                mResponseCallback.onFailure(null);
            }
            return false;
        }

        if (isKnowUpload) {
            for (String path : files) {
                if (FileUtil.getFileByteSize(path) > KNOW_ITEM_MAX_SIZE * 1024 * 1024) {
                    String sAgeFormat = mContext.getResources().getString(R.string.core_single_attachment_size_overflow);
                    String text = String.format(String.format(sAgeFormat, String.valueOf(KNOW_ITEM_MAX_SIZE)));
                    FELog.e("upload tip : " + text);
                    FEToast.showMessage(String.format(sAgeFormat, String.valueOf(KNOW_ITEM_MAX_SIZE)));
                    if (mResponseCallback != null) {
                        mResponseCallback.onFailure(null);
                    }
                    return false;
                }
            }
        } else if (totalSize > 10d) {                                                                 // 附件大于 10 M
            final java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
            final String attSize = df.format(totalSize);
            new FEMaterialDialog.Builder(mContext)
                    .setMessage(CoreZygote.getContext().getResources().getString(R.string.core_all_attachment_size) + attSize + "M")
                    .setPositiveButton(null, dialog -> {
                        dialog.dismiss();
                        executeUpload(fileRequest);
                    })
                    .setNegativeButton(null, dialog -> {
                        dialog.dismiss();
                        if (mResponseCallback != null) {
                            mResponseCallback.onFailure(null);
                        }
                    })
                    .build()
                    .show();
            return false;
        }
        return true;
    }

    private void executeUpload(FileRequest fileRequest) {                                           // 调用这个类的时候，肯定是存在附件的
        if (this.mProgressUpdateListener != null) {
            this.mProgressUpdateListener.onStart();
        }

        final OkHttpClient okHttpClient = FEHttpClient.getInstance().getOkHttpClient().newBuilder()
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new TokenInterceptor())
                .build();

        uploadTask = new UploadTask.Builder()
                .setHost(FEHttpClient.getInstance().getHost())
                .setFileRequest(fileRequest)
                .setUploadFiles(mUploadFiles)
                .setResponseCallback(mResponseCallback)
                .setOnProgressUpdateListener(mProgressUpdateListener)
                .setOkHttpClient(okHttpClient)
                .build();
        mUploadFuture = mExecutorService.submit(uploadTask);
    }

    private void executeHttpRequest(RequestContent requestContent) {                                 // 没有附件，提交相关参数信息...
        FEHttpClient.getInstance().post(requestContent, this.mResponseCallback);
    }

    public void cancelTask() {
        if (mUploadFuture == null) {
            return;
        }
        if (!mUploadFuture.isDone() || !mUploadFuture.isCancelled()) {
            mUploadFuture.cancel(true);
            uploadTask.cancelTast();
            if (this.mProgressUpdateListener != null) {
                this.mProgressUpdateListener.onCancel();
            }
        }
    }
}

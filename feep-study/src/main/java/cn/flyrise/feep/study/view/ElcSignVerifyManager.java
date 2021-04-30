package cn.flyrise.feep.study.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.study.entity.TrainFinishResponse;
import cn.flyrise.feep.study.entity.TrainingSignRequest;
import cn.flyrise.study.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ElcSignVerifyManager {

    private String infoId;
    private int action;
    private AlertDialog mVerifyDialog;
    private Activity mActivity;
    private String serverId;
    private String master_key;
    private String tableName;
    private String requestType;

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setMaster_key(String master_key) {
        this.master_key = master_key;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setInfoId(String infoId) {
        this.infoId = infoId;
    }

    public void setAction(int action) {
        this.action = action;
    }


    private ElcVerifyCallback mCallback;

    public void setListener(ElcVerifyCallback listener) {
        this.mCallback = listener;
    }

    public ElcSignVerifyManager(Activity activity) {
        this.mActivity = activity;
        if (mActivity == null) {
            throw new NullPointerException("The target activity must not be null.");
        }
    }

    @SuppressWarnings("all") public void startVerify(ElcVerifyCallback callback) {
        this.mCallback = callback;
        startPasswordVerify();
    }

    private void startPasswordVerify() {
        View dialogView = View.inflate(mActivity, R.layout.dialog_elc_sign_verify, null);
        TextView tvName = (TextView) dialogView.findViewById(R.id.tv_user_name);
        EditText editBeizhu = (EditText) dialogView.findViewById(R.id.edit_beizhu);
        String userName = CoreZygote.getLoginUserServices().getUserName();
        if (TextUtils.isEmpty(userName)){
            tvName.setVisibility(View.GONE);
        }else {
            tvName.setText(userName);
        }
        final EditText etPassword = (EditText) dialogView.findViewById(R.id.edit_pwd);
        etPassword.setFocusable(true);
        etPassword.setFocusableInTouchMode(true);
        etPassword.requestFocus();

        dialogView.findViewById(R.id.tvConfirm).setOnClickListener(v -> {
            String text = etPassword.getText().toString().trim();
            String beizhu = editBeizhu.getText().toString().trim();
            if (TextUtils.isEmpty(text)) {
                FEToast.showMessage("请输入密码");
                return;
            }
            if (TextUtils.isEmpty(beizhu)) {
                FEToast.showMessage("请输入备注");
                return;
            }
            if (TextUtils.equals(requestType,"SIGN")){
                verifyPassword("sign",serverId,master_key,tableName,text,beizhu);
            }else if (TextUtils.equals(requestType,"SUBMIT")){
                verifyPassword("submit",serverId,master_key,tableName,text,beizhu);
            }else {
                verifyPassword(text,beizhu);
            }
        });
        dialogView.findViewById(R.id.tvCancel).setOnClickListener(v -> {
            mVerifyDialog.dismiss();
        });

        mVerifyDialog = new AlertDialog.Builder(mActivity)
                .setView(dialogView)
                .setCancelable(true)
                .setOnCancelListener(dialog -> {
                    dialog.dismiss();
                    mActivity.finish();
                })
                .create();
        mVerifyDialog.setCanceledOnTouchOutside(true);
        mVerifyDialog.show();

        Observable.timer(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> DevicesUtil.showKeyboard(etPassword));
    }

    public void verifyPassword(String pwd,String beizhu){

        TrainingSignRequest request = new TrainingSignRequest();
        /**
         *
         int SendDo = 0;         // 送办（响应时才有处理类型） save_sent_doc
         int Return = 1;         // 退回          doc_back_save
         int DealLatter = 2;     // 暂存待办
         int Additional = 3;     // 加签
         int ToggleState = 4;    // 改变跟踪状态(增加跟踪,取消跟踪)
         int NewForm = 5;        // 发起表单
         */
        String actionType = "";
        if (action == 0){
            actionType = "save_sent_doc";
        }else if (action == 1){
            actionType = "doc_back_save";
        }
        request.setAction(actionType);
        request.setUserId(CoreZygote.getLoginUserServices().getUserId());
        request.setPassword(pwd);
        request.setType("13");
        request.setRemark(beizhu);
        request.setWf_inforid(infoId);
        FEHttpClient.getInstance().post(request, new ResponseCallback<TrainFinishResponse>() {
            @Override public void onCompleted(TrainFinishResponse response) {
                if (response == null || response.getResult() == null) {
                    if (mCallback!=null){
                        mCallback.onFail("网络出错");
                    }
                    return;
                }

                if (response.getResult().getCode() == 0){
                    if (mCallback!=null){
                        mCallback.onSuccess();
                    }
                    mVerifyDialog.dismiss();

                }else {
                    if (mCallback!=null){
                        mCallback.onFail(response.getResult().getMes());
                    }
                }
            }

            @Override public void onFailure(RepositoryException repository) {
                if (mCallback!=null){
                    mCallback.onFail("网络出错");
                }
            }
        });
    }

    public void verifyPassword(String action,String serverId,String master_key, String tableName,String pwd,String remark){
        TrainingSignRequest request = new TrainingSignRequest();
        request.setUserId(CoreZygote.getLoginUserServices().getUserId());
        request.setType("14");
        request.setAction(action);
        request.setPassword(pwd);
        request.setRemark(remark);
        request.setServerId(serverId);
        request.setMaster_key(master_key);
        request.setTableName(tableName);
        FEHttpClient.getInstance().post(request, new ResponseCallback<TrainFinishResponse>() {
            @Override public void onCompleted(TrainFinishResponse response) {
                if (response == null || response.getResult() == null) {
                    if (mCallback!=null){
                        mCallback.onFail("网络出错");
                    }
                    return;
                }

                if (response.getResult().getCode() == 0){
                    if (mCallback!=null){
                        mCallback.onSuccess();
                    }
                    mVerifyDialog.dismiss();

                }else {
                    if (mCallback!=null){
                        mCallback.onFail(response.getResult().getMes());
                    }
                }
            }

            @Override public void onFailure(RepositoryException repository) {
                if (mCallback!=null){
                    mCallback.onFail("网络出错");
                }
            }
        });
    }

    /**
     * 电子签名验证监听
     */
    public interface ElcVerifyCallback{

        void onSuccess();

        void onFail(String msg);
    }
}

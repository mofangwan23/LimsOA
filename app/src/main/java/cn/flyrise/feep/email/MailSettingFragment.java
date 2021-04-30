package cn.flyrise.feep.email;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.NewMailserverRequest;
import cn.flyrise.android.protocol.entity.NewMailserverResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * @author ZYP
 * @since 2016/7/27 10:15
 */
public class MailSettingFragment extends DialogFragment {

    private List<String> mMailLists;

    private TextView mTvUserName;
    private ListView mListView;

    public void setMailLists(List<String> mailLists) {
        this.mMailLists = mailLists;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.email_fragment_setting, container, false);
        mTvUserName = (TextView) view.findViewById(R.id.tvUserName);
        mListView = (ListView) view.findViewById(R.id.listView);
        setUpDataAndListener();
        return view;
    }

    private void setUpDataAndListener() {
        String userName = CoreZygote.getLoginUserServices().getUserName();
        mTvUserName.setText(userName);

        if (mMailLists.size() > 1) {
            final List<String> strings = mMailLists.subList(1, mMailLists.size());
            ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, strings);
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String account = strings.get(position);
//                    askForRefreshMails(account);
                    updateAndDismiss(account);
                }
            });
        }

        mTvUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnMailAccountChangeEvent event = new OnMailAccountChangeEvent();
                event.newAccount = mTvUserName.getText().toString();
                EventBus.getDefault().post(event);
                dismiss();
            }
        });
    }

    private void askForRefreshMails(final String account) {
        new FEMaterialDialog.Builder(getActivity())
                .setTitle(null)
                .setMessage(getResources().getString(R.string.lbl_message_will_spend_most_time))
                .setPositiveButton(null, new FEMaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(AlertDialog dialog) {
                        refreshMails(account);
                    }
                })
                .setNegativeButton(null, new FEMaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(AlertDialog dialog) {
                        updateAndDismiss(account);
                    }
                })
                .build()
                .show();
    }

    private void refreshMails(final String account) {
        LoadingHint.show(getActivity());
        NewMailserverRequest request = new NewMailserverRequest(account);
        FEHttpClient.getInstance().post(request, new ResponseCallback<NewMailserverResponse>(getActivity()) {
            @Override public void onCompleted(NewMailserverResponse newMailserverResponse) {
                LoadingHint.hide();
                FEToast.showMessage(newMailserverResponse.getErrorMessage());
                updateAndDismiss(account);
            }

            @Override public void onFailure(RepositoryException repositoryException) {
                FEToast.showMessage(getResources().getString(R.string.lbl_text_mail_server_not_open));
                updateAndDismiss(account);
            }
        });
    }


    private void updateAndDismiss(String account) {
        OnMailAccountChangeEvent event = new OnMailAccountChangeEvent();
        event.newAccount = account;
        EventBus.getDefault().post(event);
        dismiss();
    }

    public class OnMailAccountChangeEvent {
        public String newAccount;
    }

}

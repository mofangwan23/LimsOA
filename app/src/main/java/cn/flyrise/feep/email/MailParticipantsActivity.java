package cn.flyrise.feep.email;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.SelectedPerson;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.email.adapter.ParticipantsAdapter;
import cn.flyrise.feep.email.adapter.ParticipantsAdapter.Participants;
import cn.flyrise.feep.core.base.views.FEToolbar;

public class MailParticipantsActivity extends BaseActivity {

    public static final String EXTRA_WITH_MAIL = "extra_with_mail";

    private ListView listView;
    private ParticipantsAdapter adapter;
    private ArrayList<Participants> participantslist; //接收人

    public static void startMailParticipantsActivity(Context context, String account,
                                                     String from, String fromid,
                                                     String tto, String ttoid,
                                                     String cc, String ccid, boolean withMail) {
        Intent intent = new Intent(context, MailParticipantsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(K.email.mail_account, account);
        bundle.putString(K.email.mail_account, from);
        bundle.putString(K.email.mail_account_id, fromid);
        bundle.putString(K.email.mail_tto, tto);
        bundle.putString(K.email.mail_cc, cc);
        bundle.putString(K.email.mail_tto_id, ttoid);
        bundle.putString(K.email.mail_cc_id, ccid);
        bundle.putBoolean(EXTRA_WITH_MAIL, withMail);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_participants);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(getResources().getString(R.string.lbl_message_title_mail_canyuren));
    }

    @Override public void bindData() {
        listView = (ListView) findViewById(R.id.listview);
        participantslist = new ArrayList<>();
        final Bundle bundle = this.getIntent().getExtras();
        String mailform = bundle.getString(K.email.mail_account);
        String mailformid = bundle.getString(K.email.mail_account_id);
        String tto = bundle.getString(K.email.mail_tto);
        String ttoid = bundle.getString(K.email.mail_tto_id);
        String mailnamecc = bundle.getString(K.email.mail_cc);
        String ccid = bundle.getString(K.email.mail_cc_id);

        boolean withMail = bundle.getBoolean(EXTRA_WITH_MAIL, false);
        participantslist.add(new Participants(getResources().getString(R.string.lbl_text_mail_sender_1), ""));

        if (TextUtils.isEmpty(mailform)) {
            mailform = CoreZygote.getLoginUserServices().getServerAddress();
        }

        participantslist.add(new Participants(mailform, mailformid));

        if (withMail) {
            if (!TextUtils.isEmpty(tto)) {
                participantslist.add(new Participants(getResources().getString(R.string.lbl_text_mail_receiver), ""));
                processParticipants(tto, participantslist);
            }

            if (!TextUtils.isEmpty(mailnamecc)) {
                participantslist.add(new Participants(getResources().getString(R.string.lbl_text_mail_copy_to), ""));
                processParticipants(mailnamecc, participantslist);
            }
        }
        else {
            if (!TextUtils.isEmpty(tto) && !TextUtils.isEmpty(ttoid)) {
                participantslist.add(new Participants(getResources().getString(R.string.lbl_text_mail_receiver), ""));
                processParticipants(ttoid, tto, participantslist);
            }
            if (!TextUtils.isEmpty(tto) && !TextUtils.isEmpty(ccid)) {
                participantslist.add(new Participants(getResources().getString(R.string.lbl_text_mail_copy_to), ""));
                processParticipants(ccid, mailnamecc, participantslist);
            }
        }

        adapter = new ParticipantsAdapter(participantslist, new ParticipantsAdapter.XOnClickListener() {
            @Override
            public void onEmailIconClick(Participants participants) {
                NewAndReplyMailActivity.startNewReplyActivity(MailParticipantsActivity.this,
                        bundle.getString(K.email.mail_account),
                        new SelectedPerson(participants.id, participants.name, ""));
            }

            @Override
            public void onNameClick(Participants participants) {
            }
        });
        listView.setAdapter(adapter);
    }

    private void processParticipants(String ids, String names, List<Participants> participants) {
        String[] to = names.split(",");
        String[] toid = ids.split(",");
        for (int i = 0; i < to.length; i++) {
            participantslist.add(new Participants(to[i], toid[i]));
        }
    }

    private void processParticipants(String emails, List<Participants> participantses) {
        String[] strs = emails.split("&gt;;");
        for (String str : strs) {
            String[] ems = str.split("&lt;");
            participantses.add(new Participants(ems[0], ems[1]));
        }
    }
}

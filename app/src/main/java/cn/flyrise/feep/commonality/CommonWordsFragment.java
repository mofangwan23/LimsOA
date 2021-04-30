package cn.flyrise.feep.commonality;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016-08-25 13:37
 */
public class CommonWordsFragment extends DialogFragment {

    private String mCommonWordId;
    private String mCommonWord;

    public static CommonWordsFragment newInstance(String commonWord) {
        CommonWordsFragment fragment = new CommonWordsFragment();
        fragment.setCommonWord(commonWord);
        if (commonWord != null && commonWord.contains("<>")) {
            String[] split = commonWord.split("<>");
            fragment.setCommonWordId(split[0]);
            fragment.setCommonWord(split[1]);
        }
        fragment.setCancelable(false);
        return fragment;
    }

    public void setCommonWordId(String commonWordId) {
        this.mCommonWordId = commonWordId;
    }

    public void setCommonWord(String commonWord) {
        this.mCommonWord = commonWord;
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.view_common_word_dialog, container, false);
        initViewsAndListener(view);
        return view;
    }

    private void initViewsAndListener(View view) {
        TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        final EditText etCommonWord = (EditText) view.findViewById(R.id.editText);

        tvTitle.setText(TextUtils.isEmpty(mCommonWord) ? getString(R.string.add_common_language) : getString(R.string.edit_common_language));
        if (etCommonWord != null) {
            etCommonWord.setText(mCommonWord);
            etCommonWord.setSelection(mCommonWord.length());
        }

        view.findViewById(R.id.tvConfirm).setOnClickListener(v -> {
            String newCommonWord = etCommonWord.getText().toString().trim();
            if (TextUtils.isEmpty(newCommonWord)) {
                FEToast.showMessage(getResources().getString(R.string.lbl_text_common_not_empty));
                dismiss();
                return;
            }

            if (TextUtils.equals(newCommonWord, mCommonWord)) {
                dismiss();
                return;
            }

            if (newCommonWord.length() > 120) {
                FEToast.showMessage(getResources().getString(R.string.lbl_text_common_length));
                return;
            }

            if (getActivity() instanceof CommonWordsActivity) {
                if (TextUtils.isEmpty(mCommonWord)) {
                    ((CommonWordsActivity) getActivity()).addCommonWord(newCommonWord);
                }
                else {
                    ((CommonWordsActivity) getActivity()).updateCommonWord(newCommonWord, mCommonWordId);
                }
                dismiss();
            }
        });

        view.findViewById(R.id.tvCancel).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dismiss();
            }
        });
    }
}

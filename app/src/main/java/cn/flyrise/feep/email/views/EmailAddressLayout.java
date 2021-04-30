package cn.flyrise.feep.email.views;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.email.adapter.RecipientAdapter;

import static cn.flyrise.feep.core.common.utils.CommonUtil.isEmptyList;

/**
 * @author ZYP
 * @since 2016/7/15 11:54
 */
public class EmailAddressLayout extends LinearLayout {

    private boolean isShowAddBtn = true;
    private boolean isEditTextLoseFocus = false;
    private boolean isInnerBox;

    private TagEditText mTagEditText;

    private ImageView mImageView;
    private String mEditTextHint;
    private Drawable mRightDrawable;

    private List<AddressBook> mRecipients;
    private List<String> mErrorTags;
    private List<String> mEmails;

    private View mPreview;
    private TextView mTvPreviewLabel;
    private TextView mTvPreviewResult;
    private String mPreviewLabel;

    private static Map<String, String> sAddressBooks;

    private Point mPoint;

    private static void buildAddressBook(List<AddressBook> users) {
        if (users == null) {
            return;
        }

        sAddressBooks = new HashMap<>(users.size());
        for (AddressBook user : users) {
            sAddressBooks.put(user.name, user.userId);
        }
    }

    public EmailAddressLayout(Context context) {
        this(context, null);
    }

    public EmailAddressLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmailAddressLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.email_address_layout, this);
        mErrorTags = new ArrayList<>();
        mEmails = new ArrayList<>();
        mRecipients = new ArrayList<>();

        if (sAddressBooks == null) {
            List<AddressBook> addressBooks = AddressBookRepository.get().queryAllAddressBooks();
            if (addressBooks == null) {
                return;
            }
            buildAddressBook(addressBooks);
        }

        mTagEditText = (TagEditText) findViewById(R.id.tagEditText);
        mImageView = (ImageView) findViewById(R.id.ivAdd);


        mPreview = findViewById(R.id.llPreview);
        mTvPreviewLabel = (TextView) findViewById(R.id.tvLabel);
        mTvPreviewResult = (TextView) findViewById(R.id.tvResult);

        // 拿属性。
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagEditText, defStyleAttr, 0);
        typedArray.getIndexCount();
        for (int i = 0, len = typedArray.getIndexCount(); i < len; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.TagEditText_addressHint:
                    mEditTextHint = typedArray.getString(attr);
                    break;
                case R.styleable.TagEditText_rightIcon:
                    mRightDrawable = typedArray.getDrawable(attr);
                    break;
                case R.styleable.TagEditText_previewLabel:
                    mPreviewLabel = typedArray.getString(attr);
                    break;
            }
        }

        typedArray.recycle();
        init();
    }

    private void init() {
//        mTagEditText.setEditTextHint(mEditTextHint);
        mImageView.setImageDrawable(mRightDrawable);
        mTvPreviewLabel.setText(mPreviewLabel);

        final AutoCompleteTextView editText = mTagEditText.getEditText();

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mImageView.setVisibility((hasFocus && isShowAddBtn) ? View.VISIBLE : View.INVISIBLE);
                if (!hasFocus) {
                    String text = mTagEditText.getEditText().getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        editText.setText("");
                        preformInputCheck(text);
                    }
                    showPreview();
                }
            }
        });

        mTagEditText.setOnTagRemoveListener(new TagEditText.OnTagRemoveListener() {
            @Override
            public void onTagRemove(String tag, boolean isErrorTag) {
                if (isErrorTag) {
                    mErrorTags.remove(tag);
                    return;
                }

                if (isEmptyList(mRecipients)) {
                    return;
                }

                if (tag.contains("@")) {
                    mEmails.remove(tag);
                    return;
                }

                int index = -1;
                for (int i = 0, n = mRecipients.size(); i < n; i++) {
                    AddressBook person = mRecipients.get(i);
                    if (TextUtils.equals(person.name, tag)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    mRecipients.remove(index);
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String tag = v.getText().toString();
                    v.setText("");
                    preformInputCheck(tag);
                }
                return false;
            }
        });

        mPreview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePreview();
            }
        });

        editText.setAdapter(new RecipientAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>(sAddressBooks.keySet())));
        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userName = ((TextView) view).getText().toString();
                String userId = sAddressBooks.get(userName);
                AddressBook person = new AddressBook();
                person.userId = userId;
                person.name = userName;

                if (mRecipients.contains(person)) {
                    editText.setText("");
                    return;
                }
                mRecipients.add(person);
                mTagEditText.addTagView(person.name);
                editText.setText("");
            }
        });
    }

    private void preformInputCheck(String text) {
        if (validateUserInput(text)) {
            mTagEditText.addTagView(text);
            return;
        }

        boolean isContainsKey = sAddressBooks.containsKey(text);
        if (isContainsKey) {
            String ids = sAddressBooks.get(text);
            AddressBook person = new AddressBook();
            person.userId = ids;
            person.name = text;
            if (!mRecipients.contains(person)) {
                mRecipients.add(person);
                mTagEditText.addTagView(text);
            }
        }
        else {
            if (!mErrorTags.contains(text)) {
                mErrorTags.add(text);
                mTagEditText.addTagView(text, true);
            }
        }
    }

    public void initEmailAddress(Activity activity, boolean isShowAddBtn, String mailAccount) {
        mPoint = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(mPoint);
        mTagEditText.getEditText().setDropDownWidth(mPoint.x);
        mTagEditText.getEditText().setDropDownVerticalOffset(PixelUtil.dipToPx(5));

        this.isShowAddBtn = isShowAddBtn;
        mImageView.setVisibility(isShowAddBtn ? View.VISIBLE : View.INVISIBLE);

        if (mailAccount == null) {
            isInnerBox = true;
            return;
        }

        isInnerBox = !mailAccount.contains("@");
    }


    private boolean validateUserInput(String input) {
        if (input.contains("@") && !isInnerBox) {
            if (isEmail(input) && !mEmails.contains(input)) {
                mEmails.add(input);
                return true;
            }
        }
        return false;
    }

    private boolean isEmail(String input) {
        String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        return Pattern.compile(check).matcher(input).matches();
    }

    public void addPerson(AddressBook person) {
        String id = person.userId;
        if (id.contains("@")) {
            mEmails.add(id);
            mTagEditText.addTagView(id);
        }
        else {
            mRecipients.add(person);
            setRecipients(mRecipients);
        }
    }

    public void setRecipients(List<AddressBook> recipients) {
        setRecipients(recipients, true);
    }

    public void setRecipients(List<AddressBook> recipients, boolean isShowPreview) {
        if (isEmptyList(recipients)) {
            mRecipients.clear();
        }
        else {
            mRecipients = recipients;
        }

        mTagEditText.removeAllTagViews();
        for (AddressBook recipient : mRecipients) {
            mTagEditText.addTagView(recipient.name);
        }

        for (String email : mEmails) {
            mTagEditText.addTagView(email);
        }

        for (String tag : mErrorTags) {
            mTagEditText.addTagView(tag, true);
        }

        if (isShowPreview) showPreview();
    }

    public void setRightButtonClickListener(OnClickListener listener) {
        mImageView.setOnClickListener(listener);
        if (isEditTextLoseFocus) {
            mTagEditText.getEditText().setOnClickListener(listener);
        }
    }

    public boolean isEmptyTag() {
        return isEmptyList(mRecipients) && isEmptyList(mErrorTags) && isEmptyList(mEmails);
    }

    public boolean isEmptyReceiver() {
        return isEmptyList(mRecipients) && isEmptyList(mEmails);
    }

    public boolean hasErrorTag() {
        return mErrorTags.size() > 0;
    }

    public String getRecipientIds() {
        if (isEmptyList(mRecipients)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (int n = mRecipients.size(); i < n - 1; i++) {
            sb.append(mRecipients.get(i).userId).append(",");
        }
        sb.append(mRecipients.get(i).userId);
        return sb.toString();
    }

    public List<AddressBook> getRecipients() {
        return mRecipients;
    }

    public String getEmailAddress() {
        if (isEmptyList(mEmails)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (int n = mEmails.size(); i < n - 1; i++) {
            sb.append(mEmails.get(i)).append(",");
        }
        sb.append(mEmails.get(i));
        return sb.toString();
    }

    public ImageView getRightImageView() {
        return this.mImageView;
    }

    public EditText getEditText() {
        return this.mTagEditText.getEditText();
    }

    public void showPreview() {
        if (isEmptyTag()) {
            return;
        }

        int totalSize = getTotalSize();
        if (totalSize >= 2) {
            mTagEditText.setVisibility(View.GONE);
            mImageView.setVisibility(View.INVISIBLE);
            mPreview.setVisibility(View.VISIBLE);

            StringBuilder sb = new StringBuilder();

            if (mRecipients != null) {
                for (AddressBook person : mRecipients) {
                    sb.append(person.name).append(",");
                }
            }

            if (mEmails != null) {
                for (String email : mEmails) {
                    sb.append(email).append(",");
                }
            }

            if (mErrorTags != null) {
                for (String error : mErrorTags) {
                    sb.append(error).append(",");
                }
            }

            String str = sb.substring(0, sb.length() - 1);
            str += "等" + totalSize + "人";
            mTvPreviewResult.setText(str);
        }
    }

    private int getTotalSize() {
        int size = 0;
        if (mRecipients != null) {
            size += mRecipients.size();
        }

        if (mEmails != null) {
            size += mEmails.size();
        }

        if (mErrorTags != null) {
            size += mErrorTags.size();
        }
        return size;
    }

    public void hidePreview() {
        mTagEditText.setVisibility(View.VISIBLE);
        mPreview.setVisibility(View.GONE);
        mImageView.setVisibility(isShowAddBtn ? View.VISIBLE : View.INVISIBLE);
        if (isEditTextLoseFocus) {
            mTagEditText.getEditText().setFocusable(false);
            mTagEditText.getEditText().setClickable(true);
        }
    }


}

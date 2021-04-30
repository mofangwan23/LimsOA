package cn.flyrise.feep.form.contract;

import android.content.Intent;

public interface FormInputContract {

	interface View{

		void setToolarTitle(int requestType);

		boolean isWritting();

		boolean isWait();

		boolean isTrace();

		boolean isReturnCurrentNode();

		void setIdeaEditText(String text);

		String getIdeaEditText();

		void saveWrittingBitmap(String path,String bitmapName);

		void setAttachmentTitle(String text);

        void showLoading();

        void hideLoading();
	}

	interface Presenter{

		void getIntentData();

		void selectedAttachment();

		void sendElectSignature();

		void submit();

		void wordsDialog();

		String getCollaborationID();

		int getRequstType();

		boolean isAddSign();

		boolean isSendDo();

		boolean isReturn();

		void onActivityResult(int requestCode, int resultCode, Intent data);

	}
}

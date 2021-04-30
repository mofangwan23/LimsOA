package cn.flyrise.feep.collaboration.view;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import android.view.inputmethod.InputContentInfo;
import jp.wasabeef.richeditor.RichEditor;

/**
 * Created by yuepeng on 2017/5/9.
 */
public class TapInputConnection implements InputConnection {

    private RichEditor mRichEditor;
    private InputConnection mConnection;

    public TapInputConnection(InputConnection conn, RichEditor richEditor) {
        this.mConnection = conn;
        this.mRichEditor = richEditor;
    }

    @Override
    public CharSequence getTextBeforeCursor(int n, int flags) {
        return mConnection.getTextBeforeCursor(n, flags);
    }

    @Override
    public CharSequence getTextAfterCursor(int n, int flags) {
        return mConnection.getTextAfterCursor(n, flags);
    }

    @Override
    public CharSequence getSelectedText(int flags) {
        return mConnection.getSelectedText(flags);
    }

    @Override
    public int getCursorCapsMode(int reqModes) {
        return mConnection.getCursorCapsMode(reqModes);
    }

    @Override
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        return mConnection.getExtractedText(request, flags);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        return mConnection.deleteSurroundingText(beforeLength, afterLength);
    }

    @Override public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
        return false;
    }

    @Override
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        return mConnection.setComposingText(text, newCursorPosition);
    }

    @Override
    public boolean setComposingRegion(int start, int end) {
        return mConnection.setComposingRegion(start, end);
    }

    @Override
    public boolean finishComposingText() {
        return mConnection.finishComposingText();
    }

    @Override
    public boolean commitText(CharSequence text, int newCursorPosition) {
        return mConnection.commitText(text, newCursorPosition);
    }

    @Override
    public boolean commitCompletion(CompletionInfo text) {
        return mConnection.commitCompletion(text);
    }

    @Override
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        return mConnection.commitCorrection(correctionInfo);
    }

    @Override
    public boolean setSelection(int start, int end) {
        return mConnection.setSelection(start, end);
    }

    @Override
    public boolean performEditorAction(int editorAction) {
        return mConnection.performEditorAction(editorAction);
    }

    @Override
    public boolean performContextMenuAction(int id) {
        return mConnection.performContextMenuAction(id);
    }

    @Override
    public boolean beginBatchEdit() {
        return mConnection.beginBatchEdit();
    }

    @Override
    public boolean endBatchEdit() {
        return mConnection.endBatchEdit();
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                mRichEditor.delete();
            }
            return true;
        }
        return mConnection.sendKeyEvent(event);
    }

    @Override
    public boolean clearMetaKeyStates(int states) {
        return false;
    }

    @Override
    public boolean reportFullscreenMode(boolean enabled) {
        return mConnection.reportFullscreenMode(enabled);
    }

    @Override
    public boolean performPrivateCommand(String action, Bundle data) {
        return mConnection.performPrivateCommand(action, data);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean requestCursorUpdates(int cursorUpdateMode) {
        return mConnection.requestCursorUpdates(cursorUpdateMode);
    }

    @Override public Handler getHandler() {
        return null;
    }

    @Override public void closeConnection() {
    }

    @Override public boolean commitContent(@NonNull InputContentInfo inputContentInfo, int i, @Nullable Bundle bundle) {
        return false;
    }
}

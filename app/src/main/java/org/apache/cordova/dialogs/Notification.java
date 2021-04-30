/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.dialogs;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;


/**
 * This class provides access to notifications on the device.
 * <p/>
 * Be aware that this implementation gets called on
 * navigator.notification.{alert|confirm|prompt}, and that there is a separate
 * implementation in org.apache.cordova.CordovaChromeClient that gets
 * called on a simple window.{alert|confirm|prompt}.
 */
public class Notification extends CordovaPlugin {

    public int confirmResult = -1;
    public ProgressDialog spinnerDialog = null;
    public ProgressDialog progressDialog = null;

    /**
     * Constructor.
     */
    public Notification() {
    }

    /**
     * Executes the request and returns PluginResult.
     * @param action          The action to execute.
     * @param args            JSONArray of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        /*
         * Don't run any of these if the current activity is finishing
    	 * in order to avoid android.view.WindowManager$BadTokenException
    	 * crashing the app. Just return true here since false should only
    	 * be returned in the event of an invalid action.
    	 */
        if (this.cordova.getActivity().isFinishing()) return true;
        FELog.i("Notification Execute...");
        switch (action) {
            case "beep":
                this.beep(args.getLong(0));
                break;
            case "alert":
                this.alert(args.getString(0), args.getString(1), args.getString(2), callbackContext);
                return true;
            case "confirm":
                this.confirm(args.getString(0), args.getString(1), args.getJSONArray(2), callbackContext);
                return true;
            case "prompt":
                this.prompt(args.getString(0), args.getString(1), args.getJSONArray(2), args.getString(3), callbackContext);
                return true;
            case "activityStart":
                this.activityStart(args.getString(0), args.getString(1));
                break;
            case "activityStop":
                this.activityStop();
                break;
            case "progressStart":
                this.progressStart(args.getString(0), args.getString(1));
                break;
            case "progressValue":
                this.progressValue(args.getInt(0));
                break;
            case "progressStop":
                this.progressStop();
                break;
            default:
                return false;
        }
        callbackContext.success();
        return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Beep plays the default notification ringtone.
     * @param count Number of times to play notification
     */
    public void beep(final long count) {
        cordova.getThreadPool().execute(() -> {
            Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone notification = RingtoneManager.getRingtone(cordova.getActivity().getBaseContext(), ringtone);

            // If phone is not set to silent mode
            if (notification != null) {
                for (long i = 0; i < count; ++i) {
                    notification.play();
                    long timeout = 5000;
                    while (notification.isPlaying() && (timeout > 0)) {
                        timeout = timeout - 100;
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * Builds and shows a native Android alert with given Strings
     * @param message         The message the alert should display
     * @param title           The title of the alert
     * @param buttonLabel     The label of the button
     * @param callbackContext The callback context
     */
    public synchronized void alert(final String message, final String title, final String buttonLabel, final CallbackContext callbackContext) {
        final CordovaInterface cordova = this.cordova;

        FELog.i("Notification alert : " + message);
        Runnable runnable = () -> new FEMaterialDialog.Builder(cordova.getActivity())
                .setTitle(null)
                .setMessage(message)
                .setPositiveButton(null, dialog -> callbackContext.success())
                .build()
                .show();
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    /**
     * Builds and shows a native Android confirm dialog with given title, message, buttons.
     * This dialog only shows up to 3 buttons.  Any labels after that will be ignored.
     * The index of the button pressed will be returned to the JavaScript callback identified by callbackId.
     * @param message         The message the dialog should display
     * @param title           The title of the dialog
     * @param buttonLabels    A comma separated list of button labels (Up to 3 buttons)
     * @param callbackContext The callback context.
     */
    public synchronized void confirm(final String message, final String title, final JSONArray buttonLabels, final CallbackContext callbackContext) {
        final CordovaInterface cordova = this.cordova;
        FELog.i("Notification confirm : " + message);
        Runnable runnable = () -> new FEMaterialDialog.Builder(cordova.getActivity())
                .setTitle(null)
                .setMessage(message)
                .setPositiveButton(null, dialog -> callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 1)))
                .setNegativeButton(null, dialog -> callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, 3)))
                .build()
                .show();
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    /**
     * Builds and shows a native Android prompt dialog with given title, message, buttons.
     * This dialog only shows up to 3 buttons.  Any labels after that will be ignored.
     * The following results are returned to the JavaScript callback identified by callbackId:
     * buttonIndex			Index number of the button selected
     * input1				The text entered in the prompt dialog box
     * @param message         The message the dialog should display
     * @param title           The title of the dialog
     * @param buttonLabels    A comma separated list of button labels (Up to 3 buttons)
     * @param callbackContext The callback context.
     */
    public synchronized void prompt(final String message, final String title, final JSONArray buttonLabels, final String defaultText, final CallbackContext callbackContext) {

        final CordovaInterface cordova = this.cordova;

        FELog.i("Notification prompt : " + message);
        Runnable runnable = () -> {
            final EditText promptInput = new EditText(cordova.getActivity());
            promptInput.setHint(defaultText);
            Builder dlg = createDialog(cordova); // new AlertDialog.Builder(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            dlg.setMessage(message);
            dlg.setTitle(title);
            dlg.setCancelable(true);

            dlg.setView(promptInput);

            final JSONObject result = new JSONObject();

            // First button
            if (buttonLabels.length() > 0) {
                try {
                    dlg.setNegativeButton(buttonLabels.getString(0),
                            (dialog, which) -> {
                                dialog.dismiss();
                                try {
                                    result.put("buttonIndex", 1);
                                    result.put("input1", promptInput.getText().toString().trim().length() == 0 ? defaultText : promptInput.getText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                            });
                } catch (JSONException ignored) {
                }
            }

            // Second button
            if (buttonLabels.length() > 1) {
                try {
                    dlg.setNeutralButton(buttonLabels.getString(1),
                            (dialog, which) -> {
                                dialog.dismiss();
                                try {
                                    result.put("buttonIndex", 2);
                                    result.put("input1", promptInput.getText().toString().trim().length() == 0 ? defaultText : promptInput.getText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                            });
                } catch (JSONException ignored) {
                }
            }

            // Third button
            if (buttonLabels.length() > 2) {
                try {
                    dlg.setPositiveButton(buttonLabels.getString(2),
                            (dialog, which) -> {
                                dialog.dismiss();
                                try {
                                    result.put("buttonIndex", 3);
                                    result.put("input1", promptInput.getText().toString().trim().length() == 0 ? defaultText : promptInput.getText());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                            });
                } catch (JSONException ignored) {
                }
            }
            dlg.setOnCancelListener(dialog -> {
                dialog.dismiss();
                try {
                    result.put("buttonIndex", 0);
                    result.put("input1", promptInput.getText().toString().trim().length() == 0 ? defaultText : promptInput.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
            });

            changeTextDirection(dlg);
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    /**
     * Show the spinner.
     * @param title   Title of the dialog
     * @param message The message of the dialog
     */
    public synchronized void activityStart(final String title, final String message) {
        if (this.spinnerDialog != null) {
            this.spinnerDialog.dismiss();
            this.spinnerDialog = null;
        }
        final Notification notification = this;
        final CordovaInterface cordova = this.cordova;
        Runnable runnable = new Runnable() {
            public void run() {
                notification.spinnerDialog = createProgressDialog(cordova); // new ProgressDialog(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                notification.spinnerDialog.setTitle(title);
                notification.spinnerDialog.setMessage(message);
                notification.spinnerDialog.setCancelable(true);
                notification.spinnerDialog.setIndeterminate(true);
                notification.spinnerDialog.setOnCancelListener(
                        dialog -> notification.spinnerDialog = null);
                notification.spinnerDialog.show();
            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    /**
     * Stop spinner.
     */
    public synchronized void activityStop() {
        if (this.spinnerDialog != null) {
            this.spinnerDialog.dismiss();
            this.spinnerDialog = null;
        }
    }

    /**
     * Show the progress dialog.
     * @param title   Title of the dialog
     * @param message The message of the dialog
     */
    public synchronized void progressStart(final String title, final String message) {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
            this.progressDialog = null;
        }
        final Notification notification = this;
        final CordovaInterface cordova = this.cordova;
        Runnable runnable = () -> {
            notification.progressDialog = createProgressDialog(cordova); // new ProgressDialog(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            notification.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            notification.progressDialog.setTitle(title);
            notification.progressDialog.setMessage(message);
            notification.progressDialog.setCancelable(true);
            notification.progressDialog.setMax(100);
            notification.progressDialog.setProgress(0);
            notification.progressDialog.setOnCancelListener(
                    dialog -> notification.progressDialog = null);
            notification.progressDialog.show();
        };
        this.cordova.getActivity().runOnUiThread(runnable);
    }

    /**
     * Set value of progress bar.
     * @param value 0-100
     */
    public synchronized void progressValue(int value) {
        if (this.progressDialog != null) {
            this.progressDialog.setProgress(value);
        }
    }

    /**
     * Stop progress dialog.
     */
    public synchronized void progressStop() {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
            this.progressDialog = null;
        }
    }

    private AlertDialog.Builder createDialog(CordovaInterface cordova) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            return new AlertDialog.Builder(cordova.getActivity());//, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        }
        else {
            return new AlertDialog.Builder(cordova.getActivity());
        }
    }

    private ProgressDialog createProgressDialog(CordovaInterface cordova) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return new ProgressDialog(cordova.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        }
        else {
            return new ProgressDialog(cordova.getActivity());
        }
    }

    private void changeTextDirection(Builder dlg) {
        dlg.create();
        AlertDialog dialog = dlg.show();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            TextView messageview = (TextView) dialog.findViewById(android.R.id.message);
            messageview.setTextDirection(android.view.View.TEXT_DIRECTION_LOCALE);
        }
    }
}

package cn.flyrise.feep.core.premission;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.core.R;


/**
 * @author ZYP
 * @since 2016-09-19 17:26
 * Android 6.0 运行时权限管理工具。
 */
public class FePermissions {

	private static final String TAG = "permission";

	public static Builder with(Object target) {
		return new Builder(target);
	}

	private static Activity getActivity(Object target) {
		if (target instanceof Activity) {
			return (Activity) target;
		}
		else if (target instanceof Fragment) {
			return ((Fragment) target).getActivity();
		}
		else if (target instanceof android.support.v4.app.Fragment) {
			return ((android.support.v4.app.Fragment) target).getActivity();
		}

		throw new IllegalArgumentException("The target must be an instance of Activity or Fragment.");
	}

	private static void request(final Builder builder) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			Log.i(TAG, "Current SDK version is lower than Android M, don't need for permission request.");
			executePermissionGranted(builder.target, builder.requestCode);
			return;
		}

		String[] deniedPermissions = filterDeniedPermission(getActivity(builder.target), builder.permissions);
		if (deniedPermissions == null) {
			Log.i(TAG, "The request permissions was all granted before, don't need request any more.");
			executePermissionGranted(builder.target, builder.requestCode);
			return;
		}

		boolean shouldShowRationale = false;
		for (String permission : deniedPermissions) {
			shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale(builder.target, permission);
		}

		if (shouldShowRationale && !TextUtils.isEmpty(builder.rationaleMessage)) {
			Log.i(TAG, "Show a dialog for request permission rationale, because user denied grant permission last time.");
			Context context = getActivity(builder.target);
			new PermissionSettingDialog.Builder(context)
					.setTitle(context.getResources().getString(R.string.permission_title_confirm))
					.setMessage(builder.rationaleMessage)
					.setCancelable(false)
					.setPositiveText(context.getResources().getString(R.string.permission_text_confirm))
					.setPositiveListener(v -> requestPermission(builder.target, deniedPermissions, builder.requestCode))
					.build()
					.show();
			return;
		}
		requestPermission(builder.target, deniedPermissions, builder.requestCode);
	}

	public static boolean isPermissionGranted(Context context, String[] permissions) {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || filterDeniedPermission(context, permissions) == null;
	}

	private static String[] filterDeniedPermission(Context context, String[] permissions) {
		List<String> permissionLists = new ArrayList<>();
		for (String permission : permissions) {
			if (checkSelfPermission(context, permission)) {
				continue;
			}
			permissionLists.add(permission);
		}

		if (permissionLists.size() == 0) {
			return null;
		}
		return permissionLists.toArray(new String[permissionLists.size()]);
	}

	public static boolean checkSelfPermission(Context context, String permissions) {
		return ContextCompat.checkSelfPermission(context, permissions) == PackageManager.PERMISSION_GRANTED;
	}

	private static boolean shouldShowRequestPermissionRationale(Object target, String permission) {
		if (target instanceof Activity) {
			return ActivityCompat.shouldShowRequestPermissionRationale((Activity) target, permission);
		}
		else if (target instanceof Fragment && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return ((Fragment) target).shouldShowRequestPermissionRationale(permission);
		}
		else if (target instanceof android.support.v4.app.Fragment) {
			return ((android.support.v4.app.Fragment) target).shouldShowRequestPermissionRationale(permission);
		}
		return false;
	}

	private static void requestPermission(Object target, String[] permissions, int requestCode) {
		if (target instanceof Activity) {
			ActivityCompat.requestPermissions((Activity) target, permissions, requestCode);
		}
		else if (target instanceof Fragment && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			((Fragment) target).requestPermissions(permissions, requestCode);
		}
		else if (target instanceof android.support.v4.app.Fragment) {
			((android.support.v4.app.Fragment) target).requestPermissions(permissions, requestCode);
		}
	}

	public static void onRequestPermissionsResult(Object target, int requestCode, String[] permissions, int[] grantResults) {
		onRequestPermissionsResult(target, requestCode, permissions, grantResults, null);
	}


	public static void onRequestPermissionsResult(Object target, int requestCode, String[] permissions,
			int[] grantResults, OnRequestPermissionDeniedListener deniedListener) {
		executePermissionGranted(target, huntPermissionGrantedResultMethod(target, requestCode), permissions, grantResults, deniedListener);
	}

	private static void executePermissionGranted(Object target, int requestCode) {
		executePermissionGranted(target, huntPermissionGrantedResultMethod(target, requestCode), null, null, null);
	}

	private static void executePermissionGranted(Object target, Method method, String[] permissions,
			int[] grantResults, OnRequestPermissionDeniedListener deniedListener) {
		if (method == null) {
			Log.e(TAG, "Could not find a method to process when permission granted.");
			return;
		}

		if (permissions == null && grantResults == null) {
			invokeMethod(method, target);
			return;
		}

		boolean isAllPermissionGranted = true;
		for (int grantResult : grantResults) {
			if (grantResult != PackageManager.PERMISSION_GRANTED) {
				isAllPermissionGranted = false;
				break;
			}
		}

		if (isAllPermissionGranted) {
			Log.i(TAG, "All permission request was granted.");
			invokeMethod(method, target);
			return;
		}

		final Context context = getActivity(target);
		String deniedMessage = parsePermissionGrantedResult(context, permissions, grantResults);

		if (deniedListener != null) {
			Log.i(TAG, "Use the listener to process the result when user denied permission grant.");
			int requestCode = method.getAnnotation(PermissionGranted.class).value();
			deniedListener.onRequestPermissionDenied(requestCode, permissions, grantResults, deniedMessage);
			return;
		}

		Log.i(TAG, "Some request permission was denied by user.");
		new PermissionSettingDialog.Builder(context)
				.setTitle(context.getResources().getString(R.string.permission_title))
				.setMessage(deniedMessage)
				.setPositiveText(context.getResources().getString(R.string.permission_text_i_know))
				.setNeutralText(context.getResources().getString(R.string.permission_text_go_setting))
				.setNeutralListener(v -> {
					Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					intent.setData(Uri.fromParts("package", context.getPackageName(), null));
					context.startActivity(intent);
				})
				.build()
				.show();
	}

	private static void invokeMethod(Method method, Object target) {
		try {
			method.invoke(target, new Object[]{});
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Execute permission granted callback method failed : " + e.getMessage());
		}
	}

	private static Method huntPermissionGrantedResultMethod(Object object, int requestCode) {
		Class<?> clazz = object.getClass();
		Method[] declaredMethods = clazz.getDeclaredMethods();
		Method targetMethod = findTargetMethod(declaredMethods, requestCode);

		if (targetMethod != null) {
			return targetMethod;
		}

		declaredMethods = object.getClass().getMethods();
		return findTargetMethod(declaredMethods, requestCode);                  // 再查找一次，如果没有就真的没有了
	}

	private static Method findTargetMethod(Method[] declaredMethods, int requestCode) {
		for (Method method : declaredMethods) {
			int modifier = method.getModifiers();
			if (!Modifier.isPublic(modifier)) {                                 // 不是 public 的，不要
				continue;
			}

			if (Modifier.isStatic(modifier)) {                                  // 居然是 static 的，不要
				continue;
			}

			if (Modifier.isFinal(modifier)) {                                   // final 的，不要
				continue;
			}

			if (Modifier.isVolatile(modifier)) {                                // 含有 volatile 修饰符的，不要
				continue;
			}

			if (method.getParameterTypes().length != 0) {                       // 居然还有方法参数，特么鬼知道破参数是啥啊，不要
				continue;
			}

			PermissionGranted permissionGrantedAnnotation = method.getAnnotation(PermissionGranted.class);
			if (permissionGrantedAnnotation == null) {                          // 没有 PermissionGranted 注解，玩个毛线，不要
				continue;
			}

			if (permissionGrantedAnnotation.value() != requestCode) {           // requestCode 不匹配的，不要
				continue;
			}

			return method;
		}
		Log.e(TAG, "Hunt permission granted callback method failed !");
		return null;
	}

	private static String parsePermissionGrantedResult(Context context, String[] permissions, int[] grantResults) {
		if (grantResults.length == 1) {
			return PermissionCode.getPromptMessage(context, permissions[0]);
		}

		List<String> deniedPermissions = new ArrayList<>();
		for (int i = 0; i < grantResults.length; i++) {
			if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
				continue;
			}
			deniedPermissions.add(permissions[i]);
		}

		if (deniedPermissions.size() == 1) {
			return PermissionCode.getPromptMessage(context, deniedPermissions.get(0));
		}

		return PermissionCode.getPromptMessage(context, "NULL");
	}

	public static class Builder {

		private Object target;
		private String[] permissions;
		private String rationaleMessage;
		private int requestCode;

		private Builder(@NonNull Object target) {
			this.target = target;
		}

		public Builder permissions(@NonNull String[] permissions) {
			this.permissions = permissions;
			return this;
		}

		public Builder rationaleMessage(String rationaleMessage) {
			this.rationaleMessage = rationaleMessage;
			return this;
		}

		public Builder requestCode(@NonNull int requestCode) {
			this.requestCode = requestCode;
			return this;
		}

		public void request() {
			FePermissions.request(this);
		}
	}

	public interface OnRequestPermissionDeniedListener {

		void onRequestPermissionDenied(int requestCode, String[] permissions, int[] grantResults, String deniedMessage);
	}
}

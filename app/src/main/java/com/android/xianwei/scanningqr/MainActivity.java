package com.android.xianwei.scanningqr;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.xianwei.scanningqr.scanningQR.CaptureActivity;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;
import com.yanzhenjie.permission.SettingService;

import java.util.List;

public class MainActivity extends AppCompatActivity {

	//权限
	public Rationale mRationale;
	public PermissionSetting mSetting;

	private final int SCANNING_QR = 2033;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Permission
		if (null == mRationale)
			mRationale = new DefaultRationale();
		if (null == mSetting)
			mSetting = new PermissionSetting(this);

		TextView tv = findViewById(R.id.tv);
		tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AndPermission.with(MainActivity.this)
						.permission(Permission.Group.CAMERA)
						.rationale(mRationale)
						.onGranted(new Action() {
							@Override
							public void onAction(List<String> permissions) {
								startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class).putExtra("from", "MainActivity"), SCANNING_QR);
							}
						})
						.onDenied(new Action() {
							@Override
							public void onAction(@NonNull List<String> permissions) {
								if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
									mSetting.showSetting(permissions);
								}
							}
						})
						.start();
			}
		});


	}

	class DefaultRationale implements Rationale {
		@Override
		public void showRationale(Context context, List<String> permissions, final RequestExecutor executor) {
			List<String> permissionNames = Permission.transformText(context, permissions);
			String message = context.getString(R.string.message_permission_rationale, TextUtils.join("\n", permissionNames));

			AlertDialog.newBuilder(context)
					.setCancelable(false)
					.setTitle("提示")
					.setMessage(message)
					.setPositiveButton("继续", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							System.out.println("再次询问-权限申请成功");
							//这个代表的是对话框的弹出 而不是代表权限申请成功
							executor.execute();
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							System.out.println("再次询问-权限申请失败");
							executor.cancel();
						}
					})
					.show();
		}
	}

	public class PermissionSetting {

		private final Context mContext;

		public PermissionSetting(Context context) {
			this.mContext = context;
		}

		public void showSetting(final List<String> permissions) {
			List<String> permissionNames = Permission.transformText(mContext, permissions);
			String message = mContext.getString(R.string.message_permission_always_failed, TextUtils.join("\n", permissionNames));

			final SettingService settingService = AndPermission.permissionSetting(mContext);
			AlertDialog.newBuilder(mContext)
					.setCancelable(false)
					.setTitle("提示")
					.setMessage(message)
					.setPositiveButton("设置", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							System.out.println("总是询问-权限申请成功");
							settingService.execute();
						}
					})
					.setNegativeButton("不", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							System.out.println("总是询问-权限申请失败");
							settingService.cancel();
						}
					})
					.show();
		}


	}
}

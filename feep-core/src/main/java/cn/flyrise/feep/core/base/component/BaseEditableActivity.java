package cn.flyrise.feep.core.base.component;

import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;

/**
 * @author ZYP
 * @since 2017-11-15 16:36
 */
public class BaseEditableActivity extends BaseActivity {

	public void showExitDialog() {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getString(R.string.exit_edit_tig))
				.setPositiveButton(null, dialog -> finish())
				.setNegativeButton(null, null)
				.build()
				.show();
	}
}

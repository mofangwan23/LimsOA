package cn.flyrise.feep.core.function;

import android.app.Activity;

public interface IPreDefinedModuleRepository {

	int getV7Icon(int moduleId);

	PreDefinedShortCut getShortCut(int quickId);

	Class<? extends Activity> getModuleClass(int moduleId);

}

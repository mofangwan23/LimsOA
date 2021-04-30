package cn.flyrise.feep.workplan7.contract;

import cn.flyrise.android.protocol.model.User;
import cn.flyrise.feep.core.base.component.FEListContract;
import java.util.List;

/**
 * @author klc
 * @version 1.0 <br
 */
public interface WorkPlanSearchListContract {

	interface Presenter extends FEListContract.Presenter {

		void searchData(String userID);

		List<User> getSubordinate();


	}
}

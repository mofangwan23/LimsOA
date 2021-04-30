package cn.flyrise.feep.schedule;

import android.content.Intent;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-11-28 14:44
 */
public interface NewScheduleContract {

	interface IView {

		void initNewSchedule(String title, String content, String startTime, String endTime);

		void configRepeatTime(List<String> repeatValues, String defaultValue);

		void configPromptTime(List<String> promptValues, String defaultValue);

		void saveScheduleSuccess();

		void saveScheduleFailed(String errorMessage);

		void setSelectedUsers(String selectedPersons);

		void showLoading();

		void hideLoading();

	}

	interface IPresenter {

		void start(Intent intent);

		void saveSchedule(String title, String content, String startTime, String endTime);

		void setSeletedUsers(List<AddressBook> seletedUsers);

		List<AddressBook> getSeletedUsers();

		String getSeletedUserNames();

		List<String> getPromptValues();

		List<String> getRepeatValues();

		void setPrompt(int position);

		void setRepeat(int position);

		boolean isEdit();

	}
}

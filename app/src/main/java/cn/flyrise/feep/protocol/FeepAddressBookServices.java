package cn.flyrise.feep.protocol;

import android.text.TextUtils;
import android.util.LruCache;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.IAddressBookServices;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-02-25 19:30
 */
public class FeepAddressBookServices implements IAddressBookServices {

	private static final int MAX_SIZE = 48;
	private final LruCache<String, AddressBook> mUserInfoCache = new LruCache<>(MAX_SIZE);
	private final ExecutorService executor = Executors.newFixedThreadPool(36);
	private final Scheduler scheduler = Schedulers.from(executor);

	@Override
	public AddressBook queryUserInfo(String userId) {
		if (TextUtils.isEmpty(userId)) {
			return null;
		}
		String actualUserId = getActualUserId(userId);

		AddressBook addressBook = mUserInfoCache.get(actualUserId);
		if (addressBook != null) {
			return addressBook;
		}

		addressBook = AddressBookRepository.get().queryUserBaseInfo(actualUserId);
		if (addressBook == null) {
			return null;
		}
		addressBook.imageHref = tryFixImageHref(addressBook.imageHref);
		mUserInfoCache.put(actualUserId, addressBook);
		return addressBook;
	}

	@Override
	public List<AddressBook> queryUserIds(List<String> userIds) {
		if (CommonUtil.isEmptyList(userIds)) {
			return null;
		}
		List<String> actualUserIds = getActualUserIds(userIds);

		List<AddressBook> userInfoList = new ArrayList<>(userIds.size());
		List<String> tempUserIds = new ArrayList<>();
		for (String userId : actualUserIds) {
			AddressBook addressBook = mUserInfoCache.get(userId);
			if (addressBook != null) {
				userInfoList.add(addressBook);
			}
			else {
				tempUserIds.add(userId);
			}
		}

		if (CommonUtil.isEmptyList(tempUserIds)) {
			return userInfoList;
		}

		List<AddressBook> addressBooks = AddressBookRepository.get().queryUsersByUserIds(tempUserIds);
		if (CommonUtil.isEmptyList(addressBooks)) {
			return userInfoList;
		}

		for (AddressBook addressBook : addressBooks) {
			addressBook.imageHref = tryFixImageHref(addressBook.imageHref);
			userInfoList.add(addressBook);
			mUserInfoCache.put(addressBook.userId, addressBook);

		}

		return userInfoList;
	}

	public String getActualUserId(String userId) {
		if (TextUtils.isEmpty(userId)) {
			return userId;
		}
		if (userId.length() > 32) {
			if (userId.contains("_")) {
				return userId.split("_")[1];
			}
			return userId.substring(32);
		}
		return userId;
	}

	@Override
	public List<AddressBook> queryContactName(String userName, int offset) {
		return AddressBookRepository.get().queryContactByNameLike(userName, offset).contacts;
	}

	@Override
	public List<AddressBook> queryAllAddressBooks() {
		return AddressBookRepository.get().queryAllAddressBooks();
	}

	@Override
	public Observable<AddressBook> queryUserDetail(String userId) {
		return AddressBookRepository.get()
				.queryUserDetail(getActualUserId(userId))
				.subscribeOn(scheduler)//使用有限线程池，防止部分手机内存溢出
				.observeOn(AndroidSchedulers.mainThread());
	}

	private List<String> getActualUserIds(List<String> userIds) {
		List<String> actualUserIds = new ArrayList<>(userIds.size());
		for (String userId : userIds) {
			actualUserIds.add(getActualUserId(userId));
		}
		return actualUserIds;
	}

	private String tryFixImageHref(String imageHref) {
		if (TextUtils.isEmpty(imageHref)) {
			return imageHref;
		}
		return imageHref.replace("\\", "/");
	}


}

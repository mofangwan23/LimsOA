package cn.flyrise.feep.retrieval.repository;

import static cn.flyrise.feep.retrieval.bean.Retrieval.VIEW_TYPE_CONTENT;
import static cn.flyrise.feep.retrieval.vo.RetrievalType.TYPE_CONTACT;

import cn.flyrise.android.protocol.entity.AddressBookRequest;
import cn.flyrise.android.protocol.entity.AddressBookResponse;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.addressbook.model.ContactQueryVO;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.retrieval.bean.ContactRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.vo.RetrievalResults;
import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2018-05-09 16:05
 * 联系人信息检索
 */
public class ContactRetrievalRepository extends RetrievalRepository {

	@Override public void search(Subscriber<? super RetrievalResults> subscriber, String keyword) {
		AddressBookRequest request = new AddressBookRequest();
		request.setSearchUserID("");
		request.setParentItemID("");
		request.setCurrentDeptID("");
		request.setFilterType(0);
		request.setDataSourceType(1);
		request.setParentItemType(1);
		request.setPage("1");
		request.setPerPageNums("10");
		request.setSearchKey(keyword);
		request.setIsCurrentDept(false);

		FEHttpClient.getInstance().post(request, new ResponseCallback<AddressBookResponse>() {
			@Override public void onCompleted(AddressBookResponse response) {
				List<ContactRetrieval> contactRetrievals = null;
				if (response != null && CommonUtil.nonEmptyList(response.getItems())) {
					contactRetrievals = new ArrayList<>();
					contactRetrievals.add((ContactRetrieval) header("联系人"));
					int index = 0;
					for (AddressBookItem item : response.getItems()) {
						if (index > 2) break;
						contactRetrievals.add(createRetrievalContact(item, keyword));
						index++;
					}

					if (response.getItems().size() >= 3) {
						contactRetrievals.add((ContactRetrieval) footer("更多联系人"));
					}
				}

				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(contactRetrievals)
						.create());
			}

			@Override public void onFailure(RepositoryException repositoryException) {
				subscriber.onNext(new RetrievalResults.Builder()
						.retrievalType(getType())
						.retrievals(null)
						.create());
			}
		});
	}

	// 将 AddressBook 转化为 ContactRetrieval 对象
	private ContactRetrieval createRetrievalContact(AddressBookItem addressBook, String keyword) {
		ContactRetrieval contactRetrieval = new ContactRetrieval();
		contactRetrieval.viewType = VIEW_TYPE_CONTENT;
		contactRetrieval.retrievalType = TYPE_CONTACT;
		contactRetrieval.content = fontDeepen(addressBook.getName(), keyword);
		contactRetrieval.extra = addressBook.getDepartmentName();

//		contactRetrieval.deptId = addressBook.deptId;
		contactRetrieval.userId = addressBook.getId();
		contactRetrieval.username = addressBook.getName();
		contactRetrieval.imageHref = CoreZygote.getLoginUserServices().getServerAddress() + addressBook.getImageHref();
		return contactRetrieval;
	}

	@Override protected int getType() {
		return TYPE_CONTACT;
	}

	@Override protected Retrieval newRetrieval() {
		return new ContactRetrieval();
	}
}

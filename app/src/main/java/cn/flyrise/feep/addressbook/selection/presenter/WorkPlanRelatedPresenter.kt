package cn.flyrise.feep.addressbook.selection.presenter

import android.text.TextUtils
import cn.flyrise.android.protocol.entity.RelatedUserRequest
import cn.flyrise.android.protocol.entity.RelatedUserResponse
import cn.flyrise.android.protocol.model.User
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.services.model.AddressBook
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * @author ZYP
 * @since 2018-06-07 16:37
 * 计划相关人员
 */
class WorkPlanRelatedPresenter : SelectionPresenter() {

    override fun start() {
        selectionView.showLoading()
        Observable
                .create { f: Subscriber<in List<User>> ->
                    val request = RelatedUserRequest()
                    request.requestType = "0"
                    FEHttpClient.getInstance().post(request, object : ResponseCallback<RelatedUserResponse>() {
                        override fun onCompleted(response: RelatedUserResponse?) {
                            if (response != null && TextUtils.equals(response.errorCode, "0")) {
                                f.onNext(response.users)
                            } else {
                                f.onNext(mutableListOf())
                            }
                        }

                        override fun onFailure(repositoryException: RepositoryException?) {
                            f.onNext(mutableListOf())
                        }
                    })
                }
                .map {
                    var addressBooks: List<AddressBook>? = null
                    if (CommonUtil.nonEmptyList(it)) {
                        val userIds = mutableListOf<String>()
                        it.forEach { userIds.add(it.id) }
                        addressBooks = CoreZygote.getAddressBookServices().queryUserIds(userIds)

                        if (CommonUtil.nonEmptyList(addressBooks)) {
                            Collections.sort(addressBooks, object : Comparator<AddressBook> {
                                override fun compare(lhs: AddressBook, rhs: AddressBook) = compareValuesBy(lhs, rhs, AddressBook::sortPinYin,
                                        AddressBook::sortName, AddressBook::deptGrade, AddressBook::sortNo)
                            })
                        }
                    }
                    addressBooks
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    selectionView.hideLoading()
                    contacts = it
                    selectionView.showContacts(contacts,null)
                }
    }
}
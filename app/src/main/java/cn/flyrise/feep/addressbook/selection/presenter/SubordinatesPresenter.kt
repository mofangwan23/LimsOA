package cn.flyrise.feep.addressbook.selection.presenter

import android.text.TextUtils
import cn.flyrise.android.protocol.entity.LocationLocusRequest
import cn.flyrise.android.protocol.entity.LocationLocusResponse
import cn.flyrise.feep.addressbook.model.AddressBooks
import cn.flyrise.feep.addressbook.source.AddressBookRepository
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.X
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
 * @author mofangwan
 * @since 2018-06-07 16:10
 * 下属
 */
class SubordinatesPresenter : SelectionPresenter() {

    override fun start() {
        selectionView.showLoading()
        Observable
                .create { f: Subscriber<in AddressBooks> ->
                    val request = LocationLocusRequest()
                    request.requestType = X.LocationType.Person
                    request.brType = "0";
                    FEHttpClient.getInstance().post(request, object : ResponseCallback<LocationLocusResponse>() {
                        override fun onCompleted(response: LocationLocusResponse?) {
                            if (!TextUtils.equals(response?.errorCode, "0")) {
                                f.onNext(AddressBooks(mutableListOf(), mutableListOf(), mutableSetOf(), mutableSetOf()))
                                return
                            }

                            val userIds = mutableListOf<String?>()
                            response?.personList?.forEachIndexed { index, p ->
                                if (!p.userId.equals(CoreZygote.getLoginUserServices().userId)) {
                                    userIds.add(p.userId)
                                }
                            }
                            val deparList = mutableListOf<String?>()
                            if (!CommonUtil.isEmptyList(response?.departmentList)) deparList.addAll(response?.departmentList!!)
                            if (!CommonUtil.isEmptyList(response?.departmentList2)) deparList.addAll(response?.departmentList2!!)
                            f.onNext(AddressBooks(deparList, userIds, mutableSetOf(), mutableSetOf()))
                        }

                        override fun onFailure(exception: RepositoryException?) {
                            FELog.e("Query subordinate failure. Error: " + exception?.exception()?.message)
                            f.onNext(AddressBooks(mutableListOf(), mutableListOf(), mutableSetOf(), mutableSetOf()))
                        }
                    })
                }
                .map {
                    if (CommonUtil.nonEmptyList(it.userIds)) {
                        val addressBooks = CoreZygote.getAddressBookServices().queryUserIds(it.userIds)
                        if(CommonUtil.nonEmptyList(addressBooks))it.addressBooks?.addAll(addressBooks)
                        it.userIds.clear()
                    }

                    if (CommonUtil.nonEmptyList(it.deptIds)) {
                        val addressBooks = AddressBookRepository.get().queryContactsByDeptIds(it.deptIds)
                        if(CommonUtil.nonEmptyList(addressBooks))it.deptUsers?.addAll(addressBooks)
                        it.deptIds?.clear()
                    }
                    it
                }
                .map {
                    val addressBooks = ArrayList<AddressBook>(it.addressBooks)
                    if (CommonUtil.nonEmptyList(addressBooks)) {
                        Collections.sort(addressBooks, object : Comparator<AddressBook> {
                            override fun compare(lhs: AddressBook, rhs: AddressBook) = compareValuesBy(lhs, rhs, AddressBook::sortPinYin,
                                    AddressBook::sortName, AddressBook::deptGrade, AddressBook::sortNo)
                        })
                    }
                    it.addressBooks?.clear()
                    it.addressBooks?.addAll(addressBooks)
                    it
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    contacts = it.addressBooks?.toList()
                    selectionView.hideLoading()
                    selectionView.showContacts(contacts, it.deptUsers?.toList())
                }
    }

}
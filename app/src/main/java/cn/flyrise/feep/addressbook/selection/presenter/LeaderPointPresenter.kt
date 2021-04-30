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
 * @author ZYP
 * @since 2018-06-07 16:10
 * 领导选人
 */
class LeaderPointPresenter : SelectionPresenter() {

    override fun start() {
        selectionView.showLoading()
        Observable
                .create { f: Subscriber<in AddressBooks> ->
                    val request = LocationLocusRequest()
                    request.requestType = X.LocationType.Person
                    request.brType = "1"
                    FEHttpClient.getInstance().post(request, object : ResponseCallback<LocationLocusResponse>() {
                        override fun onCompleted(response: LocationLocusResponse?) {
                            if (!TextUtils.equals(response?.errorCode, "0")) {
                                f.onNext(AddressBooks(mutableListOf(), mutableListOf(), mutableSetOf(), mutableSetOf()))
                                return
                            }

                            val userIds = mutableListOf<String?>()
                            response?.personList?.forEachIndexed { index, p ->
                                userIds.add(index, p.userId)
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
                        val addressBooks = CoreZygote.getAddressBookServices().queryUserIds(it.userIds.filter { !TextUtils.isEmpty(it) })
                        it.addressBooks?.addAll(addressBooks)
                        it.userIds.clear()
                    }
                    if (CommonUtil.nonEmptyList(it.deptIds)) {
                        val addressBooks = AddressBookRepository.get().queryContactsByDeptIds(it.deptIds)
                        it.addressBooks?.addAll(addressBooks)
                        it.deptIds?.clear()
                    }
                    it.addressBooks?.filter { !TextUtils.equals(it?.userId, CoreZygote.getLoginUserServices().userId) }
                }.map {
                    val addressBooks = ArrayList<AddressBook>(it)
                    if (CommonUtil.nonEmptyList(addressBooks)) {
                        Collections.sort(addressBooks, object : Comparator<AddressBook> {
                            override fun compare(lhs: AddressBook, rhs: AddressBook) = compareValuesBy(lhs, rhs, AddressBook::sortPinYin,
                                    AddressBook::sortName, AddressBook::deptGrade, AddressBook::sortNo)
                        })
                    }
                    addressBooks
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    contacts = it
                    selectionView.hideLoading()
                    selectionView.showContacts(contacts, null)
                }
    }

    private fun isEmpty(a: String, b: String) = TextUtils.isEmpty(a) || TextUtils.isEmpty(b)

}
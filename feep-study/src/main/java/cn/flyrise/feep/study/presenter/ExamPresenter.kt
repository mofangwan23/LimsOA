package cn.flyrise.feep.study.presenter

import cn.flyrise.feep.study.entity.*
import cn.flyrise.feep.study.fragment.ExamView
import cn.flyrise.feep.study.respository.ExamDataRepository
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class ExamPresenter(val view: ExamView?, private val repository: ExamDataRepository){

    fun start(request:TrainingSignRequest){
        view?.showLoading()
        repository.requestExamMyPaperList(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {  }
                .subscribe({
                    view?.showPaperList(it.data)
                    view?.hideLoading()
                }, {
                    it.printStackTrace()
                    view?.hideLoading()
                })
    }

}
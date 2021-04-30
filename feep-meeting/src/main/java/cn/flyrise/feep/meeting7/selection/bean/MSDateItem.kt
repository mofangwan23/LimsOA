package cn.flyrise.feep.meeting7.selection.bean

import cn.flyrise.feep.meeting7.selection.memo.Memento
import cn.flyrise.feep.meeting7.selection.memo.Memorable


/**
 * @author ZYP
 * @since 2018-06-13 09:27
 */
open class MSDateItem : Memorable {

    var year: Int = 0
    var month: Int = 0
    var day: Int = 0
    var state: Int = 0
//    var extra: String? = ""

    constructor()

    constructor(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
    }

    override fun createMemento(key: Int): Memento = Memento(state, key)

    override fun restoreState(memento: Memento): Int {
        this.state = memento.state
        return memento.position
    }
}
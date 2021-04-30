package cn.flyrise.feep.meeting7.selection.bean

import cn.flyrise.feep.meeting7.selection.memo.Memento

/**
 * @author ZYP
 * @since 2018-06-14 17:01
 */
class MSDayDateItem : MSDateItem {

    var week: Int = 0

    constructor(state: Int) : super() {
        this.state = state
    }

    constructor(year: Int, month: Int, day: Int) : super(year, month, day)

    override fun createMemento(position: Int): Memento = Memento(state, position)

    override fun restoreState(memento: Memento): Int {
        this.state = memento.state
        return memento.position
    }
}
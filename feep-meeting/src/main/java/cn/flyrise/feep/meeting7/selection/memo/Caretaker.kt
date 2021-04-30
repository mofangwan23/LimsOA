package cn.flyrise.feep.meeting7.selection.memo

/**
 * @author ZYP
 * @since 2018-06-12 15:21
 * 备忘录模式操作日期/时间起止时间段的选择、以及取消后状态的恢复
 */
class Caretaker {

    private var startState: Memento? = null                                 // 记录开始日期
    private var endState: Memento? = null                                   // 记录结束日期
    private var sectionStates: MutableList<Memento> = mutableListOf()    // 记录开始-结束中间段的日期

    fun recordStartState(memento: Memento) {
        this.startState = memento
    }

    fun recordEndState(memento: Memento) {
        this.endState = memento
    }

    fun recordSectionState(memento: Memento) {
        sectionStates.add(memento)
    }

    fun getStartState(): Memento? = startState
    fun getEndState(): Memento? = endState
    fun getSectionStates(): MutableList<Memento> = sectionStates

    fun restoreToOriginalState(withStartStart: Boolean) {
        if (withStartStart) {
            startState = null
        }
        endState = null
        sectionStates.clear()
    }


}
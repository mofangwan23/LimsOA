package cn.flyrise.feep.meeting7.selection.memo

/**
 * @author ZYP
 * @since 2018-06-13 11:13
 * 可备忘记录的接口，实现了该接口的对象，都是可以进行备忘记录、恢复的对象
 */
interface Memorable {

    fun createMemento(key: Int): Memento

    fun restoreState(memento: Memento): Int

}
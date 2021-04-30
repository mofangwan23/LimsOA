package cn.flyrise.feep.addressbook.model;

/**
 * @author ZYP
 * @since 2016-12-07 17:46
 */
public class PositionEvent {

    public Position position;
    public boolean hasChange;

    public PositionEvent(Position position, boolean hasChange) {
        this.position = position;
        this.hasChange = hasChange;
    }
}

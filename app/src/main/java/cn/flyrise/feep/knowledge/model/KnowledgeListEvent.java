package cn.flyrise.feep.knowledge.model;

import java.util.List;

import cn.flyrise.feep.commonality.bean.SelectedPerson;

/**
 * Created by k on 2016/9/6.
 */
public class KnowledgeListEvent {

    final public static int SELECTPERSION = 1;
    final public static int SETTABLAYOUTENABLE = 2;

    public int eventID;
    public boolean tabEnable;
    public List<SelectedPerson> selectedPerson;

    public KnowledgeListEvent(int eventID, boolean tabEnable) {
        this.eventID = eventID;
        this.tabEnable = tabEnable;
    }

    public KnowledgeListEvent(int eventID, List<SelectedPerson> selectedPerson) {
        this.eventID = eventID;
        this.selectedPerson = selectedPerson;
    }
}
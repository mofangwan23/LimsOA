package cn.flyrise.feep.study.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;
import java.util.List;

public class TrainingTaskResponse extends ResponseContent {

    private List<TrainingTaskBean> data;

	public List<TrainingTaskBean> getTaskBeanList() {
		return data;
	}
}

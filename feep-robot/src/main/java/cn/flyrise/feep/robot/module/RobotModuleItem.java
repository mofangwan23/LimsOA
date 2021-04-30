package cn.flyrise.feep.robot.module;

import android.text.Spanned;

import cn.flyrise.feep.robot.bean.FeSearchMessageItem;
import java.util.List;

import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.entity.RiddleResultItem;
import cn.flyrise.feep.robot.entity.RobotHolidayItem;
import cn.flyrise.feep.robot.entity.RobotResultItem;
import cn.flyrise.feep.robot.entity.RobotTrainItem;
import cn.flyrise.feep.robot.entity.WeatherResultData;

/**
 * Created by Administrator on 2017-6-19.
 * 适配器中显示时的最终模块
 */

public class RobotModuleItem {

    public int indexType;           // 适配器中显示的位置
    public int moduleId;            //对应FEEnum中的id（特殊类型除外：天气、打电话）
    public String title;             //一般为名字、标题
    public String content;           //内容
    public Spanned htmlContent;      //转化后的文本
    public int icon;                //中间模块中的右边图标
    public int moduleParentType;    //当前子模块的父类类型，比如已办的父类为协同
    public String operationType;     //操作类型
    public AddressBook addressBook; //人员详情
    public List<String> textList; //文本集合
    public int process;         //当前进程
    public boolean isContentViewSwitch = false; //详情模块的选择按钮
    public FeSearchMessageItem feListItem; //消息搜索列表的详情
    public List<WeatherResultData> weatherDatas; //天气预报
    public String date; //当前请求的日期时间
    public String service; //当前模块类型
    public List<RobotResultItem> results; //诗词、笑话、故事
    public List<RobotTrainItem> trainItems;//火车票
    public List<RobotHolidayItem> holidayItems;//节假日查询
    public RiddleResultItem riddleItem;//谜语类

    public RobotModuleItem(Builder builder) {
        indexType = builder.indexType;
        moduleId = builder.moduleId;
        title = builder.title;
        content = builder.content;
        htmlContent = builder.htmlContent;
        icon = builder.icon;
        moduleParentType = builder.moduleParentType;
        operationType = builder.operationType;
        addressBook = builder.addressBook;
        textList = builder.textList;
        process = builder.process;
        isContentViewSwitch = builder.isContentViewSwitch;
        feListItem = builder.feListItem;
        weatherDatas = builder.weatherDatas;
        date = builder.date;
        service = builder.service;
        results = builder.results;
        trainItems = builder.trainItems;
        holidayItems = builder.holidayItems;
        riddleItem = builder.riddleItem;
    }

    public static class Builder {
        private int indexType;
        private int moduleId;
        private String title;
        private String content;
        private Spanned htmlContent;
        private int icon;
        private int moduleParentType;
        private String operationType;
        private AddressBook addressBook;
        private List<String> textList;
        private int process;
        private boolean isContentViewSwitch = false;
        private FeSearchMessageItem feListItem;
        private List<WeatherResultData> weatherDatas;
        private String date;
        private String service;
        private List<RobotResultItem> results;
        private List<RobotTrainItem> trainItems;
        private List<RobotHolidayItem> holidayItems;
        private RiddleResultItem riddleItem;

        public Builder setIndexType(int indexType) {// 适配器中显示的位置
            this.indexType = indexType;
            return this;
        }

        public Builder setModuleId(int moduleId) {//对应FEEnum中的id（特殊类型除外：天气、打电话）
            this.moduleId = moduleId;
            return this;
        }

        public Builder setTitle(String title) {//一般为名字、标题
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {//内容
            this.content = content;
            return this;
        }

        public Builder setHtmlContent(Spanned htmlContent) {//转化后的文本
            this.htmlContent = htmlContent;
            return this;
        }

        public Builder setIcon(int icon) {//中间模块中的右边图标
            this.icon = icon;
            return this;
        }

        public Builder setModuleParentType(int moduleParentType) {//当前子模块的父类类型，比如已办的父类为协同
            this.moduleParentType = moduleParentType;
            return this;
        }

        public Builder setOperationType(String operationType) {//操作类型
            this.operationType = operationType;
            return this;
        }

        public Builder setAddressBook(AddressBook addressBook) {//人员详情
            this.addressBook = addressBook;
            return this;
        }

        public Builder setTextList(List<String> textList) {//文本集合
            this.textList = textList;
            return this;
        }

        public Builder setProcess(int process) {//当前进程
            this.process = process;
            return this;
        }

        public Builder setContentViewSwitch(boolean contentViewSwitch) {//详情模块的选择按钮
            isContentViewSwitch = contentViewSwitch;
            return this;
        }

        public Builder setFeListItem(FeSearchMessageItem feListItem) {//消息搜索列表的详情
            this.feListItem = feListItem;
            return this;
        }

        public Builder setWeatherDatas(List<WeatherResultData> weatherDatas) {//天气预报
            this.weatherDatas = weatherDatas;
            return this;
        }

        public Builder setDate(String date) {//当前请求的日期时间
            this.date = date;
            return this;
        }

        public Builder setService(String service) {//当前模块类型
            this.service = service;
            return this;
        }

        public Builder setResults(List<RobotResultItem> results) {//诗词、笑话、故事
            this.results = results;
            return this;
        }

        public Builder setTrainItems(List<RobotTrainItem> trainItems) {//火车票 
            this.trainItems = trainItems;
            return this;
        }

        public Builder setHolidayItems(List<RobotHolidayItem> holidayItems) {//节假日查询
            this.holidayItems = holidayItems;
            return this;
        }

        public Builder setRiddleItem(RiddleResultItem riddleItem) {//节假日查询
            this.riddleItem = riddleItem;
            return this;
        }

        public RobotModuleItem create() {
            return new RobotModuleItem(this);
        }
    }


}

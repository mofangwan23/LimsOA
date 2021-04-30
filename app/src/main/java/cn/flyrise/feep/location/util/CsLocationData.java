package cn.flyrise.feep.location.util;

import com.amap.api.services.core.LatLonPoint;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.core.common.FELog;

/**
 * 新建：陈冕;
 * 日期： 2018-4-13-9:41.
 */

public class CsLocationData {

    private static int index = 0;

    private static List<LatLonPoint> getLatLngs() {//跨服大的随机跳跃
        List<LatLonPoint> latLngs = new ArrayList<>();
        latLngs.add(new LatLonPoint(22.370267365879325, 113.57177585363387));
        latLngs.add(new LatLonPoint(22.369566663608584, 113.57116162776947));//古啄

        latLngs.add(new LatLonPoint(22.36780683120411, 113.57052326202393));
        latLngs.add(new LatLonPoint(22.36479807396424, 113.56979638338089));//四季公寓

        latLngs.add(new LatLonPoint(22.368544748134017, 113.56350928544998));//高新分局
        latLngs.add(new LatLonPoint(22.36928142096621, 113.56175377964975));
        latLngs.add(new LatLonPoint(22.37575752512683, 113.5706949234009));//造船
        latLngs.add(new LatLonPoint(22.37397172794618, 113.57031002640724));//魅族宿舍
        latLngs.add(new LatLonPoint(22.371970119707477, 113.56907218694687));//魅族
        latLngs.add(new LatLonPoint(22.372930002343132, 113.57458010315897));//中国银行
        latLngs.add(new LatLonPoint(22.373427303276177, 113.57581928372383));//中国银行
        return latLngs;
    }

    private static List<LatLonPoint> getArrayLatlon() {//连续的走路移动
        List<LatLonPoint> latLngs = new ArrayList<>();
        latLngs.add(new LatLonPoint(22.371620393381182, 113.57376202940944));
        latLngs.add(new LatLonPoint(22.37147405373567, 113.57367888092995));
//        latLngs.add(new LatLonPoint(22.371402124023, 113.57361987233163));
//        latLngs.add(new LatLonPoint(22.37130167039659, 113.57356622815134));
//        latLngs.add(new LatLonPoint(22.371230980764174, 113.57339188456534));
//        latLngs.add(new LatLonPoint(22.37107347917336, 113.57309952378274));
//        latLngs.add(new LatLonPoint(22.37094450135997, 113.57289433479309));
//        latLngs.add(new LatLonPoint(22.37066918293551, 113.57261538505556));
//        latLngs.add(new LatLonPoint(22.37045215226216, 113.57245981693268));
//        latLngs.add(new LatLonPoint(22.37033557564653, 113.5724464058876));
//        latLngs.add(new LatLonPoint(22.370216518576587, 113.57247456908227));
//        latLngs.add(new LatLonPoint(22.370116064094624, 113.57219561934471));
//        latLngs.add(new LatLonPoint(22.369899032558973, 113.57224658131601));
//        latLngs.add(new LatLonPoint(22.369647275553767, 113.57233777642251));
//        latLngs.add(new LatLonPoint(22.36948109087723, 113.57205078005792));
//        latLngs.add(new LatLonPoint(22.369199568621802, 113.57217013835908));


//        latLngs.add(new LatLonPoint(22.368862237239966, 113.57229217886925));
//        latLngs.add(new LatLonPoint(22.367469496447843, 113.57021346688273));
//        latLngs.add(new LatLonPoint(22.368687370282906, 113.56946378946303));
//        latLngs.add(new LatLonPoint(22.368894482186807, 113.57017725706102));
        latLngs.add(new LatLonPoint(22.368715894695125, 113.57058629393578));
        latLngs.add(new LatLonPoint(22.368522424653975, 113.57021480798721));

        latLngs.add(new LatLonPoint(22.38684879664577, 113.57771426439285));//异常签到
        return latLngs;
    }

    public static LatLonPoint csLatLng() {
//        LatLonPoint latLonPoint = getLatLngs().get(index);
        LatLonPoint latLonPoint = getArrayLatlon().get(index);
        if (++index >= 5) index = 0;
        FELog.i("cslocation", "-->>>>csLocation:-index:" + index);
        return latLonPoint;
    }

}

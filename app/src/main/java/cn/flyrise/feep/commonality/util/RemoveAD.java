package cn.flyrise.feep.commonality.util;

public class RemoveAD {

    private static final String[] ads = { "a.baidu.com", "baidutv.baidu.com", "bar.baidu.com", "c.baidu.com", "cjhq.baidu.com", "drmcmm.baidu.com", "e.baidu.com", "eiv.baidu.com", "hc.baidu.com", "hm.baidu.com", "ma.baidu.com", "nsclick.baidu.com", "spcode.baidu.com", "tk.baidu.com", "union.baidu.com", "ucstat.baidu.com", "utility.baidu.com", "utk.baidu.com", "focusbaiduafp.allyes.com", "a.baidu.com",
            "adm.baidu.com", "baidutv.baidu.com", "banlv.baidu.com", "bar.baidu.com", "c.baidu.com", "cb.baidu.com", "cbjs.baidu.com", "cjhq.baidu.com", "cpro.baidu.com", "dl.client.baidu.com", "drmcmm.baidu.com", "dzl.baidu.com", "e.baidu.com", "eiv.baidu.com", "gimg.baidu.com", "guanjia.baidu.com", "hc.baidu.com", "hm.baidu.com", "iebar.baidu.com", "ikcode.baidu.com", "ma.baidu.com",
            "neirong.baidu.com", "nsclick.baidu.com", "pos.baidu.com", "s.baidu.com", "sobar.baidu.com", "sobartop.baidu.com", "spcode.baidu.com", "tk.baidu.com", "tkweb.baidu.com", "tongji.baidu.com", "toolbar.baidu.com", "tracker.baidu.com", "ucstat.baidu.com", "ulic.baidu.com", "union.baidu.com", "unstat.baidu.com", "utility.baidu.com", "utk.baidu.com", "ubmcmm.baidustatic.com",
            "wangmeng.baidu.com", "wm.baidu.com", };

    public static boolean contentAD(String url) {
        boolean isAdd = false;
        for (final String ad : ads) {
            if (url.contains(ad)) {
                isAdd = true;
                break;
            }
        }
        return isAdd;
    }
}

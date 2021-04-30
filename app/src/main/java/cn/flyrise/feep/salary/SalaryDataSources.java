package cn.flyrise.feep.salary;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.android.protocol.entity.SalaryDetailResponse;
import cn.flyrise.android.protocol.entity.SalaryListResponse;
import cn.flyrise.android.protocol.entity.SalaryRequest;
import cn.flyrise.android.protocol.entity.SalaryVerifyResponse;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.salary.model.Salary;
import cn.flyrise.feep.salary.model.SalaryItem;
import rx.Observable;

/**
 * @author ZYP
 * @since 2017-02-20 14:19
 * 薪资数据来源
 */
public class SalaryDataSources {

    /**
     * 校验密码
     *
     * @param password Base64 编码后的字符串
     */
    public static Observable<Integer> verifyPassword(String password) {
        return Observable.create(f -> {
            SalaryRequest request = new SalaryRequest();
            request.pwd = password;
            FEHttpClient.getInstance().post(request, new ResponseCallback<SalaryVerifyResponse>() {
                @Override
                public void onCompleted(SalaryVerifyResponse response) {
                    String errorCode = response.getErrorCode();
                    if (!TextUtils.equals(errorCode, "0")) {
                        f.onError(new RuntimeException("Failed to verify password. Error message : " + response.getErrorMessage()));
                        f.onCompleted();
                        return;
                    }

                    f.onNext(CommonUtil.parseInt(response.code));
                    f.onCompleted();
                }

                @Override
                public void onFailure(RepositoryException repositoryException) {
                    f.onError(new RuntimeException("Failed to verify password."));
                    f.onCompleted();
                }
            });
        });
    }

    /**
     * 查询月工资列表
     */
    public static Observable<Map<String, List<Salary>>> querySalaryMonthLists() {
        return Observable.create(f -> {
            SalaryRequest request = SalaryRequest.buildQueryMonthListsRequest();
            FEHttpClient.getInstance().post(request, new ResponseCallback<SalaryListResponse>() {
                @Override
                public void onCompleted(SalaryListResponse response) {
                    Map<String, List<Salary>> yearMap = new LinkedHashMap<>();
                    if (TextUtils.equals(response.getErrorCode(), "0")) {
                        List<Map<String, String>> months = response.months;
                        if (CommonUtil.nonEmptyList(months)) {
                            for (Map<String, String> month : months) {
                                Map.Entry<String, String> next = month.entrySet().iterator().next();
                                String key = next.getKey();
                                String value = next.getValue();
                                String[] date = key.split("-");
                                List<Salary> salaries = yearMap.get(date[0]);
                                if (CommonUtil.isEmptyList(salaries)) {
                                    salaries = new ArrayList<>();
                                    salaries.add(new Salary(date[1], value));
                                    yearMap.put(date[0], salaries);
                                }
                                else {
                                    salaries.add(new Salary(date[1], value));
                                }
                            }

                        }
                    }
                    f.onNext(yearMap);
                    f.onCompleted();
                }

                @Override
                public void onFailure(RepositoryException repositoryException) {
                    f.onNext(null);
                    f.onCompleted();
                }
            });
        });
    }

    /**
     * 查询具体月份工资
     *
     * @param month 具体月份，可以为空，为空则查询最近的月份
     */
    public static Observable<List<SalaryItem>> querySalaryDetail(String month) {
        return Observable.create(f -> {
            SalaryRequest request = SalaryRequest.buildSalaryDetailRequest(month);
            FEHttpClient.getInstance().post(request, new ResponseCallback<SalaryDetailResponse>() {
                @Override
                public void onCompleted(SalaryDetailResponse response) {
                    List<SalaryItem> salaryItems = new ArrayList<>();
                    if (TextUtils.equals(response.getErrorCode(), "0")) {
                        List<SalaryItem> addItems = buildDetailItems(response.add, SalaryItem.TYPE_ADD);
                        if (CommonUtil.nonEmptyList(addItems)) {
                            salaryItems.addAll(addItems);
                        }

                        List<SalaryItem> subItems = buildDetailItems(response.subtract, SalaryItem.TYPE_SUB);
                        if (CommonUtil.nonEmptyList(subItems)) {
                            salaryItems.addAll(subItems);
                        }

                        List<SalaryItem> otherItems = buildDetailItems(response.other, SalaryItem.TYPE_OTHER);
                        if (CommonUtil.nonEmptyList(otherItems)) {
                            salaryItems.addAll(otherItems);
                        }
                    }
                    f.onNext(salaryItems);
                    f.onCompleted();
                }

                @Override
                public void onFailure(RepositoryException repositoryException) {
                    f.onNext(null);
                    f.onCompleted();
                }
            });
        });
    }

    private static List<SalaryItem> buildDetailItems(List<Map<String, String>> lists, byte type) {
        List<SalaryItem> items = null;
        if (CommonUtil.nonEmptyList(lists)) {
            items = new ArrayList<>();
            for (Map<String, String> pair : lists) {
                SalaryItem item = new SalaryItem();
                Map.Entry<String, String> next = pair.entrySet().iterator().next();
                item.key = next.getKey();
                item.value = next.getValue();
                item.type = type;
                items.add(item);
            }
        }
        return items;
    }
}
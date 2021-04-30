/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����3:51:57
 */
package cn.flyrise.android.protocol.entity;

import java.util.List;

import cn.flyrise.feep.collaboration.model.WaitingSend;
import cn.flyrise.feep.core.network.request.ResponseContent;

public class WaitingSendListResponse extends ResponseContent {


    public List<WaitingSend> result;

}

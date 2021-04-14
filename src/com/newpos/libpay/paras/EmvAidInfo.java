package com.newpos.libpay.paras;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouqiang on 2017/12/11.
 * @author zhouqiang
 * EMV AID
 */
public class EmvAidInfo implements Serializable {


	public static final String FILENAME = "default_aids.dat";

	private List<byte[]> aidInfoList;

	public EmvAidInfo() {
		aidInfoList = new ArrayList<byte[]>();
	}

	public List<byte[]> getAidInfoList() {
		return aidInfoList;
	}

	public void setAidInfoList(List<byte[]> aidInfoList) {
		this.aidInfoList = aidInfoList;
	}
}

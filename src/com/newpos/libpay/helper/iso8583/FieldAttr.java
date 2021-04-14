package com.newpos.libpay.helper.iso8583;

/**
 * attr de campo
 * @author
 */
public class FieldAttr {
	private int lenType; // tipo de longitud de campo: BCD ASCII BIN
	private int lenAttr; // attr de longitud de campo: no LL LLL
	private int dataType; // tipo de datos de campo: ASCII (N CN)BCD BIN
	private int dataLen; // longitud de datos de campo

	public int getLenType() {
		return lenType;
	}

	public void setLenType(int lenType) {
		this.lenType = lenType;
	}

	public int getLenAttr() {
		return lenAttr;
	}

	public void setLenAttr(int lenAttr) {
		this.lenAttr = lenAttr;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public int getDataLen() {
		return dataLen;
	}

	public void setDataLen(int dataLen) {
		this.dataLen = dataLen;
	}

}

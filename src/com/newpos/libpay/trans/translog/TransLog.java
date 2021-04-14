package com.newpos.libpay.trans.translog;

import com.newpos.libpay.Logger;
import com.newpos.libpay.global.TMConfig;
import com.newpos.libpay.utils.PAYUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * registro de transacciones
 * @author
 */

public class TransLog implements Serializable {
	private static String TranLogPath = "translog.dat";
	private static String ScriptPath = "script.dat";
	private static String ReversalPath = "reversal.dat";

	private List<TransLogData> transLogData = new ArrayList<TransLogData>();
	private static TransLog tranLog;

	private TransLog() {
	}

	public static TransLog getInstance() {
		if (tranLog == null) {
			String filepath = TMConfig.getRootFilePath() + TranLogPath;
			try {
				tranLog = ((TransLog) PAYUtils.file2Object(filepath));
			} catch (FileNotFoundException e) {
				tranLog = null;
			} catch (IOException e) {
				tranLog = null;
			} catch (ClassNotFoundException e) {
				tranLog = null;
			}if (tranLog == null) {
				tranLog = new TransLog();
			}
		}
		return tranLog;
	}

	public List<TransLogData> getData() {
		return transLogData;
	}

	public int getSize() {
		return transLogData.size();
	}

	public TransLogData get(int position) {
		if (!(position > getSize())) {
			return transLogData.get(position);
		}
		return null;
	}

	/**
	 * borrar el registro de transacciones
	 */
	public void clearAll() {
		transLogData.clear();
		String FullName = TMConfig.getRootFilePath() + TranLogPath;
		File file = new File(FullName);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * obtener el último registro de transacciones
	 */
	public TransLogData getLastTransLog() {
		if (getSize() >= 1) {
			return transLogData.get(getSize() - 1);
		}
		return null;
	}

	/**
	 * guardar registro de transacciones
	 * @return
	 */
	public boolean saveLog(TransLogData data) {
		transLogData.add(data);
		Logger.debug("transLogData size " + transLogData.size());
		try {
			PAYUtils.object2File(tranLog, TMConfig.getRootFilePath()+ TranLogPath);
		} catch (FileNotFoundException e) {
			Logger.debug("save translog file not found");
			return false;
		} catch (IOException e) {
			Logger.debug("save translog IOException");
			return false;
		}
		return true;
	}

	/**
	 * actualizar el registro de transacciones por índice
	 * @param logIndex índice de registro de transacciones
	 * @param newData nuevo registro de transacciones
	 * @return true:si tiene exito,false:si falla
	 */
	public boolean updateTransLog(int logIndex, TransLogData newData) {
		if (getSize() > 0) {
			transLogData.set(transLogData.indexOf(transLogData.get(logIndex)), newData);
			return true;
		}
		return false;
	}

	/**
	 * obtener el índice de registro de transacciones
	 * @param data
	 * @return
     */
	public int getCurrentIndex(TransLogData data){
		int current = -1 ;
		for (int i = 0 ; i < transLogData.size() ; i++){
			if(transLogData.get(i).getTraceNo().equals(data.getTraceNo())){
				current = i ;
			}
		}
		return current ;
	}

	/**
	 * obtener el registro de transacciones por índice
	 * @param logIndex índice de registro de transacciones
	 * @return transLogData
	 */
	public TransLogData searchTransLogByIndex(int logIndex) {
		if (getSize() > 0 && getSize() - 1 >= logIndex) {
			return transLogData.get(logIndex);
		}
		return null;
	}

	/**
	 * obtener el registro de transacciones mediante el comprobante NO.
	 * @param TraceNo comprobante de transacción NO.
	 * @return transLogData
	 */
	public TransLogData searchTransLogByTraceNo(String TraceNo) {
		Logger.debug("searchTransLogByTraceNo >>>>>>>>>");
		if (getSize() > 0) {
			for (int i = 0; i < getSize(); i++) {
				Logger.debug("data.getTraceNo():" + transLogData.get(i).getTraceNo());
				if (!PAYUtils.isNullWithTrim(transLogData.get(i).getTraceNo())) {
					if (transLogData.get(i).getTraceNo().equals("" + TraceNo)) {
						return transLogData.get(i);
					}
				}
			}
		}
		return null;
	}

	/**
	 * obtener el registro de transacciones por código de autenticación y fecha.
	 * @param auth código de autorización de transacción
	 * @param date Fecha de Transacción
	 * @return TransLogData
	 */
	public TransLogData searchTransLogByAUTHDATE(String auth , String date) {
		Logger.debug("searchTransLogByAUTHDATE >>>>>>>>>");
		Logger.debug("auth:" + auth);
		Logger.debug("date:" + date);
		if (getSize() > 0) {
			for (int i = 0; i < getSize(); i++) {
				TransLogData data = transLogData.get(i);
				Logger.debug("data.getAuthCode():" + data.getAuthCode());
				Logger.debug("data.getLocalDate():" + data.getLocalDate());
				if (!PAYUtils.isNullWithTrim(data.getAuthCode()) &&
						!PAYUtils.isNullWithTrim(data.getLocalDate())) {
					if (data.getAuthCode().equals("" + auth) &&
							data.getLocalDate().equals("" + date)) {
						return data;
					}
				}
			}
		}
		return null;
	}

	/**
	 * obtener el registro de transacciones por el sistema, consulte NO y fecha
	 * @param refer número de referencia
	 * @param date datos de transaccion
	 * @return TransLogData
	 */
	public TransLogData searchTransLogByREFERDATE(String refer , String date) {
		if (getSize() > 0) {
			for (int i = 0; i < getSize(); i++) {
				TransLogData data = transLogData.get(i);
				if (!PAYUtils.isNullWithTrim(data.getRRN()) &&
						!PAYUtils.isNullWithTrim(data.getLocalDate())) {
					if (data.getRRN().equals("" + refer) &&
							data.getLocalDate().equals("" + date)) {
						return data;
					}
				}
			}
		}
		return null;
	}


	/**
	 * guardar el resultado del script del emisor
	 * @return
	 */
	public static boolean saveScriptResult(TransLogData data) {
		try {
			PAYUtils.object2File(data, TMConfig.getRootFilePath()+ ScriptPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * guardar registro de transacciones de reversión
	 * @return
	 */
	public static boolean saveReversal(TransLogData data ) {
		try {
			PAYUtils.object2File(data, TMConfig.getRootFilePath()+ ReversalPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * obtener el registro de transacciones de reversión
	 * @return
	 */
	public static TransLogData getReversal() {
		try {
			return (TransLogData) PAYUtils.file2Object(TMConfig.getRootFilePath() + ReversalPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * obtener el registro de transacciones del script del emisor
	 * @return
	 */
	public static TransLogData getScriptResult() {
		try {
			return (TransLogData) PAYUtils.file2Object(TMConfig.getRootFilePath()+ ScriptPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * claro registro de transacciones de reversión
	 * @return
	 */
	public static boolean clearReveral() {
		File file = new File(TMConfig.getRootFilePath()+ ReversalPath);
		if (file.exists() && file.isFile()) {
			file.delete();
			return false;
		} else {
			return true;
		}
	}

	/**
	 * resultado claro del script del emisor
	 * @return
	 */
	public static boolean clearScriptResult() {
		File file = new File(TMConfig.getRootFilePath()+ ScriptPath);
		if (file.exists() && file.isFile()) {
			file.delete();
			return false;
		} else {
			return true;
		}
	}
}
package yjh.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


/**
 * Simple Pool
 * 
 * @author yangjh
 * 
 */
public class DBConn {

	public int inUsed = 0;
	private final int maxConnCount = 3;
	private static DBConn uniqueConn;
	private final String filePath = "D:\\toshiba\\workspace\\ideaGroovy\\config\\dbConfiguration.xml";
//	private final String filePath = "config/dbConfiguration.xml";
	public static String uid;
	public static String dbType;
	public static String dbHost;
	public static String dbUsername;
	public static String dbPasswd;
	public static String dbPort;
	public static String defaultDB;
	public static String sid;
	public static String serviceName;
	public static String jdbcURL;
	public static String dbDriver;
	private final List<Connection> freeConnections = new ArrayList<Connection>();

	public DBConn(String jdbcStr,String dbType) throws Exception {

		if (dbType.equals("TERADATA")) {
			dbDriver = "com.teradata.jdbc.TeraDriver";
			jdbcURL = "jdbc:teradata://"
					+ dbHost
					+ "/CLIENT_CHARSET=EUC_CN,TMODE=TERA,CHARSET=ASCII,DATABASE="
					+ defaultDB;
		} else if (dbType.equalsIgnoreCase("ORACLE")) {
			dbDriver = "oracle.jdbc.driver.OracleDriver";
			jdbcURL = jdbcStr;
			System.out.println(jdbcURL);
		}
		Class.forName(dbDriver).newInstance();
		for (int i = 0; i < maxConnCount; i++) {
			freeConnections.add(DriverManager.getConnection(jdbcURL,
					dbUsername, dbPasswd));
		}

		System.out.println("conn "+dbType+":"+uid+ " success...");

	}
	private DBConn(String myuid) throws Exception {
		// PARSE XML, and make CONNECT STRING;
		Document document = Jsoup.parse(new File(filePath), "GBK");
		Elements dbConnectionInfos = document.select("dbConnectionInfo");
		for (Element dbConnectionInfo : dbConnectionInfos) {
			String unitUid = dbConnectionInfo.select("uid").text();
			if (unitUid.equals(myuid)) {
				uid = unitUid;
				dbType = dbConnectionInfo.select("dbType    ").text();
				dbHost = dbConnectionInfo.select("dbHost    ").text();
				dbUsername = dbConnectionInfo.select("dbUsername").text();
				dbPasswd = dbConnectionInfo.select("dbPasswd  ").text();
				dbPort = dbConnectionInfo.select("dbPort    ").text();
				defaultDB = dbConnectionInfo.select("defaultDB ").text();
				sid = dbConnectionInfo.select("sid").text();
				serviceName = dbConnectionInfo.select("serviceName").text();
			}
		}
		if (uid == null) {
			throw new Exception(
					"��yjh.helper.DBConn.java��cannot find dbConnectionInfo of uid:" + myuid);
		}
		if (dbType.equals("TERADATA")) {
			dbDriver = "com.teradata.jdbc.TeraDriver";
			jdbcURL = "jdbc:teradata://"
					+ dbHost
					+ "/CLIENT_CHARSET=EUC_CN,TMODE=TERA,CHARSET=ASCII,DATABASE="
					+ defaultDB;
		} else if (dbType.equalsIgnoreCase("ORACLE")) {
			dbDriver = "oracle.jdbc.driver.OracleDriver";
			jdbcURL = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + sid;
			System.out.println(jdbcURL);
		}else if(dbType.equalsIgnoreCase("ORACLE_SN")){
			dbDriver = "oracle.jdbc.driver.OracleDriver";
			jdbcURL = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + "/" + serviceName;
		}
		Class.forName(dbDriver).newInstance();
		for (int i = 0; i < maxConnCount; i++) {
			freeConnections.add(DriverManager.getConnection(jdbcURL,
					dbUsername, dbPasswd));
		}
		System.out.println("conn "+dbType+":"+uid+ " success...");

	}

	public synchronized Connection getSession() throws Exception {
		Connection conn = null;
		if (this.freeConnections.size() > 0) { // �����ﻹ�ж��Session����ȡ
			conn = this.freeConnections.get(0); // �Ӷ�����ȡ����һ��
			this.freeConnections.remove(0);
//			System.out.println("[get]Exist " + this.freeConnections.size()
//					+ " session");
		} else {
			// System.out.println("used all of session\n");
			throw new Exception("used all of session\n");
		}
		this.inUsed++;
		return conn;
	}

	public synchronized void freeSession(Connection conn) {
		this.freeConnections.add(conn);
		this.inUsed--;
//		System.out.println("[free]Now exist" + freeConnections.size());
	}

	public static ArrayList<Object[]> readDbResult(ResultSet rs) {
		if (rs == null) {
			return null;
		}
		ArrayList<Object[]> temp = new ArrayList<Object[]>();
		Object[] tempo;
		int columnCount;
		try {
			columnCount = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				tempo = new Object[columnCount];
				for (int i = 0; i < columnCount; i++) {
                    int type =rs.getMetaData().getColumnType(1);

                    if (type == 2005 || type == 12) {
							try {
								tempo[i] = rs.getString(i + 1).replaceAll("\r","\n");
							}catch (Exception e) {
								tempo[i] = "?";

							}
                    } else {
                        tempo[i] = rs.getObject(i + 1);
                    }

				}
				temp.add(tempo);
				tempo = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (temp.size() == 0) {
			return null;
		}
		return temp;
	}

	public static Object readDbResultObject(ResultSet rs) {
		if (rs == null) {
			return null;
		}
		
		Object tempo =null;
		try {
			rs.next();
            int type =rs.getMetaData().getColumnType(1);

			if (type == 2005 || type == 12) {
				tempo = rs.getString(1).replaceAll("\r","\n");
			} else {
				tempo = rs.getObject(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return tempo;
	}

	public Object queryForObject(String sql) {
		Connection session = null;
		try {
			session = uniqueConn.getSession();
			PreparedStatement pstmt = session.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			return readDbResultObject(rs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			uniqueConn.freeSession(session);
		}
		return null;

	}

	public ArrayList<Object[]> queryForList(String sql) {
		Connection session = null;
//		System.out.println("***********\n"+sql+"***********\n");
		try {
			session = uniqueConn.getSession();
			PreparedStatement pstmt = session.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			return readDbResult(rs);
		} catch (Exception e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		} finally {
			uniqueConn.freeSession(session);
		}
		return null;

	}

	public static synchronized DBConn getInstance(String uid) throws Exception {
		if (uniqueConn == null) {
			uniqueConn = new DBConn(uid);
			return uniqueConn;
		}
		return uniqueConn;
	}

	public static void main(String[] argsvStrings) {

//        System.out.println(System.getProperty("user.dir"));
		System.out.println("hello");
		try {
			DBConn uniConn = getInstance("db1");

			System.out.println(uniConn.queryForObject("show table s.t1"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

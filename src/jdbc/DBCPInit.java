package jdbc;

import java.sql.DriverManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/*
 * 1. 실제 커넥션을 생성할 ConnectionFactory를 생성
 * 2. 커넥션 풀로 사용할 PoolableConnection을 생성하는 PoolableConnectionFactory를 생성
 * 3. 커넥션 풀의 설정 정보를 생성
 * 4. 커넥션 풀을 사용할 JDBC 드라이버를 등록
 */
public class DBCPInit extends HttpServlet {

	@Override
	public void init() throws ServletException {
		loadJDBCDriver();
		initConnectionPool();
	}
	
	public void loadJDBCDriver() {
		try {
			Class.forName("com.mysql.jdbc.Driver"); /* JDBC 드라이버 로딩 */
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Fail to load JDBC Driver", e);
		}
	}
	
	public void initConnectionPool() {
		try {
			String jdbcUrl = "jdbc:mysql://localhost:3306/guestbook?useUnicode=true&characterEncoding=utf8";
			String username = "kch3338";
			String password = "kch3338";
			
			/* 커넥션 풀이 새로운 커넥션을 생성할 때 사용할 커넥션 팩토리 생성 */
			ConnectionFactory connFactory = new DriverManagerConnectionFactory(jdbcUrl, username, password);
			
			/* DBCP는 커넥션 풀에 커넥션을 보관할 때 PoolableConnection을 사용 */
			/* 내부적으로 실제 커넥션을 담고 있으며, 커넥션 풀을 관리하는 데 필요한 기능을 추가로 제공 */
			/* ex) close() 메서드를 실행하면 실제 커넥션을 종료하지 않고 풀에 커넥션을 반환 */
			PoolableConnectionFactory poolableConnFactory = new PoolableConnectionFactory(connFactory, null);
			poolableConnFactory.setValidationQuery("SELECT 1"); /* 커넥션이 유효한지 여부를 검사할 때 사용할 쿼리를 지정 */
			
			/* 커넥션 풀의 설정 정보를 생성 */
			GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
			poolConfig.setTimeBetweenEvictionRunsMillis(1000L * 60L * 5L); /* 유휴 커넥션 검사 주기 */
			poolConfig.setTestWhileIdle(true); /* 풀에 보관 중인 커넥션이 유효한지 검사할지 여부 */
			poolConfig.setMinIdle(4); /* 커넥션 최소 개수 */
			poolConfig.setMaxTotal(50); /* 커넥션 최대 개수 */
			
			/* 커넥션 풀 생성 (PoolableConnection을 생성할 때 사용할 팩토리, 커넥션 풀 설정)*/
			GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnFactory, poolConfig);
			poolableConnFactory.setPool(connectionPool); /* PoolableConnectionFactory에도 생성한 커넥션 풀을 연결 */
			
			Class.forName("org.apache.commons.dbcp2.PoolingDriver"); /* 커넥션 풀을 제공하는 JDBC 드라이버를 등록 */
			PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
			driver.registerPool("guestbook", connectionPool); /* "guestbook"을 커넥션 풀 이름으로 지정 */
			/* 이 경우 프로그램에서 사용하는 JDBC URL은 "jdbc:apache:commons:dbcp:guestbook"가 된다. */			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}

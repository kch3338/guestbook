package guestbook.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import guestbook.model.Message;
import jdbc.JdbcUtil;

public class MessageDao {
	
	/* 싱글톤 패턴 */
	private static MessageDao messageDao = new MessageDao(); /* 유일한 객체를 정적 필드에 저장 */
	
	public static MessageDao getInstance() { /* 유일한 객체에 접근할 수 있는 정적 메서드 정의 */
		return messageDao;
	}
	
	private MessageDao() { /* 생성자를 private로 설정해서 외부에서 접근하지 못함 (생성자를 이용해서 객체 생성 X) */
		
	}
	
	/*
	 * DAO에서 Connection에 접근하는 방식
	 * 1. DAO 클래스의 메서드에서 직접 Connection을 생성
	 * 2. DAO 객체를 생성할 때 생성자로 Connection을 전달받기
	 * 3. DAO 클래스의 메서드 파라미터로 Connection을 전달받기
	 * 
	 * 이 중에서 3번의 방식을 사용
	 */
	
	public int insert(Connection conn, Message message) throws SQLException {
		PreparedStatement pstmt = null;
		
		try {
			pstmt = conn.prepareStatement("INSERT INTO guestbook_message (guest_name, password, message) VALUES (?, ?, ?)");
			pstmt.setString(1, message.getGuestName());
			pstmt.setString(2, message.getPassword());
			pstmt.setString(3, message.getMessage());
			
			return pstmt.executeUpdate();
		} finally {
			JdbcUtil.close(pstmt);
		}		
	}
	
	public Message select(Connection conn, int messageId) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = conn.prepareStatement("SELECT * FROM guestbook_message WHERE messageId = ?");
			pstmt.setInt(1, messageId);			
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				return makeMessageFromResultSet(rs);
			} else {
				return null;
			}
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
	}
	
	public int selectCount(Connection conn) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT COUNT(*) FROM guestbook_message");
			rs.next();
			
			return rs.getInt(1);
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(stmt);
		}
	}
	
	public List<Message> selectList(Connection conn, int firstRow, int endRow) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = conn.prepareStatement("SELECT * FROM guestbook_message ORDER BY message_id DESC LIMIT ?, ?");
			pstmt.setInt(1, firstRow - 1); /* 시작행번호는 0부터 시작 */
			pstmt.setInt(2, endRow - firstRow + 1); /* 읽어올개수 */
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				List<Message> messageList = new ArrayList<Message>();
				
				do {
					messageList.add(makeMessageFromResultSet(rs));
				} while (rs.next());
				
				return messageList;
			} else {
				return Collections.emptyList();
			}
		} finally {
			JdbcUtil.close(rs);
			JdbcUtil.close(pstmt);
		}
	}
	
	public int delete(Connection conn, int messageId) throws SQLException {
		PreparedStatement pstmt = null;
		
		try {
			pstmt = conn.prepareStatement("DELETE FROM guestbook_message WHERE message_id = ?");
			pstmt.setInt(1, messageId);
			
			return pstmt.executeUpdate();
		} finally {
			JdbcUtil.close(pstmt);
		}
	}
	
	private Message makeMessageFromResultSet(ResultSet rs) throws SQLException {
		Message message = new Message();
		message.setId(rs.getInt("message_id"));
		message.setGuestName(rs.getString("guest_name"));
		message.setPassword(rs.getString("password"));
		message.setMessage(rs.getString("message"));
		
		return message;
	}
	
}

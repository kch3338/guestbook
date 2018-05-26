package guestbook.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import guestbook.dao.MessageDao;
import guestbook.model.Message;
import jdbc.JdbcUtil;
import jdbc.connection.ConnectionProvider;

public class GetMessageListService {
	
	private static GetMessageListService instance = new GetMessageListService();
	
	public static GetMessageListService getInstance() {
		return instance;
	}
	
	private GetMessageListService() {
		
	}
	
	private static final int MESSAGE_COUNT_PER_PAGE = 3; /* 페이지 당 메시지 출력 개수 설정 */
	
	public MessageListView getMessageList(int pageNumber) {
		Connection conn = null;
		int currentPageNumber = pageNumber; /* 요청 페이지 번호 설정 */
		
		try {
			conn = ConnectionProvider.getConnection();
			MessageDao messageDao = MessageDao.getInstance();
			
			int messageTotalCount = messageDao.selectCount(conn); /* 전체 메시지 개수 구함 */
			
			List<Message> messageList = null;
			int firstRow = 0;
			int endRow = 0;
			
			if (messageTotalCount > 0) {
				/* if (1page) */
				firstRow = (pageNumber - 1) * MESSAGE_COUNT_PER_PAGE + 1; /* firstRow = 1 */  
				endRow = firstRow + MESSAGE_COUNT_PER_PAGE - 1; /* endRow = 3 */
				messageList = messageDao.selectList(conn, firstRow, endRow); /* 요청 페이지 번호에 해당하는 메시지 목록 구함 */
			} else {
				currentPageNumber = 0;
				messageList = Collections.emptyList();
			}
			
			return new MessageListView(messageList, messageTotalCount, currentPageNumber, MESSAGE_COUNT_PER_PAGE, firstRow, endRow);
		} catch (SQLException e) {
			throw new ServiceException("목록 구하기 실패: " + e.getMessage(), e);
		} finally {
			JdbcUtil.close(conn);
		}
	}

}

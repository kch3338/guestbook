CREATE TABLE guestbook_message (
	message_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	guest_name VARCHAR(50) NOT NULL,
	password VARCHAR(10) NOT NULL,
	message TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8
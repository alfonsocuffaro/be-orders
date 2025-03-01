CREATE TABLE BE_ORDER
(
	ID       BIGINT       GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	AMOUNT   NUMBER       NOT NULL DEFAULT 0,
	OWNER    VARCHAR(256) NOT NULL,
	PRODUCT  VARCHAR(256) NOT NULL,
	QUANTITY NUMBER       NOT NULL DEFAULT 0
);

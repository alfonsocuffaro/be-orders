package com.example.beorders;

public enum Role {
	
	ADMIN, // for users with administrative privileges
	ORDER_OWNER,     // non admin users that have placed orders in the system
	NON_ORDER_OWNER, // non admin users that haven't placed yet orders in the system
}

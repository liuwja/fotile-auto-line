package com.fotile;

import com.datasweep.compatibility.client.Order;
import com.datasweep.compatibility.client.Unit;
import com.fotile.proxy.ServerImplProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AutoLineServiceApplicationTests {

	@Autowired
	ServerImplProxy proxy;

	@Test
	void contextLoads() {
	}

	@Test
	void getWorkOrderByName(){
		Order order = proxy.getWorkOrderByName("600405437");
		System.out.println(order);
	}
}

package com.fotile;

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
	void test(){
		Unit unit = proxy.getUnitBySerialNumber("1001000200040L012007200001");
		System.out.println(unit);
	}
}

package com.fotile;

import com.datasweep.compatibility.client.Order;
import com.datasweep.compatibility.client.OrderItem;
import com.datasweep.compatibility.client.Shift;
import com.datasweep.compatibility.manager.ServerImpl;
import com.datasweep.compatibility.ui.Time;
import com.fotile.proxy.ServerImplProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Vector;

@SpringBootTest
class AutoLineServiceApplicationTests {

	@Autowired
	ServerImplProxy proxy;

	@Autowired
	ServerImpl server;

	@Test
	void contextLoads() {
	}

	@Test
	void getWorkOrderByName(){
		Order order = proxy.getWorkOrderByName("600405437");
		System.out.println(order);
	}
	@Test
	void getDbTimeTest(){
		Time time = proxy.getDbTime();
		System.err.println(time.formatShortDate());
	}
	@Test
	void orderItemTest(){
		Order order = proxy.getWorkOrderByName("600405465");
		List<OrderItem> orderItems = order.getOrderItems();
		for (OrderItem orderItem :orderItems){
			System.err.println(orderItem.getPlannedStartTime());
		}
	}
	@Test
	void ShiftTest(){
		List<Shift> shifts = proxy.getShiftListByProductionLine("L01");
		for (Shift s:shifts) {
			System.err.println(s);
		}
	}
	@Test
	void testOrderItem(){
		List<OrderItem> orderItems = proxy.getOrderItemByOrderKey(213109580);
		for (OrderItem orderItem:orderItems){
			System.err.println(orderItem);
		}
	}
}

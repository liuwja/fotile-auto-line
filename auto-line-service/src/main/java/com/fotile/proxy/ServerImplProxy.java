package com.fotile.proxy;

import com.datasweep.compatibility.client.*;
import com.datasweep.compatibility.manager.ServerImpl;

import java.util.List;

public class ServerImplProxy {
    private ServerImpl server;

    public ServerImplProxy(ServerImpl server) {
        this.server = server;
    }

    public Unit getUnitBySerialNumber(String serialNumber){
        Unit unit = null;
        try {
            UnitFilter unitFilter = new UnitFilter(this.server);
            unitFilter.forSerialNumberEqualTo(serialNumber);
            unitFilter.orderByCreationTime(false);      //创建时间倒序排序
            List<Unit> units = unitFilter.exec();
            if (units.size()<1){
                throw new RuntimeException("未找到主机条码："+serialNumber);
            }
            unit = units.get(0);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return unit;
    }

    public WorkCenter getWorkCenterByName(String workCenterName){
        WorkCenter workCenter= null;
        try {
            workCenter = (WorkCenter) this.server.getWorkCenterManager().getObject(workCenterName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return workCenter;
    }

    public Order getOrderByOrderNumber(String orderNumber){
        Order order = null;
        try {
            order = (Order) this.server.getOrderManager().getObject(orderNumber);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return order;
    }

    public ProductionLine getProductionLine(String productionLineName){
        ProductionLine productionLine = null;
        try {
            productionLine = (ProductionLine) this.server.getProductionLineManager().getObject(productionLineName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return productionLine;
    }
}

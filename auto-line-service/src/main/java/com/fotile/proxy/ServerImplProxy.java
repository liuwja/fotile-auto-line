package com.fotile.proxy;

import com.datasweep.compatibility.client.*;
import com.datasweep.compatibility.manager.ServerImpl;
import com.datasweep.compatibility.ui.Time;
import com.rockwell.transactiongrouping.UserTransaction;
import com.sun.org.apache.regexp.internal.RE;

import java.util.List;

public class ServerImplProxy {
    private ServerImpl server;

    public ServerImplProxy(ServerImpl server) {
        this.server = server;
    }

    /**
     * 根据主机条码查找unit
     * @param serialNumber      主机条码
     * @return  unit
     */
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

    /**
     * 根据工作中心编号查找工作中心
     * @param workCenterName  工作中心编号
     * @return workCenter
     */
    public WorkCenter getWorkCenterByName(String workCenterName){
        WorkCenter workCenter= null;
        try {
            workCenter = (WorkCenter) this.server.getWorkCenterManager().getObject(workCenterName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return workCenter;
    }

    public Order getWorkOrderByName(String orderNumber){
        Order order = null;
        try {
            order = (Order) this.server.getOrderManager().getObject(orderNumber);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return order;
    }

    public ProductionLine getProductionLineByName(String productionLineName){
        ProductionLine productionLine = null;
        try {
            productionLine = (ProductionLine) this.server.getProductionLineManager().getObject(productionLineName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return productionLine;
    }

    public UserTransaction getUserTransaction(){
        UserTransaction userTransaction = null;
        try {
            userTransaction =  new UserTransaction(this.server);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return userTransaction;
    }

    public List<Shift> getShiftListByProductionLine(String productionLineName){
        List<Shift> shifts = null;
        ShiftFilter shiftFilter = new ShiftFilter(this.server);
        shiftFilter.forUdaEqualTo("pl_number",productionLineName);
        try {
            shifts = shiftFilter.exec();
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return shifts;
    }

    /**
     * 获取数据库时间,获取步到则返回的是系统时间
     * @return
     */
    public Time getDbTime(){
        Time time = new Time();
        try {
            time =  server.getUtilityManager().getDBTime();
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 根据主机条条码查找工单号
     * @param serialNumber
     * @return
     */
    public String getOrderNumberByPrintRecord(String serialNumber){
        ATRow atRow =null;
        ATRowFilter atRowFilter = new ATRowFilter("PrintRecord",server);
        try {
            atRowFilter.forColumnNameEqualTo("barcode","serialNumber");
            atRowFilter.forColumnNameEqualTo("record_type","打印");
            atRowFilter.setMaxRows(1);
            List<ATRow> atRows = atRowFilter.exec();
            if (atRows==null||atRows.size()<1){
                throw new RuntimeException("主机条码："+serialNumber+"未打印");
            }
            atRow = atRows.get(0);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return (String) atRow.getValue("order_number");
    }

    public List<OrderItem> getOrderItemByOrderKey(long key){
        OrderItemFilter filter = new OrderItemFilter(server);
        return null;
    }
}

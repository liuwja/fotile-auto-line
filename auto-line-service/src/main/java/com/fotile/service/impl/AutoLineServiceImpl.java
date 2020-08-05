package com.fotile.service.impl;

import com.datasweep.compatibility.client.*;
import com.datasweep.compatibility.ui.Time;
import com.fotile.bean.Result;
import com.fotile.bean.TransferBean;
import com.fotile.proxy.ServerImplProxy;
import com.fotile.service.IAutoLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutoLineServiceImpl implements IAutoLineService {

    @Autowired
    ServerImplProxy proxy;
    @Override
    public Result binging(TransferBean bean) {
        String SerialNumber = bean.getSerialNumber();
        Unit unit = proxy.getUnitBySerialNumber(SerialNumber);
        if (unit==null){        //如果没有主机条码，则进行投产
            this.produce(bean);
        }
        return null;
    }

    @Override
    public Result produce(TransferBean bean) {
        String serialNumber = bean.getSerialNumber();
        String productionLineName = bean.getProductionLine();

        //获取主机条码
        Unit unit = proxy.getUnitBySerialNumber(serialNumber);
        if (unit != null) {
            throw new RuntimeException("该主机条码已经被使用：" + serialNumber);
        }
        //根据产线编号获取产线
        ProductionLine productionLine = proxy.getProductionLineByName(productionLineName);
        if (productionLine == null) {
            throw new RuntimeException("未找到产线：" + productionLineName);
        }
        //获取产线下的班次列表
        List<Shift> shifts = proxy.getShiftListByProductionLine(productionLineName);
        if (shifts==null||shifts.size()==0){
            throw new RuntimeException("未找到产线：" + productionLineName+"下的班次信息");
        }
        //根据主机条码打印记录获取工单号
        String orderNumber = proxy.getOrderNumberByPrintRecord(serialNumber);
        if (orderNumber==null||orderNumber.length()==0){
            throw new RuntimeException("主机条码：" + serialNumber+"未打印");
        }

        Order order = proxy.getWorkOrderByName(orderNumber);
        if (order==null){
            throw new RuntimeException("工单：" + orderNumber+"不存在");
        }

        List<OrderItem> orderItems = order.getOrderItems();
        for (OrderItem orderItem : orderItems){
            if (!orderItem.getUDA(3).equals("APS")){
                continue;
            }
        }
        return null;
    }
}

package com.fotile.service.impl;

import com.datasweep.compatibility.client.*;
import com.datasweep.compatibility.ui.Time;
import com.datasweep.plantops.common.constants.IBomItemConsumptionTypes;
import com.fotile.bean.PrintInfo;
import com.fotile.bean.Result;
import com.fotile.bean.TransferBean;
import com.fotile.constant.*;
import com.fotile.proxy.ServerImplProxy;
import com.fotile.service.IAutoLineService;
import com.fotile.util.DateTimeUtils;
import com.fotile.util.StringUtil;
import com.rockwell.transactiongrouping.UserTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Service
public class AutoLineServiceImpl implements IAutoLineService {

    @Value("${spring.application.name}:${server.port}")
    private String applicationName;
    @Autowired
    private ServerImplProxy proxy;

    @Override
    public Result binging(TransferBean bean) {
        UserTransaction userTransaction = proxy.getUserTransaction();
        Result result = new Result();
        String serialNumber = bean.getSerialNumber();
        List<String> partSerialNumbers = bean.getDefectCodes();
        try {
//            userTransaction.setTransactionTimeout(30);
            userTransaction.begin();
            Unit unit = proxy.getUnitBySerialNumber(serialNumber);
            if (unit == null) {        //如果没有主机条码，则进行投产
                result = this.produce(bean);
                if (result.getStatus() == 2) {
                    throw new RuntimeException("投产失败，原因" + result.getReason());
                }
            }
            unit = proxy.getUnitBySerialNumber(serialNumber);   //再查一遍主机条码
            String orderNumber = unit.getOrderNumber();
            WorkCenter workCenter = this.proxy.getWorkCenterByName(bean.getWorkCenterName());
            Route route = unit.getRoute();
            Step step = null;
            Vector<Step> steps = route.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                Step routeStep = steps.get(i);
                Vector<WorkCenter> workCenters = routeStep.getWorkCenters();
                for (int j = 0; j < workCenters.size(); j++) {
                    WorkCenter data = workCenters.get(j);
                    if (workCenter.equals(data)) {
                        step = routeStep;
                        break;
                    }
                }
            }
            if (step == null) {
                throw new RuntimeException("未找到" + bean.getWorkCenterName() + "工位对应的工序");
            }

            // 判断产品状态是否为已投产
            if (!IUnitStatus.STARTING.equals(unit
                    .getUDA(IUDADefinition.UDA_UNIT_STATUS))) {
                throwUnitStatusException(unit);
            }

            // 判断产品是否允许在当前工序上开始
            if (!validateUnitInPreviousQueue(unit, step)) {
                throwUnitStatusException(unit);
            }
            Response response = unit.startAtStepAtWorkCenter(null, step.getName(), workCenter.getName(), null, false);
            if (response.isError()) {
                throw new RuntimeException("在该工序启动失败");
            }

            ConsumptionSet consumptionSet = null;
            for (int j = 0; j < partSerialNumbers.size(); j++) {
                String partSerialNumber = partSerialNumbers.get(j);

                // 根据BOM消耗非关键件物料
                BillOfMaterials billOfMaterials = unit.getBillOfMaterials();
                if (StringUtil.isNull(partSerialNumber)) {
                    throw new RuntimeException("该物料为关键件，消耗物料时条码不允许为空");
                }
                // 非虚拟码
                if (!partSerialNumber.startsWith("FT_")) {
                    Unit materialUnit = this.proxy.getUnitBySerialNumber(partSerialNumber);
                    if (materialUnit == null) {
                        materialUnit = createStandAloneUnit(partSerialNumber, billOfMaterials);
                    }
                    BomItem bomItem = billOfMaterials.getBomItem(materialUnit.getPartNumber());
                    if (bomItem == null) {
                        throw new RuntimeException("该物料" + partSerialNumber + "无法在该产品上消耗，请检查BOM信息是否正确");
                    }
                    // 检查是否是关键件
                    Part materialBomItem = bomItem.getPart();
                    // 创建消耗
                    String consumptionType = materialBomItem
                            .getConsumptionType();
                    // 如果是关键件
                    if (IBomItemConsumptionTypes.CONSUMPTION_TYPE_QUANTITY
                            .equals(consumptionType)) {
                        throw new RuntimeException("该物料为非关键件");
                    }

                    consumptionSet = proxy.consumedMaterial(unit, bomItem,
                            materialUnit, consumptionSet, partSerialNumber);

                }

                // 判断是否有部件
                String sequenceNumber = proxy.getSequenceNumberByPartSerial(partSerialNumber);

                if (sequenceNumber != null) {
                    List<String> componentBindings = proxy.getPartSerialBySequenceNumber(sequenceNumber);
                    for (int i = 0; i < componentBindings.size(); i++) {
                        String materialSerialNumber = componentBindings.get(i);
                        if (materialSerialNumber.equals(partSerialNumber)) {
                            continue;
                        }
                        if (materialSerialNumber.startsWith("FT_")) {
                            continue;
                        }
                        Unit material = proxy.getUnitBySerialNumber(materialSerialNumber);
                        if (material == null) {
                            throw new RuntimeException("无法找到该关键件绑定的部装关键件，条码为" + materialSerialNumber);
                        }

                        BomItem bomItem = billOfMaterials.getBomItem(material.getPartNumber());
                        if (bomItem == null) {
                            throw new RuntimeException("该物料无法在该产品上消耗，请检查BOM信息是否正确");
                        }
                        consumptionSet = proxy.consumedMaterial(unit, bomItem, material, consumptionSet, materialSerialNumber);
                    }
                }
            }
            if (consumptionSet != null) {
                response = consumptionSet.save(null, false, false, false, null, null);
                if (response.isError()) {
                    throw new RuntimeException("消耗物料失败" + response.getFirstErrorMessage());
                }
            }
            // 判断是否为最后一个绑定工序,若是，则检查是否所有关键件绑全
            Step lastBindingStep = proxy.getLastBindingStep(unit);
            if (lastBindingStep != null && step.getKey() == lastBindingStep.getKey()) {
                BillOfMaterials bom = unit.getBillOfMaterials();
                Vector<BomItem> bomItems = bom.getBomItems();
                for (int i = 0; i < bomItems.size(); i++) {
                    BomItem bomItem = bomItems.get(i);
                    Part part = bomItem.getPart();
                    Lot originalLot = proxy.getLotByName(orderNumber);
                    String controlType = "";
                    BillOfMaterials lotBom = originalLot.getBillOfMaterials();
                    Vector lotBomItems = lotBom.getBomItems();
                    for (int j = 0; j < lotBomItems.size(); j++) {
                        BomItem lotBomItem = (BomItem) lotBomItems.get(j);
                        if (part.getPartNumber().equals(lotBomItem.getBomItemName())) {
                            if ("10".equals(lotBomItem.getUDA(1))) {
                                controlType = "关键件";
                            } else if ("20".equals(lotBomItem.getUDA(1))) {
                                controlType = "附件";
                            } else {
                                throw new RuntimeException(lotBomItem.getBomKey() + "对应的BOM的关键件类型字段不允许为空");
                            }
                        }
                    }
                    if ("关键件".equals(controlType)) {
                        BigDecimal preciseQuantity = bomItem.getPreciseQuantity();
                        BigDecimal preciseQuantityConsumed = bomItem.getPreciseQuantityConsumed();
                        if (preciseQuantity.compareTo(preciseQuantityConsumed) != 0) {
                            throw new RuntimeException("当前产品缺少物料绑定，物料编号为:"
                                    + part.getPartNumber() + ";物料名称为："
                                    + part.getDescription() + ";需求数量为："
                                    + preciseQuantity.longValue());
                        }
                    }
                }
            }
            response = unit.completeAtStepAtWorkCenter(step.getName(), workCenter.getName(), ICompleteReason.NORMAL, false);
            if (response.isError()) {
                throw new RuntimeException("完成当前工序失败，原因：" + response.getFirstErrorMessage());
            }
            result.setStatus(1);
            result.setReason(applicationName + "处理成功");
            userTransaction.commit();
        } catch (Exception e) {
            result.setStatus(2);
            result.setReason(applicationName + "处理失败" + e.getMessage());
            e.printStackTrace();
            userTransaction.rollback();
        }
        return result;
    }

    private boolean validateUnitInPreviousQueue(Unit unit, Step step) {
        try {
            Queue queue = step.getQueue();
            if (queue.getKey() != unit.getQueueKey()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Result produce(TransferBean bean) {
        String serialNumber = bean.getSerialNumber();
        String productionLineName = bean.getProductionLine();
        Result result = new Result();
        try {
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
            if (shifts == null || shifts.size() == 0) {
                throw new RuntimeException("未找到产线：" + productionLineName + "下的班次信息");
            }
            //根据主机条码打印记录获取工单号
            String orderNumber = proxy.getOrderNumberByPrintRecord(serialNumber);
            if (orderNumber == null || orderNumber.length() == 0) {
                throw new RuntimeException("主机条码：" + serialNumber + "未打印");
            }

            Order order = proxy.getWorkOrderByName(orderNumber);
            if (order == null) {
                throw new RuntimeException("工单：" + orderNumber + "不存在");
            }

            String orderItemNumber = proxy.geOrderItemNumber(shifts, proxy.getDbTime(), order);         //获取班次

            OrderItem orderItem = order.getOrderItem(orderItemNumber);      //根据排程号获取排程
            if (orderItem == null) {
                throw new RuntimeException("无法找到排程,请检查产线、排程日期、班次是否符合");
            }

            Lot lot = proxy.getLotByOrderAndOrderItem(order, orderItem);
            Response response;
            if (lot.getLotType() == Lot.UNSERIALIZED) {
                response = lot.serialize();
                if (response.isError()) {
                    throw new RuntimeException("产品批次序列化失败");
                }
            }
            response = lot.addUnits(1);
            if (response.isError()) {
                throw new RuntimeException("投产失败，无法生成产品");
            }
            //从lot的相应结果中获取新增的unit
            Vector units = (Vector) (response.getResult());
            unit = (Unit) units.firstElement();


            Lot originalLot = proxy.getLotByName(orderNumber);

            unit = proxy.applyBomForUnit(unit, originalLot, serialNumber);
            unit.refresh();
            proxy.updateUnitProperty(serialNumber, orderItem, unit.getCreationTime(),
                    true, null);
            // 将产品在当前工序上开始
            Route route = proxy.getRouteByRouteName(orderItem.getPlannedRoute());
            Queue entryQueue = route.getEntryQueue();
            Step step = (Step) entryQueue.getOutgoingArcs().firstElement()
                    .getTo();
            if (bean.getStepMark() == 2) {
                if (!step.getDescription().contains("绑") && !step.getDescription().contains("投")) {
                    throw new RuntimeException("该工艺路径不允许进行投产,请检查自动化线程序");
                }
            } else if (bean.getStepMark() == 4) {
                if (!step.getDescription().contains("投")) {
                    throw new RuntimeException("该工艺路径不允许进行投产,请检查自动化线程序");
                }
            } else {
                throw new RuntimeException("该工艺路径不允许进行投产,请检查自动化线程序");
            }
            WorkCenter workCenter = getWorkCenterByLineAndStep(productionLine,
                    step);
            if (workCenter == null) {
                throw new RuntimeException("无法找到产线" + productionLineName + "在工序"
                        + step.getDescription() + "上配置的工作中心");
            }
            if (bean.getStepMark() == 4) {
                response = unit.startAtStepAtWorkCenter(
                        null, step.getName(), workCenter.getName(), null, false);
                if (response.isError()) {
                    throw new RuntimeException("投产开始失败");
                }
                response = unit.completeAtStepAtWorkCenter(step.getName(), workCenter.getName(), ICompleteReason.NORMAL, false);
                if (response.isError()) {
                    throw new RuntimeException("投产结束失败");
                }
                int TotalProductQty = getTotalProductQty(order);
                if (TotalProductQty == 1) {
                    String orderType = order.getDescription();      //工单类型
                    if (orderType.equals("ZF01") || orderType.equals("ZF06")
                            || orderType.equals("ZF09") || orderType.equals("ZF10")
                            || orderType.equals("ZF11")) {
                        this.transferWorkOrderFlag(orderNumber, orderType, "1");        //投产信息回调SAP
                    }
                    sendOrderStartToWMS(proxy, orderItem, order);       //记录WMS工单开工信息
                }
            }
            result.setStatus(1);
        } catch (Exception e) {
            result.setStatus(2);
            result.setReason(e.getMessage());
        }
        return result;
    }

    @Override
    public Result quantityValidation(TransferBean bean) {
        UserTransaction userTransaction = proxy.getUserTransaction();
        String serialNumber = bean.getSerialNumber();
        String workCenterName = bean.getWorkCenterName();
        List<String> defectCodes = bean.getDefectCodes();
        boolean isPassed = bean.isPass();
        Result result = new Result();
        try {
            userTransaction.begin();
            Unit unit = this.proxy.getUnitBySerialNumber(serialNumber);
            if (unit == null) {
                throw new RuntimeException("该产品尚未投产");
            }
            Step step = null;
            WorkCenter workCenter = proxy.getWorkCenterByName(workCenterName);
            if (workCenter == null) {
                throw new RuntimeException("无法找到工作中心" + workCenterName);
            }

            Route route = unit.getRoute();
            if (route == null) {
                throw new RuntimeException("无法找到工艺路径");
            }
            Vector<Step> steps = route.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                Step routeStep = steps.get(i);
                Vector<WorkCenter> workCenters = routeStep.getWorkCenters();
                for (int j = 0; j < workCenters.size(); j++) {
                    WorkCenter data = workCenters.get(j);
                    if (workCenter.equals(data)) {
                        step = routeStep;
                        break;
                    }
                }
            }

            if (step == null) {
                throw new RuntimeException("未找到" + workCenterName + "工位对应的工序");
            }

            if (!isPassed) {
                if (defectCodes == null || defectCodes.size() == 0) {
                    throw new RuntimeException("不良判定需要传入不良代码");
                }
            }

            // 检查产品状态
            String unitStatus = unit.getUDA(IUDADefinition.UDA_UNIT_STATUS);
            String unitState = unit.getUDA(IUDADefinition.UDA_UNIT_STATE);

            //这几行代码好像没什么卵用
//            String orderItemName = unit.getOrderItem();
//            Order order = unit.getOrder();
//            OrderItem orderItem = order.getOrderItem(orderItemName);
//            UIAssembleOrder uiOrder = new UIAssembleOrder(order, orderItem);
//            uiOrder.getFirstValidationStep();

            if (!unitState.equals(IUnitState.NORMAL)) {
                throwUnitStatusException(unit);
            }

            if (IUnitStatus.REPAIRED.equals(unitStatus)) {
                if (!unit.getReworkFlag()) {
                    throw new RuntimeException("该产品需要先通过IPQC审核");
                }
            } else {
                if (!IUnitStatus.STARTING.equals(unitStatus)) {
                    throw new Exception("该产品已经下线");
                }
            }
            if (!unitState.equals(IUnitState.NORMAL)) {
                throwUnitStatusException(unit);
            }

            if (IUnitStatus.REPAIRED.equals(unitStatus)) {
                if (!unit.getReworkFlag()) {
                    throw new RuntimeException("该产品需要先通过IPQC审核");
                }
            } else {
                if (!IUnitStatus.STARTING.equals(unitStatus)) {
                    throw new RuntimeException("该产品已经下线");
                }
            }
            if (unit.getQueueKey() != step.getQueue().getKey()) {
                throwUnitStatusException(unit);
            }
            Part part = unit.getPart();
            String productCategory = part.getUDA(IUDADefinition.UDA_PART_CATEGORY);
            // 在当前工序上开始
            Response response = unit.startAtStepAtWorkCenter(null, step.getName(), workCenter.getName(), null, false);
            if (response.isError()) {
                throw new Exception("在工序启动失败");
            }
            // 创建不良记录
            String stepName = step.getOperation().getDescription();
            String testDefinitionName = this.proxy.getTestDefinitionName(productCategory, stepName);
            if (StringUtil.isNull(testDefinitionName)) {
                throw new Exception("工序" + stepName + "未设置检测项目");
            }
            TestInstance testInstance = unit.createTestInstance(testDefinitionName);
            testInstance.setTestPassed(isPassed);
            testInstance.setTestValid(true);

            if (!isPassed) {
                // 更新产品描述
                unit.setUDA(IUnitState.TESTED_FAILED, IUDADefinition.UDA_UNIT_STATE);
                testInstance.setLocation(ITestDefinitionType.ON_LINE);
                for (int i = 0; i < defectCodes.size(); i++) {
                    DefectRepairEntry defectRepairEntry = testInstance.createDefectRepairEntry();
                    defectRepairEntry.setDefectCode(defectCodes.get(i));
                    ATRow testCodeDefintion = proxy.getUITestCodeDefinitionByCode(defectCodes.get(i));
                    if (testCodeDefintion == null) {
                        throw new RuntimeException("无法获得该不良代码");
                    }
                    String type = (String) testCodeDefintion.getValue(IATConstants.AT_COLUMN_CONFIGURATION_TYPE);
                    if (!"不良现象".equals(type)) {
                        throw new RuntimeException("检验不良代码类型只能为不良现象，不良代码" + defectCodes.get(i) + "类型为：" + type);
                    }
                    defectRepairEntry.setDefectComment((String) testCodeDefintion.getValue(IATConstants.AT_COLUMN_CONFIGURATION_TEST_NAME));
                    defectRepairEntry.setDefectRouteStepName(step.getOperationName());
                    defectRepairEntry.setDefectRouteName(route.getName());
                    defectRepairEntry.setDefectLocation(step.getOperationName());
                    defectRepairEntry.setUDT(proxy.getDbTime(), IUDADefinition.UDT_DEFECT_AND_REPAIR_ENTRY_DEFECT_TIME);
//
//                    // 判断某一不良是否发生三次   这部分代码好像并没有什么卵用
//                    String sql = " select count(*) from unit u,test_instance ti, defect_repair_entry dre "
//                            + " where 1=1 "
//                            + " and u.unit_key = ti.object_key "
//                            + " and ti.object_key = dre.test_instance_key "
//                            + " and ti.route_key = "
//                            + route.getKey()
//                            + " and ti.route_step_name = '"
//                            + step.getName()
//                            + "' "
//                            + " and trunc(dre.creation_time) = trunc(sysdate) "
//                            + " and dre.defect_code = '"
//                            + defectCodes.get(i)
//                            + "' ";
//
//                    Vector<String[]> datas = proxy.getArrayDataFromActive(sql, 0);
//                    if (datas != null && datas.size() > 0) {
//                        int count = Integer.parseInt(datas.firstElement()[0]);
//                        if (count == 3) {
//                            ArrayList<String> users = new ArrayList<>();
//                            users.add("E37275");
//                        }
//                    }
                }
                testInstance.setTestValid(false);
            }
            response = testInstance.save(null, null);
            if (response.isError()) {
                throw new RuntimeException("保存不良信息失败,原因:" + response.getFirstErrorMessage());
            }
            unit.setUDA(IUnitStatus.STARTING, IUDADefinition.UDA_UNIT_STATUS);
            unit.setReworkFlag(false);
            response = unit.save();
            if (response.isError()) {
                throw new RuntimeException("更新产品描述为" + unitState + "失败，产品原描述为" + unit.getUDA(IUDADefinition.UDA_UNIT_STATE));
            }
            response = unit.completeAtStepAtWorkCenter(step.getName(), workCenter.getName(), ICompleteReason.NORMAL, false);
            if (response.isError()) {
                throw new RuntimeException("在工序启动失败：原因：" + response.getFirstErrorMessage());
            }
            result.setStatus(1);//处理成功
            result.setReason(applicationName + "处理成功");//处理成功
            userTransaction.commit();
        } catch (Exception e) {
            result.setReason(applicationName + "处理失败，原因" + e.getMessage());
            result.setStatus(2);
            userTransaction.rollback();
        }
        return result;
    }

    @Override
    public Result packageDownLine(TransferBean bean) {
        Result result = new Result();
        UserTransaction userTransaction = this.proxy.getUserTransaction();
        try {
            userTransaction.begin();
            String serialNumber = bean.getSerialNumber();
            String workCenterName = bean.getWorkCenterName();
            //是否最后一道工序标识
            boolean flag = false;
            Unit unit = proxy.getUnitBySerialNumber(serialNumber);

            if (unit == null) {
                throw new RuntimeException("该产品尚未投产");
            }

            String unitState = unit.getUDA(IUDADefinition.UDA_UNIT_STATE);
            String unitStatus = unit.getUDA(IUDADefinition.UDA_UNIT_STATUS);
            if (!(IUnitState.NORMAL.equals(unitState) && IUnitStatus.STARTING.equals(unitStatus))) {
                throwUnitStatusException(unit);
            }

            Step step = null;
            WorkCenter workCenter = proxy.getWorkCenterByName(workCenterName);
            if (workCenter==null){
                throw new RuntimeException("工作中心"+workCenterName+"不存在");
            }

            Route route = unit.getRoute();
            Vector<Step> steps = route.getSteps();
            for (int i = 0; i < steps.size(); i++) {
                Step routeStep = steps.get(i);
                Vector<WorkCenter> workCenters = routeStep.getWorkCenters();
                for (int j = 0; j < workCenters.size(); j++) {
                    WorkCenter data = workCenters.get(j);
                    if (workCenter.equals(data)) {
                        step = routeStep;
                        break;
                    }
                }
            }

            if (step == null) {
                throw new RuntimeException("未找到" + workCenterName + "工位对应的工序");
            }

            if (unit.getQueueKey() != step.getQueue().getKey()) {
                throwUnitStatusException(unit);
            }
            // 检查工单是否是今天的
            Order order = unit.getOrder();
            OrderItem orderItem = order.getOrderItem(unit.getOrderItem());
            Time orderTime = orderItem.getPlannedStartTime();
            String shipTime = DateTimeUtils.formatDate(order.getUDT(IUDADefinition.UDT_ORDER_PUBLISH_TIME), IDateFormat.TIME_STANDARD);
            Time currentTime = proxy.getDbTime();
            // 若工单日期大于当前日期
            if ((orderTime.compareTo(currentTime) > 0)) {
                throw new RuntimeException("非今天的产品不允许下线,工单日期为:" + orderTime.toString() + ",今天日期为:" + currentTime.toString());
            }
            Response response = unit.startAtStepAtWorkCenter(null, step.getName(), workCenter.getName(), null, false);
            if (response.isError()) {
                throw new RuntimeException("在该工序启动失败原因：" + response.getFirstErrorMessage());
            }
            // 最后一道工序更新产品状态
            if (proxy.isLastStep(route.getName(), step.getName())) {
                flag = true;
                // 更新产品描述
                unit.setUDA(IUnitState.NORMAL, IUDADefinition.UDA_UNIT_STATE);
                unit.setUDA(IUnitStatus.STORAGING, IUDADefinition.UDA_UNIT_STATUS);
                response = unit.save();
                if (response.isError()) {
                    throw new RuntimeException("更新产品状态为" + unitStatus + "失败，产品原状态为" + unit.getUDA(IUDADefinition.UDA_UNIT_STATUS));
                }
            }

            response = unit.completeAtStepAtWorkCenter(step.getName(), workCenter.getName(), ICompleteReason.NORMAL, false);
            if (response.isError()) {
                throw new RuntimeException("在投产工序启动失败，原因：" + response.getFirstErrorMessage());
            }

            // 最后一道工序处理下线
            if (proxy.isLastStep(route.getName(), step.getName())) {
                response = unit.finish("包装下线");
                if (response.isError()) {
                    throw new RuntimeException("包装下线失败，修改状态失败");
                }
                ProductionLine line = workCenter.getProductionLine();
                String perviousFinishTimeStr = proxy.getPerviousFinishTime(line.getName());
                Time perviousFinishTime = StringUtil.isNull(perviousFinishTimeStr) ? null : DateTimeUtils.parseDateOfPnut(perviousFinishTimeStr, IDateFormat.TIME_LONG);
                proxy.recordFinishTime(unit.getProductionLineName(), unit.getFinishedTime(), unit.getSerialNumber());
                // 若第一个下线，则自动触发首检
                int finishedQty = orderItem.getPreciseQuantityFinished().intValue();
                if (finishedQty == 1) {
                    unit.setUDA(IUnitSequence.FIRST, IUDADefinition.UDA_UNIT_SEQUENCE);
                    response = unit.save();
                    if (response.isError()) {
                        throw new RuntimeException("标记产品首末件失败，原因：" + response.getFirstErrorMessage());
                    }
                    ATDefinition atDefinition = proxy.getATDefinition(IATConstants.AT_TABLE_INSPECTS);
                    ATRow inspectAssembly;
                    try {
                        inspectAssembly = atDefinition.createATRow_();
                    } catch (DatasweepException e) {
                        throw new RuntimeException("标记产品首末件失败，原因：" + e.getMessage());
                    }
                    if (inspectAssembly != null) {
                        inspectAssembly.setValue(IATConstants.AT_COLUMN_INSPECTS_INSPECT_TYPE, IInspectType.TYPE_ASSEMBLY);
                        inspectAssembly.setValue(IATConstants.AT_COLUMN_INSPECTS_UNIT_BARCODE, serialNumber);
                        inspectAssembly.setValue(IATConstants.AT_COLUMN_INSPECTS_DEFECT_QTY, 0);
                        inspectAssembly.setValue(IATConstants.AT_COLUMN_INSPECTS_ORDER_NUMBER, order.getOrderNumber());
                        String productionLineName = unit.getProductionLineName();
                        ProductionLine productionLine = proxy.getProductionLineByName(productionLineName);
                        inspectAssembly.setValue(IATConstants.AT_COLUMN_INSPECTS_PRODUCTION_LINE, productionLine.getDescription());
                        inspectAssembly.setValue(IATConstants.AT_COLUMN_INSPECTS_SHIFT_NAME, orderItem.getUDA(IUDADefinition.UDA_ORDER_ITEM_SHIFT));
                        inspectAssembly.setValue(IATConstants.AT_COLUMN_INSPECTS_FACTORY, productionLine.getUDA(IUDADefinition.UDA_PRODUCTION_LINE_FACTORY));
                        inspectAssembly.setValue(IATConstants.AT_COLUMN_INSPECTS_WORKCENTER, productionLine.getUDA(IUDADefinition.UDA_PRODUCTION_LINE_AREA));
                        inspectAssembly.save(null, null, null);
                    }
                }
                String orderNumber = unit.getOrderNumber();

                proxy.addStorageOrder(orderNumber, unit.getPartNumber(), shipTime, 1);

                proxy.updateUnitProperty(serialNumber, orderItem, unit.getFinishedTime(),
                        false, perviousFinishTime);
            }

            // 生成打印记录
            ProductionLine productionLine = proxy.getProductionLineByName(unit.getProductionLineName());
            if (productionLine == null) {
                throw new RuntimeException("无法获得产线信息");
            }

            String printerName = workCenter.getUDA(IUDADefinition.UDA_WORK_CENTER_PRINT);

            if (StringUtil.isNull(printerName)) {
                throw new RuntimeException("无法获得打印机信息");
            }

            String partNumber = orderItem.getPartNumber();


            ATRow uiPrintConf = proxy.getPrintConfiguration(partNumber, IPrintType.PACKAGE);
            if (uiPrintConf != null) {
                PrintInfo printInfo = new PrintInfo(serialNumber, IPrintType.PACKAGE, false, printerName, uiPrintConf, order.getUDT(IUDADefinition.UDT_ORDER_PUBLISH_TIME), 2);
                ATDefinition atDefinition = proxy.getATDefinition(IATConstants.AT_TABLE_PRINT_RECORD);
                Response res = atDefinition.createATRow();
                if (res.isOk()) {
                    ATRow atRow = (ATRow) res.getResult();
                    atRow.setValue(IATConstants.AT_COLUMN_PRINT_RECORD_BARCODE,
                            serialNumber);
                    atRow.setValue(IATConstants.AT_COLUMN_PRINT_RECORD_PRINT_TYPE,
                            IPrintType.PACKAGE);
                    atRow.setValue(IATConstants.AT_COLUMN_PRINT_RECORD_RECORD_TYPE,
                            IPrintRecordType.PRINT);
                    atRow.setValue(
                            IATConstants.AT_COLUMN_PRINT_RECORD_ORDER_NUMBER, order
                                    .getOrderNumber());
                    atRow
                            .setValue(
                                    IATConstants.AT_COLUMN_PRINT_RECORD_PRODUCTION_LINE_NAME,
                                    productionLine.getName());
                    atRow.setValue(
                            IATConstants.AT_COLUMN_PRINT_RECORD_PART_DESCRIPTION,
                            unit.getPart().getDescription());
                    atRow.setValue(IATConstants.AT_COLUMN_PRINT_RECORD_PART_NUMBER,
                            partNumber);
                    atRow.setValue(
                            IATConstants.AT_COLUMN_PRINT_RECORD_SEQUENCE_NUMBER,
                            String.valueOf(unit.getPriority()));
                    atRow.setValue(
                            IATConstants.AT_COLUMN_PRINT_RECORD_ORDER_ITEM_NUMBER,
                            unit.getOrderItem());
                    // 打印主机条码在前台打印
                    atRow.setValue(IATConstants.AT_COLUMN_PRINT_RECORD_IS_PRINT,
                            true);
                    atRow.setValue(
                            IATConstants.AT_COLUMN_PRINT_RECORD_PRINT_CONTENT,
                            printInfo.getCommand());
                    res = atRow.save(null, null, null);
                    if (res.isError()) {
                        throw new RuntimeException("保存名牌打印信息失败，原因：" + res.getFirstErrorMessage());
                    }
                }
            }
            if (flag) {
                //如果是最后一道工序，将主机条码下线数据发给WMS
                List<String> serialNumbers = new ArrayList<String>();
                serialNumbers.add(serialNumber);
                proxy.sendUnitDownLineToWMS(serialNumbers);
            }
            result.setStatus(1);
            result.setReason(applicationName+"处理成功");
            userTransaction.commit();
        } catch (Exception e) {
            result.setStatus(2);
            result.setReason(applicationName+"处理失败"+e.getMessage());
        }
        return result;
    }

    private void sendOrderStartToWMS(ServerImplProxy proxy, OrderItem orderItem, Order order) {
        ATDefinition atDefinition = proxy.getATDefinition(IATConstants.AT_TABLE_MES_TO_WMS_ORDER_START);
        Response response = atDefinition.createATRow();
        if (response.isOk()) {
            ATRow atRow = (ATRow) response.getResult();
            atRow.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_ORDER_START_ORDER_NUMBER, order.getOrderNumber());
            atRow.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_ORDER_START_ORDER_ITEM_NUMBER, orderItem.getOrderItem());
            atRow.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_ORDER_START_ORDER_TYPE, order.getDescription());
            atRow.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_ORDER_START_PLANNED_LINE, orderItem.getPlannedProductionLine());
            atRow.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_ORDER_START_STATUS, "1");
            atRow.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_ORDER_START_DEALFLAG, "0");//0未处理
            atRow.save(null, null, null);
        }
    }

    private void transferWorkOrderFlag(String orderNumber, String orderType, String s) {

    }

    private int getTotalProductQty(Order order) {
        UnitFilter unitFilter = new UnitFilter(order.getServerImpl());
        unitFilter.forOrderKeyEqualTo(order.getKey());
        try {
            Long count = unitFilter.getCount();
            return count.intValue();
        } catch (DatasweepException e) {
            throw new RuntimeException("无法获得工单" + order.getOrderNumber() + "已投产量");
        }
    }

    private WorkCenter getWorkCenterByLineAndStep(ProductionLine productionLine, Step step) {
        Vector wcs = step.getWorkCenters();
        Vector plWorkCenters = productionLine.getWorkCenters();
        for (int i = 0; i < wcs.size(); i++) {
            WorkCenter wc = (WorkCenter) wcs.get(i);
            for (int j = 0; j < plWorkCenters.size(); j++) {
                WorkCenter plWorkCenter = (WorkCenter) plWorkCenters.get(j);
                if (wc.getName().equals(plWorkCenter.getName())) {
                    return wc;
                }
            }
        }
        return null;
    }

    private void throwUnitStatusException(Unit unit) throws RuntimeException {
        if (IUnitState.TESTED_FAILED.equals(unit.getUDA(1))) {
            throw new RuntimeException("该产品应该去维修");
        }
        Step step = getNextStep(unit);
        if (step == null) {
            throw new RuntimeException("该产品已下线");
        } else {
            throw new RuntimeException("该产品应该进入工序:" + step.getDescription()
                    + ",当前产品的状态是：" + unit.getUDA(0) + ";" + unit.getUDA(1));
        }

    }

    //获取unit的下一道工序
    private Step getNextStep(Unit unit) {
        Queue queue = unit.getQueue();
        if (queue == null) {
            return null;
        }
        Vector<Arc> arcs = queue.getOutgoingArcs();
        if (arcs == null || arcs.size() == 0) {
            return null;
        }
        return (Step) queue.getOutgoingArcs().firstElement().getTo();
    }


    protected Unit createStandAloneUnit(String serialNumber, BillOfMaterials bom) {
        if (serialNumber.length() < 9) {
            throw new RuntimeException("创建物料" + serialNumber + "失败，该条码长度不合法");
        }

        String partNumber = serialNumber.substring(0, serialNumber.length() - 8);
        Part part = proxy.getPart(partNumber);
        if (part == null || (bom.getBomItem(partNumber) == null)) {
            throw new RuntimeException("物料号:" + partNumber + "不存在");
        }

        // 判断是否是没有条码的物料
        if ((serialNumber.length() - (serialNumber.lastIndexOf("0000") + 4)) == 0) {
            Time time = proxy.getDbTime();
            String timeStr = DateTimeUtils.formatDate(time, "yyyyMMdd");
            serialNumber = serialNumber + "FOTILE" + timeStr;
            int sequence = proxy.getUserSequenceByName(serialNumber);
            serialNumber = serialNumber + formatNumber(sequence, 5);
        }

        Response response = proxy.createOneStandAloneUnit(serialNumber, partNumber);
        if (response.isError()) {
            throw new RuntimeException("创建物料" + serialNumber + "失败原因：" + response.getFirstErrorMessage());
        }
        Unit unit = (Unit) response.getResult();
        unit.setUDA(IUnitStatus.UNCONSUMED, IUDADefinition.UDA_UNIT_STATUS);
        unit.setUDA(IUnitState.NORMAL, IUDADefinition.UDA_UNIT_STATE);
        unit.setDescription(unit.getPart().getDescription());
        response = unit.save();
        if (response.isError()) {
            throw new RuntimeException("更新物料" + serialNumber + "状态失败");
        }
        return unit;
    }

    protected String formatNumber(int number, int length) {
        return String.format("%0" + length + "d", number);
    }
}

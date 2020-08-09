package com.fotile.proxy;

import com.datasweep.compatibility.client.*;
import com.datasweep.compatibility.manager.ServerImpl;
import com.datasweep.compatibility.ui.Time;
import com.datasweep.plantops.common.constants.IBomItemConsumptionTypes;
import com.fotile.constant.*;
import com.fotile.util.DateTimeUtils;
import com.fotile.util.StringUtil;
import com.rockwell.transactiongrouping.UserTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerImplProxy {
    private ServerImpl server;

    public ServerImplProxy(ServerImpl server) {
        this.server = server;
    }

    /**
     * 根据主机条码查找unit
     *
     * @param serialNumber 主机条码
     * @return unit
     */
    public Unit getUnitBySerialNumber(String serialNumber) {
        Unit unit = null;
        try {
            UnitFilter unitFilter = new UnitFilter(this.server);
            unitFilter.forSerialNumberEqualTo(serialNumber);
            unitFilter.orderByCreationTime(false);      //创建时间倒序排序
            List<Unit> units = unitFilter.exec();
            if (units.size() >= 1) {
                unit = units.get(0);
            }
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return unit;
    }

    /**
     * 根据工作中心编号查找工作中心
     *
     * @param workCenterName 工作中心编号
     * @return workCenter
     */
    public WorkCenter getWorkCenterByName(String workCenterName) {
        WorkCenter workCenter = null;
        try {
            workCenter = (WorkCenter) this.server.getWorkCenterManager().getObject(workCenterName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return workCenter;
    }

    public Order getWorkOrderByName(String orderNumber) {
        Order order = null;
        try {
            order = (Order) this.server.getOrderManager().getObject(orderNumber);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return order;
    }

    public Route getRouteByRouteName(String routeName) {
        Route route = null;
        try {
            route = (Route) this.server.getRouteManager().getObject(routeName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return route;
    }

    public ProductionLine getProductionLineByName(String productionLineName) {
        ProductionLine productionLine = null;
        try {
            productionLine = (ProductionLine) this.server.getProductionLineManager().getObject(productionLineName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return productionLine;
    }

    public Shift getShiftByName(String shiftName) {
        Shift shift = null;
        try {
            shift = (Shift) this.server.getShiftManager().getObject(shiftName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return shift;
    }

    public UserTransaction getUserTransaction() {
        UserTransaction userTransaction = null;
        try {
            userTransaction = new UserTransaction(this.server);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return userTransaction;
    }

    public List<Shift> getShiftListByProductionLine(String productionLineName) {
        List<Shift> shifts = null;
        ShiftFilter shiftFilter = new ShiftFilter(this.server);
        shiftFilter.forUdaEqualTo("pl_number", productionLineName);
        try {
            shifts = shiftFilter.exec();
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return shifts;
    }

    /**
     * 获取数据库时间,获取步到则返回的是系统时间
     *
     * @return
     */
    public Time getDbTime() {
        Time time = new Time();
        try {
            time = server.getUtilityManager().getDBTime();
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 根据主机条条码查找工单号
     *
     * @param serialNumber
     * @return
     */
    public String getOrderNumberByPrintRecord(String serialNumber) {
        ATRow atRow = null;
        ATRowFilter atRowFilter = new ATRowFilter("PrintRecord", server);
        try {
            atRowFilter.forColumnNameEqualTo("barcode", serialNumber);
            atRowFilter.forColumnNameEqualTo("record_type", "打印");
            atRowFilter.setMaxRows(1);
            List<ATRow> atRows = atRowFilter.exec();
            if (atRows == null || atRows.size() < 1) {
                throw new RuntimeException("主机条码：" + serialNumber + "未打印");
            }
            atRow = atRows.get(0);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return (String) atRow.getValue("order_number");
    }

    //根据物料号+打印类型查找打印配置
    public ATRow getPrintConfiguration(String partNumber, String templateType){
        try {
            ATRowFilter atRowFilter = new ATRowFilter(IATConstants.AT_TABLE_PRINT_CONF,server);
            atRowFilter.forColumnNameEqualTo(IATConstants.AT_COLUMN_PRINT_CONF_PART_NUMBER, partNumber);
            atRowFilter.forColumnNameEqualTo(IATConstants.AT_COLUMN_PRINT_CONF_TEMPLATE_TYPE, templateType);
            Vector<ATRow> atRows = atRowFilter.exec();
            if (atRows != null && atRows.size() > 0) {
                return atRows.firstElement();
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<OrderItem> getOrderItemByOrderKey(long key) {
        OrderItemFilter filter = new OrderItemFilter(server);
        return null;
    }

    public String geOrderItemNumber(List<Shift> shifts, Time dbTime, Order order) {
        String orderItemNumber = "";
        Shift currentShift = null;
        boolean lastNight = false;      //是否晚班0点-晚班下班,默认为否
        if (dbTime == null) {
            dbTime = this.getDbTime();
        }
        try {
            if (shifts != null && shifts.size() > 0) {
                for (Shift shift : shifts) {

                    String onShiftTime = (String) shift.getUDA("on_shift_time");    //上班开始时间
                    String offShiftTime = (String) shift.getUDA("off_shift_time");  //下班时间
                    int endHour = Integer.valueOf(offShiftTime.split(":")[0]) * 60 + Integer.valueOf(offShiftTime.split(":")[1]);       //下班时间转换成整分钟数
                    int startHour = Integer.valueOf(onShiftTime.split(":")[0]) * 60 + Integer.valueOf(onShiftTime.split(":")[1]);       //上班时间转换成整分钟数
                    int nowTime = dbTime.getHour() * 60 + dbTime.getMinute();       //当前时间转换成整分钟数

                    if (startHour < endHour) {     //如果上班时间小于下班时间(分钟数)说明是白班
                        if (startHour <= nowTime && nowTime <= endHour) {  //如果当前时间在白班区间内
                            currentShift = shift;
                            break;
                        }
                    }
                    if (startHour > endHour) {     //如果上班时间分钟数大于下班时间分钟数,说明是晚班
                        if (startHour < nowTime || nowTime < endHour) {        //如果当前时间在晚班的区间内
                            currentShift = shift;
                            if (nowTime < endHour) {
                                lastNight = true;       //当前时间在0点---晚班下班时间段内
                            }
                            break;
                        }
                    }
                }
            }
            if (currentShift != null) {
                if (!lastNight) {    //如果是非0点---晚班下班
                    String timeStr = DateTimeUtils.formatDate(dbTime, "yyyyMMdd");
                    if (currentShift.getDescription().equals("白班")) {
                        timeStr = timeStr + "0";
                    } else {
                        timeStr = timeStr + "1";
                    }
                    String sql = "select woi.order_item"
                            + " from work_order wo,work_order_items woi,shift s"
                            + " where wo.order_key=woi.order_key"
                            + " and woi.uda_2=s.shift_name"
                            + " and wo.order_number='"
                            + order.getOrderNumber()
                            + "'"
                            + " and to_char(woi.planned_start_time,'yyyymmdd')||decode(s.description,'白班',0,1)>='"
                            + timeStr
                            + "'"
                            + " order by to_char(woi.planned_start_time,'yyyymmdd')||decode(s.description,'白班',0,1)";
                    System.out.println(sql);
                    Vector<String[]> vector = null;

                    vector = this.getArrayDataFromActive(sql, 30);

                    if (vector != null && vector.size() > 0) {
                        orderItemNumber = vector.firstElement()[0];
                    }
                } else {     //如果是0点-晚班下班,需要将排程时间往后推一天,然后再获取排程号
                    String timeStr = DateTimeUtils.formatDate(dbTime,
                            "yyyyMMdd");
                    timeStr = timeStr + "1";
                    String sql = "select woi.order_item"
                            + " from work_order wo,work_order_items woi,shift s"
                            + " where wo.order_key=woi.order_key"
                            + " and woi.uda_2=s.shift_name"
                            + " and wo.order_number='"
                            + order.getOrderNumber()
                            + "'"
                            + " and to_char(woi.planned_start_time+1,'yyyymmdd')||decode(s.description,'白班',0,1)='"
                            + timeStr
                            + "'"
                            + " order by to_char(woi.planned_start_time+1,'yyyymmdd')||decode(s.description,'白班',0,1)";
                    Vector<String[]> vector = this.getArrayDataFromActive(sql, 30);
                    if (vector != null && vector.size() > 0) {
                        orderItemNumber = vector.firstElement()[0];
                    }
                }
            }

            if (orderItemNumber.equals("")) {        //如果还是获取步到排程号，则说明是当前时间是白晚班之间的休息时间，这时候将当前时间往后加2小时，以查找到晚班班次
                if (shifts != null && shifts.size() > 0) {
                    for (int i = 0; i < shifts.size(); i++) {
                        Shift shift = (Shift) shifts.get(i);
                        String onShiftTime = (String) shift.getUDA("on_shift_time");
                        String offShiftTime = (String) shift.getUDA("off_shift_time");
                        //判断细分到分钟   #bug 7733 8月发布：功能完善-离线接口班次取值逻辑优化  by：shenymc
                        int endHour = Integer.valueOf(offShiftTime.split(":")[0]) * 60 + Integer.valueOf(offShiftTime.split(":")[1]);
                        int startHour = Integer.valueOf(onShiftTime.split(":")[0]) * 60 + Integer.valueOf(offShiftTime.split(":")[1]);
                        int nowTime = dbTime.getHour() * 60 + dbTime.getMinute() + 240;//谱线取往后推二个小时的班次
                        if (endHour < startHour) {//铺线不考虑跨天
                            if ((nowTime < endHour) || (nowTime >= startHour)) {
                                //晚班
                                lastNight = false;
                                currentShift = shift;
                                break;
                            }
                        } else {
                            if ((nowTime <= endHour) && (nowTime >= startHour)) {
                                //白班
                                lastNight = false;
                                currentShift = shift;
                                break;
                            }
                        }
                    }
                    if (currentShift == null) {
                        throw new RuntimeException("未找到班次");
                    }
                    System.out.println("为下一班次铺线");
                    if (orderItemNumber == "" && !lastNight) {
                        String timeStr = DateTimeUtils.formatDate(dbTime,
                                "yyyyMMdd");
                        if (currentShift.getDescription().equals("白班")) {
                            timeStr = timeStr + "0";
                        } else {
                            timeStr = timeStr + "1";
                        }
                        String sql = "select woi.order_item"
                                + " from work_order wo,work_order_items woi,shift s"
                                + " where wo.order_key=woi.order_key"
                                + " and woi.uda_2=s.shift_name"
                                + " and wo.order_number='"
                                + order.getOrderNumber()
                                + "'"
                                + " and to_char(woi.planned_start_time,'yyyymmdd')||decode(s.description,'白班',0,1)>='"
                                + timeStr
                                + "'"
                                + " order by to_char(woi.planned_start_time,'yyyymmdd')||decode(s.description,'白班',0,1)";
                        System.out.println("铺线");
                        System.out.println(sql);
                        Vector<String[]> vector = this.getArrayDataFromActive(sql, 30);
                        if (vector != null && vector.size() > 0) {
                            orderItemNumber = vector.firstElement()[0];
                        }
                    }
                }
            }
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return orderItemNumber;
    }

    public Lot getLotByOrderAndOrderItem(Order order, OrderItem orderItem) {
        List<Lot> lots = null;
        LotFilter lotFilter = new LotFilter(this.server);
        lotFilter.forOrderKeyEqualTo(order.getKey());
        lotFilter.forOrderItemKeyEqualTo(orderItem.getKey());
        try {
            lots = lotFilter.exec();
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        if (lots == null || lots.size() == 0) {
            throw new RuntimeException("未找到批次，请检查工单/排程状态是否正常");
        }
        return lots.get(0);     //返回查到的第一个lot
    }

    /**
     * 根据工单号查找原始的lot
     *
     * @param orderNumber
     * @return
     */
    public Lot getLotByName(String orderNumber) {
        Lot lot = null;
        try {
            lot = (Lot) this.server.getLotManager().getObject(orderNumber);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return lot;
    }

    /**
     * 创建一个bom
     *
     * @param name    bom名称
     * @param version bom版本
     * @return
     */
    public BillOfMaterials createBOM(String name, String version) {
        BillOfMaterials billOfMaterials = server.getBomManager().createBillOfMaterials(name, version);
        return billOfMaterials;
    }

    //创建一个bom子项
    public BomItem createBomItem() {
        return ManagerSupport.createBomItem(this.server);
    }

    public Unit applyBomForUnit(Unit unit, Lot lot, String serialNumber) {
        Response response;
        try {
            BillOfMaterials lotBom = lot.getBillOfMaterials();
            List<BomItem> lotBomItems = lotBom.getBomItems();              //获取原始bom，里面包括了所有的bom子项
            BillOfMaterials unitBom = this.createBOM(serialNumber, "1");       //创建一个bom

            for (BomItem lotBomItem : lotBomItems) {
                BomItem unitBomItem = this.createBomItem();
                unitBomItem.setPartNumber(lotBomItem.getPartNumber());
                unitBomItem.setPartRevision(lotBomItem.getPartRevision());
                unitBomItem.setDescription(lotBomItem.getDescription());
                unitBomItem.setBomItemName(lotBomItem.getPartNumber());
                unitBomItem.setQuantity(new BigDecimal(lotBomItem
                        .getUDA(0)));
                unitBomItem.setUDA(lotBomItem.getUDA(0), 0);     //uda_0字段保存的也是bomItem的数量
                unitBom.addBomItem(unitBomItem);
            }
            response = unit.changeSerialNumber(serialNumber);      //更改unit的主机条码为serialNumber
            if (response.isError()) {
                throw new RuntimeException("更改主机条码失败：" + response.getFirstErrorMessage());
            }
            unit.applyRuntimeBom(unitBom);              //应用主机条码bom
            if (response.isError()) {
                throw new RuntimeException("保存主机条码bom失败：" + response.getFirstErrorMessage());
            }
            unit.setDescription(unit.getPart().getDescription());
            unit.setUDA(IConstant.STARTING_STATUS, 0);
            unit.setUDA(IConstant.NORMAL_STATS, 1);
            response = unit.save();
            if (response.isError()) {
                throw new RuntimeException("保存产品状态失败：" + response.getFirstErrorMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return unit;
    }

    public void updateUnitProperty(String serialNumber, OrderItem orderItem, Time currentTime, boolean isCreated, Time perviousFinishTime) {
        try {
            ATRowFilter atRowFilter = new ATRowFilter(IATConstants.AT_TABLE_UNIT_PROPERTY, this.server);
            atRowFilter.forColumnNameEqualTo(IATConstants.AT_COLUMN_UNIT_PROPERTY_SERIAL_NUMBER, serialNumber);
            Vector<ATRow> atRows = atRowFilter.exec();
            ATRow atRow = null;
            if (atRows != null && atRows.size() > 0) {
                atRow = atRows.firstElement();
            } else {
                ATDefinition atDefinition = (ATDefinition) this.server.getATDefinitionManager().getObject(IATConstants.AT_TABLE_UNIT_PROPERTY);
                atRow = atDefinition.createATRow_();
                atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_SERIAL_NUMBER,
                        serialNumber);
            }
            atRow.setValue(isCreated ? IATConstants.AT_COLUMN_UNIT_PROPERTY_CREATE_USER : IATConstants.AT_COLUMN_UNIT_PROPERTY_FINISHED_USER,
                    this.server.getLoggedUser().getName());
            ShiftFilter shiftFilter = new ShiftFilter(this.server);
            shiftFilter.forUdaEqualTo(IUDADefinition.UDA_SHIFT_PL_NUMBER, orderItem.getPlannedProductionLine());
            List<Shift> list = shiftFilter.exec();
            String finishShiftName = "";
            if (list != null && list.size() > 0) {
                Shift shift = list.get(0);
                String shiftName = shift.getDescription();
                String otherName = "";
                if ("白班".equals(shiftName)) {
                    otherName = "晚班";
                } else {
                    otherName = "白班";
                }
                String day = (String) shift
                        .getUDA(IUDADefinition.UDA_SHIFT_DAY_DEADLINE);
                String night = (String) shift
                        .getUDA(IUDADefinition.UDA_SHIFT_NIGHT_DEADLINE);
                String timeStr = DateTimeUtils.formatDate(currentTime,
                        IDateFormat.TIME_STANDARD);
                String dayStr = timeStr + " " + day + ":00";
                String nightStr = timeStr + " " + night + ":00";

                String time1Str = timeStr + " 08:00:00";
                Time time1 = DateTimeUtils.parseDateOfPnut(time1Str,
                        IDateFormat.TIME_LONG);
                long lengthTime1 = currentTime.getCalendar().getTimeInMillis()
                        - time1.getCalendar().getTimeInMillis();
                if (lengthTime1 >= 0) {
                    if (isCreated) {
                        atRow.setValue(
                                IATConstants.AT_COLUMN_UNIT_PROPERTY_CREATE_DATE,
                                timeStr);
                    } else {
                        atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_DATE,
                                timeStr);
                    }
                } else {
                    if (isCreated) {
                        atRow.setValue(
                                IATConstants.AT_COLUMN_UNIT_PROPERTY_CREATE_DATE,
                                DateTimeUtils.formatDate(currentTime.addDays(-1),
                                        IDateFormat.TIME_STANDARD));
                    } else {
                        atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_DATE,
                                DateTimeUtils.formatDate(currentTime.addDays(-1),
                                        IDateFormat.TIME_STANDARD));
                    }
                }

                Time dayTime = DateTimeUtils.parseDateOfPnut(dayStr,
                        IDateFormat.TIME_LONG);
                Time nightTime = DateTimeUtils.parseDateOfPnut(nightStr,
                        IDateFormat.TIME_LONG);
                long length1 = 0;
                long length2 = 0;
                if ("白班".equals(shiftName)) {
                    length1 = currentTime.getCalendar().getTimeInMillis()
                            - dayTime.getCalendar().getTimeInMillis();
                    length2 = currentTime.getCalendar().getTimeInMillis()
                            - nightTime.getCalendar().getTimeInMillis();
                    if (length1 >= 0 && length2 <= 0) {
                        if (isCreated) {
                            atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_CREATE_SHIFT, shiftName);
                        } else {
                            atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_SHIFT, shiftName);
                        }
                        finishShiftName = shiftName;
                    } else {
                        if (isCreated) {
                            atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_CREATE_SHIFT, otherName);
                        } else {
                            atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_SHIFT, otherName);
                        }
                        finishShiftName = otherName;
                    }
                } else {
                    length1 = currentTime.getCalendar().getTimeInMillis() - nightTime.getCalendar().getTimeInMillis();
                    length2 = currentTime.getCalendar().getTimeInMillis() - dayTime.getCalendar().getTimeInMillis();
                    if (length1 >= 0 && length2 <= 0) {
                        if (isCreated) {
                            atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_CREATE_SHIFT, otherName);
                        } else {
                            atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_SHIFT, otherName);
                        }
                        finishShiftName = otherName;
                    } else {
                        if (isCreated) {
                            atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_CREATE_SHIFT, shiftName);
                        } else {
                            atRow.setValue(IATConstants.AT_COLUMN_UNIT_PROPERTY_SHIFT, shiftName);
                        }
                        finishShiftName = shiftName;
                    }
                }
            }
            // 保存第一个投产工单信息
            Unit unit = this.getUnitBySerialNumber(serialNumber);
            Order order = unit.getOrder();
            String prefix = IUDADefinition.UDA_SYSTEM_CONFIG_PLINE;
            String pLine = orderItem.getPlannedProductionLine();
            String today = DateTimeUtils.formatDate(this.getDbTime(), IDateFormat.TIME_DAY);
            String shiftNumber = orderItem.getUDA(IUDADefinition.UDA_ORDER_ITEM_SHIFT);
            Shift shift = this.getShiftByName(shiftNumber);
            if (shift != null) {
                String shiftName = shift.getDescription();
                if ("晚班".equals(shiftName)) { // 晚班时，如果时间超过晚上12点（即到了第二天），则将时间往后推一天
                    String time = DateTimeUtils.formatDate(
                            this.getDbTime(), IDateFormat.TIME_HOUR_MIN)
                            .substring(0, 2);
                    Pattern pattern = Pattern.compile("[0-9]*");
                    Matcher isNum = pattern.matcher(time);
                    if (isNum.matches() && Integer.parseInt(time) < 17) {
                        today = DateTimeUtils.formatDate(this.getDbTime()
                                .addDays(-1), IDateFormat.TIME_DAY);
                    }
                }
                String propName = prefix + pLine + today + shiftName;
                ATRowFilter atRowFilter2 = new ATRowFilter(IATConstants.AT_TABLE_SYSTEM_CONFIGURATION, this.server);
                atRowFilter2.forColumnNameEqualTo(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_NAME, propName);
                Vector vector = atRowFilter2.exec();
                if (vector == null || vector.size() == 0) {
                    ATDefinition atDefinition = (ATDefinition) this.server.getATDefinitionManager().getObject(IATConstants.AT_TABLE_SYSTEM_CONFIGURATION);
                    ATRow atRow2 = atDefinition.createATRow_();
                    atRow2.setValue(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_NAME, propName);
                    atRow2.setValue(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_VALUE, order.getOrderNumber());
                    Response response = atRow2.save(null, null, null);
                    if (response.isError()) {
                        throw new RuntimeException("保存产线第一个投产工单失败：" + response.getFirstErrorMessage());
                    }
                }
            }

            // 判断上一个产品的下线时间是否在当前班次，若在，则记录当前产品的下线时间与上一个产品的下线时间之差，否则，不记录
            // Add By Nemo
            if (perviousFinishTime != null) {
                String sql = "select s.shift_name from shift s,uda_shift us "
                        + " where 1=1"
                        + " and us.object_key=s.shift_key and us.pl_number_s='"
                        + orderItem.getPlannedProductionLine()
                        + "' and s.description='" + finishShiftName + "'";
                Vector<String[]> datas = this.getArrayDataFromActive(sql, 100);
                if (datas != null && datas.size() > 0) {
                    String number = datas.firstElement()[0];
                    Shift finishShift = this.getShiftByName(number);
                    String shiftName = finishShift.getDescription();
                    String[] times = getStartAndEndTime(currentTime, finishShift);
                    Time startTime = DateTimeUtils.parseDateOfPnut(times[0],
                            IDateFormat.TIME_LONG);
                    long diff;
                    String prefix2 = IUDADefinition.UDA_SYSTEM_CONFIG_UNIT_TIME;
                    String propName = prefix2
                            + pLine
                            + DateTimeUtils.formatDate(startTime,
                            IDateFormat.TIME_STANDARD) + shiftName;
                    ATRowFilter atRowFilter2 = new ATRowFilter(IATConstants.AT_TABLE_SYSTEM_CONFIGURATION, this.server);
                    atRowFilter2.forColumnNameEqualTo(
                            IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_NAME,
                            propName);
                    Vector vector = atRowFilter2.exec();
                    if (vector == null || vector.size() == 0) {
                        // 保存产线第一个条码下线时间
                        ATDefinition atDefinition = (ATDefinition) this.server.getATDefinitionManager().getObject(IATConstants.AT_TABLE_SYSTEM_CONFIGURATION);
                        ATRow atRow2 = atDefinition.createATRow_();
                        atRow2.setValue(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_NAME, propName);
                        atRow2.setValue(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_VALUE, DateTimeUtils.formatDate(currentTime, IDateFormat.TIME_LONG));
                        Response response = atRow2.save(null, null, null);
                        if (response.isError()) {
                            throw new RuntimeException("保存产线第一个条码下线时间失败");
                        }
                        String firstTime = getFirstTime(pLine, times[0]);
                        if (StringUtil.isNull(firstTime)) {
                            firstTime = times[0];
                        }

                        // 获得开线到第一个下线产品的时长
                        diff = getWorkingTime(number, DateTimeUtils
                                        .parseDateOfPnut(firstTime, IDateFormat.TIME_LONG),
                                currentTime);
                    } else {
                        diff = getWorkingTime(number, perviousFinishTime,
                                currentTime);
                    }
                    atRow.setValue(
                            IATConstants.AT_COLUMN_UNIT_PROPERTY_PRODUCE_LENGTH,
                            diff);
                }
            }
            atRow.save(null, null, null);
        } catch (DatasweepException e) {
        }
    }

    private long getWorkingTime(String shiftName, Time startTime, Time endTime) {
        try {
            Shift shift = this.getShiftByName(shiftName);
            if (shift == null) {
                throw new Exception("无法获得班次" + shiftName + "信息");
            }

            if (startTime == null || endTime == null) {
                throw new Exception("开始或结束时间不允许为空");
            }

            if (startTime.compareTo(endTime) > 0) {
                return 0;
            }
            int year = startTime.getYear();
            int month = startTime.getMonth();
            int day = startTime.getDay();

            long length = (endTime.getCalendar().getTimeInMillis() - startTime
                    .getCalendar().getTimeInMillis());

            long restLength = 0;
            String onRestTimeStr1 = (String) shift.getUDA("on_rest_time_1");
            String offRestTimeStr1 = (String) shift.getUDA("off_rest_time_1");
            Time onRestTime1 = getRestTime(year, month, day, onRestTimeStr1);
            Time offRestTime1 = getRestTime(year, month, day, offRestTimeStr1);

            restLength += getRestLength(startTime, endTime, onRestTime1,
                    offRestTime1);

            String onRestTimeStr2 = (String) shift.getUDA("on_rest_time_2");
            String offRestTimeStr2 = (String) shift.getUDA("off_rest_time_2");
            Time onRestTime2 = getRestTime(year, month, day, onRestTimeStr2);
            Time offRestTime2 = getRestTime(year, month, day, offRestTimeStr2);

            restLength += getRestLength(startTime, endTime, onRestTime2,
                    offRestTime2);

            String onRestTimeStr3 = (String) shift.getUDA("on_rest_time_3");
            String offRestTimeStr3 = (String) shift.getUDA("off_rest_time_3");
            Time onRestTime3 = getRestTime(year, month, day, onRestTimeStr3);
            Time offRestTime3 = getRestTime(year, month, day, offRestTimeStr3);

            restLength += getRestLength(startTime, endTime, onRestTime3,
                    offRestTime3);

            String onRestTimeStr4 = (String) shift.getUDA("on_rest_time_4");
            String offRestTimeStr4 = (String) shift.getUDA("off_rest_time_4");
            Time onRestTime4 = getRestTime(year, month, day, onRestTimeStr4);
            Time offRestTime4 = getRestTime(year, month, day, offRestTimeStr4);

            restLength += getRestLength(startTime, endTime, onRestTime4,
                    offRestTime4);

            return length - restLength;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long getRestLength(Time startTime, Time endTime, Time onRestTime, Time offRestTime) {

        if (onRestTime == null || offRestTime == null) {
            return 0;
        }
        startTime.getHour();
        startTime.getMinute();
        if (startTime.compareTo(onRestTime) < 0) {
            if (endTime.compareTo(onRestTime) < 0) {
                // CDAB 0
                return 0;
            } else {
                if (endTime.compareTo(offRestTime) < 0) {
                    // CADB D-A
                    return endTime.getCalendar().getTimeInMillis()
                            - onRestTime.getCalendar().getTimeInMillis();
                } else {
                    // CABD (B-A)
                    return offRestTime.getCalendar().getTimeInMillis()
                            - onRestTime.getCalendar().getTimeInMillis();
                }
            }
        } else {
            if (startTime.compareTo(offRestTime) < 0) {
                if (endTime.compareTo(offRestTime) < 0) {
                    // ACDB D-C
                    return endTime.getCalendar().getTimeInMillis()
                            - startTime.getCalendar().getTimeInMillis();
                } else {
                    // ACBD B-C
                    return offRestTime.getCalendar().getTimeInMillis()
                            - startTime.getCalendar().getTimeInMillis();
                }
            } else {
                return 0;
            }
        }
    }

    private Time getRestTime(int year, int month, int day, String onRestTimeStr1) {
        if (StringUtil.isNotNull(onRestTimeStr1)) {
            String[] data = onRestTimeStr1.split(":");
            int hour = Integer.valueOf(data[0]);
            int minute = Integer.valueOf(data[1]);
            if (hour < 8) {
                day = day + 1;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(year, month - 1, day, hour, minute);
            return new Time(calendar);
        }
        return null;
    }

    private String getFirstTime(String pLine, String time) {
        String sql = "select to_char(min(at.scan_time_t),'yyyy-mm-dd hh24:mi:ss') from at_assemblescanrecord at where at.production_line_number_s = '"
                + pLine
                + "' and at.scan_time_t > to_date('"
                + time + "','yyyy-mm-dd hh24:mi:ss')";
        Vector<String[]> datas = this.getArrayDataFromActive(sql, 0);
        if (datas != null && datas.size() > 0) {
            return datas.firstElement()[0];
        }
        return null;
    }

    private String[] getStartAndEndTime(Time currentTime, Shift finishShift) {
        String[] startAndEndTime = new String[2];

        boolean isSecondDay = currentTime.getHour() < 8;
        if (isSecondDay) {
            currentTime = currentTime.addDays(-1);
        }

        boolean dayType = finishShift.getDescription().contains("白班");
        try {
            startAndEndTime[0] = DateTimeUtils.formatDate(currentTime, IDateFormat.TIME_STANDARD) + " " + finishShift.getUDA(IUDADefinition.UDA_SHIFT_ON_SHIFT_TIME) + ":00";
            if (!dayType) {
                currentTime = currentTime.addDays(1);
            }
            startAndEndTime[1] = DateTimeUtils.formatDate(currentTime, IDateFormat.TIME_STANDARD) + " " + finishShift.getUDA(IUDADefinition.UDA_SHIFT_ON_SHIFT_TIME) + ":00";
        } catch (DatasweepException e) {
            throw new RuntimeException("获取班次" + finishShift + "的上下班时间失败：" + e.getMessage());
        }
        return startAndEndTime;
    }

    public ATDefinition getATDefinition(String atName) {
        ATDefinition atDefinition = null;
        try {
            atDefinition = (ATDefinition) server.getATDefinitionManager().getObject(atName);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return atDefinition;
    }

    public Part getPart(String partNumber) {
        Part part = null;
        try {
            part = (Part) server.getPartManager().getObject(partNumber);
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return part;
    }

    //获取序列的下一个序列号
    public int getUserSequenceByName(String sequenceName) {
        try {
            UserSequence userSequence = (UserSequence) server.getUserSequenceManager().getObject(sequenceName);
            if (userSequence == null) {
                userSequence = this.createUserSequence(sequenceName);
            }

            Response response = userSequence.getNextValue();
            if (response.isError()) {
                throw new RuntimeException("无法取得下一个流水号");
            }
            UserSequenceValue userSequenceValue = (UserSequenceValue) response
                    .getResult();
            return userSequenceValue.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public UserSequence createUserSequence(String sequenceName) {
        UserSequence userSequence = server.getUserSequenceManager().createUserSequence(sequenceName);
        userSequence.setInitialValue(0);
        userSequence.setIncrementValue(1);
        userSequence.setMaxValue(9999);
        String dateString = DateTimeUtils.formatDate(this
                .getDbTime(), IDateFormat.TIME_DAY);
        userSequence.setDescription(dateString);
        Response response = userSequence.save();
        if (response.isError()) {
            throw new RuntimeException("创建流水号序列出错");
        }
        return userSequence;
    }

    public Response createOneStandAloneUnit(String serialNumber, String partNumber) {
        return this.server.getUnitManager().createOneStandAloneUnit(null, null,
                serialNumber, partNumber, "1", null, null, 0, null,
                null);
    }


    public ConsumptionSet consumedMaterial(Unit unit, BomItem bomItem, Unit materialUnit, ConsumptionSet consumptionSet, String materialSerialNumber) {
        try {
            BigDecimal quantity = bomItem.getPreciseQuantity();
            if (consumptionSet == null) {
                consumptionSet = unit.createConsumptionSet();
            }

            // 创建消耗
            String consumptionType = bomItem.getPart().getConsumptionType();
            // 如果是关键件
            if (IBomItemConsumptionTypes.CONSUMPTION_TYPE_SERIALNUMBER
                    .equals(consumptionType)) {
                String partNumber = bomItem.getPartNumber();
                BigDecimal qtyOrdered = bomItem.getPreciseQuantity(); // BUG
                // #2906
                BigDecimal qtyConsumed = bomItem.getPreciseQuantityConsumed();
                int diff = qtyOrdered.subtract(qtyConsumed).intValue();
                if (diff <= 0) {
                    throw new RuntimeException("物料：" + partNumber + ";已绑定"
                            + qtyConsumed.intValue() + "，允许绑定数"
                            + qtyOrdered.intValue());
                }
                if (materialUnit != null) {
                    if (!IUnitStatus.UNCONSUMED.equals(materialUnit.getUDA(IUDADefinition.UDA_UNIT_STATUS)) && !IUnitStatus.STORAGING.equals(materialUnit.getUDA(IUDADefinition.UDA_UNIT_STATUS))) {
                        if (IUnitStatus.STARTING.equals(materialUnit.getUDA(IUDADefinition.UDA_UNIT_STATUS)) && materialUnit.getSerialNumber().length() == 26) {
                            throw new RuntimeException("该物料" + materialUnit.getSerialNumber() + "已被解绑，无法进行消耗");
                        }
                        throw new RuntimeException("该物料" + materialUnit.getSerialNumber() + "已经被消耗，无法再次使用");
                    }
                    consumptionSet.addConsumedPart(bomItem, materialUnit);

                    updateUnitStatus(materialUnit, IUnitStatus.CONSUMED); // 更新物料状态
                } else {
                    // 若物料为空，则必须为附件
                    Part part = this.getPart(partNumber);
                    String bomType = "";
                    if (unit.getOrderNumber() != null) {
                        Lot originalLot = this.getLotByName(unit.getOrderNumber());

                        BillOfMaterials lotBom = originalLot.getBillOfMaterials();
                        Vector lotBomItems = lotBom.getBomItems();
                        for (int j = 0; j < lotBomItems.size(); j++) {
                            BomItem lotBomItem = (BomItem) lotBomItems.get(j);
                            if (part.getPartNumber().equals(lotBomItem.getBomItemName())) {
                                if ("10".equals(lotBomItem.getUDA(1))) {
                                    bomType = "关键件";
                                } else if ("20".equals(lotBomItem.getUDA(1))) {
                                    bomType = "附件";
                                } else {
                                    throw new Exception(lotBomItem.getBomKey() + "对应的BOM的关键件类型字段不允许为空");
                                }
                            }
                        }

                        if (!"附件".equals(bomType)) {
                            throw new Exception("非附件的物料条码必须为21位");
                        }
                    }
                    ConsumedPart consumedPart = consumptionSet
                            .addConsumedPart(bomItem);
                    consumedPart.setPartSerial(materialSerialNumber);
                }
            }
            // 如果是非关键件
            else {
                ConsumedPart consumedPart = consumptionSet
                        .addConsumedPart(bomItem);
                consumedPart.setPartQuantity(quantity);
            }

            return consumptionSet;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void updateUnitStatus(Unit unit, String unitStatus) {
        unit.setUDA(unitStatus, IUDADefinition.UDA_UNIT_STATUS);
        Response response = unit.save();
        if (response.isError()) {
            throw new RuntimeException("更新产品状态为" + unitStatus + "失败，产品原状态为" + unit.getUDA(IUDADefinition.UDA_UNIT_STATUS));
        }
    }

    public String getSequenceNumberByPartSerial(String partSerial) {
        String sql = "select sequence_number_s from at_componentbinding where serial_number_s = '" + partSerial + "' and rownum = 1";
        List<String[]> result;
        try {
            result = this.getArrayDataFromActive(sql, 60);
            if (result.size() >= 1) {
                return result.get(0)[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getPartSerialBySequenceNumber(String sequenceNumber) {
        String sql = "select ac.serial_number_s from at_componentbinding ac where ac.sequence_number_s = '" + sequenceNumber + "'";
        List<String[]> result;
        List<String> returnList = new ArrayList<>();
        try {
            result = this.getArrayDataFromActive(sql, 60);
            if (result.size() >= 1) {
                for (String[] strings : result) {
                    returnList.add(strings[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnList;
    }

    public Step getLastBindingStep(Unit unit) {
        try {
            if (unit == null) {
                throw new RuntimeException("该产品尚未投产");
            }
            Route route = unit.getRoute();
            Queue exitQueue = route.getExitQueue();
            List<Arc> arcList = exitQueue.getIncomingArcs();
            if (arcList == null || arcList.size() == 0) {
                return null;
            }
            Step stepfinsh = (Step) arcList.get(0).getFrom(); // 最后一道工序
            Vector<Queue> queues = stepfinsh.getIncomingQueues(); // 获取前一个节点
            if (queues == null || queues.size() == 0) {
                return null;
            }
            Queue queue = queues.get(0);
            while (queue != null) {
                Vector<Arc> arcs = queue.getIncomingArcs();
                if (arcs == null || arcs.size() == 0) {
                    return null;
                }
                Arc arc = arcs.firstElement();

                Step step = (Step) arc.getFrom();
                if (step != null) {
                    Operation operation = step.getOperation();
                    String operationType = operation.getUDA(IUDADefinition.UDA_OPERATION_STEP_TYPE);
                    if ("关键件绑定".equals(operationType)) {
                        return step;
                    } else {
                        queues = step.getIncomingQueues();
                        if (queues != null && queues.size() > 0) {
                            queue = queues.firstElement();
                        } else {
                            queue = null;
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getTestDefinitionName(String productCategory, String stepName) {
        try {
            ATRowFilter atRowFilter = new ATRowFilter(IATConstants.AT_TABLE_QM_CHECK_LIST, server);
            atRowFilter.forColumnNameEqualTo(IATConstants.AT_COLUMN_QM_CHECK_LIST_PART_CATEGORY, productCategory);
            atRowFilter.forColumnNameEqualTo(IATConstants.AT_COLUMN_QM_CHECK_LIST_STEP_NAME, stepName);
            Vector<ATRow> atRows = atRowFilter.exec();
            if (atRows != null && atRows.size() > 0) {
                return (String) atRows.firstElement().getValue(IATConstants.AT_COLUMN_QM_CHECK_LIST_TEST_DEFINITION_NAME);
            }
            return null;
        } catch (DatasweepException e) {
            throw new RuntimeException("无法获得该工序的检验单名称");
        }
    }

    public ATRow getUITestCodeDefinitionByCode(String code) {
        ATRowFilter atRowFilter = new ATRowFilter(
                IATConstants.AT_TABLE_TEST_CODE_DEFINITION, server);
        try {
            atRowFilter.forColumnNameEqualTo(
                    IATConstants.AT_COLUMN_CONFIGURATION_TEST_CODE, code);
            Vector<ATRow> atRows = atRowFilter.exec();
            if (atRows != null && atRows.size() == 1) {
                ATRow atRow = atRows.firstElement();
                return atRow;
            }
        } catch (DatasweepException e) {
            throw new RuntimeException("查询不良失败");
        }
        return null;
    }

    public Vector<String[]> getArrayDataFromActive(String sql, int timeOut) {
        Vector<String[]> datas;
        try {
            datas = this.server.getUtilityManager().getArrayDataFromActive(sql, timeOut);
        } catch (DatasweepException e) {
            throw new RuntimeException("查询数据失败" + e.getMessage());
        }
        return datas;
    }

    public boolean isLastStep(String routeName, String stepName) {
        Route route;
        try {
            route = (Route) server.getRouteManager().getObject(routeName);
        } catch (DatasweepException e) {
            e.printStackTrace();
            throw new RuntimeException("无法找到工艺路径" + routeName + "原因：" + e.getMessage());
        }
        if (route == null) {
            throw new RuntimeException("无法找到工艺路径：" + routeName);
        }
        Step step = route.getStep(stepName);
        Vector<Step> steps = step.getNextSteps();
        if (steps == null || steps.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 获取产线的上一个条码包装下线时间
     *
     * @param productionLineName 产线编号
     * @return
     * @throws Exception
     */
    public String getPerviousFinishTime(String productionLineName) {
        Vector<ATRow> atRows = null;
        try {
            ATRowFilter atRowFilter = new ATRowFilter(IATConstants.AT_TABLE_SYSTEM_CONFIGURATION, server);
            atRowFilter.forColumnNameEqualTo(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_NAME, productionLineName);
            atRows = atRowFilter.exec();
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        if (atRows != null && atRows.size() > 0) {
            ATRow atRow = atRows.firstElement();
            return (String) atRow.getValue(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_VALUE);
        }
        return null;
    }

    public void recordFinishTime(String productionLineName, Time finishedTime, String number) {
        try {
            ATRowFilter atRowFilter = new ATRowFilter(IATConstants.AT_TABLE_SYSTEM_CONFIGURATION, server);
            atRowFilter.forColumnNameEqualTo(
                    IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_NAME,
                    productionLineName);
            Vector<ATRow> atRows = atRowFilter.exec();
            ATRow atRow;
            if (atRows != null && atRows.size() > 0) {
                atRow = atRows.firstElement();
            } else {
                ATDefinition atDefinition = this.getATDefinition(IATConstants.AT_TABLE_SYSTEM_CONFIGURATION);
                atRow = atDefinition.createATRow_();
                atRow.setValue(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_NAME, productionLineName);
            }
            atRow.setValue(IATConstants.AT_COLUMN_SYSTEM_CONFIGURATION_PROP_VALUE, DateTimeUtils.formatDate(finishedTime, IDateFormat.TIME_LONG) + "/" + number);
            Response response = atRow.save(null, null, null);
            if (response.isError()) {
                throw new Exception(response.getFirstErrorMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addStorageOrder(String orderNumber,String partNumber,String shipTime,int qty) {
        ATRow storageOrder = null;
        try {
            //先根据工单号查询入库单
            ATRowFilter atRowFilter = new ATRowFilter(IATConstants.AT_TABLE_STORAGE_ORDER, server);
            atRowFilter.forColumnNameEqualTo(IATConstants.AT_COLUMN_STORAGE_ORDER_ORDER_NUMBER, orderNumber);
            List<ATRow> atRows = atRowFilter.exec();
            for (ATRow atRow : atRows) {
                if (atRow.getValue(IATConstants.AT_COLUMN_STORAGE_ORDER_STATUS).equals(IStorageStatus.CREATED)) {
                    storageOrder = atRow;
                }
            }
            //没有入库单则需要新建入库单
            if (storageOrder == null) {
                String storageOrderNumber = this.generateStorageOrderNumber(IStorageType.PRODUCT_INPUT);
                ATDefinition atDefinition = this.getATDefinition(IATConstants.AT_TABLE_STORAGE_ORDER);
                storageOrder = atDefinition.createATRow_();
                storageOrder.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_STORAGE_NUMBER, storageOrderNumber);
                storageOrder.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_STATUS, IStorageStatus.CREATED);
                storageOrder.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_STORAGE_TYPE, IStorageType.PRODUCT_INPUT);
                storageOrder.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_STORAGE_ORDER_TYPE, IStorageType.PRODUCT_INPUT);
                storageOrder.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_STORAGE_TIME, this.getDbTime());
                storageOrder.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_ORDER_NUMBER, orderNumber);
                storageOrder.save(null, null, null);
                this.addUIStorageOrderDetail(storageOrder,partNumber,shipTime,qty);
            }else{
                this.addUIStorageOrderDetail(storageOrder,partNumber,shipTime,1);
                storageOrder.save(null, null,null);
            }
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
    }

    private void addUIStorageOrderDetail(ATRow storageOrder,String partNumber, String shipTime, int qty){
        try{
            ATDefinition dependentATDefinition = storageOrder.getATDefinition().getDependentATDefinition(IATConstants.AT_TABLE_STORAGE_ORDER_DETAIL);   //明细表对象
            List<DependentATRow> dependentATRows = (List<DependentATRow>) storageOrder.getDetailRows(dependentATDefinition);//获取入库单明细

            DependentATRow dependentATRow = null;      //入库单明细对象
            ATRowFilter filter = new ATRowFilter(server, IATConstants.AT_TABLE_PART_FIX);
            filter.forColumnNameEqualTo(IATConstants.AT_COLUMN_PART_FIX_OLD, partNumber);
            List<ATRow> partFixAtRows = filter.exec();
            String newPartNumber = null;
            if (partFixAtRows != null && partFixAtRows.size() == 1) {
                newPartNumber = (String) partFixAtRows.get(0).getValue(IATConstants.AT_COLUMN_PART_FIX_NEW);
            }
            for (int i = 0; i < dependentATRows.size(); i++) {
                DependentATRow uiStorageOrderDetail = dependentATRows.get(i);
                if ((uiStorageOrderDetail.getValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_PART_NUMBER).equals(partNumber)) ||
                        (uiStorageOrderDetail.getValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_PART_NUMBER).equals(newPartNumber))) {
                    dependentATRow = uiStorageOrderDetail;
                }
            }
            if (dependentATRow == null) {     //如果查不到明细表记录，则创建一条明细表记录
                ParentATRow parentATRow = (ParentATRow) storageOrder;
                dependentATRow = parentATRow.createDependentATRow_(dependentATDefinition, null);
                dependentATRow.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_PART_NUMBER, partNumber);
                dependentATRow.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_QUANTITY_ORDERED, new BigDecimal(qty));
                dependentATRow.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_SHIP_TIME, DateTimeUtils.parseDateOfPnut(
                        shipTime, IDateFormat.TIME_STANDARD));
                dependentATRow.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_QUANTITY, BigDecimal.ZERO);
                dependentATRow.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_QUANTITY_SAP, BigDecimal.ZERO);
                Part part = this.getPart(partNumber);
                if (part == null) {
                    throw new RuntimeException("无法获得该物料");
                }
                dependentATRow.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_PART_DESCRIPTION, part.getDescription());
            } else {
                dependentATRow.setValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_QUANTITY,
                        ((BigDecimal) dependentATRow.getValue(IATConstants.AT_COLUMN_STORAGE_ORDER_DETAIL_QUANTITY)).add(new BigDecimal(qty)));
            }
            storageOrder.save(null,null,null);
        }catch (Exception e){

        }
    }
    private synchronized String generateStorageOrderNumber(String storageType) {
        try {
            StringBuffer prefix = new StringBuffer();
            if (IStorageType.PRODUCT_INPUT.equals(storageType)) {
                prefix.append("PI");
            } else if (IStorageType.PRODUCT_OUTPUT.equals(storageType)) {
                prefix.append("PO");
            } else if (IStorageType.INNER_OUTPUT.equals(storageType)) {
                prefix.append("IO");
            } else if (IStorageType.INNER_BACK.equals(storageType)) {
                prefix.append("IB");
            } else if (IStorageType.PRODUCT_CANCEL_OUTPUT.equals(storageType)) {
                prefix.append("PCO");
            } else {
                throw new RuntimeException("该出入库单类型不存在");
            }

            String timeSuffix = DateTimeUtils.formatDate(this.getDbTime(), IDateFormat.TIME_DAY);
            prefix.append(timeSuffix);
            int sequenceNumber = this.getUserSequenceByName(prefix.toString());
            prefix.append(String.format("%03d", sequenceNumber));
            return prefix.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUnitDownLineToWMS(List<String> serialNumbers){
        for(String o : serialNumbers){
            ATDefinition atDefinition1 = this.getATDefinition(
                    IATConstants.AT_TABLE_MES_TO_WMS_UNIT_DOWNLINE);
            Response res1 = atDefinition1.createATRow();
            Unit unit1 = this.getUnitBySerialNumber(o);
            if (res1.isOk()){
                ATRow atRow1 = (ATRow) res1.getResult();
                //主要针对外协入库无工单的条码做直接下线操作,除了条码信息 其余都为空
                if(StringUtil.isNotNull(unit1.getOrderNumber()) && StringUtil.isNotNull(unit1.getOrderItem())){
                    Order order = unit1.getOrder();
                    OrderItem orderItem = order.getOrderItem(unit1.getOrderItem());
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_ADDR_CITY, order.getCity());
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_PLANNED_LINE, orderItem.getPlannedProductionLine());
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_ORDER_NUMBER, order.getOrderNumber());
                }else{
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_ADDR_CITY, "");
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_ORDER_NUMBER, "");
                }

                if(StringUtil.isNotNull(unit1.getPartNumber())){
                    Part part = this.getPart(unit1.getPartNumber());
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_PART_NUMBER, part.getPartNumber());
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_PART_DESCRIPTION, part.getDescription());
                }else{
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_PART_NUMBER, "");
                    atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_PART_DESCRIPTION, "");
                }

                atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_SERIAL_NUMBER, o);
                atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_STATUS, unit1.getUDA(IUDADefinition.UDA_UNIT_STATUS));
                atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_DESCRIPTION, unit1.getUDA(IUDADefinition.UDA_UNIT_STATE));
                atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_STRAT_TIME, DateTimeUtils.formatDate(unit1.getCreationTime(),"yyyy-MM-dd HH:mm:ss"));
                atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_FINISHED_TIME, DateTimeUtils.formatDate(unit1.getFinishedTime(),"yyyy-MM-dd HH:mm:ss"));
                atRow1.setValue(IATConstants.AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_DEALFLAG, IDealStatusConstants.UNDEAL);
                atRow1.save(null, null, null);
            }
        }
    }
}

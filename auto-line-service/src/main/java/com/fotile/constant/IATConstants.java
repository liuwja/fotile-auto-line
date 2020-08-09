package com.fotile.constant;

public interface IATConstants {
    String AT_TABLE_UNIT_PROPERTY="UnitProperty";							//unit扩展表
    String AT_COLUMN_UNIT_PROPERTY_SERIAL_NUMBER="serial_number";			//条码
    String AT_COLUMN_UNIT_PROPERTY_MERGER_NUMBER="merger_number";			//合并单号
    String AT_COLUMN_UNIT_PROPERTY_UNNORMAL_REASON="unnormal_reason";		//未正常下线备注原因
    String AT_COLUMN_UNIT_PROPERTY_UNNORMAL_USER="unnormal_user";			//未正常下线备注人
    String AT_COLUMN_UNIT_PROPERTY_AUTO_DOWN_MARK = "autoDownMark";		//自动化线是否下线标记
    String AT_COLUMN_UNIT_PROPERTY_SHIFT = "shift";		//班次
    String AT_COLUMN_UNIT_PROPERTY_DATE = "date";			//生产日期
    String AT_COLUMN_UNIT_PROPERTY_WORK_LENGTH = "work_length";			//时长
    String AT_COLUMN_UNIT_PROPERTY_CREATE_SHIFT = "create_shift";		//班次
    String AT_COLUMN_UNIT_PROPERTY_CREATE_DATE = "create_date";			//生产日期
    String AT_COLUMN_UNIT_PROPERTY_BOX_NUMBER ="box_number";		//装箱箱号
    String AT_COLUMN_UNIT_PROPERTY_FINISHED_USER = "finish_user";	///下线人
    String AT_COLUMN_UNIT_PROPERTY_CREATE_USER = "create_user";		//投产人
    String AT_COLUMN_UNIT_PROPERTY_PRODUCE_LENGTH = "produce_length";		//产品间隔时间(毫秒)
    String AT_COLUMN_UNIT_PROPERTY_PUZZLE_SERIAL_NUMBER = "puzzle_serial_number";	// SMT拼板条码


    String UDA_SHIFT_PL_NUMBER="pl_number";

    String AT_TABLE_SYSTEM_CONFIGURATION="SystemConfiguration";					//系统配置表
    String AT_COLUMN_SYSTEM_CONFIGURATION_PROP_NAME="prop_name";				//属性名
    String AT_COLUMN_SYSTEM_CONFIGURATION_PROP_VALUE="prop_value";				//属性值


    String AT_TABLE_MES_TO_WMS_ORDER_START = "MesToWmsOrderStart";	//开工标志同步记录表
    String AT_COLUMN_MES_TO_WMS_ORDER_START_ORDER_NUMBER = "order_number";	//工单号
    String AT_COLUMN_MES_TO_WMS_ORDER_START_ORDER_ITEM_NUMBER = "order_item_number";	//排程号
    String AT_COLUMN_MES_TO_WMS_ORDER_START_PLANNED_LINE = "planned_line";	//产线
    String AT_COLUMN_MES_TO_WMS_ORDER_START_SEND_TIME = "send_time";	//传输时间
    String AT_COLUMN_MES_TO_WMS_ORDER_START_DEALTIME = "dealtime";	//处理时间
    String AT_COLUMN_MES_TO_WMS_ORDER_START_DEALFLAG = "dealflag";	//处理标志 0 未处理 1 处理成功 2 处理失败
    String AT_COLUMN_MES_TO_WMS_ORDER_START_ORDER_TYPE = "order_type";	//订单类型
    String AT_COLUMN_MES_TO_WMS_ORDER_START_STATUS = "status";	//开工标示，1为投产，此时投产量为1
    String AT_COLUMN_MES_TO_WMS_ORDER_START_RESULTMSG = "resultmsg";	//处理信息
    String AT_COLUMN_MES_TO_WMS_ORDER_START_CREATION_TIME = "CREATION_TIME";	//投产时间



    String AT_TABLE_QM_CHECK_LIST="CheckList";         								//检验单
    String AT_COLUMN_QM_CHECK_LIST_CHECK_NAME="check_name";							//检验名称
    String AT_COLUMN_QM_CHECK_LIST_DEFECT_LIST_NAME="defect_list_name";				//缺陷list名称
    String AT_COLUMN_QM_CHECK_LIST_PART_CATEGORY="part_category";					//物料大类
    String AT_COLUMN_QM_CHECK_LIST_TEST_DEFINITION_NAME="test_definition_name";		//检验单名称
    String AT_COLUMN_QM_CHECK_LIST_STEP_NAME="step_name";							//工序名称
    String AT_COLUMN_QM_CHECK_LIST_FACTORY="factory";								//工厂

    String AT_TABLE_TEST_CODE_DEFINITION="TestCodeDefinition";						//缺陷，维修，检验项，名称代码维护表
    String AT_COLUMN_CONFIGURATION_TEST_NAME="test_name";						//名称
    String AT_COLUMN_CONFIGURATION_TEST_CODE="test_code";						//代码
    String AT_COLUMN_CONFIGURATION_TYPE="type";											//类型
    String AT_COLUMN_CONFIGURATION_FACTORY="factory";								//工厂
    String AT_COLUMN_CONFIGURATION_TEST_CLASS2="test_class2";						//二级分类


    String AT_TABLE_INSPECTS="Inspects";										//巡检记录表
    String AT_COLUMN_INSPECTS_DEFECT_LEVEL="defect_level";						//缺陷等级
    String AT_COLUMN_INSPECTS_DEFECT_MATERIAL="defect_material";				//不良物料
    String AT_COLUMN_INSPECTS_DEFECT_QTY="defect_qty";							//不良数量
    String AT_COLUMN_INSPECTS_DEFECT_RATE="defect_rate";						//不良率
    String AT_COLUMN_INSPECTS_DEFECT_TYPE="defect_type";						//不良类型
    String AT_COLUMN_INSPECTS_DUTY_MAN="duty_man";								//责任人
    String AT_COLUMN_INSPECTS_EQUIPMENT_NO="equipment_no";						//设备编号
    String AT_COLUMN_INSPECTS_FEEDING_QTY="feeding_qty";						//投料数量
    String AT_COLUMN_INSPECTS_FINDER="finder";									//发现人
    String AT_COLUMN_INSPECTS_FIND_TIME="find_time";							//发现时间
    String AT_COLUMN_INSPECTS_IMAGE="image";									//图片
    String AT_COLUMN_INSPECTS_INSPECT_DATE="inspect_date";						//巡检日期
    String AT_COLUMN_INSPECTS_INSPECT_MAN="inspect_man";						//巡检人
    String AT_COLUMN_INSPECTS_INSPECT_RESULT="inspect_result";					//检验结果
    String AT_COLUMN_INSPECTS_INSPECT_TYPE="inspect_type";						//巡检类型
    String AT_COLUMN_INSPECTS_MEASURE="measure";								//处理措施
    String AT_COLUMN_INSPECTS_NOTICE_MEN="notice_men";							//	通知人员
    String AT_COLUMN_INSPECTS_STEP_NAME="step_name";							//工序名称
    String AT_COLUMN_INSPECTS_UNIT_BARCODE="unit_barcode";						//产品主机条码
    String AT_COLUMN_INSPECTS_ORDER_NUMBER="order_number";						//工单编号
    String AT_COLUMN_INSPECTS_WORKCENTER="workcenter";							//车间
    String AT_COLUMN_INSPECTS_UNQUALIFIED_APPEARANCE="unqualified_appearance";	//不合格现象
    String AT_COLUMN_INSPECTS_FACTORY="factory";								//工厂
    String AT_COLUMN_INSPECTS_PRODUCTION_LINE="production_line";				//产线
    String AT_COLUMN_INSPECTS_PRODUCT="product_name";							//产品编号
    String AT_COLUMN_INSPECTS_GROUP_NAME="group_name";							//班组编号
    String AT_COLUMN_INSPECTS_SHIFT_NAME="shift_name";							//班次编号
    String AT_COLUMN_INSPECTS_ORDER_ITEM_NUMBER="order_item_number";			//排程号
    String AT_COLUMN_INSPECTS_STANDARD_SIZE="standard_size";					//标准尺寸
    String AT_COLUMN_INSPECTS_TRULY_SIZE="truly_size";							//实测尺寸
    String AT_COLUMN_INSPECTS_UNQUANTITY_QTY="unquantity_qty";					//不合格数量
    String AT_COLUMN_INSPECTS_TRACE_QTY="trace_qty";							//追溯数量
    String AT_COLUMN_INSPECTS_CHECKQTY="checkQty";								//
    String AT_COLUMN_INSPECTS_DEFECTQTY="defectQty";							//
    String AT_COLUMN_INSPECTS_GROUP1="group1";									//
    String AT_COLUMN_INSPECTS_GROUP2="group2";									//
    String AT_COLUMN_INSPECTS_GROUP3="group3";									//
    String AT_COLUMN_INSPECTS_IMPORT_TIME="import_time";						//录入时间
    String AT_COLUMN_INSPECTS_PROCESS_NODE="process_node";						//发生流程节点
    String AT_COLUMN_INSPECTS_BATCH="batch";									//批量大小
    String AT_COLUMN_INSPECTS_RESULT="result";									//质量后果
    String AT_COLUMN_INSPECTS_RISK_SCORE="risk_score";							//质量风险分数

    String AT_TABLE_STORAGE_ORDER = "StorageOrder";														//成品出入库单
    String AT_COLUMN_STORAGE_ORDER_STORAGE_NUMBER = "storage_number"; 			//成品出入库单号
    String AT_COLUMN_STORAGE_ORDER_STORAGE_TIME = "storage_time";						//成品出入库时间
    String AT_COLUMN_STORAGE_ORDER_STORAGE_TYPE = "storage_type";						//成品出入库单类型
    String AT_COLUMN_STORAGE_ORDER_ORDER_NUMBER = "order_number";			 		//工单号
    String AT_COLUMN_STORAGE_ORDER_SAP_ORDER_NUMBER = "sap_order_number";	//SAP日记账
    String AT_COLUMN_STORAGE_ORDER_STATUS = "status";												//当前状态
    String AT_COLUMN_STORAGE_ORDER_DESTINATION = "destination";							//发往地
    String AT_COLUMN_STORAGE_ORDER_DESTINATION_ID = "destination_id";					//发往地ID
    String AT_COLUMN_STORAGE_ORDER_LOCATION_TYPE = "location_type";								//仓库类型
    String AT_COLUMN_STORAGE_ORDER_STORAGE_ORDER_TYPE = "storage_order_type";		//日记账类型
    String AT_COLUMN_STORAGE_ORDER_USING_REASON = "using_reason";							//领用原因
    String AT_COLUMN_STORAGE_ORDER_USING_DEPARTMENT = "using_department";			//领用部门
    String AT_COLUMN_STORAGE_ORDER_USING_USER = "using_user";									//领用人
    String AT_COLUMN_STORAGE_ORDER_BACK_REASON = "back_reason";							//领用原因
    String AT_COLUMN_STORAGE_ORDER_BACK_DEPARTMENT = "back_department";			//领用部门
    String AT_COLUMN_STORAGE_ORDER_BACK_USER = "back_user";									//领用人
    String AT_COLUMN_STORAGE_ORDER_COMMENT = "comment";									//描述
    String AT_COLUMN_STORAGE_ORDER_SUPPLIER_NAME = "supplier_name";					//承运商

    String AT_TABLE_PART_FIX="Partnumberfix";				 //新旧物料编码对照表
    String AT_COLUMN_PART_FIX_NEW="new_part";		 //新物料编码
    String AT_COLUMN_PART_FIX_OLD="old_part";				 //旧物料编码
    String AT_COLUMN_PART_FIX_CREATE_USER="create_user";				 //新增修改人
    String AT_COLUMN_PART_FIX_CREATE_TIME="create_time";				 //创建时间
    String AT_COLUMN_PART_FIX_UPDATE_TIME="update_time";				 //更新时间

    String AT_TABLE_STORAGE_ORDER_DETAIL = "StorageOrderDetail";										//入库单明细
    String AT_COLUMN_STORAGE_ORDER_DETAIL_PART_DESCRIPTION = "part_description";		//物料名称
    String AT_COLUMN_STORAGE_ORDER_DETAIL_PART_NUMBER = "part_number";					//物料号
    String AT_COLUMN_STORAGE_ORDER_DETAIL_QUANTITY = "quantity";									//数量
    String AT_COLUMN_STORAGE_ORDER_DETAIL_QUANTITY_ORDERED = "quantity_ordered";	//计划数量
    String AT_COLUMN_STORAGE_ORDER_DETAIL_QUANTITY_SAP = "quantity_sap";					//过账数量
    String AT_COLUMN_STORAGE_ORDER_DETAIL_FROM_LOCATION = "from_location";			//从仓库
    String AT_COLUMN_STORAGE_ORDER_DETAIL_FROM_WMS_LOCATION = "from_wms_location";			//从储位
    String AT_COLUMN_STORAGE_ORDER_DETAIL_TO_LOCATION = "to_location";			//到仓库
    String AT_COLUMN_STORAGE_ORDER_DETAIL_TO_WMS_LOCATION = "to_wms_location";			//从到储位
    String AT_COLUMN_STORAGE_ORDER_DETAIL_R_NUMBER = "r_number";			//行号
    String AT_COLUMN_STORAGE_ORDER_DETAIL_SHIP_TIME = "ship_time";			//计划出库日期

    String AT_TABLE_PRINT_CONF = "PrintConf";
    String AT_COLUMN_PRINT_CONF_BARCODE_TEMPLATE = "barcode_template";
    String AT_COLUMN_PRINT_CONF_PART_DESCRIPTION = "part_description";
    String AT_COLUMN_PRINT_CONF_PART_NUMBER = "part_number";
    String AT_COLUMN_PRINT_CONF_TEMPLATE_TYPE = "template_type";
    String AT_COLUMN_PRINT_CONF_STATUS = "status";
    String AT_COLUMN_PRINT_CONF_CONFIRM_USER = "confirm_user";
    String AT_COLUMN_PRINT_CONF_CONFIRM_TIME = "confirm_time";

    String AT_TABLE_PRINT_PARAM = "PrintParam";
    String AT_COLUMN_PRINT_PARAM_PARAM_NAME = "param_name";
    String AT_COLUMN_PRINT_PARAM_PARAM_VALUE = "param_value";

    String AT_TABLE_PRINT_RECORD = "PrintRecord";								//打印记录表
    String AT_COLUMN_PRINT_RECORD_BARCODE = "barcode";							//条码
    String AT_COLUMN_PRINT_RECORD_PRINT_TYPE = "print_type";					//条码类型
    String AT_COLUMN_PRINT_RECORD_ORDER_NUMBER = "order_number";				//工单号
    String AT_COLUMN_PRINT_RECORD_ORDER_ITEM_NUMBER = "order_item_number";		//排程号
    String AT_COLUMN_PRINT_RECORD_PART_DESCRIPTION = "part_description";		//物料名称
    String AT_COLUMN_PRINT_RECORD_PART_NUMBER = "part_number";					//物料代码
    String AT_COLUMN_PRINT_RECORD_PRODUCTION_LINE_NAME = "production_line_name";//产线名称
    String AT_COLUMN_PRINT_RECORD_RECORD_TYPE = "record_type";					//打印类型
    String AT_COLUMN_PRINT_RECORD_SEQUENCE_NUMBER = "sequence_number";			//序列号
    String AT_COLUMN_PRINT_RECORD_IS_PRINT = "is_print";						//是否打印
    String AT_COLUMN_PRINT_RECORD_PRINT_CONTENT = "print_content";				//打印内容
    String AT_COLUMN_PRINT_RECORD_STATUS = "status";							//条码是否已经使用(暂只供部装批次下线工单使用)

    String AT_TABLE_MES_TO_WMS_UNIT_DOWNLINE = "MesToWmsUnitDownline";	//主机下线同步记录表
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_SERIAL_NUMBER = "serial_number";	//主机条码
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_PART_NUMBER = "part_number";	//物料号
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_PART_DESCRIPTION = "part_description";	//物料名称
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_STATUS = "status";	//当前状态，入库状态
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_DESCRIPTION = "description";	//入库描述
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_ORDER_NUMBER = "order_number";	//工单号
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_ADDR_CITY = "addr_city";	//工厂
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_STRAT_TIME = "start_time";	//投产时间
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_FINISHED_TIME = "finished_time";	//入库时间
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_DEALFLAG = "dealflag";	//处理标志
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_DEALTIME = "dealtime";	//处理时间
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_RESULTMSG = "resultmsg";	//处理信息
    String AT_COLUMN_MES_TO_WMS_UNIT_DOWNLINE_PLANNED_LINE = "planned_line";	//产线
}

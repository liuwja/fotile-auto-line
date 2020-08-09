package com.fotile.constant;

public interface IUDADefinition {
    String UDA_SHIFT_ON_SHIFT_TIME = "on_shift_time";				//上班时间
    String UDA_SHIFT_OFF_SHIFT_TIME = "off_shift_time";			//下班时间
    String UDA_SHIFT_ON_REST_TIME_1 = "on_rest_time_1";		//休息开始时间1
    String UDA_SHIFT_OFF_REST_TIME_1 = "off_rest_time_1";		//休息结束时间1
    String UDA_SHIFT_ON_REST_TIME_2 = "on_rest_time_2";		//休息开始时间2
    String UDA_SHIFT_OFF_REST_TIME_2 = "off_rest_time_2";		//休息结束时间2
    String UDA_SHIFT_ON_REST_TIME_3 = "on_rest_time_3";		//休息开始时间3
    String UDA_SHIFT_OFF_REST_TIME_3 = "off_rest_time_3";		//休息结束时间3
    String UDA_SHIFT_ON_REST_TIME_4 = "on_rest_time_4";		//休息开始时间4
    String UDA_SHIFT_OFF_REST_TIME_4 = "off_rest_time_4";		//休息结束时间4
    String UDA_SHIFT_AREA_NAME="area_name";							//车间编号
    String UDA_SHIFT_PL_NUMBER="pl_number";							//产线编号
    String UDA_SHIFT_FACTORY="factory";							//工厂
    String UDA_SHIFT_TOTAL_REST_TIME = "rest_time";				//总休息时间
    String UDA_SHIFT_DAY_DEADLINE="day_deadline";				//白班截止时间
    String UDA_SHIFT_NIGHT_DEADLINE="night_deadline";			//晚班截止时间


    String UDA_SYSTEM_CONFIG_PLINE="PLINE";
    String UDA_SYSTEM_CONFIG_UNIT_TIME="UNITTIME";

    int UDA_ORDER_ITEM_SHIFT = 2;				//班次

    int UDA_UNIT_STATUS = 0;			//状态
    int UDA_UNIT_STATE = 1;			//描述
    int UDA_UNIT_FREEZE_USER_NAME = 2;		//冻结人
    int UDA_UNIT_SEQUENCE = 3;			//首件、末件
    int UDA_UNIT_FQC=4;			//FQC检验
    int UDA_UNIT_OQC=5;			//OQC检验
    int UDA_UNIT_STORAGE_ORDER_NUMBER  = 6;	//出入日记账
    int UDA_UNIT_OQC_MERGER=7;			//OQC批量检验标识
    int UDA_UNIT_BACK_ORDER_NUMBER = 8;			//退库单号
    int UDA_UNIT_BACK_STATUS=9;				//退库检验状态


    int UDA_OPERATION_STEP_TYPE = 3;//工序类型
    int UDA_PART_CATEGORY = 0;								//物料大类

    int UDA_DEFECT_AND_REPAIR_ENTRY_DEFECT_REASON = 0;
    int UDA_DEFECT_AND_REPAIR_ENTRY_DEFECT_CATEGORY = 1;
    int UDA_DEFECT_AND_REPAIR_ENTRY_DEFECT_DEFECT_REASON_CLASS = 2;	//不良原因分类
    int UDA_DEFECT_AND_REPAIR_ENTRY_DEFECT_DISTRICT = 3;//国内国外
    int UDA_DEFECT_AND_REPAIR_ENTRY_DEFECT_ACCOUNTNAME = 4;//供应商名称
    int UDA_DEFECT_AND_REPAIR_ENTRY_NEW_PRODUCT	=	5;		//新品，量产
    int UDA_DEFECT_AND_REPAIR_ENTRY_GAS_SOURCE	=	6;		//气源
    int UDA_DEFECT_AND_REPAIR_ENTRY_PARTS	=	7;			//零部件
    int UDA_DEFECT_AND_REPAIR_ENTRY_VALIDATION = 8;	//验证结论
    int UDA_DEFECT_AND_REPAIR_ENTRY_MARK = 9;				//备注
    int UDT_DEFECT_AND_REPAIR_ENTRY_DEFECT_TIME = 0;		//添加不良时间
    int UDT_DEFECT_AND_REPAIR_ENTRY_REPAIR_TIME = 1;			//维修时间
    int UDT_DEFECT_AND_REPAIR_ENTRY_VALIDATE_TIME = 2;	//审核时间
    String UDA_DEFECT_AND_REPAIR_ENTRY_ISNEW_END_TIME="isnew_end_time";  //新品结束时间
    String UDA_DEFECT_AND_REPAIR_ENTRY_DUTY_NAME="duty_name"; //责任单位
    String UDA_DEFECT_AND_REPAIR_ENTRY_DUTY_FACTORY="duty_factory"; //责任工厂


    int UDT_ORDER_PUBLISH_TIME = 0;			//交货日期

    int UDA_PRODUCTION_LINE_COMPANY = 0;					//公司
    int UDA_PRODUCTION_LINE_FACTORY = 1;					//工厂
    int UDA_PRODUCTION_LINE_AREA = 2;						//车间
    int UDA_PRODUCTION_LINE_PERSON = 3;						//责任人
    int UDA_PRODUCTION_PACKAGE_PRINTER = 4;					//包装打印机名称
    String UDA_PRODUCTION_LINE_IS_RECORD="is_record";		//是否记录停线
    String UDA_PRODUCTION_LINE_DISRATE = "disrate";			//挂具间隔
    String UDA_PRODUCTION_LINE_IS_DOWN_LINE = "is_down_line";	//是否强制停线
    String UDA_PRODUCTION_LINE_CHILD_LINE = "child_line";		//子产线
    String UDA_PRODUCTION_LINE_IS_AN_DENG = "is_an_deng"; //是否安灯产线

    int UDA_WORK_CENTER_COMPUTER = 0;			//机器名
    int UDA_WORK_CENTER_URL = 1;						//URL
    int UDA_WORK_CENTER_PRINT = 2;					//打印机配置
    int UDA_WORK_CENTER_SEQUENCE = 3;			//部装顺序
    int UDA_PEOPLE_WORK_CENTER = 4;			//是否人工工作中心
}

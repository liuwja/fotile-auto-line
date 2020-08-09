package com.fotile.constant;

public interface IUnitStatus {
    String CREATED				=	"未投产";
    String STARTING 				= "生产中";
    String REPAIRING 			= "维修中";
    String REPAIRED				= "维修完成";
    String WAIT_TESTING 		= "等待OQC检验";
    String UNCONSUMED 	= "未消耗";
    String CONSUMED			= "已消耗";
    String TESTING					= "质检中";
    String FREEZING				=	"冻结";
    String TEST_PASSED			= 	"检验合格";
    String STORAGING			= 	"已入库";
    String SCRAPPED				= 	"已报废";
    String SHIPPED				= 	"已出库";
    String DISASSEMBLY			= 	"已拆机";
    String REWORK				= 	"返工";
    String NOSTARTING           =   "未投产";
    String NORMAL				=	"正常";
}

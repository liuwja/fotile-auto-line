package com.fotile.bean;

import com.datasweep.compatibility.client.ATDefinition;
import com.datasweep.compatibility.client.ATRow;
import com.datasweep.compatibility.client.DependentATRow;
import com.datasweep.compatibility.ui.Time;
import com.fotile.constant.IATConstants;
import com.fotile.constant.IPrintParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrintInfo {
    String serialNumber;

    String printType;

    boolean isReprint;

    String command;

    String printerName;

    String partNumber;

    String printSoftLocation;

    String parameters;

    public String getPrintSoftLocation()
    {
        return printSoftLocation;
    }

    public void setPrintSoftLocation(String printSoftLocation)
    {
        this.printSoftLocation = printSoftLocation;
    }

    public PrintInfo(String serialNumber, String printType, boolean isReprint, String printerName, ATRow uiPrintConf, Time time, int printCount)
    {
        String barcodeTemplate = "EMPTY";
        this.partNumber = "EMPTY";
        if(uiPrintConf != null)
        {
            barcodeTemplate = (String) uiPrintConf.getValue(IATConstants.AT_COLUMN_PRINT_CONF_BARCODE_TEMPLATE);
            this.partNumber = (String) uiPrintConf.getValue(IATConstants.AT_COLUMN_PRINT_CONF_PART_NUMBER);
        }
        this.serialNumber = serialNumber;
        this.printType = printType;
        this.isReprint = isReprint;
        this.printerName = printerName;

        int year = time.getYear();
        int month = time.getMonth();
        int day = time.getDay();

        StringBuffer sb = new StringBuffer();

        sb.append("<PRINTER_NAME>");
        sb.append(printerName);
        sb.append("</PRINTER_NAME>");

        sb.append("<DATE_CODE>"+year+"年"+month+"月"+day+"日</DATE_CODE>");
        sb.append("<UNIT_SN>"+serialNumber+"</UNIT_SN>");
        sb.append("<TEMPLATE_NAME>");
        sb.append(barcodeTemplate);
        sb.append("</TEMPLATE_NAME>");

        if(uiPrintConf != null)
        {
            Map<String,String> paramMap = new HashMap<>();
            ATDefinition depATDefinition = uiPrintConf.getATDefinition().getDependentATDefinition(IATConstants.AT_TABLE_PRINT_PARAM);
            List<DependentATRow> uiPrintParams = (List<DependentATRow>) uiPrintConf.getDetailRows(depATDefinition).getResult();
            for (int i = 0; i < uiPrintParams.size(); i++)
            {
                DependentATRow dependentATRow= uiPrintParams.get(i);
                String name = (String) dependentATRow.getValue(IATConstants.AT_COLUMN_PRINT_PARAM_PARAM_NAME);
                String value = (String) dependentATRow.getValue(IATConstants.AT_COLUMN_PRINT_PARAM_PARAM_VALUE);
                paramMap.put(name ,value);
            }
            paramMap.put(IPrintParameter.PRINT_COUNT, String.valueOf(printCount));
            Set<String> paramNames = paramMap.keySet();
            for (String paramName : paramNames)
            {
                sb.append("<"+paramName+">");
                sb.append(paramMap.get(paramName));
                sb.append("</"+paramName+">");

                if(parameters == null)
                {
                    parameters = paramName;
                }
                else
                {
                    parameters += "#@#"+paramName;
                }
            }
        }

        this.command = sb.toString();
    }

    public String getParameters()
    {
        return parameters;
    }

    public String getSerialNumber()
    {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    public String getPrintType()
    {
        return printType;
    }

    public void setPrintType(String printType)
    {
        this.printType = printType;
    }

    public boolean isReprint()
    {
        return isReprint;
    }

    public void setReprint(boolean isReprint)
    {
        this.isReprint = isReprint;
    }

    public String getCommand()
    {
        return command;
    }

    public String getPartNumber()
    {
        return partNumber;
    }

    public void setPartNumber(String partNumber)
    {
        this.partNumber = partNumber;
    }

    public void setCommand(String command)
    {
        this.command = command;
    }
}

package com.fotile.service.impl;

import com.datasweep.compatibility.client.Unit;
import com.fotile.Result;
import com.fotile.bean.TransferBean;
import com.fotile.proxy.ServerImplProxy;
import com.fotile.service.IAutoLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutoLineServiceImpl implements IAutoLineService {

    @Autowired
    ServerImplProxy proxy;
    @Override
    public Result binging(TransferBean bean) {
        String SerialNumber = bean.getSerialNumber();
        Unit unit = proxy.getUnitBySerialNumber(SerialNumber);
        if (unit==null){        //如果没有主机条码，则进行投产

        }
        return null;
    }
}

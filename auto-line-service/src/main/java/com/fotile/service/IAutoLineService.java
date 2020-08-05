package com.fotile.service;

import com.fotile.bean.Result;
import com.fotile.bean.TransferBean;

public interface IAutoLineService {
    /**
     * 关键件绑定
     * @param bean
     * @return
     */
    Result binging(TransferBean bean);

    /**
     * 主机条码投产
     * @param bean
     * @return
     */
    Result produce(TransferBean bean);
}

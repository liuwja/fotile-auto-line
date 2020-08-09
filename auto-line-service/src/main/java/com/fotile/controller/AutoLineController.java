package com.fotile.controller;


import com.fotile.bean.Result;
import com.fotile.bean.TransferBean;
import com.fotile.service.IAutoLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/autoLine")
public class AutoLineController {

    @Autowired
    IAutoLineService autoLineService;

    //绑定+投产
    @RequestMapping("binding")
    public Result binding(@RequestBody TransferBean bean){
        Result result = autoLineService.binging(bean);
        return result;
    }
    //检验
    @RequestMapping("quantityValidation")
    public Result quantityValidation(@RequestBody TransferBean bean){

        return autoLineService.quantityValidation(bean);
    }
    //包装下线
    @RequestMapping("packageDownLine")
    public Result packageDownLine(@RequestBody TransferBean bean){
        return autoLineService.packageDownLine(bean);
    }
}

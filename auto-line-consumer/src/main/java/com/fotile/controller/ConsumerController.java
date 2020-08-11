package com.fotile.controller;

import com.fotile.bean.Result;
import com.fotile.bean.TransferBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    String url = "http://auto-line-service/autoLine/";
    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("binding")
    public Object binding(@RequestBody TransferBean bean){
        return restTemplate.postForObject(url+"binding",bean,Result.class);
    }

    @RequestMapping("quantityValidation")
    public Result quantityValidation(@RequestBody TransferBean bean){

        return restTemplate.postForObject(url+"quantityValidation",bean,Result.class);
    }

    @RequestMapping("packageDownLine")
    public Result packageDownLine(@RequestBody TransferBean bean){
        return restTemplate.postForObject(url+"packageDownLine",bean,Result.class);
    }
}

package com.gupao.sprinmvc.v1;

import com.gupao.sprinmvc.annotation.YService;
import com.gupao.sprinmvc.v1.service.IDemoService;

@YService
public class DemoService implements IDemoService {

    @Override
    public String get(String name) {
        return "my name is"+name;
    }
}

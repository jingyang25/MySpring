package com.gupao.sprinmvc.v2;

import com.gupao.sprinmvc.annotation.YService;

@YService
public class DemoService implements IDemoService {

    @Override
    public String get(String name) {
        return "my name is"+name;
    }
}

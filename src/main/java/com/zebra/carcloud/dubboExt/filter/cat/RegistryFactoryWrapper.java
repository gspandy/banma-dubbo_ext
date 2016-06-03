package com.zebra.carcloud.dubboExt.filter.cat;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;

/**
 * Created by ying on 16/6/3.
 */
public class RegistryFactoryWrapper implements RegistryFactory {
    private RegistryFactory registryFactory;

    public RegistryFactoryWrapper(RegistryFactory registryFactory){
        this.registryFactory = registryFactory;
    }

    @Override
    public Registry getRegistry(URL url) {
        return new ParamRegistryWrapper(registryFactory.getRegistry(url));
    }
}

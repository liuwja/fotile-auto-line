package com.fotile.config;

import com.datasweep.compatibility.client.DatasweepException;
import com.datasweep.compatibility.client.ServerInfo;
import com.datasweep.compatibility.manager.ServerImpl;
import com.datasweep.plantops.proxies.ProxyFactory;
import com.fotile.proxy.ServerImplProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Configuration
public class AutoLineConfig {
    @Bean
    public ServerImpl server(){
        ServerImpl server=null;
        System.setProperty(ProxyFactory.J2EE_VENDOR_SYSTEM_PROPERTY,ProxyFactory.J2EE_VENDOR_JBOSS);
        try {
            Constructor<ServerImpl> constructor = ServerImpl.class.getDeclaredConstructor(ServerInfo.class);
            constructor.setAccessible(true);
            server = constructor.newInstance(new Object[]{new ServerInfo("jnp://10.250.58.35:1099","")});
            server.login("E78662","E78662");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (DatasweepException e) {
            e.printStackTrace();
        }
        return server;
    }
    @Bean
    public ServerImplProxy serverImplProxy(ServerImpl server){
        return new ServerImplProxy(server);
    }
}

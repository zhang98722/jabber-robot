package cn.shadowsong.eve.pla;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by warden on 2017/1/17.
 */
public class JabberQQAdapter {
    static Logger               logger                  = LoggerFactory.getLogger(JabberQQAdapter.class);

    public static String        lastPing                =null;

    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        logger.info("[JabberQQAdapter] service startup ...");

        //加载spring容器
        ApplicationContext context = null;
        try {
            context=new ClassPathXmlApplicationContext("spring-config.xml");
        }catch (Exception ex){
            logger.error("init spring container err", ex);
            return;
        }

        long e = System.currentTimeMillis();
        logger.info("[JabberQQAdapter] service startup ... end, elapse: " + (e - s) + " ms");

        synchronized (JabberQQAdapter.class) {
            while (true){
                try{
                    JabberQQAdapter.class.wait();
                }catch(Exception e1){
                    logger.error("wait err");
                }
            }

        }
    }
}

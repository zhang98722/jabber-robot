package cn.shadowsong.eve.pla;

import lombok.Getter;
import lombok.Setter;
import net.dongliu.requests.Requests;
import net.dongliu.requests.struct.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by warden on 2017/1/17.
 */
@Service("qqAgent")
public class QQAgent implements InitializingBean{
    static Logger                       logger                  = LoggerFactory.getLogger(QQAgent.class);

    static boolean                      selfQQ                  =false;

    static String                       httpClientUrl;

    @Getter @Setter String              groupId;

    static DatagramSocket               client;
    static InetAddress                  addr;
    static int                          port                    =18139;

    @Override
    public void afterPropertiesSet() throws Exception {
        if(selfQQ){
            addr=InetAddress.getByName("127.0.0.1");
        }
    }

    public static void sendMessage(long group,String msg){
        if(selfQQ){
            try{
                DatagramSocket client=new DatagramSocket();
                String formatMsg="SendGroupMessage|"+group+"|"+msg.replace("\\|","$内容分割$");
                byte[] bytes=formatMsg.getBytes("gb2312");
                DatagramPacket sendPacket = new DatagramPacket(bytes ,bytes.length , addr , port);
                client.send(sendPacket);
                client.close();
            }catch (Exception ex){
                logger.error("send message err,group:"+group+",msg:"+msg,ex);
            }
        }else {
            try {
                StringBuilder sb=new StringBuilder();
                sb.append(httpClientUrl);
                List<Parameter> parameterList=new ArrayList<>();
                parameterList.add(new Parameter("msg",msg));
                parameterList.add(new Parameter("groupId",String.valueOf(group)));
                String response=Requests.post(sb.toString()).charset("GB2312").params(parameterList).text().getBody();
                logger.debug("send qq msg,success:"+response);
            }catch (Exception ex){
                logger.error("send qq msg err,msg:"+msg,ex);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.finalize();
    }

    public void setSelfQQ(boolean selfQQ) {
        QQAgent.selfQQ = selfQQ;
    }

    public void setHttpClientUrl(String httpClientUrl) {
        QQAgent.httpClientUrl = httpClientUrl;
    }

    public static void main(String[] args) throws Exception {
        QQAgent qqAgent=new QQAgent();
        qqAgent.setSelfQQ(false);
        qqAgent.afterPropertiesSet();
        qqAgent.setHttpClientUrl("http://localhost:3147/ssAPI/jabber.ashx");
        while (true){
            QQAgent.sendMessage(149412937,"test");
            Thread.sleep(100000);
        }
    }
}

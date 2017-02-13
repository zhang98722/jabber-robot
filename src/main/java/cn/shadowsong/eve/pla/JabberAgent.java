package cn.shadowsong.eve.pla;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.ReconnectionStrategy;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.MessageEvent;
import rocks.xmpp.core.stanza.PresenceEvent;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.core.stanza.model.Presence;
import rocks.xmpp.extensions.chatstates.model.ChatState;
import rocks.xmpp.extensions.delay.model.DelayedDelivery;
import rocks.xmpp.extensions.muc.ChatRoom;
import rocks.xmpp.extensions.muc.ChatService;
import rocks.xmpp.extensions.muc.MultiUserChatManager;
import rocks.xmpp.im.chat.Chat;
import rocks.xmpp.im.roster.RosterManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by warden on 2017/1/17.
 */
@Service("jabberAgent")
public class JabberAgent implements InitializingBean{
    Logger                      logger                      = LoggerFactory.getLogger(JabberAgent.class);

    @Getter @Setter String      domain;
    @Getter @Setter String      host;
    @Getter @Setter String      username;
    @Getter @Setter String      password;
    @Getter @Setter String      source;
    @Getter @Setter Long        groupId;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.warn("jabber agent init");
        TcpConnectionConfiguration tcpConfiguration = TcpConnectionConfiguration.builder()
                .hostname(host)
                .port(5222)
                .sslContext(getTrustAllSslContext())
                .secure(true)
                .build();
        XmppSessionConfiguration reconnectConfig = XmppSessionConfiguration.builder()
                .reconnectionStrategy(ReconnectionStrategy.alwaysAfter(Duration.ofSeconds(10)))
                .build();

        XmppClient xmppClient = XmppClient.create(domain,reconnectConfig,tcpConfiguration);

        xmppClient.addInboundMessageListener(new Consumer<MessageEvent>() {
            @Override
            public void accept(MessageEvent messageEvent) {
                //过期消息抛弃
                DelayedDelivery delayedDelivery=messageEvent.getMessage().getExtension(DelayedDelivery.class);
                if(delayedDelivery!=null){
                    if(Date.from(delayedDelivery.getTimeStamp()).before(new Date(System.currentTimeMillis()-1000L*60))){
                        logger.debug("skip delivered message:"+messageEvent.getMessage().toString());
                        return;
                    }
                }
                //状态变化
                ChatState chatState=messageEvent.getMessage().getExtension(ChatState.class);
                if(chatState!=null&&messageEvent.getMessage().getBody()==null){
                    logger.debug("skip chatState message:"+messageEvent.getMessage().toString());
                    return;
                }
                //去除一般指令
                String body=messageEvent.getMessage().getBody();
                if(body!=null&&body.startsWith("!")){
                    logger.debug("skip command message:"+messageEvent.getMessage().toString());
                    return;
                }
                //转发
                StringBuilder sb=new StringBuilder();
                if (Message.Type.CHAT.equals(messageEvent.getMessage().getType())){
                    sb.append("~~~来舰队了!如果是Strat-OP，请所有人停止刷怪，立即到1DQ1进组，听不懂英文可以上YY36681226有翻译!~~~\n");
                    sb.append(body);
                    JabberQQAdapter.lastPing=body;
                    sb.append("\n");
                    sb.append("\n");
                    sb.append("~~~来舰队了!如果是Strat-OP，请所有人停止刷怪，立即到1DQ1进组，听不懂英文可以上YY36681226有翻译!~~~\n");
                    sb.append(body);
                    sb.append("\n");
                    sb.append("\n");
                    sb.append("~~~来舰队了!如果是Strat-OP，请所有人停止刷怪，立即到1DQ1进组，听不懂英文可以上YY36681226有翻译!~~~\n");
                    sb.append(body);

                }else if(Message.Type.GROUPCHAT.equals(messageEvent.getMessage().getType())){
                    logger.debug("skip group message:"+messageEvent.getMessage().toString());
                    return;
//                    sb.append("转发群组信息：\n");
//                    sb.append("from："+messageEvent.getMessage().getFrom().toString());
//                    sb.append("\n");
//                    sb.append(body);
                }
                String msg=sb.toString();
                QQAgent.sendMessage(groupId,msg);
                logger.info("get msg:"+messageEvent.getMessage().toString());
                logger.info("send msg:"+msg);
            }
        });

        xmppClient.connect();
        xmppClient.login(username,password,source);


        MultiUserChatManager multiUserChatManager = xmppClient.getManager(MultiUserChatManager.class);
        ChatService chatService=multiUserChatManager.createChatService(Jid.of("conference.goonfleet.com"));
        ChatRoom dragonFleet=chatService.createRoom("dragonfleet");
        ChatRoom pla=chatService.createRoom("pla");
        dragonFleet.enter("pla_bot");
        pla.enter("pla_bot");
        logger.warn("jabber agent init success");
    }

    protected static SSLContext getTrustAllSslContext() throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        }, new SecureRandom());
        return sslContext;
    }
}

package com.example.smacktest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PresenceBuilder;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            InetAddress ia = InetAddress.getLocalHost();
            String str = ia.getHostAddress();
            System.out.println( str);
            Log.d("hostfind", str);
        } catch (UnknownHostException e) {
        }
        try {
            xmppConfig();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }




    private void xmppConfig() throws SmackException, IOException, XMPPException, InterruptedException {

        XMPPTCPConnectionConfiguration configuration = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword("admin@localhost", "pass")
                .setHostAddress(InetAddress.getByName("127.0.0.1"))
                .setXmppDomain(JidCreate.domainBareFrom("127.0.0.1"))
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setPort(5443)
                .build();

        AbstractXMPPConnection connection = new XMPPTCPConnection(configuration);
        Presence presence = PresenceBuilder.buildPresence()
                .ofType(Presence.Type.unavailable)
                .setStatus("Gone fishing")
                .build();
        //Presence presence = new Presence(Presence.Type.unavailable);
        //presence.setStatus("Gone fishing");
        Log.d("Presence:", presence.getStatus().toString());
        // Send the stanza (assume we have an XMPPConnection instance called "con").
        connection.sendStanza(presence);

        connection.connect();
        connection.login();
        // Create a new presence. Pass in false to indicate we're unavailable._




        /*Message newMessage = new Message();
        newMessage.setBody("Howdy!");
        // Additional modifications to the message Stanza.
        JivePropertiesManager.addProperty(newMessage, "favoriteColor", "red");
        chat.send(newMessage);
        */

        connection.disconnect();
    }

    public void addIncomingMessageListener(XMPPTCPConnection connection) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) {
                System.out.println("New message from " + from + ": " + message.getBody());
                try {
                    chat.send(message.getBody());
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Assume a IncomingChatMessageListener we've setup with a ChatManager
    public void newIncomingMessage(EntityBareJid from, Message message, Chat chat) throws SmackException.NotConnectedException, InterruptedException {
        // Send back the same text the other user sent us.
        chat.send(message.getBody());
    }

    public void sendMessage(XMPPTCPConnection connection, String message, String to) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        try {
            Chat chat = chatManager.chatWith(JidCreate.entityBareFrom
                    (to + "@" + JidCreate.domainBareFrom("10.0.2.2"))); //format hard codings
            chat.send(message);
            Log.d("Sent","Message sent to user '{}' from user '{}'." + to +connection.getUser());

        } catch (XmppStringprepException | SmackException.NotConnectedException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
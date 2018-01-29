package com.RuiShiKeYan.dao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.PublicKey;

import com.RuiShiKeYan.Common.Method.LocalHostInfo;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

public class SSHLocalForward {

	private static final Object LOCK = new Object();

	//SSH账号信息
	//TODO 修改成你们自己的账号
	private  String SSH_SERVER_ADDRESS= "121.199.24.144";
	private String SSH_LOGIN_USER = "hm";
	private  String SSH_LOGIN_PASSWORD= "eql-LmnZ8xc9pxbg";

	//数据库服务器信息
	private  String MONGODB_INTERNAL_ADDRESS= LocalHostInfo.getHosturl();
	private  int MONGODB_INTERNAL_PORT= 3717;// ;
	private  int MONGODB_LOCAL_PORT= 3717;// ;


	private SSHClient client = null;

	public SSHLocalForward(String sshServerAddress, String sshLoginUser, String sshLoginPassword, String mongoLanAddress, int remotePort, int localPort) {
		SSH_SERVER_ADDRESS = sshServerAddress;
		SSH_LOGIN_USER = sshLoginUser;
		SSH_LOGIN_PASSWORD = sshLoginPassword;

		MONGODB_INTERNAL_ADDRESS = mongoLanAddress;
		MONGODB_INTERNAL_PORT = remotePort;
		MONGODB_LOCAL_PORT = localPort;
	}

	public SSHLocalForward(String mongoLanAddress)
	{
		MONGODB_INTERNAL_ADDRESS = mongoLanAddress;
	}
	/**
	 * 连接到SSH
	 * @throws IOException
	 */
	public void connectSSH() throws IOException {
		new Thread(new Runnable() {
			public void run() {
				connectToSSH();
			}
		}).start();

		synchronized (LOCK) {
			try {
				LOCK.wait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("如果没有报错，则SSH已连接，可连接localhost:"+MONGODB_INTERNAL_PORT+"到MongoDB...");
		Thread.yield();

	}

	public void closeSSH(){
		try {
			if (client != null) {
				client.close();
				client = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void connectToSSH(){
		try {
			if (client != null) {
				throw new RuntimeException("不能重复连接ssh...");
			}
			client = new SSHClient();

			client.addHostKeyVerifier(new HostKeyVerifier() {
				public boolean verify(String arg0, int arg1, PublicKey arg2) {
					return true; // don't bother verifying
				}
			});
			client.loadKnownHosts();

			client.connect(SSH_SERVER_ADDRESS);
			try {

				// client.authPublickey(System.getProperty("user.name"));
				client.authPassword(SSH_LOGIN_USER, SSH_LOGIN_PASSWORD);

	            /*
	            * _We_ listen on localhost:8080 and forward all connections on to server, which then forwards it to
	            * google.com:80
	            */
				final LocalPortForwarder.Parameters params
						= new LocalPortForwarder.Parameters("0.0.0.0", MONGODB_LOCAL_PORT, MONGODB_INTERNAL_ADDRESS, MONGODB_INTERNAL_PORT);
				final ServerSocket ss = new ServerSocket();
				ss.setReuseAddress(true);
				ss.bind(new InetSocketAddress(params.getLocalHost(), params.getLocalPort()));
				try {
					synchronized (LOCK) {
						LOCK.notify();
					}
					client.newLocalPortForwarder(params, ss).listen();
				} finally {
					ss.close();
				}
			} catch (Exception e) {
				try {
					client.disconnect();
					client = null;
				} catch (Exception e2) {
				}
			} finally {
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
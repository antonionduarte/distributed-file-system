package util.zookeeper;

import org.apache.zookeeper.*;
import tp1.server.common.JavaReplicatedDirectory;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Zookeeper {

	private ZooKeeper _client;
	private static Zookeeper instance;
	private static final String SERVERS = "kafka";

	private Zookeeper(String path) throws Exception {
		this.connect();
		createNode(path, new byte[0], CreateMode.PERSISTENT);
	}

	public static Zookeeper getInstance(String path) throws Exception {
		if (instance == null) {
			instance = new Zookeeper(path);
		}
		return instance;
	}

	public synchronized ZooKeeper client() {
		if (_client == null || !_client.getState().equals(ZooKeeper.States.CONNECTED)) {
			throw new IllegalStateException("ZooKeeper is not connected.");
		}
		return _client;
	}

	private void connect() throws IOException, InterruptedException {
		var connectedSignal = new CountDownLatch(1);
		int TIMEOUT = 5000;
		_client = new ZooKeeper(Zookeeper.SERVERS, TIMEOUT, (e) -> {
			System.err.println( e );
			if (e.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
				connectedSignal.countDown();
			}
		});
		connectedSignal.await();
	}

	public String createNode(String path, byte[] data, CreateMode mode) {
		try {
			return client().create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
		} catch (KeeperException.NodeExistsException e) {
			return null;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	public List<String> getChildren(String path) {
		try {
			return client().getChildren(path, false);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return Collections.emptyList();
	}

	public List<String> getChildren(String path, Watcher watcher) {
		try {
			return client().getChildren(path, watcher);
		} catch (Exception x) {
			x.printStackTrace();
		}
		return Collections.emptyList();
	}
}

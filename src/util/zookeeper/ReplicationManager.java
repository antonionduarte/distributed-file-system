package util.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.clients.ClientFactory;
import tp1.server.common.JavaReplicatedDirectory;
import tp1.server.operations.Operation;
import util.Secret;
import util.Token;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static tp1.server.rest.AbstractRestServer.SERVER_URI;

public class ReplicationManager implements Watcher {

	private URI primaryURI;
	private static ReplicationManager instance;
	private long version;
	private final Zookeeper zk;
	private final String node;
	private boolean primary;
	private static final String ROOT = "/directory";
	private final JavaReplicatedDirectory directory;

	private ReplicationManager(JavaReplicatedDirectory directory) {
		try {
			zk = Zookeeper.getInstance(ROOT);
			node = zk.createNode(ROOT + RestDirectory.PATH + "_", SERVER_URI.getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
			zk.getChildren(ROOT, this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		primary = false;
		version = 0;
		this.directory = directory;
	}

	public static ReplicationManager getInstance(JavaReplicatedDirectory directory) {
		if (instance == null)
			instance = new ReplicationManager(directory);
		return instance;
	}

	public long getCurrentVersion() {
		return version;
	}

	public void setVersion(long newVersion) {
		version = newVersion;
	}

	public void incVersion() {
		++version;
	}

	public boolean isSecondary() {
		return !primary;
	}

	public URI getPrimaryURI() {
		return primaryURI;
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getType() == Event.EventType.NodeChildrenChanged) {
			List<String> orderedNodes = new LinkedList<>(zk.getChildren(ROOT));
			Collections.sort(orderedNodes);
			String primaryNode = ROOT + "/" + orderedNodes.get(0);

			primary = node.equals(primaryNode);

			URI oldPrimaryURI = null;
			try {
				if (primaryURI != null) 
					oldPrimaryURI = new URI(primaryURI.toString());
				primaryURI = new URI(new String(zk.client().getData(primaryNode, false, new Stat())));
			} catch (URISyntaxException | KeeperException | InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			//primary died
			if (oldPrimaryURI != null && !oldPrimaryURI.equals(primaryURI)) {
				Set<Directory> clients = ClientFactory.getInstance().getOtherDirectoryClients();
				List<Operation> biggestList = null;
				for (Directory client : clients) {
					var res = client.getOperations(version, Token.generate(Secret.get()));
					if (biggestList == null || (res != null && res.isOK() && res.value().size() > biggestList.size()))
						biggestList = res.value();
				}
				if (biggestList != null)
					directory.executeOperations(biggestList);
			}
		}
	}
}

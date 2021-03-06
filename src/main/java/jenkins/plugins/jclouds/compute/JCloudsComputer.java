package jenkins.plugins.jclouds.compute;

import hudson.model.Hudson;
import hudson.model.Slave;
import hudson.slaves.OfflineCause;
import hudson.slaves.SlaveComputer;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * JClouds version of Jenkins {@link SlaveComputer} - responsible for terminating an instance.
 * 
 * @author Vijay Kiran
 */
public class JCloudsComputer extends SlaveComputer {

	private static final Logger LOGGER = Logger.getLogger(JCloudsComputer.class.getName());

	public JCloudsComputer(Slave slave) {
		super(slave);
	}

	public String getInstanceId() {
		return getName();
	}

	@Override
	public JCloudsSlave getNode() {
		return (JCloudsSlave) super.getNode();
	}

	public int getRetentionTime() {
		return getNode().getRetentionTime();
	}

	public String getCloudName() {
		return getNode().getCloudName();
	}

	/**
	 * Really deletes the slave, by terminating the instance.
	 */
	@Override
	public HttpResponse doDoDelete() throws IOException {
		setTemporarilyOffline(true, OfflineCause.create(Messages._DeletedCause()));
		getNode().setPendingDelete(true);
		return new HttpRedirect("..");
	}

	/**
	 * Delete the slave, terminate the instance. Can be called eitehr by doDoDelete() or from JCloudsRetentionStrategy.
	 */
	public void deleteSlave() throws IOException {
		LOGGER.info("Terminating " + getName() + " slave");
		JCloudsSlave slave = getNode();
		if (slave.getChannel() != null) {
			slave.getChannel().close();
		}
		slave.terminate();
		Hudson.getInstance().removeNode(slave);
	}
}

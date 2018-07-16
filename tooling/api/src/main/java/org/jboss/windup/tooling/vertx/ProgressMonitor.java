package org.jboss.windup.tooling.vertx;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jboss.windup.tooling.WindupToolingProgressMonitor;

public class ProgressMonitor extends UnicastRemoteObject implements WindupToolingProgressMonitor, Remote {

	private static final long serialVersionUID = 1L;

	private static Logger LOG = Logger.getLogger(ProgressMonitor.class.getName());
    
    public ProgressMonitor() throws RemoteException {
    }

	@Override
	public void beginTask(String task, int totalWork) throws RemoteException {
		LOG.info("beginTask: " + task + "totalWork: " + totalWork);
	}

	@Override
	public void done() throws RemoteException {
		LOG.info("done");
	}

	@Override
	public boolean isCancelled() throws RemoteException {
		return false;
	}

	@Override
	public void setCancelled(boolean value) throws RemoteException {
		LOG.info("cancelled: " + value);
	}

	@Override
	public void setTaskName(String name) throws RemoteException {
		LOG.info("setTaskName: " + name);
	}

	@Override
	public void subTask(String name) throws RemoteException {
		LOG.info("setTask: " + name);
	}

	@Override
	public void worked(int work) throws RemoteException {
		LOG.info("worked: " + work);
	}

	@Override
	public void logMessage(LogRecord logRecord) {
		LOG.info("logMessage: " + logRecord.getMessage());
	}
}
package org.jboss.windup.tooling.vertx;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.LogRecord;

import org.jboss.windup.tooling.IProgressMonitorAdapter;
import org.jboss.windup.tooling.WindupToolingProgressMonitor;

public class ProgressMonitor extends UnicastRemoteObject implements WindupToolingProgressMonitor, Remote {

	private static final long serialVersionUID = 1L;

	private final IProgressMonitorAdapter delegate;
    
    public ProgressMonitor(IProgressMonitorAdapter delegate) throws RemoteException {
		this.delegate = delegate;
	}

	@Override
	public void beginTask(String task, int totalWork) throws RemoteException {
		delegate.beginTask(task, totalWork);
	}

	@Override
	public void done() throws RemoteException {
		delegate.done();
	}

	@Override
	public boolean isCancelled() throws RemoteException {
		return delegate.isCancelled();
	}

	@Override
	public void setCancelled(boolean value) throws RemoteException {
		delegate.setCancelled(value);
	}

	@Override
	public void setTaskName(String name) throws RemoteException {
		delegate.setTaskName(name);	
	}

	@Override
	public void subTask(String name) throws RemoteException {
		delegate.subTask(name);
	}

	@Override
	public void worked(int work) throws RemoteException {
		delegate.worked(work);
	}

	@Override
	public void logMessage(LogRecord logRecord) {
		delegate.logMessage(logRecord);
	}
}
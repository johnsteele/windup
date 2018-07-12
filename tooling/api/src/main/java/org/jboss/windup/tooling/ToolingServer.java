package org.jboss.windup.tooling;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import javax.inject.Inject;

public interface ToolingServer
{
    void startServer(int port, String version);
}

package org.apache.helix.metamanager.managed;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.helix.metamanager.ClusterContainerProvider;
import org.apache.log4j.Logger;

public class ShellContainerProvider implements ClusterContainerProvider {

	static final Logger log = Logger.getLogger(ShellContainerProvider.class);
	
	static final String REQUIRED_TYPE = "container";
	static final String RUN_COMMAND = "/bin/sh";
	
	// global view of processes required
	static final Object staticLock = new Object();
	static final Map<String, ShellProcess> processes = new HashMap<String, ShellProcess>();

	final String zkAddress;
	final String clusterName;
	final String command;
	final String providerName;
	
	public ShellContainerProvider(String zkAddress, String clusterName, String owner, String command) {
		this.zkAddress = zkAddress;
		this.clusterName = clusterName;
		this.command = command;
		this.providerName = owner;
	}

	@Override
	public void create(String id, String type) throws Exception {
		synchronized (staticLock) {
			if(processes.containsKey(id))
				throw new IllegalArgumentException(String.format("Process '%s' already exists", id));
			
			if(!type.equals(REQUIRED_TYPE))
				throw new IllegalArgumentException(String.format("Type '%s' not supported", type));
			
			log.info(String.format("Running container '%s' (zkAddress='%s', clusterName='%s', command='%s')", id, zkAddress, clusterName, command));
			
			ProcessBuilder builder = new ProcessBuilder(RUN_COMMAND, command, zkAddress, clusterName, id);
			Process process = builder.start();
			
			processes.put(id, new ShellProcess(id, providerName, process));
		}
	}
	
	@Override
	public void destroy(String id) throws Exception {
		synchronized (staticLock) {
			if(!processes.containsKey(id))
				throw new IllegalArgumentException(String.format("Process '%s' does not exists", id));
			
			log.info(String.format("Destroying container '%s'", id));
			
			ShellProcess shell = processes.remove(id);
			shell.process.destroy();
		}
	}
	
	@Override
	public void destroyAll() {
		synchronized (staticLock) {
			log.info("Destroying all processes");
			for(ShellProcess process : new HashSet<ShellProcess>(processes.values())) {
				try { destroy(process.id); } catch (Exception ignore) {}
			}
		}
	}
	
	static class ShellProcess {
		final String id;
		final String owner;
		final Process process;

		public ShellProcess(String id, String owner, Process process) {
			this.id = id;
			this.owner = owner;
			this.process = process;
		}		
	}
}
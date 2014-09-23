package eu.linda.analytics.app;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import eu.linda.analytics.rest.MessageRestService;

public class MessageApplication extends Application {
	private Set<Object> singletons = new HashSet<Object>();

	public MessageApplication() {
		singletons.add(new MessageRestService());
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}

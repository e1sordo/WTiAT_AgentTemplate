package es.e1sordo.thesis.wtiat.agent;

import es.e1sordo.thesis.wtiat.agent.client.AgentHttpClient;
import es.e1sordo.thesis.wtiat.agent.dto.AgentPostDto;
import es.e1sordo.thesis.wtiat.agent.exceptions.TerminateException;
import es.e1sordo.thesis.wtiat.agent.util.SystemInfoReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Agent {

    final Logger logger = LoggerFactory.getLogger(Agent.class);

    private String name;
    private String id;
    private AgentHttpClient client;
    private boolean isRegistered;

    public Agent(String name, String id) {
        logger.info("Running new instance for agent with name = {} and id = {}", name, id);

        this.name = name;
        this.id = id;
        this.client = new AgentHttpClient();
    }

    private void exchange() {
        logger.info("Health check exchange message is generated");

        final var registerBody = new AgentPostDto();
        registerBody.setName(name);
        registerBody.setId(id);
        registerBody.setIp(SystemInfoReceiver.getSystemIpAddress());
        registerBody.setPid((int) ProcessHandle.current().pid());

        var response = client.registerAgent(registerBody);

        isRegistered = true;
        name = response.getName();

        final var idFromDb = response.getId();
        if (!this.id.equals(idFromDb)) {
            logger.error("ID that was specified when starting the agent does not exist in the database or is already taken by another launching agent.");
            logger.info("ID has been changed from {} to {}", id, idFromDb);
            this.id = idFromDb;
        }

        if (response.getShouldTerminate()) {
            throw new TerminateException("The server initiated the agent's shutdown.");
        }
    }

    public static void main(String[] args) {

        // validate args
        final var name = args[0];
        final var id = args.length > 1 ? args[1] : "";
        final var instance = new Agent(name, id);


        while (true) {

            instance.exchange();

            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

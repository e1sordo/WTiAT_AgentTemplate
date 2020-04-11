package es.e1sordo.thesis.wtiat.agent;

import es.e1sordo.thesis.wtiat.agent.client.AgentHttpClient;
import es.e1sordo.thesis.wtiat.agent.dto.AgentPostDto;
import es.e1sordo.thesis.wtiat.agent.dto.DeviceGetDto;
import es.e1sordo.thesis.wtiat.agent.exceptions.TerminateException;
import es.e1sordo.thesis.wtiat.agent.threads.MetricsBatchSender;
import es.e1sordo.thesis.wtiat.agent.threads.MetricsCollector;
import es.e1sordo.thesis.wtiat.agent.util.SystemInfoReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

import static java.util.Optional.ofNullable;

public class Agent {

    final Logger logger = LoggerFactory.getLogger(Agent.class);

    private String name;
    private String id;
    private DeviceGetDto assignedDevice;
    private AgentHttpClient client;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledGatheringFuture;
    private ScheduledFuture<?> scheduledBatchFuture;

    private ConcurrentLinkedQueue<Map<String, String>> metricsQueue;

    private boolean isRegistered;

    public Agent(String name, String id) {
        logger.info("Running new instance for agent with name = {} and id = {}", name, id);

        this.name = name;
        this.id = id;
        this.client = new AgentHttpClient();

        final var daemonThreadFactory = new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        };

        this.executorService = Executors.newScheduledThreadPool(2, daemonThreadFactory);

        metricsQueue = new ConcurrentLinkedQueue<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Try to close executor service...");
            cancelFutures(scheduledGatheringFuture, scheduledBatchFuture);
            this.executorService.shutdown();
        }));
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

        final var newAssignedDevice = response.getAssignedDevice();

        if (!Objects.equals(this.assignedDevice, newAssignedDevice)) {
            this.assignedDevice = newAssignedDevice;
            cancelFutures(scheduledGatheringFuture, scheduledBatchFuture);
            if (assignedDevice != null) {
                scheduledGatheringFuture = scheduleGatheringTask(assignedDevice.getGatheringFrequencyInMillis());
                scheduledBatchFuture = scheduleBatchTask(assignedDevice.getBatchSendingFrequencyInMillis());
            }
        }

        if (response.getShouldTerminate()) {
            throw new TerminateException("The server initiated the agent's shutdown.");
        }
    }

    private ScheduledFuture<?> scheduleBatchTask(Integer frequencyPeriodInMillis) {
        return executorService.scheduleAtFixedRate(
                new MetricsBatchSender(client, metricsQueue),
                0,
                frequencyPeriodInMillis,
                TimeUnit.MILLISECONDS);
    }

    private ScheduledFuture<?> scheduleGatheringTask(Integer frequencyPeriodInMillis) {
        return executorService.scheduleAtFixedRate(
                new MetricsCollector(metricsQueue),
                0,
                frequencyPeriodInMillis,
                TimeUnit.MILLISECONDS);
    }

    private void cancelFutures(ScheduledFuture<?>... futures) {
        for (ScheduledFuture<?> future : futures) {
            ofNullable(future).ifPresent(it -> it.cancel(true));
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

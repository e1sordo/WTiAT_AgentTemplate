package es.e1sordo.thesis.wtiat.agent;

import es.e1sordo.thesis.wtiat.agent.client.AgentHttpClient;
import es.e1sordo.thesis.wtiat.agent.dto.AgentPostDto;
import es.e1sordo.thesis.wtiat.agent.dto.DeviceGetDto;
import es.e1sordo.thesis.wtiat.agent.exceptions.TerminateException;
import es.e1sordo.thesis.wtiat.agent.model.TimestampMetric;
import es.e1sordo.thesis.wtiat.agent.threads.MetricsBatchSender;
import es.e1sordo.thesis.wtiat.agent.threads.MetricsCollector;
import es.e1sordo.thesis.wtiat.agent.util.ConnectorLoader;
import es.e1sordo.thesis.wtiat.agent.util.SystemInfoReceiver;
import es.e1sordo.thesis.wtiat.lib.ElectronicDevice;
import es.e1sordo.thesis.wtiat.lib.FailedConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static java.util.Optional.ofNullable;

public class Agent {

    final Logger logger = LoggerFactory.getLogger(Agent.class);

    private int retryCount = 0;

    private String name;
    private String id;
    private DeviceGetDto assignedDevice;

    private AgentHttpClient client;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> scheduledGatheringFuture;
    private ScheduledFuture<?> scheduledBatchFuture;

    private String currentLoadedConnectorName;
    private ElectronicDevice device;
    private ConcurrentLinkedQueue<List<TimestampMetric>> metricsQueue;

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
            logger.info("Try to close device connection...");
            if (device != null && device.isConnected()) device.disconnect();
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

        var response = client.pingAgent(registerBody);

        isRegistered = true;

        if (response == null) {
            retryCount++;
            logger.error("Failed to send heartbeat status. Number of unsuccessful attempts in a row: {}", retryCount);
            if (retryCount >= 5) {
                logger.error("The number of unsuccessful attempts in a row exceeded 5 and the agent process will be stopped");
                throw new TerminateException("Server is not responding.");
            }
            return;
        } else {
            retryCount = 0;
        }

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

                final String newConnectorName = assignedDevice.getConnectorName();
                if (newConnectorName != null) {
                    if (!Objects.equals(this.currentLoadedConnectorName, newConnectorName)) {
                        logger.info("Connector were changed since now");
                        this.currentLoadedConnectorName = newConnectorName;

                        final var connectorJarFile = client.downloadConnector(currentLoadedConnectorName);
                        device = loadDeviceConnector(connectorJarFile, assignedDevice.getConnectionValues());
                    }
                }

                if (device != null) {
                    logger.info("Connector for device {} was successfully loaded", assignedDevice.getName());
                    try {
                        device.connect();

                        scheduledGatheringFuture = scheduleGatheringTask(device,
                                assignedDevice.getMetrics(),
                                assignedDevice.getGatheringFrequencyInMillis());

                        scheduledBatchFuture = scheduleBatchTask(assignedDevice.getBatchSendingFrequencyInMillis());
                    } catch (FailedConnectionException e) {
                        logger.error("Unable to connect to device", e);
                    }
                }
            } else {
                if (device != null && device.isConnected()) device.disconnect();
            }
        }

        if (response.getShouldTerminate()) {
            throw new TerminateException("The server initiated the agent's shutdown.");
        }
    }

    @SuppressWarnings("unchecked")
    private ElectronicDevice loadDeviceConnector(File connectorJarFile, List<String> connectionValues) {
        Class mainClass = ConnectorLoader.loadJarAndGetMainClass(connectorJarFile);

        if (mainClass != null) {
            try {
                return (ElectronicDevice) mainClass.getDeclaredConstructor(List.class).newInstance(connectionValues);
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                logger.error("An error occurred while loading the connector " + connectorJarFile, ex);
            }
        }
        return null;
    }

    private ScheduledFuture<?> scheduleBatchTask(Integer frequencyPeriodInMillis) {
        return executorService.scheduleAtFixedRate(
                new MetricsBatchSender(client, assignedDevice.getId(), metricsQueue),
                frequencyPeriodInMillis,
                frequencyPeriodInMillis,
                TimeUnit.MILLISECONDS);
    }

    private ScheduledFuture<?> scheduleGatheringTask(ElectronicDevice device, List<List<String>> metrics,
                                                     Integer frequencyPeriodInMillis) {
        return executorService.scheduleAtFixedRate(
                new MetricsCollector(device, metrics, metricsQueue),
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

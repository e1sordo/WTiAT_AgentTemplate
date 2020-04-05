package es.e1sordo.thesis.wtiat.agent.configuration;

import es.e1sordo.thesis.wtiat.agent.model.GatewayInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.Integer.parseInt;

public class GatewayProperties {

    private static final Logger logger = LoggerFactory.getLogger(GatewayProperties.class);

    public static final String TARGET_PROPERTIES_FILE = buildTargetPropertiesFilePath();
    public static final String GATEWAY_PROTOCOL = "destination.gateway.protocol";
    public static final String GATEWAY_HOST = "destination.gateway.host";
    public static final String GATEWAY_PORT = "destination.gateway.port";

    private static final GatewayInfo gatewayInfo = new GatewayInfo();

    public static GatewayInfo getActualData() {
        try (InputStream input = new FileInputStream(TARGET_PROPERTIES_FILE)) {
            Properties prop = new Properties();
            prop.load(input);

            String gatewayProtocol = prop.getProperty(GATEWAY_PROTOCOL);
            String gatewayHost = prop.getProperty(GATEWAY_HOST);
            String gatewayPort = prop.getProperty(GATEWAY_PORT);

            if (gatewayProtocol != null && gatewayHost != null && gatewayPort != null)
                gatewayInfo
                        .setProtocol(gatewayProtocol)
                        .setHost(gatewayHost)
                        .setPort(parseInt(gatewayPort));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (gatewayInfo.getProtocol() == null || gatewayInfo.getHost() == null || gatewayInfo.getPort() == null) {
            logger.warn("Please provide both remote gateway protocol, host and port in target.properties file");
        }

        return gatewayInfo;
    }

    private static String buildTargetPropertiesFilePath() {
        var fileName = "target.properties";
        return System.getProperty("user.dir").endsWith("bin")
                ? "../" + fileName
                : fileName;
    }
}

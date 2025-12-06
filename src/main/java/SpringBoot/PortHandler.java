package SpringBoot;

import lombok.Getter;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PortHandler implements ApplicationListener<WebServerInitializedEvent> {
    @Getter
    private static int currentPort;
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        currentPort = event.getWebServer().getPort();
    }

}

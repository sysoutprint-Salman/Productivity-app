package SpringBoot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RestController;

import static javafx.application.Application.launch;

@SpringBootApplication
@RestController
public class Rest {
    public static ConfigurableApplicationContext context;

    //This class acts as the Rest controller which will handle JSON and launch SB.
    public static ConfigurableApplicationContext getApplicationContext() {
        return context;
    }
}

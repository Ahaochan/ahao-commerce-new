package moe.ahao.commerce.fulfill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringBoot方式启动类
 *
 * @author Ahaochan
 */
@SpringBootApplication(scanBasePackages = "moe.ahao")
public class FulfillApplication {
    private static final Logger logger = LoggerFactory.getLogger(FulfillApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication app = new SpringApplication(FulfillApplication.class);
            app.run(args);
            logger.info("{}启动成功!", FulfillApplication.class.getSimpleName());
        } catch (Exception e) {
            logger.error("{}启动失败!", FulfillApplication.class.getSimpleName(), e);
        }
    }
}

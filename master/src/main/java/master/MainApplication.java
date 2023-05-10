package master;

import master.zookeeper.Zookeeper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MainApplication {
//    private Zookeeper zk;

    public static void main(String[] args) throws Exception {
        System.out.println("this is ok");
        SpringApplication.run(MainApplication.class, args);
        Zookeeper zk = new Zookeeper();
        zk.connect();
    }
}

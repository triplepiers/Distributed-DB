package main;

import main.util.Zookeeper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SpringBootApplication
public class MasterApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MasterApplication.class, args);
        Zookeeper zk = new Zookeeper();
        zk.connect();
    }

    @RequestMapping("/")
    public String hello() {
        return "hello world";
    }

}

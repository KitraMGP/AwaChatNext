package kitra.awachat.next;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("kitra.awachat.next.mapper") // 添加这一行，指定Mapper接口所在的包
public class AwaChatNextApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwaChatNextApplication.class, args);
	}

}

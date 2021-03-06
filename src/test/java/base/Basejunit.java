/**
 * 
 */
package base;

import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author huangyakun
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(locations= {"file:src/main/resources/META-INF/spring/*.xml"})
@WebAppConfiguration
@SpringBootApplication
public class Basejunit {

}

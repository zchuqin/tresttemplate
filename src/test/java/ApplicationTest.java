import com.alibaba.fastjson.JSON;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import stoner.tresttemplate.Application;
import stoner.tresttemplate.bean.HallVo;
import stoner.tresttemplate.bean.ResponseObject;

import java.awt.image.ImageProducer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void get() {
//        ParameterizedTypeReference<ResponseObject<List<HallVo>>> responseObjectParameterizedTypeReference = new ParameterizedTypeReference<ResponseObject<List<HallVo>>>(){};
        ResponseObject responseObject = restTemplate.getForObject("http://szgk.sz-water.com.cn/api/wechat/op//Branch/All", ResponseObject.class);
        assert responseObject != null;
        System.out.println(responseObject.getData() instanceof List);
        List data = (List) (responseObject.getData());
        System.out.println(JSON.toJSONString(data.get(1)));
        System.out.println(JSON.toJSONString(responseObject));
    }

    @Test
    public void getForEntity() {
        ResponseEntity<ResponseObject> responseEntity = restTemplate.getForEntity("http://szgk.sz-water.com.cn/api/wechat/op//Branch/All", ResponseObject.class);
        HttpHeaders headers = responseEntity.getHeaders();
        headers.forEach((key, value) -> System.out.println(key + ":" + value));
        System.out.println(responseEntity.getStatusCode());
        System.out.println(responseEntity.getStatusCodeValue());
        ResponseObject body = responseEntity.getBody();
        System.out.println(JSON.toJSONString(body));
    }

    /**
     * application/x-www-form-urlencoded
     */
    @Test
    public void postForEntity() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("channel", "zzzd");
        map.add("openid", "Terminal-admin-001");
        map.add("customercode", "1387019444");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        ResponseEntity<ResponseObject> responseEntity = restTemplate.postForEntity("http://szgk.sz-water.com.cn/api/wechat/op/CustomerInfo/GetCustomerDetail", request, ResponseObject.class);
        HttpHeaders headers1 = responseEntity.getHeaders();
        headers1.forEach((key, value) -> System.out.println(key + ":" + value));
        System.out.println(responseEntity.getStatusCode());
        System.out.println(responseEntity.getStatusCodeValue());
        ResponseObject body = responseEntity.getBody();
        System.out.println(JSON.toJSONString(body));
    }

    /**
     * application/json
     */
    @Test
    public void postForEntity1() {
        Map<String,String> map = new HashMap<>();
        map.put("channel", "zzzd");
        map.put("openid", "Terminal-admin-001");
        map.put("customercode", "1387019444");

        ResponseEntity<ResponseObject> responseEntity = restTemplate.postForEntity("http://szgk.sz-water.com.cn/api/wechat/op/CustomerInfo/GetCustomerDetail", map, ResponseObject.class);
        HttpHeaders headers1 = responseEntity.getHeaders();
        headers1.forEach((key, value) -> System.out.println(key + ":" + value));
        System.out.println(responseEntity.getStatusCode());
        System.out.println(responseEntity.getStatusCodeValue());
        ResponseObject body = responseEntity.getBody();
        System.out.println(JSON.toJSONString(body));
    }

    @Test
    public void exchange() {
        Map<String,String> map = new HashMap<>();
        map.put("channel", "zzzd");
        map.put("openid", "Terminal-admin-001");
        map.put("customercode", "1387019444");
        HttpEntity<Object> httpEntity = new HttpEntity<>(map);

        ResponseEntity<ResponseObject<List<HallVo>>> responseEntity = restTemplate.exchange("http://szgk.sz-water.com.cn/api/wechat/op//Branch/All", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseObject<List<HallVo>>>() {
        }, map);
        HttpHeaders headers1 = responseEntity.getHeaders();
        headers1.forEach((key, value) -> System.out.println(key + ":" + value));
        System.out.println(responseEntity.getStatusCode());
        System.out.println(responseEntity.getStatusCodeValue());
        System.out.println(JSON.toJSONString(responseEntity.getBody()));
        HallVo hallVo = responseEntity.getBody().getData().get(0);
        System.out.println(hallVo == null);
        System.out.println(hallVo.getCounty());
    }
}

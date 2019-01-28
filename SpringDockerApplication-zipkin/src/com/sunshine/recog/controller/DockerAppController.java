package com.sunshine.recog.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.sunshine.recog.utils.Codecs;
/**
 * This user interface is not verify token is valid
 * just only decode playload
 * @author oyyl
 * @since 2019/1/18
 *
 */
@RestController
@RequestMapping("/api")
public class DockerAppController {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${app.service.url}")
	public String url;
	
	/**
	 * Parse JWT value
	 * JWT include three segement:
	 * The first is : header
	 * The second is : playload
	 * The last is: singer
	 * @param jwt
	 * @return
	 */
	private Map decodeToken(String jwt){
		String[] _jwt = jwt.split("\\.");
		String jwt_playload = Codecs.utf8Decode(Codecs.b64UrlDecode(_jwt[1].toString()));
		String jwt_header = Codecs.utf8Decode(Codecs.b64UrlDecode(_jwt[0].toString()));
		Map map = new HashMap<>();
		map.put("jwt_playload", jwt_playload);
		map.put("jwt_header", jwt_header);
		return map;
	}
	
	@RequestMapping(value = "/token/info", produces = "application/json")
	public Object test(HttpServletRequest request){
		String header_auth = request.getHeader("Authorization");
		String jwt = null;
		try {
			if(header_auth != null){
				// format: Bearer + 空格 + token
				String[] val = header_auth.split(" "); 
				if(val.length==2 && "Bearer".equals(val[0])){
					return decodeToken(val[1]);
				}
			}
			jwt = request.getParameter("jwt");
			if(jwt == null || "".equals(jwt)) 
				return "{\"msg\":\"Not provide jwt parameter\"}";
		} catch (Exception e) {
			String emsg = "Unkown Excepton," + e.getMessage();
			return "{\"msg\":\""+emsg+"\"}";
		}
		return decodeToken(jwt);
	}
	
	/**
	 * zipkin invoke trace test
	 * @return
	 */
	@RequestMapping(value = "/zipkin/test", produces = "application/json")
	public Object invokeAnotherService(){
		return restTemplate.getForEntity(url, String.class);
	}
}

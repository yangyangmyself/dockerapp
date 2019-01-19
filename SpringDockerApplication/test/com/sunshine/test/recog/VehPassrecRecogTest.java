package com.sunshine.test.recog;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartframework.common.utils.ExHttpClientUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sunshine.boot.VehicleDeckBootApplication;
/**
 * 二次识别单元测试
 * @author oyyl
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = \"classpath:/spring/applicationContext.xml\")
@SpringBootTest(classes=VehicleDeckBootApplication.class)
public class VehPassrecRecogTest {
	
	@Test
	public void passrecTest(){
		long btime = System.currentTimeMillis();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String _data = "{\"gcxh\": \"43010000000000000\", \"gateId\": \"441302102052939000\","
				+ "\"directionId\": \"4413020172\",\"driverWayId\": \"5\",\"driverWayType\": \"1\",\"licenseType\": \"02\","
				+ "\"passTime\": \"\",\"speed\": 20,\"licenseColor\": \"2\",\"carType\": \"0\",\"license\": \"云A11111\","
				+ "\"backLicense\": \"云A11111\",\"backLicenseColor\": \"2\",\"identical\": \"0\",\"carColor\": \"2\",\"limitSpeed\": 30,"
				+ "\"carBrand\": \"3\",\"carShape\": \"3\",\"travelStatus\": \"0\",\"violationFlag\": \"0\",\"picPath\": \"\","
				+ "\"picPath1\": \"http://10.45.102.221:8088/image/vrb2/i1/007318b242484d15bf8426c2ffb93c28/00050?key=6bc81edb=39376330=181640\","
				+ "\"picPath2\": \"\",\"picPath3\": \"AK47\",\"featurePic\": \"\",\"driverPic\": \"\",\"copilotPic\": \"\","
				+ "\"sendFlag\": \"0\",\"inputTime\": \"\",\"srcInputTime\": \"\"}";
		paramMap.put("data", _data);
		String url = "http://127.0.0.1:8080/api/rest/pass/recog";
		String sendPostResult = ExHttpClientUtils.doPost(url, paramMap);
		long etime = System.currentTimeMillis();
		long ttime = (etime -  btime);
		System.out.println("查询时间："+ttime+"--- sendPostResult"+sendPostResult);
	}
}


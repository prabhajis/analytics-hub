package org.wso2telco.analytics.sparkUdf;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class IPRange {

	public Boolean checkIpRange(String min, String max, String xForward){
		List<String> ipList = Arrays.asList(xForward.split(","));
		Boolean status = false;
		for(String ip : ipList){
			try {
				long ipLo = ipToLong(InetAddress.getByName(min));
				long ipHi = ipToLong(InetAddress.getByName(max));
				long ipToTest = ipToLong(InetAddress.getByName(ip.trim()));
				if (ipToTest >= ipLo && ipToTest <= ipHi){
					return true;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return false;
			}
		}
		return status;

	}

	public static long ipToLong(InetAddress ip) {
		byte[] octets = ip.getAddress();
		long result = 0;
		for (byte octet : octets) {
			result <<= 8;
			result |= octet & 0xff;
		}
		return result;
	}
}

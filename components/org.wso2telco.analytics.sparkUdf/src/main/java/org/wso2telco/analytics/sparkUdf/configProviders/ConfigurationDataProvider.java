package org.wso2telco.analytics.sparkUdf.configProviders;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.wso2.carbon.utils.CarbonUtils;


public class ConfigurationDataProvider {

	
	private File xml;
	private SAXBuilder builder;
	private Document document;
	private Element rootNode;
	private String url;
	private String password;	
	private String uname;	
	private String apiKey;	
	private String apiSecret;	

	

	public ConfigurationDataProvider() throws URISyntaxException, JDOMException, IOException {
		String filePath=CarbonUtils.getCarbonConfigDirPath() + File.separator + "KillBillEndPointConfig.xml";
		xml=new File(filePath);
		builder = new SAXBuilder();
		document = (Document) builder.build(xml);
		rootNode = document.getRootElement();
		setUrl();
		setUname();
		setPassword();
		setApiKey();
		setApiSecret();

	}

	public String getUrl() {
		return url;
	}

	public void setUrl() {
		List<Element> list = rootNode.getChildren("url");
		Element node =  list.get(0);
		url=node.getText();

	}

	public String getPassword() {
		return password;
	}

	public void setPassword() {
		List<Element> list = rootNode.getChildren("password");
		Element node =  list.get(0);
		password=node.getText();

	}

	public String getUname() {
		return uname;
	}

	public void setUname() {
		List<Element> list = rootNode.getChildren("uname");
		Element node =  list.get(0);
		uname=node.getText();
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey() {
		List<Element> list = rootNode.getChildren("apiKey");
		Element node =  list.get(0);
		apiKey=node.getText();
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public void setApiSecret() {
		List<Element> list = rootNode.getChildren("apiSecret");
		Element node =  list.get(0);
		apiSecret=node.getText();
	}

	
}

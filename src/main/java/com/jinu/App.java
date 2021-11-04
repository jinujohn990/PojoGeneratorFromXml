package com.jinu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jinu.dto.ChildNodeDetail;
import com.jinu.dto.NodeDetail;

public class App {
	private static final String GENERATEDPOJO = "GENERATEDPOJO";
	private static final String PUBLIC_VOID = "public void";
	private static final String IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ROOT_ELEMENT = "import javax.xml.bind.annotation.XmlRootElement;\r\n";
	private static final String IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ELEMENT = "import javax.xml.bind.annotation.XmlElement;\r\n";
	private static final String XML_ACCESSOR_TYPE_XML_ACCESS_TYPE_FIELD = "@XmlAccessorType(XmlAccessType.FIELD)";
	private static final String IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ACCESSTYPE_AND_ACCESSERTYPE = "import javax.xml.bind.annotation.XmlAccessType;\r\n"
			+ "import javax.xml.bind.annotation.XmlAccessorType;\r\n";
	private static final String IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ATTRIBUTE = "import javax.xml.bind.annotation.XmlAttribute;\r\n";
	private static final String THISDOT = "this.";
	private static final String RETURN = "return";
	private static final String PUBLIC_WHITESPACE = "public ";
	private static final String CLOSING_CURLY_BRACKET = "}";
	private static final String OPENING_CURLY_BRACKET = "{";
	private static final String STRING = "String";
	private static final String EQUALS = "=";
	private static final String CLOSING_BRACKET = ")";
	private static final String OPENING_BRACKET = "(";
	private static final String BLANK_SPACE = " ";
	private static final String SEMICOLAN = ";";
	private static final String TAB_SPACE = "\t";
	private static final String ELEMENT = "element";
	private static final String OBJECT = "object";
	private static final String ATTRIBUTE = "attribute";
	private static Set<NodeDetail> nodetailsSet = new HashSet<NodeDetail>();
	private static final String LINE_SEPARATOR = "line.separator";

	public static void main(String[] args) {
		try {
			System.out.println("Welcome to Pojo Generator......");
			String currentDir = System.getProperty("user.dir");
			System.out.println("Enter the xml file name:");
			Scanner in = new Scanner(System.in);
			String filename = in.nextLine();
			if (!(filename.contains(".xml") || filename.contains(".XML"))) {
				filename = filename + ".xml";
			}
			System.out.println("Enter the package name required for the generated pojo classes:");
			String packageName = in.nextLine();
			String xmlString = readXmlFromFile(currentDir + File.separatorChar + filename);
			Document document = loadDocumentFromXmlString(new java.io.ByteArrayInputStream(xmlString.getBytes()));
			Element element = document.getDocumentElement();
			NodeList childNodes = element.getChildNodes();
			NodeDetail nodeDetail = new NodeDetail();
			nodeDetail.setNodeName(element.getNodeName());
			NamedNodeMap attributes = element.getAttributes();
			List<ChildNodeDetail> childNodeDetails = new ArrayList<>();
			for (int i = 0; i < attributes.getLength(); i++) {
				Attr attr = (Attr) attributes.item(i);
				ChildNodeDetail childNodeDetail = new ChildNodeDetail();
				childNodeDetail.setNodeName(attr.getNodeName());
				childNodeDetail.setNodeType(ATTRIBUTE);
				childNodeDetails.add(childNodeDetail);
			}
			childNodeDetails.addAll(getChildeChildNodeDetails(childNodes));
			nodeDetail.setChildNodeDetails(childNodeDetails);
			nodetailsSet.add(nodeDetail);
			int nodeLength = childNodes.getLength();
			for (int i = 0; i < nodeLength; i++) {
				if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE
						&& getChildCount(childNodes.item(i).getChildNodes()) > 0) {
					createNodeDetailsForChildNodes(childNodes.item(i));
				}
			}
			File generationFolder = new File(currentDir + File.separator + GENERATEDPOJO);
			generationFolder.mkdirs();
			FileUtils.cleanDirectory(generationFolder);
			for (NodeDetail nodeDetail1 : nodetailsSet) {
				createPojoClassJavaFile(nodeDetail1,packageName);
			}
			System.out.println("Pojo classed generated on path: " + currentDir + File.separator + GENERATEDPOJO);
			System.out.println("Thanks");
			System.out.println("Jinu");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String readXmlFromFile(String file) throws  IOException {
		String xmlString;
		Reader fReader;
		BufferedReader bReader;
		fReader = new FileReader(file);
		bReader = new BufferedReader(fReader);
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = bReader.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		bReader.close();
		xmlString = sb.toString();
		return xmlString;
	}

	public static org.w3c.dom.Document loadDocumentFromXmlString(java.io.InputStream is)
			throws org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (javax.xml.parsers.ParserConfigurationException ex) {
			ex.printStackTrace();
		}
		org.w3c.dom.Document doc = builder.parse(is);
		is.close();
		return doc;
	}

	private static List<ChildNodeDetail> getChildeChildNodeDetails(NodeList nodeList) {
		List<ChildNodeDetail> childNodeDetails = new ArrayList<ChildNodeDetail>();
		int nodeLength = nodeList.getLength();
		for (int i = 0; i < nodeLength; i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				ChildNodeDetail childNodeDetail = new ChildNodeDetail();
				childNodeDetail.setNodeName(node.getNodeName());
				childNodeDetail.setNodeType(ATTRIBUTE);
			} else if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (!childNodeDetails.stream().anyMatch(n -> n.getNodeName().equals(node.getNodeName()))) {
					ChildNodeDetail childNodeDetail = new ChildNodeDetail();
					childNodeDetail.setNodeName(node.getNodeName());
					if (getChildCount(node.getChildNodes()) > 1) {
						childNodeDetail.setNodeType(OBJECT);
					} else {
						childNodeDetail.setNodeType(ELEMENT);
					}
					childNodeDetails.add(childNodeDetail);
				} 
			}
		}
		return childNodeDetails;
	}

	public static int getChildCount(NodeList nodeList) {
		int count = 0;
		int nodeLength = nodeList.getLength();
		for (int i = 0; i < nodeLength; i++) {
			if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE
					|| nodeList.item(i).getNodeType() == Node.ATTRIBUTE_NODE) {
				count++;
			}
		}
		return count;
	}

	private static void createNodeDetailsForChildNodes(Node node) {
		NodeList childNodes = node.getChildNodes();
		NodeDetail nodeDetail = new NodeDetail();
		nodeDetail.setNodeName(node.getNodeName());
		NamedNodeMap attributes = node.getAttributes();
		List<ChildNodeDetail> childNodeDetails = new ArrayList<ChildNodeDetail>();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr attr = (Attr) attributes.item(i);
			ChildNodeDetail childNodeDetail = new ChildNodeDetail();
			childNodeDetail.setNodeName(attr.getNodeName());
			childNodeDetail.setNodeType(ATTRIBUTE);
			childNodeDetails.add(childNodeDetail);
		}
		childNodeDetails.addAll(getChildeChildNodeDetails(childNodes));
		nodeDetail.setChildNodeDetails(childNodeDetails);
		if (nodetailsSet.stream().anyMatch(n -> n.getNodeName().equals(nodeDetail.getNodeName()))) {
			if (nodeDetail.getChildNodeDetails().size() > nodetailsSet.stream()
					.filter(n -> n.getNodeName().equals(nodeDetail.getNodeName())).findFirst().get()
					.getChildNodeDetails().size()) {
				nodetailsSet.add(nodeDetail);
			}
		} else {
			nodetailsSet.add(nodeDetail);
		}
		int nodeLength = childNodes.getLength();
		for (int i = 0; i < nodeLength; i++) {
			if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE
					&& getChildCount(childNodes.item(i).getChildNodes()) > 0) {
				createNodeDetailsForChildNodes(childNodes.item(i));
			}
		}
	}

	public static void createPojoClassJavaFile(NodeDetail nodeDetails,String packageName) throws IOException {
		String currentDir = System.getProperty("user.dir");
		File file = new File(currentDir + File.separator + GENERATEDPOJO + File.separator
				+ generateClassName(nodeDetails.getNodeName()) + ".java");
		try (BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			StringBuilder classFile = new StringBuilder();
			StringBuilder getterAndSetter = new StringBuilder();
			classFile.append("package " + packageName + SEMICOLAN);
			classFile.append(System.getProperty(LINE_SEPARATOR));
			if (nodeDetails.getChildNodeDetails().stream().anyMatch(n -> n.getNodeName().equals(ATTRIBUTE))) {
				classFile.append(IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ATTRIBUTE);
			}
			classFile.append(IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ACCESSTYPE_AND_ACCESSERTYPE);
			if (nodeDetails.getChildNodeDetails().stream().anyMatch(n -> n.getNodeName().equals(ATTRIBUTE))) {
				classFile.append(IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ATTRIBUTE);
			}
			classFile.append(IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ELEMENT)
					.append(IMPORT_JAVAX_XML_BIND_ANNOTATION_XML_ROOT_ELEMENT)
					.append(XML_ACCESSOR_TYPE_XML_ACCESS_TYPE_FIELD);
			classFile.append(System.getProperty(LINE_SEPARATOR));
			classFile.append("@XmlRootElement(name=\"" + generateClassName(nodeDetails.getNodeName()) + "\")");
			classFile.append(System.getProperty(LINE_SEPARATOR));
			classFile.append("public class " + generateClassName(nodeDetails.getNodeName()) + " {");
			classFile.append(System.getProperty(LINE_SEPARATOR));
			List<ChildNodeDetail> childNodeDetails = nodeDetails.getChildNodeDetails();
			for (ChildNodeDetail childNodeDetail : childNodeDetails) {
				if (childNodeDetail.getNodeType().equals(OBJECT)) {
					classFile.append(System.getProperty(LINE_SEPARATOR));
					classFile.append(createObjectElementToClass(childNodeDetail.getNodeName()));
					getterAndSetter.append(generateGetterAndSetter(childNodeDetail));
				} else if (childNodeDetail.getNodeType().equals(ATTRIBUTE)) {
					classFile.append(System.getProperty(LINE_SEPARATOR));
					classFile.append(createAttributeElementToClass(childNodeDetail.getNodeName()));
					getterAndSetter.append(generateGetterAndSetter(childNodeDetail));
				} else if (childNodeDetail.getNodeType().equals(ELEMENT)) {
					classFile.append(System.getProperty(LINE_SEPARATOR));
					classFile.append(createMemberElementToClass(childNodeDetail.getNodeName()));
					getterAndSetter.append(generateGetterAndSetter(childNodeDetail));
				}
			}
			classFile.append(System.getProperty(LINE_SEPARATOR));
			classFile.append(getterAndSetter);
			classFile.append(System.getProperty(LINE_SEPARATOR));
			classFile.append(CLOSING_CURLY_BRACKET);
			bw.write(classFile.toString());
			bw.flush();
		} catch (IOException e) {
			e.getMessage();
		}
	}

	private static String generateGetterAndSetter(ChildNodeDetail childNodeDetail) {
		StringBuilder getterAndSetter = new StringBuilder();
		getterAndSetter.append(System.getProperty(LINE_SEPARATOR));
		if (childNodeDetail.getNodeType().equals(OBJECT)) {
			getterAndSetter.append(PUBLIC_WHITESPACE).append(generateClassName(childNodeDetail.getNodeName()))
					.append(BLANK_SPACE).append(generateGetterName(childNodeDetail)).append("()")
					.append(OPENING_CURLY_BRACKET).append(System.getProperty(LINE_SEPARATOR)).append(TAB_SPACE)
					.append(RETURN).append(BLANK_SPACE)
					.append(generateMemeberVariableName(childNodeDetail.getNodeName())).append(SEMICOLAN)
					.append(System.getProperty(LINE_SEPARATOR)).append(CLOSING_CURLY_BRACKET);
			getterAndSetter.append(System.getProperty(LINE_SEPARATOR));
			getterAndSetter.append(PUBLIC_VOID).append(BLANK_SPACE).append(generateSetterName(childNodeDetail))
					.append(OPENING_BRACKET)
					.append(generateClassName(childNodeDetail.getNodeName()) + BLANK_SPACE
							+ generateMemeberVariableName(childNodeDetail.getNodeName()))
					.append(CLOSING_BRACKET).append(OPENING_CURLY_BRACKET).append(System.getProperty(LINE_SEPARATOR))
					.append(TAB_SPACE).append(THISDOT)
					.append(generateMemeberVariableName(childNodeDetail.getNodeName())).append(EQUALS)
					.append(generateMemeberVariableName(childNodeDetail.getNodeName())).append(SEMICOLAN)
					.append(System.getProperty(LINE_SEPARATOR)).append(CLOSING_CURLY_BRACKET);
		} else {
			getterAndSetter.append(PUBLIC_WHITESPACE).append(STRING).append(BLANK_SPACE)
					.append(generateGetterName(childNodeDetail)).append("()").append(OPENING_CURLY_BRACKET)
					.append(System.getProperty(LINE_SEPARATOR)).append(TAB_SPACE).append(RETURN).append(BLANK_SPACE)
					.append(generateMemeberVariableName(childNodeDetail.getNodeName())).append(SEMICOLAN)
					.append(System.getProperty(LINE_SEPARATOR)).append(CLOSING_CURLY_BRACKET);
			getterAndSetter.append(System.getProperty(LINE_SEPARATOR));
			getterAndSetter.append(PUBLIC_VOID).append(BLANK_SPACE).append(generateSetterName(childNodeDetail))
					.append(OPENING_BRACKET)
					.append(STRING + BLANK_SPACE + generateMemeberVariableName(childNodeDetail.getNodeName()))
					.append(CLOSING_BRACKET).append(OPENING_CURLY_BRACKET).append(System.getProperty(LINE_SEPARATOR))
					.append(TAB_SPACE).append(THISDOT)
					.append(generateMemeberVariableName(childNodeDetail.getNodeName())).append(EQUALS)
					.append(generateMemeberVariableName(childNodeDetail.getNodeName())).append(SEMICOLAN)
					.append(System.getProperty(LINE_SEPARATOR)).append(CLOSING_CURLY_BRACKET);
		}
		return getterAndSetter.toString();

	}

	private static String generateClassName(String nodeName) {
		String className = null;
		if (nodeName.contains("_")) {
			String[] nameNameArray = nodeName.split("-");
			for (String str : nameNameArray) {
				str = str.toLowerCase();
				String firstLetter = str.substring(0, 1);
				String remainingLetter = str.substring(1);
				str = firstLetter.toUpperCase() + remainingLetter;
			}
			className = String.join("", nameNameArray);
		} else if (StringUtils.isAllLowerCase(nodeName)) {
			String firstLetter = nodeName.substring(0, 1);
			String remainingLetters = nodeName.substring(1);
			className = firstLetter.toUpperCase() + remainingLetters;
		} else if (StringUtils.isAllUpperCase(nodeName)) {
			String firstLetter = nodeName.substring(0, 1);
			String remainingLetters = nodeName.substring(1);
			className = firstLetter + remainingLetters.toLowerCase();
		} else if (StringUtils.isAlphanumeric(nodeName)) {
			String firstLetter = nodeName.substring(0, 1);
			String remainingLetters = nodeName.substring(1);
			className = firstLetter.toUpperCase() + remainingLetters.toLowerCase();
		}
		return className;
	}

	private static StringBuilder createObjectElementToClass(String nodeName) {
		String className = generateClassName(nodeName);
		StringBuilder objectElement = new StringBuilder();
		objectElement.append("@XmlElement(name=\"" + nodeName + "\")");
		objectElement.append(System.getProperty(LINE_SEPARATOR));
		objectElement.append("private " + className + BLANK_SPACE + generateMemeberVariableName(nodeName) + SEMICOLAN);
		return objectElement;
	}

	private static String generateMemeberVariableName(String nodeName) {
		String memberVariableName = null;
		if (nodeName.contains("_")) {
			String[] nameNameArray = nodeName.split("-");
			for (int i = 0; i < nameNameArray.length; i++) {
				String str = nameNameArray[i];
				str = str.toLowerCase();
				if (i == 0) {
					nameNameArray[i] = str;
				} else {
					String firstLetter = str.substring(0, 1);
					String remainingLetter = str.substring(1);
					str = firstLetter.toUpperCase() + remainingLetter;
					nameNameArray[i] = str;
				}
			}
			memberVariableName = String.join("", nameNameArray);
		} else if (StringUtils.isAllLowerCase(nodeName)) {
			memberVariableName = nodeName;
		} else if (StringUtils.isAllUpperCase(nodeName)) {
			memberVariableName = nodeName.toLowerCase();
		} else if (StringUtils.isAlphanumeric(nodeName)) {
			String firstLetter = nodeName.substring(0, 1);
			String remainingLetter = nodeName.substring(1);
			memberVariableName = firstLetter.toLowerCase() + remainingLetter;
		}
		return memberVariableName;
	}

	private static StringBuilder createAttributeElementToClass(String nodeName) {
		String memberName = generateMemeberVariableName(nodeName);
		StringBuilder attributeElement = new StringBuilder();
		attributeElement.append("@XmlAttribute(name=\"" + nodeName + "\")");
		attributeElement.append(System.getProperty(LINE_SEPARATOR));
		attributeElement.append("private String " + memberName + SEMICOLAN);
		return attributeElement;
	}

	private static StringBuilder createMemberElementToClass(String nodeName) {
		String memberName = generateMemeberVariableName(nodeName);
		StringBuilder attributeElement = new StringBuilder();
		attributeElement.append("@XmlElement(name=\"" + nodeName + "\")");
		attributeElement.append(System.getProperty(LINE_SEPARATOR));
		attributeElement.append("private String " + memberName + SEMICOLAN);
		return attributeElement;
	}

	private static String generateGetterName(ChildNodeDetail childNodeDetail) {
		String memberVaribleName = generateMemeberVariableName(childNodeDetail.getNodeName());
		return "get" + memberVaribleName.substring(0, 1).toUpperCase() + memberVaribleName.substring(1);
	}

	private static String generateSetterName(ChildNodeDetail childNodeDetail) {
		String memberVaribleName = generateMemeberVariableName(childNodeDetail.getNodeName());
		return "set" + memberVaribleName.substring(0, 1).toUpperCase() + memberVaribleName.substring(1);
	}
}

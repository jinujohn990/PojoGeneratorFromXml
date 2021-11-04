package com.jinu.dto;

import java.util.List;

import lombok.Data;

@Data
public class NodeDetail {
	private String nodeName;
	List<ChildNodeDetail> childNodeDetails;
}

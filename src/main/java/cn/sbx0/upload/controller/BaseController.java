package cn.sbx0.upload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Controller;

@Controller
public class BaseController {
	// 为了返回json
	ObjectMapper mapper = new ObjectMapper();
	ObjectNode objectNode = mapper.createObjectNode();
}
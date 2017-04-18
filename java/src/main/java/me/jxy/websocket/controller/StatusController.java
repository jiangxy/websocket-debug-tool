package me.jxy.websocket.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 测试用
 *
 * @version 1.0
 * @author jiangxy
 */
@Controller
public class StatusController {

    private static Logger logger = LoggerFactory.getLogger(StatusController.class);

    /**
     * 检查API是否正常
     *
     * @return
     */
    @RequestMapping("/status")
    @ResponseBody
    public String status() {
        logger.info("check api status");
        return "OK";
    }

}

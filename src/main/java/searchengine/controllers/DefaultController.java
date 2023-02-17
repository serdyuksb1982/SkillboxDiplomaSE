package searchengine.controllers;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public record DefaultController() {

    /**
     * Метод формирует страницу из HTML-файла index.html,
     * который находится в папке resources/templates.
     * Это делает библиотека Thymeleaf.
     */
   @ApiOperation("Open main page 'Index'")
    @RequestMapping("/")
    public String index() {
        return "index";
    }
}

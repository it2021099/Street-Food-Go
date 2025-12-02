package gr.hua.dit.Street_Food_Go.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

@RestController
public class TestController {

    @GetMapping(value="test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String test(){
        return "test";
    }


}
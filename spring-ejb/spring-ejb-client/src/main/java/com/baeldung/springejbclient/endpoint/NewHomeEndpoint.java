package com.baeldung.springejbclient.endpoint;

import com.baeldung.ejb.tutorial.HelloStatefulWorld;
import com.baeldung.ejb.tutorial.HelloStatelessWorld;
import com.baeldung.springejbclient.service.HelloStatelessWorldService;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewHomeEndpoint {

    private HelloStatelessWorldService helloStatelessWorldService;

    public NewHomeEndpoint(
        HelloStatelessWorldService helloStatelessWorldService) {
        this.helloStatelessWorldService = helloStatelessWorldService;
    }

    @GetMapping("/newstateless")
    public String getStateless()
        throws HeuristicRollbackException, HeuristicMixedException, NotSupportedException, RollbackException, SystemException {
        return helloStatelessWorldService.getHelloWorld();
    }




}

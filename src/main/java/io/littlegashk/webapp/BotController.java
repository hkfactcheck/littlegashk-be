package io.littlegashk.webapp;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bot")
@Api(value = "Bot")
@Log4j2
public class BotController {


  @Autowired
  AWSCognitoIdentityProvider cognito;
  @ApiOperation("add user")
  @PostMapping("/user")
  public ResponseEntity<String> addUser(@RequestParam String username, @RequestParam String tgid, @RequestParam String tempPassword) throws JsonProcessingException {
    AdminCreateUserRequest req = new AdminCreateUserRequest()
        .withUserPoolId("us-east-2_IUuEJR8MY")
        .withUsername(username)
        .withUserAttributes(new AttributeType().withName("custom:tgid").withValue(tgid))
        .withTemporaryPassword(tempPassword)
        .withMessageAction("SUPPRESS");
    AdminCreateUserResult createUserResult =  cognito.adminCreateUser(req);
    UserType u = createUserResult.getUser();
    return ResponseEntity.ok(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(u));
  }
}

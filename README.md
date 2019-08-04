## LITTLE-GAS-BOOK backend module

This is a spring boot project intended to be run within AWS architecture. 

### Setup local workspace

1. Make sure jdk11 and maven (https://maven.apache.org/) is installed.
2. Make sure docker is installed
3. `docker run -p 8000:8000 amazon/dynamodb-local` to spin up a local DynamoDB. Reference https://hub.docker.com/r/amazon/dynamodb-local/
4. In your IDE, set active run profile to `dev` then run the spring boot application
5. Alternatively, `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

The application should start and you can checkout the swagger doc by navigating to http://localhost/swagger-ui.html#

### About your local DynamoDB

When application starts, it will install schema as written in `DynamoDbSchemaInitializer.java`


### What about on AWS?

Application will detect instance meta-data and discover DynamoDB itself. It will fail to start if it cannot find one. 

To set it up, you have to
1. Register an AWS subscription (if you do not have one)
2. Create an IAM role with DynamoDB full access. 
3. Create EC2/Target group/ECS or the like, attach the role there
4. Configure security group to allow 80 (or 443) incoming traffic.
5. Put the jar file (`mvn clean install`) there. Kick start the jar

(Sorry it is super brief above, those without AWS experience will need to .... well.... spend some time)

### Build and deployment pipeline

(TBC)

### One final word

PRs welcome, and enjoy :)
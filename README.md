## LITTLE-GAS-BOOK backend module

This is a spring boot project intended to be run within AWS architecture. 

### Setup local workspace

1. Make sure jdk11 and maven (https://maven.apache.org/) is installed.
2. Make sure docker is installed
3. `docker run --rm --name pg -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 -v ~/pgdata:/var/lib/postgresql/data postgres` to spin up a local DB. 
4. In your IDE, set active run profile to `dev` then run the spring boot application
5. Alternatively, `mvn spring-boot:run -Dspring-boot.run.profiles=dev`

The application should start and you can checkout the swagger doc by navigating to http://localhost/swagger-ui.html#

### Setup local workspace (the lazy way)
1. `docker-compose build`
2. `docker-compose up`

### Build and deployment pipeline

(TBC)

### One final word

PRs welcome, and enjoy :)
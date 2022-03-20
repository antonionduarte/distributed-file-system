# distributed-file-system
Distributed Systems project repository.

# TODO:

- [ ] just an example 
- [ ] another example 

# maven + docker notes

## useful commands:

### docker
- `docker system prune`: completely clear docker
- `docker ps`: see active containers
- `docker run sd2122-aula1`: example run docker container from image
- `docker network create -d bridge sdnet`: create a bridge network, named sdnet, to connect container
- `docker run -h srv1 --name srv1 --network sdnet sd2122-aula1`: example run with hostname and net

### maven
- `mvn clean`: cleans the project, removing generated files
- `mvn compile`: compiles the project
- `mvn assembly:single`: creates a single file with all compiled classes and dependencies (jar file)
- `mvn docker:build`: builds a docker image using the Dockerfile
- `mvn clean compile assembly:single docker:build`: run everything at once


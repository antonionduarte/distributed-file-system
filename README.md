# distributed-file-system
Distributed Systems project repository.

# TODO:

- [ ] Cache 
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

### dropbox
- `app key`: 2v5aoett5ga8tec
- `app secret`: 1qw5p1vin7d07r2
- `access token`: sl.BIdt5A8FVj9EDA9yo7a248yEchTwYDYZ7-nfnVHsHk1dDOiMXFucXhV3jDBdbHPoGhXQBBd8z_4PKGOIPKIvFpgo6Uy1b1E9o9551YKaqPv8sdd4GtYnJXCEyQV9ARrEccGviY4


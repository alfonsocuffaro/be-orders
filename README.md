# Back-End Orders
**Back-End Orders** is a *small-but-not-too-small* java project that builds a back-end tool for managing orders.

It will be capable of allowing a front-end application to:

1. view/search orders stored in the system
2. display order information (along with associated products)
3. perform the usual CRUD operations on orders 
	- create
	- read
	- update
	- delete

In addition to all of the above it should be possible:

4. to track and manage product stock levels


## Rationale

The rationale of the project is to showcase development skills in a easy but not too easy java project.




<br/><br/>
# How to build the docker image of the project

**Prerequisites**:

- Java, with the JAVA_HOME environment variable set
- Docker or Podman
- Gradle is not a requirement (it will be automatically installed by a script already present in the project’s repository; don't worry the script comes from the project https://start.spring.io used to initialize the spring boot application)

  
<br/>

**Steps to build**:

- Run **`cd <PROJECT_FOLDER>`** (**`<PROJECT_FOLDER>`** is the folder where the project has been cloned/downloaded into; if **`<PROJECT_FOLDER>`** is the correct one, you should seeing the following files **`build.gradle`**, **`dockerfile`**, etc.)
- build the project: **`./gradlew build`**
- run the tests: **`./gradlew test`** (in windows **`./gradlew.bat test`**)
- if everything is ok you can build the image for running the container with **`docker build -t beorders .`**


<br/>

To build the image for the container the following **`dockerfile`** will be used:

```dockerfile
# Use an official java runtime as a parent image
FROM openjdk:23-rc

# Set the working directory to /app
WORKDIR /app

# copy the spring boot application jar
COPY build/libs/beorders-0.0.1-SNAPSHOT.jar /app/beorders.jar

# expose the port that be-orders will use
EXPOSE 8080

# start the be-orders app with the following command
CMD ["java", "-jar", "beorders.jar"]
```

  
<br/>

**Run the container.**

In a terminal emulator run one of the commands

1.  **`docker run -p 8000:8080 beorders`**
2.  **`docker run -d -p 8000:8080 beorders`**

**Command 1** runs the container, mapping its port **`8080`** (defined with the **EXPOSE** command in the dockerfile) to the host’s port **`8000`**.

**Command 2** similar to **Command 1**, in addition here the container runs in the background (the so called **detached mode**) meaning that:

- you can start a container and continue working on your terminal without being blocked by the container’s output
- the container will continue to run even if you close the terminal where you started it (in this case you can still manage the container using the usual docker commands)

  
<br/>

**Working with the container.** You can work with the container using tools like:

- **curl**, **wget**, **httpie**, …
- **Postman**, **echoapi**, **Swagger UI**

reaching it at any of its URI at **`http://localhost:8000`**


##  Todo list

- [x] think on how to store data (db, db schema, initial data, ...)
- [x] think about users
- [ ] **WIP** list the URI of the endpoints


### Orders management

- [?] ORDERS: filtered by date
- [?] ORDERS: search by username
- [?] ORDERS: search by description
- [?] ORDERS: display order info (with prods)
- [?] ORDERS: create
- [?] ORDERS: edit/update
- [?] ORDERS: delete


### Products management

- [?] PRODUCTS: create (optional?)
- [?] PRODUCTS: read/search (optional?)
- [?] PRODUCTS: update/edit (optional?)
- [?] PRODUCTS: delete (optional?)


### Stock levels management

- [?] STOCK levels: search (optional?)
- [?] STOCK levels: increase (optional?)


### Users management

- [?] USERS: create (optional?)
- [?] USERS: read/search (optional?)
- [?] USERS: edit/update (optional?)
- [?] USERS: delete (optional?)


### Others

- [x] dockerize
- [ ] integrate tools like Elasticsearch/Meilisearch for enahanced search capabilities
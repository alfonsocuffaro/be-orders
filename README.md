# Back-End Orders
**Back-End Orders** is a *small-but-not-too-small* java project to build a back-end tool for managing orders.

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
<br/><br/>


# Rationale

The rationale of the project is to exercise and showcase development skills in a *easy-but-not-too_easy* java project.
<br/><br/>


# Rationale
The project is written using the **Spring Boot** framework. **Gradle** is tool used for managing the dependencies and building the software. Instructions are provided to build a docker image for the project and running the corresponding container.
<br/><br/>


# How to build the docker image of the project

**Prerequisites.**

- Java, with the JAVA_HOME environment variable set
- Docker or Podman
- Gradle is not a requirement (it will be automatically installed by a script already present in the project’s repository; don't worry the script comes from the project https://start.spring.io used to initialize the spring boot application)

<br/>

**Build the project.**

- Run **`cd <PROJECT_FOLDER>`** (**`<PROJECT_FOLDER>`** is the folder where the project has been cloned/downloaded into; if **`<PROJECT_FOLDER>`** is the correct one, you should seeing the following files **`build.gradle`**, **`dockerfile`**, etc.)
- build the project running: **`./gradlew build`**
- run the tests with: **`./gradlew test`** (in windows **`./gradlew.bat test`**)

<br/>

**Build the image for the container.**

If everything is ok, you can build an image for the project **`docker build -t beorders .`**

The build process of the image is based on a **`dockerfile`** with the following content:

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

**Command 1** runs the container, mapping its port **`8080`** (defined with the **`EXPOSE`** command in the dockerfile) to the host’s port **`8000`**.

**Command 2** is similar to **Command 1**. In addition though the container runs in the background (the so called **detached mode**) meaning that:

- you can start a container and continue working on your terminal without being blocked by the container’s output
- the container will continue to run even if you close the terminal where you started it (in this case you can still manage the container using the usual docker commands)
  
<br/>

**Interacting with the container.**

You can interact with the container using tools like:

- **curl**, **wget**, **httpie**, …
- **Postman**, **echoapi**, **Swagger UI**

to reach any of its URIs at **`http://localhost:8000`**
<br/><br/>


#  List of the endpoints

There are two types of endpoints:
- **private** (starting with **`/v1/admin`**): these are reserved exclusively to the users having the **ADMIN** role
- **public** (starting with **`/v1/orders`**): these can be accessed by the user having the **ADMIN** or the **OWNER** role

<br/>

## public URIs (starting with `/v1/orders`)

|       |        **public URIs**            | can be operated by<br>**ADMIN** | can be operated by<br>**OWNER** |
|:-----:|:----------------------------------|:-------------------------------:|:-------------------------------:|
|**1.** | **`GET    /v1/orders`**           |                y                |                y                |
|**2.** | **`GET    /v1/orders/{id}`**      |                y                |                y                |
|**3.** | **`POST   /v1/orders`**           |                y                |                y                |
|**4.** | **`PUT    /v1/orders/{id}`**      |                y                |                y                |
|**5.** | **`DELETE /v1/orders/{id}`**      |                y                |                y                |

<br/>

## private URIs (starting with `/v1/admin`)

|       | **private URIs**                     | can be operated by<br>**ADMIN** | can be operated by<br>**OWNER** |
|:-----:|:-------------------------------------|:-------------------------------:|:-------------------------------:|
|**6.** | **`GET    /v1/admin/orders`**        |                y                |                -                |
|**7.** | **`GET    /v1/admin/orders/{id}`**   |                y                |                -                |
|**8.** | **`POST   /v1/admin/orders`**        |                y                |                -                |
|**9.** | **`PUT    /v1/admin/orders/{id}`**   |                y                |                -                |
|**10.**| **`DELETE /v1/admin/orders/{id}`**   |                y                |                -                |

<br/><br/>

## 1. **`GET /v1/orders`**

### Description
The uri to call to retrieve a list of orders.

### Request
- **URI**: /v1/orders
- **HTTP Verb**: GET
- **Body**: (none)
- **Body type**: (none)
- **Query parameters**: The following query parameters are accepted

| query parameter | optional | type    | notes                                                                                                                                     |
|-----------------|----------|---------|-------------------------------------------------------------------------------------------------------------------------------------------|
| **product** | yes      | string  | case insensitive                                                                                                                          |
| **page**        | yes      | integer | page number (starting from 0)                                                                                                             |
| **size**        | yes      | integer | number of 'orders' shown in any page                                                                                                      |
| **sort**        | yes      | string  | format: **field,sortOrder**<br/> - field can be one of: **id**, **owner**, **amount**, **product**, **quantity**<br/> - sort order can be one of: **asc**, **desc** |


### Examples of URIs

```json
GET /v1/orders
```

```json
GET /v1/orders?productType=libro
```

```json
GET /v1/orders?page=0&size=50
```

```json
GET /v1/orders?productType=libro&page=0&size=5&sort=amount,desc
```

### Response

|  HTTP                | Status |
|----------------------|--------|
| **200 OK**           | the user is authorized and the order was successfully retrieved            |
| **401 UNAUTHORIZED** | the user is unauthenticated or unauthorized                                |
| **404 NOT FOUND**    | the user is authenticated and authorized but the order cannot be found     |

### Response Body Type
application/json

### Examples of Response Body

~~~json
{
	"id": 2,
	"amount": 5200.0,
	"owner": "Alice",
	"product": "anello di diamanti",
	"quantity": 2
}
~~~

~~~json
[
	{
		"id": 2,
		"amount": 300.0,
		"owner": "Alice",
		"product": "libro",
		"quantity": 10
	},
	{
		"id": 3,
		"amount": 5200.0,
		"owner": "Alice",
		"product": "anello di diamanti",
		"quantity": 2
	}
]
~~~
<br/><br/>

## 2. **`GET /v1/orders/{id}`**

### Description
The uri to call to retrieve a specific order.

### Request

- **URI**: /v1/orders/{id}
- **HTTP Verb**: GET
- **Body**: (none)
- **Body type**: (none)
- **Query parameters**: (none)

### Examples of URIs

```json
GET /v1/orders/12
```

### Examples of Request Body
(none)

### Response

|  HTTP Status         | Notes   |
|----------------------|--------|
| **200 OK**           | the user is authorized and the order was successfully retrieved            |
| **401 UNAUTHORIZED** | the user is unauthenticated or unauthorized                                |
| **404 NOT FOUND**    | the user is authenticated and authorized but the order cannot be found     |
	
### Response Body Type
application/json

### Examples of Response Body
~~~json
{
	"id": 2,
	"amount": 5200.0,
	"owner": "Alice",
	"product": "anello di diamanti",
	"quantity": 2
}
~~~
<br/><br/>
  

## 3. **`POST /v1/orders`**

### Description
The uri to call to create an order.

### Request
- **URI**: /v1/orders
- **HTTP Verb**: POST
- **Body**: yes
- **Body type**: application/json
- **Query parameters**: (none)


### Examples of URIs

```json
POST /v1/orders/12
```


### Examples of Request Body
```json
{
	"amount": 1200.00,
	"product": "Braccialetto",
	"quantity": 2
}
```


### Response

|  HTTP Status         | Notes   |
|----------------------|--------|
| **201 CREATED**      | the user is authorized and the order was successfully retrieved            |
| **401 UNAUTHORIZED** | the user is unauthenticated or unauthorized                                |

When the creation of the new order is successful (HTTP status code 201) the **Location** header of the response is populated with the URL to retrieve the new order (example: `http://localhost:8000/v1/orders/11`)
	
### Response Body Type:
none

### Examples of Response Body
none
<br/><br/>



### 4. **`PUT /v1/orders/{id}`**
### Description
The uri to call to modify an order.

### Request
- **URI**:PUT /v1/orders/{id}
- **HTTP Verb**: PUT
- **Body**: yes
- **Body type**: application/json
- **Query parameters**: (none)

### Examples of URIs

```json
PUT /v1/orders/28
```

### Examples of Request Body
```json
{
	"amount": 1200.00,
	"quantity": 2
}
```

```json
{
	"product": "Braccialetto",
	"quantity": 5
}
```

```json
{
	"amount": 1200.00,
	"product": "Braccialetto",
}
```

```json
{
	"amount": 1200.00,
	"product": "Braccialetto",
	"quantity": 5
}
```


### Response

|  HTTP                | Status                                                                     |
|----------------------|----------------------------------------------------------------------------|
| **204 NO CONTENT**   | the user is authorized and the order was successfully retrieved            |
| **401 UNAUTHORIZED** | the user is unauthenticated or unauthorized                                |
| **404 NOT FOUND**    | the user is authenticated and authorized but the order cannot be found     |

<br/><br/>

### 5. **`DELETE /v1/orders/{id}`**
### Description
The uri to call to delete an order.

### Request
- **URI**: /v1/orders/{id}
- **HTTP Verb**: DELETE
- **Body**: (none)
- **Body type**: (none)
- **Query parameters**: (none)

### Examples of URIs

```json
DELETE /v1/orders/28
```


### Examples of Request Body
none

### Response

|  HTTP                | Status                                                                           |
|----------------------|----------------------------------------------------------------------------------|
| **204 NO CONTENT**   | the record exists, the user is authorized and the order was successfully deleted |
| **404 NOT FOUND**    | the record does not exist                                                        |
| **404 NOT FOUND**    | the record does exist but the principal is not the owner                         |

<br/><br/>


### 6. **`GET /v1/admin/orders`**
TO BE COMPLETED
<br/><br/>

### 7. **`GET /v1/admin/orders/{id}`**
TO BE COMPLETED
<br/><br/>

### 8. **`POST /v1/admin/orders`**
TO BE COMPLETED
<br/><br/>

### 9. **`PUT /v1/admin/orders/{id}`**
TO BE COMPLETED
<br/><br/>

### 10. **`DELETE /admin/orders/{id}`**
TO BE COMPLETED
<br/><br/>
<br/><br/><br/><br/>


#  Todo list

- [x] ~~think on how to store data (db, db schema, initial data, ...)~~
- [x] ~~think about users~~
- [x] list the URsI of the endpoints  **still WIP**


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
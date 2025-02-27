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
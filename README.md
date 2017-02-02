[![Build Status](https://travis-ci.org/tripod-oss/arangodb-scala-driver.svg?branch=master)](https://travis-ci.org/tripod-oss/arangodb-scala-driver)

# Introduction

`arangodb-scala-driver` is an [ArangoDB](https://www.arangodb.com/) client driver written in [scala](http://scala-lang.org/). It uses [akka](http://akka.io/) and [akka-http](http://doc.akka.io/docs/akka-http/current/scala.html) for async and reactive features. 

# API coverage

`arangodb-scala-driver` implements [Arangodb 3.1 HTTP API specifications](https://docs.arangodb.com/3.1/HTTP/index.html). In details, `arangodb-scala-driver` provides implementation calls for the following API:
* Administration :
  * `GET` `/_admin/database/target-version`
  * `GET` `/_api/version`
* Database:
  * `GET` `/_api/database`
  * `POST` `/_api/database`
  * `GET` `/_api/database/current`
  * `GET` `/_api/database/user`
  * `DELETE` `/_api/database/{database-name}`
* Collection:
  * `GET` `/_api/collection`
  * `POST` `/_api/collection`
  * `DELETE` `/_api/collection/{collection-name}`
  * `GET` `/_api/collection/{collection-name}`
  * `GET` `/_api/collection/{collection-name}/checksum`
  * `GET` `/_api/collection/{collection-name}/count`
  * `GET` `/_api/collection/{collection-name}/figures`
  * `PUT` `/_api/collection/{collection-name}/load`
  * `GET` `/_api/collection/{collection-name}/properties`
  * `PUT` `/_api/collection/{collection-name}/properties`
  * `PUT` `/_api/collection/{collection-name}/rename`
  * `GET` `/_api/collection/{collection-name}/revision`
  * `PUT` `/_api/collection/{collection-name}/rotate`
  * `PUT` `/_api/collection/{collection-name}/truncate`
  * `PUT` `/_api/collection/{collection-name}/unload`
* Document:
  * `GET` `/_api/document/{document-handle}`
  * `HEAD` `/_api/document/{document-handle}`
  * `PUT` `/_api/simple/all-keys`
  * `POST` `/_api/document/{collection}`
  * `PUT` `/_api/document/{document-handle}`
  * `PUT` `/_api/document/{collection}` 

# Usage

## Low-level API

The `ArangoDriver` class provides direct access to the HTTP API. 

## High-level API

To be done. See `ArangoDatabase`.
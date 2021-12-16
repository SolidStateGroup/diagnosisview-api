# Diagnosis View Api

Aggregates data from multiple providers and provides diagnosis data via an api.

## Authentication

All authentication is handled using an inbuilt login service, which will create a token for a user.
Java Web Tokens (JWT) are created by the API on successful authentication with ADFS. These must be
sent with all authenticated requests as a header "X-Auth-Token".

## Authorisation

Currently there is only one user in the application who has access to the complete project summary.

## Data

Data is created using an admin service which will create a unique set of projects

## Environment Variables

* FRONT_END_URL - The redirect location on successful SAML authentication, points to the React front
  end.
* JWT_ENABLED - Enables request filtering for authorisation, turns security on if set to "true".
* SERVER_PORT - Sets the listening port for the API, set to "5000" if running on Elastic Beanstalk.
* SPRING_DATASOURCE_PASSWORD - Password for data store, currently PostgreSQL in RDS.
* SPRING_DATASOURCE_URL - JDBC URL for data store, e.g. jdbc:postgresql://instance:5432/database.
* SPRING_DATASOURCE_USERNAME - Username for data store.
* NHS_CHOICES_API_KEY
* RECAPTCHA_SITEKEY
* RECAPTCHA_SITESECRET
* CHARGEBEE_API_KEY
* CHARGEBEE_SITE

## Create database

We are using PostgreSQL for storing the data. You would need local instance running locally before
running the project. Project using flyway to created database schema and apply any changes. Please
make sure database exist before deploying application.

```sh 
Install it local 
```

once installed check the version

```sh  
postgres -V
```

to start PostgreSQL server if not started automatically.

```sh 
brew services start postgresql 
```

Using `psql` terminal based tool to to carry admin function on PostgreSQL, you can use other
alternative tools.

```sh 
psql postgres
```

Check what user currently installed

```sh 
\du
```

Create local users and grant CREATEDB role

```sh 
CREATE ROLE diagnosisview WITH LOGIN PASSWORD 'diagnosisview';
ALTER ROLE diagnosisview CREATEDB;
\q # quits
```

Login with newly created user and create databases

```sh 
psql postgres -U diagnosisview
CREATE DATABASE diagnosisview;
GRANT ALL PRIVILEGES ON DATABASE diagnosisview TO diagnosisview;
\q # quits
```

Some PostgreSQL commands

```sh 
\list  		# lists all databases in Postgres
\connect  	# connect to a specific database
\dt  		# list the tables in the currently connected database
```

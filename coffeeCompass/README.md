## Coffee compass

This is Coffee compass web application - application for creating, searching, updating database of the Coffee sites - i.e. places where you can get a coffee to go - especially coffee machines, but also bistros, cafes and similar.
The main purpose is to allow user to find the nearest coffee sites according user's actual position. User will be allowed to create/update/delete a new coffee site, too.

Backend of the project is based on Spring Boot and Hibernate. Frontend is expected to be in Angular and in Android for mobile devices. Currently, the Front-end is developed in Thymeleaf for prototyping purposes.
Newest version is running on www.coffeecompass.cz

## Getting Started

Basic steps to make app. runnig: 

- import like git project from GitHub into your IDE.
- create DB and configure DB connection.
- run on local IDE as Java or Spring Boot application

### Prerequisites

Java 8 installed, Maven, created PostgreSQL DB, Thymeleaf

### Installing

1] Cloning project from GitHub to your local Git repository:

Open Git Repositories View. Window -> Show View -> Other -> Git -> Git Repositories
In that new View click on "Clone a Git repository", fill in URI: https://github.com/MichalVaclavek/CoffeeCompass and click "Next". If not selected, select "master" branch, and click "Next". Select directory where the local repository will be located/cloned to, click "Finish". 

2] Importing project from local Git repository to Eclipse:
Go to File -> Import -> Maven -> Existing Maven Project -> Next, select Directory where you cloned the project (step 1). Click "Finish" and your project is imported into Eclipse and ready to run.

After importing/creating project from GitHub, a DB must be created. If the PostgreSQL DB is used, create DB/schema named "coffeecompass" and create tables using /scripts/postgres_script.sql file included in the source code.

## Running the tests

//TODO

## Deployment

//TODO

## Built With

* [IntelliJ IDEA, Eclipse] - IDE
* [Maven](https://maven.apache.org/) - Dependency Management
* [SpringBoot]
* [Thymeleaf]
* [Hibernate]
* [PostgreSQL]

## Authors

* **Michal Václavek**


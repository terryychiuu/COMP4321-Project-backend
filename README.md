# COMP4321-Project-backend
The project is the course project for the HKUST COMP 4321 course. It is a web search engine based on vector space model.

##Installation Guide

Please be noted that this part of the installation guide will only guide you through the process of installing the backend web server. Please refer to the README.md from the UI repository (folder) for instructions setting up the frontend UI.

###Prerequisite
####Java
The project runs with Java 18.

### IDE
The project is using IntelliJ IDEA. You can go to https://www.jetbrains.com/idea/, and install it.

###Marven
Marven is used for dependency management for this project. 

## Deploying the Web Scrapper (spider)
1. open `Main.java` in comp4321-Project
2. initially, `./src/database.db` and `./src/database.lg` contain the indexed 30 pages starting from http://www.cse.ust.hk/, find them and delete them before reproducing the result
3. run Main.java main

You can find the log in the console output

##Deploying the Search Engine
1. open `SearchEngineApplication.java` under `src/main/java/net/searchengine/searchengine/`
2. run SearchEngineApplication.java main

##Remark
You can set the path, including `DATABASE_PATH`, `RESULT_PATH`, `PHRASE_2_RESULT_PATH` and `FRONTEND_PATH` under the file in `src/main/java/net/searchengine/searchengine/uitil/Constants.java`

### Phase 1 Test Program
1. To retrieve the result from the db, open Phase1Test.java and right click "run"

The result will output to spider_result.txt 

